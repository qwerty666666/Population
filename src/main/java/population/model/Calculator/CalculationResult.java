package population.model.Calculator;

import population.model.StateModel.State;
import population.model.TaskV4;

/**
 * Result of task calculation
 */
public class CalculationResult {
    /** calculated states count [step][state index] */
    private double[][] stepsCount;
    /** task which was calculated */
    private TaskV4 task;

    public CalculationResult(TaskV4 task, double[][] stepsCount) {
        this.stepsCount = stepsCount;
        this.task = task;
    }

    /**
     * @return calculated states count [step][state index]
     */
    public double[][] getStatesCount() {
        return stepsCount;
    }

    public TaskV4 getTask() {
        return task;
    }

    /**
     * @return index of passed state in stepsCount
     */
    public int getStateIndex(State state) {
        return this.task.getStates().indexOf(state);
    }


    public int getStepsCount() {
        return this.stepsCount.length;
    }
}
