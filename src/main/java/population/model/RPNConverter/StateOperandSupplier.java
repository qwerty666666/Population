package population.model.RPNConverter;

public class StateOperandSupplier<T> extends VariableOperandSupplier<T> {
    private int delay;

    public StateOperandSupplier(T val, int delay) {
        this(val);
        this.delay = delay;
    }

    private StateOperandSupplier(T val) {
        super(val);
    }

    public void setVal(T val) {
        this.val = val;
    }

    public int getDelay() {
        return delay;
    }
}
