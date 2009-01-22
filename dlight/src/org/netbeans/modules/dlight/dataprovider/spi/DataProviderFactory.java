/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.dataprovider.spi;

import java.util.List;
import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;
import org.netbeans.modules.dlight.storage.spi.DataStorageType;

/**
 *
 * @author mt154047
 */
public interface DataProviderFactory {

  DataProvider create();

  /**
   * Returns the list of {@link org.netbeans.modules.dlight.core.dataprovider.model.DataModelScheme}
   * this data provider can serve.
   * @return the list of data model this DataProvider can serve
   */
  List<? extends DataModelScheme> getProvidedDataModelScheme();

  /**
   * Checks if DataProvider can provider information according to
   * te <param>dataModel</param>
   * @param dataModel
   * @return <code>true</code> if DataProvider provides information required
   * by <param>dataModel</param>
   */
  boolean provides(DataModelScheme dataModel);

  /**
   * The types of {@link org.netbeans.modules.dlight.core.storage.model.DataStorage} this
   *  DataProvider can get data from
   * @return the list of {@link org.netbeans.modules.dlight.core.storage.model.DataStorageType}
   *  supported by this DataProvider
   */
  List<DataStorageType> getSupportedDataStorageTypes();
}
