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
package org.netbeans.modules.dlight.perfan.dataprovider;

import org.netbeans.modules.dlight.perfan.util.TasksCachedProcessor;
import org.netbeans.modules.dlight.perfan.util.Computable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.core.stack.api.Function;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionImpl;
import org.netbeans.modules.dlight.perfan.storage.impl.PerfanDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.util.CollectionToStringConvertor;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;

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
class SSStackDataProvider implements StackDataProvider {

    private static final Logger log = DLightLogger.getLogger(SSStackDataProvider.class);
    private final Computable<HotSpotFunctionsFetcherParams, List<FunctionCall>> hotSpotFunctionsFetcher =
            new TasksCachedProcessor<HotSpotFunctionsFetcherParams, List<FunctionCall>>(new HotSpotFunctionsFetcher(), true);
    int[] index = new int[]{1, 2, 3, 4};
    private List<FunctionMetric> metricsList = Arrays.asList(
            TimeMetric.UserFuncTimeExclusive,
            TimeMetric.UserFuncTimeInclusive,
            TimeMetric.SyncWaitCallInclusive,
            TimeMetric.SyncWaitTimeInclusive);
    private PerfanDataStorage storage;

    private static enum CC_MODE {

        CALLEES,
        CALLERS
    };

    public SSStackDataProvider() {
    }

    public synchronized List<FunctionCall> getCallers(FunctionCall[] path, boolean aggregate) {
        return getCallersCallees(CC_MODE.CALLERS, path, aggregate);
    }

    public synchronized List<FunctionCall> getCallees(FunctionCall[] path, boolean aggregate) {
        return getCallersCallees(CC_MODE.CALLEES, path, aggregate);
    }

    public List<FunctionCallTreeTableNode> getTableView(List<Column> columns, List<Column> orderBy, int limit) {
        return FunctionCallTreeTableNode.getFunctionCallTreeTableNodes(getHotSpotFunctions(columns, orderBy, limit));
    }

    public List<FunctionCallTreeTableNode> getChildren(List<FunctionCallTreeTableNode> path) {
        return FunctionCallTreeTableNode.getFunctionCallTreeTableNodes(getCallers(FunctionCallTreeTableNode.getFunctionCalls(path).toArray(new FunctionCall[0]), false));
    }

    public FunctionCallTreeTableNode getValueAt(int row) {
        return null;
    }

    public String getTableValueAt(Column column, int row) {
        return null;
    }

    // TODO: implement
    private synchronized List<FunctionCall> getCallersCallees(CC_MODE mode, FunctionCall[] path, boolean aggregate) {
        //TODO: Now just take the last from the path...
//        FunctionCall parent_fc = path[path.length - 1];
//
//        Object[] raw_data = mode == CC_MODE.CALLEES ? storage.getCallees(((FunctionImpl) parent_fc.getFunction()).getRef()) : storage.getCallers(((FunctionImpl) parent_fc.getFunction()).getRef());
//
//        if (raw_data == null || raw_data.length == 0) {
//            return Collections.emptyList();
//        }
//
//        SortedSet<FunctionCall> result = rawDataToResult(raw_data, NaturalFunctionCallComparator.getInstance(TimeMetric.UserFuncTimeInclusive));
//
//        List<FunctionCall> callees = new ArrayList<FunctionCall>(10);
//        Iterator<FunctionCall> it = result.iterator();
//        while (it.hasNext()) {
//            FunctionCall fc = it.next();
//            callees.add(fc);
//        }
//
//        return callees;
        return Collections.emptyList();
    }

    public List<FunctionCall> getHotSpotFunctions(
            final List<Column> columns, final List<Column> orderBy, final int limit) {

        try {
            return hotSpotFunctionsFetcher.compute(new HotSpotFunctionsFetcherParams(columns, orderBy, limit));
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        return Collections.emptyList();
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

    public List<FunctionMetric> getMetricsList() {
        return metricsList;
    }

    public void attachTo(DataStorage storage) {
        if (storage instanceof PerfanDataStorage) {
            this.storage = (PerfanDataStorage) storage;
        } else {
            String msg = "Attempt to attach SSStackDataProvider to storage " +
                    "'" + storage + "'"; // NOI18N

            throw new IllegalArgumentException(msg);
        }
    }

    private static class HotSpotFunctionsFetcherParams {

        private final List<Column> columns;
        private final List<Column> orderBy;
        private final int limit;
        private final Metrics metrics;

        public HotSpotFunctionsFetcherParams(
                final List<Column> columns,
                final List<Column> orderBy,
                final int limit) {
            this.columns = columns;
            this.orderBy = orderBy;
            this.limit = limit;
            this.metrics = MetricsHandler.getMetrics(columns, orderBy);
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
            implements Computable<HotSpotFunctionsFetcherParams, List<FunctionCall>> {

        public List<FunctionCall> compute(HotSpotFunctionsFetcherParams taskArguments) throws InterruptedException {
            log.fine("Started to fetch Hot Spot Functions"); // NOI18N
            
            Metrics metrics = taskArguments.metrics;

            String[] er_result = storage.getTopFunctions(
                    metrics.mspec, metrics.msort, taskArguments.limit);

            if (er_result == null || er_result.length == 0) {
                return Collections.emptyList();
            }

            int limit = Math.min(er_result.length, taskArguments.limit);
            ArrayList<FunctionCall> result = new ArrayList<FunctionCall>(limit);

            for (int i = 0; i < limit; i++) {
                String[] info = er_result[i].split("[ \t]+"); // NOI18N
                String name = info[metrics.nameIdx];
                Function f = new FunctionImpl(name, name.hashCode());

                Map<FunctionMetric, Object> metricsValues =
                        new HashMap<FunctionMetric, Object>();

                for (int midx = 0; midx < info.length; midx++) {
                    if (midx == metrics.nameIdx) {
                        continue;
                    }

                    String columnData = info[midx];
                    FunctionMetric metric = getMetricInstance(taskArguments.columns.get(midx).getColumnName());
                    String svalue = columnData.replaceAll(",", "."); // NOI18N

                    metricsValues.put(metric, Double.parseDouble(svalue));
                }

                FunctionCallImpl fc = new FunctionCallImpl(f, metricsValues);
                result.add(fc);
            }

            log.fine("Done with Hot Spot Functions fetching"); // NOI18N

            return result;
        }
    }

    private static class Metrics {

        private final String mspec;
        private final String msort;
        private final int nameIdx;

        public Metrics(String mspec, String msort, int nameIdx) {
            this.mspec = mspec;
            this.msort = msort;
            this.nameIdx = nameIdx;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Metrics)) {
                throw new IllegalArgumentException();
            }
            Metrics o = (Metrics) obj;
            return o.msort.equals(msort) && o.mspec.equals(mspec);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.mspec != null ? this.mspec.hashCode() : 0);
            hash = 89 * hash + (this.msort != null ? this.msort.hashCode() : 0);
            return hash;
        }
    }

    private static class MetricsHandler {

        private final static CollectionToStringConvertor<Column> convertor;


        static {
            convertor = new CollectionToStringConvertor<Column>(":", // NOI18N
                    new CollectionToStringConvertor.Convertor<Column>() {

                public String itemToString(Column item) {
                    return item.getColumnName();
                }
            });
        }

        static Metrics getMetrics(List<Column> columns, List<Column> orderBy) {
            String mspecResult = convertor.collectionToString(columns);
            String msortResult = convertor.collectionToString(orderBy);

            if ("".equals(msortResult)) { // NOI18N
                msortResult = "i.user"; // NOI18N
            }

            int nameColumnIdx = -1;

            for (int i = 0; i < columns.size(); i++) {
                if (nameColumnIdx < 0 &&
                        columns.get(i).getColumnName().equals("name")) { // NOI18N
                    nameColumnIdx = i;
                    break;
                }
            }

            assert nameColumnIdx >= 0;
            return new Metrics(mspecResult, msortResult, nameColumnIdx);
        }
    }
}
