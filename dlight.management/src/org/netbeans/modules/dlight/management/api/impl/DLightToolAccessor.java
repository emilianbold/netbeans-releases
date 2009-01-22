/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.management.api.impl;

import java.util.List;
import org.netbeans.modules.dlight.collector.spi.DataCollector;
import org.netbeans.modules.dlight.indicator.api.Indicator;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProvider;
import org.netbeans.modules.dlight.management.api.DLightTool;
import org.netbeans.modules.dlight.tool.api.DLightToolConfiguration;


/**
 *
 * @author masha
 */
public abstract class DLightToolAccessor {
  private static volatile DLightToolAccessor DEFAULT;
  
  public static DLightToolAccessor getDefault(){
    DLightToolAccessor a = DEFAULT;
    if (a!= null){
      return a;
    }
    
    try{
      Class.forName(DLightTool.class.getName(), true, DLightTool.class.getClassLoader());//
    }catch(Exception e){

    }
    return DEFAULT;
  }

  public static void setDefault(DLightToolAccessor accessor){
    if (DEFAULT != null){
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public DLightToolAccessor(){
    
  }

  public abstract List<IndicatorDataProvider> getIndicatorDataProviders(DLightTool tool);
  public abstract DLightTool newDLightTool(DLightToolConfiguration configuration);
  public abstract List<Indicator> getIndicators(DLightTool tool);
  public abstract List<DataCollector> getCollectors(DLightTool tool);

}
