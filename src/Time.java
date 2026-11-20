package dev.d3nzus.ecs.util;

/** World-wide time state, normally stored as a resource. */
public final class Time {
    public float delta;
    public float elapsed;
    public float timeScale = 1f;
    public long frame;

    public void advance(float rawDelta) {
        delta = rawDelta * timeScale;
        elapsed += delta;
        frame++;
    }

    public void reset() {
        delta = 0f;
        elapsed = 0f;
        timeScale = 1f;
        frame = 0;
    }
}
