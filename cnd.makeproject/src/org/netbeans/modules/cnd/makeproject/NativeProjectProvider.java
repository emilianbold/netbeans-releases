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
import org.openide.filesystems.FileUtil;

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
        for (int i = 0; i < items.length; i++) {
            String suffix = null;
            int si = items[i].getPath().lastIndexOf("."); // NOI18N
            if (si >= 0)
                suffix = items[i].getPath().substring(si+1);
            else
                continue;
            if (amongSuffixes(suffix, HDataLoader.getInstance().suffixes()))
                list.add(items[i]);
        }
        return list;
    }
    
    private boolean amongSuffixes(String suffix, String[] suffixes) {
        for (int i = 0; i < suffixes.length; i++) {
            if (suffixes[i].equals(suffix))
                return true;
        }
        return false;
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
    
    public void fireFileAdded(NativeFileItem nativeFileIetm) {
        if (listeners.size() == 0)
            return;
        int tool = ((Item)nativeFileIetm).getDefaultTool();
        if (tool != Tool.CCompiler && tool != Tool.CCCompiler)
            return; // IZ 87407
        for (int i = 0; i < listeners.size(); i++) {
            NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
            listener.fileAdded(nativeFileIetm);
        }
    }
    
    public void fireFileRemoved(NativeFileItem nativeFileIetm) {
        if (listeners.size() == 0)
            return;
        ItemConfiguration itemConfiguration = (ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(((Item)nativeFileIetm).getPath()));
        if (!itemConfiguration.isCompilerToolConfiguration() || itemConfiguration.getExcluded().getValue())
            return; // IZ 87407
        for (int i = 0; i < listeners.size(); i++) {
            NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
            listener.fileRemoved(nativeFileIetm);
        }
    }
    
    public void fireFilePropertiesChanged(NativeFileItem nativeFileIetm) {
        for (int i = 0; i < listeners.size(); i++) {
            NativeProjectItemsListener listener = (NativeProjectItemsListener)listeners.get(i);
            listener.filePropertiesChanged(nativeFileIetm);
        }
    }
    
    public NativeFileItem findFileItem(File file) {
        return (NativeFileItem)getMakeConfigurationDescriptor().findItemByFile(file);
    }
    
    public void checkConfigurationChanged(Configuration oldConf, Configuration newConf) {
        MakeConfiguration oldMConf = (MakeConfiguration)oldConf;
        MakeConfiguration newMConf = (MakeConfiguration)newConf;
        
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
            checkForChangedItems(true, true);
            return;
        }
        
        boolean allCFiles = false;
        boolean allCCFiles = false;
        OptionsConfiguration oldPreprocessorOption = oldMConf.getCCompilerConfiguration().getPreprocessorConfiguration();
        OptionsConfiguration newPreprocessorOption = newMConf.getCCompilerConfiguration().getPreprocessorConfiguration();
        allCFiles = !oldPreprocessorOption.getValue().equals(newPreprocessorOption.getValue());
        
        VectorConfiguration oldIncludeDirectories = oldMConf.getCCompilerConfiguration().getIncludeDirectories();
        VectorConfiguration newIncludeDirectories = newMConf.getCCompilerConfiguration().getIncludeDirectories();
        allCFiles = allCFiles || !oldIncludeDirectories.equals(newIncludeDirectories);
        
        oldPreprocessorOption = oldMConf.getCCCompilerConfiguration().getPreprocessorConfiguration();
        newPreprocessorOption = newMConf.getCCCompilerConfiguration().getPreprocessorConfiguration();
        allCCFiles = !oldPreprocessorOption.getValue().equals(newPreprocessorOption.getValue());
        
        oldIncludeDirectories = oldMConf.getCCCompilerConfiguration().getIncludeDirectories();
        newIncludeDirectories = newMConf.getCCCompilerConfiguration().getIncludeDirectories();
        allCCFiles = allCCFiles || !oldIncludeDirectories.equals(newIncludeDirectories);
        
        // Check all items
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration oldItemConf = (ItemConfiguration)oldMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            ItemConfiguration newItemConf = (ItemConfiguration)newMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            
            if (oldItemConf == null || newItemConf == null) {
                System.err.println("NativeProjectProvider - checkConfigurationChanged - project " + project); // NOI18N FIXUP
                System.err.println("NativeProjectProvider - checkConfigurationChanged - item " + items[i].getPath()); // NOI18N FIXUP
                System.err.println("NativeProjectProvider - checkConfigurationChanged - oldItemConf " + oldItemConf); // NOI18N FIXUP
                System.err.println("NativeProjectProvider - checkConfigurationChanged - newItemConf " + newItemConf); // NOI18N FIXUP
                continue;
            }
            oldPreprocessorOption = oldItemConf.getCCompilerConfiguration().getPreprocessorConfiguration();
            newPreprocessorOption = newItemConf.getCCompilerConfiguration().getPreprocessorConfiguration();
            newPreprocessorOption.setDirty(!oldPreprocessorOption.getValue().equals(newPreprocessorOption.getValue()));
            
            oldIncludeDirectories = oldItemConf.getCCompilerConfiguration().getIncludeDirectories();
            newIncludeDirectories = newItemConf.getCCompilerConfiguration().getIncludeDirectories();
            newIncludeDirectories.setDirty(!oldIncludeDirectories.equals(newIncludeDirectories));
            
            oldPreprocessorOption = oldItemConf.getCCCompilerConfiguration().getPreprocessorConfiguration();
            newPreprocessorOption = newItemConf.getCCCompilerConfiguration().getPreprocessorConfiguration();
            newPreprocessorOption.setDirty(!oldPreprocessorOption.getValue().equals(newPreprocessorOption.getValue()));
            
            oldIncludeDirectories = oldItemConf.getCCCompilerConfiguration().getIncludeDirectories();
            newIncludeDirectories = newItemConf.getCCCompilerConfiguration().getIncludeDirectories();
            newIncludeDirectories.setDirty(!oldIncludeDirectories.equals(newIncludeDirectories));
        }
        checkForChangedItems(allCFiles, allCCFiles);
    }
    
    public void checkForChangedItems() {
        if (listeners.size() == 0)
            return;
        
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        boolean allCFiles = false;
        boolean allCCFiles = false;
        OptionsConfiguration preprocessorOption = makeConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
        VectorConfiguration includeDirectories = makeConfiguration.getCCompilerConfiguration().getIncludeDirectories();
        if (preprocessorOption.getDirty() || includeDirectories.getDirty()) {
            allCFiles = true;
            preprocessorOption.setDirty(false);
            includeDirectories.setDirty(false);
        }
        preprocessorOption = makeConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
        includeDirectories = makeConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
        if (preprocessorOption.getDirty() || includeDirectories.getDirty()) {
            allCCFiles = true;
            preprocessorOption.setDirty(false);
            includeDirectories.setDirty(false);
        }
        
        checkForChangedItems(allCFiles, allCCFiles);
    }
    
    public void checkForChangedItems(boolean allCFiles, boolean allCCFiles) {
        ArrayList list = new ArrayList();
        
        // Handle project and file level changes
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = (ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (itemConfiguration.getExcluded().getValue())
                continue;
            if (itemConfiguration.getTool() != Tool.CCompiler && itemConfiguration.getTool() != Tool.CCCompiler)
                continue;
            if ((allCFiles && itemConfiguration.getTool() == Tool.CCompiler) || (allCCFiles && itemConfiguration.getTool() == Tool.CCCompiler)) {
                list.add(items[i]);
                continue;
            }
            OptionsConfiguration preprocessorOption = itemConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            VectorConfiguration includeDirectories = itemConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            if (itemConfiguration.getTool() == Tool.CCompiler && (preprocessorOption.getDirty() || includeDirectories.getDirty())) {
                preprocessorOption.setDirty(false);
                includeDirectories.setDirty(false);
                list.add(items[i]);
                continue;
            }
            preprocessorOption = itemConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            includeDirectories = itemConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            if (itemConfiguration.getTool() == Tool.CCCompiler && (preprocessorOption.getDirty() || includeDirectories.getDirty())) {
                preprocessorOption.setDirty(false);
                includeDirectories.setDirty(false);
                list.add(items[i]);
                continue;
            }
        }
        // Fire ...
        for (int i = 0; i < list.size(); i++)
            fireFilePropertiesChanged((NativeFileItem)list.get(i));
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
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
