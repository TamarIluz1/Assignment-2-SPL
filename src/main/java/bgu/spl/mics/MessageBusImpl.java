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
      eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
      eventSubscribers.get(type).add(m);
   }

   public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
      broadcastSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
      broadcastSubscribers.get(type).add(m);
   }

   public <T> void complete(Event<T> e, T result) {
      Future<T> future = eventFutures.get(e);
      if(future != null) {
         future.resolve(result);
      }
   }

   public void sendBroadcast(Broadcast b) {
      ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
      if(subscribers != null) {
         for(MicroService m : subscribers) {
            microServiceQueues.get(m).add(b);
         }
      }
   }

   public <T> Future<T> sendEvent(Event<T> e) {
      ConcurrentLinkedQueue<MicroService> subscribers = eventSubscribers.get(e.getClass());
      if (subscribers != null && !subscribers.isEmpty()) {
         MicroService m = subscribers.poll();
         if (m != null) {
            microServiceQueues.get(m).add(e);
            subscribers.add(m);
            Future<T> future = new Future<>();
            eventFutures.put(e, future);
            return future;
         }
      }
      return null;
   }

   public void register(MicroService m) {
      microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
   }

   public void unregister(MicroService m) {
      microServiceQueues.remove(m);
      eventSubscribers.values().forEach(queue -> queue.remove(m));
      broadcastSubscribers.values().forEach(queue -> queue.remove(m)); 
   }

   public Message awaitMessage(MicroService m) throws InterruptedException {
      LinkedBlockingQueue<Message> queue = microServiceQueues.get(m);
      if (queue == null) {
         throw new IllegalStateException("MicroService is not registered");
      }
      return queue.take();
   }
   
}