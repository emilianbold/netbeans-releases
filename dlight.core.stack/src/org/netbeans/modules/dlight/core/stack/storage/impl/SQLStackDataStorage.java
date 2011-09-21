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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
import org.netbeans.modules.dlight.spi.support.SQLDataStorage;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.api.storage.DataTableMetadataFilter;
import org.netbeans.modules.dlight.api.storage.DataTableMetadataFilterSupport;
import org.netbeans.modules.dlight.core.stack.api.CallStackEntry;
import org.netbeans.modules.dlight.core.stack.api.CallStackEntryParser;
import org.netbeans.modules.dlight.core.stack.api.impl.DefaultStackParserImpl;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage2;
import org.netbeans.modules.dlight.core.stack.storage.impl.DBProxy.StackNode;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ProxyDataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.spi.support.SQLExceptions;
import org.netbeans.modules.dlight.spi.support.SQLRequestsProcessor;
import org.netbeans.modules.dlight.spi.support.SQLStatementsCache;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.util.Util;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NotImplementedException;

/**
 *
 * @author Alexey Vladykin
 */
public class SQLStackDataStorage implements ProxyDataStorage, StackDataStorage2, ThreadDumpProvider {

    private static final Logger LOG = DLightLogger.getLogger(SQLStackDataStorage.class);
    private static final CallStackEntryParser defaultParser = new DefaultStackParserImpl();
    private static final HashMap<Class<?>, String> classToType = new HashMap<Class<?>, String>();
    
    private final HashMap<String, DataTableMetadata> tableMetadatas;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private DBProxy dbProxy;
    private SQLRequestsProcessor requestsProcessor;
    private SQLStackRequestsProvider requestsProvider;
    private SQLStatementsCache stmtCache;
    private SQLDataStorage sqlStorage;
    private CppSymbolDemangler demangler;
    private ServiceInfoDataStorage serviceInfoDataStorage;

    static {
        classToType.put(Byte.class, "tinyint");     // NOI18N
        classToType.put(Short.class, "smallint");   // NOI18N
        classToType.put(Integer.class, "int");      // NOI18N
        classToType.put(Long.class, "bigint");      // NOI18N
        classToType.put(Double.class, "double");    // NOI18N
        classToType.put(Float.class, "real");       // NOI18N
        classToType.put(String.class, "varchar");   // NOI18N
        classToType.put(Time.class, "bigint");      // NOI18N
    }
    
    public SQLStackDataStorage() {
        tableMetadatas = new HashMap<String, DataTableMetadata>();
    }

    @Override
    public void syncAddData(String tableName, List<DataRow> data) {
        addData(tableName, data);
    }

    @Override
    public final void attachTo(ServiceInfoDataStorage serviceInfoStorage) {
        this.serviceInfoDataStorage = serviceInfoStorage;
        CppSymbolDemanglerFactory factory = Lookup.getDefault().lookup(CppSymbolDemanglerFactory.class);
        demangler = (factory == null) ? null : factory.getForCurrentSession(serviceInfoStorage.getInfo());
        //exec Env?
    }

    @Override
    public DataStorageType getBackendDataStorageType() {
        return DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE);
    }

    @Override
    public List<DataTableMetadata> getBackendTablesMetadata() {
        return Collections.emptyList();
    }

    @Override
    public synchronized void attachTo(DataStorage storage) {
        if (sqlStorage != null) {
            throw new IllegalStateException("Already attached"); // NOI18N
        }

        sqlStorage = (SQLDataStorage) storage;
        stmtCache = SQLStatementsCache.getFor(sqlStorage);
        requestsProvider = new SQLStackRequestsProvider(stmtCache);
        requestsProcessor = sqlStorage.getRequestsProcessor();
        dbProxy = new DBProxy(requestsProcessor, requestsProvider);

        initTables();
        loadSchema();
    }

    private <T extends DataFilter> Collection<T> getDataFilters(List<DataFilter> filters, Class<T> clazz) {
        Collection<T> result = new ArrayList<T>();
        for (DataFilter f : filters) {
            if (f.getClass() == clazz) {
                result.add(clazz.cast(f));
            }
        }
        return result;
    }

    @Override
    public boolean hasData(DataTableMetadata data) {
        return data.isProvidedBy(new ArrayList<DataTableMetadata>(tableMetadatas.values()));
    }

    @Override
    public void addData(String tableName, List<DataRow> data) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return Collections.singletonList(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
    }

    @Override
    public boolean supportsType(DataStorageType storageType) {
        return getStorageTypes().contains(storageType);
    }

    @Override
    public void createTables(List<DataTableMetadata> tableMetadatas) {
        for (DataTableMetadata dataTableMetadata : tableMetadatas) {
            this.tableMetadatas.put(dataTableMetadata.getName(), dataTableMetadata);
        }
    }

    // For tests ... 
    public boolean shutdown(boolean shutdownSqlStorage) {
        boolean result = shutdown();

        if (shutdownSqlStorage) {
            result = sqlStorage.shutdown() && result;
        }

        return result;
    }

    @Override
    public boolean shutdown() {
        if (closed.compareAndSet(false, true)) {
            dbProxy.shutdown();
            requestsProvider = null;

            try {
                stmtCache.close();
            } catch (SQLException ex) {
                SQLExceptions.printStackTrace(sqlStorage, ex);
            }

            stmtCache = null;
        }

        return true;
    }

    private void initTables() {
        InputStream is = SQLStackDataStorage.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/core/stack/resources/schema.sql"); // NOI18N
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        try {
            Pattern autoIncrementPattern = Pattern.compile("\\{AUTO_INCREMENT\\}"); // NOI18N
            String line;
            StringBuilder buf = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-- ")) { // NOI18N
                    continue;
                }
                line = autoIncrementPattern.matcher(line).replaceAll(sqlStorage.getAutoIncrementExpresion());
                buf.append(line);
                if (line.endsWith(";")) { // NOI18N
                    String sql = buf.toString();
                    buf.setLength(0);
                    String sqlToExecute = sql.substring(0, sql.length() - 1);
                    try {
                        sqlStorage.executeUpdate(sqlToExecute);
                    } catch (SQLException e) {
                        if (LOG.isLoggable(Level.WARNING)) {
                            LOG.log(Level.WARNING, "Exception while tables initialization: {0}", e.getMessage()); // NOI18N
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (LOG.isLoggable(Level.WARNING)) {
                LOG.log(Level.WARNING, "Exception while tables initialization: {0}", e.getMessage()); // NOI18N
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    private void loadSchema() {
        try {
            ResultSet rs = sqlStorage.select("INFORMATION_SCHEMA.TABLES", null, // NOI18N
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE LIKE 'TABLE'"); // NOI18N
            
            if (rs == null) {
                return;
            }
            
            while (rs.next()) {
                String tableName = rs.getString(1);
                loadTable(tableName);
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected Class<?> typeToClass(String type) {
        Set<Class<?>> clazzes = classToType.keySet();
        for (Class<?> clazz : clazzes) {
            if (classToType.get(clazz).equalsIgnoreCase(type)) {
                return clazz;
            }
        }
        return String.class;
    }
    
    private void loadTable(String tableName) {
        try {
            ResultSet rs = sqlStorage.select("INFORMATION_SCHEMA.COLUMNS", null, "SELECT COLUMN_NAME, "// NOI18N
                    + "TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME LIKE '" + tableName + "'");// NOI18N
            List<Column> columns = new ArrayList<Column>();
            while (rs.next()) {
                Column c = new Column(rs.getString("COLUMN_NAME"), typeToClass(rs.getString("TYPE_NAME")));// NOI18N
                columns.add(c);
            }
            DataTableMetadata result = new DataTableMetadata(tableName, columns, null);
//            sqlStorage.loadTable(result);
            tableMetadatas.put(tableName, result);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }    

    @Override
    public long putStack(long contextID, List<CharSequence> stack) {
        // Even if this stack has no any associated metrics, we need to store the 
        // association with the context
        return putSample(contextID, stack, 0, 0);
    }

    @Override
    public long putStack(long contextID, List<CharSequence> stack, CallStackEntryParser parser) {
        // Even if this stack has no any associated metrics, we need to store the 
        // association with the context
        return putSample(contextID, stack, 0, 0);
    }

    @Override
    /**
     * stack is a list of entries in the following format:
     */
    public long putSample(long contextID, List<CharSequence> stack, long timestamp, long duration) {
        return putSample(contextID, stack, timestamp, duration, null);
    }

    @Override
    public synchronized long putSample(long contextID, List<CharSequence> stack, long timestamp, long duration, CallStackEntryParser parser) {
        if (parser == null) {
            parser = defaultParser;
        }

        long callerId = 0;
        Set<Long> funcs = new HashSet<Long>();
        StackNode node;
        String stackEntry;
        AtomicBoolean isNewNode = new AtomicBoolean();

        for (int i = stack.size() - 1; i >= 0; i--) {
            // Create a String() object from the passed CharSequence
            // this guarantees that caches will use immutable keys...
            stackEntry = stack.get(i).toString();

            CallStackEntry entry = parser.parseEntry(stackEntry);

            node = dbProxy.getNodeID(entry, callerId, isNewNode);

            if (isNewNode.get()) {
                CharSequence module = entry.getModulePath();
                if (module != null) {
                    dbProxy.addModuleInfo(node, contextID, module, entry.getOffsetInModule());
                }

                SourceFileInfo sourceFileInfo = entry.getSourceFileInfo();
                if (sourceFileInfo != null) {
                    dbProxy.addSourceInfo(node, contextID, sourceFileInfo);
                }
            }

            if (duration > 0) {
                dbProxy.updateFuncMetrics(node.funcID, contextID, timestamp, duration, !funcs.contains(node.funcID), i == 0);
            }

            // in case of recursion metrics will not be added again next time
            funcs.add(node.funcID);
            callerId = node.nodeID;
        }

        if (duration > 0) {
            dbProxy.updateNodeMetrics(callerId, contextID, timestamp, duration);
        }

        return callerId;
    }

    public void flush() {
        requestsProcessor.flush();
    }

    @Override
    public List<FunctionMetric> getMetricsList() {
        return METRICS;
    }

    @Override
    public List<FunctionCallWithMetric> getCallers(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        throw new NotImplementedException();
    }

    @Override
    public List<FunctionCallWithMetric> getCallees(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        throw new NotImplementedException();
    }

    @Override
    public List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, List<DataFilter> filters, int limit) {
        List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
        try {
            TimeIntervalDataFilter timeFilter = Util.firstInstanceOf(TimeIntervalDataFilter.class, filters);

            StringBuilder sqlQuery = new StringBuilder();
            sqlQuery.append("select func_id, fname, context_id, sum(time_incl) as time_incl, sum(time_excl) as time_excl "); // NOI18N
            sqlQuery.append("from funcmetrics left join funcnames on funcnames.id = funcmetrics.func_id "); // NOI18N
            if (timeFilter != null) {
                Range<Long> interval = timeFilter.getInterval();
                sqlQuery.append("where bucket >= ").append(DBProxy.timeToBucket(interval.getStart())); // NOI18N
                sqlQuery.append(" and bucket <= ").append(DBProxy.timeToBucket(interval.getEnd())); // NOI18N
            }
            sqlQuery.append(" group by func_id, fname, context_id"); // NOI18N
            sqlQuery.append(" order by ").append(metric.getMetricID()).append(" desc"); // NOI18N

            PreparedStatement select = stmtCache.getPreparedStatement(sqlQuery.toString());
            select.setMaxRows(limit);

            ResultSet rs = select.executeQuery();
            try {
                while (rs.next()) {
                    long func_id = rs.getLong(1);
                    String name = rs.getString(2);
                    long context_id = rs.getLong(3);
                    Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                    metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                    metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                    FunctionImpl func = new FunctionImpl(func_id, context_id, name, name);
                    result.add(new FunctionCallImpl(func, metrics));
                }
            } finally {
                rs.close();
            }

            demangle(result);
        } catch (Exception e) {
        }

        return result;
    }

    @Override
    /**
     * get list of function calls with metrics based on a provided metadata 
     * (table or view), filtered with filters ... 
     * 
     * functionDescription gives names of some columns that are needed to 
     * construct Function object ('name', 'offset', 'id' and 'context')
     */
    public List<FunctionCallWithMetric> getFunctionsList(DataTableMetadata metadata,
            List<Column> metricsColumn, FunctionDatatableDescription functionDescription, List<DataFilter> filters) {
        List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();

        try {
            Collection<FunctionMetric> metrics = new ArrayList<FunctionMetric>();
            for (Column metricColumn : metricsColumn) {
                FunctionMetric metric = FunctionMetricsFactory.getInstance().getFunctionMetric(new FunctionMetric.FunctionMetricConfiguration(metricColumn.getColumnName(), metricColumn.getColumnUName(), metricColumn.getColumnClass()));
                metrics.add(metric);
            }

            String cFuncID = functionDescription.getFunctionIDColumnName();
            String cFuncName = functionDescription.getFunctionNameColumnName();
            String cFuncContext = functionDescription.getContextIDColumnName();
            String cOffset = functionDescription.getOffsetColumnName();

            Collection<TimeIntervalDataFilter> timeFilters = getDataFilters(filters, TimeIntervalDataFilter.class);
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
                            Object value = rs.getObject(m.getMetricID());
                            if (m.getMetricValueClass() == Time.class && value != null) {
                                value = new Time(Long.valueOf(value.toString()));
                            }
                            metricValues.put(m, value);
                        } catch (SQLException ex) {
                            SQLExceptions.printStackTrace(sqlStorage, ex);
                        }
                    }

                    long funcID = rs.getLong(cFuncID);
                    long contextID = cFuncContext == null ? -1 : rs.getLong(cFuncContext);
                    String funcName = rs.getString(cFuncName);
                    long offset = cOffset == null ? -1 : rs.getLong(cOffset);

                    FunctionImpl func = new FunctionImpl(funcID, contextID, funcName, funcName);
                    result.add(new FunctionCallImpl(func, offset, metricValues));
                }
            } finally {
                rs.close();
            }

            demangle(result);
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
        }
        return result;
    }

    private ThreadSnapshot fetchSnapshot(int threadId, long timestamp, boolean fullMsa) throws SQLException {
        PreparedStatement s = stmtCache.getPreparedStatement("SELECT stack_id, mstate FROM CallStack WHERE thread_id = ? AND timestamp = ?"); // NOI18N
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

    @Override
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
                conditions.add(timeFilter.getStartTime() + " <= timestamp"); // NOI18N
            }
            if (0 <= timeFilter.getEndTime()) {
                conditions.add("timestamp <= " + timeFilter.getEndTime()); // NOI18N
            }
        }

        StringBuilder select = new StringBuilder("SELECT thread_id, "); // NOI18N
        if (timeFilter != null) {
            switch (timeFilter.getMode()) {
                case FIRST:
                    select.append("MIN(timestamp) "); // NOI18N
                    break;
                case LAST:
                    select.append("MAX(timestamp) "); // NOI18N
                    break;
                default:
                    select.append("timestamp "); // NOI18N
            }
        } else {
            select.append("timestamp "); // NOI18N
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
            SQLExceptions.printStackTrace(sqlStorage, ex);
            return Collections.emptyList();
        }
    }

    @Override
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
            if (query.getThreadID() == dump.getThreadInfo().getThreadId()
                    && query.getPreferredState() == getTrueState(dump, query)) {
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
                if (query.getThreadID() == dump.getThreadInfo().getThreadId()
                        && query.getPreferredState() == getTrueState(dump, query)) {
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
                    if (query.getThreadID() == dump.getThreadInfo().getThreadId()
                            && query.getPreferredState() == getTrueState(dump, query)) {
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

    @Override
    public Function getLeafFunction(long stackId) {
        long funcID, offset, offsetInModule, srcLine, srcColumn;
        String funcName, module, srcFile;
        StringBuilder qname = new StringBuilder();
        long nodeID = stackId;
        Function func = null;
        try {
            PreparedStatement ps = stmtCache.getPreparedStatement(
                    "select caller_id, func_id, offset, fname, modules.path, module_offset, sourcefiles.path, sourceinfo.fline, sourceinfo.fcolumn" // NOI18N
                    + " from stacknode left join funcnames on funcnames.id = stacknode.func_id" // NOI18N
                    + " left join moduleinfo on moduleinfo.node_id = stacknode.id" // NOI18N
                    + " left join modules on modules.id = moduleinfo.module_id" // NOI18N
                    + " left join sourceinfo on sourceinfo.node_id = stacknode.id" // NOI18N
                    + " left join sourcefiles on sourcefiles.id = sourceinfo.file_id" // NOI18N
                    + " where stacknode.id = ?"); // NOI18N

            ps.setLong(1, nodeID);

            ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    nodeID = rs.getLong(1);
                    funcID = rs.getLong(2);
                    offset = rs.getLong(3);
                    funcName = rs.getString(4);
                    module = rs.getString(5);
                    offsetInModule = rs.getLong(6);
                    srcFile = rs.getString(7);
                    srcLine = rs.getLong(8);
                    srcColumn = rs.getLong(9);

                    String moduleOffset = offsetInModule < 0 ? null : "0x" + Long.toHexString(offsetInModule); // NOI18N

                    if (module != null) {
                        qname.append(module);
                        if (moduleOffset != null) {
                            qname.append('+').append(moduleOffset);
                        }
                        qname.append('`');
                    }

                    qname.append(funcName);

                    if (offset > 0) {
                        qname.append("+0x").append(Long.toHexString(offset)); // NOI18N
                    }

                    if (srcFile != null) {
                        qname.append(':').append(srcFile);
                        if (srcLine > 0) {
                            qname.append(':').append(srcLine);
                            if (srcColumn > 0) {
                                qname.append(':').append(srcColumn);
                            }
                        }
                    }

                    func = new FunctionImpl(funcID, -1, funcName, qname.toString(), module, moduleOffset, srcFile);

                } else {//try to get from the cache
                    func =  dbProxy.getLeafFunction(stackId);
                }
            } finally {
                qname.setLength(0);
                rs.close();
            }

        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
        }
        if (func != null){
            demangle(func);
        }
        return func;
    }

    @Override
    /**
     * Callstack itself is out of any context. So, returned list is not binded 
     * to any context (!) - context should be used in sequent calls to other 
     * API methods...
     */
    public synchronized List<FunctionCall> getCallStack(final long stackId) {
        long funcID, offset, offsetInModule, srcLine, srcColumn;
        String funcName, module, srcFile;
        StringBuilder qname = new StringBuilder();

        List<FunctionCall> result = new ArrayList<FunctionCall>();
        try {
            long nodeID = stackId;
            while (0 < nodeID) {
                PreparedStatement ps = stmtCache.getPreparedStatement(
                        "select caller_id, func_id, offset, fname, modules.path, module_offset, sourcefiles.path, sourceinfo.fline, sourceinfo.fcolumn" // NOI18N
                        + " from stacknode left join funcnames on funcnames.id = stacknode.func_id" // NOI18N
                        + " left join moduleinfo on moduleinfo.node_id = stacknode.id" // NOI18N
                        + " left join modules on modules.id = moduleinfo.module_id" // NOI18N
                        + " left join sourceinfo on sourceinfo.node_id = stacknode.id" // NOI18N
                        + " left join sourcefiles on sourcefiles.id = sourceinfo.file_id" // NOI18N
                        + " where stacknode.id = ?"); // NOI18N

                ps.setLong(1, nodeID);

                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        nodeID = rs.getLong(1);
                        funcID = rs.getLong(2);
                        offset = rs.getLong(3);
                        funcName = rs.getString(4);
                        module = rs.getString(5);
                        offsetInModule = rs.getLong(6);
                        srcFile = rs.getString(7);
                        srcLine = rs.getLong(8);
                        srcColumn = rs.getLong(9);

                        String moduleOffset = offsetInModule < 0 ? null : "0x" + Long.toHexString(offsetInModule); // NOI18N

                        if (module != null) {
                            qname.append(module);
                            if (moduleOffset != null) {
                                qname.append('+').append(moduleOffset);
                            }else{
                                qname.append('+').append("0x0");//NOI18N
                            }
                            qname.append('`');
                        }

                        qname.append(funcName);

                        if (offset > 0) {
                            qname.append("+0x").append(Long.toHexString(offset)); // NOI18N
                        }else{
                            qname.append('+').append("0x0");//NOI18N
                        }

                        if (srcFile != null) {
                            qname.append(':').append(srcFile);
                            if (srcLine >= 0) {
                                qname.append(':').append(srcLine);
                                if (srcColumn >= 0) {
                                    qname.append(':').append(srcColumn);
                                }
                            }
                        }

                        FunctionImpl func = new FunctionImpl(funcID, -1, funcName, qname.toString(), module, moduleOffset, srcFile);
                        result.add(new FunctionCallImpl(func, offset, Collections.<FunctionMetric, Object>emptyMap()));
                    } else {
                        break;
                    }
                } finally {
                    qname.setLength(0);
                    rs.close();
                }
            }
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
        }
        demangle(result);
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

    private void demangle(Function f) {
        if (demangler != null) {
            String demangled = demangler.demangle(f.getName());
            ((FunctionImpl) f).setName(demangled);
        }
    }
}
