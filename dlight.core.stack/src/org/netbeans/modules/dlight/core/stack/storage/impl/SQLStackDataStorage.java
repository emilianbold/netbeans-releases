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
package org.netbeans.modules.dlight.core.stack.storage.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpProvider;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpQuery;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshotQuery;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshotQuery.ThreadFilter;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshotQuery.TimeFilter;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionMetricsFactory;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.api.storage.DataTableMetadataFilter;
import org.netbeans.modules.dlight.api.storage.DataTableMetadataFilterSupport;
import org.netbeans.modules.dlight.core.stack.utils.FunctionNameUtils;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorage;
import org.netbeans.modules.dlight.spi.storage.ProxyDataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.Util;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Alexey Vladykin
 */
public class SQLStackDataStorage implements ProxyDataStorage, StackDataStorage, ThreadDumpProvider {

    private SQLDataStorage sqlStorage;
    private final List<DataTableMetadata> tableMetadatas;
    private final Map<String, PreparedStatement> stmtCache;
    private CppSymbolDemangler demangler = null;
    private ServiceInfoDataStorage serviceInfoDataStorage;

    public SQLStackDataStorage() {
        this.tableMetadatas = new ArrayList<DataTableMetadata>();
        funcCache = new HashMap<CharSequence, Long>();
        nodeCache = new HashMap<NodeCacheKey, Long>();
        funcIdSequence = 0;
        nodeIdSequence = 0;
        executor = new ExecutorThread();
        executor.setPriority(Thread.MIN_PRIORITY);
        executor.start();
        this.stmtCache = new ConcurrentHashMap<String, PreparedStatement>();
    }

    @Override
    public void syncAddData(String tableName, List<DataRow> data) {
        addData(tableName, data);
    }


    public final void attachTo(ServiceInfoDataStorage serviceInfoStorage) {
        this.serviceInfoDataStorage = serviceInfoStorage;
        CppSymbolDemanglerFactory factory = Lookup.getDefault().lookup(CppSymbolDemanglerFactory.class);
        if (factory != null) {
            demangler = factory.getForCurrentSession(serviceInfoStorage.getInfo());
        } else {
            demangler = null;
        }
    }

    public DataStorageType getBackendDataStorageType() {
        return DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE);
    }

    public List<DataTableMetadata> getBackendTablesMetadata() {
        return Collections.emptyList();
    }

    public void attachTo(DataStorage storage) {
        this.sqlStorage = (SQLDataStorage) storage;
        try {
            initTables();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private final <T extends DataFilter> Collection<T> getDataFilters(List<DataFilter> filters, Class<T> clazz) {
        Collection<T> result = new ArrayList<T>();
        for (DataFilter f : filters) {
            if (f.getClass() == clazz) {
                result.add(clazz.cast(f));
            }
        }
        return result;
    }

    public boolean hasData(DataTableMetadata data) {
        return data.isProvidedBy(tableMetadatas);
    }

    public void addData(String tableName, List<DataRow> data) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<DataStorageType> getStorageTypes() {
        return Collections.singletonList(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
    }

    public boolean supportsType(DataStorageType storageType) {
        return getStorageTypes().contains(storageType);
    }

    public void createTables(List<DataTableMetadata> tableMetadatas) {
        this.tableMetadatas.addAll(tableMetadatas);
    }

    // For tests ... 
    public boolean shutdown(boolean shutdownSqlStorage) {
        boolean result = shutdown();
        
        if (shutdownSqlStorage) {
            result = sqlStorage.shutdown() && result;
        }

        return result;
    }
    
    public boolean shutdown() {
        isRunning = false;
        funcCache.clear();
        nodeCache.clear();
        for (PreparedStatement stmt : stmtCache.values()) {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
        }
        stmtCache.clear();
        return true;
    }
////////////////////////////////////////////////////////////////////////////////
    private final Map<CharSequence, Long> funcCache;
    private final Map<NodeCacheKey, Long> nodeCache;
    private long funcIdSequence;
    private long nodeIdSequence;
    private final ExecutorThread executor;
    private boolean isRunning = true;

    private synchronized PreparedStatement getPreparedStatement(String sql) throws SQLException {
        PreparedStatement stmt = stmtCache.get(sql);
        if (stmt == null) {
            stmt = sqlStorage.prepareStatement(sql);
            stmtCache.put(sql, stmt);
        }
        return stmt;
    }

    private void initTables() throws SQLException, IOException {
        InputStream is = SQLStackDataStorage.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/core/stack/resources/schema.sql"); //NOI18N
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            StringBuilder buf = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-- ")) { //NOI18N
                    continue;
                }
                buf.append(line);
                if (line.endsWith(";")) { //NOI18N
                    String sql = buf.toString();
                    buf.setLength(0);
                    String sqlToExecute = sql.substring(0, sql.length() - 1);
                    sqlStorage.executeUpdate(sqlToExecute);
                }
            }
        } finally {
            reader.close();
        }
    }

    public long putStack(List<CharSequence> stack) {
        return putSample(stack, -1, -1);
    }

    public long putSample(List<CharSequence> stack, long timestamp, long duration) {
        long callerId = 0;
        Set<Long> funcs = new HashSet<Long>();
        for (int i = 0; i < stack.size(); ++i) {
            boolean isLeaf = i + 1 == stack.size();
            CharSequence funcName = stack.get(i);
            long funcId = generateFuncId(funcName);
            updateMetrics(funcId, false, timestamp, duration, !funcs.contains(funcId), isLeaf);
            funcs.add(funcId);
            long nodeId = generateNodeId(callerId, funcId, getOffset(funcName));
            updateMetrics(nodeId, true, timestamp, duration, true, isLeaf);
            callerId = nodeId;
        }
        return callerId;
    }

    public void flush() {
        try {
            executor.flush();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public List<FunctionMetric> getMetricsList() {
        return METRICS;
    }

    public List<FunctionCallWithMetric> getCallers(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        try {
            List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
            ResultSet rs = sqlStorage.select(null, null, prepareCallersSelect(path));
            try {
                while (rs.next()) {
                    Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                    metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                    metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                    String funcName = rs.getString(2);
                    result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName, rs.getString(3)), metrics));
                }
            } finally {
                rs.close();
            }
            demangle(result);
            return result;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    public List<FunctionCallWithMetric> getCallees(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        try {
            List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
            ResultSet rs = sqlStorage.select(null, null, prepareCalleesSelect(path));
            try {
                while (rs.next()) {
                    Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                    metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                    metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                    String funcName = rs.getString(2);
                    result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName, rs.getString(3)), metrics));
                }
            } finally {
                rs.close();
            }
            demangle(result);
            return result;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    public List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, List<DataFilter> filters, int limit) {
        try {
            List<FunctionCallWithMetric> funcList = new ArrayList<FunctionCallWithMetric>();
            TimeIntervalDataFilter timeFilter = Util.firstInstanceOf(TimeIntervalDataFilter.class, filters);
            PreparedStatement select = getPreparedStatement(
                    "SELECT Func.func_id, Func.func_name, Func.func_full_name, SUM(FuncMetricAggr.time_incl) AS time_incl, SUM(FuncMetricAggr.time_excl) AS time_excl " + //NOI18N
                    "FROM Func LEFT JOIN FuncMetricAggr ON Func.func_id = FuncMetricAggr.func_id " + // NOI18N
                    (timeFilter != null ? "WHERE ? <= FuncMetricAggr.bucket_id AND FuncMetricAggr.bucket_id < ? " : "") + // NOI18N
                    "GROUP BY Func.func_id, Func.func_name, Func.func_full_name " + // NOI18N
                    "ORDER BY " + metric.getMetricID() + " DESC"); //NOI18N
            if (timeFilter != null) {
                select.setLong(1, timeToBucketId(timeFilter.getInterval().getStart()));//.getStartMilliSeconds()));
                select.setLong(2, timeToBucketId(timeFilter.getInterval().getEnd()));//.getEndMilliSeconds()));
            }
            select.setMaxRows(limit);

            ResultSet rs = select.executeQuery();
            try {
                while (rs.next()) {
                    Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                    metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                    metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                    String name = rs.getString(2);
                    funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), name, rs.getString(3)), metrics));
                }
            } finally {
                rs.close();
            }

            demangle(funcList);

            return funcList;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }

    public List<FunctionCallWithMetric> getFunctionsList(DataTableMetadata metadata,
            List<Column> metricsColumn, FunctionDatatableDescription functionDescription, List<DataFilter> filters) {
        try {
            Collection<FunctionMetric> metrics = new ArrayList<FunctionMetric>();
            for (Column metricColumn : metricsColumn) {
                FunctionMetric metric = FunctionMetricsFactory.getInstance().getFunctionMetric(new FunctionMetric.FunctionMetricConfiguration(metricColumn.getColumnName(), metricColumn.getColumnUName(), metricColumn.getColumnClass()));
                metrics.add(metric);
            }
            String functionColumnName = functionDescription.getNameColumn();
            String offesetColumnName = functionDescription.getOffsetColumn();
            String functionUniqueID = functionDescription.getUniqueColumnName();
            List<FunctionCallWithMetric> funcList = new ArrayList<FunctionCallWithMetric>();
            Collection<TimeIntervalDataFilter> timeFilters = getDataFilters(filters, TimeIntervalDataFilter.class);
            //create list of DataTableMetadataFilter
            //if we hvae Time column create filter
            Collection<DataTableMetadataFilter> tableFilters = new ArrayList<DataTableMetadataFilter>();
            DataTableMetadataFilterSupport filtersSupport = DataTableMetadataFilterSupport.getInstance();
            for (TimeIntervalDataFilter timeFilter : timeFilters) {
                tableFilters.addAll(filtersSupport.createFilters(metadata, timeFilter));
            }

            ResultSet rs = sqlStorage.select(metadata, tableFilters);
            try {
                while (rs.next()) {
                    Map<FunctionMetric, Object> metricValues = new HashMap<FunctionMetric, Object>();
                    for (FunctionMetric m : metrics) {
                        try {
                            rs.findColumn(m.getMetricID());
                            Object value = rs.getObject(m.getMetricID());
                            if (m.getMetricValueClass() == Time.class && value != null) {
                                value = new Time(Long.valueOf(value + ""));
                            }
                            metricValues.put(m, value);
                        } catch (SQLException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                    String funcName = rs.getString(functionColumnName);
                    funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(functionUniqueID), funcName, funcName), offesetColumnName != null ? rs.getLong(offesetColumnName) : -1, metricValues));
                }
            } finally {
                rs.close();
            }

            demangle(funcList);

            return funcList;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

////////////////////////////////////////////////////////////////////////////////
    private void updateMetrics(long id, boolean funcOrNode, long timestamp, long duration, boolean addIncl, boolean addExcl) {
        if (0 < duration) {
            UpdateMetrics cmd = new UpdateMetrics();
            cmd.objId = id;
            cmd.bucketId = timeToBucketId(timestamp);
            cmd.funcOrNode = funcOrNode;
            if (addIncl) {
                cmd.cpuTimeInclusive = duration;
            }
            if (addExcl) {
                cmd.cpuTimeExclusive = duration;
            }
            executor.submitCommand(cmd);
        }
    }

    private long generateNodeId(long callerId, long funcId, long offset) {
        synchronized (nodeCache) {
            NodeCacheKey cacheKey = new NodeCacheKey(callerId, funcId, offset);
            Long nodeId = nodeCache.get(cacheKey);
            if (nodeId == null) {
                nodeId = ++nodeIdSequence;
                AddNode cmd = new AddNode();
                cmd.id = nodeId;
                cmd.callerId = callerId;
                cmd.funcId = funcId;
                cmd.offset = offset;
                executor.submitCommand(cmd);
                nodeCache.put(cacheKey, nodeId);
            }
            return nodeId;
        }
    }

    private long generateFuncId(final CharSequence fname) {
        // Need an immutable copy of fname. Otherwise will use
        // wrong key in funcCache (mutuable fname)
        String funcName = fname.toString();
        
        int plusPos = lastIndexOf(funcName, '+'); // NOI18N
        if (0 <= plusPos) {
            funcName = funcName.substring(0, plusPos);
        }
        synchronized (funcCache) {
            Long funcId = funcCache.get(funcName);
            if (funcId == null) {
                funcId = ++funcIdSequence;
                AddFunction cmd = new AddFunction();
                cmd.id = funcId;
                cmd.name = funcName;
                executor.submitCommand(cmd);
                funcCache.put(funcName, funcId);
            }
            return funcId;
        }
    }

    private long getOffset(CharSequence cs) {
        int plusPos = lastIndexOf(cs, '+'); // NOI18N
        if (0 <= plusPos) {
            try {
                return Long.parseLong(cs.subSequence(plusPos + 3, cs.length()).toString(), 16);
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        return 0l;
    }

    private int lastIndexOf(CharSequence cs, char c) {
        for (int i = cs.length() - 1; 0 <= i; --i) {
            if (cs.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    private String prepareCallersSelect(List<FunctionCallWithMetric> path) throws SQLException {
        StringBuilder buf = new StringBuilder();
        int size = path.size();

        buf.append(" SELECT F.func_id, F.func_name, F.func_full_name, SUM(N.time_incl), SUM(N.time_excl) FROM Node AS N "); //NOI18N
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id "); //NOI18N
        buf.append(" INNER JOIN Node N1 ON N.node_id = N1.caller_id "); //NOI18N

        for (int i = 1; i < size; ++i) {
            buf.append(" INNER JOIN Node AS N").append(i + 1); //NOI18N
            buf.append(" ON N").append(i).append(".node_id = N").append(i + 1).append(".caller_id "); //NOI18N
        }
        buf.append(" WHERE "); //NOI18N
        for (int i = 0; i < size; ++i) {
            if (0 < i) {
                buf.append("AND "); //NOI18N
            }
            buf.append("N").append(i + 1).append(".func_id = "); //NOI18N
            buf.append(((FunctionImpl) path.get(i).getFunction()).getId());
        }
        buf.append(" GROUP BY F.func_id, F.func_name, F.func_full_name"); //NOI18N
        return buf.toString();
    }

    private String prepareCalleesSelect(List<FunctionCallWithMetric> path) throws SQLException {
        StringBuilder buf = new StringBuilder();
        int size = path.size();

        buf.append("SELECT F.func_id, F.func_name, F.func_full_name, SUM(N.time_incl), SUM(N.time_excl) FROM Node AS N1 "); //NOI18N
        for (int i = 1; i < size; ++i) {
            buf.append(" INNER JOIN Node AS N").append(i + 1); //NOI18N
            buf.append(" ON N").append(i).append(".node_id = N").append(i + 1).append(".caller_id "); //NOI18N
        }
        buf.append(" INNER JOIN Node N ON N").append(size).append(".node_id = N.caller_id "); //NOI18N
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id WHERE "); //NOI18N
        for (int i = 0; i < size; ++i) {
            if (0 < i) {
                buf.append(" AND "); //NOI18N
            }
            buf.append(" N").append(i + 1).append(".func_id = "); //NOI18N
            buf.append(((FunctionImpl) path.get(i).getFunction()).getId());
        }
        buf.append(" GROUP BY F.func_id, F.func_name, F.func_full_name"); //NOI18N
        return buf.toString();
    }

    private ThreadSnapshot fetchSnapshot(int threadId, long timestamp, boolean fullMsa) throws SQLException {
        PreparedStatement s = getPreparedStatement("SELECT leaf_id, mstate FROM CallStack WHERE thread_id = ? AND time_stamp = ?"); // NOI18N
        s.setInt(1, threadId);
        s.setLong(2, timestamp);
        ResultSet rs = s.executeQuery();
        try {
            if (rs.next()) {
                return new SnapshotImpl(this, timestamp, threadId, rs.getInt(1), MSAState.fromCode(rs.getInt(2), fullMsa));
            } else {
                return null;
            }
        } finally {
            rs.close();
        }

        // do not close s here, it is cached
    }

    public List<ThreadSnapshot> getThreadSnapshots(ThreadSnapshotQuery query) {
        List<String> conditions = new ArrayList<String>(3);

        ThreadFilter threadFilter = Util.firstInstanceOf(ThreadFilter.class, query.getFilters());
        if (threadFilter != null) {
            StringBuilder where = new StringBuilder("thread_id IN ("); // NOI18N
            boolean first = true;
            for (int threadId : threadFilter.getThreadIds()) {
                if (first) {
                    first = false;
                } else {
                    where.append(','); // NOI18N
                }
                where.append(threadId);
            }
            where.append(')'); // NOI18N
            conditions.add(where.toString());
        }

        TimeFilter timeFilter = Util.firstInstanceOf(TimeFilter.class, query.getFilters());
        if (timeFilter != null) {
            if (0 <= timeFilter.getStartTime()) {
                conditions.add(timeFilter.getStartTime() + " <= time_stamp"); // NOI18N
            }
            if (0 <= timeFilter.getEndTime()) {
                conditions.add("time_stamp <= " + timeFilter.getEndTime()); // NOI18N
            }
        }

        StringBuilder select = new StringBuilder("SELECT thread_id, "); // NOI18N
        if (timeFilter != null) {
            switch (timeFilter.getMode()) {
                case FIRST:
                    select.append("MIN(time_stamp) "); // NOI18N
                    break;
                case LAST:
                    select.append("MAX(time_stamp) "); // NOI18N
                    break;
                default:
                    select.append("time_stamp "); // NOI18N
            }
        } else {
            select.append("time_stamp "); // NOI18N
        }

        select.append("FROM CallStack "); // NOI18N

        if (!conditions.isEmpty()) {
            select.append("WHERE "); // NOI18N
            boolean first = true;
            for (String condition : conditions) {
                if (first) {
                    first = false;
                } else {
                    select.append("AND "); // NOI18N
                }
                select.append(condition).append(' '); // NOI18N
            }
        }

        if (timeFilter != null && timeFilter.getMode() != TimeFilter.Mode.ALL) {
            select.append("GROUP BY thread_id"); // NOI18N
        }

        try {
            List<ThreadSnapshot> snapshots = new ArrayList<ThreadSnapshot>();
            ResultSet rs = sqlStorage.select(null, null, select.toString());
            try {
                if (rs != null) {
                    while (rs.next()) {
                        ThreadSnapshot snapshot = fetchSnapshot(rs.getInt(1), rs.getLong(2), query.isFullMSA());
                        if (snapshot != null) {
                            snapshots.add(snapshot);
                        }
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return snapshots;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    public ThreadDump getThreadDump(ThreadDumpQuery query) {
        ThreadDump res = _getThreadDump(query);
        if (res == null) {
            return null;
        }
        ThreadDumpImpl result = new ThreadDumpImpl(res.getTimestamp());
        for (Integer i : query.getShowThreads()) {
            for (ThreadSnapshot dump : res.getThreadStates()) {
                if (dump.getThreadInfo().getThreadId() == i) {
                    result.addStack(dump);
                    break;
                }
            }
        }
        return result;
    }

    private MSAState getTrueState(ThreadSnapshot dump, ThreadDumpQuery query) {
        MSAState state = dump.getState();
//        List<FunctionCall> stack = dump.getStack();
//        String where = ""; // NOI18N
//        if (stack.size() > 0) {
//            where = stack.get(stack.size()-1).getDisplayedName();
//        }
//        if (where.indexOf("__lwp_park+")>=0) {
//            if (query.isFullMode()) {
//                state = MSAState.SleepingUserLock;
//            } else {
//                state = MSAState.Blocked;
//            }
//        } else if (where.indexOf("__nanosleep+")>=0) {
//            if (query.isFullMode()) {
//                state = MSAState.Sleeping;
//            } else {
//                state = MSAState.SleepingOther;
//            }
//        }
        return state;
    }

    private ThreadDump _getThreadDump(ThreadDumpQuery query) {
        long start;
        long middle;
        long end;
        if (true) {
            start = query.getThreadState().getTimeStamp();
            middle = query.getThreadState().getTimeStamp() + query.getThreadState().getMSASamplePeriod() / 2;
            end = query.getThreadState().getTimeStamp() + query.getThreadState().getMSASamplePeriod();
        } else {
            start = query.getThreadState().getTimeStamp() * 1000 * 1000;
            middle = query.getThreadState().getTimeStamp() * 1000 * 1000 + query.getThreadState().getMSASamplePeriod() / 2; ///1000/1000;
            end = query.getThreadState().getTimeStamp() * 1000 * 1000 + query.getThreadState().getMSASamplePeriod(); ///1000/1000;
        }
        // 1. try to find needed thread in needed state in sampling interval
        TimeFilter time = new TimeFilter(start, end, TimeFilter.Mode.ALL);
        ThreadFilter threads = new ThreadFilter(query.getShowThreads());
        List<ThreadSnapshot> res = getThreadSnapshots(new ThreadSnapshotQuery(query.isFullMode(), time, threads));
        ThreadSnapshot found = null;
        long foundTimeSamp = -1;
        for (ThreadSnapshot dump : res) {
            if (query.getThreadID() == dump.getThreadInfo().getThreadId() &&
                    query.getPreferredState() == getTrueState(dump, query)) {
                found = dump;
                foundTimeSamp = found.getTimestamp();
                break;
            }
        }
        if (found == null) {
            // 2. try to find needed thread in needed state before middle of the sampling interval
            time = new TimeFilter(0, middle, TimeFilter.Mode.LAST);
            threads = new ThreadFilter(Collections.<Integer>singletonList(Integer.valueOf((int) query.getThreadID())));
            List<ThreadSnapshot> res2 = getThreadSnapshots(new ThreadSnapshotQuery(query.isFullMode(), time, threads));
            long foundAny = -1;
            for (ThreadSnapshot dump : res2) {
                foundAny = dump.getTimestamp();
                if (query.getThreadID() == dump.getThreadInfo().getThreadId() &&
                        query.getPreferredState() == getTrueState(dump, query)) {
                    found = dump;
                    foundTimeSamp = middle;
                    break;
                }
            }
            // 3. try to find needed thread in needed state in case equals time stamps
            if (found == null && foundAny > 0) {
                time = new TimeFilter(foundAny - 1000 * 1000, foundAny + 1, TimeFilter.Mode.ALL);
                threads = new ThreadFilter(Collections.<Integer>singletonList(Integer.valueOf((int) query.getThreadID())));
                res2 = getThreadSnapshots(new ThreadSnapshotQuery(query.isFullMode(), time, threads));
                for (ThreadSnapshot dump : res2) {
                    foundAny = dump.getTimestamp();
                    if (query.getThreadID() == dump.getThreadInfo().getThreadId() &&
                            query.getPreferredState() == getTrueState(dump, query)) {
                        found = dump;
                        foundTimeSamp = middle;
                        break;
                    }
                }
                if (found == null) {
                    // 4. take any dump of the thread
                    for (ThreadSnapshot dump : res2) {
                        foundAny = dump.getTimestamp();
                        if (query.getThreadID() == dump.getThreadInfo().getThreadId()) {
                            found = dump;
                            foundTimeSamp = middle;
                            break;
                        }
                    }
                }
            }
        }
        if (found != null) {
            // 5. find closed other thread in the sampling interval
            HashMap<Integer, ThreadSnapshot> map = new HashMap<Integer, ThreadSnapshot>();
            map.put(found.getThreadInfo().getThreadId(), found);
            for (ThreadSnapshot dump : res) {
                if (dump.getThreadInfo().getThreadId() != found.getThreadInfo().getThreadId()) {
                    int id = dump.getThreadInfo().getThreadId();
                    ThreadSnapshot prev = map.get(id);
                    if (prev == null) {
                        map.put(id, dump);
                    } else {
                        if (Math.abs(prev.getTimestamp() - foundTimeSamp) > Math.abs(dump.getTimestamp() - foundTimeSamp)) {
                            map.put(id, dump);
                        }
                    }
                }
            }
            Set<Integer> toAdd = new HashSet<Integer>(query.getShowThreads());
            ThreadDumpImpl result = new ThreadDumpImpl(foundTimeSamp);
            for (ThreadSnapshot dump : map.values()) {
                toAdd.remove(dump.getThreadInfo().getThreadId());
                result.addStack(dump);
            }
            // 6. complete other thread by last call stacks
            if (!toAdd.isEmpty()) {
                time = new TimeFilter(0, foundTimeSamp, TimeFilter.Mode.LAST);
                threads = new ThreadFilter(toAdd);
                res = getThreadSnapshots(new ThreadSnapshotQuery(query.isFullMode(), time, threads));
                for (ThreadSnapshot dump : res) {
                    result.addStack(dump);
                }
            }
            return result;
        }
        return null;
    }

    public synchronized List<FunctionCall> getCallStack(final long stackId) {
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        try {
            long nodeID = stackId;
            while (0 < nodeID) {
                PreparedStatement ps = getPreparedStatement(
                        "SELECT Node.node_id, Node.caller_id, Node.func_id, Node.offset, Func.func_name " + // NOI18N
                        "FROM Node LEFT JOIN Func ON Node.func_id = Func.func_id " + // NOI18N
                        "WHERE node_id = ?"); // NOI18N
                ps.setLong(1, nodeID);

                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        String funcName = rs.getString(5);
                        FunctionImpl func = new FunctionImpl(rs.getInt(3), funcName, funcName);
                        result.add(new FunctionCallImpl(func, rs.getLong(4), new HashMap<FunctionMetric, Object>()));
                        nodeID = rs.getInt(2);
                    } else {
                        break;
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        demangle(result);
        Collections.reverse(result);
        return result;
    }

    private void demangle(List<? extends FunctionCall> calls) {
        if (demangler != null) {
            List<String> mangled = new ArrayList<String>(calls.size());
            for (FunctionCall call : calls) {
                mangled.add(call.getFunction().getName());
            }
            List<String> demangled = demangler.demangle(mangled);
            for (int i = 0; i < calls.size(); ++i) {
                ((FunctionImpl) calls.get(i).getFunction()).setName(demangled.get(i));
            }
        }
    }

    /**
     *
     * @param timestamp  in nanoseconds
     * @return bucket id
     */
    private static long timeToBucketId(long timestamp) {
        return timestamp / 1000 / 1000 / 1000;  // bucket is 1 second
    }
    /**
     * Maximal string length that underlying database can store.
     * Must match the numbers in schema.sql.
     */
    private static final int MAX_STRING_LENGTH = 16384;

    private static String truncateString(String str) {
        if (str.length() <= MAX_STRING_LENGTH) {
            return str;
        } else {
            return str.substring(0, MAX_STRING_LENGTH - 3) + "..."; // NOI18N
        }
    }

////////////////////////////////////////////////////////////////////////////////
    private static class NodeCacheKey {

        private final long callerId;
        private final long funcId;
        private final long offset;

        public NodeCacheKey(long callerId, long funcId, long offset) {
            this.callerId = callerId;
            this.funcId = funcId;
            this.offset = offset;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NodeCacheKey)) {
                return false;
            }
            final NodeCacheKey that = (NodeCacheKey) obj;
            return callerId == that.callerId && funcId == that.funcId && offset == that.offset;
        }

        @Override
        public int hashCode() {
            return 13 * ((int) (callerId >> 32) | (int) callerId) + 17 * ((int) (funcId >> 32) | (int) funcId) + ((int) (offset >> 32) | (int) offset);
        }
    }

    protected static class FunctionImpl implements Function {

        private final long id;
        private String name;
        private final String quilifiedName;

        public FunctionImpl(long id, String name, String qualifiedName) {
            this.id = id;
            this.name = name;
            this.quilifiedName = qualifiedName;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        public String getSignature() {
            return quilifiedName;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getQuilifiedName() {
            return FunctionNameUtils.getFunctionQName(name);
        }
    }

    protected static class FunctionCallImpl extends FunctionCallWithMetric {

        private final Map<FunctionMetric, Object> metrics;

        FunctionCallImpl(Function function, long offset, Map<FunctionMetric, Object> metrics) {
            super(function, offset);
            this.metrics = metrics;
        }

        FunctionCallImpl(Function function, Map<FunctionMetric, Object> metrics) {
            this(function, 0, metrics);
        }

        @Override
        public String getDisplayedName() {
            return getFunction().getName() + (hasOffset() ? ("+0x" + Long.toHexString(getOffset())) : ""); //NOI18N
        }

        @Override
        public Object getMetricValue(FunctionMetric metric) {
            return metrics.get(metric);
        }

        @Override
        public Object getMetricValue(String metric_id) {
            for (FunctionMetric metric : metrics.keySet()) {
                if (metric.getMetricID().equals(metric_id)) {
                    return metrics.get(metric);
                }
            }
            return null;
        }

        @Override
        public boolean hasMetric(String metric_id) {
            for (FunctionMetric metric : metrics.keySet()) {
                if (metric.getMetricID().equals(metric_id)) {
                    return true;
                }
            }
            return false;

        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("FunctionCall{ function=").append(getFunction()); //NOI18N
            buf.append(", metrics=").append(metrics).append(" }"); //NOI18N
            return buf.toString();
        }
    }

    private static class AddFunction {

        public long id;
        public CharSequence name;
//        public CharSequence full_name;
    }

    private static class AddNode {

        public long id;
        public long callerId;
        public long funcId;
        public long offset;
    }

    private static class UpdateMetrics {

        public boolean funcOrNode; // false => func, true => node
        public long objId;
        public long bucketId;
        public long cpuTimeInclusive;
        public long cpuTimeExclusive;

        public void add(UpdateMetrics delta) {
            cpuTimeInclusive += delta.cpuTimeInclusive;
            cpuTimeExclusive += delta.cpuTimeExclusive;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(funcOrNode ? "func" : "node"); // NOI18N
            buf.append(" id=").append(objId); // NOI18N
            buf.append(": time_incl+=").append(cpuTimeInclusive); // NOI18N
            buf.append(", time_excl+=").append(cpuTimeExclusive); // NOI18N
            return buf.toString();
        }
    }

    private class ExecutorThread extends Thread {

        private static final int MAX_COMMANDS = 5000;
        private static final long SLEEP_INTERVAL = 200L;
        private final LinkedBlockingQueue<Object> queue;

        public ExecutorThread() {
            setName("DLIGTH: SQLStackStorage executor thread"); // NOI18N
            queue = new LinkedBlockingQueue<Object>();
        }

        public void submitCommand(Object cmd) {
            queue.offer(cmd);
        }

        public synchronized void flush() throws InterruptedException {
            // Proper synchronization is needed to guarantee that
            // queue.isEmpty() is checked either before commands are
            // taken from the queue, or after they are executed.
            while (!queue.isEmpty()) {
                wait();
            }
        }

        @Override
        public void run() {
            try {
                Map<Long, UpdateMetrics> funcMetrics = new HashMap<Long, UpdateMetrics>();
                Map<Long, UpdateMetrics> nodeMetrics = new HashMap<Long, UpdateMetrics>();
                List<Object> cmds = new LinkedList<Object>();
                while (isRunning) {
                    // Taking commands from queue and executing them should be one atomic action.
                    synchronized (this) {
                        queue.drainTo(cmds, MAX_COMMANDS);

                        // first pass: collect metrics
                        Iterator<Object> cmdIterator = cmds.iterator();
                        while (cmdIterator.hasNext()) {
                            if (!isRunning) {
                                return;
                            }
                            Object cmd = cmdIterator.next();
                            if (cmd instanceof UpdateMetrics) {
                                UpdateMetrics updateMetricsCmd = (UpdateMetrics) cmd;
                                Map<Long, UpdateMetrics> map = updateMetricsCmd.funcOrNode ? nodeMetrics : funcMetrics;
                                UpdateMetrics original = map.get(updateMetricsCmd.objId);
                                if (original == null) {
                                    map.put(updateMetricsCmd.objId, updateMetricsCmd);
                                } else {
                                    original.add(updateMetricsCmd);
                                }
                                cmdIterator.remove();
                            }
                        }

                        // second pass: insert new functions/nodes
                        cmdIterator = cmds.iterator();
                        while (cmdIterator.hasNext()) {
                            if (!isRunning) {
                                return;
                            }
                            Object cmd = cmdIterator.next();
                            try {
                                if (cmd instanceof AddFunction) {
                                    AddFunction addFunctionCmd = (AddFunction) cmd;
                                    //demagle here
                                    PreparedStatement stmt = getPreparedStatement("INSERT INTO Func (func_id, func_full_name, func_name) VALUES (?, ?, ?)"); //NOI18N
                                    stmt.setLong(1, addFunctionCmd.id);
                                    stmt.setString(2, truncateString(addFunctionCmd.name.toString()));
                                    stmt.setString(3, truncateString(addFunctionCmd.name.toString()));
                                    stmt.executeUpdate();
                                } else if (cmd instanceof AddNode) {
                                    AddNode addNodeCmd = (AddNode) cmd;
                                    PreparedStatement stmt = getPreparedStatement("INSERT INTO Node (node_id, caller_id, func_id, offset, time_incl, time_excl) VALUES (?, ?, ?, ?, ?, ?)"); //NOI18N
                                    stmt.setLong(1, addNodeCmd.id);
                                    stmt.setLong(2, addNodeCmd.callerId);
                                    stmt.setLong(3, addNodeCmd.funcId);
                                    stmt.setLong(4, addNodeCmd.offset);
                                    UpdateMetrics metrics = nodeMetrics.remove(addNodeCmd.id);
                                    if (metrics == null) {
                                        stmt.setLong(5, 0);
                                        stmt.setLong(6, 0);
                                    } else {
                                        stmt.setLong(5, metrics.cpuTimeInclusive);
                                        stmt.setLong(6, metrics.cpuTimeExclusive);
                                    }
                                    stmt.executeUpdate();
                                }
                            } catch (SQLException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        cmds.clear();

                        // third pass: record metrics
                        for (UpdateMetrics cmd : funcMetrics.values()) {
                            try {
                                PreparedStatement stmt = getPreparedStatement("SELECT func_id, bucket_id, time_incl, time_excl FROM FuncMetricAggr WHERE func_id = ? AND bucket_id = ? FOR UPDATE"); //NOI18N
                                stmt.setLong(1, cmd.objId);
                                stmt.setLong(2, cmd.bucketId);
                                ResultSet rs = stmt.executeQuery();
                                try {
                                    if (rs.next()) {
                                        rs.updateLong(3, rs.getLong(3) + cmd.cpuTimeInclusive);
                                        rs.updateLong(4, rs.getLong(4) + cmd.cpuTimeExclusive);
                                        rs.updateRow();
                                    } else {
                                        rs.moveToInsertRow();
                                        rs.updateLong(1, cmd.objId);
                                        rs.updateLong(2, cmd.bucketId);
                                        rs.updateLong(3, cmd.cpuTimeInclusive);
                                        rs.updateLong(4, cmd.cpuTimeExclusive);
                                        rs.insertRow();
                                    }
                                } finally {
                                    rs.close();
                                }
                            } catch (SQLException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        funcMetrics.clear();

                        for (UpdateMetrics cmd : nodeMetrics.values()) {
                            try {
                                PreparedStatement stmt = getPreparedStatement("UPDATE Node SET time_incl = time_incl + ?, time_excl = time_excl + ? WHERE node_id = ?"); //NOI18N
                                stmt.setLong(1, cmd.cpuTimeInclusive);
                                stmt.setLong(2, cmd.cpuTimeExclusive);
                                stmt.setLong(3, cmd.objId);
                                stmt.executeUpdate();
                            } catch (SQLException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                        nodeMetrics.clear();

                        notifyAll();
                    }

                    Thread.sleep(SLEEP_INTERVAL);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
