package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
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

    public void terminateService(){
        sendBroadcast(new TerminatedBroadcast("pose"));
        this.terminate();
    }


    /**
     * Initializes the PoseService.
     * Subscribes to TickBroadcast and sends PoseEvents at every tick based on the current pose.
     */
    @Override
    protected void initialize() {

        messageBus.register(this);
        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast->{
            if (terminateBroadcast.getSender() == "time"){
                gpsimu.setStatus(STATUS.DOWN);
                terminate();
            }
        });
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            // TODO Implement this
            //SUBSCRIBE TO CRASHED BROADCAST 30.12 TAMAR
            gpsimu.setStatus(STATUS.ERROR);
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            if (gpsimu.getStatus() == STATUS.UP){
                System.out.println("Received TickBroadcast at tick: " + tickBroadcast.getTick());
                gpsimu.setCurrentTick(tickBroadcast.getTick());
                // invariant- the pose is updated every tick, when the pose is null, the poses are finished
                if (gpsimu.getCurrentPose() == null){
                    System.out.println("Poses are finished");
                    terminateService();
                }
                else{
                    PoseEvent event = new PoseEvent(gpsimu.getCurrentPose());
                    sendEvent(event);
                    System.out.println("PoseEvent sent for tick: " + tickBroadcast.getTick());
                }

            }

        });
        
    }
}
