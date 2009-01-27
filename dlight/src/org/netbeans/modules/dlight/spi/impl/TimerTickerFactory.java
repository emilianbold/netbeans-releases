/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.spi.impl;

import org.netbeans.modules.dlight.spi.support.TimerIDPConfiguration;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProviderFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service=IndicatorDataProviderFactory.class)
public final  class TimerTickerFactory implements IndicatorDataProviderFactory<TimerIDPConfiguration>{

  public IndicatorDataProvider<TimerIDPConfiguration> create(TimerIDPConfiguration configuration) {
    return new TimerTicker(configuration);
  }

  public String getID() {
    return TimerIDPConfiguration.ID;
  }

}
