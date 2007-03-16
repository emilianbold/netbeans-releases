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

package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.loaders.HDataLoader;
import org.netbeans.modules.cnd.makeproject.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.OptionsConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platforms;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.compilers.CompilerSets;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ExtensionList;

final public class NativeProjectProvider implements NativeProject, PropertyChangeListener {
    private Project project;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private ArrayList listeners = new ArrayList();
    
    
    public NativeProjectProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
    }
    
    private void addMyListeners() {
        getMakeConfigurationDescriptor().getConfs().addPropertyChangeListener(this);
    }
    
    private void removeMyListeners() {
        getMakeConfigurationDescriptor().getConfs().removePropertyChangeListener(this);
    }
    
    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        return (MakeConfigurationDescriptor)projectDescriptorProvider.getConfigurationDescriptor();
    }
    
    private MakeConfiguration getMakeConfiguration() {
        return (MakeConfiguration)getMakeConfigurationDescriptor().getConfs().getActive();
    }
    
    public String getProjectRoot() {
        return FileUtil.toFile(project.getProjectDirectory()).getPath();
    }
    
    public String getProjectDisplayName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }
    
    public List getAllSourceFiles() {
        ArrayList list = new ArrayList();
        if (getMakeConfigurationDescriptor() == null)
            return null;
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = (ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (itemConfiguration != null && itemConfiguration.isCompilerToolConfiguration() && !itemConfiguration.getExcluded().getValue())
                list.add(items[i]);
        }
        return list;
    }
    
    public List getAllHeaderFiles() {
        ArrayList list = new ArrayList();
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        ExtensionList hlist = HDataLoader.getInstance().getExtensions();
        
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = (ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (itemConfiguration != null){
                if (!itemConfiguration.getExcluded().getValue()){
                    list.add(items[i]);
                }
            } else if (hlist.isRegistered(items[i].getPath())) {
                list.add(items[i]);
            }
        }
        return list;
    }
    
    public void addProjectItemsListener(NativeProjectItemsListener listener) {
        if (listeners.size() == 0)
            addMyListeners();
        listeners.add(listener);
    }
    
    public void removeProjectItemsListener(NativeProjectItemsListener listener) {
        listeners.remove(listener);
        if (listeners.size() == 0)
            removeMyListeners();
    }
    
    public void fireFilesAdded(List<NativeFileItem> nativeFileIetms) {
        //System.out.println("fireFileAdded ");
        ArrayList actualList = new ArrayList();
        ExtensionList hlist = HDataLoader.getInstance().getExtensions();
        if (listeners.size() == 0)
            return;
        // Remove non C/C++ items
        Iterator iter = nativeFileIetms.iterator();
        while (iter.hasNext()) {
            NativeFileItem nativeFileIetm = (NativeFileItem)iter.next();
            int tool = ((Item)nativeFileIetm).getDefaultTool();
            if (tool == Tool.CustomTool && !hlist.isRegistered(((Item)nativeFileIetm).getPath()))
                continue; // IZ 87407
            actualList.add(nativeFileIetm);
            //System.out.println("    " + ((Item)nativeFileIetm).getPath());
        }
        // Fire NativeProject change event
        if (actualList.size() > 0) {
            for (int i = 0; i < listeners.size(); i++) {
                NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
                if (actualList.size() == 1)
                    listener.fileAdded((NativeFileItem)actualList.get(0));
                else
                    listener.filesAdded(actualList);
            }
        }
    }
    
    public void fireFilesRemoved(List<NativeFileItem> nativeFileIetms) {
        //System.out.println("fireFilesRemoved ");
        ArrayList actualList = new ArrayList();
        ExtensionList hlist = HDataLoader.getInstance().getExtensions();
        if (listeners.size() == 0)
            return;
        // Remove non C/C++ items
        Iterator iter = nativeFileIetms.iterator();
        while (iter.hasNext()) {
            NativeFileItem nativeFileIetm = (NativeFileItem)iter.next();
            ItemConfiguration itemConfiguration = (ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(((Item)nativeFileIetm).getPath()));
            if ((!itemConfiguration.isCompilerToolConfiguration() && !hlist.isRegistered(((Item)nativeFileIetm).getPath())) || itemConfiguration.getExcluded().getValue())
                continue; // IZ 87407
            actualList.add(nativeFileIetm);
            //System.out.println("    " + ((Item)nativeFileIetm).getPath());
        }
        // Fire NativeProject change event
        if (actualList.size() > 0) {
            for (int i = 0; i < listeners.size(); i++) {
                NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
                if (actualList.size() == 1)
                    listener.fileRemoved((NativeFileItem)actualList.get(0));
                else
                    listener.filesRemoved(actualList);
            }
        }
    }
    
    public void fireFilePropertiesChanged(NativeFileItem nativeFileIetm) {
        //System.out.println("fireFilePropertiesChanged " + nativeFileIetm.getFile());
        for (int i = 0; i < listeners.size(); i++) {
            NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
            listener.filePropertiesChanged(nativeFileIetm);
        }
    }
    
    public void fireFilesPropertiesChanged(List<NativeFileItem> fileItems) {
        //System.out.println("fireFilesPropertiesChanged " + fileItems);
        for (int i = 0; i < listeners.size(); i++) {
            NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
            listener.filesPropertiesChanged(fileItems);
        }
    }
    
    public void fireFilesPropertiesChanged() {
        //System.out.println("fireFilesPropertiesChanged ");
        for (int i = 0; i < listeners.size(); i++) {
            NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
            listener.filesPropertiesChanged();
        }
    }
    
    public NativeFileItem findFileItem(File file) {
        return (NativeFileItem)getMakeConfigurationDescriptor().findItemByFile(file);
    }
    
    private void checkConfigurationChanged(Configuration oldConf, Configuration newConf) {
        MakeConfiguration oldMConf = (MakeConfiguration)oldConf;
        MakeConfiguration newMConf = (MakeConfiguration)newConf;
        List<NativeFileItem> list = new ArrayList();
        
        if (listeners.size() == 0)
            return;
        
        if (newConf == null) {
            // How can this happen?
            System.err.println("Nativeprojectprovider - checkConfigurationChanged - newConf is null!"); // NOI18N
            return;
        }
        
        if (!newConf.isDefault())
            return;
        
        if (oldConf == null) {
            // What else can we do?
            firePropertiesChanged(getMakeConfigurationDescriptor().getProjectItems(), true, true);
            return;
        }
        
        // Check all items
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration oldItemConf = (ItemConfiguration)oldMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            ItemConfiguration newItemConf = (ItemConfiguration)newMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (oldItemConf == null || newItemConf == null) {
                continue;
            }
            
            if (newItemConf.getExcluded().getValue())
                continue;
            
            if (newItemConf.getTool() == Tool.CCompiler) {
                if (!oldItemConf.getCCompilerConfiguration().getPreprocessorOptions().equals(newItemConf.getCCompilerConfiguration().getPreprocessorOptions())) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions().equals(newItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions())) {
                    list.add(items[i]);
                    continue;
                }
            }
            if (newItemConf.getTool() == Tool.CCCompiler) {
                if (!oldItemConf.getCCCompilerConfiguration().getPreprocessorOptions().equals(newItemConf.getCCCompilerConfiguration().getPreprocessorOptions())) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions().equals(newItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions())) {
                    list.add(items[i]);
                    continue;
                }
            }
        }
        
        firePropertiesChanged(list);
    }
    
    public void checkForChangedItems(Folder folder, Item item) {
        if (listeners.size() == 0)
            return;
        
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        boolean cFiles = false;
        boolean ccFiles = false;
        VectorConfiguration cIncludeDirectories;
        OptionsConfiguration cPpreprocessorOption;
        VectorConfiguration ccIncludeDirectories;
        OptionsConfiguration ccPreprocessorOption;
        Item[] items;
        
        if (folder != null) {
            FolderConfiguration folderConfiguration = folder.getFolderConfiguration(makeConfiguration);
            cIncludeDirectories = folderConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cPpreprocessorOption = folderConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            ccIncludeDirectories = folderConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccPreprocessorOption = folderConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            items = folder.getAllItemsAsArray();
        } else if (item != null) {
            ItemConfiguration itemConfiguration = (ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(item.getPath()));
            cIncludeDirectories = itemConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cPpreprocessorOption = itemConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            ccIncludeDirectories = itemConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccPreprocessorOption = itemConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            items = new Item[] {item};
        } else {
            cIncludeDirectories = makeConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cPpreprocessorOption = makeConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            ccIncludeDirectories = makeConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccPreprocessorOption = makeConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            items = getMakeConfigurationDescriptor().getProjectItems();
        }
        
        if (cIncludeDirectories.getDirty() || cPpreprocessorOption.getDirty()) {
            cFiles = true;
            cIncludeDirectories.setDirty(false);
            cPpreprocessorOption.setDirty(false);
        }
        if (ccIncludeDirectories.getDirty() || ccPreprocessorOption.getDirty()) {
            ccFiles = true;
            ccIncludeDirectories.setDirty(false);
            ccPreprocessorOption.setDirty(false);
        }
        
        if (cFiles || ccFiles)
            firePropertiesChanged(items, cFiles, ccFiles);
    }
    
    private void firePropertiesChanged(Item[] items, boolean cFiles, boolean ccFiles) {
        List<NativeFileItem> list = selectItems(items, cFiles, ccFiles);
        firePropertiesChanged(list);
    }
    
    private void firePropertiesChanged(List<NativeFileItem> list) {
        if (list.size() > 1) {
            fireFilesPropertiesChanged(list);
        } else if (list.size() == 1) {
            fireFilePropertiesChanged((NativeFileItem)list.get(0));
        } else {
            ; // nothing
        }
    }
    
    public List<NativeFileItem> selectItems(Item[] items, boolean cFiles, boolean ccFiles) {
        ArrayList<NativeFileItem> list = new ArrayList();
        
        // Handle project and file level changes
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = (ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (itemConfiguration.getExcluded().getValue())
                continue;
            if (itemConfiguration.getTool() != Tool.CCompiler && itemConfiguration.getTool() != Tool.CCCompiler)
                continue;
            
            if ((cFiles && itemConfiguration.getTool() == Tool.CCompiler) || (ccFiles && itemConfiguration.getTool() == Tool.CCCompiler)) {
                list.add(items[i]);
            }
        }
        
        return list;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        //System.out.println("propertyChange " + evt.getPropertyName());
        if (evt.getPropertyName().equals(Configurations.PROP_ACTIVE_CONFIGURATION))
            checkConfigurationChanged((Configuration)evt.getOldValue(), (Configuration)evt.getNewValue());
    }
    
    /**
     * Returns a list <String> of compiler defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    /*
     * Return C++ settings
     **/
    public List/*<String>*/ getSystemIncludePaths() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
        vec.addAll(compiler.getSystemIncludeDirectories(platform));
        return vec;
    }
    
    /**
     * Returns a list <String> of user defined include paths used when parsing 'orpan' source files.
     * @return a list <String> of user defined include paths.
     * A path is always an absolute path.
     * Include paths are not prefixed with the compiler include path option (usually -I).
     */
    /*
     * Return C++ settings
     **/
    public List/*<String>*/ getUserIncludePaths() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        //Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CCCompilerConfiguration cccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
        ArrayList vec2 = new ArrayList();
        vec2.addAll(cccCompilerConfiguration.getIncludeDirectories().getValue());
        // Convert all paths to absolute paths
        Iterator iter = vec2.iterator();
        while (iter.hasNext()) {
            vec.add(IpeUtils.toAbsolutePath(makeConfiguration.getBaseDir(), (String)iter.next()));
        }
        return vec;
    }
    
    /**
     * Returns a list <String> of compiler defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of compiler defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    /*
     * Return C++ settings
     **/
    public List/*<String>*/ getSystemMacroDefinitions() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CompilerSet compilerSet = CompilerSets.getCompilerSet(makeConfiguration.getCompilerSet().getValue());
        BasicCompiler compiler = (BasicCompiler)compilerSet.getTool(Tool.CCCompiler);
        vec.addAll(compiler.getSystemPreprocessorSymbols(platform));
        return vec;
    }
    
    /**
     * Returns a list <String> of user defined macro definitions used when parsing 'orpan' source files.
     * @return a list <String> of user defined macro definitions.
     * Macro definitions are not prefixed with the compiler option (usually -D).
     */
    /*
     * Return C++ settings
     **/
    public List/*<String>*/ getUserMacroDefinitions() {
        ArrayList vec = new ArrayList();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        //Platform platform = Platforms.getPlatform(makeConfiguration.getPlatform().getValue());
        CCCompilerConfiguration cccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
        vec.addAll(cccCompilerConfiguration.getPreprocessorConfiguration().getValuesAsVector());
        return vec;
    }
}
