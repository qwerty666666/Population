package population.model.RPNConverter;

import expression.Function;
import expression.FunctionExecutor;
import expression.FunctionExecutorProvider;
import expression.OperandSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DoubleFunctionExecutorProvider implements FunctionExecutorProvider<Double> {
    static Map<Class<? extends Function>, FunctionExecutor<Double>> DOUBLE_FUNCTIONS_MAP = new HashMap<>();
    static {
        DOUBLE_FUNCTIONS_MAP.put(SumFunction.class, (Object... args) -> Stream.of(args).mapToDouble(Double.class::cast).sum());
        DOUBLE_FUNCTIONS_MAP.put(MinFunction.class, (Object... args) -> Stream.of(args).mapToDouble(Double.class::cast).min().orElse(0));
    }


    @Override
    public FunctionExecutor<Double> get(Class<? extends Function<Double>> functionClass) {
        if (DOUBLE_FUNCTIONS_MAP.containsKey(functionClass)) {
            return DOUBLE_FUNCTIONS_MAP.get(functionClass);
        }
        throw new IllegalArgumentException("FunctionExecutor implementation for " + functionClass + " doesn't provided");
    }
}
