package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;


/**
 * TimeService acts as the global timer for the system, broadcasting TickBroadcast messages
 * at regular intervals and controlling the simulation's duration.
 */
public class TimeService extends MicroService {

    /**
     * Constructor for TimeService.
     *
     * @param TickTime  The duration of each tick in milliseconds.
     * @param Duration  The total number of ticks before the service terminates.
     */

    long TickTime,Duration;
    volatile boolean isTerminated = false;
    private Thread tickThread;

    

    public TimeService(long TickTime, long Duration) {
        super("TimeService");
        this.TickTime = TickTime;
        this.Duration = Duration;
    }

    public void terminateTime() {
        isTerminated = true;
        if (tickThread != null) {
            tickThread.interrupt(); // Stop the thread if sleeping
        }
        super.terminate();
    }


    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {

        messageBus.register(this);
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            //SUBSCRIBE TO CRASHED BROADCAST 30.12 TAMAR
            System.out.println("TIME CRASHBROADCAST received, terminating TimeService.");
            terminateTime();
        });

        subscribeBroadcast(TerminatedBroadcast.class, terminateBroadcast->{
            if (terminateBroadcast.getSender().equals("fusionslam")){
                isTerminated = true;
                terminateTime();
            }
        });

        // Start a new thread to handle tick broadcasting
        tickThread = new Thread(() -> {
        int currentTick = 1;
        while (currentTick <= Duration && !isTerminated) {
            try {
                Thread.sleep(TickTime*1000);// added delay Tamar 3.1
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (isTerminated) {
                break;
            }
            sendBroadcast(new TickBroadcast(currentTick));
            System.out.println("TimeService " + getName() + " sent TickBroadcast with tick " + currentTick);
            currentTick++;
        }
        sendBroadcast(new TerminatedBroadcast("time"));
        terminateTime();
        
        
        });
        tickThread.start();


    }
}
