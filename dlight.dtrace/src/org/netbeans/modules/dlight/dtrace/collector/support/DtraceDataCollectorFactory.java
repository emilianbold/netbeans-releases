/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import org.netbeans.modules.dlight.collector.spi.DataCollectorFactory;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.impl.DTDCConfigurationAccessor;
import org.netbeans.modules.dlight.indicator.spi.IndicatorDataProviderFactory;

/**
 *
 * @author mt154047
 */
public final class DtraceDataCollectorFactory
        implements DataCollectorFactory<DTDCConfiguration>,
        IndicatorDataProviderFactory<DTDCConfiguration> {

    public DtraceDataCollectorFactory() {
    }

    public DtraceDataCollector create(DTDCConfiguration configuration) {
        return new DtraceDataCollector(configuration);
    }

    public String getID() {
        return DTDCConfigurationAccessor.getDefault().getID();
    }
}
