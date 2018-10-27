package population.controller;

import javafx.application.Platform;
import javafx.concurrent.WorkerStateEvent;
import population.App;
import population.component.parametricPortrait.History.History;
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

    /** ParametricPortrait shown on scene */
    private ParametricPortraitNode shownParametricPortrait;

    private ParametricPortraitPropertiesNode propertiesNode;
    private StateSettingsTable stateSettingsTable;

    private History history;


    /*********************************
     *
     *      PROPERTIES SECTION
     *
     *********************************/

    /**
     * initialize property section
     */
    private void initPropertiesSection() {
        this.propertiesNode = new ParametricPortraitPropertiesNode(App.getTask());
        portraitPropertiesContainer.getChildren().add(this.propertiesNode);

        initStateSettingsTable();
    }


    /**
     * @return PortraitProperties binded to this.propertiesNode
     */
    private PortraitProperties getPortraitProperties() {
        return this.propertiesNode.getPortraitProperties();
    }


    /**
     * initialize state settings table
     */
    private void initStateSettingsTable() {
        this.stateSettingsTable = new StateSettingsTable(App.getTask(), this.getPortraitProperties().stateSettingProperty());
        this.stateSettingsTable.setColorGenerator(new SimpleColorGenerator());

        stateSettingsContainer.getChildren().add(this.stateSettingsTable);

        // TODO bind color property to shown PP
//        table.getItems().addListener((ListChangeListener<? super StateSetting>) c -> {
//            if (this.shownParametricPortrait != null) {
//                this.shownParametricPortrait.redrawCells();
//            }
//        });
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


    /*********************************
     *
     *      HISTORY SECTION
     *
     *********************************/

    /**
     * Initialize history
     */
    private void initHistory() {
        this.history = new History();
        this.historySection.getChildren().add(history);

        this.history.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.setEnvironmentByParametricPortrait(newValue.getParametricPortraitNode().getParametricPortrait());
            }
        });
    }


    /**
     * Clear whole history
     */
    @FXML
    private void clearHistory() {
        this.history.clear();
    }


    @Override
    public void initialize() {
        initPropertiesSection();
        initParametricPortraitSection();
        initHistory();
//
        Platform.runLater(() ->
                rootSplitPane.setDividerPositions(
                        history.getMinWidth() / rootSplitPane.getWidth(),
                        1 - propertiesSection.getMinWidth() / rootSplitPane.getWidth())
        );
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
     * Save shownParametricPortrait to .png file
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
     * Set application task and portrait properties from given portrait
     *
     * @param parametricPortrait parametric portrait
     */
    private void setEnvironmentByParametricPortrait(ParametricPortrait parametricPortrait) {
        // TODO remove it and set colors from portrait properties state
        this.stateSettingsTable.setColorGenerator(new SimpleColorGenerator());

        // set application task from parametricPortrait
        App.setTask(parametricPortrait.getTask());

        // set properties from parametricPortrait
        this.propertiesNode.setPortraitPropertiesValues(parametricPortrait.getProperties());

        // show PP Node
        this.setShownParametricPortraitNode(new ParametricPortraitNode(parametricPortrait));
    }


    /**
     * Create new ParametricPortrait and set standard onAreaSelected behavior
     *
     * @return created ParametricPortrait instance
     */
    private ParametricPortrait getNewParametricPortraitInstance() {
        return new ParametricPortrait(App.getTask().clone(), this.getPortraitProperties().clone());
    }


    /**
     * Calculate parametric portrait and show it on scene
     */
    @FXML
    private void calculate() {
        if (!validateUserInput()) {
            return;
        }

        // remove all PP Node
        parametricPortraitSection.getChildren().remove(shownParametricPortrait);
        this.shownParametricPortrait = null;

        // TODO show preloader

        // create new parametric portrait
        ParametricPortrait portrait = getNewParametricPortraitInstance();

        // and calculate async
        javafx.concurrent.Task<Void> calculationTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    portrait.calculate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        calculationTask.setOnSucceeded( (WorkerStateEvent event) -> {
            Platform.runLater(() -> {
                // TODO hide preloader

                // update properties from PP props
                this.setShownParametricPortraitNode(new ParametricPortraitNode(portrait));

                // push to history
                this.history.push(shownParametricPortrait);
                this.history.select(0);
            });
        });

        new Thread(calculationTask).start();
    }


    /**
     * Set currently shown ParametricPortraitNode
     */
    private void setShownParametricPortraitNode(ParametricPortraitNode node) {
        // remove previous node
        parametricPortraitSection.getChildren().clear();

        // show new one
        shownParametricPortrait = node;
        parametricPortraitSection.getChildren().add(shownParametricPortrait);

        // and force available size update
        this.updateShownParametricPortraitSize();
    }
}
