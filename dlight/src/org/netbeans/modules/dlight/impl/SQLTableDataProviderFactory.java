/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.impl;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProviderFactory;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author mt154047
 */
@ServiceProviders({@ServiceProvider(service = DataProviderFactory.class), @ServiceProvider(service = VisualizerDataProviderFactory.class)})
public final class SQLTableDataProviderFactory implements DataProviderFactory {

    private final DataStorageType supportedStorageType = SQLDataStorage.getStorageType();
    private final Collection<DataModelScheme> providedSchemas = Collections.singletonList(DataModelSchemeProvider.getInstance().getScheme("model:table")); //NOI18N

    public DataProvider create() {
        return new SQLTableDataProvider();
    }

    /**
     * Returns {@link org.netbeans.modules.dlight.core.dataprovider.model.TableDataModel} as
     * provided data model scheme
     * @return
     */
    public Collection<DataModelScheme> getProvidedDataModelScheme() {
        return providedSchemas;
    }

    public final boolean provides(DataModelScheme dataModel) {
        return getProvidedDataModelScheme().contains(dataModel);
    }

    public boolean validate(DataStorage storage) {
        return storage.supportsType(supportedStorageType);
    }
}
