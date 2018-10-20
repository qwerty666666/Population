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
     * do task calculation
     */
    void calculate();

    /**
     * @return result of calculation
     */
    T getCalculationResult();
}
