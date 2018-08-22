package population.controller;

import population.App;
import population.component.UIComponents.TextFieldTableCell;
import population.controller.base.AbstractController;
import population.model.Expression.ExpressionManager;
import population.model.StateModel.State;
import population.util.Converter;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.ArrayList;
import java.util.Collections;


public class StatesController extends AbstractController {
    @FXML
    private TableView<State> statesTable;
    @FXML
    private TableColumn<State, String> nameColumn;
    @FXML
    private TableColumn<State, String> aliasColumn;
    @FXML
    private TableColumn<State, Double> countColumn;
    @FXML
    private TableColumn<State, Double> delayColumn;
    @FXML
    private TableColumn<State, Double> delayedCountColumn;
    @FXML
    private TableColumn<State, ExpressionManager> growthRateColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button moveUpButton;
    @FXML
    private Button moveDownButton;


    /** bind table to States model */
    protected ObservableList<State> states;

    /** convert StateModel name */
    protected final StringConverter<String> NAME_CONVERTER = new StringConverter<String>() {
        @Override
        public String toString(String object) {
            if (object == null) return "";
            return object.isEmpty() ? getString("unnamed") : object;
        }
        @Override
        public String fromString(String string) {
            return string;
        }
    };


    /**************************************************
     *
     *                initialization
     *
     *************************************************/

    @Override
    public void initialize() {
        this.states = App.getStates();
        this.initTable();
        this.initButtons();
    }

    private void initTable() {
        statesTable.setItems(states);

        nameColumn.setCellFactory(list -> new TextFieldTableCell<>(NAME_CONVERTER));
        aliasColumn.setCellFactory(list -> new TextFieldTableCell<>(new DefaultStringConverter()));
        countColumn.setCellFactory(list -> new TextFieldTableCell<>(Converter.DOUBLE_STRING_CONVERTER));
//        delayColumn.setCellFactory(list -> new TextFieldTableCell<>(Converter.DOUBLE_STRING_CONVERTER));
//        delayedCountColumn.setCellFactory(list -> new TextFieldTableCell<>(Converter.DOUBLE_STRING_CONVERTER));
//        growthRateColumn.setCellFactory(list -> new TextFieldTableCell<>(Converter.EXPRESSION_STRING_CONVERTER));
    }

    private void initButtons() {
        EventHandler<MouseEvent> eventHandler = event -> {
            // request focus back to table
            statesTable.requestFocus();
            // do that to remove focus from first row
            statesTable.getFocusModel().focus(statesTable.getSelectionModel().getSelectedIndex());
        };
        addButton.setOnMouseClicked(eventHandler);
        removeButton.setOnMouseClicked(eventHandler);
        moveUpButton.setOnMouseClicked(eventHandler);
        moveDownButton.setOnMouseClicked(eventHandler);
    }


    /*************************************************
     *
     *              FXML Bindings
     *
     *************************************************/

    @FXML
    public void addState() {
        State state = new State();
        states.add(state);
        statesTable.getSelectionModel().select(states.size() - 1);
    }

    @FXML
    public void removeState() {
        int row = statesTable.getSelectionModel().getSelectedIndex();
        if (row > -1) {
            states.remove(row);
        }
    }

    @FXML
    public void moveStateUp() {
        int row = statesTable.getSelectionModel().getSelectedIndex();
        if (row > 0) {
            Collections.swap(states, row, row - 1);
        }
    }

    @FXML
    public void moveStateDown() {
        int row = statesTable.getSelectionModel().getSelectedIndex();
        if (row > -1 && row < states.size() - 1) {
            Collections.swap(states, row, row + 1);
        }
    }


    /*************************************************
     *
     *              Public Methods
     *
     *************************************************/

    public ObservableList<State> getStates() {
        return this.states;
    }
}