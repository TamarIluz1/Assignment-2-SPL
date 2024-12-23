package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {

    // TODO: Define fields and methods.
    String id;
    String descripition;

    public DetectedObject(String id, String descripition) {
        this.id = id;
        this.descripition = descripition;
    }

    // Getters and setters (if needed)
    public String getId() {
        return id;
    }

    public String getDescripition() {
        return descripition;
    }

    
}
