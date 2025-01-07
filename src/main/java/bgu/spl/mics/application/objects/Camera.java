// Source code is decompiled from a .class file using FernFlower decompiler.
package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.Iterator;

public class Camera {
   int id;
   int frequency;
   STATUS status;
   ArrayList<StampedDetectedObjects> detectedObjectsList;
   Iterator<StampedDetectedObjects> detectedObjectsIterator;

   public static Camera fromJson(int id, int frequency) {
      return new Camera(id, frequency, STATUS.UP);
   }

   public Camera(int id, int frequency, STATUS status) {
      this.id = id;
      this.frequency = frequency;
      this.status = status;
      this.detectedObjectsList = new ArrayList();
      this.detectedObjectsIterator = this.detectedObjectsList.iterator();
   }

   public int getId() {
      return this.id;
   }

   public int getFrequency() {
      return this.frequency;
   }

   public STATUS getStatus() {
      synchronized(this) {
         return this.status;
      }
   }

   public void setStatus(STATUS status) {
      synchronized(this) {
         this.status = status;
      }
   }

   public ArrayList<StampedDetectedObjects> getDetectedObjectsList() {
      return this.detectedObjectsList;
   }

   public void setDetectedObjectsList(ArrayList<StampedDetectedObjects> detectedObjectsList) {
      this.detectedObjectsList = detectedObjectsList;
      this.detectedObjectsIterator = detectedObjectsList.iterator();
   }

   public StampedDetectedObjects getNextDetectedObjects() {
      return this.detectedObjectsIterator.hasNext() ? (StampedDetectedObjects)this.detectedObjectsIterator.next() : null;
   }


}
