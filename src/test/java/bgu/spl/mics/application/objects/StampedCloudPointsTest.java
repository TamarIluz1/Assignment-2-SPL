package bgu.spl.mics.application.objects;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class StampedCloudPointsTest {

    @Test
    public void testConstructorAndGetters() {
        String id = "Wall_1";
        int time = 5;

        ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
        cloudPoints.add(new CloudPoint(1.0, 2.0));
        cloudPoints.add(new CloudPoint(3.0, 4.0));

        StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, cloudPoints);

        assertEquals(id, stampedCloudPoints.getId(), "ID should match the constructor input");
        assertEquals(time, stampedCloudPoints.getTime(), "Time should match the constructor input");
        assertEquals(cloudPoints, stampedCloudPoints.getCloudPoints(), "CloudPoints should match the constructor input");
    }

    @Test
    public void testEmptyCloudPoints() {
        String id = "Wall_2";
        int time = 10;

        ArrayList<CloudPoint> emptyCloudPoints = new ArrayList<>();

        StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, emptyCloudPoints);

        assertEquals(0, stampedCloudPoints.getCloudPoints().size(), "CloudPoints list should be empty");
    }



    @Test
    public void testAddCloudPoints() {
        String id = "Wall_4";
        int time = 20;

        ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
        cloudPoints.add(new CloudPoint(0.0, 0.0));

        StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, cloudPoints);

        // Add new CloudPoint to the list within the object
        stampedCloudPoints.getCloudPoints().add(new CloudPoint(1.0, 1.0));

        assertEquals(2, stampedCloudPoints.getCloudPoints().size(), "CloudPoints list should reflect additions");
    }

    @Test
    public void testCloudPointValues() {
        String id = "Wall_5";
        int time = 25;

        ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
        cloudPoints.add(new CloudPoint(2.5, 3.5));
        cloudPoints.add(new CloudPoint(4.5, 5.5));

        StampedCloudPoints stampedCloudPoints = new StampedCloudPoints(id, time, cloudPoints);

        CloudPoint firstPoint = stampedCloudPoints.getCloudPoints().get(0);
        assertEquals(2.5, firstPoint.getX(), 0.001, "X value of the first CloudPoint should match");
        assertEquals(3.5, firstPoint.getY(), 0.001, "Y value of the first CloudPoint should match");
    }
}
