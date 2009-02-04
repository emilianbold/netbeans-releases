/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.indicator;

import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;

/**
 * Factory to create {@link org.netbeans.modules.dlight.spi.indicator.Indicator} instance.
 * Please register your factory in the Global Lookup.
 * @param <T> indicator configuration inplementation this factory can create {@link org.netbeans.modules.dlight.spi.indicator.Indicator} instances on the base of
 */
public interface IndicatorFactory<T extends IndicatorConfiguration> {

  /**
   * Creates new  {@link org.netbeans.modules.dlight.spi.indicator.Indicator} instance for
   * the <code>configuration</code>
   * @param configuration configuration to create {@link org.netbeans.modules.dlight.spi.indicator.Indicator} for
   * @return newly created Indicatro instance
   */
  Indicator<T> create(T configuration);

    /**
   * Unique id, it is used by infrastructure to compare with the
   *  {@link org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration#getID() }
   *  to find the proper factory.
   * @return unique ID, should be the same as configuration  id this factory can create
     * Indicator  for
   *
   */
  String getID();
}
