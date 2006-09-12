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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.ui.J2MECustomizerProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
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
 * Helper class implementing ProjectConfigurationProvider for Ant based projects.
 * @author Adam Sotona, David Kaspar
 */
public final class ProjectConfigurationsHelper implements ProjectConfigurationProvider<ProjectConfiguration>, AntProjectListener {
    
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
    private TreeMap<String,ProjectConfiguration> configurations;
    private PropertyChangeSupport psp;
    private ProjectConfiguration activeConfiguration;
    //private ProjectConfiguration[] configurations;
    private ProjectConfiguration defaultConfiguration;
    private J2MEProject p;
    
    /**
     * Creates new instance of the helper.
     * @param helper AntProjectHelper for accessing Ant project properties.
     * @param emp ExtensibleMetadataProvider to access project XML.
     */
    public ProjectConfigurationsHelper(AntProjectHelper helper, AuxiliaryConfiguration aux, J2MEProject p) {
        this.h = helper;
        this.aux = aux;
        this.p = p;
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
        if (configName == null || configName.equals(getDefaultConfiguration().getDisplayName())) return false;
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
        final String abilities = J2MEProjectUtils.evaluateProperty(h, DefaultPropertiesDescriptor.ABILITIES, cfg.getDisplayName());
        final Map<String,String> m = abilities == null ? new HashMap<String,String>() : CommentingPreProcessor.decodeAbilitiesMap(abilities);
        m.put("DebugLevel", J2MEProjectUtils.evaluateProperty(h, DefaultPropertiesDescriptor.DEBUG_LEVEL, cfg.getDisplayName())); //NOI18N
        return m;
    }
    
    public Map<String,String> getActiveAbilities() {
        return getAbilitiesFor(getActiveConfiguration());
    }
    
    public Set<String> getAllIdentifiers(final boolean includeConfigNames) {
        final TreeSet<String> s = new TreeSet<String>();
        final ProjectConfiguration devConfigs[] = getConfigurations().toArray(new ProjectConfiguration[0]);
        for (int i=0; i<devConfigs.length; i++) {
            if (includeConfigNames) s.add(devConfigs[i].getDisplayName());
            final String propName = getDefaultConfiguration().equals(devConfigs[i]) ? DefaultPropertiesDescriptor.ABILITIES : J2MEProjectProperties.CONFIG_PREFIX + devConfigs[i].getDisplayName() + '.' + DefaultPropertiesDescriptor.ABILITIES;
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
                    success = removeConfig(config.getDisplayName(), configs);
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
                final String projProp = "configs." + config.getDisplayName(); // NOI18N
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
     * Implements ProjectConfigurationProvider.
     * Get list of project configuration names.
     * @return ProjectConfiguration[] list.
     */
    public final synchronized Collection<ProjectConfiguration> getConfigurations() {
        return getConfigurations(configurations);
    }
    
    private final synchronized Collection<ProjectConfiguration> getConfigurations(final TreeMap<String,ProjectConfiguration> oldConfig) {
        if (configurations == null) {
            configurations = ProjectManager.mutex().readAccess(new Mutex.Action<TreeMap<String,ProjectConfiguration>>() {
                public TreeMap<String,ProjectConfiguration> run() {
                    final Element configs = loadConfigs(false);
                    final TreeMap<String,ProjectConfiguration> newByName = new TreeMap<String,ProjectConfiguration>(new Comparator<String>() {
                        public int compare(String o1, String o2) {
                            return DEFAULT_CONFIGURATION_NAME.equals(o1) ? (DEFAULT_CONFIGURATION_NAME.equals(o2) ? 0 : -1) : (DEFAULT_CONFIGURATION_NAME.equals(o2) ? 1 : o1.compareToIgnoreCase(o2));
                        }
                    });
                    newByName.put(getDefaultConfiguration().getDisplayName(),getDefaultConfiguration());
                    if (configs != null) {
                        try {
                            final NodeList subEls = configs.getElementsByTagNameNS(CONFIGS_NS, CONFIG_NAME);
                            for (int i=0; i<subEls.getLength(); i++) {
                                final String configName = getConfigName(subEls.item(i));
                                final ProjectConfiguration conf = oldConfig == null ? null : oldConfig.get(configName);
                                if ( conf == null ) {
                                    final ProjectConfiguration confNew = createConfiguration(configName);
                                    newByName.put(configName, confNew);
                                } else
                                    newByName.put(configName,conf);
                            }
                        } catch (IllegalArgumentException e) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        }
                    }
                    return newByName;
                }
            });
        }
        return configurations == null ? null : Collections.unmodifiableCollection(configurations.values());
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
     * Implements ProjectConfigurationProvider.
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
     * Implements ProjectConfigurationProvider.
     * Get currently active configuration of the project.
     * @return String active configuration name.
     */
    public final synchronized ProjectConfiguration getActiveConfiguration() {
        if (activeConfiguration == null) {
            activeConfiguration = getDefaultConfiguration();
            final String confName = h.getStandardPropertyEvaluator().getProperty(PROJ_PROP_CONFIGURATION_ACTIVE);
            if (confName == null || confName.length() == 0) return getDefaultConfiguration();
            final ProjectConfiguration confs[] = getConfigurations().toArray(new ProjectConfiguration[0]);
            for (int i=0; i<confs.length; i++) {
                if (confName.equals((confs[i]).getDisplayName())) {
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
    public final synchronized ProjectConfiguration getConfigurationByName(String configName) {
        return configurations.get(configName);
    }
    
    /**
     * Implements ProjectConfigurationProvider.
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
     * Implements ProjectConfigurationProvider.
     * Set the active configuration name.
     * Fire property change with PROP_CONFIGURATION_ACTIVE to all listeners.
     * @param configuration new active ProjectConfiguration
     */
    public final synchronized void setActiveConfiguration(ProjectConfiguration configuration) throws IllegalArgumentException, IOException {
        final ProjectConfiguration oldAC = activeConfiguration;
        activeConfiguration = null;
        
        final EditableProperties ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.put(PROJ_PROP_CONFIGURATION_ACTIVE, (configuration == null || configuration.equals(getDefaultConfiguration())) ? "" : configuration.getDisplayName()); //NOI18N
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
        }
        
        final ProjectConfiguration newAC = getActiveConfiguration();
        if ((oldAC != null) ? (!oldAC.equals(newAC)) : (newAC != null))
            psp.firePropertyChange(PROP_CONFIGURATION_ACTIVE, oldAC, newAC);
    }
    
    public void configurationXmlChanged(final AntProjectEvent ev) {
        if (psp != null && AntProjectHelper.PROJECT_XML_PATH.equals(ev.getPath())) {
            final TreeMap<String,ProjectConfiguration> old = configurations;
            final ProjectConfiguration oldCFs[]=old.values().toArray(new ProjectConfiguration[old.size()]);
            configurations = null;
            final ProjectConfiguration newCFs[] = getConfigurations(old).toArray(new ProjectConfiguration[0]);
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
    
    public boolean hasCustomizer() {
        return true;
    }
    
    public void customize() {
        final J2MECustomizerProvider cp = p.getLookup().lookup(J2MECustomizerProvider.class);
        if (cp != null) SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                cp.showCustomizer(true);
            }
        });
    }
    
    public boolean configurationsAffectAction(String command) {
        return false;
    }
    
    private static final class ConfigurationImpl implements ProjectConfiguration {
        
        private final String name;
        
        public ConfigurationImpl(String name) {
            this.name = name;
        }
        
        public String getDisplayName() {
            return name;
        }
        
        public boolean equals(final Object o) {
            if (! (o instanceof ConfigurationImpl))
                return false;
            final String name2 = ((ConfigurationImpl) o).getDisplayName();
            return (name != null) ? name.equals(name2) : name2 == null;
        }
        
        public int hashCode() {
            return (name != null) ? name.hashCode() : 0;
        }
        
    }
}
