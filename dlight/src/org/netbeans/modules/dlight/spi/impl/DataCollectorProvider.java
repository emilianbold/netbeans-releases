/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.impl;

import java.util.Collection;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.collector.DataCollectorFactory;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
public final class DataCollectorProvider {

    private final static DataCollectorProvider instance =
            new DataCollectorProvider();

    private DataCollectorProvider() {
    }

    public static final DataCollectorProvider getInstance() {
        return instance;
    }

    /**
     * Creates new DataCollector instance on the base of DataCollectorConfiguration
     * @param configuraiton
     * @return new instance of data collector is returned each time this method is invoked;
     */
    public DataCollector<?> createDataCollector(DataCollectorConfiguration configuraiton) {
        Collection<? extends DataCollectorFactory> result =
                Lookup.getDefault().lookupAll(DataCollectorFactory.class);

        for (DataCollectorFactory collectorFactory : result) {
            if (collectorFactory.getID().equals(configuraiton.getID())) {
                @SuppressWarnings("unchecked")
                // Impossible to do it in checked manner. Have to rely on factory ID check.
                DataCollector<?> dataCollector = collectorFactory.create(configuraiton);
                return dataCollector;
            }
        }

        return null;
    }

    public void reset() {
        Collection<? extends DataCollectorFactory> result =
                Lookup.getDefault().lookupAll(DataCollectorFactory.class);

        for (DataCollectorFactory f : result) {
            f.reset();
        }
    }
}
