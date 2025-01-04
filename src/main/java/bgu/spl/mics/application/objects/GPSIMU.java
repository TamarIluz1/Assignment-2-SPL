package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents the robot's GPS and IMU system.
 * Provides information about the robot's position and movement.
 */
public class GPSIMU {
    int currentTick;
    STATUS status;
    ArrayList<Pose> poseList;

    public GPSIMU(ArrayList<Pose> PoseList){
        currentTick = 0;
        this.poseList = PoseList;
        this.status = STATUS.UP;

    }

    public Pose getCurrentPose() {
        // Find the pose that matches the current tick, default to the last known pose if not found
        for (Pose pose : poseList) {
            if (pose.getTime() == currentTick) {
                return pose;
            }
        }
        return null;
    }

    public void setCurrentTick(int tick) {
        this.currentTick = tick;
        // Optional: Check if currentTick exceeds available data and adjust status
        if (!poseList.isEmpty() && tick > poseList.get(poseList.size() - 1).getTime()) {
            this.status = STATUS.DOWN; // No more data available for future ticks
        }
    }

    
    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status){
        this.status = status;
    }

     @Override
    public String toString() {
        return "GPSIMU{" +
               "currentTick=" + currentTick +
               ", status=" + status +
               ", currentPose=" + getCurrentPose() +
               '}';
    }



}
