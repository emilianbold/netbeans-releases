/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.threadmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState.MSAState;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.indicators.PlotIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.graph.DataRowToPlot;
import org.netbeans.modules.dlight.indicators.graph.GraphConfig;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.threadmap.collector.MSAParser;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.ThreadMapVisualizerConfiguration;
import org.openide.util.NbBundle;

public class ThreadMapToolConfigurationProvider implements DLightToolConfigurationProvider {

    public static final int INDICATOR_POSITION = 10;
    private static final String TOOL_NAME = loc("ThreadMapTool.ToolName"); // NOI18N
    private static final String DETAILED_TOOL_NAME = loc("ThreadMapTool.DetailedToolName"); // NOI18N

    private static final int[] MSA_TO_SIMPLE = {0, 1, 3, 3, 3, 3, 2, 3, 3, 3};

    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(TOOL_NAME, DETAILED_TOOL_NAME);

        DataTableMetadata msaTableMetadata = createIndicatorTableMetadata();

        PlotIndicatorConfiguration indicatorConfig = new PlotIndicatorConfiguration(
                new IndicatorMetadata(msaTableMetadata.getColumns()), INDICATOR_POSITION, loc("ThreadMapTool.Indicator.Title"), 1, // NOI18N
                Arrays.asList(
                    new GraphDescriptor(GraphConfig.COLOR_1, loc("ThreadMapTool.Indicator.LMS_USER"), GraphDescriptor.Kind.REL_SURFACE), // NOI18N
                    new GraphDescriptor(GraphConfig.COLOR_3, loc("ThreadMapTool.Indicator.LMS_SYSTEM"), GraphDescriptor.Kind.REL_SURFACE), // NOI18N
                    new GraphDescriptor(GraphConfig.COLOR_2, loc("ThreadMapTool.Indicator.LMS_USER_LOCK"), GraphDescriptor.Kind.REL_SURFACE), // NOI18N
                    new GraphDescriptor(GraphConfig.COLOR_4, loc("ThreadMapTool.Indicator.LMS_OTHER"), GraphDescriptor.Kind.REL_SURFACE)), // NOI18N
                new DataRowToMSAPlot(msaTableMetadata.getColumns()));
        indicatorConfig.setActionDisplayName(loc("ThreadMapTool.Indicator.Action")); // NOI18N

        VisualizerConfiguration visualizerConfig = new ThreadMapVisualizerConfiguration();

        indicatorConfig.addVisualizerConfiguration(visualizerConfig);
        toolConfiguration.addIndicatorConfiguration(indicatorConfig);

        String scriptFile = Util.copyResource(getClass(),
                Util.getBasePath(getClass()) + "/resources/msa.d"); // NOI18N

        DTDCConfiguration dtraceDataCollectorConfiguration = new DTDCConfiguration(scriptFile, Arrays.asList(msaTableMetadata));

        dtraceDataCollectorConfiguration.setDtraceParser(new MSAParser(new TimeDuration(TimeUnit.SECONDS, 1), null));
        dtraceDataCollectorConfiguration.setIndicatorFiringFactor(1); // MSAParser will do aggregation once per second...

        MultipleDTDCConfiguration collector = new MultipleDTDCConfiguration(dtraceDataCollectorConfiguration, "msa"); // NOI18N

        toolConfiguration.addIndicatorDataProviderConfiguration(collector);
        toolConfiguration.addDataCollectorConfiguration(collector);

        return toolConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                ThreadMapToolConfigurationProvider.class, key, params);
    }

    private DataTableMetadata createIndicatorTableMetadata() {
        Column threads = new Column("threads", Integer.class); // NOI18N
        Column usrTime = new Column(MSAState.RunningUser.toString(), Integer.class);
        Column sysTime = new Column(MSAState.RunningSystemCall.toString(), Integer.class);
        Column othTime = new Column(MSAState.RunningOther.toString(), Integer.class);
        Column tpfTime = new Column(MSAState.SleepingUserTextPageFault.toString(), Integer.class);
        Column dpfTime = new Column(MSAState.SleepingUserDataPageFault.toString(), Integer.class);
        Column kpfTime = new Column(MSAState.SleepingKernelPageFault.toString(), Integer.class);
        Column lckTime = new Column(MSAState.SleepingUserLock.toString(), Integer.class);
        Column slpTime = new Column(MSAState.SleepingOther.toString(), Integer.class);
        Column latTime = new Column(MSAState.WaitingCPU.toString(), Integer.class);
        Column stpTime = new Column(MSAState.ThreadStopped.toString(), Integer.class);

        return new DataTableMetadata("MSA", // NOI18N
                Arrays.asList(threads, usrTime, sysTime, othTime, tpfTime, dpfTime, kpfTime, lckTime, slpTime, latTime, stpTime), null);
    }

    private static class DataRowToMSAPlot implements DataRowToPlot {

        private final List<Column> columns;
        private float[] data;

        public DataRowToMSAPlot(List<Column> columns) {
            this.columns = new ArrayList<Column>(columns);
            this.data = new float[4];
        }

        public void addDataRow(DataRow row) {
            String threadsColumn = columns.get(0).getColumnName();
            int threads = DataUtil.toInt(row.getData(threadsColumn), 1);
            float[] newData = new float[4];
            int sum = 0;
            for (int i = 1; i < columns.size(); ++i) {
                String columnName = columns.get(i).getColumnName();
                Object value = row.getData(columnName);
                if (value != null) {
                    int intValue = DataUtil.toInt(value);
                    newData[MSA_TO_SIMPLE[i - 1]] += intValue;
                    sum += intValue;
                }
            }
            if (0 < sum) {
                for (int i = 0; i < newData.length; ++i) {
                    newData[i] = threads * newData[i] / sum;
                }
                data = newData;
            }
        }

        public void tick() {
        }

        public float[] getGraphData() {
            return data;
        }

        public Map<String, String> getDetails() {
            return Collections.emptyMap();
        }
    }
}
