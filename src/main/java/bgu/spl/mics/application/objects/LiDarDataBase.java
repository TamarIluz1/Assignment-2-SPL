package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private static LiDarDataBase instance = null;// i added this line
    private Vector<StampedCloudPoints> cloudPoints;// i added this line

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        // TODO: Implement this
        if(instance == null){
            instance = new LiDarDataBase();
        }
        return instance;
    }

    // add private meadod to singleton class
    private LiDarDataBase() {
        cloudPoints = new Vector<>();
    }


}
