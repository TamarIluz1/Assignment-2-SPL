package bgu.spl.mics.application.objects;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    String id;
    int time;
    String description;
    CloudPoint[] coordinates;

    public TrackedObject(String id, int time, String description, CloudPoint[] coordinates){
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
}