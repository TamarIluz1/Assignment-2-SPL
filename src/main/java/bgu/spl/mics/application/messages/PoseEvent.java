package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;

public class PoseEvent implements Event<Pose>{
    // we need to implement in the futare

    private final Pose pose;

<<<<<<< HEAD
    public PoseEvent(){
        
    }

    public PoseEvent(Pose pose){
        this.pose = pose; 
=======
    public PoseEvent(Pose pose) {
        this.pose = pose;    
>>>>>>> aa217fd045c3559ecd17560ed13dcc5ba603c454
    }

    public Pose getPose(){
        return pose;
    }

}
