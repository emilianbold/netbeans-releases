/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.api.impl;

import java.util.List;
import org.netbeans.modules.dlight.api.tool.DLightToolConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.Indicator;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.tool.DLightToolConfigurationProvider;

/**
 *
 * @author masha
 */
public abstract class DLightToolAccessor {

    private static volatile DLightToolAccessor DEFAULT;

    public static DLightToolAccessor getDefault() {
        DLightToolAccessor a = DEFAULT;
        if (a != null) {
            return a;
        }

        try {
            Class.forName(DLightTool.class.getName(), true, DLightTool.class.getClassLoader());//
        } catch (Exception e) {
        }
        return DEFAULT;
    }

    public static void setDefault(DLightToolAccessor accessor) {
        if (DEFAULT != null) {
            throw new IllegalStateException();
        }
        DEFAULT = accessor;
    }

    public DLightToolAccessor() {
    }

    public abstract List<IndicatorDataProvider<?>> getIndicatorDataProviders(DLightTool tool);

    public abstract DLightTool newDLightTool(DLightToolConfiguration configuration);

    public abstract List<Indicator<?>> getIndicators(DLightTool tool);

    public abstract List<DataCollector<?>> getCollectors(DLightTool tool);

    public abstract boolean collectorsTurnedOn(DLightTool tool);

    public abstract void turnCollectorsState(DLightTool tool, boolean turnedOn);
    
}
