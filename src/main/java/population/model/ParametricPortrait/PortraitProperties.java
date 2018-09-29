package population.model.ParametricPortrait;

import javafx.beans.property.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PortraitProperties {
    private int dimensions = 2;

    private static final double START_VALUE_DEFAULT = 0.1;
    private static final double STEP_DELTA_DEFAULT = 0.1;
    private static final int STEP_COUNT_DEFAULT = 10;

    private List<DoubleProperty> startValues = generateListValues(() -> new SimpleDoubleProperty(START_VALUE_DEFAULT), dimensions);
    private List<DoubleProperty> stepDeltas = generateListValues(() -> new SimpleDoubleProperty(STEP_DELTA_DEFAULT), dimensions);
    private List<IntegerProperty> stepCounts = generateListValues(() -> new SimpleIntegerProperty(STEP_COUNT_DEFAULT), dimensions);
    private List<ObjectProperty> instances = generateListValues(() -> new SimpleObjectProperty<>(null), dimensions);
    private List<ObjectProperty<ParametricPortrait.Property>> properties = generateListValues(() -> new SimpleObjectProperty<>(null), dimensions);


    public PortraitProperties() { }


    /**
     * @return List of object returned by supplier with size of limit parameter
     */
    private <T> List<T> generateListValues(Supplier<T> supplier, int limit) {
        return Stream.generate(supplier)
            .limit(limit)
            .collect(Collectors.toList());
    }


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
}
