/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.indicator.spi.impl;

import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProvider;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProviderFactory;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
public final class IDPProvider {

  private static IDPProvider instance = null;
  

  private IDPProvider() {
  }

  public static final IDPProvider getInstance() {
    if (instance == null) {
      instance = new IDPProvider();
    }
    return instance;
  }

  /**
   * Creates new DataCollector instance on the base of DataCollectorConfiguration
   * @param configuraiton
   * @return new instance of data collector is returned each time this method is invoked;
   */
  public IndicatorDataProvider create(IndicatorDataProviderConfiguration configuraiton) {
    Collection<? extends IndicatorDataProviderFactory> result = Lookup.getDefault().lookupAll(IndicatorDataProviderFactory.class);
    if (result.isEmpty()) {
      return null;
    }
    Iterator<? extends IndicatorDataProviderFactory> iterator = result.iterator();
    while (iterator.hasNext()) {
      IndicatorDataProviderFactory indicator = iterator.next();
      if (indicator.getID().equals(configuraiton.getID())) {
        return indicator.create(configuraiton);
      }
    }
    return null;
  }
  
}
