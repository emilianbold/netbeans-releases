/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.dataprovider.spi.support;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;
import org.netbeans.modules.dlight.dataprovider.impl.TableDataModel;
import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.dataprovider.spi.DataProviderFactory;
import org.netbeans.modules.dlight.storage.spi.DataStorageType;
import org.netbeans.modules.dlight.storage.spi.DataStorageTypeFactory;
import org.netbeans.modules.dlight.storage.spi.support.SQLDataStorage;

/**
 *
 * @author mt154047
 */
public final class SQLTableDataProviderFactory implements DataProviderFactory{

  public DataProvider create() {
    return new SQLTableDataProvider();
  }

 /**
   * Returns {@link org.netbeans.modules.dlight.core.dataprovider.model.TableDataModel} as
   * provided data model scheme
   * @return
   */
  public List<? extends DataModelScheme> getProvidedDataModelScheme() {
    return Arrays.asList(TableDataModel.instance);
  }

  public final boolean provides(DataModelScheme dataModel) {
    return getProvidedDataModelScheme().contains(dataModel);
  }

  public List<DataStorageType> getSupportedDataStorageTypes() {
    return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
  }

}
