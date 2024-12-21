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
}
