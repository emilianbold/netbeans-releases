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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.clientproject.spi.ClientProjectConfiguration;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class ClientSideConfigurationProvider extends AbstractListModel implements ProjectConfigurationProvider<ClientProjectConfiguration>, ComboBoxModel {

    private static final Logger LOGGER = Logger.getLogger(ClientSideConfigurationProvider.class.getName());

    public static final String PROP_CONFIG = "config";
    public static final String CONFIG_PROPS_PATH = AntProjectHelper.PRIVATE_PROPERTIES_PATH; // NOI18N

    private final ClientSideProject p;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final FileChangeListener fcl = new FileChangeAdapter() {
        public void fileFolderCreated(FileEvent fe) {
            update(fe);
        }
        public void fileDataCreated(FileEvent fe) {
            update(fe);
        }
        public void fileDeleted(FileEvent fe) {
            update(fe);
        }
        public void fileRenamed(FileRenameEvent fe) {
            update(fe);
        }
        private void update(FileEvent ev) {
            LOGGER.log(Level.FINEST, "Received {0}", ev);
            Set<String> oldConfigs = configs != null ? configs.keySet() : Collections.<String>emptySet();
            configDir = p.getProjectDirectory().getFileObject("nbproject/configs"); // NOI18N
            if (configDir != null) {
                configDir.removeFileChangeListener(fclWeak);
                configDir.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "(Re-)added listener to {0}", configDir);
            } else {
                LOGGER.log(Level.FINEST, "No nbproject/configs exists");
            }
            calculateConfigs();
            Set<String> newConfigs = configs.keySet();
            if (!oldConfigs.equals(newConfigs)) {
                LOGGER.log(Level.FINER, "Firing " + ProjectConfigurationProvider.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[] {oldConfigs, newConfigs});
                pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATIONS, null, null);
                // XXX also fire PROP_ACTIVE_CONFIGURATION?
            }
        }
    };
    private final FileChangeListener fclWeak;
    private FileObject configDir;
    private Map<String,ClientProjectConfiguration> configs;
    private FileObject nbp;
    
    public ClientSideConfigurationProvider(ClientSideProject p) {
        this.p = p;
        fclWeak = FileUtil.weakFileChangeListener(fcl, null);
        nbp = p.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        if (nbp != null) {
            nbp.addFileChangeListener(fclWeak);
            LOGGER.log(Level.FINEST, "Added listener to {0}", nbp);
            configDir = nbp.getFileObject("configs"); // NOI18N
            if (configDir != null) {
                configDir.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "Added listener to {0}", configDir);
            }
        }
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
        configs = new HashMap<String,ClientProjectConfiguration>();
        if (configDir != null) {
            for (FileObject kid : configDir.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                ClientProjectConfiguration conf = ClientProjectConfiguration.create(kid);
                configs.put(conf.getName(), conf);
            }
        }
        LOGGER.log(Level.FINEST, "Calculated configurations: {0}", configs);
    }

    @Override
    public Collection<ClientProjectConfiguration> getConfigurations() {
        if (configs==null)
            calculateConfigs();
        List<ClientProjectConfiguration> l = new ArrayList<ClientProjectConfiguration>();
        l.addAll(configs.values());
        Collections.sort(l, new Comparator<ClientProjectConfiguration>() {
            Collator c = Collator.getInstance();
            public int compare(ClientProjectConfiguration c1, ClientProjectConfiguration c2) {
                return c.compare(c1.getDisplayName(), c2.getDisplayName());
            }
        });
        return l;
    }

    @Override
    public ClientProjectConfiguration getActiveConfiguration() {
        if (configs == null)
            calculateConfigs();
        String config = p.getEvaluator().getProperty(PROP_CONFIG);
        //TODO: hack for default
        if(config==null)
            config="browser";
        if (configs.containsKey(config)) {
            return configs.get(config);
        }
        return null;
    }

    public void setActiveConfiguration(ClientProjectConfiguration c) throws IllegalArgumentException, IOException {
        if ( !configs.values().contains(c)) {
            throw new IllegalArgumentException();
        }
        final String n = c.getName();
        EditableProperties ep = p.getHelper().getProperties(CONFIG_PROPS_PATH);
        if (Utilities.compareObjects(n, ep.getProperty(PROP_CONFIG))) {
            return;
        }
        if (n != null) {
            ep.setProperty(PROP_CONFIG, n);
        } else {
            ep.remove(PROP_CONFIG);
        }
        p.getHelper().putProperties(CONFIG_PROPS_PATH, ep);
        pcs.firePropertyChange(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE, null, null);
        ProjectManager.getDefault().saveProject(p);
        assert p.getProjectDirectory().getFileObject(CONFIG_PROPS_PATH) != null;
    }

    public boolean hasCustomizer() {
        return true;
    }

    public void customize() {
        p.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer();
    }

    public boolean configurationsAffectAction(String command) {
        return command.equals(ActionProvider.COMMAND_RUN)
                || command.equals(ActionProvider.COMMAND_BUILD)
                || command.equals(ActionProvider.COMMAND_CLEAN)
                || command.equals(ActionProvider.COMMAND_RUN_SINGLE);
    }

    public void addPropertyChangeListener(PropertyChangeListener lst) {
        pcs.addPropertyChangeListener(lst);
    }

    public void removePropertyChangeListener(PropertyChangeListener lst) {
        pcs.removePropertyChangeListener(lst);
    }



    @Override
    public int getSize() {
        return getConfigurations().size();
    }

    @Override
    public Object getElementAt(int index) {
        return ((ArrayList) getConfigurations()).get(index);
    }

    @Override
    public void setSelectedItem(Object anItem) {
        try {
            setActiveConfiguration((ClientProjectConfiguration) anItem);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Object getSelectedItem() {
        return getActiveConfiguration();
    }
}
