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
package org.netbeans.modules.dlight.msa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.collector.procfs.ProcFSDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.msa.support.MSASQLTables;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.visualizers.api.ThreadMapVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import org.openide.util.NbBundle;

public class ThreadMapToolConfigurationProvider implements DLightToolConfigurationProvider {

    private final static Logger log = DLightLogger.getLogger(ThreadMapToolConfigurationProvider.class);
    public static final int INDICATOR_POSITION = 10;
    private static final String ID = "dlight.tool.threadmap"; // NOI18N
    private static final String TOOL_NAME = loc("ThreadMapTool.ToolName"); // NOI18N
    private static final String DETAILED_TOOL_NAME = loc("ThreadMapTool.DetailedToolName"); // NOI18N

    @Override
    public DLightToolConfiguration create() {
        final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(ID, TOOL_NAME);
        toolConfiguration.setLongName(DETAILED_TOOL_NAME);
        toolConfiguration.setIcon("org/netbeans/modules/dlight/msa/resources/thread_microstates_16.png");//NOI18N
        toolConfiguration.setDescription(loc("ThreadMapTool.Description")); // NOI18N
        final List<Column> indicatorDataColumns = Arrays.asList(
                MSASQLTables.prstat.P_SLEEP,
                MSASQLTables.prstat.P_WAIT,
                MSASQLTables.prstat.P_BLOCKED,
                MSASQLTables.prstat.P_RUNNING);

        final TimeSeriesIndicatorConfiguration indicatorConfig = new TimeSeriesIndicatorConfiguration(
                new IndicatorMetadata(indicatorDataColumns), INDICATOR_POSITION);

        indicatorConfig.setPersistencePrefix("dlight_msa"); // NOI18N
        indicatorConfig.setTitle(loc("ThreadMapTool.Indicator.Title")); // NOI18N
        indicatorConfig.setGraphScale(1);

        indicatorConfig.addTimeSeriesDescriptors(
                new TimeSeriesDescriptor("sleeping", ThreadStateResources.THREAD_SLEEPING.name, ThreadStateResources.THREAD_SLEEPING.color, TimeSeriesDescriptor.Kind.REL_SURFACE), // NOI18N
                new TimeSeriesDescriptor("waiting", ThreadStateResources.THREAD_WAITING.name, ThreadStateResources.THREAD_WAITING.color, TimeSeriesDescriptor.Kind.REL_SURFACE), // NOI18N
                new TimeSeriesDescriptor("blocked", ThreadStateResources.THREAD_BLOCKED.name, ThreadStateResources.THREAD_BLOCKED.color, TimeSeriesDescriptor.Kind.REL_SURFACE), // NOI18N
                new TimeSeriesDescriptor("running", ThreadStateResources.THREAD_RUNNING.name, ThreadStateResources.THREAD_RUNNING.color, TimeSeriesDescriptor.Kind.REL_SURFACE)); // NOI18N
        indicatorConfig.setDataRowHandler(new IndicatorDataHandler(indicatorDataColumns));
        indicatorConfig.setActionDisplayName(loc("ThreadMapTool.Indicator.Action")); // NOI18N
        indicatorConfig.setActionTooltip(loc("ThreadMapTool.Indicator.Action.Tooltip"));//NOI18N

        // Here we should configure visualizer...
        // TODO: Currently is dummy imlpementation.
        // We say which data it will visualize and which column is a "thread",
        // which colors to use and so on...
        // Moreover, later, on opening visualizer, a tables that are returned by 
        // getMetadata() if visualizerConfiguration (SEARCHED BY TABLE NAME(!))
        // will be searched in DB...
        VisualizerConfiguration visualizerConfig = new ThreadMapVisualizerConfiguration();

        indicatorConfig.addVisualizerConfiguration(visualizerConfig);
        toolConfiguration.addIndicatorConfiguration(indicatorConfig);

        ProcFSDCConfiguration procFSDCConfig = new ProcFSDCConfiguration();
        procFSDCConfig.collectProcInfo(1000);
        procFSDCConfig.collectMSA(1000);
        toolConfiguration.addIndicatorDataProviderConfiguration(procFSDCConfig);
        toolConfiguration.addDataCollectorConfiguration(procFSDCConfig);

//        CLIODCConfiguration prstatConfig = new CLIODCConfiguration(
//                "/bin/prstat", "-mL -p @PID -c 1", // NOI18N
//                new MSAPrstatParser1(msaTableMetadata), Arrays.asList(msaTableMetadata));
//        prstatConfig.setName("SunStudio"); // NOI18N

//        toolConfiguration.addIndicatorDataProviderConfiguration(prstatConfig);
//        toolConfiguration.addDataCollectorConfiguration(prstatConfig);

//        prstatConfig.setName("SunStudio");//NOI18N
//        dataCollectorConfigurations.add(prstatConfig);

        // Enable call stacks without dependency on CPU tool.
        toolConfiguration.addDataCollectorConfiguration(DTDCConfiguration.createCpuSamplingConfiguration());

        return toolConfiguration;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                ThreadMapToolConfigurationProvider.class, key, params);
    }

    private static class IndicatorDataHandler implements DataRowToTimeSeries {

        private final static Object lock = IndicatorDataHandler.class.getName() + "Lock"; // NOI18N
        private final List<String> colNames;

        private IndicatorDataHandler(List<Column> indicatorDataColumns) {
            colNames = new ArrayList<String>();

            for (Column c : indicatorDataColumns) {
                colNames.add(c.getColumnName());
            }
        }

        @Override
        public float[] getData(DataRow row) {
            int idx = 0;
            float[] result = null;

            synchronized (lock) {
                for (String cn : colNames) {
                    try {
                        float f = row.getFloatValue(cn);
                        if (result == null) {
                            result = new float[colNames.size()];
                        }
                        result[idx] = f;
                    } catch (RuntimeException ex) {
                        if (log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "Will not add this entry", ex); // NOI18N
                        }
                    }
                    ++idx;
                }
            }

            return result;
        }

        @Override
        public Map<String, String> getDetails() {
            return Collections.emptyMap();
        }
    }
}
