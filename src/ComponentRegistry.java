package dev.d3nzus.ecs.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Assigns each component class a dense type id within one {@link World}. */
public final class ComponentRegistry {
    private final Map<Class<?>, Integer> ids = new HashMap<>();
    private final List<SparsePool<?>> poolList = new ArrayList<>();
    private SparsePool<?>[] pools = new SparsePool<?>[16];

    public int register(Class<?> type) {
        Integer existing = ids.get(type);
        if (existing != null) {
            return existing;
        }
        int id = poolList.size();
        ids.put(type, id);
        SparsePool<?> pool = new SparsePool<>();
        poolList.add(pool);
        if (id >= pools.length) {
            pools = Arrays.copyOf(pools, pools.length * 2);
        }
        pools[id] = pool;
        return id;
    }

    /** Returns the id of a registered type, or -1 if no component of that type exists yet. */
    public int id(Class<?> type) {
        Integer id = ids.get(type);
        return id == null ? -1 : id;
    }

    public int count() {
        return poolList.size();
    }

    public SparsePool<?> pool(int typeId) {
        return pools[typeId];
    }

    public SparsePool<?>[] allPools() {
        return Arrays.copyOf(pools, poolList.size());
    }

    @SuppressWarnings("unchecked")
    public <T> SparsePool<T> pool(Class<T> type) {
        int id = id(type);
        return id < 0 ? null : (SparsePool<T>) pools[id];
    }

    @SuppressWarnings("unchecked")
    public <T> SparsePool<T> poolOrCreate(Class<T> type) {
        return (SparsePool<T>) pools[register(type)];
    }
}
