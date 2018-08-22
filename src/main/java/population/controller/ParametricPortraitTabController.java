package population.controller;

import population.component.Calculator;
import population.component.ParametricPortrait;
import population.component.UIComponents.ColorTableCell;
import population.component.UIComponents.DraggableVerticalScrollPane;
import population.component.UIComponents.IconButton;
import population.controller.base.AbstractController;
import population.model.State;
import population.model.Task;
import population.model.Transition;
import population.util.Utils;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
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
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    private TableView<StateSettings> stateSettingsTable;
    @FXML
    private TableColumn<StateSettings, Boolean> stateSettingsTableVisibilityColumn;
    @FXML
    private TableColumn<StateSettings, String> stateSettingsTableNameColumn;
    @FXML
    private TableColumn<StateSettings, Color> stateSettingsTableColorColumn;

    private List<TextField> tfStartValues = new ArrayList<>();
    private List<TextField> tfEndValues = new ArrayList<>();
    private List<TextField> tfStepsCnt = new ArrayList<>();
    private List<ComboBox<Object>> cbInstances = new ArrayList<>();
    private List<ComboBox<String>> cbProperties = new ArrayList<>();

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

    /** task states */
    private ObservableList<State> states;
    /** task transition */
    private ObservableList<Transition> transitions;
    /** max steps count for task calculating */
    private int calculateStepsCount;

    private PrimaryController primaryController;

    private ParametricPortraitAreaSelectedCallback parametricPortraitAreaSelectedCallback = new ParametricPortraitAreaSelectedCallback();

    /** default colors used to init colors in StateSettingsGroup */
    private List<Color> defaultColors;

    /** ParametricPortrait shown on scene */
    private ParametricPortrait shownParametricPortrait;
    /** current stateSettingsGroup */
    private StateSettingsGroup stateSettingsGroup;

    /** parametric portraits history */
    private History history = new History();




    /**
     * initialize state settings table
     */
    private void initStateSettingsTable() {
        stateSettingsGroup = new StateSettingsGroup();
        stateSettingsTable.setItems(stateSettingsGroup.getStateSettingsList());

        stateSettingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        stateSettingsTableNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        stateSettingsTableNameColumn.setCellValueFactory(param -> param.getValue().getState().nameProperty());

        stateSettingsTableVisibilityColumn.setCellFactory(
                CheckBoxTableCell.forTableColumn(stateSettingsTableVisibilityColumn));
        stateSettingsTableVisibilityColumn.setCellValueFactory(param -> param.getValue().showProperty());

        stateSettingsTableColorColumn.setCellFactory(param -> new ColorTableCell<>(stateSettingsTableColorColumn));
        stateSettingsTableColorColumn.setCellValueFactory(param -> param.getValue().colorProperty());
        stateSettingsTableColorColumn.setOnEditCommit(t -> {
            StateSettings stateSettings = t.getTableView().getItems().get(t.getTablePosition().getRow());
            stateSettings.setColor(t.getNewValue());
            stateSettingsGroup.updateGroup();
            history.update(stateSettingsGroup);
        });
    }


    /**
     * bind changes from states to stateSettingsGroup
     */
    private ListChangeListener<State> updateStateSettingsOnStateChangeListener = new ListChangeListener<State>() {
        @Override
        public void onChanged(Change<? extends State> c) {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        stateSettingsGroup.getStateSettingsList().get(i).setState(c.getList().get(i));
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<StateSettings> copy = new ArrayList<>(stateSettingsGroup.getStateSettingsList().subList(from, to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        stateSettingsGroup.getStateSettingsList().set(newIndex, copy.get(oldIndex - from));
                    }

                } else {
                    int i = 0;
                    for (State state: c.getAddedSubList()) {
                        stateSettingsGroup.add(c.getFrom() + i, state);
                        i++;
                    }

                    for (i = c.getRemovedSize() - 1; i >= 0; i--) {
                        stateSettingsGroup.getStateSettingsList().remove(c.getFrom() + i);
                    }
                }
            }
        }
    };

    /**
     * bind changes from transitions to instanceList
     */
    private ListChangeListener<Object> updateInstanceListOnTransitionChangeListener = new ListChangeListener<Object>() {
        @Override
        public void onChanged(Change<?> c) {
            int startInd = states.size();

            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        instancesList.set(startInd + i, c.getList().get(i));
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<Object> copy = new ArrayList<>(instancesList.subList(startInd + from, startInd + to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        instancesList.set(startInd + newIndex, copy.get(oldIndex - from));
                    }

                } else if (c.wasAdded()) {
                    int i = 0;
                    for (Object o: c.getAddedSubList()) {
                        instancesList.add(c.getFrom() + i + startInd, o);
                        i++;
                    }
                } else if (c.wasRemoved()) {
                    instancesList.remove(c.getFrom() + startInd, c.getFrom() + startInd + c.getRemovedSize());
                }
            }
        }
    };

    /**
     * bind changes from states to instanceList
     */
    private ListChangeListener<Object> updateInstanceListOnStateChangeListener = new ListChangeListener<Object>() {
        @Override
        public void onChanged(Change<?> c) {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        instancesList.set(i, c.getList().get(i));
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<Object> copy = new ArrayList<>(instancesList.subList(from, to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        instancesList.set(newIndex, copy.get(oldIndex - from));
                    }

                } else if (c.wasAdded()) {
                    int i = 0;
                    for (Object o: c.getAddedSubList()) {
                        instancesList.add(c.getFrom() + i, o);
                        i++;
                    }
                } else if (c.wasRemoved()) {
                    instancesList.remove(c.getFrom(), c.getFrom() + c.getRemovedSize());
                }
            }
        }
    };


    /**
     * convert instance to string
     * @param states StateModel List. Used for map transition's states id
     * @param instance converted object
     * @return instance string
     */
    public static String getInstanceString(List<State> states, Object instance) {
        if (instance instanceof State)
            return (((State) instance).getName());
        else if (instance instanceof Transition) {
            return (getTransitionString(states, (Transition) instance));
        }
        return "";
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


    /**
     * init instance ComboBoxes
     */
    private void initInstancesComboBoxes() {
        instancesList = FXCollections.observableList(new ArrayList<>(),
                (Object o) -> {
                    List<Observable> list = new ArrayList<>();
                    if (o instanceof State)
                        list.add(((State)o).nameProperty());
                    if (o instanceof Transition) {
                        Collections.addAll(list,
                                ((Transition) o).sourceStateProperty(),
                                ((Transition) o).operandStateProperty(),
                                ((Transition) o).resultStateProperty(),
                                ((Transition) o).probabilityProperty()
                        );
                    }
                    Observable[] res = new Observable[list.size()];
                    return list.toArray(res);
                });

        StringConverter<Object> stringConverter = new StringConverter<Object>() {
            @Override
            public String toString(Object item) {
                return getInstanceString(states, item);
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        };

        for (int i = 0; i < cbInstances.size(); i++) {
            ComboBox<Object> cbInstance = cbInstances.get(i);
            cbInstance.setConverter(stringConverter);
            cbInstance.setItems(instancesList);

            final int finalInd = i;
            cbInstance.getSelectionModel().selectedIndexProperty().addListener(
                    (observable, oldValue, newValue) -> instances.set(finalInd, cbInstances.get(finalInd).getValue())
            );
        }
    }


    /**
     * initialize properties ComboBoxes
     */
    private void initPropertiesComboBoxes() {
        final ObservableList<String> transitionChooseList = FXCollections.observableArrayList(
                TransitionPropertyChooseList.PROBABILITY,
                TransitionPropertyChooseList.SOURCE_DELAY,
                TransitionPropertyChooseList.OPERAND_DELAY,
                TransitionPropertyChooseList.SOURCE_COEFF,
                TransitionPropertyChooseList.OPERAND_COEFF,
                TransitionPropertyChooseList.RESULT_COEFF
        );
        final ObservableList<String> stateChooseList = FXCollections.observableArrayList(
                StatePropertyChooseList.COUNT
        );

        StringConverter<String> stringConverter = new StringConverter<String>() {
            @Override
            public String toString(String item) {
                return getString(item);
            }

            @Override
            public String fromString(String userId) {
                return null;
            }
        };

        for (int i = 0; i < cbInstances.size(); i++) {
            final int finalInd = i;
            cbProperties.get(i).setConverter(stringConverter);
            cbInstances.get(i).getSelectionModel().selectedIndexProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue.intValue() < 0) {
                            cbProperties.get(finalInd).setItems(FXCollections.observableArrayList());
                            return;
                        }
                        if (newValue.intValue() < states.size())
                            cbProperties.get(finalInd).setItems(stateChooseList);
                        else
                            cbProperties.get(finalInd).setItems(transitionChooseList);
                    }
            );

        }
    }


    public Map<ComboBox, Integer> getSelectionModel() {
        Map<ComboBox, Integer> result = new HashMap<>();
        for (ComboBox<Object> cbInstance : cbInstances) {
            result.put(cbInstance, cbInstance.getSelectionModel().getSelectedIndex());
        }
        for (ComboBox<String> cbProperty : cbProperties) {
            result.put(cbProperty, cbProperty.getSelectionModel().getSelectedIndex());
        }
        return result;
    }

    public void setSelectionModel(Map<ComboBox, Integer> map) {
        List<ComboBox> instances = map.keySet().stream()
                .filter(x -> cbInstances.contains(x))
                .collect(Collectors.toList());
        for (ComboBox cb: instances) {
            cb.getSelectionModel().select((int)map.get(cb));
        }

        List<ComboBox> properties = map.keySet().stream()
                .filter(x -> cbProperties.contains(x))
                .collect(Collectors.toList());
        for (ComboBox cb: properties) {
            cb.getSelectionModel().select((int)map.get(cb));
        }
    }


    /**
     * transition property items shown in Properties ComboBoxes
     */
    public static class TransitionPropertyChooseList {
        public final static String PROBABILITY = "transition_probability";
        public final static String SOURCE_DELAY = "transition_source_delay";
        public final static String OPERAND_DELAY = "transition_operand_delay";
        public final static String SOURCE_COEFF = "transition_source_coefficient";
        public final static String OPERAND_COEFF = "transition_operand_coefficient";
        public final static String RESULT_COEFF = "transition_result_coefficient";
    }

    /**
     * state property items shown in Properties ComboBoxes
     */
    public static class StatePropertyChooseList {
        public final static String COUNT = "state_count";
    }


    /**
     * set primaryController. Bind task properties to ComboBoxes
     */
    private void initPrimaryController() {
        this.primaryController = getApplication().getPrimaryController();
        states = primaryController.getStates();
        transitions = primaryController.getTransitions();

        states.addListener(updateInstanceListOnStateChangeListener);
        states.addListener(updateStateSettingsOnStateChangeListener);
        transitions.addListener(updateInstanceListOnTransitionChangeListener);
    }


    /**
     * get string from transition
     * @param transition transition
     * @return string from transition
     */
    private static String getTransitionString(List<State> states, Transition transition) {
        if (transition == null)
            return "";

        StringBuilder sb = new StringBuilder();
        State s = getStateById(states, transition.getSourceState());
        sb.append(s != null ? s.getName() : "");
        sb.append(": ");

        s = getStateById(states, transition.getOperandState());
        sb.append(s != null ? s.getName() : "");
        sb.append(" -> ");

        s = getStateById(states, transition.getResultState());
        sb.append(s != null ? s.getName() : "");
        //sb.append("; ");

        //sb.append(transition.getProbability());

        return sb.toString();
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


    /**
     * set defaultColors from javafx.scene.paint.Color constants
     */
    private void initDefaultColors() {
        List<Color> customColors = new ArrayList<>(Arrays.asList(new Color[]{
                Color.RED,
                Color.YELLOW,
                Color.GREEN,
                Color.BLUE,
                Color.LIME,
                Color.FIREBRICK,
                Color.DARKMAGENTA,
                Color.MAROON
        }));

        try {
            List<Color> colors = new ArrayList<>();
            Class colorClass = Class.forName("javafx.scene.paint.Color");

            if (colorClass == null) {
                throw new ClassNotFoundException();
            }

            Field[] fields = colorClass.getFields();
            for (Field field : fields) {
                Object obj = field.get(null);
                if (obj instanceof Color) {
                    colors.add((Color) obj);
                }
            }
            defaultColors = colors;

            // use custom colors first
            defaultColors.remove(Color.TRANSPARENT);
            int i = 0;
            for (Color color: customColors) {
                Collections.swap(defaultColors, i++, defaultColors.indexOf(color));
            }

        } catch (Exception e) {
            defaultColors = customColors;
        }
    }


    /**
     * initialize property section
     */
    private void initPropertiesSection() {
        tfStartValues.addAll(Arrays.asList(tfStartValue1, tfStartValue2));
        startValues = Arrays.asList(new Double[tfStartValues.size()]);

        tfEndValues.addAll(Arrays.asList(tfEndValue1, tfEndValue2));
        endValues = Arrays.asList(new Double[tfEndValues.size()]);

        tfStepsCnt.addAll(Arrays.asList(tfStepsCnt1, tfStepsCnt2));
        stepsCnt = new ArrayList<>(Collections.nCopies(tfStepsCnt.size(), 0));

        cbProperties.addAll(Arrays.asList(cbProperty1, cbProperty2));
        properties = Arrays.asList(new String[cbProperties.size()]);

        cbInstances.addAll(Arrays.asList(cbInstance1, cbInstance2));
        instances = Arrays.asList(new Object[cbInstances.size()]);

        initInstancesComboBoxes();
        initPropertiesComboBoxes();
        initStateSettingsTable();
    }


    @Override
    public void initialize() {
        initPrimaryController();
        initDefaultColors();
        initPropertiesSection();
        initParametricPortraitSection();
        initHistory();

        Platform.runLater(() ->
                rootSplitPane.setDividerPositions(
                        history.getMinWidth() / rootSplitPane.getWidth(),
                        1 - propertiesSection.getMinWidth() / rootSplitPane.getWidth())
        );


        if (getApplication().IS_DEVELOP) {
            test();
        }
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



    private boolean validateStatesInput() {
        if (states.size() == 0) {
            getApplication().showAlert(getString("alert_error"),
                    null, getString("states_missing"), Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateTransitionsInput() {
        if (transitions.size() == 0) {
            getApplication().showAlert(getString("alert_error"),
                    null, getString("transitions_missing"), Alert.AlertType.WARNING);
            return false;
        }

        for (Transition transition : transitions) {
            if (transition.getSourceState() == State.UNDEFINED ||
                    transition.getOperandState() == State.UNDEFINED ||
                    transition.getResultState() == State.UNDEFINED) {
                getApplication().showAlert(getString("alert_error"), null,
                        getString("transitions_incorrect"), Alert.AlertType.WARNING);
                return false;
            }
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
                    .showAlert(getString("alert_error"), null, getString("steps_count_invalid"),
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
                    .showAlert(getString("alert_error"), null,
                            getString("parametric_portrait_invalid_instance"),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validatePropertyInputs() {
        try {
            int i = 0;
            for (ComboBox<String> cb: cbProperties) {
                if (cb.getValue().equals(""))
                    throw new NullPointerException();
                properties.set(i++, cb.getValue());
            }
        } catch (Exception e) {
            getApplication()
                    .showAlert(getString("alert_error"), null,
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

            for (TextField tf: tfStartValues) {
                String property = cbProperties.get(i).getValue();
                if (property.equals(TransitionPropertyChooseList.SOURCE_DELAY)
                        || property.equals(TransitionPropertyChooseList.OPERAND_DELAY))
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
                    .showAlert(getString("alert_error"), null,
                            getString("parametric_portrait_invalid_start_value") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("alert_error"), null,
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

            for (TextField tf: tfEndValues) {
                String property = cbProperties.get(i).getValue();
                if (property.equals(TransitionPropertyChooseList.SOURCE_DELAY)
                        || property.equals(TransitionPropertyChooseList.OPERAND_DELAY))
                    propertyValue = Integer.parseInt(tf.getText());
                else
                    propertyValue = Double.parseDouble(tf.getText());

                if (propertyValue < 0) {
                    throw new IllegalArgumentException("parametric_portrait_illegal_end_value");
                }
                if (propertyValue < startValues.get(i)) {
                    throw new IllegalArgumentException("parametric_portrait_end_value_lower_than_start_value");
                }
                if ( ( property.equals(TransitionPropertyChooseList.SOURCE_DELAY)
                        || property.equals(TransitionPropertyChooseList.OPERAND_DELAY) )
                        && ((propertyValue - startValues.get(i)) % (stepsCnt.get(i) - 1) != 0)
                        )
                    throw new IllegalArgumentException("parametric_portrait_unable_discretize");

                endValues.set(i++, propertyValue);
            }
        }
        catch (NumberFormatException e) {
            getApplication()
                    .showAlert(getString("alert_error"), null,
                            getString("parametric_portrait_invalid_end_value") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("alert_error"), null, getString(e.getMessage()),
                            Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateDiscretizationInputs() {
        try {
            int steps;
            int i = 0;
            for (TextField tf: tfStepsCnt) {
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
                    .showAlert(getString("alert_error"), null,
                            getString("parametric_portrait_invalid_steps_count") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("alert_error"), null, getString(e.getMessage()),
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
                    .showAlert(getString("alert_error"), null,
                            getString("parametric_portrait_illegal_precision") + ": " + e.getMessage(),
                            Alert.AlertType.WARNING);
            return false;
        }
        catch (IllegalArgumentException e) {
            getApplication()
                    .showAlert(getString("alert_error"), null,
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
        setNewStateSettingsGroup();
    }


    /**
     * create new stateSettingsGroup
     */
    void setNewStateSettingsGroup() {
        // remain old stateSettingsGroup clone in settings table for maintain observe on main state table
        stateSettingsGroup = stateSettingsGroup.clone();
        stateSettingsTable.setItems(stateSettingsGroup.getStateSettingsList());
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
        // set settings group
        this.stateSettingsGroup = parametricPortrait.getStateSettingsGroup();
        stateSettingsTable.setItems(stateSettingsGroup.getStateSettingsList());

        // set application task from parametricPortrait
        // there is no need update settingsGroup, so remove change listener while updated main states table
        primaryController.getStates().removeListener(updateStateSettingsOnStateChangeListener);
        Task task = parametricPortrait.getCommonTask();
        // steps count field displays number stepsCount-1
        task.setStepsCount(task.getStepsCount() - 1);
        primaryController.setTask(task);
        task.setStepsCount(task.getStepsCount() + 1);
        primaryController.getStates().addListener(updateStateSettingsOnStateChangeListener);

        // set properties from parametricPortrait
        for (int i = 0; i < tfStartValues.size(); i++) {
            cbInstances.get(i).getSelectionModel().select(parametricPortrait.getInstances().get(i));
            cbProperties.get(i).getSelectionModel().select(parametricPortrait.getSelectedProperties().get(i));
            tfStartValues.get(i).setText(parametricPortrait.getStartValues().get(i).toString());
            tfEndValues.get(i).setText(parametricPortrait.getEndValues().get(i).toString());
            tfStepsCnt.get(i).setText(parametricPortrait.getStepsCnt().get(i).toString());
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
        ParametricPortrait parametricPortrait = new ParametricPortrait(getApplication(), stateSettingsGroup);
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

        // TODO enable group possibility
        // settingsGroup should be updated on changing state size and states name should be observable in group
        // remove it
        setNewStateSettingsGroup();
        shownParametricPortrait = getNewParametricPortraitInstance();


        javafx.concurrent.Task<Void> calculationTask = new javafx.concurrent.Task<Void>() {
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

    }


    /**
     * clear history
     */
    @FXML
    private void clearHistory() {
        history.clear();
    }






    /**
     * parametric portrait state settings
     */
    public class StateSettings {
        /** state */
        private ObjectProperty<State> state = new SimpleObjectProperty<>();
        /** should state be shown in portrait */
        private BooleanProperty show = new SimpleBooleanProperty(true);
        /**  color in portrait */
        private ObjectProperty<Color> color = new SimpleObjectProperty<>();
        /** map color to state */
        private Map<Integer, Color> stateColorMap;
        /** index of next color for this stateSettingsList in default colors */
        private AtomicInteger nextColorInd;


        StateSettings(State state, StateSettingsGroup stateSettingsGroup) {
            this.setState(state);
            this.stateColorMap = stateSettingsGroup.stateColorMap;
            this.nextColorInd = stateSettingsGroup.nextColorInd;
            Color color = stateColorMap.get( this.state.get().getId() );
            if (color == null) {
                color = defaultColors.get(nextColorInd.getAndIncrement());
            }
            setColor(color);
        }

        final ObjectProperty<State> stateProperty() {
            return state;
        }

        final public State getState() {
            return state.get();
        }

        final void setState(State state) {
            this.state.set(state);
        }

        final BooleanProperty showProperty() {
            return show;
        }

        final public Boolean getShow() {
            return show.get();
        }

        final void setShow(Boolean show) {
            this.show.set(show);
        }

        final ObjectProperty<Color> colorProperty() {
            return color;
        }

        final public Color getColor() {
            return color.get();
        }

        final void setColor(Color color) {
            stateColorMap.put(state.get().getId(), color);
            this.color.set(color);
        }
    }






    /**
     * Use to group parametric portraits by StateSettings for save state colors the same in group.
     */
    public class StateSettingsGroup {
        /** map color to state */
        private Map<Integer, Color> stateColorMap = new HashMap<>();
        /** index of next color for this stateSettingsList in default colors */
        private AtomicInteger nextColorInd = new AtomicInteger(0);
        /** list of StateSettings */
        private ObservableList<StateSettings> stateSettingsList = FXCollections.observableArrayList();
        /** stateSettingsList of StateSettings */
        private ObservableList<ParametricPortrait> parametricPortraitsList = FXCollections.observableArrayList();


        StateSettingsGroup() {}

        public void add(int index, State state) {
            stateSettingsList.add(index, new StateSettings(state, this));
        }

        public void add(ParametricPortrait parametricPortrait) {
            parametricPortraitsList.add(parametricPortrait);
        }

        public ObservableList<StateSettings> getStateSettingsList() {
            return stateSettingsList;
        }

        /**
         * update parametric portraits fill in group
         */
        void updateGroup() {
            parametricPortraitsList.forEach(ParametricPortrait::updateParametricPortraitFill);
        }

        @Override
        public StateSettingsGroup clone() {
            StateSettingsGroup clone = new StateSettingsGroup();
            for (StateSettings stateSettings: this.stateSettingsList) {
                int ind = clone.getStateSettingsList().size();
                clone.add(ind, stateSettings.getState());
                clone.getStateSettingsList().get(ind).setShow(stateSettingsList.get(ind).getShow());
                clone.getStateSettingsList().get(ind).setColor(stateSettingsList.get(ind).getColor());
            }
            return clone;
        }
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
         * @param stateSettingsGroup stateSettingsGroup defining group
         */
        void update(StateSettingsGroup stateSettingsGroup) {
            content.getChildren().stream()
                    .filter(x -> x instanceof HistoryItem)
                    .map(HistoryItem.class::cast)
                    .filter(item -> item.getParametricPortrait().getStateSettingsGroup() == stateSettingsGroup)
                    .forEach(HistoryItem::updateNailImage);
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
