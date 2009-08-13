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
package org.netbeans.modules.dlight.webstack;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.indicators.ClockIndicatorConfiguration;
import org.netbeans.modules.dlight.spi.support.TimerIDPConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public final class MySQLConfigurationProvider implements DLightToolConfigurationProvider {

  public MySQLConfigurationProvider() {
  }

  public DLightToolConfiguration create() {
    final String toolName = getMessage("MysqlTool.Name"); // NOI18N
    final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(toolName);
    List<Column> mysqlColumns = Arrays.asList(
            new Column("timestamp", Long.class, getMessage("Column.Timestamp"), null), // NOI18N
            new Column("query", String.class, getMessage("Column.SqlQuery"), null), // NOI18N
            new Column("time", Double.class, getMessage("Column.ExecutionTime"), null)); // NOI18N
    final DataTableMetadata mysqlDatatableMetadata = new DataTableMetadata("mysql", mysqlColumns, null); // NOI18N
    DTDCConfiguration dcConfiguration = new DTDCConfiguration(Util.copyResource(PhpConfigurationProvider.class,
            "org/netbeans/modules/dlight/webstack/resources/script_1.d"), Arrays.asList(mysqlDatatableMetadata)); // NOI18N
    dcConfiguration.setRequiredDTracePrivileges(Arrays.asList(DTDCConfiguration.DTRACE_KERNEL, DTDCConfiguration.DTRACE_PROC, DTDCConfiguration.DTRACE_USER, "proc_owner")); // NOI18N
    dcConfiguration.setScriptArgs("`pgrep -x mysqld`"); // NOI18N
    toolConfiguration.addDataCollectorConfiguration(dcConfiguration);
    toolConfiguration.addIndicatorDataProviderConfiguration(new TimerIDPConfiguration());
    IndicatorMetadata indicatorMetadata1 = new IndicatorMetadata(Arrays.asList(TimerIDPConfiguration.TIME_INFO));
    ClockIndicatorConfiguration clockIndicator = new ClockIndicatorConfiguration(indicatorMetadata1);
    clockIndicator.addVisualizerConfiguration(new TableVisualizerConfiguration(mysqlDatatableMetadata));
    toolConfiguration.addIndicatorConfiguration(clockIndicator);
    return toolConfiguration;
  }

  private static String getMessage(String name) {
      return NbBundle.getMessage(MySQLConfigurationProvider.class, name);
  }
}
