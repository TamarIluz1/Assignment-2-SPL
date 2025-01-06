package bgu.spl.mics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageBusImpl implements MessageBus {

    private static class SingletonHolder {
        private static final MessageBusImpl instance = new MessageBusImpl();
    }

    private final Map<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventSubscribers;
    private final Map<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers;
    private final Map<MicroService, LinkedBlockingQueue<Message>> microServiceQueues;
    private final Map<Event, Future> eventFutures;

    private MessageBusImpl() {
        eventSubscribers = new ConcurrentHashMap<>();
        broadcastSubscribers = new ConcurrentHashMap<>();
        microServiceQueues = new ConcurrentHashMap<>();
        eventFutures = new ConcurrentHashMap<>();
    }

    public static MessageBusImpl getInstance() {
        return SingletonHolder.instance;
    }

    @Override
    public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
        eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        eventSubscribers.get(type).add(m);
    }

    @Override
    public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
        broadcastSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
        broadcastSubscribers.get(type).add(m);
    }

    @Override
    public synchronized <T> void complete(Event<T> e, T result) {
        Future<T> future = (Future<T>) eventFutures.remove(e);
        if (future != null) {
            future.resolve(result);
        }
    }

    @Override
    public void sendBroadcast(Broadcast b) {
        ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
        if (subscribers != null) {
            for (MicroService m : subscribers) {
                LinkedBlockingQueue<Message> queue = microServiceQueues.get(m);
                if (queue != null) {
                    queue.offer(b);
                    synchronized (queue) {
                        queue.notifyAll();
                    }
                }
            }
        }
    }

    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        ConcurrentLinkedQueue<MicroService> subscribers = eventSubscribers.get(e.getClass());
        if (subscribers == null || subscribers.isEmpty()) {
            return null;
        }

        synchronized (subscribers) {
            MicroService m = subscribers.poll(); // Take the next MicroService in line
            if (m != null) {
                subscribers.offer(m); // Add it back to the end of the queue
                LinkedBlockingQueue<Message> queue = microServiceQueues.get(m);
                if (queue != null) {
                    Future<T> future = new Future<>();
                    eventFutures.put(e, future);
                    queue.offer(e);
                    synchronized (queue) {
                        queue.notifyAll();
                    }
                    return future;
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void register(MicroService m) {
        microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
    }

    @Override
    public synchronized void unregister(MicroService m) {
        microServiceQueues.remove(m);
        eventSubscribers.values().forEach(queue -> queue.remove(m));
        broadcastSubscribers.values().forEach(queue -> queue.remove(m));
    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {
        LinkedBlockingQueue<Message> queue = microServiceQueues.get(m);
        if (queue == null) {
            throw new IllegalStateException(m + " is not registered.");
        }

        synchronized (queue) {
            while (queue.isEmpty()) {
                queue.wait();
            }
            return queue.poll();
        }
    }
}
