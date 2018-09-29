package population.model.ParametricPortrait;

import population.model.StateModel.State;
import population.model.TaskV4;

import java.util.List;

public interface ParametricPortraitCalculator {
    /**
     * @return task which will be calculated
     */
    TaskV4 getTask();

    /**
     * @return states list which will be returned in the result list
     */
    List<State> getAllowedStates();
}
