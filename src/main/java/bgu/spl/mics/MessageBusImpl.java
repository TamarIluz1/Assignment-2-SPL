package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageBusImpl implements MessageBus {

   // The singleton instance of the message bus
   private static class SingletonHolder {
      private static final MessageBusImpl instance = new MessageBusImpl();
   }
    
   private final Map<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventSubscribers;
   private final  Map<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers;
   private final  Map<MicroService, LinkedBlockingQueue<Message>> microServiceQueues;
   private final  Map<Event, Future> eventFutures;
   private final Object eventSubscribers_l = new Object();
   
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
       synchronized (broadcastSubscribers) {
           broadcastSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);

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
       
       if (subscribers == null) {
           System.err.println("No subscribers for broadcast: " + b.getClass().getSimpleName());
           return;
       }
   
       synchronized (subscribers) {
           for (MicroService m : subscribers) {
               LinkedBlockingQueue<Message> queue;
               synchronized (microServiceQueues) {
                  if (b.getClass().equals(TickBroadcast.class)){
                     System.out.println(b.toString() + " has " + subscribers.size() + " subscribers.");
                  }
               
                   queue = microServiceQueues.get(m);
               }
   
               if (queue != null) {
                   queue.offer(b);
                   synchronized (queue) {
                       queue.notifyAll(); // Ensure the MicroService waiting on the queue wakes up
                   }
                   System.out.println(b.getClass().getSimpleName() + " sent to: " + m.getName());
               } else {
                   System.err.println("Queue for " + m.getName() + " is null.");
               }
           }
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
           throw new IllegalStateException(m + " is not registered.");
       }
   
       // Wait for a message
       Message message = queue.take();
       System.out.println(m.getName() + " received message: " + message.getClass().getSimpleName());
       return message;
   }
      
}