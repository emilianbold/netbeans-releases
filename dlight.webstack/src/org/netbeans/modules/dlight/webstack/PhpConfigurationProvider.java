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
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.CLIOParser;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.indicators.BarIndicatorConfiguration;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;

/**
 *
 * @author mt154047
 */
public final class PhpConfigurationProvider implements DLightToolConfigurationProvider {

  public PhpConfigurationProvider() {
  }

  public DLightToolConfiguration create() {
    final String toolName = "Php D-Light Tool";
    final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(toolName);
    List<Column> indicatorColumns = Arrays.asList(
            new Column("utime", Double.class, "User Time", null),
            new Column("stime", Double.class, "System Time", null),
            new Column("wtime", Double.class, "Wait Time", null));

    final DataTableMetadata dbTableMetadata = new DataTableMetadata("prstat", indicatorColumns);
    CLIODCConfiguration clioCollectorConfiguration = new CLIODCConfiguration("/bin/prstat", "-mv -p `pgrep -x mysqld` -c 1", new MyCLIOParser(), Arrays.asList(dbTableMetadata));
    toolConfiguration.addIndicatorDataProviderConfiguration(clioCollectorConfiguration);


    IndicatorMetadata indicatorMetadata = new IndicatorMetadata(indicatorColumns);
    BarIndicatorConfiguration cpuIndicator = new BarIndicatorConfiguration(indicatorMetadata);
    toolConfiguration.addIndicatorConfiguration(cpuIndicator);

    List<Column> phpColumns = Arrays.asList(
            new Column("kind", Integer.class, "Kind", null),
            new Column("timestamp", Long.class, "Timestamp", null),
            new Column("function_name", String.class, "FunctionName", null),
            new Column("source_file", String.class, "PHP script", null),
            new Column("line_number", Integer.class, "Line Number", null),
            new Column("class_name", String.class, "Class", null));

/// "`pgrep -x mysqld`"
    final DataTableMetadata phpDatatableMetadata = new DataTableMetadata("php", phpColumns);
    DTDCConfiguration dcConfiguration = new DTDCConfiguration(Util.copyResource(PhpConfigurationProvider.class,
            "org/netbeans/modules/dlight/webstack/resources/script.d"), Arrays.asList(phpDatatableMetadata));
    dcConfiguration.setRequiredDTracePrivileges(Arrays.asList(DTDCConfiguration.DTRACE_KERNEL, DTDCConfiguration.DTRACE_PROC, DTDCConfiguration.DTRACE_USER, "proc_owner"));
    toolConfiguration.addDataCollectorConfiguration(dcConfiguration);
    cpuIndicator.addVisualizerConfiguration(new TableVisualizerConfiguration(phpDatatableMetadata));

    return toolConfiguration;
  }

  class MyCLIOParser implements CLIOParser {

    private final List<String> colnames = Arrays.asList(new String[]{
              "utime",
              "stime",
              "wtime"
            });
    Double utime, stime, wtime;

    public DataRow process(String line) {
      if (line == null) {
        return null;
      }
      String l = line.trim();
      l = l.replaceAll(",", ".");
      String[] tokens = l.split("[ \t]+");

      if (tokens.length != 15) {
        return null;
      }

      try {
        utime = Double.valueOf(tokens[2]);
        stime = Double.valueOf(tokens[3]);
        wtime = Double.valueOf(tokens[8]);
      } catch (NumberFormatException ex) {
        return null;
      }

      return new DataRow(colnames, Arrays.asList(new Double[]{utime, stime, wtime}));
    }
  }
}
