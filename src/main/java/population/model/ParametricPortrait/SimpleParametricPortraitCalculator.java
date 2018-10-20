package population.model.ParametricPortrait;

import population.App;
import population.model.StateModel.State;
import population.model.TaskV4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleParametricPortraitCalculator implements ParametricPortraitCalculator<List<List<State>>> {
    private TaskV4 task;
    private List<State> allowedStates;

    public SimpleParametricPortraitCalculator(TaskV4 task, List<State> allowedStates) {
        this.task = task;
        this.allowedStates = allowedStates;
    }

    @Override
    public void calculate() {

    }

    @Override
    public List<List<State>> getCalculationResult() {
        return Arrays.asList(Arrays.asList(App.getStates().get(0), App.getStates().get(0)));
    }
}
