package bgu.spl.mics.application.objects;
import java.util.ArrayList;

import bgu.spl.mics.application.messages.DetectObjectsEvent;

/**
 * LiDarWorkerTracker is responsible for managing a LiDAR worker.
 * It processes DetectObjectsEvents and generates TrackedObjectsEvents by using data from the LiDarDataBase.
 * Each worker tracks objects and sends observations to the FusionSlam service.
 */
public class LiDarWorkerTracker {
    private final int id;
    private final int frequency;
    private STATUS status;
    private final ArrayList<TrackedObject> lastTrackedObjects;
    private final LiDarDataBase db;// can we delete this? Tamar 31/12
    // we need this to relate to the db. but we can implement differently , noam
    private final ArrayList<DetectObjectsEvent> eventsRecieved;


    public LiDarWorkerTracker(int id, int frequency,String pathToDataFile) {// added pathToDataFile Tamar 31/12
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new ArrayList<>();
        db = LiDarDataBase.getInstance(pathToDataFile);
        eventsRecieved = new ArrayList<>();
        
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
    public ArrayList<TrackedObject> getLastTrackedObjects(){
        return lastTrackedObjects;
    }


    public void addNewDetectEvent(DetectObjectsEvent e){
        eventsRecieved.add(e);
    }

    public ArrayList<DetectObjectsEvent> getEventsRecieved(){
        return eventsRecieved;
    }


    public ArrayList<StampedCloudPoints> getNewCloudPointsByTime(int detectionTime){
        return db.fetchByTime(detectionTime);// i fixed this line 31/12 Tamar 
        // it was missing a return statement
    }

    public boolean isFinished(){
        return db.isFinishedTracking();
    }

    public void handleProcessedDetected(ArrayList<DetectObjectsEvent> events){
        for (DetectObjectsEvent e : events){
            eventsRecieved.remove(e);
        }
    }

    public void reportTracked(){
        db.reportTracked();
        StatisticalFolder.getInstance().incrementTrackedObjects(1);
    }



}

