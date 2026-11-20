package dev.d3nzus.ecs.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A cached, immutable snapshot of the entities matching a {@link Query}.
 * The snapshot is recomputed lazily whenever the world's structural version
 * changes, so holding on to a {@code QueryResult} across frames is safe.
 */
public final class QueryResult implements Iterable<Entity> {
    private static final int[] EMPTY = new int[0];

    private final World world;
    private final int[] entityIndices;
    private final long version;

    QueryResult(World world, int[] entityIndices, long version) {
        this.world = world;
        this.entityIndices = entityIndices;
        this.version = version;
    }

    public int size() {
        return entityIndices.length;
    }

    public boolean isEmpty() {
        return entityIndices.length == 0;
    }

    public Entity get(int index) {
        return world.entityAt(entityIndices[index]);
    }

    /** Raw entity index, useful for allocation-free direct component access. */
    public int entityIndexAt(int index) {
        return entityIndices[index];
    }

    public void forEachEntity(Queries.ForEach action) {
        for (int index : entityIndices) {
            action.accept(world.entityAt(index));
        }
    }

    public boolean contains(Entity entity) {
        if (!world.isAlive(entity)) {
            return false;
        }
        for (int index : entityIndices) {
            if (index == entity.index()) {
                return true;
            }
        }
        return false;
    }

    long version() {
        return version;
    }

    @Override
    public Iterator<Entity> iterator() {
        return new Iterator<>() {
            private int cursor;

            @Override
            public boolean hasNext() {
                return cursor < entityIndices.length;
            }

            @Override
            public Entity next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return world.entityAt(entityIndices[cursor++]);
            }
        };
    }
}
