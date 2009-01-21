/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.cpu;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.netbeans.dlight.collector.stdout.api.CLIODCConfiguration;
import org.netbeans.dlight.collector.stdout.api.CLIOParser;
import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.dlight.perfan.SunStudioDCConfiguration;
import org.netbeans.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorMetadata;
import org.netbeans.modules.dlight.tool.spi.DLightToolConfigurationProvider;
import org.netbeans.modules.dlight.storage.api.DataRow;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.modules.dlight.tool.api.DLightToolConfiguration;
import org.netbeans.modules.dlight.util.Util;

/**
 *
 * @author mt154047
 */
public final class DLightCPUToolConfigurationProvider implements DLightToolConfigurationProvider {

  private static final boolean USE_DTRACE = Boolean.getBoolean("gizmo.cpu.dtrace");

  public DLightToolConfiguration create() {
    final String toolName = "CPU Monitor";
    final DLightToolConfiguration toolConfiguration = new DLightToolConfiguration(toolName);
    List<Column> indicatorColumns = Arrays.asList(
            new Column("utime", Float.class, "User Time", null),
            new Column("stime", Float.class, "System Time", null),
            new Column("wtime", Float.class, "Wait Time", null));

    final DataTableMetadata dbTableMetadata = new DataTableMetadata("prstat", indicatorColumns);
    CLIODCConfiguration clioCollectorConfiguration = new CLIODCConfiguration("/bin/prstat", "-mv -p @PID -c 1", new MyCLIOParser(), Arrays.asList(dbTableMetadata));
    toolConfiguration.addIndicatorDataProviderConfiguration(clioCollectorConfiguration);

    IndicatorMetadata indicatorMetadata = new IndicatorMetadata(indicatorColumns);
    HashMap<String, Object> indConfiguration = new HashMap<String, Object>();
    indConfiguration.put("aggregation", "avrg");
    DataTableMetadata functionsListMetaData = null;
    if (USE_DTRACE) {
      Column cpuId = new Column("cpu_id", Integer.class, "CPU", null);
      Column threadId = new Column("thread_id", Integer.class, "Thread", null);
      Column timestamp = new Column("time_stamp", Long.class, "Timestamp", null);
      Column stackId = new Column("leaf_id", Integer.class, "Stack", null);
      functionsListMetaData = new DataTableMetadata("CallStack", Arrays.asList(cpuId, threadId, timestamp, stackId));
      String scriptFile = Util.copyResource(getClass(), Util.getBasePath(getClass()) + "/resources/calls.d");
      DTDCConfiguration dtraceDataCollectorConfiguration = new DTDCConfiguration(scriptFile, Arrays.asList(functionsListMetaData));
      dtraceDataCollectorConfiguration.setStackSupportEnabled(true);
      toolConfiguration.addDataCollectorConfiguration(new MultipleDTDCConfiguration(dtraceDataCollectorConfiguration, "cpu:"));
    } else {
      SunStudioDCConfiguration sunStudioConfiguration = new SunStudioDCConfiguration(Arrays.asList(SunStudioDCConfiguration.CollectedInfo.FUNCTIONS_LIST));
      toolConfiguration.addDataCollectorConfiguration(sunStudioConfiguration);
      functionsListMetaData = SunStudioDCConfiguration.getDataTableMetaDataFor(Arrays.asList(SunStudioDCConfiguration.CollectedInfo.FUNCTIONS_LIST));
    }

    CpuIndicator cpu2 = new CpuIndicator(indicatorMetadata);
    toolConfiguration.addIndicator(cpu2);
    final DataTableMetadata data = functionsListMetaData;
    cpu2.setVisualizerConfiguration(new CallersCalleesVisualizerConfiguration(data, SunStudioDCConfiguration.getFunctionNameColumnName(), true));
    return toolConfiguration;

  }

  class MyCLIOParser implements CLIOParser {

    private final List<String> colnames = Arrays.asList(new String[]{
              "utime",
              "stime",
              "wtime"
            });
    Float utime, stime, wtime;

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
        utime = Float.valueOf(tokens[2]);
        stime = Float.valueOf(tokens[3]);
        wtime = Float.valueOf(tokens[8]);
      } catch (NumberFormatException ex) {
        return null;
      }

      return new DataRow(colnames, Arrays.asList(new Float[]{utime, stime, wtime}));
    }
  }
}
