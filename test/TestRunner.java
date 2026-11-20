package dev.d3nzus.ecs.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/** Dependency-free test runner: discovers public static {@code test*} methods. */
public final class TestRunner {
    private static final String[] TEST_CLASSES = {
            "dev.d3nzus.ecs.tests.EntityTest",
            "dev.d3nzus.ecs.tests.SparsePoolTest",
            "dev.d3nzus.ecs.tests.WorldTest",
            "dev.d3nzus.ecs.tests.QueryTest",
            "dev.d3nzus.ecs.tests.CommandTest",
            "dev.d3nzus.ecs.tests.SystemTest",
            "dev.d3nzus.ecs.tests.ResourceTest",
            "dev.d3nzus.ecs.tests.EventBusTest",
            "dev.d3nzus.ecs.tests.UtilTest"
    };

    private static int passed;
    private static int failed;

    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        for (String className : TEST_CLASSES) {
            Class<?> testClass = Class.forName(className);
            for (Method method : testClass.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (!Modifier.isStatic(modifiers) || !method.getName().startsWith("test")
                        || method.getParameterCount() != 0) {
                    continue;
                }
                String label = testClass.getSimpleName() + "." + method.getName();
                try {
                    method.invoke(null);
                    passed++;
                    java.lang.System.out.println("PASS " + label);
                } catch (InvocationTargetException error) {
                    failed++;
                    Throwable cause = error.getCause();
                    java.lang.System.out.println("FAIL " + label + ": " + cause);
                    cause.printStackTrace(java.lang.System.out);
                }
            }
        }
        java.lang.System.out.println();
        java.lang.System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            java.lang.System.exit(1);
        }
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void eq(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void eq(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }

    public static void eq(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }

    public static void eq(float expected, float actual, String message) {
        if (Math.abs(expected - actual) > 1e-4f) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }
}
