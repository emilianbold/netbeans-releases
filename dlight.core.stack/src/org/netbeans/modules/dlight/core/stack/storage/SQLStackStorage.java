/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.core.stack.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionMetricsFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.spi.CppSymbolDemangler;
import org.netbeans.modules.dlight.spi.CppSymbolDemanglerFactory;
import org.openide.util.Lookup;

/**
 * Stack data storage over a relational database.
 *
 * @author Alexey Vladykin
 */
public final class SQLStackStorage {

    public static final List<FunctionMetric> METRICS = Arrays.<FunctionMetric>asList(
            FunctionMetric.CpuTimeExclusiveMetric, FunctionMetric.CpuTimeInclusiveMetric);
    protected final SQLDataStorage sqlStorage;
    private final Map<CharSequence, Integer> funcCache;
    private final Map<NodeCacheKey, Integer> nodeCache;
    private int funcIdSequence;
    private int nodeIdSequence;
    private final ExecutorThread executor;
    private boolean isRunning = true;
    private final CppSymbolDemangler demangler;

    public SQLStackStorage(SQLDataStorage sqlStorage) throws SQLException, IOException {
        this.sqlStorage = sqlStorage;
        initTables();
        funcCache = new HashMap<CharSequence, Integer>();
        nodeCache = new HashMap<NodeCacheKey, Integer>();
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
    }

    private void initTables() throws SQLException, IOException {
        InputStream is = SQLStackStorage.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/core/stack/resource/schema.sql"); //NOI18N
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            sqlStorage.execute(reader);
        } finally {
            reader.close();
        }
    }

    public boolean shutdown() {
        isRunning = false;
        funcCache.clear();
        nodeCache.clear();
        return true;
    }

    public int putStack(List<CharSequence> stack, long sampleDuration) {
        int callerId = 0;
        Set<Integer> funcs = new HashSet<Integer>();
        for (int i = 0; i < stack.size(); ++i) {
            boolean isLeaf = i + 1 == stack.size();
            CharSequence funcName = stack.get(i);
            int funcId = generateFuncId(funcName);
            updateMetrics(funcId, false, sampleDuration, !funcs.contains(funcId), isLeaf);
            funcs.add(funcId);
            int nodeId = generateNodeId(callerId, funcId, getOffset(funcName));
            updateMetrics(nodeId, true, sampleDuration, true, isLeaf);
            callerId = nodeId;
        }
        return callerId;
    }

    public void flush() throws InterruptedException {
        executor.flush();
    }

    public List<Long> getPeriodicStacks(long startTime, long endTime, long interval) throws SQLException {
        List<Long> result = new ArrayList<Long>();
        PreparedStatement ps = sqlStorage.prepareStatement(
                "SELECT time_stamp FROM CallStack " + //NOI18N
                "WHERE ? <= time_stamp AND time_stamp < ? ORDER BY time_stamp"); //NOI18N
        ps.setMaxRows(1);
        for (long time1 = startTime; time1 < endTime; time1 += interval) {
            long time2 = Math.min(time1 + interval, endTime);
            ps.setLong(1, time1);
            ps.setLong(2, time2);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.add(rs.getLong(1));
            }
            rs.close();
        }
        return result;
    }

    public List<FunctionMetric> getMetricsList() {
        return METRICS;
    }

    public List<FunctionCallWithMetric> getCallers(FunctionCallWithMetric[] path, boolean aggregate) throws SQLException {
        List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
        PreparedStatement select = prepareCallersSelect(path);
        ResultSet rs = select.executeQuery();
        while (rs.next()) {
            Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
            metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
            metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
            String funcName = rs.getString(2);
            if (demangler != null) {
                funcName = demangler.demangle(funcName);
            }
            result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName, rs.getString(3)), metrics));
        }
        rs.close();
        return result;
    }

    public List<FunctionCallWithMetric> getCallees(FunctionCallWithMetric[] path, boolean aggregate) throws SQLException {
        List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
        PreparedStatement select = prepareCalleesSelect(path);
        ResultSet rs = select.executeQuery();
        while (rs.next()) {
            Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
            metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
            metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
            String funcName = rs.getString(2);
            if (demangler != null) {
                funcName = demangler.demangle(funcName);
            }
            result.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), funcName, rs.getString(3)), metrics));
        }
        rs.close();
        return result;
    }

    public FunctionCall getFunctionCall(int stackID) {
        return null;
    }


    public List<FunctionCallWithMetric> getHotSpotFunctions(FunctionMetric metric, int limit) {
        try {
            List<String> funcNames = new ArrayList<String>();
            List<FunctionCallWithMetric> funcList = new ArrayList<FunctionCallWithMetric>();
            PreparedStatement select = sqlStorage.prepareStatement(
                    "SELECT func_id, func_name, func_full_name,  time_incl, time_excl " + //NOI18N
                    "FROM Func ORDER BY " + metric.getMetricID() + " DESC"); //NOI18N
            select.setMaxRows(limit);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();
                metrics.put(FunctionMetric.CpuTimeInclusiveMetric, new Time(rs.getLong(4)));
                metrics.put(FunctionMetric.CpuTimeExclusiveMetric, new Time(rs.getLong(5)));
                String name = rs.getString(2);
                funcNames.add(name);
                funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(1), name, rs.getString(3)), metrics));
            }
            rs.close();

            if (demangler != null) {
                funcNames = demangler.demangle(funcNames);
                if (funcNames.size() == funcList.size()) {
                    for (int i = 0; i < funcList.size(); ++i) {
                        ((FunctionImpl) funcList.get(i).getFunction()).setName(funcNames.get(i));
                    }
                }
            }

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
            List<String> funcNames = new ArrayList<String>();
            List<FunctionCallWithMetric> funcList = new ArrayList<FunctionCallWithMetric>();
            PreparedStatement select = sqlStorage.prepareStatement(metadata.getViewStatement());
            ResultSet rs = select.executeQuery();
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
                funcNames.add(funcName);
                funcList.add(new FunctionCallImpl(new FunctionImpl(rs.getInt(functionUniqueID), funcName, funcName), offesetColumnName != null ? rs.getLong(offesetColumnName) : -1, metricValues));
            }
            rs.close();

            if (demangler != null) {
                funcNames = demangler.demangle(funcNames);
                if (funcNames.size() == funcList.size()) {
                    for (int i = 0; i < funcList.size(); ++i) {
                        ((FunctionImpl) funcList.get(i).getFunction()).setName(funcNames.get(i));
                    }
                }
            }

            return funcList;
        } catch (SQLException ex) {
        }
        return Collections.emptyList();
    }

////////////////////////////////////////////////////////////////////////////////
    private void updateMetrics(int id, boolean funcOrNode, long sampleDuration, boolean addIncl, boolean addExcl) {
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

    private int generateNodeId(int callerId, int funcId, long offset) {
        synchronized (nodeCache) {
            NodeCacheKey cacheKey = new NodeCacheKey(callerId, funcId, offset);
            Integer nodeId = nodeCache.get(cacheKey);
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

    private int generateFuncId(CharSequence funcName) {
        int plusPos = lastIndexOf(funcName, '+'); // NOI18N
        if (0 <= plusPos) {
            funcName = funcName.subSequence(0, plusPos);
        }
        synchronized (funcCache) {
            Integer funcId = funcCache.get(funcName);
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

    private PreparedStatement prepareCallersSelect(FunctionCallWithMetric[] path) throws SQLException {
        StringBuilder buf = new StringBuilder();
        buf.append(" SELECT F.func_id, F.func_name, F.func_full_name, SUM(N.time_incl), SUM(N.time_excl) FROM Node AS N "); //NOI18N
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id "); //NOI18N
        buf.append(" INNER JOIN Node N1 ON N.node_id = N1.caller_id "); //NOI18N
        for (int i = 1; i < path.length; ++i) {
            buf.append(" INNER JOIN Node AS N").append(i + 1); //NOI18N
            buf.append(" ON N").append(i).append(".node_id = N").append(i + 1).append(".caller_id "); //NOI18N
        }
        buf.append(" WHERE "); //NOI18N
        for (int i = 1; i <= path.length; ++i) {
            if (1 < i) {
                buf.append("AND "); //NOI18N
            }
            buf.append("N").append(i).append(".func_id = ? "); //NOI18N
        }
        buf.append(" GROUP BY F.func_id, F.func_name, F.func_full_name"); //NOI18N
        PreparedStatement select = sqlStorage.prepareStatement(buf.toString());
        for (int i = 0; i < path.length; ++i) {
            select.setInt(i + 1, ((FunctionImpl) path[i].getFunction()).getId());
        }
        return select;
    }

    private PreparedStatement prepareCalleesSelect(FunctionCallWithMetric[] path) throws SQLException {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT F.func_id, F.func_name, F.func_full_name, SUM(N.time_incl), SUM(N.time_excl) FROM Node AS N1 "); //NOI18N
        for (int i = 1; i < path.length; ++i) {
            buf.append(" INNER JOIN Node AS N").append(i + 1); //NOI18N
            buf.append(" ON N").append(i).append(".node_id = N").append(i + 1).append(".caller_id "); //NOI18N
        }
        buf.append(" INNER JOIN Node N ON N").append(path.length).append(".node_id = N.caller_id "); //NOI18N
        buf.append(" LEFT JOIN Func AS F ON N.func_id = F.func_id WHERE "); //NOI18N
        for (int i = 1; i <= path.length; ++i) {
            if (1 < i) {
                buf.append(" AND "); //NOI18N
            }
            buf.append(" N").append(i).append(".func_id = ? "); //NOI18N
        }
        buf.append(" GROUP BY F.func_id, F.func_name, F.func_full_name"); //NOI18N
        PreparedStatement select = sqlStorage.prepareStatement(buf.toString());
        for (int i = 0; i < path.length; ++i) {
            select.setInt(i + 1, ((FunctionImpl) path[i].getFunction()).getId());
        }
        return select;
    }

    public ThreadDump getThreadDump(long timestamp, long threadID, int threadState) {
        ThreadDumpImpl result = null;

        try {
            // First, we need ts of the thread threadID when it was in required state.
          PreparedStatement start_ts_st = sqlStorage.prepareStatement("SELECT min(time_stamp) FROM CallStack" ); // NOI18N
          ResultSet rs_start_ts = start_ts_st.executeQuery();
            long start_ts = 0;

            if (rs_start_ts.next()) {
                start_ts = rs_start_ts.getLong(1);
            }

          PreparedStatement st = sqlStorage.prepareStatement("SELECT * from CallStack"); // NOI18N
          ResultSet rs1 = st.executeQuery();
          ResultSetMetaData rsm= rs1.getMetaData();
          int columnsCount = rsm.getColumnCount();
          System.out.print("\nColumns   :"); // NOI18N
          for ( int i = 0 ; i < columnsCount; i++){
              System.out.print(" " + rsm.getColumnName(i + 1)); // NOI18N
          }
          while (rs1.next()){
              System.out.print("\nNew record :"); // NOI18N
            for (int i=0; i < columnsCount; i++){
                System.out.print(" " + rs1.getObject(i + 1)); // NOI18N
            }
          }

            PreparedStatement statement = sqlStorage.prepareStatement(
                    "select max(time_stamp) from CallStack where " + // NOI18N
     //               "thread_id = ? and time_stamp <= ? and mstate = ?"); // NOI18N
                                   "thread_id = ? and time_stamp <= ? "); // NOI18N

            statement.setLong(1, threadID);
            statement.setLong(2, timestamp + start_ts);
            //statement.setInt(3, threadState);

            ResultSet rs = statement.executeQuery();
            long ts = -1;

            if (rs.next()) {
                ts = rs.getLong(1);
            }

            rs.close();

            if (ts < 0) {
                // Means that no callstack found for this thread in this state
                //System.out.println("No callstack found!!!");
                return null;
            }

            //System.out.println("Nearest callstack found at " + ts);

            result = new ThreadDumpImpl(ts);

            // Next, get all times for all threads for alligned stacks (time <= ts)
            // select threadid, max(ts) from test where ts <= 6 group by threadid;

            statement = sqlStorage.prepareStatement(
                    "select thread_id, max(time_stamp) from CallStack where " + // NOI18N
                    "time_stamp <= ? group by thread_id"); // NOI18N

            statement.setLong(1, ts + start_ts);

            rs = statement.executeQuery();

            HashMap<Integer, Long> idToTime = new HashMap<Integer, Long>();

            while (rs.next()) {
                int callStackThreadId = rs.getInt(1);
                long callStackTimeStamp = rs.getLong(2);
                idToTime.put(callStackThreadId, callStackTimeStamp);
            }
            //get leaf_id's
            Iterator<Integer> iterator = idToTime.keySet().iterator();
            while (iterator.hasNext()){
                int thread_id = iterator.next();
                long t = idToTime.get(thread_id);
                statement = sqlStorage.prepareStatement("SELECT leaf_id from CallStack where thread_id=" + thread_id + " AND time_stamp=" + t); // NOI18N
                ResultSet set = statement.executeQuery();
                if (set.next()){
                    int leaf_id  = set.getInt(1);
                    break;
                }

            }
            // Next, get stacks from database having tstamps and thread ids..
            

        } catch (SQLException ex) {
            System.err.println("ex: " + ex.getSQLState());  // NOI18N
        }

        return result;
    }

////////////////////////////////////////////////////////////////////////////////
    private static class NodeCacheKey {

        private final int callerId;
        private final int funcId;
        private long offset;

        public NodeCacheKey(int callerId, int funcId, long offset) {
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
            return 13 * callerId + 17 * funcId + ((int) (offset >> 32) | (int) offset);
        }
    }

    protected static class FunctionImpl implements Function {

        private final int id;
        private String name;
        private final String quilifiedName;

        public FunctionImpl(int id, String name, String qualifiedName) {
            this.id = id;
            this.name = name;
            this.quilifiedName = qualifiedName;
        }

        public int getId() {
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
            return getFunction().getName() + (hasOffset() ? ("+0x" + getOffset()) : ""); //NOI18N
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

        public int id;
        public CharSequence name;
//        public CharSequence full_name;
    }

    private static class AddNode {

        public int id;
        public int callerId;
        public int funcId;
        public long offset;
    }

    private static class UpdateMetrics {

        public int objId;
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
                Map<Integer, UpdateMetrics> funcMetrics = new HashMap<Integer, UpdateMetrics>();
                Map<Integer, UpdateMetrics> nodeMetrics = new HashMap<Integer, UpdateMetrics>();
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
                                Map<Integer, UpdateMetrics> map = updateMetricsCmd.funcOrNode ? nodeMetrics : funcMetrics;
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
                                    PreparedStatement stmt = sqlStorage.prepareStatement("INSERT INTO Func (func_id, func_full_name, func_name, time_incl, time_excl) VALUES (?, ?, ?, ?, ?)"); //NOI18N
                                    stmt.setInt(1, addFunctionCmd.id);
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
                                    PreparedStatement stmt = sqlStorage.prepareStatement("INSERT INTO Node (node_id, caller_id, func_id, offset, time_incl, time_excl) VALUES (?, ?, ?, ?, ?, ?)"); //NOI18N
                                    stmt.setInt(1, addNodeCmd.id);
                                    stmt.setInt(2, addNodeCmd.callerId);
                                    stmt.setInt(3, addNodeCmd.funcId);
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
                                PreparedStatement stmt = sqlStorage.prepareStatement("UPDATE Func SET time_incl = time_incl + ?, time_excl = time_excl + ? WHERE func_id = ?"); //NOI18N
                                stmt.setLong(1, cmd.cpuTimeInclusive);
                                stmt.setLong(2, cmd.cpuTimeExclusive);
                                stmt.setInt(3, cmd.objId);
                                stmt.executeUpdate();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }
                        funcMetrics.clear();

                        for (UpdateMetrics cmd : nodeMetrics.values()) {
                            try {
                                PreparedStatement stmt = sqlStorage.prepareStatement("UPDATE Node SET time_incl = time_incl + ?, time_excl = time_excl + ? WHERE node_id = ?"); //NOI18N
                                stmt.setLong(1, cmd.cpuTimeInclusive);
                                stmt.setLong(2, cmd.cpuTimeExclusive);
                                stmt.setInt(3, cmd.objId);
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
