package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.Resources;
import dev.d3nzus.ecs.core.World;

public final class ResourceTest {

    public static final class Config {
        public int value;
    }

    public static void testPutGetRemove() {
        Resources resources = new Resources();
        Config config = new Config();
        resources.put(Config.class, config);
        TestRunner.check(resources.has(Config.class), "resource present");
        TestRunner.check(resources.get(Config.class) == config, "same resource instance");
        TestRunner.eq(1, resources.size(), "resource count");
        TestRunner.check(resources.remove(Config.class) == config, "removed instance");
        TestRunner.check(!resources.has(Config.class), "resource gone");
        TestRunner.check(resources.get(Config.class) == null, "missing resource is null");
    }

    public static void testGetOrCreate() {
        World world = new World();
        Config first = world.resources().getOrCreate(Config.class, Config::new);
        Config second = world.resources().getOrCreate(Config.class, Config::new);
        TestRunner.check(first == second, "created once");
        world.resources().clear();
        TestRunner.eq(0, world.resources().size(), "cleared");
    }
}
