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
package org.netbeans.modules.dlight.threadmap.dataprovider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.core.stack.api.ThreadInfo;
import org.netbeans.modules.dlight.core.stack.api.ThreadState;
import org.netbeans.modules.dlight.threadmap.api.ThreadData;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpQuery;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataQuery;
import org.netbeans.modules.dlight.threadmap.api.ThreadMapData;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.threadmap.spi.dataprovider.ThreadMapDataProvider;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpProvider;
import org.netbeans.modules.dlight.msa.support.MSASQLTables;
import org.netbeans.modules.dlight.threadmap.storage.ThreadInfoImpl;
import org.netbeans.modules.dlight.threadmap.storage.ThreadStateImpl;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;

public class ThreadMapDataProviderImpl implements ThreadMapDataProvider {

    private final static Logger log = DLightLogger.getLogger(ThreadMapDataProviderImpl.class);
    private SQLDataStorage sqlStorage;
    private PreparedStatement stmt;
    private final HashMap<Integer, ThreadInfo> ti = new HashMap<Integer, ThreadInfo>();
    private ThreadDumpProvider threadDumpProvider;

    public void attachTo(ServiceInfoDataStorage serviceInfoDataStorage) {
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
            stmt.setLong(1, query.getTimeFrom());
            stmt.setLong(2, query.getTimeTo());
            ResultSet rs = stmt.executeQuery();

            while (!rs.isLast()) {
                rs.next();
                if (rs.getRow() == 0){
                    break;
                }

                int threadID = rs.getInt(MSASQLTables.lwps.LWP_ID.getColumnName());

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

                long ts = rs.getLong(MSASQLTables.msa.TIMESTAMP.getColumnName());
                long sample = rs.getLong(MSASQLTables.msa.SAMPLE.getColumnName());

                int[] stateValues = new int[13];
                stateValues[0] = 1; // ???
                stateValues[1] = 0;
                stateValues[2] = 0;
                stateValues[3] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_USR.getColumnName()) * 100);
                stateValues[4] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_SYS.getColumnName()) * 100);
                stateValues[5] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_TRP.getColumnName()) * 100);
                stateValues[6] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_TFL.getColumnName()) * 100);
                stateValues[7] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_DFL.getColumnName()) * 100);
                stateValues[8] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_KFL.getColumnName()) * 100);
                stateValues[9] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_LAT.getColumnName()) * 100);
                stateValues[10] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_STP.getColumnName()) * 100);
                stateValues[11] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_LCK.getColumnName()) * 100);
                stateValues[12] = (int) (rs.getFloat(MSASQLTables.msa.LWP_MSA_SLP.getColumnName()) * 100);

                ThreadState threadState = new ThreadStateImpl(ts / 10, sample, stateValues);
                states.add(threadState);
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }

        ThreadMapData tmd = new ThreadMapData() {

            public List<ThreadData> getThreadsData() {
                return data;
            }

            public TimeDuration getSamplingPeriod() {
                // TODO: deprecate
                return new TimeDuration(TimeUnit.SECONDS, 1);
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

    public void attachTo(final DataStorage storage) {
        if (storage instanceof SQLDataStorage) {
            sqlStorage = (SQLDataStorage) storage;
            StringBuilder sqlQuery = new StringBuilder();
            sqlQuery.append("select ").append(MSASQLTables.msa.TIMESTAMP.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.SAMPLE.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_ID.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_USR.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_SYS.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_TRP.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_TFL.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_DFL.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_KFL.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_LCK.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_SLP.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_LAT.getColumnName()).append(", "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.LWP_MSA_STP.getColumnName());
            sqlQuery.append(" from ").append(MSASQLTables.msa.tableMetadata.getName()).append(" where "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.TIMESTAMP.getColumnName()).append(" >= ? and "); // NOI18N
            sqlQuery.append(MSASQLTables.msa.TIMESTAMP.getColumnName()).append(" < ?"); // NOI18N
            try {
                stmt = sqlStorage.prepareStatement(sqlQuery.toString());
            } catch (SQLException ex) {
                Exceptions.printStackTrace(ex);
            }
            ti.clear();
        }
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    private ThreadInfo getLWPInfo(int lwpID) {
        ThreadInfo lwpInfo = null;
        try {
            String lwpQuery = String.format("select %s from %s where %s = %d", // NOI18N
                    MSASQLTables.lwps.LWP_START.getColumnName(),
                    MSASQLTables.lwps.tableMetadata.getName(),
                    MSASQLTables.lwps.LWP_ID.getColumnName(),
                    lwpID);

            ResultSet rset = sqlStorage.select(null, null, lwpQuery);

            if (!rset.next()) {
                return null;
            }

            long startts = rset.getLong(1);
            lwpInfo = new ThreadInfoImpl(lwpID, "Thread " + lwpID, startts); // NOI18N

        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return lwpInfo;
    }
}
