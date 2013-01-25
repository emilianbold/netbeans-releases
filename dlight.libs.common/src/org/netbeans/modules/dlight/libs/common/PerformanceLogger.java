/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.libs.common;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openide.util.RequestProcessor;

/**
 * Logger for internal profiling performance issues.
 * 
 * @author Alexander Simon
 */
public class PerformanceLogger {
    public static final boolean IS_ACTIVE = false;
    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    public final class PerformaceAction {

        public void log(Object... extra) {
            PerformanceLogger.INSANCE.log(this, extra);
        }
        //<editor-fold defaultstate="collapsed" desc="Private Implemenration">
        private final String id;
        private final Object source;
        private final long start;
        private final long cpuTime;
        private final long userTime;

        private PerformaceAction(String id, Object source) {
            this.id = id;
            this.source = source;
            start = System.nanoTime();
            cpuTime = threadMXBean.getCurrentThreadCpuTime();
            userTime = threadMXBean.getCurrentThreadUserTime();
        }
        //</editor-fold>
    }

    public interface PerformanceEvent {

        /**
         *
         * @return event ID
         */
        String getId();

        /**
         *
         * @return event source
         */
        Object getSource();

        /**
         *
         * @return additional attributes of event
         */
        Object[] getAttrs();

        /**
         *
         * @return time of consumed from start event to log in nanoseconds
         */
        long getTime();
        
        /**
         *
         * @return CPU time of consumed from start event to log in nanoseconds
         */
        long getCpuTime();

        /**
         *
         * @return user time of consumed from start event to log in nanoseconds
         */
        long getUserTime();

        /**
         *
         * @return java used memory in bytes
         */
        long getUsedMemory();
    }

    public interface PerformanceListener {

        void processEvent(PerformanceEvent event);
    }
    //<editor-fold defaultstate="collapsed" desc="Private Implemenration">
    private static final PerformanceLogger INSANCE = new PerformanceLogger();
    private final ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();
    private final List<PerformanceListener> listeners = new ArrayList<PerformanceListener>();
    private final ReentrantReadWriteLock lineLock = new ReentrantReadWriteLock();
    private final LinkedList<PerformanceEvent> line = new LinkedList<PerformanceEvent>();
    private final RequestProcessor.Task task;

    private PerformanceLogger() {
        task = new RequestProcessor("PerformanceLoggerUpdater").create(new Runnable() { //NOI18N
            @Override
            public void run() {
                while (true) {
                    PerformanceEvent last = null;
                    lineLock.writeLock().lock();
                    try {
                        if (!line.isEmpty()) {
                            last = line.pollLast();
                        }
                    } finally {
                        lineLock.writeLock().unlock();
                    }
                    if (last != null) {
                        listenersLock.readLock().lock();
                        try {
                            if (listeners.size() > 0) {
                                for (PerformanceListener listener : listeners) {
                                    listener.processEvent(last);
                                }
                            }
                        } finally {
                            listenersLock.readLock().unlock();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        });
        task.setPriority(Thread.MIN_PRIORITY);
        if (IS_ACTIVE) {
            task.schedule(1000);
        }
    }

    private void log(PerformaceAction action, Object... extra) {
        if (IS_ACTIVE) {
            long delta = System.nanoTime() - action.start;
            Runtime runtime = Runtime.getRuntime();
            long usedMemeory = runtime.totalMemory() - runtime.freeMemory();
            long cpuTime = threadMXBean.getCurrentThreadCpuTime() - action.cpuTime;
            long userTime = threadMXBean.getCurrentThreadUserTime() - action.userTime;
            PerformanceEvent event = new PerformanceEventImpl(action.id, action.source, delta, cpuTime, userTime, usedMemeory, extra);
            lineLock.writeLock().lock();
            try {
                line.addFirst(event);
            } finally {
                lineLock.writeLock().unlock();
            }
        }
    }
    
    //</editor-fold>

    public static PerformanceLogger getLogger() {
        return INSANCE;
    }

    public PerformaceAction start(String id, Object source) {
        return new PerformaceAction(id, source);
    }

    public void addPerformanceListener(PerformanceListener listener) {
        listenersLock.writeLock().lock();
        try {
            listeners.add(listener);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }

    public void removePerformanceListener(PerformanceListener listener) {
        listenersLock.writeLock().lock();
        try {
            listeners.remove(listener);
        } finally {
            listenersLock.writeLock().unlock();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Private Class">
    private static final class PerformanceEventImpl implements PerformanceEvent {

        private final String id;
        private final Object source;
        private final long time;
        private final long cpu;
        private final long user;
        private final Object[] extra;
        private final long usedMemeory;

        private PerformanceEventImpl(String id, Object source, long time, long cpu, long user, long usedMemeory, Object[] extra) {
            this.id = id;
            this.source = source;
            this.time = time;
            this.cpu = cpu;
            this.user = user;
            this.extra = extra;
            this.usedMemeory = usedMemeory;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Object getSource() {
            return source;
        }

        @Override
        public Object[] getAttrs() {
            return extra;
        }

        @Override
        public long getTime() {
            return time;
        }

        @Override
        public long getCpuTime() {
            return cpu;
        }

        @Override
        public long getUserTime() {
            return user;
        }

        @Override
        public long getUsedMemory() {
            return usedMemeory;
        }
    }
    //</editor-fold>
}
