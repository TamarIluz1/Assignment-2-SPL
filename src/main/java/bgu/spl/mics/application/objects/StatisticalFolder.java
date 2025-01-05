package bgu.spl.mics.application.objects;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds statistical information about the system's operation.
 * This class aggregates metrics such as the runtime of the system,
 * the number of objects detected and tracked, and the number of landmarks identified.
 */
public class StatisticalFolder {
    // TODO: Define fields and methods for statistics tracking.
    
    private int systemRuntime;// changed to long from int
    private final AtomicInteger numDetectedObjects;
    private final AtomicInteger numTrackedObjects;
    private final AtomicInteger numLandMarks;

    private StatisticalFolder() {
        this.numDetectedObjects = new AtomicInteger(0);
        this.numTrackedObjects = new AtomicInteger(0);
        this.numLandMarks = new AtomicInteger(0);
    }

    private static class StatisticalFolderHolder {
        private static final StatisticalFolder instance = new StatisticalFolder();
    }

    public static StatisticalFolder getInstance() {
        if(StatisticalFolderHolder.instance == null) {
            return new StatisticalFolder();
        }
        return StatisticalFolderHolder.instance;
    }

    public synchronized void setSystemRuntime(int runtime) {
        this.systemRuntime = runtime;
    }

    public synchronized void incrementDetectedObjects(int count) {
        numDetectedObjects.addAndGet(count);
    }

    public synchronized void incrementTrackedObjects(int count) {
        numTrackedObjects.addAndGet(count);
    }

    public synchronized void incrementLandMarks() {
        numLandMarks.incrementAndGet();
    }

    public synchronized int getSystemRuntime() {
        return systemRuntime;
    }

    public  synchronized int getNumDetectedObjects() {
        return numDetectedObjects.get();
    }

    public synchronized int getNumTrackedObjects() {
        return numTrackedObjects.get();
    }

    public synchronized int getNumLandMarks() {
        return numLandMarks.get();
    }

}
