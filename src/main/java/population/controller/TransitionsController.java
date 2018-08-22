package population.controller;

import javafx.scene.Node;
import javafx.scene.control.TableRow;
import population.App;
import population.component.UIComponents.TextFieldTableCell;
import population.controller.base.AbstractController;
import population.model.StateModel.State;
import population.model.TransitionModel.*;
import population.model.TransitionType;
import population.util.Converter;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TransitionsController extends AbstractController {
    @FXML
    private TableView<TransitionTableRowItem> transitionsTable;
    @FXML
    private TableColumn<TransitionTableRowItem, Integer> idColumn;
    @FXML
    private TableColumn<TransitionTableRowItem, Double> probabilityColumn;
    @FXML
    private TableColumn<TransitionTableRowItem, Number> typeColumn;
    @FXML
    private TableColumn<TransitionTableRowItem, String> blockColumn;
    @FXML
    private Button addTransitionButton;
    @FXML
    private Button removeTransitionButton;
    @FXML
    private Button addTransitionExtensionButton;


    /** states count shown in one table row */
    private int statesInTransitionColumnsCount = 3;
    private TransitionTableRowItemObservableList transitions;
    private ObservableList<Transition> model;
    private final ObservableList<State> statesList = FXCollections.observableArrayList();



    /**************************************************
     *
     *                initialization
     *
     *************************************************/

    @Override
    public void initialize() {
        this.model = App.getTransitions();
        this.initTransitions();
        this.initStatesList();
        this.initTable();
        this.initButtons();
    }


    private void initTransitions() {
        transitions = new TransitionTableRowItemObservableList(model, statesInTransitionColumnsCount);
        transitions.addListener((ListChangeListener<? super TransitionTableRowItem>) c -> this.updateTableRowClassList());
    }


    private void initStatesList() {
        // add external state
        State external = new State();
        external.setId(State.EXTERNAL_ID);
        external.setName(App.getString("state_external"));
        external.setAlias(App.getString("state_external"));
        statesList.add(external);

        // listen for changes in State Table
        App.getStates().addListener((ListChangeListener<State>) c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        statesList.set(i, c.getList().get(i));
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<State> copy = new ArrayList<>(statesList.subList(from, to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        statesList.set(newIndex, copy.get(oldIndex - from));
                    }

                } else if (c.wasAdded()) {
                    int i = 0;
                    for (State o: c.getAddedSubList()) {
                        statesList.add(c.getFrom() + i, o);
                        i++;
                    }

                } else if (c.wasRemoved()) {
                    statesList.remove(c.getFrom(), c.getFrom() + c.getRemovedSize());
                }
            }
        });
    }


    private void initTable() {
        // disable column dragging
//        transitionsTable.skinProperty().addListener((a, b, newSkin) -> {
//            // at this point transitionTable is rendered and we can get its Header
//            TableHeaderRow header = ((TableViewSkinBase)newSkin).getTableHeaderRow();
//            header.setMouseTransparent(true);
//        });

        transitionsTable.setItems(transitions.getItems());

        this.initIdColumn();
        this.initProbabilityColumn();
        this.initTypeColumn();
//        this.initBlockColumn();
        this.initStateColumns();
    }


    private void initIdColumn() {
        // hide numbers for transition extensions
        idColumn.setCellValueFactory(param -> {
            TransitionTableRowItem item = param.getValue();
            return item.isExtension() ? null : item.numberProperty().asObject();
        });
    }

    private void initProbabilityColumn() {
        // set probability editable only for transition Head
        probabilityColumn.setCellFactory(list -> new TextFieldTableCell<TransitionTableRowItem, Double>(Converter.DOUBLE_STRING_CONVERTER) {
            @Override
            public void startEdit() {
                if (((TransitionTableRowItem)this.getTableRow().getItem()).isExtension()) {
                    return;
                }
                super.startEdit();
            }
        });
        // and hide value for extensions
        probabilityColumn.setCellValueFactory(param -> {
            TransitionTableRowItem item = param.getValue();
            return item.isExtension() ? null : item.probabilityProperty().asObject();
        });
    }

    private void initTypeColumn() {
        // set type editable only for transition Head
        typeColumn.setCellFactory(list -> new ComboBoxTableCell<TransitionTableRowItem, Number>(Converter.TRANSITION_TYPE_STRING_CONVERTER, TransitionType.TYPES) {
            @Override
            public void startEdit() {
                if (((TransitionTableRowItem)this.getTableRow().getItem()).isExtension()) {
                    return;
                }
                super.startEdit();
            }
        });
        // and hide value for extensions
        typeColumn.setCellValueFactory(param -> {
            TransitionTableRowItem item = param.getValue();
            return item.isExtension() ? null : item.typeProperty();
        });
    }

    private void initBlockColumn() {
        blockColumn.setCellFactory(list -> new TextFieldTableCell<>(new DefaultStringConverter()));
    }

    private void initStateColumns() {
        for (int i = 0; i < this.statesInTransitionColumnsCount; i++) {
            final int index = i;

            TableColumn<TransitionTableRowItem, State> stateColumn = new TableColumn<>(this.getString("transition_state"));
            stateColumn.setCellValueFactory(param -> {
                if (index >= param.getValue().getStates().size()) {
                    return null;
                }
                return param.getValue().getStates().get(index).stateProperty();
            });
            stateColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Converter.STATE_STRING_CONVERTER, statesList));
            stateColumn.setSortable(false);
            stateColumn.getStyleClass().add("transition-state");

            TableColumn<TransitionTableRowItem, Double> inColumn = new TableColumn<>(this.getString("transition_state_in"));
            inColumn.setCellValueFactory(param -> {
                if (index >= param.getValue().getStates().size()) {
                    return null;
                }
                StateInTransition state = param.getValue().getStates().get(index);
                if (state.getState() == null || state.getState().getId() == State.EXTERNAL_ID) {
                    return null;
                }
                return param.getValue().getStates().get(index).inProperty().asObject();
            });
            inColumn.setCellFactory(list -> new TextFieldTableCell<>(Converter.DOUBLE_STRING_CONVERTER));
            inColumn.setSortable(false);
            inColumn.getStyleClass().add("transition-state-in");

            TableColumn<TransitionTableRowItem, Double> outColumn = new TableColumn<>(this.getString("transition_state_out"));
            outColumn.setCellValueFactory(param -> {
                if (index >= param.getValue().getStates().size()) {
                    return null;
                }
                return param.getValue().getStates().get(index).outProperty().asObject();
            });
            outColumn.setCellFactory(list -> new TextFieldTableCell<>(Converter.DOUBLE_STRING_CONVERTER));
            outColumn.setSortable(false);
            outColumn.getStyleClass().add("transition-state-out");

            TableColumn<TransitionTableRowItem, Integer> delayColumn = new TableColumn<>(this.getString("transition_state_delay"));
            delayColumn.setCellValueFactory(param -> {
                if (index >= param.getValue().getStates().size()) {
                    return null;
                }
                return param.getValue().getStates().get(index).delayProperty().asObject();
            });
            delayColumn.setCellFactory(list -> new TextFieldTableCell<>(new IntegerStringConverter()));
            delayColumn.setSortable(false);
            delayColumn.getStyleClass().add("transition-state-delay");

            TableColumn<TransitionTableRowItem, Number> modeColumn = new TableColumn<>(this.getString("transition_mode"));
            modeColumn.setCellValueFactory(param -> {
                if (index >= param.getValue().getStates().size()) {
                    return null;
                }
                return param.getValue().getStates().get(index).modeProperty();
            });
            modeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Converter.STATE_IN_TRANSITION_MODE_STRING_CONVERTER, StateMode.MODES));
            modeColumn.setSortable(false);
            modeColumn.getStyleClass().add("transition-state-mode");

            final List<TableColumn<TransitionTableRowItem, ?>> columns = Arrays.asList(
                    stateColumn,
                    inColumn,
                    outColumn,
                    delayColumn,
                    modeColumn
            );
            for (int j = 0; j < columns.size(); j++) {
                transitionsTable.getColumns().add(3 + j + index * columns.size(), columns.get(j));
            }

        }
    }


    private void initButtons() {
        EventHandler<MouseEvent> eventHandler = event -> {
            // request focus back to table
            transitionsTable.requestFocus();
            // do that to remove focus from first row
            transitionsTable.getFocusModel().focus(transitionsTable.getSelectionModel().getSelectedIndex());
        };
        addTransitionButton.setOnMouseClicked(eventHandler);
        addTransitionExtensionButton.setOnMouseClicked(eventHandler);
        removeTransitionButton.setOnMouseClicked(eventHandler);
    }


    /*************************************************
     *
     *              FXML Bindings
     *
     *************************************************/

    /**
     * update css classes for table rows
     * append .new-transition class for rows in which transition starts
     * append .with-data class for rows with associated transition
     */
    protected void updateTableRowClassList() {
        int rowIndex = 0;
        List<TransitionTableRowItem> items = transitionsTable.getItems();

        for (Node node: transitionsTable.lookupAll("TableRow")) {
            if (node instanceof TableRow) {
                TableRow row = (TableRow) node;

                if (rowIndex < items.size()) {
                    row.getStyleClass().add("with-data");
                    if (!items.get(rowIndex).isExtension()) {
                        row.getStyleClass().add("new-transition");
                    }
                } else {
                    row.getStyleClass().removeAll("new-transition", "with-data");
                }
            }

            rowIndex++;
        }
    }


    @FXML
    public void addTransition() {
        TransitionTableRowItem transition = new TransitionTableRowItem(false, statesInTransitionColumnsCount);
        int row = transitionsTable.getSelectionModel().getSelectedIndex();
        if (row < 0) {
            row = transitions.size() - 1;
        }
        transitions.add(row + 1, transition);
        transitionsTable.getSelectionModel().select(this.transitions.indexOf(transition));
    }

    @FXML
    public void removeTransition() {
        int row = transitionsTable.getSelectionModel().getSelectedIndex();
        if (row > -1) {
            transitions.remove(row);
        }
    }

    /**
     * add row which continue previous transition
     */
    @FXML
    public void addTransitionExtension() {
        int row = transitionsTable.getSelectionModel().getSelectedIndex();
        if (row < 0) {
            return;
        }

        TransitionTableRowItem transition = new TransitionTableRowItem(true, statesInTransitionColumnsCount);
        transitions.add(row + 1, transition);
        transitionsTable.getSelectionModel().select(this.transitions.indexOf(transition));
    }
//
//    @FXML
//    public void moveStateUp() {
//        int row = statesTable.getSelectionModel().getSelectedIndex();
//        if (row > 0) {
//            Collections.swap(states, row, row - 1);
//        }
//    }
//
//    @FXML
//    public void moveStateDown() {
//        int row = statesTable.getSelectionModel().getSelectedIndex();
//        if (row > -1 && row < states.size() - 1) {
//            Collections.swap(states, row, row + 1);
//        }
//    }

    /*************************************************
     *
     *              Public Methods
     *
     *************************************************/

    public ObservableList<Transition> getTransitions() {
        return this.model;
    }
}