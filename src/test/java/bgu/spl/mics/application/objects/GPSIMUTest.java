package bgu.spl.mics.application.objects;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class GPSIMUTest {

    @Test
    public void testInitializationAndGetStatus() {

        ArrayList<Pose> poses = new ArrayList<>();
        poses.add(new Pose(5.0f, 10.0f, 45.0f, 1));
        poses.add(new Pose(15.0f, 20.0f, 90.0f, 2));

        GPSIMU gpsimu = new GPSIMU(poses);

        assertEquals(STATUS.UP, gpsimu.getStatus(), "GPSIMU should initialize with status UP.");
        //assertEquals(poses, gpsimu.getPoseList(), "Pose list should be correctly initialized.");
    }

    @Test
    public void testGetCurrentPose() {
        ArrayList<Pose> poses = new ArrayList<>();
        poses.add(new Pose(5.0f, 10.0f, 45.0f, 1));
        poses.add(new Pose(15.0f, 20.0f, 90.0f, 2));

        GPSIMU gpsimu = new GPSIMU(poses);

        gpsimu.setCurrentTick(1);
        Pose currentPose = gpsimu.getCurrentPose();

        assertNotNull(currentPose, "Current pose should not be null.");
        assertEquals(5.0f, currentPose.getX(), 0.001, "X coordinate of current pose should match.");
        assertEquals(10.0f, currentPose.getY(), 0.001, "Y coordinate of current pose should match.");
        assertEquals(45.0f, currentPose.getYaw(), 0.001, "Yaw angle of current pose should match.");
    }



    @Test
    public void testSetStatus() {
        ArrayList<Pose> poses = new ArrayList<>();
        GPSIMU gpsimu = new GPSIMU(poses);

        gpsimu.setStatus(STATUS.DOWN);
        assertEquals(STATUS.DOWN, gpsimu.getStatus(), "GPSIMU should update status correctly.");
    }
}
