/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.api.tool;

import org.netbeans.modules.dlight.api.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.impl.DLightToolConfigurationAccessor;

/**
 * This class represents configuration object to create D-Light Tool  on the base of.
 * 
 */
public final class DLightToolConfiguration {

    private final String toolName;
    private final String detailedToolName;
    private final List<DataCollectorConfiguration> dataCollectors;
    private final List<IndicatorDataProviderConfiguration> indicatorDataProvidersConfiguration;
    private final List<IndicatorConfiguration> indicators;
    private String iconPath = null;

    static {
        DLightToolConfigurationAccessor.setDefault(new DLightToolConfigurationAccessorIml());
    }

    /**
     * Creates new D-Light Tool configuration using the name <code>toolName</code>
     * @param toolName tool name configuration will be created for
     */
    public DLightToolConfiguration(String toolName)  {
        this(toolName,  toolName);

    }

    /**
     * Creates new D-Light Tool configuration using the name <code>toolName</code>
     * @param toolName tool name configuration will be created for
     * @param detailedToolName  detailed tool name
     */
    public DLightToolConfiguration(String toolName, String detailedToolName)  {
        this.toolName = toolName;
        this.detailedToolName = detailedToolName;
        dataCollectors = Collections.synchronizedList(new ArrayList<DataCollectorConfiguration>());
        indicators = Collections.synchronizedList(new ArrayList<IndicatorConfiguration>());
        indicatorDataProvidersConfiguration = Collections.synchronizedList(new ArrayList<IndicatorDataProviderConfiguration>());
    }

    /**
     * Adds {@link org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration}
     * @param collector collector configuration
     */
    public void addDataCollectorConfiguration(DataCollectorConfiguration collector) {
        dataCollectors.add(collector);
    }

    public void setIcon(String iconPath){
        this.iconPath = iconPath;
    }

    /**
     * Adds indicator configration
     * @param indicator indicator configuration
     */
    public void addIndicatorConfiguration(IndicatorConfiguration indicator) {
        indicators.add(indicator);
    }

    /**
     * Adds indicator data provider configuration
     * @param indDataProvider indicator data provider configuration
     */
    public void addIndicatorDataProviderConfiguration(IndicatorDataProviderConfiguration indDataProvider) {
        if (indDataProvider == null) {
            throw new NullPointerException(
                    "An attempt to add NULL IndicatorDataProviderConfiguration"); // NOI18N
        }
        indicatorDataProvidersConfiguration.add(indDataProvider);
    }

    List<DataCollectorConfiguration> getDataCollectors() {
        return dataCollectors;
    }

    List<IndicatorDataProviderConfiguration> getIndicatorDataProviders() {
        return indicatorDataProvidersConfiguration;
    }

    List<IndicatorConfiguration> getIndicators() {
        return indicators;
    }

    String getToolName() {
        return toolName;
    }

    String getDetailedToolName() {
        return detailedToolName;
    }

    private static final class DLightToolConfigurationAccessorIml extends DLightToolConfigurationAccessor {

        @Override
        public List<DataCollectorConfiguration> getDataCollectors(DLightToolConfiguration conf) {
            return conf.getDataCollectors();
        }

        @Override
        public List<IndicatorDataProviderConfiguration> getIndicatorDataProviders(DLightToolConfiguration conf) {
            return conf.getIndicatorDataProviders();
        }

        @Override
        public List<IndicatorConfiguration> getIndicators(DLightToolConfiguration conf) {
            return conf.getIndicators();
        }

        @Override
        public String getToolName(DLightToolConfiguration conf) {
            return conf.getToolName();
        }

        @Override
        public String getDetailedToolName(DLightToolConfiguration conf) {
            return conf.getDetailedToolName();
        }


        @Override
        public String getIconPath(DLightToolConfiguration conf) {
            return conf.iconPath;
        }
    }
}
