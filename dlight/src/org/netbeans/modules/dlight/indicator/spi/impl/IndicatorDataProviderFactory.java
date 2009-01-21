/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicator.spi.impl;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProvider;
import org.openide.util.Lookup;


/**
 *
 * @author masha
 */
final class IndicatorDataProviderFactory {
private static IndicatorDataProviderFactory instance = null;

  static{
    IndicatorDataProviderFactoryAccessor.setDefault(new IndicatorDataProviderFactoryAccessorImpl());
  }

  private IndicatorDataProviderFactory() {

  }

  static final IndicatorDataProviderFactory getInstance(){
    if(instance == null){
      instance = new IndicatorDataProviderFactory();
    }
    return instance;
  }


  /**
   * Creates new DataCollector instance on the base of DataCollectorConfiguration
   * @param configuraiton
   * @return new instance of data collector is returned each time this method is invoked;
   */
   IndicatorDataProvider createDataProvider(IndicatorDataProviderConfiguration configuraiton){
    Collection<? extends IndicatorDataProvider> result = Lookup.getDefault().lookupAll(IndicatorDataProvider.class);
    if (result.isEmpty()){
      return null;
    }
    Iterator<? extends IndicatorDataProvider> iterator = result.iterator();
    while (iterator.hasNext()){
      IndicatorDataProvider indicator = iterator.next();
      if (indicator.getID().equals(configuraiton.getID())){
        return indicator.create(configuraiton);
      }
    }
    return null;
  }

   private static final class IndicatorDataProviderFactoryAccessorImpl extends IndicatorDataProviderFactoryAccessor{

    @Override
    public IndicatorDataProvider create(IndicatorDataProviderConfiguration configuraiton) {
      return IndicatorDataProviderFactory.getInstance().createDataProvider(configuraiton);
    }

   
   }
}
