package bgu.spl.mics.application;

import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import java.io.IOException;

import bgu.spl.mics.application.objects.Camera;
import bgu.spl.mics.application.objects.FusionSlam;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.STATUS;
import bgu.spl.mics.application.services.CameraService;
import bgu.spl.mics.application.services.FusionSlamService;
import bgu.spl.mics.application.services.LiDarService;
import bgu.spl.mics.application.services.PoseService;
import bgu.spl.mics.application.services.TimeService;
import bgu.spl.mics.example.ServiceCreator;


import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;



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
        Gson gson = new Gson();
        
        // try {
        //     FileReader reader = new FileReader(args[0]); // user sends the path to the configuration file at run
        //     //Map<String, Object> config = reader.fromJson(reader, HashMap.class);
        // }
        // catch (JsonIOException | IOException e) {
        //     e.printStackTrace();
        // }

       
    }

}
