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
import org.netbeans.modules.dlight.core.stack.storage.impl.SQLStackRequestsProvider.AddFunctionRequest;
import org.netbeans.modules.dlight.core.stack.storage.impl.SQLStackRequestsProvider.AddNodeRequest;
import org.netbeans.modules.dlight.core.stack.utils.FunctionNameUtils;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ProxyDataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.spi.support.SQLExceptions;
import org.netbeans.modules.dlight.spi.support.SQLRequest;
import org.netbeans.modules.dlight.spi.support.SQLRequestsProcessor;
import org.netbeans.modules.dlight.spi.support.SQLStatementsCache;
import org.netbeans.modules.dlight.util.Util;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Alexey Vladykin
 */
public class SQLStackDataStorage implements ProxyDataStorage, StackDataStorage, ThreadDumpProvider {

    private final List<DataTableMetadata> tableMetadatas;
    private final Map<CharSequence, Long> funcCache;
    private final Map<NodeCacheKey, Long> nodeCache;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private MetricsCache metricsCache;
    private SQLRequestsProcessor requestsProcessor;
    private SQLStackRequestsProvider requestsProvider;
    private SQLStatementsCache stmtCache;
    private SQLDataStorage sqlStorage;
    private CppSymbolDemangler demangler;
    private ServiceInfoDataStorage serviceInfoDataStorage;
    private long funcIdSequence;
    private long nodeIdSequence;

    public SQLStackDataStorage() {
        tableMetadatas = new ArrayList<DataTableMetadata>();
        funcCache = new HashMap<CharSequence, Long>();
        nodeCache = new HashMap<NodeCacheKey, Long>();
        funcIdSequence = 0;
        nodeIdSequence = 0;
    }

    @Override
    public void syncAddData(String tableName, List<DataRow> data) {
        addData(tableName, data);
    }

    @Override
    public final void attachTo(ServiceInfoDataStorage serviceInfoStorage) {
        this.serviceInfoDataStorage = serviceInfoStorage;
        CppSymbolDemanglerFactory factory = Lookup.getDefault().lookup(CppSymbolDemanglerFactory.class);
        if (factory != null) {
            demangler = factory.getForCurrentSession(serviceInfoStorage.getInfo());
        } else {
            demangler = null;
        }
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

        this.sqlStorage = (SQLDataStorage) storage;
        stmtCache = SQLStatementsCache.getFor(sqlStorage);
        metricsCache = new MetricsCache();
        requestsProvider = new SQLStackRequestsProvider(stmtCache, metricsCache);
        requestsProcessor = sqlStorage.getRequestsProcessor();

        try {
            initTables();
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
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
        return data.isProvidedBy(tableMetadatas);
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

    @Override
    public boolean shutdown() {
        if (closed.compareAndSet(false, true)) {
            funcCache.clear();
            nodeCache.clear();
            metricsCache = null;
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

    private void initTables() throws SQLException, IOException {
        InputStream is = SQLStackDataStorage.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/core/stack/resources/schema.sql"); //NOI18N
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            Pattern autoIncrementPattern = Pattern.compile("\\{AUTO_INCREMENT\\}"); //NOI18N
            String line;
            StringBuilder buf = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("-- ")) { //NOI18N
                    continue;
                }
                line = autoIncrementPattern.matcher(line).replaceAll(sqlStorage.getAutoIncrementExpresion());
                buf.append(line);
                if (line.endsWith(";")) { //NOI18N
                    String sql = buf.toString();
                    buf.setLength(0);
                    String sqlToExecute = sql.substring(0, sql.length() - 1);
                    try{
                        sqlStorage.executeUpdate(sqlToExecute);
                    }catch(SQLException e){

                    }
                }
            }
        } finally {
            reader.close();
        }
    }

    @Override
    public synchronized long putStack(long context_id, List<CharSequence> stack) {
        return putSample(context_id, stack, -1, -1);
    }

    @Override
    public synchronized long putSample(long context_id, List<CharSequence> stack, long timestamp, long duration) {
        long callerId = 0;
        Set<Long> funcs = new HashSet<Long>();
        boolean isLeaf;
        for (int i = stack.size() - 1; i >= 0; i--) {
            isLeaf = i == 0;
            CharSequence funcName = stack.get(i);
            SourceFileInfo sourceFile = FunctionNameUtils.getSourceFileInfo(funcName.toString());
            long funcId = generateFuncId(context_id, funcName, sourceFile);
            updateMetrics(funcId, false, timestamp, duration, !funcs.contains(funcId), isLeaf);
            funcs.add(funcId);
            long nodeId = generateNodeId(callerId, funcId, getOffset(funcName), sourceFile == null ? -1 : sourceFile.getLine());
            updateMetrics(nodeId, true, timestamp, duration, true, isLeaf);
            callerId = nodeId;
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
        try {
            List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
            ResultSet rs = sqlStorage.select(null, null, prepareCallersSelect(path));
            try {
                while (rs.next()) {
                    Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                    metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(3)));
                    metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(4)));
                    String funcName = rs.getString(2);
                    String fileName = rs.getString(5);
                    long line_number  = rs.getLong(6);
                    String createdFullName = funcName +
                            (fileName != null ? ":" + fileName + ":" + line_number : "");//NOI18N
                    result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName,
                            createdFullName, fileName), metrics));
                }
            } finally {
                rs.close();
            }
            demangle(result);
            return result;
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<FunctionCallWithMetric> getCallees(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        try {
            List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
            ResultSet rs = sqlStorage.select(null, null, prepareCalleesSelect(path));
            try {
                while (rs.next()) {
                    Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                    metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(3)));
                    metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(4)));
                    String funcName = rs.getString(2);
                    String fileName = rs.getString(5);
                    long line_number  = rs.getLong(6);
                    String createdFullName = funcName + 
                            (fileName != null ? ":" + fileName + ":" + line_number : "");//NOI18N
                    result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName, 
                            createdFullName, fileName), metrics));
                }
            } finally {
                rs.close();
            }
            demangle(result);
            return result;
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, List<DataFilter> filters, int limit) {
        try {
            List<FunctionCallWithMetric> funcList = new ArrayList<FunctionCallWithMetric>();
            TimeIntervalDataFilter timeFilter = Util.firstInstanceOf(TimeIntervalDataFilter.class, filters);
            PreparedStatement select = stmtCache.getPreparedStatement(
                    "SELECT Func.func_id, Func.func_name, SUM(FuncMetricAggr.time_incl) AS time_incl, " //NOI18N
                    + "SUM(FuncMetricAggr.time_excl) AS time_excl, SourceFiles.source_file, Func.line_number  " + //NOI18N
                    " FROM Func LEFT JOIN FuncMetricAggr ON Func.func_id = FuncMetricAggr.func_id " + // NOI18N
                    " LEFT JOIN SourceFiles ON Func.func_source_file_id = SourceFiles.id " + // NOI18N                    
                    (timeFilter != null ? "WHERE ? <= FuncMetricAggr.bucket_id AND FuncMetricAggr.bucket_id < ? " : "") + // NOI18N
                    "GROUP BY Func.func_id, Func.func_name,  SourceFiles.source_file, Func.line_number " + // NOI18N
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
                    metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(3)));
                    metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(4)));
                    String name = rs.getString(2);
                    String fileName = rs.getString(5);
                    long line_number  = rs.getLong(6);
                    String createdFullName = name +
                            (fileName != null ? ":" + fileName + ":" + line_number : "");//NOI18N
                    funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), name, 
                            createdFullName, fileName), metrics));
                }
            } finally {
                rs.close();
            }

            demangle(funcList);

            return funcList;
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
        }
        return Collections.emptyList();
    }

    @Override
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
                        } catch (SQLException ex) {
                            SQLExceptions.printStackTrace(sqlStorage, ex);
                        }
                    }
                    String funcName = rs.getString(functionColumnName);
                    funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(functionUniqueID),
                            funcName, funcName), offesetColumnName != null ? rs.getLong(offesetColumnName) : -1, metricValues));
                }
            } finally {
                rs.close();
            }

            demangle(funcList);

            return funcList;
        } catch (SQLException ex) {
            SQLExceptions.printStackTrace(sqlStorage, ex);
            return Collections.emptyList();
        }
    }

////////////////////////////////////////////////////////////////////////////////
    private void updateMetrics(long id, boolean funcOrNode, long timestamp, long duration, boolean addIncl, boolean addExcl) {
        if (duration > 0) {
            long bucket = timeToBucketId(timestamp);
            SQLRequest request;

            if (funcOrNode) {
                metricsCache.updateNodeMetrics(id, bucket, duration, addIncl, addExcl);
                request = requestsProvider.updateNodeMetrics(id, bucket);
            } else {
                metricsCache.updateFunctionMetrics(id, bucket, duration, addIncl, addExcl);
                request = requestsProvider.updateFunctionMetrics(id, bucket);
            }

            requestsProcessor.queueRequest(request);
        }
    }

    private long generateNodeId(long callerId, long funcId, long offset, int lineNumber) {
        synchronized (nodeCache) {
            long lastNodeKey = offset;
            if (lineNumber != -1){//use ut as a key
                lastNodeKey = lineNumber;
            }
            NodeCacheKey cacheKey = new NodeCacheKey(callerId, funcId, lastNodeKey);
            Long nodeId = nodeCache.get(cacheKey);
            if (nodeId == null) {
                nodeId = ++nodeIdSequence;
                AddNodeRequest cmd = requestsProvider.addNode(nodeId, callerId, funcId, offset, lineNumber);
                requestsProcessor.queueRequest(cmd);
                nodeCache.put(cacheKey, nodeId);
            }
            return nodeId;
        }
    }

    private long generateFuncId(long context_id, final CharSequence fname, SourceFileInfo sourceFileInfo) {
        // Need an immutable copy of fname. Otherwise will use
        // wrong key in funcCache (mutuable fname)
        String funcName = fname.toString();
        String fullFuncName = funcName;
        int source_file_index = -1;
        int line_number = -1;
        //check if there is a source file name information        
        if (sourceFileInfo != null && sourceFileInfo.getFileName() != null) {
            //funcName = FunctionNameUtils.getFullFunctionName(funcName);
            line_number = sourceFileInfo.getLine();
            int plusPos = lastIndexOf(funcName, '+'); // NOI18N
            if (0 <= plusPos) {
                funcName = funcName.substring(0, plusPos);
            }

            try {
                final PreparedStatement ps = stmtCache.getPreparedStatement(
                        "SELECT id from SourceFiles where source_file=?"); // NOI18N
                ResultSet rs = null;
                //syncronized is used as getPreparedStatement() method is not thread-safe
                synchronized(ps){
                    ps.setString(1, sourceFileInfo.getFileName());
                    rs  = ps.executeQuery();
                }
                if (rs != null && rs.next()) {
                    //get the id
                    source_file_index = rs.getInt("id"); //NOI18N
                } else {
                    final PreparedStatement stmt = stmtCache.getPreparedStatement(
                            "INSERT INTO SourceFiles (source_file) VALUES (?)"); // NOI18N
                    int r = 0;
                    //syncronized is used as getPreparedStatement() method is not thread-safe
                    synchronized(stmt) {
                        stmt.setString(1, sourceFileInfo.getFileName());
                        r = stmt.executeUpdate();
                    }
                    if (r > 0) {
                        ResultSet generatedKeys = stmt.getGeneratedKeys();
                        if (generatedKeys != null && generatedKeys.next() && generatedKeys.getMetaData().getColumnCount() > 0) {
                            source_file_index = generatedKeys.getInt(1);
                        }
                    }
                }
            } catch (SQLException ex) {
                SQLExceptions.printStackTrace(sqlStorage, ex);
            }
        } else {
            int plusPos = lastIndexOf(funcName, '+'); // NOI18N
            if (0 <= plusPos) {
                funcName = funcName.substring(0, plusPos);
            }
            fullFuncName = funcName;
        }

        synchronized (funcCache) {
            Long funcId = funcCache.get(funcName);
            if (funcId == null) {
                funcId = ++funcIdSequence;
                AddFunctionRequest cmd = requestsProvider.addFunction(funcId, funcName, source_file_index, line_number);
                requestsProcessor.queueRequest(cmd);
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

        buf.append(" SELECT F.func_id, F.func_name, SUM(N.time_incl), SUM(N.time_excl), " //NOI18N
                + " S.source_file, N.line_number  FROM Node AS N "); //NOI18N
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id "); //NOI18N
        buf.append(" LEFT JOIN SourceFiles AS S ON F.func_source_file_id = S.id  "); //NOI18N        
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
        buf.append(" GROUP BY F.func_id, F.func_name, S.source_file, N.line_number"); //NOI18N
        return buf.toString();
    }

    private String prepareCalleesSelect(List<FunctionCallWithMetric> path) throws SQLException {
        StringBuilder buf = new StringBuilder();
        int size = path.size();

        buf.append("SELECT F.func_id, F.func_name,  SUM(N.time_incl), SUM(N.time_excl), " //NOI18N
                + " S.source_file, N1.line_number  FROM Node AS N1 "); //NOI18N
        for (int i = 1; i < size; ++i) {
            buf.append(" INNER JOIN Node AS N").append(i + 1); //NOI18N
            buf.append(" ON N").append(i).append(".node_id = N").append(i + 1).append(".caller_id "); //NOI18N
        }
        buf.append(" INNER JOIN Node N ON N").append(size).append(".node_id = N.caller_id "); //NOI18N
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id "); //NOI18N
        buf.append(" LEFT JOIN SourceFiles  AS S ON F.func_source_file_id = S.id  "); //NOI18N                                
        buf.append(" WHERE "); //NOI18N
        for (int i = 0; i < size; ++i) {
            if (0 < i) {
                buf.append(" AND "); //NOI18N
            }
            buf.append(" N").append(i + 1).append(".func_id = "); //NOI18N
            buf.append(((FunctionImpl) path.get(i).getFunction()).getId());
        }
        buf.append(" GROUP BY F.func_id, F.func_name, S.source_file, N1.line_number"); //NOI18N
        return buf.toString();
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
    public synchronized List<FunctionCall> getCallStack(final long stackId) {
        List<FunctionCall> result = new ArrayList<FunctionCall>();
        try {
            long nodeID = stackId;
            while (0 < nodeID) {
                PreparedStatement ps = stmtCache.getPreparedStatement(
                        "SELECT Node.node_id, Node.caller_id, Node.func_id, Node.offset, Node.line_number, "  //NOI18N
                        + "Func.func_name, "//NOI18N
                        + " SourceFiles.source_file " + // NOI18N
                        "FROM Node LEFT JOIN Func ON Node.func_id = Func.func_id LEFT JOIN SourceFiles ON Func.func_source_file_id = SourceFiles.id " + // NOI18N
                        "WHERE node_id = ?"); // NOI18N
                ps.setLong(1, nodeID);

                ResultSet rs = ps.executeQuery();
                try {
                    if (rs.next()) {
                        String funcName = rs.getString(6);
                        long line_number = rs.getLong(5);
                        String fileName = rs.getString(7);
                        final long offset = rs.getLong(4);
                        String fullFuncName = funcName + "+0x" + Long.toHexString(offset) + (fileName != null ? ":" + fileName + ":" + line_number : "");//NOI18N
                        FunctionImpl func = new FunctionImpl(rs.getInt(3), funcName, fullFuncName, fileName);
                        result.add(new FunctionCallImpl(func, offset, new HashMap<FunctionMetric, Object>()));
                        nodeID = rs.getInt(2);
                    } else {
                        break;
                    }
                } finally {
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

    /**
     *
     * @param timestamp  in nanoseconds
     * @return bucket id
     */
    private static long timeToBucketId(long timestamp) {
        return timestamp / 1000 / 1000 / 1000;  // bucket is 1 second
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

        private final long context_id;
        private final long id;
        private String name;
        private final String quilifiedName;
        private final String module_name;
        private final String module_offset;
        private final String source_file;

        public FunctionImpl(long id, String name, String qualifiedName) {
            this(id, name, qualifiedName, FunctionNameUtils.getFunctionModule(qualifiedName), FunctionNameUtils.getFunctionModuleOffset(qualifiedName),
                    FunctionNameUtils.getSourceFileInfo(qualifiedName) == null ? null : FunctionNameUtils.getSourceFileInfo(qualifiedName).getFileName());
        }

        public FunctionImpl(long id, String name, String qualifiedName, String source_file) {
            this(id, name, qualifiedName, FunctionNameUtils.getFunctionModule(qualifiedName),
                    FunctionNameUtils.getFunctionModuleOffset(qualifiedName), source_file);
        }

        public FunctionImpl(long id, String name, String qualifiedName, String module_name, String module_offset, String source_file) {
            this.id = id;
            this.name = name;
            this.quilifiedName = qualifiedName;
            this.module_name = module_name;
            this.module_offset = module_offset;
            this.source_file = source_file;
            this.context_id  = -1;
        }

        @Override
        public long getContextID() {
            return context_id;
        }


        public long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        @Override
        public String getSignature() {
            return quilifiedName;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String getQuilifiedName() {
            return FunctionNameUtils.getFunctionQName(name);
        }

        @Override
        public String getModuleName() {
            return FunctionNameUtils.getFunctionModule(name);
        }

        String getFullName() {
            return FunctionNameUtils.getFullFunctionName(quilifiedName);
        }

        @Override
        public String getModuleOffset() {
            return module_offset;
        }

        @Override
        public String getSourceFile() {
            return source_file;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FunctionImpl)) {
                return false;
            }

            FunctionImpl that = (FunctionImpl) obj;
            return (this.id == that.id && this.getFullName().equals(that.getFullName()));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + (this.getFullName() != null ? this.getFullName().hashCode() : 0);
            hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
            return hash;
        }
    }

    protected static class FunctionCallImpl extends FunctionCallWithMetric {

        private final Map<FunctionMetric, Object> metrics;
        private final int lineNumber;

        FunctionCallImpl(Function function, long offset, Map<FunctionMetric, Object> metrics) {
            super(function, offset);
            this.metrics = metrics;
            SourceFileInfo sourceFileInfo = FunctionNameUtils.getSourceFileInfo(function.getSignature());
            lineNumber = sourceFileInfo == null ? -1 : sourceFileInfo.getLine();
            setLineNumber(lineNumber);

        }

        FunctionCallImpl(Function function, Map<FunctionMetric, Object> metrics) {
            this(function, 0, metrics);
        }

        @Override
        public String getDisplayedName() {
            if (hasLineNumber()) {
                return FunctionNameUtils.getFunctionName(getFunction().getSignature());
                //+ "  " + getFunction().getSourceFile() + ":" + getLineNumber();//NOI18N
            }
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

        @Override
        public int getLineNumber() {

            return lineNumber;
        }
    }
}
