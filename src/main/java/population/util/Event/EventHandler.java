package population.util.Event;


public interface EventHandler<T extends Event> {
    public boolean handle(T event);
}
