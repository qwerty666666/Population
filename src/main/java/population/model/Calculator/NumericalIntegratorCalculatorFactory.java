package population.model.Calculator;

import population.model.TaskV4;


/**
 * Factory for creating {@link TaskCalculator} instance
 */
public interface NumericalIntegratorCalculatorFactory {
    public TaskCalculator create(TaskV4 task, double stepSize);
}
