/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.visualizer.spi;

import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.visualizer.api.VisualizerConfiguration;

/**
 *
 * @author mt154047
 */
public interface VisualizerFactory <T extends VisualizerConfiguration> {
  String getID();
  Visualizer<T> create(T visualizer, DataProvider provider);

}
