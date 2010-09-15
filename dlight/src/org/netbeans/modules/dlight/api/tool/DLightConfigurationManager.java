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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationManagerAccessor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * This class is manager for DLight Configuration
 */
public final class DLightConfigurationManager {

    private static final String ROOT = "DLight/Configurations"; // NOI18N
    private final static DLightConfigurationManager instance = new DLightConfigurationManager();
    private final List<DLightConfiguration> configurations = new ArrayList<DLightConfiguration>();
    private final FSListener fslistener = new FSListener();
    private final FileObject cfgRoot;

    static {
        DLightConfigurationManagerAccessor.setDefault(new DLightConfigurationManagerAccessorImpl());
    }

    private DLightConfigurationManager() {
        FileObject root = FileUtil.getConfigRoot().getFileObject(ROOT);

        if (root == null) {
            System.err.println("Configurations folder is NULL which should not be"); // NOI18N
            try {
                root = FileUtil.getConfigRoot().createFolder("FAKE"); // NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            cfgRoot = root;
        } else {
            cfgRoot = root;
            listenerOn();
        }
    }

    public static DLightConfigurationManager getInstance() {
        return instance;
    }

    private synchronized void refreshConfigurations() {
        configurations.clear();

        for (FileObject cfgFile : cfgRoot.getChildren()) {
            configurations.add(DLightConfiguration.create(cfgFile));
        }
    }

    boolean canDelete(String configurationName) {
        DLightConfiguration configuration = getConfigurationByName(configurationName);
        return configuration != null && !configuration.isSystem();
    }

    synchronized boolean removeConfiguration(String configurationName) {
        DLightConfiguration cfg = getConfigurationByName(configurationName);

        if (cfg == null) {
            return false;
        }

        try {
            cfg.getRootFolder().delete();
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    private String commaSeparatedList(List<String> list) {
        StringBuilder res = new StringBuilder();
        for (String s : list) {
            if (res.length() > 0) {
                res.append(',');
            }
            res.append(s);
        }
        return res.toString();
    }

    synchronized DLightConfiguration registerConfigurationAsACopy(DLightConfiguration configuration,
            String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators) {

        FileObject copy = null;

        try {
            listenerOff();
            copy = configuration.getRootFolder().copy(cfgRoot, configurationName, null);
            copy.setAttribute("displayedName", displayedName); // NOI18N
            copy.setAttribute("category", category); // NOI18N
            copy.setAttribute("platforms", commaSeparatedList(platforms)); // NOI18N
            copy.setAttribute("collector.providers", collector); // NOI18N
            copy.setAttribute("indicator.providers", commaSeparatedList(indicators)); // NOI18N
            copy.setAttribute("system", false); // NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            listenerOn();
        }

        return getConfigurationByName(configurationName);
    }

    synchronized DLightConfiguration registerConfiguration(
            String configurationName, String displayedName,
            String category, List<String> platforms,
            String collector, List<String> indicators) {
        try {
            listenerOff();

            FileObject cfg = cfgRoot.createFolder(configurationName);
            cfg.setAttribute("displayedName", displayedName); // NOI18N
            cfg.setAttribute("category", category); // NOI18N
            cfg.setAttribute("platforms", commaSeparatedList(platforms)); // NOI18N
            cfg.setAttribute("collector.providers", collector); // NOI18N
            cfg.setAttribute("indicator.providers", commaSeparatedList(indicators)); // NOI18N
            cfg.createFolder(ToolsConfiguration.KNOWN_TOOLS_SET);
        } catch (IOException ex) {
            return null;
        } finally {
            listenerOn();
        }

        return getConfigurationByName(configurationName);
    }

    /**
     * Returns DLightConfiguration which belongs to the category with the name
     * <code>categoryName</code>, <code>empty collection</code> otherwise
     * @param categoryName category to get the list of the configurations for
     * @return DLightConfigurations collection for category <code>categoryName</code>,
     * <code>empty collection</code> otherwise
     */
    public synchronized Collection<DLightConfiguration> getConfigurationsByCategoryName(String categoryName) {
        if (categoryName == null) {
            return Collections.emptyList();
        }

        Collection<DLightConfiguration> result = new ArrayList<DLightConfiguration>();
        for (DLightConfiguration conf : configurations) {
            if (categoryName.equals(conf.getCategoryName())) {
                result.add(conf);
            }
        }
        return result;
    }

    /**
     * Returns DLightConfiguration by name if exists, <code>null</code> otherwise
     * @param configurationName configuration name
     * @return DLightConfiguration by name if exists, <code>null</code> otherwise
     */
    public synchronized DLightConfiguration getConfigurationByName(String configurationName) {
        for (DLightConfiguration conf : configurations) {
            if (conf.getConfigurationName().equals(configurationName)) {
                return conf;
            }
        }
        return null;
    }

    synchronized List<DLightConfiguration> getDLightConfigurations() {
        return Collections.unmodifiableList(configurations);
    }

    /**
     * This method returns the default configuration (all tools)
     */
    public final DLightConfiguration getDefaultConfiguration() {
        return DLightConfiguration.createDefault();
    }

    final synchronized boolean registerTool(String configurationName, String toolID, boolean isOnByDefault) {
        DLightConfiguration configurationToRegister = getConfigurationByName(configurationName);
        DLightConfiguration allToolsConfiguration = getDefaultConfiguration();
        ToolsConfiguration toolsConfiguration = configurationToRegister.getToolsConfiguration();
        boolean result;
        try {
            listenerOff();
            final FileObject toolFileObject = allToolsConfiguration.getToolsConfiguration().getFileObject(toolID);
            result = toolsConfiguration.register(toolFileObject, isOnByDefault);
        } finally {
            listenerOn();
        }
        return result;
    }

    final boolean registerTool(String configurationName, DLightTool tool) {
        return registerTool(configurationName, tool.getID(), tool.isEnabled());
    }

    final boolean deleteTool(String configurationName, DLightTool tool) {
        return getConfigurationByName(configurationName).getToolsConfiguration().remove(tool.getID());
    }

    private static class DLightConfigurationManagerAccessorImpl extends DLightConfigurationManagerAccessor {

        @Override
        public DLightConfiguration getDefaultConfiguration() {
            return instance.getDefaultConfiguration();
        }

        @Override
        public List<DLightConfiguration> getDLightConfigurations() {
            return instance.getDLightConfigurations();
        }

        @Override
        public boolean registerTool(String configurationName, DLightTool tool) {
            return instance.registerTool(configurationName, tool);
        }

        @Override
        public boolean deleteTool(String configurationName, DLightTool tool) {
            return instance.deleteTool(configurationName, tool);
        }

        @Override
        public DLightConfiguration registerConfiguration(String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators) {
            return instance.registerConfiguration(configurationName, displayedName, category, platforms, collector, indicators);
        }

        @Override
        public DLightConfiguration registerConfigurationAsACopy(DLightConfiguration configuration,
                String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators) {
            return instance.registerConfigurationAsACopy(configuration,
                    configurationName, displayedName, category, platforms, collector, indicators);
        }

        @Override
        public boolean removeConfiguration(String configurationName) {
            return instance.removeConfiguration(configurationName);
        }

        @Override
        public boolean canRemoveConfiguration(String configurationName) {
            return instance.canDelete(configurationName);
        }

        @Override
        public boolean registerTool(String configurationName, String toolID, boolean isOneByDefault) {
            return instance.registerTool(configurationName, toolID, isOneByDefault);
        }
    }

    private void listenerOn() {
        cfgRoot.addRecursiveListener(fslistener);
        refreshConfigurations();
    }

    private void listenerOff() {
        cfgRoot.removeRecursiveListener(fslistener);
    }

    private class FSListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            refreshConfigurations();
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            refreshConfigurations();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            refreshConfigurations();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            refreshConfigurations();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            refreshConfigurations();
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            refreshConfigurations();
        }
    }
}
