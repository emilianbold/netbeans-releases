/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.clientproject.browser.ClientProjectConfigurationImpl;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectPlatformImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectPlatformProvider;
import org.netbeans.modules.web.clientproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class ClientSideConfigurationProvider implements ProjectConfigurationProvider<ClientProjectConfigurationImplementation>, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ClientSideConfigurationProvider.class.getName());

    public static final String PROP_CONFIG = "config";
    public static final String CONFIG_PROPS_PATH = AntProjectHelper.PRIVATE_PROPERTIES_PATH; // NOI18N

    private Lookup.Result<ClientProjectPlatformProvider> res = 
            Lookup.getDefault().lookupResult(ClientProjectPlatformProvider.class);
    
    private final ClientSideProject p;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Map<String,ClientProjectConfigurationImplementation> configs;
    private List<ClientProjectConfigurationImplementation> orderedConfigurations;
    
    public ClientSideConfigurationProvider(ClientSideProject p) {
        this.p = p;
        res.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                refreshConfigurations();
            }
        });
        p.getEvaluator().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (PROP_CONFIG.equals(evt.getPropertyName())) {
                    LOGGER.log(Level.FINER, "Refiring " + PROP_CONFIG + " -> " + ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE);
                    pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
                }
            }
        });
    }

    private void calculateConfigs() {
        configs = new HashMap<String,ClientProjectConfigurationImplementation>();
        orderedConfigurations = new ArrayList<ClientProjectConfigurationImplementation>();
        for (ClientProjectPlatformProvider prov : res.allInstances()) {
            for (ClientProjectPlatformImplementation platform : prov.getPlatforms(p)) {
                platform.removePropertyChangeListener(this);
                platform.addPropertyChangeListener(this);
                for (ClientProjectConfigurationImplementation cfg : platform.getConfigurations()) {
                    configs.put(cfg.getId(), cfg);
                    orderedConfigurations.add(cfg);
                }
            }
        }
        LOGGER.log(Level.FINEST, "Calculated configurations: {0}", configs);
    }
    
    @Override
    public Collection<ClientProjectConfigurationImplementation> getConfigurations() {
        if (configs==null) {
            calculateConfigs();
            }
        List<ClientProjectConfigurationImplementation> l = new ArrayList<ClientProjectConfigurationImplementation>();
        l.addAll(orderedConfigurations);
        return l;
    }

    @Override
    public ClientProjectConfigurationImplementation getActiveConfiguration() {
        if (configs == null) {
            calculateConfigs();
        }
        String config = p.getEvaluator().getProperty(PROP_CONFIG);
        if (config != null && configs.containsKey(config)) {
            return configs.get(config);
        }
        return getDefaultConfiguration(orderedConfigurations);
    }

    @Override
    public void setActiveConfiguration(ClientProjectConfigurationImplementation c) throws IllegalArgumentException, IOException {
        final String n = c.getId();
        EditableProperties ep = p.getProjectHelper().getProperties(CONFIG_PROPS_PATH);
        if (Utilities.compareObjects(n, ep.getProperty(PROP_CONFIG))) {
            return;
        }
        if (n != null) {
            ep.setProperty(PROP_CONFIG, n);
        } else {
            ep.remove(PROP_CONFIG);
        }
        p.getProjectHelper().putProperties(CONFIG_PROPS_PATH, ep);
        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        ProjectManager.getDefault().saveProject(p);
        assert p.getProjectDirectory().getFileObject(CONFIG_PROPS_PATH) != null;
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void customize() {
        p.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer();
    }

    @Override
    public boolean configurationsAffectAction(String command) {
        return command.equals(ActionProvider.COMMAND_RUN)
                || command.equals(ActionProvider.COMMAND_BUILD)
                || command.equals(ActionProvider.COMMAND_CLEAN)
                || command.equals(ActionProvider.COMMAND_RUN_SINGLE);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }



    @Override
    public void propertyChange(PropertyChangeEvent e) {
        LOGGER.log(Level.FINEST, "Received {0}", e);
        refreshConfigurations();
    }
    
    private void refreshConfigurations() {
        Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
        calculateConfigs();
        Set<String> newConfigs = configs.keySet();
        if (!oldConfigs.equals(newConfigs)) {
            LOGGER.log(Level.FINER, "Firing " + ProjectConfigurationProvider.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[] {oldConfigs, newConfigs});
            pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
        }
    }

    public String[] getNewConfigurationTypes() {
        List<String> types = new ArrayList<String>();
        for (ClientProjectPlatformProvider prov : res.allInstances()) {
            for (ClientProjectPlatformImplementation platform : prov.getPlatforms(p)) {
                types.addAll(platform.getNewConfigurationTypes());
            }
        }
        return types.toArray(new String[types.size()]);
    }

    public String createNewConfiguration(String type, String newName) {
        for (ClientProjectPlatformProvider prov : res.allInstances()) {
            for (ClientProjectPlatformImplementation platform : prov.getPlatforms(p)) {
                String id = platform.createConfiguration(type, newName);
                if (id != null) {
                    return id;
                }
            }
        }
        assert false : "should never happen: no platform can create configuration of type "+type+" and name it "+newName;
        return null;
    }

    private ClientProjectConfigurationImplementation getDefaultConfiguration(List<ClientProjectConfigurationImplementation> cfgs) {
        for (ClientProjectConfigurationImplementation cfg : cfgs) {
            if (cfg instanceof ClientProjectConfigurationImpl && 
                    ((ClientProjectConfigurationImpl)cfg).canBeDefaultConfiguration()) {
                return cfg;
            }
        }
        // fallback on first one:
        return cfgs.get(0);
    }
}
