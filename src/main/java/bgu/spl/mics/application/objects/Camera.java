package bgu.spl.mics.application.objects;
import java.io.FileReader;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a camera sensor on the robot.
 * Responsible for detecting objects in the environment.
 */
public class Camera {
    final int id;
    int  frequency;
    STATUS status;
    Vector<StampedDetectedObjects> detectedObjectsList;
    Iterator<StampedDetectedObjects> detectedObjectsIterator;

    public static Camera fromJson(int id, int frequency) { //FACTORY!
        return new Camera(id, frequency, STATUS.UP); 
    }

    public Camera(int id, int frequency, STATUS status) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new Vector<>();
        detectedObjectsIterator = detectedObjectsList.iterator();
    }

     // Getters and setters (if needed)
     public int getId() {
        return id;
    }

    public static Camera getCamera(int Id){ // TODO
        return new Camera(Id, 0, STATUS.UP);
    }

    public int getFrequency() {
        return frequency;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public Vector<StampedDetectedObjects> getDetectedObjectsList() {
        return detectedObjectsList;
    }

    // to be used in the parsing of the json file camera_data.json
    public void setDetectedObjectsList(Vector<StampedDetectedObjects> detectedObjectsList) { 
        this.detectedObjectsList = detectedObjectsList;
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


// json files will be vector<stampedDetectedObjects>



