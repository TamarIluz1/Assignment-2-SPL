package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Represents a group of cloud points corresponding to a specific timestamp.
 * Used by the LiDAR system to store and process point cloud data for tracked objects.
 */
public class StampedCloudPoints {
    // TODO: Define fields and methods.
    private String id; //CHANGED BY NOAM 30/12
    private int time;
    Vector<CloudPoint> cloudPoints;// thet saied listof list of type floats

    public String getId(){
        return id;
    }

    public int getTime(){
        return time;
    }
}
