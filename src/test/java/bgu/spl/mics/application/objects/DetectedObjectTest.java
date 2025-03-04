package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DetectedObjectTest {

    @Test
    public void testConstructorAndGetters() {
        String id = "Wall_1";
        String description = "A wall object";
        DetectedObject detectedObject = new DetectedObject(id, description);

        assertEquals(id, detectedObject.getId(), "The ID should match the constructor input");
        assertEquals(description, detectedObject.getDescripition(), "The description should match the constructor input");
    }

    @Test
    public void testEquality() {
        DetectedObject obj1 = new DetectedObject("Door_1", "A door object");
        DetectedObject obj2 = new DetectedObject("Door_1", "A door object");
        DetectedObject obj3 = new DetectedObject("Window_1", "A window object");

        assertEquals(obj1.toString(), obj2.toString(), "Objects with the same ID and description should be equal");
        assertNotEquals(obj1.toString(), obj3.toString(), "Objects with different IDs or descriptions should not be equal");
    }


    @Test
    public void testStringRepresentation() {
        DetectedObject detectedObject = new DetectedObject("Lamp_1", "A lamp object");
        String expected = "DetectedObject{id='Lamp_1', description='A lamp object'}";
        assertEquals(expected, detectedObject.toString(), "String representation should match the expected format");
    }

    @Test
    public void testNullDescription() {
        DetectedObject detectedObject = new DetectedObject("Object_1", null);

        assertEquals("Object_1", detectedObject.getId(), "ID should be 'Object_1'");
        assertNull(detectedObject.getDescripition(), "Description should be null");
    }
}
