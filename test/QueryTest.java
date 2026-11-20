package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.Query;
import dev.d3nzus.ecs.core.Query1;
import dev.d3nzus.ecs.core.Query2;
import dev.d3nzus.ecs.core.QueryResult;
import dev.d3nzus.ecs.core.World;

import java.util.HashSet;
import java.util.Set;

public final class QueryTest {

    public static final class A {
    }

    public static final class B {
    }

    public static final class C {
    }

    public static final class D {
    }

    private static World populatedWorld() {
        World world = new World();
        Entity ab = world.create();
        world.add(ab, new A());
        world.add(ab, new B());
        Entity abc = world.create();
        world.add(abc, new A());
        world.add(abc, new B());
        world.add(abc, new C());
        Entity a = world.create();
        world.add(a, new A());
        Entity c = world.create();
        world.add(c, new C());
        world.create();
        return world;
    }

    public static void testAllAnyNone() {
        World world = populatedWorld();
        TestRunner.eq(3, world.query(Query.all(A.class)).size(), "all A");
        TestRunner.eq(2, world.query(Query.all(A.class, B.class)).size(), "all A+B");
        TestRunner.eq(2, world.query(Query.all(A.class).any(B.class)).size(), "A with any(B)");
        TestRunner.eq(2, world.query(Query.all(A.class).none(C.class)).size(), "A without C");
        TestRunner.eq(2, world.query(Query.all(B.class).any(A.class, C.class)).size(), "B with any(A,C)");
        TestRunner.eq(5, world.query(Query.all()).size(), "no constraints matches all");
        TestRunner.eq(0, world.query(Query.all(D.class)).size(), "unregistered type matches nothing");
        TestRunner.eq(0, world.query(Query.all(A.class).any(D.class)).size(),
                "any with only unregistered types matches nothing");
    }

    public static void testExclusive() {
        World world = populatedWorld();
        TestRunner.eq(1, world.query(Query.exclusive(A.class, B.class)).size(), "exactly A+B");
        TestRunner.eq(1, world.query(Query.exclusive()).size(), "no components at all");
        TestRunner.eq(1, world.query(Query.exclusive(A.class)).size(), "entity with only A matches exact A");
        TestRunner.eq(0, world.query(Query.exclusive(A.class, C.class)).size(), "no entity has exactly A+C");
    }

    public static void testCacheInvalidationAndSnapshot() {
        World world = new World();
        Query query = Query.all(A.class);
        TestRunner.eq(0, world.query(query).size(), "empty initially");

        Entity first = world.create();
        world.add(first, new A());
        QueryResult snapshot = world.query(query);
        TestRunner.eq(1, snapshot.size(), "recomputed after add");

        world.add(first, new B());
        TestRunner.eq(1, world.query(query).size(), "still matches after extra component");
        TestRunner.eq(1, snapshot.size(), "old snapshot still readable");

        world.remove(first, A.class);
        TestRunner.eq(0, world.query(query).size(), "recomputed after remove");

        Entity second = world.create();
        world.add(second, new A());
        TestRunner.eq(1, world.query(query).size(), "recomputed after new entity");
    }

    public static void testIteration() {
        World world = populatedWorld();
        QueryResult result = world.query(Query.all(A.class));
        Set<Entity> seen = new HashSet<>();
        result.forEachEntity(seen::add);
        TestRunner.eq(3, seen.size(), "forEach visits all matches");
        int iterated = 0;
        for (Entity entity : result) {
            TestRunner.check(world.isAlive(entity), "iterated entity alive");
            iterated++;
        }
        TestRunner.eq(3, iterated, "iterator count");
        TestRunner.check(result.contains(world.entityAt(0)), "contains works");
    }

    public static void testTypedViews() {
        World world = populatedWorld();
        Query2<A, B> pair = world.query2(A.class, B.class);
        TestRunner.eq(2, pair.size(), "typed query size");
        int[] count = {0};
        pair.forEach((entity, a, b) -> {
            TestRunner.check(a != null && b != null, "typed components non-null");
            count[0]++;
        });
        TestRunner.eq(2, count[0], "typed iteration count");

        Query1<A> single = world.query1(Query.all(A.class).none(C.class), A.class);
        TestRunner.eq(2, single.size(), "typed query with filter");
    }
}
