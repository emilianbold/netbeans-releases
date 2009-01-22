/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.visualizer.spi.Visualizer;
import org.netbeans.modules.dlight.visualizer.spi.VisualizerFactory;
import org.netbeans.modules.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;

/**
 *
 * @author mt154047
 */
public final class CallersCalleesVisualizerFactory implements VisualizerFactory<TreeTableVisualizerConfiguration> {

  @Override
  public String getID() {
    return VisualizerConfigurationIDsProvider.CALLERS_CALLEES_VISUALIZER;
  }

  @Override
  public Visualizer<TreeTableVisualizerConfiguration> create(TreeTableVisualizerConfiguration configuration, DataProvider provider) {
    if (configuration instanceof CallersCalleesVisualizerConfiguration &&
        provider instanceof StackDataProvider) {
      return new CallersCalleesVisualizer(provider, configuration);
    }
    throw new IllegalStateException("Trying to create CallersCallees Visualizer " +
        "using incorrect VisualizerConfiguration and/or DataProvider object");
  }


}
