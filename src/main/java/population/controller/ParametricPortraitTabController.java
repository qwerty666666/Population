package population.controller;

import javafx.collections.FXCollections;
import population.App;
import population.component.Calculator;
import population.component.ParametricPortrait;
import population.component.UIComponents.ColorTableCell;
import population.component.UIComponents.DraggableVerticalScrollPane;
import population.component.UIComponents.IconButton;
import population.component.parametricPortrait.ParametricPortraitPropertiesNode;
import population.component.parametricPortrait.StateSettingsTable;
import population.model.ParametricPortrait.PortraitProperties;
import population.model.ParametricPortrait.SimpleColorGenerator;
import population.model.ParametricPortrait.StateSetting;
import population.controller.base.AbstractController;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.Transition;
import population.util.Utils;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


public class ParametricPortraitTabController extends AbstractController {
    /** parametric portrait min insets in percent*/
    private static final double PARAMETRIC_PORTRAIT_SECTION_INSETS_PERCENTAGE = 0.35;

    @FXML
    private SplitPane rootSplitPane;
    @FXML
    private Button calculateButton;
    @FXML
    private VBox propertiesSection;
    @FXML
    private StackPane historySection;
    @FXML
    private StackPane parametricPortraitSection;
    @FXML
    private StackPane portraitPropertiesContainer;
    @FXML
    private StackPane stateSettingsContainer;
    @FXML
    private ScrollPane parametricPortraitSectionContainer;
    @FXML
    private TextField tfStartValue1;
    @FXML
    private TextField tfStartValue2;
    @FXML
    private TextField tfEndValue1;
    @FXML
    private TextField tfEndValue2;
    @FXML
    private TextField tfStepsCnt1;
    @FXML
    private TextField tfStepsCnt2;
    @FXML
    private TextField tfPrecision;
    @FXML
    private ComboBox<Object> cbInstance1;
    @FXML
    private ComboBox<Object> cbInstance2;
    @FXML
    private ComboBox<String> cbProperty1;
    @FXML
    private ComboBox<String> cbProperty2;
    @FXML
    private TableView<StateSetting> stateSettingsTable;
    @FXML
    private TableColumn<StateSetting, Boolean> stateSettingsTableVisibilityColumn;
    @FXML
    private TableColumn<StateSetting, String> stateSettingsTableNameColumn;
    @FXML
    private TableColumn<StateSetting, Color> stateSettingsTableColorColumn;

    private List<TextField> startValueTextFields = new ArrayList<>();
    private List<TextField> endValueTextFields = new ArrayList<>();
    private List<TextField> stepCountTextFields = new ArrayList<>();
    private List<ComboBox<Object>> instanceComboBoxes = new ArrayList<>();
    private List<ComboBox<String>> propertyComboBoxes = new ArrayList<>();

    /** calculation precision used in task (digits after commma) */
    private int taskScale;

    /** start property value */
    private List<Double> startValues = new ArrayList<>();
    /** end property value */
    private List<Double> endValues = new ArrayList<>();
    /** steps count in parametric portrait */
    private List<Integer> stepsCnt = new ArrayList<>();
    /** selected properties */
    private List<String> properties = new ArrayList<>();
    /** selected instances */
    private List<Object> instances = new ArrayList<>();

    /** List of states and transitions which user can select in instance ComboBoxes */
    private ObservableList<Object> instancesList;

    /** initial task for which parametric portrait properties will be shown */
    private TaskV4 task;
    /** max steps count for task calculating */
    private int calculateStepsCount;

    private PrimaryController primaryController;

    private ParametricPortraitAreaSelectedCallback parametricPortraitAreaSelectedCallback = new ParametricPortraitAreaSelectedCallback();


    /** ParametricPortrait shown on scene */
    private ParametricPortrait shownParametricPortrait;

    /** parametric portraits history */
    private History history = new History();


    /**
     * bind to task mutations
     */
    private void initTask() {
        task = App.getTask();
    }


    /*********************************
     *
     *      PROPERTIES SECTION
     *
     *********************************/

    /**
     * initialize property section
     */
    private void initPropertiesSection() {
        ParametricPortrait parametricPortrait = new ParametricPortrait();
        PortraitProperties portraitProperties = parametricPortrait.getPortraitProperties();
        ParametricPortraitPropertiesNode propNode = new ParametricPortraitPropertiesNode(App.getTask(), portraitProperties.getDimensions());
        propNode.setPortraitProperties(portraitProperties);
        portraitPropertiesContainer.getChildren().add(propNode);

        initStateSettingsTable();
    }













    @Override
    public void initialize() {
        initTask();
        initPropertiesSection();
//        initParametricPortraitSection();
//        initHistory();
//
//        Platform.runLater(() ->
//                rootSplitPane.setDividerPositions(
//                        history.getMinWidth() / rootSplitPane.getWidth(),
//                        1 - propertiesSection.getMinWidth() / rootSplitPane.getWidth())
//        );
//
//
//        if (getApplication().IS_DEVELOP) {
//            test();
//        }
    }


    public void test() {
        primaryController.openTaskFromFile("C:\\Users\\user\\Desktop\\популяция ОНД 1000 тактов.pmt");

        this.cbInstance1.getSelectionModel().select(9);
        this.cbProperty1.getSelectionModel().select(0);

        this.cbInstance2.getSelectionModel().select(13);
        this.cbProperty2.getSelectionModel().select(0);

        //this.primaryController.mStepsCountField.setText("1000");

        //this.tfStartValue1.setText();

        calculate();
    }


    /**
     * initialize state settings table
     */
    private void initStateSettingsTable() {
        StateSettingsTable table = new StateSettingsTable(App.getTask());
        table.setColorGenerator(new SimpleColorGenerator());
        stateSettingsContainer.getChildren().add(table);
    }




    /**
     * get state from states which id equal id
     * @param states states list
     * @param id searching id
     * @return state if exist, null otherwise
     */
    private static State getStateById(List<State> states, int id) {
        return states.stream()
            .filter(x -> x.getId() == id)
            .findFirst()
            .orElse(null);
    }





    public Map<ComboBox, Integer> getSelectionModel() {
        Map<ComboBox, Integer> result = new HashMap<>();
        for (ComboBox<Object> cbInstance : instanceComboBoxes) {
            result.put(cbInstance, cbInstance.getSelectionModel().getSelectedIndex());
        }
        for (ComboBox<String> cbProperty : propertyComboBoxes) {
            result.put(cbProperty, cbProperty.getSelectionModel().getSelectedIndex());
        }
        return result;
    }

    public void setSelectionModel(Map<ComboBox, Integer> map) {
        List<ComboBox> instances = map.keySet().stream()
            .filter(x -> instanceComboBoxes.contains(x))
            .collect(Collectors.toList());
        for (ComboBox cb: instances) {
            cb.getSelectionModel().select((int)map.get(cb));
        }

        List<ComboBox> properties = map.keySet().stream()
            .filter(x -> propertyComboBoxes.contains(x))
            .collect(Collectors.toList());
        for (ComboBox cb: properties) {
            cb.getSelectionModel().select((int)map.get(cb));
        }
    }


    /**
     * set parametric portrait available size
     */
    private void updateShownParametricPortraitSize() {
        if (shownParametricPortrait != null) {
            shownParametricPortrait.setAvailableSize(
                parametricPortraitSectionContainer.getViewportBounds().getWidth()
                    * (1 - PARAMETRIC_PORTRAIT_SECTION_INSETS_PERCENTAGE),
                parametricPortraitSectionContainer.getViewportBounds().getHeight()
                    * (1 - PARAMETRIC_PORTRAIT_SECTION_INSETS_PERCENTAGE)
            );
        }
    }


    /**
     * bind properties to parametric portrait
     */
    private void initParametricPortraitSection() {
        // set parametric portrait size on resize
        ChangeListener<? super Number> changeListener = (observable, oldValue, newValue) -> updateShownParametricPortraitSize();
        parametricPortraitSectionContainer.widthProperty().addListener(changeListener);
        parametricPortraitSectionContainer.heightProperty().addListener(changeListener);
    }


    /**
     * initialize history section
     */
    private void initHistory() {
        history = new History();
        historySection.getChildren().add(history);
        history.setFitToWidth(true);
        history.setFitToHeight(true);
        history.setPadding(new Insets(20));

        /*for (int i = 0; i < 10; i++)
            history.addItem(0, new Label(Integer.toString(i)));*/
    }


    private boolean validateStatesInput() {
        if (this.task.getStates().size() == 0) {
            getApplication().showAlert(getString("App.ErrorAlert.Title"),
                    null, getString("states_missing"), Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateTransitionsInput() {
        if (this.task.getTransitions().size() == 0) {
            getApplication().showAlert(getString("App.ErrorAlert.Title"),
                    null, getString("transitions_missing"), Alert.AlertType.WARNING);
            return false;
        }

        for (Transition transition : this.task.getTransitions()) {
            // TODO
            /*if (transition.getSourceState() == State.UNDEFINED ||
                    transition.getOperandState() == State.UNDEFINED ||
                    transition.getResultState() == State.UNDEFINED) {
                getApplication().showAlert(getString("App.ErrorAlert.Title"), null,
                        getString("transitions_incorrect"), Alert.AlertType.WARNING);
                return false;
            }*/
        }

        return true;
    }

    private boolean validateStepsCountInput() {
        int stepsCount;
        try {
            stepsCount = Integer.parseInt(primaryController.mStepsCountField.getText());
            if (stepsCount < 1 || stepsCount == Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null, getString("steps_count_invalid"),
                            Alert.AlertType.WARNING);
            return false;
        }
        this.calculateStepsCount = stepsCount + 1;

        return true;
    }

    private boolean validateInstanceInputs() {
        try {
            for (Object o : instances) {
                if (o == null)
                    throw new NullPointerException();
            }
        } catch (Exception e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_invalid_instance"),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validatePropertyInputs() {
        try {
            int i = 0;
            for (ComboBox<String> cb: propertyComboBoxes) {
                if (cb.getValue().equals(""))
                    throw new NullPointerException();
                properties.set(i++, cb.getValue());
            }
        } catch (Exception e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_invalid_property"),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateStartValueInputs() {
        try {
            double propertyValue;
            int i = 0;

            for (TextField tf: startValueTextFields) {
                String property = propertyComboBoxes.get(i).getValue();
                if (property.equals(ParametricPortrait.Property.SOURCE_DELAY)
                        || property.equals(ParametricPortrait.Property.STATE_IN))
                    propertyValue = Integer.parseInt(tf.getText());
                else
                    propertyValue = Double.parseDouble(tf.getText());
                if (propertyValue < 0) {
                    throw new IllegalArgumentException();
                }
                startValues.set(i++, propertyValue);
            }
        }
        catch (NumberFormatException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_invalid_start_value") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_illegal_start_value"),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateEndValueInputs() {
        try {
            double propertyValue;
            int i = 0;

            for (TextField tf: endValueTextFields) {
                String property = propertyComboBoxes.get(i).getValue();
                if (property.equals(ParametricPortrait.Property.SOURCE_DELAY)
                        || property.equals(ParametricPortrait.Property.STATE_IN))
                    propertyValue = Integer.parseInt(tf.getText());
                else
                    propertyValue = Double.parseDouble(tf.getText());

                if (propertyValue < 0) {
                    throw new IllegalArgumentException("parametric_portrait_illegal_end_value");
                }
                if (propertyValue < startValues.get(i)) {
                    throw new IllegalArgumentException("parametric_portrait_end_value_lower_than_start_value");
                }
                if ( ( property.equals(ParametricPortrait.Property.SOURCE_DELAY)
                        || property.equals(ParametricPortrait.Property.STATE_IN) )
                        && ((propertyValue - startValues.get(i)) % (stepsCnt.get(i) - 1) != 0)
                        )
                    throw new IllegalArgumentException("parametric_portrait_unable_discretize");

                endValues.set(i++, propertyValue);
            }
        }
        catch (NumberFormatException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_invalid_end_value") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null, getString(e.getMessage()),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateDiscretizationInputs() {
        try {
            int steps;
            int i = 0;
            for (TextField tf: stepCountTextFields) {
                steps = Integer.parseInt(tf.getText());
                if (steps <= 0) {
                    throw new IllegalArgumentException("parametric_portrait_illegal_steps_count");
                } else if (steps == 1 && !startValues.get(i).equals(endValues.get(i))) {
                    throw new IllegalArgumentException("parametric_portrait_same_start_and_end_values");
                } else if (startValues.get(i).equals(endValues.get(i)) && steps != 1) {
                    throw new IllegalArgumentException("parametric_portrait_same_start_and_end_values");
                }
                stepsCnt.set(i++, steps);
            }
        }
        catch (NumberFormatException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_invalid_steps_count") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null, getString(e.getMessage()),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validatePrecisionInput() {
        try {
            this.taskScale = Integer.parseInt(tfPrecision.getText());
            if (taskScale < 0
                    || taskScale > Calculator.HIGHER_ACCURACY_SCALE
                    || (!primaryController.mHigherAccuracy.isSelected() && taskScale > Utils.MAX_PRECISION)
            ) {
                throw new IllegalArgumentException("parametric_portrait_illegal_precision");
            }
            if (taskScale == 0)
                taskScale = primaryController.mHigherAccuracy.isSelected() ?
                        Calculator.HIGHER_ACCURACY_SCALE :
                        Utils.MAX_PRECISION;
        }
        catch (NumberFormatException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_illegal_precision") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("App.ErrorAlert.Title"), null,
                            getString("parametric_portrait_illegal_precision") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    /**
     * test calculation conditions for calculateParametricPortrait portrait
     * @return true if al values valid, else show alert and return false
     */
    private boolean validateUserInput() {
        if (!validateStatesInput())
            return false;
        if (!validateTransitionsInput())
            return false;
        if (!validateStepsCountInput())
            return false;
        if (!validateInstanceInputs())
            return false;
        if (!validatePropertyInputs())
            return false;
        if (!validateStartValueInputs())
            return false;
        if (!validateEndValueInputs())
            return false;
        if (!validateDiscretizationInputs())
            return false;
        if (!validatePrecisionInput()) {
            return false;
        }
        return true;
    }


    /**
     * enable / disable users controls
     * @param disable should disable
     */
    private void setControlsDisable(boolean disable) {
        primaryController.mCalculationProgressBar.setVisible(disable);

        calculateButton.setDisable(disable);
        primaryController.setControlsDisable(disable);
    }


    /**
     * remove parametric portrait. Set new stateSettingsGroup.
     */
    void clearEnvironment() {
        parametricPortraitSection.getChildren().remove(shownParametricPortrait);
    }


    /**
     * save shownParametricPortrait to .png file
     */
    @FXML
    private void save() {
        if (shownParametricPortrait == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(getString("parametric_portrait_save_title"));
        String workDirectory = getApplication().getWorkDirectory();
        if (workDirectory != null) {
            File file = new File(workDirectory);
            if (file.canWrite()) {
                fileChooser.setInitialDirectory(file);
            }
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "png files (*.png)", "*.png"));

        File file = fileChooser.showSaveDialog(this.getStage().getOwner());
        if(file != null){
            try {
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(
                        shownParametricPortrait.takeSnapshot(
                                shownParametricPortrait.getMaxPortraitWidth(),
                                shownParametricPortrait.getMaxPortraitHeight(),
                                true
                        ), null);
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * set application task and properties from parametricPortrait
     * @param parametricPortrait parametric portrait
     */
    private void setEnvironmentByParametricPortrait(ParametricPortrait parametricPortrait) {
        // set application task from parametricPortrait
        // there is no need update settingsGroup, so remove change listener while updated main states table
        // TODO
        /*primaryController.getStates().removeListener(updateStateSettingsOnStateChangeListener);
        Task task = parametricPortrait.getCommonTask();
        // steps count field displays number stepsCount-1
        task.setStepsCount(task.getStepsCount() - 1);
        primaryController.setTask(task);
        task.setStepsCount(task.getStepsCount() + 1);
        primaryController.getStates().addListener(updateStateSettingsOnStateChangeListener);*/

        // set properties from parametricPortrait
        for (int i = 0; i < startValueTextFields.size(); i++) {
            instanceComboBoxes.get(i).getSelectionModel().select(parametricPortrait.getInstances().get(i));
            propertyComboBoxes.get(i).getSelectionModel().select(parametricPortrait.getSelectedProperties().get(i));
            startValueTextFields.get(i).setText(parametricPortrait.getStartValues().get(i).toString());
            endValueTextFields.get(i).setText(parametricPortrait.getEndValues().get(i).toString());
            stepCountTextFields.get(i).setText(parametricPortrait.getStepsCnt().get(i).toString());
        }
    }


    /**
     * show parametric portrait on scene
     * @param parametricPortrait shownParametricPortrait to show
     */
    private void showParametricPortraitOnScene(ParametricPortrait parametricPortrait) {
        parametricPortraitSection.getChildren().remove(shownParametricPortrait);
        parametricPortraitSection.getChildren().add(parametricPortrait);
        shownParametricPortrait = parametricPortrait;
        updateShownParametricPortraitSize();
    }


    /**
     * create new ParametricPortrait and set standard onAreaSelected behavior
     * @return created ParametricPortrait instance
     */
    private ParametricPortrait getNewParametricPortraitInstance() {
        ParametricPortrait parametricPortrait = new ParametricPortrait();
        parametricPortrait.setSubareaSelectedCallback(parametricPortraitAreaSelectedCallback);
        return parametricPortrait;
    }


    /**
     * calculate parametric portrait
     */
    @FXML
    private void calculate() {
        if (!validateUserInput())
            return;

        // disable controls
        primaryController.setCalculating(true);
        primaryController.mCalculationProgressBar.setProgress(0);
        setControlsDisable(true);

        // create new parametric portrait
        parametricPortraitSection.getChildren().remove(shownParametricPortrait);

        shownParametricPortrait = getNewParametricPortraitInstance();


        // TODO
        /*javafx.concurrent.Task<Void> calculationTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Task commonTask = new Task();
                    commonTask.setStates(states);
                    commonTask.setTransitions(transitions);
                    commonTask.setStepsCount(calculateStepsCount);
                    commonTask.setStartPoint(0);
                    commonTask.setParallel(primaryController.mParallel.isSelected());
                    commonTask.setAllowNegative(false);
                    //commonTask.setScale(taskScale);
                    shownParametricPortrait.calculate(commonTask, instances, properties, startValues, endValues, stepsCnt, taskScale);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        };


        calculationTask.setOnSucceeded( (WorkerStateEvent event) -> {
            // show parametric portrait
            shownParametricPortrait.updateView();
            showParametricPortraitOnScene(shownParametricPortrait);

            // add to history
            stateSettingsGroup.add(shownParametricPortrait);
            history.add(shownParametricPortrait);
            history.select(0);

            // enable controls
            setControlsDisable(false);
            primaryController.setCalculating(false);
        });


        new Thread(calculationTask).start();
*/
    }


    /**
     * clear history
     */
    @FXML
    private void clearHistory() {
        history.clear();
    }



    /**
     * history block view
     */
    private class History extends DraggableVerticalScrollPane {
        /** selected item in history */
        private HistoryItem selectedItem = null;


        History() {
            super();
            content.setSpacing(10);
            this.setMinWidth(240);
        }


        /**
         * set selected item
         * @param item selected item
         */
        void select(HistoryItem item) {
            if (selectedItem != null)
                selectedItem.thumbnail.getStyleClass().remove("active");

            item.thumbnail.getStyleClass().add("active");
            selectedItem = item;
        }

        void select(int ind) {
            if (ind >= 0 && ind < content.getChildren().size())
                select((HistoryItem)content.getChildren().filtered(x -> x instanceof HistoryItem).get(ind));
        }


        /**
         * add parametric portrait to history
         * @param parametricPortrait parametric portrait to add
         */
        void add(ParametricPortrait parametricPortrait) {
            HistoryItem item = new HistoryItem(parametricPortrait);
            addItem(0, item);

            // apply drag ability to item thumbnail
            item.thumbnail.setOnDragDetected(item.getOnDragDetected());
        }


        /**
         * remove historyItem from history
         * @param item historyItem to remove
         */
        void remove(HistoryItem item) {
            content.getChildren().remove(item);
        }


        /**
         * remove all items from history
         */
        void clear() {
            content.getChildren().clear();
        }


        /**
         * update parametric portraits in history in stateSettingsGroup group
         */
        void update() {
            // TODO
            /*content.getChildren().stream()
                    .filter(x -> x instanceof HistoryItem)
                    .map(HistoryItem.class::cast)
                    .filter(item -> item.getParametricPortrait().getStateSettingsGroup() == stateSettingsGroup)
                    .forEach(HistoryItem::updateNailImage);*/
        }




        /**
         * represents item in history
         */
        private class HistoryItem extends StackPane {
            /** snapshot thumbnail width */
            private static final double THUMBNAIL_WIDTH = 110;
            /** snapshot thumbnail height */
            private static final double THUMBNAIL_HEIGHT = 110;
            private ParametricPortrait parametricPortrait;
            /** button for ParametricPortrait thumbnail */
            private IconButton thumbnail;


            HistoryItem(ParametricPortrait parametricPortrait) {
                this.parametricPortrait = parametricPortrait;
                setNail();
                setDeleteButton();
            }

            /**
             * add delete button to item
             */
            private void setDeleteButton() {
                ImageView iv = new ImageView(new Image(ParametricPortraitTabController.class
                        .getResourceAsStream("/com//population/resource/images/remove-icon.png")));
                iv.setFitHeight(17);
                iv.setFitWidth(17);
                IconButton deleteButton = new IconButton(iv);
                deleteButton.setPadding(new Insets(3));
                deleteButton.setOnMouseClicked(event -> remove(this));

                // set button position to right side of parametricPortrait thumbnail
                setAlignment(deleteButton, Pos.TOP_LEFT);
                ChangeListener<? super Number> listener = (observable, oldValue, newValue) ->
                    setMargin(deleteButton, new Insets(
                            (getHeight() - thumbnail.getHeight()) / 2 + 5,
                            0, 0,
                            (getWidth()  + thumbnail.getWidth()) / 2 + 5)
                    );
                this.widthProperty().addListener(listener);
                this.heightProperty().addListener(listener);
                thumbnail.widthProperty().addListener(listener);
                thumbnail.heightProperty().addListener(listener);

                this.getChildren().add(deleteButton);
            }

            /**
             * add thumbnail button to item
             */
            private void setNail() {
                ImageView iv = new ImageView(parametricPortrait.getThumbnail());
                iv.setFitWidth(THUMBNAIL_WIDTH);
                iv.setFitHeight(THUMBNAIL_HEIGHT);
                thumbnail = new IconButton(iv);

                // put thumbnail to container to apply rounded border to the thumbnail
                BorderPane container = new BorderPane(iv);
                Rectangle clip = new Rectangle(iv.getFitWidth(), iv.getFitHeight());
                clip.setArcWidth(4);
                clip.setArcHeight(4);
                iv.setClip(clip);
                container.setBorder(new Border(new BorderStroke(Color.valueOf("#ccc"),
                        BorderStrokeStyle.SOLID, new CornerRadii(3), BorderWidths.DEFAULT)));
                thumbnail.setGraphic(container);

                // on click show parametric portrait on scene
                thumbnail.setOnMouseClicked(event -> {
                    history.select(this);
                    setEnvironmentByParametricPortrait(parametricPortrait);
                    showParametricPortraitOnScene(parametricPortrait);
                    event.consume();
                });

                this.getChildren().add(thumbnail);
            }

            /**
             * update thumbnail view
             */
            void updateNailImage() {
                thumbnail.getImageView().setImage(parametricPortrait.getThumbnail());
            }

            /**
             *
             * @return parametric portrait associated with item
             */
            ParametricPortrait getParametricPortrait() {
                return parametricPortrait;
            }
        }
    }



    /**
     * on area selected show parametric portrait on scene and update user inputs and task tables
     */
    private class ParametricPortraitAreaSelectedCallback implements ParametricPortrait.SubareaSelectedCallback {
        @Override
        public void selected(ParametricPortrait parametricPortrait) {
            // show parametric portrait
            setEnvironmentByParametricPortrait(parametricPortrait);
            calculate();
        }
    }
}
