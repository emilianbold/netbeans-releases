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
package org.netbeans.modules.dlight.cpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionDatatableDescription;
import org.netbeans.modules.dlight.core.stack.datacollector.CpuSamplingSupport;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.tools.ProcDataProviderConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.indicators.graph.DataRowToPlot;
import org.netbeans.modules.dlight.indicators.PlotIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.graph.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.graph.GraphConfig;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.ColumnsUIMapping;
import org.netbeans.modules.dlight.visualizers.api.FunctionsListViewVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public final class DLightCPUToolConfigurationProvider
        implements DLightToolConfigurationProvider {

    public static final int INDICATOR_POSITION = 100;
    public static final String ID = "dlight.tool.cpu"; // NOI18N
    private static final String TOOL_NAME = loc("CPUMonitorTool.ToolName"); // NOI18N
    private static final String DETAILED_TOOL_NAME = loc("CPUMonitorTool.DetailedToolName"); // NOI18N
    private static final boolean CPU_TREE_TABLE = Boolean.valueOf(System.getProperty("cpu.tree.table", "false"));
    private static final String TIME_DETAIL_ID = "elapsed-time"; // NOI18N
    private static final int SECONDS_PER_MINUTE = 60;

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(ID, TOOL_NAME);
        toolConfiguration.setLongName(DETAILED_TOOL_NAME);
        toolConfiguration.setIcon("org/netbeans/modules/dlight/cpu/resources/cpu.png"); // NOI18N

        // SunStudio should collect data about most CPU-expensive functions
        // i.e. create a configuration that collects
        // CollectedInfo.FUNCTIONS_LIST
        SunStudioDCConfiguration ssCollectorConfig =
                new SunStudioDCConfiguration(CollectedInfo.FUNCTIONS_LIST);

        // This tool will use DataCollector with this configuration...
        toolConfiguration.addDataCollectorConfiguration(ssCollectorConfig);

        // Now configure what happens when user clicks on the monitor....
        // We should display Detailed view (using CallersCalleesVisualizer)
        // with (some of) data that SunStudio collects when it configured
        // with CollectedInfo.FUNCTIONS_LIST

        DataTableMetadata detailedViewTableMetadataSS =
                SunStudioDCConfiguration.getCPUTableMetadata(
                SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_iUser,
                SunStudioDCConfiguration.c_eUser);
        ColumnsUIMapping columnsUIMapping = new ColumnsUIMapping();
        columnsUIMapping.setDisplayedName(SunStudioDCConfiguration.c_name.getColumnName(), loc("CPUMonitorTool.ColumnName.func_name")); // NOI18N
        columnsUIMapping.setColumnUI(SunStudioDCConfiguration.c_iUser.getColumnName(), loc("CPUMonitorTool.ColumnName.time_incl"), loc("CPUMonitorTool.ColumnTooltip.time_incl")); // NOI18N
        columnsUIMapping.setColumnUI(SunStudioDCConfiguration.c_eUser.getColumnName(), loc("CPUMonitorTool.ColumnName.time_excl"), loc("CPUMonitorTool.ColumnTooltip.time_excl")); // NOI18N

        // Register configured detailed view to be opened on indicator click...
        VisualizerConfiguration detailsVisualizerConfigSS;
        if (CPU_TREE_TABLE) {
            detailsVisualizerConfigSS =
                    new CallersCalleesVisualizerConfiguration(
                    detailedViewTableMetadataSS,
                    "name", // NOI18N
                    false);
            ((CallersCalleesVisualizerConfiguration) detailsVisualizerConfigSS).setColumnsUIMapping(columnsUIMapping);
        } else {
            FunctionDatatableDescription funcDescription = new FunctionDatatableDescription(SunStudioDCConfiguration.c_name.getColumnName(), null, SunStudioDCConfiguration.c_name.getColumnName());
            detailsVisualizerConfigSS = new FunctionsListViewVisualizerConfiguration(detailedViewTableMetadataSS, funcDescription, Arrays.asList(SunStudioDCConfiguration.c_eUser, SunStudioDCConfiguration.c_iUser));
            ((FunctionsListViewVisualizerConfiguration) detailsVisualizerConfigSS).setColumnsUIMapping(columnsUIMapping);
            ((FunctionsListViewVisualizerConfiguration) detailsVisualizerConfigSS).setEmptyAnalyzeMessage(loc("DetailedView.EmptyAnalyzeMessage"));//NOI18N
            ((FunctionsListViewVisualizerConfiguration) detailsVisualizerConfigSS).setEmptyRunningMessage(loc("DetailedView.EmptyRunningMessage"));//NOI18N
        }

        DTDCConfiguration dtraceDataCollectorConfiguration =
                DTDCConfiguration.createCpuSamplingConfiguration();

        toolConfiguration.addDataCollectorConfiguration(
                dtraceDataCollectorConfiguration);

//        DataTableMetadata detailedViewTableMetadataDtrace =
//            createFunctionsListMetadata(profilerTableMetadata);
        // Register configured detailed view to be opened on indicator click...
//        CallersCalleesVisualizerConfiguration detailsVisualizerConfigDtrace =
//            new CallersCalleesVisualizerConfiguration(
//            detailedViewTableMetadataDtrace,
//            "name", // NOI18N
//            true);
        VisualizerConfiguration detailsVisualizerConfigDtrace = createDTraceBasedVisualizerConfiguration(CpuSamplingSupport.CPU_SAMPLE_TABLE);

        ProcDataProviderConfiguration indicatorProviderConfiguration = new ProcDataProviderConfiguration();
        toolConfiguration.addIndicatorDataProviderConfiguration(indicatorProviderConfiguration);

        List<Column> resultColumns = new ArrayList<Column>();
        resultColumns.add(ProcDataProviderConfiguration.USR_TIME);
        resultColumns.add(ProcDataProviderConfiguration.SYS_TIME);
        IndicatorMetadata indicatorMetadata = new IndicatorMetadata(resultColumns);
        PlotIndicatorConfiguration indicatorConfiguration = new PlotIndicatorConfiguration(
                indicatorMetadata, INDICATOR_POSITION,
                loc("indicator.title"), 100, // NOI18N
                Arrays.<GraphDescriptor>asList(
                new GraphDescriptor(GraphConfig.COLOR_3, loc("graph.description.system"), GraphDescriptor.Kind.REL_SURFACE), // NOI18N
                new GraphDescriptor(GraphConfig.COLOR_1, loc("graph.description.user"), GraphDescriptor.Kind.REL_SURFACE)), // NOI18N
                new DataRowToCpuPlot(
                Arrays.asList(ProcDataProviderConfiguration.USR_TIME),
                Arrays.asList(ProcDataProviderConfiguration.SYS_TIME)));
        indicatorConfiguration.setActionDisplayName(loc("indicator.action")); // NOI18N
        indicatorConfiguration.setDetailDescriptors(Arrays.asList(
                new DetailDescriptor(TIME_DETAIL_ID, loc("detail.time"), formatTime(0)))); // NOI18N

        indicatorConfiguration.addVisualizerConfiguration(detailsVisualizerConfigDtrace);
        indicatorConfiguration.addVisualizerConfiguration(detailsVisualizerConfigSS);
        toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        return toolConfiguration;
    }

    private VisualizerConfiguration createDTraceBasedVisualizerConfiguration(DataTableMetadata profilerTableMetadata) {
        List<Column> columns = new ArrayList<Column>();
        List<Column> metrics = new ArrayList<Column>();
        Column nameColumn = new Column("name", String.class, loc("CPUMonitorTool.ColumnName.name"), null);//NOI18N
        columns.add(nameColumn);

        //  columns.add(new Column("name_qualified", String.class, loc("CPUMonitorTool.ColumnName.name_qualified"), null)); // NOI18N

        List<FunctionMetric> metricsList = StackDataStorage.METRICS;
        ColumnsUIMapping columnsUIMapping = new ColumnsUIMapping();

        for (FunctionMetric metric : metricsList) {
            String metricID = metric.getMetricID();
            String displayedName = locMetricDisplayedName(metricID);
            String metricTooltip = locMetricTooltip(metricID);
            if (displayedName != null) {
                columnsUIMapping.setDisplayedName(metricID, displayedName);
            }
            if (metricTooltip != null) {
                columnsUIMapping.setTooltip(metricID, metricTooltip);
            }
            Column metricColumn = new Column(
                    metricID,
                    metric.getMetricValueClass(),
                    metric.getMetricDisplayedName(), null);
            columns.add(metricColumn);
            metrics.add(metricColumn);
        }

        DataTableMetadata result = new DataTableMetadata(
                StackDataStorage.STACK_METADATA_VIEW_NAME,
                columns, null, Arrays.asList(profilerTableMetadata));
        if (CPU_TREE_TABLE) {
            CallersCalleesVisualizerConfiguration configuration =
                    new CallersCalleesVisualizerConfiguration(
                    result,
                    "name", // NOI18N
                    false);
            configuration.setColumnsUIMapping(columnsUIMapping);
            return configuration;
        } else {
            FunctionDatatableDescription funcDescription = new FunctionDatatableDescription(nameColumn.getColumnName(), null, nameColumn.getColumnName());
            FunctionsListViewVisualizerConfiguration configuration = new FunctionsListViewVisualizerConfiguration(result, funcDescription, metrics);
            configuration.setEmptyAnalyzeMessage(loc("DetailedView.EmptyAnalyzeMessage"));//NOI18N
            configuration.setEmptyRunningMessage(loc("DetailedView.EmptyRunningMessage"));//NOI18N
            configuration.setColumnsUIMapping(columnsUIMapping);
            return configuration;
        }
    }

    private static String locMetricDisplayedName(String metricID) {
        try {
            return NbBundle.getMessage(DLightCPUToolConfigurationProvider.class, "FunctionMetric." + metricID);//NOI18N
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private static String locMetricTooltip(String metricID) {
        try {
            return NbBundle.getMessage(DLightCPUToolConfigurationProvider.class, "FunctionMetric.tooltip." + metricID);//NOI18N
        } catch (MissingResourceException e) {
            return null;
        }

    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                DLightCPUToolConfigurationProvider.class, key, params);
    }

    private static class DataRowToCpuPlot implements DataRowToPlot {

        private final List<Column> usrColumns;
        private final List<Column> sysColumns;
        private int seconds;
        private int usr;
        private int sys;

        public DataRowToCpuPlot(List<Column> usrColumns, List<Column> sysColumns) {
            this.usrColumns = new ArrayList<Column>(usrColumns);
            this.sysColumns = new ArrayList<Column>(sysColumns);
        }

        public void addDataRow(DataRow row) {
            for (String columnName : row.getColumnNames()) {
                for (Column usrColumn : usrColumns) {
                    if (usrColumn.getColumnName().equals(columnName)) {
                        usr = DataUtil.toInt(row.getData(columnName));
                    }
                }
                for (Column sysColumn : sysColumns) {
                    if (sysColumn.getColumnName().equals(columnName)) {
                        sys = DataUtil.toInt(row.getData(columnName));
                    }
                }
            }
        }

        public void tick(float[] data, Map<String, String> details) {
            ++seconds;
            data[0] = sys;
            data[1] = usr;
            details.put(TIME_DETAIL_ID, formatTime(seconds));
        }
    }

    private static String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE); // NOI18N
    }
}
