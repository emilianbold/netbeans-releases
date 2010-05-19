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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.threadmap.dataprovider;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilterFactory;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.core.stack.api.ThreadInfo;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshotQuery;
import org.netbeans.modules.dlight.core.stack.api.ThreadState;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.threadmap.api.ThreadData;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpQuery;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.threadmap.api.ThreadMapSummaryData;
import org.netbeans.modules.dlight.threadmap.api.ThreadSummaryData;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataQuery;
import org.netbeans.modules.dlight.threadmap.api.ThreadMapData;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataProvider;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpProvider;
import org.netbeans.modules.dlight.msa.support.MSASQLTables;
import org.netbeans.modules.dlight.threadmap.api.ThreadSummaryData.StateDuration;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapSummaryDataQuery;
import org.netbeans.modules.dlight.threadmap.storage.ThreadInfoImpl;
import org.netbeans.modules.dlight.threadmap.storage.ThreadStateImpl;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Range;

// TODO: review synchronization... oversynchronized.
public class ThreadMapDataProviderImpl implements ThreadMapDataProvider {

    private final static Logger log = DLightLogger.getLogger(ThreadMapDataProviderImpl.class);
    private SQLDataStorage sqlStorage;
    private PreparedStatement queryDataStatement;
    private final static String[] summaryColNames;
    private PreparedStatement querySummaryStatement;
    private PreparedStatement queryLWPInfo;
    private final HashMap<Integer, ThreadInfo> ti = new HashMap<Integer, ThreadInfo>();
    private ThreadDumpProvider threadDumpProvider;

    static {
        summaryColNames = new String[]{
                    "", // Index 0 is not used in SQL queries/result sets...
                    MSASQLTables.msa.LWP_ID.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_USR.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_SYS.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_TRP.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_TFL.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_DFL.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_KFL.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_LCK.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_SLP.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_LAT.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_STP.getColumnName(),};

    }

    public synchronized void attachTo(ServiceInfoDataStorage serviceInfoDataStorage) {
        DLightSession session = DLightManager.getDefault().getActiveSession();

        StackDataProvider sdp = (StackDataProvider) session.createDataProvider(
                DataModelSchemeProvider.getInstance().getScheme("model:threaddump"), null); // NOI18N ?????

        if (sdp != null) {
            threadDumpProvider = sdp.getThreadDumpProvider();
        }
    }

    public synchronized ThreadMapData queryData(final ThreadMapDataQuery query) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("DataQuery: [%d, %d], fullstate: %s", query.getTimeFrom(), query.getTimeTo(), query.isFullState() ? "yes" : "no")); // NOI18N
        }

        if (sqlStorage == null) {
            throw new NullPointerException("No STORAGE"); // NOI18N
        }

        final List<ThreadData> data = new ArrayList<ThreadData>();
        final HashMap<Integer, List<ThreadState>> lwpStates = new HashMap<Integer, List<ThreadState>>();

        try {

            queryDataStatement.setLong(1, query.getTimeFrom());
            queryDataStatement.setLong(2, query.getTimeTo());
            ResultSet rset = queryDataStatement.executeQuery();
            try {

                while (rset.next()) {
                    int threadID = rset.getInt(MSASQLTables.lwps.LWP_ID.getColumnName());

                    final ThreadInfo lwpInfo;
                    final List<ThreadState> states;

                    if (ti.containsKey(threadID)) {
                        lwpInfo = ti.get(threadID);
                    } else {
                        lwpInfo = getLWPInfo(threadID);
                        ti.put(threadID, lwpInfo);
                    }

                    if (lwpStates.containsKey(threadID)) {
                        states = lwpStates.get(threadID);
                    } else {
                        states = new ArrayList<ThreadState>();
                        lwpStates.put(threadID, states);
                        data.add(new ThreadData() {

                            public ThreadInfo getThreadInfo() {
                                return lwpInfo;
                            }

                            public List<ThreadState> getThreadState() {
                                return states;
                            }
                        });
                    }

                    long ts = rset.getLong(MSASQLTables.msa.TIMESTAMP.getColumnName());
                    long sample = rset.getLong(MSASQLTables.msa.SAMPLE.getColumnName());

                    long[] stateValues = new long[13];
                    stateValues[0] = 1; // ???
                    stateValues[1] = 0;
                    stateValues[2] = 0;
                    stateValues[3] = rset.getLong(MSASQLTables.msa.LWP_MSA_USR.getColumnName());
                    stateValues[4] = rset.getLong(MSASQLTables.msa.LWP_MSA_SYS.getColumnName());
                    stateValues[5] = rset.getLong(MSASQLTables.msa.LWP_MSA_TRP.getColumnName());
                    stateValues[6] = rset.getLong(MSASQLTables.msa.LWP_MSA_TFL.getColumnName());
                    stateValues[7] = rset.getLong(MSASQLTables.msa.LWP_MSA_DFL.getColumnName());
                    stateValues[8] = rset.getLong(MSASQLTables.msa.LWP_MSA_KFL.getColumnName());
                    stateValues[9] = rset.getLong(MSASQLTables.msa.LWP_MSA_LAT.getColumnName());
                    stateValues[10] = rset.getLong(MSASQLTables.msa.LWP_MSA_STP.getColumnName());
                    stateValues[11] = rset.getLong(MSASQLTables.msa.LWP_MSA_LCK.getColumnName());
                    stateValues[12] = rset.getLong(MSASQLTables.msa.LWP_MSA_SLP.getColumnName());

                    ThreadState threadState = new ThreadStateImpl(ts, sample, stateValues);
                    states.add(threadState);
                }

            } finally {
                rset.close();
            }

        } catch (SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        ThreadMapData tmd = new ThreadMapData() {

            public List<ThreadData> getThreadsData() {
                return data;
            }

            public boolean isSamplingMode() {
                return false;
            }
        };


        return tmd;
    }

    public synchronized ThreadDump getThreadDump(final ThreadDumpQuery query) {
        return threadDumpProvider == null ? null : threadDumpProvider.getThreadDump(query);
    }

    public Collection<ThreadSnapshot> getThreadSnapshots(ThreadSnapshotQuery query) {
        return threadDumpProvider == null ? null : threadDumpProvider.getThreadSnapshots(query);
    }

    public synchronized void attachTo(final DataStorage storage) {
        if (storage instanceof SQLDataStorage) {
            sqlStorage = (SQLDataStorage) storage;
            String query = String.format("select %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s from %s where %s >= ? and %s < ?", // NOI18N
                    MSASQLTables.msa.TIMESTAMP.getColumnName(),
                    MSASQLTables.msa.SAMPLE.getColumnName(),
                    MSASQLTables.msa.LWP_ID.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_USR.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_SYS.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_TRP.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_TFL.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_DFL.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_KFL.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_LCK.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_SLP.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_LAT.getColumnName(),
                    MSASQLTables.msa.LWP_MSA_STP.getColumnName(),
                    MSASQLTables.msa.tableMetadata.getName(),
                    MSASQLTables.msa.TIMESTAMP.getColumnName(),
                    MSASQLTables.msa.TIMESTAMP.getColumnName());

            try {
                queryDataStatement = sqlStorage.prepareStatement(query);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, null, ex);
            }

            query = String.format("select %s, sum(%s), sum(%s), sum(%s), sum(%s), sum(%s), sum(%s), sum(%s), sum(%s), sum(%s), sum(%s) from %s where %s >= ? and %s < ? group by %s", // NOI18N
                    summaryColNames[1],
                    summaryColNames[2],
                    summaryColNames[3],
                    summaryColNames[4],
                    summaryColNames[5],
                    summaryColNames[6],
                    summaryColNames[7],
                    summaryColNames[8],
                    summaryColNames[9],
                    summaryColNames[10],
                    summaryColNames[11],
                    MSASQLTables.msa.tableMetadata.getName(),
                    MSASQLTables.msa.TIMESTAMP.getColumnName(),
                    MSASQLTables.msa.TIMESTAMP.getColumnName(),
                    MSASQLTables.msa.LWP_ID.getColumnName());
            try {
                querySummaryStatement = sqlStorage.prepareStatement(query);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, null, ex);
            }

            query = String.format("select %s from %s where %s = ?", // NOI18N
                    MSASQLTables.lwps.LWP_START.getColumnName(),
                    MSASQLTables.lwps.tableMetadata.getName(),
                    MSASQLTables.lwps.LWP_ID.getColumnName());

            try {
                queryLWPInfo = sqlStorage.prepareStatement(query);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, null, ex);
            }

            ti.clear();
        }
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    private synchronized ThreadInfo getLWPInfo(int lwpID) {
        ThreadInfo lwpInfo = null;

        try {
            queryLWPInfo.setInt(1, lwpID);
            ResultSet rset = queryLWPInfo.executeQuery();
            try {

                if (rset.next()) {
                    long startts = rset.getLong(1);
                    lwpInfo = new ThreadInfoImpl(lwpID, "Thread " + lwpID, startts); // NOI18N
                }

            } finally {
                rset.close();
            }

        } catch (SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        return lwpInfo;
    }

    public synchronized ThreadMapSummaryData queryData(ThreadMapSummaryDataQuery query) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("DataQuery: [%s], fullstate: %s", Arrays.toString(query.getIntervals().toArray()), query.isFullState() ? "yes" : "no")); // NOI18N
        }

        if (sqlStorage == null) {
            throw new NullPointerException("No STORAGE"); // NOI18N
        }

        Collection<TimeIntervalDataFilter> intervals = query.getIntervals();

        //System.out.println("Intervals size: " + intervals.size()); // NOI18N

        if (intervals.isEmpty()) {
            intervals = new ArrayList<TimeIntervalDataFilter>(1);
            intervals.add(TimeIntervalDataFilterFactory.create(new Range<Long>(Long.valueOf(0), Long.MAX_VALUE)));
        }

        final List<ThreadSummaryData> result = new ArrayList<ThreadSummaryData>();

        // TODO: for now take the first one...
        Range<Long> interval = intervals.iterator().next().getInterval();

        try {
            querySummaryStatement.setLong(1, interval.getStart());
            querySummaryStatement.setLong(2, interval.getEnd());

            ResultSet rset = querySummaryStatement.executeQuery();
            try {

                while (rset.next()) {
                    int threadID = rset.getInt(MSASQLTables.lwps.LWP_ID.getColumnName());

                    final ThreadInfo lwpInfo = getLWPInfo(threadID);
                    if (lwpInfo == null) {
                        continue;
                    }
                    final List<StateDuration> states = new ArrayList<StateDuration>();

                    for (int colNum = 2; colNum < 12; colNum++) {
                        final long stateDuration = rset.getLong(colNum);
                        if (stateDuration > 0) {
                            final MSAState state = MSAState.fromCode(colNum - 2, query.isFullState());
                            states.add(new StateDuration() {

                                public MSAState getState() {
                                    return state;
                                }

                                public long getDuration() {
                                    return stateDuration;
                                }

                                @Override
                                public String toString() {
                                    return String.format("%s: %dns", state.toString(), stateDuration); // NOI18N
                                }
                            });
                        }
                    }

                    result.add(new ThreadSummaryData() {

                        public ThreadInfo getThreadInfo() {
                            return lwpInfo;
                        }

                        public List<StateDuration> getThreadSummary() {
                            return states;
                        }
                    });
                }

            } finally {
                rset.close();
            }

        } catch (SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        return new ThreadMapSummaryData() {

            public List<ThreadSummaryData> getThreadsData() {
                return result;
            }
        };
    }

    public List<ThreadNameDetails> getThreadNameDetails(final int threadID) {
        // empty stub implementation
        List<ThreadNameDetails> res = new ArrayList<ThreadNameDetails>();
        res.add(new ThreadNameDetails() {
            public String getName() {
                return "Thread"+threadID; // NOI18N
            }
            public Action goToSource() {
                return new AbstractAction(){
                    public void actionPerformed(ActionEvent e) {
                    }
                };
            }
        });
        return res;
    }
}
