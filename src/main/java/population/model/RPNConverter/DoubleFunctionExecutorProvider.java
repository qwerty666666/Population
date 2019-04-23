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
        DOUBLE_FUNCTIONS_MAP.put(MultiplyFunction.class, (Object... args) -> Stream.of(args).mapToDouble(Double.class::cast).reduce(1, (a, b) -> a * b));
        DOUBLE_FUNCTIONS_MAP.put(PowFunction.class, (Object... args) -> Math.pow((double)args[0], (double)args[1]));
        DOUBLE_FUNCTIONS_MAP.put(ProbabilisticFactorialFunction.class, ProbabilisticFactorialFunction.EXECUTOR);
    }


    @Override
    public FunctionExecutor<Double> get(Class<? extends Function<Double>> functionClass) {
        if (DOUBLE_FUNCTIONS_MAP.containsKey(functionClass)) {
            return DOUBLE_FUNCTIONS_MAP.get(functionClass);
        }
        throw new IllegalArgumentException("FunctionExecutor implementation for " + functionClass + " doesn't provided");
    }
}
