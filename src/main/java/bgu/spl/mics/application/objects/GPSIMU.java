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
    Vector<Pose> poseList;

    public GPSIMU(Vector<Pose> PoseList){
        currentTick = 0;
        this.poseList = PoseList;
        this.status = STATUS.UP;

    }

    public Pose getCurrentPose() {
        // Find the pose that matches the current tick, default to the last known pose if not found
        return poseList.stream()
                .filter(pose -> pose.getTime() == currentTick)
                .findFirst()
                .orElse(poseList.isEmpty() ? null : poseList.lastElement()); // Safe fallback to the last element
    }

    public void setCurrentTick(int tick) {
        this.currentTick = tick;
        // Optional: Check if currentTick exceeds available data and adjust status
        if (!poseList.isEmpty() && tick > poseList.lastElement().getTime()) {
            this.status = STATUS.DOWN; // No more data available for future ticks
        }
    }

    
    public STATUS getStatus() {
        return status;
    }



}
