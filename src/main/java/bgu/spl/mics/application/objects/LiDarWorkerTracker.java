package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {

    // TODO: Define fields and methods.
    private int id;
    private int frequency;
    private STATUS status;
    private Vector<TrackedObject> trackedObjectsList;

    public int getFrequency() {
        return frequency;
    }   
}

