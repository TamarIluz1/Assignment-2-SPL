package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CameraTest {

    @Test
    public void testConstructor() {
        Camera camera = new Camera(1, 10, STATUS.UP);
        assertEquals(1, camera.getId());
        assertEquals(10, camera.getFrequency());
        assertEquals(STATUS.UP, camera.getStatus());
    }

    @Test
    public void testGettersAndSetters() {
        Camera camera = new Camera(1, 10, STATUS.UP);
        camera.setStatus(STATUS.DOWN);
        assertEquals(STATUS.DOWN, camera.getStatus());
    }

    /**
     * Test: Verifies that the camera processes data correctly before sending.
     * Pre-conditions:
     *  - Camera has valid raw data.
     * Post-conditions:
     *  - Processed data is correct and matches expectations.
     */
    @Test
    public void testCameraDataProcessing() {
        Camera camera = new Camera(1, 10, STATUS.UP);
        camera.setDetectedObjectsList(generateTestDetectedObjects());

        ArrayList<StampedDetectedObjects> processedData = camera.getDetectedObjectsList();

        assertNotNull(processedData, "Processed data should not be null.");
        assertEquals(2, processedData.size(), "Processed data should have 2 entries.");
        assertEquals(1, processedData.get(0).getTimestamp(), "First entry time should be 1.");
        assertEquals("Wall_1", processedData.get(0).getDetectedObjects().get(0).getId(), "First detected object ID should be Wall_1.");
    }

    /**
     * Test: Verifies that invalid camera data is handled appropriately.
     * Pre-conditions:
     *  - Camera receives invalid or null data.
     * Post-conditions:
     *  - Camera should not process invalid data.
     */
    @Test
    public void testCameraInvalidDataHandling() {
        Camera camera = new Camera(1, 10, STATUS.UP);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            camera.setDetectedObjectsList(null);
        });

        assertEquals("Detected objects list cannot be null", exception.getMessage());
    }

    /**
     * Generates a list of test `StampedDetectedObjects` for testing.
     */
    private ArrayList<StampedDetectedObjects> generateTestDetectedObjects() {
        ArrayList<StampedDetectedObjects> testObjects = new ArrayList<>();

        ArrayList<DetectedObject> detectedObjects1 = new ArrayList<>();
        detectedObjects1.add(new DetectedObject("Wall_1", "Wall"));
        testObjects.add(new StampedDetectedObjects(1, detectedObjects1));

        ArrayList<DetectedObject> detectedObjects2 = new ArrayList<>();
        detectedObjects2.add(new DetectedObject("Wall_2", "Wall"));
        detectedObjects2.add(new DetectedObject("Chair_1", "Chair"));
        testObjects.add(new StampedDetectedObjects(2, detectedObjects2));

        return testObjects;
    }
}
