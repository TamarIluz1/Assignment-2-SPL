package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.objects.LiDarDataBase;
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

    public LiDarWorkerTracker(int id, int frequency){
        this.id = id;
        this.frequency = frequency;
        this.status = STATUS.UP;
        this.lastTrackedObjects = new Vector<>();
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

    public Vector<TrackedObject> getLastTrackedObjects(){
        return lastTrackedObjects;
    }

    public Vector<CloudPoint> getCoorCloudPoints(String objectID){
        // TODO implement
        // psuedo code: for each TrackedObject in lastTrackedObjects: to get the coordinates of the object from the dataBase
        return  null;

    }

    public void fetchByTime(int tickTime){
        // add to the database the objects detected at time ticktime
    }



}

