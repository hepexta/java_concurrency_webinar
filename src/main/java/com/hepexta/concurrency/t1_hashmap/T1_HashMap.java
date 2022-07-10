package com.hepexta.concurrency.t1_hashmap;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
* Create HashMap<Integer, Integer>. The first thread adds elements into the map, the other go along the given map and sum the values.
* Threads should work before catching ConcurrentModificationException. Try to fix the problem with ConcurrentHashMap and Collections.synchronizedMap().
* What has happened after simple Map implementation exchanging? How it can be fixed in code?
* Try to write your custom ThreadSafeMap with synchronization and without.
* Run your samples with different versions of Java (6, 8, and 10, 11) and measure the performance.
* */
public class T1_HashMap {

    private static final int NUMBER_ITERATION = 10_000;

    public static void main(String[] args) {

        Map<Integer, Integer> map = new HashMap<>();
        final Breaker breaker = new Breaker();

        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(addElements(map, breaker));
        service.submit(sumElements(map, breaker));

        Date time = new Date();
        while (breaker.isShouldWork()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        System.out.printf("ExecutionTime: %d%n", (new Date().getTime()) -time.getTime());
        service.shutdown();
    }

    private static Runnable sumElements(Map<Integer, Integer> map, Breaker breaker) {
        return () -> {
            while (breaker.isShouldWork()) {
                try {
                    long sum = map.values().stream().mapToLong(e -> e).sum();
                    System.out.println("Sum is " + sum);
                }
                catch (ConcurrentModificationException e) {
                    System.out.println("Error: "+e);
                    breaker.setShouldWork(false);
                }
            }
        };
    }

    private static Runnable addElements(Map<Integer, Integer> map, Breaker breaker) {
        return () -> {
            Random random = new Random();
            for (int i = 0; i < NUMBER_ITERATION; i++) {
                try {
                    if (breaker.isShouldWork()) {
                        int value = Math.abs(random.nextInt(100));
                        map.put(i, value);
                        System.out.println("Added new " + value);
                    }
                }
                catch (ConcurrentModificationException e) {
                    System.out.println("Error: "+e.getMessage());
                    breaker.setShouldWork(false);
                }
            }
            breaker.setShouldWork(false);
        };
    }


}
