package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast{
    // we need to implement in the futare
    String objectID; // camera, lidar
    String message;
    public CrashedBroadcast( String objectID, String message) {
        this.objectID = objectID;
        this.message = message;
    }

    @Override
    public String toString() {
        return "CrashedBroadcast of " +objectID + " with message: " + message;
    }

    public void print(){
        System.out.println("CrashedBroadcast");
    }

    


}
