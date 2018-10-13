package population.model;

import population.model.StateModel.State;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.Transition;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import population.util.ListUtils;

import java.util.concurrent.atomic.AtomicInteger;


public class TaskV4 {
    protected static final AtomicInteger ID_COUNTER = new AtomicInteger();

    protected int id;
    protected StringProperty name = new SimpleStringProperty("");
    protected ObservableList<State> states;
    protected ObservableList<Transition> transitions;
    protected IntegerProperty startPoint = new SimpleIntegerProperty(0);
    protected IntegerProperty stepsCount = new SimpleIntegerProperty(0);
    protected BooleanProperty isAllowNegative = new SimpleBooleanProperty(false);

    public boolean getIsAllowNegative() {
        return isAllowNegative.get();
    }

    public void setIsAllowNegative(boolean isAllowNegative) {
        this.isAllowNegative.set(isAllowNegative);
    }

    public BooleanProperty isAllowNegativeProperty() {
        return this.isAllowNegative;
    }

    public TaskV4() {
        this.id = ID_COUNTER.incrementAndGet();
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ObservableList<State> getStates() {
        return states;
    }

    public void setStates(ObservableList<State> states) {
        this.states = states;
    }

    public ObservableList<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(ObservableList<Transition> transitions) {
        this.transitions = transitions;
    }

    public int getStartPoint() {
        return startPoint.get();
    }

    public IntegerProperty startPointProperty() {
        return startPoint;
    }

    public void protectedStartPoint(int startPoint) {
        this.startPoint.set(startPoint);
    }

    public int getStepsCount() {
        return stepsCount.get();
    }

    public IntegerProperty stepsCountProperty() {
        return stepsCount;
    }

    public void setStartPoint(int startPoint) {
        this.startPoint.set(startPoint);
    }

    public void setStepsCount(int stepsCount) {
        this.stepsCount.set(stepsCount);
    }

    public TaskV4 clone() {
        TaskV4 clone = new TaskV4();

        clone.setName(this.getName());
        clone.setStartPoint(this.getStartPoint());
        clone.setStepsCount(this.getStepsCount());
        clone.setIsAllowNegative(this.getIsAllowNegative());
        clone.states = ListUtils.cloneObservableList(this.getStates());
        clone.transitions = ListUtils.cloneObservableList(this.getTransitions());

        // replace states in transitions with new cloned states
        for (Transition transition: clone.getTransitions()) {
            for (StateInTransition stateInTransition: transition.getStates()) {
                State oldState = stateInTransition.getState();
                if (oldState != null && !oldState.isEmptyState()) {
                    State newState = clone.getStates().stream()
                        .filter(state -> state.getId() == stateInTransition.getState().getId())
                        .findFirst()
                        .get();
                    stateInTransition.setState(newState);
                }
            }
        }

        return clone;
    }
}
