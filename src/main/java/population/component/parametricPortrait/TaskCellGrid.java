package population.component.parametricPortrait;

import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import population.model.ParametricPortrait.ParametricPortrait;
import population.model.ParametricPortrait.PortraitProperties;
import population.model.ParametricPortrait.SimpleParametricPortraitCalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Grid of parametric portrait task cells
 */
public class TaskCellGrid extends GridPane {
    /** grid of TaskCell (numeration from upper left corner [row][col]) *//*
    private List<List<TaskCell>> taskCells = new ArrayList<>();
    *//** highlight rectangle on drag *//*
    private Pane overlay = new Pane();
    *//** start drag cell index *//*
    private int dragStartX = 0;
    *//** start drag cell index *//*
    private int dragStartY = 0;

    private ParametricPortrait portrait;

    private ParametricPortraitNode.SubareaSelectedCallback SubareaSelectedCallback = null;


    TaskCellGrid(ParametricPortrait portrait) {
        this.portrait = portrait;
        this.initOverlay();
    }


    private void initOverlay() {
        overlay.setBackground(new Background(new BackgroundFill(new Color(177./255, 208./255, 255./255, 0.5),
            CornerRadii.EMPTY, Insets.EMPTY)));
        overlay.setBorder(new Border(new BorderStroke(new Color(0./255, 100./255, 255./255, 0.2),
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        this.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                int endX = getColumnByXCoord(event.getX());
                int endY = getRowByYCoord(event.getY());
                int startX = dragStartX, startY = dragStartY;

                if (startX > endX) {
                    startX = endX;
                    endX = dragStartX;
                }
                if (startY > endY) {
                    startY = endY;
                    endY = dragStartY;
                }
                int spanX = endX - startX + 1;
                int spanY = endY - startY + 1;
                overlay.setMaxWidth(taskCells.get(startY).get(startX).getWidth() * spanX);
                overlay.setMaxHeight(taskCells.get(startY).get(startX).getHeight() * spanY);

                this.getChildren().remove(overlay);
                this.add(overlay, startX, startY, spanX, spanY);
            }
        });

        // calculate start drag cell
        this.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                dragStartX = getColumnByXCoord(event.getX());
                dragStartY = getRowByYCoord(event.getY());
            }
        });

        this.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                this.getChildren().remove(overlay);

                if (SubareaSelectedCallback != null) {
                    // get new parametric portrait bounds
                    int endX = getColumnByXCoord(event.getX());
                    int endY = getRowByYCoord(event.getY());
                    if (dragStartX == endX && dragStartY == endY)
                        return;

                    PortraitProperties props = this.portrait.getUniqueTaskProperties();

                    List<Double> start = Arrays.asList(
                        getValueOnStep(props.getStartValues().get(0).get(), props.getStepDeltas().get(0).get(), Math.min(dragStartX, endX))
                            .setScale(5, RoundingMode.HALF_UP).doubleValue(),
                        getValueOnStep(props.getStartValues().get(1).get(), props.getStepDeltas().get(1).get(),
                            Math.min(props.getStepCounts().get(1).get() - dragStartY - 1, props.getStepCounts().get(1).get() - endY - 1))
                            .setScale(5, RoundingMode.HALF_UP).doubleValue()
                    );
                    List<Double> end = Arrays.asList(
                        getValueOnStep(props.getStartValues().get(0).get(), props.getStepDeltas().get(0).get(), Math.max(dragStartX, endX))
                            .setScale(5, RoundingMode.HALF_UP).doubleValue(),
                        getValueOnStep(props.getStartValues().get(1).get(), props.getStepDeltas().get(1).get(),
                            Math.max(props.getStepCounts().get(1).get() - dragStartY - 1, props.getStepCounts().get(1).get() - endY - 1))
                            .setScale(5, RoundingMode.HALF_UP).doubleValue()
                    );
                    // TODO
                    *//*List<Integer> steps = getListDeepCopy(stepsCnt);
                    for (int i = 0; i < start.size(); i++) {
                        if (start.get(i).equals(end.get(i)))
                            steps.set(i, 1);
                    }

                    ParametricPortraitNode parametricPortrait = new ParametricPortraitNode();
                    parametricPortrait.setParameters(commonTask, instances, properties, start, end, steps, scale);

                    SubareaSelectedCallback.selected(parametricPortrait);*//*
                }
            }
        });
    }


    *//**
     *
     * @param x coordinate
     * @return column index by x coord. Assuming all columns have the same size.
     * if coord out of bound return nearest column
     *//*
    private int getColumnByXCoord(double x) {
        if (x <= 0) return 0;
        return Math.min((int)(x / (this.getWidth() / this.getColumnConstraints().size())),
            this.getColumnConstraints().size() - 1);
    }

    *//**
     *
     * @param y coordinate
     * @return row index by y coord. Assuming all rows have the same size.
     * if coord out of bound return nearest row
     *//*
    private int getRowByYCoord(double y) {
        if (y <= 0) return 0;
        return Math.min((int)(y / (this.getHeight() / this.getRowConstraints().size())),
            this.getRowConstraints().size() - 1);
    }


    *//**
     * make new taskCells List
     * @param cols columns count
     * @param rows rows count
     *//*
    void newTaskCellsList(int cols, int rows) {
        taskCells = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            List<TaskCell> rowCells = new ArrayList<>();

            for (int col = 0; col < cols; col++) {
//                TaskCell cell = new TaskCell();
//                rowCells.add(cell);
            }

            taskCells.add(rowCells);
        }
    }


    *//**
     *
     * @return taskCells list
     *//*
    List<List<TaskCell>> getTaskCells() {
        return taskCells;
    }


    *//**
     * set grid
     * @param cols columns count
     * @param rows rows count
     *//*
    void updateGrid(int cols, int rows) {
        this.getChildren().clear();
        this.getColumnConstraints().clear();
        for (int col = 0; col < cols; col++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setFillWidth(true);
            cc.setPercentWidth(100.0 / cols);
            this.getColumnConstraints().add(cc);
        }

        this.getRowConstraints().clear();
        for (int row = 0; row < rows; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setFillHeight(true);
            rc.setPercentHeight(100.0 / rows);
            this.getRowConstraints().add(rc);
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                this.add(taskCells.get(row).get(col), col, row);
            }
        }
    }


    *//**
     *
     * @param startValue start value
     * @param delta steps count
     * @param step step number
     * @return calculated value
     *//*
    private BigDecimal getValueOnStep(double startValue, double delta, int step) {
        BigDecimal bdStartValue = new BigDecimal(startValue);
        BigDecimal bdSteps = new BigDecimal(Math.max(1, delta - 1));
        BigDecimal bdStep = new BigDecimal(step);
        return bdStartValue.add(new BigDecimal(delta * step), MathContext.DECIMAL64);
    }
*/}