package com.hepexta.concurrency.t2_threethreads;

import com.hepexta.concurrency.t1_hashmap.Breaker;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
Create three threads:

1st thread is infinitely writing random number to the collection;
2nd thread is printing sum of the numbers in the collection;
3rd is printing square root of sum of squares of all numbers in the collection.
Make these calculations thread-safe using synchronization block. Fix the possible deadlock.
* */
public class T2_ThreeThreads {

    public static void main(String[] args) {

        List<Integer> list = new CopyOnWriteArrayList<>();
        final Breaker breaker = new Breaker();

        ExecutorService service = Executors.newCachedThreadPool();
        service.submit(addElements(list, breaker));
        service.submit(sumElements(list, breaker));
        service.submit(squareElements(list, breaker));
        service.submit(clearElements(list, breaker));

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

    private static Runnable clearElements(List<Integer> list, Breaker breaker) {
        return () -> {
            while (breaker.isShouldWork()) {
                try {
                    list.clear();
                    System.out.println("Cleared list");
                    Thread.sleep(1000);
                }
                catch (ConcurrentModificationException e) {
                    System.out.println("Error: "+e);
                    e.printStackTrace();
                    breaker.setShouldWork(false);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static Runnable squareElements(List<Integer> list, Breaker breaker) {
        return () -> {
            while (breaker.isShouldWork()) {
                try {
                    double sqrt = Math.sqrt(list.stream().mapToLong(e -> Math.multiplyExact(e, e)).sum()); // ExecutionTime:
                    System.out.println("Square root is " + sqrt);
                }
                catch (ConcurrentModificationException e) {
                    System.out.println("Error: "+e);
                    e.printStackTrace();
                    breaker.setShouldWork(false);
                }
            }
        };
    }

    private static Runnable sumElements(List<Integer> list, Breaker breaker) {
        return () -> {
            while (breaker.isShouldWork()) {
                try {
                //    long sum = map.values().parallelStream().mapToLong(e -> e).sum(); // ExecutionTime: 2665
                    long sum = list.stream().mapToLong(e -> e).sum(); // ExecutionTime: 1201
                    System.out.println("Sum is " + sum);
                }
                catch (ConcurrentModificationException e) {
                    System.out.println("Error: "+e);
                    e.printStackTrace();
                    breaker.setShouldWork(false);
                }
            }
        };
    }

    private static Runnable addElements(List<Integer> list, Breaker breaker) {
        return () -> {
            Random random = new Random();
            try {
                while (breaker.isShouldWork()) {
                    int value = Math.abs(random.nextInt(100));
                    list.add(value);
                    System.out.println("Added new " + value);
                }
            }
            catch (ConcurrentModificationException e) {
                System.out.println("Error: "+e.getMessage());
                e.printStackTrace();
                breaker.setShouldWork(false);
            }
            breaker.setShouldWork(false);
        };
    }


}
