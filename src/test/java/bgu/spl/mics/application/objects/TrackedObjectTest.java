package bgu.spl.mics.application.objects;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TrackedObject class.
 */
public class TrackedObjectTest {

    @Test
    public void testConstructorAndGetters() {
        String id = "Object1";
        int time = 5;
        String description = "This is a tracked object";
        ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
        cloudPoints.add(new CloudPoint(1.0, 2.0));
        cloudPoints.add(new CloudPoint(3.0, 4.0));

        TrackedObject trackedObject = new TrackedObject(id, time, description, cloudPoints);

        assertEquals(id, trackedObject.getId(), "ID should match the value set in the constructor.");
        assertEquals(time, trackedObject.getTime(), "Time should match the value set in the constructor.");
        assertEquals(description, trackedObject.getDescription(), "Description should match the value set in the constructor.");
        assertEquals(cloudPoints, trackedObject.getCloudPoint(), "Cloud points should match the value set in the constructor.");
    }

    @Test
    public void testAddCloudPoint() {
        TrackedObject trackedObject = new TrackedObject("Object2", 10, "Another tracked object", new ArrayList<>());

        CloudPoint cloudPoint1 = new CloudPoint(5.0, 6.0);
        CloudPoint cloudPoint2 = new CloudPoint(7.0, 8.0);

        trackedObject.getCloudPoint().add(cloudPoint1);
        trackedObject.getCloudPoint().add(cloudPoint2);

        ArrayList<CloudPoint> cloudPoints = trackedObject.getCloudPoint();
        assertEquals(2, cloudPoints.size(), "Cloud points list should contain 2 points.");
        assertEquals(cloudPoint1, cloudPoints.get(0), "First cloud point should match.");
        assertEquals(cloudPoint2, cloudPoints.get(1), "Second cloud point should match.");
    }

    @Test
    public void testModifyDescription() {
        TrackedObject trackedObject = new TrackedObject("Object3", 15, "Initial description", new ArrayList<>());

        String newDescription = "Updated description";
        trackedObject.description = newDescription;

        assertEquals(newDescription, trackedObject.getDescription(), "Description should be updated.");
    }

    @Test
    public void testEmptyCloudPoints() {
        TrackedObject trackedObject = new TrackedObject("Object4", 20, "Empty cloud points", new ArrayList<>());

        assertTrue(trackedObject.getCloudPoint().isEmpty(), "Cloud points list should be empty.");
    }



    @Test
    public void testTimeGetter() {
        TrackedObject trackedObject = new TrackedObject("Object6", 30, "Test object", new ArrayList<>());

        assertEquals(30, trackedObject.getTime(), "Time getter should return the correct time value.");
    }
}
