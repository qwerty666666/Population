package population.controller;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import population.App;
import population.component.UIComponents.TextFieldTableCell;
import population.component.ui.TableUtils;
import population.controller.base.AbstractController;
import population.model.StateModel.State;
import population.model.StateModel.StateFactory;
import population.model.TransitionModel.*;
import population.model.TransitionType;
import population.util.Converter;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import population.util.Resources.StringResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


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


    /**
     * states count shown in one table row
     */
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
    }


    private void initStatesList() {
        statesList.add(new StateFactory().makeEmptyState());

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

        /*
         * update table styles when it's item list changed
         */
        transitionsTable.getItems().addListener(new ListChangeListener<TransitionTableRowItem>() {
            /*
             * we will lookup through the table to find actual TableRows with data,
             * so we should do it after table layout update.
             * We'll call it in GUI thread and to prevent redundant calls, we'll use called flag
             * which will be set to false every time item list changed and to true when GUI thread has updated
             */
            AtomicBoolean called = new AtomicBoolean(true);
            @Override
            public void onChanged(Change<? extends TransitionTableRowItem> c) {
                if (called.get()) {
                    called.set(false);
                    Platform.runLater(() -> {
                        called.set(true);
                        transitionsTable.layout();
                        updateTableRowClassList();
                    });
                } else {
                    called.set(false);
                }
            }
        });

        this.initIdColumn();
        this.initProbabilityColumn();
        this.initTypeColumn();
//        this.initBlockColumn();
        this.initStateColumns();
    }

    /**
     * id column
     */
    private void initIdColumn() {
        // hide numbers for transition extensions
        idColumn.setCellValueFactory(param -> {
            TransitionTableRowItem item = param.getValue();
            return item.isExtension() ? null : item.numberProperty().asObject();
        });

        // add tooltip to header
        TableUtils.setColumnHeaderTextWithTooltip(idColumn, StringResource.getBundle(), "Transitions.IdColumnHeader");
    }

    /**
     * probability column
     */
    private void initProbabilityColumn() {
        // set probability editable only for transition Head
        probabilityColumn.setCellFactory(list -> new TextFieldTableCell<TransitionTableRowItem, Double>(Converter.DOUBLE_STRING_CONVERTER) {
            @Override
            public void startEdit() {
                if (((TransitionTableRowItem) this.getTableRow().getItem()).isExtension()) {
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

        // add tooltip to header
        TableUtils.setColumnHeaderTextWithTooltip(probabilityColumn, StringResource.getBundle(), "Transitions.ProbabilityColumnHeader");
    }

    /**
     * transition type column
     */
    private void initTypeColumn() {
        // set type editable only for transition Head
        typeColumn.setCellFactory(list -> {
            ComboBoxTableCell<TransitionTableRowItem, Number> cell = new ComboBoxTableCell<TransitionTableRowItem, Number>(
                new Converter.TransitionTypeStringConverter(true), TransitionType.TYPES
            ) {
                @Override
                public void startEdit() {
                    if (((TransitionTableRowItem) this.getTableRow().getItem()).isExtension()) {
                        return;
                    }
                    super.startEdit();
                }
            };

            // set tooltip
            Tooltip tooltip = new Tooltip();
            cell.itemProperty().addListener(new ChangeListener<Number>() {
                StringConverter<Number> converter = new Converter.TransitionTypeStringConverter(false);
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    tooltip.setText(converter.toString(newValue));
                }
            });
            cell.setTooltip(tooltip);
            //TableUtils.hideCellTooltipInEmptyRow(cell);

            return cell;
        });

        // and hide value for extensions
        typeColumn.setCellValueFactory(param -> {
            TransitionTableRowItem item = param.getValue();
            return item.isExtension() ? null : item.typeProperty();
        });

        // add tooltip to header
        TableUtils.setColumnHeaderTextWithTooltip(typeColumn, StringResource.getBundle(), "Transitions.TransitionTypeColumnHeader");
    }


    /**
     *
     */
    private void initBlockColumn() {
        blockColumn.setCellFactory(list -> new TextFieldTableCell<>(new DefaultStringConverter()));
    }


    /**
     * initialize states columns (state, in, out, delay)
     */
    private void initStateColumns() {
        for (int index = 0; index < this.statesInTransitionColumnsCount; index++) {
            final List<TableColumn<TransitionTableRowItem, ?>> columns = Arrays.asList(
                this.getStateColumn(index),
                this.getInColumn(index),
                this.getOutColumn(index),
                this.getDelayColumn(index),
                this.getModeColumn(index)
            );
            for (int j = 0; j < columns.size(); j++) {
                transitionsTable.getColumns().add(3 + j + index * columns.size(), columns.get(j));
            }
        }
    }


    /**
     * Set tooltip to table cells which will contain "choose state" text every time
     * associated State will be empty, in other time default tooltip will be shown
     *
     * @param cell table cell wo which tooltip will be applied
     * @param stateIndex index of associated state in table row
     */
    private <T> void showChooseStateCellTooltip(TableCell<TransitionTableRowItem, T> cell, int stateIndex) {
        ObjectProperty<TransitionTableRowItem> rowItemProperty = cell.getTableRow().itemProperty();

        Tooltip tooltip = cell.getTooltip();
        Tooltip chooseStateTooltip = new Tooltip(StringResource.getString("Transitions.ChooseStateTooltip"));

        // set "choose state" text when state is empty
        ChangeListener<State> stateChangeListener = (observable, oldValue, newValue) -> {
            if (newValue.isEmptyState()) {
                cell.setTooltip(chooseStateTooltip);
            } else {
                cell.setTooltip(tooltip);
            }
        };

        // update listener binding every time row item changes
        rowItemProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.getStates().get(stateIndex).stateProperty().removeListener(stateChangeListener);
            }
            if (newValue != null) {
                newValue.getStates().get(stateIndex).stateProperty().addListener(stateChangeListener);
            }

            if (newValue != null) {
                State state = newValue.getStates().get(stateIndex).getState();
                if (state != null && !state.isEmptyState()) {
                    cell.setTooltip(chooseStateTooltip);
                }
            }
        });

        // set initial tooltip text
        TransitionTableRowItem rowItem = (TransitionTableRowItem) cell.getTableRow().getItem();
        if (rowItem != null) {
            State state = rowItem.getStates().get(stateIndex).getState();
            if (state != null && !state.isEmptyState()) {
                cell.setTooltip(chooseStateTooltip);
                rowItem.getStates().get(stateIndex).stateProperty().addListener(stateChangeListener);
            }
        }
    }


    /**
     * initialize cell tooltip to specific StateInTransition property features
     *
     * @param cell table cell
     * @param stateIndex associated State index in table row item
     */
    private <T> void initStateInTransitionPropertyTooltip(TableCell<TransitionTableRowItem, T> cell, int stateIndex) {
        // and show "choose state" text when associated State is empty
        showChooseStateCellTooltip(cell, stateIndex);

        // hide tooltip when row item is null
        TableUtils.hideCellTooltipInEmptyRow(cell);
    }


    /**
     * @param converter cell item to string converter
     * @param stateIndex index of associated State in table row
     * @param <T> cell item type
     * @return TextFieldTableCell specific to StateInTransition property
     */
    private <T> TableCell<TransitionTableRowItem, T> getStatePropertyTextFieldTableCell(StringConverter<T> converter, int stateIndex) {
        return new TextFieldTableCell<TransitionTableRowItem, T>(converter) {
            private boolean isTooltipInitialized = false;

            private void initTooltip() {
                initStateInTransitionPropertyTooltip(this, stateIndex);
            }

            @Override
            public void updateItem(T item, boolean empty) {
                // set tooltip there because at this point we can get row item property
                if (!this.isTooltipInitialized) {
                    this.initTooltip();
                    this.isTooltipInitialized = true;
                }
                super.updateItem(item, empty);
            }
        };
    }


    /**
     *
     * @param converter string converter for ComboBox items
     * @param tooltipConverter string converter for ComboBox items for tooltip
     * @param itemList list of items in ComboBox
     * @param stateIndex index of associated state in table row
     * @param <T> cell item type
     * @return ComboBoxTableCell specific to StateInTransition property
     */
    private <T> TableCell<TransitionTableRowItem, T> getStatePropertyComboBoxTableCell(
        StringConverter<T> converter, StringConverter<T> tooltipConverter, List<T> itemList, int stateIndex
    ) {
        return new ComboBoxTableCell<TransitionTableRowItem, T>(converter, FXCollections.observableList(itemList)) {
            private boolean isTooltipInitialized = false;
            private Tooltip defaultTooltip = new Tooltip();

            private void initTooltip() {
                // add tooltip
                this.setTooltip(this.defaultTooltip);
                this.defaultTooltip.setText(converter.toString(this.getItem()));

                // update tooltip text every time value changed
                this.itemProperty().addListener((observable, oldValue, newValue) -> {
                    this.defaultTooltip.setText(tooltipConverter.toString(newValue));
                });

                initStateInTransitionPropertyTooltip(this, stateIndex);
            }

            @Override
            public void updateItem(T item, boolean empty) {
                // set tooltip there because at this point we can get row item property
                if (!this.isTooltipInitialized) {
                    this.initTooltip();
                    this.isTooltipInitialized = true;
                }
                super.updateItem(item, empty);
            }
        };
    }


    /**
     * @param index index of StateInTransition in table row
     * @return state column
     */
    private TableColumn<TransitionTableRowItem, State> getStateColumn(int index) {
        TableColumn<TransitionTableRowItem, State> stateColumn = new TableColumn<>();
        TableUtils.setColumnHeaderTextWithTooltip(stateColumn, StringResource.getBundle(), "Transitions.StateColumnHeader");

        stateColumn.setCellValueFactory(param -> {
            if (index >= param.getValue().getStates().size()) {
                return null;
            }
            return param.getValue().getStates().get(index).stateProperty();
        });

        // show state's full name Tooltip
        stateColumn.setCellFactory(list -> {
            StringConverter converter = new Converter.StateStringConverter(true);
            StringConverter tooltipConverter = new Converter.StateStringConverter(false);
            return this.getStatePropertyComboBoxTableCell(converter, tooltipConverter, statesList, index);
        });

        // stylizing
        stateColumn.setSortable(false);
        stateColumn.setPrefWidth(60);
        stateColumn.getStyleClass().add("transition-state");

        // hide in, out and delay values on Empty State choose
        stateColumn.setOnEditCommit(event -> {
            State newValue = event.getNewValue();

            StateInTransition stateInTransition = event.getRowValue().getStates().get(index);
            stateInTransition.setState(newValue);

            if (newValue != null && newValue.isEmptyState()) {
                stateInTransition.setIn(0);
                stateInTransition.setOut(0);
                stateInTransition.setDelay(0);
                stateInTransition.setMode(StateMode.SIMPLE);
            }
        });

        return stateColumn;
    }


    /**
     * @param index index of StateInTransition in table row
     * @return in property column for state
     */
    private TableColumn<TransitionTableRowItem, Double> getInColumn(int index) {
        TableColumn<TransitionTableRowItem, Double> inColumn = new TableColumn<>();
        TableUtils.setColumnHeaderTextWithTooltip(inColumn, StringResource.getBundle(), "Transitions.StateInColumnHeader");

        inColumn.setCellValueFactory(param -> {
            if (index >= param.getValue().getStates().size()) {
                return null;
            }
            return param.getValue().getStates().get(index).inProperty().asObject();
        });

        inColumn.setCellFactory(list -> {
            Converter.HideDefaultValueDecoratorConverter<Double> converter = new Converter.HideDefaultValueDecoratorConverter<>(
                Converter.DOUBLE_STRING_CONVERTER, 0., Double::compare
            );
            return getStatePropertyTextFieldTableCell(converter, index);
        });

        inColumn.setSortable(false);
        inColumn.setPrefWidth(40);
        inColumn.getStyleClass().add("transition-state-in");

        return inColumn;
    }


    /**
     * @param index index of StateInTransition in table row
     * @return out property column for state
     */
    private TableColumn<TransitionTableRowItem, Double> getOutColumn(int index) {
        TableColumn<TransitionTableRowItem, Double> outColumn = new TableColumn<>();
        TableUtils.setColumnHeaderTextWithTooltip(outColumn, StringResource.getBundle(), "Transitions.StateOutColumnHeader");

        outColumn.setCellValueFactory(param -> {
            if (index >= param.getValue().getStates().size()) {
                return null;
            }
            return param.getValue().getStates().get(index).outProperty().asObject();
        });

        outColumn.setCellFactory(list -> {
            Converter.HideDefaultValueDecoratorConverter<Double> converter = new Converter.HideDefaultValueDecoratorConverter<>(
                Converter.DOUBLE_STRING_CONVERTER, 0., Double::compare
            );
            return getStatePropertyTextFieldTableCell(converter, index);
        });

        outColumn.setSortable(false);
        outColumn.setPrefWidth(40);
        outColumn.getStyleClass().add("transition-state-out");

        return outColumn;
    }


    /**
     * @param index index of StateInTransition in table row
     * @return delay property column for state
     */
    private TableColumn<TransitionTableRowItem, Integer> getDelayColumn(int index) {
        TableColumn<TransitionTableRowItem, Integer> delayColumn = new TableColumn<>();
        TableUtils.setColumnHeaderTextWithTooltip(delayColumn, StringResource.getBundle(), "Transitions.StateDelayColumnHeader");

        delayColumn.setCellValueFactory(param -> {
            if (index >= param.getValue().getStates().size()) {
                return null;
            }
            return param.getValue().getStates().get(index).delayProperty().asObject();
        });

        delayColumn.setCellFactory(list -> {
            Converter.HideDefaultValueDecoratorConverter<Integer> converter = new Converter.HideDefaultValueDecoratorConverter<>(
                new IntegerStringConverter(), 0, Integer::compare
            );
            return getStatePropertyTextFieldTableCell(converter, index);
        });

        delayColumn.setSortable(false);
        delayColumn.setPrefWidth(40);
        delayColumn.getStyleClass().add("transition-state-delay");

        return delayColumn;
    }


    /**
     * @param index index of StateInTransition in table row
     * @return mode property column for state
     */
    private TableColumn<TransitionTableRowItem, Integer> getModeColumn(int index) {
        TableColumn<TransitionTableRowItem, Integer> modeColumn = new TableColumn<>();
        TableUtils.setColumnHeaderTextWithTooltip(modeColumn, StringResource.getBundle(), "Transitions.StateModeColumnHeader");

        modeColumn.setCellValueFactory(param -> {
            if (index >= param.getValue().getStates().size()) {
                return null;
            }
            return param.getValue().getStates().get(index).modeProperty().asObject();
        });

        modeColumn.setCellFactory(list -> {
            StringConverter converter = new Converter.HideDefaultValueDecoratorConverter<>(
                new Converter.StateInTransitionModeStringConverter(true), StateMode.SIMPLE, Integer::compare
            );
            StringConverter tooltipConverter = new Converter.StateInTransitionModeStringConverter(false);
            return this.getStatePropertyComboBoxTableCell(converter, tooltipConverter, StateMode.MODES, index);
        });

        modeColumn.setSortable(false);
        modeColumn.setPrefWidth(60);
        modeColumn.getStyleClass().add("transition-state-mode");

        return modeColumn;
    }


    /**
     * initialize table buttons
     */
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