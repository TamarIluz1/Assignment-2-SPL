package bgu.spl.mics.application.services;

import java.util.ArrayList;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.CloudPoint;
import bgu.spl.mics.application.objects.FusionSlam;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.application.objects.StatisticalFolder;

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
            //StatisticalFolder.getInstance().setSystemRuntime(currentTime);
            terminate();
        });


        // Subscribe to TrackedObjectsEvent
        subscribeEvent(TrackedObjectsEvent.class, trackedObjectsEvent -> {
            // Process tracked objects and update landmarks

            if (fusionSlam.getPoses().size() >= trackedObjectsEvent.getTime() & currentTime >= trackedObjectsEvent.getTime() && !GurionRockRunner.isSystemCrashed()){ // we have the pose for this tick
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
                if (!fusionSlam.getUnhandledTrackedObjects().isEmpty() && !GurionRockRunner.isSystemCrashed()){
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
            if (fusionSlam.isFinished()){
                System.out.println("EDGE CASE");
                terminate();
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


    
}

