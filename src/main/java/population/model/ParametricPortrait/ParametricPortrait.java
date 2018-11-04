package population.model.ParametricPortrait;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.util.StringConverter;
import population.model.Exception.ParametricPortrait.UnsupportedParametricPortraitProperty;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.Transition;
import population.util.Resources.StringResource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


public class ParametricPortrait {
    private PortraitProperties properties;
    /** common task for all cells */
    private TaskV4 task;
    /** grid of portrait cells [row][col] */
    private List<List<List<List<State>>>> calculationResult = new ArrayList<>();
    /** progress of calculation [0..1] */
    private ReadOnlyDoubleWrapper calculationProgress = new ReadOnlyDoubleWrapper(0);


    /**
     * Transition and state properties which can be chosen as parametric
     * portrait property for certain axe
     */
    public enum Property {
        PROBABILITY,
        SOURCE_DELAY,
        STATE_IN,
        STATE_OUT,
        COUNT
    }


    /**
     * Convert instance to string
     */
    public static StringConverter<Object> INSTANCE_STRING_CONVERTER = new StringConverter<Object>() {
        @Override
        public String toString(Object instance) {
            if (instance == null) {
                return "";
            }
            if (instance instanceof State)
                return (((State) instance).getName());
            else if (instance instanceof Transition) {
                return Integer.toString(((Transition)instance).getId());
            }
            return "";
        }

        @Override
        public Object fromString(String string) {
            return null;
        }
    };


    /**
     * Convert instance property to string
     */
    public static StringConverter<ParametricPortrait.Property> PROPERTY_STRING_CONVERTER = new StringConverter<ParametricPortrait.Property>() {
        @Override
        public String toString(ParametricPortrait.Property item) {
            switch (item) {
                case PROBABILITY: {
                    return StringResource.getString("ParametricPortrait.ProbabilityProperty");
                }
                case COUNT: {
                    return StringResource.getString("States.Count");
                }
            }
            return "";
        }

        @Override
        public ParametricPortrait.Property fromString(String userId) {
            return null;
        }
    };


    /**
     *
     * @param task common task for all cell
     * @param portraitProperties properties for PP
     */
    public ParametricPortrait(TaskV4 task, PortraitProperties portraitProperties) {
        this.task = task;
        this.properties = portraitProperties;
    }


    /**
     * @return portrait properties
     */
    public PortraitProperties getProperties() {
        return this.properties;
    }


    /**
     * @return rows count in portrait
     */
    public int getRowCount() {
        return this.properties.getStepCounts().get(1).get();
    }

    /**
     * @return column count in portrait
     */
    public int getColCount() {
        return this.properties.getStepCounts().get(0).get();
    }


    /**
     * @return common task for portrait
     */
    public TaskV4 getTask() {
        return this.task;
    }


    /**
     * Сalculate all inner cells
     */
    public void calculate() {
        this.calculationResult = new ArrayList<>();

        for (int row = 0; row < this.getRowCount(); row++) {
            List<List<List<State>>> rowResults = new ArrayList<>();
            this.calculationResult.add(rowResults);

            for (int col = 0; col < this.getColCount(); col++) {
                ParametricPortraitCalculator calculator = new SimpleParametricPortraitCalculator(
                    this.getTask(row, col), this.properties.getShownStateList()
                );
                calculator.calculate();
                rowResults.add(((SimpleParametricPortraitCalculator) calculator).getCalculationResult());

                this.calculationProgress.set((double)(row * this.getColCount() + col) / (this.getRowCount() * this.getColCount()));
            }
        }
    }


    /**
     * @return calculation cell result
     */
    public List<List<State>> getCalculationResult(int row, int col) {
        if (this.calculationResult.size() <= row || this.calculationResult.get(row).size() <= col) {
            return null;
        }
        return this.calculationResult.get(row).get(col);
    }


    /**
     * Change common task properties according to certain cell and return that task
     *
     * @return common task with changed properties
     */
    public TaskV4 getTask(int row, int col) {
        TaskV4 result = this.task;

        for (int ind = 0; ind < 2; ind++) {
            double propertyValue = this.getPropertyValueOnStep(ind, ind == 0 ? col : row);

            Property property = this.properties.getProperties().get(ind).get();
            switch (property) {
                case PROBABILITY: {
                    Transition transition = (Transition) this.properties.getInstances().get(ind).get();
                    task.getTransitionById(transition.getId()).setProbability(propertyValue);
                    break;
                }
                case COUNT: {
                    State state = (State) this.properties.getInstances().get(ind).get();
                    task.getStateById(state.getId()).setCount(propertyValue);
                    break;
                }
                default: {
                    throw new UnsupportedParametricPortraitProperty("PP property '" + property + "' is unsupported");
                }
            }
        }

        return result;
    }


    /**
     * @param propertyIndex index of property in this properties
     * @param step step index
     * @return value of property on step
     */
    public double getPropertyValueOnStep(int propertyIndex, int step) {
        double res = this.properties.getStartValues().get(propertyIndex).get() +
            this.properties.getStepDeltas().get(propertyIndex).get() * step;

        // round value
        return new BigDecimal(res)
            .setScale(5, RoundingMode.HALF_UP)
            .doubleValue();
    }


    /**
     * @return progress of calculation [0..1]
     */
    public ReadOnlyDoubleProperty calculationProgressProperty() {
        return this.calculationProgress.getReadOnlyProperty();
    }
}
