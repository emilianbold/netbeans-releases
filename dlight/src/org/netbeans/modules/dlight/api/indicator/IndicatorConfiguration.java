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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.api.indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.impl.IndicatorConfigurationAccessor;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;

/**
 * Configuration to create {@link org.netbeans.modules.dlight.spi.indicator.Indicator}
 * on the base of. Use it to register in
 * {@link org.netbeans.modules.dlight.api.tool.DLightToolConfiguration#addIndicatorConfiguration(org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration)}
 *  
 */
public abstract class IndicatorConfiguration {

    private final IndicatorMetadata metadata;
    private final int position;
    private final List<VisualizerConfiguration> visualizerConfigurations;
    private final List<IndicatorActionDescriptor> actionDescriptors;
    private IndicatorActionDescriptor defaultActionDescriptor;
    private boolean visible;

    static {
        IndicatorConfigurationAccessor.setDefault(new IndicatorConfigurationAccessorImpl());
    }

    /**
     * Created new Indicator Configuration on the base of {@link org.netbeans.modules.dlight.api.indicator.IndicatorMetadata}
     * @param metadata meta-data to create Indicator configuration for
     * @param position indicator position
     */
    public IndicatorConfiguration(IndicatorMetadata metadata, int position, boolean visible) {
        this.metadata = metadata;
        this.position = position;
        this.visible = visible;
        visualizerConfigurations = new ArrayList<VisualizerConfiguration>();
        actionDescriptors = new ArrayList<IndicatorActionDescriptor>();
    }

    /**
     * Created new Indicator Configuration on the base of {@link org.netbeans.modules.dlight.api.indicator.IndicatorMetadata}
     * @param metadata meta-data to create Indicator configuration for
     */
    public IndicatorConfiguration(IndicatorMetadata metadata) {
        this(metadata, 0, true);
    }

    /**
     * Unique id which will be used to find the proper factory
     * @return id
     */
    public abstract String getID();

    /**
     * Sets {@link org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration}
     * which means Detailed View which will be opened when double or single mouse click on
     * {@link org.netbeans.modules.dlight.spi.indicator.Indicator}
     * @param configuration configuration to create {@link org.netbeans.modules.dlight.spi.visualizer.Visualizer}
     * which will represent Detailed View
     */
    public final void addVisualizerConfiguration(VisualizerConfiguration configuration) {
        if (configuration != null && !visualizerConfigurations.contains(configuration)) {
            this.visualizerConfigurations.add(configuration);
        }
    }

    /**
     * Appends specified {@link org.netbeans.modules.dlight.api.indicator.IndicatorActionDescriptor} 
     * to a list of "open-visualizer" actions associated with the 
     * {@link org.netbeans.modules.dlight.spi.indicator.Indicator}.
     * <p>
     * Current design prohibits to provide an arbitrary action accessible via 
     * indicator's UI. Instead it gives a way to provide one or several 
     * "open-visualizer" actions based on 
     * {@link org.netbeans.modules.dlight.api.indicator.IndicatorActionDescriptor} list.
     * <p>
     * When an action associated with the descriptor is performed an attempt to 
     * open a Visualizer for specified by the descriptor 
     * {@link org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration}
     * is performed.
     * <p>
     * If descriptor contains no reference to a <code>VisualizerConfiguration</code> 
     * (i.e. it is null), then action will open a first 'suitable' visualizer 
     * identified by a configuration from a list constructed with 
     * {@link addVisualizerConfiguration(VisualizerConfiguration)}.
     * 
     * @param descr descriptor to be used for creating an "open-visualizer" action 
     * associated with the {@link org.netbeans.modules.dlight.spi.indicator.Indicator}.
     */
    public final void addActionDescription(final IndicatorActionDescriptor descr) {
        if (descr != null && !actionDescriptors.contains(descr)) {
            this.actionDescriptors.add(descr);
        }
    }

    /**
     * Sets a description of a default action associated with an indicator and 
     * adds it (if not there yet) to a list of all registered descriptions.
     * <p>
     * If default descriptor is not registered implicitly, the first added 
     * description is treated as a default one.
     * 
     * @param descr descriptor to be used for creating a default action 
     * associated with an indicator.
     * 
     * @see addActionDescription(IndicatorActionDescriptor)
     */
    public final void setDefaultActionDescription(final IndicatorActionDescriptor descr) {
        defaultActionDescriptor = descr;
        // will be added if not registered yet only
        addActionDescription(descr);
    }
    
    /**
     * Deprecated API for specifying default action's display name.
     * 
     * @param actionDisplayName - visible name of a default action
     * @see setDefaultActionDescription(IndicatorActionDescriptor)
     * @deprecated
     */
    @Deprecated
    public final void setActionDisplayName(String actionDisplayName) {
        String tooltip = null;
        
        if (defaultActionDescriptor != null) {
            actionDescriptors.remove(defaultActionDescriptor);
            tooltip = defaultActionDescriptor.getTooltip();
        }
        
        defaultActionDescriptor = new IndicatorActionDescriptor(actionDisplayName, tooltip, null);
        setDefaultActionDescription(defaultActionDescriptor);
    }

    /**
     * Deprecated API for specifying default action's tooltip text.
     * 
     * @param actionTooltip - tooltip of a default action
     * @see setDefaultActionDescription(IndicatorActionDescriptor)
     * @deprecated
     */
    @Deprecated
    public final void setActionTooltip(String actionTooltip) {
        String displayName = null;
        
        if (defaultActionDescriptor != null) {
            actionDescriptors.remove(defaultActionDescriptor);
            displayName = defaultActionDescriptor.getDisplayName();
        }
        
        defaultActionDescriptor = new IndicatorActionDescriptor(displayName, actionTooltip, null);
        setDefaultActionDescription(defaultActionDescriptor);
    }

    /**
     * Returns indicator meta-data, see {@link org.netbeans.modules.dlight.api.indicator.IndicatorMetadata}
     * @return indicator meta-data
     */
    protected final IndicatorMetadata getIndicatorMetadata() {
        return metadata;
    }

    /**
     * Returns indicator position
     * @return indicator position
     */
    protected final int getIndicatorPosition() {
        return position;
    }

    /**
     * Returns  a list of {@link org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration} used
     * by the indicator as a configuration of Detailed View which will be opened by clicking on the
     * indicator created on the base of this configuration
     * @return visualizer configurations list
     */
    protected final List<VisualizerConfiguration> getVisualizerConfigurations() {
        return Collections.unmodifiableList(visualizerConfigurations);
    }

    private IndicatorActionDescriptor getDefaultActionDescriptor() {
        // If default is initialized, return it
        
        if (defaultActionDescriptor != null) {
            return defaultActionDescriptor;
        }
                
        // If here - return first entry from the list of actionDescriptors
        if (!actionDescriptors.isEmpty()) {
            return actionDescriptors.get(0);
        }
        
        return null;
    }

    public boolean isVisible() {
        return visible;
    }

    private static final class IndicatorConfigurationAccessorImpl extends IndicatorConfigurationAccessor {

        @Override
        public IndicatorMetadata getIndicatorMetadata(IndicatorConfiguration configuration) {
            return configuration.getIndicatorMetadata();
        }

        @Override
        public int getIndicatorPosition(IndicatorConfiguration configuration) {
            return configuration.getIndicatorPosition();
        }

        @Override
        public List<VisualizerConfiguration> getVisualizerConfigurations(IndicatorConfiguration configuration) {
            return configuration.getVisualizerConfigurations();
        }

        @Override
        public List<IndicatorActionDescriptor> getActionDescriptors(IndicatorConfiguration configuration) {
            return Collections.unmodifiableList(configuration.actionDescriptors);
        }

        @Override
        public IndicatorActionDescriptor getDefaultActionDescriptor(IndicatorConfiguration configuration) {
            return configuration.getDefaultActionDescriptor();
        }
    }
}
