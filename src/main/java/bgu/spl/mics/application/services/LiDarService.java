package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.objects.LiDarWorkerTracker;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.TickBroadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.STATUS;
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
    private final LiDarWorkerTracker LiDarWorkerTracker;
    /**
     * Constructor for LiDarService.
     *
     * @param liDarTracker The LiDAR tracker object that this service will use to process data.
     */
    public LiDarService(LiDarWorkerTracker LiDarWorkerTracker) {
        super("LidarService");
        this.LiDarWorkerTracker = LiDarWorkerTracker;
    }

    /**
     * Initializes the LiDarService.
     * Registers the service to handle DetectObjectsEvents and TickBroadcasts,
     * and sets up the necessary callbacks for processing data.
     */
    @Override
    protected void initialize() {
        // the thread is automatically registered to the relevant broadcasts and events thanks to its type

        messageBus.register(this);
        subscribeBroadcast(TickBroadcast.class, (currentTick)->{
            if (this.LiDarWorkerTracker.getStatus() == STATUS.UP) {
                if (currentTick.getTick() % LiDarWorkerTracker.getFrequency() == 0) {
                    // LI-DAR worker ready to get new data
                    // to be implemented- check if i need to parse 
                    
                }
            }
            else if (this.LiDarWorkerTracker.getStatus() == STATUS.ERROR){
                // TODO print error message
            }
            else{
                // LiDar worker is down
            }

        });
        Thread lidarThread = new Thread(() -> {
            // TODO Implement this

        });
        lidarThread.start();
    }
}
