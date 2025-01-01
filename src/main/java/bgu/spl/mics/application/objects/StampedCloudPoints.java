package bgu.spl.mics.application.objects;

import java.util.ArrayList;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    private String id; 
    private int time;
    ArrayList<CloudPoint> cloudPoints;// thet saied listof list of type floats

    public String getId(){
        return id;
    }

    public int getTime(){
        return time;
    }

    public ArrayList<CloudPoint> getCloudPoints(){
        return cloudPoints;
    }
}
