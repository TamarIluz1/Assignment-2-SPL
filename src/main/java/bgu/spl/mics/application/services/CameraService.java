package bgu.spl.mics.application.services;

import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
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
        messageBus.register(this);

        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast -> {
            System.out.println("CameraService " + getName() + " received TerminateBroadcast");
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, (currentTick) -> {
            // TODO sensitive to tickbroadcast- send detectobjectevent according to json file
            if (camera.getStatus() == STATUS.UP) {
                StampedDetectedObjects newlyDetected = camera.getDetectedObjectsByTime(currentTick.getTick() + camera.getFrequency());
                //send DetectObjectsEvent
                // if there are new objects to be sent on this tick
                for(DetectedObject detectedObject : newlyDetected.getDetectedObjects()){
                    sendEvent(new DetectObjectsEvent(detectedObject.getId(), detectedObject.getDescripition()));
                }
                
            }
            else if (camera.getStatus() == STATUS.ERROR){
                // TODO print error message
                // turn off camera
            }
            else{
                // Camera is down
                System.out.println("Camera is down" + camera.getId());
            }
        });
        Thread CameraThread = new Thread();
        CameraThread.start();

    }
}
