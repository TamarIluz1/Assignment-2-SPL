package bgu.spl.mics.application.objects;

/**
 * Represents an object tracked by the LiDAR.
 * This object includes information about the tracked object's ID, description, 
 * time of tracking, and coordinates in the environment.
 */
public class TrackedObject {
    // TODO: Define fields and methods.
    String id;
    int time;
    String description;
    CloudPoint[] coordinates;
}
