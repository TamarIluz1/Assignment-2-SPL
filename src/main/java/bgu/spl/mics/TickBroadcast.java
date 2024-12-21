package bgu.spl.mics;

/**
 * A "Marker" interface extending {@link Message}. When sending a Broadcast message
 * using the {@link MessageBus}, it will be received by all the subscribers of this
 * Broadcast-message type (the message Class).
 */
public class TickBroadcast implements Broadcast {
    private int tick;

    public TickBroadcast(int tick) {
        this.tick = tick;
    }

    public int getTick() {
        return tick;
    }
}