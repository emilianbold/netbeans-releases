/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicator.spi.impl;

import org.netbeans.modules.dlight.indicator.impl.TimerIDPConfiguration;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProvider;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProviderFactory;

/**
 *
 * @author mt154047
 */
public final  class TimerTickerFactory implements IndicatorDataProviderFactory<TimerIDPConfiguration>{

  public IndicatorDataProvider<TimerIDPConfiguration> create(TimerIDPConfiguration configuration) {
    return new TimerTicker(configuration);
  }

  public String getID() {
    return TimerIDPConfiguration.ID;
  }

}
