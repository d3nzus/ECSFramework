package dev.d3nzus.ecs.examples;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.Phase;
import dev.d3nzus.ecs.core.Query2;
import dev.d3nzus.ecs.core.World;

/** Tiny dependency-free throughput check: {@code Benchmark [entities] [frames]}. */
public final class Benchmark {

    public static final class Position {
        public float x;
        public float y;
    }

    public static final class Velocity {
        public float x = 1f;
        public float y = 1f;
    }

    public static void main(String[] args) {
        int entityCount = args.length > 0 ? Integer.parseInt(args[0]) : 100_000;
        int frames = args.length > 1 ? Integer.parseInt(args[1]) : 60;

        World world = new World();
        Query2<Position, Velocity> movers = world.query2(Position.class, Velocity.class);
        world.systems().add(new dev.d3nzus.ecs.core.System() {
            @Override
            public void update(World w, float delta) {
                movers.forEach((entity, position, velocity) -> {
                    position.x += velocity.x * delta;
                    position.y += velocity.y * delta;
                });
            }
        }, Phase.UPDATE);

        for (int i = 0; i < entityCount; i++) {
            Entity entity = world.create();
            world.add(entity, new Position());
            world.add(entity, new Velocity());
        }

        for (int i = 0; i < 10; i++) {
            world.update(1f / 60f);
        }

        long start = java.lang.System.nanoTime();
        for (int i = 0; i < frames; i++) {
            world.update(1f / 60f);
        }
        double millis = (java.lang.System.nanoTime() - start) / 1_000_000.0;

        java.lang.System.out.printf("entities=%d frames=%d total=%.2f ms avg=%.3f ms/frame throughput=%.1fM entity-updates/s%n",
                entityCount, frames, millis, millis / frames,
                (double) entityCount * frames / millis / 1000.0);
    }
}
