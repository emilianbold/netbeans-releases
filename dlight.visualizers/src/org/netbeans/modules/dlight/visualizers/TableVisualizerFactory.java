/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.visualizer.spi.Visualizer;
import org.netbeans.modules.dlight.visualizer.spi.VisualizerFactory;
import org.netbeans.modules.dlight.visualizers.api.TableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;

/**
 *
 * @author mt154047
 */
public final class TableVisualizerFactory implements VisualizerFactory<TableVisualizerConfiguration> {

  public String getID() {
    return VisualizerConfigurationIDsProvider.TABLE_VISUALIZER;
  }

  public Visualizer<TableVisualizerConfiguration> create(TableVisualizerConfiguration visualizer, DataProvider provider) {
    return new TableVisualizer(provider, visualizer);
  }
}
