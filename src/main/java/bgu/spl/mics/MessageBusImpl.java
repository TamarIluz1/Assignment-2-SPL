package bgu.spl.mics;

public class MessageBusImpl implements MessageBus {

    private static final MessageBusImpl INSTANCE = new MessageBusImpl(); // The singleton instance of the message bus
    private MessageBusImpl() {
    }


    // Public method to provide access to the singleton instance
    public static MessageBusImpl getInstance() {
        // INSTANCE is already initialized
        return INSTANCE;
    }

   public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
   }

   public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
   }

   public <T> void complete(Event<T> e, T result) {
   }

   public void sendBroadcast(Broadcast b) {
   }

   public <T> Future<T> sendEvent(Event<T> e) {
      return null;
   }

   public void register(MicroService m) {
   }

   public void unregister(MicroService m) {
   }

   public Message awaitMessage(MicroService m) throws InterruptedException {
      return null;
   }
   
}