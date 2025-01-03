package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.Iterator;

;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    int id;
    int  frequency;
    STATUS status;
    ArrayList<StampedDetectedObjects> detectedObjectsList;
    Iterator<StampedDetectedObjects> detectedObjectsIterator;

    public static Camera fromJson(int id, int frequency) { //FACTORY!
        return new Camera(id, frequency, STATUS.UP); 
    }

    public Camera(int id, int frequency, STATUS status) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new ArrayList<>();
        detectedObjectsIterator = detectedObjectsList.iterator();
    }

     // Getters and setters (if needed)
     public int getId() {
        return id;
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        synchronized(this) {
            return status;
        }
    }

    public void setStatus(STATUS status) {
        synchronized(this) {
            this.status = status;
        }
    }

    public ArrayList<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    // to be used in the parsing of the json file camera_data.json
    public void setDetectedObjectsList(ArrayList<StampedDetectedObjects> detectedObjectsList) { 
        this.detectedObjectsList = detectedObjectsList;
        detectedObjectsIterator = detectedObjectsList.iterator();
    }



    public StampedDetectedObjects getNextDetectedObjects() {
        if (detectedObjectsIterator.hasNext()) {
            return detectedObjectsIterator.next();
        }
        else{
            return null;
        }
    }
}





