package dev.d3nzus.ecs.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Typed, queued event dispatcher. Events are delivered by {@link #drain()} at
 * phase boundaries, which keeps handler execution predictable and avoids
 * re-entrant system updates.
 */
public final class EventBus {
    private final Map<Class<?>, List<Consumer<?>>> handlers = new HashMap<>();
    private final ArrayDeque<Object> queue = new ArrayDeque<>();
    private boolean draining;

    public <T> void on(Class<T> type, Consumer<T> handler) {
        handlers.computeIfAbsent(type, key -> new ArrayList<>()).add(handler);
    }

    public <T> boolean off(Class<T> type, Consumer<T> handler) {
        List<Consumer<?>> list = handlers.get(type);
        if (list == null) {
            return false;
        }
        boolean removed = list.remove(handler);
        if (list.isEmpty()) {
            handlers.remove(type);
        }
        return removed;
    }

    public void emit(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        queue.addLast(event);
    }

    public int pending() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }

    @SuppressWarnings("unchecked")
    public void drain() {
        if (draining) {
            return;
        }
        draining = true;
        try {
            while (!queue.isEmpty()) {
                Object event = queue.removeFirst();
                List<Consumer<Object>> targets = new ArrayList<>();
                for (Map.Entry<Class<?>, List<Consumer<?>>> entry : handlers.entrySet()) {
                    if (entry.getKey().isInstance(event)) {
                        for (Consumer<?> handler : entry.getValue()) {
                            targets.add((Consumer<Object>) handler);
                        }
                    }
                }
                for (Consumer<Object> handler : targets) {
                    handler.accept(event);
                }
            }
        } finally {
            draining = false;
        }
    }
}
