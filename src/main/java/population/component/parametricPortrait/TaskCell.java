package population.component.parametricPortrait;

import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import population.App;
import population.model.ParametricPortrait.ParametricPortrait;
import population.model.ParametricPortrait.ParametricPortraitCalculationResult;
import population.model.ParametricPortrait.PortraitProperties;
import population.model.StateModel.State;

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

    private ParametricPortrait portrait;
    /** row in portrait */
    private int row;
    /** col in portrait */
    private int col;


    TaskCell(ParametricPortrait portrait, int row, int col) {
        this.portrait = portrait;
        this.row = row;
        this.col = col;
        this.numCols = this.numRows = (int)Math.ceil(Math.sqrt(portrait.getProperties().getShownStateList().size()));

        setGrid();

        this.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        // open cell task on mouse clicked
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                PortraitProperties portraitProperties = App.getParametricPortraitController().getPortraitProperties().clone();

                App.clearCalculationEnvironment();
                App.setTask(this.portrait.getTask(row, col));

                App.getParametricPortraitController().setPortraitPropertiesValues(portraitProperties);

                App.openChartTab();
                App.calculateTask();
            }
        });
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
                Pane rect = new Pane();
                rect.setBorder(new Border(new BorderStroke(Color.GRAY,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT, new Insets(-0))));
                add(rect, col, row);
            }
    }


    /**
     * get color by state
     * @param state state
     * @return color from stateColorMap if specified, new color from default colors otherwise
     */
    private Color getStateColor(State state) {
        return this.portrait.getProperties().getColor(state);
    }


    /**
     *
     * @return max needed square size to fill all aliveStates
     */
    public int getRequestedSize() {
        ParametricPortraitCalculationResult result = this.portrait.getCalculationResult(this.row, this.col);
        if (result != null && result.getResult().size() > 0) {
            int size = 0, mul = 1;
            for (List<State> states : result.getResult()) {
                size += states.size() * mul;
                mul++;
            }
            return (int) Math.ceil(Math.sqrt(size));
        }
        return 1;
    }


    /**
     * fill background in all colors proportionality to state volume
     */
    private void fillCyclic(ParametricPortraitCalculationResult result) {
        List<List<State>> aliveStates = result.getResult();

        if (aliveStates.size() == 0) {
            return;
        }

        if (aliveStates.size() == 1 && aliveStates.get(0).size() == 1) {
            fillStable(result);
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
    private void fillStable(ParametricPortraitCalculationResult result) {
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
        List<List<State>> aliveStates = result.getResult();
        // FILL BY PROPORTION
        if (aliveStates == null || aliveStates.size() == 0) {
            return;
        }

//        if (aliveStates.size() == 1 && aliveStates.get(0).size() == statesListShownOnParametricPortrait.size()) {
//            // show transparent square if all states has the same value
//            return;
//        }

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
    public void fill() {
        ParametricPortraitCalculationResult result = this.portrait.getCalculationResult(this.row, this.col);
        if (result == null) {
            return;
        }

        switch (result.getType()) {
            case STABLE:
                fillStable(result);
                break;
            case TRENDS:
                fillStable(result);
                break;
            case INTEGRAL_SUM:
                fillCyclic(result);
                break;
            default:
                throw new RuntimeException("Unexpected parametric portrait CalculationResultType");
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
