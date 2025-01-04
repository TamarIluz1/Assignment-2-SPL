package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.ArrayList;


public class TrackedObjectsEvent implements Event<Boolean>{
    // we need to implement in the future


    private final ArrayList<TrackedObject> trackedObjects; 
    private final int tickTime;
    
    public TrackedObjectsEvent(ArrayList<TrackedObject> trackedObjects, int tickTime){
        this.trackedObjects = trackedObjects;
        this.tickTime = tickTime;
    }   

    public ArrayList<TrackedObject> getTrackedObject(){
        return trackedObjects;
    }

    public int getTime() {
        return tickTime;
    }

    @Override
    public String toString() {
        return "TrackedObjectsEvent{" +
                "trackedObjects=" + (trackedObjects != null ? trackedObjects.toString() : "null") +
                ", tickTime=" + tickTime +
                '}';
    }





}
