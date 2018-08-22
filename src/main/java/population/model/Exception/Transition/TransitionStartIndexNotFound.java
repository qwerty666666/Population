package population.model.Exception.Transition;

/**
 * start index of transition was not found
 */
public class TransitionStartIndexNotFound extends RuntimeException {
    public TransitionStartIndexNotFound(String message) {
        super(message);
    }
}
