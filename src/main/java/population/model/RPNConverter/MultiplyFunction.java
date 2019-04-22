package population.model.RPNConverter;

import expression.Function;
import expression.Operand;

import java.util.Arrays;
import java.util.List;

/**
 * Multiply all args or returns 1 if there is no args
 * @param <T>
 */
public class MultiplyFunction<T> extends Function<T> {
    List<Object> args;

    @SafeVarargs
    public MultiplyFunction(Operand<T>... args) {
        this.args = Arrays.asList(args);
    }

    @Override
    public List<Object> getArgs() {
        return this.args;
    }
}
