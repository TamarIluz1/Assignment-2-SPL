package bgu.spl.mics.application.objects;
import java.util.ArrayList;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private final int time;
    private final ArrayList<DetectedObject> detectedObjects;

    public StampedDetectedObjects(int time ,ArrayList<DetectedObject> detectedObjects) {
        this.time = time;
        this.detectedObjects = new ArrayList<>(detectedObjects);
    }
    public StampedDetectedObjects(ArrayList<DetectedObject> detectedObjects, int time) {
        this.time = time;
        this.detectedObjects = new ArrayList<>(detectedObjects);
    }

    public long getTimestamp() {
        return time;
    }

    public ArrayList<DetectedObject> getDetectedObjects() {
        return new ArrayList<>(detectedObjects);
    }

    @Override
    public String toString() {
        return "StampedDetectedObjects{" +
               "time=" + time +
               ", detectedObjects=" + detectedObjects +
               '}';
    }
}
