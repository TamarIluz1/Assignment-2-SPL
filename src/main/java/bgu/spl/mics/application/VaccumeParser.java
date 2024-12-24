// package bgu.spl.mics.application;

// import java.io.FileReader;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.Scanner;
// import com.google.gson.Gson;
// import com.google.gson.JsonArray;
// import com.google.gson.JsonElement;
// import com.google.gson.JsonIOException;
// import com.google.gson.JsonObject;

// import java.io.IOException;

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
// public class VaccumeParser {
//  public static void main(String[] args) {
//     Gson gson = new Gson();
//     try  {
        
//         FileReader reader = new FileReader("configuration_file.json");
//         JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
//     // Define the type for the list of employees
//         JsonObject camerasJson = jsonObject.getAsJsonObject("Cameras");
//         JsonArray camerasConfigArray = camerasJson.getAsJsonArray("CamerasConfigurations");
//         List<Camera> cameras = new ArrayList<>();
//         for (JsonElement cameraElement : camerasConfigArray) {
//             JsonObject cameraConfig = cameraElement.getAsJsonObject();
//             int id = cameraConfig.get("id").getAsInt();
//             int frequency = cameraConfig.get("frequency").getAsInt();
//             Camera camera = Camera.fromJson(id, frequency);
//             cameras.add(camera);
//             CameraService cameraService = new CameraService(camera);
//             Thread t = new Thread(cameraService.initialize());
//         }
        

//     } catch (IOException e) {
//     e.printStackTrace();
//     }

    
//  }
// }


