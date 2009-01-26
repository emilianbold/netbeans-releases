/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.spi.visualizer;

import java.util.Collection;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;

/**
 *
 * @param <T> 
 */
public interface VisualizerDataProviderFactory<T extends VisualizerDataProvider> {
     /**
   * Returns the list of {@link org.netbeans.modules.dlight.dataprovider.api.DataModelScheme}
   * this data provider can serve.
   * @return the list of data model this DataProvider can serve
   */
  Collection<DataModelScheme> getProvidedDataModelScheme();

  /**
   * Checks if DataProvider can provider information according to
   * te <param>dataModel</param>
   * @param dataModel
   * @return <code>true</code> if DataProvider provides information required
   * by <param>dataModel</param>
   */
  boolean provides(DataModelScheme dataModel);

  /**
   * 
   * @return
   */
  T create();
}
