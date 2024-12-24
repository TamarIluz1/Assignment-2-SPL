package bgu.spl.mics.application.objects;

/**
 * CloudPoint represents a specific point in a 3D space as detected by the LiDAR.
 * These points are used to generate a point cloud representing objects in the environment.
 */
public class CloudPoint {

    // TODO: Define fields and methods.
    private final double x;
    private final double y;

    public CloudPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Getters and setters (if needed)
    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
