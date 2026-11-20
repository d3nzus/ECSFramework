package dev.d3nzus.ecs.core;

import java.util.Arrays;

/**
 * Dense component array plus an entity-index-to-dense-slot lookup.
 *
 * <p>Adding, reading and removing a component are all O(1). Removal uses the
 * swap-with-last trick, so dense iteration order is intentionally unspecified.
 * Slot 0 in the sparse array means "absent", which lets new sparse entries be
 * zero-filled without an explicit initialization pass.</p>
 */
public final class SparsePool<T> {
    private Object[] components = new Object[16];
    private int[] entities = new int[16];
    private int[] sparse = new int[256];
    private int size;

    public int size() {
        return size;
    }

    public boolean has(int entityIndex) {
        return entityIndex >= 0 && entityIndex < sparse.length && sparse[entityIndex] != 0;
    }

    @SuppressWarnings("unchecked")
    public T get(int entityIndex) {
        int slot = slot(entityIndex);
        return slot < 0 ? null : (T) components[slot];
    }

    /** Adds or replaces the component for an entity index. */
    public void add(int entityIndex, T component) {
        if (has(entityIndex)) {
            components[sparse[entityIndex] - 1] = component;
            return;
        }
        ensureSparse(entityIndex);
        if (size == components.length) {
            growDense();
        }
        int slot = size++;
        components[slot] = component;
        entities[slot] = entityIndex;
        sparse[entityIndex] = slot + 1;
    }

    @SuppressWarnings("unchecked")
    public T remove(int entityIndex) {
        int slot = slot(entityIndex);
        if (slot < 0) {
            return null;
        }
        Object removed = components[slot];
        int last = size - 1;
        if (slot != last) {
            components[slot] = components[last];
            entities[slot] = entities[last];
            sparse[entities[slot]] = slot + 1;
        }
        components[last] = null;
        entities[last] = 0;
        sparse[entityIndex] = 0;
        size = last;
        return (T) removed;
    }

    public void clear() {
        Arrays.fill(components, 0, size, null);
        Arrays.fill(entities, 0, size, 0);
        Arrays.fill(sparse, 0);
        size = 0;
    }

    public int entityAt(int slot) {
        checkSlot(slot);
        return entities[slot];
    }

    @SuppressWarnings("unchecked")
    public T componentAt(int slot) {
        checkSlot(slot);
        return (T) components[slot];
    }

    private void checkSlot(int slot) {
        if (slot < 0 || slot >= size) {
            throw new IndexOutOfBoundsException("slot " + slot + ", size " + size);
        }
    }

    private int slot(int entityIndex) {
        return (entityIndex >= 0 && entityIndex < sparse.length) ? sparse[entityIndex] - 1 : -1;
    }

    private void ensureSparse(int entityIndex) {
        if (entityIndex >= sparse.length) {
            int newLength = sparse.length;
            while (newLength <= entityIndex) {
                newLength *= 2;
            }
            sparse = Arrays.copyOf(sparse, newLength);
        }
    }

    private void growDense() {
        int newLength = components.length * 2;
        components = Arrays.copyOf(components, newLength);
        entities = Arrays.copyOf(entities, newLength);
    }
}
