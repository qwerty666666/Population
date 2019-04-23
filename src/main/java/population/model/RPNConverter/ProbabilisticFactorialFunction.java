package population.model.RPNConverter;


import expression.Function;
import expression.FunctionExecutor;
import expression.Operand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Вероятностный факториал.
 * Факториал вещественного числа как математическое ожидание
 * от факториалов двух соседних целых.
 */
public class ProbabilisticFactorialFunction<T> extends Function<T> {
    double arg;

    public ProbabilisticFactorialFunction(double arg) {
        this.arg = arg;
    }

    @Override
    public List<Object> getArgs() {
        return Collections.singletonList(arg);
    }


    public static FunctionExecutor<Double> EXECUTOR = (Object... args) -> {
        double u = (double)args[0];
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
    };
}
