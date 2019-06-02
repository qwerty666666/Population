package population.model.StateModel;

import population.model.Expression.ExpressionManager;
import javafx.beans.property.*;
import population.util.Cloneable;

import java.util.concurrent.atomic.AtomicInteger;


public class State implements Cloneable<State> {
    /**
     * used to show empty states in transitions table
     */
    public static final int EMPTY_STATE_ID = -1;

    private int id;
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty alias = new SimpleStringProperty("");
    private final DoubleProperty count = new SimpleDoubleProperty();


    public State() {
        id = ID_COUNTER.incrementAndGet();
    }

    protected State(int id) {
        this.id = id;
    }


    public StringProperty nameProperty() {
        return this.name;
    }

    public StringProperty aliasProperty() {
        return this.alias;
    }

    public String getName() {
        return this.name.getValue();
    }

    public void setName(String name) {
        this.nameProperty().setValue(name);
    }

    public void setAlias(String name) {
        this.aliasProperty().setValue(name);
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEmptyState() {
        return this.id == EMPTY_STATE_ID;
    }

    public double getCount() {
        return count.get();
    }

    public void setCount(double count) {
        this.count.set(count);
    }

    public int getId() {
        return id;
    }

    public String getAlias() {
        return alias.get();
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof State && this.id == ((State)obj).id;
    }


    @Override
    public State clone() {
        State clone = new State(this.getId());

        clone.setName(this.getName());
        clone.setAlias(this.getAlias());
        clone.setCount(this.getCount());

        return clone;
    }
}
