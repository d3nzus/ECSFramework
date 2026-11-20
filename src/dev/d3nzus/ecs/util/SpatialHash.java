package dev.d3nzus.ecs.util;

import dev.d3nzus.ecs.core.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Uniform-grid broad phase for 2D positions. Rebuilding it each frame is cheap;
 * {@link #insert(Entity, float, float)} replaces any previous cell for an entity.
 */
public final class SpatialHash {
    private final float cellSize;
    private final Map<Long, List<Entity>> cells = new HashMap<>();
    private final Map<Entity, Long> occupied = new HashMap<>();

    public SpatialHash(float cellSize) {
        if (cellSize <= 0f) {
            throw new IllegalArgumentException("cell size must be positive");
        }
        this.cellSize = cellSize;
    }

    public void clear() {
        cells.clear();
        occupied.clear();
    }

    public void insert(Entity entity, float x, float y) {
        remove(entity);
        long key = key(cellX(x), cellY(y));
        cells.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entity);
        occupied.put(entity, key);
    }

    public void remove(Entity entity) {
        Long key = occupied.remove(entity);
        if (key == null) {
            return;
        }
        List<Entity> bucket = cells.get(key);
        if (bucket != null) {
            bucket.remove(entity);
            if (bucket.isEmpty()) {
                cells.remove(key);
            }
        }
    }

    public void query(float minX, float minY, float maxX, float maxY, Consumer<Entity> consumer) {
        int x0 = cellX(minX);
        int x1 = cellX(maxX);
        int y0 = cellY(minY);
        int y1 = cellY(maxY);
        Set<Entity> seen = new HashSet<>();
        for (int cx = x0; cx <= x1; cx++) {
            for (int cy = y0; cy <= y1; cy++) {
                List<Entity> bucket = cells.get(key(cx, cy));
                if (bucket == null) {
                    continue;
                }
                for (Entity entity : bucket) {
                    if (seen.add(entity)) {
                        consumer.accept(entity);
                    }
                }
            }
        }
    }

    public List<Entity> query(float minX, float minY, float maxX, float maxY) {
        List<Entity> result = new ArrayList<>();
        query(minX, minY, maxX, maxY, result::add);
        return result;
    }

    public void queryRadius(float x, float y, float radius, Consumer<Entity> consumer) {
        query(x - radius, y - radius, x + radius, y + radius, consumer);
    }

    public List<Entity> queryRadius(float x, float y, float radius) {
        List<Entity> result = new ArrayList<>();
        queryRadius(x, y, radius, result::add);
        return result;
    }

    public int cellCount() {
        return cells.size();
    }

    public int entityCount() {
        return occupied.size();
    }

    private int cellX(float x) {
        return (int) Math.floor(x / cellSize);
    }

    private int cellY(float y) {
        return (int) Math.floor(y / cellSize);
    }

    private static long key(int cx, int cy) {
        return (((long) cx) << 32) ^ (cy & 0xFFFFFFFFL);
    }
}
