/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.dlight.dtrace.collector;

import org.netbeans.dlight.dtrace.collector.impl.MultipleDTDCConfigurationAccessor;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;

/**
 * This class is to configure
 */
public final class MultipleDTDCConfiguration implements DataCollectorConfiguration, IndicatorDataProviderConfiguration {

    private static final String ID = "MultipleDtraceDataCollectorConfiguration";

    static {
        MultipleDTDCConfigurationAccessor.setDefault(new MultipleDTDCConfigurationAccessorImpl());
    }

    private final DTDCConfiguration configuration;

    public MultipleDTDCConfiguration(DTDCConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getID() {
        return ID;
    }

    /*package*/ DTDCConfiguration getDTDCConfiguration() {
        return configuration;
    }

    private static final class MultipleDTDCConfigurationAccessorImpl extends MultipleDTDCConfigurationAccessor {

        @Override
        public String getID() {
            return ID;
        }

        @Override
        public DTDCConfiguration getDTDCConfiguration(MultipleDTDCConfiguration configuration) {
            return configuration.getDTDCConfiguration();
        }
    }
}
