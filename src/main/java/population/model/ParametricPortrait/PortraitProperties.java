package population.model.ParametricPortrait;

import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import population.model.StateModel.State;
import population.util.ListUtils;
import population.util.Cloneable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Portrait properties describes parameters for building axes in PP (steps count
 */
public class PortraitProperties implements Cloneable<PortraitProperties> {
    private int dimensions = 2;

    private static final double START_VALUE_DEFAULT = 0.1;
    private static final double STEP_DELTA_DEFAULT = 0.1;
    private static final int STEP_COUNT_DEFAULT = 9;

    private List<DoubleProperty> startValues = ListUtils.generateListValues(() -> new SimpleDoubleProperty(START_VALUE_DEFAULT), dimensions);
    private List<DoubleProperty> stepDeltas = ListUtils.generateListValues(() -> new SimpleDoubleProperty(STEP_DELTA_DEFAULT), dimensions);
    private List<IntegerProperty> stepCounts = ListUtils.generateListValues(() -> new SimpleIntegerProperty(STEP_COUNT_DEFAULT), dimensions);
    private List<ObjectProperty> instances = ListUtils.generateListValues(() -> new SimpleObjectProperty<>(null), dimensions);
    private List<ObjectProperty<ParametricPortrait.Property>> properties = ListUtils.generateListValues(() -> new SimpleObjectProperty<>(null), dimensions);

    private ObservableList<StateSetting> stateSettings = FXCollections.observableList(new ArrayList<>(),
        // trigger change event when this properties changed
        stateSetting -> new Observable[] {
            stateSetting.colorProperty(),
            stateSetting.showProperty()
        }
    );

    private DoubleProperty precision = new SimpleDoubleProperty(0.001);


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

    public DoubleProperty getPrecision() {
        return this.precision;
    }

    public List<ObjectProperty<ParametricPortrait.Property>> getProperties() {
        return properties;
    }

    public int getDimensions() {
        return dimensions;
    }

    public ObservableList<StateSetting> stateSettingProperty() {
        return this.stateSettings;
    }

    /**
     * @return states which can be shown in portrait
     */
    public List<State> getShownStateList() {
        return this.stateSettings.stream()
            .filter(StateSetting::getShow)
            .map(StateSetting::getState)
            .collect(Collectors.toList());
    }


    /**
     * @return color for state
     */
    public Color getColor(State state) {
        return this.stateSettings.stream()
            .filter(stateSetting -> stateSetting.getState().equals(state))
            .map(StateSetting::getColor)
            .findFirst()
            .orElse(null);
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
        clone.precision.set(this.precision.get());

        return clone;
    }
}
