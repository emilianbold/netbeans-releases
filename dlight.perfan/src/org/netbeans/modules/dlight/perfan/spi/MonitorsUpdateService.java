/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.perfan.storage.impl.ErprintSession;
import org.netbeans.modules.dlight.perfan.storage.impl.ExperimentStatistics;
import org.netbeans.modules.dlight.perfan.storage.impl.Metrics;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;

public class MonitorsUpdateService {

    private static Pattern lineStartsWithIntegerPattern = Pattern.compile("^ *([0-9]+).*$"); // NOI18N
    private static final Logger log = DLightLogger.getLogger(MonitorsUpdateService.class);
    private static final List<String> syncColNames = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_ulockSummary.getColumnName()));
    private static final List<String> leaksColNames = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_leakSize.getColumnName()));
    private final ErprintSession erprintSession;
    private final SunStudioDataCollector ssdc;
    private final boolean isSyncMonitor;
    private final boolean isMemoryMonitor;
    private final Metrics metrics;
    private Future task;
    private volatile boolean serviceStarted;
    private BlockingQueue<Object> requestsQueue = new LinkedBlockingQueue<Object>(1);

    MonitorsUpdateService(SunStudioDataCollector ssdc,
            ExecutionEnvironment execEnv,
            String sproHome, String experimentDir,
            Collection<CollectedInfo> collectedInfo) {
        this.ssdc = ssdc;
        this.erprintSession = new ErprintSession(execEnv, sproHome, experimentDir);
        isSyncMonitor = collectedInfo.contains(SunStudioDCConfiguration.CollectedInfo.SYNCSUMMARY);
        isMemoryMonitor = collectedInfo.contains(SunStudioDCConfiguration.CollectedInfo.MEMSUMMARY);
        metrics = isMemoryMonitor ? Metrics.constructFrom(
                Arrays.asList(SunStudioDCConfiguration.c_leakSize),
                Arrays.asList(SunStudioDCConfiguration.c_leakSize))
                : null;

        serviceStarted = false;
    }

    public void start() {
        synchronized (this) {
            if (isBlank()) {
                return;
            }

            if (serviceStarted) {
                // Already started
                return;
            }

            task = DLightExecutorService.scheduleAtFixedRate(new Runnable() {

                public void run() {
                    if (requestsQueue.remainingCapacity() == 0) {
                        return;
                    }

                    try {
                        requestsQueue.put(new Object());
                    } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
                    }
                }
            }, 1, TimeUnit.SECONDS, "SunStudio monitors update task"); // NOI18N

            serviceStarted = true;

            DLightExecutorService.submit(new Updater(), MonitorsUpdateService.class.getName());
        }
    }

    public void stop() {
        synchronized (this) {
            serviceStarted = false;

            if (task != null) {
                task.cancel(true);
            }

            erprintSession.close();
        }
    }

    boolean isBlank() {
        return !(isMemoryMonitor || isSyncMonitor);
    }

    private class Updater implements Runnable {

        public void run() {
            while (serviceStarted) {
                try {
                    requestsQueue.take();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                boolean restarted = false;

                List<DataRow> newData = new ArrayList<DataRow>();

                try {
                    if (isSyncMonitor) {
                        try {
                            ExperimentStatistics stat = erprintSession.getExperimentStatistics(5, !restarted);
                            restarted = true;
                            if (stat != null) {
                                newData.add(new DataRow(syncColNames, Arrays.asList(stat.getULock_p())));
                            }
                        } catch (Throwable ex) {
                            log.log(Level.FINEST, "Exception while getExperimentStatistics in MonitorUpdateService", ex);
                        }
                    }

                    if (isMemoryMonitor) {
                        try {
                            String[] result = erprintSession.getHotFunctions(metrics, 1, 5, !restarted);
                            restarted = true;

                            if (result == null || result.length == 0) {
                                continue;
                            }

                            Matcher m = lineStartsWithIntegerPattern.matcher(result[0]);

                            if (!m.matches()) {
                                continue;
                            }

                            String value = m.group(1);

                            if (value != null) {
                                Long lvalue = Long.valueOf(0);

                                try {
                                    lvalue = Long.valueOf(value);
                                } catch (NumberFormatException ex) {
                                }

                                newData.add(new DataRow(leaksColNames, Arrays.asList(lvalue)));
                            }
                        } catch (Throwable ex) {
                            log.log(Level.FINEST, "Exception while getHotFunctions in MonitorUpdateService", ex);
                        }
                    }

                    ssdc.updateIndicators(newData);
                    
                } catch (Throwable ex) {
                    log.log(Level.FINEST, "Exception while updateIndicators in MonitorUpdateService", ex);
                }

            }
        }
    }
}
