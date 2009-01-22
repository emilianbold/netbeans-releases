/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicators.support;

import org.netbeans.modules.dlight.indicator.api.ConfigurationData;
import org.netbeans.modules.dlight.indicators.BarIndicatorConfiguration;

/**
 *
 * @author mt154047
 */
public abstract class BarIndicatorConfigurationAccessor {

  private static volatile BarIndicatorConfigurationAccessor DEFAULT;

  public static BarIndicatorConfigurationAccessor getDefault() {
    BarIndicatorConfigurationAccessor a = DEFAULT;
    if (a != null) {
      return a;
    }

    try {
      Class.forName(BarIndicatorConfiguration.class.getName(), true, BarIndicatorConfiguration.class.getClassLoader());//
    } catch (Exception e) {
    }
    return DEFAULT;
  }

  public static void setDefault(BarIndicatorConfigurationAccessor accessor) {
    if (DEFAULT != null) {
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public BarIndicatorConfigurationAccessor() {
  }

  public abstract ConfigurationData getConfigurationData(BarIndicatorConfiguration configuration);
}
