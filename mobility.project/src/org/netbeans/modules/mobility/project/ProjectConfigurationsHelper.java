/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.mobility.project;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

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
    private boolean preprocessorOn, invalid = true;
    
    /**
     * Creates new instance of the helper.
     * @param helper AntProjectHelper for accessing Ant project properties.
     * @param emp ExtensibleMetadataProvider to access project XML.
     */
    public ProjectConfigurationsHelper(AntProjectHelper helper, J2MEProject p) {
        this.h = helper;
        this.p = p;
    }

    public boolean isPreprocessorOn() {
        if (invalid) {
            String prop = h.getStandardPropertyEvaluator().getProperty(DefaultPropertiesDescriptor.USE_PREPROCESSOR);
            preprocessorOn = prop == null || Boolean.parseBoolean(prop);
            invalid = false;
        }
        return preprocessorOn;
    }
    
    public ProjectConfiguration getDefaultConfiguration() {
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
                        else if (add && i > 0) {
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
    public final Collection<ProjectConfiguration> getConfigurations() {
        return getConfigurations(configurations);
    }
    
    private final Collection<ProjectConfiguration> getConfigurations(final TreeMap<String,ProjectConfiguration> oldConfig) {
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
    public final ProjectConfiguration getActiveConfiguration() {
        if (activeConfiguration == null) {
            ProjectManager.mutex().readAccess(new Action<Boolean>(){
                public Boolean run(){
                    activeConfiguration = getDefaultConfiguration();
                    final String confName = h.getStandardPropertyEvaluator().getProperty(PROJ_PROP_CONFIGURATION_ACTIVE);
                    if (confName == null || confName.length() == 0) {
                        activeConfiguration = getDefaultConfiguration();
                        return null;
                    }
                    final ProjectConfiguration confs[] = getConfigurations().toArray(new ProjectConfiguration[0]);
                    for (int i = 0; i < confs.length; i++) {
                        if (confName.equals((confs[i]).getDisplayName())) {
                            activeConfiguration = confs[i];
                            return null;
                        }
                    }
                    return null;                    
                }
            });
        }
        return activeConfiguration;
    }
    
    /**
     * Helper method that returns ProjectConfiguration by name
     * @param configName name of the ProjectConfiguration to retrieve
     * @return ProjectConfiguration object that has the passed name
     */
    public final ProjectConfiguration getConfigurationByName(String configName) {
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
    public final void setActiveConfiguration(final ProjectConfiguration configuration) throws IllegalArgumentException, IOException {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ProjectConfigurationsHelper.class,
            "TTL_CHANGE_CONFIG")); //NOI18N
        //First do this to get outside ProjectManager.mutex()
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JComponent comp = ProgressHandleFactory.createProgressComponent(handle);
                JLabel lbl = ProgressHandleFactory.createMainLabelComponent(handle);
                JPanel pnl = new JPanel(new BorderLayout());
                pnl.add (lbl, BorderLayout.NORTH);
                pnl.add (comp, BorderLayout.SOUTH);
                String title = NbBundle.getMessage(ProjectConfigurationsHelper.class,
                        "MSG_CHANGE_CONFIG"); //NOI18N
                JDialog dlg = new JDialog (WindowManager.getDefault().getMainWindow());
                dlg.setModal(true);
                dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dlg.setTitle(title);
                dlg.getContentPane().setLayout(new BorderLayout());
                dlg.getContentPane().add(pnl, BorderLayout.CENTER);
                dlg.setLocationRelativeTo(dlg.getParent());
                RequestProcessor.Task task = p.getRequestProcessor().post(new ConfigurationChanger(handle, configuration, dlg));
                try {
                    //50ms not noticable to the user - we can wait this long
                    //before showing a dialog - simple projects will be fast.
                    task.waitFinished(50);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (!task.isFinished()) {
                    dlg.pack();
                    dlg.setVisible(true);
                }
            }
        });

    }
    
    private final class ConfigurationChanger extends WindowAdapter implements Runnable {
        private final ProgressHandle handle;
        private final ProjectConfiguration configuration;
        private final Dialog dlg;
        private volatile boolean done;
        ConfigurationChanger(ProgressHandle handle, ProjectConfiguration config, Dialog dlg) {
            this.configuration = config;
            this.handle = handle;
            this.dlg = dlg;
            handle.start();
            handle.switchToIndeterminate();
            dlg.addWindowListener(this);
        }

        @Override
        public void windowOpened(WindowEvent e) {
            //This can happen:
            //Task completes and tries to hide the dialog before it actually
            //gets on screen.  So we also listen to the window and make sure
            //it doesn't show up *after* the task has completed
            if (done) {
                runOnEQ();
            }
        }

        public void run() {
            if (!EventQueue.isDispatchThread()) {
                try {
                    runOffEQ();
                } finally {
                    handle.finish();
                    EventQueue.invokeLater(this);
                }
                done = true;
            } else {
                runOnEQ();
            }
        }

        private void runOnEQ() {
            dlg.setVisible(false);
            dlg.dispose();
        }

        private void runOffEQ() {
            final ProjectConfiguration oldAC = activeConfiguration;
            activeConfiguration = null;

            final EditableProperties ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            ep.put(PROJ_PROP_CONFIGURATION_ACTIVE, (configuration == null ||
                    configuration.equals(getDefaultConfiguration())) ? "" : configuration.getDisplayName()); //NOI18N
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Object>() {
                    public Object run() {
                        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                        return null;
                    }
                });
                ProjectManager.getDefault().saveProject(p);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } catch (MutexException me) {
                Exceptions.printStackTrace(me);
            }

            final ProjectConfiguration newAC = getActiveConfiguration();
            if ((oldAC != null) ? (!oldAC.equals(newAC)) : (newAC != null)) {
                psp.firePropertyChange(PROP_CONFIGURATION_ACTIVE, oldAC, newAC);
            }
        }
        
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
            invalid = true;
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
