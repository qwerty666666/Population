package population.model.Calculator;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.stream.Collectors;


public class ResultTableRow {
    protected IntegerProperty step = new SimpleIntegerProperty();
    protected ObservableList<DoubleProperty> statesCount = FXCollections.observableArrayList();

    public ResultTableRow(int step, List<Double> statesCount) {
        this.step.set(step);
        this.statesCount.addAll(
                statesCount.stream()
                    .map(x -> x == null ? null : new SimpleDoubleProperty(x))
                    .collect(Collectors.toList())
        );
    }

    public IntegerProperty stepProperty() {
        return this.step;
    }

    public int getStep() {
        return step.get();
    }

    public ObservableList<DoubleProperty> getStatesCount() {
        return this.statesCount;
    }
}
