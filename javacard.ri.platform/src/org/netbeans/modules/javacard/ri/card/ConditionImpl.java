package org.netbeans.modules.javacard.ri.card;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

final class ConditionImpl implements Condition {
    private volatile int steps;
    private final RICard card;
    private final Object lock = new Object();

    ConditionImpl(int steps, RICard card) {
        this (steps, card, false);
    }

    ConditionImpl() {
        this (0, null, true);
    }

    private ConditionImpl(int steps, RICard card, boolean done) {
        this.card = card;
        this.steps = steps;
        this.done = done;
    }

    synchronized void countdown() {
        steps--;
        card.log("ConditionImpl " + this + " countdown " + steps); //NOI18N
        if (steps == 0) {
            signalAll();
        }
    }

    public void await() throws InterruptedException {
        if (done) return;
        card.log("ConditionImpl " + this + " await in " + Thread.currentThread()); //NOI18N
        synchronized (lock) {
            lock.wait();
        }
    }

    public void awaitUninterruptibly() {
        if (done) return;
        card.log("ConditionImpl " + this + " awaitUninterruptibly " + Thread.currentThread()); //NOI18N
        try {
            synchronized (lock) {
                await();
            }
        } catch (InterruptedException e) {
            awaitUninterruptibly();
        }
    }

    public long awaitNanos(long nanosTimeout) throws InterruptedException {
        if (done) return 0L;
        synchronized (lock) {
            lock.wait(0, (int) nanosTimeout);
        }
        return 0;
    }

    public boolean await(long time, TimeUnit unit) throws InterruptedException {
        if (done) return true;
        long millis = unit.convert(time, TimeUnit.MILLISECONDS);
        synchronized (lock) {
            lock.wait(millis);
        }
        return card.getState().isRunning();
    }

    public boolean awaitUntil(Date deadline) throws InterruptedException {
        if (done) return true;
        long time = deadline.getTime() - System.currentTimeMillis();
        await(time, TimeUnit.MILLISECONDS);
        return card.getState().isRunning();
    }

    private volatile boolean done;
    public void signal() {
        signalAll();
    }

    public void signalAll() {
        done = true;
        card.log("ConditionImpl " + this + " signalAll "); //NOI18N
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
