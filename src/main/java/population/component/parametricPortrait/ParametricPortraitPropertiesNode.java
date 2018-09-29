package population.component.parametricPortrait;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import population.model.ParametricPortrait.ParametricPortrait;
import population.model.ParametricPortrait.PortraitProperties;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.Transition;
import population.util.Converter;
import population.util.Resources.StringResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParametricPortraitPropertiesNode extends HBox {
    /** dimension of the parametric portrait */
    private final int dimensions;
    /**
     * Task to which states and transitions this instances will bind to.
     * It usually should be global task instance
     */
    private final TaskV4 task;

    /** Portrait properties which will be changed by this inputs */
    private PortraitProperties portraitProperties;

    /** Instance list shown in instance ComboBox */
    private ObservableList<Object> instanceList;

    private List<TextField> startValueTextFields = new ArrayList<>();
    private List<TextField> stepDeltaValueTextFields = new ArrayList<>();
    private List<TextField> stepCountTextFields = new ArrayList<>();
    private List<ComboBox<Object>> instanceComboBoxes = new ArrayList<>();
    private List<ComboBox<ParametricPortrait.Property>> propertyComboBoxes = new ArrayList<>();




    public ParametricPortraitPropertiesNode(TaskV4 task, int dimensions) {
        super();
        this.task = task;
        this.dimensions = dimensions;

        this.setLayout();

        this.initInstancesComboBoxes();
        this.initPropertiesComboBoxes();
        this.initTextFields();

        task.getStates().addListener(updateInstanceListOnStateChangeListener);
        task.getTransitions().addListener(updateInstanceListOnTransitionChangeListener);
    }


    public void setPortraitProperties(PortraitProperties properties) {
        this.portraitProperties = properties;
        this.setValuesFromPortraitProperties();
    }


    /**
     * Set input values from portraitProperties.
     * Call this method only after this.task is the same as task associated with portraitProperties
     */
    private void setValuesFromPortraitProperties() {
        for (int i = 0; i < this.dimensions; i++) {
            this.instanceComboBoxes.get(i).getSelectionModel().select(portraitProperties.getInstances().get(i).get());
            this.propertyComboBoxes.get(i).getSelectionModel().select(portraitProperties.getProperties().get(i).get());
            this.startValueTextFields.get(i).setText(Converter.DOUBLE_STRING_CONVERTER.toString(portraitProperties.getStartValues().get(i).get()));
            this.stepDeltaValueTextFields.get(i).setText(Converter.DOUBLE_STRING_CONVERTER.toString(portraitProperties.getStepDeltas().get(i).get()));
            this.stepCountTextFields.get(i).setText(Integer.toString(portraitProperties.getStepCounts().get(i).get()));
        }
    }



    /*********************************
     *
     *      OBSERVABLE BINDINGS
     *
     ********************************/


    /**
     * bind changes from transitions to instanceList
     * (transitions placed immediately after states in instanceList)
     */
    private ListChangeListener<Object> updateInstanceListOnTransitionChangeListener = new ListChangeListener<Object>() {
        @Override
        public void onChanged(Change<?> c) {
            int startInd = task.getStates().size();

            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        instanceList.set(startInd + i, c.getList().get(i));
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<Object> copy = new ArrayList<>(instanceList.subList(startInd + from, startInd + to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        instanceList.set(startInd + newIndex, copy.get(oldIndex - from));
                    }

                } else if (c.wasAdded()) {
                    int i = 0;
                    for (Object o: c.getAddedSubList()) {
                        instanceList.add(c.getFrom() + i + startInd, o);
                        i++;
                    }

                } else if (c.wasRemoved()) {
                    instanceList.remove(c.getFrom() + startInd, c.getFrom() + startInd + c.getRemovedSize());
                }
            }
        }
    };


    /**
     * bind changes from task's states to instanceList
     * (states placed on firsts positions in instanceList)
     */
    private ListChangeListener<Object> updateInstanceListOnStateChangeListener = new ListChangeListener<Object>() {
        @Override
        public void onChanged(Change<?> c) {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        instanceList.set(i, c.getList().get(i));
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<Object> copy = new ArrayList<>(instanceList.subList(from, to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        instanceList.set(newIndex, copy.get(oldIndex - from));
                    }

                } else if (c.wasAdded()) {
                    int i = 0;
                    for (Object o: c.getAddedSubList()) {
                        instanceList.add(c.getFrom() + i, o);
                        i++;
                    }

                } else if (c.wasRemoved()) {
                    instanceList.remove(c.getFrom(), c.getFrom() + c.getRemovedSize());
                }
            }
        }
    };


    /**
     * bind properties shown in instance ComboBoxes to global task
     */
    private void initInstancesComboBoxes() {
        // instances shown in ComboBoxes
        instanceList = FXCollections.observableList(new ArrayList<>(),
            // listen for changes in model to update instance values
            (Object o) -> {
                List<Observable> list = new ArrayList<>();
                if (o instanceof State) {
                    list.add(((State) o).nameProperty());
                }
                if (o instanceof Transition) {
                    list.add(((Transition) o).probabilityProperty());
                        /*((Transition)o).getActualStates()
                            .forEach(state -> {
                                Collections.addAll(list,
                                    state.inProperty(),
                                    state.outProperty(),
                                    state.delayProperty()
                                );
                            });*/
                }
                Observable[] res = new Observable[list.size()];
                return list.toArray(res);
            });

        for (int i = 0; i < instanceComboBoxes.size(); i++) {
            ComboBox<Object> cbInstance = instanceComboBoxes.get(i);
            cbInstance.setConverter(ParametricPortrait.INSTANCE_STRING_CONVERTER);
            cbInstance.setItems(instanceList);

            final int finalInd = i;
            cbInstance.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                this.portraitProperties.getInstances().get(finalInd).set(cbInstance.getValue());
            });
        }
    }


    /**
     * add values to properties ComboBoxes
     */
    private void initPropertiesComboBoxes() {
        final ObservableList<ParametricPortrait.Property> transitionChooseList = FXCollections.observableArrayList(
            ParametricPortrait.Property.PROBABILITY
        );
        final ObservableList<ParametricPortrait.Property> stateChooseList = FXCollections.observableArrayList(
            ParametricPortrait.Property.COUNT
        );

        for (int i = 0; i < instanceComboBoxes.size(); i++) {
            final int finalInd = i;

            ComboBox<ParametricPortrait.Property> cbProperty = propertyComboBoxes.get(i);
            cbProperty.setConverter(ParametricPortrait.PROPERTY_STRING_CONVERTER);
            cbProperty.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                this.portraitProperties.getProperties().get(finalInd).set(cbProperty.getValue());
            });

            instanceComboBoxes.get(i).getSelectionModel().selectedIndexProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue.intValue() < 0) {
                        cbProperty.setItems(FXCollections.observableArrayList());
                        return;
                    }
                    if (newValue.intValue() < task.getStates().size())
                        cbProperty.setItems(stateChooseList);
                    else
                        cbProperty.setItems(transitionChooseList);
                }
            );
        }
    }


    private void initTextFields() {
        for (int i = 0; i < dimensions; i++) {
            int finalInd = i;

            startValueTextFields.get(i).textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    this.portraitProperties.getStartValues().get(finalInd).set(Double.valueOf(newValue.replaceAll("\\.$", "")));
                } catch (NumberFormatException e) {
                    // ignore if input is invalid
                }
            });

            stepDeltaValueTextFields.get(i).textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    this.portraitProperties.getStepDeltas().get(finalInd).set(Double.valueOf(newValue.replaceAll("\\.$", "")));
                } catch (NumberFormatException e) {
                    // ignore if input is invalid
                }
            });

            stepCountTextFields.get(i).textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    this.portraitProperties.getStepCounts().get(finalInd).set(Integer.valueOf(newValue));
                } catch (NumberFormatException e) {
                    // ignore if input is invalid
                }
            });
        }
    }


    /********************************
     *
     *          LAYOUT
     *
     ********************************/


    /**
     * set layout for this Node
     */
    private void setLayout() {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(25);
        this.setPadding(new Insets(25, 0, 0, 0));
        HBox.setHgrow(this, Priority.ALWAYS);

        for (int i = 0; i < dimensions; i++) {
            GridPane gridPane = this.getGridPane();

            this.getChildren().add(gridPane);

            ComboBox<Object> instanceComboBox = new ComboBox<>();
            this.addGridPaneItem(gridPane, instanceComboBox, StringResource.getString("parametric_portrait_instance_label"), 0);
            this.instanceComboBoxes.add(instanceComboBox);

            ComboBox<ParametricPortrait.Property> propertyComboBox = new ComboBox<>();
            this.addGridPaneItem(gridPane, propertyComboBox, StringResource.getString("parametric_portrait_property_label"), 1);
            this.propertyComboBoxes.add(propertyComboBox);

            TextField startValueTextField = new TextField();
            this.addGridPaneItem(gridPane, startValueTextField, StringResource.getString("parametric_portrait_start_value_label"), 2);
            this.startValueTextFields.add(startValueTextField);

            TextField endValueTextField = new TextField();
            this.addGridPaneItem(gridPane, endValueTextField, StringResource.getString("parametric_portrait_end_value_label"), 3);
            this.stepDeltaValueTextFields.add(endValueTextField);

            TextField stepCountTextField = new TextField();
            this.addGridPaneItem(gridPane, stepCountTextField, StringResource.getString("parametric_portrait_steps_count_label"), 4);
            this.stepCountTextFields.add(stepCountTextField);

            if (i < dimensions - 1) {
                Separator separator = new Separator(Orientation.VERTICAL);
                separator.setPadding(new Insets(15, 0, 15, 0));
                this.getChildren().add(separator);
            }
        }
    }


    private GridPane getGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        HBox.setHgrow(gridPane, Priority.ALWAYS);

        ColumnConstraints column0 = new ColumnConstraints();
        column0.setHgrow(Priority.SOMETIMES);
        column0.setMinWidth(70);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.ALWAYS);
        column1.setMinWidth(60);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.NEVER);

        gridPane.getColumnConstraints().addAll(column0, column1);
        gridPane.getRowConstraints().addAll(Collections.nCopies(5, row));

        return gridPane;
    }


    private void addGridPaneItem(GridPane gridPane, Region node, String labelText, int row) {
        Label label = new Label(labelText);
        label.setWrapText(true);
        gridPane.add(label, 0, row);

        node.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(node, 1, row);
    }
}
