package dev.d3nzus.ecs.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Typed singleton store for world-wide state such as time, input or camera. */
public final class Resources {
    private final Map<Class<?>, Object> values = new HashMap<>();

    public <T> void put(Class<T> type, T value) {
        values.put(type, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return (T) values.get(type);
    }

    public <T> T getOrCreate(Class<T> type, Supplier<T> factory) {
        T value = get(type);
        if (value == null) {
            value = factory.get();
            put(type, value);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(Class<T> type) {
        return (T) values.remove(type);
    }

    public boolean has(Class<?> type) {
        return values.containsKey(type);
    }

    public int size() {
        return values.size();
    }

    public void clear() {
        values.clear();
    }
}
