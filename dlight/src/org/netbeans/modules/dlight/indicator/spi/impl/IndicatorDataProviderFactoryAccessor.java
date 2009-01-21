/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicator.spi.impl;

import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProvider;

/**
 *
 * @author masha
 */
public abstract class IndicatorDataProviderFactoryAccessor {
 private static volatile IndicatorDataProviderFactoryAccessor DEFAULT;

  public static IndicatorDataProviderFactoryAccessor getDefault(){
    IndicatorDataProviderFactoryAccessor a = DEFAULT;
    if (a!= null){
      return a;
    }

    try{
      Class.forName(IndicatorDataProviderFactory.class.getName(), true,IndicatorDataProviderFactory.class.getClassLoader());//
    }catch(Exception e){

    }
    return DEFAULT;
  }

  public static void setDefault(IndicatorDataProviderFactoryAccessor accessor){
    if (DEFAULT != null){
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public IndicatorDataProviderFactoryAccessor(){

  }

  public abstract  IndicatorDataProvider create(IndicatorDataProviderConfiguration configuraiton);

}
