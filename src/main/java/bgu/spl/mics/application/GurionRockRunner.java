package bgu.spl.mics.application;

import java.io.FileReader;
import java.util.Comparator;
//import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

//import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.DetectedObject;
import bgu.spl.mics.application.objects.StampedDetectedObjects;

import java.io.IOException;
import java.lang.reflect.Type;

// import bgu.spl.mics.application.objects.Camera;
// import bgu.spl.mics.application.objects.FusionSlam;

// import bgu.spl.mics.MessageBusImpl;
// import bgu.spl.mics.MicroService;
// import bgu.spl.mics.application.objects.STATUS;
// import bgu.spl.mics.application.services.CameraService;
// import bgu.spl.mics.application.services.FusionSlamService;
// import bgu.spl.mics.application.services.LiDarService;
// import bgu.spl.mics.application.services.PoseService;
// import bgu.spl.mics.application.services.TimeService;
// import bgu.spl.mics.example.ServiceCreator;


// import com.google.gson.reflect.TypeToken;
// import java.lang.reflect.Type;
// import java.util.List;



/**
 * The main entry point for the GurionRock Pro Max Ultra Over 9000 simulation.
 * <p>
 * This class initializes the system and starts the simulation by setting up
 * services, objects, and configurations.
 * </p>
 */
public class GurionRockRunner {

    /**
     * The main method of the simulation.
     * This method sets up the necessary components, parses configuration files,
     * initializes services, and starts the simulation.
     *
     * @param args Command-line arguments. The first argument is expected to be the path to the configuration file.
     */
    public static void main(String[] args) {
        System.out.println("Hello World!");
        // TODO: Parse configuration file.
        // TODO: Initialize system components and services. also TODO add thread to each created microService
        // TODO: Start the simulation.
        
        // try {
        //     FileReader reader = new FileReader(args[0]); // user sends the path to the configuration file at run
        //     //Map<String, Object> config = reader.fromJson(reader, HashMap.class);
        // }
        // catch (JsonIOException | IOException e) {
        //     e.printStackTrace();
        // }

       
    }

    public static void camera_data_parser(String[] args) {
        Gson gson = new Gson();
        try  {
            System.out.println("Current working directory: " + System.getProperty("user.dir"));

            FileReader reader = new FileReader(".\\example_input\\camera_data.json");
            JsonObject camerasJson = gson.fromJson(reader, JsonObject.class);
            Type camerasDetected = new TypeToken<Map<String, List<StampedDetectedObjects>>>(){}.getType();
            Map<String, Vector<StampedDetectedObjects>> camerasDetectedMap = gson.fromJson(reader, camerasDetected);
            for (Map.Entry<String, Vector<StampedDetectedObjects>> entry : camerasDetectedMap.entrySet()) {
                Camera camera = new Camera(Integer.parseInt(entry.getKey()), 0); // it will be held in main
                camera.setDetectedObjectsList(entry.getValue());
            }

            // }
        } catch (IOException e) {
        e.printStackTrace();
        }

        
    }




}
