package population.model.RPNConverter;

import expression.Function;
import expression.Operand;
import population.model.StateModel.State;

import java.util.Arrays;
import java.util.List;

/**
 * Find min value from the args or returns 0 if there is no args
 * @param <T>
 */
public class MinFunction<T> extends Function<T> {
    List<Object> args;

    @SafeVarargs
    public MinFunction(Operand<T>... args) {
        this.args = Arrays.asList(args);
    }

    @Override
    public List<Object> getArgs() {
        return this.args;
    }
}
