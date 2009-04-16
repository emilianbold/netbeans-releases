/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.impl;

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProvider;
import org.netbeans.modules.dlight.spi.dataprovider.DataProviderFactory;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerDataProviderFactory;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author mt154047
 */
@ServiceProviders({@ServiceProvider(service=DataProviderFactory.class), @ServiceProvider(service=VisualizerDataProviderFactory.class)})
public final class SQLTableDataProviderFactory implements DataProviderFactory {

    private final Collection<DataModelScheme> providedSchemas = Arrays.asList(DataModelSchemeProvider.getInstance().getScheme("model:table")); //NOI18N

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

    public Collection<DataStorageType> getSupportedDataStorageTypes() {
        return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }
}
