package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.SparsePool;

public final class SparsePoolTest {

    public static void testAddGetRemove() {
        SparsePool<String> pool = new SparsePool<>();
        pool.add(0, "a");
        pool.add(1, "b");
        pool.add(2, "c");

        TestRunner.eq(3, pool.size(), "size after adds");
        TestRunner.eq("a", pool.get(0), "get index 0");
        TestRunner.check(pool.has(1), "has index 1");
        TestRunner.eq("b", pool.remove(1), "removed value");
        TestRunner.check(!pool.has(1), "removed index absent");
        TestRunner.eq(2, pool.size(), "size after removal");
        TestRunner.eq("a", pool.get(0), "index 0 unchanged");
        TestRunner.eq("c", pool.get(2), "index 2 unchanged by swap");

        pool.add(1, "d");
        TestRunner.eq(3, pool.size(), "size after re-add");
        TestRunner.eq("d", pool.get(1), "re-added value");
        pool.add(1, "e");
        TestRunner.eq(3, pool.size(), "replace does not grow pool");
        TestRunner.eq("e", pool.get(1), "replaced value");
    }

    public static void testDenseIterationAndGrowth() {
        SparsePool<Integer> pool = new SparsePool<>();
        for (int i = 0; i < 1000; i++) {
            pool.add(i * 2, i);
        }
        TestRunner.eq(1000, pool.size(), "grown size");
        int sum = 0;
        for (int slot = 0; slot < pool.size(); slot++) {
            int entityIndex = pool.entityAt(slot);
            TestRunner.eq(0, entityIndex % 2, "even entity indices");
            sum += pool.componentAt(slot);
        }
        TestRunner.eq(499500, sum, "dense component sum");
        pool.clear();
        TestRunner.eq(0, pool.size(), "cleared");
    }
}
