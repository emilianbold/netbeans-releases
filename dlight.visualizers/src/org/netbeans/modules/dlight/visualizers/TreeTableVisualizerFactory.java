/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.visualizer.spi.Visualizer;
import org.netbeans.modules.dlight.visualizer.spi.VisualizerFactory;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;

/**
 *
 * @author mt154047
 */
public final class TreeTableVisualizerFactory implements VisualizerFactory<TreeTableVisualizerConfiguration>{
public String getID() {
    return VisualizerConfigurationIDsProvider.TREE_TABLE_VISUALIZER;
  }

  public Visualizer<TreeTableVisualizerConfiguration> create(TreeTableVisualizerConfiguration visualizer, DataProvider provider) {
    return new TreeTableVisualizer(visualizer, provider);
  }

}
