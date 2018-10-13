package population.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import population.model.StateModel.State;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.StateMode;
import population.model.TransitionModel.Transition;
import java.util.Arrays;


public class TaskV4Test {
    private TaskV4 task;

    @BeforeEach
    public void setUp() throws Exception {
        /*
         * states
         */
        population.model.StateModel.State state1 = new population.model.StateModel.State();
        population.model.StateModel.State state2 = new population.model.StateModel.State();

        state1.setCount(50);
        state2.setCount(100);

        ObservableList<State> states = FXCollections.observableList(Arrays.asList(
            state1,
            state2
        ));


        /*
         * transitions
         */
        ObservableList<population.model.TransitionModel.Transition> transitions = FXCollections.observableList(Arrays.asList(
            new population.model.TransitionModel.Transition(),
            new Transition()
        ));

        transitions.get(0).getStates().addAll(
            new StateInTransition(state1, 1, 1, 1, StateMode.SIMPLE),
            new StateInTransition(state2, 0, 1, 0, StateMode.SIMPLE)
        );
        transitions.get(1).getStates().addAll(
            new StateInTransition(state1, 1, 1, 2, StateMode.SIMPLE),
            new StateInTransition(state2, 1, 0, 1, StateMode.SIMPLE)
        );


        /*
         * task
         */
        this.task = new TaskV4();
        task.setStates(states);
        task.setTransitions(transitions);
        task.setStartPoint(0);
        task.setStepsCount(100);
    }

    @Test
    public void cloneTask() {
        TaskV4 clone = task.clone();

        Assertions.assertNotSame(clone.getStates().get(0), task.getStates().get(0),
            "states should not be the same"
        );
        Assertions.assertNotSame(clone.getTransitions().get(0), task.getTransitions().get(0),
            "transitions should not be the same"
        );
        Assertions.assertNotSame(clone.getTransitions().get(0).getStates().get(0), task.getTransitions().get(0).getStates().get(0),
            "stateInTransitions should not be the same"
        );
        Assertions.assertNotSame(clone.getTransitions().get(0).getStates().get(0).getState(), task.getTransitions().get(0).getStates().get(0).getState(),
            "states in stateInTransitions should not be the same"
        );
    }
}
