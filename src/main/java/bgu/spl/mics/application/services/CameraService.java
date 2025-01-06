package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedDetectedObjects;
import bgu.spl.mics.application.objects.StatisticalFolder;

/**
 * CameraService is responsible for processing data from the camera and
 * sending DetectObjectsEvents to LiDAR workers.
 * 
 * This service interacts with the Camera object to detect objects and updates
 * the system's StatisticalFolder upon sending its observations.
 */
public class CameraService extends MicroService {

    private final  Camera camera;

    private final MessageBus messageBus = MessageBusImpl.getInstance();
    private StampedDetectedObjects nextDetected;


    /**
     * Constructor for CameraService.
     *
     * @param camera The Camera object that this service will use to detect objects.
     */
    public CameraService(Camera camera) {
        super("CameraService");
        this.camera = camera;
        nextDetected = camera.getNextDetectedObjects();
    }

    public void terminateService(){
        sendBroadcast(new TerminatedBroadcast("camera"));
        camera.setStatus(STATUS.DOWN);

        this.terminate();
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
            if (terminateBroadcast.getSender().equals("time")){
                camera.setStatus(STATUS.DOWN);
                terminateService();
            }
        });
    
        // Subscribe to CrashedBroadcast: Handle system-wide crash
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.out.println(camera.getId() + "CAMERA CRASHBROADCAST camera received CrashedBroadcast.");
            camera.setStatus(STATUS.DOWN);
            terminateService();
        });
    

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            int currentTick = tickBroadcast.getTick();
            if (camera.getStatus() == STATUS.UP ) {
                if (nextDetected == null) {
                    // finished working- no more objects to detect
                    System.out.println("Camera " + camera.getId() + " terminated at tick " + currentTick);
                    terminateService();
                }
                else if (nextDetected.getTimestamp() + camera.getFrequency() <= currentTick ) {
                    // invariant: if one object is an error, the whole service is terminated and the data won't be sent
                    for (DetectedObject object : nextDetected.getDetectedObjects()) {
                        if (object.getId().equals("ERROR")) {
                            // Handle camera error scenario
                            camera.setStatus(STATUS.ERROR);
                            GurionRockRunner.setSystemCrashed(true);
                            GurionRockRunner.setFaultySensor("Camera" + camera.getId()); 
                            GurionRockRunner.setErorrMassage(object.getDescripition());
                            // forcibly "Camera1" so the JSON matches "faultySensor": "Camera1"
                            sendBroadcast(new CrashedBroadcast("camera " +camera.getId(), "Camera error detected at tick " + currentTick));
                            terminateService();
                            return;
                        }
                        StatisticalFolder.getInstance().incrementDetectedObjects(1);
                    }

                    GurionRockRunner.getLastCamerasFrame().put("Camera" + camera.getId(), nextDetected);
                    sendEvent(new DetectObjectsEvent(nextDetected.getTimestamp() , nextDetected));
                    nextDetected = camera.getNextDetectedObjects();
                }
            }
            else{
                
                terminateService();
            }
        });
        System.out.println("CamaraService initialized successfully.");
        
    }
    
}