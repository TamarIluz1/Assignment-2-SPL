package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.messages.DetectObjectsEvent;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;



/**
 * Unit tests for the LiDarWorkerTracker class.
 * These tests verify the functionality and behavior of the LiDarWorkerTracker,
 * including tracking objects, handling events, interacting with the LiDarDataBase,
 * and maintaining its internal state.
 */
public class LiDarWorkerTrackerTest {

    private LiDarWorkerTracker lidarWorker;
    private ArrayList<CloudPoint> cloudPoints;
    private ArrayList<TrackedObject> trackedObjects;
    private TrackedObject trackedObject;
    private DetectObjectsEvent detectEvent;


    @BeforeEach
    public void setUp() {
        // Setup LiDarWorkerTracker with mock data file
        Path mockFilePath = Paths.get("src", "test", "java", "bgu", "example_input", "mock_lidar_data.json");
        lidarWorker = new LiDarWorkerTracker(1, 10, mockFilePath.toAbsolutePath().toString());

        // Mock tracked object
        cloudPoints = new ArrayList<>();
        cloudPoints.add(new CloudPoint(1.0, 1.0));
        cloudPoints.add(new CloudPoint(2.0, 2.0));
        trackedObject = new TrackedObject("Object_1", 1, "Test Object", cloudPoints);

        // Mock tracked objects list
        trackedObjects = new ArrayList<>();
        trackedObjects.add(trackedObject);

        // Mock DetectObjectsEvent
        detectEvent = new DetectObjectsEvent(1, new StampedDetectedObjects(1, new ArrayList<>()));
    }


    /**
     * Test the creation of a LiDarWorkerTracker instance.
     * Pre-condition: The constructor is called with valid parameters.
     * Post-condition: The LiDarWorkerTracker instance is correctly initialized.
     */
    @Test
    public void testLiDarWorkerTrackerCreation() {
        assertNotNull(lidarWorker, "LiDarWorkerTracker should be initialized.");
        assertEquals(1, lidarWorker.getId(), "Incorrect LiDarWorkerTracker ID.");
        assertEquals(10, lidarWorker.getFrequency(), "Incorrect frequency.");
        assertEquals(STATUS.UP, lidarWorker.getStatus(), "Status should be UP on initialization.");
    }

    /**
     * Test adding and retrieving tracked objects.
     * Pre-condition: A tracked object is added to the LiDarWorkerTracker.
     * Post-condition: The tracked object is correctly stored and retrieved.
     */
    @Test
    public void testAddAndRetrieveTrackedObjects() {
        lidarWorker.addLastTrackedObject(trackedObject);
        ArrayList<TrackedObject> retrievedObjects = lidarWorker.getLastTrackedObjects();

        assertNotNull(retrievedObjects, "Tracked objects list should not be null.");
        assertEquals(1, retrievedObjects.size(), "Tracked objects list size mismatch.");
        assertEquals(trackedObject, retrievedObjects.get(0), "Tracked object mismatch.");
    }


    /**
     * Test adding and retrieving DetectObjectsEvent instances.
     * Pre-condition: A DetectObjectsEvent is added to the LiDarWorkerTracker.
     * Post-condition: The DetectObjectsEvent is correctly stored and retrieved.
     */
    @Test
    public void testAddAndRetrieveDetectObjectsEvent() {
        lidarWorker.addNewDetectEvent(detectEvent);
        ArrayList<DetectObjectsEvent> retrievedEvents = lidarWorker.getEventsRecieved();

        assertNotNull(retrievedEvents, "DetectObjectsEvent list should not be null.");
        assertEquals(1, retrievedEvents.size(), "DetectObjectsEvent list size mismatch.");
        assertEquals(detectEvent, retrievedEvents.get(0), "DetectObjectsEvent mismatch.");
    }

    

    /**
     * Test fetching cloud points by time from the LiDarDataBase.
     * Pre-condition: The LiDarDataBase contains cloud points for the given time.
     * Post-condition: The cloud points for the specified time are correctly fetched.
     */
    @Test
    public void testGetNewCloudPointsByTime() {
        ArrayList<StampedCloudPoints> fetchedCloudPoints = lidarWorker.getNewCloudPointsByTime(1);

        assertNotNull(fetchedCloudPoints, "Fetched cloud points should not be null.");
        assertTrue(fetchedCloudPoints.size() > 0, "No cloud points fetched for the given time.");
    }


    /**
     * Test reporting tracked objects and checking the finished state.
     * Pre-condition: Tracking progress is reported.
     * Post-condition: The finished state is correctly updated based on tracked objects.
     */
    @Test
    public void testReportTrackedAndIsFinished() {
        lidarWorker.reportTracked();
        assertFalse(lidarWorker.isFinished(), "Should not be finished after one report if data exists.");

        for (int i = 0; i < lidarWorker.getNewCloudPointsByTime(1).size(); i++) {
            lidarWorker.reportTracked();
        }

        assertTrue(lidarWorker.isFinished(), "Should be finished after tracking all data.");
    }

    /**
     * Test handling processed DetectObjectsEvent instances.
     * Pre-condition: A DetectObjectsEvent is added and marked as processed.
     * Post-condition: The processed DetectObjectsEvent is removed from the list.
     */
    @Test
    public void testHandleProcessedDetected() {
        lidarWorker.addNewDetectEvent(detectEvent);
        ArrayList<DetectObjectsEvent> eventsToProcess = new ArrayList<>();
        eventsToProcess.add(detectEvent);

        lidarWorker.handleProcessedDetected(eventsToProcess);

        ArrayList<DetectObjectsEvent> remainingEvents = lidarWorker.getEventsRecieved();
        assertTrue(remainingEvents.isEmpty(), "Processed events should be removed from the list.");
    }

    /**
     * Test setting and retrieving the status of the LiDarWorkerTracker.
     * Pre-condition: The status of the LiDarWorkerTracker is updated.
     * Post-condition: The updated status is correctly retrieved.
     */
    @Test
    public void testSetAndGetStatus() {
        lidarWorker.setStatus(STATUS.DOWN);
        assertEquals(STATUS.DOWN, lidarWorker.getStatus(), "Status should be DOWN after setting it.");
    }
}
