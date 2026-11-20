package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.Entity;

public final class EntityTest {

    public static void testPacking() {
        Entity entity = Entity.of(5, 3);
        TestRunner.eq(5, entity.index(), "index");
        TestRunner.eq(3, entity.generation(), "generation");
        TestRunner.eq(entity, Entity.of(5, 3), "equal handles");
        TestRunner.check(!entity.equals(Entity.of(5, 4)), "different generations differ");
    }

    public static void testInvalidSentinelIsReserved() {
        TestRunner.check(!Entity.INVALID.equals(Entity.of(0, 0)), "invalid is not a real entity");
        TestRunner.check(!Entity.INVALID.equals(Entity.of(Entity.MAX_INDEX, Entity.MAX_GENERATION)),
                "reserved high values are not allocated");
    }

    public static void testRangeChecks() {
        boolean threw = false;
        try {
            Entity.of(Entity.MAX_INDEX + 1, 0);
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        TestRunner.check(threw, "out-of-range index rejected");
    }
}
