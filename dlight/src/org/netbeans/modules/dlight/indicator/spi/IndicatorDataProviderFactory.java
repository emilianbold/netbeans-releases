/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.indicator.spi;

import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;

/**
 *
 * @author mt154047
 */
public interface IndicatorDataProviderFactory<T extends IndicatorDataProviderConfiguration> {

  IndicatorDataProvider<T> create(T configuration);

  String getID();
}
