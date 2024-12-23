package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.DetectedObject;

public class DetectObjectsEvent implements Event<DetectedObject>{    
    // we need to implement in the futare

    private final String objectId;
    private final String objectDetails;

    public DetectObjectsEvent(String objectId, String objectDetails) {
        this.objectId = objectId;
        this.objectDetails = objectDetails;
    }

    public String getObjectId() {
        return objectId;
    }

    public String getObjectDetails() {
        return objectDetails;
    }

}
