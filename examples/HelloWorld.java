package dev.d3nzus.ecs.examples;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.Phase;
import dev.d3nzus.ecs.core.Query;
import dev.d3nzus.ecs.core.Query2;
import dev.d3nzus.ecs.core.System;
import dev.d3nzus.ecs.core.World;

/** Minimal movement simulation that prints positions for a few frames. */
public final class HelloWorld {

    public static final class Position {
        public float x;
        public float y;

        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static final class Velocity {
        public float x;
        public float y;

        public Velocity(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static final class Frozen {
    }

    public static final class MovementSystem implements System {
        private final Query2<Position, Velocity> movers;

        public MovementSystem(World world) {
            this.movers = world.query2(
                    Query.all(Position.class, Velocity.class).none(Frozen.class),
                    Position.class, Velocity.class);
        }

        @Override
        public void update(World world, float delta) {
            movers.forEach((entity, position, velocity) -> {
                position.x += velocity.x * delta;
                position.y += velocity.y * delta;
            });
        }
    }

    public static final class ReportSystem implements System {
        private final Query2<Position, Velocity> movers;

        public ReportSystem(World world) {
            this.movers = world.query2(Position.class, Velocity.class);
        }

        @Override
        public void update(World world, float delta) {
            java.lang.System.out.print("frame " + world.resources().getOrCreate(Frame.class, Frame::new).value + ": ");
            movers.forEach((entity, position, velocity) ->
                    java.lang.System.out.print(entity + "=(" + position.x + ", " + position.y + ") "));
            java.lang.System.out.println();
            world.resources().getOrCreate(Frame.class, Frame::new).value++;
        }
    }

    public static final class Frame {
        public int value;
    }

    public static void main(String[] args) {
        World world = new World();
        world.systems().add(new MovementSystem(world), Phase.UPDATE, 0);
        world.systems().add(new ReportSystem(world), Phase.RENDER, 0);

        Entity ship = world.create();
        world.add(ship, new Position(0f, 0f));
        world.add(ship, new Velocity(10f, 2f));

        Entity rock = world.create();
        world.add(rock, new Position(50f, 0f));
        world.add(rock, new Velocity(0f, 0f));
        world.add(rock, new Frozen());

        for (int i = 0; i < 4; i++) {
            world.update(0.25f);
        }

        world.destroy(ship);
        world.update(0f);
        java.lang.System.out.println("ship alive after deferred destroy: " + world.isAlive(ship));
    }
}
