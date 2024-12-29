package bgu.spl.mics.application.objects;
import java.util.Vector;

/**
 * Represents objects detected by the camera at a specific timestamp.
 * Includes the time of detection and a list of detected objects.
 */
public class StampedDetectedObjects {
    private final int time;
    private final Vector<DetectedObject> detectedObjects;

    public StampedDetectedObjects(Vector<DetectedObject> detectedObjects) {
        this.time = (int) System.currentTimeMillis();
        this.detectedObjects = new Vector<>(detectedObjects);
    }
    public StampedDetectedObjects(Vector<DetectedObject> detectedObjects, int time) {
        this.time = time;
        this.detectedObjects = new Vector<>(detectedObjects);
    }

    public long getTimestamp() {
        return time;
    }

    public Vector<DetectedObject> getDetectedObjects() {
        return new Vector<>(detectedObjects);
    }
}
