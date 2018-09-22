package population.model.ParametricPortrait;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import population.model.StateModel.State;


/**
 * parametric portrait state settings
 */
public class StateSettings {
    private ObjectProperty<State> state = new SimpleObjectProperty<>();
    /** should state be shown in portrait */
    private BooleanProperty show = new SimpleBooleanProperty(true);
    /** state color in parametric portrait */
    private ObjectProperty<Color> color = new SimpleObjectProperty<>();
    /** group to which the state setting belongs */
    private StateSettingsGroup stateSettingsGroup;

    public StateSettings(State state, StateSettingsGroup stateSettingsGroup) {
        this.setState(state);
        this.stateSettingsGroup = stateSettingsGroup;

        Color color = stateSettingsGroup.getStateColor(state);
        if (color == null) {
            color = stateSettingsGroup.getNextColor();
        }
        setColor(color);
    }

    public ObjectProperty<State> stateProperty() {
        return state;
    }

    public State getState() {
        return state.get();
    }

    public void setState(State state) {
        this.state.set(state);
    }

    public BooleanProperty showProperty() {
        return show;
    }

    public Boolean getShow() {
        return show.get();
    }

    public void setShow(Boolean show) {
        this.show.set(show);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public Color getColor() {
        return color.get();
    }

    public void setColor(Color color) {
        this.stateSettingsGroup.setStateColor(state.get(), color);
        this.color.set(color);
    }
}