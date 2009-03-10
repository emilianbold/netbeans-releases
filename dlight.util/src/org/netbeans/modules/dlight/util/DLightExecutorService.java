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
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;

/**
 * Default implementation of tasks executor service.
 * Uses RequestProcessor but allows submit Callable tasks.
 */
public class DLightExecutorService {

    private final static String PREFIX = "DLIGHT: "; // NOI18N
    private final static ScheduledExecutorService service;
    private final static Object lock;
    private static String threadName;
    private final static Logger log;

    static {
        log = DLightLogger.getLogger(DLightExecutorService.class);
        service = Executors.newScheduledThreadPool(10, new TaskThreadFactory());
        lock = new String(DLightExecutorService.class.getName());

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                log.fine("Stop DLight ScheduledExecutorService"); // NOI18N
                service.shutdownNow();
            }
        });
    }

    public static <T> Future<T> submit(final Callable<T> task, String name) {
        final RequestProcessor processor = new RequestProcessor(PREFIX + name, 1);
        final FutureTask<T> ftask = new FutureTask<T>(task);
        processor.post(ftask);

        return ftask;
    }

    public static void submit(final Runnable task, String name) {
        final RequestProcessor processor = new RequestProcessor(PREFIX + name, 1);
        processor.post(task);
    }

    public static Future scheduleAtFixedRate(Runnable task, long period, TimeUnit unit, String descr) {
        synchronized (lock) {
            threadName = descr;
            return service.scheduleAtFixedRate(task, 0, period, unit);
        }
    }

    static class TaskThreadFactory implements ThreadFactory {

        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix = PREFIX + "ScheduledExecutorService Task No. "; // NOI18N

        TaskThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            synchronized (lock) {
                Thread t = new Thread(group, r,
                        namePrefix + threadNumber.getAndIncrement() + " [ " + threadName + " ]", // NOI18N
                        0);
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
