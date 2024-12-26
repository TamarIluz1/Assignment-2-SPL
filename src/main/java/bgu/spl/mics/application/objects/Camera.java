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

    // TODO: Define fields and methods.
    final int id;
    int  frequency;
    STATUS status;
    Vector<StampedDetectedObjects> detectedObjectsList;

    public static Camera fromJson(int id, int frequency) { //FACTORY!
        return new Camera(id, frequency, STATUS.UP); 
    }

    public Camera(int id, int frequency, STATUS status) {
        this.id = id;
        this.frequency = frequency;
        this.status = status;
        this.detectedObjectsList = new Vector<>();
    }

    // public DetectedObject processDetectedObject(String id, String description) {
    //     DetectedObject detectedObject = new DetectedObject(id, description);
    //     Vector<DetectedObject> detectedObjects = new Vector<>();
    //     detectedObjects.add(detectedObject);
    //     detectedObjectsList.add(new StampedDetectedObjects(detectedObjects));
    //     return detectedObject;
    // }

     // Getters and setters (if needed)
     public int getId() {
        return id;
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

    public Vector<DetectedObject> getDetectedObjectsByTime(int discoveryTime) {
        Vector<DetectedObject> matchingObjects = new Vector<>();
        for (StampedDetectedObjects stampedDetectedObjects : detectedObjectsList) {
            if (stampedDetectedObjects.getTimestamp() == discoveryTime) {
                matchingObjects.addAll(stampedDetectedObjects.getDetectedObjects());
            }
        }
        return matchingObjects; 
    }



// json files will be vector<stampedDetectedObjects>

public StampedDetectedObjects getNextStampedDetectedObjects(int currentTime) {
    // Iterate through the detected objects list
    for (StampedDetectedObjects stampedDetectedObjects : detectedObjectsList) {
        // Find the first set of objects with a timestamp greater than or equal to the current time
        if (stampedDetectedObjects.getTimestamp() >= currentTime) {
            return stampedDetectedObjects;
        }
    }
    // Return null if no matching timestamp is found
    return null;
}}

