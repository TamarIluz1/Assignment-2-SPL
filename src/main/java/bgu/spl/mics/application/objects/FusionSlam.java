package bgu.spl.mics.application.objects;
import java.util.Vector;
/**
 * Manages the fusion of sensor data for simultaneous localization and mapping (SLAM).
 * Combines data from multiple sensors (e.g., LiDAR, camera) to build and update a global map.
 * Implements the Singleton pattern to ensure a single instance of FusionSlam exists.
 */
public class FusionSlam {
   
    LandMark[] landmarks;
    Vector<Pose> poses;

    private FusionSlam() {

    }
     // Singleton instance holder
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }
    private static class FusionSlamHolder {
        // TODO: Implement singleton instance logic.
        private static final FusionSlam instance = new FusionSlam();
    }
    

}
