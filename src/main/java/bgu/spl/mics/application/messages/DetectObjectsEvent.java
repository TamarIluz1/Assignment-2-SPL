package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean> {
    @Override
    public void complete(Boolean result) {
        if (future != null) {
            future.resolve(result);
        }
    }
    private final int tickTime;
    private final StampedDetectedObjects objectDetails;
    private final Future<Boolean> future;

    // Constructor
    public DetectObjectsEvent(int tickTime, StampedDetectedObjects objectDetails) {
        this.tickTime = tickTime;
        this.objectDetails = objectDetails;
        this.future = new Future<>();
    }

    // Getter for tick time
    public int getTickTime() {
        return tickTime;
    }

    // Getter for object details
    public StampedDetectedObjects getObjectDetails() {
        return objectDetails;
    }

    // Getter for the future
    public Future<Boolean> getFuture() {
        return future;
    }

    // Mark the event as complete

}
