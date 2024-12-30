package bgu.spl.mics.application.objects;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    String id;
    int time;
    String description;
    Vector<CloudPoint> coordinates;// changed form array to this

    public TrackedObject(String id, int time, String description, Vector<CloudPoint> coordinates){
        this.id = id;
        this.time = time;
        this.description = description;
        this.coordinates = coordinates;
    }

    public int getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public Vector<CloudPoint> getCloudPoint(){
        return coordinates;
    }
}