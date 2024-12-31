package bgu.spl.mics.application.objects;
import java.util.Vector;
/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
   
    Vector<LandMark> landmarks;// changed from [] array to Vector bt tamar 31/12
    Vector<Pose> poses;

    private FusionSlam() {
        this.landmarks = new Vector<>();
        this.poses = new Vector<>();

    }
     // Singleton instance holder
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    /**
     * Adds a new pose to the system.
     *
     * @param pose The pose to add.
     */
    public synchronized void addPose(Pose pose) {
        poses.add(pose);
    }

    /**
     * Retrieves all poses in the system.
     *
     * @return A copy of the vector of poses.
     */
    public synchronized Vector<Pose> getPoses() {
        return new Vector<>(poses); // Return a copy for thread safety
    }

    /**
     * Adds a new landmark to the system.
     *
     * @param landmark The landmark to add.
     */
    public synchronized void addLandmark(LandMark landmark) {
        landmarks.add(landmark);
    }

    /**
     * Retrieves all landmarks in the system.
     *
     * @return A copy of the vector of landmarks.
     */
    public synchronized Vector<LandMark> getLandmarks() {
        return new Vector<>(landmarks); // Return a copy for thread safety
    }

    /**
     * Finds a landmark by its ID.
     *
     * @param id The ID of the landmark.
     * @return The landmark with the specified ID, or null if not found.
     */
    public synchronized LandMark findLandmarkById(String id) {
        for (LandMark landmark : landmarks) {
            if (landmark.getId().equals(id)) {
                return landmark;
            }
        }
        return null;
    }

    public void addOrUpdateLandmark(String id, String newDescription, Vector<CloudPoint> newCoordinates) {
        for (LandMark landmark : landmarks) {
            if (landmark.getId().equals(id)) {
                // Update the landmark properties
                landmark.setCoordinates(newCoordinates);
                return; // Exit after updating
            }
        }
        // If the landmark was not found, add it
        LandMark newLandmark = new LandMark(id, newDescription, newCoordinates);
        addLandmark(newLandmark);

    }
    
    public int getNumLandmarks() {
        return landmarks.size();
    }


    
}
