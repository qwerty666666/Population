package population.model.RPNConverter;

import expression.Function;
import expression.Operand;
import expression.OperandSupplier;

import java.util.Arrays;
import java.util.List;

/**
 * Sum all the arguments or return 0 if there is no args
 * @param <T>
 */
public class SumFunction<T> extends Function<T> {
    List<Object> args;

    @SafeVarargs
    public SumFunction(Operand<T>... args) {
        this.args = Arrays.asList(args);
    }

    @Override
    public List<Object> getArgs() {
        return this.args;
    }
}
