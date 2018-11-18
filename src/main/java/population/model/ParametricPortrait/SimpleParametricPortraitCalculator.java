package population.model.ParametricPortrait;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import population.model.Calculator.CalculationResult;
import population.model.Calculator.CalculatorFactory;
import population.model.StateModel.State;
import population.model.TaskV4;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Default parametric portrait calculator
 */
public class SimpleParametricPortraitCalculator extends ParametricPortraitCalculator {
    protected TaskV4 task;

    /** is task calculated */
    protected boolean isCalculated = false;
    /** result of task calculation */
    protected CalculationResult calculationResult;
    /** comparison precision */
    protected double precision;


    @AssistedInject
    SimpleParametricPortraitCalculator(
        CalculatorFactory calculatorFactory,
        @Assisted TaskV4 task,
        @Assisted PortraitProperties properties
    ) {
        super(calculatorFactory.create(task), properties);

        this.task = task;
        this.precision = properties.getPrecision().get();
        this.taskCalculator.progressProperty().addListener((observable, oldValue, newValue) -> {
            progress.set(newValue.doubleValue());
        });
    }


    @Override
    public void calculate() {
        this.taskCalculator.calculate();
        this.calculationResult = this.taskCalculator.getCalculationResult();
        this.isCalculated = true;
    }


    /**
     * @return is calculated result stable
     */
    protected boolean isTaskStable() {
        if (!this.isCalculated) {
            throw new RuntimeException("Task must be calculated before call this method. Call calculate() method first");
        }

        int maxDelay = this.task.getMaxDelay();

        if (this.task.getStepsCount() <= maxDelay - 2) {
            return false;
        }

        /*
         * Consider task is stable when state counts haven't changed for last maxDelay steps
         */
        double[][] calcResult = this.calculationResult.getStatesCount();
        int statesCnt = calcResult[0].length;
        int stepsCnt = this.task.getStepsCount();

        for (int i = 0; i < statesCnt; i++) {
            double max, min;
            max = min = calcResult[stepsCnt - maxDelay - 2][i];
            for (int step = stepsCnt - maxDelay - 1; step < stepsCnt; step++) {
                min = Math.min(min, calcResult[step][i]);
                max = Math.max(max, calcResult[step][i]);
                if (max - min > precision) {
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * @return calculation result for stable task
     * @see #getCalculationResult()
     */
    private List<List<State>> getStableResult() {
        List<State> shownStates = this.properties.getShownStateList();
        double[][] states = this.calculationResult.getStatesCount();

        // remain states whose relative count greater then precision
        int calculatedStepsCnt = this.task.getStepsCount();
        double totalCnt = 0;
        for (int i = 0; i < states[0].length; i++) {
            totalCnt += states[calculatedStepsCnt - 1][i];
        }

        final double fTotalCnt = totalCnt;
        shownStates = shownStates.stream()
            .filter(state -> states[calculatedStepsCnt - 1][this.calculationResult.getStateIndex(state)] / fTotalCnt > precision)
            .collect(Collectors.toList());


        List<List<State>> result = new ArrayList<>();
        List<State> dominants = new ArrayList<>(shownStates);

        while (dominants.size() > 0) {
            remainDominantsWithEps(dominants, (int)(this.task.getStepsCount() * 0.2));
            result.add(0,dominants);
            shownStates.removeAll(dominants);
            dominants = new ArrayList<>(shownStates);
        }

        return result;
    }


    /**
     * Remove from statesList states which are lower than max state for last 'steps' steps.
     * States considered as the same if their relative difference is lower than precision for all last steps
     *
     * @param stateList list form which states will be removed
     * @param steps last steps count for which comparison will be performed
     */
    private void remainDominantsWithEps(List<State> stateList, int steps) {
        List<Integer> indexes = stateList.stream()
            .map(state -> this.calculationResult.getStateIndex(state))
            .collect(Collectors.toList());

        // remain only biggest state and all states in precision neighbourhood for
        // prevent calculation error in the same graphics
        for (int ind1: indexes) {
            for (int ind2: indexes) {
                if (ind2 == ind1)
                    continue;

                boolean ind1Lower = true;
                boolean isNear = true;
                int calculatedStepsCnt = this.task.getStepsCount();
                double[][] states = this.calculationResult.getStatesCount();
                for (int step = calculatedStepsCnt - steps; step < calculatedStepsCnt; step++) {
                    if (states[step][ind1] > states[step][ind2]) {
                        ind1Lower = false;
                    }
                    if (isNear && Math.abs(states[step][ind1] - states[step][ind2]) / Math.max(states[step][ind1], states[step][ind2]) > precision) {
                        isNear = false;
                    }
                }
                if (ind1Lower && !isNear) {
                    stateList.remove(this.task.getStates().get(ind1));
                    break;
                }
            }
        }
    }


    /**
     * Try to find result based on trends monotonicity in last half steps.
     *
     * @param stateList states which monotonicity will be checked
     * @return If all trends monotonic returns states ordered by their monotonicity, otherwise return null.
     */
    private List<List<State>> getResultByMonotonicity(List<State> stateList) {
        Map<State, Integer> trends = new HashMap<>();
        boolean isTrendsDetermined = true;
        int calculatedStepsCnt = this.task.getStepsCount();
        double[][] states = this.calculationResult.getStatesCount();

        cycle:
        for (State state: stateList) {
            int ind = this.calculationResult.getStateIndex(state);
            int trend = (int)Math.signum(states[calculatedStepsCnt / 2][ind] - states[calculatedStepsCnt / 2 - 1][ind]);
            trends.put(state, trend);
            for (int step = calculatedStepsCnt / 2 + 1; step < calculatedStepsCnt; step++) {
                if (trend != (int)Math.signum(states[step][ind] - states[step - 1][ind])) {
                    isTrendsDetermined = false;
                    break cycle;
                }
            }
        }

        if (isTrendsDetermined) {
            List<List<State>> result = new ArrayList<>();
            for (int val : new int[]{-1, 0, 1}) {
                if (trends.containsValue(val)) {
                    result.add(
                        trends.entrySet().stream()
                            .filter(entry -> entry.getValue() == val)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList())
                    );
                }
            }
            return result;
        }

        return null;
    }


    /**
     * @param stateList states which can be in result
     * @param steps steps count for which integral sum wil be calculated
     * @return states ordered by their integral sum. States with integral sum difference lower than precision
     * considered as the same
     */
    private List<List<State>> getResultByIntegralSum(List<State> stateList, int steps) {
        Map<State, Double> stateIntegralSumMap = new HashMap<>();
        int calculatedStepsCnt = this.task.getStepsCount();
        double[][] statesCnt = this.calculationResult.getStatesCount();

        // get integral sum
        for (State state: stateList) {
            int ind = this.calculationResult.getStateIndex(state);
            double sum = 0;
            for (int step = calculatedStepsCnt  - steps; step < calculatedStepsCnt; step++) {
                sum += statesCnt[step][ind];
            }
            stateIntegralSumMap.put(task.getStates().get(ind), sum);
        }

        // sort states by its integral sum
        List<Map.Entry<State, Double>> entries = stateIntegralSumMap.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getValue))
            .collect(Collectors.toList());

        // form result
        double lastVal = entries.iterator().next().getValue();
        List<List<State>> result = new ArrayList<>();
        List<State> eq = new ArrayList<>();
        for (Map.Entry<State, Double> entry: entries) {
            if (entry.getValue() / lastVal > (1 + precision)) {
                result.add(eq);
                eq = new ArrayList<>();
                lastVal = entry.getValue();
            }
            eq.add(entry.getKey());
        }
        if (eq.size() > 0) {
            result.add(eq);
        }

        return result;
    }


    /**
     * @param states states which will be filtered
     * @return states whose count wasn't lower than precision for last half step
     */
    private List<State> getAliveStates(List<State> states) {
        double[][] statesCnt = this.calculationResult.getStatesCount();
        return states.stream()
            .filter(state -> {
                int ind = this.calculationResult.getStateIndex(state);
                for (int step = (int)(this.task.getStepsCount() / 2); step < this.task.getStepsCount(); step++) {
                    if (statesCnt[step][ind] > precision) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
    }


    @Override
    public ParametricPortraitCalculationResult getCalculationResult() {
        if (!this.isCalculated) {
            throw new RuntimeException("Task must be calculated before retrieve result. Call calculate() method first");
        }

        if (this.isTaskStable()) {
            return new ParametricPortraitCalculationResult(this.getStableResult(), CalculationResultType.STABLE);
        }


        List<State> aliveStates = this.getAliveStates(this.properties.getShownStateList());
        if (aliveStates.size() == 0) {
            return new ParametricPortraitCalculationResult(Arrays.asList(Arrays.asList()), CalculationResultType.STABLE);
        }

        List<List<State>> statesOrderedByMonotonicity = this.getResultByMonotonicity(aliveStates);
        if (statesOrderedByMonotonicity != null) {
            return new ParametricPortraitCalculationResult(statesOrderedByMonotonicity, CalculationResultType.TRENDS);
        }


        List<List<State>> statesOrderedByIntegralSum = this.getResultByIntegralSum(aliveStates, this.task.getStepsCount() / 2);
        return new ParametricPortraitCalculationResult(statesOrderedByIntegralSum, CalculationResultType.INTEGRAL_SUM);
    }
}
