package bgu.spl.mics.application.objects;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

import bgu.spl.mics.application.objects.CloudPoint;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance = new LiDarDataBase();
    }

    private Vector<StampedCloudPoints> cloudPointsDB;// i added this line

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        return LiDarDataBaseHolder.instance;
    }

    // add private meadod to singleton class TODO explain to noam :)
    private LiDarDataBase() {
        cloudPointsDB = new Vector<>();
    }



    public void parseLidarData(String filePath) {
        // TODO implement
        try{
            Gson gson = new Gson();
            FileReader fileReader = new FileReader(filePath);
            Type ObjectsDetected = new TypeToken<StampedCloudPoints>(){}.getType(); 
            StampedCloudPoints stampedCloudPoints = gson.fromJson(fileReader, ObjectsDetected);           
            cloudPointsDB.add(stampedCloudPoints);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Vector<StampedCloudPoints> fetchByTime(int tickTime){
        // returns only stampedObjects with the time tickTime!! 
        //TODO
        return new Vector<>();
    }


}
