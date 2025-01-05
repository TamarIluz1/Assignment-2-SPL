package bgu.spl.mics;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.services.CameraService;

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

}
