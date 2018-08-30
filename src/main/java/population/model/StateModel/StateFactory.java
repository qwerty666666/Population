package population.model.StateModel;

public class StateFactory {
    public State makeEmptyState() {
        State state = new State();
        state.setId(State.EMPTY_STATE_ID);
        return state;
    }
}
