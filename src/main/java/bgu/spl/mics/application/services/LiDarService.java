package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarDataBase;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.STATUS;
import java.util.Vector;
/** PARTY OF SPL
 * LiDarService is responsible for processing data from the LiDAR sensor and
 * sending TrackedObjectsEvents to the FusionSLAM service.
 * 
 * This service interacts with the LiDarTracker object to retrieve and process
 * cloud point data and updates the system's StatisticalFolder upon sending its
 * observations.
 */
public class LiDarService extends MicroService {
    private final MessageBus messageBus = MessageBusImpl.getInstance();
    private final LiDarWorkerTracker liDarWorkerTracker;
    /**
     * Constructor for LiDarService.
     *
     * @param liDarTracker The LiDAR tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker liDarWorkerTracker) {
        super("LidarService");
        this.liDarWorkerTracker = liDarWorkerTracker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // the thread is automatically registered to the relevant broadcasts and events thanks to its type
        // according to what i understand- the lidar working only happens after recieving the event
        messageBus.register(this);
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.out.println("crashed broadcast" + crashedBroadcast.toString() + "\nrecieved termination at lidar" + liDarWorkerTracker.getId() + "TERMINATING");
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, (currentTick)->{
            // for curr tick, if there's a DetectObjectsEvent send event to FusionSlam
            Vector<TrackedObject> toSend = new Vector<>();
            for (TrackedObject o: liDarWorkerTracker.getLastTrackedObjects()){
                // for each tracked object, if the timing is right, send the object to the FusionSlam service // TODO make sure ticks are correct
                if (o.getTime() <= currentTick.getTick() + liDarWorkerTracker.getFrequency()){
                    toSend.add(o);
                }
            }
            sendEvent(new TrackedObjectsEvent(toSend, currentTick.getTick()));
        });
        subscribeEvent(DetectObjectsEvent.class, (detectObjectsEvent)->{
            // psuedo code: for each DetectObjectsEvent:
            // 1. TODO get the relevant cloud points from the LiDarDataBase
            // 2. send the detected objects to the FusionSlam service
            //  The LiDar gets the X’s,Y’s coordinates from the DataBase of them and sends a newTrackedObjectsEvent to the Fusion.
            // After the LiDar Worker completes the event, it saves the coordinates in the lastObjects variable in DataBase and sends True value to the Camera.

            // note that according to the invariant, errors come only from the camera service
            // TODO implement termination
            for (DetectedObject o  : detectObjectsEvent.getObjectDetails().getDetectedObjects()){
                // foreach object found, we will add it to the list of what we can process.
                //
                liDarWorkerTracker.addTrackedObject(new TrackedObject(o.getId(),detectObjectsEvent.getTickTime(), o.getDescripition(),liDarWorkerTracker.getCoorCloudPoints(o.getId())));
            }
            return true;
        });

    }
}
