package population.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import population.App;
import population.model.ParametricPortrait.ParametricPortrait;
import population.component.parametricPortrait.ParametricPortraitNode;
import population.component.parametricPortrait.ParametricPortraitPropertiesNode;
import population.component.parametricPortrait.StateSettingsTable;
import population.model.ParametricPortrait.PortraitProperties;
import population.model.ColorGenerator.SimpleColorGenerator;
import population.controller.base.AbstractController;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import population.model.ParametricPortrait.StateSetting;


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

    /** calculation precision used in task (digits after comma) */
    private int taskScale;

    private PrimaryController primaryController;

    /** ParametricPortrait shown on scene */
    private ParametricPortraitNode shownParametricPortrait;


    private PortraitProperties portraitProperties;


    /*********************************
     *
     *      PROPERTIES SECTION
     *
     *********************************/

    /**
     * initialize property section
     */
    private void initPropertiesSection() {
        this.portraitProperties = new PortraitProperties();

        ParametricPortraitPropertiesNode propNode = new ParametricPortraitPropertiesNode(App.getTask(), this.portraitProperties.getDimensions());
        propNode.setPortraitProperties(this.portraitProperties);
        portraitPropertiesContainer.getChildren().add(propNode);

        initStateSettingsTable();
    }


    /**
     * initialize state settings table
     */
    private void initStateSettingsTable() {
        StateSettingsTable table = new StateSettingsTable(App.getTask(), this.portraitProperties.stateSettingProperty());
        table.setColorGenerator(new SimpleColorGenerator());

        // TODO bind color property to shown PP
//        table.getItems().addListener((ListChangeListener<? super StateSetting>) c -> {
//            if (this.shownParametricPortrait != null) {
//                this.shownParametricPortrait.redrawCells();
//            }
//        });

        stateSettingsContainer.getChildren().add(table);
    }


    /*********************************
     *
     *      PORTRAIT SECTION
     *
     *********************************/


    /**
     * bind properties to parametric portrait
     */
    private void initParametricPortraitSection() {
        // set parametric portrait size on resize
        ChangeListener<? super Number> changeListener = (observable, oldValue, newValue) -> {
            updateShownParametricPortraitSize();
        };
        parametricPortraitSectionContainer.widthProperty().addListener(changeListener);
        parametricPortraitSectionContainer.heightProperty().addListener(changeListener);
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






    @Override
    public void initialize() {
        initPropertiesSection();
        initParametricPortraitSection();
//        initHistory();
//
        /*Platform.runLater(() ->
                rootSplitPane.setDividerPositions(
                        history.getMinWidth() / rootSplitPane.getWidth(),
                        1 - propertiesSection.getMinWidth() / rootSplitPane.getWidth())
        );*/
//
//
        if (getApplication().IS_DEVELOP) {

        }
    }

    private boolean validateStatesInput() {
        /*if (this.task.getStates().size() == 0) {
            getApplication().showAlert(getString("App.ErrorAlert.Title"),
                    null, getString("states_missing"), Alert.AlertType.WARNING);
            return false;
        }*/
        return true;
    }

    private boolean validateTransitionsInput() {
        /*if (this.task.getTransitions().size() == 0) {
            getApplication().showAlert(getString("App.ErrorAlert.Title"),
                    null, getString("transitions_missing"), Alert.AlertType.WARNING);
            return false;
        }

        for (Transition transition : this.task.getTransitions()) {
            // TODO
            if (transition.getSourceState() == State.UNDEFINED ||
                    transition.getOperandState() == State.UNDEFINED ||
                    transition.getResultState() == State.UNDEFINED) {
                getApplication().showAlert(getString("App.ErrorAlert.Title"), null,
                        getString("transitions_incorrect"), Alert.AlertType.WARNING);
                return false;
            }
        }
*/
        return true;
    }

    private boolean validateStepsCountInput() {
        /*int stepsCount;
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
*/
        return true;
    }

    private boolean validateInstanceInputs() {
        /*try {
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
        }*/
        return true;
    }

    private boolean validatePropertyInputs() {
        /*try {
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
        }*/
        return true;
    }

    private boolean validateStartValueInputs() {
        /*try {
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
        }*/
        return true;
    }

    private boolean validateEndValueInputs() {
        /*try {
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
        }*/
        return true;
    }

    private boolean validateDiscretizationInputs() {
        /*try {
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
        }*/
        return true;
    }

    private boolean validatePrecisionInput() {
        /*try {
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
        }*/
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
        /*if (shownParametricPortrait == null)
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
        }*/
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
        /*for (int i = 0; i < startValueTextFields.size(); i++) {
            instanceComboBoxes.get(i).getSelectionModel().select(parametricPortrait.getInstances().get(i));
            propertyComboBoxes.get(i).getSelectionModel().select(parametricPortrait.getSelectedProperties().get(i));
            startValueTextFields.get(i).setText(parametricPortrait.getStartValues().get(i).toString());
            endValueTextFields.get(i).setText(parametricPortrait.getEndValues().get(i).toString());
            stepCountTextFields.get(i).setText(parametricPortrait.getStepsCnt().get(i).toString());
        }*/
    }


    /**
     * show parametric portrait on scene
     * @param parametricPortrait shownParametricPortrait to show
     */
    private void showParametricPortraitOnScene(ParametricPortrait parametricPortrait) {
        parametricPortraitSection.getChildren().remove(shownParametricPortrait);
//        parametricPortraitSection.getChildren().add(parametricPortrait);
//        shownParametricPortrait = parametricPortrait;
        updateShownParametricPortraitSize();
    }


    /**
     * create new ParametricPortrait and set standard onAreaSelected behavior
     * @return created ParametricPortrait instance
     */
    private ParametricPortrait getNewParametricPortraitInstance() {
        return new ParametricPortrait(App.getTask().clone(), this.portraitProperties.clone());
    }


    /**
     * calculate parametric portrait
     */
    @FXML
    private void calculate() {
        if (!validateUserInput()) {
            return;
        }

        // disable controls
//        primaryController.setCalculating(true);
//        primaryController.mCalculationProgressBar.setProgress(0);
//        setControlsDisable(true);
//
        // create new parametric portrait
        parametricPortraitSection.getChildren().remove(shownParametricPortrait);
        this.shownParametricPortrait = null;

        ParametricPortrait portrait = getNewParametricPortraitInstance();
        portrait.calculate();

        shownParametricPortrait = new ParametricPortraitNode(portrait);
        parametricPortraitSection.getChildren().add(shownParametricPortrait);
        this.updateShownParametricPortraitSize();


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
}
