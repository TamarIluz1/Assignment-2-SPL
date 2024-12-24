package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TickBroadcast;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private GPSIMU gpsimu;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
        // TODO Implement this
        this.gpsimu = gpsimu;
    }


    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {
        // TODO Implement this

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            // Handle the TickBroadcast message
            System.out.println("Received TickBroadcast: " + tickBroadcast.getTick());
            // Send a PoseEvent in response to the TickBroadcast
            

            //we need to find a way to make it work !!!
            //not working now
            //needs to understand how to use GPSIMU
            PoseEvent event =  new PoseEvent(gpsimu.getPose());
            sendEvent(event);
        });
        
    }
}
