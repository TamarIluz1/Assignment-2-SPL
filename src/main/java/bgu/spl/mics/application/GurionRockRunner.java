package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
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
        System.out.println("Starting Simulation...");

        if (args.length < 1) {
            System.out.println("Configuration file path must be provided as the first command-line argument.");
            System.exit(1);
        }
        String configFilePath = args[0];

        try {
            // Parse configuration
            Configuration config = parseConfiguration(configFilePath);

            // Resolve paths for data files
            String cameraDataPath = getAbsolutePath(configFilePath, config.getCameras().getCameraDatasPath());
            String lidarDataPath = getAbsolutePath(configFilePath, config.getLiDarWorkers().getLidarsDataPath());
            String poseDataPath = getAbsolutePath(configFilePath, config.getPoseJsonFile());

            // Parse sensor data
            Map<String, ArrayList<StampedDetectedObjects>> cameraData = parseCameraData(cameraDataPath);
            ArrayList<StampedCloudPoints> lidarData = parseLidarData(lidarDataPath);
            ArrayList<Pose> poseData = parsePoseData(poseDataPath);

            // Initialize system configuration
            SystemConfig systemConfig = initializeSystem(config, cameraData, lidarData, poseData);

            // Start services
            startServices(systemConfig);

            System.out.println("Simulation started successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to start the simulation: " + e.getMessage());
        }
    }

    private static String getAbsolutePath(String configFilePath, String relativePath) {
        Path configPath = Paths.get(configFilePath);
        Path resolvedPath = configPath.getParent().resolve(relativePath).normalize();
        return resolvedPath.toString();
    }

    private static Configuration parseConfiguration(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, Configuration.class);
        }
    }

    private static Map<String, ArrayList<StampedDetectedObjects>> parseCameraData(String filePath) throws IOException {
        Type type = new TypeToken<Map<String, ArrayList<StampedDetectedObjects>>>() {}.getType();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, type);
        }
    }

    private static ArrayList<StampedCloudPoints> parseLidarData(String filePath) throws IOException {
        Type type = new TypeToken<ArrayList<StampedCloudPoints>>() {}.getType();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, type);
        }
    }

    private static ArrayList<Pose> parsePoseData(String filePath) throws IOException {
        Type type = new TypeToken<ArrayList<Pose>>() {}.getType();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, type);
        }
    }

    private static SystemConfig initializeSystem(Configuration config,
                                                  Map<String, ArrayList<StampedDetectedObjects>> cameraData,
                                                  ArrayList<StampedCloudPoints> lidarData,
                                                  ArrayList<Pose> poseData) {
        Map<Integer, Camera> cameras = new HashMap<>();
        for (CameraConfiguration camConfig : config.getCameras().getCamerasConfigurations()) {
            Camera camera = new Camera(camConfig.getId(), camConfig.getFrequency(),STATUS.UP);
            if (cameraData.containsKey(camConfig.getCamera_key())) {
                camera.setDetectedObjectsList(cameraData.get(camConfig.getCamera_key()));
            }
            cameras.put(camConfig.getId(), camera);
        }

        Map<Integer, LiDarWorkerTracker> lidars = new HashMap<>();
        for (LidarConfiguration lidConfig : config.getLiDarWorkers().getLidarConfigurations()) {
            LiDarWorkerTracker lidar = new LiDarWorkerTracker(lidConfig.getId(), lidConfig.getFrequency());
            lidars.put(lidConfig.getId(), lidar);
        }

        GPSIMU gpsimu = new GPSIMU(poseData);

        return new SystemConfig(cameras, lidars, gpsimu, config.getTickTime(), config.getDuration());
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

        PoseService poseService = new PoseService(config.gpsimu);
        new Thread(poseService).start();

        TimeService timeService = new TimeService(config.tickTime, config.duration);
        new Thread(timeService).start();

        FusionSlamService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        new Thread(fusionSlamService).start();
    }

    public static class SystemConfig {
        Map<Integer, Camera> cameras;
        Map<Integer, LiDarWorkerTracker> lidars;
        GPSIMU gpsimu;
        long tickTime;
        long duration;

        public SystemConfig(Map<Integer, Camera> cameras,
                            Map<Integer, LiDarWorkerTracker> lidars,
                            GPSIMU gpsimu,
                            long tickTime,
                            long duration) {
            this.cameras = cameras;
            this.lidars = lidars;
            this.gpsimu = gpsimu;
            this.tickTime = tickTime;
            this.duration = duration;
        }
    }

    
}



