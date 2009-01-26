/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.dtrace.collector;

import org.netbeans.modules.dlight.dtrace.collector.impl.MultipleDTDCConfigurationAccessor;
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
    private final String prefix;

    public MultipleDTDCConfiguration(DTDCConfiguration configuration, String prefix) {
        this.configuration = configuration;
        this.prefix = prefix;
    }

    public String getID() {
        return ID;
    }

    /*package*/ DTDCConfiguration getDTDCConfiguration() {
        return configuration;
    }

    /*package*/ String getOutputPrefix() {
        return prefix;
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

        public String getOutputPrefix(MultipleDTDCConfiguration configuration) {
            return configuration.getOutputPrefix();
        }
    }
}
