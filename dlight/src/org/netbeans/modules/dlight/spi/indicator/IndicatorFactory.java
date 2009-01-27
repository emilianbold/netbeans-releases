/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.indicator;

import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;

/**
 * Factory to create {@link org.netbeans.modules.dlight.indicator.spi.Indicator} instance.
 * Please register your factory in the Global Lookup.
 */
public interface IndicatorFactory<T extends IndicatorConfiguration> {

  /**
   * Creates new  {@link org.netbeans.modules.dlight.indicator.spi.Indicator} instance for
   * the <code>configuration</code>
   * @param configuration configuration to create {@link org.netbeans.modules.dlight.indicator.spi.Indicator} for
   * @return newly created Indicatro instance
   */
  Indicator<T> create(T configuration);

    /**
   * Unique id, it is used by infrastructure to compare with the
   *  {@link org.netbeans.modules.dlight.indicator.api.IndicatorConfiguration#getID() }
   *  to find the proper factory.
   * @return unique ID, should be the same as configuration  id this factory can create
     * Indicator  for
   *
   */
  String getID();
}
