/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.indicator.spi;

import org.netbeans.modules.dlight.indicator.api.IndicatorConfiguration;

/**
 * Register in Global Lookup
 */
public interface IndicatorFactory<T extends IndicatorConfiguration> {

  Indicator<T> create(T configuration);

  String getID();
}
