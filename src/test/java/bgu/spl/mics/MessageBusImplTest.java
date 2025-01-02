package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class MessageBusImplTest {
    private MessageBusImpl messageBus;
    private MicroService testMicroService;
    private MicroService microService1;
    private MicroService microService2;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("MicroService1") {
            @Override
            protected void initialize() {}
        };
        microService2 = new MicroService("MicroService2") {
            @Override
            protected void initialize() {}
        };
        testMicroService = new MicroService("TestService") {
            @Override
            protected void initialize() {
                // No-op for testing
            }
        };
        messageBus.register(microService1);
        messageBus.register(microService2);
    }

    /**
     * Test: Verifies that a registered microservice can subscribe to an event and receive it.
     * Pre-condition:
     *  - Microservice is registered.
     *  - Event is sent.
     * Post-condition:
     *  - Microservice receives the event.
     */
    @Test
    public void testSubscribeEvent() throws InterruptedException {
        Event<String> event = new Event<String>() {};
        messageBus.subscribeEvent(event.getClass(), microService1);

        messageBus.sendEvent(event);
        Message receivedMessage = messageBus.awaitMessage(microService1);

        assertEquals(event, receivedMessage, "MicroService should receive the event it subscribed to.");
    }

    /**
     * Test: Ensures that a microservice can subscribe to a broadcast and receive it.
     * Pre-condition:
     *  - Microservice is registered.
     *  - Broadcast is sent.
     * Post-condition:
     *  - Microservice receives the broadcast.
     */
    @Test
    public void testSubscribeBroadcast() throws InterruptedException {
        Broadcast broadcast = new Broadcast() {};
        messageBus.subscribeBroadcast(broadcast.getClass(), microService1);

        messageBus.sendBroadcast(broadcast);
        Message receivedMessage = messageBus.awaitMessage(microService1);

        assertEquals(broadcast, receivedMessage, "MicroService should receive the broadcast it subscribed to.");
    }

    /**
     * Test: Ensures that unregistering a microservice prevents it from receiving messages.
     * Pre-condition:
     *  - Microservice is registered and subscribed.
     * Post-condition:
     *  - Microservice no longer receives messages after being unregistered.
     */
    @Test
    public void testUnregisterMicroService() throws InterruptedException {
        Event<String> event = new Event<String>() {};
        messageBus.subscribeEvent(event.getClass(), microService1);
        messageBus.unregister(microService1);

        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(microService1), "Unregistered MicroService should not receive messages.");
    }


    @Test
    public void testRegisterAndUnregister() {
        messageBus.register(testMicroService);
        assertDoesNotThrow(() -> messageBus.awaitMessage(testMicroService));

        messageBus.unregister(testMicroService);
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService));
    }

    @Test
    public void testSubscribeEvent() throws InterruptedException {
        Event<String> testEvent = new Event<String>() {};
        messageBus.register(testMicroService);

        messageBus.subscribeEvent(testEvent.getClass(), testMicroService);
        Future<String> future = messageBus.sendEvent(testEvent);

        assertNotNull(future, "Future should not be null after sending an event");
        Message receivedMessage = messageBus.awaitMessage(testMicroService);
        assertEquals(testEvent, receivedMessage, "The received message should match the sent event");
    }

    @Test
    public void testSubscribeBroadcast() throws InterruptedException {
        Broadcast testBroadcast = new Broadcast() {};
        messageBus.register(testMicroService);

        messageBus.subscribeBroadcast(testBroadcast.getClass(), testMicroService);
        messageBus.sendBroadcast(testBroadcast);

        Message receivedMessage = messageBus.awaitMessage(testMicroService);
        assertEquals(testBroadcast, receivedMessage, "The received message should match the sent broadcast");
    }

    @Test
    public void testSendEventWithoutSubscribers() {
        Event<String> testEvent = new Event<String>() {};
        Future<String> future = messageBus.sendEvent(testEvent);

        assertNull(future, "Future should be null when there are no subscribers for the event");
    }

    @Test
    public void testSendBroadcastWithoutSubscribers() {
        Broadcast testBroadcast = new Broadcast() {};
        assertDoesNotThrow(() -> messageBus.sendBroadcast(testBroadcast));
    }

    @Test
    public void testRoundRobinEventDistribution() throws InterruptedException {
        MicroService microService1 = new MicroService("Service1") {
            @Override
            protected void initialize() {}
        };
        MicroService microService2 = new MicroService("Service2") {
            @Override
            protected void initialize() {}
        };

        messageBus.register(microService1);
        messageBus.register(microService2);

        Event<String> testEvent = new Event<String>() {};
        messageBus.subscribeEvent(testEvent.getClass(), microService1);
        messageBus.subscribeEvent(testEvent.getClass(), microService2);

        Future<String> future1 = messageBus.sendEvent(testEvent);
        Future<String> future2 = messageBus.sendEvent(testEvent);

        Message receivedMessage1 = messageBus.awaitMessage(microService1);
        Message receivedMessage2 = messageBus.awaitMessage(microService2);

        assertEquals(testEvent, receivedMessage1, "First event should go to the first service");
        assertEquals(testEvent, receivedMessage2, "Second event should go to the second service");
    }

    @Test
    public void testCompleteEvent() {
        Event<String> testEvent = new Event<String>() {};
        messageBus.register(testMicroService);

        messageBus.subscribeEvent(testEvent.getClass(), testMicroService);
        Future<String> future = messageBus.sendEvent(testEvent);

        messageBus.complete(testEvent, "Completed");
        assertTrue(future.isDone(), "Future should be resolved after calling complete");
        assertEquals("Completed", future.get(100, TimeUnit.MILLISECONDS), "Future result should match the completed value");
    }

    @Test
    public void testAwaitMessageUnregisteredService() {
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService),
                "Unregistered microservice should throw an exception when awaiting messages");
    }






}