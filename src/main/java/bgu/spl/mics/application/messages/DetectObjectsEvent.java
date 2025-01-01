package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

public class DetectObjectsEvent implements Event<Boolean> {

    private final int tickTime;
    private final StampedDetectedObjects objectDetails;

    // Constructor
    public DetectObjectsEvent(int tickTime, StampedDetectedObjects objectDetails) {
        this.tickTime = tickTime;
        this.objectDetails = objectDetails;

    }

    // Getter for tick time
    public int getTickTime() {
        return tickTime;
    }

    // Getter for object details
    public StampedDetectedObjects getObjectDetails() {
        return objectDetails;
    }


}
