package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;

import java.util.ArrayList;

import bgu.spl.mics.MicroService;

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

    private final FusionSlam fusionSlam;
    private int currentTime;
    private int poseCounter = 0;

    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        // TODO Implement this
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
            if (terminateBroadcast.getSender() == "time"){
                System.out.println("FusionSlamService received TerminatedBroadcast from TimeService.");
                StatisticalFolder.getInstance().setSystemRuntime(currentTime);
                terminate();

            }

            else{
                fusionSlam.reportTracked();
                if (fusionSlam.isFinished()){
                    // closing the whole system
                    StatisticalFolder.getInstance().setSystemRuntime(currentTime);
                    sendBroadcast(new TerminatedBroadcast("fusionslam"));
                    terminate();
                }
                
            }
            
            
        });
    
        // Subscribe to CrashedBroadcast: Handle system-wide crash
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {   
            System.out.println("FusionSlamService received CrashedBroadcast.");       
            StatisticalFolder.getInstance().setSystemRuntime(currentTime);
            terminate();
        });


        // Subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            // Process tracked objects and update landmarks
            if (currentTime >= trackedObjectsEvent.getTickTime()){ // we have the pose for this tick
                handleEvent(trackedObjectsEvent, trackedObjectsEvent.getTickTime());
            }

            else{ // we don't have the pose for this trackedEvent
                fusionSlam.addUnhandledTrackedObject(trackedObjectsEvent);
            }

            });
            


        // Subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, poseEvent -> {
                Pose pose = poseEvent.getPose();
                poseCounter = pose.getTime();
                fusionSlam.addPose(pose); // Update the robot's pose in FusionSlam
                complete(poseEvent, pose); // Acknowledge processing is done
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            System.out.println("FusionSlamService received TickBroadcast at tick: " + tickBroadcast.getTick());
            currentTime = tickBroadcast.getTick();     
            if (!fusionSlam.getUnhandledTrackedObjects().isEmpty()){
                for (TrackedObjectsEvent trackedObjectsEvent : fusionSlam.getUnhandledTrackedObjects()){
                    if (currentTime >= trackedObjectsEvent.getTickTime() & poseCounter > currentTime){
                        handleEvent(trackedObjectsEvent, trackedObjectsEvent.getTickTime());
                        fusionSlam.removeHandledTrackedObjects(trackedObjectsEvent);
                    }
                    
                }
            }
            
        });

        System.out.println("FusionSlamService initialized successfully.");
        
    }

    public void handleEvent(TrackedObjectsEvent trackedObjectsEvent, int time){
        trackedObjectsEvent.getTrackedObject().forEach(trackedObject -> {
            String id = trackedObject.getId();
            String description = trackedObject.getDescription();
            ArrayList<CloudPoint> trackedCoordinates = trackedObject.getCloudPoint();
            // to transform the coordinates to the global map

            fusionSlam.addOrUpdateLandmark(id, description, fusionSlam.convertToGlobal(trackedCoordinates, fusionSlam.getPoseByTime(time) ) );
        });
        complete(trackedObjectsEvent, true); // Acknowledge processing is done
    }
}

