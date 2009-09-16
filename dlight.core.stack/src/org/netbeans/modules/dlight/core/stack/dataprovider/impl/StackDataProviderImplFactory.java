/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.core.stack.dataprovider.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory.class)
public final class StackDataProviderImplFactory implements DataProviderFactory {

    private final Collection<DataModelScheme> providedSchemas = Collections.unmodifiableList(Arrays.asList(
            DataModelSchemeProvider.getInstance().getScheme("model:stack"), // NOI18N
            DataModelSchemeProvider.getInstance().getScheme("model:threaddump"))); //NOI18N

    public DataProvider create() {
        return new StackDataProviderImpl();
    }

    public Collection<DataModelScheme> getProvidedDataModelScheme() {
        return providedSchemas;
    }

    public boolean provides(DataModelScheme dataModel) {
        return getProvidedDataModelScheme().contains(dataModel);
    }

    public boolean validate(DataStorage storage) {
        return storage.supportsType(StackDataStorage.STACK_DATA_STORAGE_TYPE);
    }
}
