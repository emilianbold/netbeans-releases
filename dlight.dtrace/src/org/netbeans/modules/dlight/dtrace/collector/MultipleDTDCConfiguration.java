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
public final class MultipleDTDCConfiguration
        implements DataCollectorConfiguration,
        IndicatorDataProviderConfiguration {

    private static final String ID =
            "MultipleDtraceDataCollectorConfiguration"; // NOI18N
    private final DTDCConfiguration configuration;
    private final String prefix;


    static {
        MultipleDTDCConfigurationAccessor.setDefault(
                new MultipleDTDCConfigurationAccessorImpl());
    }

    /**
     * Constructs new MultipleDTDCConfiguration object
     * @param configuration dtrace data collector configuration
     * @param prefix script output prefix
     */
    public MultipleDTDCConfiguration(
            final DTDCConfiguration configuration, final String prefix) {
        this.configuration = configuration;
        this.prefix = prefix;
    }

    /**
     * Returns unique ID to be used to identify configuration
     * @return unique id
     */
    public String getID() {
        return ID;
    }

    /*package*/ DTDCConfiguration getDTDCConfiguration() {
        return configuration;
    }

    /*package*/ String getOutputPrefix() {
        return prefix;
    }

    private static final class MultipleDTDCConfigurationAccessorImpl
            extends MultipleDTDCConfigurationAccessor {

        @Override
        public String getID() {
            return ID;
        }

        @Override
        public DTDCConfiguration getDTDCConfiguration(
                MultipleDTDCConfiguration configuration) {
            return configuration.getDTDCConfiguration();
        }

        public String getOutputPrefix(MultipleDTDCConfiguration configuration) {
            return configuration.getOutputPrefix();
        }
    }
}
