package population.model.TransitionModel;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * represents each row in transition table.
 */
public class TransitionTableRowItem extends Transition {
    /**
     * number in transition table
     */
    protected final IntegerProperty number = new SimpleIntegerProperty();
    /**
     * is item extension of previous row
     */
    protected final BooleanProperty isExtension = new SimpleBooleanProperty();
    /**
     * {@link StateInTransition} count in one item
     */
    protected final IntegerProperty statesCount = new SimpleIntegerProperty();


    public TransitionTableRowItem(boolean isExtension, int statesCount) {
        this.isExtension.setValue(isExtension);
        this.statesCount.setValue(statesCount);
        for (int i = 0; i < statesCount; i++) {
            this.states.add(new StateInTransition());
        }
    }

    public IntegerProperty numberProperty() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number.setValue(number);
    }

    public int getNumber() {
        return this.number.getValue();
    }

    public boolean isExtension() {
        return this.isExtension.getValue();
    }

    public int getStatesCount() {
        return this.statesCount.getValue();
    }

    public void setId(int id) {
        this.id = id;
    }
}
