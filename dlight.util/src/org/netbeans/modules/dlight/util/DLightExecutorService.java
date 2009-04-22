/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Default implementation of tasks executor service.
 * Uses RequestProcessor but allows submit Callable tasks.
 */
public class DLightExecutorService {

    private final static String PREFIX = "DLIGHT: "; // NOI18N
    private final static RequestProcessor processor = new RequestProcessor(PREFIX, 50);
    private final static Object lock;


    static {
        lock = new String(DLightExecutorService.class.getName());
    }

    public static <T> Future<T> submit(final Callable<T> task, final String name) {
        final FutureTask<T> ftask = new FutureTask<T>(new Callable<T>() {

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

            public void run() {
                Thread.currentThread().setName(PREFIX + name);
                task.run();
            }
        });
    }

    public static Future scheduleAtFixedRate(final Runnable task, final long period, final TimeUnit unit, final String descr) {
        synchronized (lock) {
            final FutureHolder futureHolder = new FutureHolder();

            Runnable runnable = new Runnable() {

                public void run() {
                    final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
                            new TaskThreadFactory(descr));

                    service.scheduleAtFixedRate(task, 0, period, unit);

                    try {
                        while (true) {
                            if (futureHolder.future.isDone()) {
                                break;
                            }

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ex) {
                                break;
                            }
                        }
                    } finally {
                        service.shutdownNow();
                    }
                }
            };

            final FutureTask<Boolean> ftask = new FutureTask<Boolean>(runnable, Boolean.TRUE);
            futureHolder.future = ftask;
            RequestProcessor.getDefault().post(ftask);

            return ftask;
        }
    }

    private static class FutureHolder {

        private volatile FutureTask future;
    }

    static class TaskThreadFactory implements ThreadFactory {

        final static AtomicInteger threadNumber = new AtomicInteger(1);
        final static String namePrefix = PREFIX + "ScheduledExecutorService No. "; // NOI18N
        final String threadName;
        final ThreadGroup group;

        TaskThreadFactory(String threadName) {
            this.threadName = namePrefix + threadNumber.getAndIncrement() + " [ " + threadName + " ]"; // NOI18N
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            synchronized (lock) {
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
    }
}
