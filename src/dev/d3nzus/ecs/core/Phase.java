package dev.d3nzus.ecs.core;

/** Fixed execution phases for systems, in execution order. */
public enum Phase {
    INPUT,
    UPDATE,
    RENDER,
    CLEANUP
}
