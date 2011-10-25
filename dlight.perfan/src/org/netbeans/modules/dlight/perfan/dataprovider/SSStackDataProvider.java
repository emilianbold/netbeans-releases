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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.dataprovider;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.core.stack.api.ThreadDumpProvider;
import org.netbeans.modules.dlight.perfan.util.TasksCachedProcessor;
import org.netbeans.modules.dlight.perfan.util.Computable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.api.datafilter.support.TimeIntervalDataFilter;
import org.netbeans.modules.dlight.management.remote.spi.PathMapper;
import org.netbeans.modules.dlight.management.remote.spi.PathMapperProvider;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.dataprovider.SSMetrics.MemoryMetric;
import org.netbeans.modules.dlight.perfan.dataprovider.SSMetrics.TimeMetric;
import org.netbeans.modules.dlight.perfan.lineinfo.impl.SSSourceFileInfoSupport;
import org.netbeans.modules.dlight.perfan.spi.datafilter.HotSpotFunctionsFilter;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionImpl;
import org.netbeans.modules.dlight.perfan.storage.impl.Address;
import org.netbeans.modules.dlight.perfan.storage.impl.ErprintCommand;
import org.netbeans.modules.dlight.perfan.storage.impl.Metrics;
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider.SourceFileInfo;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.threads.api.Datarace;
import org.netbeans.modules.dlight.threads.api.Deadlock;
import org.netbeans.modules.dlight.threads.dataprovider.ThreadAnalyzerDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Lookup;

/**
 * This class suppose to be thread-safe.
 * This means that it's methods (getHotSpotFunctions) can be called from any
 * thread at any moment.
 *
 * As any operation with er_print takes a while (could be significant time),
 * not all requests can be served in time.
 * So, the policy here is:
 *    - when request is arrives, it is stored in a queue and it's processing
 *      starts;
 *
 *    - CALLER THREAD IS BLOCKED until after result fetching.
 *
 *    - in case when new request arrives as long as it is not THE SAME request,
 *      it is stored to the queue
 *
 *    - in case when THE SAME request arrives prior to processing for the previous
 *      one is done, the result will be the one, received from the ORIGINAL (FIRST)
 *      request.
 *
 */
class SSStackDataProvider implements StackDataProvider, ThreadAnalyzerDataProvider {

    private final static Logger log = DLightLogger.getLogger(SSStackDataProvider.class);
    private final static Pattern fullInfoPattern = Pattern.compile("^(.*), line ([0-9]+) in \"(.*)\""); // NOI18N
    private final static Pattern noLineInfoPattern = Pattern.compile("^<Function: (.*), instructions from source file (.*)>"); // NOI18N
    private final static Pattern noDebugInfoPattern = Pattern.compile("^<Function: (.*), instructions without line numbers>"); // NOI18N
    private final static List<FunctionMetric> metricsList = Arrays.asList(
            TimeMetric.UserFuncTimeExclusive,
            TimeMetric.UserFuncTimeInclusive,
            TimeMetric.SyncWaitCallInclusive,
            TimeMetric.SyncWaitTimeInclusive,
            TimeMetric.SyncWaitCallExclusive,
            TimeMetric.SyncWaitTimeExclusive,
            MemoryMetric.LeakBytesMetric,
            MemoryMetric.LeaksCountMetric);
    private static boolean ompSupport = Boolean.valueOf(System.getProperty("dlight.sunstudio.omp")); // NOI18N
    private final Computable<HotSpotFunctionsFetcherParams, List<FunctionCallWithMetric>> hotSpotFunctionsFetcher =
            new TasksCachedProcessor<HotSpotFunctionsFetcherParams, List<FunctionCallWithMetric>>(new HotSpotFunctionsFetcher(), true);
    private final HashMap<Long, SourceFileInfo> nonSSSourceInfoCache = new HashMap<Long, SourceFileInfo>();
    private PerfanDataStorage storage;
    private SSSourceFileInfoSupport sourceFileInfoSupport = null;
    private PathMapper pathMapper = null;
    private String fullRemotePrefix = "";
    private Map<String, String> serviceInfo = null;
    private volatile HotSpotFunctionsFilter filter;
    private volatile TimeIntervalDataFilter timeIntervalDataFilter;

    @Override
    public void attachTo(final ServiceInfoDataStorage serviceInfoStorage) {
        if (serviceInfoStorage == null) {
            throw new NullPointerException();
        }

        String envID = serviceInfoStorage.getValue(ServiceInfoDataStorage.EXECUTION_ENV_KEY);
        ExecutionEnvironment execEnv = envID == null
                ? ExecutionEnvironmentFactory.getLocal()
                : ExecutionEnvironmentFactory.fromUniqueID(envID);

        String fullRemoteValue = serviceInfoStorage.getValue(ServiceInfoDataStorage.FULL_REMOTE_KEY);
        if (Boolean.valueOf(fullRemoteValue)) {
            assert envID != null && !execEnv.isLocal() : "unexpected envID " + envID + " and env " + execEnv;
            fullRemotePrefix = "rfs:" + envID; // NOI18N
        } else {
            PathMapperProvider pathMapperProvider = Lookup.getDefault().lookup(PathMapperProvider.class);
            pathMapper = pathMapperProvider == null ? null : pathMapperProvider.getPathMapper(execEnv);
        }
        serviceInfo = Collections.unmodifiableMap(serviceInfoStorage.getInfo());
        nonSSSourceInfoCache.clear();
    }

    @Override
    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
        boolean hasTimeIntervalFilter = false;
        for (DataFilter f : newSet) {
            if (f instanceof HotSpotFunctionsFilter) {
                filter = (HotSpotFunctionsFilter) f;
            }
            if (f instanceof TimeIntervalDataFilter) {
                timeIntervalDataFilter = (TimeIntervalDataFilter) f;
                hasTimeIntervalFilter = true;
            }
        }
        if (!hasTimeIntervalFilter && timeIntervalDataFilter != null) {
            //clear filter
            if (storage != null) {
                storage.setFilter("\"\"");//NOI18Ns
            }
            timeIntervalDataFilter = null;
        }
        if (hasTimeIntervalFilter && timeIntervalDataFilter != null) {
            Range<?> filterInterval = timeIntervalDataFilter.getInterval();
            if (filterInterval.getStart() != null || filterInterval.getEnd() != null) {
                storage.setFilter(filterInterval.toString(null, "TSTAMP>%d", "&&", "TSTAMP<%d", null)); // NOI18N
            } else {
                storage.setFilter(null);
            }
        }
    }

    @Override
    public ThreadDumpProvider getThreadDumpProvider() {
        return null;
    }

    private static enum CC_MODE {

        CALLEES,
        CALLERS
    };

    public SSStackDataProvider() {
    }

    @Override
    public List<FunctionCallWithMetric> getCallers(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        return getCallersCallees(CC_MODE.CALLERS, path, aggregate);
    }

    @Override
    public List<FunctionCallWithMetric> getCallees(List<FunctionCallWithMetric> path, List<Column> columns, List<Column> orderBy, boolean aggregate) {
        return getCallersCallees(CC_MODE.CALLEES, path, aggregate);
    }

    @Override
    public List<FunctionCall> getCallStack(int stackId) {
        //throw new UnsupportedOperationException("Not supported yet.");
        return Collections.emptyList();
    }

    @Override
    public List<FunctionCallTreeTableNode> getTableView(List<Column> columns, List<Column> orderBy, int limit) {
        return FunctionCallTreeTableNode.getFunctionCallTreeTableNodes(getFunctionCalls(columns, orderBy, limit));
    }

    @Override
    public List<FunctionCallTreeTableNode> getChildren(List<FunctionCallTreeTableNode> path, List<Column> columns, List<Column> orderBy) {
        List<FunctionCallWithMetric> fpath = FunctionCallTreeTableNode.getFunctionCalls(path);
        List<FunctionCallWithMetric> callers = getCallers(fpath, columns, orderBy, false);

        return FunctionCallTreeTableNode.getFunctionCallTreeTableNodes(callers);
    }

    @Override
    public FunctionCallTreeTableNode getValueAt(int row) {
        return null;
    }

    @Override
    public String getTableValueAt(Column column, int row) {
        return null;
    }

    // TODO: implement
    private synchronized List<FunctionCallWithMetric> getCallersCallees(CC_MODE mode, List<FunctionCallWithMetric> path, boolean aggregate) {
        //TODO: Now just take the last from the path...
//        FunctionCall parent_fc = path[path.length - 1];
//
//        Object[] raw_data = mode == CC_MODE.CALLEES ? storage.getCallees(((FunctionImpl) parent_fc.getFunction()).getRef()) : storage.getCallers(((FunctionImpl) parent_fc.getFunction()).getRef());
//
//        if (raw_data == null || raw_data.length == 0) {
//            return Collections.emptyList();
//        }
//
//        SortedSet<FunctionCallWithMetric> result = rawDataToResult(raw_data, NaturalFunctionCallComparator.getInstance(TimeMetric.UserFuncTimeInclusive));
//
//        List<FunctionCallWithMetric> callees = new ArrayList<FunctionCallWithMetric>(10);
//        Iterator<FunctionCallWithMetric> it = result.iterator();
//        while (it.hasNext()) {
//            FunctionCallWithMetric fc = it.next();
//            callees.add(fc);
//        }
//
//        return callees;
        return Collections.emptyList();
    }

    public List<FunctionCallWithMetric> getFunctionCalls(final List<Column> columns, final List<Column> orderBy, final int limit) {
        List<FunctionCallWithMetric> result = Collections.emptyList();

        try {
            result = hotSpotFunctionsFetcher.compute(
                    new HotSpotFunctionsFetcherParams(ErprintCommand.lines(), columns, orderBy, limit, filter));
        } catch (InterruptedException ex) {
            log.fine("HotSpotFunctionsFetcher interrupted"); // NOI18N
        }

        return result;
    }

    @Override
    public List<FunctionCallWithMetric> getHotSpotFunctions(
            final List<Column> columns, final List<Column> orderBy, final int limit) {

        try {
            return hotSpotFunctionsFetcher.compute(new HotSpotFunctionsFetcherParams(ErprintCommand.functions(), columns, orderBy, limit, filter));
        } catch (InterruptedException ex) {
            log.fine("HotSpotFunctionsFetcher interrupted."); // NOI18N
        }

        return Collections.emptyList();
    }

    @Override
    public List<? extends Deadlock> getDeadlocks() {
        return storage.getDeadlocks();
    }

    @Override
    public List<? extends Datarace> getDataraces() {
        return storage.getDataraces();
    }

    // TODO: !!!
    private FunctionMetric getMetricInstance(String name) {
        for (FunctionMetric metric : metricsList) {
            if (metric.getMetricID().equals(name)) {
                return metric;
            }
        }

        return null;
    }

    @Override
    public List<FunctionMetric> getMetricsList() {
        return metricsList;
    }

    @Override
    public void attachTo(DataStorage storage) {
        if (storage instanceof PerfanDataStorage) {
            this.storage = (PerfanDataStorage) storage;
            this.sourceFileInfoSupport = SSSourceFileInfoSupport.getSourceFileInfoSupportFor(this.storage);
            nonSSSourceInfoCache.clear();
        } else {
            String msg = "Attempt to attach SSStackDataProvider to storage " + // NOI18N
                    "'" + storage + "'"; // NOI18N

            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public SourceFileInfo getSourceFileInfo(final FunctionCall functionCall) {
        if (sourceFileInfoSupport == null) {
            return null;
        }

        if (!(functionCall instanceof FunctionCallImpl)) {
            return null;
        }

        final FunctionCallImpl fci = (FunctionCallImpl) functionCall;
        final Long refID = fci.getFunctionRefID();

        SourceFileInfo result = sourceFileInfoSupport.getSourceFileInfo(fci, pathMapper, fullRemotePrefix);

        if (result == null || !result.isSourceKnown()) {
            synchronized (nonSSSourceInfoCache) {
                if (nonSSSourceInfoCache.containsKey(refID)) {
                    result = nonSSSourceInfoCache.get(refID);
                } else {
                    Collection<? extends SourceFileInfoProvider> sourceInfoProviders =
                            Lookup.getDefault().lookupAll(SourceFileInfoProvider.class);

                    for (SourceFileInfoProvider provider : sourceInfoProviders) {
                        result = provider.getSourceFileInfo(
                                functionCall.getFunction().getQuilifiedName(),
                                (int) functionCall.getOffset(), -1, serviceInfo);
                        if (result != null && result.isSourceKnown()) {
                            log.log(Level.FINEST, "SourceLineInfo data from {0}: {1}", 
                                    new Object[]{provider.getClass().getSimpleName(), 
                                    result.toString()});
                            break;
                        }
                    }

                    nonSSSourceInfoCache.put(refID, result);
                }
            }
        }

        fci.setSourceFileInfo(result);

        return result;
    }

    private static class HotSpotFunctionsFetcherParams {

        private final ErprintCommand command;
        private final List<Column> resultColumns;
        private final List<Column> requestColumns;
        private final int[] columnsIdxRef;
        private final List<Column> orderBy;
        private final int limit;
        private final Metrics metrics;
        private final int nameIdx;
        private final int addressIdx;
        private final HotSpotFunctionsFilter filter;

        HotSpotFunctionsFetcherParams(ErprintCommand command,
                final List<Column> columns,
                final List<Column> orderBy,
                final int limit,
                HotSpotFunctionsFilter filter) {

            if (columns == null) {
                throw new NullPointerException();
            }

            if (columns.isEmpty()) {
                throw new IllegalArgumentException("HotSpotFunctionsFetcherParams: empty columns list!"); // NOI18N
            }

            // It is much easier to parse function name once it is the last column ;)
            // 
            // Moreover, identification of function just by name is not enough
            // so... for fething we use "own" list of columns based on provided one
            // But the result will comply with the request.

            resultColumns = new ArrayList<Column>();

            int size = columns.size();
            columnsIdxRef = new int[size];
            int nameColumnIdx = -1;
            int addressColumnIdx = -1;
            int idx = 0;

            for (int cidx = 0; cidx < size; cidx++) {
                Column c = columns.get(cidx);

                if (c.equals(SunStudioDCConfiguration.c_address)) {
                    addressColumnIdx = cidx;
                } else if (c.equals(SunStudioDCConfiguration.c_name)) {
                    nameColumnIdx = cidx;
                } else {
                    resultColumns.add(c);
                    columnsIdxRef[cidx] = idx++;
                }
            }

            if (addressColumnIdx == -1) {
                resultColumns.add(SunStudioDCConfiguration.c_address);
                addressColumnIdx = idx;
            } else {
                resultColumns.add(columns.get(addressColumnIdx));
                columnsIdxRef[addressColumnIdx] = idx;
            }

            addressIdx = idx;
            idx += 2; // increase idx by 2 as address takes 2 columns

            if (nameColumnIdx == -1) {
                resultColumns.add(SunStudioDCConfiguration.c_name);
                nameColumnIdx = idx;
            } else {
                resultColumns.add(columns.get(nameColumnIdx));
                columnsIdxRef[nameColumnIdx] = idx;
            }

            nameIdx = idx;
            idx += 1;

            this.command = (command == null) ? ErprintCommand.functions() : command;
            this.requestColumns = columns;
            this.orderBy = orderBy == null ? Arrays.asList(columns.get(0)) : orderBy;
            this.limit = limit;

            // To identify functions use of address is better than use of names

            this.metrics = Metrics.constructFrom(resultColumns, orderBy);
            this.filter = filter;
        }

        boolean isDefaultCommand() {
            return ErprintCommand.functions().equals(command);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof HotSpotFunctionsFetcherParams)) {
                throw new IllegalArgumentException();
            }
            HotSpotFunctionsFetcherParams o = (HotSpotFunctionsFetcherParams) obj;
            return o.metrics.equals(metrics) && o.limit == limit;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + this.limit;
            hash = 71 * hash + (this.metrics != null ? this.metrics.hashCode() : 0);
            return hash;
        }
    }

    private class HotSpotFunctionsFetcher
            implements Computable<HotSpotFunctionsFetcherParams, List<FunctionCallWithMetric>> {

        private final DecimalFormat df = new DecimalFormat();

        public HotSpotFunctionsFetcher() {
            DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(symbols);
        }

        @Override
        public List<FunctionCallWithMetric> compute(HotSpotFunctionsFetcherParams taskArguments) throws InterruptedException {
            if (log.isLoggable(Level.FINEST)) {
                log.log(Level.FINEST, "Started to fetch Hot Spot Functions @ {0}", Thread.currentThread()); // NOI18N
            }

            Metrics metrics = taskArguments.metrics;

            String[] er_result = null;

            try {
                er_result = storage.getTopFunctions(taskArguments.command, metrics, taskArguments.limit);
            } catch (InterruptedException ex) {
                log.log(Level.FINEST, "Fetching Interrupted! Hot Spot Functions @ {0}", Thread.currentThread()); // NOI18N
                return null;
            }

            int limit = er_result == null || er_result.length == 0 ? 0 : Math.min(er_result.length, taskArguments.limit);
            ArrayList<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>(limit);

            Column primarySortColumn = taskArguments.orderBy.get(0);

            for (int i = 0; i < limit; i++) {
                int lineNumber = -1;
                String fileName = null;

                // name is ALWAYS the last column (see HotSpotFunctionsFetcherParams)
                // Splitting output string on nameIdx pieces

                String[] info = er_result[i].split("[ \t]+", taskArguments.nameIdx + 1); // NOI18N
                String name = info[taskArguments.nameIdx];

                if (!taskArguments.isDefaultCommand()) {
                    //parse
                    if (ErprintCommand.lines().equals(taskArguments.command)) { // NOI18N
                        //if name.startsWith< will skip
                        Matcher match;

                        match = fullInfoPattern.matcher(name);
                        if (match.matches()) {
                            name = match.group(1);
                            lineNumber = Integer.valueOf(match.group(2));
                            fileName = match.group(3);
                        } else {
                            if (filter != null && filter.getType() == HotSpotFunctionsFilter.CollectedDataType.WITHSOURCECODEONLY) {
                                continue;
                            }
                            match = noLineInfoPattern.matcher(name);
                            if (match.matches()) {
                                name = match.group(1);
                                fileName = match.group(2);
                            } else {
                                match = noDebugInfoPattern.matcher(name);
                                if (match.matches()) {
                                    name = match.group(1);
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                }

                Address address = Address.parse(info[taskArguments.addressIdx] + info[taskArguments.addressIdx + 1]);
                FunctionImpl f = new FunctionImpl(name, address == null ? name.hashCode() : address.getAddress());

                Map<FunctionMetric, Object> metricsValues =
                        new HashMap<FunctionMetric, Object>();

                // Will skip function if value of primary sorting metric == 0
                boolean skipFunction = false;

                // Returned result is not, actually what was requested..
                // need to return what was requested.

                for (int midx = 0; midx < taskArguments.requestColumns.size(); midx++) {
                    Column col = taskArguments.requestColumns.get(midx);

                    if (col.equals(SunStudioDCConfiguration.c_name)) {
                        continue;
                    }

                    String colName = col.getColumnName();
                    Class<?> colClass = col.getColumnClass();
                    FunctionMetric metric = getMetricInstance(colName);
                    boolean isPrimaryColumn = col.equals(primarySortColumn);

                    String val = info[taskArguments.columnsIdxRef[midx]];
                    Object metricValue = val;

                    try {
                        Number nvalue = df.parse(val);
                        if (Integer.class == colClass) {
                            if (isPrimaryColumn && nvalue.intValue() == 0) {
                                skipFunction = true;
                            }
                            metricValue = Integer.valueOf(nvalue.intValue());
                        } else if (Double.class == colClass) {
                            if (isPrimaryColumn && nvalue.doubleValue() == 0) {
                                skipFunction = true;
                            }
                            metricValue = Double.valueOf(nvalue.doubleValue());
                        } else if (Float.class == colClass) {
                            if (isPrimaryColumn && nvalue.floatValue() == 0) {
                                skipFunction = true;
                            }
                            metricValue = Float.valueOf(nvalue.floatValue());
                        } else if (Long.class == colClass) {
                            if (isPrimaryColumn && nvalue.longValue() == 0) {
                                skipFunction = true;
                            }
                            metricValue = Long.valueOf(nvalue.longValue());
                        }

                    } catch (ParseException ex) {
                        // use plain info[midx]
                    }
                    metricsValues.put(metric, metricValue);
                }

                if (!skipFunction) {
                    FunctionCallImpl fc = new FunctionCallImpl(f, lineNumber, metricsValues);
                    if (fileName != null) {
                        fc.setSourceFileInfo(new SourceFileInfo(fileName, lineNumber, 0));
                    }
                    result.add(fc);
                }
            }



            Column ompPrimarySortColumn = null;
            List<Column> ompColumns = null;
            int omp_limit = Integer.MAX_VALUE;
            if (ompSupport && storage.hasOMPCollected()) {
                //add additional results
                if (metrics.getMspec().indexOf(SunStudioDCConfiguration.c_eSync.getColumnName()) != -1 && metrics.getMsort().indexOf(SunStudioDCConfiguration.c_eSync.getColumnName()) != -1) {
                    //add i.ompwait
                    ompPrimarySortColumn = SunStudioDCConfiguration.c_iOMPWait;
                    ompColumns = Arrays.asList(SunStudioDCConfiguration.c_iOMPWait, SunStudioDCConfiguration.c_name);
                }
                if (metrics.getMspec().indexOf(SunStudioDCConfiguration.c_eUser.getColumnName()) != -1 && metrics.getMsort().indexOf(SunStudioDCConfiguration.c_eUser.getColumnName()) != -1) {
                    //add i.ompwait
                    ompPrimarySortColumn = SunStudioDCConfiguration.c_iOMPWork;
                    ompColumns = Arrays.asList(SunStudioDCConfiguration.c_iOMPWork, SunStudioDCConfiguration.c_name);
                }

                String[] omp_er_result = null;
                HotSpotFunctionsFetcherParams ompTaskArguments = null;
                try {
                    if (ompColumns != null) {
                        ompTaskArguments =
                                new HotSpotFunctionsFetcherParams(taskArguments.command, ompColumns, Arrays.asList(ompPrimarySortColumn), omp_limit, filter);
                        //add additional results
                        omp_er_result =
                                storage.getTopFunctions(ompTaskArguments.command,
                                ompTaskArguments.metrics,
                                omp_limit);

                    }

                } catch (InterruptedException ex) {
                    log.log(Level.FINEST, "Fetching Interrupted! Hot Spot Functions @ {0}", Thread.currentThread()); // NOI18N
                }

                omp_limit = omp_er_result == null || omp_er_result.length == 0 ? 0 : omp_er_result.length;
                for (int i = 0; i < omp_limit; i++) {
                    int lineNumber = -1;
                    String fileName = null;

                    // name is ALWAYS the last column (see HotSpotFunctionsFetcherParams)
                    // Splitting output string on nameIdx pieces

                    String[] info = omp_er_result[i].split("[ \t]+", ompTaskArguments.nameIdx + 1); // NOI18N
                    String name = info[ompTaskArguments.nameIdx];

                    if (!taskArguments.isDefaultCommand()) {
                        //parse
                        if (ErprintCommand.lines().equals(ompTaskArguments.command)) { // NOI18N
                            //if name.startsWith< will skip
                            Matcher match;

                            match = fullInfoPattern.matcher(name);
                            if (match.matches()) {
                                name = match.group(1);
                                lineNumber = Integer.valueOf(match.group(2));
                                fileName = match.group(3);
                            } else {
                                if (filter != null && filter.getType() == HotSpotFunctionsFilter.CollectedDataType.WITHSOURCECODEONLY) {
                                    continue;
                                }
                                match = noLineInfoPattern.matcher(name);
                                if (match.matches()) {
                                    name = match.group(1);
                                    fileName = match.group(2);
                                } else {
                                    match = noDebugInfoPattern.matcher(name);
                                    if (match.matches()) {
                                        name = match.group(1);
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    Address address = Address.parse(info[ompTaskArguments.addressIdx] + info[ompTaskArguments.addressIdx + 1]);
                    FunctionImpl f = new FunctionImpl(name, address == null ? name.hashCode() : address.getAddress());

                    Map<FunctionMetric, Object> metricsValues =
                            new HashMap<FunctionMetric, Object>();

                    // Will skip function if value of primary sorting metric == 0
                    boolean skipFunction = false;

                    // Returned result is not, actually what was requested..
                    // need to return what was requested.

                    for (int midx = 0; midx < ompTaskArguments.requestColumns.size(); midx++) {
                        Column col = ompTaskArguments.requestColumns.get(midx);
                        if (col.equals(SunStudioDCConfiguration.c_name)) {
                            continue;
                        }

                        String colName = col.getColumnName();
                        Class<?> colClass = col.getColumnClass();
                        FunctionMetric metric = getMetricInstance(colName);
                        if (SunStudioDCConfiguration.c_iOMPWait.getColumnName().equals(colName)) {
                            metric = getMetricInstance(SunStudioDCConfiguration.c_eSync.getColumnName());
                        } else if (SunStudioDCConfiguration.c_iOMPWork.getColumnName().equals(colName)) {
                            metric = getMetricInstance(SunStudioDCConfiguration.c_eUser.getColumnName());
                        }

                        boolean isPrimaryColumn = col.equals(ompPrimarySortColumn);

                        String val = info[ompTaskArguments.columnsIdxRef[midx]];
                        Object metricValue = val;

                        try {
                            Number nvalue = df.parse(val);
                            if (Integer.class == colClass) {
                                if (isPrimaryColumn && nvalue.intValue() == 0) {
                                    skipFunction = true;
                                }
                                metricValue = Integer.valueOf(nvalue.intValue());
                            } else if (Double.class == colClass) {
                                if (isPrimaryColumn && nvalue.doubleValue() == 0) {
                                    skipFunction = true;
                                }
                                metricValue = Double.valueOf(nvalue.doubleValue());
                            } else if (Float.class == colClass) {
                                if (isPrimaryColumn && nvalue.floatValue() == 0) {
                                    skipFunction = true;
                                }
                                metricValue = Float.valueOf(nvalue.floatValue());
                            } else if (Long.class == colClass) {
                                if (isPrimaryColumn && nvalue.longValue() == 0) {
                                    skipFunction = true;
                                }
                                metricValue = Long.valueOf(nvalue.longValue());
                            }

                        } catch (ParseException ex) {
                            // use plain info[midx]
                        }
                        metricsValues.put(metric, metricValue);
                    }

                    if (!skipFunction) {
                        FunctionCallImpl fc = new FunctionCallImpl(f, lineNumber, metricsValues);
                        if (fileName != null) {
                            fc.setSourceFileInfo(new SourceFileInfo(fileName, lineNumber, 0));
                        }
                        result.add(fc);
                    }
                }
            }
            log.fine("Done with Hot Spot Functions fetching"); // NOI18N

            return result;
        }
    }
}
