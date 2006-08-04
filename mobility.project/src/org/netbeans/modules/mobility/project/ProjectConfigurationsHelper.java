/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.project;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Icon;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.configurations.ProjectConfiguration;
import org.netbeans.api.project.configurations.ProjectConfigurationsProvider;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Helper class implementing ProjectConfigurationsProvider for Ant based projects.
 * @author Adam Sotona, David Kaspar
 */
public final class ProjectConfigurationsHelper implements ProjectConfigurationsProvider, AntProjectListener {
    
    /**
     * name of the Ant property storing the active configuration
     */
    public static final String PROJ_PROP_CONFIGURATION_ACTIVE = "config.active";  // NOI18N
    
    /**
     * XML element name used to store configurations in <code>project.xml</code>.
     */
    static final String CONFIGS_NAME = "configurations"; // NOI18N
    
    /**
     * XML element name used to store one configuration in <code>project.xml</code>.
     */
    static final String CONFIG_NAME = "configuration"; // NOI18N
    
    /**
     * XML namespace used to store configurations in <code>project.xml</code>.
     */
    static final String CONFIGS_NS = "http://www.netbeans.org/ns/project-configurations/1"; // NOI18N
    
    /**
     * Default configuration name.
     */
    static final String DEFAULT_CONFIGURATION_NAME = "DefaultConfiguration"; // NOI18N
    
    /**
     * Default configuration name.
     */
    public static final String PROJECT_PROPERTIES = "ProjectProperties"; // NOI18N
    
    protected final AntProjectHelper h;
    protected final AuxiliaryConfiguration aux;
    protected HashMap<String,ProjectConfiguration> configurationsByName = new HashMap<String,ProjectConfiguration>();
    private PropertyChangeSupport psp;
    private ProjectConfiguration activeConfiguration;
    private ProjectConfiguration[] configurations;
    private ProjectConfiguration defaultConfiguration;
    
    
    /**
     * Creates new instance of the helper.
     * @param helper AntProjectHelper for accessing Ant project properties.
     * @param emp ExtensibleMetadataProvider to access project XML.
     */
    public ProjectConfigurationsHelper(AntProjectHelper helper, AuxiliaryConfiguration aux) {
        h = helper;
        this.aux = aux;
    }
    
    public synchronized ProjectConfiguration getDefaultConfiguration() {
        if (defaultConfiguration == null) {
            defaultConfiguration = createConfiguration(DEFAULT_CONFIGURATION_NAME); //NOI18N
        }
        return defaultConfiguration;
    }
    
    /**
     * Load <config> from project.xml.
     * @param create if true, create an empty element if it was missing, else leave as null
     */
    protected Element loadConfigs(final boolean create) {
        Element configs = aux.getConfigurationFragment(CONFIGS_NAME, CONFIGS_NS, true);
        if (configs == null && create) {
            configs = XMLUtil.createDocument("ignore", null, null, null).createElementNS(CONFIGS_NS, CONFIGS_NAME); // NOI18N
        }
        return configs;
    }
    
    /**
     * Store <config> to project.xml (i.e. to memory and mark project modified).
     */
    protected void storeConfigs(final Element configs) {
        assert configs != null && configs.getLocalName().equals(CONFIGS_NAME) && CONFIGS_NS.equals(configs.getNamespaceURI());
        aux.putConfigurationFragment(configs, true);
    }
    
    /**
     * Append new configuration to the project.
     * @param configName String new configuration name
     * @return boolean success
     */
    public final boolean addConfiguration(final String configName) {
        if (configName == null || configName.equals(getDefaultConfiguration().getName())) return false;
        return (ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                final Element configs = loadConfigs(true);
                boolean success;
                try {
                    success = addConfig(configName, configs);
                } catch (IllegalArgumentException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return Boolean.FALSE;
                }
                if (success) {
                    storeConfigs(configs);
                }
                return Boolean.valueOf(success);
            }
        })).booleanValue();
    }
    
    protected static boolean addConfig(final String configName, final Element configs) {
        Node nextConfig = null;
        final NodeList subEls = configs.getElementsByTagNameNS(CONFIGS_NS, CONFIG_NAME);
        int comp = -1;
        for (int i=0; i<subEls.getLength() && comp < 0; i++) {
            nextConfig = subEls.item(i);
            comp = getConfigName(nextConfig).compareTo(configName);
        }
        if (comp == 0) return false;
        // Need to insert a new record before nextRef.
        final Element newConfigEl = createConfigElement(configs.getOwnerDocument(), configName);
        // Note: OK if nextConfig == null, that means insert as last child.
        configs.insertBefore(newConfigEl, nextConfig);
        return true;
    }
    
    public Map<String,String> getAbilitiesFor(final ProjectConfiguration cfg) {
        final String abilities = J2MEProjectUtils.evaluateProperty(h, DefaultPropertiesDescriptor.ABILITIES, cfg.getName());
        final Map<String,String> m = abilities == null ? new HashMap<String,String>() : CommentingPreProcessor.decodeAbilitiesMap(abilities);
        m.put("DebugLevel", J2MEProjectUtils.evaluateProperty(h, DefaultPropertiesDescriptor.DEBUG_LEVEL, cfg.getName())); //NOI18N
        return m;
    }
    
    public Map<String,String> getActiveAbilities() {
        return getAbilitiesFor(getActiveConfiguration());
    }
    
    public Set<String> getAllIdentifiers(final boolean includeConfigNames) {
        final TreeSet<String> s = new TreeSet<String>();
        final ProjectConfiguration devConfigs[] = getConfigurations();
        for (int i=0; i<devConfigs.length; i++) {
            if (includeConfigNames) s.add(devConfigs[i].getName());
            final String propName = getDefaultConfiguration().equals(devConfigs[i]) ? DefaultPropertiesDescriptor.ABILITIES : J2MEProjectProperties.CONFIG_PREFIX + devConfigs[i].getName() + '.' + DefaultPropertiesDescriptor.ABILITIES;
            final String prop = h.getStandardPropertyEvaluator().getProperty(propName);
            if (prop != null) s.addAll(CommentingPreProcessor.decodeAbilitiesMap(prop).keySet());
        }
        return s;
    }
    
    /**
     * Remove configuration from project.
     * @param configName String configuration name
     * @return boolean success
     */
    public final boolean removeConfiguration(final ProjectConfiguration config) {
        if (config == null || config.equals(getDefaultConfiguration())) return false;
        return (ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                final Element configs = loadConfigs(true);
                boolean success;
                try {
                    success = removeConfig(config.getName(), configs);
                } catch (IllegalArgumentException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return Boolean.FALSE;
                }
                if (success) {
                    storeConfigs(configs);
                }
                // Note: try to delete obsoleted properties from both project.properties
                // and private.properties, just in case.
                final String[] PROPS_PATHS = {
                    AntProjectHelper.PROJECT_PROPERTIES_PATH,
                    AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                };
                final String projProp = "configs." + config.getName(); // NOI18N
                for (int i = 0; i < PROPS_PATHS.length; i++) {
                    final EditableProperties props = h.getProperties(PROPS_PATHS[i]);
                    if (props.containsKey(projProp)) {
                        props.remove(projProp);
                        h.putProperties(PROPS_PATHS[i], props);
                        success = true;
                    }
                }
                return Boolean.valueOf(success);
            }
        })).booleanValue();
    }
    
    protected static boolean removeConfig(final String configName, final Element configs) {
        final NodeList subEls = configs.getElementsByTagNameNS(CONFIGS_NS, CONFIG_NAME);
        for (int i=0; i<subEls.getLength(); i++) {
            final Node configEl = subEls.item(i);
            if (getConfigName(configEl).equals(configName)) {
                configs.removeChild(configEl);
                return true;
            }
        }
        // Searched through to the end and did not find it.
        return false;
    }
    
    /**
     * Implements ProjectConfigurationsProvider.
     * Get list of project configuration names.
     * @return ProjectConfiguration[] list.
     */
    public final synchronized ProjectConfiguration[] getConfigurations() {
        if (configurations == null) {
            configurations = ProjectManager.mutex().readAccess(new Mutex.Action<ProjectConfiguration[]>() {
                public ProjectConfiguration[] run() {
                    final Element configs = loadConfigs(false);
                    if (configs != null) {
                        try {
                            final NodeList subEls = configs.getElementsByTagNameNS(CONFIGS_NS, CONFIG_NAME);
                            ProjectConfiguration confs[] = new ProjectConfiguration[subEls.getLength()+1];
                            final HashMap<String,ProjectConfiguration> newByName = new HashMap<String,ProjectConfiguration>();
                            confs[0] = getDefaultConfiguration();
                            newByName.put(confs[0].getName(), confs[0]);
                            for (int i=1; i<confs.length; i++) {
                                final String configName = getConfigName(subEls.item(i-1));
                                confs[i] = configurationsByName.get(configName);
                                if (confs[i] == null) confs[i] = createConfiguration(configName);
                                newByName.put(configName, confs[i]);
                            }
                            configurationsByName = newByName;
                            return confs;
                        } catch (IllegalArgumentException e) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                    return new ProjectConfiguration[] {getDefaultConfiguration()};
                }
            });
        }
        // returning just "configuration" here exposes private object to possible outside modification
        return configurations == null ? null : (ProjectConfiguration[]) configurations.clone();
    }
    
    protected static String getConfigName(final Node xml) throws IllegalArgumentException {
        if (!CONFIG_NAME.equals(xml.getLocalName()) || !CONFIGS_NS.equals(xml.getNamespaceURI())) {
            throw new IllegalArgumentException("bad element name: " + xml); // NOI18N
        }
        final NodeList l = xml.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                return ((Text)l.item(i)).getNodeValue();
            }
        }
        return null;
    }
    
    private static Element createConfigElement(final Document ownerDocument, final String configName) {
        final Element el = ownerDocument.createElementNS(CONFIGS_NS, CONFIG_NAME);
        el.appendChild(ownerDocument.createTextNode(configName));
        return el;
    }
    
    /**
     * Implements ProjectConfigurationsProvider.
     * Allows listenning on configurations and active configuration.
     * @param lst PropertyChangeListener
     */
    public final void addPropertyChangeListener(final PropertyChangeListener lst) {
        synchronized (this) {
            if (psp == null) {
                psp = new PropertyChangeSupport(this);
                getConfigurations();
                getActiveConfiguration();
                h.addAntProjectListener(this);
            }
        }
        psp.addPropertyChangeListener(lst);
    }
    
    /**
     * Implements ProjectConfigurationsProvider.
     * Get currently active configuration of the project.
     * @return String active configuration name.
     */
    public final synchronized ProjectConfiguration getActiveConfiguration() {
        if (activeConfiguration == null) {
            activeConfiguration = getDefaultConfiguration();
            final String confName = h.getStandardPropertyEvaluator().getProperty(PROJ_PROP_CONFIGURATION_ACTIVE);
            if (confName == null || confName.length() == 0) return getDefaultConfiguration();
            final ProjectConfiguration confs[] = getConfigurations();
            for (int i=0; i<confs.length; i++) {
                if (confName.equals((confs[i]).getName())) {
                    activeConfiguration = confs[i];
                    return activeConfiguration;
                }
            }
        }
        return activeConfiguration;
    }
    
    
    /**
     * Helper method that returns ProjectConfiguration by name
     * @param configName name of the ProjectConfiguration to retrieve
     * @return ProjectConfiguration object that has the passed name
     */
    public final synchronized ProjectConfiguration getConfigurationByName(final String configName) {
        return configurationsByName.get(configName);
    }
    
    
    /**
     * Implements ProjectConfigurationsProvider.
     * Removes listener on configurations and active configuration.
     * @param lst PropertyChangeListener
     */
    public final void removePropertyChangeListener(final PropertyChangeListener lst) {
        if (psp != null) psp.removePropertyChangeListener(lst);
    }
    
    protected ProjectConfiguration createConfiguration(final String configName) {
        return new ConfigurationImpl(configName);
    }
    
    /**
     * Implements ProjectConfigurationsProvider.
     * Set the active configuration name.
     * Fire property change with PROP_CONFIGURATION_ACTIVE to all listeners.
     * @param configuration new active ProjectConfiguration
     */
    public final synchronized void setActiveConfiguration(final ProjectConfiguration configuration) {
        final ProjectConfiguration oldAC = activeConfiguration;
        activeConfiguration = null;
        
        final EditableProperties ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.put(PROJ_PROP_CONFIGURATION_ACTIVE, (configuration == null || configuration.equals(getDefaultConfiguration())) ? "" : configuration.getName()); //NOI18N
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Object>() {
                public Object run() {
                    h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    return null;
                }
            });
            final ProjectManager pm = ProjectManager.getDefault();
            pm.saveProject(pm.findProject(h.getProjectDirectory()));
        } catch (MutexException me) {
            ErrorManager.getDefault().notify(me.getException());
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        
        final ProjectConfiguration newAC = getActiveConfiguration();
        if ((oldAC != null) ? (!oldAC.equals(newAC)) : (newAC != null))
            psp.firePropertyChange(PROP_CONFIGURATION_ACTIVE, oldAC, newAC);
    }
    
    public void configurationXmlChanged(final AntProjectEvent ev) {
        if (psp != null && AntProjectHelper.PROJECT_XML_PATH.equals(ev.getPath())) {
            final ProjectConfiguration oldCFs[] = configurations;
            configurations = null;
            final ProjectConfiguration newCFs[] = getConfigurations();
            if (!Arrays.equals(oldCFs, newCFs)) {
                psp.firePropertyChange(PROP_CONFIGURATIONS, oldCFs, newCFs);
            }
        }
    }
    
    public synchronized void propertiesChanged(final AntProjectEvent ev) {
        if (psp == null) return;
        if (AntProjectHelper.PRIVATE_PROPERTIES_PATH.equals(ev.getPath())) {
            final ProjectConfiguration oldAC = activeConfiguration;
            activeConfiguration = null;
            final ProjectConfiguration newAC = getActiveConfiguration();
            if ((oldAC == null  &&  newAC != null)  ||  (oldAC != null  && ! oldAC.equals(newAC))) {
                psp.firePropertyChange(PROP_CONFIGURATION_ACTIVE, oldAC, newAC);
            }
        } else if (AntProjectHelper.PROJECT_PROPERTIES_PATH.equals(ev.getPath())) {
            psp.firePropertyChange(PROJECT_PROPERTIES, null, getActiveConfiguration());
        }
    }
    
    private static final class ConfigurationImpl implements ProjectConfiguration {
        
        private final String name;
        
        public ConfigurationImpl(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public Icon getIcon() {
            return null;
        }
        
        public boolean equals(final Object o) {
            if (! (o instanceof ConfigurationImpl))
                return false;
            final String name2 = ((ConfigurationImpl) o).getName();
            return (name != null) ? name.equals(name2) : name2 == null;
        }
        
        public int hashCode() {
            return (name != null) ? name.hashCode() : 0;
        }
        
    }
}
