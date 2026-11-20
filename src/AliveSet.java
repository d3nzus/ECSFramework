package dev.d3nzus.ecs.core;

import java.util.Arrays;

/**
 * Sparse set of currently alive entity indices. Doubles as the iteration source
 * for queries that have no mandatory component type.
 */
final class AliveSet {
    private int[] dense = new int[16];
    private int[] sparse = new int[256];
    private int size;

    boolean has(int entityIndex) {
        return entityIndex >= 0 && entityIndex < sparse.length && sparse[entityIndex] != 0;
    }

    void add(int entityIndex) {
        if (has(entityIndex)) {
            return;
        }
        ensureSparse(entityIndex);
        if (size == dense.length) {
            dense = Arrays.copyOf(dense, dense.length * 2);
        }
        dense[size] = entityIndex;
        sparse[entityIndex] = size + 1;
        size++;
    }

    void remove(int entityIndex) {
        int slot = slot(entityIndex);
        if (slot < 0) {
            return;
        }
        int last = size - 1;
        if (slot != last) {
            dense[slot] = dense[last];
            sparse[dense[slot]] = slot + 1;
        }
        dense[last] = 0;
        sparse[entityIndex] = 0;
        size = last;
    }

    int size() {
        return size;
    }

    int get(int slot) {
        return dense[slot];
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
}
