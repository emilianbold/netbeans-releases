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
package org.netbeans.modules.dlight.visualizers.api;

import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;

/**
 * Configuration class which will be used to create Callers/Callees visualizer.
 * Callers/Callees visualizer is Tree Table View which contains functions list
 * along with metrics and with the posibility to shift View  from Callers View  to Callees View,
 *  refresh all data and focus on some selected function.
 */
public final class CallersCalleesVisualizerConfiguration extends TreeTableVisualizerConfiguration {

//    /**
//     * Creates new configuration to create Callers/Callees Visualizer
//     * @param metrics
//     * @param isPlainTable
//     */
//    private CallersCalleesVisualizerConfiguration(List<FunctionMetric> metrics, boolean isPlainTable) {
//        List<Column> columns = new ArrayList<Column>();
//        columns.add(new Column("name", String.class, "Function Name", null));
//        for (FunctionMetric metric : metrics) {
//            columns.add(new Column(metric.getMetricID(), metric.getMetricValueClass(), metric.getMetricDisplayedName(), null));
//        }
//        DataTableMetadata result = new DataTableMetadata("DtraceStacks", columns);
//        setDataTableMetadata(result, "name");
//    }
//
//    /**
//     *
//     * @param metrics
//     */
//    private CallersCalleesVisualizerConfiguration(List<FunctionMetric> metrics) {
//        this(metrics, false);
//
//    }

    /**
     * Creates new Callers/Callees Visualizer configuration using <code>dataTableMetadat</code> as function tables list and
     * <code>functionNameColumnName</code> as name of the column which represents function name and will be used
     * in Tree Table View as tree column
     * @param dataTableMetadata functions table
     * @param functionNameColumnName name of the column which represents function name and will be used
     */
    public CallersCalleesVisualizerConfiguration(DataTableMetadata dataTableMetadata, String functionNameColumnName) {
        super(dataTableMetadata, functionNameColumnName);
    }

    /**
     * Creates new Callers/Callees Visualizer configuration using <code>dataTableMetadat</code> as function tables list and
     * <code>functionNameColumnName</code> as name of the column which represents function name and will be used
     * in Tree Table View as tree column
     * @param dataTableMetadata functions table
     * @param functionNameColumnName name of the column which represents function name and will be used
     * @param isTableView  <code>true</code> if you would liek to see plain table, <code>false</code> otherwise
     */
    public CallersCalleesVisualizerConfiguration(DataTableMetadata dataTableMetadata, String functionNameColumnName, boolean isTableView) {
        super(dataTableMetadata, functionNameColumnName, isTableView);
    }

    @Override
    public String getID() {
        return VisualizerConfigurationIDsProvider.CALLERS_CALLEES_VISUALIZER;
    }

    @Override
    public DataModelScheme getSupportedDataScheme() {
        return DataModelSchemeProvider.getInstance().getScheme("model:stack");//NOI18N
    }
}
