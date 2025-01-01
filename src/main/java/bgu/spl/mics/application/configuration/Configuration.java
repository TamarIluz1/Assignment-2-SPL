package bgu.spl.mics.application.configuration;

public class Configuration {
    private CameraConfig Cameras;
    private LiDarConfig LiDarWorkers;
    private String poseJsonFile;
    private int TickTime;
    private int Duration;

    // Getters and setters
    public CameraConfig getCameras() { return Cameras; }
    public void setCameras(CameraConfig cameras) { this.Cameras = cameras; }
    public LiDarConfig getLiDarWorkers() { return LiDarWorkers; }
    public void setLiDarWorkers(LiDarConfig liDarWorkers) { this.LiDarWorkers = liDarWorkers; }
    public String getPoseJsonFile() { return poseJsonFile; }
    public void setPoseJsonFile(String poseJsonFile) { this.poseJsonFile = poseJsonFile; }
    public int getTickTime() { return TickTime; }
    public void setTickTime(int tickTime) { this.TickTime = tickTime; }
    public int getDuration() { return Duration; }
    public void setDuration(int duration) { this.Duration = duration; }
}
