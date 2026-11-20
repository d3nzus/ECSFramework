package dev.d3nzus.ecs.util;

/** Mutable 2D vector with chainable operations. */
public final class Vec2 {
    public float x;
    public float y;

    public Vec2() {
    }

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(Vec2 other) {
        this(other.x, other.y);
    }

    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vec2 set(Vec2 other) {
        return set(other.x, other.y);
    }

    public Vec2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vec2 add(Vec2 other) {
        return add(other.x, other.y);
    }

    public Vec2 sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Vec2 sub(Vec2 other) {
        return sub(other.x, other.y);
    }

    public Vec2 scale(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vec2 negate() {
        return scale(-1f);
    }

    public Vec2 normalize() {
        float length = length();
        if (length > 0f) {
            this.x /= length;
            this.y /= length;
        }
        return this;
    }

    public Vec2 lerp(Vec2 other, float t) {
        this.x = MathUtils.lerp(this.x, other.x, t);
        this.y = MathUtils.lerp(this.y, other.y, t);
        return this;
    }

    public Vec2 rotate(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float nx = x * cos - y * sin;
        float ny = x * sin + y * cos;
        this.x = nx;
        this.y = ny;
        return this;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public float lengthSquared() {
        return x * x + y * y;
    }

    public float distanceTo(Vec2 other) {
        float dx = other.x - x;
        float dy = other.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float dot(Vec2 other) {
        return x * other.x + y * other.y;
    }

    /** 2D cross product (signed area). */
    public float cross(Vec2 other) {
        return x * other.y - y * other.x;
    }

    public float angle() {
        return (float) Math.atan2(y, x);
    }

    public Vec2 copy() {
        return new Vec2(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vec2 vector)) {
            return false;
        }
        return Float.floatToIntBits(x) == Float.floatToIntBits(vector.x)
                && Float.floatToIntBits(y) == Float.floatToIntBits(vector.y);
    }

    @Override
    public int hashCode() {
        return 31 * Float.floatToIntBits(x) + Float.floatToIntBits(y);
    }

    @Override
    public String toString() {
        return "Vec2[" + x + ", " + y + "]";
    }
}
