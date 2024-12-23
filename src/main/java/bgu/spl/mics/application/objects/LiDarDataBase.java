package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {

    private static class LiDarDataBaseHolder {
        private static LiDarDataBase instance = new LiDarDataBase();
    }

    private Vector<StampedCloudPoints> cloudPoints;// i added this line

    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     * @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(String filePath) {
        return LiDarDataBaseHolder.instance;
    }

    // add private meadod to singleton class
    private LiDarDataBase() {
        cloudPoints = new Vector<>();
    }


}
