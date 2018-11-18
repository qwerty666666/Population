package population;

import population.controller.Calculation.CalculationController;
import population.controller.Calculation.ResultChartController;
import population.controller.Calculation.ResultTableController;
import population.controller.ParametricPortraitTabController;
import population.controller.PrimaryController1;
import population.model.ParametricPortrait.ParametricPortrait;
import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.Transition;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import population.util.Resources.AppResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Global state of application
 */
public class App {

    public static void init() {
        initTask();
    }


    public static boolean isDev() {
        return AppResource.getString("buildType").equals("dev");
    }

    public static String getVersion() {
        return AppResource.getString("version");
    }

    /**
     * Clear calculation graphic and table
     */
    public static void clearCalculationEnvironment() {
        App.getResultChartController().clear();
        App.getResultTableController().clear();
    }

    /**
     * Open chart tab
     */
    public static void openChartTab() {
        App.getPrimaryController().openCalculationTab();
        App.getResultChartController().openChartTab();
    }

    /**
     * Calculate global task
     */
    public static void calculateTask() {
        App.getCalculationController().calculate();
    }


    /************************************************
     *
     *               App Controllers
     *
     ***********************************************/

    /**
     * save all instantiated controllers to map
     */
    private static Map<Class, Object> controllers = new HashMap<>();

    static void setController(Class classType, Object controller) {
        controllers.put(classType, controller);
    }

    private static Object getController(Class classType) {
        return controllers.get(classType);
    }

    public static PrimaryController1 getPrimaryController() {
        return (PrimaryController1)App.getController(PrimaryController1.class);
    }

    public static ResultChartController getResultChartController() {
        return (ResultChartController)App.getController(ResultChartController.class);
    }

    public static ResultTableController getResultTableController() {
        return (ResultTableController)App.getController(ResultTableController.class);
    }

    public static CalculationController getCalculationController() {
        return (CalculationController)App.getController(CalculationController.class);
    }

    public static ParametricPortraitTabController getParametricPortraitController() {
        return (ParametricPortraitTabController)App.getController(ParametricPortraitTabController.class);
    }

    /************************************************
     *
     *                  Global Task
     *
     ***********************************************/

    /**
     * Global task is current task for the whole Application. It's states and transitions are displayed
     * and its task should be calculated
     */
    protected static ObjectProperty<TaskV4> task =  new SimpleObjectProperty<>(new TaskV4());


    private static void initTask() {
        ObservableList<State> states = FXCollections.observableList(new ArrayList<>(),
            (State s) -> new Observable[] {s.nameProperty()}
        );
        ObservableList<Transition> transitions = FXCollections.observableArrayList();

        TaskV4 task = getTask();
        task.setStates(states);
        task.setTransitions(transitions);
    }

    /**
     *
     * @return States from global task
     */
    public static TaskV4 getTask() {
        return task.get();
    }

    /**
     *
     * @return States from global task
     */
    public static ObservableList<State> getStates() {
        return getTask().getStates();
    }

    /**
     *
     * @return Transitions from global task
     */
    public static ObservableList<Transition> getTransitions() {
        return getTask().getTransitions();
    }


    /**
     * Set global task properties according to newTask
     */
    public static void setTask(TaskV4 newTask) {
        TaskV4 t = task.get();

        t.getStates().clear();
        t.getStates().addAll(newTask.getStates());

        t.getTransitions().clear();
        t.getTransitions().addAll(newTask.getTransitions());

        t.setStartPoint(newTask.getStartPoint());
        t.setStepsCount(newTask.getStepsCount());
        t.setName(newTask.getName());
    }

    public static void clearTask() {
        TaskV4 t = task.get();

        t.getStates().clear();
        t.getTransitions().clear();
        t.setStartPoint(0);
        t.setStepsCount(0);
        t.setName("");
    }
}
