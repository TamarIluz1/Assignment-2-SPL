package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.TrackedObject;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.TickBroadcast;

import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedCloudPoints;
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
    
    public void terminateService(){
        sendBroadcast(new TerminatedBroadcast("lidar"));
        this.terminate();
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
            if (terminateBroadcast.getSender() == "time"){
                System.out.println("recieved termination at lidar" + liDarWorkerTracker.getId() + "TERMINATING");
                terminateService();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.out.println("crashed broadcast" + crashedBroadcast.toString() + "\nrecieved termination at lidar" + liDarWorkerTracker.getId() + "TERMINATING");
            liDarWorkerTracker.setStatus(STATUS.ERROR);
            terminateService();
        });

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            // for curr tick, if there's a DetectObjectsEvent send event to FusionSlam
            if (liDarWorkerTracker.getStatus() == STATUS.UP){
                if (liDarWorkerTracker.isFinished()){
                    liDarWorkerTracker.setStatus(STATUS.DOWN);
                    terminateService();
                }
                Vector<StampedCloudPoints> newCloudPoints = liDarWorkerTracker.getNewCloudPointsUntilTime(tickBroadcast.getTick() + liDarWorkerTracker.getFrequency());
                Vector<DetectObjectsEvent> handled = new Vector<>();
                Vector<TrackedObject> newlyTracked = new Vector<>();
                TrackedObject curr;
                for (StampedCloudPoints s : newCloudPoints){
                    // if the relevant event is availiable- add the tracked objects
                    if (s.getId() == "ERROR"){
                        liDarWorkerTracker.setStatus(STATUS.ERROR);
                        sendBroadcast(new CrashedBroadcast(liDarWorkerTracker.getId(), "Lidar crashed at tick" + tickBroadcast.getTick()));
                        terminateService();
                        return;
                    }
                    for (DetectObjectsEvent e :liDarWorkerTracker.getEventsRecieved()){
                        for (DetectedObject d : e.getObjectDetails().getDetectedObjects()){
                            if (d.getId() == s.getId()){
                                // we can create the object
                                handled.add(e);
                                curr = new TrackedObject(d.getId(), tickBroadcast.getTick(), d.getDescripition(),s.getCloudPoints());
                                newlyTracked.add(curr);
                                liDarWorkerTracker.reportTracked();
                                liDarWorkerTracker.addLastTrackedObject(curr);
                            }
                        }
                    }
                }
                liDarWorkerTracker.handleProcessedDetected(handled);
                sendEvent(new TrackedObjectsEvent(newlyTracked,tickBroadcast.getTick()));
            }
        });

        subscribeEvent(DetectObjectsEvent.class, detectObjectsEvent -> {
            liDarWorkerTracker.addNewDetectEvent(detectObjectsEvent);
        });
    
    }
}
