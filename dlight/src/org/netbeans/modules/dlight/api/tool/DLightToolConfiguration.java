/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.api.tool;

import java.beans.FeatureDescriptor;
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
    private String shortDescription;
    FeatureDescriptor detailsDescription;

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

    public void setFeatureDescriptor(FeatureDescriptor descriptor){
        this.detailsDescription = descriptor;
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

    public void setDescription(final String description){
        this.shortDescription = description;
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

    String getDescription(){
        return shortDescription;
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

        @Override
        public String getToolDescription(DLightToolConfiguration conf) {
            return conf.getDescription();
        }
    }
}
