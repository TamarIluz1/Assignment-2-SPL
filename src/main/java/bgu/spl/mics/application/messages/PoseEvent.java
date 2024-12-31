package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Pose;
import bgu.spl.mics.Future;

public class PoseEvent implements Event<Pose>{
    // we need to implement in the futare

    private final Pose pose;
    private final Future<Pose> future;

    public PoseEvent(Pose pose) {
        this.pose = pose;
        future = new Future<>();    
    }

    public Pose getPose(){
        return pose;
    }

    @Override
    public Future<Pose> getFuture() {
        return future;
    }

    @Override
    public void complete(Pose result) {
        future.resolve(result);
    }



}
