package population.model.Calculator;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import population.model.TaskV4;


/**
 * Base class for task calculation.
 */
public abstract class TaskCalculator {
    /** Task which will be calculated by this */
    protected TaskV4 task;
    /** calculation progress form 0 to 1 */
    protected ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(0);
    /** calculated states count [step][state index] */
    protected double[][] statesCount;


    /**
     * The passed task will be cloned, therefore any subsequent task changes will be ignored.
     * So be aware of create calculator instance right before calling {@link TaskCalculator#calculate()} method.
     * It is done in order to maintain consistency between {@link CalculationResult} and its task reference.
     */
    public TaskCalculator(TaskV4 task) {
        this.task = task.clone();
    }


    /**
     * Calculate task
     */
    public void calculate() {
        this.calculateToStep(this.task.getStepsCount() - 1);
    }


    /**
     * Calculate from previous step to passed step
     *
     * @param step int step number (from 0)
     */
    public abstract void calculateToStep(int step);


    /**
     * @return is calculation finished for a whole task
     */
    public abstract boolean isFinished();


    /**
     * @return calculated states count [step][stateIndex]
     */
    public CalculationResult getCalculationResult() {
        if (!this.isFinished()) {
            throw new IllegalStateException("Calculation should be finished before getCalculationResult() call");
        }
        return new CalculationResult(this.task, this.statesCount);
    }


    /**
     * @return calculated task
     */
    public TaskV4 getTask() {
        return this.task;
    }


    /**
     * calculation progress form 0 to 1
     */
    public ReadOnlyDoubleProperty progressProperty() {
        return this.progress.getReadOnlyProperty();
    }
}
