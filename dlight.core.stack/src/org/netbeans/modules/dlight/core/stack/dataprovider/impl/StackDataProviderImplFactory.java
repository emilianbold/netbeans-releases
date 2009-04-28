/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.core.stack.dataprovider.impl;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service=org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory.class)
public final class StackDataProviderImplFactory implements DataProviderFactory {

    private final Collection<DataStorageType> supportedStorageTypes = Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
    private final Collection<DataModelScheme> providedSchemas = Arrays.asList(DataModelSchemeProvider.getInstance().getScheme("model:stack")); //NOI18N

    public DataProvider create() {
        return new StackDataProviderImpl();
    }

    public Collection<DataModelScheme> getProvidedDataModelScheme() {
        return providedSchemas;
    }

    public boolean provides(DataModelScheme dataModel) {
        return getProvidedDataModelScheme().contains(dataModel);
    }

    public Collection<DataStorageType> getSupportedDataStorageTypes() {
        return supportedStorageTypes;
    }
}
