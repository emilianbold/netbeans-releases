/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.discovery.wizard.bridge;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;

/**
 *
 * @author Alexander Simon
 */
public class ProjectBridge {
    private String baseFolder;
    private MakeConfiguration extConf;
    private MakeConfigurationDescriptor makeConfigurationDescriptor;
    private Set resultSet = new HashSet();
    
    private ProjectBridge() {
    }
    
    public ProjectBridge(String baseFolder) {
        this.baseFolder = baseFolder;
        extConf = new MakeConfiguration(baseFolder, "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
        String workingDir = baseFolder;
        String workingDirRel = IpeUtils.toRelativePath(baseFolder, FilePathAdaptor.naturalize(workingDir));
        workingDirRel = FilePathAdaptor.normalize(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
    }
    
    
    public void createproject() throws IOException{
        Project project = ProjectGenerator.createBlankProject("DiscoveryProject", baseFolder, new MakeConfiguration[] {extConf}, true); // NOI18N
        resultSet.add(project);
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
    }
    
    public Folder createFolder(Folder parent, String name){
        return new Folder(makeConfigurationDescriptor, parent, name, name, true);
    }
    
    
    public Item createItem(String path){
        return new Item(getRelativepath(path));
    }
    
    public String getRelativepath(String path){
        path = IpeUtils.toRelativePath(makeConfigurationDescriptor.getBaseDir(), path);
        path = FilePathAdaptor.mapToRemote(path);
        path = FilePathAdaptor.normalize(path);
        return path;
    }
    
    
    public Folder getRoot(){
        return makeConfigurationDescriptor.getLogicalFolders();
    }
    
    public Set getResult(){
        return resultSet;
    }
    
    public void setupProject(Vector includes, String macros, boolean isCPP){
        if (isCPP) {
            extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(includes);
            extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
        } else {
            extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includes);
            extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
        }
    }
    
    public void setupFolder(Vector includes, boolean inheriteIncludes, String macros, boolean inheriteMacros, boolean isCPP, Folder folder) {
        // TODO: implement folder configuration
    }
    
    public void setupFile(String compilepath, Vector includes, boolean inheriteIncludes, String macros, boolean inheriteMacros, Item item) {
        MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
        ItemConfiguration itemConfiguration = (ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return;
        }
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration)compilerConfiguration;
            cccCompilerConfiguration.getIncludeDirectories().setValue(includes);
            cccCompilerConfiguration.getInheritIncludes().setValue(inheriteIncludes);
            cccCompilerConfiguration.getPreprocessorConfiguration().setValue(macros);
            cccCompilerConfiguration.getInheritPreprocessor().setValue(inheriteMacros);
        }
    }
}
