/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.discovery.wizard.bridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
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
    
    public void process(){
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
        projectBridge.save();
    }
    
    public Set makeProject(){
        process();
        return projectBridge.getResult();
    }
    
    private Set<String> getSourceFolders(){
        Set<String> used = new HashSet<String>();
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
                    projectBridge.setHeaderTool(item);
                }
            }
        }
        if (needAdd.size()>0) {
            projectBridge.checkForNewExtensions(needAdd);
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
        for (Map.Entry<String,Item> entry : sorted.entrySet()){
            String path = entry.getKey();
            Item item = entry.getValue();
            String canonicalPath = item.getCanonicalFile().getAbsolutePath();
            if (!(relatives.contains(path) || used.contains(path) ||
                  relatives.contains(canonicalPath) || used.contains(canonicalPath))) {
                // remove item;
                if (DEBUG) {System.out.println("Exclude Item "+path);} // NOI18N
                projectBridge.setExclude(item,true);
            }
        }
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
            List<String> vector = new ArrayList<String>(set);
            List<String> buf = buildMacrosString(macros);
            projectBridge.setupProject(vector, buf, config.getLanguageKind() == ItemProperties.LanguageKind.CPP);
        } else {
            // cleanup project configuration
            List<String> vector = Collections.<String>emptyList();
            List<String> buf = Collections.<String>emptyList();
            projectBridge.setupProject(vector, buf, config.getLanguageKind() == ItemProperties.LanguageKind.CPP);
        }
    }
    
    private List<String> buildMacrosString(final Map<String, String> map) {
        List<String> vector = new ArrayList<String>();
        for(Map.Entry<String,String> entry : map.entrySet()){
            if (entry.getValue()!=null) {
                vector.add(entry.getKey()+"="+entry.getValue()); // NOI18N
            } else {
                vector.add(entry.getKey());
            }
        }
        return vector;
    }
    
    private void setupFile(FileConfiguration config, Item item, boolean isCPP) {
        projectBridge.setSourceTool(item,isCPP);
        if ("file".equals(level)){ // NOI18N
            Set<String> set = new HashSet<String>();
            Map<String,String> macros = new HashMap<String,String>();
            reConsolidatePaths(set, config);
            macros.putAll(config.getUserMacros());
            List<String> vector = new ArrayList<String>(set);
            List<String> buf = buildMacrosString(macros);
            projectBridge.setupFile(config.getCompilePath(), vector, !config.overrideIncludes(), buf, !config.overrideMacros(), item);
        } else {
            // cleanup file configuration
            List<String> vector = Collections.<String>emptyList();
            List<String> buf = Collections.<String>emptyList();
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
            name = name.replace('\\', '/'); // NOI18N
        }
        int i = name.lastIndexOf('/'); // NOI18N
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
                        if (DEBUG) {System.err.println("Cannot find pair by path "+pair.fileConfiguration.getFilePath());} // NOI18N
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
                List<String> buf = buildMacrosString(macros);
                List<String> vector = new ArrayList<String>(inludes);
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
                List<String> buf = Collections.<String>emptyList();
                List<String> vector = Collections.<String>emptyList();
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
                    if (DEBUG) {System.err.println("Orphan pair found by path "+file);} // NOI18N
                }
                pair.item = item;
                setupFile(pair.fileConfiguration, pair.item, isCPP);
            } else {
                if (DEBUG) {System.err.println("Cannot find pair by path "+file);} // NOI18N
            }
        }
    }
    
    
    private List<Pair> detectOrphan(final Map<String, Set<Pair>> configurationStructure, boolean isCPP) {
        Map<String,Folder> preffered = prefferedFolders();
        List<Pair> orphan = new ArrayList<Pair>();
        for(Map.Entry<String,Set<Pair>> entry : configurationStructure.entrySet()){
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
                        prefferedFolder = prefferedFolder.replace('\\', '/'); // NOI18N
                    }
                    int i = prefferedFolder.lastIndexOf('/'); // NOI18N
                    if (i >= 0){
                        prefferedFolder = prefferedFolder.substring(0,i);
                        folder = preffered.get(prefferedFolder);
                    }
                    //if (folder == null) {
                        list.add(pair);
                    //}
                }
            }
            if (folder != null) {
                for(Pair pair : list){
                    String relPath = projectBridge.getRelativepath(pair.fileConfiguration.getFilePath());
                    Item item = projectBridge.getProjectItem(relPath);
                    if (item == null){
                        item = projectBridge.createItem(pair.fileConfiguration.getFilePath());
                        pair.item = item;
                        folder.addItem(item);
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
