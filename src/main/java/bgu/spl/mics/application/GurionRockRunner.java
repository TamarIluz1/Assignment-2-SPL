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
import bgu.spl.mics.application.objects.StampedCloudPoints;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
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


import bgu.spl.mics.application.configuration.Configuration;
import bgu.spl.mics.application.configuration.CameraConfig;
import bgu.spl.mics.application.configuration.CameraConfiguration;
import bgu.spl.mics.application.configuration.LiDarConfig;
import bgu.spl.mics.application.configuration.LidarConfiguration;

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

        if (args.length < 1) {
            System.out.println("Configuration file path must be provided as the first command-line argument.");
            System.exit(1);
        }
        String configFilePath = args[0];

        try {
            Configuration config = parseConfiguration(configFilePath);
            String cameraDataPath = getAbsolutePath(configFilePath, config.getCameras().getCameraDatasPath());
            String lidarDataPath = getAbsolutePath(configFilePath, config.getLiDarWorkers().getLidarsDataPath());
            String poseDataPath = getAbsolutePath(configFilePath, config.getPoseJsonFile());

            // Load data
            Vector<StampedDetectedObjects> cameraData = parseCameraData(cameraDataPath);
            Vector<StampedCloudPoints> lidarData = parseLidarData(lidarDataPath);
            Vector<Pose> poseData = parsePoseData(poseDataPath);

            // Initialize and start services
            SystemConfig systemConfig = new SystemConfig(cameraData, lidarData, poseData, config.getTickTime(), config.getDuration());
            startServices(systemConfig);
            System.out.println("Simulation started successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to start the simulation: " + e.getMessage());
        }


        // Vector<Camera> cameras = new Vector<>();
        // Vector<LiDarWorkerTracker> LiDarWorkers = new Vector<>();
        // config_parser(args);
        // camera_data_parser(args);
        // lotem - send the amount of each sensor to slam in order to follow terminations
       // init the thread of timeservice last (according to lotem)
    }


    private static void startServices(SystemConfig config) {
        config.cameras.values().forEach(camera -> {
            CameraService service = new CameraService(camera);
            new Thread(service).start();
        });

        config.lidars.values().forEach(lidar -> {
            LiDarService service = new LiDarService(lidar);
            new Thread(service).start();
        });

        // Start GPSIMU service if required
        PoseService poseService = new PoseService(config.gpsimu);
        new Thread(poseService).start();

        // Start any other services like TimeService
        TimeService timeService = new TimeService(config.tickTime, config.duration);
        new Thread(timeService).start();
        
        // Example FusionSlam service
        FusionSlamService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        new Thread(fusionSlamService).start();
    }

    public static String getAbsolutePath(String configFilePath, String relativePath) {
        Path configPath = Paths.get(configFilePath);
        Path resolvedPath = configPath.getParent().resolve(relativePath).normalize();
        return resolvedPath.toString();
    }

    private static Configuration parseConfiguration(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Configuration.class);
        }
    }

    public static CameraData parseCameraData(String filePath) throws IOException {
        Gson gson = new Gson();
        Type dataType = new TypeToken<CameraData>(){}.getType();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, dataType);
        }
    }

    public static Vector<StampedCloudPoints> parseLidarData(String filePath) throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<Vector<StampedCloudPoints>>(){}.getType();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, listType);
        }
    }

    public static Vector<Pose> parsePoseData(String filePath) throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<Vector<Pose>>(){}.getType();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, listType);
        }
    }

    public static class SystemConfig {
        Map<Integer, Camera> cameras;
        Map<Integer, LiDarWorkerTracker> lidars;
        GPSIMU gpsimu;
        long tickTime;
        long duration;

        public SystemConfig(Vector<StampedDetectedObjects> cameraData, Vector<StampedCloudPoints> lidarData, Vector<Pose> poseData, int tickTime, int duration) {
            this.tickTime = tickTime;
            this.duration = duration;
            // Initialize cameras and LiDarWorkers based on your specific requirements
            this.cameras = new HashMap<>();
            this.lidars = new HashMap<>();
            // Assuming single GPSIMU for simplicity
            this.gpsimu = new GPSIMU(poseData);
        }
    }


    public class CameraData {
        private Map<String, List<StampedDetectedObjects>> cameras;

        public Map<String, List<StampedDetectedObjects>> getCameras() {
            return cameras;
        }

        public void setCameras(Map<String, List<StampedDetectedObjects>> cameras) {
            this.cameras = cameras;
        }
    }

    public void distributeCameraData(Map<String, Camera> cameraMap, CameraData cameraData) {
        cameraData.getCameras().forEach((key, stampedObjects) -> {
            if (cameraMap.containsKey(key)) {
                cameraMap.get(key).setDetectedObjectsList(new Vector<>(stampedObjects));
            } else {
                System.out.println("No camera found for key: " + key);
            }
        });
    }

    // Method to get or create camera map
    private static Map<String, Camera> getOrCreateCameras(CameraData cameraData) {
        Map<String, Camera> cameraMap = new HashMap<>();
        cameraData.getCameras().keySet().forEach(key -> {
            cameraMap.put(key, new Camera(key,)); // Modify according to how you initialize cameras
        });
        return cameraMap;
    }

    
}



