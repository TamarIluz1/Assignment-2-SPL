package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;


import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.TickBroadcast;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private Camera camera;
    private final MessageBus messageBus = MessageBusImpl.getInstance();
    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService");
        this.camera = camera;
    }

    /**
     * Initializes the CameraService.
     * Registers the service to handle TickBroadcasts and sets up callbacks for sending
     * DetectObjectsEvents.
     */
    @Override
    protected void initialize() {
        // Register the service with the MessageBus
        messageBus.register(this);
    
        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast -> {
            System.out.println(camera.getId() + " received TerminateBroadcast");
            terminate();
        });
    
        // Subscribe to CrashedBroadcast: Handle system-wide crash
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.out.println(camera.getId() + " received CrashedBroadcast.");
            terminate();
        });
    

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int currentTick = tickBroadcast.getTick();
            if (camera.getStatus() == STATUS.UP) {
                StampedDetectedObjects detectedObjects = camera.getNextStampedDetectedObjects(currentTick+ camera.getFrequency());
                if (detectedObjects != null && detectedObjects.getTimestamp() <= currentTick) {
                    // Send a DetectObjectsEvent for each detected object
                    for (DetectedObject object : detectedObjects.getDetectedObjects()) {
                        if ("ERROR" == object.getId()) {
                            // Handle camera error scenario
                            camera.setStatus(STATUS.ERROR);
                            sendBroadcast(new CrashedBroadcast(camera.getId(), "Camera error detected at tick " + currentTick));
                            terminate();
                            return;
                        }
                        sendEvent(new DetectObjectsEvent(object.getId(), object.getDescripition()));
                    }
                }
            }
        });
    }
    
}
