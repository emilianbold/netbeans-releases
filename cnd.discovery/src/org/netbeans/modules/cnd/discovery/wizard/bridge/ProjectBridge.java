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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ProjectBridge {
    private String baseFolder;
    private MakeConfigurationDescriptor makeConfigurationDescriptor;
    private Project project;
    private Set<Project> resultSet = new HashSet<Project>();
    private Map<String,Item> canonicalItems;
    
    public ProjectBridge(Project project) {
        this.project = project;
        baseFolder = File.separator+project.getProjectDirectory().getPath();
        resultSet.add(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
    }
    
    public ProjectBridge(String baseFolder) throws IOException{
        this.baseFolder = baseFolder;
        MakeConfiguration extConf = new MakeConfiguration(baseFolder, "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
        String workingDir = baseFolder;
        String workingDirRel = IpeUtils.toRelativePath(baseFolder, FilePathAdaptor.naturalize(workingDir));
        workingDirRel = FilePathAdaptor.normalize(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        project = ProjectGenerator.createBlankProject("DiscoveryProject", baseFolder, new MakeConfiguration[] {extConf}, true); // NOI18N
        resultSet.add(project);
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        makeConfigurationDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
    }
    
    public Folder createFolder(Folder parent, String name){
        return new Folder(makeConfigurationDescriptor, parent, name, name, true);
    }
    
    
    /**
     * Create new item. Path is converted to relative.
     */
    public Item createItem(String path){
        return new Item(getRelativepath(path));
    }
    
    /**
     * Find project item by relative path.
     */
    public Item getProjectItem(String path){
        Item item = makeConfigurationDescriptor.findProjectItemByPath(path);
        if (item == null){
            if (!IpeUtils.isPathAbsolute(path)) {
                path = IpeUtils.toAbsolutePath(baseFolder, path);
            }
            item = findByCanonicalName(path);
        }
        return item;
    }
    
    private Item findByCanonicalName(String path){
        if (canonicalItems == null) {
            canonicalItems = new HashMap<String,Item>();
            for(Item item : makeConfigurationDescriptor.getProjectItems()){
                canonicalItems.put(item.getCanonicalFile().getAbsolutePath(),item);
            }
        }
        return canonicalItems.get(path);
    }
    
    public Object getAuxObject(Item item){
        MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        return itemConfiguration;
    }
    
    public void setAuxObject(Item item, Object pao){
        if (pao instanceof ItemConfiguration) {
            ItemConfiguration conf = (ItemConfiguration)pao;
            MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
            itemConfiguration.setCCCompilerConfiguration(conf.getCCCompilerConfiguration());
            itemConfiguration.setCCompilerConfiguration(conf.getCCompilerConfiguration());
            itemConfiguration.setCustomToolConfiguration(conf.getCustomToolConfiguration());
        }
    }
    
    /**
     * Convert absolute path to relative.
     * Converter does some simplifications:
     * /some/../ => /
     * /./ => /
     */
    public String getRelativepath(String path){
        if (Utilities.isWindows()) {
            path = path.replace('/', File.separatorChar);
        }
        path = IpeUtils.toRelativePath(makeConfigurationDescriptor.getBaseDir(), path);
        path = FilePathAdaptor.mapToRemote(path);
        path = cutLocalRelative(path);
        path = FilePathAdaptor.normalize(path);
        return path;
    }
    
    private static final String PATTERN_1 = File.separator+"."+File.separator; // NOI18N
    private static final String PATTERN_2 = File.separator+"."; // NOI18N
    private static final String PATTERN_3 = File.separator+".."+File.separator; // NOI18N
    private static final String PATTERN_4 = File.separator+".."; // NOI18N
    private String cutLocalRelative(String path){
        String pattern = PATTERN_1;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            path = path.substring(0,i+1)+path.substring(i+pattern.length());
        }
        pattern = PATTERN_2;
        if (path.endsWith(pattern)){
            path = path.substring(0,path.length()-pattern.length());
        }
        pattern = PATTERN_3;
        while(true) {
            int i = path.indexOf(pattern);
            if (i < 0){
                break;
            }
            int k = -1;
            for (int j = i-1; j >= 0; j-- ){
                if ( path.charAt(j)==File.separatorChar){
                    k = j;
                    break;
                }
            }
            if (k<0) {
                break;
            }
            path = path.substring(0,k+1)+path.substring(i+pattern.length());
        }
        pattern = PATTERN_4;
        if (path.endsWith(pattern)){
            int k = -1;
            for (int j = path.length()-pattern.length()-1; j >= 0; j-- ){
                if ( path.charAt(j)==File.separatorChar){
                    k = j;
                    break;
                }
            }
            if (k>0) {
                path = path.substring(0,k);
            }
        }
        return path;
    }
    
    public Item[] getAllSources(){
        return makeConfigurationDescriptor.getProjectItems();
    }
    
    public Folder getRoot(){
        Folder folder = makeConfigurationDescriptor.getLogicalFolders();
        Vector sources = folder.getFolders();
        List<Folder> roots = new ArrayList<Folder>();
        for (Object o : sources){
            Folder sub = (Folder)o;
            if (sub.isProjectFiles()) {
                if (MakeConfigurationDescriptor.SOURCE_FILES_FOLDER.equals(sub.getName())) {
                    Vector v = sub.getFolders();
                    for (Object e : v){
                        Folder s = (Folder)e;
                        if (s.isProjectFiles()) {
                            roots.add(s);
                        }
                    }
                }
            }
        }
        if (roots.size()>0){
            return roots.get(0);
        }
        return folder;
    }
    
    public Set getResult(){
        makeConfigurationDescriptor.save();
        if (SwingUtilities.isEventDispatchThread()) {
            makeConfigurationDescriptor.checkForChangedItems(project, null, null);
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    makeConfigurationDescriptor.checkForChangedItems(project, null, null);
                }
            });
        }
        return resultSet;
    }
    
    public void setupProject(Vector includes, String macros, boolean isCPP){
        Configuration c = makeConfigurationDescriptor.getConfs().getActive();
        if (c instanceof MakeConfiguration) {
            MakeConfiguration extConf = (MakeConfiguration)c;
            if (isCPP) {
                extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(includes);
                extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
                extConf.getCCCompilerConfiguration().getIncludeDirectories().setDirty(true);
                extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().setDirty(true);
            } else {
                extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includes);
                extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setValue(macros);
                extConf.getCCompilerConfiguration().getIncludeDirectories().setDirty(true);
                extConf.getCCompilerConfiguration().getPreprocessorConfiguration().setDirty(true);
            }
        }
        makeConfigurationDescriptor.setModified();
    }
    
    public void setupFolder(Vector includes, boolean inheriteIncludes, String macros, boolean inheriteMacros, boolean isCPP, Folder folder) {
        MakeConfiguration makeConfiguration = (MakeConfiguration)folder.getConfigurationDescriptor().getConfs().getActive();
        //FolderConfiguration folderConfiguration = (FolderConfiguration)makeConfiguration.getAuxObject(folder.getId());
        FolderConfiguration folderConfiguration = folder.getFolderConfiguration(makeConfiguration);
        if (folderConfiguration == null) {
            return;
        }
        if (isCPP) {
            CCCompilerConfiguration ccCompilerConfiguration = folderConfiguration.getCCCompilerConfiguration();
            if (ccCompilerConfiguration != null) {
                ccCompilerConfiguration.getIncludeDirectories().setValue(includes);
                ccCompilerConfiguration.getInheritIncludes().setValue(inheriteIncludes);
                ccCompilerConfiguration.getPreprocessorConfiguration().setValue(macros);
                ccCompilerConfiguration.getInheritPreprocessor().setValue(inheriteMacros);
            }
        } else {
            CCompilerConfiguration cCompilerConfiguration = folderConfiguration.getCCompilerConfiguration();
            if (cCompilerConfiguration != null) {
                cCompilerConfiguration.getIncludeDirectories().setValue(includes);
                cCompilerConfiguration.getInheritIncludes().setValue(inheriteIncludes);
                cCompilerConfiguration.getPreprocessorConfiguration().setValue(macros);
                cCompilerConfiguration.getInheritPreprocessor().setValue(inheriteMacros);
            }
        }
    }
    
    public void setExclude(Item item, boolean exclude){
        MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return;
        }
        BooleanConfiguration excl =itemConfiguration.getExcluded();
        if (excl.getValue() ^ exclude){
            excl.setValue(exclude);
        }
        //itemConfiguration.setTool(Tool.CustomTool);
    }
    
    public void setHeaderTool(Item item){
        MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return;
        }
        if (itemConfiguration.getTool() == Tool.CCCompiler || itemConfiguration.getTool() == Tool.CCompiler) {
            itemConfiguration.setTool(Tool.CustomTool);
        }
    }

    public void setSourceTool(Item item, boolean isCPP){
        MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null) {
            return;
        }
        if (isCPP) {
            if (itemConfiguration.getTool() != Tool.CCCompiler) {
                itemConfiguration.setTool(Tool.CCCompiler);
            }
        } else {
            if (itemConfiguration.getTool() != Tool.CCompiler) {
                itemConfiguration.setTool(Tool.CCompiler);
            }
        }
    }
    
    public void setupFile(String compilepath, Vector includes, boolean inheriteIncludes, String macros, boolean inheriteMacros, Item item) {
        MakeConfiguration makeConfiguration = (MakeConfiguration)item.getFolder().getConfigurationDescriptor().getConfs().getActive();
        ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return;
        }
        BooleanConfiguration excl =itemConfiguration.getExcluded();
        if (excl.getValue()){
            excl.setValue(false);
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
    
    private List<String> systemIncludePathsC;
    private List<String> systemIncludePathsCpp;
    public List<String> getSystemIncludePaths(boolean isCPP) {
        List<String> systemIncludePaths;
        if (isCPP) {
            systemIncludePaths = systemIncludePathsCpp;
        } else {
            systemIncludePaths = systemIncludePathsC;
        }
        if (systemIncludePaths == null) {
            systemIncludePaths = new ArrayList<String>();
            MakeConfiguration makeConfiguration = (MakeConfiguration)makeConfigurationDescriptor.getConfs().getActive();
            Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
            CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(makeConfiguration.getCompilerSet().getValue());
            BasicCompiler compiler;
            if (isCPP) {
                compiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
            } else {
                compiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);
            }
            for(Object o :compiler.getSystemIncludeDirectories(platform)){
                String path = (String)o;
                systemIncludePaths.add(fixWindowsPath(path));
            }
            if (isCPP) {
                systemIncludePathsCpp = systemIncludePaths;
            } else {
                systemIncludePathsC = systemIncludePaths;
            }
        }
        return systemIncludePaths;
    }

    private static final String CYG_DRIVE_UNIX = "/cygdrive/"; // NOI18N
    private String fixWindowsPath(String path){
        if (Utilities.isWindows()) {
            // use unix style path 
            path = path.replace('\\', '/');
            // fix /cygdrive/d/gcc/bin/../lib/gcc/i686-pc-cygwin/3.4.4/include
            int i = path.indexOf(CYG_DRIVE_UNIX);
            if (i >= 0 && path.length() > i+CYG_DRIVE_UNIX.length()+1) {
                path = Character.toUpperCase(path.charAt(i+CYG_DRIVE_UNIX.length()))+":"+ // NOI18N
                        path.substring(i+CYG_DRIVE_UNIX.length()+1);
            }
        }
        return path;
    }
    
    private Map<String,String> systemMacroDefinitionsC;
    private Map<String,String> systemMacroDefinitionsCpp;
    public Map<String,String> getSystemMacroDefinitions(boolean isCPP) {
        Map<String,String> systemMacroDefinitions;
        if (isCPP) {
            systemMacroDefinitions = systemMacroDefinitionsCpp;
        } else {
            systemMacroDefinitions = systemMacroDefinitionsC;
        }
        if (systemMacroDefinitions == null) {
            systemMacroDefinitions = new HashMap<String,String>();
            MakeConfiguration makeConfiguration = (MakeConfiguration)makeConfigurationDescriptor.getConfs().getActive();
            Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
            CompilerSet compilerSet = CompilerSetManager.getDefault().getCompilerSet(makeConfiguration.getCompilerSet().getValue());
            BasicCompiler compiler;
            if (isCPP) {
                compiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
            } else {
                compiler = (BasicCompiler)compilerSet.getTool(Tool.CCompiler);
            }
            for(Object o :compiler.getSystemPreprocessorSymbols(platform)){
                String macro = (String)o;
                int i = macro.indexOf('=');
                if (i>0){
                    systemMacroDefinitions.put(macro.substring(0,i), macro.substring(i+1).trim());
                } else {
                    systemMacroDefinitions.put(macro, null);
                }
            }
            if (isCPP) {
                systemMacroDefinitionsCpp = systemMacroDefinitions;
            } else {
                systemMacroDefinitionsC = systemMacroDefinitions;
            }
        }
        return systemMacroDefinitions;
    }
}
