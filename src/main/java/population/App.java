package population;

import population.model.StateModel.State;
import population.model.TaskV4;
import population.model.TransitionModel.Transition;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import population.util.Resources.AppResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;


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


    protected static void initTask() {
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
