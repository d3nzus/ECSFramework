package dev.d3nzus.ecs.util;

import dev.d3nzus.ecs.core.System;
import dev.d3nzus.ecs.core.World;

/** Creates/advances the {@link Time} resource each frame. Register in INPUT phase. */
public final class TimeSystem implements System {
    @Override
    public void update(World world, float delta) {
        Time time = world.resources().getOrCreate(Time.class, Time::new);
        time.advance(delta);
    }
}
