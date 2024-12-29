package bgu.spl.mics.application.messages;


import bgu.spl.mics.Event;
import bgu.spl.mics.Future;

import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean>{    
    // we need to implement in the futare

    private final int tickTime;
    private final StampedDetectedObjects objectDetails;
    private Future<Boolean> future;
    public DetectObjectsEvent(int tickTime, StampedDetectedObjects objectDetails) {
        this.tickTime = tickTime;
        this.objectDetails = objectDetails;
    }

    public int getTickTime() {
        return tickTime;
    }

    public StampedDetectedObjects getObjectDetails() {
        return objectDetails;
    }

    public Future<Boolean> getFuture() {
        return future;
    }

}
