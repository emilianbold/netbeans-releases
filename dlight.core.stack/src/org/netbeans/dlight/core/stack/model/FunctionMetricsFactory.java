/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.dlight.core.stack.model;

import java.util.HashMap;
import org.netbeans.dlight.core.stack.model.FunctionMetric.FunctionMetricConfiguration;

/**
 *
 * @author masha
 */
public final class FunctionMetricsFactory {
  private static FunctionMetricsFactory instance = null;
  private final HashMap<FunctionMetricConfiguration, FunctionMetric> metricsCache = new HashMap<FunctionMetricConfiguration, FunctionMetric>();


  private FunctionMetricsFactory() {
  }

  public static FunctionMetricsFactory getInstance(){
    if (instance == null){
      instance = new FunctionMetricsFactory();
    }
    return instance;
  }


  public  FunctionMetric create(FunctionMetricConfiguration metricConfiguration){
    if (metricsCache.containsKey(metricConfiguration)){
      return metricsCache.get(metricConfiguration);
    }

    FunctionMetric metric = new FunctionMetric(metricConfiguration);
    metricsCache.put(metricConfiguration, metric);
    return metric;
  }

}
