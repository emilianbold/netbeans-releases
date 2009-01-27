/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.derby.support;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorageFactory;

/**
 *
 * @author masha
 */
public final class DerbyDataStorageFactory extends SQLDataStorageFactory<DerbyDataStorage>{
  static final String DERBY_DATA_STORAGE_TYPE = "db:sql:derby";
  private final Collection<DataStorageType> supportedStorageTypes = new ArrayList<DataStorageType>();

  public DerbyDataStorageFactory() {
    supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(DERBY_DATA_STORAGE_TYPE));
    supportedStorageTypes.add(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
    supportedStorageTypes.addAll(super.getStorageTypes());

  }

  @Override
  public Collection<DataStorageType> getStorageTypes() {
    return supportedStorageTypes;
  }

  @Override
  public DerbyDataStorage createStorage() {
    return new DerbyDataStorage();
  }
}
