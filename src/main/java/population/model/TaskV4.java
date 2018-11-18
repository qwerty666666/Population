package population.model;

import population.model.StateModel.State;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.Transition;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import population.util.ListUtils;

import java.util.Comparator;
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


    public TaskV4() {
        this.id = ID_COUNTER.incrementAndGet();
    }


    /************************
     *
     *  allow negative prop
     *
     ************************/

    public boolean getIsAllowNegative() {
        return isAllowNegative.get();
    }

    public void setIsAllowNegative(boolean isAllowNegative) {
        this.isAllowNegative.set(isAllowNegative);
    }

    public BooleanProperty isAllowNegativeProperty() {
        return this.isAllowNegative;
    }


    /************************
     *
     *       name prop
     *
     ************************/

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }


    /************************
     *
     *       states prop
     *
     ************************/

    public ObservableList<State> getStates() {
        return states;
    }

    public void setStates(ObservableList<State> states) {
        this.states = states;
    }


    /************************
     *
     *    transitions prop
     *
     ************************/

    public ObservableList<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(ObservableList<Transition> transitions) {
        this.transitions = transitions;
    }


    /************************
     *
     *    start point prop
     *
     ************************/

    public int getStartPoint() {
        return startPoint.get();
    }

    public void setStartPoint(int startPoint) {
        this.startPoint.set(startPoint);
    }

    public IntegerProperty startPointProperty() {
        return startPoint;
    }


    /************************
     *
     *    steps count prop
     *
     ************************/

    public int getStepsCount() {
        return stepsCount.get();
    }

    public IntegerProperty stepsCountProperty() {
        return stepsCount;
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


    public Transition getTransitionById(int id) {
        return this.transitions.stream()
            .filter(transition -> transition.getId() == id)
            .findFirst()
            .orElse(null);
    }


    public State getStateById(int id) {
        return this.states.stream()
            .filter(state -> state.getId() == id)
            .findFirst()
            .orElse(null);
    }


    public int getMaxDelay() {
         return this.transitions.stream()
             .flatMap(transition -> transition.getActualStates().stream())
             .map(StateInTransition::getDelay)
             .max(Comparator.comparingInt(o -> o))
             .orElse(0);
    }
}
