package dev.d3nzus.ecs.util;

/** Local and resolved world-space 2D transform component. */
public final class Transform2D {
    public float x;
    public float y;
    public float rotation;
    public float scaleX = 1f;
    public float scaleY = 1f;

    public float worldX;
    public float worldY;
    public float worldRotation;
    public float worldScaleX = 1f;
    public float worldScaleY = 1f;

    public boolean dirty = true;

    public Transform2D() {
    }

    public Transform2D(float x, float y) {
        this.x = x;
        this.y = y;
        this.worldX = x;
        this.worldY = y;
    }

    public Transform2D setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        dirty = true;
        return this;
    }

    public Transform2D setRotation(float radians) {
        this.rotation = radians;
        dirty = true;
        return this;
    }

    public Transform2D setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        dirty = true;
        return this;
    }
}
