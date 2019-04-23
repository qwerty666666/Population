package population.model.RPNConverter;

import expression.*;
import population.model.Calculator.DifferentialEquationSystem;
import population.model.Exception.UnknownTransitionType;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.StateMode;
import population.model.TransitionModel.Transition;
import population.model.TransitionType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Convert task model to system of differential equations presented by array of
 * Reverse Polish Notation expressions
 */
public class RPNConverter {
    TaskV4 task;
    Map<StateOperandSupplierKey, StateOperandSupplier<Double>> stateOperandSuppliers = new HashMap<>();
    Map<State, ExpressionBuilder<Double>> ODEs = new HashMap<>();


    public RPNConverter(TaskV4 task) {
        this.task = task;
        this.task.getStates().forEach(state -> this.ODEs.put(state, new ExpressionBuilder<Double>().add(0.)));
    }


    private StateOperandSupplier<Double> getStateOperandSupplier(State state) {
        return this.getStateOperandSupplier(state, 0);
    }


    private StateOperandSupplier<Double> getStateOperandSupplier(StateInTransition state) {
        return this.getStateOperandSupplier(state.getState(), state.getDelay());
    }


    private StateOperandSupplier<Double> getStateOperandSupplier(State state, int delay) {
        StateOperandSupplierKey key = new StateOperandSupplierKey(state, delay);

        if (this.stateOperandSuppliers.containsKey(key)) {
            return this.stateOperandSuppliers.get(key);
        }

        this.stateOperandSuppliers.put(key, new StateOperandSupplier<>(state.getCount(), state, delay));

        return this.stateOperandSuppliers.get(key);
    }


    public DifferentialEquationSystem<Double> convert() {
        task.getTransitions().forEach(this::buildTransitionODE);

        List<RPNExpression<Double>> ODEs = this.task.getStates().stream()
            .map(state -> new ShuntingYardRPNConverter<Double>().convert(new ExpressionBuilderTokenizer<>(this.ODEs.get(state)).tokenize()))
            .collect(Collectors.toList());

        List<VariableOperandSupplier<Double>> variables = new ArrayList<>(this.stateOperandSuppliers.values());

        Map<RPNExpression<Double>, VariableOperandSupplier<Double>> expressionVariablesMap = new HashMap<>();
        for (int i = 0; i < this.task.getStates().size(); i++) {
            expressionVariablesMap.put(ODEs.get(i), this.getStateOperandSupplier(task.getStates().get(i)));
        }

        return new DifferentialEquationSystem<>(ODEs, variables, expressionVariablesMap);
    }


    /**
     * Convert task transition to ODE (ordinary differential equation)
     */
    private void buildTransitionODE(Transition transition) {
        if (transition.isResidual()) {
            buildResidualExpression(transition);
        } else {
            buildNormalExpression(transition);
        }
    }


    private void buildResidualExpression(Transition transition) {
        ExpressionBuilder<Double> intensity = new ExpressionBuilder<Double>()
            .add(transition.getProbability())
            .multiply(getIntensity(transition))
            .multiply(getTotalCount(transition));

        List<StateInTransition> residualStates = transition.getActualStates().stream()
            .filter(state -> state.getMode() == StateMode.RESIDUAL)
            .collect(Collectors.toList());

        // sum of all residual states
        Operand[] args = residualStates.stream()
            .map(stateInTransition -> {
                return new ExpressionBuilder<Double>()
                        .add(this.getStateOperandSupplier(stateInTransition.getState()))
                        .subtract(intensity);
            })
            .toArray(Operand[]::new);
        Operand<Double> residualSum = new SumFunction<Double>(args);

        // subtract count from residual states
        residualStates.forEach(stateInTransition -> {
            this.ODEs.get(stateInTransition.getState()).subtract(
                new ExpressionBuilder<Double>()
                    .add(this.getStateOperandSupplier(stateInTransition.getState()))
                    .subtract(intensity)
            );
        });

        // and add count to all states which Out > 0
        transition.getActualStates().stream()
            .filter(stateInTransition -> stateInTransition.getOut() != 0)
            .forEach(stateInTransition -> {
                this.ODEs.get(stateInTransition.getState())
                    .add(getStateInTransitionCoefficient(stateInTransition))
                    .multiply(residualSum);
            });
    }


        private void buildNormalExpression(Transition transition) {
            ExpressionBuilder<Double> intensity = new ExpressionBuilder<Double>()
                .add(transition.getProbability())
                .multiply(getIntensity(transition))
                .multiply(getTotalCount(transition));

            transition.getActualStates().forEach(stateInTransition -> {
                this.ODEs.get(stateInTransition.getState())
                    .add(getStateInTransitionCoefficient(stateInTransition))
                    .multiply(intensity);
            });
    }


    private double getStateInTransitionCoefficient(StateInTransition state) {
        return state.getOut() - state.getIn();
    }


    /**
     * @return Expression Operand representing intensity formula
     */
    private Operand<Double> getIntensity(Transition transition) {
        List<StateInTransition> states = transition.getActualStates().stream()
            .filter(stateInTransition -> stateInTransition.getIn() > 0)
            .collect(Collectors.toList());

        switch (transition.getType()) {
            case TransitionType.LINEAR: {
                Operand[] args = states.stream()
                    .map(stateInTransition -> {
                        StateOperandSupplier<Double> count = this.getStateOperandSupplier(stateInTransition);
                        double in = stateInTransition.getIn();

                        ExpressionBuilder<Double> res = new ExpressionBuilder<Double>()
                            .add(count)
                            .divide(in);

                        if (stateInTransition.getMode() == StateMode.INHIBITOR) {
                            return new ExpressionBuilder<Double>()
                                .add(count)
                                .subtract(res);
                        }

                        return res;
                    })
                    .toArray(Operand[]::new);

                return new MinFunction<Double>(args);
            }

            case TransitionType.SOLUTE:
            case TransitionType.BLEND: {
                Operand<Double> totalCount = this.getTotalCount(transition);
                // TODO if totalCount == 0 return 0

                Operand[] args = states.stream()
                    .map(stateInTransition -> {
                        StateOperandSupplier<Double> cur = this.getStateOperandSupplier(stateInTransition);
                        double in = stateInTransition.getIn();

                        ExpressionBuilder<Double> res = new ExpressionBuilder<Double>()
                            .add(new PowFunction<>(cur, in))
                            .divide(new ProbabilisticFactorialFunction<>(in))
                            .divide(new PowFunction<>(totalCount, in));

                        if (stateInTransition.getMode() == StateMode.INHIBITOR) {
                            return new ExpressionBuilder<Double>()
                                .add(1.)
                                .subtract(res);
                        }

                        return res;
                    })
                    .toArray(Operand[]::new);

                return new MultiplyFunction<Double>(args);
            }

            default: {
                throw new UnknownTransitionType("Can't' get intensity for transition with unknown type");
            }
        }
    }


    /**
     * @return Expression Operand representing total count formula
     */
    private Operand<Double> getTotalCount(Transition transition) {
        switch (transition.getType()) {
            case TransitionType.LINEAR: {
                return new OperandSupplier<>(1.);
            }

            case TransitionType.SOLUTE: {
                VariableOperandSupplier[] args = this.task.getStates().stream()
                    .map(this::getStateOperandSupplier)
                    .toArray(VariableOperandSupplier[]::new);
                return new SumFunction<Double>(args);
            }

            case TransitionType.BLEND: {
                VariableOperandSupplier[] statesArgs = transition.getActualStates().stream()
                    //.filter(stateInTransition -> stateInTransition.getIn() > 0)
                    // remain only distinct states
                    .filter(new Predicate<StateInTransition>() {
                        Set<State> set = new HashSet<>();
                        @Override
                        public boolean test(StateInTransition stateInTransition) {
                            return set.add(stateInTransition.getState());
                        }
                    })
                    .map(StateInTransition::getState)
                    .map(this::getStateOperandSupplier)
                    .toArray(VariableOperandSupplier[]::new);

                return new SumFunction<Double>(statesArgs);
            }

            default: {
                throw new UnknownTransitionType("Can't' get total count for transition with unknown type");
            }
        }
    }


    /**
     * Class representing key in HashMap for equations variables
     */
    private class StateOperandSupplierKey {
        State state;
        int delay;

        StateOperandSupplierKey(State state, int delay) {
            this.state = state;
            this.delay = delay;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StateOperandSupplierKey that = (StateOperandSupplierKey) o;
            return delay == that.delay &&
                Objects.equals(state, that.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, delay);
        }
    }
}
