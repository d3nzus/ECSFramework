package dev.d3nzus.ecs.examples;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.World;
import dev.d3nzus.ecs.util.SpatialHash;

/** Demonstrates the standalone broad-phase grid. */
public final class SpatialHashDemo {

    public static void main(String[] args) {
        World world = new World();
        SpatialHash grid = new SpatialHash(10f);

        float[][] points = {{0, 0}, {5, 5}, {12, 1}, {40, 40}, {-8, 2}};
        for (float[] point : points) {
            Entity entity = world.create();
            grid.insert(entity, point[0], point[1]);
        }

        java.lang.System.out.println("entities=" + grid.entityCount() + ", occupied cells=" + grid.cellCount());
        for (Entity entity : grid.queryRadius(0f, 0f, 15f)) {
            java.lang.System.out.println("near origin: " + entity);
        }

        grid.remove(world.entityAt(0));
        java.lang.System.out.println("after removal: " + grid.entityCount());
    }
}
