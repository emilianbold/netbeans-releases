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
package org.netbeans.modules.dlight.collector.procfs.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.Info;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.impl.DLightTargetAccessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.collector.procfs.ProcFSDCConfiguration;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.msa.support.MSASQLTables;
import org.netbeans.modules.dlight.msa.support.MSASQLTables.msa;
import org.netbeans.modules.dlight.procfs.api.LWPUsage;
import org.netbeans.modules.dlight.procfs.api.PStatus;
import org.netbeans.modules.dlight.procfs.api.PStatus.ThreadsInfo;
import org.netbeans.modules.dlight.procfs.api.PUsage;
import org.netbeans.modules.dlight.procfs.api.SamplingData;
import org.netbeans.modules.dlight.procfs.reader.api.ProcReader;
import org.netbeans.modules.dlight.procfs.reader.api.ProcReaderFactory;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.collector.DataCollectorListener;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.TasksCachedProcessor;

public class ProcFSDataCollector
        extends IndicatorDataProvider<ProcFSDCConfiguration>
        implements DataCollector<ProcFSDCConfiguration> {

    private final static Logger log = DLightLogger.getLogger(ProcFSDataCollector.class);
    private final List<ValidationListener> validationListeners = new CopyOnWriteArrayList<ValidationListener>();
    private final ProcFSDCConfiguration configuration;
    private TasksCachedProcessor<DLightTarget, ValidationStatus> validator =
            new TasksCachedProcessor<DLightTarget, ValidationStatus>(new ProcFSDataCollectorValidator(), false);
    private DLightTarget target;
    private volatile Future<?> mainLoop;
    private final boolean msaEnabled, prstatEnabled;
    private final List<DataTableMetadata> providedDataTables;
    private SQLDataStorage sqlStorage;
    private PreparedStatement insertMSAStatement = null;
    private final List<DataCollectorListener> listeners = new ArrayList<DataCollectorListener>();

    public ProcFSDataCollector(ProcFSDCConfiguration configuration) {
        this.configuration = configuration;

        ProcFSDCConfigurationAccessor access = ProcFSDCConfigurationAccessor.getDefault();
        msaEnabled = access.msaSampling(configuration) > 0;
        prstatEnabled = access.procInfoSampling(configuration) > 0;

        List<DataTableMetadata> tables = new ArrayList<DataTableMetadata>();

        if (prstatEnabled) {
            tables.add(MSASQLTables.prstat.tableMetadata);
        }

        if (msaEnabled) {
            tables.add(MSASQLTables.msa.tableMetadata);
            tables.add(MSASQLTables.lwps.tableMetadata);
        }

        providedDataTables = Collections.unmodifiableList(tables);
    }

/**
     * Adds collector state listener, all listeners will be notified about
     * collector state change.
     * @param listener add listener
     */
    @Override
    public final void addDataCollectorListener(DataCollectorListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    /**
     * Remove collector listener
     * @param listener listener to remove from the list
     */
    @Override
    public final void removeDataCollectorListener(DataCollectorListener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifies listeners target state changed in separate thread
     * @param oldState state target was
     * @param newState state  target is
     */
    protected final void notifyListeners(final CollectorState state) {
        DataCollectorListener[] ll;

        synchronized (this) {
            ll = listeners.toArray(new DataCollectorListener[0]);
        }

        final CountDownLatch doneFlag = new CountDownLatch(ll.length);

        // Will do notification in parallel, but wait until all listeners
        // finish processing of event.
        for (final DataCollectorListener l : ll) {
            DLightExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        l.collectorStateChanged(ProcFSDataCollector.this, state);
                    } finally {
                        doneFlag.countDown();
                    }
                }
            }, "Notifying " + l); // NOI18N
        }

        try {
            doneFlag.await();
        } catch (InterruptedException ex) {
        }

    }

    @Override
    public String getName() {
        return "ProcFSReader"; // NOI18N
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                targetStarted(event.target);
                break;
            case FAILED:
                targetFinished(event.target);
                break;
            case TERMINATED:
                targetFinished(event.target);
                break;
            case DONE:
                targetFinished(event.target);
                break;
            case STOPPED:
                targetFinished(event.target);
                return;
        }
    }

    public ValidationStatus validate(final DLightTarget target) {
        this.target = target;
        ValidationStatus result = ValidationStatus.initialStatus();

        try {
            result = validator.compute(target);
        } catch (InterruptedException ex) {
            result = ValidationStatus.invalidStatus(ex.getMessage());
        }

        notifyStatusChanged(null, result);
        return result;
    }

    public synchronized void invalidate() {
        validator.remove(target);
        target = null;
        notifyStatusChanged(null, ValidationStatus.initialStatus());
    }

    public ValidationStatus getValidationStatus() {
        if (target == null) {
            return ValidationStatus.initialStatus();
        }

        return validate(target);
    }

    public void addValidationListener(ValidationListener listener) {
        if (!validationListeners.contains(listener)) {
            validationListeners.add(listener);
        }
    }

    public void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private final void notifyStatusChanged(ValidationStatus oldStatus, ValidationStatus newStatus) {
        if (oldStatus != null && oldStatus.equals(newStatus)) {
            return;
        }
        for (ValidationListener validationListener : validationListeners) {
            validationListener.validationStateChanged(this, oldStatus, newStatus);
        }
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<DataStorageType> getRequiredDataStorageTypes() {
        DataStorageTypeFactory dstf = DataStorageTypeFactory.getInstance();

        return Arrays.asList(
                dstf.getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

    public List<DataTableMetadata> getDataTablesMetadata() {
        return providedDataTables;
    }

    public void init(Map<DataStorageType, DataStorage> storages, DLightTarget target) {
        // TODO: make it better...
        for (DataStorage ds : storages.values()) {
            if (ds instanceof SQLDataStorage) {
                SQLDataStorage storage = (SQLDataStorage) ds;
                if (prepareStatements(storage)) {
                    sqlStorage = storage;
                    break;
                }
            }
        }
    }

    public boolean isAttachable() {
        return true;
    }

    public String getCmd() {
        throw new UnsupportedOperationException("Not supported operation."); // NOI18N
    }

    public String[] getArgs() {
        throw new UnsupportedOperationException("Not supported operation."); // NOI18N
    }

    private void targetStarted(final DLightTarget target) {
        if (this.target != target) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(String.format("Got UNEXPECTED targetStarted() notification. " + // NOI18N
                        "Validation was performed against %s, but notified about %s", // NOI18N
                        this.target.toString(), target.toString()));
            }
            return;
        }

        if (!prstatEnabled && !msaEnabled) {
            // Nothing to do...
            return;
        }

        final int pid = ((AttachableTarget) target).getPID();
        final ProcReader reader = ProcReaderFactory.getReader(target.getExecEnv(), pid);

        DLightTargetAccessor<? extends DLightTarget> targetAccess = DLightTargetAccessor.getDefault();
        Info targetInfo = targetAccess.getDLightTargetInfo(target);
        String attachTimeString = targetInfo.getInfo().get("AttachTimeNano"); // NOI18N
        // If the target was started using "attach",
        // then current time will be used
        // as an offset of collected data events...
        // If it is a "regular" run, then a fair ts (from proc)
        // will be used.
        long attachTime = 0;
        if (attachTimeString != null) {
            try {
                attachTime = Long.parseLong(attachTimeString);
            } catch (NumberFormatException nfe) {
            }
        }

        mainLoop = DLightExecutorService.scheduleAtFixedRate(
                new FetchAndUpdateTask(reader, attachTime),
                1, TimeUnit.SECONDS,
                "ProcFSDataCollector data fetching loop"); // NOI18N
    }

    private void targetFinished(DLightTarget target) {
        if (this.target == target && mainLoop != null) {
            mainLoop.cancel(true);
        }
    }

    private boolean prepareStatements(SQLDataStorage storage) {
        boolean result = false;
        try {
            insertMSAStatement = storage.prepareStatement(
                    String.format("insert into %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) " + // NOI18N
                    "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", // NOI18N
                    msa.tableMetadata.getName(),
                    msa.TIMESTAMP.getColumnName(), msa.SAMPLE.getColumnName(), msa.LWP_ID.getColumnName(),
                    msa.LWP_MSA_USR.getColumnName(), msa.LWP_MSA_SYS.getColumnName(), msa.LWP_MSA_TRP.getColumnName(),
                    msa.LWP_MSA_TFL.getColumnName(), msa.LWP_MSA_DFL.getColumnName(), msa.LWP_MSA_KFL.getColumnName(),
                    msa.LWP_MSA_LCK.getColumnName(), msa.LWP_MSA_SLP.getColumnName(), msa.LWP_MSA_LAT.getColumnName(),
                    msa.LWP_MSA_STP.getColumnName()));
            result = true;
        } catch (SQLException ex) {
            log.warning(String.format("Exception while preparing insert statement (%s)", ex.getMessage())); // NOI18N
        }
        return result;
    }

    private class FetchAndUpdateTask implements Runnable {

        private final ProcReader reader;
        private final LWPsTracker lwpsTracker = new LWPsTracker();
        private final AtomicLong processStartTime = new AtomicLong(0);
        private final long attachTimeNano;

        public FetchAndUpdateTask(final ProcReader reader, long attachTimeNano) {
            this.reader = reader;
            this.attachTimeNano = attachTimeNano;
        }

        private final long toNanoOffset(long timenano) {
            return timenano - ((attachTimeNano > 0) ? attachTimeNano : processStartTime.get());
        }

        public void run() {
            try {
                if (prstatEnabled) {
                    PStatus processStatus = reader.getProcessStatus();
                    PUsage processUsage = reader.getProcessUsage();

                    if (processStatus == null || processUsage == null) {
                        // Cannot get even this data...
                        // No sense to continue..
                        return;
                    }

                    processStartTime.compareAndSet(0, processUsage.getUsageInfo().pr_create);

                    ThreadsInfo tinfo = processStatus.getThreadInfo();
                    SamplingData p_samplingInfo = processUsage.getSamplingData();
                    PUsage.MSAInfo p_msaInfo = processUsage.getMSAInfo();
                    float normCoef = (float) tinfo.pr_nlwp / p_msaInfo.sum_states;

                    ProcFSDataCollector.this.notifyIndicators(Arrays.asList(
                            new DataRow(MSASQLTables.prstat.tableMetadata.getColumnNames(),
                            Arrays.asList(
                            toNanoOffset(p_samplingInfo.timestamp),
                            p_samplingInfo.sample,
                            tinfo.pr_nlwp,
                            tinfo.pr_nzomb,
                            normCoef * (p_msaInfo.pr_utime + p_msaInfo.pr_stime + p_msaInfo.pr_ttime), // run
                            normCoef * (p_msaInfo.pr_ltime), // blocked
                            normCoef * (p_msaInfo.pr_tftime + p_msaInfo.pr_dftime + p_msaInfo.pr_kftime + p_msaInfo.pr_wtime + p_msaInfo.pr_stoptime), // wait
                            normCoef * (p_msaInfo.pr_slptime) // sleep
                            ))));

                }

                if (sqlStorage != null && msaEnabled) {
                    // Query information about each thread and store
                    // in in the database..

                    List<LWPUsage> lwpUsageData = reader.getThreadsInfo();
                    if (lwpUsageData != null) {
                        for (LWPUsage lwpUsage : lwpUsageData) {
                            try {
                                SamplingData lwp_samplingInfo = lwpUsage.getSamplingData();
                                LWPUsage.UsageInfo lwp_usageInfo = lwpUsage.getUsageInfo();
                                LWPUsage.MSAInfo lwp_msaInfo = lwpUsage.getMSAInfo();

                                lwpsTracker.update(lwpUsage);

                                insertMSAStatement.setLong(1, toNanoOffset(lwp_samplingInfo.timestamp));
                                insertMSAStatement.setLong(2, lwp_samplingInfo.sample);
                                insertMSAStatement.setInt(3, lwp_usageInfo.pr_lwpid);
                                insertMSAStatement.setLong(4, lwp_msaInfo.pr_utime);
                                insertMSAStatement.setLong(5, lwp_msaInfo.pr_stime);
                                insertMSAStatement.setLong(6, lwp_msaInfo.pr_ttime);
                                insertMSAStatement.setLong(7, lwp_msaInfo.pr_tftime);
                                insertMSAStatement.setLong(8, lwp_msaInfo.pr_dftime);
                                insertMSAStatement.setLong(9, lwp_msaInfo.pr_kftime);
                                insertMSAStatement.setLong(10, lwp_msaInfo.pr_ltime);
                                insertMSAStatement.setLong(11, lwp_msaInfo.pr_slptime);
                                insertMSAStatement.setLong(12, lwp_msaInfo.pr_wtime);
                                insertMSAStatement.setLong(13, lwp_msaInfo.pr_stoptime);

                                insertMSAStatement.executeUpdate();
                            } catch (SQLException ex) {
                                if (log.isLoggable(Level.FINE)) {
                                    log.log(Level.FINE, "SQL exception", ex);
                                }
                            }
                        }
                        lwpsTracker.endOfUpdate();
                    }
                }
            } catch (Throwable th) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Unhandled exception!", th); // NOI18N
                }
            }
        }

        private final class LWPsTracker {

            private final Object lock = LWPsTracker.class.getName() + "_lock"; // NOI18N
            private final HashMap<Integer, LWPUsage> lastReportedLWPs = new HashMap<Integer, LWPUsage>();
            private final HashMap<Integer, LWPUsage> newReportedLWPs = new HashMap<Integer, LWPUsage>();
            private PreparedStatement insertStatement = null;
            private PreparedStatement updateStatement = null;

            public LWPsTracker() {
                try {
                    insertStatement = sqlStorage.prepareStatement(
                            String.format("insert into %s (%s, %s, %s) values (?, ?, null)", // NOI18N
                            MSASQLTables.lwps.tableMetadata.getName(),
                            MSASQLTables.lwps.LWP_ID.getColumnName(),
                            MSASQLTables.lwps.LWP_START.getColumnName(),
                            MSASQLTables.lwps.LWP_END.getColumnName()));
                    updateStatement = sqlStorage.prepareStatement(
                            String.format("update %s set %s = ? where %s = ?", // NOI18N
                            MSASQLTables.lwps.tableMetadata.getName(),
                            MSASQLTables.lwps.LWP_END.getColumnName(),
                            MSASQLTables.lwps.LWP_ID.getColumnName()));
                } catch (Throwable th) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "will not provide data...", th); // NOI18N
                    }

                }
            }

            private void update(LWPUsage lwp_usageInfo) {
                if (insertStatement == null || updateStatement == null) {
                    return;
                }

                boolean thread_started = false;
                int id = lwp_usageInfo.getUsageInfo().pr_lwpid;

                synchronized (lock) {
                    newReportedLWPs.put(id, lwp_usageInfo);
                    if (!lastReportedLWPs.containsKey(id)) {
                        thread_started = true;
                    }
                }

                if (thread_started) {
                    try {
                        insertStatement.setInt(1, id);
                        insertStatement.setLong(2, toNanoOffset(attachTimeNano > 0 ? attachTimeNano : lwp_usageInfo.getUsageInfo().pr_create)); // USE MILLISECONDS PASSED FROM START
                        insertStatement.executeUpdate();
                    } catch (SQLException ex) {
                        if (log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "", ex);
                        }
                    }
                }
            }

            private void endOfUpdate() {
                Set<LWPUsage> deadThreads = new HashSet<LWPUsage>();

                synchronized (lock) {
                    for (Integer id : lastReportedLWPs.keySet()) {
                        if (!newReportedLWPs.containsKey(id)) {
                            deadThreads.add(lastReportedLWPs.get(id));
                        }
                    }

                    lastReportedLWPs.clear();
                    lastReportedLWPs.putAll(newReportedLWPs);
                    newReportedLWPs.clear();
                }

                for (LWPUsage deadThreadUsage : deadThreads) {
                    try {
                        updateStatement.setLong(1, toNanoOffset(deadThreadUsage.getSamplingData().timestamp));
                        updateStatement.setInt(2, deadThreadUsage.getUsageInfo().pr_lwpid);
                        updateStatement.executeUpdate();
                    } catch (SQLException ex) {
                        if (log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "", ex);
                        }
                    }
                }
            }
        }
    }
}
