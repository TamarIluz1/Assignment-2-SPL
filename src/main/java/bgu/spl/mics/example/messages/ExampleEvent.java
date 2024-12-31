package bgu.spl.mics.example.messages;

import bgu.spl.mics.Event;

import bgu.spl.mics.Future;

public class ExampleEvent implements Event<String>{

    private Future<String> future = new Future<>();

        @Override
        public Future<String> getFuture() {
            return future;
        }
    
        @Override
        public void complete(String result) {
            future.resolve(result);
        }
    private String senderName;


    public ExampleEvent(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }


}