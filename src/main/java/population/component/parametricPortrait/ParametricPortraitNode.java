package population.component.parametricPortrait;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import population.model.ParametricPortrait.ParametricPortrait;
import population.model.ParametricPortrait.PortraitProperties;

import java.util.List;


public class ParametricPortraitNode extends GridPane {
    /** needed width of parametric portrait to take nail snapshot */
    private static final double THUMBNAIL_WIDTH = 230;
    /** needed height of parametric portrait to take nail snapshot */
    private static final double THUMBNAIL_HEIGHT = 230;

    /** max GridPane width */
    private DoubleProperty availableWidth = new SimpleDoubleProperty();
    /** max GridPane height */
    private DoubleProperty availableHeight = new SimpleDoubleProperty();

    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis yAxis = new NumberAxis();
    private Label xLabel = new Label();
    private Label yLabel = new Label();
    
    private TaskCellGrid cellContainer;

    private ParametricPortrait parametricPortrait;


    public ParametricPortraitNode(ParametricPortrait parametricPortrait) {
        this.parametricPortrait = parametricPortrait;
        this.cellContainer = new TaskCellGrid(parametricPortrait);

        this.setLayout();
//this.setGridLinesVisible(true);

        this.availableWidth.addListener((observable, oldValue, newValue) -> updateSize());
        this.availableHeight.addListener((observable, oldValue, newValue) -> updateSize());
    }


    /********************************
     * 
     *           LAYOUT
     * 
     *******************************/


    /**
     * initialize this layout
     */
    private void setLayout() {
        this.initAxes();
        this.initAxesLabels();
        this.setAxesUnits();
        this.initConstraints();
        this.redrawCells();
        this.updateSize();
    }
    

    private void initAxes() {
        xAxis.setAutoRanging(false);
        xAxis.setSide(Side.BOTTOM);
        xAxis.setMinorTickVisible(false);
        xAxis.setAnimated(false);

        yAxis.setAutoRanging(false);
        yAxis.setSide(Side.LEFT);
        yAxis.setMinorTickVisible(false);
        yAxis.setAnimated(false);
    }


    private void initAxesLabels() {
        yLabel.setRotate(270);
        yLabel.setWrapText(true);
        yLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

        xLabel.setWrapText(true);
        xLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        xLabel.setAlignment(Pos.CENTER);
    }


    /**
     * set constraints to this
     */
    private void initConstraints() {
        RowConstraints rc0 = new RowConstraints();
        rc0.setFillHeight(true);
        rc0.setVgrow(Priority.ALWAYS);
        getRowConstraints().add(rc0);

        RowConstraints rc1 = new RowConstraints();
        rc1.setFillHeight(true);
        rc1.setVgrow(Priority.NEVER);
        getRowConstraints().add(rc1);

        RowConstraints rc2 = new RowConstraints();
        rc2.setFillHeight(true);
        rc2.setVgrow(Priority.NEVER);
        getRowConstraints().add(rc2);

        ColumnConstraints cc0 = new ColumnConstraints();
        cc0.setFillWidth(true);
        cc0.setHgrow(Priority.NEVER);
        getColumnConstraints().add(cc0);

        ColumnConstraints cc1 = new ColumnConstraints();
        cc1.setFillWidth(true);
        cc1.setHgrow(Priority.NEVER);
        getColumnConstraints().add(cc1);

        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setFillWidth(true);
        cc2.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(cc2);

        GridPane.setHalignment(yLabel, HPos.CENTER);
        this.add(new Group(yLabel), 0, 0);
        GridPane.setHalignment(xLabel, HPos.CENTER);
        this.add(xLabel, 2, 2);
        GridPane.setHalignment(xAxis, HPos.CENTER);
        this.add(xAxis, 2, 1);
        GridPane.setHalignment(yAxis, HPos.CENTER);
        this.add(yAxis, 1, 0);
        this.add(cellContainer, 2, 0);
    }


    /**
     * set portrait axis
     */
    private void setAxesUnits() {
        PortraitProperties props = this.parametricPortrait.getProperties();
        xAxis.setTickUnit(props.getStepDeltas().get(0).get());
        xAxis.setLowerBound(props.getStartValues().get(0).get());
        xAxis.setUpperBound(props.getStartValues().get(0).get() + 
            (props.getStepCounts().get(0).get() - 1) * props.getStepDeltas().get(0).get()
        );

        yAxis.setTickUnit(props.getStepDeltas().get(1).get());
        yAxis.setLowerBound(props.getStartValues().get(1).get());
        yAxis.setUpperBound(props.getStartValues().get(1).get() +
            (props.getStepCounts().get(1).get() - 1) * props.getStepDeltas().get(1).get()
        );
    }
    

    /**
     * set max parametric portrait size
     * @param availableWidth max available width
     * @param availableHeight max available height
     */
    public void setAvailableSize(double availableWidth, double availableHeight) {
        this.availableWidth.setValue(availableWidth);
        this.availableHeight.setValue(availableHeight);
    }


    /**
     * set parametric portrait size depends on window size
     */
    private void updateSize() {
        int xSteps = parametricPortrait.getColCount();
        int ySteps = parametricPortrait.getRowCount();
        
        double availableWidth = this.availableWidth.get();
        double availableHeight = this.availableHeight.get();

        double minCellSize = cellContainer.getTaskCells().size() == 0 ? TaskCell.MIN_INNER_CELL_SIZE :
                cellContainer.getTaskCells().get(0).get(0).getRequestedSize() * TaskCell.MIN_INNER_CELL_SIZE;
        double maxCellSize = cellContainer.getTaskCells().size() == 0 ? TaskCell.MAX_INNER_CELL_SIZE :
                cellContainer.getTaskCells().get(0).get(0).getRequestedSize() * TaskCell.MAX_INNER_CELL_SIZE;
        double calculatedCellSize = Math.min(
                (availableHeight - xAxis.getHeight() - xLabel.getHeight()) / ySteps,
                (availableWidth - yAxis.getWidth() - yLabel.getHeight()) / xSteps);
        double cellSize = Math.min( Math.max(minCellSize, calculatedCellSize), maxCellSize );

        double width = cellSize * xSteps + yAxis.getWidth() + yLabel.getHeight();
        double height = cellSize * ySteps + xAxis.getHeight() + xLabel.getHeight();
        this.setMinSize(width, height);
        this.setMaxSize(width, height);

        xAxis.setMaxWidth(cellSize * (xSteps - 1));
        yAxis.setMaxHeight(cellSize * (ySteps - 1));
        yLabel.setMaxWidth(cellSize * ySteps - 1);
        xLabel.setMaxWidth(cellSize * xSteps - 1);

        double fontSize = 18./500 * Math.max(width, height);
        xAxis.setTickLabelFont(new Font(fontSize));
        yAxis.setTickLabelFont(new Font(fontSize));

        fontSize = 17./500 * Math.max(width, height);
        xLabel.setFont(new Font(fontSize));
        yLabel.setFont(new Font(fontSize));
    }


    /**
     * Redraw each cells in portrait node
     */
    public void redrawCells() {
        if (cellContainer.getTaskCells() == null)
            return;

        int maxSize = 1;
        for (List<TaskCell> rowCells: cellContainer.getTaskCells()) {
            for (TaskCell taskCell : rowCells) {
                maxSize = Math.max(taskCell.getRequestedSize(), maxSize);
            }
        }

        cellContainer.redrawCells();
    }


    /**
     * @return snapshot of parametric portrait
     */
    public WritableImage getThumbnail() {
        return takeSnapshot(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, false);
    }


    /**
     * Make portrait snapshot
     *
     * @param width snapshot width
     * @param height snapshot height
     * @param withLabels should show labels on snapshot
     */
    protected WritableImage takeSnapshot(double width, double height, boolean withLabels) {
        // set size of this to snapshot size to fit all elements right
        double w = availableWidth.doubleValue(),
            h = availableHeight.doubleValue();

        // remove this from parent to resize it without any impact
        Pane parent = (Pane)this.getParent();
        if (parent != null)
            parent.getChildren().remove(this);
        setLabelsVisibility(withLabels);

        // put parametric portrait in container for centered it
        BorderPane container = new BorderPane(this);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: transparent;");
        new Scene(container, width, height, Color.TRANSPARENT);
        container.applyCss();
        container.layout();
        setAvailableSize(width, height);
        WritableImage snapshot = container.snapshot(new SnapshotParameters(), null);

        // return this back to parent
        container.getChildren().remove(this);
        if (parent != null)
            parent.getChildren().add(this);
        setLabelsVisibility(true);

        // restore initial size
        setAvailableSize(w, h);

        return snapshot;
    }


    /**
     * Set labels visibility on portrait
     * @param visible should be visible
     */
    private void setLabelsVisibility(boolean visible) {
        double maxSize = visible ? Double.MAX_VALUE : 0;
        xLabel.setVisible(visible);
        yLabel.setVisible(visible);
        xLabel.setMaxHeight(maxSize);
        //yLabel.setMaxWidth(maxSize);
    }


    public ParametricPortrait getParametricPortrait() {
        return parametricPortrait;
    }


    /**
     * Add listener for subarea selected
     */
    public void addSubareaSelectedListener(ParametricPortraitSubareaSelectListener listener) {
        this.cellContainer.addSubareaSelectedListener(listener);
    }

}
