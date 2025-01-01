package bgu.spl.mics.application.configuration;
import java.util.List;
import java.util.Vector;

public class CameraConfig {
    private List<CameraConfiguration> CamerasConfigurations;
    private String camera_datas_path;

    public List<CameraConfiguration> getCamerasConfigurations() {
        return CamerasConfigurations;
    }

    public void setCamerasConfigurations(List<CameraConfiguration> camerasConfigurations) {
        CamerasConfigurations = camerasConfigurations;
    }

    public String getCameraDatasPath() {
        return camera_datas_path;
    }

    public void setCameraDatasPath(String camera_datas_path) {
        this.camera_datas_path = camera_datas_path;
    }
}
