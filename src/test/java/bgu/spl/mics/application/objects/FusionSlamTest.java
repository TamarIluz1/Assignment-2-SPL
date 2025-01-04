package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class FusionSlamTest {

    private FusionSlam fusionSlam;

    @BeforeEach
    public void setUp() {
        fusionSlam = FusionSlam.getInstance();
    }

    /**
     * Test for convertToGlobal method in FusionSlam.
     * Pre-condition: A list of local CloudPoints and a Pose are provided.
     * Post-condition: The global CloudPoints match the expected transformed coordinates.
     */
    @Test
    public void testConvertToGlobal() {
        // Local coordinates
        List<CloudPoint> localCoordinates = new ArrayList<>();
        localCoordinates.add(new CloudPoint(1.0, 1.0)); // Local point 1
        localCoordinates.add(new CloudPoint(2.0, 2.0)); // Local point 2

        // Pose of the robot
        Pose pose = new Pose(2.0f, 3.0f, 45.0f, 1); // Position (2,3) and yaw angle 45 degrees

        // Expected global coordinates after transformation
        List<CloudPoint> expectedGlobalCoordinates = new ArrayList<>();
        expectedGlobalCoordinates.add(new CloudPoint(2.0 + Math.sqrt(2), 3.0 + Math.sqrt(2))); // Transformed local point 1
        expectedGlobalCoordinates.add(new CloudPoint(2.0 + 2 * Math.sqrt(2), 3.0 + 2 * Math.sqrt(2))); // Transformed local point 2

        // Perform the conversion
        List<CloudPoint> globalCoordinates = fusionSlam.convertToGlobal(localCoordinates, pose);

        // Assert the results
        assertEquals(expectedGlobalCoordinates.size(), globalCoordinates.size(), "Size mismatch in global coordinates.");

        for (int i = 0; i < globalCoordinates.size(); i++) {
            CloudPoint expected = expectedGlobalCoordinates.get(i);
            CloudPoint actual = globalCoordinates.get(i);

            assertEquals(expected.getX(), actual.getX(), 0.001, "X-coordinate mismatch at index " + i);
            assertEquals(expected.getY(), actual.getY(), 0.001, "Y-coordinate mismatch at index " + i);
        }
    }

    /**
     * Test for addOrUpdateLandmark method.
     * Pre-condition: A new landmark or existing landmark with updated data is provided.
     * Post-condition: The landmark is added or updated correctly in the landmarks list.
     */
    @Test
    public void testAddOrUpdateLandmark() {
        String landmarkId = "Wall_1";
        String description = "Wall";
        ArrayList<CloudPoint> initialCoordinates = new ArrayList<>();
        initialCoordinates.add(new CloudPoint(1.0, 1.0));
        initialCoordinates.add(new CloudPoint(2.0, 2.0));

        fusionSlam.addOrUpdateLandmark(landmarkId, description, initialCoordinates);

        LandMark landmark = fusionSlam.findLandmarkById(landmarkId);
        assertNotNull(landmark, "Landmark should have been added.");
        assertEquals(description, landmark.getDescription(), "Landmark description mismatch.");
        assertEquals(initialCoordinates.size(), landmark.getCoordinates().size(), "Landmark coordinates size mismatch.");

        ArrayList<CloudPoint> newCoordinates = new ArrayList<>();
        newCoordinates.add(new CloudPoint(1.5, 1.5));
        newCoordinates.add(new CloudPoint(2.5, 2.5));

        fusionSlam.addOrUpdateLandmark(landmarkId, description, newCoordinates);

        landmark = fusionSlam.findLandmarkById(landmarkId);
        assertNotNull(landmark, "Landmark should still exist after update.");
        assertEquals(newCoordinates.size(), landmark.getCoordinates().size(), "Updated landmark coordinates size mismatch.");
    }

    /**
     * Test for getPoseByTime method.
     * Pre-condition: A Pose with a specific time is added to the system.
     * Post-condition: The Pose can be retrieved correctly by its time.
     */
    @Test
    public void testGetPoseByTime() {
        Pose pose = new Pose(1.0f, 1.0f, 90.0f, 5);
        fusionSlam.addPose(pose);

        Pose retrievedPose = fusionSlam.getPoseByTime(5);
        assertNotNull(retrievedPose, "Pose should be retrieved.");
        assertEquals(pose.getX(), retrievedPose.getX(), "Pose X mismatch.");
        assertEquals(pose.getY(), retrievedPose.getY(), "Pose Y mismatch.");
        assertEquals(pose.getYaw(), retrievedPose.getYaw(), "Pose yaw mismatch.");
    }

    /**
     * Test for processTrackedObjectsToLandmarks method.
     * Pre-condition: A TrackedObjectsEvent with tracked objects is provided.
     * Post-condition: The tracked objects are converted to landmarks.
     */
    @Test
    public void testProcessTrackedObjectsToLandmarks() {
        // Setup
        Pose testPose = new Pose(0, 0, 0, 1);
        fusionSlam.addPose(testPose);

        ArrayList<CloudPoint> points = new ArrayList<>();
        points.add(new CloudPoint(1.0, 2.0));
        points.add(new CloudPoint(2.0, 3.0));

        TrackedObject trackedObject = new TrackedObject("Landmark_1", 1, "Test Landmark", points);
        ArrayList<TrackedObject> trackedObjects = new ArrayList<>();
        trackedObjects.add(trackedObject);

        TrackedObjectsEvent trackedObjectsEvent = new TrackedObjectsEvent(trackedObjects, 1);

        // Execute
        fusionSlam.processTrackedObjectsToLandmarks(trackedObjectsEvent);

        // Verify
        LandMark landmark = fusionSlam.findLandmarkById("Landmark_1");
        assertNotNull(landmark, "Landmark should have been added.");
        assertEquals("Test Landmark", landmark.getDescription(), "Landmark description mismatch.");
        assertEquals(2, landmark.getCoordinates().size(), "Landmark coordinates size mismatch.");

        // Verify coordinate transformation (if applicable)
        ArrayList<CloudPoint> expectedCoordinates = points; // Replace with expected transformed coordinates
        for (int i = 0; i < expectedCoordinates.size(); i++) {
            assertEquals(expectedCoordinates.get(i).getX(), landmark.getCoordinates().get(i).getX(), 0.001, "X-coordinate mismatch.");
            assertEquals(expectedCoordinates.get(i).getY(), landmark.getCoordinates().get(i).getY(), 0.001, "Y-coordinate mismatch.");
        }
    }
}

