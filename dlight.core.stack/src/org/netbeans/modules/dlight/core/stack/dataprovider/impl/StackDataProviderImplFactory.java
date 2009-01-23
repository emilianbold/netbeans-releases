/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.core.stack.dataprovider.impl;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataModel;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;
import org.netbeans.modules.dlight.dataprovider.spi.DataProvider;
import org.netbeans.modules.dlight.dataprovider.spi.DataProviderFactory;
import org.netbeans.modules.dlight.storage.spi.DataStorageType;
import org.netbeans.modules.dlight.storage.spi.DataStorageTypeFactory;

/**
 *
 * @author mt154047
 */
public final class StackDataProviderImplFactory implements DataProviderFactory {

  private final List<DataStorageType> supportedStorageTypes = Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));

  public DataProvider create() {
    return new StackDataProviderImpl();
  }

  public List<? extends DataModelScheme> getProvidedDataModelScheme() {
    return Arrays.asList(StackDataModel.instance);
  }

  public boolean provides(DataModelScheme dataModel) {
    return getProvidedDataModelScheme().contains(dataModel);
  }

  public List<DataStorageType> getSupportedDataStorageTypes() {
    return supportedStorageTypes;
  }
}
