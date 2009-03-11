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
import java.util.prefs.Preferences;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIOParser;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.storage.SQLStackStorage;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.cpu.impl.CpuIndicatorConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration.CollectedInfo;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.tools.LLDataCollectorConfiguration;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author mt154047
 */
public final class DLightCPUToolConfigurationProvider
    implements DLightToolConfigurationProvider {

    private static final String TOOL_NAME = loc("CPUMonitorTool.ToolName"); // NOI18N
//    private static final String PRSTAT_DTRACE = "prstat+dtrace"; // NOI18N
    private static final String SUNSTUDIO = "SunStudio"; // NOI18N
//    private static final String LLTOOL = "lltool"; // NOI18N
//    private static final String COLLECTOR =
//            System.getProperty("dlight.cpu.collector", PRSTAT_DTRACE); // NOI18N
    private static final List<Column> PRSTAT_COLUMNS = Arrays.asList(
        new Column("utime", Float.class, loc("CPUMonitorTool.ColumnName.utime"), null), // NOI18N
        new Column("stime", Float.class, loc("CPUMonitorTool.ColumnName.stime"), null), // NOI18N
        new Column("wtime", Float.class, loc("CPUMonitorTool.ColumnName.wtime"), null)); // NOI18N

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration =
            new DLightToolConfiguration(TOOL_NAME);
        String collector = "sunStudio";//preferences.get("DLightConfiguration.Gizmo.Collectors", "SunStudio");//NOI18N
        VisualizerConfiguration detailsVisualizerConfig = null;
        DataTableMetadata detailedViewTableMetadata;
        if (collector.equals(SUNSTUDIO)) {
            // SunStudio should collect data about most CPU-expensive functions
            // i.e. create a configuration that collects
            // CollectedInfo.FUNCTIONS_LIST
            final CollectedInfo info = CollectedInfo.FUNCTIONS_LIST;

            SunStudioDCConfiguration ssCollectorConfig =
                new SunStudioDCConfiguration(info);

            // This tool will use DataCollector with this configuration...
            toolConfiguration.addDataCollectorConfiguration(ssCollectorConfig);

            // Now configure what happens when user clicks on the monitor....
            // We should display Detailed view (using CallersCalleesVisualizer)
            // with (some of) data that SunStudio collects when it configured
            // with CollectedInfo.FUNCTIONS_LIST

            detailedViewTableMetadata =
                SunStudioDCConfiguration.getCPUTableMetadata(
                SunStudioDCConfiguration.c_name,
                SunStudioDCConfiguration.c_iUser,
                SunStudioDCConfiguration.c_eUser);
        } else {//DTrace
            // Use D-Trace as a provider of data for detailed view
            String scriptFile = Util.copyResource(getClass(),
                Util.getBasePath(getClass()) + "/resources/calls.d"); // NOI18N

            DataTableMetadata profilerTableMetadata = createProfilerTableMetadata();

            DTDCConfiguration dtraceDataCollectorConfiguration =
                new DTDCConfiguration(scriptFile,
                Arrays.asList(profilerTableMetadata));

            dtraceDataCollectorConfiguration.setStackSupportEnabled(true);

            toolConfiguration.addDataCollectorConfiguration(
                new MultipleDTDCConfiguration(
                dtraceDataCollectorConfiguration, "cpu:")); // NOI18N

            detailedViewTableMetadata =
                createFunctionsListMetadata(profilerTableMetadata);
        }
        // Register configured detailed view to be opened on indicator click...
        detailsVisualizerConfig =
            new CallersCalleesVisualizerConfiguration(
            detailedViewTableMetadata,
            "name", // NOI18N
            true);

        //we have registered collectors, not show
        //register indicator data providers
        //if we are on Linux: use LL, prstat otherwise
        String indicatorDataProvider = NbPreferences.root().get("DLightConfiguration.Gizmo.IndicatorDataProvider", "prstat");//NOI18N
        if (indicatorDataProvider.equals("prstat")) {//NOI18N
            // both use prstat as indicator data provider
            final DataTableMetadata indicatorTableMetadata =
                new DataTableMetadata("prstat", PRSTAT_COLUMNS); // NOI18N

            CLIODCConfiguration indicatorProviderConfiguration =
                new CLIODCConfiguration("/bin/prstat", "-mv -p @PID -c 1", // NOI18N
                new PrstatParser(), Arrays.asList(indicatorTableMetadata));

            toolConfiguration.addIndicatorDataProviderConfiguration(
                indicatorProviderConfiguration);
            IndicatorMetadata indicatorMetadata =
                new IndicatorMetadata(PRSTAT_COLUMNS);
            CpuIndicatorConfiguration indicatorConfiguration =
                new CpuIndicatorConfiguration(indicatorMetadata);
            if (detailsVisualizerConfig != null) {
                indicatorConfiguration.setVisualizerConfiguration(detailsVisualizerConfig);
            }
            toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        } else {//use LL
            LLDataCollectorConfiguration llDataCollectorConfiguration =
                new LLDataCollectorConfiguration(LLDataCollectorConfiguration.CollectedData.CPU);

            toolConfiguration.addIndicatorDataProviderConfiguration(llDataCollectorConfiguration);
            IndicatorMetadata indicatorMetadata =
                new IndicatorMetadata(LLDataCollectorConfiguration.CPU_TABLE.getColumns());
            CpuIndicatorConfiguration indicatorConfiguration =
                new CpuIndicatorConfiguration(indicatorMetadata);
            if (detailsVisualizerConfig != null) {
                indicatorConfiguration.setVisualizerConfiguration(detailsVisualizerConfig);
            }
            toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        }
        return toolConfiguration;
    }

    private DataTableMetadata createProfilerTableMetadata() {
        Column cpuId = new Column("cpu_id", Integer.class, loc("CPUMonitorTool.ColumnName.cpu_id"), null); // NOI18N
        Column threadId = new Column("thread_id", Integer.class, loc("CPUMonitorTool.ColumnName.thread_id"), null); // NOI18N
        Column timestamp = new Column("time_stamp", Long.class, loc("CPUMonitorTool.ColumnName.time_stamp"), null); // NOI18N
        Column stackId = new Column("leaf_id", Integer.class, loc("CPUMonitorTool.ColumnName.leaf_id"), null); // NOI18N

        return new DataTableMetadata("CallStack", // NOI18N
            Arrays.asList(cpuId, threadId, timestamp, stackId));
    }

    private DataTableMetadata createFunctionsListMetadata(
        DataTableMetadata profilerTableMetadata) {

        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("name", String.class, loc("CPUMonitorTool.ColumnName.name"), null)); // NOI18N
        columns.add(new Column("name_qualified", String.class, loc("CPUMonitorTool.ColumnName.name_qualified"), null)); // NOI18N

        List<FunctionMetric> metricsList = SQLStackStorage.METRICS;

        for (FunctionMetric metric : metricsList) {
            columns.add(new Column(
                metric.getMetricID(),
                metric.getMetricValueClass(),
                metric.getMetricDisplayedName(), null));
        }

        DataTableMetadata result = new DataTableMetadata(
            StackDataStorage.STACK_METADATA_VIEW_NAME,
            columns, null, Arrays.asList(profilerTableMetadata));


        return result;
    }

    private static class PrstatParser implements CLIOParser {

        private final List<String> colnames = Arrays.asList(new String[]{
                "utime", // NOI18N
                "stime", // NOI18N
                "wtime" // NOI18N
            });
        Float utime, stime, wtime;

        public DataRow process(String line) {
            if (line == null) {
                return null;
            }
            String l = line.trim();
            l = l.replaceAll(",", "."); // NOI18N
            String[] tokens = l.split("[ \t]+"); // NOI18N

            if (tokens.length != 15) {
                return null;
            }

            try {
                utime = Float.valueOf(tokens[2]);
                stime = Float.valueOf(tokens[3]);
                wtime = Float.valueOf(tokens[8]);
            } catch (NumberFormatException ex) {
                return null;
            }

            return new DataRow(colnames, Arrays.asList(
                new Float[]{utime, stime, wtime}));
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
            DLightCPUToolConfigurationProvider.class, key, params);
    }
}
