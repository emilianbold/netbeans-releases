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
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.core.stack.ui.StackRenderer;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.indicators.PlotIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.graph.DataRowToPlot;
import org.netbeans.modules.dlight.indicators.graph.Graph.LabelRenderer;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey Vladykin
 */
public class FopsToolConfigurationProvider implements DLightToolConfigurationProvider {
    private static final String ID = "dlight.tool.fops"; // NOI18N

    private static final int INDICATOR_POSITION = 400;

    private static final int BINARY_ORDER = 1024;
    private static final int DECIMAL_ORDER = 1000;
    private static final String[] SUFFIXES = {"b", "K", "M", "G", "T"};//NOI18N

    private static final NumberFormat INT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
    private static final NumberFormat FRAC_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    static {
        FRAC_FORMAT.setMaximumFractionDigits(1);
    }

    public FopsToolConfigurationProvider() {
    }

    public DLightToolConfiguration create() {
        final String toolName = getMessage("Tool.Name"); // NOI18N
        final DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(ID, toolName);

        Column opColumn = new Column("operation", String.class, getMessage("Column.OpType"), null); // NOI18N
        Column fileColumn = new Column("file", String.class, getMessage("Column.Filename"), null); // NOI18N
        Column sizeColumn = new Column("size", Long.class, getMessage("Column.Size"), null); // NOI18N

        List<Column> fopsColumns = Arrays.asList(
                new Column("timestamp", Long.class, getMessage("Column.Timestamp"), null), // NOI18N
                opColumn,
                new Column("sid", Integer.class, getMessage("Column.SID"), null), // NOI18N
                fileColumn,
                sizeColumn,
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
        final MultipleDTDCConfiguration multiDtraceCollectorConfig
                = new MultipleDTDCConfiguration(dtraceCollectorConfig, "fops:"); // NOI18N

        toolConfiguration.addDataCollectorConfiguration(multiDtraceCollectorConfig);
        toolConfiguration.addIndicatorDataProviderConfiguration(multiDtraceCollectorConfig);

        Column openStackColumn = new Column("open_stack_id", Long.class, getMessage("Column.OpenStack"), null); // NOI18N
        Column closeStackColumn = new Column("close_stack_id", Long.class, getMessage("Column.CloseStack"), null); // NOI18N

        DataTableMetadata detailsMetadata = new DataTableMetadata("iosummary", // NOI18N
                Arrays.asList(
                        fileColumn,
                        new Column("bytes_read", Long.class, getMessage("Column.BytesRead"), null), // NOI18N
                        new Column("bytes_written", Long.class, getMessage("Column.BytesWritten"), null), // NOI18N
                        openStackColumn,
                        closeStackColumn,
                        new Column("closed", Boolean.class, getMessage("Column.Closed"), null)), // NOI18N
                "SELECT file, SUM(CASEWHEN(operation='read', size, 0)) AS bytes_read, " + // NOI18N
                "SUM(CASEWHEN(operation='write', size, 0)) AS bytes_written, " + // NOI18N
                "SUM(CASEWHEN(operation='open', stack_id, 0)) AS open_stack_id, " + // NOI18N
                "SUM(CASEWHEN(operation='close', stack_id, 0)) AS close_stack_id, " + // NOI18N
                "BOOL_OR(operation='close') AS closed " + // NOI18N
                "FROM fops GROUP BY sid, file " + // NOI18N
                "ORDER BY closed ASC", // NOI18N
                Arrays.asList(dtraceFopsMetadata));

        IndicatorMetadata indicatorMetadata = new IndicatorMetadata(fopsColumns);

        PlotIndicatorConfiguration indicatorConfiguration = new PlotIndicatorConfiguration(
                indicatorMetadata, INDICATOR_POSITION, getMessage("Indicator.Title"), BINARY_ORDER, // NOI18N
                Arrays.asList(
                        new GraphDescriptor(new Color(0xE7, 0x6F, 0x00), getMessage("Indicator.Write"), GraphDescriptor.Kind.LINE), // NOI18N
                        new GraphDescriptor(new Color(0xFF, 0xC7, 0x26), getMessage("Indicator.Read"), GraphDescriptor.Kind.LINE)), // NOI18N
                new DataRowToIOPlot(opColumn, sizeColumn));
        indicatorConfiguration.setActionDisplayName(getMessage("Indicator.Action")); // NOI18N
        indicatorConfiguration.setLabelRenderer(new LabelRenderer() {
            public String render(int value) {
                return formatValue(value);
            }
        });

        AdvancedTableViewVisualizerConfiguration tableConfiguration =
                new AdvancedTableViewVisualizerConfiguration(detailsMetadata, fileColumn.getColumnName(), fileColumn.getColumnName());
        tableConfiguration.setHiddenColumnNames(Arrays.asList(openStackColumn.getColumnName(), closeStackColumn.getColumnName()));
        tableConfiguration.setEmptyAnalyzeMessage(getMessage("Details.EmptyAnalyze")); // NOI18N
        tableConfiguration.setEmptyRunningMessage(getMessage("Details.EmptyRunning")); // NOI18N
        tableConfiguration.setDualPaneMode(true);
        tableConfiguration.setDataRowRenderer(new StackRenderer(Arrays.asList(openStackColumn, closeStackColumn)));
        indicatorConfiguration.addVisualizerConfiguration(tableConfiguration);

        toolConfiguration.addIndicatorConfiguration(indicatorConfiguration);

        return toolConfiguration;
    }

    private static String getMessage(String name) {
        return NbBundle.getMessage(FopsToolConfigurationProvider.class, name);
    }

    private static String formatValue(long value) {
        double dbl = value;
        int i = 0;
        while (BINARY_ORDER <= dbl && i + 1 < SUFFIXES.length) {
            dbl /= BINARY_ORDER;
            ++i;
        }
        if (DECIMAL_ORDER <= dbl && i + 1 < SUFFIXES.length) {
            dbl /= BINARY_ORDER;
            ++i;
        }
        NumberFormat nf = dbl < 10? FRAC_FORMAT : INT_FORMAT;
        return nf.format(dbl) + SUFFIXES[i];
    }

    private static class DataRowToIOPlot implements DataRowToPlot {

        private final String opColumn;
        private final String sizeColumn;
        private long reads;
        private long writes;

        public DataRowToIOPlot(Column opColumn, Column sizeColumn) {
            this.opColumn = opColumn.getColumnName();
            this.sizeColumn = sizeColumn.getColumnName();
        }

        public synchronized void addDataRow(DataRow row) {
            String op = row.getStringValue(opColumn);
            if ("read".equals(op) || "write".equals(op)) { // NOI18N
                int bytes = DataUtil.toInt(row.getData(sizeColumn));
                if ("read".equals(op)) { // NOI18N
                    reads += bytes;
                } else {
                    writes += bytes;
                }
            }
        }

        public synchronized void tick(float[] data, Map<String,String> details) {
            data[0] = writes;
            data[1] = reads;
            writes = 0;
            reads = 0;
        }
    }
}
