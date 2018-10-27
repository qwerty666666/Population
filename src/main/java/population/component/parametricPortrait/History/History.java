package population.component.parametricPortrait.History;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import population.component.UIComponents.DraggableVerticalScrollPane;
import population.component.parametricPortrait.ParametricPortraitNode;


/**
 * History block
 */
public class History extends DraggableVerticalScrollPane {
    /** selected item in history */
    private ObjectProperty<HistoryItem> selectedItem = new SimpleObjectProperty<>(null);


    public History() {
        super();
        content.setSpacing(10);
        this.setMinWidth(240);
    }


    /**
     * @return currently selected history item
     */
    public HistoryItem getSelectedItem() {
        return this.selectedItem.get();
    }


    /**
     * Set selected item
     *
     * @param item selected item
     */
    public void select(HistoryItem item) {
        if (this.getSelectedItem() != null) {
            this.getSelectedItem().getThumbnail().getStyleClass().remove("active");
        }

        item.getThumbnail().getStyleClass().add("active");
        this.selectedItem.set(item);
    }

    /**
     * Select item at index
     */
    public void select(int ind) {
        this.select((HistoryItem) content.getChildren()
            .filtered(x -> x instanceof HistoryItem)
            .get(ind)
        );
    }


    /**
     * Add parametric portrait to history
     *
     * @param parametricPortrait parametric portrait to push
     */
    public void push(ParametricPortraitNode parametricPortrait) {
        HistoryItem item = new HistoryItem(this, parametricPortrait);
        addItem(0, item);

        // apply drag ability to item thumbnail
        item.getThumbnail().setOnDragDetected(item.getOnDragDetected());

        // on click select this history item
        item.getThumbnail().setOnMouseClicked(event -> {
            this.select(item);
            event.consume();
        });
    }


    /**
     * Remove history item from history
     *
     * @param item historyItem to remove
     */
    public void remove(HistoryItem item) {
        content.getChildren().remove(item);
    }


    /**
     * Remove all items from history
     */
    public void clear() {
        content.getChildren().clear();
    }


    /**
     * Update history item thumbnail
     *
     * @param index item index in history
     */
    public void updateItem(int index) {
        HistoryItem item = (HistoryItem) content.getChildren()
            .filtered(x -> x instanceof HistoryItem)
            .get(index);
        item.updateNailImage();
    }


    /**
     * You can observe this value to detect when selection changed
     */
    public ObjectProperty<HistoryItem> selectedItemProperty() {
        return this.selectedItem;
    }
}