package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.TickBroadcast;

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
        int i=1; // i is the current tick
        while(i<=Duration){
            try {
                Thread.sleep(TickTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendBroadcast(new TickBroadcast(i));
            i++;
        }
        terminate();
    }
}
