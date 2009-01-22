/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.dtrace.collector.support;

import org.netbeans.modules.dlight.collector.spi.DataCollector;
import org.netbeans.modules.dlight.collector.spi.DataCollectorFactory;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.impl.MultipleDTDCConfigurationAccessor;

/**
 *
 * @author mt154047
 */
public final class MultipleDtraceDataCollectorFactory implements DataCollectorFactory<MultipleDTDCConfiguration>{

  public DataCollector<MultipleDTDCConfiguration> create(MultipleDTDCConfiguration configuration) {
    return MultipleDtraceDataCollectorSupport.getInstance().getCollector(configuration);
  }

 public String getID() {
    return MultipleDTDCConfigurationAccessor.getDefault().getID();
  }

}
