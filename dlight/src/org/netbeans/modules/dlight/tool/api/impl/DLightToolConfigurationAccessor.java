/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.tool.api.impl;

import java.util.List;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.tool.api.DLightToolConfiguration;

/**
 *
 * @author masha
 */
public abstract class DLightToolConfigurationAccessor {
  private static volatile DLightToolConfigurationAccessor DEFAULT;

  public static DLightToolConfigurationAccessor getDefault(){
    DLightToolConfigurationAccessor a = DEFAULT;
    if (a!= null){
      return a;
    }

    try{
      Class.forName(DLightToolConfiguration.class.getName(), true,DLightToolConfiguration.class.getClassLoader());//
    }catch(Exception e){

    }
    return DEFAULT;
  }

  public static void setDefault(DLightToolConfigurationAccessor accessor){
    if (DEFAULT != null){
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public DLightToolConfigurationAccessor(){

  }

  public abstract List<DataCollectorConfiguration> getDataCollectors(DLightToolConfiguration conf);

  public abstract List<IndicatorDataProviderConfiguration> getIndicatorDataProviders(DLightToolConfiguration conf);

  public abstract List<IndicatorConfiguration> getIndicators(DLightToolConfiguration conf);

  public abstract String getToolName(DLightToolConfiguration conf);

}
