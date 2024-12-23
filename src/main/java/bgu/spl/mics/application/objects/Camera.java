package bgu.spl.mics.application.objects;
import java.util.Vector;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {

    // TODO: Define fields and methods.
    int id;
    int  frequency;
    STATUS status;
    Vector<StampedDetectedObjects> detectedObjectsList;

    public Camera(int id, int frequency, STATUS status) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new Vector<>();
    }

    public DetectedObject processDetectedObject(String id, String description) {
        DetectedObject detectedObject = new DetectedObject(id, description);
        Vector<DetectedObject> detectedObjects = new Vector<>();
        detectedObjects.add(detectedObject);
        detectedObjectsList.add(new StampedDetectedObjects(detectedObjects));
        return detectedObject;
    }

     // Getters and setters (if needed)
     public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public Vector<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

}
