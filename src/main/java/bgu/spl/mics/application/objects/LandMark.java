package bgu.spl.mics.application.objects;
import java.util.Vector;
/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    // TODO: Define fields and methods.
    String id;
    String description;
    Vector<CloudPoint> coordinates;

    public LandMark(String id, String description, Vector<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = new Vector<>(coordinates);
    }

    // Getters and setters (if needed)
    public synchronized String getId() {
        return id;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized Vector<CloudPoint> getCoordinates() {
        return new Vector<>(coordinates);
    }

    public synchronized void setCoordinates(Vector<CloudPoint> coordinates) {
        this.coordinates.clear();
        this.coordinates.addAll(coordinates);
    }

    
}
