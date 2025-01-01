package bgu.spl.mics.application.objects;


import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;



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
    Object trackedLock;

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
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<StampedCloudPoints>>() {}.getType();
            cloudPointsDB = gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            
            cloudPointsDB = new ArrayList<>();
        }
    }

    
    private LiDarDataBase(String filePath) {
        cloudPointsDB = new ArrayList<>();
        TrackedCounter = 0;
        loadDataFromFile(filePath);
    }


    // the things we need to add to lastTrackedObject in lidarworker
    public ArrayList<StampedCloudPoints> fetchUntilTime(int tickTime){
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
            if (s.getId() == id & s.getTime() == tickTime){
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
        synchronized(trackedLock){
            return (TrackedCounter == cloudPointsDB.size());
        }
        
    }


}