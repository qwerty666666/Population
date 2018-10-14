package population.model.Calculator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import population.App;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.StateInTransition;
import population.model.TransitionModel.StateMode;
import population.model.TransitionModel.Transition;
import population.model.TransitionType;
import population.util.TaskParser;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;


@DisplayName("Mobilization Task")
public class InhibitorAndResidualTaskTest {
    TaskV4 task;


    @BeforeEach
    public void setUp() {
        TaskV4 task = new TaskV4();

        State adult = new State();
        adult.setCount(20);

        State grand = new State();
        grand.setCount(20);

        State child = new State();
        child.setCount(4);

        State area = new State();
        area.setCount(10);

        task.setStates(FXCollections.observableArrayList(Arrays.asList(
            adult,
            grand,
            child,
            area
        )));

        Transition transition1 = new Transition();
        transition1.getStates().addAll(
            new StateInTransition(area, 1, 0, 0, StateMode.SIMPLE),
            new StateInTransition(child, 1, 0, 0, StateMode.SIMPLE),
            new StateInTransition(adult, 0, 1, 0, StateMode.SIMPLE)
        );
        transition1.setType(TransitionType.LINEAR);
        transition1.setProbability(1);

        Transition transition2 = new Transition();
        transition2.getStates().addAll(
            new StateInTransition(area, 1, 0, 0, StateMode.SIMPLE),
            new StateInTransition(child, 1, 0, 0, StateMode.RESIDUAL),
            new StateInTransition(adult, 0, 0, 0, StateMode.SIMPLE)
        );
        transition2.setType(TransitionType.LINEAR);
        transition2.setProbability(1);

        Transition transition3 = new Transition();
        transition3.getStates().addAll(
            new StateInTransition(adult, 2, 2, 0, StateMode.SIMPLE),
            new StateInTransition(grand, 1, 1, 0, StateMode.SIMPLE),
            new StateInTransition(child, 2, 1, 0, StateMode.INHIBITOR)
        );
        transition3.setType(TransitionType.SOLUTE);
        transition3.setProbability(0.5);

        task.setTransitions(FXCollections.observableArrayList(
            transition1,
            transition2,
            transition3
        ));

        task.setStartPoint(0);
        task.setStepsCount(2);

        this.task = task;
    }


    @Test
    public void getIntensityLinearInhibitor() {
        Transition transition = task.getTransitions().get(2);
        transition.setType(TransitionType.LINEAR);

        Calculator calc = new Calculator(task);
        calc.copyStep(0);
        calc.applyTransition(transition, 1);

        double[] expected = new double[]{20, 20, 0, 10};
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertTrue(Math.abs(calc.statesCount[1][i] - expected[i]) < 0.001,
                "expected: " + expected[i] + ", given: " + calc.statesCount[1][i]
            );
        }
    }


    @Test
    public void getIntensitySoluteInhibitor() {
        Transition transition = task.getTransitions().get(2);

        Calculator calc = new Calculator(task);
        calc.copyStep(0);
        calc.applyTransition(transition, 1);

        double[] expected = new double[]{20, 20, 3.316, 10};
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertTrue(Math.abs(calc.statesCount[1][i] - expected[i]) < 0.001,
                "expected: " + expected[i] + ", given: " + calc.statesCount[1][i]
            );
        }
    }


    @Test
    public void applyResidualLinearTransition() {
        Transition transition = task.getTransitions().get(1);
        transition.setType(TransitionType.LINEAR);

        Calculator calc = new Calculator(task);

        calc.copyStep(0);
        calc.applyResidualTransition(transition, 1);

        double[] expected = new double[]{20, 20, 4, 10};
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertTrue(Math.abs(calc.statesCount[1][i] - expected[i]) < 0.001,
                "expected: " + expected[i] + ", given: " + calc.statesCount[1][i]
            );
        }
    }


    @Test
    public void applyResidualSoluteTransition() {
        Transition transition = task.getTransitions().get(1);
        transition.setType(TransitionType.SOLUTE);

        Calculator calc = new Calculator(task);

        calc.copyStep(0);
        calc.applyResidualTransition(transition, 1);

        double[] expected = new double[]{20, 20, 0.74, 10};
        for (int i = 0; i < expected.length; i++) {
            Assertions.assertTrue(Math.abs(calc.statesCount[1][i] - expected[i]) < 0.001,
                "expected: " + expected[i] + ", given: " + calc.statesCount[1][i]
            );
        }
    }
}