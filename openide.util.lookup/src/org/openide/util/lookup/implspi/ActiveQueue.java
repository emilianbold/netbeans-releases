package org.openide.util.lookup.implspi;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the active reference queue.
 * @since 8.1
 */
public final class ActiveQueue {

    private ActiveQueue() {}

    private static final Logger LOGGER = Logger.getLogger(ActiveQueue.class.getName().replace('$', '.'));
    private static Impl activeReferenceQueue;

    /**
     * Gets the active reference queue.
     * @return the singleton queue
     */
    public static synchronized ReferenceQueue<Object> queue() {
        if (activeReferenceQueue == null) {
            activeReferenceQueue = new Impl();
        }

        activeReferenceQueue.ping();

        return activeReferenceQueue;
    }

    private static final class Impl extends ReferenceQueue<Object> implements Runnable {
        /** number of known outstanding references */
        private int count;

        Impl() {
        }

        @Override
        public Reference<Object> poll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Reference<Object> remove(long timeout) throws IllegalArgumentException, InterruptedException {
            throw new InterruptedException();
        }

        @Override
        public Reference<Object> remove() throws InterruptedException {
            throw new InterruptedException();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Reference<?> ref = super.remove(0);
                    LOGGER.finer("dequeued reference");
                    if (!(ref instanceof Runnable)) {
                        LOGGER.log(Level.WARNING, "A reference not implementing runnable has been added to the Utilities.activeReferenceQueue(): {0}", ref.getClass());
                        continue;
                    }
                    // do the cleanup
                    try {
                        ((Runnable) ref).run();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Should not happen.
                        // If it happens, it is a bug in client code, notify!
                        LOGGER.log(Level.WARNING, null, t);
                    } finally {
                        // to allow GC
                        ref = null;
                    }
                } catch (InterruptedException ex) {
                    // Can happen during VM shutdown, it seems. Ignore.
                    continue;
                }
                synchronized (this) {
                    assert count > 0;
                    count--;
                    if (count == 0) {
                        // We have processed all we have to process (for now at least).
                        // Could be restarted later if ping() called again.
                        // This could also happen in case someone called queue() once and tried
                        // to use it for several references; in that case run() might never be called on
                        // the later ones to be collected. Can't really protect against that situation.
                        // See issue #86625 for details.
                        LOGGER.fine("stopping thread");
                        break;
                    }
                }
            }
        }

        synchronized void ping() {
            if (count == 0) {
                Thread t = new Thread(this, "Active Reference Queue Daemon");
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(true);
                t.start();
                LOGGER.fine("starting thread");
            } else {
                LOGGER.finer("enqueuing reference");
            }
            count++;
        }

    }

}
