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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard.bridge;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.AbstractRoot;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.UnusedFactory;
import org.netbeans.modules.cnd.discovery.wizard.tree.FileSystemFactory;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class DiscoveryProjectGenerator {
    private static boolean DEBUG = Boolean.getBoolean("cnd.discovery.trace.project_update"); // NOI18N
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
            projectBridge = new ProjectBridge(project);
        } else {
            projectBridge = new ProjectBridge(baseFolder);
        }
    }
    
    public Set makeProject(){
        List<ProjectConfiguration> projectConfigurations = wizard.getConfigurations();
        Folder sourceRoot = projectBridge.getRoot();
        level = wizard.getLevel();
        Set<Item> used = new HashSet<Item>();
        for (ProjectConfiguration config: projectConfigurations){
            setupCompilerConfiguration(config);
            addConfiguration(sourceRoot, config, used);
        }
        // add other files
        addAdditional(sourceRoot, baseFolder, used);
        return projectBridge.getResult();
    }
    
    private Set<String> getSourceFolders(){
        Set<String> used = new HashSet<String>();
        Set<String> folders = new HashSet<String>();
        List<ProjectConfiguration> projectConfigurations = wizard.getConfigurations();
        for (ProjectConfiguration conf : projectConfigurations) {
            for (FileConfiguration file : conf.getFiles()){
                String path = file.getFilePath();
                if (Utilities.isWindows()) {
                    path = path.replace('\\', '/');
                }
                int i = path.lastIndexOf('/');
                if (i > 0) {
                    path = path.substring(0,i+1);
                }
                used.add(path);
            }
        }
        return used;
    }
    
    private Map<String,Folder> prefferedFolders(){
        Map<String,Folder> folders = new HashMap<String,Folder>();
        for(Item item : projectBridge.getAllSources()){
            String path = item.getAbsPath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            if (path.indexOf("/../")>=0 || path.indexOf("/./")>=0) { // NOI18N
                path = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
            }
            int i = path.lastIndexOf('/');
            if (i >= 0){
                String folder = path.substring(0,i);
                folders.put(folder,item.getFolder());
            }
        }
        return folders;
    }
    
    private void addAdditional(Folder folder, String base, Set<Item> usedItems){
        Set<String> folders = getSourceFolders();
        Set<String> used = new HashSet<String>();
        Set<String> needAdd = new HashSet<String>();
        List<String> list = wizard.getIncludedFiles();
        Map<String,Folder> preffered = prefferedFolders();
        for (String name : list){
            used.add(name);
            String path = projectBridge.getRelativepath(name);
            Item item = projectBridge.getProjectItem(path);
            if (item == null){
                path = name;
                if (Utilities.isWindows()) {
                    path = path.replace('\\', '/');
                }
                boolean isNeedAdd = false;
                if (path.startsWith(base)){
                    isNeedAdd = true;
                } else {
                    for(String dir : folders){
                        if (path.startsWith(dir)){
                            isNeedAdd = true;
                            break;
                        }
                    }
                }
                if (isNeedAdd){
                    int i = path.lastIndexOf('/');
                    if (i >= 0){
                        String folderPath = path.substring(0,i);
                        Folder prefferedFolder = preffered.get(folderPath);
                        if (prefferedFolder != null) {
                            item = projectBridge.createItem(name);
                            item = prefferedFolder.addItem(item);
                            isNeedAdd = false;
                        }
                    }
                }
                if (isNeedAdd){
                    needAdd.add(name);
                }
            } else {
                if (!usedItems.contains(item)) {
                    projectBridge.setExclude(item,false);
                }
            }
        }
        if (needAdd.size()>0) {
            addNewExtension(needAdd);
            AbstractRoot additional = UnusedFactory.createRoot(needAdd);
            addAdditionalFolder(folder, additional);
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
            if (!usedItems.contains(item)) {
                sorted.put(item.getPath(),item);
            }
        }
        Map<String,Item> unused = new HashMap<String,Item>();
        for (Map.Entry<String,Item> entry : sorted.entrySet()){
            String path = entry.getKey();
            Item item = entry.getValue();
            String canonicalPath = item.getCanonicalFile().getAbsolutePath();
            if (!(relatives.contains(path) || used.contains(path) ||
                  relatives.contains(canonicalPath) || used.contains(canonicalPath))) {
                // remove item;
                Folder parent = item.getFolder();
                if (DEBUG) System.out.println("Exclude Item "+path); // NOI18N
                projectBridge.setExclude(item,true);
            }
        }
    }

    private void addNewExtension(Set<String> needAdd){
        Set<String> headerExtension = FileSystemFactory.getHeaderSuffixes();
        Set<String> sourceExtension = FileSystemFactory.getSourceSuffixes();
        Set<String> usedExtension = FileSystemFactory.createExtensionSet();
        for(String name : needAdd){
            name = name.replace('\\','/');
            int i = name.lastIndexOf('/');
            if (i >= 0){
                name = name.substring(i);
            }
            i = name.lastIndexOf('.');
            if (i > 0){
                String extension = name.substring(i+1);
                if (extension.length()>0) {
                    if (!headerExtension.contains(extension) && !sourceExtension.contains(extension)){
                        usedExtension.add(extension);
                    }
                }
            }
        }
        if (usedExtension.size()>0 && addNewExtensionDialog(usedExtension)){
            // add unknown extensin to HDataLoader
            HDataLoader.getInstance().addExtensions(usedExtension);
        }
    }
    
    private boolean addNewExtensionDialog(Set<String> usedExtension) {
        String message = getString("ADD_EXTENSION_QUESTION"+(usedExtension.size()==1?"":"S")); // NOI18N
        StringBuilder extensions = new StringBuilder();
        for(String ext : usedExtension){
            if (extensions.length()>0){
                extensions.append(',');
            }
            extensions.append(ext);
        }
        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                MessageFormat.format(message, new Object[]{extensions.toString()}),
                getString("ADD_EXTENSION_DIALOG_TITLE"+(usedExtension.size()==1?"":"S")), // NOI18N
                NotifyDescriptor.YES_NO_OPTION); 
        return DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION;
    }

    private String getString(String key) {
        return NbBundle.getBundle(DiscoveryProjectGenerator.class).getString(key);
    }
    
    private void addAdditionalFolder(Folder folder, AbstractRoot used){
        String name = used.getName();
        Folder added = folder.findFolderByName(name);
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            folder.addFolder(added);
        }
        for(AbstractRoot sub : used.getChildren()){
            addAdditionalFolder(added, sub);
        }
        List<String> files = used.getFiles();
        if (files != null) {
            for(String file : files){
                String path = projectBridge.getRelativepath(file);
                Item item =  projectBridge.getProjectItem(path);
                if (item!=null) {
                    if (item.getFolder() != added){
                        Object old = projectBridge.getAuxObject(item);
                        item.getFolder().removeItem(item);
                        item = added.addItem(item);
                        if (old != null) {
                            projectBridge.setAuxObject(item, old);
                        }
                    }
                    projectBridge.setExclude(item,false);
                    projectBridge.setHeaderTool(item);
                } else {
                    item = projectBridge.createItem(file);
                    item = added.addItem(item);
                    projectBridge.setExclude(item,false);
                    projectBridge.setHeaderTool(item);
                }
            }
        }
    }
    
    private void setupCompilerConfiguration(ProjectConfiguration config){
        if ("project".equals(level)){ // NOI18N
            Set<String> set = new HashSet<String>();
            Map<String,String> macros = new HashMap<String,String>();
            for(FileConfiguration file : config.getFiles()){
                reConsolidatePaths(set, file);
                macros.putAll(file.getUserMacros());
            }
            Vector<String> vector = new Vector<String>(set);
            String buf = buildMacrosString(macros);
            projectBridge.setupProject(vector, buf, config.getLanguageKind() == ItemProperties.LanguageKind.CPP);
        } else {
            // cleanup project configuration
            Vector<String> vector = new Vector<String>();
            String buf = "";// NOI18N
            projectBridge.setupProject(vector, buf, config.getLanguageKind() == ItemProperties.LanguageKind.CPP);
        }
    }
    
    private String buildMacrosString(final Map<String, String> map) {
        StringBuilder buf = new StringBuilder();
        for(Map.Entry<String,String> entry : map.entrySet()){
            buf.append(entry.getKey());
            if (entry.getValue()!=null) {
                buf.append('=');
                buf.append(entry.getValue());
            }
            buf.append('\n');
        }
        return buf.toString();
    }
    
    private void setupFile(FileConfiguration config, Item item, boolean isCPP) {
        projectBridge.setSourceTool(item,isCPP);
        if ("file".equals(level)){ // NOI18N
            Set<String> set = new HashSet<String>();
            Map<String,String> macros = new HashMap<String,String>();
            reConsolidatePaths(set, config);
            macros.putAll(config.getUserMacros());
            Vector<String> vector = new Vector<String>(set);
            String buf = buildMacrosString(macros);
            projectBridge.setupFile(config.getCompilePath(), vector, !config.overrideIncludes(), buf, !config.overrideMacros(), item);
        } else {
            // cleanup file configuration
            Vector<String> vector = new Vector<String>();
            String buf = "";// NOI18N
            projectBridge.setupFile(config.getCompilePath(), vector, true, buf, true, item);
        }
    }
    
    private void reConsolidatePaths(Set<String> set, FileConfiguration file){
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
    
    private void addConfiguration(Folder sourceRoot, ProjectConfiguration conf, Set<Item> used){
        boolean isCPP =conf.getLanguageKind()==ItemProperties.LanguageKind.CPP;
        Map<String,Set<Pair>> configurationStructure = analyzeConfigurationStructure(conf.getFiles(), isCPP);
        List<Pair> orphan = detectOrphan(configurationStructure, isCPP);
        if (orphan.size() > 0) {
            createOrphan(sourceRoot, orphan, isCPP);
        }
        if ("folder".equals(level)){ // NOI18N
            // reconsolidate folders;
            Map<Folder,Set<FileConfiguration>> folders = new HashMap<Folder,Set<FileConfiguration>>();
            for(Map.Entry<String,Set<Pair>> entry : configurationStructure.entrySet()){
                String path = entry.getKey();
                Set<Pair> files = entry.getValue();
                for(Pair pair : files){
                    if (pair.item != null) {
                        Folder folder = pair.item.getFolder();
                        Set<FileConfiguration> content = folders.get(folder);
                        if (content == null){
                            content = new HashSet<FileConfiguration>();
                            folders.put(folder,content);
                        }
                        content.add(pair.fileConfiguration);
                    } else {
                        if (DEBUG) System.err.println("Cannot find pair by path "+pair.fileConfiguration.getFilePath());
                    }
                }
            }
            for(Map.Entry<Folder,Set<FileConfiguration>> entry : folders.entrySet()){
                Folder folder = entry.getKey();
                Set<FileConfiguration> confs = entry.getValue();
                Set<String> inludes = new HashSet<String>();
                Map<String,String> macros = new HashMap<String,String>();
                for(FileConfiguration file : confs){
                    reConsolidatePaths(inludes, file);
                    macros.putAll(file.getUserMacros());
                }
                String buf = buildMacrosString(macros);
                Vector<String> vector = new Vector<String>(inludes);
                projectBridge.setupFolder(vector, false,
                        buf, false, conf.getLanguageKind()==ItemProperties.LanguageKind.CPP, folder);
            }
        } else {
            // cleanup folder configurations
            Set<Folder> folders = new HashSet<Folder>();
            for(Map.Entry<String,Set<Pair>> entry : configurationStructure.entrySet()){
                Set<Pair> files = entry.getValue();
                for(Pair pair : files){
                    if (pair.item != null) {
                        Folder folder = pair.item.getFolder();
                        folders.add(folder);
                    }
                }
            }
            for(Folder folder : folders){
                String buf = ""; // NOI18N
                Vector<String> vector = new Vector<String>();
                projectBridge.setupFolder(vector, true,
                        buf, true, conf.getLanguageKind()==ItemProperties.LanguageKind.CPP, folder);
            }
        }
        for(Set<Pair> set : configurationStructure.values()){
            for(Pair pair : set){
                if (pair.item != null){
                    used.add(pair.item);
                }
            }
        }
    }
    
    private void createOrphan(Folder sourceRoot, List<Pair> orphan, boolean isCPP){
        Map<String,Pair> folders = new HashMap<String,Pair>();
        for(Pair pair : orphan){
            String path = pair.fileConfiguration.getFilePath();
            folders.put(path,pair);
        }
        AbstractRoot additional = UnusedFactory.createRoot(folders.keySet());
        addFolder(sourceRoot, additional, folders, isCPP);
    }
    
    private void addFolder(Folder folder, AbstractRoot additional, Map<String,Pair> folders, boolean isCPP){
        String name = additional.getName();
        Folder added = folder.findFolderByName(name);
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            folder.addFolder(added);
        }
        for(AbstractRoot sub : additional.getChildren()){
            addFolder(added, sub, folders, isCPP);
        }
        for(String file : additional.getFiles()){
            Pair pair = folders.get(file);
            if (pair != null) {
                String path = projectBridge.getRelativepath(file);
                Item item = projectBridge.getProjectItem(path);
                if (item == null){
                    item = projectBridge.createItem(file);
                    added.addItem(item);
                } else {
                    if (DEBUG) System.err.println("Orphan pair found by path "+file);
                }
                pair.item = item;
                setupFile(pair.fileConfiguration, pair.item, isCPP);
            } else {
                if (DEBUG) System.err.println("Cannot find pair by path "+file);
            }
        }
    }
    
    
    private List<Pair> detectOrphan(final Map<String, Set<Pair>> configurationStructure, boolean isCPP) {
        Map<String,Folder> preffered = prefferedFolders();
        List<Pair> orphan = new ArrayList<Pair>();
        for(Map.Entry<String,Set<Pair>> entry : configurationStructure.entrySet()){
            String path = entry.getKey();
            Set<Pair> files = entry.getValue();
            Folder folder = null;
            List<Pair> list = new ArrayList<Pair>();
            for(Pair pair : files){
                Item item = pair.item;
                if (item != null){
                    if (folder != null) {
                        folder = item.getFolder();
                    }
                } else {
                    String prefferedFolder = pair.fileConfiguration.getFilePath();
                    if (Utilities.isWindows()) {
                        prefferedFolder = prefferedFolder.replace('\\', '/');
                    }
                    int i = prefferedFolder.lastIndexOf('/');
                    if (i >= 0){
                        prefferedFolder = prefferedFolder.substring(0,i);
                        folder = preffered.get(prefferedFolder);
                    }
                    if (folder == null) {
                        list.add(pair);
                    }
                }
            }
            if (folder != null) {
                for(Pair pair : list){
                    String relPath = projectBridge.getRelativepath(pair.fileConfiguration.getFilePath());
                    Item item = projectBridge.getProjectItem(path);
                    if (item == null){
                        item = projectBridge.createItem(pair.fileConfiguration.getFilePath());
                        folder.addItem(item);
                        pair.item = item;
                    }
                    setupFile(pair.fileConfiguration, item, isCPP);
                }
            } else {
                for(Pair pair : list){
                    orphan.add(pair);
                }
            }
        }
        return orphan;
    }
    
    private Map<String,Set<Pair>> analyzeConfigurationStructure(List<FileConfiguration> files, boolean isCPP){
        Map<String,Set<Pair>> folders = new HashMap<String,Set<Pair>>();
        for (FileConfiguration file : files){
            String path = file.getFilePath();
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
            int i = path.lastIndexOf('/');
            if (i >= 0){
                String folder = path.substring(0,i);
                Set<Pair> set = folders.get(folder);
                if (set == null){
                    set = new HashSet<Pair>();
                    folders.put(folder,set);
                }
                String relPath = projectBridge.getRelativepath(path);
                Item item = projectBridge.getProjectItem(relPath);
                if (item != null) {
                    setupFile(file,item, isCPP);
                }
                set.add(new Pair(file,item));
            }
        }
        return folders;
    }
    
    private static class Pair{
        private FileConfiguration fileConfiguration;
        private Item item;
        private Pair(FileConfiguration fileConfiguration, Item item){
            this.fileConfiguration = fileConfiguration;
            this.item = item;
        }
    }
    
}
