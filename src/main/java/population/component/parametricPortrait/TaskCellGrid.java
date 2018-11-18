package population.component.parametricPortrait;

import javafx.geometry.Insets;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import population.model.ParametricPortrait.ParametricPortrait;
import population.model.ParametricPortrait.PortraitProperties;
import population.model.TaskV4;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Grid of parametric portrait task cells
 */
public class TaskCellGrid extends GridPane {
    /** grid of TaskCell (numeration from upper left corner [row][col]) */
    private List<List<TaskCell>> taskCells = new ArrayList<>();
    /** highlight rectangle on drag */
    private Pane overlay = new Pane();
    /** start drag cell index */
    private int dragStartX = 0;
    /** start drag cell index */
    private int dragStartY = 0;

    private ParametricPortrait portrait;


    TaskCellGrid(ParametricPortrait portrait) {
        this.portrait = portrait;
        this.initOverlay();

        this.initTaskCellsList();

        this.updateGrid();
    }


    /**
     * Init grid subarea selection
     */
    private void initOverlay() {
        overlay.setBackground(new Background(new BackgroundFill(new Color(177./255, 208./255, 255./255, 0.5),
            CornerRadii.EMPTY, Insets.EMPTY)));
        overlay.setBorder(new Border(new BorderStroke(new Color(0./255, 100./255, 255./255, 0.2),
            BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        // change subarea size and position on mouse dragged
        this.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                int endX = getColumnByXCoord(event.getX());
                int endY = getRowByYCoord(event.getY());
                int startX = dragStartX;
                int startY = dragStartY;

                // swap values if necessary
                if (startX > endX) {
                    startX = endX;
                    endX = dragStartX;
                }
                if (startY > endY) {
                    startY = endY;
                    endY = dragStartY;
                }

                // set overlay size
                int spanX = endX - startX + 1;
                int spanY = endY - startY + 1;
                overlay.setMaxWidth(taskCells.get(startY).get(startX).getWidth() * spanX);
                overlay.setMaxHeight(taskCells.get(startY).get(startX).getHeight() * spanY);

                // and set overlay to new position
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

        // on mouse released get new parametric portrait parameters and calculate it
        this.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                this.getChildren().remove(overlay);

                // get new parametric portrait bounds
                int endX = getColumnByXCoord(event.getX());
                int endY = getRowByYCoord(event.getY());
                if (dragStartX == endX || dragStartY == endY) {
                    return;
                }

                PortraitProperties props = this.portrait.getProperties().clone();

                // replace start value and step delta with new values
                double minXValue = this.portrait.getPropertyValueOnStep(0, Math.min(dragStartX, endX));
                double maxXValue = this.portrait.getPropertyValueOnStep(0, Math.max(dragStartX, endX));
                double xStepDelta = new BigDecimal((maxXValue - minXValue) / (props.getStepCounts().get(0).get() - 1))
                    .setScale(5, RoundingMode.HALF_UP)
                    .doubleValue();

                double minYValue = this.portrait.getPropertyValueOnStep(1, Math.min(
                    this.portrait.getRowCount() - dragStartY - 1,
                    this.portrait.getRowCount() - endY - 1
                ));
                double maxYValue = this.portrait.getPropertyValueOnStep(1, Math.max(
                    this.portrait.getRowCount() - dragStartY - 1,
                    this.portrait.getRowCount() - endY - 1
                ));
                double yStepDelta = new BigDecimal((maxYValue - minYValue) / (props.getStepCounts().get(1).get() - 1))
                    .setScale(5, RoundingMode.HALF_UP)
                    .doubleValue();

                props.getStartValues().get(0).set(minXValue);
                props.getStepDeltas().get(0).set(xStepDelta);
                props.getStartValues().get(1).set(minYValue);
                props.getStepDeltas().get(1).set(yStepDelta);

                this.emitSubareaSelectedEvent(this.portrait.getTask(), props);
            }
        });
    }


    /**
     *
     * @param x coordinate
     * @return column index by x coord. Assuming all columns have the same size.
     * if coord out of bound return nearest column
     */
    private int getColumnByXCoord(double x) {
        if (x <= 0) return 0;
        return Math.min((int)(x / (this.getWidth() / this.getColumnConstraints().size())),
            this.getColumnConstraints().size() - 1);
    }

    /**
     *
     * @param y coordinate
     * @return row index by y coord. Assuming all rows have the same size.
     * if coord out of bound return nearest row
     */
    private int getRowByYCoord(double y) {
        if (y <= 0) return 0;
        return Math.min((int)(y / (this.getHeight() / this.getRowConstraints().size())),
            this.getRowConstraints().size() - 1);
    }


    /**
     * Make new taskCells List
     */
    void initTaskCellsList() {
        taskCells = new ArrayList<>();
        int rows = this.portrait.getRowCount();
        int cols = this.portrait.getColCount();

        for (int row = 0; row < rows; row++) {
            List<TaskCell> rowCells = new ArrayList<>();

            for (int col = 0; col < cols; col++) {
                TaskCell cell = new TaskCell(this.portrait, this.portrait.getRowCount() - row - 1, col);
                rowCells.add(cell);
            }

            taskCells.add(rowCells);
        }
    }


    /**
     *
     * @return taskCells list
     */
    List<List<TaskCell>> getTaskCells() {
        return taskCells;
    }


    /**
     * Set own grid
     */
    public void updateGrid() {
        int cols = this.portrait.getColCount();
        int rows = this.portrait.getRowCount();

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

        this.redrawCells();
    }


    /**
     * Redraw all inner portrait cells
     */
    public void redrawCells() {
        this.taskCells.forEach(list -> {
            list.forEach(TaskCell::fill);
        });
    }


    /**************************
     *
     *    subarea selection
     *
     *************************/

    private List<ParametricPortraitSubareaSelectListener> subareaSelectedListeners = new ArrayList<>();

    public void addSubareaSelectedListener(ParametricPortraitSubareaSelectListener listener) {
        this.subareaSelectedListeners.add(listener);
    }

    private void emitSubareaSelectedEvent(TaskV4 task, PortraitProperties props) {
        this.subareaSelectedListeners.forEach(listener -> {
            listener.onSubareaSelected(task, props);
        });
    }
}
