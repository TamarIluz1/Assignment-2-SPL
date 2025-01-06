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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.configuration.CameraConfiguration;
import bgu.spl.mics.application.configuration.Configuration;
import bgu.spl.mics.application.configuration.LidarConfiguration;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;

/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 */
public class GurionRockRunner {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final StatisticalFolder statistics = StatisticalFolder.getInstance();
    private static final FusionSlam fusionSlam = FusionSlam.getInstance();
    static CountDownLatch latch;

    private static volatile boolean systemCrashed = false;
    private static String faultySensor = null;

    


    // For camera frames
    private static final Map<String, StampedDetectedObjects> lastCamerasFrame = new HashMap<>();

    // For LiDar frames
    private static final Map<String, TrackedObject> lastLiDarWorkerTrackersFrame = new HashMap<>();

    // For final poses
    private static final List<Pose> poses = new ArrayList<>();

    private static final List<Thread> serviceThreads = new ArrayList<>();

    private static String cameraDataPath;
    private static String lidarDataPath;
    private static String poseDataPath;

    // Setters used by services on crashes
    public static void setSystemCrashed(boolean crashed) { systemCrashed = crashed; }
    public static void setFaultySensor(String sensorName) { faultySensor = sensorName; }
    
    private static String errror_msg = null;
    public static void setErorrMassage(String msg) {errror_msg = msg;}

    public static Map<String, StampedDetectedObjects> getLastCamerasFrame() { return lastCamerasFrame; }
    public static Map<String, TrackedObject> getLastLiDarWorkerTrackersFrame() { return lastLiDarWorkerTrackersFrame; }
    public static List<Pose> getPoses() { return poses; }
    public static CountDownLatch getLatch() { return latch; }
    public static boolean isSystemCrashed() { return systemCrashed; }

    public static void main(String[] args) {
        System.out.println("Starting Simulation...");


        String configFilePath = args[0];
        Path configPath = Paths.get(configFilePath).toAbsolutePath();
        Path configDir = configPath.getParent();
        Path outputPath = configDir.resolve("output_file2.json");
        // Use outputPath.toString() to get the string representation of the path
        String outputFilePath = outputPath.toString();



        try {
            // 1) Parse config
            Configuration config = parseConfiguration(configFilePath);

            // 2) Resolve paths
            cameraDataPath = getAbsolutePath(configFilePath, config.getCameras().getCameraDatasPath());
            lidarDataPath  = getAbsolutePath(configFilePath, config.getLiDarWorkers().getLidarsDataPath());
            poseDataPath   = getAbsolutePath(configFilePath, config.getPoseJsonFile());

            // 3) Parse sensor data
            Map<String, ArrayList<StampedDetectedObjects>> cameraData = parseCameraData(cameraDataPath);
            ArrayList<Pose> poseData = parsePoseData(poseDataPath);

            // 4) Build system config
            SystemConfig systemConfig = initializeSystem(config, cameraData, poseData);

            // 5) Start services
            startServices(systemConfig);

            // 6) Wait for simulation to end
            waitForSimulationCompletion();

            // 7) If crashed => error, else success
            if (systemCrashed) {
                writeErrorOutput(
                    outputFilePath,
                    errror_msg, // or "LiDar disconnected" etc.
                    (faultySensor != null ? faultySensor : "UnknownSensor"),
                    lastCamerasFrame,
                    lastLiDarWorkerTrackersFrame,
                    poses,
                    statistics
                );
            } else {
                ArrayList<LandMark> landMarkList = fusionSlam.getLandmarks();
                Map<String, LandMark> landMarkMap = landMarkList.stream()
                    .collect(Collectors.toMap(LandMark::getId, lm -> lm));
                writeSuccessOutput(outputFilePath, statistics, landMarkMap);
            }
        }
        catch (Exception e) {
            // If main fails, produce an error
            writeErrorOutput(
                outputFilePath,
                "Exception in main: " + e.getMessage(),
                (faultySensor != null ? faultySensor : "UnknownSensor"),
                lastCamerasFrame,
                lastLiDarWorkerTrackersFrame,
                poses,
                statistics
            );
        }
    }

    // --- Parsing & building
    private static Configuration parseConfiguration(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Type type = new TypeToken<Configuration>() {}.getType();
            return gson.fromJson(reader, type);
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

    private static void waitForSimulationCompletion() throws InterruptedException {
        for (Thread t : serviceThreads) {
            t.join();
        }
    }

    private static String getAbsolutePath(String configFilePath, String relativePath) {
        Path configPath = Paths.get(configFilePath);
        Path resolvedPath = configPath.getParent().resolve(relativePath).normalize();
        return resolvedPath.toString();
    }

    private static SystemConfig initializeSystem(
        Configuration config,
        Map<String, ArrayList<StampedDetectedObjects>> cameraData,
        ArrayList<Pose> poseData
    ) {
        Map<Integer, Camera> cameras = new HashMap<>();
        for (CameraConfiguration camConfig : config.getCameras().getCamerasConfigurations()) {
            Camera camera = new Camera(camConfig.getId(), camConfig.getFrequency(), STATUS.UP);
            if (cameraData.containsKey(camConfig.getCamera_key())) {
                camera.setDetectedObjectsList(cameraData.get(camConfig.getCamera_key()));
            }
            cameras.put(camConfig.getId(), camera);
        }

        Map<Integer, LiDarWorkerTracker> lidars = new HashMap<>();
        for (LidarConfiguration lidConfig : config.getLiDarWorkers().getLidarConfigurations()) {
            LiDarWorkerTracker tracker =
                new LiDarWorkerTracker(lidConfig.getId(), lidConfig.getFrequency(), lidarDataPath);
            lidars.put(lidConfig.getId(), tracker);
        }

        GPSIMU gpsimu = new GPSIMU(poseData);

        int totalSensors = cameras.size() + lidars.size() + 1;
        fusionSlam.setSensorAmount(totalSensors);

        return new SystemConfig(cameras, lidars, gpsimu, config.getTickTime(), config.getDuration());
    }

    private static void startServices(SystemConfig config) {
        latch = new CountDownLatch(config.cameras.size() + config.lidars.size() + 2);

        // Pose
        System.out.println("Initializing PoseService...");
        PoseService poseService = new PoseService(config.gpsimu);
        Thread poseThread = new Thread(() -> {
            try {
                poseService.run();
            } catch (Exception e) {
                System.err.println("PoseService failed: " + e.getMessage());
                setSystemCrashed(true);
                setFaultySensor("PoseService");
            }
        });
        poseThread.start();
        serviceThreads.add(poseThread);

        // FusionSlam
        System.out.println("Initializing FusionSlamService...");
        FusionSlamService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        Thread fusionThread = new Thread(() -> {
            try {
                fusionSlamService.run();
            } catch (Exception e) {
                System.err.println("FusionSlamService failed: " + e.getMessage());
                setSystemCrashed(true);
                setFaultySensor("FusionSlamService");
            }
        });
        fusionThread.start();
        serviceThreads.add(fusionThread);

        // Cameras
        System.out.println("Initializing CameraServices...");
        for (Camera cam : config.cameras.values()) {
            CameraService service = new CameraService(cam);
            Thread t = new Thread(() -> {
                try {
                    service.run();
                } catch (Exception e) {
                    System.err.println("CameraService failed: " + e.getMessage());
                    setSystemCrashed(true);
                    setFaultySensor("Camera" + cam.getId());
                }
            });
            t.start();
            serviceThreads.add(t);
        }

        // LiDar
        System.out.println("Initializing LiDarServices...");
        for (LiDarWorkerTracker lw : config.lidars.values()) {
            LiDarService service = new LiDarService(lw);
            Thread t = new Thread(() -> {
                try {
                    service.run();
                } catch (Exception e) {
                    System.err.println("LiDarService failed: " + e.getMessage());
                    setSystemCrashed(true);
                    setFaultySensor("LiDar" + lw.getId());
                }
            });
            t.start();
            serviceThreads.add(t);
        }

        // Time
        System.out.println("Waiting for all services to initialize...");
        TimeService timeService = new TimeService(config.tickTime, config.duration);
        Thread timeThread = new Thread(() -> {
            try {
                latch.await();
                System.out.println("All services initialized. Starting TimeService...");
                timeService.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Latch awaiting interrupted: " + e.getMessage());
                setSystemCrashed(true);
                setFaultySensor("TimeService");
            }
        });
        timeThread.start();
        serviceThreads.add(timeThread);
    }

    private static void writeErrorOutput(
        String outputFilePath,
        String errorMessage,
        String faultySensor,
        Map<String, StampedDetectedObjects> lastCamerasFrame,
        Map<String, TrackedObject> lastLiDarWorkerTrackersFrame,
        List<Pose> poses,
        StatisticalFolder statistics
    ) {
        try {
        


            JsonObject errorOutput = new JsonObject();

            errorOutput.addProperty("error", errorMessage);
            errorOutput.addProperty("faultySensor", faultySensor);
            // lastCamerasFrame
            JsonObject camerasFrameJson = new JsonObject();
            for (Map.Entry<String, StampedDetectedObjects> entry : lastCamerasFrame.entrySet()) {
                String cameraName = entry.getKey();
                StampedDetectedObjects stamped = entry.getValue();

                JsonObject stampedJson = new JsonObject();
                stampedJson.addProperty("time", stamped.getTimestamp());

                JsonArray detectedArr = new JsonArray();
                for (DetectedObject obj : stamped.getDetectedObjects()) {
                    JsonObject objJson = new JsonObject();
                    objJson.addProperty("id", obj.getId());
                    objJson.addProperty("description", obj.getDescripition());
                    detectedArr.add(objJson);
                }
                stampedJson.add("detectedObjects", detectedArr);

                camerasFrameJson.add(cameraName, stampedJson);
            }
            errorOutput.add("lastCamerasFrame", camerasFrameJson);

            // lastLiDarWorkerTrackersFrame
            JsonObject lidarFrameJson = new JsonObject();
            for (Map.Entry<String, TrackedObject> entry : lastLiDarWorkerTrackersFrame.entrySet()) {
                String lidarName = entry.getKey();
                TrackedObject scpList = entry.getValue();

                JsonArray scpArray = new JsonArray();

                JsonObject scpJson = new JsonObject();
                scpJson.addProperty("id", scpList.getId());
                scpJson.addProperty("time", scpList.getTime());
                // description if you have it

                JsonArray coordsArr = new JsonArray();
                for (CloudPoint cp : scpList.getCloudPoint()) {
                    JsonObject cpObj = new JsonObject();
                    cpObj.addProperty("x", cp.getX());
                    cpObj.addProperty("y", cp.getY());
                    coordsArr.add(cpObj);
                    
                }
                scpJson.addProperty("description", scpList.getDescription());
                scpJson.add("coordinates", coordsArr);

                scpArray.add(scpJson);
                
                lidarFrameJson.add(lidarName, scpArray);
            }
            errorOutput.add("lastLiDarWorkerTrackersFrame", lidarFrameJson);

            // poses
            JsonArray posesArr = new JsonArray();
            for (Pose p : poses) {
                JsonObject poseJson = new JsonObject();
                poseJson.addProperty("time", p.getTime());
                poseJson.addProperty("x", p.getX());
                poseJson.addProperty("y", p.getY());
                poseJson.addProperty("yaw", p.getYaw());
                posesArr.add(poseJson);
            }
            errorOutput.add("poses", posesArr);

            // statistics
            JsonObject statsJson = new JsonObject();
            statsJson.addProperty("systemRuntime", statistics.getSystemRuntime());
            statsJson.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
            statsJson.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
            statsJson.addProperty("numLandmarks", statistics.getNumLandMarks());

            // landMarks
            JsonObject landMarksJson = new JsonObject();
            for (LandMark lm : fusionSlam.getLandmarks()) {
                JsonObject lmJson = new JsonObject();
                lmJson.addProperty("id", lm.getId());
                lmJson.addProperty("description", lm.getDescription());

                JsonArray coordsArr = new JsonArray();
                for (CloudPoint cp : lm.getCoordinates()) {
                    JsonObject cpObj = new JsonObject();
                    cpObj.addProperty("x", cp.getX());
                    cpObj.addProperty("y", cp.getY());
                    coordsArr.add(cpObj);
                }
                lmJson.add("coordinates", coordsArr);

                landMarksJson.add(lm.getId(), lmJson);
            }
            statsJson.add("landMarks", landMarksJson);

            errorOutput.add("statistics", statsJson);

            try (FileWriter writer = new FileWriter(outputFilePath)) {
                gson.toJson(errorOutput, writer);
            }
            System.out.println("Detailed crash error output written to " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Failed to write error output: " + e.getMessage());
        }
    }

    private static void writeSuccessOutput(
        String outputFilePath,
        StatisticalFolder statistics,
        Map<String, LandMark> landMarks
    ) {
        try {
            JsonObject successOutput = new JsonObject();
            successOutput.addProperty("systemRuntime", statistics.getSystemRuntime());
            successOutput.addProperty("numDetectedObjects", statistics.getNumDetectedObjects());
            successOutput.addProperty("numTrackedObjects", statistics.getNumTrackedObjects());
            successOutput.addProperty("numLandmarks", landMarks.size());

            JsonObject landMarksJson = new JsonObject();
            for (Map.Entry<String, LandMark> entry : landMarks.entrySet()) {
                LandMark lm = entry.getValue();
                JsonObject lmJson = new JsonObject();
                lmJson.addProperty("id", lm.getId());
                lmJson.addProperty("description", lm.getDescription());

                JsonArray coordsArr = new JsonArray();
                for (CloudPoint cp : lm.getCoordinates()) {
                    JsonObject cpObj = new JsonObject();
                    cpObj.addProperty("x", cp.getX());
                    cpObj.addProperty("y", cp.getY());
                    coordsArr.add(cpObj);
                }
                lmJson.add("coordinates", coordsArr);
                landMarksJson.add(entry.getKey(), lmJson);
            }
            successOutput.add("landMarks", landMarksJson);

            try (FileWriter writer = new FileWriter(outputFilePath)) {
                gson.toJson(successOutput, writer);
            }
            System.out.println("Success output written to " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Failed to write success output: " + e.getMessage());
        }
    }

    public static class SystemConfig {
        public Map<Integer, Camera> cameras;
        public Map<Integer, LiDarWorkerTracker> lidars;
        public GPSIMU gpsimu;
        public long tickTime;
        public long duration;

        public SystemConfig(
            Map<Integer, Camera> cameras,
            Map<Integer, LiDarWorkerTracker> lidars,
            GPSIMU gpsimu,
            long tickTime,
            long duration
        ) {
            this.cameras = cameras;
            this.lidars = lidars;
            this.gpsimu = gpsimu;
            this.tickTime = tickTime;
            this.duration = duration;
        }
    }
}
