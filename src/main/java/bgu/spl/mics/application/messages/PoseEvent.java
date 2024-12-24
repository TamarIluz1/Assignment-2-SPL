package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Pose>{
    // we need to implement in the futare

    private final Pose pose;

    public PoseEvent(int x, int y,int yaw, int time) {
        this.pose = new Pose(x,y,yaw,time);    
    }

    public Pose getPose(){
        return pose;
    }

}
