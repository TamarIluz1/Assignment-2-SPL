package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.Vector;
import bgu.spl.mics.Future;


public class TrackedObjectsEvent implements Event<Boolean>{
    // we need to implement in the future


    private final Vector<TrackedObject> trackedObjects; // TODO not sure if we need to use vector- instructions say list
    private final int tickTime;
    
    public TrackedObjectsEvent(Vector<TrackedObject> trackedObjects, int tickTime){
        this.trackedObjects = trackedObjects;
        this.tickTime = tickTime;
    }   

    public Vector<TrackedObject> getTrackedObject(){
        return trackedObjects;
    }

    public int getTickTime() {
        return tickTime;
    }



}
