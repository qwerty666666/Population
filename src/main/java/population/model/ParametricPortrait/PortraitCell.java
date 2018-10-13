package population.model.ParametricPortrait;

import population.model.TaskV4;

import java.util.List;

/**
 * Model for single parametric portrait cell
 * @param <T> type of calculation result
 */
public class PortraitCell<T> {
    private ParametricPortraitCalculator<T> calculator;
    private boolean isCalculated = false;
    private List<Double> uniqueTaskProperties;


    public PortraitCell(ParametricPortraitCalculator<T> calculator) {
        this.calculator = calculator;
    }

    /**
     * @return result of calculation for this cell
     */
    public T getCalculationResult() {
        if (!this.isCalculated) {
            this.calculator.calculate();
            this.isCalculated = true;
        }
        return this.calculator.getCalculationResult();
    }

    /**
     * @return task associated to this cell
     */
    public TaskV4 getTask() {
        return this.calculator.getTask();
    }


    public void addUniqueTaskProperty(double property) {
        this.uniqueTaskProperties.add(property);
    }


    public List<Double> getUniqueTaskProperties() {
        return this.uniqueTaskProperties;
    }


    public void calculate() {
        this.calculator.calculate();
        this.isCalculated = true;
    }
}
