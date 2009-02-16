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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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

class SSStackDataProvider implements StackDataProvider {

    private static final Logger log = DLightLogger.getLogger(SSStackDataProvider.class);
    int[] index = new int[]{1, 2, 3, 4};
    private List<FunctionMetric> metricsList = Arrays.asList(
            TimeMetric.UserFuncTimeExclusive,
            TimeMetric.UserFuncTimeInclusive,
            TimeMetric.SyncWaitCallInclusive,
            TimeMetric.SyncWaitTimeInclusive);
    private PerfanDataStorage storage;
    private static CollectionToStringConvertor<Column> convertor;
    private List<Column> columns;
    private String mspec;
    private int nameColumnIdx;

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

    private synchronized List<FunctionCall> getCallersCallees(CC_MODE mode, FunctionCall[] path, boolean aggregate) {
        //TODO: Now just take the last from the path...
        FunctionCall parent_fc = path[path.length - 1];

        Object[] raw_data = mode == CC_MODE.CALLEES ? storage.getCallees(((FunctionImpl) parent_fc.getFunction()).getRef()) : storage.getCallers(((FunctionImpl) parent_fc.getFunction()).getRef());

        if (raw_data == null || raw_data.length == 0) {
            return Collections.emptyList();
        }

        SortedSet<FunctionCall> result = rawDataToResult(raw_data, NaturalFunctionCallComparator.getInstance(TimeMetric.UserFuncTimeInclusive));

        List<FunctionCall> callees = new ArrayList<FunctionCall>(10);
        Iterator<FunctionCall> it = result.iterator();
        while (it.hasNext()) {
            FunctionCall fc = it.next();
            callees.add(fc);
        }

        return callees;
    }

    private void setMSpec(List<Column> columns) {
        if (this.columns == columns) {
            return;
        }

        this.columns = columns;
        if (convertor == null) {
            convertor = new CollectionToStringConvertor<Column>(":", new CollectionToStringConvertor.Convertor<Column>() {

                public String itemToString(Column item) {
                    return item.getColumnName();
                }
            });
        }

        mspec = convertor.collectionToString(columns);
        nameColumnIdx = -1;

        for (int i = 0; i < columns.size(); i++) {
            if (nameColumnIdx < 0 && columns.get(i).getColumnName().equals("name")) { // NOI18N
                nameColumnIdx = i;
                break;
            }
        }

        assert nameColumnIdx >= 0;

    }

    public synchronized List<FunctionCall> getHotSpotFunctions(List<Column> columns, List<Column> orderBy, int limit) {
        // TODO: re-design
        setMSpec(columns);

        String msort = convertor.collectionToString(orderBy);
        if ("".equals(msort)) {
            msort = "i.user";
        }

        String[] er_result = storage.getTopFunctions(mspec, msort, limit);

        if (er_result == null || er_result.length == 0) {
            return Collections.emptyList();
        }

        ArrayList<FunctionCall> result = new ArrayList<FunctionCall>(limit);

        System.out.println("-------------");
        
        for (int i = 0; i < limit; i++) {
            if (i >= er_result.length) {
                break;
            }

            String[] info = er_result[i].split("[ \t]+");
            String name = info[nameColumnIdx + 1];
            Function f = new FunctionImpl(name, name.hashCode());
            System.out.println("Function: " + f.getName());
            


        }

        result.trimToSize();

        return result;


//    if (raw_data == null || raw_data.length == 0) {
//      return Collections.emptyList();
//    }
//
//    SortedSet<FunctionCall> result = rawDataToResult(raw_data, NaturalFunctionCallComparator.getInstance(TimeMetric.UserFuncTimeInclusive));
//
//    List<FunctionCall> topCalls = new ArrayList<FunctionCall>(10);
//    int idx = 0;
//    Iterator<FunctionCall> it = result.iterator();
//    while (it.hasNext() && idx < limit) {
//      FunctionCall fc = it.next();
//      topCalls.add(fc);
//      idx++;
//    }
//
//    return topCalls;
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
            throw new IllegalArgumentException("Attempt to attach SSStackDataProvider to storage '" + storage + "'");
        }
    }

    private synchronized SortedSet<FunctionCall> rawDataToResult(Object[] raw_data, NaturalFunctionCallComparator comparator) {
        SortedSet<FunctionCall> result = new TreeSet<FunctionCall>(comparator);

        String[] names = (String[]) raw_data[nameColumnIdx];
        long[] objRefs = (long[]) raw_data[raw_data.length - 1]; // last line is always objRefs

        int records = names.length;

        for (int i = 0; i < records; i++) {
            Function f = new FunctionImpl(names[i], objRefs[i]);
            Map<FunctionMetric, Object> metrics = new HashMap<FunctionMetric, Object>();

            for (int midx = 0; midx < raw_data.length - 1; midx++) {
                if (midx == nameColumnIdx) {
                    continue;
                }

                String[] columnData = (String[]) raw_data[midx];
                FunctionMetric metric = getMetricInstance(columns.get(midx).getColumnName());
                String svalue = columnData[i].replaceAll(",", ".");

                metrics.put(metric, Double.parseDouble(svalue));
            }

            FunctionCallImpl fc = new FunctionCallImpl(f, metrics);
            result.add(fc);
        }

        return result;
    }
}
