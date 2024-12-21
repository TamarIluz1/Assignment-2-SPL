package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    // TODO: Define fields and methods.
    int currentTick;
    STATUS status;
    Vector<Pose> PoseList;
}
