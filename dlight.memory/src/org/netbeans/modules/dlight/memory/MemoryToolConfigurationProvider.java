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
package org.netbeans.modules.dlight.memory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIOParser;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Kvashin
 */
public final class MemoryToolConfigurationProvider implements DLightToolConfigurationProvider {

    private static final boolean useCollector =
            Util.getBoolean("dlight.memory.collector", true); // NOI18N
    private static final boolean redirectStdErr =
            Util.getBoolean("dlight.memory.log.stderr", false); // NOI18N
    private static final boolean USE_SUNSTUDIO =
            Boolean.getBoolean("gizmo.mem.sunstudio"); // NOI18N
    private static final String TOOL_NAME = loc("MemoryTool.ToolName"); // NOI18N
    private static final Column totalColumn;
    private static final DataTableMetadata rawTableMetadata;


    static {
        final Column timestampColumn = new _Column(Long.class, "timestamp"); // NOI18N
        final Column kindColumn = new _Column(Integer.class, "kind"); // NOI18N
        final Column sizeColumn = new _Column(Integer.class, "size"); // NOI18N
        final Column addressColumn = new _Column(Long.class, "address"); // NOI18N
        final Column stackColumn = new _Column(Integer.class, "stackid"); // NOI18N

        totalColumn = new _Column(Integer.class, "total"); // NOI18N

        List<Column> columns = Arrays.asList(
                timestampColumn,
                kindColumn,
                sizeColumn,
                addressColumn,
                totalColumn,
                stackColumn);


        rawTableMetadata = new DataTableMetadata("mem", columns); // NOI18N
    }

    public MemoryToolConfigurationProvider() {
    }

    public DLightToolConfiguration create() {
        DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(TOOL_NAME);

        if (useCollector) {
            toolConfiguration.addDataCollectorConfiguration(initDataCollectorConfiguration());
        }

        // AK: Disable indicator data provider until issue with USR1 signal is resolved
        if (!(useCollector && USE_SUNSTUDIO)) {
            toolConfiguration.addIndicatorDataProviderConfiguration(initIndicatorDataProviderConfiguration());
        }
        
        toolConfiguration.addIndicatorConfiguration(initIndicatorConfiguration());

        return toolConfiguration;
    }

    private DataCollectorConfiguration initDataCollectorConfiguration() {
        DataCollectorConfiguration result = null;

        if (USE_SUNSTUDIO) {
            result = new SunStudioDCConfiguration(SunStudioDCConfiguration.CollectedInfo.MEMORY);
        } else {

            String scriptFile = Util.copyResource(getClass(),
                    Util.getBasePath(getClass()) + "/resources/mem.d"); // NOI18N

            DTDCConfiguration dataCollectorConfiguration =
                    new DTDCConfiguration(scriptFile, Arrays.asList(rawTableMetadata));

            // dataCollectorConfiguration.setIndicatorFiringFactor(1);
            // DTDCConfiguration collectorConfiguration = new DtraceDataAndStackCollector(dataCollectorConfiguration);
            dataCollectorConfiguration.setStackSupportEnabled(true);

            result = new MultipleDTDCConfiguration(dataCollectorConfiguration, "mem:"); // NOI18N
        }

        return result;
    }

    private IndicatorDataProviderConfiguration initIndicatorDataProviderConfiguration() {
        CLIODCConfiguration memIndicatorDataProvider = null;

        final DataTableMetadata indicatorTableMetadata =
                new DataTableMetadata("magent", Arrays.asList(totalColumn)); // NOI18N

        String monitor = NativeToolsUtil.getExecutable("mmonitor");
        String envVar = NativeToolsUtil.getLdPreloadEnvVarName();
        String agent = NativeToolsUtil.getSharefLibrary("magent");

        DLightLogger.instance.fine("Memory Indicator:\nmonitor:\n" + // NOI18N
                monitor + "\nagent:\n" + agent + "\n\n"); // NOI18N

        if (monitor != null && agent != null) {
            memIndicatorDataProvider = new CLIODCConfiguration(monitor,
                    " @PID " + (redirectStdErr ? " 2>/tmp/mmonitor.err" : ""), // NOI18N
                    new MAgentClioParser(totalColumn),
                    Arrays.asList(indicatorTableMetadata));

            Map<String, String> env = new LinkedHashMap<String, String>();
            env.put(envVar, agent);

            DLightLogger.instance.fine("SET " + envVar + "=" + agent);//NOI18N

            if (monitor != null && agent != null) {
                memIndicatorDataProvider.setDLightTargetExecutionEnv(env);
            }
        }

        return memIndicatorDataProvider;
    }

    private IndicatorConfiguration initIndicatorConfiguration() {
        IndicatorMetadata indicatorMetadata = null;
        indicatorMetadata = new IndicatorMetadata(Arrays.asList(totalColumn));

        MemoryIndicatorConfiguration indicatorConfiguration =
                new MemoryIndicatorConfiguration(indicatorMetadata, "total"); // NOI18N

        if (useCollector) {
            if (USE_SUNSTUDIO) {
                DataTableMetadata detailedViewTableMetadata =
                        SunStudioDCConfiguration.getMemTableMetadata(
                        SunStudioDCConfiguration.c_name,
                        SunStudioDCConfiguration.c_leakSize,
                        SunStudioDCConfiguration.c_leakCount);

                indicatorConfiguration.setVisualizerConfiguration(
                        new TableVisualizerConfiguration(detailedViewTableMetadata));
            } else {
                indicatorConfiguration.setVisualizerConfiguration(getDetails(rawTableMetadata));
            }
        }

        return indicatorConfiguration;
    }

    private VisualizerConfiguration getDetails(DataTableMetadata rawTableMetadata) {

        List<Column> viewColumns = Arrays.asList(
                (Column) new _Column(String.class, "func_name"), // NOI18N
                (Column) new _Column(Long.class, "leak")); // NOI18N

        String sql =
                "SELECT func.func_name as func_name, SUM(size) as leak " + // NOI18N
                "FROM mem, node AS node, func, ( " + // NOI18N
                "   SELECT MAX(timestamp) as leak_timestamp FROM mem, ( " + // NOI18N
                "       SELECT address as leak_address, sum(kind*size) AS leak_size FROM mem GROUP BY address HAVING sum(kind*size) > 0 " + // NOI18N
                "   ) AS vt1 WHERE address = leak_address GROUP BY address " + // NOI18N
                ") AS vt2 WHERE timestamp = leak_timestamp " + // NOI18N
                "AND stackid = node.node_id and node.func_id = func.func_id " + // NOI18N
                "GROUP BY node.func_id, func.func_name"; // NOI18N

        DataTableMetadata viewTableMetadata = new DataTableMetadata(
                "mem", viewColumns, sql, Arrays.asList(rawTableMetadata)); // NOI18N

        AdvancedTableViewVisualizerConfiguration tableVisualizerConfiguration =
                new AdvancedTableViewVisualizerConfiguration(viewTableMetadata, "func_name"); // NOI18N

        tableVisualizerConfiguration.setEmptyAnalyzeMessage(
                loc("DetailedView.EmptyAnalyzeMessage")); // NOI18N

        tableVisualizerConfiguration.setEmptyRunningMessage(
                loc("DetailedView.EmptyRunningMessage")); // NOI18N

        tableVisualizerConfiguration.setDefaultActionProvider();

        return tableVisualizerConfiguration;
    }

    private static class MAgentClioParser implements CLIOParser {

        private List<String> colNames;

        public MAgentClioParser(Column totalColumn) {
            colNames = Arrays.asList(totalColumn.getColumnName());
        }

        public DataRow process(String line) {
            DLightLogger.instance.fine("MAgentClioParser: " + line); //NOI18N
            if (line == null) {
                return null;
            }
            line = line.trim();
            if (!Character.isDigit(line.charAt(0))) {
                return null;
            }
            try {
                Long value = Long.parseLong(line);
                return new DataRow(colNames, Arrays.asList((Object) value));
            } catch (NumberFormatException e) {
                DLightLogger.instance.log(Level.WARNING, e.getMessage(), e);
            }
            return null;
        }

        int parseInt(String s) throws NumberFormatException {
            DLightLogger.assertTrue(s != null);
            return Integer.parseInt(s);
        }
    }

//    private static class TrussClioParser implements CLIOParser {
//
//        private List<String> colNames;
//        private long allocated;
//        private Logger log = DLightLogger.instance;
//
//        /**
//         * Maps a thread  (string pair process-thread printed by truss in 1-st column)
//         * to size (string hex representation, including 0x prefix)
//         */
//        private Map<String, String> threadToSize = new LinkedHashMap<String, String>();
//
//        private Map<Long, Long> addressToSize = new HashMap<Long, Long>();
//
//        public TrussClioParser(Column totalColumn) {
//            colNames = Arrays.asList(totalColumn.getColumnName());
//            allocated = 0;
//        }
//
//        public DataRow process(String line) {
//
//            if (line == null) {
//                return null;
//            }
//            String l = line.trim();
//            if (log.isLoggable(Level.FINE)) { log.fine(line); }
//
//            // The example of line is:
//            //      /1@1:   14.9686 -> libc:malloc(0x1388, 0x2710, 0x88, 0x1)
//            //      /1@1:   14.9700 <- libc:malloc() = 0x8064c08
//            //      /1@1:   14.9720 -> libc:free(0x8064c08, 0x2710, 0x88, 0x1)
//            //      /1@1:   14.9728 <- libc:free() = 0
//
//            String[] tokens = l.split("[ \t(),]+"); //NOI18N
//
//            if (tokens.length < 4) {
//                return null;
//            }
//            try {
//                String threadName = tokens[0];
//                String direction = tokens[2];
//                String funcName = tokens[3];
//                if ( "libc:malloc".equals(funcName)) { //NOI18N
//                    if ("->".equals(direction)) { //NOI18N
//                        String strSize = tokens[4];
//                        threadToSize.put(threadName, strSize);
//                    } else if ("<-".equals(direction)) { //NOI18N
//                        String strSize = threadToSize.remove(threadName);
//                        if ("0".equals(tokens[5])) {
//                            if (log.isLoggable(Level.FINE)) { log.fine("alloc FAILED"); } //NOI18N
//                        } else {
//                            if (strSize != null) {
//                                int size = parseHex(strSize);
//                                allocated += size;
//                                long address = parseHex(tokens[5]);
//                                addressToSize.put(new Long(address), new Long(size));
//                                if (log.isLoggable(Level.FINE)) { log.fine(String.format("allocated %d address %X total %d extents %d",  //NOI18N
//                                        size, address, allocated, addressToSize.size())); }
//                                return new DataRow(colNames, Arrays.asList(new Long[] { allocated }));
//                            }
//                        }
//                    }
//                }
//                else if ( "libc:free".equals(funcName) && "->".equals(direction)) { //NOI18N
//                    if ("0x0".equals(tokens[4])) {
//                        if (log.isLoggable(Level.FINE)) { log.fine("free FAILED"); } //NOI18N
//                    } else {
//                        long address = parseHex(tokens[4]);
//                        Long size = addressToSize.remove(address);
//                        if (size == null) {
//                            if (log.isLoggable(Level.FINE)) {
//                                log.fine(String.format("free: wrong address %X", address)); //NOI18N
//                            }
//                        } else {
//                            allocated -= size.longValue();
//                            if (log.isLoggable(Level.FINE)) {
//                                log.fine(String.format("freed %d total %d extents %d", size.longValue(), allocated, addressToSize.size())); //NOI18N
//                            }
//                            return new DataRow(colNames, Arrays.asList(new Long[] { allocated }));
//                        }
//                    }
//                }
//            } catch (NumberFormatException nfe) {
//                nfe.printStackTrace();
//            }
//            return null;
//        }
//
//        int parseHex(String s) throws NumberFormatException {
//            DLightLogger.assertTrue(s != null);
//            DLightLogger.assertTrue(s.startsWith("0x"));
//            return Integer.parseInt(s.substring(2), 16);
//        }
//    }
    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                MemoryToolConfigurationProvider.class, key, params);
    }

    private static class _Column extends Column {

        public _Column(Class clazz, String name) {
            super(name, clazz, loc("MemoryTool.ColumnName." + name), null); // NOI18N
        }
    }
}
