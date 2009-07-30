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

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
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
import org.netbeans.modules.dlight.perfan.storage.impl.DataraceImpl;
import org.netbeans.modules.dlight.perfan.storage.impl.DeadlockImpl;
import org.netbeans.modules.dlight.perfan.storage.impl.ErprintSession;
import org.netbeans.modules.dlight.perfan.storage.impl.ExperimentStatistics;
import org.netbeans.modules.dlight.perfan.storage.impl.Metrics;
import org.netbeans.modules.dlight.perfan.storage.impl.ThreadsStatistic;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

public class MonitorsUpdateService {

    private static final Pattern METRIC_LINE_PATTERN = Pattern.compile("^ *([0-9]+(?:,[0-9]*)?) [^<]*$"); // NOI18N
    private static final DecimalFormat METRIC_FORMAT = new DecimalFormat();
    static {
        METRIC_FORMAT.getDecimalFormatSymbols().setDecimalSeparator(',');
    }

    private static final Metrics LEAK_METRICS = Metrics.constructFrom(
            Arrays.asList(SunStudioDCConfiguration.c_leakSize),
            Arrays.asList(SunStudioDCConfiguration.c_leakSize));
    private static final List<String> LEAK_COLNAMES = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_leakSize.getColumnName()));

    private static final Metrics SYNC_METRICS = Metrics.constructFrom(
            Arrays.asList(SunStudioDCConfiguration.c_eSync),
            Arrays.asList(SunStudioDCConfiguration.c_eSync));
    private static final List<String> SYNC_COLNAMES = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_ulockSummary.getColumnName(), SunStudioDCConfiguration.c_threadsCount.getColumnName()));

    private static final List<String> DATARACE_COLNAMES = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_Datarace.getColumnName()));

    private static final List<String> DEADLOCK_COLNAMES = Collections.unmodifiableList(
            Arrays.asList(SunStudioDCConfiguration.c_Deadlocks.getColumnName()));

    private static final Logger log = DLightLogger.getLogger(MonitorsUpdateService.class);
    private final SunStudioDataCollector ssdc;
    private final ExecutionEnvironment execEnv;
    private final String sproHome;
    private final String experimentDir;
    private final boolean isSyncMonitor;
    private final boolean isMemoryMonitor;
    private final boolean isDataRaceMonitor;
    private final boolean isDeadlockMonitor;
    private final Object updaterLock = new String(MonitorsUpdateService.class.getName() + " UpdaterLock"); // NOI18N
    private Updater updater = null;
    private BlockingQueue<Object> requestsQueue = new LinkedBlockingQueue<Object>(1);

    MonitorsUpdateService(SunStudioDataCollector ssdc,
            ExecutionEnvironment execEnv,
            String sproHome, String experimentDir,
            Set<CollectedInfo> collectedInfo) {
        this.ssdc = ssdc;
        this.sproHome = sproHome;
        this.execEnv = execEnv;
        this.experimentDir = experimentDir;
        isSyncMonitor = collectedInfo.contains(CollectedInfo.SYNCSUMMARY);
        isMemoryMonitor = collectedInfo.contains(CollectedInfo.MEMSUMMARY);
        isDataRaceMonitor = collectedInfo.contains(CollectedInfo.DATARACES);
        isDeadlockMonitor = collectedInfo.contains(CollectedInfo.DEADLOCKS);
    }

    public void start() {
        if (isBlank()) {
            return;
        }

        boolean linuxMode;
        try {
            linuxMode = HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.LINUX;
        } catch (CancellationException ex) {
            linuxMode = false;
        } catch (IOException ex) {
            linuxMode = false;
        }

        synchronized (updaterLock) {
            if (updater != null) {
                return;
            }

            updater = new Updater(linuxMode);
            DLightExecutorService.submit(updater, MonitorsUpdateService.class.getName());
        }
    }

    public void stop() {
        synchronized (updaterLock) {
            if (updater == null) {
                return;
            }

            updater.stop();
            updater = null;
        }
    }

    boolean isBlank() {
        return !(isMemoryMonitor || isSyncMonitor);
    }

    private class Updater implements Runnable {

        private volatile boolean isStopped = false;
        private volatile ErprintSession erprintSession;
        private boolean linuxMode;

        public Updater(boolean linuxMode) {
            this.linuxMode = linuxMode;
        }

        public void stop() {
            isStopped = true;
        }

        public void run() {
            if (erprintSession != null) {
                throw new IllegalStateException("Updater can be started only once!"); // NOI18N
            }

            erprintSession = new ErprintSession(execEnv, sproHome, experimentDir, ssdc);

            final Future notifyer = DLightExecutorService.scheduleAtFixedRate(new Runnable() {

                public void run() {
                    if (requestsQueue.remainingCapacity() == 0) {
                        return;
                    }

                    try {
                        requestsQueue.put(new Object());
                    } catch (InterruptedException ex) {
                    }
                }
            }, 1, TimeUnit.SECONDS, "SunStudio monitors update task"); // NOI18N

            try {
                double prevTime = 0;
                double prevLocks = 0;

                while (!isStopped) {
                    try {
                        requestsQueue.take();
                    } catch (InterruptedException ex) {
                        break;
                    }

                    boolean restarted = false;
                    List<DataRow> newData = new ArrayList<DataRow>();

                    try {
                        if (isSyncMonitor) {
                            ExperimentStatistics stat = erprintSession.getExperimentStatistics(5, !restarted);
                            ThreadsStatistic threadsStatistic = erprintSession.getThreadsStatistic(5, false);
                            restarted = true;

                            int currThreads = threadsStatistic.getThreadsCount();
                            Double ctime = stat.getDuration();
                            double currTime = (ctime == null) ? 0 : ctime.doubleValue();
                            double currLocks;

                            if (linuxMode) {
                                // on Linux we can't rely on User Lock line of experiment statistics
                                // see IZ#164758
                                String[] syncFunctions = erprintSession.getHotFunctions(SYNC_METRICS, Integer.MAX_VALUE, 5, false);
                                if (syncFunctions == null || syncFunctions.length == 0) {
                                    currLocks = prevLocks;
                                } else {
                                    currLocks = sumMetrics(syncFunctions);
                                }
                            } else {
                                Double clocks = stat.getULock();
                                currLocks = clocks == null ? 0 : clocks;
                            }

                            if (0.1 < currTime - prevTime) {
                                newData.add(new DataRow(SYNC_COLNAMES, Arrays.asList(
                                        100 * (currLocks - prevLocks) / (currTime - prevTime) / currThreads,
                                        currThreads)));

                                //System.out.printf("currTime: %f, currThreads: %d, currLocks: %f\n", currTime, currThreads, currLocks);
                                //System.out.printf("(100 * (%f - %f) / (%f - %f) / %d = %f\n",
                                //        currLocks, prevLocks, currTime, prevTime, currThreads,
                                //        100 * (currLocks - prevLocks) / (currTime - prevTime) / currThreads);

                                prevTime = currTime;
                                prevLocks = currLocks;
                            }
                        }

                        if (isMemoryMonitor) {
                            String[] leakFunctions = erprintSession.getHotFunctions(LEAK_METRICS, Integer.MAX_VALUE, 5, !restarted);
                            restarted = true;

                            long leaks;
                            if (leakFunctions == null || leakFunctions.length == 0) {
                                leaks = 0;
                            } else {
                                leaks = (long) sumMetrics(leakFunctions);
                            }

                            newData.add(new DataRow(LEAK_COLNAMES, Arrays.asList(leaks)));
                        }

                        if (isDataRaceMonitor) {
                            List<DataraceImpl> dataraces = erprintSession.getDataRaces(!restarted);
                            restarted = true;

                            newData.add(new DataRow(DATARACE_COLNAMES, Arrays.asList(dataraces.size())));
                        }

                        if (isDeadlockMonitor) {
                            List<DeadlockImpl> deadlocks = erprintSession.getDeadlocks(!restarted);
                            restarted = true;

                            newData.add(new DataRow(DEADLOCK_COLNAMES, Arrays.asList(deadlocks.size())));
                        }

                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        log.log(Level.FINEST, "Exception while updateIndicators in MonitorUpdateService: " + ex.toString());
                    } finally {
                        ssdc.updateIndicators(newData);
                    }
                }
            } finally {
                if (notifyer != null) {
                    notifyer.cancel(false);
                }

                if (erprintSession != null) {
                    erprintSession.close();
                }
            }
        }

        private double sumMetrics(String[] erprintOutput) {
            double sum = 0;
            for (String line : erprintOutput) {
                Matcher m = METRIC_LINE_PATTERN.matcher(line);
                if (m.matches()) {
                    String stringValue = m.group(1);
                    try {
                        sum += METRIC_FORMAT.parse(stringValue).doubleValue();
                    } catch (ParseException ex) {
                        log.log(Level.WARNING, null, ex);
                    }
                }
            }
            return sum;
        }

    }
}
