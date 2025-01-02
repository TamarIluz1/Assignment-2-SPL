package bgu.spl.mics.application.objects;

/**
 * DetectedObject represents an object detected by the camera.
 * It contains information such as the object's ID and description.
 */
public class DetectedObject {

    String id;
    String description;

    public DetectedObject(String id, String description) {
        this.id = id;
        this.description = description;
    }

    // Getters and setters (if needed)
    public String getId() {
        return id;
    }

    public String getDescripition() {
        return description;
    }

    
}
