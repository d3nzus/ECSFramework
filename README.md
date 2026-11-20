# ECSFramework

A small, reusable, engine-agnostic Entity Component System for Java 17+ game
development. No external dependencies, no build tool required — just `javac`.

## Design

| Concern | Choice |
| --- | --- |
| Component storage | Sparse-set pools (dense arrays + entity→slot lookup), O(1) add/get/remove |
| Entity handles | Immutable `int` packing 20-bit index + 12-bit generation; stale handles are rejected |
| Queries | Fluent `all` / `any` / `none` / `exclusive`, cached by structural version, typed views up to 4 components |
| Mutation safety | Destruction and `Commands` changes are deferred to phase boundaries |
| Scheduling | Four fixed phases (`INPUT`, `UPDATE`, `RENDER`, `CLEANUP`) with priority ordering |
| Services | Typed resources, queued event bus, game math, 2D transforms, fixed timestep, spatial hash |

## Layout

```
src/       — all core and utility Java sources
examples/  — runnable demos and benchmark
test/      — test suite and runner
```

Sources live directly in these three folders; there are no nested `dev/d3nzus/ecs`
directories. Java packages are unchanged (`dev.d3nzus.ecs.core`,
`dev.d3nzus.ecs.util`, `dev.d3nzus.ecs.examples`, `dev.d3nzus.ecs.tests`) —
the build compiles an explicit file list, so source folders do not need to
mirror package names. Core covers world, entities, pools, queries, commands,
systems, resources and events; utilities cover `Vec2`, `Vec3`, `MathUtils`,
`Transform2D`, `Parent`, `Time`, `FixedStepAccumulator` and `SpatialHash`.
Utilities depend on core; core has no dependency on utilities.

## Build and run

Requires a standard JDK 17 or newer.

```bash
./build.sh          # compiles into out/ and runs the test suite
java -cp out dev.d3nzus.ecs.examples.HelloWorld
java -cp out dev.d3nzus.ecs.examples.SpatialHashDemo
java -cp out dev.d3nzus.ecs.examples.Benchmark 100000 60   # optional throughput check
```

On Windows use `build.bat`. If `JAVA_HOME` is set, the scripts use it.

## Quick start

```java
import dev.d3nzus.ecs.core.*;

public final class Position { public float x, y; public Position(float x, float y) { this.x = x; this.y = y; } }
public final class Velocity { public float x, y; public Velocity(float x, float y) { this.x = x; this.y = y; } }
public final class Frozen { }

public final class MovementSystem implements dev.d3nzus.ecs.core.System {
    private final Query2<Position, Velocity> movers;

    public MovementSystem(World world) {
        movers = world.query2(
                Query.all(Position.class, Velocity.class).none(Frozen.class),
                Position.class, Velocity.class);
    }

    @Override
    public void update(World world, float delta) {
        movers.forEach((entity, position, velocity) -> {
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
        });
    }
}
```

```java
World world = new World();
world.systems().add(new MovementSystem(world), Phase.UPDATE);

Entity ship = world.create();
world.add(ship, new Position(0, 0));
world.add(ship, new Velocity(10, 2));

for (int frame = 0; frame < 60; frame++) {
    world.update(1f / 60f);
}
```

## Core API

**World**

| Method | Notes |
| --- | --- |
| `create()` | Immediate; returns a fresh generation-stamped handle |
| `destroy(entity)` | Queued destruction, applied at the next command flush |
| `isAlive(entity)` | Also rejects stale generations |
| `add/get/has/remove` | Immediate component access; `add` registers the component type on first use |
| `query(Query)` | Cached `QueryResult` snapshot; recomputed when structure changes |
| `query1..query4(...)` | Typed views with allocation-light `forEach` callbacks |
| `update(delta)` | Runs phases, flushing commands and draining events after each |
| `clear()` | Removes all entities and components; systems and resources remain |

**Queries**

```java
Query.all(Position.class, Velocity.class).none(Frozen.class)
Query.all(Attack.class).any(Melee.class, Ranged.class)
Query.exclusive(Position.class, Sprite.class)   // exactly these components
Query.all()                                     // every alive entity
```

`QueryResult` is a snapshot: it stays readable even if entities are destroyed
later in the same phase. For more than four components, use `QueryResult` plus
`world.get(entity, Type.class)`.

**Commands and safety**

Component add/remove is immediate; destroying while iterating a snapshot is
safe because destruction is deferred. For everything else inside a system,
prefer the command queue:

```java
world.commands().destroy(target);
world.commands().add(target, new Velocity(1, 0));
world.commands().remove(target, Frozen.class);
```

Commands are applied before the first phase and after every phase, so a later
phase sees earlier changes. Never share a `World` between threads.

**Systems**

```java
world.systems().add(system, Phase.INPUT, -10);   // lower priority runs first
world.systems().remove(system);                   // calls dispose()
```

`init(World)` runs on the first `update()` (or immediately for systems added
later). `dispose()` runs on removal.

**Resources and events**

```java
world.resources().put(Camera.class, camera);
Camera camera = world.resources().get(Camera.class);

world.events().on(Collision.class, event -> ...);
world.events().emit(new Collision(a, b));   // delivered on the next drain
```

Handlers registered for a supertype also receive subtype events. Events emitted
during a drain are delivered in the same drain.

## Utilities

- `Vec2` / `Vec3` — mutable, chainable vector math
- `MathUtils` — clamp, lerp, smoothstep, wrap, angle conversion
- `Transform2D` — local + world-space position/rotation/scale; use with `Parent`
- `TransformHierarchySystem` — parents-before-children world transform propagation
- `Time` + `TimeSystem` — frame delta, elapsed time, time scale
- `FixedStepAccumulator` — deterministic ticks with render interpolation alpha
- `SpatialHash` — cell-grid broad phase with `insert`, `remove`, `query`, `queryRadius`

## Notes and limits

- Single-threaded by design; pools are not synchronized.
- Dense component order is unspecified (swap-removal) — never depend on it.
- Component classes should be plain mutable types with public fields or accessors.
- The reserved `Entity.INVALID` handle can be used as "no parent"/"no target".
- No serialization, dependency-graph system scheduler, or parallel iteration yet.
