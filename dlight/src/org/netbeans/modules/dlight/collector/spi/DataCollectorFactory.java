/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.collector.spi;

import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;

/**
 * Register factory in the global Lookup
 */
public interface DataCollectorFactory<T extends DataCollectorConfiguration> {

  DataCollector<T> create(T configuration);
  String getID();

}
