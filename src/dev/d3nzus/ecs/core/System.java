package dev.d3nzus.ecs.core;

/**
 * A unit of game logic. Note that this shadows {@code java.lang.System}; use the
 * fully qualified name when standard output is needed in the same source file.
 */
public interface System {
    default void init(World world) {
    }

    void update(World world, float delta);

    default void dispose() {
    }
}
