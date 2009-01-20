/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.dlight.dtrace.collector;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.dlight.core.stack.model.FunctionMetric;
import org.netbeans.dlight.core.stack.storage.SQLStackStorage;
import org.netbeans.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.dlight.dtrace.collector.impl.DTSTDCConfigurationAccessor;
import org.netbeans.dlight.dtrace.collector.support.DtraceStackDataCollector;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.modules.dlight.util.Util;

/**
 *
 * @author masha
 */
public final class DTSTDCConfiguration implements  DataCollectorConfiguration {
  static final String DTSTDC_CONFIGURATION_ID = "DtraceStackCollectorConfigurationId";
  private DTDCConfiguration configuration;
  

  static {
    DTSTDCConfigurationAccessor.setDefault(new DTSTDCConfigurationAccessorImpl());
  }

  public DTSTDCConfiguration() {
    configuration = new DTDCConfiguration(Util.copyResource(DtraceStackDataCollector.class,
            "org/netbeans/dlight/dtrace/resources/calls.d"), new ArrayList<DataTableMetadata>());
  }


  public void setOutputPrefix(String prefix){
    configuration.setOutputPrefix(prefix);
  }

  public String getID() {
    return DTSTDC_CONFIGURATION_ID;
  }

  public static final DataTableMetadata getMetadaData() {
    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column("name", String.class, "Function Name", null));
    //e.user:i.user:i.sync:i.syncn:name
//    List<Column> tableColumns = new ArrayList<Column>();

    List<FunctionMetric> metricsList = SQLStackStorage.METRICS;
    for (FunctionMetric metric : metricsList) {
      columns.add(new Column(metric.getMetricID(), metric.getMetricValueClass(), metric.getMetricDisplayedName(), null));
    }
    DataTableMetadata result = new DataTableMetadata(StackDataStorage.STACK_METADATA_VIEW_NAME, columns);
    return result;
  }

  DTDCConfiguration getDTDCConfiguration(){
    return configuration;
  }

  private static final class DTSTDCConfigurationAccessorImpl extends DTSTDCConfigurationAccessor {

    @Override
    public DTDCConfiguration getDTDCConfiguration(DTSTDCConfiguration conf) {
      return conf.getDTDCConfiguration();
    }

    @Override
    public String getID() {
      return DTSTDC_CONFIGURATION_ID;
    }
  }
}
