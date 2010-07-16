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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.dlight.api.tool.impl.DLightConfigurationManagerAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class is manager for DLight Configuration
 */
public final class DLightConfigurationManager {

    static {
        DLightConfigurationManagerAccessor.setDefault(new DLightConfigurationManagerAccessorImpl());
    }
    private static DLightConfigurationManager instance = null;
    private String selectedConfigurationName = null;

    private DLightConfigurationManager() {
    }

    private final FileObject getToolsFSRoot() {
        FileObject fsRoot = FileUtil.getConfigRoot();
        return fsRoot.getFileObject(getToolsFSRootPath());
    }

    private final String getToolsFSRootPath() {
        return "DLight/Configurations"; // NOI18N
    }

    void selectConfiguration(String configurationName) {
        this.selectedConfigurationName = configurationName;
    }

    boolean canDelete(String configurationName) {
        DLightConfiguration configuration = getConfigurationByName(configurationName);
        return configuration != null && !configuration.isSystem();
    }

    boolean removeConfiguration(String configurationName) {
        FileObject configurationsFolder = getToolsFSRoot();
        if (configurationsFolder == null) {
            System.err.println("Configurations folder is NULL which should not be");//NOI18N
            return false;
        }
        FileObject[] configurations = configurationsFolder.getChildren();

        if (configurations == null || configurations.length == 0) {
            return false;
        }
        FileObject toDelete = null;
        for (FileObject configuration : configurations) {
            if (configuration.getName().equals(configurationName)) {
                try {
                    configuration.delete();
                } catch (IOException ex) {
                    return false;
                }
            }
        }
        return true;

    }

    private String commaSeparatedList(List<String> list) {
        StringBuffer res = new StringBuffer();
        for (String s : list) {
            if (res.length() > 0) {
                res.append(","); // NOI18N
            }
            res.append(s);
        }
        return res.toString();
    }

    DLightConfiguration registerConfigurationAsACopy(DLightConfiguration configuration,
            String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators) {
        FileObject configurationsFolder = getToolsFSRoot();
        FileObject configurationFolder;
        try {
            configurationFolder = configurationsFolder.createFolder(configurationName);
            configurationFolder.setAttribute("displayedName", displayedName);//NOI18N
            configurationFolder.setAttribute("category", category);//NOI18N
            configurationFolder.setAttribute("platforms", commaSeparatedList(platforms));//NOI18N
            configurationFolder.setAttribute("collector.providers", collector);//NOI18N
            configurationFolder.setAttribute("indicator.providers", commaSeparatedList(indicators));//NOI18N
            configurationFolder.createFolder(ToolsConfiguration.KNOWN_TOOLS_SET);
            FileObject rootFolder = configuration.getRootFolder();
            FileObject configurationOptionsFolder = rootFolder.getFileObject(DLightConfiguration.CONFIGURATION_OPTIONS);
            if (configurationOptionsFolder != null) {
                FileObject[] children = configurationOptionsFolder.getChildren();
                FileObject folderForCOnfigurationOptions = null;
                
                if (children != null && children.length > 0) {
                    folderForCOnfigurationOptions = configurationFolder.createFolder(configurationOptionsFolder.getName());
                    for (FileObject fo : children) {
                        if (!fo.isFolder()) {
                            FileUtil.copyFile(fo, folderForCOnfigurationOptions, fo.getName());
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return getConfigurationByName(configurationName);

        //and add new with the
    }

    DLightConfiguration registerConfiguration(String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators) {
        FileObject configurationsFolder = getToolsFSRoot();
        FileObject configurationFolder;
        try {
            configurationFolder = configurationsFolder.createFolder(configurationName);
            configurationFolder.setAttribute("displayedName", displayedName);//NOI18N
            configurationFolder.setAttribute("category", category);//NOI18N
            configurationFolder.setAttribute("platforms", commaSeparatedList(platforms));//NOI18N
            configurationFolder.setAttribute("collector.providers", collector);//NOI18N
            configurationFolder.setAttribute("indicator.providers", commaSeparatedList(indicators));//NOI18N
            configurationFolder.createFolder(ToolsConfiguration.KNOWN_TOOLS_SET);
        } catch (IOException ex) {
            return null;
        }
        return getConfigurationByName(configurationName);

        //and add new with the
    }

    /**
     * Returns DLightConfiguration which belongs to the category with the name  <code>categoryName</code>, <code>empty collection</code> otherwise
     * @param categoryName category to get the list of the configirations for
     * @return DLightConfigurations collection for category <code>categoryName</code>, <code>empty collection</code> otherwise
     */
    public Collection<DLightConfiguration> getConfigurationsByCategoryName(String categoryName) {
        List<DLightConfiguration> toolConfigurations = new ArrayList<DLightConfiguration>(getDLightConfigurations());
        Collection<DLightConfiguration> result = new ArrayList<DLightConfiguration>();
        for (DLightConfiguration conf : toolConfigurations) {
            if (categoryName != null && conf.getCategoryName() != null && conf.getCategoryName().equals(categoryName)) {
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
    public DLightConfiguration getConfigurationByName(String configurationName) {
        List<DLightConfiguration> toolConfigurations = getDLightConfigurations();
        for (DLightConfiguration conf : toolConfigurations) {
            if (conf.getConfigurationName().equals(configurationName)) {
                return conf;
            }
        }
        return null;
    }

    List<DLightConfiguration> getDLightConfigurations() {
        List<DLightConfiguration> result = new ArrayList<DLightConfiguration>();
        FileObject configurationsFolder = getToolsFSRoot();

        if (configurationsFolder == null) {
            System.err.println("Configurations folder is NULL which should not be");//NOI18N
            return result;
        }

        List<FileObject> configurations = Arrays.asList(configurationsFolder.getChildren());

        if (configurations.isEmpty()) {
            return result;
        }

        // configurations created by user do not have positions => pass false to suppress warnings
        configurations = FileUtil.getOrder(configurations, false);

        for (FileObject conf : configurations) {
            result.add(DLightConfiguration.create(conf));
        }
        return result;
    }

    DLightConfiguration getSelectedDLightConfiguration() {
        if (selectedConfigurationName != null) {
            return getConfigurationByName(selectedConfigurationName);
        }
        List<DLightConfiguration> tools = getDLightConfigurations();
        if (tools == null || tools.size() == 0) {
            return DLightConfiguration.createDefault();
        }
        return tools.get(0);
    }

    /**
     * This method returns the default configuration (all tools)
     */
    public final DLightConfiguration getDefaultConfiguration() {
        return DLightConfiguration.createDefault();
    }

    /**
     *
     * @return
     */
    public static synchronized final DLightConfigurationManager getInstance() {
        if (instance == null) {
            instance = new DLightConfigurationManager();
        }
        return instance;
    }

    final boolean registerTool(String configurationName, String toolID, boolean isOnByDefault) {
        DLightConfiguration configurationToRegister = getConfigurationByName(configurationName);
        DLightConfiguration defaultConfiguration = getDefaultConfiguration();
        ToolsConfiguration toolsConfiguration = configurationToRegister.getToolsConfiguration();
        return toolsConfiguration.register(defaultConfiguration.getToolsConfiguration().getFileObject(toolID), isOnByDefault);
    }

    final boolean registerTool(String configurationName, DLightTool tool) {
        //should find the tool by ID
        DLightConfiguration configurationToRegister = getConfigurationByName(configurationName);
        DLightConfiguration defaultConfiguration = getDefaultConfiguration();
        ToolsConfiguration toolsConfiguration = configurationToRegister.getToolsConfiguration();
        return toolsConfiguration.register(defaultConfiguration.getToolsConfiguration().getFileObject(tool.getID()), tool.isEnabled());
    }

    final boolean deleteTool(String configurationName, DLightTool tool) {
        return getConfigurationByName(configurationName).getToolsConfiguration().remove(tool.getID());
    }

    private static class DLightConfigurationManagerAccessorImpl extends DLightConfigurationManagerAccessor {

        @Override
        public DLightConfiguration getDefaultConfiguration(DLightConfigurationManager manager) {
            return manager.getDefaultConfiguration();
        }

        @Override
        public List<DLightConfiguration> getDLightConfigurations(DLightConfigurationManager manager) {
            return manager.getDLightConfigurations();
        }

        @Override
        public boolean registerTool(DLightConfigurationManager manager, String configurationName, DLightTool tool) {
            return manager.registerTool(configurationName, tool);
        }

        @Override
        public boolean deleteTool(DLightConfigurationManager manager, String configurationName, DLightTool tool) {
            return manager.deleteTool(configurationName, tool);
        }

        @Override
        public DLightConfiguration registerConfiguration(DLightConfigurationManager manager, String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators) {
            return manager.registerConfiguration(configurationName, displayedName, category, platforms, collector, indicators);
        }

        @Override
        public DLightConfiguration registerConfigurationAsACopy(DLightConfigurationManager manager, DLightConfiguration configuration,
                String configurationName, String displayedName, String category, List<String> platforms, String collector, List<String> indicators) {
            return manager.registerConfigurationAsACopy(configuration,
                    configurationName, displayedName, category, platforms, collector, indicators);
        }

        @Override
        public boolean removeConfiguration(String configurationName) {
            return DLightConfigurationManager.getInstance().removeConfiguration(configurationName);
        }

        @Override
        public boolean canRemoveConfiguration(String configurationName) {
            return DLightConfigurationManager.getInstance().canDelete(configurationName);
        }

        @Override
        public boolean registerTool(String configurationName, String toolID, boolean isOneByDefault) {
            return DLightConfigurationManager.getInstance().registerTool(configurationName, toolID, isOneByDefault);
        }
    }
}
