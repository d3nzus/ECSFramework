package dev.d3nzus.ecs.core;

/** Callback interfaces used by query iteration. */
public final class Queries {
    private Queries() {
    }

    @FunctionalInterface
    public interface ForEach {
        void accept(Entity entity);
    }

    @FunctionalInterface
    public interface ForEach1<A> {
        void accept(Entity entity, A a);
    }

    @FunctionalInterface
    public interface ForEach2<A, B> {
        void accept(Entity entity, A a, B b);
    }

    @FunctionalInterface
    public interface ForEach3<A, B, C> {
        void accept(Entity entity, A a, B b, C c);
    }

    @FunctionalInterface
    public interface ForEach4<A, B, C, D> {
        void accept(Entity entity, A a, B b, C c, D d);
    }
}
