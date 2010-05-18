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
import java.beans.FeatureDescriptor;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.storage.DataUtil;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.core.stack.ui.StackRenderer;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.indicators.Aggregation;
import org.netbeans.modules.dlight.indicators.TimeSeriesIndicatorConfiguration;
import org.netbeans.modules.dlight.indicators.DataRowToTimeSeries;
import org.netbeans.modules.dlight.indicators.DetailDescriptor;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.BytesFormatter;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey Vladykin
 */
public class FopsToolConfigurationProvider implements DLightToolConfigurationProvider {
    private static final String ID = "dlight.tool.fops"; // NOI18N
    private static final String FILE_COUNT_ID = "file-count"; // NOI18N

    private static final int INDICATOR_POSITION = 400;

    public FopsToolConfigurationProvider() {
    }

    @Override
    public DLightToolConfiguration create() {
        final String toolName = getMessage("Tool.Name"); // NOI18N
        final DLightToolConfiguration toolConfiguration =
                new DLightToolConfiguration(ID, toolName);
        toolConfiguration.setIcon("org/netbeans/modules/dlight/fops/resources/i_o_usage_16.png");//NOI18N
        toolConfiguration.setDescription(getMessage("Tool.Description"));//NOI18N
        FeatureDescriptor descriptor = new FeatureDescriptor();
        descriptor.setValue(DTDCConfiguration.DSCRIPT_TOOL_PROPERTY, getScriptUrl());
        toolConfiguration.setFeatureDescriptor(descriptor);
        Column opColumn = new Column("operation", String.class, getMessage("Column.OpType"), null); // NOI18N
        Column fileColumn = new Column("file", String.class, getMessage("Column.Filename"), null); // NOI18N
        Column sizeColumn = new Column("size", Long.class, getMessage("Column.Size"), null); // NOI18N
        Column fileCountColumn = new Column("file_count", Long.class, getMessage("Column.FileCount"), null); // NOI18N

        List<Column> fopsColumns = Arrays.asList(
                new Column("timestamp", Time.class, getMessage("Column.Timestamp"), null), // NOI18N
                opColumn,
                new Column("sid", Integer.class, getMessage("Column.SID"), null), // NOI18N
                fileColumn,
                sizeColumn,
                fileCountColumn,
                new Column("stack_id", Long.class, getMessage("Column.StackId"), null)); // NOI18N

        final DataTableMetadata dtraceFopsMetadata =
                new DataTableMetadata("fops", fopsColumns, null); // NOI18N

        final DTDCConfiguration dtraceCollectorConfig =
                new DTDCConfiguration(getScriptUrl(), Arrays.asList(dtraceFopsMetadata));
        dtraceCollectorConfig.setStackSupportEnabled(true);
        dtraceCollectorConfig.setIndicatorFiringFactor(1);
        dtraceCollectorConfig.setOutputPrefix("fops:"); // NOI18N

        toolConfiguration.addDataCollectorConfiguration(dtraceCollectorConfig);
        toolConfiguration.addIndicatorDataProviderConfiguration(dtraceCollectorConfig);

        Column openStackColumn = new Column("open_stack_id", Long.class, getMessage("Column.OpenStack"), null); // NOI18N
        Column closeStackColumn = new Column("close_stack_id", Long.class, getMessage("Column.CloseStack"), null); // NOI18N
        Column statusColumn = new Column("status", String.class, getMessage("Column.Status"), null); // NOI18N

        DataTableMetadata detailsMetadata = new DataTableMetadata("iosummary", // NOI18N
                Arrays.asList(
                        fileColumn,
                        new Column("bytes_read", Long.class, getMessage("Column.BytesRead"), null), // NOI18N
                        new Column("bytes_written", Long.class, getMessage("Column.BytesWritten"), null), // NOI18N
                        openStackColumn,
                        closeStackColumn,
                        statusColumn),
                "SELECT file, bytes_read, bytes_written, open_stack_id, close_stack_id, CASEWHEN(open_failed, 'err', CASEWHEN(open_seen AND NOT close_seen, 'warn', 'ok')) AS status FROM " + // NOI18N
                "(SELECT file, SUM(CASEWHEN(operation='read', size, 0)) AS bytes_read, " + // NOI18N
                "SUM(CASEWHEN(operation='write', size, 0)) AS bytes_written, " + // NOI18N
                "SUM(CASEWHEN(operation='open', stack_id, 0)) AS open_stack_id, " + // NOI18N
                "SUM(CASEWHEN(operation='close', stack_id, 0)) AS close_stack_id, " + // NOI18N
                "SUM(CASEWHEN(operation='open', timestamp, 0)) AS open_timestamp, " + // NOI18N
                "BOOL_OR(operation='open') AS open_seen, " + // NOI18N
                "BOOL_OR(operation='open' AND sid=0) AS open_failed, " + // NOI18N
                "BOOL_OR(operation='close') AS close_seen " + // NOI18N
                "FROM fops GROUP BY sid, file) ORDER BY open_timestamp, file", // NOI18N
                Arrays.asList(dtraceFopsMetadata));

        IndicatorMetadata indicatorMetadata = new IndicatorMetadata(fopsColumns);

        TimeSeriesIndicatorConfiguration indicatorConfiguration = new TimeSeriesIndicatorConfiguration(
                indicatorMetadata, INDICATOR_POSITION);
        indicatorConfiguration.setPersistencePrefix("dlight_fops"); // NOI18N
        indicatorConfiguration.setTitle(getMessage("Indicator.Title")); // NOI18N
        indicatorConfiguration.setGraphScale(1024);
        indicatorConfiguration.addTimeSeriesDescriptors(
                new TimeSeriesDescriptor("write", getMessage("Indicator.Write"), new Color(0xE7, 0x6F, 0x00), TimeSeriesDescriptor.Kind.LINE), // NOI18N
                new TimeSeriesDescriptor("read", getMessage("Indicator.Read"), new Color(0xFF, 0xC7, 0x26), TimeSeriesDescriptor.Kind.LINE)); // NOI18N
        indicatorConfiguration.setDataRowHandler(
                new DataRowToFops(opColumn, sizeColumn, fileCountColumn));
        indicatorConfiguration.setAggregation(Aggregation.SUM);
        indicatorConfiguration.setLastNonNull(false);
        indicatorConfiguration.addDetailDescriptors(
                new DetailDescriptor(FILE_COUNT_ID, getMessage("Indicator.FileCount"), String.valueOf(0))); // NOI18N
        indicatorConfiguration.setActionDisplayName(getMessage("Indicator.Action")); // NOI18N
        indicatorConfiguration.setActionTooltip(getMessage("Indicator.Action.Tooltip")); // NOI18N
        indicatorConfiguration.setLabelFormatter(new BytesFormatter());

        AdvancedTableViewVisualizerConfiguration tableConfiguration =
                new AdvancedTableViewVisualizerConfiguration(detailsMetadata, fileColumn.getColumnName(), fileColumn.getColumnName());
        tableConfiguration.setHiddenColumnNames(Arrays.asList(openStackColumn.getColumnName(), closeStackColumn.getColumnName(), statusColumn.getColumnName()));
        tableConfiguration.setNodeColumnIcon(statusColumn.getColumnName(), "org/netbeans/modules/dlight/fops/resources"); // NOI18N
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

    private URL getScriptUrl() {
        return  getClass().getResource("resources/fops.d"); // NOI18N
    }

    private static class DataRowToFops implements DataRowToTimeSeries {

        private final String opColumn;
        private final String sizeColumn;
        private final String fileCountColumn;
        private long fileCount;

        public DataRowToFops(Column opColumn, Column sizeColumn, Column fileCountColumn) {
            this.opColumn = opColumn.getColumnName();
            this.sizeColumn = sizeColumn.getColumnName();
            this.fileCountColumn = fileCountColumn.getColumnName();
        }

        @Override
        public float[] getData(DataRow row) {
            String op = row.getStringValue(opColumn);
            if (op == null) {
                return null;
            }
            int reads = 0;
            int writes = 0;
            if ("read".equals(op) || "write".equals(op)) { // NOI18N
                int bytes = DataUtil.toInt(row.getData(sizeColumn));
                if ("read".equals(op)) { // NOI18N
                    reads += bytes;
                } else {
                    writes += bytes;
                }
            }
            int newFileCount = DataUtil.toInt(row.getData(fileCountColumn), -1);
            if (0 <= newFileCount) {
                fileCount = newFileCount;
            }
            return new float[]{writes, reads};
        }

        @Override
        public Map<String, String> getDetails() {
            return Collections.singletonMap(FILE_COUNT_ID, String.valueOf(fileCount));
        }
    }
}
