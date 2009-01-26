/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.cpu.impl;

import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorMetadata;


/**
 *
 * @author mt154047
 */
public final class CpuIndicatorConfiguration extends IndicatorConfiguration{
  static final String ID = "CpuIndicatorConfiguration_ID";

  public CpuIndicatorConfiguration(IndicatorMetadata metadata) {
    super(metadata);
  }

  @Override
  public String getID() {
    return ID;
  }



}
