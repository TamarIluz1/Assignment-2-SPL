package bgu.spl.mics.application.services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.LandMark;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;
import bgu.spl.mics.application.objects.TrackedObject;

/**
 * FusionSlamService integrates data from multiple sensors to build and update
 * the robot's global map.
 * 
 * This service receives TrackedObjectsEvents from LiDAR workers and PoseEvents from the PoseService,
 * transforming and updating the map with new landmarks.
 */
public class FusionSlamService extends MicroService {

    /**
     * Constructor for FusionSlamService.
     *
     * @param fusionSlam The FusionSLAM object responsible for managing the global map.
     */

    private static FusionSlam fusionSlam;

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private int currentTime;

    private StatisticalFolder statisticas = StatisticalFolder.getInstance();

    private Path outputFilePath = null;

    public void setOutputFilePath(Path path) {
        this.outputFilePath = path;
    }

    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        this.fusionSlam = fusionSlam;
        currentTime = 0;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {

        messageBus.register(this);
        
        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast -> {
            System.out.println("FusionSlamService received TerminatedBroadcast from " + terminateBroadcast.getSender());
            if (terminateBroadcast.getSender().equals("time")){
                System.out.println("FusionSlamService received TerminatedBroadcast from TimeService.");
                statisticas.setSystemRuntime(currentTime);
                sendBroadcast(new TerminatedBroadcast("fusionslam"));
                ArrayList<LandMark> landMarkList = fusionSlam.getLandmarks();
                Map<String, LandMark> landMarkMap = landMarkList.stream()
                    .collect(Collectors.toMap(LandMark::getId, lm -> lm));
                writeSuccessOutput(outputFilePath.resolve("output_file.json").toString(), statisticas, landMarkMap);
                terminate();

            }
            else{
                fusionSlam.reportTracked();
                if (fusionSlam.isFinished()){
                    // closing the whole system
                    statisticas.setSystemRuntime(currentTime);
                    sendBroadcast(new TerminatedBroadcast("fusionslam"));

                    ArrayList<LandMark> landMarkList = fusionSlam.getLandmarks();
                    Map<String, LandMark> landMarkMap = landMarkList.stream()
                    .collect(Collectors.toMap(LandMark::getId, lm -> lm));
                    writeSuccessOutput(outputFilePath.resolve("output_file.json").toString(), statisticas, landMarkMap);
                    terminate();
                }
                
            }
            
            
        });
    
        // Subscribe to CrashedBroadcast: Handle system-wide crash
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {   
            System.out.println("FusionSlamService received CrashedBroadcast.");       
            //StatisticalFolder.getInstance().setSystemRuntime(currentTime);


            writeErrorOutput(
                 outputFilePath.resolve("ErrorOutput.json").toString(),
                    crashedBroadcast.getMessage(), // or "LiDar disconnected" etc.
                    (crashedBroadcast.getObjectID() != null ? crashedBroadcast.getObjectID() : "UnknownSensor"),
                    StatisticalFolder.getLastCamerasFrame(),
                    StatisticalFolder.getLastLiDarWorkerTrackersFrame(),
                    StatisticalFolder.getPoses(),
                    statisticas
            );

            terminate();

        });


        // Subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            // Process tracked objects and update landmarks

            if (fusionSlam.getPoses().size() >= trackedObjectsEvent.getTime() & currentTime >= trackedObjectsEvent.getTime()){ // we have the pose for this tick
                handleEvent(trackedObjectsEvent);
            }
            else{ // we don't have the pose for this trackedEvent
                fusionSlam.addUnhandledTrackedObject(trackedObjectsEvent);
            }

            });
            


        // Subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, poseEvent -> {
                Pose pose = poseEvent.getPose();
                //System.err.println("[Fusion] got pose at time "+pose.getTime());
                fusionSlam.addPose(pose); // Update the robot's pose in FusionSlam
                complete(poseEvent, pose); // Acknowledge processing is done
                //  we'll check if we can handle event now.
                if (!fusionSlam.getUnhandledTrackedObjects().isEmpty()){
                    // we might be able to handle the event now
                    ArrayList<TrackedObjectsEvent> handled = new ArrayList<>();
                    for (TrackedObjectsEvent e : fusionSlam.getUnhandledTrackedObjects()){
                        if (e.getTime() <= currentTime & fusionSlam.getPoses().size() >= e.getTime()){
                            handleEvent(e);
                            handled.add(e);
                        }
                    }
                    fusionSlam.removeHandledTrackedObjects(handled);
                }
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            //System.err.println("FusionSlamService received TickBroadcast at tick: " + tickBroadcast.getTick());
            ArrayList<TrackedObjectsEvent> handled = new ArrayList<>();
            currentTime = tickBroadcast.getTick();
            if (currentTime >= 22){
                System.out.println("DEBUG");
            }
            if (!fusionSlam.getUnhandledTrackedObjects().isEmpty()){
                for (TrackedObjectsEvent trackedObjectsEvent : fusionSlam.getUnhandledTrackedObjects()){
                    if (fusionSlam.getPoses().size() >= trackedObjectsEvent.getTime() & tickBroadcast.getTick() >= trackedObjectsEvent.getTime()){
                        handleEvent(trackedObjectsEvent);
                        handled.add(trackedObjectsEvent);
                }

            }
        
            fusionSlam.removeHandledTrackedObjects(handled);
            }

            
        });

        System.out.println("FusionSlamService initialized successfully.");
        
    }

    public void handleEvent(TrackedObjectsEvent trackedObjectsEvent){
        
        trackedObjectsEvent.getTrackedObject().forEach(trackedObject -> {
            String id = trackedObject.getId();
            String description = trackedObject.getDescription();
            ArrayList<CloudPoint> trackedCoordinates = trackedObject.getCloudPoint();
            // to transform the coordinates to the global map

            fusionSlam.addOrUpdateLandmark(id, description, fusionSlam.convertToGlobal(trackedCoordinates,fusionSlam.getPoseByTime(trackedObject.getTime())));
        });
        complete(trackedObjectsEvent, true); // Acknowledge processing is done
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


    
}

