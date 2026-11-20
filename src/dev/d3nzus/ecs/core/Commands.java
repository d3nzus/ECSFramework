package dev.d3nzus.ecs.core;

import java.util.ArrayDeque;

/**
 * Queue of structural changes. Systems should use this instead of mutating the
 * world directly when a query iteration is in progress: commands are applied at
 * phase boundaries, so snapshots stay stable for the whole phase.
 */
public final class Commands {
    private final World world;
    private final ArrayDeque<Command> queue = new ArrayDeque<>();

    Commands(World world) {
        this.world = world;
    }

    /** Entity creation is immediate so the returned handle can be used right away. */
    public Entity create() {
        return world.create();
    }

    public void destroy(Entity entity) {
        queue.add(new Command.Destroy(entity));
    }

    @SuppressWarnings("unchecked")
    public <T> void add(Entity entity, T component) {
        add(entity, (Class<T>) component.getClass(), component);
    }

    public <T> void add(Entity entity, Class<T> type, T component) {
        queue.add(new Command.Add(entity, world.registry().register(type), component));
    }

    public void remove(Entity entity, Class<?> type) {
        queue.add(new Command.Remove(entity, world.registry().id(type)));
    }

    public int pending() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }

    void flush() {
        if (queue.isEmpty()) {
            return;
        }
        Command[] batch = queue.toArray(Command[]::new);
        queue.clear();
        for (Command command : batch) {
            world.apply(command);
        }
    }
}
