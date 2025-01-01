package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.Vector;
/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    String id;
    String description;
    ArrayList<CloudPoint> coordinates;

    public LandMark(String id, String description, ArrayList<CloudPoint> coordinates) {
        this.id = id;
        this.description = description;
        this.coordinates = new ArrayList<>(coordinates);
    }

    // Getters and setters (if needed)
    public synchronized String getId() {
        return id;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized ArrayList<CloudPoint> getCoordinates() {
        return new ArrayList<>(coordinates);
    }

    public synchronized void setCoordinates(ArrayList<CloudPoint> coordinates) {
        this.coordinates.clear();
        this.coordinates.addAll(coordinates);
    }

    
}
