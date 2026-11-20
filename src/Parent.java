package dev.d3nzus.ecs.util;

import dev.d3nzus.ecs.core.Entity;

/** Optional component pointing at another entity's {@link Transform2D}. */
public final class Parent {
    public Entity value;

    public Parent() {
        this(Entity.INVALID);
    }

    public Parent(Entity value) {
        this.value = value;
    }
}
