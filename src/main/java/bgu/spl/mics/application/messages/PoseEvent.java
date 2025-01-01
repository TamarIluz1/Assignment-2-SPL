package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.Future;

public class PoseEvent implements Event<Pose>{
    // we need to implement in the futare

    private final Pose pose;


    public PoseEvent(Pose pose) {
        this.pose = pose;
    }

    public Pose getPose(){
        return pose;
    }




}
