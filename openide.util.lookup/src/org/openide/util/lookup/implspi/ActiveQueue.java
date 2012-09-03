/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.openide.util.lookup.implspi;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the active reference queue.
 * @since 8.1
 */
public final class ActiveQueue {
    private ActiveQueue() {}

    private static final Logger LOGGER = Logger.getLogger(ActiveQueue.class.getName());
    private static final ReferenceQueue<Impl> ACTIVE = new ReferenceQueue<Impl>();
    private static Reference<Impl> activeReferenceQueue = new WeakReference<Impl>(null);

    /**
     * Gets the active reference queue.
     * @return the singleton queue
     */
    public static synchronized ReferenceQueue<Object> queue() {
        Impl impl = activeReferenceQueue.get();
        if (impl == null) {
            impl = new Impl();
            activeReferenceQueue = new WeakReference<Impl>(impl, ACTIVE);
            Daemon.ping();
        }
        return impl;
    }

    private static final class Impl extends ReferenceQueue<Object> {
        private static final Field LOCK;
        static {
            Field f = null;
            try {
                f = ReferenceQueue.class.getDeclaredField("lock"); // NOI18N
                f.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                reportError(ex);
            } catch (SecurityException ex) {
                reportError(ex);
            }
            LOCK = f;
        }
        private final Object myLock;
        
        Impl() {
            Object l = this;
            try {
                if (LOCK != null) {
                    LOCK.set(this, l = LOCK.get(ACTIVE));
                }
            } catch (IllegalArgumentException ex) {
                reportError(ex);
            } catch (IllegalAccessException ex) {
                reportError(ex);
            }
            myLock = l;
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
        
        final Object lock() {
            return myLock;
        }

        final Reference<? extends Object> pollSuper() throws IllegalArgumentException, InterruptedException {
            return super.poll();
        }
    }

    private static final class Daemon extends Thread {
        private static Daemon running;
        
        public Daemon() {
            super("Active Reference Queue Daemon");
        }
        
        static synchronized void ping() {
            if (running == null) {
                Daemon t = new Daemon();
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(true);
                t.start();
                LOGGER.fine("starting thread");
                running = t;
            }
        }
        
        static synchronized boolean isActive() {
            return running != null;
        }
        
        static synchronized void wakeUp() {
            if (running != null) {
                running.interrupt();
            }
        }
        
        static synchronized Impl obtainQueue() {
            Impl impl= activeReferenceQueue.get();
            if (impl == null) {
                running = null;
            }
            return impl;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Impl impl = obtainQueue();
                    if (impl == null) {
                        return;
                    }
                    Reference<?> ref;
                    synchronized (impl.lock()) {
                        ref = impl.pollSuper();
                        impl = null;
                        if (ref == null) {
                            ACTIVE.remove(Integer.MAX_VALUE);
                            continue;
                        }
                    }
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
                        LOGGER.log(Level.WARNING, "Cannot process " + ref, t);
                    } finally {
                        // to allow GC
                        ref = null;
                    }
                } catch (InterruptedException ex) {
                    // Can happen during VM shutdown, it seems. Ignore.
                    continue;
                }
            }
        }
    }
    private static <T extends Throwable> T reportError(T ex) throws IllegalStateException {
        LOGGER.log(Level.WARNING, "Cannot hack ReferenceQueue to fix bug #206621!", ex);
        return ex;
    }
}
