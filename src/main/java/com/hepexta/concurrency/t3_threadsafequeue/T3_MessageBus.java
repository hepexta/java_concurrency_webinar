package com.hepexta.concurrency.t3_threadsafequeue;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
* Implement message bus using Producer-Consumer pattern.

Implement asynchronous message bus. Do not use queue implementations from java.util.concurrent.
Implement producer, which will generate and post randomly messages to the queue.
Implement consumer, which will consume messages on specific topic and log to the console message payload.
(Optional) Application should create several consumers and producers that run in parallel.
* */
public class T3_MessageBus {
    public static void main(String[] args) throws InterruptedException {
        MessageBus messageBus = new MessageBus();

        Producer producer1 = new Producer(messageBus);
        Producer producer2 = new Producer(messageBus);
        Consumer consumer1 = new Consumer(messageBus);
        Consumer consumer2 = new Consumer(messageBus);
        Consumer consumer3 = new Consumer(messageBus);

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(consumer1);
        executorService.submit(consumer2);
        executorService.submit(consumer3);

        executorService.submit(() -> new T3_MessageBus().run(producer1, "Hello"));
        executorService.submit(() -> new T3_MessageBus().run(producer2, "Hi"));
    }

    private void run(Producer producer, String message) {
        int i = 0;
        while (true) {
            producer.produce(message+(i++));
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
