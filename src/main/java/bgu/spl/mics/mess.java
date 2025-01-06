// package bgu.spl.mics;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.LinkedBlockingQueue;
// /**
//  * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
//  * Write your implementation here!
//  * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
//  * All other methods and members you add the class must be private.
//  */
// public class MessageBusImpl implements MessageBus {



// 	private ConcurrentHashMap<Event<?>,Future<?>> eventFutureMap;
// 	private ConcurrentHashMap<Class<? extends Event<?>>, LinkedBlockingQueue<MicroService>> eventSubscribers;
// 	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> MicroserviceQueue; // set of microservice with its queue
// 	private ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> BroadCastSubscribers;

// 	private static class singletonHolder {
// 		private static final MessageBusImpl INSTANCE = new MessageBusImpl();
// 	}

// 	private MessageBusImpl()
// 	{
// 		MicroserviceQueue = new ConcurrentHashMap<>();
// 		eventSubscribers = new ConcurrentHashMap<>();
// 		BroadCastSubscribers = new ConcurrentHashMap<>();
// 		eventFutureMap = new ConcurrentHashMap<>();
// 	}
// // singleton
// 	public static MessageBusImpl getInstance()
// 	{
// 		return singletonHolder.INSTANCE;
// 	}


// 	@Override
// 	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

// 		BroadCastSubscribers.putIfAbsent(type, new LinkedBlockingQueue<>());
// 		synchronized(type){
// 			BroadCastSubscribers.get(type).add(m);
// 		}
// 	}
// 	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService micro) {

// 		eventSubscribers.putIfAbsent(type, new LinkedBlockingQueue<>());
// 		synchronized(type){
// 			eventSubscribers.get(type).add(micro);
// 		}
// 	}


// 	@Override
// 	public void sendBroadcast(Broadcast b) {
// 		LinkedBlockingQueue<MicroService> subscribers = BroadCastSubscribers.get(b.getClass());
// 		for (MicroService m : subscribers) {
// 			synchronized(m) {
// 				LinkedBlockingQueue<Message> queue = MicroserviceQueue.get(m);
// 				if (queue != null) {
// 					queue.add(b);
// 					m.notifyAll();
// 				}
// 			}
// 		}
// 	}

// 	//round robin fashoin
// 	@Override
// 	public <T> Future<T> sendEvent(Event<T> event) {
// 		LinkedBlockingQueue<MicroService> subscribers = eventSubscribers.get(event.getClass());
// 		if(subscribers == null)
// 			return null;
// 		if(subscribers.isEmpty())
// 			return null;
// 		MicroService nextmicro;
// 		synchronized (event.getClass()) {
// 			nextmicro = subscribers.poll();
// 			if (nextmicro != null)
// 			{
// 				subscribers.offer(nextmicro);
// 			}
// 		}

// 		if (nextmicro != null) {
// 			synchronized(nextmicro){
// 				LinkedBlockingQueue<Message> queue = MicroserviceQueue.get(nextmicro);
// 				if (queue != null) {
// 					Future<T> future = new Future<>();
// 					eventFutureMap.put(event, future);
// 					queue.offer(event);
// 					nextmicro.notifyAll();
// 					return future;
// 				}
// 			}
// 		}
// 		return null;
// 	}
// 	// add a new queue for the new microservice
// 	@Override
// 	public void register(MicroService m) {
// 		LinkedBlockingQueue<Message> q = new LinkedBlockingQueue<>();
// 		MicroserviceQueue.putIfAbsent(m, q);
// 	}

// 	@Override
// 	public void unregister(MicroService m) {
// 		// part 1 - event
// 		MicroserviceQueue.remove(m); // first remove microservice's queue
// 		for (Class<? extends Event<?>> eventType : eventSubscribers.keySet()) {
// 			synchronized (eventType) {
// 				MicroService rememberMicro = eventSubscribers.get(eventType).poll(); // poll the first microservice to remember and add to the end
// 				eventSubscribers.get(eventType).offer(rememberMicro);
// 				//find each appearance of the microservice in any queue
// 				while (!eventSubscribers.get(eventType).peek().equals(rememberMicro)) {
// 					MicroService nextMicroservice = eventSubscribers.get(eventType).poll();
// 					if (!nextMicroservice.equals(m)) // check if need to remove
// 						eventSubscribers.get(eventType).offer(nextMicroservice);
// 				}
// 			}
// 		}
// 		// part 1 - broadcast
// 		// remove for each type of broadcast queue - delete the microservice
// 		for (Class<? extends Broadcast> BroadcastType : BroadCastSubscribers.keySet()) {
// 			synchronized (BroadcastType) {
// 				if (!BroadCastSubscribers.isEmpty()) {
// 					MicroService rememberMicro = BroadCastSubscribers.get(BroadcastType).poll();
// 					BroadCastSubscribers.get(BroadcastType).offer(rememberMicro);
// 					while (!BroadCastSubscribers.get(BroadcastType).peek().equals(rememberMicro)) {
// 						MicroService nextMicroservice = BroadCastSubscribers.get(BroadcastType).poll();
// 						if (!nextMicroservice.equals(m))
// 							BroadCastSubscribers.get(BroadcastType).offer(nextMicroservice);
// 					}
// 				}
// 			}
// 		}
// 	}
// 	// microservice gets his next message
// 	@Override
// 	public Message awaitMessage(MicroService mic) throws InterruptedException {
// 		LinkedBlockingQueue<Message> queue = MicroserviceQueue.get(mic);
// 		if (queue == null) {
// 			throw new IllegalStateException( mic.getName() + " Not Register");
// 		}
// 		synchronized (mic){
// 			while (queue.isEmpty()) {
// 				try {
// 					mic.wait();
// 				} catch (InterruptedException e) {
// 					Thread.currentThread().interrupt();
// 				}
// 			}
// 		}
// 		Message _massage; // a new message arrived
// 		synchronized (mic){
// 			_massage = queue.poll();
// 		}
// 		return _massage;
// 	}
// 	@Override
// 	public <T> void complete(Event<T> e, T result) {
// 		Future<T> future = (Future<T>)eventFutureMap.get(e);
// 		if (future != null) {
// 			future.resolve(result);
// 			eventFutureMap.remove(e);
// 		}
// 	}
// 	// getters
// 	//@Override
// 	public ConcurrentHashMap<Class<? extends Broadcast>, LinkedBlockingQueue<MicroService>> getBroadCastSubscribers() {
// 		return BroadCastSubscribers;
// 	}

// 	//@Override
// 	public ConcurrentHashMap<Class<? extends Event<?>>, LinkedBlockingQueue<MicroService>> getEventSubscribers() {
// 		return eventSubscribers;
// 	}

// 	public ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> getMicroserviceQueue() {
// 		return MicroserviceQueue;
// 	}
// }