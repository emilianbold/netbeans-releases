/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.core.stack.api.support;

import org.netbeans.modules.dlight.core.stack.api.*;
import java.util.HashMap;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric.FunctionMetricConfiguration;
import org.netbeans.modules.dlight.core.stack.api.impl.FunctionMetricAccessor;

/**
 * Factory to create {@link org.netbeans.modules.dlight.core.stack.api.FunctionMetric} instance on
 * the base of {@link org.netbeans.modules.dlight.core.stack.api.FunctionMetric.FunctionMetricConfiguration}
 */
public final class FunctionMetricsFactory {
  private static FunctionMetricsFactory instance = null;
  private final HashMap<FunctionMetricConfiguration, FunctionMetric> metricsCache = new HashMap<FunctionMetricConfiguration, FunctionMetric>();


  private FunctionMetricsFactory() {
  }

  /**
   * Singlton method
   * @return instance
   */
  public static FunctionMetricsFactory getInstance(){
    if (instance == null){
      instance = new FunctionMetricsFactory();
    }
    return instance;
  }


  /**
   * Return function metric for the <code>metricConfiguration</code>
   * @param metricConfiguration configuration to create metric for
   * @return FunctionMetric instance.
   */
  public  FunctionMetric getFunctionMetric(FunctionMetricConfiguration metricConfiguration){
    if (metricsCache.containsKey(metricConfiguration)){
      return metricsCache.get(metricConfiguration);
    }

    FunctionMetric metric = FunctionMetricAccessor.getDefault().createNew(metricConfiguration);
    metricsCache.put(metricConfiguration, metric);
    return metric;
  }

}
