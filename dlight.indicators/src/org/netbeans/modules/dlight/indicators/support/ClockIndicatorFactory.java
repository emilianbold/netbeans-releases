/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicators.support;

import org.netbeans.modules.dlight.indicator.spi.Indicator;
import org.netbeans.modules.dlight.indicator.spi.IndicatorFactory;
import org.netbeans.modules.dlight.indicators.ClockIndicatorConfiguration;

/**
 *
 * @author mt154047
 */
public class ClockIndicatorFactory implements IndicatorFactory<ClockIndicatorConfiguration>{

  @Override
  public Indicator<ClockIndicatorConfiguration> create(ClockIndicatorConfiguration configuration) {
    return new ClockIndicator(configuration);
  }

  @Override
  public String getID() {
    return IndicatorConfigurationIDs.BAR_ID;
  }

}
