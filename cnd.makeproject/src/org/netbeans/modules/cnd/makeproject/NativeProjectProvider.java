/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.project.NativeExitStatus;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configurations;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

final public class NativeProjectProvider implements NativeProject, PropertyChangeListener {
    private static final boolean TRACE = false;

    private Project project;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private final Set<NativeProjectItemsListener> listeners = new HashSet<NativeProjectItemsListener>();

    public NativeProjectProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
    }

    @Override
    public void runOnCodeModelReadiness(Runnable task) {
        if (getMakeConfigurationDescriptor() != null) {
            getMakeConfigurationDescriptor().getConfs().runOnCodeModelReadiness(task);
        }
    }

    private void addMyListeners() {
        if (getMakeConfigurationDescriptor() != null) {
            getMakeConfigurationDescriptor().getConfs().addPropertyChangeListener(this);
        }
    }

    private void removeMyListeners() {
        if (getMakeConfigurationDescriptor() != null) {
            getMakeConfigurationDescriptor().getConfs().removePropertyChangeListener(this);
        }
    }

    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        return projectDescriptorProvider.getConfigurationDescriptor();
    }

    private MakeConfiguration getMakeConfiguration() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            return descriptor.getActiveConfiguration();
        }
        return null;
    }

    @Override
    public Object getProject() {
        return this.project;
    }

    @Override
    public List<String> getSourceRoots() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            return descriptor.getAbsoluteSourceRoots();
//            List<String> res = new ArrayList<String>(1);
//            res.add(descriptor.getBaseDir());
//            return res;
        }
        return Collections.<String>emptyList();
    }

    @Override
    public String getProjectRoot() {
        return FileUtil.toFile(project.getProjectDirectory()).getPath();
    }

    @Override
    public String getProjectDisplayName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    @Override
    public List<NativeFileItem> getAllFiles() {
        List<NativeFileItem> list = new ArrayList<NativeFileItem>();
        if (getMakeConfigurationDescriptor() == null || getMakeConfiguration() == null) {
            return list;
        }
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = items[i].getItemConfiguration(getMakeConfiguration());
            if (itemConfiguration != null) {
                if (itemConfiguration.isCompilerToolConfiguration()) {
                    list.add(items[i]);
                } else if (items[i].hasHeaderOrSourceExtension(true, true)) {
                    list.add(items[i]);
                }
            }
        }
        return list;
    }

    private Reference<List<NativeProject>> cachedDependency = new SoftReference<List<NativeProject>>(null);
    @Override
    public List<NativeProject> getDependences() {
        List<NativeProject> cachedList = cachedDependency.get();
        if (cachedList == null) {
            cachedList = new ArrayList<NativeProject>(0);
            MakeConfiguration makeConfiguration = getMakeConfiguration();
            int size = 0;
            NativeProject oneOf = null;
            if (makeConfiguration != null) {
                for (Object lib : makeConfiguration.getSubProjects()) {
                    Project prj = (Project) lib;
                    NativeProject nativeProject = prj.getLookup().lookup(NativeProject.class);
                    if (nativeProject != null) {
                        cachedList.add(nativeProject);
                        size++;
                        oneOf = nativeProject;
                    }
                }
            }
            if (size == 0) {
                cachedList = Collections.<NativeProject>emptyList();
            } else if (size == 1) {
                cachedList = Collections.singletonList(oneOf);
            } else {
                cachedList = Collections.unmodifiableList(cachedList);
            }
            cachedDependency = new SoftReference<List<NativeProject>>(cachedList);
        }
        return cachedList;
    }

    @Override
    public void addProjectItemsListener(NativeProjectItemsListener listener) {
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                addMyListeners();
            }
            listeners.add(listener);
        }
    }

    @Override
    public void removeProjectItemsListener(NativeProjectItemsListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                removeMyListeners();
            }
        }
    }

    public void fireFilesAdded(List<NativeFileItem> nativeFileIetms) {
        if (TRACE){
            System.out.println("fireFileAdded "); // NOI18N
        }
        ArrayList<NativeFileItem> actualList = new ArrayList<NativeFileItem>();
        // Remove non C/C++ items
        Iterator<NativeFileItem> iter = nativeFileIetms.iterator();
        while (iter.hasNext()) {
            NativeFileItem nativeFileIetm = iter.next();
            PredefinedToolKind tool = ((Item) nativeFileIetm).getDefaultTool();
            if (tool == PredefinedToolKind.CustomTool
                // check of mime type is better to support headers without extensions
                && !MIMENames.HEADER_MIME_TYPE.equals(((Item) nativeFileIetm).getMIMEType())) {
                continue; // IZ 87407
            }
            actualList.add(nativeFileIetm);
            if (TRACE){
                System.out.println("    " + ((Item)nativeFileIetm).getPath()); // NOI18N
            }
        }
        // Fire NativeProject change event
        if (actualList.size() > 0) {
            for (NativeProjectItemsListener listener : getListenersCopy()) {
                if (actualList.size() == 1) {
                    listener.fileAdded(actualList.get(0));
                } else {
                    listener.filesAdded(actualList);
                }
            }
        }
    }

    public void fireFilesRemoved(List<NativeFileItem> nativeFileIetms) {
        if (TRACE){
            System.out.println("fireFilesRemoved "); // NOI18N
        }
        ArrayList<NativeFileItem> actualList = new ArrayList<NativeFileItem>();
        // Remove non C/C++ items
        Iterator<NativeFileItem> iter = nativeFileIetms.iterator();
        while (iter.hasNext()) {
            NativeFileItem nativeFileIetm = iter.next();
            ItemConfiguration itemConfiguration = ((Item) nativeFileIetm).getItemConfiguration(getMakeConfiguration());
            if (itemConfiguration == null) {
                continue;
            }
            if ((!itemConfiguration.isCompilerToolConfiguration() 
                // check of mime type is better to support headers without extensions
                && !MIMENames.HEADER_MIME_TYPE.equals(((Item) nativeFileIetm).getMIMEType()))) {
                continue; // IZ 87407
            }
            actualList.add(nativeFileIetm);
            if (TRACE){
                System.out.println("    " + ((Item)nativeFileIetm).getPath()); // NOI18N
            }
        }
        // Fire NativeProject change event
        if (actualList.size() > 0) {
            for (NativeProjectItemsListener listener : getListenersCopy()) {
                if (actualList.size() == 1) {
                    listener.fileRemoved(actualList.get(0));
                } else {
                    listener.filesRemoved(actualList);
                }
            }
        }
    }

    public void fireFileRenamed(String oldPath, NativeFileItem newNativeFileIetm) {
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.fileRenamed(oldPath, newNativeFileIetm);
        }
    }

    public void fireFilePropertiesChanged(NativeFileItem nativeFileIetm) {
        //System.out.println("fireFilePropertiesChanged " + nativeFileIetm.getFile());
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filePropertiesChanged(nativeFileIetm);
        }
    }

    public void fireFilesPropertiesChanged(List<NativeFileItem> fileItems) {
        //System.out.println("fireFilesPropertiesChanged " + fileItems);
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filesPropertiesChanged(fileItems);
        }
    }

    public void fireFilesPropertiesChanged() {
        if (TRACE){
            new Exception().printStackTrace(System.err);
            System.out.println("fireFilesPropertiesChanged "); // NOI18N
        }
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filesPropertiesChanged();
        }
    }

    public void fireProjectDeleted() {
        if (TRACE){
            System.out.println("fireProjectDeleted "); // NOI18N
        }
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.projectDeleted(this);
        }
    }

    @SuppressWarnings("unchecked")
    private List<NativeProjectItemsListener> getListenersCopy() {
        synchronized (listeners) {
            return (listeners.isEmpty()) ? Collections.EMPTY_LIST : new ArrayList<NativeProjectItemsListener>(listeners);
        }
    }

    @Override
    public NativeFileItem findFileItem(File file) {
        MakeConfigurationDescriptor descr = getMakeConfigurationDescriptor();
        if (descr != null) {
            return (NativeFileItem) descr.findItemByFile(file);
        }
        return null;
    }

    private void checkConfigurationChanged(final Configuration oldConf, final Configuration newConf) {
        if (TRACE){
            new Exception().printStackTrace(System.err);
        }
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    checkConfigurationChangedWorker(oldConf, newConf);
                }
            });
        } else {
            checkConfigurationChangedWorker(oldConf, newConf);
        }
    }

    private void checkConfigurationChangedWorker(Configuration oldConf, Configuration newConf) {
        MakeConfiguration oldMConf = (MakeConfiguration) oldConf;
        MakeConfiguration newMConf = (MakeConfiguration) newConf;
        List<NativeFileItem> list = new ArrayList<NativeFileItem>();
        List<NativeFileItem> added = new ArrayList<NativeFileItem>();
        List<NativeFileItem> deleted = new ArrayList<NativeFileItem>();

        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
        }

        if (newConf == null) {
            // How can this happen?
            System.err.println("Nativeprojectprovider - checkConfigurationChanged - newConf is null!"); // NOI18N
            return;
        }

        if (!newConf.isDefault()) {
            return;
        }

        ConfigurationDescriptorProvider.recordMetrics(ConfigurationDescriptorProvider.USG_PROJECT_CONFIG_CND, getMakeConfigurationDescriptor());

        if (oldConf == null) {
            // What else can we do?
            firePropertiesChanged(getMakeConfigurationDescriptor().getProjectItems(), true, true, true);
            MakeLogicalViewProvider.checkForChangedViewItemNodes(getMakeConfigurationDescriptor().getProject(), null, null);
            MakeLogicalViewProvider.checkForChangedName(getMakeConfigurationDescriptor().getProject());
            return;
        }

        // Check compiler collection. Fire if different (IZ 131825)
        if (!oldMConf.getCompilerSet().getName().equals(newMConf.getCompilerSet().getName()) ||
                !oldMConf.getDevelopmentHost().getExecutionEnvironment().equals(newMConf.getDevelopmentHost().getExecutionEnvironment())) {
            fireFilesPropertiesChanged(); // firePropertiesChanged(getAllFiles(), true);
            MakeLogicalViewProvider.checkForChangedViewItemNodes(getMakeConfigurationDescriptor().getProject(), null, null);
            if (!oldMConf.getDevelopmentHost().getExecutionEnvironment().equals(newMConf.getDevelopmentHost().getExecutionEnvironment())) {
                MakeLogicalViewProvider.checkForChangedName(getMakeConfigurationDescriptor().getProject());
            }
            return;
        }

        CompilerSet oldCompilerSet = oldMConf.getCompilerSet().getCompilerSet();
        CompilerSet newCompilerSet = newMConf.getCompilerSet().getCompilerSet();

        // Check all items
        Item[] items = getMakeConfigurationDescriptor().getProjectItems();
        Project proj = getMakeConfigurationDescriptor().getProject();
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration oldItemConf = items[i].getItemConfiguration(oldMConf); //ItemConfiguration)oldMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            ItemConfiguration newItemConf = items[i].getItemConfiguration(newMConf); //ItemConfiguration)newMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (oldItemConf == null || newItemConf == null) {
                continue;
            }

            if ((newItemConf.getExcluded().getValue() ^ oldItemConf.getExcluded().getValue()) &&
                    (newItemConf.getTool() == PredefinedToolKind.CCompiler ||
                    newItemConf.getTool() == PredefinedToolKind.CCCompiler ||
                    items[i].hasHeaderOrSourceExtension(true, true))) {
                if (newItemConf.getExcluded().getValue()) {
                    // excluded
                    deleted.add(items[i]);
                } else {
                    // included
                    added.add(items[i]);
                }
                MakeLogicalViewProvider.checkForChangedViewItemNodes(proj, null, items[i]);
            }

            if (newItemConf.getExcluded().getValue()) {
                continue;
            }

            if (newItemConf.getTool() == PredefinedToolKind.CCompiler) {
                if (oldItemConf.getTool() != PredefinedToolKind.CCompiler) {
                    list.add(items[i]);
                    continue;
                }
                if (oldCompilerSet == null || newCompilerSet == null) {
                    if (oldCompilerSet != null || newCompilerSet != null) {
                        list.add(items[i]);
                    }
                    continue;
                }
                if (!oldItemConf.getCCompilerConfiguration().getPreprocessorOptions(oldCompilerSet).equals(newItemConf.getCCompilerConfiguration().getPreprocessorOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions(oldCompilerSet).equals(newItemConf.getCCompilerConfiguration().getIncludeDirectoriesOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
            }
            if (newItemConf.getTool() == PredefinedToolKind.CCCompiler) {
                if (oldItemConf.getTool() != PredefinedToolKind.CCCompiler) {
                    list.add(items[i]);
                    continue;
                }
                if (oldCompilerSet == null || newCompilerSet == null) {
                    if (oldCompilerSet != null || newCompilerSet != null) {
                        list.add(items[i]);
                    }
                    continue;
                }
                if (!oldItemConf.getCCCompilerConfiguration().getPreprocessorOptions(oldCompilerSet).equals(newItemConf.getCCCompilerConfiguration().getPreprocessorOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
                if (!oldItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions(oldCompilerSet).equals(newItemConf.getCCCompilerConfiguration().getIncludeDirectoriesOptions(newCompilerSet))) {
                    list.add(items[i]);
                    continue;
                }
            }
        }
        fireFilesRemoved(deleted);
        fireFilesAdded(added);
        firePropertiesChanged(list, true);
    }

    public void checkForChangedItems(final Folder folder, final Item item) {
        if (SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    checkForChangedItemsWorker(folder, item);
                }
            });
        } else {
            checkForChangedItemsWorker(folder, item);
        }
    }

    private void checkForChangedItemsWorker(Folder folder, Item item) {
        clearCache();
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
        }

        MakeConfiguration makeConfiguration = getMakeConfiguration();
        boolean cFiles = false;
        boolean ccFiles = false;
        boolean libsChanged = false;
        boolean projectChanged = false;
        VectorConfiguration<String> cIncludeDirectories = null;
        BooleanConfiguration cInheritIncludes = null;
        VectorConfiguration<String> cPpreprocessorOption = null;
        BooleanConfiguration cInheritMacros = null;
        VectorConfiguration<String> ccIncludeDirectories = null;
        BooleanConfiguration ccInheritIncludes = null;
        VectorConfiguration<String> ccPreprocessorOption = null;
        BooleanConfiguration ccInheritMacros = null;
        Item[] items;

        // Check first whether the development host has changed
        if (makeConfiguration.getDevelopmentHost().getDirty()) {
            makeConfiguration.getDevelopmentHost().setDirty(false);
            items = getMakeConfigurationDescriptor().getProjectItems();
            firePropertiesChanged(items, true, true, true);
            return;
        } else if (makeConfiguration.getCompilerSet().getDirty()) {
            // Next, check whether the compiler set has changed
            makeConfiguration.getCompilerSet().setDirty(false);
            items = getMakeConfigurationDescriptor().getProjectItems();
            firePropertiesChanged(items, true, true, true);
            return;
        }

        if (folder != null) {
            FolderConfiguration folderConfiguration = folder.getFolderConfiguration(makeConfiguration);
            cIncludeDirectories = folderConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cInheritIncludes = folderConfiguration.getCCompilerConfiguration().getInheritIncludes();
            cPpreprocessorOption = folderConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            cInheritMacros = folderConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
            ccIncludeDirectories = folderConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccInheritIncludes = folderConfiguration.getCCCompilerConfiguration().getInheritIncludes();
            ccPreprocessorOption = folderConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            ccInheritMacros = folderConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            items = folder.getAllItemsAsArray();
        } else if (item != null) {
            ItemConfiguration itemConfiguration = item.getItemConfiguration(getMakeConfiguration()); //ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(item.getPath()));
            if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                cIncludeDirectories = itemConfiguration.getCCompilerConfiguration().getIncludeDirectories();
                cInheritIncludes = itemConfiguration.getCCompilerConfiguration().getInheritIncludes();
                cInheritMacros = itemConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
                cPpreprocessorOption = itemConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            }
            if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                ccIncludeDirectories = itemConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
                ccInheritIncludes = itemConfiguration.getCCCompilerConfiguration().getInheritIncludes();
                ccPreprocessorOption = itemConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
                ccInheritMacros = itemConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            }
            if (itemConfiguration.getExcluded().getDirty()) {
                itemConfiguration.getExcluded().setDirty(false);
                ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>();
                list.add(item);
                if (itemConfiguration.getExcluded().getValue()) {
                    fireFilesRemoved(list);
                } else {
                    fireFilesAdded(list);
                }
            }
            items = new Item[]{item};
        } else {
            libsChanged = makeConfiguration.getRequiredProjectsConfiguration().getDirty() ||
                    makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration().getDirty();
            cIncludeDirectories = makeConfiguration.getCCompilerConfiguration().getIncludeDirectories();
            cInheritIncludes = makeConfiguration.getCCompilerConfiguration().getInheritIncludes();
            cPpreprocessorOption = makeConfiguration.getCCompilerConfiguration().getPreprocessorConfiguration();
            cInheritMacros = makeConfiguration.getCCompilerConfiguration().getInheritPreprocessor();
            ccIncludeDirectories = makeConfiguration.getCCCompilerConfiguration().getIncludeDirectories();
            ccInheritIncludes = makeConfiguration.getCCCompilerConfiguration().getInheritIncludes();
            ccPreprocessorOption = makeConfiguration.getCCCompilerConfiguration().getPreprocessorConfiguration();
            ccInheritMacros = makeConfiguration.getCCCompilerConfiguration().getInheritPreprocessor();
            items = getMakeConfigurationDescriptor().getProjectItems();
            projectChanged = true;
//            cFiles = true;
//            ccFiles = true;
        }

        if (cIncludeDirectories != null &&
            (cIncludeDirectories.getDirty() || cPpreprocessorOption.getDirty() ||
             cInheritIncludes.getDirty() || cInheritMacros.getDirty())) {
            cFiles = true;
            cIncludeDirectories.setDirty(false);
            cPpreprocessorOption.setDirty(false);
            cInheritIncludes.setDirty(false);
            cInheritMacros.setDirty(false);
        }
        if (ccIncludeDirectories != null &&
            (ccIncludeDirectories.getDirty() || ccPreprocessorOption.getDirty() ||
             ccInheritIncludes.getDirty() || ccInheritMacros.getDirty())) {
            ccFiles = true;
            ccIncludeDirectories.setDirty(false);
            ccPreprocessorOption.setDirty(false);
            ccInheritIncludes.setDirty(false);
            ccInheritMacros.setDirty(false);
        }
        if (libsChanged) {
            makeConfiguration.getRequiredProjectsConfiguration().setDirty(false);
            makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration().setDirty(false);
            cFiles = true;
            ccFiles = true;
        }
        if (cFiles || ccFiles) {
            firePropertiesChanged(items, cFiles, ccFiles, projectChanged);
        }
    }

    private void firePropertiesChanged(Item[] items, boolean cFiles, boolean ccFiles, boolean projectChanged) {
        ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>();
        ArrayList<NativeFileItem> deleted = new ArrayList<NativeFileItem>();
        // Handle project and file level changes
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = items[i].getItemConfiguration(getMakeConfiguration()); //ItemConfiguration)getMakeConfiguration().getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (itemConfiguration != null) { // prevent NPE for corrupted projects IZ#174350
                if (itemConfiguration.getExcluded().getValue()) {
                    deleted.add(items[i]);
                    continue;
                }
                if ((cFiles && itemConfiguration.getTool() == PredefinedToolKind.CCompiler) ||
                        (ccFiles && itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) ||
                        items[i].hasHeaderOrSourceExtension(cFiles, ccFiles)) {
                    list.add(items[i]);
                }
            }
        }
        if (deleted.size() > 0) {
            fireFilesRemoved(deleted);
        }
        firePropertiesChanged(list, projectChanged);
    }

    private void firePropertiesChanged(List<NativeFileItem> list, boolean projectChanged) {
        if (list.size() > 1 || (projectChanged && list.size() == 1)) {
            fireFilesPropertiesChanged(list);
        } else if (list.size() == 1) {
            fireFilePropertiesChanged(list.get(0));
        } else {
            // nothing
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (TRACE){
            System.out.println("propertyChange " + evt.getPropertyName()); // NOI18N
        }
        if (evt.getPropertyName().equals(Configurations.PROP_ACTIVE_CONFIGURATION)) {
            checkConfigurationChanged((Configuration) evt.getOldValue(), (Configuration) evt.getNewValue());
        }
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
    @Override
    public List<String> getSystemIncludePaths() {
        ArrayList<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return vec;
            }
            AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            if (compiler != null) {
                vec.addAll(compiler.getSystemIncludeDirectories());
            }
        }
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
    @Override
    public List<String> getUserIncludePaths() {
        ArrayList<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CCCompilerConfiguration cccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            ArrayList<String> vec2 = new ArrayList<String>();
            vec2.addAll(cccCompilerConfiguration.getIncludeDirectories().getValue());
            // Convert all paths to absolute paths
            Iterator<String> iter = vec2.iterator();
            while (iter.hasNext()) {
                vec.add(CndPathUtilitities.toAbsolutePath(makeConfiguration.getBaseDir(), iter.next()));
            }
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
    @Override
    public List<String> getSystemMacroDefinitions() {
        ArrayList<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return vec;
            }
            AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            if (compiler != null) {
                vec.addAll(compiler.getSystemPreprocessorSymbols());
            }
        }
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
    @Override
    public List<String> getUserMacroDefinitions() {
        ArrayList<String> vec = new ArrayList<String>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CCCompilerConfiguration cccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            vec.addAll(cccCompilerConfiguration.getPreprocessorConfiguration().getValue());
        }
        return vec;
    }

    @Override
    public String toString() {
        return getProjectDisplayName()+" "+getProjectRoot(); // NOI18N
    }

    private void clearCache() {
        cachedDependency.clear();
    }

    @Override
    public NativeExitStatus execute(String executable, String[] env, String... args) {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ExecutionEnvironment ev = makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        if (ev.isLocal()) {
            String exePath = Path.findCommand(executable);
            String arguments = "";
            for (String s : args) {
                arguments += " " + s; // NOI18N
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StringBuffer output = new StringBuffer();

            try {
                Process p0 = Runtime.getRuntime().exec(exePath + " " + arguments, env); // NOI18N
                InputStream is = p0.getInputStream();
                InputStreamReader ist = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(ist);
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line + "\n"); // NOI18N
                }
                br.close();
                ist.close();
                is.close();
                bos.close();
                return new NativeExitStatus(0, output.toString(), "");
            }
            catch (IOException ioe) {
                return new NativeExitStatus(-1, "", output.toString());
            }
        }
        else {
            ExitStatus exitStatus = ProcessUtils.execute(ev, executable, args); // NOI18N
            // FIXUP: need to handle env!
            return new NativeExitStatus(exitStatus.exitCode, exitStatus.output, exitStatus.error);
        }
    }
}
