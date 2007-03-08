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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.FolderConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.AbstractRoot;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.UnusedFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class DiscoveryProjectGenerator {
    private static boolean DEBUG = Boolean.getBoolean("cnd.discovery.trace.project_update"); // NOI18N
    private static boolean KEEP_UNUSED = Boolean.getBoolean("cnd.discovery.keep_unused"); // NOI18N
    private ProjectBridge projectBridge;
    private DiscoveryDescriptor wizard;
    private String baseFolder;
    private String level;
    
    /** Creates a new instance of PrjectGenerator */
    public DiscoveryProjectGenerator(DiscoveryDescriptor wizard) throws IOException {
        this.wizard = wizard;
        baseFolder = wizard.getRootFolder();
        Project project = wizard.getProject();
        if (project != null) {
            //baseFolder = File.separator+project.getProjectDirectory().getPath();
            projectBridge = new ProjectBridge(project);
        } else {
            projectBridge = new ProjectBridge(baseFolder);
        }
    }
    
    public Set makeProject(){
        List<ProjectConfiguration> projectConfigurations = wizard.getConfigurations();
        Folder sourceRoot = projectBridge.getRoot();
        level = wizard.getLevel();
        for (ProjectConfiguration config: projectConfigurations){
            setupCompilerConfiguration(config);
            FolderConfiguration folderConfig = config.getRoot();
            addFolder(sourceRoot, folderConfig, config.getLanguageKind()==ItemProperties.LanguageKind.CPP, true);
        }
        // add other files
        addAdditional(sourceRoot, baseFolder);
        return projectBridge.getResult();
    }
    
    private void addAdditional(Folder folder, String base){
        Set<String> used = new HashSet<String>();
        List<String> list = wizard.getIncludedFiles();
        for (String name : list){
            String path = name;
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (path.startsWith(base)){
                used.add(name);
            }
        }
        AbstractRoot additional = UnusedFactory.createRoot(used);
        if (used.size()>0) {
            addFolder(folder, additional, true);
        }
        // remove unused
        List<ProjectConfiguration> projectConfigurations = wizard.getConfigurations();
        for (ProjectConfiguration conf : projectConfigurations) {
            for (FileConfiguration file : conf.getFiles()){
                used.add(file.getFilePath());
            }
        }
        Set<String> relatives = new HashSet<String>();
        for (String name : used){
            relatives.add(projectBridge.getRelativepath(name));
        }
        TreeMap<String,Item> sorted = new TreeMap<String,Item>();
        for (Item item : projectBridge.getAllSources()){
            sorted.put(item.getPath(),item);
        }
        Map<String,Item> unused = new HashMap<String,Item>();
        for (Map.Entry<String,Item> entry : sorted.entrySet()){
            String path = entry.getKey();
            Item item = entry.getValue();
            if (!relatives.contains(path)) {
                // remove item;
                Folder parent = item.getFolder();
                if (DEBUG) System.out.println("Exclude Item "+path); // NOI18N
                projectBridge.setExclude(item,true);
            }
        }
    }
    
    private void addFolder(Folder folder, AbstractRoot used, boolean first){
        String name = used.getName();
        Folder added = null;
        if (first && folder.getName().equals(name)) {
            added = folder;
        } else {
            added = folder.findFolderByName(name);
        }
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            folder.addFolder(added);
        }
        for(AbstractRoot sub : used.getChildren()){
            addFolder(added, sub, false);
        }
        List<String> files = used.getFiles();
        if (files != null) {
            for(String file : files){
                String path = projectBridge.getRelativepath(file);
                Item item = added.findItemByPath(path);
                if (item==null){
                    Item itemInAnotheFolder = projectBridge.getProjectItem(path);
                    Object old = null;
                    if (itemInAnotheFolder != null) {
                        // TODO: What we should do? May remove item from folder and create in current folder?
                        old = projectBridge.getAuxObject(itemInAnotheFolder);
                        itemInAnotheFolder.getFolder().removeItem(itemInAnotheFolder);
                        item = itemInAnotheFolder;
                    } else {
                        item = projectBridge.createItem(file);
                    }
                    item = added.addItem(item);
                    if (old != null) {
                        projectBridge.setAuxObject(item, old);
                    }
                    projectBridge.setExclude(item,false);
                }
            }
        }
    }
    
    private void addFolder(Folder folder, FolderConfiguration folderConfig, boolean isCPP, boolean first){
        String name = folderConfig.getFolderName();
        Folder added = null;
        if (first && folder.getName().equals(name)) {
            added = folder;
        } else {
            added = folder.findFolderByName(name);
        }
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            folder.addFolder(added);
        }
        setupFolder(folderConfig, added, isCPP);
        for(FolderConfiguration sub : folderConfig.getFolders()){
            addFolder(added, sub, isCPP, false);
        }
        for(FileConfiguration file : folderConfig.getFiles()){
            String path = projectBridge.getRelativepath(file.getFilePath());
            Item item = added.findItemByPath(path);
            if (item == null){
                Item itemInAnotheFolder = projectBridge.getProjectItem(path);
                Object old = null;
                if (itemInAnotheFolder != null) {
                    // TODO: What we should do? May remove item from folder and create in current folder?
                    old = projectBridge.getAuxObject(itemInAnotheFolder);
                    itemInAnotheFolder.getFolder().removeItem(itemInAnotheFolder);
                    item = itemInAnotheFolder;
                } else {
                    item = projectBridge.createItem(file.getFilePath());
                }
                added.addItem(item);
                if (old != null) {
                    projectBridge.setAuxObject(item, old);
                }
            }
            setupFile(file, item);
        }
    }
    
    private void setupCompilerConfiguration(ProjectConfiguration config){
        // TODO: set relative path when project system will be support it.
        //Vector vector = new Vector(config.getUserInludePaths(false));
        Set<String> set = new HashSet<String>();
        if ("project".equals(level)){ // NOI18N
            for(FileConfiguration file : config.getFiles()){
                reConsolidate(set, file);
            }
        }
        Vector vector = new Vector(set);
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
    
    private void setupFolder(FolderConfiguration config, Folder item, boolean isCPP) {
        //Vector vector = new Vector(config.getUserInludePaths(false));
        Set<String> set = new HashSet<String>();
        if ("folder".equals(level)){ // NOI18N
            for(FileConfiguration file : config.getFiles()){
                reConsolidate(set, file);
            }
        }
        Vector vector = new Vector(set);
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
    
    private void setupFile(FileConfiguration config, Item item) {
        Set<String> set = new HashSet<String>();
        reConsolidate(set, config);
        Vector vector = new Vector(set);
        StringBuffer buf = new StringBuffer();
        for(Map.Entry<String,String> entry : config.getUserMacros(false).entrySet()){
            buf.append(entry.getKey());
            if (entry.getValue()!=null) {
                buf.append('=');
                buf.append(entry.getValue());
            }
            buf.append('\n');
        }
        projectBridge.setupFile(config.getCompilePath(), vector, !config.overrideIncludes(), buf.toString(), !config.overrideMacros(), item);
    }

    private void reConsolidate(Set set, FileConfiguration file){
        String compilePath = file.getCompilePath();
        for (String path : file.getUserInludePaths()){
            if ( !( path.startsWith("/") || (path.length()>1 && path.charAt(1)==':') ) ) { // NOI18N
                if (path.equals(".")) { // NOI18N
                    path = compilePath;
                } else {
                    path = compilePath+File.separator+path;
                }
                File f = new File(path);
                path = FileUtil.normalizeFile(f).getAbsolutePath();
            }
            set.add(projectBridge.getRelativepath(path));
        }
        if (isDifferentCompilePath(file.getFilePath(),compilePath)){
            set.add(projectBridge.getRelativepath(compilePath));
        }
    }
    
    private boolean isDifferentCompilePath(String name, String path){
        if (Utilities.isWindows()) {
            name = name.replace('\\', '/');
        }
        int i = name.lastIndexOf('/');
        if (i > 0) {
            name = name.substring(0,i);
            if (!name.equals(path)) {
                return true;
            }
        }
        return false;
    }
}
