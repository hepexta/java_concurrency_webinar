package com.hepexta.concurrency.t3_threadsafequeue;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageBus extends ArrayDeque<String> {

    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();
    public String getThreadSafe() {
        try {
            readLock.lock();
            while (this.isEmpty()) {
                Thread.sleep(100);
            }
            return this.pop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
        }
    }

    public void addThreadSafe(String message) {
        try {
            writeLock.lock();
            this.add(message);
        } finally {
            writeLock.unlock();
        }
    }
}
