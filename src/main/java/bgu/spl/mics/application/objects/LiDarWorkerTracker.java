package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.messages.DetectObjectsEvent;

import java.util.Vector;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private int id;
    private int frequency;
    private STATUS status;
    private Vector<TrackedObject> lastTrackedObjects;
    private LiDarDataBase db;// can we delete this? Tamar 31/12
    // we need this to relate to the db. but we can implement differently , noam
    private Vector<DetectObjectsEvent> eventsRecieved;


    public LiDarWorkerTracker(int id, int frequency){
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new Vector<>();
        db = LiDarDataBase.getInstance("path_to_lidar_data_file.json");
        eventsRecieved = new Vector<>();
        
    }

    public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }
    
    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public void addLastTrackedObject(TrackedObject e){
        lastTrackedObjects.add(e);
    }
    public Vector<TrackedObject> getLastTrackedObjects(){
        return lastTrackedObjects;
    }


    public void addNewDetectEvent(DetectObjectsEvent e){
        eventsRecieved.add(e);
    }

    public Vector<DetectObjectsEvent> getEventsRecieved(){
        return eventsRecieved;
    }


    public Vector<StampedCloudPoints> getNewCloudPointsUntilTime(int detectionTime){
        return db.fetchUntilTime(detectionTime);// i fixed this line 31/12 Tamar 
        // it was missing a return statement
    }

    public boolean isFinished(){
        return db.isFinishedTracking();
    }

    public void handleProcessedDetected(Vector<DetectObjectsEvent> events){
        for (DetectObjectsEvent e : events){
            eventsRecieved.remove(e);
        }
    }

    public void reportTracked(){
        db.reportTracked();
    }



}

