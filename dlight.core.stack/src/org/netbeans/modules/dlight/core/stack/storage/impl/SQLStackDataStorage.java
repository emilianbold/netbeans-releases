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
import java.util.concurrent.LinkedBlockingQueue;
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
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ProxyDataStorage;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.Util;
import org.openide.util.Lookup;

/**
 *
 * @author Alexey Vladykin
 */
public class SQLStackDataStorage implements ProxyDataStorage, StackDataStorage, ThreadDumpProvider {

    private SQLDataStorage sqlStorage;
    private final List<DataTableMetadata> tableMetadatas;
    private final Map<String, PreparedStatement> stmtCache;

    public SQLStackDataStorage() {
        this.tableMetadatas = new ArrayList<DataTableMetadata>();
        funcCache = new HashMap<CharSequence, Long>();
        nodeCache = new HashMap<NodeCacheKey, Long>();
        funcIdSequence = 0;
        nodeIdSequence = 0;
        executor = new ExecutorThread();
        executor.setPriority(Thread.MIN_PRIORITY);
        executor.start();
        CppSymbolDemanglerFactory factory = Lookup.getDefault().lookup(CppSymbolDemanglerFactory.class);
        if (factory != null) {
            demangler = factory.getForCurrentSession();
        } else {
            demangler = null;
        }
        this.stmtCache = new HashMap<String, PreparedStatement>();
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
    private final CppSymbolDemangler demangler;

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

    public long putStack(List<CharSequence> stack, long sampleDuration) {
        long callerId = 0;
        Set<Long> funcs = new HashSet<Long>();
        for (int i = 0; i < stack.size(); ++i) {
            boolean isLeaf = i + 1 == stack.size();
            CharSequence funcName = stack.get(i);
            long funcId = generateFuncId(funcName);
            updateMetrics(funcId, false, sampleDuration, !funcs.contains(funcId), isLeaf);
            funcs.add(funcId);
            long nodeId = generateNodeId(callerId, funcId, getOffset(funcName));
            updateMetrics(nodeId, true, sampleDuration, true, isLeaf);
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
            while (rs.next()) {
                Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                String funcName = rs.getString(2);
                result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName, rs.getString(3)), metrics));
            }
            rs.close();
            demangle(result);
            return result;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<FunctionCallWithMetric> getCallees(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        try {
            List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
            ResultSet rs = sqlStorage.select(null, null, prepareCalleesSelect(path));
            while (rs.next()) {
                Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                String funcName = rs.getString(2);
                result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName, rs.getString(3)), metrics));
            }
            rs.close();
            demangle(result);
            return result;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, int limit) {
        try {
            List<FunctionCallWithMetric> funcList = new ArrayList<FunctionCallWithMetric>();
            PreparedStatement select = getPreparedStatement(
                    "SELECT func_id, func_name, func_full_name,  time_incl, time_excl " + //NOI18N
                    "FROM Func ORDER BY " + metric.getMetricID() + " DESC"); //NOI18N
            select.setMaxRows(limit);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                String name = rs.getString(2);
                funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), name, rs.getString(3)), metrics));
            }
            rs.close();

            demangle(funcList);

            return funcList;
        } catch (SQLException ex) {
        }
        return Collections.emptyList();
    }

    public List<FunctionCallWithMetric> getFunctionsList(DataTableMetadata metadata,
            List<Column> metricsColumn, FunctionDatatableDescription functionDescription) {
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
            ResultSet rs = null;
            if (metadata.getViewStatement() != null){
                PreparedStatement select = getPreparedStatement(metadata.getViewStatement());
                rs = select.executeQuery();
            }else{
                rs = sqlStorage.select(metadata.getName(), metricsColumn);
            }
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
                    }
                }
                String funcName = rs.getString(functionColumnName);
                funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(functionUniqueID), funcName, funcName), offesetColumnName != null ? rs.getLong(offesetColumnName) : -1, metricValues));
            }
            rs.close();

            demangle(funcList);

            return funcList;
        } catch (SQLException ex) {
        }
        return Collections.emptyList();
    }

////////////////////////////////////////////////////////////////////////////////
    private void updateMetrics(long id, boolean funcOrNode, long sampleDuration, boolean addIncl, boolean addExcl) {
        if (0 < sampleDuration) {
            UpdateMetrics cmd = new UpdateMetrics();
            cmd.objId = id;
            cmd.funcOrNode = funcOrNode;
            if (addIncl) {
                cmd.cpuTimeInclusive = sampleDuration;
            }
            if (addExcl) {
                cmd.cpuTimeExclusive = sampleDuration;
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

    private long generateFuncId(CharSequence funcName) {
        int plusPos = lastIndexOf(funcName, '+'); // NOI18N
        if (0 <= plusPos) {
            funcName = funcName.subSequence(0, plusPos);
        }
        synchronized (funcCache) {
            Long funcId = funcCache.get(funcName);
            if (funcId == null) {
                funcId = ++funcIdSequence;
                AddFunction cmd = new AddFunction();
                cmd.id = funcId;
                funcName = new String(funcName.toString());
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
        try {
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
        } finally {
            //s.close(); // do not close! SQLStorage caches these prepared statements
        }
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
                while (rs.next()) {
                    ThreadSnapshot snapshot = fetchSnapshot(rs.getInt(1), rs.getLong(2), query.isFullMSA());
                    if (snapshot != null) {
                        snapshots.add(snapshot);
                    }
                }
            } finally {
                rs.close();
            }
            return snapshots;
        } catch (SQLException ex) {
            return Collections.emptyList();
        }
    }

    public ThreadDump getThreadDump(ThreadDumpQuery query) {
        ThreadDumpImpl result = null;

        try {

            PreparedStatement statement = getPreparedStatement(
                    "SELECT mstate, MAX(time_stamp) FROM CallStack WHERE thread_id = ? and time_stamp <= ? GROUP BY mstate"); // NOI18N
            statement.setLong(1, query.getThreadID());
            statement.setLong(2, query.getThreadState().getTimeStamp());

            ResultSet threadAndTimestampResult = statement.executeQuery();
            int bestState = -1;
            long bestTimestamp = -1;
            while (threadAndTimestampResult.next()) {
                int state = threadAndTimestampResult.getInt(1);
                long timestamp = threadAndTimestampResult.getLong(2);
                if ((query.getPreferredState().matches(state) && !query.getPreferredState().matches(bestState)) ||
                        (query.getPreferredState().matches(state) || !query.getPreferredState().matches(bestState)) && bestTimestamp < timestamp) {
                    bestState = state;
                    bestTimestamp = timestamp;
                }
            }
            threadAndTimestampResult.close();

            if (bestTimestamp < 0) {
                // Means that no callstack found for this thread in this state
                //System.out.println("No callstack found!!!");
                return new ThreadDumpImpl(query.getThreadState().getTimeStamp());
            }

            //System.out.println("Nearest callstack found at " + ts);

            result = new ThreadDumpImpl(bestTimestamp);

            // Next, get all times for all threads for alligned stacks (time <= ts)
            // select threadid, max(ts) from test where ts <= 6 group by threadid;

            statement = getPreparedStatement(
                    "SELECT thread_id, MAX(time_stamp) FROM CallStack WHERE " + // NOI18N
                    "time_stamp <= ? GROUP BY thread_id"); // NOI18N
            statement.setLong(1, bestTimestamp);
            threadAndTimestampResult = statement.executeQuery();

            HashMap<Integer, Long> idToTime = new HashMap<Integer, Long>();

            while (threadAndTimestampResult.next()) {
                int callStackThreadId = threadAndTimestampResult.getInt(1);
                long callStackTimeStamp = threadAndTimestampResult.getLong(2);
                if (query.getShowThreads().contains(callStackThreadId)) {
                    idToTime.put(callStackThreadId, callStackTimeStamp);
                }
            }
            threadAndTimestampResult.close();

            //get leaf_id's
            for (Map.Entry<Integer, Long> entry : idToTime.entrySet()){
                int thread_id = entry.getKey();
                long t = entry.getValue();
                statement = getPreparedStatement("SELECT leaf_id, mstate FROM CallStack WHERE thread_id = ? AND time_stamp = ?"); // NOI18N
                statement.setInt(1, thread_id);
                statement.setLong(2, t);
                ResultSet stackAndStateResult = statement.executeQuery();
                if (stackAndStateResult.next()) {
                    int stackID = stackAndStateResult.getInt(1);
                    int mstate = stackAndStateResult.getInt(2);
                    result.addStack(new SnapshotImpl(this, t, thread_id, stackID, MSAState.fromCode(mstate, query.isFullMode())));
                }
                stackAndStateResult.close();
            }

        } catch (SQLException ex) {
            System.err.println("ex: " + ex.getSQLState());  // NOI18N
        }

        return result;
    }

    public List<FunctionCall> getCallStack(final long stackId) {
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
                    }
                } finally {
                    rs.close();
                }
            }
        } catch (SQLException ex) {
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
            return 13 * ((int) (callerId >> 32) | (int) callerId)
                    + 17 * ((int) (funcId >> 32) | (int) funcId)
                    + ((int) (offset >> 32) | (int) offset);
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

        public String getQuilifiedName() {
            return quilifiedName;
        }

        @Override
        public String toString() {
            return name;
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

        public long objId;
        public boolean funcOrNode; // false => func, true => node
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

                        // second pass: execute inserts
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
                                    PreparedStatement stmt = getPreparedStatement("INSERT INTO Func (func_id, func_full_name, func_name, time_incl, time_excl) VALUES (?, ?, ?, ?, ?)"); //NOI18N
                                    stmt.setLong(1, addFunctionCmd.id);
                                    stmt.setString(2, addFunctionCmd.name.toString());
//                                    if (demanglingService == null) {
                                    stmt.setString(3, addFunctionCmd.name.toString());
//                                    } else {
//                                        Future<String> demangled = demanglingService.demangle(addFunctionCmd.name.toString());
//                                        try {
//                                            stmt.setString(3, demangled.get());
//                                        } catch (ExecutionException ex) {
//                                            stmt.setString(3, addFunctionCmd.name.toString());
//                                        }
//                                    }
                                    UpdateMetrics metrics = funcMetrics.remove(addFunctionCmd.id);
                                    if (metrics == null) {
                                        stmt.setLong(4, 0);
                                        stmt.setLong(5, 0);
                                    } else {
                                        stmt.setLong(4, metrics.cpuTimeInclusive);
                                        stmt.setLong(5, metrics.cpuTimeExclusive);
                                    }
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
                                ex.printStackTrace();
                            }
                        }
                        cmds.clear();

                        // third pass: execute updates
                        for (UpdateMetrics cmd : funcMetrics.values()) {
                            try {
                                PreparedStatement stmt = getPreparedStatement("UPDATE Func SET time_incl = time_incl + ?, time_excl = time_excl + ? WHERE func_id = ?"); //NOI18N
                                stmt.setLong(1, cmd.cpuTimeInclusive);
                                stmt.setLong(2, cmd.cpuTimeExclusive);
                                stmt.setLong(3, cmd.objId);
                                stmt.executeUpdate();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
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
                                ex.printStackTrace();
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
