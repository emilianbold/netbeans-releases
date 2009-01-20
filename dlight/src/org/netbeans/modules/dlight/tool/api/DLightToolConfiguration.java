/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.tool.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.indicator.api.Indicator;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.tool.api.impl.DLightToolConfigurationAccessor;

/**
 * This class represents configuration object to create DLightTool instance on the base of
 */
public final class DLightToolConfiguration {

  private final String toolName;
  private final List<DataCollectorConfiguration> dataCollectors;
  private final List<IndicatorDataProviderConfiguration> indicatorDataProvidersConfiguration;
  private final List<Indicator> indicators;

  static{
    DLightToolConfigurationAccessor.setDefault(new DLightToolConfigurationAccessorIml());
  }

  public DLightToolConfiguration(String toolName) {
    this.toolName = toolName;
    dataCollectors = Collections.synchronizedList(new ArrayList<DataCollectorConfiguration>());
    indicators = Collections.synchronizedList(new ArrayList<Indicator>());
    indicatorDataProvidersConfiguration = Collections.synchronizedList(new ArrayList<IndicatorDataProviderConfiguration>());
  }

  public void addDataCollectorConfiguration(DataCollectorConfiguration collector) {
    dataCollectors.add(collector);
  }

  public void addIndicator(Indicator indicator) {
    indicators.add(indicator);
  }

  public void addIndicatorDataProviderConfiguration(IndicatorDataProviderConfiguration indDataProvider) {
    indicatorDataProvidersConfiguration.add(indDataProvider);
  }

  List<DataCollectorConfiguration> getDataCollectors() {
    return dataCollectors;
  }

  List<IndicatorDataProviderConfiguration> getIndicatorDataProviders() {
    return indicatorDataProvidersConfiguration;
  }

  List<Indicator> getIndicators() {
    return indicators;
  }

  String getToolName() {
    return toolName;
  }

  private static final class DLightToolConfigurationAccessorIml extends DLightToolConfigurationAccessor{

    @Override
    public List<DataCollectorConfiguration> getDataCollectors(DLightToolConfiguration conf) {
      return conf.getDataCollectors();
    }

    @Override
    public List<IndicatorDataProviderConfiguration> getIndicatorDataProviders(DLightToolConfiguration conf) {
      return conf.getIndicatorDataProviders();
    }

    @Override
    public List<Indicator> getIndicators(DLightToolConfiguration conf) {
      return conf.getIndicators();
    }

    @Override
    public String getToolName(DLightToolConfiguration conf) {
      return conf.getToolName();
    }
    
  }
}
