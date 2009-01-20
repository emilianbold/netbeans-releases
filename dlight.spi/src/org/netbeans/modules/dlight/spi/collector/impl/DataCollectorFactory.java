/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.spi.collector.impl;

import org.netbeans.modules.dlight.spi.collector.*;
import org.netbeans.modules.dlight.collector.api.*;
import java.util.Collection;
import java.util.Iterator;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
final class DataCollectorFactory {
  private static DataCollectorFactory instance = null;

  static{
    DataCollectorFactoryAccessor.setDefault(new DataCollectorFactoryAccessorImpl());
  }

  private DataCollectorFactory() {
    
  }

  static final DataCollectorFactory getInstance(){
    if(instance == null){
      instance = new DataCollectorFactory();
    }
    return instance;
  }
  

  /**
   * Creates new DataCollector instance on the base of DataCollectorConfiguration
   * @param configuraiton
   * @return new instance of data collector is returned each time this method is invoked;
   */
   DataCollector createDataCollector(DataCollectorConfiguration configuraiton){
    Collection<? extends DataCollector> result = Lookup.getDefault().lookupAll(DataCollector.class);
    if (result.isEmpty()){
      return null;
    }
    Iterator<? extends DataCollector> iterator = result.iterator();
    while (iterator.hasNext()){
      DataCollector collector = iterator.next();
      if (collector.getID().equals(configuraiton.getID())){
        return collector.create(configuraiton);
      }
    }
    return null;
  }

   private static final class DataCollectorFactoryAccessorImpl extends DataCollectorFactoryAccessor{

    @Override
    public DataCollector create(DataCollectorConfiguration configuraiton) {
      return getInstance().createDataCollector(configuraiton);
    }
     
   }

}
