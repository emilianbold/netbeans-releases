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
package org.netbeans.modules.cordova.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cordova.android.AndroidPlatform;
import org.netbeans.modules.cordova.ios.IOSPlatform;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectConfigurationImplementation;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectPlatformImplementation;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 *
 */
public class ClientProjectPlatformImpl implements ClientProjectPlatformImplementation {

    private Project p;
    private Map<String, ClientProjectConfigurationImpl> configs;
    private FileObject configDir;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    private static final Logger LOGGER = Logger.getLogger(ClientProjectPlatformImpl.class.getName());
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
                LOGGER.log(Level.FINER, "Firing " + ClientProjectPlatformImplementation.PROP_CONFIGURATIONS + ": {0} -> {1}", new Object[]{oldConfigs, newConfigs});
                support.firePropertyChange(ClientProjectPlatformImplementation.PROP_CONFIGURATIONS, null, null);
            }
        }
    };
    private final FileChangeListener fclWeak;

    public ClientProjectPlatformImpl(Project p) {
        this.p = p;
        fclWeak = FileUtil.weakFileChangeListener(fcl, null);
        FileObject nbp = p.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        if (nbp != null) {
            nbp.addFileChangeListener(fclWeak);
            LOGGER.log(Level.FINEST, "Added listener to {0}", nbp);
            configDir = nbp.getFileObject("configs"); // NOI18N
            if (configDir != null) {
                configDir.addFileChangeListener(fclWeak);
                LOGGER.log(Level.FINEST, "Added listener to {0}", configDir);
            }
        }
    }

    private void calculateConfigs() {
        configs = new HashMap<String, ClientProjectConfigurationImpl>();
        if (configDir != null) {
            for (FileObject kid : configDir.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                ClientProjectConfigurationImpl conf = ClientProjectConfigurationImpl.create(p, kid);
                configs.put(conf.getId(), conf);
            }
        }
        LOGGER.log(Level.FINEST, "Calculated configurations: {0}", configs);
    }

    @Override
    public List<? extends ClientProjectConfigurationImplementation> getConfigurations() {
        if (configs == null) {
            calculateConfigs();
        }
        List<ClientProjectConfigurationImpl> l = new ArrayList<ClientProjectConfigurationImpl>(configs.values());
        return l;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener lst) {
        support.addPropertyChangeListener(lst);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener lst) {
        support.removePropertyChangeListener(lst);
    }

    @Override
    public List<String> getNewConfigurationTypes() {
        return Arrays.asList(new String[]{AndroidPlatform.TYPE, IOSPlatform.TYPE});
    }

    @Override
    public String createConfiguration(String configurationType, String configurationName) {
        EditableProperties props = new EditableProperties(true);
        props.put("type", configurationType);
        props.put("display.name", configurationName);
        FileObject conf;
        try {
            conf = ConfigUtils.createConfigFile(p.getProjectDirectory(), configurationType, props);
            ClientProjectConfigurationImpl cfg = ClientProjectConfigurationImpl.create(p, conf);
            return cfg.getId();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
