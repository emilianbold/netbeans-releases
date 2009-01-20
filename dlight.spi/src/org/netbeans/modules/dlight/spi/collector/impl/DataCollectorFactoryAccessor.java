/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.spi.collector.impl;

import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.spi.collector.DataCollector;

/**
 *
 * @author masha
 */
public abstract class DataCollectorFactoryAccessor {
 private static volatile DataCollectorFactoryAccessor DEFAULT;

  public static DataCollectorFactoryAccessor getDefault(){
    DataCollectorFactoryAccessor a = DEFAULT;
    if (a!= null){
      return a;
    }

    try{
      Class.forName(DataCollectorFactory.class.getName(), true,DataCollectorFactory.class.getClassLoader());//
    }catch(Exception e){

    }
    return DEFAULT;
  }

  public static void setDefault(DataCollectorFactoryAccessor accessor){
    if (DEFAULT != null){
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public DataCollectorFactoryAccessor(){

  }

  public abstract  DataCollector create(DataCollectorConfiguration configuraiton);


}
