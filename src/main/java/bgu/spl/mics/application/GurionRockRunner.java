package bgu.spl.mics.application;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.configuration.CameraConfiguration;
import bgu.spl.mics.application.configuration.Configuration;
import bgu.spl.mics.application.configuration.LidarConfiguration;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
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
    static CountDownLatch latch;
    private static final FusionSlam fusionSlam = FusionSlam.getInstance();

    private static final List<Thread> serviceThreads = new ArrayList<>();

    private static String cameraDataPath;
    private static String lidarDataPath;
    private static String poseDataPath;
    private static Path configDir;

    public static CountDownLatch getLatch() { return latch; }

    

    public static void main(String[] args) {
        System.out.println("Starting Simulation...");


        String configFilePath = args[0];
        Path configPath = Paths.get(configFilePath).toAbsolutePath();
        configDir = configPath.getParent();
        // Use outputPath.toString() to get the string representation of the path




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
            
        }
        catch (Exception e) {
            // If main fails, produce an error
            System.err.println("Simulation failed: " + e.getMessage());
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
            }
        });
        poseThread.start();
        serviceThreads.add(poseThread);

        // FusionSlam
        System.out.println("Initializing FusionSlamService...");
        FusionSlamService fusionSlamService = new FusionSlamService(FusionSlam.getInstance());
        fusionSlamService.setOutputFilePath(configDir);
        Thread fusionThread = new Thread(() -> {
            try {
                fusionSlamService.run();
            } catch (Exception e) {
                System.err.println("FusionSlamService failed: " + e.getMessage());
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
            }
        });
        timeThread.start();
        serviceThreads.add(timeThread);
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
