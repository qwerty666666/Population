package population.model.ParametricPortrait;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import population.component.ParametricPortrait;
import population.model.StateModel.State;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Use to group parametric portraits by the same StateSettings list for preserve state colors the same
 * for all parametric portraits within the single group
 */
public class StateSettingsGroup {
    private static List<Color> defaultColors;
    static {
        /*
         * set defaultColors from javafx.scene.paint.Color constants
         */
        // this colors will be first in color list
        List<Color> customColors = new ArrayList<>(Arrays.asList(
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.LIME,
            Color.FIREBRICK,
            Color.DARKMAGENTA,
            Color.MAROON)
        );

        try {
            List<Color> colors = new ArrayList<>();
            Class colorClass = Class.forName("javafx.scene.paint.Color");

            if (colorClass == null) {
                throw new ClassNotFoundException();
            }

            Field[] fields = colorClass.getFields();
            for (Field field : fields) {
                Object obj = field.get(null);
                if (obj instanceof Color) {
                    colors.add((Color) obj);
                }
            }
            defaultColors = colors;

            // use custom colors first
            defaultColors.remove(Color.TRANSPARENT);
            int i = 0;
            for (Color color: customColors) {
                Collections.swap(defaultColors, i++, defaultColors.indexOf(color));
            }

        } catch (Exception e) {
            defaultColors = customColors;
        }
    }



    /** map color to state */
    private Map<State, Color> stateColorMap = new HashMap<>();
    /** index of next color for this stateSettingsList in default colors */
    private AtomicInteger nextColorInd = new AtomicInteger(0);
    /** list of StateSettings */
    private ObservableList<StateSettings> stateSettingsList = FXCollections.observableArrayList();
    /** stateSettingsList of StateSettings */
    private ObservableList<ParametricPortrait> parametricPortraitsList = FXCollections.observableArrayList();


    public StateSettingsGroup() {}

    public void add(int index, StateSettings stateSettings) {
        stateSettingsList.add(index, stateSettings);
    }

    public void add(StateSettings stateSettings) {
        stateSettingsList.add(stateSettings);
    }

    public void add(ParametricPortrait parametricPortrait) {
        parametricPortraitsList.add(parametricPortrait);
    }

    public ObservableList<StateSettings> getStateSettingsList() {
        return stateSettingsList;
    }

    /**
     * @return List of parametric portraits within the group
     */
    public ObservableList<ParametricPortrait> getParametricPortraitsList() {
        return parametricPortraitsList;
    }

    /**
     * @return next free color for state
     */
    public Color getNextColor() {
        return defaultColors.get(nextColorInd.getAndIncrement() % defaultColors.size());
    }

    /**
     * @param state state
     * @return color for state within group
     */
    public Color getStateColor(State state) {
        return this.stateColorMap.get(state);
    }

    /**
     * set color for all states within the group
     */
    public void setStateColor(State state, Color color) {
        this.stateColorMap.put(state, color);
    }

    /**
     * update parametric portraits fill in group
     */
    public void updateGroup() {
        parametricPortraitsList.forEach(ParametricPortrait::updateParametricPortraitFill);
    }

    /**
     * bind state list updates to this group
     */
    public void bindToStateList(ObservableList<State> states) {
        states.addListener((ListChangeListener<State>) c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        this.getStateSettingsList().get(i).setState(c.getList().get(i));
                    }

                } else if (c.wasPermutated()) {
                    int from = c.getFrom(), to = c.getTo();
                    List<StateSettings> copy = new ArrayList<>(this.getStateSettingsList().subList(from, to));
                    for (int oldIndex = from; oldIndex < to; oldIndex++) {
                        int newIndex = c.getPermutation(oldIndex);
                        this.getStateSettingsList().set(newIndex, copy.get(oldIndex - from));
                    }

                } else {
                    int i = 0;
                    for (State state: c.getAddedSubList()) {
                        this.add(c.getFrom() + i, new StateSettings(state, this));
                        i++;
                    }

                    for (i = c.getRemovedSize() - 1; i >= 0; i--) {
                        this.getStateSettingsList().remove(c.getFrom() + i);
                    }
                }
            }
        });
    }

    @Override
    public StateSettingsGroup clone() {
        StateSettingsGroup clone = new StateSettingsGroup();
        for (StateSettings stateSettings: this.stateSettingsList) {
            StateSettings newStateSettings = new StateSettings(stateSettings.getState(), clone);
            newStateSettings.setShow(stateSettings.getShow());
            newStateSettings.setColor(stateSettings.getColor());
            clone.add(newStateSettings);
        }
        return clone;
    }
}

