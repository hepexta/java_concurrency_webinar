package com.hepexta.concurrency.t1_hashmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ThreadSafeMap<K, V> extends HashMap<K, V> {
    ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    // ...
 //   Lock lock = readWriteLock.writeLock(); // ExecutionTime: 438
    Lock lock = new ReentrantLock(); // ExecutionTime: 444
    @Override
    public V put(K key, V value) {
        try {
            lock.lock();
            return super.put(key, value);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        try {
            lock.lock();
            return new ArrayList<>(super.values());
        }
        finally {
            lock.unlock();
        }
    }
}
