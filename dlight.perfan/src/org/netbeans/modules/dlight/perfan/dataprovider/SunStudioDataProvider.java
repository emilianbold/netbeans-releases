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
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionCallTreeTableNode;
import org.netbeans.modules.dlight.core.stack.api.FunctionCallWithMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.dataprovider.FunctionsListDataProvider;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.impl.SunStudioDCConfigurationAccessor;
import org.netbeans.modules.dlight.spi.impl.TableDataProvider;

/**
 *
 * @author mt154047
 */
final class SunStudioDataProvider extends SSStackDataProvider
        implements TableDataProvider, FunctionsListDataProvider {
    private static final String cpuTableName;
    
    static{
        cpuTableName = SunStudioDCConfigurationAccessor.getDefault().getCPUTableName();
    }

    SunStudioDataProvider() {
    }

    public List<DataRow> queryData(DataTableMetadata tableMetadata) {
        List<Column> columns = tableMetadata.getColumns();

        List<DataRow> result = new ArrayList<DataRow>();
        List<String> columnNames = new ArrayList<String>();

        for (Column c : columns) {
            columnNames.add(c.getColumnName());
        }

        List<FunctionCallTreeTableNode> nodes =
                super.getTableView(columns, null, Integer.MAX_VALUE);

        for (FunctionCallTreeTableNode node : nodes) {
            FunctionCallWithMetric call = node.getDeligator();
            List<Object> data = new ArrayList<Object>();
            for (Column c : columns) {
                if (c.getColumnName().equals("name")) { // NOI18N
                    data.add(call.getFunction().getName());
                    continue;
                }
                data.add(call.getMetricValue(c.getColumnName()));
            }

            result.add(new DataRow(columnNames, data));
        }
        return result;
    }

    public List<FunctionCallWithMetric> getFunctionsList(DataTableMetadata metadata,
            FunctionDatatableDescription functionDecsr, List<Column> metricsColumn) {
        //if we have CPU table here we should call functions command, not lines
        List<FunctionCallWithMetric> result = new ArrayList<FunctionCallWithMetric>();
        if (!metricsColumn.contains(SunStudioDCConfiguration.c_name)) {
            List<Column> oldMetrics = metricsColumn;
            metricsColumn = new ArrayList<Column>();
            metricsColumn.addAll(oldMetrics);
            metricsColumn.add(SunStudioDCConfiguration.c_name);
        }
        if (metadata.getName().equals(cpuTableName)){
            return super.getHotSpotFunctions(metricsColumn, metricsColumn, Integer.MAX_VALUE);
        }
        List<FunctionCallTreeTableNode> nodes = super.getTableView(metricsColumn, null, Integer.MAX_VALUE);
        for (FunctionCallTreeTableNode node : nodes) {
            FunctionCallWithMetric call = node.getDeligator();
            result.add(call);
        }

        return result;

    }
}
