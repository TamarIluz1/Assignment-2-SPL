package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MessageBusImpl implements MessageBus {

   // The singleton instance of the message bus
   private static class SingletonHolder {
      private static MessageBusImpl instance = new MessageBusImpl();
   }
    
   private  Map<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventSubscribers;
   private  Map<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers;
   private  Map<MicroService, LinkedBlockingQueue<Message>> microServiceQueues;
   private  Map<Event, Future> eventFutures;
   private Object eventSubscribers_l = new Object();
   
   private MessageBusImpl() {
      eventSubscribers = new ConcurrentHashMap<>();
      broadcastSubscribers = new ConcurrentHashMap<>();
      microServiceQueues = new ConcurrentHashMap<>();
      eventFutures = new ConcurrentHashMap<>();
   }


   // Public method to provide access to the singleton instance
   public static MessageBusImpl getInstance() {
      // INSTANCE is already initialized
      return SingletonHolder.instance;
   }

   public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
      synchronized(eventSubscribers_l) {
         eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
         eventSubscribers.get(type).add(m);
      }
   }

   public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
      synchronized(broadcastSubscribers) {
         broadcastSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
         broadcastSubscribers.get(type).add(m);
      }
   }

   public <T> void complete(Event<T> e, T result) {
      @SuppressWarnings("unchecked")
      Future<T> future = (Future<T>) eventFutures.get(e); // TAMARCHECK
      if(future != null) {
         future.resolve(result);
      }
      //what needs to be checked?
   }

   public void sendBroadcast(Broadcast b) { // no need to synchronize because the broadcastSubscribers is a ConcurrentHashMap
      ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
      if(subscribers != null) {
         for(MicroService m : subscribers) {
            microServiceQueues.get(m).add(b);
         }
      }
   }

   public <T> Future<T> sendEvent(Event<T> e) { 
      // TODO need to implement round-robin
      ConcurrentLinkedQueue<MicroService> subscribers;
      synchronized(eventSubscribers_l) { // no other thread can access the eventSubscribers while adding subscribers
         subscribers = eventSubscribers.get(e.getClass());
         if (subscribers != null && !subscribers.isEmpty()) {
            MicroService m = subscribers.poll();
            if (m != null) {
               synchronized(microServiceQueues) {
                  microServiceQueues.get(m).add(e);
               }
               subscribers.add(m);
               Future<T> future = new Future<>();
               eventFutures.put(e, future);
               return future;
            }
         }
          return null;
      }
   }

   public void register(MicroService m) {
      synchronized(microServiceQueues) {
         microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
      }
      // TODO need to add the microService to the eventSubscribers and broadcastSubscribers??
   }

   public void unregister(MicroService m) {
      synchronized(microServiceQueues) {
         microServiceQueues.remove(m);
      }
      synchronized(eventSubscribers_l) {
         eventSubscribers.values().forEach(queue -> queue.remove(m));
      }
      synchronized(broadcastSubscribers) {
         broadcastSubscribers.values().forEach(queue -> queue.remove(m));
      }
   }

   public Message awaitMessage(MicroService m) throws InterruptedException {
      LinkedBlockingQueue<Message> queue = microServiceQueues.get(m);
      if (queue == null) {
         throw new IllegalStateException("MicroService is not registered");
      }
      Message m1 = null;
      while(m1 == null) {
         m1 = queue.take();
         m.wait(); // waiting for the future to be resolved- getting an event/broadcast
         
      }
      //recomended instade of the while loop 31.12 Tamar
      //synchronized (queue) {
      //return queue.take();
      //}
      return m1;
   }
   
}