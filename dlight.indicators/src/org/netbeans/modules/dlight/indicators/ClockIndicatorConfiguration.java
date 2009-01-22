/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicators;

import org.netbeans.modules.dlight.indicator.api.IndicatorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorMetadata;
import org.netbeans.modules.dlight.indicators.support.IndicatorConfigurationIDs;

/**
 *
 * @author mt154047
 */
public final class ClockIndicatorConfiguration extends IndicatorConfiguration{

  public ClockIndicatorConfiguration(IndicatorMetadata indicatorMetadata) {
    super(indicatorMetadata);
  }
  
  @Override
  public String getID() {
    return IndicatorConfigurationIDs.CLOCK_ID;
  }


}
