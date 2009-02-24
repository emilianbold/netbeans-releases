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
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
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
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public final class DLightCPUToolConfigurationProvider
        implements DLightToolConfigurationProvider {

    private static final boolean USE_SUNSTUDIO =
            Boolean.getBoolean("gizmo.cpu.sunstudio"); // NOI18N
    private static final String toolName = loc("CPUMonitorTool.ToolName"); // NOI18N
    private static final List<Column> indicatorColumns;


    static {
        Column utime = new _Column<Float>("utime"); // NOI18N
        Column stime = new _Column<Float>("stime"); // NOI18N
        Column wtime = new _Column<Float>("wtime"); // NOI18N

        indicatorColumns = Arrays.asList(utime, stime, wtime);
    }

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(toolName);

        // Configure Indicator Data Provider ...
        // Use /bin/prstat.

        final DataTableMetadata indicatorTableMetadata =
                new DataTableMetadata("prstat", indicatorColumns); // NOI18N

        CLIODCConfiguration indicatorCollectorConfig =
                new CLIODCConfiguration("/bin/prstat", "-mv -p @PID -c 1", // NOI18N
                new PrstatParser(), Arrays.asList(indicatorTableMetadata));

        // Register indicator data provider ...
        toolConfiguration.addIndicatorDataProviderConfiguration(
                indicatorCollectorConfig);

        // Configure Indicator
        IndicatorMetadata indicatorMetadata =
                new IndicatorMetadata(indicatorColumns);

        CpuIndicatorConfiguration indicatorConfiguration =
                new CpuIndicatorConfiguration(indicatorMetadata);

        // Configure detailed view
        DataTableMetadata detailedViewTableMetadata = null;

        if (USE_SUNSTUDIO) {
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
        } else {
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
        VisualizerConfiguration detailsVisualizerConfig =
                new CallersCalleesVisualizerConfiguration(
                detailedViewTableMetadata,
                "name", // NOI18N
                true);

        indicatorConfiguration.setVisualizerConfiguration(detailsVisualizerConfig);
        toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        return toolConfiguration;
    }

    private DataTableMetadata createProfilerTableMetadata() {
        Column cpuId = new _Column<Integer>("cpu_id"); // NOI18N
        Column threadId = new _Column<Integer>("thread_id"); // NOI18N
        Column timestamp = new _Column<Long>("time_stamp"); // NOI18N
        Column stackId = new _Column<Integer>("leaf_id"); // NOI18N

        return new DataTableMetadata("CallStack", // NOI18N
                Arrays.asList(cpuId, threadId, timestamp, stackId));
    }

    private DataTableMetadata createFunctionsListMetadata(
            DataTableMetadata profilerTableMetadata) {

        List<Column> columns = new ArrayList<Column>();
        columns.add(new _Column<String>("name")); // NOI18N

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

    private static class _Column<T> extends Column {

        public _Column(String name) {
            super(name, ((T) new Object[0]).getClass(),
                    loc("CPUMonitorTool.ColumnName." + name), null); // NOI18N
        }
    }
}
