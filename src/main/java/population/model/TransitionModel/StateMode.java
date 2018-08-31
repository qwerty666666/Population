package population.model.TransitionModel;


import population.util.Resources.StringResource;

import java.util.Arrays;
import java.util.List;


public class StateMode {
    public static final int SIMPLE = 0;
    public static final int INHIBITOR = 1;
    public static final int RESIDUAL = 2;

    public static final List<Integer> MODES =
            Arrays.asList(INHIBITOR, RESIDUAL, SIMPLE);

    public static String getName(int type) {
        switch (type) {
            case SIMPLE: {
                return StringResource.getString("States.Mode.Simple");
            }
            case INHIBITOR: {
                return StringResource.getString("States.Mode.Inhibitor");
            }
            case RESIDUAL: {
                return StringResource.getString("States.Mode.Residual");
            }
            default: {
                return "";
            }
        }
    }
}
