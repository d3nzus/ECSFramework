package dev.d3nzus.ecs.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Registers systems and runs them in phase/priority order. */
public final class SystemManager {
    private static final Comparator<Entry> ORDER = Comparator
            .comparingInt((Entry entry) -> entry.phase.ordinal())
            .thenComparingInt(entry -> entry.priority)
            .thenComparingInt(entry -> entry.sequence);

    private final World world;
    private final List<Entry> entries = new ArrayList<>();
    private Entry[] sorted = new Entry[0];
    private boolean initialized;

    SystemManager(World world) {
        this.world = world;
    }

    public void add(System system) {
        add(system, Phase.UPDATE, 0);
    }

    public void add(System system, Phase phase) {
        add(system, phase, 0);
    }

    public void add(System system, Phase phase, int priority) {
        if (system == null || phase == null) {
            throw new IllegalArgumentException("system and phase are required");
        }
        Entry entry = new Entry(system, phase, priority, entries.size());
        entries.add(entry);
        rebuild();
        if (initialized) {
            system.init(world);
        }
    }

    public boolean remove(System system) {
        boolean removed = entries.removeIf(entry -> entry.system == system);
        if (removed) {
            rebuild();
            system.dispose();
        }
        return removed;
    }

    public int count() {
        return entries.size();
    }

    public void init() {
        for (Entry entry : entries) {
            entry.system.init(world);
        }
        initialized = true;
    }

    public void dispose() {
        for (Entry entry : entries) {
            entry.system.dispose();
        }
        initialized = false;
    }

    void update(Phase phase, float delta) {
        for (Entry entry : sorted) {
            if (entry.phase == phase) {
                entry.system.update(world, delta);
            }
        }
    }

    private void rebuild() {
        sorted = entries.stream().sorted(ORDER).toArray(Entry[]::new);
    }

    private static final class Entry {
        private final System system;
        private final Phase phase;
        private final int priority;
        private final int sequence;

        private Entry(System system, Phase phase, int priority, int sequence) {
            this.system = system;
            this.phase = phase;
            this.priority = priority;
            this.sequence = sequence;
        }
    }
}
