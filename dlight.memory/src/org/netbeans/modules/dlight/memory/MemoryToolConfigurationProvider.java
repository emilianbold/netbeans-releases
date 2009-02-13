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
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import java.util.logging.Level;
import org.netbeans.modules.dlight.collector.stdout.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIOParser;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.dlight.util.Util;

/**
 * 
 * @author Vladimir Kvashin
 */
public final class MemoryToolConfigurationProvider implements DLightToolConfigurationProvider {

    private static final boolean useCollector = Util.getBoolean("dlight.memory.collector", true);
    private static final boolean redirectStdErr = Util.getBoolean("dlight.memory.log.stderr", false);

    public MemoryToolConfigurationProvider() {
    }

    public DLightToolConfiguration create() {
        final String toolName = "Memory Tool";
        final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(toolName);
        Column totalColumn = new Column("total", Integer.class, "Heap size", null);
        DataTableMetadata rawTableMetadata = null;
        if (useCollector) {
            Column timestampColumn = new Column("timestamp", Long.class, "Timestamp", null);
            Column kindColumn = new Column("kind", Integer.class, "Kind", null);
            Column sizeColumn = new Column("size", Integer.class, "Size", null);
            Column addressColumn = new Column("address", Integer.class, "Address", null);
            Column stackColumn = new Column("stackid", Integer.class, "Stack ID", null);

            List<Column> columns = Arrays.asList(
                    timestampColumn,
                    kindColumn,
                    sizeColumn,
                    addressColumn,
                    totalColumn,
                    stackColumn);

            String scriptFile = Util.copyResource(getClass(), Util.getBasePath(getClass()) + "/resources/mem.d");

            rawTableMetadata = new DataTableMetadata("mem", columns);
            DTDCConfiguration dataCollectorConfiguration = new DTDCConfiguration(scriptFile, Arrays.asList(rawTableMetadata));
            dataCollectorConfiguration.setStackSupportEnabled(true);
            //dataCollectorConfiguration.setIndicatorFiringFactor(1);
            // DTDCConfiguration collectorConfiguration = new DtraceDataAndStackCollector(dataCollectorConfiguration);
            MultipleDTDCConfiguration multipleDTDCConfiguration = new MultipleDTDCConfiguration(dataCollectorConfiguration, "mem:");
            toolConfiguration.addDataCollectorConfiguration(multipleDTDCConfiguration);
        }
        List<Column> indicatorColumns = Arrays.asList(
                totalColumn);
        IndicatorMetadata indicatorMetadata = new IndicatorMetadata(indicatorColumns);
        DataTableMetadata indicatorTableMetadata = new DataTableMetadata("truss", indicatorColumns);

        String monitor = MemoryMonitorUtil.getMonitorCmd();
        String envVar = MemoryMonitorUtil.getEnvVar();
        String agent = MemoryMonitorUtil.getAgentLib();
        DLightLogger.instance.fine("Memory Indicator:\nmonitor:\n" + monitor + "\nagent:\n" + agent + "\n\n");
        if (monitor != null && agent != null) {
            CLIODCConfiguration clioCollectorConfiguration = new CLIODCConfiguration(monitor,
                    " @PID " + (redirectStdErr ? " 2>/tmp/mmonitor.err" : ""),
                    new MAgentClioParser(totalColumn), Arrays.asList(indicatorTableMetadata));
            Map<String, String> env = new LinkedHashMap<String, String>();
            env.put(envVar, agent);
            DLightLogger.instance.fine("SET " + envVar + "=" + agent);//NOI18N
            clioCollectorConfiguration.setDLightTargetExecutionEnv(env);
            toolConfiguration.addIndicatorDataProviderConfiguration(clioCollectorConfiguration);
            MemoryIndicatorConfiguration indicator = new MemoryIndicatorConfiguration(indicatorMetadata, "total");
            if (useCollector) {
                indicator.setVisualizerConfiguration(getDetails(rawTableMetadata));
            }
            toolConfiguration.addIndicatorConfiguration(indicator);
        }

        return toolConfiguration;
    }

    private VisualizerConfiguration getDetails(DataTableMetadata rawTableMetadata) {

        List<Column> viewColumns = Arrays.asList(
                new Column("func_name", String.class, "Function", null),
                new Column("leak", Long.class, "Leak", null));

        String sql =
            "SELECT func.func_name as func_name, SUM(size) as leak " +
            "FROM mem, node AS node, func, ( " +
            "   SELECT MAX(timestamp) as leak_timestamp FROM mem, ( " +
            "       SELECT address as leak_address, sum(kind*size) AS leak_size FROM mem GROUP BY address HAVING sum(kind*size) > 0 " +
            "   ) AS vt1 WHERE address = leak_address GROUP BY address " +
            ") AS vt2 WHERE timestamp = leak_timestamp " +
            "AND stackid = node.node_id and node.func_id = func.func_id " +
            "GROUP BY node.func_id, func.func_name";

        DataTableMetadata viewTableMetadata = new DataTableMetadata("mem", viewColumns, sql, Arrays.asList(rawTableMetadata));
        return new TableVisualizerConfiguration(viewTableMetadata);
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
                long value = Integer.parseInt(line);
                return new DataRow(colNames, Arrays.asList(new Long[] { value }));
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
}
