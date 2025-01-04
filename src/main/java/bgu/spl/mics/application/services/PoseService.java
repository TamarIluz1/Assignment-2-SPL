package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.GurionRockRunner;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.GPSIMU;
import bgu.spl.mics.application.objects.STATUS;

/**
 * PoseService is responsible for maintaining the robot's current pose (position and orientation)
 * and broadcasting PoseEvents at every tick.
 */
public class PoseService extends MicroService {

    private final  GPSIMU gpsimu;
    /**
     * Constructor for PoseService.
     *
     * @param gpsimu The GPSIMU object that provides the robot's pose data.
     */
    public PoseService(GPSIMU gpsimu) {
        super("PoseService");
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
        System.out.println("PoseService registered to MessageBus.");

        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast->{
            if (terminateBroadcast.getSender().equals("time")){
                gpsimu.setStatus(STATUS.DOWN);
                terminate();
            }
        });
        
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            // TODO Implement this
            //SUBSCRIBE TO CRASHED BROADCAST 30.12 TAMAR
            System.out.println("POSE CRASHBROADCAST received, terminating PoseService.");
            gpsimu.setStatus(STATUS.ERROR);
            terminate();
        });

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            System.out.println("POSE Received TickBroadcast at tick: " + tickBroadcast.getTick());
            gpsimu.setCurrentTick(tickBroadcast.getTick());
            if (gpsimu.getStatus() == STATUS.UP){
                gpsimu.setCurrentTick(tickBroadcast.getTick());
                // invariant- the pose is updated every tick, when the pose is null, the poses are finished
                if (gpsimu.getCurrentPose() == null){
                    System.out.println("POSE out of poses, terminates " + tickBroadcast.getTick());

                    terminateService();
                }
                else{
                    PoseEvent event = new PoseEvent(gpsimu.getCurrentPose());
                    sendEvent(event);
                    System.out.println("PoseEvent sent for tick: " + tickBroadcast.getTick());
                
                    GurionRockRunner.getPoses().add(gpsimu.getCurrentPose());

                }

            }
            else if(gpsimu.getStatus().equals(STATUS.DOWN)){
                System.out.println("POSE Terminating at tick: " + tickBroadcast.getTick());
                terminateService();
            }

        });

        System.out.println("PoseService initialized successfully.");
        
    }
}
