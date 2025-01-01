package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;

import java.util.Vector;

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

    public FusionSlamService(FusionSlam fusionSlam) {
        super("FusionSlamService");
        // TODO Implement this
        this.fusionSlam = fusionSlam;
    }

    /**
     * Initializes the FusionSlamService.
     * Registers the service to handle TrackedObjectsEvents, PoseEvents, and TickBroadcasts,
     * and sets up callbacks for updating the global map.
     */
    @Override
    protected void initialize() {
        // TODO Implement this

        messageBus.register(this);
    
        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast -> {
            if (terminateBroadcast.getSender() == "time"){
                terminate();
            }
            
        });
    
        // Subscribe to CrashedBroadcast: Handle system-wide crash
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            // TODO Implement this
            //SUBSCRIBE TO CRASHED BROADCAST 30.12 TAMAR
            
            terminate();
        });


        // Subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            // Process tracked objects and update landmarks
            trackedObjectsEvent.getTrackedObject().forEach(trackedObject -> {
                String id = trackedObject.getId();
                String description = trackedObject.getDescription();
                Vector<CloudPoint> trackedCoordinates = trackedObject.getCloudPoint();
                fusionSlam.addOrUpdateLandmark(id, description, trackedCoordinates);
            });
            complete(trackedObjectsEvent, true); // Acknowledge processing is done

        });

        // Subscribe to PoseEvent
        subscribeEvent(PoseEvent.class, poseEvent -> {
                Pose pose = poseEvent.getPose();
                fusionSlam.addPose(pose); // Update the robot's pose in FusionSlam
                complete(poseEvent, pose); // Acknowledge processing is done
        });

        // Subscribe to TickBroadcast
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
                long currentTick = tickBroadcast.getTick();
                System.out.println("FusionSlamService received TickBroadcast: " + currentTick);
            });

            System.out.println("FusionSlamService initialized successfully.");
        
        }
}

