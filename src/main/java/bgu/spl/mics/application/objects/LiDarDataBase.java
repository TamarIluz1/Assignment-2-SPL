package bgu.spl.mics.application.objects;


import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance;
    }

    private ArrayList<StampedCloudPoints> cloudPointsDB;// i added this line
    private int TrackedCounter;
    final Object trackedLock = new Object();

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        if (LiDarDataBaseHolder.instance == null) {
            LiDarDataBaseHolder.instance = new LiDarDataBase(filePath);
        }
        return LiDarDataBaseHolder.instance;
    }


    private void loadDataFromFile(String filePath) {
        try  {

            cloudPointsDB = parseLidarData(filePath);
        } catch (IOException e) {
            System.err.println("Error loading LiDAR data from file: " + e.getMessage());

        }
    }

    
    private LiDarDataBase(String filePath) {
        cloudPointsDB = new ArrayList<>();
        TrackedCounter = 0;
        loadDataFromFile(filePath);
    }


    // the things we need to add to lastTrackedObject in lidarworker
    public ArrayList<StampedCloudPoints> fetchByTime(int tickTime){
        ArrayList<StampedCloudPoints> toReturn = new ArrayList<>();
        for (StampedCloudPoints s: cloudPointsDB){
            if (s.getTime() == tickTime){
                toReturn.add(s);
            }
        }
        return toReturn;
    }

    public StampedCloudPoints fetchCloudPoints(int tickTime, String id){
        for (StampedCloudPoints s : cloudPointsDB){
            if (s.getId().equals(id) & s.getTime() == tickTime){
                return s;
            }
        }
        return null;
    }

    public void reportTracked(){
        synchronized(trackedLock){
            TrackedCounter++;
        }
        
    }

    public boolean isFinishedTracking(){
        synchronized (trackedLock) {
            boolean isFinished = (TrackedCounter == cloudPointsDB.size());
            return isFinished;
        }
    
        
    }


    private static ArrayList<StampedCloudPoints> parseLidarData(String filePath) throws IOException {
        JsonArray jsonArray;
        try (FileReader reader = new FileReader(filePath)) {
            jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
        }

        ArrayList<StampedCloudPoints> stampedCloudPointsList = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            String id = jsonObject.get("id").getAsString();
            int time = jsonObject.get("time").getAsInt();
            JsonArray cloudPointsArray = jsonObject.getAsJsonArray("cloudPoints");

            ArrayList<CloudPoint> cloudPoints = new ArrayList<>();
            for (JsonElement pointElement : cloudPointsArray) {
                JsonArray pointArray = pointElement.getAsJsonArray();
                if (pointArray.size() >= 2) {
                    double x = pointArray.get(0).getAsDouble();
                    double y = pointArray.get(1).getAsDouble();
                    cloudPoints.add(new CloudPoint(x, y));
                }
            }

            stampedCloudPointsList.add(new StampedCloudPoints(id, time, cloudPoints));
        }

        return stampedCloudPointsList;
    }


}