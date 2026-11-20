package dev.d3nzus.ecs.util;

/** Small collection of scalar math helpers. */
public final class MathUtils {
    private MathUtils() {
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp((x - edge0) / (edge1 - edge0), 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    public static boolean approx(float a, float b, float epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    public static float toRadians(float degrees) {
        return degrees * (float) Math.PI / 180f;
    }

    public static float toDegrees(float radians) {
        return radians * 180f / (float) Math.PI;
    }

    /** Wraps a value into {@code [min, max)}. */
    public static float wrap(float value, float min, float max) {
        float range = max - min;
        if (range <= 0f) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        return min + (((value - min) % range + range) % range);
    }
}
