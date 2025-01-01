package bgu.spl.mics.application.configuration;
import java.util.Vector;


public  class CameraConfig {
        private Vector<CameraConfiguration> CamerasConfigurations;
        private String camera_datas_path;

        public Vector<CameraConfiguration> getCamerasConfigurations() { return CamerasConfigurations; }
        public void setCamerasConfigurations(Vector<CameraConfiguration> CamerasConfigurations) { this.CamerasConfigurations = CamerasConfigurations; }
        public String getCameraDatasPath() { return camera_datas_path; }
        public void setCameraDatasPath(String camera_datas_path) { this.camera_datas_path = camera_datas_path; }
}
