package population.model.Calculator;

import expression.RPNExpression;
import population.model.RPNConverter.VariableOperandSupplier;

import java.util.List;
import java.util.Map;


public class DifferentialEquationSystem<T> {
    private List<RPNExpression<T>> ODEs;
    /** variables in ODEs */
    private List<VariableOperandSupplier<T>> variables;
    /** variables representing ODEs */
    private Map<RPNExpression<T>, VariableOperandSupplier<T>> expressionVariables;


    public DifferentialEquationSystem(
        List<RPNExpression<T>> ODEs,
        List<VariableOperandSupplier<T>> variables,
        Map<RPNExpression<T>, VariableOperandSupplier<T>> expressionVariables
    ) {
        this.ODEs = ODEs;
        this.variables = variables;
        this.expressionVariables = expressionVariables;
    }

    public List<RPNExpression<T>> getODEs() {
        return ODEs;
    }

    public List<VariableOperandSupplier<T>> getVariables() {
        return variables;
    }

    public VariableOperandSupplier<T> getOdeVariable(RPNExpression<T> expr) {
        return this.expressionVariables.get(expr);
    }
}
