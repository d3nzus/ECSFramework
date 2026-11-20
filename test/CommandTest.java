package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.Query;
import dev.d3nzus.ecs.core.QueryResult;
import dev.d3nzus.ecs.core.World;

import java.util.concurrent.atomic.AtomicInteger;

public final class CommandTest {

    public static final class Marker {
    }

    public static void testDeferredDestroy() {
        World world = new World();
        Entity entity = world.create();
        world.destroy(entity);
        TestRunner.check(world.isAlive(entity), "destroy queued");
        TestRunner.eq(1, world.commands().pending(), "one pending command");
        world.update(0f);
        TestRunner.eq(0, world.commands().pending(), "queue drained");
        TestRunner.check(!world.isAlive(entity), "entity destroyed after flush");
    }

    public static void testDeferredAddAndRemove() {
        World world = new World();
        Entity entity = world.create();

        world.commands().add(entity, new Marker());
        TestRunner.eq(0, world.componentCount(Marker.class), "not applied yet");
        world.update(0f);
        TestRunner.eq(1, world.componentCount(Marker.class), "add applied on flush");
        TestRunner.check(world.has(entity, Marker.class), "component present");

        world.commands().remove(entity, Marker.class);
        TestRunner.eq(1, world.componentCount(Marker.class), "remove not applied yet");
        world.update(0f);
        TestRunner.eq(0, world.componentCount(Marker.class), "remove applied on flush");
    }

    public static void testSnapshotStableWhileDestroying() {
        World world = new World();
        for (int i = 0; i < 4; i++) {
            Entity entity = world.create();
            world.add(entity, new Marker());
        }
        QueryResult snapshot = world.query(Query.all(Marker.class));
        AtomicInteger visited = new AtomicInteger();
        snapshot.forEachEntity(entity -> {
            visited.incrementAndGet();
            world.destroy(entity);
        });
        TestRunner.eq(4, visited.get(), "all entities visited before destroy takes effect");
        TestRunner.eq(4, world.query(Query.all(Marker.class)).size(), "query unchanged during phase");
        world.update(0f);
        TestRunner.eq(0, world.entityCount(), "all entities destroyed after flush");
    }

    public static void testFlushAtPhaseBoundary() {
        World world = new World();
        Entity entity = world.create();
        world.commands().add(entity, new Marker());

        dev.d3nzus.ecs.core.System observer = new dev.d3nzus.ecs.core.System() {
            @Override
            public void update(World w, float delta) {
                TestRunner.check(w.has(entity, Marker.class), "command applied before UPDATE phase");
            }
        };
        world.systems().add(observer, dev.d3nzus.ecs.core.Phase.UPDATE);
        world.update(0f);
    }
}
