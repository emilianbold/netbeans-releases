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
package org.netbeans.modules.dlight.perfan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.perfan.dataprovider.MemoryMetric;
import org.netbeans.modules.dlight.perfan.dataprovider.THAMetric;
import org.netbeans.modules.dlight.perfan.dataprovider.TimeMetric;
import org.netbeans.modules.dlight.perfan.impl.SunStudioDCConfigurationAccessor;
import org.netbeans.modules.dlight.perfan.spi.SunStudioIDsProvider;
import org.openide.util.NbBundle;

/**
 * This class is configuration classs to create {@link @org-netbeans-modules-dlight@/org/netbeans/modules/dlight/spi/collector/DataCollector.html}
 *  based onbn SunStudio Performance Analyzer.
 * This collector can collect the following information for Java and Native code:
 * <ul>
 * <li>Functions list with metrics: Incl. Time and Excl.Time</li>
 * <li>Functions list with the synchronization metrics:  Sync. Wait Time and Sync. Wait Count</li>
 * <li>Functoins list containing memory leaks</li>
 * </ul>
 */
public final class SunStudioDCConfiguration
        implements DataCollectorConfiguration, IndicatorDataProviderConfiguration {

    private static final String CPU_TABLE_NAME = "SunStudioCPUDetailedData"; // NOI18N
    private static final String SYNC_TABLE_NAME = "SunStudioSyncDetailedData"; // NOI18N
    private static final String MEM_TABLE_NAME = "SunStudioMemDetailedData"; // NOI18N
    private static final String DATARACE_TABLE_NAME = "SunStudioDataRacesDetailedData"; // NOI18N
    private static final String DEADLOCKS_TABLE_NAME = "SunStudioDeadlocksDetailedData"; // NOI18N
    public static final Column c_name = new Column("name", String.class, loc("SSDataCollector.ColumnName.name"), null); // NOI18N
    public static final Column c_address = new Column("address", String.class, loc("SSDataCollector.ColumnName.address"), null); // NOI18N
    public static final Column c_eUser =
            new Column(TimeMetric.UserFuncTimeExclusive.getMetricID(),
            TimeMetric.UserFuncTimeExclusive.getMetricValueClass(),
            TimeMetric.UserFuncTimeExclusive.getMetricDisplayedName(), null);
    public static final Column c_iUser =
            new Column(TimeMetric.UserFuncTimeInclusive.getMetricID(),
            TimeMetric.UserFuncTimeInclusive.getMetricValueClass(),
            TimeMetric.UserFuncTimeInclusive.getMetricDisplayedName(), null);
    public static final Column c_iSync =
            new Column(TimeMetric.SyncWaitTimeInclusive.getMetricID(),
            TimeMetric.SyncWaitTimeInclusive.getMetricValueClass(),
            TimeMetric.SyncWaitTimeInclusive.getMetricDisplayedName(), null);
    public static final Column c_iSyncn =
            new Column(TimeMetric.SyncWaitCallInclusive.getMetricID(),
            TimeMetric.SyncWaitCallInclusive.getMetricValueClass(),
            TimeMetric.SyncWaitCallInclusive.getMetricDisplayedName(), null);
    public static final Column c_eSync =
            new Column(TimeMetric.SyncWaitTimeExclusive.getMetricID(),
            TimeMetric.SyncWaitTimeExclusive.getMetricValueClass(),
            TimeMetric.SyncWaitTimeExclusive.getMetricDisplayedName(), null);
    public static final Column c_eSyncn =
            new Column(TimeMetric.SyncWaitCallExclusive.getMetricID(),
            TimeMetric.SyncWaitCallExclusive.getMetricValueClass(),
            TimeMetric.SyncWaitCallExclusive.getMetricDisplayedName(), null);
    public static final Column c_leakCount =
            new Column(MemoryMetric.LeaksCountMetric.getMetricID(),
            MemoryMetric.LeaksCountMetric.getMetricValueClass(),
            MemoryMetric.LeaksCountMetric.getMetricDisplayedName(), null);
    public static final Column c_leakSize =
            new Column(MemoryMetric.LeakBytesMetric.getMetricID(),
            MemoryMetric.LeakBytesMetric.getMetricValueClass(),
            MemoryMetric.LeakBytesMetric.getMetricDisplayedName(), null);
    public static final Column c_Deadlocks =
            new Column(THAMetric.DeadlockMetric.getMetricID(),
            THAMetric.DeadlockMetric.getMetricValueClass(),
            THAMetric.DeadlockMetric.getMetricDisplayedName(), null);
    public static final Column c_Datarace =
            new Column(THAMetric.RaceMetric.getMetricID(),
            THAMetric.RaceMetric.getMetricValueClass(),
            THAMetric.RaceMetric.getMetricDisplayedName(), null);
    public static final Column c_ulockSummary = new Column("user_lock", Long.class, loc("SSDataCollector.ColumnName.user_lock"), null); // NOI18N
    public static final Column c_threadsCount = new Column("ss_threads", Long.class, loc("SSDataCollector.ColumnName.threads"), null); // NOI18N

    static {
        SunStudioDCConfigurationAccessor.setDefault(new SunStudioDCConfigurationAccessorImpl());
    }

    /**
     * Types of information to be collected by SunStudio Performance Analyzer
     */
    public enum CollectedInfo {

        /**
         * Collects Functions Info with the metrics : Inclusive Time  and Exclusive Time
         */
        FUNCTIONS_LIST,
        /**
         * Collects synchronization information: Sync. Wait Time and Sync. Wait Count
         */
        SYNCHRONIZATION,
        /**
         * Collects information about memory leaks
         */
        MEMORY,
        SYNCSUMMARY,
        MEMSUMMARY,
        DATARACES,
        DEADLOCKS,
    }
    private final Set<CollectedInfo> collectedInfo = EnumSet.noneOf(CollectedInfo.class);

    /**
     * Creates new SunStudio Data Collector Configuration which should collect information <code>info</code>
     * @param info information to be collected
     */
    public SunStudioDCConfiguration(CollectedInfo info) {
        collectedInfo.add(info);
    }

    public static final DataTableMetadata getSyncTableMetadata(Column... columns) {
        return getTableMetadata(SYNC_TABLE_NAME,
                columns, Arrays.asList(c_iSync, c_iSyncn, c_eSync, c_eSyncn));
    }

    public static final DataTableMetadata getCPUTableMetadata(Column... columns) {
        return getTableMetadata(CPU_TABLE_NAME,
                columns, Arrays.asList(c_iUser, c_eUser));
    }

    public static final DataTableMetadata getMemTableMetadata(Column... columns) {
        return getTableMetadata(MEM_TABLE_NAME,
                columns, Arrays.asList(c_leakCount, c_leakSize));
    }

    public static final DataTableMetadata getDataRaceTableMetadata(Column... columns) {
        return getTableMetadata(DATARACE_TABLE_NAME,
                columns, Arrays.asList(c_Datarace));
    }

    public static final DataTableMetadata getDeadlockTableMetadata(Column... columns) {
        return getTableMetadata(DEADLOCKS_TABLE_NAME,
                columns, Arrays.asList(c_Deadlocks));
    }

    private static DataTableMetadata getTableMetadata(String tableName, Column[] columns, final List<Column> allowedColumns) {
        final List<Column> cols = new ArrayList<Column>();
        for (Column c : columns) {
            if (c == c_name) {
                continue;
            }
            if (allowedColumns.contains(c)) {
                cols.add(c);
            } else {
                throw new IllegalArgumentException("An attempt to create " + // NOI18N
                        "DataTableMetadata " + tableName + " with column " + // NOI18N
                        c.getColumnName() + " which is not related to it"); // NOI18N
            }
        }
        cols.add(c_name);
        return new DataTableMetadata(tableName, cols, null);
    }

    /**
     * Return name of the column which represents Function name
     * @return name of the column which represents Function name
     */
    public static final String getFunctionNameColumnName() {
        return "name"; // NOI18N
    }

    public String getID() {
        return SunStudioIDsProvider.DATA_COLLECTOR_ID;
    }

    Set<CollectedInfo> getCollectedInfo() {
        return collectedInfo;
    }

    private static final class SunStudioDCConfigurationAccessorImpl extends SunStudioDCConfigurationAccessor {

        @Override
        public Set<CollectedInfo> getCollectedInfo(SunStudioDCConfiguration configuration) {
            return configuration.getCollectedInfo();
        }

        public String getCPUTableName() {
            return CPU_TABLE_NAME;
        }

        public String getSyncTableName() {
            return SYNC_TABLE_NAME;
        }

        public String getMemTableName() {
            return MEM_TABLE_NAME;
        }

        public String getDeadlockTableName() {
            return DEADLOCKS_TABLE_NAME;
        }

        public String getDataraceTableName() {
            return DATARACE_TABLE_NAME;
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(SunStudioDCConfiguration.class, key, params);
    }
}
