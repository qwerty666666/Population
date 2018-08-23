package population.model.StateModel;

import population.model.Expression.ExpressionManager;
import javafx.beans.property.*;

import java.util.concurrent.atomic.AtomicInteger;


public class State {
    /**
     * used to show empty states in transitions table
     */
    public static final int EMPTY_STATE_ID = -1;

    private int id;
    private static final AtomicInteger ID_COUNTER = new AtomicInteger();

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty alias = new SimpleStringProperty("");
    private final DoubleProperty count = new SimpleDoubleProperty();
    private final DoubleProperty delay = new SimpleDoubleProperty();
    private final DoubleProperty delayedCount = new SimpleDoubleProperty();
    private final SimpleObjectProperty growthRate = new SimpleObjectProperty<ExpressionManager>();


    public State() {
        id = ID_COUNTER.incrementAndGet();
    }


    public StringProperty nameProperty() {
        return this.name;
    }

    public StringProperty aliasProperty() {
        return this.alias;
    }

    public DoubleProperty delayProperty() {
        return this.delay;
    }

    public DoubleProperty delayedCountProperty() {
        return this.delayedCount;
    }

    public DoubleProperty countProperty() {
        return this.count;
    }

    public SimpleObjectProperty growthRateProperty() {
        return this.growthRate;
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
        return obj != null && this.id == ((State)obj).id;
    }
}
