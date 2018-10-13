package population.model.ParametricPortrait;

import population.model.StateModel.State;
import population.model.TaskV4;

import java.util.List;

public class SimpleParametricPortraitCalculator implements ParametricPortraitCalculator {
    private TaskV4 task;
    private List<State> allowedStates;

    public SimpleParametricPortraitCalculator(TaskV4 task, List<State> allowedStates) {
        this.task = task;
        this.allowedStates = allowedStates;
    }

    @Override
    public TaskV4 getTask() {
        return task;
    }

    @Override
    public List<State> getAllowedStates() {
        return allowedStates;
    }

    @Override
    public void calculate() {

    }

    @Override
    public Object getCalculationResult() {
        return null;
    }
}
