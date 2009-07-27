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

    private final String id;
    private final List<DataCollectorConfiguration> dataCollectors;
    private final List<IndicatorDataProviderConfiguration> indicatorDataProvidersConfiguration;
    private final List<IndicatorConfiguration> indicators;
    private String shortName;
    private String longName;
    private boolean visible;
    private String iconPath = null;

    static {
        DLightToolConfigurationAccessor.setDefault(new DLightToolConfigurationAccessorImpl());
    }

    /**
     * Creates new DLightToolConfiguration with the specified ID
     * @param id ID of the tool to be created
     */
    public DLightToolConfiguration(final String id) {
        this(id, id);
    }

    /**
     * Creates new DLightToolConfiguration with the specified ID and name.
     * visibility flag of created tool is set to <code>true<code>
     * long name is initialized with the shortName provided.
     * @param id
     * @param name
     */
    public DLightToolConfiguration(final String id, final String name) {
        this.id = id;
        this.shortName = name;
        this.longName = name;
        this.visible = true;
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

    public void setIcon(String iconPath) {
        this.iconPath = iconPath;
    }

    public void setLongName(final String longName) {
        this.longName = longName;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public String getID() {
        return id;
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

    String getShortName() {
        return shortName;
    }

    String getLongName() {
        return longName;
    }

    boolean isVisible() {
        return visible;
    }

    private static final class DLightToolConfigurationAccessorImpl extends DLightToolConfigurationAccessor {

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
            return conf.getShortName();
        }

        @Override
        public String getDetailedToolName(DLightToolConfiguration conf) {
            return conf.getLongName();
        }

        @Override
        public String getIconPath(DLightToolConfiguration conf) {
            return conf.iconPath;
        }
    }
}
