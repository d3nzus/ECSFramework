package dev.d3nzus.ecs.core;

/** Typed query view exposing two components. */
public final class Query2<A, B> {
    private final World world;
    private final Query query;
    private final int idA;
    private final int idB;

    Query2(World world, Query query, Class<A> a, Class<B> b) {
        this.world = world;
        this.query = query;
        this.idA = world.registry().register(a);
        this.idB = world.registry().register(b);
    }

    public int size() {
        return world.query(query).size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void forEach(Queries.ForEach2<A, B> action) {
        QueryResult result = world.query(query);
        for (int i = 0; i < result.size(); i++) {
            int entityIndex = result.entityIndexAt(i);
            action.accept(world.entityAt(entityIndex),
                    world.componentAt(entityIndex, idA),
                    world.componentAt(entityIndex, idB));
        }
    }
}
