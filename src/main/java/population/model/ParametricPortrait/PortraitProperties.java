package population.model.ParametricPortrait;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import population.model.StateModel.State;
import population.util.ListUtils;
import population.util.Cloneable;

import java.util.List;
import java.util.stream.Collectors;


public class PortraitProperties implements Cloneable<PortraitProperties> {
    private int dimensions = 2;

    private static final double START_VALUE_DEFAULT = 0.1;
    private static final double STEP_DELTA_DEFAULT = 0.1;
    private static final int STEP_COUNT_DEFAULT = 10;

    private List<DoubleProperty> startValues = ListUtils.generateListValues(() -> new SimpleDoubleProperty(START_VALUE_DEFAULT), dimensions);
    private List<DoubleProperty> stepDeltas = ListUtils.generateListValues(() -> new SimpleDoubleProperty(STEP_DELTA_DEFAULT), dimensions);
    private List<IntegerProperty> stepCounts = ListUtils.generateListValues(() -> new SimpleIntegerProperty(STEP_COUNT_DEFAULT), dimensions);
    private List<ObjectProperty> instances = ListUtils.generateListValues(() -> new SimpleObjectProperty<>(null), dimensions);
    private List<ObjectProperty<ParametricPortrait.Property>> properties = ListUtils.generateListValues(() -> new SimpleObjectProperty<>(null), dimensions);

    private ObservableList<StateSetting> stateSettings = FXCollections.observableArrayList();


    public PortraitProperties() { }


    public List<DoubleProperty> getStartValues() {
        return startValues;
    }

    public List<DoubleProperty> getStepDeltas() {
        return stepDeltas;
    }

    public List<IntegerProperty> getStepCounts() {
        return stepCounts;
    }

    public List<ObjectProperty> getInstances() {
        return instances;
    }

    public List<ObjectProperty<ParametricPortrait.Property>> getProperties() {
        return properties;
    }

    public int getDimensions() {
        return dimensions;
    }

    public List<State> getShownStateList() {
        return this.stateSettings.stream()
            .filter(StateSetting::getShow)
            .map(StateSetting::getState)
            .collect(Collectors.toList());
    }

    public ObservableList<StateSetting> stateSettingProperty() {
        return this.stateSettings;
    }


    @Override
    public PortraitProperties clone() {
        PortraitProperties clone = new PortraitProperties();

        clone.startValues = this.startValues.stream()
            .map(val -> new SimpleDoubleProperty(val.getValue()))
            .collect(Collectors.toList());
        clone.stepDeltas = this.stepDeltas.stream()
            .map(val -> new SimpleDoubleProperty(val.getValue()))
            .collect(Collectors.toList());
        clone.stepCounts = this.stepCounts.stream()
            .map(val -> new SimpleIntegerProperty(val.getValue()))
            .collect(Collectors.toList());
        clone.instances = this.instances.stream()
            .map(val -> new SimpleObjectProperty<>(val.getValue()))
            .collect(Collectors.toList());
        clone.properties = this.properties.stream()
            .map(val -> new SimpleObjectProperty<>(val.getValue()))
            .collect(Collectors.toList());
        clone.stateSettings = ListUtils.cloneObservableList(this.stateSettings);

        return clone;
    }
}
