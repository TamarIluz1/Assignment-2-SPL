package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.configuration.CameraConfiguration;
import bgu.spl.mics.application.configuration.Configuration;
import bgu.spl.mics.application.configuration.LidarConfiguration;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
 import bgu.spl.mics.application.services.CameraService;
 import bgu.spl.mics.application.services.FusionSlamService;
 import bgu.spl.mics.application.services.LiDarService;
 import bgu.spl.mics.application.services.PoseService;
 import bgu.spl.mics.application.services.TimeService;

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
    private static StatisticalFolder statistics = StatisticalFolder.getInstance();
    private static FusionSlam fusionSlam = FusionSlam.getInstance();
    static CountDownLatch latch;

    private static final List<Thread> serviceThreads = new ArrayList<>();


    private static String cameraDataPath;
    private static String lidarDataPath;
    private static String poseDataPath;
    
    public static CountDownLatch getLatch() {
        return latch;
    }

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
            cameraDataPath = getAbsolutePath(configFilePath, config.getCameras().getCameraDatasPath());
            lidarDataPath = getAbsolutePath(configFilePath, config.getLiDarWorkers().getLidarsDataPath());
            poseDataPath = getAbsolutePath(configFilePath, config.getPoseJsonFile());

            // Parse sensor data
            Map<String, ArrayList<StampedDetectedObjects>> cameraData = parseCameraData(cameraDataPath);
            ArrayList<Pose> poseData = parsePoseData(poseDataPath);

            // Initialize system configuration
            SystemConfig systemConfig = initializeSystem(config, cameraData, poseData);
            
            // Start services
            startServices(systemConfig);

            // Wait for simulation to finish
            waitForSimulationCompletion();


            // Assuming fusionSlam.getLandmarks() returns List<LandMark>
            ArrayList<LandMark> landMarkList = fusionSlam.getLandmarks();
            Map<String, LandMark> landMarkMap = landMarkList.stream()
                .collect(Collectors.toMap(LandMark::getId, landMark -> landMark));

            writeSuccessOutput("output_file.json", statistics, landMarkMap);
        } catch (Exception e) {

            // Write error output
            writeErrorOutput("error_output.json", e.getMessage(), statistics);
        }
    }

    private static void waitForSimulationCompletion() throws InterruptedException {
        for (Thread thread : serviceThreads) {
            thread.join(); // Wait for each thread to finish
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
        catch (IOException e) {
            System.err.println("Failed to parse configuration file: " + e.getMessage());
            throw e;
        }
    }

    private static Map<String, ArrayList<StampedDetectedObjects>> parseCameraData(String filePath) throws IOException {
        Type type = new TypeToken<Map<String, ArrayList<StampedDetectedObjects>>>() {}.getType();
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
            LiDarWorkerTracker lidar = new LiDarWorkerTracker(lidConfig.getId(), lidConfig.getFrequency(), lidarDataPath);
            lidars.put(lidConfig.getId(), lidar);
        }

        GPSIMU gpsimu = new GPSIMU(poseData);

        // Calculate the total number of sensors
        int totalSensors = cameras.size() + lidars.size() + 1;

        // Update FusionSlam with the total number of sensors
        fusionSlam.setSensorAmount(totalSensors);

        return new SystemConfig(cameras, lidars, gpsimu, config.getTickTime(), config.getDuration());
    }

    private static void startServices(SystemConfig config) {
        latch = new CountDownLatch(config.cameras.size() + config.lidars.size() + 2);

        PoseService poseService = new PoseService(config.gpsimu);
        Thread poseThread = new Thread(poseService);
        poseThread.start();
        serviceThreads.add(poseThread);
                
        FusionSlamService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        Thread fusionThread = new Thread(fusionSlamService);
        fusionThread.start();
        serviceThreads.add(fusionThread);
        
        config.cameras.values().forEach(camera -> {
            CameraService service = new CameraService(camera);
            Thread thread = new Thread(service);
            thread.start();
            serviceThreads.add(thread);
        });

        config.lidars.values().forEach(lidar -> {
             LiDarService service = new LiDarService(lidar);
            Thread thread = new Thread(service);
            thread.start();
            serviceThreads.add(thread); // Track the thread
        });


        
        // we will implement countDownLatch in TimeService
        
        TimeService timeService = new TimeService(config.tickTime, config.duration);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Latch await interrupted: " + e.getMessage());
        }
        Thread timeThread = new Thread(timeService);
        timeThread.start();
        serviceThreads.add(timeThread);


    }



    
private static void writeSuccessOutput(String outputFilePath, StatisticalFolder statistics, Map<String, LandMark> landMarks) {
    try {
        JsonObject successOutput = new JsonObject();
        successOutput.addProperty("systemRuntime", statistics.getSystemRuntime());
        successOutput.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
        successOutput.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
        successOutput.addProperty("numLandmarks", landMarks.size());

        // Add landmarks to JSON
        JsonObject landMarksJson = new JsonObject();
        for (Map.Entry<String, LandMark> entry : landMarks.entrySet()) {
            JsonObject landmarkJson = new JsonObject();
            landmarkJson.addProperty("id", entry.getValue().getId());
            landmarkJson.addProperty("description", entry.getValue().getDescription());

            JsonArray coordinatesArray = new JsonArray();
            for (CloudPoint coordinate : entry.getValue().getCoordinates()) {
                JsonObject coordinateJson = new JsonObject();
                coordinateJson.addProperty("x", coordinate.getX());
                coordinateJson.addProperty("y", coordinate.getY());
                coordinatesArray.add(coordinateJson);
            }
            landmarkJson.add("coordinates", coordinatesArray);
            landMarksJson.add(entry.getKey(), landmarkJson);
        }
        successOutput.add("landMarks", landMarksJson);

        // Write to file
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            gson.toJson(successOutput, writer);
        }
        System.out.println("Success output written to " + outputFilePath);
    } catch (IOException e) {
        System.err.println("Failed to write success output: " + e.getMessage());
    }
}


    private static void writeErrorOutput(String outputFilePath, String errorMessage, StatisticalFolder statistics) {
        try {
            JsonObject errorOutput = new JsonObject();
            errorOutput.addProperty("error", errorMessage);
            errorOutput.addProperty("systemRuntime", statistics.getSystemRuntime());
            errorOutput.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
            errorOutput.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
            errorOutput.addProperty("numLandMarks", statistics.getNumLandMarks());

            // Write to file
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                gson.toJson(errorOutput, writer);
            }
            System.out.println("Error output written to " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Failed to write error output: " + e.getMessage());
        }
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


