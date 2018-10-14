package population.model.TransitionModel;

import population.model.StateModel.State;
import javafx.beans.property.*;

public class StateInTransition {
    protected ObjectProperty<State> state = new SimpleObjectProperty<>(null);
    protected DoubleProperty in = new SimpleDoubleProperty(0);
    protected DoubleProperty out = new SimpleDoubleProperty(0);
    protected IntegerProperty delay = new SimpleIntegerProperty(0);
    protected IntegerProperty mode = new SimpleIntegerProperty(StateMode.SIMPLE);


    public StateInTransition() {}

    public StateInTransition(State state, double in, double out, int delay, int mode) {
        this.setState(state);
        this.setIn(in);
        this.setOut(out);
        this.setDelay(delay);
        this.setMode(mode);
    }


    public int getMode() {
        return mode.get();
    }

    public IntegerProperty modeProperty() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode.set(mode);
    }

    public ObjectProperty<State> stateProperty() {
        return this.state;
    }

    public DoubleProperty inProperty() {
        return this.in;
    }

    public DoubleProperty outProperty() {
        return this.out;
    }

    public State getState() {
        return state.get();
    }

    public double getIn() {
        return in.get();
    }

    public double getOut() {
        return out.get();
    }

    public int getDelay() {
        return delay.get();
    }

    public IntegerProperty delayProperty() {
        return this.delay;
    }

    public void setState(State state) {
        this.state.set(state);
    }

    public void setIn(double in) {
        this.in.set(in);
    }

    public void setOut(double out) {
        this.out.set(out);
    }

    public void setDelay(int delay) {
        this.delay.set(delay);
    }
}
