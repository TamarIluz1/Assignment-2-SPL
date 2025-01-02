package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

   @Override
   public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
      eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
      synchronized(type) {
         eventSubscribers.get(type).add(m);
      }
   }

   @Override
   public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
      broadcastSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
      synchronized(type) {
         broadcastSubscribers.get(type).add(m);
         System.out.println(m.getName() + " subscribed to " + type.getSimpleName());
      }
   }

   @Override
   public <T> void complete(Event<T> e, T result) {
      @SuppressWarnings("unchecked")
      Future<T> future = (Future<T>) eventFutures.get(e); // TAMARCHECK
      if(future != null) {
         future.resolve(result);
      }
      //what needs to be checked?
   }

   @Override
   public void sendBroadcast(Broadcast b) {
       ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
       if (subscribers != null) {
           synchronized (subscribers) {
               for (MicroService m : subscribers) {
                   LinkedBlockingQueue<Message> queue;
                   synchronized (microServiceQueues) {
                       queue = microServiceQueues.get(m); // Ensure thread-safe access
                       System.out.println("TickBroadcast sent to: " + m.getName());

                   }
                   if (queue != null) {
                       queue.offer(b);
                       synchronized (queue) {
                           queue.notifyAll();
                       }
                   }
               }
           }
       } else {
           System.err.println("No subscribers for broadcast: " + b.getClass().getSimpleName());
       }
   }
      
   @Override
   public <T> Future<T> sendEvent(Event<T> e) { 
      //implementing round robin
      ConcurrentLinkedQueue<MicroService> subscribers = eventSubscribers.get(e.getClass());
      if (subscribers == null || subscribers.isEmpty()){
         return null;
      }

      MicroService m;
      synchronized(e.getClass()){
         m = subscribers.poll();
         if (m!= null){
            subscribers.offer(m);
         }
      }
      if (m != null) {
         LinkedBlockingQueue<Message> q = microServiceQueues.get(m);
         if (q!= null){
            Future<T> future = new Future<>();
            eventFutures.put(e, future);
            synchronized(m){
               q.offer(e);
            }
            return future;
         }
      }
         
      return null;
   }

   @Override
   public void register(MicroService m) {
      synchronized(microServiceQueues) {
         microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
      }
   }

   @Override
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

   @Override
   public Message awaitMessage(MicroService m) throws InterruptedException {
      LinkedBlockingQueue<Message> queue = microServiceQueues.get(m);
      if (queue == null) {
         throw new IllegalStateException(m+ "MicroService is not registered");
      }
      return queue.take();
      // synchronized(m){
      //    while(queue.isEmpty()) {
      //       try{
      //          m.wait();// waiting for the future to be resolved- getting an event/broadcast
      //       } catch (InterruptedException e) {
      //          e.printStackTrace();
      //       }
      //    }
      // }

      // Message message;
      // synchronized(m){
      //    message = queue.poll();
      // }
      // return message;

   }
   
}