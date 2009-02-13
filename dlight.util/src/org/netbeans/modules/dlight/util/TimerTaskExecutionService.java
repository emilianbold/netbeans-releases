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

import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public final class TimerTaskExecutionService {

    private static final ExecutorService timerTasksExecutor;
    private static final Logger log;
    private static final TimerTaskExecutionService instance;
    private final ConcurrentHashMap<Callable<Integer>, SubscriberTask> subscribedTasks;
    private final ConcurrentHashMap<Callable<Integer>, Future<Integer>> runningHash;
    private Timer timer = null;
    private final Object lock;


    static {
        log = DLightLogger.getLogger(TimerTaskExecutionService.class);
        timerTasksExecutor = Executors.newCachedThreadPool();
        instance = new TimerTaskExecutionService();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                log.fine("Stop TimerTaskExecutionService"); // NOI18N
                timerTasksExecutor.shutdown();
            }
        });
    }

    public static TimerTaskExecutionService getInstance() {
        return instance;
    }

    private TimerTaskExecutionService() {
        lock = new String(TimerTaskExecutionService.class.getName());
        subscribedTasks = new ConcurrentHashMap<Callable<Integer>, SubscriberTask>();
        runningHash = new ConcurrentHashMap<Callable<Integer>, Future<Integer>>();
    }

    public void registerTimerTask(Callable<Integer> task, int factor) {
        if (task == null) {
            return;
        }

        boolean willStartTimer = false;

        if (subscribedTasks.isEmpty()) {
            willStartTimer = true;
        }

        if (!subscribedTasks.contains(task)) {
            SubscriberTask newTask = new SubscriberTask(task, factor);
            SubscriberTask prevTask = subscribedTasks.putIfAbsent(task, newTask);
            if (prevTask == null) {
                log.fine("Register timer task " + task + " for every " + // NOI18N
                        factor + " tick"); // NOI18N
            }
        }

        if (willStartTimer) {
            synchronized (lock) {
                if (timer == null) {
                    log.fine("Start D-Light Global Timer"); // NOI18N
                    timer = new Timer("D-Light Global Timer"); // NOI18N
                    timer.schedule(new DLightGlobalTimerTask(), 0, 100);
                }
            }
        }

    }

    public void unregisterTimerTask(final Callable<Integer> task) {
        SubscriberTask removedTask = subscribedTasks.remove(task);

        if (removedTask != null) {
            log.fine("Timer task " + task + " unregistered"); // NOI18N
        }

        if (subscribedTasks.isEmpty()) {
            synchronized (lock) {
                if (timer != null) {
                    log.fine("No tasks left for D-Light Global Timer; Stop timer"); // NOI18N
                    timer.cancel();
                    timer = null;
                }
            }
        }

        Future<Integer> submittedTask = runningHash.remove(task);

        if (submittedTask != null) {
            submittedTask.cancel(true);
        }
    }

    private class DLightGlobalTimerTask extends TimerTask {

        @Override
        // On every timer tick ...
        public void run() {

            Enumeration<Callable<Integer>> keys = subscribedTasks.keys();
            while (keys.hasMoreElements()) {
                Callable<Integer> key = keys.nextElement();
                SubscriberTask task = subscribedTasks.get(key);
                Future<Integer> future = runningHash.get(key);
                if ((future == null || future.isDone()) && task != null) {
                    if (task.count-- == 0) {
                        task.count = task.factor;
                        runningHash.put(task.callable,
                                timerTasksExecutor.submit(task.callable));
                    }
                }
            }
        }
    }

    private static class SubscriberTask {

        private final Callable<Integer> callable;
        private final int factor;
        private int count;

        public SubscriberTask(Callable<Integer> callable, int factor) {
            if (factor <= 0) {
                throw new IllegalArgumentException("Timer factor should be a positive integer"); // NOI18N
            }

            this.callable = callable;
            this.factor = factor;
            this.count = factor;
        }
    }
}
