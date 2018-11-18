package population.model.Calculator;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import population.model.StateModel.State;
import population.model.TaskV4;
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
        Assertions.assertEquals(2, new EulerCalculator(task).getMaxDelay());
    }


    @Test
    public void getTotalCount() {
        EulerCalculator calc = new EulerCalculator(task);
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
    public void getIntensity() {
        EulerCalculator calc = new EulerCalculator(task);
        Transition transition = task.getTransitions().get(1);
        transition.getStates().get(0).setDelay(0);
        transition.getStates().get(1).setDelay(0);
        transition.getStates().get(0).setIn(1);
        transition.getStates().get(1).setIn(4);

        double expected;
        double actual;

        // LINEAR

        transition.setType(TransitionType.LINEAR);
        expected = 25;
        actual = calc.getIntensity(transition, 0);
        Assertions.assertEquals(expected, actual,
                "getIntensity for LINEAR: expected: " + expected + ", actual: " + actual
        );

        // LINEAR INHIBITOR

        transition.getStates().get(0).setIn(2);
        transition.getStates().get(0).setMode(StateMode.INHIBITOR);
        transition.getStates().get(1).setIn(2);
        expected = 25;
        actual = calc.getIntensity(transition, 0);
        Assertions.assertEquals(expected, actual,
                "getIntensity for LINEAR INHIBITOR: expected: " + expected + ", actual: " + actual
        );
        transition.getStates().get(0).setIn(1);
        transition.getStates().get(0).setMode(StateMode.SIMPLE);
        transition.getStates().get(1).setIn(4);

        // SOLUTE

        transition.setType(TransitionType.SOLUTE);
        expected = 0.0027;
        actual = calc.getIntensity(transition, 0);
        Assertions.assertTrue(Math.abs(expected - actual) < 0.0001,
            "getIntensity for SOLUTE: expected: " + expected + ", actual: " + actual
        );

        // SOLUTE INHIBITOR

        transition.setType(TransitionType.SOLUTE);
        transition.getStates().get(0).setMode(StateMode.INHIBITOR);
        expected = 0.0054;
        actual = calc.getIntensity(transition, 0);
        Assertions.assertTrue(Math.abs(expected - actual) < 0.0001,
                "getIntensity for SOLUTE INHIBITOR: expected: " + expected + ", actual: " + actual
        );
    }


    @Test
    public void applyResidualTransition() {
        EulerCalculator calc = new EulerCalculator(task);
        Transition transition = task.getTransitions().get(1);
        transition.setType(TransitionType.SOLUTE);
        transition.setProbability(0.1);

        transition.getStates().get(0).setDelay(0);
        transition.getStates().get(0).setIn(1);
        transition.getStates().get(0).setOut(0);
        transition.getStates().get(0).setMode(StateMode.RESIDUAL);

        transition.getStates().get(1).setDelay(0);
        transition.getStates().get(1).setIn(0);
        transition.getStates().get(1).setOut(1);

        calc.copyStep(0);
        calc.applyResidualTransition(transition, 1);
        double expected1 = 5;
        double expected2 = 145;
        Assertions.assertAll(
                () -> Assertions.assertTrue(Math.abs(calc.statesCount[1][0] - expected1) < 0.001,
                        "expected: " + expected1 + ", given: " + calc.statesCount[1][0]
                ),
                () -> Assertions.assertTrue(Math.abs(calc.statesCount[1][1] - expected2) < 0.001,
                        "expected: " + expected2 + ", given: " + calc.statesCount[1][1]
                )
        );
    }
}
