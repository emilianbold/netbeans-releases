/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.impl;

import java.util.Collection;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProviderFactory;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
public final class IDPProvider {

    private static IDPProvider instance = null;

    private IDPProvider() {
    }

    public static IDPProvider getInstance() {
        if (instance == null) {
            instance = new IDPProvider();
        }
        return instance;
    }

    /**
     * Creates new DataCollector instance on the base of DataCollectorConfiguration
     * @param configuraiton
     * @return new instance of data collector is returned each time this method is invoked;
     */
    public <T extends IndicatorDataProviderConfiguration> IndicatorDataProvider<T> create(T configuraiton) {
        @SuppressWarnings("unchecked")
        Collection<IndicatorDataProviderFactory<?>> result = (Collection<IndicatorDataProviderFactory<?>>) Lookup.getDefault().lookupAll(IndicatorDataProviderFactory.class);

        if (result.isEmpty()) {
            return null;
        }

        for (IndicatorDataProviderFactory<?> idpFactory : result) {
            if (idpFactory.getID().equals(configuraiton.getID())) {
                @SuppressWarnings("unchecked")
                // Impossible to do it in checked manner. Have to rely on factory ID check.
                IndicatorDataProvider<T> indicatorDataProvider = ((IndicatorDataProviderFactory<T>) idpFactory).create(configuraiton);
                return indicatorDataProvider;
            }
        }

        return null;
    }
}
