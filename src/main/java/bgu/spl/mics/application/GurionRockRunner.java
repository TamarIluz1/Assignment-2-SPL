package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.Comparator;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

//import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.objects.STATUS;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Paths;

// import bgu.spl.mics.application.objects.Camera;
// import bgu.spl.mics.application.objects.FusionSlam;

import bgu.spl.mics.application.objects.Pose;

// import bgu.spl.mics.MessageBusImpl;
// import bgu.spl.mics.MicroService;
// import bgu.spl.mics.application.objects.STATUS;
 import bgu.spl.mics.application.services.CameraService;
 import bgu.spl.mics.application.services.FusionSlamService;
 import bgu.spl.mics.application.services.LiDarService;
 import bgu.spl.mics.application.services.PoseService;
 import bgu.spl.mics.application.services.TimeService;
 //import bgu.spl.mics.example.ServiceCreator;


// import com.google.gson.reflect.TypeToken;
// import java.lang.reflect.Type;
// import java.util.List;




/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */

    private static final Gson gson = new Gson();
    private static StatisticalFolder statistics;
    private static FusionSlam fusionSlam;
    
    public static void main(String[] args) {
        System.out.println("Hello World!");
        // TODO: Parse configuration file.
        // TODO: Initialize system components and services. also TODO add thread to each created microService
        // TODO: Start the simulation.

        // if (args.length < 1) {
        //     System.out.println("Configuration file path must be provided as the first command-line argument.");
        //     System.exit(1);
        // }
    
        

        //String configFilePath = args[0];
        // String configFilePath = "example_input_2\\configuration_file.json";
        // boolean simulationSuccessful = true;

        // try {
        //     statistics = new StatisticalFolder();
        //     fusionSlam = FusionSlam.getInstance();
        //     SystemConfig config = initializeSystem(configFilePath);
        //     startServices(config);

        //     // Simulation logic here...

        //     // Write successful output
        //     writeSuccessOutput("Simulation completed successfully", capturePoses(), statistics);

        // } catch (Exception e) {
        //     simulationSuccessful = false;
        //     statistics.incrementErrors();

        //     writeErrorOutput(e.getMessage(), null, capturePoses(), statistics);
        // }
    



        // Vector<Camera> cameras = new Vector<>();
        // Vector<LiDarWorkerTracker> LiDarWorkers = new Vector<>();
        // config_parser(args);
        // camera_data_parser(args);
        // lotem - send the amount of each sensor to slam in order to follow terminations
       // init the thread of timeservice last (according to lotem)
    }

    /**
    * Captures the current poses from the GPSIMU.
    * @return A Vector of Pose objects.
    */
    private static Vector<Pose> capturePoses() {
        return fusionSlam.getPoses();
    }


    // private static void writeErrorOutput(String errorMessage, String faultySensor, Vector<Pose> poses, StatisticalFolder statistics) {
    //     String fileName = "error.json";
    //     JsonObject errorOutput = new JsonObject();

    //     // Add error details
    //     errorOutput.addProperty("error", errorMessage);
    //     errorOutput.addProperty("faultySensor", faultySensor != null ? faultySensor : "Unknown");

    //     // Add last frames
    //     JsonObject lastCamerasFrame = new JsonObject();
    //     JsonObject lastLiDarWorkerTrackersFrame = new JsonObject();
    //     errorOutput.add("lastCamerasFrame", lastCamerasFrame);
    //     errorOutput.add("lastLiDarWorkerTrackersFrame", lastLiDarWorkerTrackersFrame);

    //     // Add poses
    //     errorOutput.add("poses", gson.toJsonTree(poses));

    //     // Add statistics
    //     JsonObject statisticsJson = new JsonObject();
    //     statisticsJson.addProperty("systemRuntime", statistics.getSystemRuntime());
    //     statisticsJson.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
    //     statisticsJson.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
    //     statisticsJson.addProperty("numLandmarks", FusionSlam.getInstance().getNumLandmarks());
    //     statisticsJson.add("landMarks", gson.toJsonTree(FusionSlam.getInstance().getLandmarks()));
    //     errorOutput.add("statistics", statisticsJson);

    //     // Write the error JSON to file
    //     try (FileWriter writer = new FileWriter(fileName)) {
    //         writer.write(errorOutput.toString());
    //         System.out.println("Error output written to " + fileName);
    //     } catch (IOException e) {
    //         System.err.println("Failed to write error output file: " + e.getMessage());
    //     }
    // }


    // private static void writeSuccessOutput(String message, Vector<Pose> poses, StatisticalFolder statistics) {
    //     String fileName = "success.json";
    //     JsonObject successOutput = new JsonObject();

    //     // Add success message
    //     successOutput.addProperty("message", message);

    //     // Add poses
    //     successOutput.add("poses", gson.toJsonTree(poses));

    //     // Add statistics
    //     JsonObject statisticsJson = new JsonObject();
    //     statisticsJson.addProperty("systemRuntime", statistics.getSystemRuntime());
    //     statisticsJson.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
    //     statisticsJson.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
    //     statisticsJson.addProperty("numLandmarks", FusionSlam.getInstance().getNumLandmarks());
    //     statisticsJson.add("landMarks", gson.toJsonTree(FusionSlam.getInstance().getLandmarks()));
    //     successOutput.add("statistics", statisticsJson);

    //     // Write the success JSON to file
    //     try (FileWriter writer = new FileWriter(fileName)) {
    //         writer.write(successOutput.toString());
    //         System.out.println("Success output written to " + fileName);
    //     } catch (IOException e) {
    //         System.err.println("Failed to write success output file: " + e.getMessage());
    //     }
    // }


    /**
     * Initializes the system by loading configurations from the specified file path.
     * @param configFilePath The path to the JSON configuration file.
     * @return A fully configured SystemConfig instance containing all system components.
     * @throws Exception Throws if there is an issue reading or parsing the configuration file.
     */
    private static SystemConfig initializeSystem(String configFilePath) throws Exception {
            JsonObject config = parseJsonConfig(configFilePath);
            
            Map<Integer, Camera> cameras = loadCameras(config.getAsJsonObject("Cameras"));
            Map<Integer, LiDarWorkerTracker> lidars = loadLiDars(config.getAsJsonObject("LiDarWorkers"));
            GPSIMU gpsimu = new GPSIMU(loadPoses(config.get("poseJsonFile").getAsString()));

            long tickTime = config.get("TickTime").getAsLong();
            long duration = config.get("Duration").getAsLong();

            return new SystemConfig(cameras, lidars, gpsimu, tickTime, duration);
        }


    /**
     * Parses the JSON configuration file.
     * @param filePath The file path to the JSON configuration file.
     * @return A JsonObject parsed from the configuration file.
     * @throws Exception If the file cannot be read or parsed.
     */
    private static JsonObject parseJsonConfig(String filePath) throws Exception {
        FileReader reader = new FileReader(Paths.get(filePath).toAbsolutePath().toString());
        return gson.fromJson(reader, JsonObject.class);
    }


    /**
     * Loads camera configurations from the provided JSON object.
     * @param cameraJson The JsonObject containing the camera configurations.
     * @return A map of camera ID to Camera object.
     * @throws Exception If there is an error during parsing.
     */
     private static Map<Integer, Camera> loadCameras(JsonObject cameraJson) throws Exception {
        String path = Paths.get(cameraJson.get("camera_datas_path").getAsString()).toAbsolutePath().toString();
        FileReader reader = new FileReader(path);
        JsonArray cameraConfigs = cameraJson.getAsJsonArray("CamerasConfigurations");
        Map<Integer, Camera> cameras = new HashMap<>();
        for (int i = 0; i < cameraConfigs.size(); i++) {
            JsonObject camConfig = cameraConfigs.get(i).getAsJsonObject();
            Camera camera = gson.fromJson(camConfig, Camera.class);

            // Load StampedDetectedObjects for the camera
            String detectedObjectsPath = Paths.get(camConfig.get("detected_objects_path").getAsString()).toAbsolutePath().toString();
            FileReader detectedObjectsReader = new FileReader(detectedObjectsPath);
            Vector<StampedDetectedObjects> stampedDetectedObjects = gson.fromJson(detectedObjectsReader, new TypeToken<Vector<StampedDetectedObjects>>() {}.getType());
            camera.setDetectedObjectsList(new Vector<>(stampedDetectedObjects));

            cameras.put(camera.getId(), camera);
        }
        return cameras;
    }


    /**
     * Loads LiDar worker tracker configurations from the provided JSON object.
     * @param lidarJson The JsonObject containing the LiDar configurations.
     * @return A map of LiDar worker tracker ID to LiDarWorkerTracker object.
     * @throws Exception If there is an error during parsing.
     */
    private static Map<Integer, LiDarWorkerTracker> loadLiDars(JsonObject lidarJson) throws Exception {
        String path = Paths.get(lidarJson.get("lidars_data_path").getAsString()).toAbsolutePath().toString();
        FileReader reader = new FileReader(path);
        JsonArray lidarConfigs = lidarJson.getAsJsonArray("LidarConfigurations");
        Map<Integer, LiDarWorkerTracker> lidars = new HashMap<>();
        for (int i = 0; i < lidarConfigs.size(); i++) {
            JsonObject lidConfig = lidarConfigs.get(i).getAsJsonObject();
            LiDarWorkerTracker lidar = gson.fromJson(lidConfig, LiDarWorkerTracker.class);
            lidars.put(lidar.getId(), lidar);
        }
        return lidars;
    }

    /**
     * Loads poses from the specified file path into a Vector of Pose objects.
     * @param poseFilePath The path to the file containing pose data.
     * @return A Vector of Pose objects.
     * @throws Exception If there is an error during file reading or parsing.
     */
    private static Vector<Pose> loadPoses(String poseFilePath) throws Exception {
        FileReader reader = new FileReader(Paths.get(poseFilePath).toAbsolutePath().toString());
        Type type = new TypeToken<Vector<Pose>>(){}.getType();
        return gson.fromJson(reader, type);
    }


    /**
     * Starts all the services necessary for the simulation, each in its own thread.
     * @param config The SystemConfig containing all initialized components.
     */
    private static void startServices(SystemConfig config) {
        config.cameras.forEach((id, camera) -> {
            CameraService service = new CameraService(camera);
            new Thread(service).start();
        });

        config.lidars.forEach((id, lidar) -> {
            LiDarService service = new LiDarService(lidar);
            new Thread(service).start();
        });

        PoseService poseService = new PoseService(config.gpsimu);
        new Thread(poseService).start();

        TimeService timeService = new TimeService(config.tickTime, config.duration);
         new Thread(timeService).start();
         
        FusionSlam fusionSlam = FusionSlam.getInstance();
        FusionSlamService fusionSlamService = new FusionSlamService(fusionSlam);
        new Thread(fusionSlamService).start();
    }

     /**
     * A configuration class that holds all system components needed for the simulation.
     */
    static class SystemConfig {
        Map<Integer, Camera> cameras;
        Map<Integer, LiDarWorkerTracker> lidars;
        GPSIMU gpsimu;
;
        long tickTime;
        long duration;

        SystemConfig(Map<Integer, Camera> cameras, Map<Integer, LiDarWorkerTracker> lidars, GPSIMU gpsimu, long tickTime, long duration) {
            this.cameras = cameras;
            this.lidars = lidars;
            this.gpsimu = gpsimu;
            this.tickTime = tickTime;
            this.duration = duration;
        }
    }
    


    // public static Camera getCamera(int Id){ // TODO- FROM ARRAY OF CAMERAS
    //     return new Camera(Id, 0, STATUS.UP);
    // }
}


