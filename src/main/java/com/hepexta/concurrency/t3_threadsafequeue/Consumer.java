package com.hepexta.concurrency.t3_threadsafequeue;

import java.util.concurrent.Callable;

public class Consumer implements Callable<String> {

    private final MessageBus messageBus;
    private boolean isReady = true;

    public Consumer(MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    @Override
    public String call() {
        while (isReady) {
            System.out.println(Thread.currentThread().getName() + messageBus.getThreadSafe());
        }
        return null;
    }

    public void close() {
        isReady = false;
    }
}
