package dev.d3nzus.ecs.util;

import dev.d3nzus.ecs.core.Entity;
import dev.d3nzus.ecs.core.Query;
import dev.d3nzus.ecs.core.QueryResult;
import dev.d3nzus.ecs.core.System;
import dev.d3nzus.ecs.core.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Propagates local {@link Transform2D} values into world space, parents before
 * children. Cycles are capped: an entity more than 64 parents deep is treated
 * as a root. Register after any system that moves local transforms.
 */
public final class TransformHierarchySystem implements System {
    private static final int MAX_DEPTH = 64;

    private final Query query = Query.all(Transform2D.class);
    private final List<Node> ordered = new ArrayList<>();

    @Override
    public void update(World world, float delta) {
        QueryResult result = world.query(query);
        ordered.clear();
        for (int i = 0; i < result.size(); i++) {
            Entity entity = result.get(i);
            ordered.add(new Node(entity, depthOf(world, entity)));
        }
        ordered.sort(Comparator.comparingInt(node -> node.depth));

        for (Node node : ordered) {
            Entity entity = node.entity;
            Transform2D transform = world.get(entity, Transform2D.class);
            if (transform == null) {
                continue;
            }
            Parent parentComponent = world.get(entity, Parent.class);
            Transform2D parent = resolveParent(world, parentComponent, entity);

            if (parent == null) {
                transform.worldX = transform.x;
                transform.worldY = transform.y;
                transform.worldRotation = transform.rotation;
                transform.worldScaleX = transform.scaleX;
                transform.worldScaleY = transform.scaleY;
            } else {
                float cos = (float) Math.cos(parent.worldRotation);
                float sin = (float) Math.sin(parent.worldRotation);
                float offsetX = transform.x * parent.worldScaleX;
                float offsetY = transform.y * parent.worldScaleY;
                transform.worldX = parent.worldX + offsetX * cos - offsetY * sin;
                transform.worldY = parent.worldY + offsetX * sin + offsetY * cos;
                transform.worldRotation = parent.worldRotation + transform.rotation;
                transform.worldScaleX = parent.worldScaleX * transform.scaleX;
                transform.worldScaleY = parent.worldScaleY * transform.scaleY;
            }
            transform.dirty = false;
        }
    }

    private Transform2D resolveParent(World world, Parent parentComponent, Entity child) {
        if (parentComponent == null) {
            return null;
        }
        Entity parentEntity = parentComponent.value;
        if (!world.isAlive(parentEntity) || parentEntity.equals(child)) {
            parentComponent.value = Entity.INVALID;
            return null;
        }
        return world.get(parentEntity, Transform2D.class);
    }

    private int depthOf(World world, Entity entity) {
        int depth = 0;
        Entity current = entity;
        for (int hop = 0; hop < MAX_DEPTH; hop++) {
            Parent parent = world.get(current, Parent.class);
            if (parent == null || !world.isAlive(parent.value) || parent.value.equals(current)) {
                break;
            }
            current = parent.value;
            depth++;
        }
        return depth;
    }

    private record Node(Entity entity, int depth) {
    }
}
