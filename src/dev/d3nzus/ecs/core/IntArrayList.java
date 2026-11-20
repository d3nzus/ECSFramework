package dev.d3nzus.ecs.core;

import java.util.Arrays;

/** Minimal growable int array used internally for dense entity index lists. */
public final class IntArrayList {
    private static final int[] EMPTY = new int[0];

    private int[] data = new int[16];
    private int size;

    public void add(int value) {
        if (size == data.length) {
            data = Arrays.copyOf(data, data.length * 2);
        }
        data[size++] = value;
    }

    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        return data[index];
    }

    public void set(int index, int value) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + ", size " + size);
        }
        data[index] = value;
    }

    public int removeLast() {
        if (size == 0) {
            throw new IllegalStateException("list is empty");
        }
        return data[--size];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        size = 0;
    }

    public int[] toArray() {
        return size == 0 ? EMPTY : Arrays.copyOf(data, size);
    }
}
