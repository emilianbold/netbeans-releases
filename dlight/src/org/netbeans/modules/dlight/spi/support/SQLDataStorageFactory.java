/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.support;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory;

/**
 * Please be sure you have connected the database at the time it was created
 * Use connect() method 
 */
public abstract class SQLDataStorageFactory<G extends SQLDataStorage> implements PersistentDataStorageFactory<G> {

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

}
