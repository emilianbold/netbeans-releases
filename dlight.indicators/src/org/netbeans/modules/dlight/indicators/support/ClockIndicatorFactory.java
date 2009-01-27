/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicators.support;

import org.netbeans.modules.dlight.indicators.ClockIndicatorConfiguration;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorFactory;



/**
 *
 * @author mt154047
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.dlight.spi.indicator.IndicatorFactory.class)
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
