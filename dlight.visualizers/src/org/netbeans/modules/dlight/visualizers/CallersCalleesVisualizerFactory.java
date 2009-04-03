/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers;

import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerFactory;
import org.netbeans.modules.dlight.visualizers.api.CallersCalleesVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.TreeTableVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service=org.netbeans.modules.dlight.spi.visualizer.VisualizerFactory.class)
public final class CallersCalleesVisualizerFactory implements VisualizerFactory<TreeTableVisualizerConfiguration> {

  @Override
  public String getID() {
    return VisualizerConfigurationIDsProvider.CALLERS_CALLEES_VISUALIZER;
  }

  @Override
  public Visualizer<TreeTableVisualizerConfiguration> create(TreeTableVisualizerConfiguration configuration,
          VisualizerDataProvider provider) {
    if (configuration instanceof CallersCalleesVisualizerConfiguration &&
        provider instanceof StackDataProvider) {
      return new CallersCalleesVisualizer((StackDataProvider)provider, configuration);
    }
    throw new IllegalStateException("Trying to create CallersCallees Visualizer " +//NOI18N
        "using incorrect VisualizerConfiguration and/or DataProvider object");//NOI18N
  }


}
