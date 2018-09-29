package population.component.parametricPortrait;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
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
import population.model.StateModel.State;
import population.model.Task;
import population.model.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class ParametricPortraitNode extends GridPane {
    /** needed width of parametric portrait to take nail snapshot*/
    private static final double THUMBNAIL_WIDTH = 230;
    /** needed height of parametric portrait to take nail snapshot*/
    private static final double THUMBNAIL_HEIGHT = 230;

    /** max GridPane width */
    private DoubleProperty availableWidth = new SimpleDoubleProperty();
    /** max GridPane height */
    private DoubleProperty availableHeight = new SimpleDoubleProperty();

    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis yAxis = new NumberAxis();
    private Label xLabel = new Label();
    private Label yLabel = new Label();
    
    private TaskCellGrid cellContainer = new TaskCellGrid();

    private ParametricPortrait parametricPortrait;
    

    /**
     * transitions and states properties which can be chosen as parametric portrait property for certain axe
     */
    public enum Property {
        PROBABILITY,
        SOURCE_DELAY,
        STATE_IN,
        STATE_OUT,
        COUNT
    }


    public ParametricPortraitNode(ParametricPortrait parametricPortrait) {
        this.parametricPortrait = parametricPortrait;

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
        this.initConstraints();
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
            props.getStepCounts().get(0).get() * props.getStepDeltas().get(0).get()
        );

        yAxis.setTickUnit(props.getStepDeltas().get(1).get());
        yAxis.setLowerBound(props.getStartValues().get(1).get());
        yAxis.setUpperBound(props.getStartValues().get(1).get() +
            props.getStepCounts().get(1).get() * props.getStepDeltas().get(1).get()
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
        int xSteps = parametricPortrait.getProperties().getStepCounts().get(0).get();
        int ySteps = parametricPortrait.getProperties().getStepCounts().get(1).get();
        
        double availableWidth = this.availableWidth.get();
        double availableHeight = this.availableHeight.get();

        double minCellSize = cellContainer.getTaskCells().size() == 0 ? TaskCell.MIN_INNER_CELL_SIZE :
                cellContainer.getTaskCells().get(0).get(0).numCols * TaskCell.MIN_INNER_CELL_SIZE;
        double maxCellSize = cellContainer.getTaskCells().size() == 0 ? TaskCell.MAX_INNER_CELL_SIZE :
                cellContainer.getTaskCells().get(0).get(0).numCols * TaskCell.MAX_INNER_CELL_SIZE;
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
     * update parametric portrait depending on task. Set grid, axes, size etc...
     */
    public void updateView() {
        List<IntegerProperty> stepCounts = parametricPortrait.getProperties().getStepCounts();
        cellContainer.updateGrid(stepCounts.get(0).get(), stepCounts.get(1).get());
        
        setAxesUnits();

        List<ObjectProperty> instances = parametricPortrait.getProperties().getInstances();
        List<ObjectProperty<ParametricPortrait.Property>> properties = parametricPortrait.getProperties().getProperties();
        xLabel.setText(ParametricPortrait.INSTANCE_STRING_CONVERTER.toString(instances.get(0).get()) + " : " + 
            ParametricPortrait.PROPERTY_STRING_CONVERTER.toString(properties.get(0).get())
        );
        yLabel.setText(ParametricPortrait.INSTANCE_STRING_CONVERTER.toString(instances.get(1).get()) + " : " +
            ParametricPortrait.PROPERTY_STRING_CONVERTER.toString(properties.get(1).get())
        );
        
        updateParametricPortraitFill();
        updateSize();
    }




    /**************************
     *
     *      tODO SOME STUFF HERE
     *
     ***************************/


    /**
     * updateView parametric portrait decoration
     */
    public void updateParametricPortraitFill() {
        if (cellContainer.getTaskCells() == null)
            return;

        int maxSize = 1;
        for (List<TaskCell> rowCells: cellContainer.getTaskCells())
            for (TaskCell taskCell: rowCells)
                maxSize = Math.max(taskCell.getRequestedSize(), maxSize);

        for (List<TaskCell> rowCells: cellContainer.getTaskCells())
            for (TaskCell taskCell: rowCells)
                taskCell.setSquareSize(maxSize);

        for (List<TaskCell> rowCells: cellContainer.getTaskCells()) {
            for (TaskCell taskCell : rowCells) {
                taskCell.fill();
            }
        }
    }


    /**
     * set new property value to instance
     * @param instance instance (state or transition)
     * @param property changed property
     * @param val new value
     */
    private void setPropertyValue(Object instance, Property property, double val) {
        if (instance instanceof State) {
            switch (property) {
                case COUNT: {
                    ((State)instance).setCount((double)val);
                }
            }
        }

        else if (instance instanceof Transition){
            switch (property) {
                case STATE_OUT: {
                    ((Transition)instance).setSourceCoefficient((int)val);
                    break;
                }
                case PROBABILITY: {
                    ((Transition)instance).setProbability((double)val);
                    break;
                }
                case SOURCE_DELAY: {
                    ((Transition)instance).setSourceDelay((int)val);
                    break;
                }
                case STATE_IN: {
                    ((Transition)instance).setOperandDelay((int)val);
                    break;
                }
            }

        }
    }


    /**
     *
     * @param instance state or transition with description
     * @return list of instances of instance Class, whose trimmed descriptions equals to instance description
     */
    private List<Object> getInstancesByInstanceDescription(Object instance) {
        List<Object> instancesByDescription = new ArrayList<>();
        instancesByDescription.add(instance);

        // TODO do it on id
        /*if (instance instanceof State) {
            String description = ((State)instance).getDescription().trim();
            if (!description.equals("")) {
                instancesByDescription.addAll(commonTask.getStates().stream()
                        .filter(x -> x.getDescription().trim().equals(description))
                        .collect(Collectors.toList()));
            }

        } else if (instance instanceof Transition){
            String description = ((Transition)instance).getDescription().trim();
            if (!description.equals("")) {
                instancesByDescription.addAll(commonTask.getTransitions().stream()
                        .filter(x -> x.getDescription().trim().equals(description))
                        .collect(Collectors.toList()));
            }
        }*/

        return instancesByDescription;
    }


    /**
     * get task in the specified cell
     * Clone states and transitions in task
     * @param steps step number for each changed instance property. ({col, row})
     * @return task with properties set for specified task
     */
    private Task getTask(int[] steps) {
        //Task task = new Task(commonTask);
        // TODO
        /*task.setAllowNegative(false);

        // set cloned states and transition to task for modify their fields without impact
        List<Transition> clonedTransitions = getListDeepCopy(transitions);
        List<State> clonedStates = getListDeepCopy(states);
        task.setTransitions(clonedTransitions);
        task.setStates(clonedStates);

        // set changeable properties to selected instances
        for (int i = 0; i < instances.size(); i++) {
            // calculate next property and round because computing error can
            // cause essential deviation from expected result
            double val = getValueOnStep(this.startValues.get(i), this.endValues.get(i), this.stepsCnt.get(i), steps[i])
                    .setScale(5, RoundingMode.HALF_UP)
                    .doubleValue();

            String selectedProperty = properties.get(i);
            Object selectedInstance = null;
            List<Object> instancesByDescription = getInstancesByInstanceDescription(instances.get(i));
            for (Object instance: instancesByDescription) {
                if (instance instanceof State) {
                    selectedInstance = clonedStates.get(states.indexOf(instance));
                } else if (instance instanceof Transition) {
                    selectedInstance = clonedTransitions.get(transitions.indexOf(instance));
                }
                setPropertyValue(selectedInstance, selectedProperty, val);
            }
        }
*/
        return task;
    }


    /**
     * set primary parametric portrait parameters
     * @param commonTask commonTask
     * @param instances instances list
     * @param properties properties list
     * @param startValues start values
     * @param endValues end values
     * @param stepsCnt steps count
     */
    private void setParameters(Task commonTask,
                               List<Object> instances,
                               List<String> properties,
                               List<Double> startValues,
                               List<Double> endValues,
                               List<Integer> stepsCnt,
                               int scale
    ) {
        // TODO
        /*List<State> primaryStates = commonTask.getStates();
        List<Transition> primaryTransitions = commonTask.getTransitions();

        // clone task
        this.commonTask = new Task(commonTask);
        this.commonTask.setTransitions(getListDeepCopy(commonTask.getTransitions()));
        this.commonTask.setStates(getListDeepCopy(commonTask.getStates()));

        this.states = this.commonTask.getStates();
        this.transitions = this.commonTask.getTransitions();
        this.instances = instances.stream()
                .map(mapped -> {
                    if (mapped instanceof State)
                        return this.states.get(primaryStates.indexOf(mapped));
                    if (mapped instanceof Transition)
                        return this.transitions.get(primaryTransitions.indexOf(mapped));
                    return null;
                })
                .collect(Collectors.toList());

        this.properties = getListDeepCopy(properties);
        this.startValues = getListDeepCopy(startValues);
        this.endValues = getListDeepCopy(endValues);
        this.stepsCnt = getListDeepCopy(stepsCnt);

        cellContainer.newTaskCellsList(stepsCnt.get(0), stepsCnt.get(1));

        this.scale = scale;*/
    }



    /**
     * calculate parametric portrait
     * calculate each task concurrent in multithreading mode
     * @param commonTask common task for whole parametric portrait.
     * (Immutable parameter, it will be cloned)
     * @param instances List of instance which will be changed for each axis
     * @param properties List of properties which will be changed for each axis
     * @param startValues List of start values of changed properties
     * @param endValues List of end values of changed properties
     * @param stepsCnt List of steps count by which split values
     */
    public void calculate( Task commonTask,
                           List<Object> instances,
                           List<String> properties,
                           List<Double> startValues,
                           List<Double> endValues,
                           List<Integer> stepsCnt,
                           int scale) {

        setParameters(commonTask, instances, properties, startValues, endValues, stepsCnt, scale);

        // TODO
        /*statesListShownOnParametricPortrait = stateSettingsGroup.getStateSettingList().stream()
                .filter(StateSetting::getShow)
                .map(StateSetting::getState)
                .collect(Collectors.toList());*/

        if (commonTask.isParallel())
            calculateConcurrent();
        else
            calculateSequential();
    }


    /**
     * calculate each taskCells in portrait sequential
     */
    private void calculateSequential() {
        double taskProgress[] = {0};        // progress of current calculated task
        final int tasksCnt = stepsCnt.get(0) * stepsCnt.get(1);

        for (int col = 0; col < stepsCnt.get(0); col++)
            for (int row = 0; row < stepsCnt.get(1); row++) {
//                col = 9; row = 0;
                TaskCell taskCell = cellContainer.getTaskCells().get(row).get(col);
                taskCell.setTask(getTask(new int[]{col, stepsCnt.get(1) - row - 1}));
                taskCell.calculateTask(
                        null,
                        progress -> Platform.runLater(() -> {
                                primaryController.mCalculationProgressBar.setProgress(
                                        primaryController.mCalculationProgressBar.getProgress()
                                                - taskProgress[0] + progress / tasksCnt);
                                taskProgress[0] = progress / tasksCnt;
                        }),
                        result -> Platform.runLater(() -> {
                                primaryController.mCalculationProgressBar.setProgress(
                                        primaryController.mCalculationProgressBar.getProgress()
                                                - taskProgress[0] + 1. / tasksCnt);
                                taskProgress[0] = 0;
                        })
                );
            }
    }

    /**
     * calculate each taskCell concurrent
     */
    private void calculateConcurrent() {
        final double[] threadProgress = {0};
        final int tasksCnt = stepsCnt.get(0) * stepsCnt.get(1);
        List<Callable<Void>> tasksCalculations = new ArrayList<>();

        for (int col = 0; col < stepsCnt.get(0); col++)
            for (int row = 0; row < stepsCnt.get(1); row++) {
                final int finalRow = row, finalCol = col;

                tasksCalculations.add(() -> {
                    TaskCell taskCell = cellContainer.getTaskCells().get(finalRow).get(finalCol);
                    taskCell.setTask(getTask(new int[]{finalCol, stepsCnt.get(1) - finalRow - 1}));
                    taskCell.calculateTask(
                            null,
                            progress -> Platform.runLater(() -> {
                                    primaryController.mCalculationProgressBar.setProgress(
                                            primaryController.mCalculationProgressBar.getProgress()
                                                    - threadProgress[0] + progress / tasksCnt);
                                    threadProgress[0] = progress / tasksCnt;
                            }),
                            result -> Platform.runLater(() ->
                                primaryController.mCalculationProgressBar.setProgress(
                                        primaryController.mCalculationProgressBar.getProgress()
                                                - threadProgress[0] + 1. / tasksCnt)
                            )
                    );

                    return null;
                });
            }


        // await for all Threads finished
        try {
            Executors.newCachedThreadPool().invokeAll(tasksCalculations);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void setSubareaSelectedCallback(SubareaSelectedCallback callback) {
        cellContainer.SubareaSelectedCallback = callback;
    }


    /**
     * set labels visibility on portrait
     * @param visible should be visible
     */
    private void setLabelsVisibility(boolean visible) {
        double maxSize = visible ? Double.MAX_VALUE : 0;
        xLabel.setVisible(visible);
        yLabel.setVisible(visible);
        xLabel.setMaxHeight(maxSize);
        //yLabel.setMaxWidth(maxSize);
    }


    /**
     * make snapshot
     */
    public WritableImage takeSnapshot(double width, double height, boolean withLabels) {
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
     *
     * @return snapshot of parametric portrait
     */
    public WritableImage getThumbnail() {
        return takeSnapshot(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, false);
    }


    public double getMaxPortraitHeight() {
        if (this.cellContainer.taskCells.size() == 0)
            return 0;
        this.applyCss();
        return this.cellContainer.taskCells.size() * TaskCell.MAX_INNER_CELL_SIZE *
                this.cellContainer.taskCells.get(0).get(0).numRows + this.xLabel.getHeight();
    }


    public double getMaxPortraitWidth() {
        if (this.cellContainer.taskCells.size() == 0)
            return 0;
        this.applyCss();
        return this.cellContainer.taskCells.get(0).size() * TaskCell.MAX_INNER_CELL_SIZE *
                this.cellContainer.taskCells.get(0).get(0).numCols + this.yLabel.getHeight();
    }















    public interface SubareaSelectedCallback {
        /**
         * called when area in parametric portrait was selected
         * @param parametricPortrait new parametric portrait with new bounds
         */
        void selected(ParametricPortraitNode parametricPortrait);
    }







    /**
     *
     * @param list list to clone
     * @param <T> list type
     * @return new List with cloned items
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> getListDeepCopy(List<T> list) {
        return  (List<T>)list.stream()
            .map(x -> {
                if (x instanceof Number || x instanceof String)
                    return x;
                if (x instanceof State)
                    return ((State)x).clone();
                if (x instanceof Transition)
                    return ((Transition)x).clone();
                return null;
            })
            .collect(Collectors.toList());
    }
}
