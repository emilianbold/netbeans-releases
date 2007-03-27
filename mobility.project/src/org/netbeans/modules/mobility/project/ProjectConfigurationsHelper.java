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
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mobility.project.ui.J2MECustomizerProvider;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

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
     * Default configuration name.
     */
    static public final String DEFAULT_CONFIGURATION_NAME = "DefaultConfiguration"; // NOI18N
    
    /**
     * Default configuration name.
     */
    public static final String PROJECT_PROPERTIES = "ProjectProperties"; // NOI18N
    
    protected final AntProjectHelper h;
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
    public ProjectConfigurationsHelper(AntProjectHelper helper, J2MEProject p) {
        this.h = helper;
        this.p = p;
    }
    
    public synchronized ProjectConfiguration getDefaultConfiguration() {
        if (defaultConfiguration == null) {
            defaultConfiguration = createConfiguration(DEFAULT_CONFIGURATION_NAME); //NOI18N
        }
        return defaultConfiguration;
    }
    
    /**
     * Append new configuration to the project.
     * @param configName String new configuration name
     * @return boolean success
     */
    public final boolean addConfiguration(final String configName) {
        if (configName == null || configName.equals(getDefaultConfiguration().getDisplayName())) return false;
        boolean ret = (ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String cfgs = props.getProperty(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS);
                if (cfgs == null) cfgs = ""; //NOI18N
                boolean add = true;
                StringBuffer sb = new StringBuffer(" ");
                for (String s : cfgs.split(",")) { //NOI18N
                    if (s.trim().length() > 0) {
                        int i = s.compareTo(configName);
                        if (i == 0) return Boolean.FALSE;
                        else if (i > 0) {
                            add = false;
                            sb.append(',').append(configName);
                        }
                        sb.append(',').append(s);
                    }
                }
                if (add) {
                    sb.append(',').append(configName);
                }
                props.put(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS, sb.toString());
                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                return Boolean.TRUE;
            }
        })).booleanValue();
        try {
            ProjectManager.getDefault().saveProject(p);
            return ret;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return false;
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
        boolean ret = (ProjectManager.mutex().writeAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                String cfgs = props.getProperty(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS);
                if (cfgs == null) return Boolean.FALSE;
                boolean succ = false;
                StringBuffer sb = new StringBuffer();
                for (String s : cfgs.split(",")) { //NOI18N
                    if (s.equals(config.getDisplayName())) {
                        succ = true;
                    } else {
                        if (sb.length() > 0) sb.append(',');
                        sb.append(s);
                    }
                }
                if (succ) {
                    final String projProp = "configs." + config.getDisplayName(); // NOI18N
                    for (String key : props.keySet().toArray(new String[0])) {
                        if (key.startsWith(projProp)) props.remove(key);
                    }
                    props.put(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS, sb.toString());
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                }
                return Boolean.valueOf(succ);
            }
        })).booleanValue();
        try {
            ProjectManager.getDefault().saveProject(p);
            return ret;
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
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
                    final TreeMap<String,ProjectConfiguration> newByName = new TreeMap<String,ProjectConfiguration>(new Comparator<String>() {
                        public int compare(String o1, String o2) {
                            return DEFAULT_CONFIGURATION_NAME.equals(o1) ? (DEFAULT_CONFIGURATION_NAME.equals(o2) ? 0 : -1) : (DEFAULT_CONFIGURATION_NAME.equals(o2) ? 1 : o1.compareToIgnoreCase(o2));
                        }
                    });
                    newByName.put(getDefaultConfiguration().getDisplayName(),getDefaultConfiguration());
                    String cfgs = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(DefaultPropertiesDescriptor.ALL_CONFIGURATIONS);
                    if (cfgs != null) {
                        for (String configName : cfgs.split(",")) { //NOII8N
                            if (configName.length() > 0 && !configName.equals(" ")) { //NOI18N
                                ProjectConfiguration conf = oldConfig == null ? null : oldConfig.get(configName);
                                newByName.put(configName, conf == null ? createConfiguration(configName) : conf);
                            }
                        }
                    }
                    return newByName;
                }
            });
        }
        return configurations == null ? null : Collections.unmodifiableCollection(configurations.values());
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
            ProjectManager.getDefault().saveProject(p);
        } catch (MutexException me) {
            ErrorManager.getDefault().notify(me.getException());
        }
        
        final ProjectConfiguration newAC = getActiveConfiguration();
        if ((oldAC != null) ? (!oldAC.equals(newAC)) : (newAC != null))
            psp.firePropertyChange(PROP_CONFIGURATION_ACTIVE, oldAC, newAC);
    }
    
    public void configurationXmlChanged(final AntProjectEvent ev) {
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
            final TreeMap<String,ProjectConfiguration> old = configurations;
            final ProjectConfiguration oldCFs[]=old.values().toArray(new ProjectConfiguration[old.size()]);
            configurations = null;
            final ProjectConfiguration newCFs[] = getConfigurations(old).toArray(new ProjectConfiguration[0]);
            if (!Arrays.equals(oldCFs, newCFs)) {
                psp.firePropertyChange(PROP_CONFIGURATIONS, oldCFs, newCFs);
            }
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
