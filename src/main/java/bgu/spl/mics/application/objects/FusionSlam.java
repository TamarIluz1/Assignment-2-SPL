package bgu.spl.mics.application.objects;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import bgu.spl.mics.application.messages.TrackedObjectsEvent;
public class FusionSlam {
   
    CopyOnWriteArrayList<LandMark> landmarks;// changed from [] array to List bt tamar 31/12
    ArrayList<Pose> poses;
    int terminatedCounter; 
    Object terminatedCounterLock= new Object();
    ArrayList<TrackedObjectsEvent> unhandledTrackedObjects;
    int sensorAmount; 
    private FusionSlam() {
        this.landmarks = new CopyOnWriteArrayList<>();
        this.poses = new ArrayList<>();
        unhandledTrackedObjects = new ArrayList<>();
        terminatedCounter = 0;
        this.sensorAmount = 0;

    }
     // Singleton instance holder
    public static FusionSlam getInstance() {
        return FusionSlamHolder.instance;
    }

    private static class FusionSlamHolder {
        private static final FusionSlam instance = new FusionSlam();
    }

    /**
     * Adds a new pose to the system.
     *
     * @param pose The pose to add.
     */
    public synchronized void addPose(Pose pose) {
        poses.add(pose);
    }

    /**
     * Retrieves all poses in the system.
     *
     * @return A copy of the List of poses.
     */
    public synchronized List<Pose> getPoses() {
        return new ArrayList <>(poses); // Return a copy for thread safety
    }

    /**
     * Adds a new landmark to the system.
     *
     * @param landmark The landmark to add.
     */
    public void addLandmark(LandMark landmark) {
        synchronized(landmarks){
            landmarks.add(landmark);
        }
        
    }

    public ArrayList<CloudPoint> convertToGlobal(List<CloudPoint> localCoordinates, Pose pose) {
        ArrayList<CloudPoint> globalCoordinates = new ArrayList<>();
        for (CloudPoint localCP : localCoordinates) {
            globalCoordinates.add(transform(localCP, pose));
        }
        return globalCoordinates;
    }
    
    public CloudPoint transform(CloudPoint localCP, Pose pose) {
        // Convert yaw angle to radians
        double yawRadians = Math.toRadians(pose.getYaw());
        
        // Extract pose data
        double robotX = pose.getX();
        double robotY = pose.getY();
    
        // Extract local point data
        double localX = localCP.getX();
        double localY = localCP.getY();
    
        // Apply rotation and translation
        double globalX = Math.cos(yawRadians) * localX - Math.sin(yawRadians) * localY + robotX;
        double globalY = Math.sin(yawRadians) * localX + Math.cos(yawRadians) * localY + robotY;
    
        // Return the transformed CloudPoint
        return new CloudPoint(globalX, globalY); // Z remains unchanged
    }
    
    /**
     * Retrieves all landmarks in the system.
     *
     * @return A copy of the List of landmarks.
     */
    public ArrayList<LandMark> getLandmarks() {
        synchronized(landmarks){
            return new ArrayList <>(landmarks); // Return a copy for thread safety

        }
    }

    /**
     * Finds a landmark by its ID.
     *
     * @param id The ID of the landmark.
     * @return The landmark with the specified ID, or null if not found.
     */
    public synchronized LandMark findLandmarkById(String id) {
        synchronized(landmarks){
            for (LandMark landmark : landmarks) {
                if (landmark.getId().equals(id)) {
                    return landmark;
                }
            }
            return null;
        }

    }

    public void addOrUpdateLandmark(String id, String newDescription, ArrayList <CloudPoint> newCoordinates) {
        // Search for existing landmark by ID
        LandMark existingLandmark = null;
        synchronized (landmarks) {
            
            for (LandMark landmark : landmarks) {
                if (landmark.id.equals(id)) {
                    existingLandmark = landmark;
                    break;
                }
            }
        
            if (existingLandmark == null) {
                // Add new landmark
                LandMark newLandmark = new LandMark(id, newDescription, newCoordinates);
                newLandmark.id = id;
                newLandmark.description = newDescription;
                newLandmark.coordinates = newCoordinates;
                landmarks.add(newLandmark);
                // Increment the number of uniqe landmarks
                StatisticalFolder.getInstance().incrementLandMarks();
                
                System.out.println("New landmark added with ID: " + id);
            } else {
                // Update existing landmark
                ArrayList<CloudPoint> refinedCoordinates = new ArrayList<>();
                int existingSize = existingLandmark.coordinates.size();
                int newSize = newCoordinates.size();
        
                // Average coordinates
                for (int i = 0; i < Math.min(existingSize, newSize); i++) {
                    CloudPoint existingCP = existingLandmark.coordinates.get(i);
                    CloudPoint newCP = newCoordinates.get(i);
        
                    double avgX = (existingCP.getX() + newCP.getX()) / 2;
                    double avgY = (existingCP.getY() + newCP.getY()) / 2;
        
                    refinedCoordinates.add(new CloudPoint(avgX, avgY));
                }
        
                // Handle excess coordinates if the new List is longer
                for (int i = Math.min(existingSize, newSize); i < newSize; i++) {
                    refinedCoordinates.add(newCoordinates.get(i));
                }
        
                existingLandmark.coordinates = refinedCoordinates;
        
                System.out.println("Updated landmark with ID: " + id);
            }
    

        }
    }
        
    public int getNumLandmarks() {
        synchronized(landmarks){
            return landmarks.size();
        }
    }

    public void reportTracked(){
        synchronized(terminatedCounterLock){
            terminatedCounter++;
        }
    }

    public synchronized void setSensorAmount(int sensorAmount){
        this.sensorAmount = sensorAmount;

    }

    public synchronized void addUnhandledTrackedObject(TrackedObjectsEvent e){
        unhandledTrackedObjects.add(e);
    }

    public synchronized List<TrackedObjectsEvent> getUnhandledTrackedObjects(){
        return unhandledTrackedObjects;
    }

    public synchronized void removeHandledTrackedObjects(ArrayList<TrackedObjectsEvent> handledEvents) {
        unhandledTrackedObjects.removeAll(handledEvents);
    }
   

    public synchronized boolean isFinished(){
        synchronized(terminatedCounterLock){
            if (terminatedCounter == sensorAmount){
                return true;
            }
            else{
                return false;
            }
        }
    }

    public synchronized Pose getPoseByTime(int time){
        for (Pose p : poses){
            if (p.getTime() == time){
                return p;
            }
        }
        return null;
    }


    /**
     * Processes tracked objects into landmarks (for testing purposes).
     * 
     * @param trackedObjectsEvent The event containing tracked objects to be processed.
     */
    public synchronized void processTrackedObjectsToLandmarks(TrackedObjectsEvent trackedObjectsEvent) {
        int time = trackedObjectsEvent.getTickTime();
        Pose pose = getPoseByTime(time);

        if (pose == null) {
            throw new IllegalStateException("Pose not found for time: " + time);
        }

        trackedObjectsEvent.getTrackedObject().forEach(trackedObject -> {
            String id = trackedObject.getId();
            String description = trackedObject.getDescription();
            ArrayList<CloudPoint> trackedCoordinates = trackedObject.getCloudPoint();

            // Transform coordinates to the global map
            ArrayList<CloudPoint> globalCoordinates = convertToGlobal(trackedCoordinates, pose);

            // Add or update the landmark
            addOrUpdateLandmark(id, description, globalCoordinates);
        });
    }


    
}
