package population.model.ParametricPortrait;

import javafx.util.StringConverter;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.Transition;
import population.util.Resources.StringResource;

public class ParametricPortrait {
    private PortraitProperties properties;
    private TaskV4 task;


    /**
     * transitions and states properties which can be chosen as parametric portrait property for certain axe
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


    public void calculate() {
        // TODO
    }
}
