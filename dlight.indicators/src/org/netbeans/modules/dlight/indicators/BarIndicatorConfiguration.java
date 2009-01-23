/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicators;


import org.netbeans.modules.dlight.indicator.api.ConfigurationData;
import org.netbeans.modules.dlight.indicator.api.IndicatorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorMetadata;
import org.netbeans.modules.dlight.indicators.support.BarIndicatorConfigurationAccessor;
import org.netbeans.modules.dlight.indicators.support.IndicatorConfigurationIDs;

/**
 *
 * @author mt154047
 */
public final class BarIndicatorConfiguration extends IndicatorConfiguration{


  static{
    BarIndicatorConfigurationAccessor.setDefault(new BarIndicatorConfigurationAccessorImpl());
  }
  public BarIndicatorConfiguration(IndicatorMetadata metadata) {
    super(metadata);
  }

  @Override
  public String getID() {
    return IndicatorConfigurationIDs.BAR_ID;
  }


  ConfigurationData getData(){
    return super.getConfigurationData();
  }

  private static final class BarIndicatorConfigurationAccessorImpl extends BarIndicatorConfigurationAccessor{

    @Override
    public ConfigurationData getConfigurationData(BarIndicatorConfiguration configuration) {
      return configuration.getData();
    }
    
  }
  


  


}
