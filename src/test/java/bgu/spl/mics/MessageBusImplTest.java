package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private MicroService dummyMicroService;
    private DetectObjectsEvent dummyEvent;
    private Broadcast dummyBroadcast;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        dummyMicroService = new CameraService(new Camera(1,2 ,STATUS.UP)); 
        
        DetectedObject dummyObject = new DetectedObject("Wall1", "wall");
        ArrayList<DetectedObject> dummyObjects = new ArrayList<>();
        dummyObjects.add(dummyObject);
        StampedDetectedObjects dummyStampedObjects = new StampedDetectedObjects(1, dummyObjects);
        dummyEvent = new DetectObjectsEvent(1, dummyStampedObjects);
        dummyBroadcast = new TerminatedBroadcast("camera");
    }

    /**
     * Test 1: Subscription to events.
     * Pre-condition: Microservice subscribes to a specific event type.
     * Post-condition: Event published is delivered to the subscribed microservice.
     */
     @Test
     public void testSubscribeEvent() throws InterruptedException {
         messageBus.register(dummyMicroService);
         messageBus.subscribeEvent(dummyEvent.getClass(), dummyMicroService);

         messageBus.sendEvent(dummyEvent);
         Message received = messageBus.awaitMessage(dummyMicroService);

         assertEquals(dummyEvent, received, "Microservice did not receive the expected event.");
     }

     /**
    //  * Test 2: Subscription to broadcasts.
    //  * Pre-condition: Microservice subscribes to a broadcast type.
    //  * Post-condition: Broadcast published is delivered to all subscribed microservices.
    //  */
     @Test
     public void testSubscribeBroadcast() throws InterruptedException {
         messageBus.register(dummyMicroService);
         messageBus.subscribeBroadcast(dummyBroadcast.getClass(), dummyMicroService);

         messageBus.sendBroadcast(dummyBroadcast);
         Message received = messageBus.awaitMessage(dummyMicroService);

         assertEquals(dummyBroadcast, received, "Microservice did not receive the expected broadcast.");
     }

    // /**
    //  * Test 3: Queue management for microservices.
    //  * Pre-condition: Messages are sent to a microservice queue.
    //  * Post-condition: Microservice retrieves messages in the order they were sent.
    //  */
    // @Test
    // public void testMicroServiceQueue() throws InterruptedException {
    //     // Step 1: Register the microservice with the message bus
    //     messageBus.register(dummyMicroService);

    //     // Step 2: Send a dummy event and a dummy broadcast to the microservice queue
    //     messageBus.sendEvent(dummyEvent);
    //     messageBus.sendBroadcast(dummyBroadcast);

    //     // Step 3: Await and retrieve messages from the microservice's queue
    //     Message firstMessage = messageBus.awaitMessage(dummyMicroService);
    //     Message secondMessage = messageBus.awaitMessage(dummyMicroService);

    //     // Step 4: Validate the order of messages retrieved
    //     assertEquals(dummyEvent.toString(), firstMessage.toString(), "First message in queue is not as expected.");
    //     assertEquals(dummyBroadcast.toString(), secondMessage.toString(), "Second message in queue is not as expected.");

    //     // Step 5: Clean up by unregistering the microservice
    //     messageBus.unregister(dummyMicroService);
    // }


    /**
     * Test 4: Event handling with no subscribers.
     * Pre-condition: No microservices are subscribed to a specific event type.
     * Post-condition: Sending the event returns null.
     */
    @Test
    public void testSendEventNoSubscribers() {
        Event<Boolean> unhandledEvent = new DetectObjectsEvent(0, null);
        Future<Boolean> future = messageBus.sendEvent(unhandledEvent);

        assertNull(future, "Future should be null when no subscribers are registered.");
    }

    /**
     * Test 5: Broadcast handling with no subscribers.
     * Pre-condition: No microservices are subscribed to a specific broadcast type.
     * Post-condition: Sending the broadcast does not throw any exceptions.
     */
    @Test
    public void testSendBroadcastNoSubscribers() {
        Broadcast unhandledBroadcast = new TerminatedBroadcast("camera");

        // Sending broadcast should not throw exceptions even with no subscribers
        assertDoesNotThrow(() -> messageBus.sendBroadcast(unhandledBroadcast));
    }

    /**
     * Test 6: Unregistering a microservice.
     * Pre-condition: A microservice is registered and subscribed to an event.
     * Post-condition: Unregistering the microservice prevents it from receiving any more messages.
     */
    @Test
    public void testUnregisterMicroService() throws InterruptedException {
        messageBus.register(dummyMicroService);
        messageBus.subscribeEvent(dummyEvent.getClass(), dummyMicroService);

        // Ensure the microservice can initially receive messages
        messageBus.sendEvent(dummyEvent);
        assertNotNull(messageBus.awaitMessage(dummyMicroService), "Microservice did not receive the initial event.");

        // Unregister the microservice
        messageBus.unregister(dummyMicroService);

        // Sending another event should not be delivered
        messageBus.sendEvent(dummyEvent);
        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(dummyMicroService), 
                    "Microservice should not receive messages after being unregistered.");
    }

    /**
     * Test 7: Handling multiple microservices subscribed to the same broadcast.
     * Pre-condition: Multiple microservices subscribe to a broadcast.
     * Post-condition: All microservices receive the broadcast.
     */
    @Test
    public void testMultipleSubscribersBroadcast() throws InterruptedException {
        MicroService secondMicroService = new CameraService(new Camera(2, 3, STATUS.UP));
        messageBus.register(dummyMicroService);
        messageBus.register(secondMicroService);

        messageBus.subscribeBroadcast(dummyBroadcast.getClass(), dummyMicroService);
        messageBus.subscribeBroadcast(dummyBroadcast.getClass(), secondMicroService);

        messageBus.sendBroadcast(dummyBroadcast);

        // Both microservices should receive the broadcast
        assertEquals(dummyBroadcast, messageBus.awaitMessage(dummyMicroService), "First microservice did not receive the broadcast.");
        assertEquals(dummyBroadcast, messageBus.awaitMessage(secondMicroService), "Second microservice did not receive the broadcast.");
    }

    /**
     * Test 8: Round-robin event distribution.
     * Pre-condition: Two microservices are subscribed to the same event.
     * Post-condition: Events are distributed in round-robin order.
     */
    @Test
    public void testRoundRobinEventDistribution() throws InterruptedException {
        MicroService secondMicroService = new CameraService(new Camera(2, 3, STATUS.UP));
        messageBus.register(dummyMicroService);
        messageBus.register(secondMicroService);

        messageBus.subscribeEvent(dummyEvent.getClass(), dummyMicroService);
        messageBus.subscribeEvent(dummyEvent.getClass(), secondMicroService);

        // Send two events
        messageBus.sendEvent(dummyEvent);
        messageBus.sendEvent(dummyEvent);

        // Verify round-robin distribution
        assertEquals(dummyEvent, messageBus.awaitMessage(dummyMicroService), "First event not received by first microservice.");
        assertEquals(dummyEvent, messageBus.awaitMessage(secondMicroService), "Second event not received by second microservice.");
    }

    /**
     * Test 9: Complete event resolution.
     * Pre-condition: An event is sent and resolved.
     * Post-condition: Future associated with the event is resolved with the correct value.
     */
    @Test
    public void testCompleteEvent() {
        messageBus.register(dummyMicroService);
        messageBus.subscribeEvent(dummyEvent.getClass(), dummyMicroService);

        Future<Boolean> future = messageBus.sendEvent(dummyEvent);
        assertNotNull(future, "Future should not be null after sending an event.");

        // Resolve the event
        messageBus.complete(dummyEvent, true);

        // Verify resolution
        assertTrue(future.isDone(), "Future was not resolved.");
        assertTrue(future.get(), "Future did not resolve with the correct value.");
    }

    /**
     * Test 10: Sending event to unregistered microservice.
     * Pre-condition: An event is sent to a microservice that is not registered.
     * Post-condition: Event is not delivered, and no exceptions occur.
     */
    @Test
    public void testSendEventToUnregisteredMicroService() {
        Event<Boolean> anotherEvent = new DetectObjectsEvent(0, null);

        // Send an event with no microservice registered
        Future<Boolean> future = messageBus.sendEvent(anotherEvent);
        assertNull(future, "Future should be null when the receiving microservice is not registered.");
    }
}
