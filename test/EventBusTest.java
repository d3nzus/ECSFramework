package dev.d3nzus.ecs.tests;

import dev.d3nzus.ecs.core.EventBus;

public final class EventBusTest {

    public static class Ping {
        final int value;

        Ping(int value) {
            this.value = value;
        }
    }

    public static final class SubPing extends Ping {
        SubPing(int value) {
            super(value);
        }
    }

    public static void testQueuedDispatchAndSupertypes() {
        EventBus bus = new EventBus();
        int[] pingCount = {0};
        int[] subCount = {0};
        bus.on(Ping.class, event -> pingCount[0]++);
        bus.on(SubPing.class, event -> subCount[0]++);

        bus.emit(new Ping(1));
        TestRunner.eq(0, pingCount[0], "not delivered before drain");
        TestRunner.eq(1, bus.pending(), "event queued");
        bus.drain();
        TestRunner.eq(1, pingCount[0], "ping delivered");
        TestRunner.eq(0, subCount[0], "subtype handler not called for super event");

        bus.emit(new SubPing(2));
        bus.drain();
        TestRunner.eq(2, pingCount[0], "supertype handler also called");
        TestRunner.eq(1, subCount[0], "subtype handler called");
        TestRunner.eq(0, bus.pending(), "queue empty after drain");
    }

    public static void testUnsubscribe() {
        EventBus bus = new EventBus();
        int[] count = {0};
        java.util.function.Consumer<Ping> handler = event -> count[0]++;
        bus.on(Ping.class, handler);
        bus.emit(new Ping(1));
        bus.drain();
        TestRunner.eq(1, count[0], "delivered before unsubscribe");
        TestRunner.check(bus.off(Ping.class, handler), "handler removed");
        bus.emit(new Ping(2));
        bus.drain();
        TestRunner.eq(1, count[0], "not delivered after unsubscribe");
    }

    public static void testEventEmittedDuringDrain() {
        EventBus bus = new EventBus();
        int[] count = {0};
        bus.on(Ping.class, event -> {
            count[0]++;
            if (event.value == 1) {
                bus.emit(new Ping(2));
            }
        });
        bus.emit(new Ping(1));
        bus.drain();
        TestRunner.eq(2, count[0], "nested event delivered in same drain");
    }
}
