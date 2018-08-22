package population.component.UIComponents;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.ArrayList;

public class TextFieldTableCell<S, T> extends TableCell<S, T> {
    /** Text field for editing */
    protected final TextField textField = new TextField();

    /** Converter for converting the text in the text field to the user type, and vice-versa: */
    protected final StringConverter<T> converter;
    /** errors while trying convert textField value to T */
    protected final ArrayList<String> parseErrors = new ArrayList<>();

    /**  textField KeyPressed Handler */
    protected final EventHandler<KeyEvent> textFieldKeyEventHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
            if (event.getCode() == KeyCode.ESCAPE) {
                textField.setText(converter.toString(getItem()));
                cancelEdit();
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                getTableView().getSelectionModel().selectAboveCell();
                commitEdit(getItemFromTextField());
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                getTableView().getSelectionModel().selectBelowCell();
                commitEdit(getItemFromTextField());
                event.consume();
            }
        }
    };


    public TextFieldTableCell(StringConverter<T> converter) {
        this.converter = converter;
        initTextField();

        itemProperty().addListener((obx, oldItem, newItem) -> {
            setText(converter.toString(newItem));
        });

        setGraphic(textField);
        setContentDisplay(ContentDisplay.TEXT_ONLY);

        // start editing on mouse clicked
//        this.setOnMouseClicked(event -> {
//            this.getTableView().edit(this.getIndex(), this.getTableColumn());
//        });
    }


    /**
     * init textField
     * @return TextField
     */
    protected TextField initTextField() {
        textField.addEventFilter(KeyEvent.KEY_PRESSED, textFieldKeyEventHandler);
        // enter pressed
        textField.setOnAction(evt -> {
            commitEdit(getItemFromTextField());
        });

        // commit on lose focus
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEdit(getItemFromTextField());
            }
        });

        return textField;
    }


    /**
     *
     * @return T value from textField. If if input invalid, return old value and add error message to {@link TextFieldTableCell#parseErrors}
     */
    protected T getItemFromTextField() {
        parseErrors.clear();
        T item;
        try {
            item = this.converter.fromString(textField.getText());
        } catch (Exception e) {
            this.parseErrors.add(e.getMessage());
            return getItem();
        }
        return item;
    }


    /*********************************
     *
     *      public interface
     *
     *********************************/


    // set the text of the text field and display the graphic
    @Override
    public void startEdit() {
        super.startEdit();
        textField.setText(converter.toString(getItem()));
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
    }


    // revert to text display
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    // commits the edit. Update property if possible and revert to text display
    @Override
    public void commitEdit(T item) {
        // This block is necessary to support commit on losing focus, because the baked-in mechanism
        // sets our editing state to false before we can intercept the loss of focus.
        // The default commitEdit(...) method simply bails if we are not editing...
        if (!isEditing() && item != null && !item.equals(getItem())) {
            TableView<S> table = getTableView();
            if (table != null) {
                TableColumn<S, T> column = getTableColumn();
                TableColumn.CellEditEvent<S, T> event = new TableColumn.CellEditEvent<>(
                        table,
                        new TablePosition<S,T>(table, getIndex(), column),
                        TableColumn.editCommitEvent(),
                        item
                );
                Event.fireEvent(column, event);
            }
        }

        super.commitEdit(item);

        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}
