package population.controller.Calculation;

import population.controller.base.AbstractController;
import population.model.Calculator.*;
import population.model.StateModel.State;
import population.util.Event.EventManager;
import population.util.Utils;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.converter.FormatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class ResultTableController extends AbstractController {
    @FXML
    private TableView<ResultTableRow> resultTable;
    @FXML
    private TextField precisionTextField;

    protected List<Result> results = new ArrayList<>();

    protected int precision = 3;



    /**************************************************
     *
     *                initialization
     *
     *************************************************/

    @Override
    public void initialize() {
        precisionTextField.setText(String.valueOf(precision));
        precisionTextField.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) {
                applyPrecision();
            }
        });

        EventManager.addEventHandler(Calculator.FINISHED_EVENT, event -> {
            Platform.runLater(() -> {
                Calculator calculator = ((CalculationFinishedEvent)event).getCalculator();
                results.add(new Result(
                        calculator.getStatesCount(),
                        calculator.getStates(),
                        calculator.getStartPoint(),
                        calculator.getStepsCount()
                ));
                this.refreshTable();
            });
            return true;
        });
    }

    /*************************************************
     *
     *              FXML Bindings
     *
     *************************************************/

    @FXML
    public void clear() {
        resultTable.getItems().clear();
        resultTable.getColumns().clear();
        this.results.clear();
    }

    @FXML
    void applyPrecision() {
        try {
            this.precision = Integer.parseInt(precisionTextField.getText());

            ObservableList<TableColumn<ResultTableRow, ?>> columns = resultTable.getColumns();
            for (int i = 1; i < columns.size(); i++) {
                ((TableColumn<ResultTableRow, Double>)columns.get(i)).setCellFactory(getValueColumnCellFactory());
            }
        } catch (NumberFormatException e) {
            // TODO
        }
    }


    /*************************************************
     *
     *
     *
     *************************************************/


    /**
     * rebuild whole table by {@link ResultTableController#results}
     */
    protected void refreshTable() {
        resultTable.getItems().clear();
        resultTable.getColumns().clear();

        if (this.results.size() == 0) {
            return;
        }

        Integer selectedStep = getSelectedStep();

        // add columns
        resultTable.getColumns().add(getStepColumn());

        int index = 0;
        for (Result result: this.results) {
            for (State state: result.getStates()) {
                resultTable.getColumns().add(getValueColumn(state.getName(), index));
                index++;
            }
        }

        // find first and last step
        int first = Integer.MAX_VALUE;
        int last = Integer.MIN_VALUE;
        for (Result result: this.results) {
            int f = result.getStartPoint();
            int l = f + result.getStepsCount();
            first = Math.min(first, f);
            last = Math.max(last, l);
        }

        // build ResultTableRows
        ObservableList<ResultTableRow> rows = FXCollections.observableArrayList();
        for (int step = first; step < last; step++) {
            List<Double> statesCount = new ArrayList<>();
            for (Result result: this.results) {
                int f = result.getStartPoint();
                int l = f + result.getStepsCount();
                if (step >= f && step < l) {
                    statesCount.addAll(
                            Arrays.stream(result.getStatesCount()[step - f])
                                .boxed()
                                .collect(Collectors.toList())
                    );
                } else {
                    statesCount.addAll(Collections.nCopies(result.getStates().size(), null));
                }
            }
            rows.add(new ResultTableRow(step, statesCount));
        }

        resultTable.setItems(rows);

        if (selectedStep != null) {
            selectRowByStep(selectedStep);
        }
    }


    protected TableColumn<ResultTableRow, Integer> getStepColumn() {
        TableColumn<ResultTableRow, Integer> column = new TableColumn<>(getString("step"));
        column.setMinWidth(30);
        column.setSortable(false);
        column.setEditable(false);
        column.setCellFactory(list -> new TextFieldTableCell<>(new IntegerStringConverter() {
            @Override
            public String toString(Integer value) {
                return super.toString(value + 1);
            }
        }));
        column.setCellValueFactory(param -> param.getValue().stepProperty().asObject());
        return column;
    }

    protected TableColumn<ResultTableRow, Double> getValueColumn(String name, int index) {
        TableColumn<ResultTableRow, Double> column = new TableColumn<>(name);
        column.setMinWidth(50);
        column.setSortable(false);
        column.setEditable(false);
        column.setCellFactory(getValueColumnCellFactory());
        column.setCellValueFactory(param -> {
            DoubleProperty prop = param.getValue().getStatesCount().get(index);
            return prop != null ? prop.asObject() : null;
        });
        return column;
    }

    protected Callback<TableColumn<ResultTableRow, Double>, TableCell<ResultTableRow, Double>> getValueColumnCellFactory() {
        return list -> new TextFieldTableCell<>(new FormatStringConverter<>(
                new DecimalFormat(Utils.buildDecimalFormat(precision)))
        );
    }

    /**
     *
     * @return real step number
     */
    protected Integer getSelectedStep() {
        ResultTableRow selectedRow = resultTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            return null;
        }
        return selectedRow.getStep();
    }

    /**
     *
     * @param step real step number
     */
    private void selectRowByStep(int step) {
        ResultTableRow row = resultTable.getItems().stream()
                .filter(item -> item.getStep() == step)
                .findFirst()
                .orElse(null);
        resultTable.getSelectionModel().select(row);
        resultTable.scrollTo(row);
    }
}