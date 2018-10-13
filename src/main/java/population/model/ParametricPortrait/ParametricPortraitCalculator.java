package population.model.ParametricPortrait;

import population.model.StateModel.State;
import population.model.TaskV4;

import java.util.List;


/**
 *
 * @param <T> type of calculation result
 */
public interface ParametricPortraitCalculator<T> {
    /**
     * @return task which will be calculated
     */
    TaskV4 getTask();

    /**
     * @return states list which will be returned in the result list
     */
    List<State> getAllowedStates();

    /**
     * do task calculation
     */
    void calculate();

    /**
     * @return result of calculation
     */
    T getCalculationResult();
}
