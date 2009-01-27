/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicators.support;

import org.netbeans.modules.dlight.indicator.spi.Indicator;
import org.netbeans.modules.dlight.indicator.spi.IndicatorFactory;
import org.netbeans.modules.dlight.indicators.BarIndicatorConfiguration;

/**
 *
 * @author mt154047
 */
public final class BarIndicatorFactory implements IndicatorFactory<BarIndicatorConfiguration>{

  @Override
  public Indicator<BarIndicatorConfiguration> create(BarIndicatorConfiguration configuration) {
    return new BarIndicator(configuration);
  }

  @Override
  public String getID() {
    return IndicatorConfigurationIDs.BAR_ID;
  }

}
