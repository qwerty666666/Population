package population.model.ParametricPortrait;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import population.model.ColorGenerator.ColorGenerator;
import population.model.StateModel.State;
import population.util.Cloneable;


/**
 * Parametric portrait state settings: state color and visibility in portrait
 */
public class StateSetting implements Cloneable<StateSetting> {
    private ObjectProperty<State> state = new SimpleObjectProperty<>();
    /** should state be shown in portrait */
    private BooleanProperty show = new SimpleBooleanProperty(true);
    /** state color in parametric portrait */
    private ObjectProperty<Color> color = new SimpleObjectProperty<>();

    private ColorGenerator colorGenerator;

    public StateSetting(State state, ColorGenerator colorGenerator) {
        this.setState(state);
        this.colorGenerator = colorGenerator;
        this.setColor(colorGenerator.getNext());
    }

    protected StateSetting() {}

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
        this.color.set(color);
    }

    @Override
    public StateSetting clone() {
        StateSetting clone = new StateSetting();
        clone.state.set(this.getState());
        clone.colorGenerator = this.colorGenerator;
        clone.setColor(this.getColor());
        clone.setShow(this.getShow());
        return clone;
    }
}