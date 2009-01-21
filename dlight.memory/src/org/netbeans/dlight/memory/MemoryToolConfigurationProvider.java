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
package org.netbeans.dlight.memory;

import java.util.Arrays;
import java.util.List;
import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.dlight.visualizers.api.TableVisualizerConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.modules.dlight.tool.api.DLightToolConfiguration;
import org.netbeans.modules.dlight.tool.spi.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.Util;

/**
 * 
 * @author Vladimir Kvashin
 */
public final class MemoryToolConfigurationProvider implements DLightToolConfigurationProvider {

  public MemoryToolConfigurationProvider() {
  
  }

  public DLightToolConfiguration create() {
    final String toolName = "Memory Tool";
    final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(toolName);
    Column timestampColumn = new Column("timestamp", Long.class, "Timestamp", null);
    Column timeColumn = new Column("kind", Integer.class, "Kind", null);
    Column sizeColumn = new Column("size", Integer.class, "Size", null);
    Column addressColumn = new Column("address", Integer.class, "Address", null);
    Column totalColumn = new Column("total", Integer.class, "Heap size", null);
    Column stackColumn = new Column("stackid", Integer.class, "Stack ID", null);

    List<Column> columns = Arrays.asList(
            timestampColumn,
            timeColumn,
            sizeColumn,
            addressColumn,
            totalColumn,
            stackColumn);

    String scriptFile = Util.copyResource(getClass(), Util.getBasePath(getClass()) + "/resources/mem.d");

    final DataTableMetadata dbTableMetadata = new DataTableMetadata("mem", columns);
    DTDCConfiguration dataCollectorConfiguration = new DTDCConfiguration(scriptFile, Arrays.asList(dbTableMetadata));
    dataCollectorConfiguration.setStackSupportEnabled(true);
    dataCollectorConfiguration.setIndicatorFiringFactor(1);
   // DTDCConfiguration collectorConfiguration = new DtraceDataAndStackCollector(dataCollectorConfiguration);
    MultipleDTDCConfiguration multipleDTDCConfiguration = new MultipleDTDCConfiguration(dataCollectorConfiguration, "mem:");
    toolConfiguration.addDataCollectorConfiguration(multipleDTDCConfiguration);
    toolConfiguration.addIndicatorDataProviderConfiguration(multipleDTDCConfiguration);

    List<Column> indicatorColumns = Arrays.asList(
            totalColumn);
    IndicatorMetadata indicatorMetadata = new IndicatorMetadata(indicatorColumns);

//        HashMap<String, Object> configuration = new HashMap<String, Object>();
//        configuration.put("aggregation", "avrg");
//        BarIndicator indicator = new BarIndicator(indicatorMetadata, new BarIndicatorConfig(configuration));
    MemoryIndicator indicator = new MemoryIndicator(indicatorMetadata, "total");

    toolConfiguration.addIndicator(indicator);
    indicator.setVisualizerConfiguration(new TableVisualizerConfiguration(dbTableMetadata));
   
    return toolConfiguration;
  }
}
