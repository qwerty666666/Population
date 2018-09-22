package population.component;

import population.PopulationApplication;
import population.model.ParametricPortrait.StateSettings;
import population.model.ParametricPortrait.StateSettingsGroup;
import population.controller.PrimaryController;
import population.model.StateModel.State;
import population.model.Task;
import population.model.Transition;
import javafx.application.Platform;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static population.controller.ParametricPortraitTabController.*;


public class ParametricPortrait extends GridPane {
    /** needed width of parametric portrait to take nail snapshot*/
    private static final double THUMBNAIL_WIDTH = 230;
    /** needed height of parametric portrait to take nail snapshot*/
    private static final double THUMBNAIL_HEIGHT = 230;

    /** reference to primary controller */
    private PrimaryController primaryController;
    /** application */
    private PopulationApplication application;

    /** task states */
    private List<State> states;
    /** task transitions */
    private List<Transition> transitions;
    /** state settings */
    private StateSettingsGroup stateSettingsGroup;

    /** max GridPane width */
    private DoubleProperty availableWidth = new SimpleDoubleProperty();
    /** max GridPane height */
    private DoubleProperty availableHeight = new SimpleDoubleProperty();

    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis yAxis = new NumberAxis();
    private Label xLabel = new Label();
    private Label yLabel = new Label();
    private CellContainer cellContainer = new CellContainer();

    private List<Double> startValues;
    private List<Double> endValues;
    private List<Integer> stepsCnt;
    private List<String> properties;
    private List<Object> instances;
    private Task commonTask;

    private int scale = 3;

    /** states which must be considered in parametric portrait */
    private List<State> statesListShownOnParametricPortrait;



    public ParametricPortrait(PopulationApplication application, StateSettingsGroup stateSettingsGroup) {
        this.application = application;
        this.primaryController = application.getPrimaryController();
        this.stateSettingsGroup = stateSettingsGroup;

        initAxes();
        initLabels();
//this.setGridLinesVisible(true);
        initConstraints();

        this.availableWidth.addListener((observable, oldValue, newValue) -> updateSize());
        this.availableHeight.addListener((observable, oldValue, newValue) -> updateSize());
    }


    public List<Double> getStartValues() {
        return startValues;
    }

    public List<Double> getEndValues() {
        return endValues;
    }

    public List<Integer> getStepsCnt() {
        return stepsCnt;
    }

    public List<Object> getInstances() {
        return instances;
    }

    public List<String> getSelectedProperties() {
        return properties;
    }

    public Task getCommonTask() {
        return commonTask;
    }

    /**
     * first initialize pahse portrait axis
     */
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


    /**
     * initialize axis labels
     */
    private void initLabels() {
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


    public StateSettingsGroup getStateSettingsGroup() {
        return stateSettingsGroup;
    }


    /**
     * set axis units
     */
    private void setAxisUnits() {
        xAxis.setTickUnit((endValues.get(0) - startValues.get(0)) / (stepsCnt.get(0) - 1));
        xAxis.setLowerBound(startValues.get(0));
        xAxis.setUpperBound(endValues.get(0));

        yAxis.setTickUnit((endValues.get(1) - startValues.get(1)) / (stepsCnt.get(1) - 1));
        yAxis.setLowerBound(startValues.get(1));
        yAxis.setUpperBound(endValues.get(1));
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
        if (stepsCnt == null || stepsCnt.size() < 2)
            return;

        double availableWidth = this.availableWidth.get();
        double availableHeight = this.availableHeight.get();

        double minCellSize = cellContainer.getTaskCells().size() == 0 ? TaskCell.MIN_INNER_CELL_SIZE :
                cellContainer.getTaskCells().get(0).get(0).numCols * TaskCell.MIN_INNER_CELL_SIZE;
        double maxCellSize = cellContainer.getTaskCells().size() == 0 ? TaskCell.MAX_INNER_CELL_SIZE :
                cellContainer.getTaskCells().get(0).get(0).numCols * TaskCell.MAX_INNER_CELL_SIZE;
        double calculatedCellSize = Math.min(
                (availableHeight - xAxis.getHeight() - xLabel.getHeight()) / stepsCnt.get(1),
                (availableWidth - yAxis.getWidth() - yLabel.getHeight()) / stepsCnt.get(0));
        double cellSize = Math.min( Math.max(minCellSize, calculatedCellSize), maxCellSize );

        double width = cellSize * stepsCnt.get(0) + yAxis.getWidth() + yLabel.getHeight();
        double height = cellSize * stepsCnt.get(1) + xAxis.getHeight() + xLabel.getHeight();
        this.setMinSize(width, height);
        this.setMaxSize(width, height);

        xAxis.setMaxWidth(cellSize * (stepsCnt.get(0) - 1));
        yAxis.setMaxHeight(cellSize * (stepsCnt.get(1) - 1));
        yLabel.setMaxWidth(cellSize * stepsCnt.get(1) - 1);
        xLabel.setMaxWidth(cellSize * stepsCnt.get(0) - 1);

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
        if (stepsCnt == null || stepsCnt.size() < 2)
            return;

        cellContainer.updateGrid(stepsCnt.get(0), stepsCnt.get(1));
        setAxisUnits();
        // TODO
        /*xLabel.setText(getInstanceString(states, instances.get(0)) + " : " + primaryController.getString(properties.get(0)));
        yLabel.setText(getInstanceString(states, instances.get(1)) + " : " + primaryController.getString(properties.get(1)));*/
        updateParametricPortraitFill();
        updateSize();
    }



    /**
     * get stateSettingsGroup by state id
     * @param id state id
     * @return stateSettingsGroup from states with id, null if it doesn't exist
     */
    private StateSettings getStateSettingsById(int id) {
        return stateSettingsGroup.getStateSettingsList().stream()
                .filter(x -> x.getState().getId() == id)
                .findFirst()
                .orElse(null);
    }


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
     *
     * @param startValue start value
     * @param endValue end value
     * @param steps steps count
     * @param step step number
     * @return calculated value
     */
    private BigDecimal getValueOnStep(double startValue, double endValue, int steps, int step) {
        BigDecimal bdStartValue = new BigDecimal(startValue);
        BigDecimal bdEndValue = new BigDecimal(endValue);
        BigDecimal bdSteps = new BigDecimal(Math.max(1, steps - 1));
        BigDecimal bdStep = new BigDecimal(step);
        return bdStartValue.add(bdEndValue.subtract(bdStartValue, MathContext.DECIMAL64)
                        .divide(bdSteps, MathContext.DECIMAL64)
                        .multiply(bdStep, MathContext.DECIMAL64),
                MathContext.DECIMAL64);
    }


    /**
     * set new property value to instance
     * @param instance instance (state or transition)
     * @param property changed property
     * @param val new value
     */
    private void setPropertyValue(Object instance, String property, double val) {
        if (instance instanceof State) {
            switch (property) {
                case StatePropertyChooseList.COUNT: {
                    ((State)instance).setCount((double)val);
                }
            }
        }

        else if (instance instanceof Transition){
            switch (property) {
                case TransitionPropertyChooseList.STATE_OUT: {
                    ((Transition)instance).setSourceCoefficient((int)val);
                    break;
                }
                case TransitionPropertyChooseList.PROBABILITY: {
                    ((Transition)instance).setProbability((double)val);
                    break;
                }
                case TransitionPropertyChooseList.SOURCE_DELAY: {
                    ((Transition)instance).setSourceDelay((int)val);
                    break;
                }
                case TransitionPropertyChooseList.STATE_IN: {
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
        Task task = new Task(commonTask);
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

        statesListShownOnParametricPortrait = stateSettingsGroup.getStateSettingsList().stream()
                .filter(StateSettings::getShow)
                .map(StateSettings::getState)
                .collect(Collectors.toList());

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




    /**
     * gridPane contains all TaskCells
     */
    private class CellContainer extends GridPane {
        /** grid of TaskCell (numeration from upper left corner [row][col]) */
        private List<List<TaskCell>> taskCells = new ArrayList<>();
        /** highlight rectangle on drag */
        private Pane overlay = new Pane();
        /** start drag cell index */
        private int dragStartX = 0;
        /** start drag cell index */
        private int dragStartY = 0;
        private SubareaSelectedCallback SubareaSelectedCallback = null;


        CellContainer() {
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

                        List<Double> start = Arrays.asList(
                                getValueOnStep(startValues.get(0), endValues.get(0), stepsCnt.get(0),
                                        Math.min(dragStartX, endX))
                                        .setScale(5, RoundingMode.HALF_UP).doubleValue(),
                                getValueOnStep(startValues.get(1), endValues.get(1), stepsCnt.get(1),
                                        Math.min(stepsCnt.get(1) - dragStartY - 1, stepsCnt.get(1) - endY - 1))
                                        .setScale(5, RoundingMode.HALF_UP).doubleValue()
                        );
                        List<Double> end = Arrays.asList(
                                getValueOnStep(startValues.get(0), endValues.get(0), stepsCnt.get(0),
                                        Math.max(dragStartX, endX))
                                        .setScale(5, RoundingMode.HALF_UP).doubleValue(),
                                getValueOnStep(startValues.get(1), endValues.get(1), stepsCnt.get(1),
                                        Math.max(stepsCnt.get(1) - dragStartY - 1, stepsCnt.get(1) - endY - 1))
                                        .setScale(5, RoundingMode.HALF_UP).doubleValue()
                        );
                        List<Integer> steps = getListDeepCopy(stepsCnt);
                        for (int i = 0; i < start.size(); i++) {
                            if (start.get(i).equals(end.get(i)))
                                steps.set(i, 1);
                        }

                        ParametricPortrait parametricPortrait = new ParametricPortrait(application, stateSettingsGroup);
                        parametricPortrait.setParameters(commonTask, instances, properties, start, end, steps, scale);

                        SubareaSelectedCallback.selected(parametricPortrait);
                    }
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
         * make new taskCells List
         * @param cols columns count
         * @param rows rows count
         */
        void newTaskCellsList(int cols, int rows) {
            taskCells = new ArrayList<>();
            for (int row = 0; row < rows; row++) {
                List<TaskCell> rowCells = new ArrayList<>();

                for (int col = 0; col < cols; col++) {
                    TaskCell cell = new TaskCell();
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
         * set grid
         * @param cols columns count
         * @param rows rows count
         */
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
    }



    private enum TaskCellType {
        STABLE,
        CYCLIC
    }


    /**
     * class represents view for each task
     */
    private class TaskCell extends GridPane {
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
        private Task task;
        private TaskCellType type = TaskCellType.STABLE;
        private TaskAnalyser taskAnalyser;


        TaskCell() {
            this.numCols = this.numRows = (int)Math.ceil(Math.sqrt(stateSettingsGroup.getStateSettingsList().stream()
                    .filter(StateSettings::getShow)
                    .collect(Collectors.toList())
                    .size()
            ));

            setGrid();

            this.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

            this.setOnMouseClicked(event -> {
                if (task == null || primaryController.isCalculating())
                    return;

                if (event.getButton() == MouseButton.SECONDARY) {
                    // clear graphic and table
                    primaryController.clearResultsTable();
                    primaryController.clearResultsChart();
                    // open graphic tab
                    primaryController.mCalculationsTabPane.getSelectionModel().select(
                            primaryController.mResultChartTab);
                    // save portrait properties selections because they losed when set new task to primaryController
                    Map<ComboBox, Integer> selections = primaryController.mParametricPortraitTabController.getSelectionModel();
                    // need set task steps - 1 because of steps increase in PrimaryController validation
                    Task task = this.getTask();
                    task.setStepsCount(task.getStepsCount() - 1);
                    primaryController.setTask(this.getTask());
                    task.setStepsCount(task.getStepsCount() + 1);
                    primaryController.calculateTask(task);
                    primaryController.mParametricPortraitTabController.setSelectionModel(selections);
                }
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
            /*final TaskAnalyser taskAnalyser = new TaskAnalyser(task);
            taskAnalyser.setStablePrecision(scale);
            taskAnalyser.buildGraph();
            taskAnalyser.setAnalysedStatesList(statesListShownOnParametricPortrait);
            this.taskAnalyser = taskAnalyser;

            Calculator.calculateTaskAnalyser(
                    taskAnalyser,
                    application.getThreadFactory(),
                    resultCallback, progressCallback,
                    (result) -> {
                        setAliveStates(taskAnalyser.getDominantsOrdered());
                        this.type = taskAnalyser.getCalculationFinishedReason() == TaskAnalyser.CalculationFinishedReason.TASK_STABLE ?
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
            Color color = null;
            StateSettings stateSettings = getStateSettingsById(state.getId());
            if (stateSettings != null)
                color = stateSettings.getColor();

            return color;
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
            if (type == TaskCellType.STABLE) {
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



    public interface SubareaSelectedCallback {
        /**
         * called when area in parametric portrait was selected
         * @param parametricPortrait new parametric portrait with new bounds
         */
        void selected(ParametricPortrait parametricPortrait);
    }
}
