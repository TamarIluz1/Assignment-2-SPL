package bgu.spl.mics.application.configuration;

public class CameraConfiguration {
    private int id;
    private int frequency;
    private String camera_key;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    public String getCamera_key() { return camera_key; }
    public void setCamera_key(String camera_key) { this.camera_key = camera_key; }
}