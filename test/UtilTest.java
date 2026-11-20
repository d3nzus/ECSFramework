package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.Phase;
import dev.d3nzus.ecs.core.World;
import dev.d3nzus.ecs.util.FixedStepAccumulator;
import dev.d3nzus.ecs.util.MathUtils;
import dev.d3nzus.ecs.util.Parent;
import dev.d3nzus.ecs.util.SpatialHash;
import dev.d3nzus.ecs.util.Time;
import dev.d3nzus.ecs.util.TimeSystem;
import dev.d3nzus.ecs.util.Transform2D;
import dev.d3nzus.ecs.util.TransformHierarchySystem;
import dev.d3nzus.ecs.util.Vec2;
import dev.d3nzus.ecs.util.Vec3;

import java.util.List;

public final class UtilTest {

    public static void testVectorsAndMath() {
        Vec2 vector = new Vec2(1f, 0f).rotate((float) Math.PI / 2f);
        TestRunner.eq(0f, vector.x, "rotated x");
        TestRunner.eq(1f, vector.y, "rotated y");
        TestRunner.eq(1f, new Vec2(3f, 4f).normalize().length(), "normalize length");
        TestRunner.eq(3f, new Vec2(1f, 2f).dot(new Vec2(1f, 1f)), "dot");
        TestRunner.eq(1f, new Vec2(1f, 0f).cross(new Vec2(0f, 1f)), "cross");

        Vec3 cross = new Vec3(1f, 0f, 0f).cross(new Vec3(0f, 1f, 0f));
        TestRunner.eq(0f, cross.x, "cross x");
        TestRunner.eq(0f, cross.y, "cross y");
        TestRunner.eq(1f, cross.z, "cross z");

        TestRunner.eq(5f, MathUtils.clamp(7f, 0f, 5f), "clamp upper");
        TestRunner.eq(0.5f, MathUtils.lerp(0f, 1f, 0.5f), "lerp");
        TestRunner.eq(1f, MathUtils.wrap(11f, 0f, 10f), "wrap");
        TestRunner.eq(9f, MathUtils.wrap(-1f, 0f, 10f), "wrap negative");
    }

    public static void testFixedStepAccumulator() {
        FixedStepAccumulator accumulator = new FixedStepAccumulator(0.25f);
        int[] ticks = {0};
        int steps = accumulator.update(0.6f, ignored -> ticks[0]++);
        TestRunner.eq(2, steps, "completed steps");
        TestRunner.eq(2, ticks[0], "tick callbacks");
        TestRunner.eq(0.4f, accumulator.alpha(), "interpolation alpha");

        accumulator.reset();
        TestRunner.eq(0f, accumulator.alpha(), "reset alpha");
    }

    public static void testSpatialHash() {
        World world = new World();
        SpatialHash grid = new SpatialHash(10f);
        Entity near = world.create();
        Entity far = world.create();
        grid.insert(near, 1f, 1f);
        grid.insert(far, 100f, 100f);
        TestRunner.eq(2, grid.entityCount(), "inserted count");

        List<Entity> found = grid.queryRadius(0f, 0f, 15f);
        TestRunner.eq(1, found.size(), "radius result count");
        TestRunner.eq(near, found.get(0), "radius result entity");

        grid.insert(near, 50f, 50f);
        TestRunner.eq(2, grid.cellCount(), "entity moved to another cell");
        grid.remove(near);
        TestRunner.eq(1, grid.entityCount(), "removed entity");
        TestRunner.eq(0, grid.queryRadius(0f, 0f, 15f).size(), "no stale bucket entries");
    }

    public static void testTransformHierarchy() {
        World world = new World();
        world.systems().add(new TransformHierarchySystem(), Phase.UPDATE);

        Entity parent = world.create();
        Transform2D parentTransform = new Transform2D(10f, 20f);
        parentTransform.rotation = (float) Math.PI / 2f;
        world.add(parent, parentTransform);

        Entity child = world.create();
        world.add(child, new Transform2D(5f, 0f));
        world.add(child, new Parent(parent));

        world.update(0f);
        Transform2D childTransform = world.get(child, Transform2D.class);
        TestRunner.eq(10f, childTransform.worldX, "world x with rotated parent");
        TestRunner.eq(25f, childTransform.worldY, "world y with rotated parent");
        TestRunner.eq((float) Math.PI / 2f, childTransform.worldRotation, "world rotation");
        TestRunner.check(!childTransform.dirty, "dirty flag cleared");

        world.destroy(parent);
        world.update(0f);
        TestRunner.eq(5f, childTransform.worldX, "orphaned child uses local transform");
        TestRunner.eq(Entity.INVALID, world.get(child, Parent.class).value, "stale parent cleared");
    }

    public static void testTimeSystem() {
        World world = new World();
        world.systems().add(new TimeSystem(), Phase.INPUT);
        world.update(0.5f);
        Time time = world.resources().get(Time.class);
        TestRunner.check(time != null, "time resource created");
        TestRunner.eq(0.5f, time.delta, "delta");
        TestRunner.eq(0.5f, time.elapsed, "elapsed");
        TestRunner.eq(1L, time.frame, "frame");
    }
}
