package population.model.Calculator;

import expression.ExpressionBuilder;
import expression.RPNExpression;
import population.model.RPNConverter.StateOperandSupplier;
import population.model.StateModel.State;

import java.util.List;
import java.util.Map;

public class DifferentialEquationSystem<T> {
    private Map<State, RPNExpression<Double>> ODEs;
    private List<StateOperandSupplier<T>> variables;


    public DifferentialEquationSystem(Map<State, RPNExpression<Double>> ODEs, List<StateOperandSupplier<T>> variables) {
        this.ODEs = ODEs;
        this.variables = variables;
    }

    public Map<State, RPNExpression<Double>> getODEs() {
        return ODEs;
    }

    public List<StateOperandSupplier<T>> getVariables() {
        return variables;
    }
}
