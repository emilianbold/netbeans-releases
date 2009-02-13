/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicators.support;

import org.netbeans.modules.dlight.indicators.BarIndicatorConfiguration;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorFactory;

/**
 *
 * @author mt154047
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.dlight.spi.indicator.IndicatorFactory.class)
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
