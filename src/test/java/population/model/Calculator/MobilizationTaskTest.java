package population.model.Calculator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.StateMode;
import population.model.TransitionModel.Transition;
import population.model.TransitionType;

import java.util.Arrays;
import java.util.stream.Stream;


@DisplayName("Mobilization Task")
public class MobilizationTaskTest {
    @BeforeEach
    public void setUp() throws Exception {
    }


    protected TaskV4 getMobilizationTask() {
        /*
         * states
         */
        State aliveState = new State();
        State diedState = new State();

        ObservableList<State> states = FXCollections.observableList(Arrays.asList(
            aliveState,
            diedState
        ));


        /*
         * transitions
         */
        ObservableList<Transition> transitions = FXCollections.observableList(Arrays.asList(
            new Transition(),
            new Transition()
        ));

        transitions.get(0).getStates().addAll(
            new StateInTransition(aliveState, 1, 1, 0, StateMode.SIMPLE),
            new StateInTransition(aliveState, 1, 0, 0, StateMode.SIMPLE),
            new StateInTransition(diedState, 0, 1, 0, StateMode.SIMPLE)
        );
        transitions.get(1).getStates().addAll(
            new StateInTransition(aliveState, 1, 1, 0, StateMode.SIMPLE),
            new StateInTransition(diedState, 1, 0, 0, StateMode.SIMPLE),
            new StateInTransition(aliveState, 0, 1, 0, StateMode.SIMPLE)
        );


        /*
         * task
         */
        TaskV4 task = new TaskV4();
        task.setStates(states);
        task.setTransitions(transitions);
        task.setStartPoint(0);
        task.setStepsCount(100);
        return task;
    }


    static Stream<Arguments> getTaskParameters() {
        return Stream.of(
            //1
            Arguments.of(
                20, 80,
                0.6, TransitionType.LINEAR,
                0.2, TransitionType.LINEAR,
                0, 100,
                100, 0.001
            ),
            // 2
            Arguments.of(
                20, 80,
                0.2, TransitionType.LINEAR,
                0.6, TransitionType.LINEAR,
                75, 25,
                100, 0.001
            ),
            // 3
            Arguments.of(
                80, 20,
                0.2, TransitionType.LINEAR,
                0.2, TransitionType.LINEAR,
                50, 50,
                100, 0.001
            ),
            // 4
            Arguments.of(
                20, 80,
                0.2, TransitionType.LINEAR,
                0.6, TransitionType.SOLUTE,
                66.666, 33.333,
                100, 0.001
            ),
            // 5
            Arguments.of(
                20, 80,
                0.2, TransitionType.LINEAR,
                0.6, TransitionType.BLEND,
                66.666, 33.333,
                100, 0.001
            ),
            // 6
            Arguments.of(
                20, 80,
                0.6, TransitionType.LINEAR,
                0.2, TransitionType.BLEND,
                0, 100,
                100, 0.001
            ),
            // 7
            Arguments.of(
                20, 80,
                0.6, TransitionType.LINEAR,
                0.2, TransitionType.SOLUTE,
                0, 100,
                100, 0.001
            ),
            // 8
            Arguments.of(
                20, 80,
                1, TransitionType.SOLUTE,
                0.225, TransitionType.LINEAR,
                45, 55,
                100, 0.001
            ),
            // 9
            Arguments.of(
                20, 80,
                0.2, TransitionType.SOLUTE,
                0.6, TransitionType.LINEAR,
                87.298, 12.702,
                100, 0.001
            ),
            // 10
            Arguments.of(
                20, 80,
                0.2, TransitionType.SOLUTE,
                0.6, TransitionType.SOLUTE,
                85.714, 14.286,
                100, 0.001
            ),
            // 11
            Arguments.of(
                20, 80,
                0.6, TransitionType.BLEND,
                0.2, TransitionType.LINEAR,
                54.858, 45.141,
                100, 0.001
            ),
            // 12
            Arguments.of(
                20, 80,
                0.2, TransitionType.BLEND,
                0.6, TransitionType.LINEAR,
                87.298, 12.701,
                100, 0.001
            ),
            // 13
            Arguments.of(
                20, 80,
                0.2, TransitionType.BLEND,
                0.6, TransitionType.BLEND,
                85.714, 14.285,
                100, 0.001
            )
        );
    }


    @ParameterizedTest
    @MethodSource("getTaskParameters")
    public void calculate(
        double aliveCount, double diedCount,
        double firstProbability, int firstTransitionType,
        double secondProbability, int secondTransitionType,
        double expectedAlive, double expectedDied,
        int step, double eps
    ) {
        TaskV4 task = this.getMobilizationTask();

        task.getStates().get(0).setCount(aliveCount);
        task.getStates().get(1).setCount(diedCount);

        task.getTransitions().get(0).setProbability(firstProbability);
        task.getTransitions().get(0).setType(firstTransitionType);
        task.getTransitions().get(0).normalizeStates();

        task.getTransitions().get(1).setProbability(secondProbability);
        task.getTransitions().get(1).setType(secondTransitionType);
        task.getTransitions().get(1).normalizeStates();

        EulerCalculator calculator = new EulerCalculator(task);
        calculator.calculate();
        double[][] calculationResult = calculator.getStatesCount();

        double actualAlive = calculationResult[step - 1][0];
        double actualDied = calculationResult[step - 1][1];

        Assertions.assertAll(
                () -> Assertions.assertTrue(Math.abs(expectedAlive - actualAlive) < eps,
                        "expectedAlive: " + expectedAlive + ", actual Alive: " + actualAlive
                ),
                () -> Assertions.assertTrue(Math.abs(expectedDied - actualDied) < eps,
                        "expectedDied: " + expectedDied + ", actual Died: " + actualDied
                )
        );
    }
}