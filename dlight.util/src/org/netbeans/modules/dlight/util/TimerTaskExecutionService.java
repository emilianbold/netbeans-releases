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

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public final class TimerTaskExecutionService {

    private final Object lock = new Object();
    private Collection<SubscriberTask> subscribedTasks = new ArrayList<SubscriberTask>();
    private Map<Callable<Integer>, Future<Integer>> hash = new HashMap<Callable<Integer>, Future<Integer>>();
    private ExecutorService timerTasksExecutor = null;
    private Timer timer = null;
    private static final Logger log = DLightLogger.getLogger(TimerTaskExecutionService.class);
    private static final TimerTaskExecutionService instance = new TimerTaskExecutionService();

    public static TimerTaskExecutionService getInstance() {
        return instance;
    }

    private TimerTaskExecutionService() {
    }

    public void registerTimerTask(Callable<Integer> task, int factor) {
        if (task == null) {
            return;
        }

        SubscriberTask st = new SubscriberTask(task, factor);
        boolean willStartTimer = false;

        synchronized (lock) {
            if (subscribedTasks.isEmpty()) {
                willStartTimer = true;
            } else {
                for (SubscriberTask t : subscribedTasks) {
                    if (t.callable == task) {
                        // Already registered
                        return;
                    }
                }
            }
            subscribedTasks.add(st);
        }

        if (willStartTimer) {
            timerTasksExecutor = Executors.newCachedThreadPool();
            timer = new Timer("D-Light Global Timer"); // NOI18N
            timer.schedule(new DLightGlobalTimerTask(), 0, 100);
        }

    }

    public void unregisterTimerTask(Callable task) {
        synchronized (lock) {
            for (SubscriberTask t : subscribedTasks.toArray(new SubscriberTask[0])) {
                if (t.callable == task) {
                    subscribedTasks.remove(t);
                }
            }
            hash.remove(task);
            if (subscribedTasks.isEmpty()) {
                // No subscribers - stop timer.
                log.info("NO MORE TASKS - STOP GLOBAL TIMER"); // NOI18N
                timer.cancel();
                timer = null;
                try {
                    timerTasksExecutor.shutdownNow();
                } catch (AccessControlException ace) {
                    // TODO: ??? When it appears?
                    DLightLogger.instance.severe(ace.getMessage());
                }
                timerTasksExecutor = null;
            }
        }
    }

    private class DLightGlobalTimerTask extends TimerTask {

        @Override
        // On every timer tick ...
        public void run() {
            synchronized (lock) {
                for (SubscriberTask task : subscribedTasks) {
                    // Will submit only finished tasks!
                    Future<Integer> f = hash.get(task.callable);

                    if (f == null || f.isDone() || f.isCancelled()) {
                        if (task.count-- == 0) {
                            task.count = task.factor;
                            hash.put(task.callable, timerTasksExecutor.submit(task.callable));
                        }
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
                throw new IllegalArgumentException("Timer factor should be positive integer"); // NOI18N
            }

            this.callable = callable;
            this.factor = factor;
            this.count = factor;
        }
    }
}
