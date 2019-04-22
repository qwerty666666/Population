package population.model.RPNConverter;


import expression.Function;
import expression.Operand;

import java.util.Arrays;
import java.util.List;


/**
 * Math power function
 * @param <T>
 */
public class PowFunction<T> extends Function<T> {
    Operand<T> operand;
    double pow;

    public PowFunction(Operand<T> operand, double pow) {
        this.operand = operand;
        this.pow = pow;
    }

    @Override
    public List<Object> getArgs() {
        return Arrays.asList(operand, pow);
    }
}
