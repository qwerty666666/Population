package population.component.parametricPortrait;

import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import population.component.Calculator;
import population.component.TaskAnalyser;
import population.model.ParametricPortrait.ParametricPortraitCalculator;
import population.model.StateModel.State;
import population.model.Task;
import population.model.TaskV4;

import java.util.ArrayList;
import java.util.List;

/**
 * class represents view for each task
 */
public class TaskCell extends GridPane {
    /** min cells size of inner grid*/
    static final double MIN_INNER_CELL_SIZE = 10;
    /** max cells size of inner grid*/
    static final double MAX_INNER_CELL_SIZE = 30;
    /** own columns count */
    private int numCols;
    /** own rows count */
    private int numRows;
    /** alive states */
    private List<List<State>> aliveStates = new ArrayList<>();
    /** own task */
    private TaskV4 task;
    private ParametricPortraitCalculator calculator;
    private Type type = Type.STABLE;

    private List<State> shownStates;

    public enum Type {
        STABLE,
        CYCLIC
    }


    TaskCell(ParametricPortraitCalculator calculator) {
        this.calculator = calculator;
        this.shownStates = calculator.getAllowedStates();

        this.numCols = this.numRows = (int)Math.ceil(Math.sqrt(shownStates.size()));

        setGrid();

        this.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        this.setOnMouseClicked(event -> {
            // TODO
            /*if (task == null || primaryController.isCalculating())
                return;

            if (event.getButton() == MouseButton.SECONDARY) {
                // clear graphic and table
                primaryController.clearResultsTable();
                primaryController.clearResultsChart();
                // open graphic tab
                primaryController.mCalculationsTabPane.getSelectionModel().select(
                    primaryController.mResultChartTab);
                // save portrait properties selections because they losed when set new task to primaryController
                //Map<ComboBox, Integer> selections = primaryController.mParametricPortraitTabController.getSelectionModel();
                // need set task steps - 1 because of steps increase in PrimaryController validation
                Task task = this.getTask();
                task.setStepsCount(task.getStepsCount() - 1);
                primaryController.setTask(this.getTask());
                task.setStepsCount(task.getStepsCount() + 1);
                primaryController.calculateTask(task);
                //primaryController.mParametricPortraitTabController.setSelectionModel(selections);
            }*/
        });
    }


    /**
     * calculate task
     */
    void calculateTask(
        Calculator.ResultCallback resultCallback,
        Calculator.ProgressCallback progressCallback,
        Calculator.CompleteCallback<Double> completeCallback
    ) {
        // TODO
            /*final TaskAnalyser calculator = new TaskAnalyser(task);
            calculator.setStablePrecision(scale);
            calculator.buildGraph();
            calculator.setAnalysedStatesList(statesListShownOnParametricPortrait);
            this.calculator = calculator;

            Calculator.calculateTaskAnalyser(
                    calculator,
                    application.getThreadFactory(),
                    resultCallback, progressCallback,
                    (result) -> {
                        setAliveStates(calculator.getDominantsOrdered());
                        this.type = calculator.getCalculationFinishedReason() == TaskAnalyser.CalculationFinishedReason.TASK_STABLE ?
                                TaskCellType.STABLE :
                                TaskCellType.CYCLIC;
                        completeCallback.onComplete((Double)result);
                    }
            );*/


    }


    void setTask(Task task) {
        this.task = task;
    }


    Task getTask() {
        return this.task;
    }


    /**
     * set own grid depends on numRows numCols
     */
    private void setGrid() {
        getChildren().clear();
        getColumnConstraints().clear();
        for (int col = 0; col < numCols; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setFillWidth(true);
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / numCols);
            getColumnConstraints().add(cc);
        }

        getRowConstraints().clear();
        for (int row = 0; row < numRows; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setFillHeight(true);
            rc.setVgrow(Priority.ALWAYS);
            rc.setPercentHeight(100.0 / numRows);
            getRowConstraints().add(rc);
        }

        this.getChildren().clear();
        for (int col = 0; col < numCols; col++)
            for (int row = 0; row < numRows; row++) {
                switch (type) {
                    case CYCLIC:
                            /*Polygon polygon = new Polygon();
                            polygon.getPoints().addAll(
                                    0.0, 0.0,
                                    0.0, 30.0,
                                    30.0, 0.0
                            );
                            polygon.setFill(Color.BLACK);
                            add(polygon, col, row);
                            break;*/

                    default:
                        Pane rect = new Pane();
                        rect.setBorder(new Border(new BorderStroke(Color.GRAY,
                            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT, new Insets(-0))));
                        add(rect, col, row);
                }

            }
    }


    void setAliveStates(List<List<State>> aliveStates) {
        this.aliveStates = aliveStates;
    }


    /**
     * get color by state
     * @param state state
     * @return color from stateColorMap if specified, new color from default colors otherwise
     */
    private Color getStateColor(State state) {
        return null;
        // TODO
            /*Color color = null;
            StateSetting stateSetting = getStateSettingsById(state.getId());
            if (stateSetting != null)
                color = stateSetting.getColor();

            return color;*/
    }

    /**
     *
     * @return max needed square size to fill all aliveStates
     */
    public int getRequestedSize() {
        switch (type) {
                /*case STABLE:
                    return (int) Math.ceil(aliveStates.size());*/

            case CYCLIC:
            case STABLE: {
                int size = 0, mul = 1;
                for (List<State> states : aliveStates) {
                    size += states.size() * mul;
                    mul++;
                }
                return (int) Math.ceil(Math.sqrt(size));
            }

            default:
                return 0;
        }


    }

    public void setSquareSize(int size) {
        this.numCols = this.numRows = size;
        setGrid();
    }

    /**
     * fill background in all colors proportionality to state volume
     */
    private void fillCyclic() {
        if (aliveStates.size() == 0) {
            return;
        }

        if (aliveStates.size() == 1 && aliveStates.get(0).size() == 1) {
            fillStable();
            return;
        }

        int cell = numRows * numCols - 1;
        int size = (aliveStates.size() == 1) ? numRows * numCols : 1;

        for (int statesInd = 0; statesInd < aliveStates.size(); statesInd++) {
            List<State> states = aliveStates.get(statesInd);
            if (statesInd == aliveStates.size() - 1) {
                size = (cell + 1) / aliveStates.get(aliveStates.size() - 1).size();
            }

            for (int stateInd = 0; stateInd < states.size(); stateInd++) {
                State state = states.get(stateInd);
                LinearGradient lg = new LinearGradient(0, 0, 1, 1,
                    true, CycleMethod.NO_CYCLE,
                    new Stop(0.5, getStateColor(state)),
                    new Stop(0.5, Color.TRANSPARENT));
                Background bg = new Background(new BackgroundFill(lg, CornerRadii.EMPTY, Insets.EMPTY));

                for (int i = 0; i < size && cell >= 0; i++, cell--) {
                    int row = cell / numRows;
                    int col = cell - row * numRows;
                    getPane(col, row).setBackground(bg);
                }

                if (statesInd == aliveStates.size() - 1 && stateInd == states.size() - 2)
                    size = cell + 1;
            }
            size++;
        }
    }


    /**
     * fill background by colors of dominant states
     */
    private void fillStable() {
        // FILL REPEATED SQUARE
            /*List<StateModel> aliveStates = this.aliveStates.get(this.aliveStates.size() - 1);

            if (aliveStates.size() == 0 || aliveStates.size() == statesListShownOnParametricPortrait.size())
                return;

            List<Background> backgrounds = new ArrayList<>();
            for (StateModel state: aliveStates) {
                if (getStateSettingsById(state.getId()).getShow())
                    backgrounds.add(new Background(new BackgroundFill(getStateColor(state),
                            CornerRadii.EMPTY, Insets.EMPTY)));
            }

            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++)
                    if (aliveStates.size() == numCols) {
                        boolean isRowEven = ((numCols * row + col) / backgrounds.size()) % 2 == 0;
                        Background bg = isRowEven ?
                                backgrounds.get( (numCols * row + col) % backgrounds.size() ) :
                                backgrounds.get( backgrounds.size() - ((numCols * row + col) % backgrounds.size()) - 1 );
                        getPane(row, col).setBackground(bg);
                    } else {
                        getPane(row, col).setBackground(backgrounds.get((numCols * row + col) % backgrounds.size()));
                    }
            }*/

        // FILL BY PROPORTION
        if (aliveStates.size() == 0) {
            return;
        }

        if (aliveStates.size() == 1 && aliveStates.get(0).size() == statesListShownOnParametricPortrait.size()) {
            // show transparent square if all states has the same value
            return;
        }

        int cell = numRows * numCols - 1;
        int size = (aliveStates.size() == 1) ? numRows * numCols : 1;

        for (int statesInd = 0; statesInd < aliveStates.size(); statesInd++) {
            List<State> states = aliveStates.get(statesInd);
            if (statesInd == aliveStates.size() - 1) {
                size = (cell + 1) / aliveStates.get(aliveStates.size() - 1).size();
            }

            for (int stateInd = 0; stateInd < states.size(); stateInd++) {
                State state = states.get(stateInd);
                Background bg = new Background(new BackgroundFill(getStateColor(state),
                    CornerRadii.EMPTY, Insets.EMPTY));

                for (int i = 0; i < size && cell >= 0; i++, cell--) {
                    int row = cell / numRows;
                    int col = cell - row * numRows;
                    getPane(col, row).setBackground(bg);
                }

                if (statesInd == aliveStates.size() - 1 && stateInd == states.size() - 2)
                    size = cell + 1;
            }
            size++;
        }
    }


    /**
     * set background
     */
    void fill() {
        if (type == ParametricPortraitNode.TaskCellType.STABLE) {
            fillStable();
        } else {
            fillCyclic();
        }
    }


    /**
     * get child Pane node from row, col
     * @param row row
     * @param col column
     * @return child Node
     */
    private Pane getPane(int row, int col) {
        return (Pane)this.getChildren().get(numCols * row + col);
    }
}
