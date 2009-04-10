/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.spi.impl.TreeTableDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerFactory;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = org.netbeans.modules.dlight.spi.visualizer.VisualizerFactory.class)
public final class TreeTableVisualizerFactory implements VisualizerFactory<TreeTableVisualizerConfiguration> {

    public String getID() {
        return VisualizerConfigurationIDsProvider.TREE_TABLE_VISUALIZER;
    }

    @SuppressWarnings("unchecked")
    public Visualizer<TreeTableVisualizerConfiguration> create(TreeTableVisualizerConfiguration visualizer, VisualizerDataProvider provider) {
        if (!(provider instanceof TreeTableDataProvider)) {
            return null;
        }
        return new TreeTableVisualizer(visualizer, (TreeTableDataProvider) provider);
    }
}
