package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PoseTest {

    @Test
    public void testConstructorAndGetters() {
        float x = 10.5f;
        float y = 20.3f;
        float yaw = 45.0f;
        int time = 5;

        Pose pose = new Pose(x, y, yaw, time);

        assertEquals(x, pose.getX(), 0.001, "X coordinate should match the constructor input");
        assertEquals(y, pose.getY(), 0.001, "Y coordinate should match the constructor input");
        assertEquals(yaw, pose.getYaw(), 0.001, "Yaw should match the constructor input");
        assertEquals(time, pose.getTime(), "Time should match the constructor input");
    }

    @Test
    public void testEquality() {
        Pose pose1 = new Pose(10.5f, 20.3f, 45.0f, 5);
        Pose pose2 = new Pose(10.5f, 20.3f, 45.0f, 5);
        Pose pose3 = new Pose(12.0f, 25.0f, 90.0f, 6);

        assertEquals(pose1, pose2, "Poses with the same attributes should be equal");
        assertNotEquals(pose1, pose3, "Poses with different attributes should not be equal");
    }

    @Test
    public void testHashCode() {
        Pose pose1 = new Pose(10.5f, 20.3f, 45.0f, 5);
        Pose pose2 = new Pose(10.5f, 20.3f, 45.0f, 5);
        Pose pose3 = new Pose(12.0f, 25.0f, 90.0f, 6);

        assertEquals(pose1.hashCode(), pose2.hashCode(), "Hashcodes should match for equal poses");
        assertNotEquals(pose1.hashCode(), pose3.hashCode(), "Hashcodes should differ for different poses");
    }

    @Test
    public void testStringRepresentation() {
        Pose pose = new Pose(10.5f, 20.3f, 45.0f, 5);
        String expected = "Pose{x=10.5, y=20.3, yaw=45.0, time=5}";
        assertEquals(expected, pose.toString(), "String representation should match the expected format");
    }
}
