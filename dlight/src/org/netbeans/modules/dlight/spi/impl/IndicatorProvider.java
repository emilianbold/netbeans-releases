/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.impl;

import java.util.Collection;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorFactory;
import org.openide.util.Lookup;

/**
 *
 * @author mt154047
 */
public final class IndicatorProvider {

    private static final IndicatorProvider instance = new IndicatorProvider();

    private IndicatorProvider() {
    }

    public static final IndicatorProvider getInstance() {
        return instance;
    }

    /**
     * Creates new DataCollector instance on the base of DataCollectorConfiguration
     * @param configuraiton
     * @return new instance of data collector is returned each time this method is invoked;
     */
    public Indicator<?> createIndicator(final String toolName,
            final IndicatorConfiguration configuraiton) {
        
        Collection<? extends IndicatorFactory> result =
                Lookup.getDefault().lookupAll(IndicatorFactory.class);

        if (result.isEmpty()) {
            return null;
        }

        for (IndicatorFactory indicatorFactory : result) {
            if (indicatorFactory.getID().equals(configuraiton.getID())) {
                @SuppressWarnings("unchecked")
                // Impossible to do it in checked manner. Have to rely on factory ID check.
                Indicator<?> indicator = indicatorFactory.create(configuraiton);
                IndicatorAccessor.getDefault().setToolName(indicator, toolName);
                IndicatorAccessor.getDefault().initMouseListener(indicator);
                return indicator;
            }
        }

        return null;
    }
}
