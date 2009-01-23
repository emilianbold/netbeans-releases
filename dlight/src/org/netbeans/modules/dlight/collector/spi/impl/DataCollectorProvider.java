/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.collector.spi.impl;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.collector.spi.DataCollector;
import org.netbeans.modules.dlight.collector.spi.DataCollectorFactory;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
public final class DataCollectorProvider {
  private static DataCollectorProvider instance = null;

 
  private DataCollectorProvider() {
    
  }

  public static final DataCollectorProvider getInstance(){
    if(instance == null){
      instance = new DataCollectorProvider();
    }
    return instance;
  }
  

  /**
   * Creates new DataCollector instance on the base of DataCollectorConfiguration
   * @param configuraiton
   * @return new instance of data collector is returned each time this method is invoked;
   */
   public DataCollector createDataCollector(DataCollectorConfiguration configuraiton){
    Collection<? extends DataCollectorFactory> result = Lookup.getDefault().lookupAll(DataCollectorFactory.class);
    if (result.isEmpty()){
      return null;
    }
    Iterator<? extends DataCollectorFactory> iterator = result.iterator();
    while (iterator.hasNext()){
      DataCollectorFactory collectorFactory = iterator.next();
      if (collectorFactory.getID().equals(configuraiton.getID())){
        return collectorFactory.create(configuraiton);
      }
    }
    return null;
  }

}
