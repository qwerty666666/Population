package population.model.RPNConverter;

import population.model.StateModel.State;

public class StateOperandSupplier<T> extends VariableOperandSupplier<T> {
    private int delay;
    private State state;

    public StateOperandSupplier(T val, State state, int delay) {
        this(val);
        this.delay = delay;
        this.state = state;
    }

    private StateOperandSupplier(T val) {
        super(val);
    }

    public void setVal(T val) {
        this.val = val;
    }

    public State getState() {
        return state;
    }

    public int getDelay() {
        return delay;
    }
}
