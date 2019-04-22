package population.model.RPNConverter;

import expression.OperandSupplier;

public class VariableOperandSupplier<T> extends OperandSupplier<T> {
    public VariableOperandSupplier(T val) {
        super(val);
    }

    public void setVal(T val) {
        this.val = val;
    }
}
