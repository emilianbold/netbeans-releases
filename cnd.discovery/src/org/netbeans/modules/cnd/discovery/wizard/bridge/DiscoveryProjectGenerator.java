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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.Root;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.UnusedFactory;
import org.netbeans.modules.cnd.discovery.wizard.tree.ProjectConfigurationImpl;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.openide.WizardDescriptor;

/**
 *
 * @author Alexander Simon
 */
public class DiscoveryProjectGenerator {
    
    /** Creates a new instance of PrjectGenerator */
    private DiscoveryProjectGenerator() {
    }
    
    public static Set makeProject(WizardDescriptor wizard){
        Set resultSet = new HashSet();
        String baseFolder = (String)wizard.getProperty("rootFolder"); // NOI18N
        ProjectBridge projectBridge = new ProjectBridge(baseFolder);
        
        List<ProjectConfigurationImpl> projectConfigurations = (List<ProjectConfigurationImpl>)wizard.getProperty("configurations"); // NOI18N
        for (ProjectConfigurationImpl config: projectConfigurations){
            setupCompilerConfiguration(config, projectBridge);
        }
        
        try {
            projectBridge.createproject();
            Folder sourceRoot = projectBridge.getRoot();
            for (ProjectConfigurationImpl config: projectConfigurations){
                FolderConfiguration folderConfig = config.getRoot();
                addFolder(sourceRoot, folderConfig, projectBridge, config.getLanguageKind()==ItemProperties.LanguageKind.CPP);
            }
            // add other files
            addAdditional(sourceRoot, projectBridge, wizard, baseFolder);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return projectBridge.getResult();
    }
    
    private static void addAdditional(Folder folder, ProjectBridge projectBridge, WizardDescriptor wizard, String base){
        Set<String> used = new HashSet();
        List<String> list = (List<String>)wizard.getProperty("included");  // NOI18N
        for (String name : list){
            if (name.startsWith(base)){
                used.add(name);
            }
        }
        list = (List<String>) wizard.getProperty("additionalFiles"); // NOI18N
        for (String name : list){
            if (name.startsWith(base)){
                used.add(name);
            }
        }
        Root additional = UnusedFactory.createRoot(used);
        addFolder(folder, additional, projectBridge);
    }

    private static void addFolder(Folder folder, Root used, ProjectBridge projectBridge){
        String name = used.getName();
        Folder added = folder.findFolderByName(name);
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            folder.addFolder(added);
        }
        for(Root sub : used.getChildren()){
            addFolder(added, sub, projectBridge);
        }
        for(String file : used.getFiles()){
            if (added.findItemByPath(projectBridge.getRelativepath(file))==null){
                Item item = projectBridge.createItem(file);
                added.addItem(item);
            }
        }
    }
    
    private static void addFolder(Folder folder, FolderConfiguration folderConfig, ProjectBridge projectBridge, boolean isCPP){
        String name = folderConfig.getFolderName();
        Folder added = folder.findFolderByName(name);
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            folder.addFolder(added);
        }
        setupFolder(folderConfig, added, isCPP, projectBridge);
        for(FolderConfiguration sub : folderConfig.getFolders()){
            addFolder(added, sub, projectBridge, isCPP);
        }
        for(FileConfiguration file : folderConfig.getFiles()){
            Item item = projectBridge.createItem(file.getFilePath());
            added.addItem(item);
            setupFile(file, item, projectBridge);
        }
    }
    
    private static void setupCompilerConfiguration(ProjectConfigurationImpl config, ProjectBridge projectBridge){
        Vector vector = new Vector(config.getUserInludePaths(false));
        StringBuffer buf = new StringBuffer();
        for(Map.Entry<String,String> entry : config.getUserMacros(false).entrySet()){
            buf.append(entry.getKey());
            if (entry.getValue()!=null) {
                buf.append('=');
                buf.append(entry.getValue());
            }
            buf.append('\n');
        }
        projectBridge.setupProject(vector, buf.toString(), config.getLanguageKind() == ItemProperties.LanguageKind.CPP);
    }
    
    private static void setupFolder(FolderConfiguration config, Folder item, boolean isCPP, ProjectBridge projectBridge) {
        Vector vector = new Vector(config.getUserInludePaths(false));
        StringBuffer buf = new StringBuffer();
        for(Map.Entry<String,String> entry : config.getUserMacros(false).entrySet()){
            buf.append(entry.getKey());
            if (entry.getValue()!=null) {
                buf.append('=');
                buf.append(entry.getValue());
            }
            buf.append('\n');
        }
        projectBridge.setupFolder(vector, !config.overrideIncludes(),
                buf.toString(), !config.overrideMacros(), isCPP, item);
    }
    
    private static boolean isDifferentCompilePath(String name, String path){
        int i = name.lastIndexOf('/');
        if (i > 0) {
            name = name.substring(0,i);
            if (!name.equals(path)) {
                return true;
            }
        }
        return false;
    }
    
    private static void setupFile(FileConfiguration config, Item item, ProjectBridge projectBridge) {
        String compilePath = config.getCompilePath();
        Vector vector = new Vector();
        for (String path : config.getUserInludePaths(false)){
            // TODO: set relative path when project system will be support it.
            //vector.add(path);
            String name = null;
            if (path.startsWith(File.separator)) {
                name = path;
            } else {
                name = compilePath+File.separator+path;
            }
            vector.add(projectBridge.getRelativepath(name));
        }
        // TODO: remove it when project system will be support compile path.
        if (isDifferentCompilePath(config.getFilePath(),compilePath)){
            vector.add(projectBridge.getRelativepath(compilePath));
        }
        StringBuffer buf = new StringBuffer();
        for(Map.Entry<String,String> entry : config.getUserMacros(false).entrySet()){
            buf.append(entry.getKey());
            if (entry.getValue()!=null) {
                buf.append('=');
                buf.append(entry.getValue());
            }
            buf.append('\n');
        }
        projectBridge.setupFile(compilePath, vector, !config.overrideIncludes(), buf.toString(), !config.overrideMacros(), item);
    }
}
