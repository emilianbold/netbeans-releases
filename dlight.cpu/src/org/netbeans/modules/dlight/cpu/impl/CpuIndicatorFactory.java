/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.cpu.impl;

import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorFactory;



/**
 *
 * @author mt154047
 */
public final class CpuIndicatorFactory implements IndicatorFactory<CpuIndicatorConfiguration>{

  @Override
  public Indicator<CpuIndicatorConfiguration> create(CpuIndicatorConfiguration configuration) {
    return new CpuIndicator(configuration);
  }

  @Override
  public String getID() {
    return CpuIndicatorConfiguration.ID;
  }

}
