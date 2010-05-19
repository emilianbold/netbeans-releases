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
package org.netbeans.modules.dlight.memory;

import java.awt.Color;
import java.beans.FeatureDescriptor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.util.ValueFormatter;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.tools.LLDataCollectorConfiguration;
import org.netbeans.modules.dlight.util.BytesFormatter;
import org.netbeans.modules.dlight.visualizers.api.ColumnsUIMapping;
import org.netbeans.modules.dlight.visualizers.api.FunctionName;
import org.netbeans.modules.dlight.visualizers.api.FunctionsListViewVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public final class MemoryToolConfigurationProvider implements DLightToolConfigurationProvider {

    private static final int INDICATOR_POSITION = 200;
    private static final String ID = "dlight.tool.mem"; // NOI18N
    private static final String TOOL_NAME = loc("MemoryTool.ToolName"); // NOI18N
    private static final String TOOL_NAME_DETAILED = loc("MemoryTool.ToolName.Detailed"); // NOI18N
    private static final Column totalColumn;
    private static final DataTableMetadata rawTableMetadata;
    private static final String MAX_HEAP_DETAIL_ID = "max-heap"; // NOI18N
//    /** this is for the case of using DTrace for indicator only  */


    static {
        final Column timestampColumn = new Column("timestamp", Time.class, loc("MemoryTool.ColumnName.timestamp"), null); // NOI18N
        final Column kindColumn = new Column("kind", Integer.class, loc("MemoryTool.ColumnName.kind"), null); // NOI18N
        final Column sizeColumn = new Column("size", Integer.class, loc("MemoryTool.ColumnName.size"), null); // NOI18N
        final Column addressColumn = new Column("address", Long.class, loc("MemoryTool.ColumnName.address"), null); // NOI18N
        final Column stackColumn = new Column("stackid", Long.class, loc("MemoryTool.ColumnName.stackid"), null); // NOI18N

        totalColumn = new Column("total", Integer.class, loc("MemoryTool.ColumnName.total"), null); // NOI18N

        List<Column> columns = Arrays.asList(
                timestampColumn,
                kindColumn,
                sizeColumn,
                addressColumn,
                totalColumn,
                stackColumn);

        rawTableMetadata = new DataTableMetadata("mem", // NOI18N
                columns, Arrays.asList(timestampColumn, addressColumn));
    }

    public MemoryToolConfigurationProvider() {
    }

    @Override
    public DLightToolConfiguration create() {
        DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(ID, TOOL_NAME);
        toolConfiguration.setLongName(TOOL_NAME_DETAILED);
        toolConfiguration.setIcon("org/netbeans/modules/dlight/memory/resources/memory.png"); // NOI18N
        toolConfiguration.setDescription(loc("MemoryTool.Description"));//NOI18N
        FeatureDescriptor descriptor = new FeatureDescriptor();
        descriptor.setValue(DTDCConfiguration.DSCRIPT_TOOL_PROPERTY, getScriptUrl());
        toolConfiguration.setFeatureDescriptor(descriptor);
        DataCollectorConfiguration dcc = initSunStudioDataCollectorConfiguration();
        toolConfiguration.addDataCollectorConfiguration(dcc);
        DTDCConfiguration dtcc = initDtraceDataCollectorConfiguration();
        toolConfiguration.addDataCollectorConfiguration(dtcc);
        // it's an indicator data provider as well!
        toolConfiguration.addIndicatorDataProviderConfiguration(dtcc);
        toolConfiguration.addIndicatorDataProviderConfiguration(
                initDtraceIndicatorDataProviderConfiguration());
        toolConfiguration.addIndicatorDataProviderConfiguration(initSunStudioIndicatorDataProviderConfiguration());
        LLDataCollectorConfiguration lldcc = initLLDataCollectorConfiguration();
        toolConfiguration.addIndicatorDataProviderConfiguration(lldcc);
        toolConfiguration.addIndicatorConfiguration(initIndicatorConfiguration());

        return toolConfiguration;
    }

    private DataCollectorConfiguration initSunStudioDataCollectorConfiguration() {
        return new SunStudioDCConfiguration(SunStudioDCConfiguration.CollectedInfo.MEMORY);
    }

    private DTDCConfiguration initDtraceDataCollectorConfiguration() {

        DTDCConfiguration dataCollectorConfiguration =
                new DTDCConfiguration(getScriptUrl(), Arrays.asList(rawTableMetadata));

        dataCollectorConfiguration.setIndicatorFiringFactor(1);
        // DTDCConfiguration collectorConfiguration = new DtraceDataAndStackCollector(dataCollectorConfiguration);
        dataCollectorConfiguration.setStackSupportEnabled(true);
        dataCollectorConfiguration.setOutputPrefix("mem:"); // NOI18N

        return dataCollectorConfiguration;
    }

    private IndicatorDataProviderConfiguration initSunStudioIndicatorDataProviderConfiguration() {
        IndicatorDataProviderConfiguration result = new SunStudioDCConfiguration(CollectedInfo.MEMSUMMARY);
        return result;
    }

    private IndicatorDataProviderConfiguration initDtraceIndicatorDataProviderConfiguration() {

        DTDCConfiguration dataCollectorConfiguration =
                new DTDCConfiguration(getScriptUrl(), Arrays.asList(rawTableMetadata)); // indicatorTableMetadata
        dataCollectorConfiguration.setIndicatorFiringFactor(1);
        dataCollectorConfiguration.setScriptArgs(" -DNOSTACK"); // NOI18N
        dataCollectorConfiguration.setStackSupportEnabled(true); // true
        dataCollectorConfiguration.setOutputPrefix("mem:"); // NOI18N

        return dataCollectorConfiguration;

    }

    private URL getScriptUrl() {
        return getClass().getResource("resources/mem.d"); // NOI18N
    }

    private LLDataCollectorConfiguration initLLDataCollectorConfiguration() {

        LLDataCollectorConfiguration memIndicatorDataProvider =
                new LLDataCollectorConfiguration(LLDataCollectorConfiguration.CollectedData.MEM);

        return memIndicatorDataProvider;
    }

    private IndicatorConfiguration initIndicatorConfiguration() {
        IndicatorMetadata indicatorMetadata = null;
        List<Column> indicatorColumns = new ArrayList<Column>();
        indicatorColumns.add(SunStudioDCConfiguration.c_leakSize);
        indicatorColumns.addAll(LLDataCollectorConfiguration.MEM_TABLE.getColumns());
        indicatorColumns.addAll(Arrays.asList(totalColumn));
        indicatorMetadata = new IndicatorMetadata(indicatorColumns);

        ValueFormatter formatter = new BytesFormatter();
        TimeSeriesIndicatorConfiguration indicatorConfiguration = new TimeSeriesIndicatorConfiguration(
                indicatorMetadata, INDICATOR_POSITION);
        indicatorConfiguration.setPersistencePrefix("dlight_mem"); // NOI18N
        indicatorConfiguration.setTitle(loc("indicator.title")); // NOI18N
        indicatorConfiguration.setGraphScale(1024);
        indicatorConfiguration.addTimeSeriesDescriptors(
                new TimeSeriesDescriptor("mem", loc("graph.description"), new Color(0x53, 0x82, 0xA1), TimeSeriesDescriptor.Kind.LINE)); // NOI18N
        indicatorConfiguration.setDataRowHandler(new DataRowToMemory(indicatorColumns, formatter));
        indicatorConfiguration.addDetailDescriptors(
                new DetailDescriptor(MAX_HEAP_DETAIL_ID, loc("MemoryTool.Legend.Max"), formatter.format(0))); // NOI18N
        indicatorConfiguration.setActionDisplayName(loc("indicator.action")); // NOI18N
        indicatorConfiguration.setActionTooltip(loc("indicator.action.tooltip"));//NOI18N
        indicatorConfiguration.setLabelFormatter(formatter);

        DataTableMetadata detailedViewTableMetadata =
                SunStudioDCConfiguration.getMemTableMetadata(
                SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_leakSize,
                SunStudioDCConfiguration.c_leakCount);

        ColumnsUIMapping columnsUIMapping = new ColumnsUIMapping();
        columnsUIMapping.setColumnUI(SunStudioDCConfiguration.c_name.getColumnName(), loc("MemoryTool.ColumnName.func_name"), loc("MemoryTool.ColumnTooltip.func_name")); // NOI18N
        columnsUIMapping.setColumnUI(SunStudioDCConfiguration.c_leakSize.getColumnName(), loc("MemoryTool.ColumnName.leak"), loc("MemoryTool.ColumnTooltip.leak")); // NOI18N
        FunctionDatatableDescription functionDesc = new FunctionDatatableDescription(SunStudioDCConfiguration.c_name.getColumnName(), null, SunStudioDCConfiguration.c_name.getColumnName());
        FunctionsListViewVisualizerConfiguration tableVisualizerConfiguration =
                new FunctionsListViewVisualizerConfiguration(detailedViewTableMetadata, functionDesc, Arrays.asList(SunStudioDCConfiguration.c_leakSize, SunStudioDCConfiguration.c_leakCount));
        tableVisualizerConfiguration.setColumnsUIMapping(columnsUIMapping);
        tableVisualizerConfiguration.setEmptyAnalyzeMessage(loc("DetailedView.EmptyAnalyzeMessage"));//NOI18N
        tableVisualizerConfiguration.setEmptyRunningMessage(loc("DetailedView.EmptyRunningMessage"));//NOI18N
        indicatorConfiguration.addVisualizerConfiguration(tableVisualizerConfiguration);

        indicatorConfiguration.addVisualizerConfiguration(getDetails(rawTableMetadata));
        return indicatorConfiguration;
    }

    private VisualizerConfiguration getDetails(DataTableMetadata rawTableMetadata) {
        Column metricColumn = new Column("leak", Long.class, loc("MemoryTool.ColumnName.leak"), loc("MemoryTool.ColumnTooltip.leak"), null); // NOI18N
        List<Column> viewColumns = Arrays.asList(
                new Column("func_name", FunctionName.class, loc("MemoryTool.ColumnName.func_name"), loc("MemoryTool.ColumnTooltip.func_name"), null), // NOI18N
                metricColumn);

        String sql =
                "SELECT func.func_id AS func_id, func.func_name AS func_name, node.offset AS offset, SUM(leak.size) AS leak " + // NOI18N
                "FROM (SELECT MAX(timestamp) AS timestamp, address, SUM(kind * size) AS size FROM mem GROUP BY address HAVING SUM(kind * size) > 0) AS leak " + // NOI18N
                "LEFT JOIN mem ON leak.timestamp = mem.timestamp AND leak.address = mem.address " + // NOI18N
                "LEFT JOIN node ON mem.stackid = node.node_id " + // NOI18N
                "LEFT JOIN func ON node.func_id = func.func_id " + // NOI18N
                "GROUP BY func_id, func_name, offset " + // NOI18N
                "ORDER BY leak DESC"; // NOI18N
//                "SELECT func.func_id as func_id, func.func_name as func_name, node.offset as offset, SUM(size) as leak " + // NOI18N
//                "FROM mem, node AS node, func, ( " + // NOI18N
//                "   SELECT MAX(timestamp) as leak_timestamp FROM mem, ( " + // NOI18N
//                "       SELECT address as leak_address, sum(kind*size) AS leak_size FROM mem GROUP BY address HAVING sum(kind*size) > 0 " + // NOI18N
//                "   ) AS vt1 WHERE address = leak_address GROUP BY address " + // NOI18N
//                ") AS vt2 WHERE timestamp = leak_timestamp " + // NOI18N
//                "AND stackid = node.node_id and node.func_id = func.func_id " + // NOI18N
//                " GROUP BY node.func_id, func.func_id, func.func_name, node.offset"; // NOI18N

        FunctionDatatableDescription functionDesc = new FunctionDatatableDescription("func_name", "offset", "func_id"); // NOI18N

        DataTableMetadata viewTableMetadata = new DataTableMetadata(
                "mem", viewColumns, sql, Arrays.asList(rawTableMetadata)); // NOI18N
        ColumnsUIMapping columnsUIMapping = new ColumnsUIMapping();
        columnsUIMapping.setColumnUI("func_name", loc("MemoryTool.ColumnName.func_name"), loc("MemoryTool.ColumnTooltip.func_name")); // NOI18N
        columnsUIMapping.setColumnUI(metricColumn.getColumnName(), loc("MemoryTool.ColumnName.leak"), loc("MemoryTool.ColumnTooltip.leak")); // NOI18N

        FunctionsListViewVisualizerConfiguration tableVisualizerConfiguration =
                new FunctionsListViewVisualizerConfiguration(viewTableMetadata, functionDesc, Arrays.asList(metricColumn));
        tableVisualizerConfiguration.setColumnsUIMapping(columnsUIMapping);
        tableVisualizerConfiguration.setEmptyAnalyzeMessage(loc("DetailedView.EmptyAnalyzeMessage"));//NOI18N
        tableVisualizerConfiguration.setEmptyRunningMessage(loc("DetailedView.EmptyRunningMessage"));//NOI18N
        return tableVisualizerConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                MemoryToolConfigurationProvider.class, key, params);
    }

    private static final class DataRowToMemory implements DataRowToTimeSeries {

        private final List<Column> columns;
        private final ValueFormatter formatter;
        private int max;

        public DataRowToMemory(List<Column> columns, ValueFormatter formatter) {
            this.columns = new ArrayList<Column>(columns);
            this.formatter = formatter;
        }

        @Override
        public float[] getData(DataRow row) {
            float[] result = null;
            for (String columnName : row.getColumnNames()) {
                for (Column column : columns) {
                    if (column.getColumnName().equals(columnName)) {
                        if (result == null) {
                            result = new float[1];
                        }
                        int mem = DataUtil.toInt(row.getData(columnName));
                        result[0] = mem;
                        if (max < mem) {
                            max = mem;
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public Map<String, String> getDetails() {
            return Collections.singletonMap(MAX_HEAP_DETAIL_ID, formatter.format(max));
        }
    }
}
