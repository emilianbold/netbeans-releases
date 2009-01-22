/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.indicator.api.impl;

import org.netbeans.modules.dlight.indicator.api.ConfigurationData;
import org.netbeans.modules.dlight.indicator.api.IndicatorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorMetadata;
import org.netbeans.modules.dlight.visualizer.api.VisualizerConfiguration;

/**
 *
 * @author mt154047
 */
public abstract class IndicatorConfigurationAccessor {

  private static volatile IndicatorConfigurationAccessor DEFAULT;

  public static IndicatorConfigurationAccessor getDefault() {
    IndicatorConfigurationAccessor a = DEFAULT;
    if (a != null) {
      return a;
    }

    try {
      Class.forName(IndicatorConfiguration.class.getName(), true, IndicatorConfiguration.class.getClassLoader());//
    } catch (Exception e) {
    }
    return DEFAULT;
  }

  public static void setDefault(IndicatorConfigurationAccessor accessor) {
    if (DEFAULT != null) {
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public IndicatorConfigurationAccessor() {
  }

  public abstract ConfigurationData getConfigurationData(IndicatorConfiguration configuration);

  public abstract IndicatorMetadata getIndicatorMetadata(IndicatorConfiguration configuration);

  public abstract VisualizerConfiguration getVisualizerConfiguration(IndicatorConfiguration configuration);
}
