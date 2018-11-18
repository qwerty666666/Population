package population.model.ParametricPortrait;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import population.model.Calculator.TaskCalculator;


/**
 * Calculator for parametric portrait. It determines which States will be dominant in task calculation.
 */
public abstract class ParametricPortraitCalculator {
    protected TaskCalculator taskCalculator;
    protected PortraitProperties properties;
    /** calculation progress form 0 to 1 */
    protected ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0);


    ParametricPortraitCalculator(TaskCalculator taskCalculator, PortraitProperties properties) {
        this.taskCalculator = taskCalculator;
        this.properties = properties;
    }


    /**
     * Do task calculation
     */
    abstract void calculate();


    /**
     * @return result of calculation
     */
    abstract ParametricPortraitCalculationResult getCalculationResult();


    /**
     * calculation progress form 0 to 1
     */
    public ReadOnlyDoubleProperty progressProperty() {
        return this.progress.getReadOnlyProperty();
    }
}
