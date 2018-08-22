package population.model.TransitionModel;


import population.model.Exception.Transition.TransitionStartIndexNotFound;
import javafx.collections.*;


import java.util.ArrayList;
import java.util.List;


/**
 * represents list of rows in transition table.
 * It preserves transition numeration in table on List actions.
 *
 * And it stand as adapter between original model and table representation, in purpose to provide binding properties
 * from multiple rows to single model instance
 */
public class TransitionTableRowItemObservableList extends ModifiableObservableListBase<TransitionTableRowItem> {
    /**
     * list of rows in transition table.
     * In table have the next structure:
     *  --------------------------------------
     *  - number | transition (Head)           |
     *             transition extension        |   <- one item in {@link TransitionTableRowItemObservableList#model}
     *             ...                         |
     *             transition extension (Tail) |
     *  --------------------------------------
     *  - next transition                      |
     *  --------------------------------------
     *
     *  transition Head should contain main info, extensions only stand for {@link StateInTransition}
     */
    protected final ObservableList<TransitionTableRowItem> items = FXCollections.observableArrayList();
    /**
     * original model constructed from {@link TransitionTableRowItemObservableList#items}
     * each item is transition Head from items (points to the same object)
     */
    protected ObservableList<Transition> model;
    /**
     * states count displayed in row
     */
    protected int statesCount = 3;
    /**
     * if model change was caused by this, then doing nothing
     */
    protected boolean isModelAffected = false;


    public TransitionTableRowItemObservableList(ObservableList<Transition> model, int statesCount) {
        this.statesCount = statesCount;
        this.model = model;
        this.model.addListener(this.onModelChanged);
    }


    public ObservableList<TransitionTableRowItem> getItems() {
            return this.items;
    }

    /**
     * rebuild all this.items when model changed
     */
    protected ListChangeListener<Transition> onModelChanged = c -> {
        // TODO should do it more efficient way
        // if model change was caused by this, then doing nothing
        if (this.isModelAffected) {
            this.isModelAffected = false;
            return;
        }

        // fill items states from model
        this.items.clear();
        for (Transition transition: model) {
            int statesCount = transition.getStates().size();
            for (int i = 0; i < statesCount; i+= this.statesCount) {
                TransitionTableRowItem item = new TransitionTableRowItem(i != 0, this.statesCount);
                if (i == 0) {
                    item.setId(transition.getId());
                    item.setProbabilityProperty(transition.probabilityProperty());
                    item.setTypeProperty(transition.typeProperty());
                }
                for (int j = 0; j < this.statesCount && i + j < statesCount; j++) {
                    item.getStates().set(j, transition.getStates().get(i + j));
                }
                this.items.add(item);
            }
        }

        this.rebuildItemsNumbers();
    };


    /**
     * @param index row index
     * @return index in items where transition starts for given rowIndex
     */
    protected int findTransitionHeadIndex(int index) {
        if (index >= this.items.size()) {
            return -1;
        }
        while (index >= 0 && this.get(index).isExtension()) {
            index--;
        }
        return index;
    }


    /**
     * @param index row index
     * @return index in items where transition lasts for given rowIndex, or -1 if no found
     */
    protected int findTransitionTailIndex(int index) {
        if (index >= items.size()) {
            return -1;
        }
        while (++index < items.size() && items.get(index).isExtension());
        return index - 1;
    }


    /**
     *
     * @param index row index
     * @return Transition in {@link TransitionTableRowItemObservableList#model} which corresponds to given row index
     * or null
     */
    protected Transition getModelByIndex(int index) {
        int startIndex = this.findTransitionHeadIndex(index);
        if (startIndex < 0) {
            return null;
        }
        final int id = this.items.get(startIndex).getId();
        return this.model.stream()
                .filter(transition -> transition.getId() == id)
                .findFirst()
                .orElse(null);
    }


    /**
     * setup states ({@link StateInTransition}) to transition from all it's extension rows
     * @param index index of row
     */
    protected void updateTransitionStates(int index) {
        // find row where transition starts
        int startIndex = this.findTransitionHeadIndex(index - 1);

        // update transition states list by delete all previous states and adding new from this.items
        Transition model = this.getModelByIndex(startIndex);
        // need to remove all states after model.getStatesCount(), because first states point to original model
        model.getStates().clear();
        for (int i = startIndex; i <= this.findTransitionTailIndex(index); i++) {
            TransitionTableRowItem item = this.items.get(i);
            model.getStates().addAll(item.getStates());
        };
    }


    /**
     * rebuild transition numbers for all items.
     */
    protected void rebuildItemsNumbers() {
        int number = 0;
        for (TransitionTableRowItem item: items) {
            if (!item.isExtension()) {
                number++;
            }
            item.setNumber(number);
        }
    }


    /**
     *
     * @param index row
     * @param element transition
     */
    protected void addNewTransition(int index, TransitionTableRowItem element) {
        Transition clone = ((Transition)element).cloneWithPreserveProperties();

        this.isModelAffected = true;
        if (index == this.items.size()) {
            model.add(clone);
        } else {
            model.add(model.indexOf(this.getModelByIndex(index - 1)) + 1, clone);
        }

        if (index < items.size()) {
            index = this.findTransitionTailIndex(index - 1) + 1;
        }
        items.add(index, element);

        this.rebuildItemsNumbers();
    }


    /**
     *
     * @param index row index to insert
     * @param element transition
     */
    protected void addTransitionExtension(int index, TransitionTableRowItem element) {
        this.items.add(index, element);
        this.updateTransitionStates(index);
    }


    /**
     * remove transition with it's extensions
     * @param index index
     */
    protected void removeTransition(int index) {
        this.isModelAffected = true;
        this.model.remove(this.getModelByIndex(index));

        int head = this.findTransitionHeadIndex(index);
        int tail = this.findTransitionTailIndex(index);
        this.items.subList(head, tail + 1).clear();

        this.rebuildItemsNumbers();
    }


    /***********************************************
     *
     *              Override List
     *
     **********************************************/


    @Override
    public TransitionTableRowItem get(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    protected void doAdd(int index, TransitionTableRowItem element) {
        if (!element.isExtension()) {
            this.addNewTransition(index, element);
        } else {
            if (index != this.items.size() && this.findTransitionHeadIndex(index) < 0) {
                throw new TransitionStartIndexNotFound("Add transition extension allowed only to Head transition" + index);
            }
            this.addTransitionExtension(index, element);
        }
    }

    @Override
    protected TransitionTableRowItem doSet(int index, TransitionTableRowItem element) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    protected TransitionTableRowItem doRemove(int index) {
        TransitionTableRowItem cur = this.items.get(index);

        if (cur.isExtension()) {
            this.items.remove(index);
            this.updateTransitionStates(index);
        } else {
            this.removeTransition(index);
        }

        return cur;
    }
}
