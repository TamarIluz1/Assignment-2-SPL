package bgu.spl.mics.application.services;

import java.util.ArrayList;

import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.objects.StampedCloudPoints;
import bgu.spl.mics.application.objects.TrackedObject;
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
    ArrayList<StampedCloudPoints> ToProcessCloudPoints = new ArrayList<>();
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
    // if object id is in both ArrayLists, it will send the object to the FusionSlam service
    protected void initialize() {
        // the thread is automatically registered to the relevant broadcasts and events thanks to its type
        // according to what i understand- the lidar working only happens after recieving the event
        messageBus.register(this);
        
        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast->{
            if (terminateBroadcast.getSender().equals("time")){
                System.out.println("recieved termination at lidar" + liDarWorkerTracker.getId() + "TERMINATING");
                terminateService();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            System.out.println("crashed broadcast" + crashedBroadcast.toString() + "LIDAR" + liDarWorkerTracker.getId() + "TERMINATING");
            liDarWorkerTracker.setStatus(STATUS.ERROR);
            terminateService();
        });

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            // for curr tick, if there's a DetectObjectsEvent send event to FusionSlam
            if (liDarWorkerTracker.getStatus() == STATUS.UP){
                if (liDarWorkerTracker.isFinished()){
                    liDarWorkerTracker.setStatus(STATUS.DOWN);
                    System.out.println("Lidar terminated at tick "+ tickBroadcast.getTick());
                    terminateService();
                }
                ArrayList<StampedCloudPoints> newCloudPoints = liDarWorkerTracker.getNewCloudPointsByTime(tickBroadcast.getTick() + liDarWorkerTracker.getFrequency());//TODO  is it the right way to get the new cloud points? Tamar 3.1
                ArrayList<StampedCloudPoints> processedCloudPoints = new ArrayList<>();
                for (StampedCloudPoints s : newCloudPoints){
                    ToProcessCloudPoints.add(s);
                }
                ArrayList<DetectObjectsEvent> handled = new ArrayList<>();
                ArrayList<TrackedObject> newlyTracked = new ArrayList<>();
                TrackedObject curr;
                for (StampedCloudPoints s : ToProcessCloudPoints){
                    // if the relevant event is availiable- add the tracked objects
                    if (s.getId().equals("ERROR")){
                        liDarWorkerTracker.setStatus(STATUS.ERROR);
                        GurionRockRunner.setSystemCrashed(true);
                        GurionRockRunner.setFaultySensor("LiDar" + liDarWorkerTracker.getId());
                        sendBroadcast(new CrashedBroadcast("Lidar"+liDarWorkerTracker.getId(), "Lidar crashed at tick" + tickBroadcast.getTick()));
                        terminateService();
                        return;
                    }
                    for (DetectObjectsEvent e :liDarWorkerTracker.getEventsRecieved()){
                        for (DetectedObject d : e.getObjectDetails().getDetectedObjects()){
                            
                            if (d.getId().equals(s.getId())){
                                // we can create the object
                                handled.add(e);
                                processedCloudPoints.add(s);
                                curr = new TrackedObject(d.getId(), tickBroadcast.getTick(), d.getDescripition(),s.getCloudPoints());
                                newlyTracked.add(curr);
                                liDarWorkerTracker.reportTracked();
                                liDarWorkerTracker.addLastTrackedObject(curr);
                            }
                            
                        }
                    }
                }
                liDarWorkerTracker.handleProcessedDetected(handled);
                for (StampedCloudPoints s : processedCloudPoints){
                    ToProcessCloudPoints.remove(s);
                }

                if (!newlyTracked.isEmpty()){
                    sendEvent(new TrackedObjectsEvent(newlyTracked,tickBroadcast.getTick()));
                }

                if (!newCloudPoints.isEmpty() && !GurionRockRunner.isSystemCrashed() ) {
                    String workerName = "LiDarWorkerTracker" + liDarWorkerTracker.getId();
                    StampedCloudPoints lastFrame = newCloudPoints.get(newCloudPoints.size() - 1);
                    GurionRockRunner.getLastLiDarWorkerTrackersFrame().put(workerName, lastFrame);
                }
            }
        });

        subscribeEvent(DetectObjectsEvent.class, detectObjectsEvent -> {
            liDarWorkerTracker.addNewDetectEvent(detectObjectsEvent);
        });

        System.out.println("LidarService initialized successfully.");
        
    
    }


}
