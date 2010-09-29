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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 * Default implementation of tasks executor service.
 * Uses RequestProcessor but allows submit Callable tasks.
 */
public class DLightExecutorService {

    private static final Logger log = DLightLogger.getLogger(DLightExecutorService.class);
    private static final String PREFIX = "DLIGHT: "; // NOI18N
    private static final RequestProcessor processor = new RequestProcessor(PREFIX, 50);
    private static final CopyOnWriteArrayList<DLightScheduledTask> scheduledTasks =
            new CopyOnWriteArrayList<DLightScheduledTask>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                if (!scheduledTasks.isEmpty()) {
                    log.log(Level.WARNING, "DLightExecutorService: not all registered scheduled tasks cancelled!"); // NOI18N
                }

                for (DLightScheduledTask task : scheduledTasks) {
                    task.cancel();
                }
            }
        }));
    }

    private DLightExecutorService() {
    }

    public static <T> Future<T> submit(final Callable<T> task, final String name) {
        final FutureTask<T> ftask = new FutureTask<T>(new Callable<T>() {

            @Override
            public T call() throws Exception {
                Thread.currentThread().setName(PREFIX + name);
                return task.call();
            }
        });

        processor.post(ftask);
        return ftask;
    }

    public static void submit(final Runnable task, final String name) {
        processor.post(new Runnable() {

            @Override
            public void run() {
                Thread.currentThread().setName(PREFIX + name);
                task.run();
            }
        });
    }

    public static DLightScheduledTask scheduleAtFixedRate(final Runnable task, final long period, final TimeUnit unit, final String descr) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
                new TaskThreadFactory(descr));
        service.scheduleAtFixedRate(task, 0, period, unit);
        DLightScheduledTask result = new DLightScheduledTask(service, descr);
        scheduledTasks.add(result);
        return result;
    }

    static class TaskThreadFactory implements ThreadFactory {

        final static AtomicInteger threadNumber = new AtomicInteger(1);
        final static String namePrefix = PREFIX + "DLightScheduledTask No. "; // NOI18N
        final String threadName;
        final ThreadGroup group;

        TaskThreadFactory(String threadName) {
            this.threadName = namePrefix + threadNumber.getAndIncrement() + " [ " + threadName + " ]"; // NOI18N
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, threadName, 0);

            if (t.isDaemon()) {
                t.setDaemon(false);
            }

            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            return t;
        }
    }

    public static final class DLightScheduledTask implements Cancellable {

        private final ScheduledExecutorService service;
        private final String descr;

        DLightScheduledTask(ScheduledExecutorService service, String descr) {
            this.service = service;
            this.descr = descr;
            log.log(Level.FINEST, "DLightScheduledTask ({0}) started", descr); // NOI18N
        }

        /**
         * Stops the task. If <code>timeoutSeconds</code> &gt; 0 and task is
         * executed at the time of method call, wait for specified amount of
         * seconds for it's completion before forcely interrupt it.
         * @param timeoutSeconds timeout in seconds to wait for current task to
         * finish before interrupting it
         *
         * @return true if the job was succesfully cancelled, false if job
         *         can't be cancelled for some reason
         */
        public boolean cancel(long timeoutSeconds) {
            try {
                service.shutdown();

                try {
                    if (!service.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                        service.shutdownNow();
                    }
                } catch (InterruptedException ex) {
                    service.shutdownNow();
                    Thread.currentThread().interrupt();
                }

                if (service.isShutdown()) {
                    log.log(Level.FINEST, "DLightScheduledTask ({0}) stopped", descr); // NOI18N
                    return true;
                } else {
                    log.log(Level.FINEST, "DLightScheduledTask ({0}) FAILED to stop", descr); // NOI18N
                    return false;
                }
            } finally {
                scheduledTasks.remove(this);
            }
        }

        @Override
        public boolean cancel() {
            return cancel(0);
        }
    }
}
