package bgu.spl.mics.application.configuration;
import java.util.List;
import java.util.Vector;

public class LiDarConfig {
    private List<LidarConfiguration> LidarConfigurations;
    private String lidars_data_path;

    // Getters and setters
    public List<LidarConfiguration> getLidarConfigurations() { return LidarConfigurations; }
    public void setLidarConfigurations(Vector<LidarConfiguration> lidarConfigurations) { this.LidarConfigurations = lidarConfigurations; }
    public String getLidarsDataPath() { return lidars_data_path; }
    public void setLidarsDataPath(String lidars_data_path) { this.lidars_data_path = lidars_data_path; }
}
