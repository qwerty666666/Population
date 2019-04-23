package population.model.RPNConverter;

import expression.Operator;
import expression.OperatorExecutor;
import expression.OperatorExecutorProvider;
import operators.AddOperator;
import operators.DivideOperator;
import operators.MultiplyOperator;
import operators.SubtractOperator;

import java.util.HashMap;
import java.util.Map;


public class DoubleOperatorExecutorProvider implements OperatorExecutorProvider<Double> {
    static Map<Class<? extends Operator>, OperatorExecutor<Double>> DOUBLE_OPERATORS_MAP = new HashMap<>();
    static {
        DOUBLE_OPERATORS_MAP.put(AddOperator.class, (left, right) -> left + right);
        DOUBLE_OPERATORS_MAP.put(SubtractOperator.class, (left, right) -> left - right);
        DOUBLE_OPERATORS_MAP.put(MultiplyOperator.class, (left, right) -> left * right);
        DOUBLE_OPERATORS_MAP.put(DivideOperator.class, (left, right) -> {
            if (right == 0) {
                return 0.;
            }
            return left / right;
        });
    }

    @Override
    public OperatorExecutor<Double> get(Class<? extends Operator<Double>> operatorClass) {
        if (DOUBLE_OPERATORS_MAP.containsKey(operatorClass)) {
            return DOUBLE_OPERATORS_MAP.get(operatorClass);
        }
        throw new IllegalArgumentException("OperatorExecutor implementation for " + operatorClass + " doesn't provided");
    }
}
