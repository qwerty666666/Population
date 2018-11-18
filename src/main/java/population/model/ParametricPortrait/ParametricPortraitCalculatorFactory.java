package population.model.ParametricPortrait;


import population.model.TaskV4;


/**
 * Factory for creating {@link ParametricPortraitCalculator} instance
 */
public interface ParametricPortraitCalculatorFactory {
    public ParametricPortraitCalculator create(TaskV4 task, PortraitProperties properties);
}
