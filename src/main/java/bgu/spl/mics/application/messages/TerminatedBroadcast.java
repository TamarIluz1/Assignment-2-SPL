package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminatedBroadcast implements Broadcast{
    // we need to implement in the futare
    String sender;
    public TerminatedBroadcast(String sender) {
        this.sender = sender; // camera, time, lidar
    }

    @Override
    public String toString() {
        return "TerminatedBroadcast sent by " + sender;
    }

    public String getSender(){
        return sender;
    }

    
}
