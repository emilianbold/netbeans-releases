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
package org.netbeans.modules.dlight.procfs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.msa.support.MSASQLTables;
import org.netbeans.modules.dlight.msa.support.MSASQLTables.msa;
import org.netbeans.modules.dlight.procfs.ProcFSDCConfiguration;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.TasksCachedProcessor;
import org.openide.util.Exceptions;

public class ProcFSDataCollector
        extends IndicatorDataProvider<ProcFSDCConfiguration>
        implements DataCollector<ProcFSDCConfiguration> {

    private final static Logger log = DLightLogger.getLogger(ProcFSDataCollector.class);
    private final List<ValidationListener> validationListeners = new CopyOnWriteArrayList<ValidationListener>();
    private final ProcFSDCConfiguration configuration;
    private TasksCachedProcessor<DLightTarget, ValidationStatus> validator =
            new TasksCachedProcessor<DLightTarget, ValidationStatus>(new ProcFSDataCollectorValidator(), false);
    private DLightTarget target;
    private final boolean msaEnabled, prstatEnabled;
    private final List<DataTableMetadata> providedDataTables;
    private SQLDataStorage sqlStorage;

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

    public ValidationStatus validate(DLightTarget target) {
        this.target = target;
        ValidationStatus result = ValidationStatus.initialStatus();

        try {
            result = validator.compute(target);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
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

    public void dataFiltersChanged(List<DataFilter> newSet) {
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
                sqlStorage = (SQLDataStorage) ds;
                break;
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
    Thread t;

    private void targetStarted(DLightTarget target) {
        if (this.target == target) {

            final int pid = ((AttachableTarget) target).getPID();
            final File lwp = new File("/proc/" + pid + "/lwp"); // NOI18N
            final File statusFile = new File("/proc/" + pid + "/status"); // NOI18N

            try {
                PStatus.PIDInfo pidinfo = PStatus.getPIDInfo(new FileInputStream(statusFile));

                System.err.println("PID from file: " + pidinfo.pr_pid + "; Read pid: " + pid);

                if (pidinfo.pr_pid != pid) {
                    DataReader.switchEndian();
                }
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }

            PreparedStatement _stmt = null;

            try {
                _stmt = sqlStorage.prepareStatement(
                        String.format("insert into %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) " + // NOI18N
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", // NOI18N
                        msa.tableMetadata.getName(),
                        msa.TIMESTAMP.getColumnName(), msa.SAMPLE.getColumnName(), msa.LWP_ID.getColumnName(),
                        msa.LWP_MSA_USR.getColumnName(), msa.LWP_MSA_SYS.getColumnName(), msa.LWP_MSA_TRP.getColumnName(),
                        msa.LWP_MSA_TFL.getColumnName(), msa.LWP_MSA_DFL.getColumnName(), msa.LWP_MSA_KFL.getColumnName(),
                        msa.LWP_MSA_LCK.getColumnName(), msa.LWP_MSA_SLP.getColumnName(), msa.LWP_MSA_LAT.getColumnName(),
                        msa.LWP_MSA_STP.getColumnName()));

            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }

            final PreparedStatement stmt = _stmt;

            t = new Thread(new Runnable() {

                long ts, sample;
                // normalized MSA details
                float futd, fstd, fttd, ftftd, fdftd, fkftd, fltd, fslptd, fwtd, fstoptd;
                final HashMap<String, UsageStatistics> lwpsStat = new HashMap<String, UsageStatistics>();
                final HashMap<String, Long> lastTimestamps = new HashMap<String, Long>();
                // threadID => threadRefID
                final HashSet<String> liveLWPs = new HashSet<String>();

                // TODO: need less synchronization
                public synchronized void run() {
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            int lwps_lcount = 0; //lwp.list().length;
                            int lwps_zcount = 0;

                            PStatus.ThreadsInfo threadsInfo;

                            try {
                                threadsInfo = PStatus.getThreadsInfo(new FileInputStream(statusFile));
                                lwps_lcount = threadsInfo.pr_nlwp;
                                lwps_zcount = threadsInfo.pr_nzomb;
                            } catch (IOException ex) {
                                // process is gone
                            }

                            if (lwps_lcount == 0) {
                                continue; // could break, actually...
                            }

                            getMSA("0", lwps_lcount); // NOI18N

                            ProcFSDataCollector.this.notifyIndicators(Arrays.asList(
                                    new DataRow(MSASQLTables.prstat.tableMetadata.getColumnNames(),
                                    Arrays.asList(
                                    ts, // timestamp
                                    sample, // sample
                                    lwps_lcount, // live lwps
                                    lwps_zcount, // zombie lwps
                                    futd + fstd + fttd, // run
                                    fltd, // blocked
                                    ftftd + fdftd + fkftd + fwtd + fstoptd, // wait
                                    fslptd // sleep
                                    ))));

                            if (sqlStorage != null && msaEnabled) {
                                try {
                                    // Query information about each thread and store
                                    // in in the database..

                                    final String[] lwps = lwp.list();

                                    // It could happen that some threads are gone
                                    // while we were between updates...
                                    Set<String> deadLWPIDs = new HashSet<String>(liveLWPs);
                                    deadLWPIDs.removeAll(Arrays.asList(lwps));

                                    // Remove them...
                                    for (String deadLWPID : deadLWPIDs) {
                                        setLWPEndTime(deadLWPID);
                                    }

                                    for (String id : lwps) {
                                        boolean result = getMSA(id, 1);

                                        if (!result) {
                                            // either LWP is gone or it is a zombie LWP...
                                            // In case when LWP is clearly finished - set end timestamp
                                            // Otherwise - just skip it!

                                            // LWP is gone...
                                            if (!new File(lwp, id).exists()) {
                                                log.info("LWP " + id + " is gone! " + Arrays.toString(liveLWPs.toArray()));
                                                setLWPEndTime(id);
                                                log.info("LWP      " + Arrays.toString(liveLWPs.toArray()));
                                            } else {
                                                if (!liveLWPs.contains(id)) {
                                                    // It was really "quick" thread -
                                                    // it died even before we registered it!

                                                    // TODO: here the problem is with non-zombie threads:
                                                    // if their execution time was less than sampling - we
                                                    // just will not notice it ...
                                                    setLWPStartTime(id, System.nanoTime());
                                                }
                                                log.info("LWP " + id + " is a ZOMBIE!");
                                            }
                                            continue;
                                        }

                                        if (!liveLWPs.contains(id)) {
                                            setLWPStartTime(id);
                                        }

                                        stmt.setLong(1, ts);
                                        stmt.setLong(2, sample);
                                        stmt.setInt(3, Integer.parseInt(id));
                                        stmt.setFloat(4, futd);
                                        stmt.setFloat(5, fstd);
                                        stmt.setFloat(6, fttd);
                                        stmt.setFloat(7, ftftd);
                                        stmt.setFloat(8, fdftd);
                                        stmt.setFloat(9, fkftd);
                                        stmt.setFloat(10, fltd);
                                        stmt.setFloat(11, fslptd);
                                        stmt.setFloat(12, fwtd);
                                        stmt.setFloat(13, fstoptd);

                                        stmt.executeUpdate();
                                    }
                                } catch (SQLException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    } catch (InterruptedException ex) {
                    } finally {
                        // Mark all live LWPs as finished
                        for (String id : liveLWPs.toArray(new String[0])) {
                            setLWPEndTime(id);
                        }
                    }

                }

                /**
                 * returns false if lwp / process is gone...
                 */
                private boolean getMSA(final String thrID, final int lwpscount) {
                    final File usageFile = "0".equals(thrID) ? new File("/proc/" + pid + "/usage") : new File("/proc/" + pid + "/lwp/" + thrID + "/lwpusage"); // NOI18N

                    // MSA deltas
                    long utd, std, ttd, tftd, dftd, kftd, ltd, slptd, wtd, stoptd;

                    UsageStatistics usage = null;

                    try {
                        if (usageFile.exists()) {
                            usage = UsageStatistics.get(new FileInputStream(usageFile));
                        }
                    } catch (IOException ex) {
                    }

                    if (usage == null) {
                        return false;
                    }

                    ts = usage.pr_tstamp.time;
                    long lastTS = lastTimestamps.containsKey(thrID) ? lastTimestamps.get(thrID) : 0;

                    if (lastTS == 0) {
                        lastTS = usage.pr_create.time;
                    }

                    sample = ts - lastTS;
                    lastTimestamps.put(thrID, ts);

                    UsageStatistics prevUsage = lwpsStat.get(thrID);

                    if (prevUsage == null) {
                        utd = usage.pr_utime.time;
                        std = usage.pr_stime.time;
                        ttd = usage.pr_ttime.time;
                        tftd = usage.pr_tftime.time;
                        dftd = usage.pr_dftime.time;
                        kftd = usage.pr_kftime.time;
                        ltd = usage.pr_ltime.time;
                        slptd = usage.pr_slptime.time;
                        wtd = usage.pr_wtime.time;
                        stoptd = usage.pr_stoptime.time;
                    } else {
                        utd = usage.pr_utime.time - prevUsage.pr_utime.time;
                        std = usage.pr_stime.time - prevUsage.pr_stime.time;
                        ttd = usage.pr_ttime.time - prevUsage.pr_ttime.time;
                        tftd = usage.pr_tftime.time - prevUsage.pr_tftime.time;
                        dftd = usage.pr_dftime.time - prevUsage.pr_dftime.time;
                        kftd = usage.pr_kftime.time - prevUsage.pr_kftime.time;
                        ltd = usage.pr_ltime.time - prevUsage.pr_ltime.time;
                        slptd = usage.pr_slptime.time - prevUsage.pr_slptime.time;
                        wtd = usage.pr_wtime.time - prevUsage.pr_wtime.time;
                        stoptd = usage.pr_stoptime.time - prevUsage.pr_stoptime.time;
                    }

                    long sum = utd + std + ttd + tftd + dftd + kftd + ltd + slptd + wtd + stoptd;

                    // normalize results ...
                    futd = (float) utd * lwpscount / sum;
                    fstd = (float) std * lwpscount / sum;
                    fttd = (float) ttd * lwpscount / sum;
                    ftftd = (float) tftd * lwpscount / sum;
                    fdftd = (float) dftd * lwpscount / sum;
                    fkftd = (float) kftd * lwpscount / sum;
                    fltd = (float) ltd * lwpscount / sum;
                    fslptd = (float) slptd * lwpscount / sum;
                    fwtd = (float) wtd * lwpscount / sum;
                    fstoptd = (float) stoptd * lwpscount / sum;
                    lwpsStat.put(thrID, usage);

                    return true;
                }

                private void setLWPStartTime(final String id, final long timestamp) {
                    liveLWPs.add(id);

                    String insertQuery = String.format("insert into %s (%s, %s, %s) values (%s, %d, null)", // NOI18N
                            MSASQLTables.lwps.tableMetadata.getName(),
                            MSASQLTables.lwps.LWP_ID.getColumnName(),
                            MSASQLTables.lwps.LWP_START.getColumnName(),
                            MSASQLTables.lwps.LWP_END.getColumnName(),
                            id,
                            timestamp);

                    try {
                        sqlStorage.executeUpdate(insertQuery);
                    } catch (SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                private void setLWPStartTime(final String id) {
                    final UsageStatistics lwpStat = lwpsStat.get(id);
                    log.info("LWP " + id + " started! " + lwpStat.pr_create.nano);
                    setLWPStartTime(id, lwpStat.pr_create.time);
                }

                private void setLWPEndTime(final String id) {
                    final long timestamp = System.nanoTime(); // TODO: not very accurate time
                    log.info("LWP " + id + " finished! " + timestamp);
                    liveLWPs.remove(id);

                    String updateQuery = String.format("update %s set %s = %d where %s = %s", // NOI18N
                            MSASQLTables.lwps.tableMetadata.getName(),
                            MSASQLTables.lwps.LWP_END.getColumnName(),
                            timestamp,
                            MSASQLTables.lwps.LWP_ID.getColumnName(),
                            id);
                    try {
                        sqlStorage.executeUpdate(updateQuery);
                    } catch (SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });

            t.start();
        }
    }

    private void targetFinished(DLightTarget target) {
        if (this.target == target) {
            log.info(target.toString() + " finished!");
            t.interrupt();
        }
    }
}
