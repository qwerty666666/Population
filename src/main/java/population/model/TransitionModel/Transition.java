package population.model.TransitionModel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import population.model.State;
import population.model.TransitionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Transition {
    protected int id;
    protected static final AtomicInteger ID_COUNTER = new AtomicInteger();

    protected DoubleProperty probability = new SimpleDoubleProperty();
    protected StringProperty block = new SimpleStringProperty("");
    protected IntegerProperty type = new SimpleIntegerProperty();
    protected ObservableList<StateInTransition> states = FXCollections.observableArrayList();


    public Transition() {
        this.id = ID_COUNTER.incrementAndGet();
    }

    protected Transition(Integer id) {
        if (id == null) {
            this.id = ID_COUNTER.incrementAndGet();
        } else {
            this.id = id;
        }
    }

    public int getId() {
        return this.id;
    }

    public IntegerProperty typeProperty() {
        return this.type;
    }

    public StringProperty blockProperty() {
        return this.block;
    }

    public DoubleProperty probabilityProperty() {
        return this.probability;
    }

    public void setProbabilityProperty(DoubleProperty prop) {
        this.probability = prop;
    }

    public ObservableList<StateInTransition> getStates() {
        return this.states;
    }

    public List<StateInTransition> getActualStates() {
        return this.states.stream()
                .filter(state -> state != null && state.getState() != null && !state.getState().isEmptyState())
                .collect(Collectors.toList());
    }

    public int getType() {
        return type.get();
    }

    public double getProbability() {
        return probability.get();
    }

    public void setProbability(double probability) {
        this.probability.set(probability);
    }

    public void setType(int type) {
        this.type.set(type);
    }

    public void setTypeProperty(IntegerProperty type) {
        this.type = type;
    }

    public String getBlock() {
        return block.get();
    }

    public void setBlock(String block) {
        this.block.set(block);
    }

    @Override
    protected Transition clone() throws CloneNotSupportedException {
        Transition clone = new Transition();
        clone.setProbability(this.getProbability());
        clone.setBlock(this.getBlock());
        clone.setType(this.getType());
        clone.getStates().addAll(this.getStates());
        return clone;
    }

    /**
     *
     * @return Transition transition clone with the same id and properties linked to this properties
     */
    public Transition cloneWithPreserveProperties() {
        Transition clone = new Transition(this.id);
        clone.probability = this.probability;
        clone.block = this.block;
        clone.type = this.type;
        clone.states = this.states;
        return clone;
    }


    /**
     * Use this method only for import from legacy versions
     * combines the same states in one state
     */
    public void normalizeStates() {
        List<StateInTransition> states = new ArrayList<>();

        for (StateInTransition stateInTransition: this.getStates()) {
            if (states.stream().anyMatch(state -> state.getState() == stateInTransition.getState())) {
                continue;
            }

            List<StateInTransition> sameStates = this.states.stream()
                    .filter(state -> state.getState() == stateInTransition.getState())
                    .collect(Collectors.toList());

            if (sameStates.size() > 1) {
                double in = sameStates.stream().mapToDouble(StateInTransition::getIn).sum();
                double out = sameStates.stream().mapToDouble(StateInTransition::getOut).sum();

                if (this.getType() == TransitionType.BLEND || this.getType() == TransitionType.SOLUTE) {
                    stateInTransition.setIn(in);
                    stateInTransition.setOut(out);
                } else if (this.getType() == TransitionType.LINEAR) {
                    double maxIn = sameStates.stream().mapToDouble(StateInTransition::getIn).max().orElse(0);
                    stateInTransition.setIn(maxIn);
                    stateInTransition.setOut(out - (in - maxIn));
                }
            }

            states.add(stateInTransition);
        }

        this.getStates().clear();
        this.getStates().addAll(states);
    }
}
