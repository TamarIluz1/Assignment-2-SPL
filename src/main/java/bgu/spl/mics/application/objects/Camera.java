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

}
