package bgu.spl.mics.application.objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CloudPointTest {

    @Test
    public void testConstructorAndGetters() {
        double x = 1.0;
        double y = 2.0;
        CloudPoint point = new CloudPoint(x, y);

        assertEquals(x, point.getX(), 0.001);
        assertEquals(y, point.getY(), 0.001);
    }

    @Test
    public void testEquality() {
        CloudPoint point1 = new CloudPoint(3.5, 4.5);
        CloudPoint point2 = new CloudPoint(3.5, 4.5);
        CloudPoint point3 = new CloudPoint(4.0, 5.0);

        assertEquals(point1, point2, "Points with the same coordinates should be equal");
        assertNotEquals(point1, point3, "Points with different coordinates should not be equal");
    }

    @Test
    public void testHashCode() {
        CloudPoint point1 = new CloudPoint(3.5, 4.5);
        CloudPoint point2 = new CloudPoint(3.5, 4.5);
        CloudPoint point3 = new CloudPoint(4.0, 5.0);

        assertEquals(point1.hashCode(), point2.hashCode(), "Hashcodes should match for equal points");
        assertNotEquals(point1.hashCode(), point3.hashCode(), "Hashcodes should differ for different points");
    }

    @Test
    public void testStringRepresentation() {
        CloudPoint point = new CloudPoint(2.0, 3.0);
        String expected = "CloudPoint{x=2.0, y=3.0}";
        assertEquals(expected, point.toString(), "String representation of CloudPoint should match the format");
    }

    @Test
    public void testNegativeCoordinates() {
        CloudPoint point = new CloudPoint(-1.0, -2.0);

        assertEquals(-1.0, point.getX(), 0.001, "X-coordinate should be -1.0");
        assertEquals(-2.0, point.getY(), 0.001, "Y-coordinate should be -2.0");
    }

    @Test
    public void testZeroCoordinates() {
        CloudPoint point = new CloudPoint(0.0, 0.0);

        assertEquals(0.0, point.getX(), 0.001, "X-coordinate should be 0.0");
        assertEquals(0.0, point.getY(), 0.001, "Y-coordinate should be 0.0");
    }
}