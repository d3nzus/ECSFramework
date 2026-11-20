package dev.d3nzus.ecs.util;

/** Mutable 3D vector with chainable operations. */
public final class Vec3 {
    public float x;
    public float y;
    public float z;

    public Vec3() {
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(Vec3 other) {
        this(other.x, other.y, other.z);
    }

    public Vec3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3 set(Vec3 other) {
        return set(other.x, other.y, other.z);
    }

    public Vec3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3 add(Vec3 other) {
        return add(other.x, other.y, other.z);
    }

    public Vec3 sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3 sub(Vec3 other) {
        return sub(other.x, other.y, other.z);
    }

    public Vec3 scale(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }

    public Vec3 negate() {
        return scale(-1f);
    }

    public Vec3 normalize() {
        float length = length();
        if (length > 0f) {
            this.x /= length;
            this.y /= length;
            this.z /= length;
        }
        return this;
    }

    public Vec3 lerp(Vec3 other, float t) {
        this.x = MathUtils.lerp(this.x, other.x, t);
        this.y = MathUtils.lerp(this.y, other.y, t);
        this.z = MathUtils.lerp(this.z, other.z, t);
        return this;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public float distanceTo(Vec3 other) {
        float dx = other.x - x;
        float dy = other.y - y;
        float dz = other.z - z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float dot(Vec3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /** Cross product; returns a new vector to avoid aliasing. */
    public Vec3 cross(Vec3 other) {
        return new Vec3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x);
    }

    public Vec3 copy() {
        return new Vec3(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vec3 vector)) {
            return false;
        }
        return Float.floatToIntBits(x) == Float.floatToIntBits(vector.x)
                && Float.floatToIntBits(y) == Float.floatToIntBits(vector.y)
                && Float.floatToIntBits(z) == Float.floatToIntBits(vector.z);
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        return 31 * result + Float.floatToIntBits(z);
    }

    @Override
    public String toString() {
        return "Vec3[" + x + ", " + y + ", " + z + "]";
    }
}
