package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.World;

public final class WorldTest {

    public static final class Position {
        public float x;
    }

    public static final class Velocity {
    }

    public static void testLifecycleAndGenerations() {
        World world = new World();
        Entity first = world.create();
        TestRunner.check(world.isAlive(first), "new entity alive");
        TestRunner.eq(1, world.entityCount(), "entity count");

        world.add(first, new Position());
        TestRunner.check(world.has(first, Position.class), "component present");
        TestRunner.check(world.get(first, Position.class) != null, "component retrievable");
        TestRunner.eq(1, world.componentCount(Position.class), "pool size");

        world.destroy(first);
        TestRunner.check(world.isAlive(first), "destroy is deferred");
        world.update(0f);
        TestRunner.check(!world.isAlive(first), "destroy applied on flush");
        TestRunner.eq(0, world.entityCount(), "entity count after destroy");
        TestRunner.eq(0, world.componentCount(Position.class), "component removed on destroy");

        Entity reused = world.create();
        TestRunner.eq(first.index(), reused.index(), "index reused");
        TestRunner.eq(first.generation() + 1, reused.generation(), "generation incremented");
        TestRunner.check(!world.isAlive(first), "stale handle rejected");
    }

    public static void testComponentOperations() {
        World world = new World();
        Entity entity = world.create();
        Position position = new Position();
        world.add(entity, position);
        TestRunner.check(world.get(entity, Position.class) == position, "same instance returned");
        TestRunner.check(world.remove(entity, Position.class) == position, "removed instance returned");
        TestRunner.check(!world.has(entity, Position.class), "component gone");
        TestRunner.eq(0, world.componentCount(Position.class), "pool empty");
        TestRunner.check(world.get(entity, Velocity.class) == null, "unregistered type returns null");
        TestRunner.check(world.remove(entity, Velocity.class) == null, "removing absent type is null");
    }

    public static void testClear() {
        World world = new World();
        for (int i = 0; i < 5; i++) {
            Entity entity = world.create();
            world.add(entity, new Position());
        }
        world.clear();
        TestRunner.eq(0, world.entityCount(), "clear removes entities");
        TestRunner.eq(0, world.componentCount(Position.class), "clear removes components");
        TestRunner.eq(11L, world.structuralVersion(), "clear bumps version");
    }
}
