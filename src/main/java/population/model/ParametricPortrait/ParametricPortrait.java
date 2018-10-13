package population.model.ParametricPortrait;

import javafx.util.StringConverter;
import population.model.Exception.ParametricPortrait.UnsupportedParametricPortraitProperty;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.Transition;
import population.util.Resources.StringResource;

import java.util.ArrayList;
import java.util.List;


public class ParametricPortrait {
    private PortraitProperties properties;
    /** common task for all cells */
    private TaskV4 task;
    /** grid of portrait cells */
    private List<List<PortraitCell<List<List<State>>>>> cells = new ArrayList<>();


    /**
     * transition and state properties which can be chosen as parametric portrait property for certain axe
     */
    public enum Property {
        PROBABILITY,
        SOURCE_DELAY,
        STATE_IN,
        STATE_OUT,
        COUNT
    }


    /**
     * convert instance to string
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


    public PortraitProperties getProperties() {
        return this.properties;
    }

    public TaskV4 getTask() {
        return this.task;
    }


    /**
     * calculate all inner cells
     */
    public void calculate() {
        this.buildCellGrid();

        this.cells.forEach(list -> {
            list.forEach(PortraitCell::calculate);
        });
    }


    /**
     * build cells grid depended on portrait properties
     */
    protected void buildCellGrid() {
        for (int i = 0; i < this.properties.getStepCounts().get(0).get(); i++) {
            List<PortraitCell<List<List<State>>>> list = new ArrayList<>();
            this.cells.add(list);

            for (int j = 0; j < this.properties.getStepCounts().get(1).get(); j++) {
                TaskV4 clone = this.task.clone();
                PortraitCell<List<List<State>>> cell = new PortraitCell<List<List<State>>>(new SimpleParametricPortraitCalculator(clone, properties.getShownStateList()));
                list.add(cell);

                for (int ind = 0; ind < 2; ind++) {
                    double propertyValue = this.getPropertyValueOnStep(ind, ind == 0 ? i : j);
                    cell.addUniqueTaskProperty(propertyValue);

                    Property property = this.properties.getProperties().get(ind).get();
                    switch (property) {
                        case PROBABILITY: {
                            ((Transition)this.properties.getInstances().get(ind).get()).setProbability(propertyValue);
                            break;
                        }
                        case COUNT: {
                            ((State)this.properties.getInstances().get(ind).get()).setCount(propertyValue);
                            break;
                        }
                        default: {
                            throw new UnsupportedParametricPortraitProperty("PP property '" + property + "' is unsupported");
                        }
                    }
                }
            }
        }
    }


    /**
     *
     * @param propertyIndex index of property in this properties
     * @param step step index
     * @return value of property on step
     */
    protected double getPropertyValueOnStep(int propertyIndex, int step) {
        return this.properties.getStartValues().get(propertyIndex).get() +
            this.properties.getStepDeltas().get(propertyIndex).get() * step;
    }
}
