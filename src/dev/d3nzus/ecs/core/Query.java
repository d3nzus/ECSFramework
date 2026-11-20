package dev.d3nzus.ecs.core;

import java.util.Arrays;

/**
 * Immutable description of which entities a system is interested in.
 *
 * <p>{@code all} types are mandatory, {@code any} requires at least one match,
 * {@code none} excludes matches, and {@code exclusive} requires the entity to
 * have exactly the listed component types and no others.</p>
 */
public final class Query {
    private static final Class<?>[] EMPTY = new Class<?>[0];

    private final Class<?>[] all;
    private final Class<?>[] any;
    private final Class<?>[] none;
    private final Class<?>[] exactly;
    private final boolean exclusive;
    private final int hash;

    private Query(Class<?>[] all, Class<?>[] any, Class<?>[] none, Class<?>[] exactly, boolean exclusive) {
        this.all = all;
        this.any = any;
        this.none = none;
        this.exactly = exactly;
        this.exclusive = exclusive;
        this.hash = 31 * (31 * (31 * Arrays.hashCode(all) + Arrays.hashCode(any))
                + Arrays.hashCode(none)) + Arrays.hashCode(exactly) + (exclusive ? 1 : 0);
    }

    public static Query all(Class<?>... types) {
        return new Query(types.clone(), EMPTY, EMPTY, EMPTY, false);
    }

    /** Entities matching at least one of the given types. */
    public Query any(Class<?>... types) {
        return new Query(all, types.clone(), none, exactly, exclusive);
    }

    /** Excludes entities having any of the given types. */
    public Query none(Class<?>... types) {
        return new Query(all, any, types.clone(), exactly, exclusive);
    }

    /** Requires exactly this component set (no additional components). */
    public static Query exclusive(Class<?>... types) {
        return new Query(EMPTY, EMPTY, EMPTY, types.clone(), true);
    }

    /** Combines this query with an exact component-set requirement. */
    public Query andExclusive(Class<?>... types) {
        return new Query(all, any, none, types.clone(), true);
    }

    public Class<?>[] allTypes() {
        return all.clone();
    }

    public Class<?>[] anyTypes() {
        return any.clone();
    }

    public Class<?>[] noneTypes() {
        return none.clone();
    }

    public Class<?>[] exclusiveTypes() {
        return exactly.clone();
    }

    public boolean isExclusive() {
        return exclusive;
    }

    Class<?>[] allArray() {
        return all;
    }

    Class<?>[] anyArray() {
        return any;
    }

    Class<?>[] noneArray() {
        return none;
    }

    Class<?>[] exclusiveArray() {
        return exactly;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Query query)) {
            return false;
        }
        return exclusive == query.exclusive
                && Arrays.equals(all, query.all)
                && Arrays.equals(any, query.any)
                && Arrays.equals(none, query.none)
                && Arrays.equals(exactly, query.exactly);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "Query[all=" + Arrays.toString(all)
                + ", any=" + Arrays.toString(any)
                + ", none=" + Arrays.toString(none)
                + (exclusive ? ", exactly=" + Arrays.toString(exactly) : "") + "]";
    }
}
