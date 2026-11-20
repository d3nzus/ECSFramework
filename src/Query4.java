package dev.d3nzus.ecs.core;

/** Typed query view exposing four components. */
public final class Query4<A, B, C, D> {
    private final World world;
    private final Query query;
    private final int idA;
    private final int idB;
    private final int idC;
    private final int idD;

    Query4(World world, Query query, Class<A> a, Class<B> b, Class<C> c, Class<D> d) {
        this.world = world;
        this.query = query;
        this.idA = world.registry().register(a);
        this.idB = world.registry().register(b);
        this.idC = world.registry().register(c);
        this.idD = world.registry().register(d);
    }

    public int size() {
        return world.query(query).size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void forEach(Queries.ForEach4<A, B, C, D> action) {
        QueryResult result = world.query(query);
        for (int i = 0; i < result.size(); i++) {
            int entityIndex = result.entityIndexAt(i);
            action.accept(world.entityAt(entityIndex),
                    world.componentAt(entityIndex, idA),
                    world.componentAt(entityIndex, idB),
                    world.componentAt(entityIndex, idC),
                    world.componentAt(entityIndex, idD));
        }
    }
}
