package population.model.StateModel;

public class StateFactory {
    public State createEmptyState() {
        State state = new State();
        state.setId(State.EMPTY_STATE_ID);
        return state;
    }
}
