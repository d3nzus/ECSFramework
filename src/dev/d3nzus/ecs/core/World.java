package dev.d3nzus.ecs.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The central ECS container. Owns entity identities, component pools, queries,
 * systems, resources and events.
 *
 * <p>All operations are single-threaded. Entity creation and component
 * add/remove are immediate; destruction is deferred through {@link #commands()}
 * so that query snapshots remain valid while a system is iterating them.</p>
 */
public final class World {
    private static final int INITIAL_CAPACITY = 256;
    private static final int[] EMPTY_INDICES = new int[0];

    private final ComponentRegistry registry = new ComponentRegistry();
    private final SystemManager systems = new SystemManager(this);
    private final Resources resources = new Resources();
    private final EventBus events = new EventBus();
    private final Commands commands = new Commands(this);
    private final Map<Query, QueryResult> queryCache = new HashMap<>();

    private AliveSet alive = new AliveSet();
    private IntArrayList freeIndices = new IntArrayList();
    private int[] generations = new int[INITIAL_CAPACITY];
    private int nextIndex;
    private int entityCount;
    private long structuralVersion;
    private boolean systemsInitialized;

    public World() {
    }

    // ------------------------------------------------------------------
    // Entity lifecycle
    // ------------------------------------------------------------------

    public Entity create() {
        int index;
        if (!freeIndices.isEmpty()) {
            index = freeIndices.removeLast();
        } else {
            index = nextIndex++;
            ensureGenerations(index);
            generations[index] = 0;
        }
        alive.add(index);
        entityCount++;
        structuralVersion++;
        return Entity.of(index, generations[index]);
    }

    /** Queues destruction; the entity dies at the next command flush. */
    public void destroy(Entity entity) {
        commands.destroy(entity);
    }

    public boolean isAlive(Entity entity) {
        if (entity == null) {
            return false;
        }
        int index = entity.index();
        return index >= 0 && index < generations.length
                && generations[index] == entity.generation()
                && alive.has(index);
    }

    /** Builds the current handle for a raw entity index. */
    public Entity entityAt(int entityIndex) {
        return Entity.of(entityIndex, generations[entityIndex]);
    }

    public int entityCount() {
        return entityCount;
    }

    public int componentCount(Class<?> type) {
        SparsePool<?> pool = registry.pool(type);
        return pool == null ? 0 : pool.size();
    }

    // ------------------------------------------------------------------
    // Components
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public <T> void add(Entity entity, T component) {
        add(entity, (Class<T>) component.getClass(), component);
    }

    public <T> void add(Entity entity, Class<T> type, T component) {
        requireAlive(entity);
        int typeId = registry.register(type);
        SparsePool<T> pool = poolFor(typeId);
        boolean wasPresent = pool.has(entity.index());
        pool.add(entity.index(), component);
        if (!wasPresent) {
            structuralVersion++;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Entity entity, Class<T> type) {
        if (!isAlive(entity)) {
            return null;
        }
        int typeId = registry.id(type);
        return typeId < 0 ? null : ((SparsePool<T>) registry.pool(typeId)).get(entity.index());
    }

    public boolean has(Entity entity, Class<?> type) {
        if (!isAlive(entity)) {
            return false;
        }
        int typeId = registry.id(type);
        return typeId >= 0 && registry.pool(typeId).has(entity.index());
    }

    @SuppressWarnings("unchecked")
    public <T> T remove(Entity entity, Class<T> type) {
        if (!isAlive(entity)) {
            return null;
        }
        int typeId = registry.id(type);
        if (typeId < 0) {
            return null;
        }
        T removed = ((SparsePool<T>) registry.pool(typeId)).remove(entity.index());
        if (removed != null) {
            structuralVersion++;
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    <T> T componentAt(int entityIndex, int typeId) {
        return typeId < 0 ? null : (T) registry.pool(typeId).get(entityIndex);
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    public QueryResult query(Query definition) {
        QueryResult cached = queryCache.get(definition);
        if (cached != null && cached.version() == structuralVersion) {
            return cached;
        }
        QueryResult fresh = computeQuery(definition);
        queryCache.put(definition, fresh);
        return fresh;
    }

    public <A> Query1<A> query1(Class<A> a) {
        return new Query1<>(this, Query.all(a), a);
    }

    public <A> Query1<A> query1(Query definition, Class<A> a) {
        return new Query1<>(this, definition, a);
    }

    public <A, B> Query2<A, B> query2(Class<A> a, Class<B> b) {
        return new Query2<>(this, Query.all(a, b), a, b);
    }

    public <A, B> Query2<A, B> query2(Query definition, Class<A> a, Class<B> b) {
        return new Query2<>(this, definition, a, b);
    }

    public <A, B, C> Query3<A, B, C> query3(Class<A> a, Class<B> b, Class<C> c) {
        return new Query3<>(this, Query.all(a, b, c), a, b, c);
    }

    public <A, B, C> Query3<A, B, C> query3(Query definition, Class<A> a, Class<B> b, Class<C> c) {
        return new Query3<>(this, definition, a, b, c);
    }

    public <A, B, C, D> Query4<A, B, C, D> query4(Class<A> a, Class<B> b, Class<C> c, Class<D> d) {
        return new Query4<>(this, Query.all(a, b, c, d), a, b, c, d);
    }

    public <A, B, C, D> Query4<A, B, C, D> query4(Query definition, Class<A> a, Class<B> b,
                                                   Class<C> c, Class<D> d) {
        return new Query4<>(this, definition, a, b, c, d);
    }

    private QueryResult computeQuery(Query definition) {
        int[] allIds = idsFor(definition.allArray());
        for (int id : allIds) {
            if (id < 0) {
                return emptyResult();
            }
        }

        Class<?>[] anyTypes = definition.anyArray();
        int[] anyIds = registeredIds(anyTypes);
        if (anyTypes.length > 0 && anyIds.length == 0) {
            return emptyResult();
        }

        int[] noneIds = registeredIds(definition.noneArray());
        int[] exactIds = null;
        if (definition.isExclusive()) {
            Class<?>[] exactTypes = definition.exclusiveArray();
            exactIds = idsFor(exactTypes);
            for (int id : exactIds) {
                if (id < 0) {
                    return emptyResult();
                }
            }
        }

        IntArrayList matches = new IntArrayList();
        if (allIds.length > 0) {
            SparsePool<?> smallest = registry.pool(allIds[0]);
            for (int i = 1; i < allIds.length; i++) {
                SparsePool<?> candidate = registry.pool(allIds[i]);
                if (candidate.size() < smallest.size()) {
                    smallest = candidate;
                }
            }
            for (int slot = 0; slot < smallest.size(); slot++) {
                int entityIndex = smallest.entityAt(slot);
                if (matches(entityIndex, allIds, anyIds, noneIds, exactIds)) {
                    matches.add(entityIndex);
                }
            }
        } else {
            for (int slot = 0; slot < alive.size(); slot++) {
                int entityIndex = alive.get(slot);
                if (matches(entityIndex, allIds, anyIds, noneIds, exactIds)) {
                    matches.add(entityIndex);
                }
            }
        }
        return new QueryResult(this, matches.toArray(), structuralVersion);
    }

    private boolean matches(int entityIndex, int[] allIds, int[] anyIds, int[] noneIds, int[] exactIds) {
        for (int id : allIds) {
            if (!registry.pool(id).has(entityIndex)) {
                return false;
            }
        }
        if (anyIds.length > 0) {
            boolean found = false;
            for (int id : anyIds) {
                if (registry.pool(id).has(entityIndex)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        for (int id : noneIds) {
            if (registry.pool(id).has(entityIndex)) {
                return false;
            }
        }
        if (exactIds != null) {
            int present = 0;
            for (SparsePool<?> pool : registry.allPools()) {
                if (pool.has(entityIndex)) {
                    present++;
                }
            }
            if (present != exactIds.length) {
                return false;
            }
            for (int id : exactIds) {
                if (!registry.pool(id).has(entityIndex)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int[] idsFor(Class<?>[] types) {
        int[] ids = new int[types.length];
        for (int i = 0; i < types.length; i++) {
            ids[i] = registry.id(types[i]);
        }
        return ids;
    }

    private int[] registeredIds(Class<?>[] types) {
        int[] ids = new int[types.length];
        int count = 0;
        for (Class<?> type : types) {
            int id = registry.id(type);
            if (id >= 0) {
                ids[count++] = id;
            }
        }
        return count == ids.length ? ids : Arrays.copyOf(ids, count);
    }

    private QueryResult emptyResult() {
        return new QueryResult(this, EMPTY_INDICES, structuralVersion);
    }

    // ------------------------------------------------------------------
    // Frame execution
    // ------------------------------------------------------------------

    public void update(float delta) {
        if (!systemsInitialized) {
            systems.init();
            systemsInitialized = true;
        }
        commands.flush();
        events.drain();
        for (Phase phase : Phase.values()) {
            systems.update(phase, delta);
            commands.flush();
            events.drain();
        }
    }

    /** Removes all entities and cached query results; systems stay registered. */
    public void clear() {
        commands.clear();
        events.clear();
        for (SparsePool<?> pool : registry.allPools()) {
            pool.clear();
        }
        alive = new AliveSet();
        freeIndices = new IntArrayList();
        generations = new int[INITIAL_CAPACITY];
        nextIndex = 0;
        entityCount = 0;
        queryCache.clear();
        structuralVersion++;
    }

    // ------------------------------------------------------------------
    // Services
    // ------------------------------------------------------------------

    public ComponentRegistry registry() {
        return registry;
    }

    public SystemManager systems() {
        return systems;
    }

    public Resources resources() {
        return resources;
    }

    public EventBus events() {
        return events;
    }

    public Commands commands() {
        return commands;
    }

    public long structuralVersion() {
        return structuralVersion;
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    void apply(Command command) {
        if (command instanceof Command.Destroy destroy) {
            destroyNow(destroy.entity());
        } else if (command instanceof Command.Add add) {
            if (isAlive(add.entity()) && add.typeId() >= 0) {
                SparsePool<Object> pool = poolFor(add.typeId());
                boolean wasPresent = pool.has(add.entity().index());
                pool.add(add.entity().index(), add.component());
                if (!wasPresent) {
                    structuralVersion++;
                }
            }
        } else if (command instanceof Command.Remove remove && remove.typeId() >= 0) {
            if (isAlive(remove.entity())
                    && registry.pool(remove.typeId()).remove(remove.entity().index()) != null) {
                structuralVersion++;
            }
        }
    }

    private void destroyNow(Entity entity) {
        if (!isAlive(entity)) {
            return;
        }
        int index = entity.index();
        for (SparsePool<?> pool : registry.allPools()) {
            pool.remove(index);
        }
        alive.remove(index);
        entityCount--;
        structuralVersion++;

        int nextGeneration = generations[index] + 1;
        if (nextGeneration <= Entity.MAX_GENERATION) {
            generations[index] = nextGeneration;
            freeIndices.add(index);
        } else {
            // Generation space exhausted: retire this index permanently.
            generations[index] = Entity.GENERATION_MASK;
        }
    }

    private void requireAlive(Entity entity) {
        if (!isAlive(entity)) {
            throw new IllegalArgumentException("entity is not alive: " + entity);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> SparsePool<T> poolFor(int typeId) {
        return (SparsePool<T>) registry.pool(typeId);
    }

    private void ensureGenerations(int index) {
        if (index >= generations.length) {
            int newLength = generations.length;
            while (newLength <= index) {
                newLength *= 2;
            }
            generations = Arrays.copyOf(generations, newLength);
        }
    }
}
