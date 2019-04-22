package population.model.RPNConverter;


import expression.Function;
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
}
