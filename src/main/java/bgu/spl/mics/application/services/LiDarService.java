package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
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
import bgu.spl.mics.application.objects.StampedCloudPoints;

import java.util.Vector;
import java.util.HashMap;
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
    private HashMap<String, DetectObjectsEvent> detectedEventsToProcess;
    private HashMap<String, DetectedObject> objectsToProcess;
    /**
     * Constructor for LiDarService.
     *
     * @param liDarTracker The LiDAR tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker liDarWorkerTracker) {
        super("LidarService");
        this.liDarWorkerTracker = liDarWorkerTracker;
        //detectedEventsToProcess = new HashMap<String, DetectObjectsEvent>();
        objectsToProcess = new HashMap<String, DetectedObject>();
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    // psuedoCode- in tick, it will read from jsonFile and add to lastTrackedObjects
    // in DetectObjectsEvent, it will add to detectedEventsToProcess
    // if object id is in both vectors, it will send the object to the FusionSlam service
    protected void initialize() {
        // the thread is automatically registered to the relevant broadcasts and events thanks to its type
        // according to what i understand- the lidar working only happens after recieving the event
        messageBus.register(this);
        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast->{
            System.out.println("recieved termination at lidar" + liDarWorkerTracker.getId() + "TERMINATING");
            terminate();
        });
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.out.println("crashed broadcast" + crashedBroadcast.toString() + "\nrecieved termination at lidar" + liDarWorkerTracker.getId() + "TERMINATING");
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, (currentTick)->{
            // for curr tick, if there's a DetectObjectsEvent send event to FusionSlam
            if (liDarWorkerTracker.getStatus() == STATUS.UP){
                liDarWorkerTracker.fetchByTime(currentTick.getTick() + liDarWorkerTracker.getFrequency());
                // TODO understand how and when to terminate
                Vector<TrackedObject> trackedObjects = new Vector<>();
                for (TrackedObject p : liDarWorkerTracker.getLastTrackedObjects()){
                    if (p.getId() == "ERROR"){
                        sendBroadcast(new CrashedBroadcast(liDarWorkerTracker.getId() , "lidar recieved Error, CRASHED at tick " + currentTick.getTick()));
                        terminate();
                        return; // TODO WE NEED TO RESOLVE FUTURE OF EVENT?
                    }
                    
                    // otherwise, the frequency is for sure good enough, thus we can try to process the event
                    else{
                        // search the corresponding event in service map
                        if (objectsToProcess.containsKey(p.getId())){
                            // the event exists
                            DetectedObject e = objectsToProcess.get(p.getId());
    
                            trackedObjects.add(new TrackedObject(e.getId(), currentTick.getTick(),e.getDescripition(),p.getCloudPoint())); 
    
                        }
                    }
                }
                sendEvent(new TrackedObjectsEvent(trackedObjects, currentTick.getTick()));
    
            }

        });

        subscribeEvent(DetectObjectsEvent.class, (detectObjectsEvent)->{
            for (DetectedObject d : detectObjectsEvent.getObjectDetails().getDetectedObjects()){
                objectsToProcess.put(d.getId(), d);
            }
            
        });
    }
}
