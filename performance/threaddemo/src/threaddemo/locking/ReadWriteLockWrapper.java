/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package threaddemo.locking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of a regular lock (read/write).
 * @author Jesse Glick
 */
final class ReadWriteLockWrapper implements DuplexLock {
    
    private static final ExecutorService POOL = Executors.newCachedThreadPool();
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private final ThreadLocal<Integer> reading = new ThreadLocal<Integer>() {
        protected Integer initialValue() {
            return 0;
        }
    };
    
    public ReadWriteLockWrapper() {}

    public void enterRead() {
        lock.readLock().lock();
        reading.set(reading.get() + 1);
    }

    public void exitRead() {
        lock.readLock().unlock();
        assert reading.get() > 0;
        reading.set(reading.get() - 1);
    }

    public void enterWrite() {
        lock.writeLock().lock();
    }

    public void exitWrite() {
        lock.writeLock().unlock();
    }

    public void read(Runnable action) {
        enterRead();
        try {
            action.run();
        } finally {
            exitRead();
        }
    }

    public void write(Runnable action) {
        enterWrite();
        try {
            action.run();
        } finally {
            exitWrite();
        }
    }

    public <T> T read(LockAction<T> action) {
        enterRead();
        try {
            return action.run();
        } finally {
            exitRead();
        }
    }

    public <T> T write(LockAction<T> action) {
        enterWrite();
        try {
            return action.run();
        } finally {
            exitWrite();
        }
    }

    public <T, E extends Exception> T read(LockExceptionAction<T, E> action) throws E {
        enterRead();
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            E _e = (E) e;
            throw _e;
        } finally {
            exitRead();
        }
    }

    public <T, E extends Exception> T write(LockExceptionAction<T, E> action) throws E {
        enterWrite();
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            @SuppressWarnings("unchecked")
            E _e = (E) e;
            throw _e;
        } finally {
            exitWrite();
        }
    }

    public void readLater(final Runnable action) {
        POOL.submit(new Runnable() {
            public void run() {
                read(action);
            }
        });
    }

    public void writeLater(final Runnable action) {
        POOL.submit(new Runnable() {
            public void run() {
                write(action);
            }
        });
    }

    public boolean canRead() {
        // XXX in Mustang can just use: return lock.getReadHoldCount() > 0;
        return reading.get() > 0;
    }

    public boolean canWrite() {
        return lock.isWriteLockedByCurrentThread();
    }
    
}
