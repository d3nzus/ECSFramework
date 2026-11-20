package dev.d3nzus.ecs.util;

import java.util.function.IntConsumer;

/** Fixed-timestep helper with an interpolation alpha. */
public final class FixedStepAccumulator {
    private final float step;
    private float accumulator;

    public FixedStepAccumulator(float step) {
        if (step <= 0f) {
            throw new IllegalArgumentException("step must be positive");
        }
        this.step = step;
    }

    /** Consumes elapsed time and runs {@code tick} once per completed step. */
    public int update(float delta, IntConsumer tick) {
        accumulator += delta;
        int steps = 0;
        while (accumulator >= step) {
            accumulator -= step;
            tick.accept(steps);
            steps++;
            if (steps >= 1000) {
                // Spiral-of-death guard.
                accumulator = 0f;
                break;
            }
        }
        return steps;
    }

    /** Fraction of a step still pending, for render interpolation. */
    public float alpha() {
        return accumulator / step;
    }

    public float step() {
        return step;
    }

    public void reset() {
        accumulator = 0f;
    }
}
