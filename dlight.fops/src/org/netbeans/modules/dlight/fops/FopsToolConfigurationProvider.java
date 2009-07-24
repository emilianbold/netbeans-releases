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
package org.netbeans.modules.dlight.fops;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.indicators.PlotIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.graph.DataRowToPlot;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey Vladykin
 */
public class FopsToolConfigurationProvider implements DLightToolConfigurationProvider {
    private static final String ID = "dlight.tool.fops"; // NOI18N

    private static final int INDICATOR_POSITION = 400;

    public FopsToolConfigurationProvider() {
    }

    public DLightToolConfiguration create() {
        final String toolName = getMessage("Tool.Name"); // NOI18N
        final DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(ID, toolName);

        /* DTrace tool - FOPS */

        List<Column> fopsColumns = Arrays.asList(
                new Column("timestamp", Long.class, getMessage("Column.Timestamp"), null), // NOI18N
                new Column("cpu", Integer.class, getMessage("Column.CPU"), null), // NOI18N
                new Column("thread", Integer.class, getMessage("Column.Thread"), null), // NOI18N
                new Column("operation", String.class, getMessage("Column.OpType"), null), // NOI18N
                new Column("handle", Integer.class, getMessage("Column.Handle"), null), // NOI18N
                new Column("file", String.class, getMessage("Column.Filename"), null), // NOI18N
                new Column("size", Long.class, getMessage("Column.Size"), null), // NOI18N
                new Column("stack_id", Long.class, getMessage("Column.StackId"), null)); // NOI18N

        final DataTableMetadata dtraceFopsMetadata =
                new DataTableMetadata("fops", fopsColumns, null); // NOI18N

        final String script = Util.copyResource(
                FopsToolConfigurationProvider.class,
                "org/netbeans/modules/dlight/fops/resources/fops.d"); // NOI18N

        final DTDCConfiguration dtraceCollectorConfig =
                new DTDCConfiguration(script, Arrays.asList(dtraceFopsMetadata));
        dtraceCollectorConfig.setStackSupportEnabled(true);
        dtraceCollectorConfig.setIndicatorFiringFactor(1);

        toolConfiguration.addDataCollectorConfiguration(
                new MultipleDTDCConfiguration(dtraceCollectorConfig, "fops:")); // NOI18N

        toolConfiguration.addIndicatorDataProviderConfiguration(dtraceCollectorConfig);

        IndicatorMetadata indicatorMetadata = new IndicatorMetadata(fopsColumns);

        PlotIndicatorConfiguration indicatorConfiguration = new PlotIndicatorConfiguration(
                indicatorMetadata, INDICATOR_POSITION, getMessage("Indicator.Title"), 100, // NOI18N
                Arrays.asList(
                        new GraphDescriptor(Color.GRAY, getMessage("Indicator.Value"), GraphDescriptor.Kind.LINE)), // NOI18N
                new DataRowToIOPlot());
        indicatorConfiguration.setActionDisplayName(getMessage("Indicator.Action")); // NOI18N
        indicatorConfiguration.addVisualizerConfiguration(
                new TableVisualizerConfiguration(dtraceFopsMetadata));

        toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        return toolConfiguration;
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(FopsToolConfigurationProvider.class, name);
    }

    private static class DataRowToIOPlot implements DataRowToPlot {
        public void addDataRow(DataRow row) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
        public void tick(float[] data, Map<String,String> details) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
