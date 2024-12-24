package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;

public class TrackedObjectsEvent implements Event<TrackedObject>{
    // we need to implement in the futare

    private final TrackedObject trackedObject;

    public TrackedObjectsEvent(TrackedObject trackedObject){
        this.trackedObject = trackedObject;
    }

    public TrackedObject getTrackedObject(){
        return trackedObject;
    }

}
