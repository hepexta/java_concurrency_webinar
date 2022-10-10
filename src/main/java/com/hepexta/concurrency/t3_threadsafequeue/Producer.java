package com.hepexta.concurrency.t3_threadsafequeue;

public class Producer {
    private final MessageBus messageBus;

    public Producer(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    public void produce(String message) {
        messageBus.addThreadSafe(message);
    }
}
