package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.TrackedObject;
import java.util.Vector;
import bgu.spl.mics.Future;

import java.util.concurrent.CompletableFuture;

public class TrackedObjectsEvent implements Event<Boolean>{
    // we need to implement in the future

    @Override
    public Future<Boolean> getFuture() {
        // TODO and 
        // Implementation of the method
        return future; // Replace with actual implementation
    }

    private final Vector<TrackedObject> trackedObjects; // TODO not sure if we need to use vector- instructions say list
    private final int tickTime;
    private Future<Boolean> future = new Future<>();
    
    public TrackedObjectsEvent(Vector<TrackedObject> trackedObjects, int tickTime){
        this.trackedObjects = trackedObjects;
        this.tickTime = tickTime;
    }   

    public Vector<TrackedObject> getTrackedObject(){
        return trackedObjects;
    }

    public int getTickTime() {
        return tickTime;
    }

	@Override
	public void complete() {
		// TODO Auto-generated method stub
        future.resolve(true);
	}


}
