/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.impl;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.PersistentDataStorageFactory;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;

/**
 *
 * @author masha
 */
public abstract class SQLDataStorageFactory<G extends SQLDataStorage> implements PersistentDataStorageFactory<G> {

    public Collection<DataStorageType> getStorageTypes() {
        return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

}
