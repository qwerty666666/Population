package population.model.ParametricPortrait;

import population.model.StateModel.State;

import java.util.List;

/**
 * Result of parametric portrait calculation.
 *
 * Result is a List which contains on the first position List of States which are dominant in calculation
 * (i.e. theirs count will be the greatest), on the next position - List of States with lower count, and so on...
 */
public class ParametricPortraitCalculationResult {
    private List<List<State>> result;
    private CalculationResultType type;


    public ParametricPortraitCalculationResult(List<List<State>> result, CalculationResultType type) {
        this.result = result;
        this.type = type;
    }

    public List<List<State>> getResult() {
        return result;
    }

    public CalculationResultType getType() {
        return this.type;
    }

    public void setResult(List<List<State>> result) {
        this.result = result;
    }
}
