package population.model.Calculator;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionMode;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.StateMode;
import population.model.TransitionModel.Transition;
import population.model.TransitionType;

import java.util.Arrays;

public class CalculatorTest {
    TaskV4 task;


    @BeforeEach
    public void setUp() throws Exception {
        /*
         * states
         */
        State state1 = new State();
        State state2 = new State();

        state1.setCount(50);
        state2.setCount(100);

        ObservableList<State> states = FXCollections.observableList(Arrays.asList(
                state1,
                state2
        ));


        /*
         * transitions
         */
        ObservableList<Transition> transitions = FXCollections.observableList(Arrays.asList(
                new Transition(),
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
    public void getMaxDelay() {
        Assertions.assertEquals(2, new Calculator(task).getMaxDelay());
    }


    @Test
    public void getTotalCount() {
        Calculator calc = new Calculator(task);
        double expected;

        task.getTransitions().get(0).setType(TransitionType.LINEAR);
        expected = 1;
        Assertions.assertEquals(expected, calc.getTotalCount(task.getTransitions().get(0), 0));

        task.getTransitions().get(0).setType(TransitionType.SOLUTE);
        expected = 150;
        Assertions.assertEquals(expected, calc.getTotalCount(task.getTransitions().get(0), 0));

        task.getTransitions().get(0).setType(TransitionType.BLEND);
        expected = 150;
        Assertions.assertEquals(expected, calc.getTotalCount(task.getTransitions().get(0), 0));
    }


    @Test
    public void getIntensityLinear() {
        Calculator calc = new Calculator(task);
        Transition transition = task.getTransitions().get(1);
        transition.getStates().get(0).setDelay(0);
        transition.getStates().get(1).setDelay(0);
        transition.getStates().get(0).setIn(1);
        transition.getStates().get(1).setIn(4);
        transition.setType(TransitionType.LINEAR);

        double expected = 25;
        double actual = calc.getIntensity(transition, 0);
        Assertions.assertEquals(expected, actual,
            "getIntensity for LINEAR: expected: " + expected + ", actual: " + actual
        );
    }


    @Test
    public void getIntensitySolute() {
        Calculator calc = new Calculator(task);
        Transition transition = task.getTransitions().get(1);
        transition.getStates().get(0).setDelay(0);
        transition.getStates().get(1).setDelay(0);
        transition.getStates().get(0).setIn(1);
        transition.getStates().get(0).setMode(StateMode.SIMPLE);
        transition.getStates().get(1).setIn(4);
        transition.setType(TransitionType.SOLUTE);

        double expected = 0.0027;
        double actual = calc.getIntensity(transition, 0);
        Assertions.assertTrue(Math.abs(expected - actual) < 0.0001,
            "getIntensity for SOLUTE: expected: " + expected + ", actual: " + actual
        );
    }
}
