package population.model.Calculator;


import population.model.StateModel.State;

import java.util.List;

public class Result {
    protected double[][] statesCount;
    protected List<State> states;
    protected int startPoint;
    protected int stepsCount;

    public Result(double[][] statesCount, List<State> states, int startPoint, int stepsCount) {
        this.statesCount = statesCount;
        this.states = states;
        this.startPoint = startPoint;
        this.stepsCount = stepsCount;
    }

    public double[][] getStatesCount() {
        return statesCount;
    }

    public List<State> getStates() {
        return states;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public int getStepsCount() {
        return stepsCount;
    }
}
