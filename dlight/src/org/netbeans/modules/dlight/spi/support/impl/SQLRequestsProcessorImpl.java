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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.support.impl;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.spi.support.SQLRequest;
import org.netbeans.modules.dlight.spi.support.SQLRequestsProcessor;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ak119685
 */
public final class SQLRequestsProcessorImpl implements SQLRequestsProcessor {

    private static final Logger log = DLightLogger.getLogger(SQLRequestsProcessorImpl.class);
    private static final RequestProcessor RP = new RequestProcessor(SQLRequestsProcessorImpl.class.getName(), 10);
    private final int pollTimeout;
    private final TimeUnit pollTimeoutUnit;
    private final AtomicBoolean isActive = new AtomicBoolean();
    private final LinkedBlockingQueue<SQLRequest> queue = new LinkedBlockingQueue<SQLRequest>();
    private final Lock lock = new ReentrantLock();
    private final Condition queueProcessingDone = lock.newCondition();
    private final Lock taskLock = new ReentrantLock();
    private ScheduledFuture<?> task = null;
    private final int pollRequests;
    private final int maxIdleLoops;

    public SQLRequestsProcessorImpl(int pollRequests, int pollTimeout, TimeUnit pollTimeoutUnit) {
        this.pollRequests = pollRequests;
        this.pollTimeout = pollTimeout;
        this.pollTimeoutUnit = pollTimeoutUnit;

        maxIdleLoops = Math.max(3, (int) (pollTimeoutUnit.convert(2, TimeUnit.SECONDS) / pollTimeout));
    }

    @Override
    public boolean queueRequest(SQLRequest request) {
        if (!queue.offer(request)) {
            log.fine("Request dropped - no space in queue"); // NOI18N
            return false;
        }

        if (!isActive.get()) {
            taskLock.lock();
            try {
                if (isActive.compareAndSet(false, true)) {
                    log.fine("Starting SQLRequestsProcessor worker ..."); // NOI18N
                    task = RP.scheduleAtFixedRate(new Worker(), 0, pollTimeout, pollTimeoutUnit);
                }
            } finally {
                taskLock.unlock();
            }
        }

        return true;
    }

    @Override
    public void processRequest(SQLRequest request) throws SQLException {
        request.execute();
    }

    private void stopWorker() {
        taskLock.lock();
        try {
            if (isActive.compareAndSet(true, false)) {
                log.fine("Stopping SQLRequestsProcessor worker ..."); // NOI18N
                task.cancel(true);
                task = null;
            }
        } finally {
            taskLock.unlock();
        }
    }

    @Override
    public void flush() {
        while (true) {
            lock.lock();

            try {
                if (queue.isEmpty()) {
                    return;
                }
                queueProcessingDone.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                lock.unlock();
            }
        }
    }

    private class Worker implements Runnable {

        private boolean isInterrupted;
        private int idleLoops = 0;
        private final List<SQLRequest> requests = new LinkedList<SQLRequest>();

        @Override
        public void run() {
            if (!isActive.get() || isInterrupted()) {
                return;
            }

            lock.lock();

            try {
                int count = queue.drainTo(requests, pollRequests);

                if (count == 0) {
                    log.log(Level.FINE, "SQLQueueProcessor is empty {0}", idleLoops); // NOI18N
                    if (++idleLoops > maxIdleLoops) {
                        stopWorker();
                    }
                    return;
                }

                idleLoops = 0;

                if (count == pollRequests) {
                    log.fine("SQLQueueProcessor is overload... "); // NOI18N
                } else {
                    log.log(Level.FINE, "SQLQueueProcessor load is {0}... ", count); // NOI18N
                }

                for (SQLRequest request : requests) {
                    if (isInterrupted()) {
                        break;
                    }

                    try {
                        request.execute();
                    } catch (Throwable th) {
                        Exceptions.printStackTrace(th);
                    }
                }

                requests.clear();

                queueProcessingDone.signalAll();
            } finally {
                lock.unlock();
            }
        }

        private boolean isInterrupted() {
            try {
                Thread.sleep(0);
            } catch (InterruptedException ex) {
                isInterrupted = true;
                Thread.currentThread().interrupt();
            }

            isInterrupted |= Thread.currentThread().isInterrupted();

            if (isInterrupted) {
                lock.lock();
                try {
                    queue.clear();
                    queueProcessingDone.signalAll();
                } finally {
                    lock.unlock();
                }
            }

            return isInterrupted;
        }
    }
}
