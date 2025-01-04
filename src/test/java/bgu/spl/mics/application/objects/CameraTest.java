// package bgu.spl.mics.application.objects;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import java.util.ArrayList;
// import java.util.Arrays;

// import static org.junit.jupiter.api.Assertions.*;

// class CameraTest {

//     private Camera camera;
//     private ArrayList<StampedDetectedObjects> detectedObjectsList;

//     @BeforeEach
//     void setUp() {
//         // Initialize detected objects
//         DetectedObject detectedObject1 = new DetectedObject("ObjectA", "a");
//         DetectedObject detectedObject2 = new DetectedObject("ObjectB", "b");
//         DetectedObject detectedObject3 = new DetectedObject("ObjectC", "c");
//         DetectedObject detectedObject4 = new DetectedObject("ObjectD", "d");
//         DetectedObject detectedObject5 = new DetectedObject("ObjectE", "e");
//         DetectedObject detectedObject6 = new DetectedObject("ObjectF", "f");
//         DetectedObject detectedObject7 = new DetectedObject("ObjectG", "g");

//         // Initialize the list of stamped detected objects
//         detectedObjectsList = new ArrayList<>();
//         detectedObjectsList.add(new StampedDetectedObjects(5, new ArrayList<>(Arrays.asList(detectedObject1, detectedObject2, detectedObject3))));
//         detectedObjectsList.add(new StampedDetectedObjects(10, new ArrayList<>(Arrays.asList(detectedObject4))));
//         detectedObjectsList.add(new StampedDetectedObjects(15, new ArrayList<>(Arrays.asList(detectedObject5, detectedObject6, detectedObject7))));

//         // Initialize the camera with id, frequency, and the detected objects list
//         camera = new Camera(1, 5, STATUS.UP);
//         camera.setDetectedObjectsList(detectedObjectsList);
//     }

//     /**
//      * Test prepareData method with a valid current time.
//      *
//      * Precondition:
//      * - The camera is initialized with a frequency of 5.
//      * - The detectedObjectsList contains entries with times 5, 10, and 15.
//      *
//      * Postcondition:
//      * - The result is not null.
//      * - The result's time is 5.
//      */
//     @Test
//     void testDataFetching() {
//         int currentTime = 10;
//         StampedDetectedObjects result = camera.getDataByTime(currentTime);
//         assertNotNull(result, "Expected a non-null result for currentTime = 10");
//         assertEquals(5, result.getTimestamp(), "Expected time of 5 for currentTime = 10");
//     }

//     /**
//      * Test prepareData method with increasing current time.
//      *
//      * Precondition:
//      * - The camera is initialized with a frequency of 5.
//      * - The detectedObjectsList contains entries with times 5, 10, and 15.
//      *
//      * Postcondition:
//      * - For currentTime = 9, the result is null.
//      * - For currentTime = 10, the result contains detected objects 1, 2, and 3.
//      */
//     @Test
//     void FetchDataIncorrectTime() {
//         int currentTime = 9;
//         StampedDetectedObjects result = camera.getDataByTime(currentTime);
//         assertNull(result, "Expected a null result for currentTime = 9");

//         currentTime = 10;
//         result = camera.getDataByTime(currentTime);
//         assertNotNull(result, "Expected a non-null result for currentTime = 10");
//         assertEquals(5, result.getTimestamp(), "Expected time of 5 for currentTime = 10");
//         assertEquals(
//                 Arrays.asList(
//                         new DetectedObject("ObjectA", "a"),
//                         new DetectedObject("ObjectB", "b"),
//                         new DetectedObject("ObjectC", "c")
//                 ),
//                 result.getObjectsList(),
//                 "Expected detected objects 1, 2, 3 for currentTime = 10"
//         );
//     }

//     /**
//      * Test prepareData method with a current time that has no corresponding detected objects.
//      *
//      * Precondition:
//      * - The camera is initialized with a frequency of 5.
//      * - The detectedObjectsList does not contain an entry for time 0.
//      *
//      * Postcondition:
//      * - The result is null.
//      */
//     @Test
//     void testPrepareDataWithNoRelevantTime() {
//         int currentTime = 3;
//         StampedDetectedObjects result = camera.prepareData(currentTime);
//         assertNull(result, "Expected a null result for currentTime = 3");
//     }

//     /**
//      * Test prepareData method with an empty detected objects list.
//      *
//      * Precondition:
//      * - The camera is initialized with a frequency of 5.
//      * - The detectedObjectsList is empty.
//      *
//      * Postcondition:
//      * - The result is null.
//      */
//     @Test
//     void testPrepareDataWithEmptyDetectedObjectsList() {
//         camera = new Camera(1, 5, new ArrayList<>());
//         int currentTime = 10;
//         StampedDetectedObjects result = camera.prepareData(currentTime);
//         assertNull(result, "Expected a null result for empty detected objects list");
//     }
// }