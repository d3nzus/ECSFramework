package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.Phase;
import dev.d3nzus.ecs.core.System;
import dev.d3nzus.ecs.core.World;

import java.util.ArrayList;
import java.util.List;

public final class SystemTest {

    public static void testPhaseAndPriorityOrdering() {
        World world = new World();
        List<String> order = new ArrayList<>();
        world.systems().add(recorder("render", order), Phase.RENDER, 0);
        world.systems().add(recorder("update-low", order), Phase.UPDATE, 10);
        world.systems().add(recorder("input-late", order), Phase.INPUT, 5);
        world.systems().add(recorder("cleanup", order), Phase.CLEANUP, 0);
        world.systems().add(recorder("input-early", order), Phase.INPUT, -5);
        world.systems().add(recorder("update-high", order), Phase.UPDATE, -10);

        world.update(0f);
        TestRunner.eq(List.of("input-early", "input-late", "update-high", "update-low", "render", "cleanup"),
                order, "execution order");
        TestRunner.eq(6, world.systems().count(), "system count");
    }

    public static void testLifecycleHooks() {
        World world = new World();
        List<String> calls = new ArrayList<>();
        System system = new System() {
            @Override
            public void init(World w) {
                calls.add("init");
            }

            @Override
            public void update(World w, float delta) {
                calls.add("update");
            }

            @Override
            public void dispose() {
                calls.add("dispose");
            }
        };
        world.systems().add(system);
        world.update(0f);
        world.update(0f);
        TestRunner.eq(List.of("init", "update", "update"), calls, "init once, update per frame");
        TestRunner.check(world.systems().remove(system), "system removed");
        TestRunner.eq(List.of("init", "update", "update", "dispose"), calls, "dispose on removal");
    }

    public static void testLateAddedSystemIsInitialized() {
        World world = new World();
        world.update(0f);
        List<String> calls = new ArrayList<>();
        world.systems().add(new System() {
            @Override
            public void init(World w) {
                calls.add("init");
            }

            @Override
            public void update(World w, float delta) {
                calls.add("update");
            }
        });
        TestRunner.eq(List.of("init"), calls, "late system initialized immediately");
    }

    private static System recorder(String name, List<String> order) {
        return new System() {
            @Override
            public void update(World world, float delta) {
                order.add(name);
            }
        };
    }
}
