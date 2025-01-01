package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.TickBroadcast;
import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.TerminatedBroadcast;


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

    

    public TimeService(long TickTime, long Duration) {
        super("TimeService");
        // TODO Implement this
        this.TickTime = TickTime;
        this.Duration = Duration;
    }

    /**
     * Initializes the TimeService.
     * Starts broadcasting TickBroadcast messages and terminates after the specified duration.
     */
    @Override
    protected void initialize() {
        // TODO Implement this

        messageBus.register(this);
        subscribeBroadcast(CrashedBroadcast.class, crashedBroadcast -> {
            // TODO Implement this
            //SUBSCRIBE TO CRASHED BROADCAST 30.12 TAMAR
            terminate();
        });

        // Start a new thread to handle tick broadcasting
        Thread tickThread = new Thread(() -> {
        int currentTick = 0;
        while (currentTick <= Duration) {
            try {
                Thread.sleep(TickTime*1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            sendBroadcast(new TickBroadcast(currentTick));
            System.out.println("TimeService " + getName() + " sent TickBroadcast with tick " + currentTick);
            currentTick++;
        }
        sendBroadcast(new TerminatedBroadcast("time"));
        terminate();
        });
        tickThread.start();


    }
}
