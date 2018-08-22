package population.util.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventManager {
    protected static Map<Event, List<EventHandler<? extends Event>>> eventHandlers = new HashMap<>();

    public static void addEventHandler(Event event, EventHandler<? extends Event> handler) {
        if (!eventHandlers.containsKey(event)) {
            eventHandlers.put(event, new ArrayList<>());
        }
        eventHandlers.get(event).add(handler);
    }

    public static void removeEventHandler(Event event, EventHandler<Event> eventHandler) {
        if (eventHandlers.containsKey(event)) {
            eventHandlers.get(event).remove(eventHandler);
        }
    }

    public static <T extends Event> boolean fireEvent(T event) {
        List<EventHandler<? extends Event>> handlers = eventHandlers.get(event);
        if (handlers != null) {
            for (EventHandler handler: handlers) {
                if (!handler.handle(event)) {
                    return false;
                }
            }
        }
        return true;
    }
}
