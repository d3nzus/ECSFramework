package dev.d3nzus.ecs.core;

/** Typed query view exposing one component. */
public final class Query1<A> {
    private final World world;
    private final Query query;
    private final int idA;

    Query1(World world, Query query, Class<A> a) {
        this.world = world;
        this.query = query;
        this.idA = world.registry().register(a);
    }

    public int size() {
        return world.query(query).size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void forEach(Queries.ForEach1<A> action) {
        QueryResult result = world.query(query);
        for (int i = 0; i < result.size(); i++) {
            int entityIndex = result.entityIndexAt(i);
            action.accept(world.entityAt(entityIndex), world.componentAt(entityIndex, idA));
        }
    }
}
