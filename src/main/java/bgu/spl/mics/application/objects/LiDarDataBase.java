package bgu.spl.mics.application.objects;


import java.util.ArrayList;



/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance = new LiDarDataBase();
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
        return LiDarDataBaseHolder.instance;
    }

    
    private LiDarDataBase() {
        cloudPointsDB = new ArrayList<>();
        TrackedCounter = 0;
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