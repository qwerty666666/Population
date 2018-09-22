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

    public static String getName(int type, boolean useAbbreviation) {
        switch (type) {
            case SIMPLE: {
                return StringResource.getString(
                    useAbbreviation ? "States.Mode.SimpleAbbreviation" : "States.Mode.Simple"
                );
            }
            case INHIBITOR: {
                return StringResource.getString(
                    useAbbreviation ? "States.Mode.InhibitorAbbreviation" : "States.Mode.Inhibitor"
                );
            }
            case RESIDUAL: {
                return StringResource.getString(
                    useAbbreviation ? "States.Mode.ResidualAbbreviation" : "States.Mode.Residual"
                );
            }
            default: {
                return "";
            }
        }
    }

    public static String getName(int type) {
        return getName(type, false);
    }

    public static String getAbbreviation(int type) {
        return getName(type, true);
    }
}
