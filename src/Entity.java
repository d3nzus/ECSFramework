package dev.d3nzus.ecs.core;

/**
 * Immutable entity handle. The entity index and a generation counter are packed
 * into a single {@code int}, so a handle from a destroyed entity can be detected
 * instead of silently referring to a recycled entity.
 *
 * <p>Layout: 20 bits for the index, 12 bits for the generation. The highest
 * index and generation values are reserved so that {@link #INVALID} is always
 * distinguishable from a real entity.</p>
 */
public record Entity(int id) {

    public static final int INDEX_BITS = 20;
    public static final int GENERATION_BITS = 12;

    public static final int INDEX_MASK = (1 << INDEX_BITS) - 1;
    public static final int GENERATION_MASK = (1 << GENERATION_BITS) - 1;

    /** Highest index that will ever be allocated. */
    public static final int MAX_INDEX = INDEX_MASK - 1;
    /** Highest generation that will ever be used for an index. */
    public static final int MAX_GENERATION = GENERATION_MASK - 1;

    /** Reserved sentinel for "no entity". Never returned by {@link World#create()}. */
    public static final Entity INVALID = new Entity(-1);

    public static Entity of(int index, int generation) {
        if (index < 0 || index > MAX_INDEX) {
            throw new IllegalArgumentException("entity index out of range: " + index);
        }
        if (generation < 0 || generation > GENERATION_MASK) {
            throw new IllegalArgumentException("entity generation out of range: " + generation);
        }
        return new Entity((generation << INDEX_BITS) | index);
    }

    public int index() {
        return id & INDEX_MASK;
    }

    public int generation() {
        return (id >>> INDEX_BITS) & GENERATION_MASK;
    }

    @Override
    public String toString() {
        return "Entity[" + index() + ":" + generation() + "]";
    }
}
