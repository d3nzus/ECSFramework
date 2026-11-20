package dev.d3nzus.ecs.core;

/** Deferred structural mutations, applied when {@link Commands#flush()} runs. */
sealed interface Command permits Command.Destroy, Command.Add, Command.Remove {

    record Destroy(Entity entity) implements Command {
    }

    record Add(Entity entity, int typeId, Object component) implements Command {
    }

    record Remove(Entity entity, int typeId) implements Command {
    }
}
