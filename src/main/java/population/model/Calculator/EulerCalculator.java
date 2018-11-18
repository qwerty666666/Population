package population.model.Calculator;


import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import population.model.Exception.UnknownTransitionType;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.StateMode;
import population.model.TransitionModel.Transition;
import population.model.TransitionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Calculator implementing Euler method with step 1
 */
public class EulerCalculator extends TaskCalculator {
    protected List<State> states;
    protected List<Transition> transitions;
    protected int startPoint;
    protected int stepsCount;

    protected boolean isTaskStable = false;
    protected int maxDelay;
    protected Integer scale = 16;

    protected boolean isFinished = false;

    protected int currentCalculatedStep = 0;


    @AssistedInject
    public EulerCalculator(@Assisted TaskV4 task) {
        super(task);
        init();
    }

    protected void init() {
        this.states = task.getStates();
        this.transitions = task.getTransitions();
        this.startPoint = task.getStartPoint();
        this.stepsCount = task.getStepsCount();

        this.maxDelay = this.getMaxDelay();

        // init first step
        this.statesCount = new double[task.getStepsCount()][task.getStates().size()];
        if (task.getStepsCount() > 0) {
            for (State state : states) {
                this.statesCount[0][getStateIndex(state)] = state.getCount();
            }
        }
    }


    protected int getMaxDelay() {
        int maxDelay = 0;
        for (Transition transition : this.task.getTransitions()) {
            for (StateInTransition state: transition.getActualStates()) {
                maxDelay = Math.max(maxDelay, state.getDelay());
            }
        }
        return maxDelay;
    }


    @Override
    public void calculateToStep(int step) {
        while (this.currentCalculatedStep < step) {
            this.copyStep(this.currentCalculatedStep);

            for (Transition transition: this.transitions) {
                this.applyTransition(transition, this.currentCalculatedStep + 1);
            }

            if (this.scale != null) {
                this.roundStates(this.currentCalculatedStep + 1, scale);
            }

            if (!this.task.getIsAllowNegative()) {
                this.restrictNegativeness(this.currentCalculatedStep + 1);
            }

            this.progress.set((double)this.currentCalculatedStep / (this.stepsCount - 1));

            this.currentCalculatedStep++;
        }

        this.isFinished = true;
    }


    /**
     * push count to states according to transition
     * @param transition
     * @param step
     */
    protected void applyTransition(Transition transition, int step) {
        if (this.isTransitionResidual(transition)) {
            this.applyResidualTransition(transition, step);
            return;
        }

        this.applyCommonTransition(transition, step);
    }


    protected void applyResidualTransition(Transition transition, int step) {
        final double intensity = this.getIntensity(transition, step - 1) * this.getTotalCount(transition, step - 1) *
                transition.getProbability();

        // sum of all residual states
        double residualSum = transition.getActualStates().stream()
                .filter(state -> state.getMode() == StateMode.RESIDUAL)
                .mapToDouble(state -> this.statesCount[step][this.getStateIndex(state.getState())] - intensity)
                .sum();

        // subtract count from residual states
        transition.getActualStates().stream()
                .filter(state -> state.getMode() == StateMode.RESIDUAL)
                .forEach(state -> {
                    this.statesCount[step][this.getStateIndex(state.getState())] -=
                            this.statesCount[step][this.getStateIndex(state.getState())] - intensity;
                });

        // and add count to all states which Out > 0
        transition.getActualStates().stream()
                .filter(state -> state.getOut() != 0)
                .forEach(state -> {
                    this.statesCount[step][this.getStateIndex(state.getState())] +=
                            this.getStateInTransitionCoefficient(state) * residualSum;
                });
    }


    protected void applyCommonTransition(Transition transition, int step) {
        final double intensity = this.getIntensity(transition, step - 1) * this.getTotalCount(transition, step - 1) *
                transition.getProbability();

        for (StateInTransition state: transition.getActualStates()) {
            // add count to state
            this.statesCount[step][this.getStateIndex(state.getState())] +=
                    this.getStateInTransitionCoefficient(state) * intensity;
        }
    }


    protected double getStateInTransitionCoefficient(StateInTransition state) {
        return state.getOut() - state.getIn();
    }


    /**
     *
     * @param transition
     * @param step
     * @return
     */
    protected double getIntensity(Transition transition, int step) {
        List<StateInTransition> states = transition.getActualStates().stream()
                .filter(stateInTransition -> stateInTransition.getIn() > 0)
                .collect(Collectors.toList());

        double intensity = 0;

        switch (transition.getType()) {
            case TransitionType.LINEAR: {
                intensity = states.stream()
                        .mapToDouble(stateInTransition -> {
                            double count = getDelayedStateCount(stateInTransition, step);
                            double res = count / stateInTransition.getIn();
                            if (stateInTransition.getMode() == StateMode.INHIBITOR) {
                                res = count - res;
                            }
                            return res;
                        })
                        .min()
                        .orElse(0);
                break;
            }

            case TransitionType.SOLUTE:
            case TransitionType.BLEND: {
                final double totalCount = this.getTotalCount(transition, step);
                if (totalCount != 0) {
                    intensity = states.stream()
                        .mapToDouble(stateInTransition -> {
                            double cur = getDelayedStateCount(stateInTransition, step);
                            double in = stateInTransition.getIn();
                            double res = Math.pow(cur, in) / this.probabilisticFactorial(in) / Math.pow(totalCount, in);
                            if (stateInTransition.getMode() == StateMode.INHIBITOR) {
                                res = 1 - res;
                            }
                            return res;
                        })
                        .reduce(1, (a, b) -> a * b);
                }

                break;
            }

            default: {
                throw new UnknownTransitionType("Can't' get intensity for transition with unknown type");
            }
        }

        return intensity;
    }

    /**
     *
     * @param stateInTransition
     * @param step
     * @return state's count with applied delay
     */
    protected double getDelayedStateCount(StateInTransition stateInTransition, int step) {
        int delayedStep = Math.max(0, step - stateInTransition.getDelay());
        return this.statesCount[delayedStep][this.getStateIndex(stateInTransition.getState())];
    }

    /**
     * Вероятностный факториал.
     * Факториал вещественного числа как математическое ожидание
     * от факториалов двух соседних целых.
     *
     * @param u исходное значение
     * @return результат
     */
    protected double probabilisticFactorial(double u) {
        double result = 1;
        double r = u % 1;
        if (r > 0) {
            double v = Math.floor(u);
            for (double i = 2; i <= v; i++) {
                result *= i;
            }
            result = result * (1 - r) + result * (v + 1) * r;
        } else {
            for (double i = 2; i <= u; i++) {
                result *= i;
            }
        }
        return result;
    }

    /**
     * copy step to next
     * @param step step
     */
    protected void copyStep(int step) {
        for (int i = 0; i < this.states.size(); i++) {
            System.arraycopy(this.statesCount[step], 0, this.statesCount[step + 1], 0, this.states.size());
        }
    }


    /**
     *
     * @param state State
     * @return index of state in statesCount
     */
    public int getStateIndex(State state) {
        return this.states.indexOf(state);
    }

    /**
     *
     * @param step step
     * @return states total count in system on step
     */
    protected double getTotalCount(Transition transition, int step) {
        switch (transition.getType()) {
            case TransitionType.LINEAR: {
                return 1;
            }

            case TransitionType.SOLUTE: {
                return this.states.stream()
                        .mapToDouble(state -> this.statesCount[step][this.getStateIndex(state)])
                        .sum();
            }

            case TransitionType.BLEND: {
                return transition.getActualStates().stream()
                        //.filter(stateInTransition -> stateInTransition.getIn() > 0)
                        // remain only distinct states
                        .filter(new Predicate<StateInTransition>() {
                            Set<State> set = new HashSet<>();
                            @Override
                            public boolean test(StateInTransition stateInTransition) {
                                return set.add(stateInTransition.getState());
                            }
                        })
                        .mapToDouble(stateInTransition -> this.statesCount[step][this.getStateIndex(stateInTransition.getState())])
                        .sum();
            }

            default: {
                throw new UnknownTransitionType("Can't' get total count for transition with unknown type");
            }
        }
    }


    /**
     * if state's count < 0, make it equals to zero
     * @param step
     */
    protected void restrictNegativeness(int step) {
        for (int i = 0 ; i < this.statesCount[step].length; i++) {
            if (this.statesCount[step][i] < 0) {
                this.statesCount[step][i] = 0;
            }
        }
    }


    /**
     *
     * @param step
     * @return can task change statesCount (compared on roundedStatesCount)
     */
    protected boolean isTaskStable(int step) {
        if (step >= this.maxDelay + 2) {
            for (int i = 0; i < statesCount[step].length; i++) {
                double val = statesCount[step - maxDelay - 2][i];
                for (int j = 0; j <= maxDelay; j++) {
                    if (statesCount[step - j - 1][i] != val) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }


    /**
     * round states count in statesCount on step with given precision
     * @param step  step
     * @param scale precision
     */
    protected void roundStates(int step, Integer scale) {
        isTaskStable = this.isTaskStable(step);

        if (isTaskStable) {
            for (int i = 0; i < statesCount[step].length; i++) {
                statesCount[step][i] = statesCount[step - 1][i];
            }
        } else {
            double eps = Math.pow(10., -scale);
            for (int i = 0; i < statesCount[step].length; i++) {
                if (statesCount[step][i] <= eps && !this.task.getIsAllowNegative()) {
                    statesCount[step][i] = 0;
                } else {
                    statesCount[step][i] = new BigDecimal(statesCount[step][i]).setScale(scale, RoundingMode.HALF_UP).doubleValue();
                }
            }
        }
    }


    protected boolean isTransitionResidual(Transition transition) {
        for (StateInTransition state: transition.getActualStates()) {
            if (state.getMode() == StateMode.RESIDUAL) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return states count [step][stateIndex]
     */
    public double[][] getStatesCount() {
        return this.statesCount;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public List<State> getStates() {
        return states;
    }

    @Override
    public boolean isFinished() {
        return this.isFinished;
    }
}
