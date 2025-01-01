package bgu.spl.mics.application.objects;
import java.util.ArrayList;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    String id;
    int time;
    String description;
    ArrayList<CloudPoint> coordinates;// changed form array to this

    public TrackedObject(String id, int time, String description, ArrayList<CloudPoint> coordinates){
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

    public ArrayList<CloudPoint> getCloudPoint(){
        return coordinates;
    }

    public String getDescription() {
        return description;
    }

    
}