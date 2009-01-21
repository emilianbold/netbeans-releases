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
package org.netbeans.dlight.sync;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.dlight.indicators.BarIndicator;
import org.netbeans.dlight.indicators.BarIndicatorConfig;
import org.netbeans.dlight.visualizers.api.TableVisualizerConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.modules.dlight.tool.api.DLightToolConfiguration;
import org.netbeans.modules.dlight.tool.spi.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizer.api.VisualizerConfiguration;




/**
 *
 * @author Vladimir Kvashin
 */
public final class SyncToolConfigurationProvider implements DLightToolConfigurationProvider {
  private static final String TOOL_NAME = "Sync Tool";//NOI18N

  public SyncToolConfigurationProvider() {
  }


  public DLightToolConfiguration create() {
    final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(TOOL_NAME);
    Column timestampColumn = new Column("timestamp", Long.class, "Timestamp", null);
    Column waiterColumn = new Column("waiter", Integer.class, "Waiting thread", null);
    Column mutexColumn = new Column("mutex", Long.class, "Mutex address", null);
    Column blockerColumn = new Column("blocker", Integer.class, "Blocker thread", null);
    Column timeColumn = new Column("time", Long.class, "Blocked time", null);
    Column stackColumn = new Column("stackid", Integer.class, "Stack ID", null);

    List<Column> rawColumns = Arrays.asList(
            timestampColumn,
            waiterColumn,
            mutexColumn,
            blockerColumn,
            timeColumn,
            stackColumn);

    String scriptFile = Util.copyResource(getClass(), Util.getBasePath(getClass()) + "/resources/sync.d");

    final DataTableMetadata rawTableMetadata = new DataTableMetadata("sync", rawColumns);
    DTDCConfiguration dataCollectorConfiguration = new DTDCConfiguration(scriptFile, Arrays.asList(rawTableMetadata));
    dataCollectorConfiguration.setStackSupportEnabled(true);
    dataCollectorConfiguration.setIndicatorFiringFactor(1);
    MultipleDTDCConfiguration multipleDTDCConfiguration = new MultipleDTDCConfiguration(dataCollectorConfiguration, "sync:");
    toolConfiguration.addDataCollectorConfiguration(multipleDTDCConfiguration);
    toolConfiguration.addIndicatorDataProviderConfiguration(multipleDTDCConfiguration);

    List<Column> indicatorColumns = Arrays.asList(
            timeColumn);
    IndicatorMetadata indicatorMetadata = new IndicatorMetadata(indicatorColumns);

    HashMap<String, Object> configuration = new HashMap<String, Object>();
    configuration.put("aggregation", "avrg");

    BarIndicator indicator = new BarIndicator(indicatorMetadata, new BarIndicatorConfig(configuration));
    toolConfiguration.addIndicator(indicator);
    indicator.setVisualizerConfiguration(getDetails(rawTableMetadata));
    return toolConfiguration;
  }


  private VisualizerConfiguration getDetails(DataTableMetadata rawTableMetadata) {
//        DLightManager.getDefault().openVisualizer(SyncToolConfigurationProvider.this, TableVisualizer.id, new TableVisualizerConfiguration(rawTableMetadata));
    List<Column> viewColumns = Arrays.asList(
            new Column("func_name", String.class, "Function", null),
            new Column("time", Long.class, "Time, ms", null),
            new Column("count", Long.class, "Count", null));
//        String sql = "SELECT func.func_name as func_name, SUM(sync.time) as time, COUNT(*) as count" +
//                " FROM sync, node AS node1, node AS node2, func" +
//                " WHERE  sync.stackid = node1.node_id and node1.caller_id = node2.node_id and node2.func_id = func.func_id" +
//                " GROUP BY node2.func_id";
    String sql = "SELECT func.func_name as func_name, SUM(sync.time/1000000) as time, COUNT(*) as count" +
            " FROM sync, node AS node, func" +
            " WHERE  sync.stackid = node.node_id and node.func_id = func.func_id" +
            " GROUP BY node.func_id";

    final DataTableMetadata viewTableMetadata = new DataTableMetadata("sync", viewColumns, sql, Arrays.asList(rawTableMetadata));
    return new TableVisualizerConfiguration(viewTableMetadata);
  }

}
