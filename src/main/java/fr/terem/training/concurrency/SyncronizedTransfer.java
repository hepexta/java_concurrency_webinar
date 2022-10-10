package fr.terem.training.concurrency;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncronizedTransfer implements Callable<Boolean> {
    private static final AtomicInteger idGenerator = new AtomicInteger(1);

    private static final int LOCK_WAIT_SEC = 5;

    private final int id;

    private final Account accFrom;
    private final Account accTo;
    private final int amount;

    private final Random waitRandom = new Random();

    private boolean retryFail;
    private CountDownLatch startLatch;
    private CountDownLatch endLatch;
    private CyclicBarrier endBarrier;

    public SyncronizedTransfer(Account accFrom, Account accTo, int amount) {
        this.id = idGenerator.getAndIncrement();

        this.accFrom = accFrom;
        this.accTo = accTo;
        this.amount = amount;
    }

    public SyncronizedTransfer(Account accFrom, Account accTo, int amount,
                    boolean retryFail, CountDownLatch startLatch,
                    CountDownLatch endLatch, CyclicBarrier endBarrier) {
        this(accFrom, accTo, amount);
        this.retryFail = retryFail;
        this.startLatch = startLatch;
        this.endLatch = endLatch;
        this.endBarrier = endBarrier;
    }

    @Override
    public Boolean call() throws Exception {

        // waiting before start
        if (startLatch != null) {
            System.out.println("[" + id + "] " + "Waiting to start...");
            startLatch.await();
        }

        for (;;) {
            if (accFrom.getLock().tryLock(LOCK_WAIT_SEC, TimeUnit.SECONDS)) {
                try {
                    if (accTo.getLock()
                            .tryLock(LOCK_WAIT_SEC, TimeUnit.SECONDS)) {

                        try {
                            if (accFrom.getBalance() < amount) {
                                throw new IllegalStateException("[" + id
                                        + "] " + "Failed to transfer " + amount
                                        + " from Account " + accFrom.getId()
                                        + " (Balance is "
                                        + accFrom.getBalance() + ")");
                            }

                            accFrom.withdraw(amount);
                            accTo.deposit(amount);

                            Thread.sleep(waitRandom.nextInt(200));

                            System.out.println("[" + id + "] " + "Transfer "
                                    + amount + " done from " + accFrom.getId()
                                    + " to " + accTo.getId());

                            return true;

                        } finally {
                            accFrom.getLock().unlock();
                            accTo.getLock().unlock();

                            // count down after finish
                            if (endLatch != null) {
                                endLatch.countDown();
                            }
                            // Waiting for others to finish
                            if (endBarrier != null) {
                                endBarrier.await();
                            }
                        }

                    } else {
                        accTo.incFailedTransferCount();
                        if (!retryFail) {
                            return false;
                        }
                    }
                } finally {
                    accFrom.getLock().unlock();
                }
            } else {
                accFrom.incFailedTransferCount();
                if (!retryFail) {
                    return false;
                }
            }
        }
    }

}
