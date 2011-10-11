/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.project.NativeExitStatus;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectChangeSupport;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
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
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.Delta;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

final public class NativeProjectProvider implements NativeProject, PropertyChangeListener, ChangeListener, NativeProjectChangeSupport {

    private static final boolean TRACE = false;
    private final Project project;
    private final ConfigurationDescriptorProvider projectDescriptorProvider;
    private final Set<NativeProjectItemsListener> listeners = new HashSet<NativeProjectItemsListener>();

    public NativeProjectProvider(Project project, ConfigurationDescriptorProvider projectDescriptorProvider) {
        this.project = project;
        this.projectDescriptorProvider = projectDescriptorProvider;
        ToolsPanelSupport.addCodeAssistanceChangeListener(this);
    }

    @Override
    public void runOnProjectReadiness(NamedRunnable task) {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            descriptor.getConfs().runOnProjectReadiness(task);
        }
    }

    private void addMyListeners() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            descriptor.getConfs().addPropertyChangeListener(this);
        }
    }

    private void removeMyListeners() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            descriptor.getConfs().removePropertyChangeListener(this);
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
    public Lookup.Provider getProject() {
        return project;
    }

    @Override
    public FileSystem getFileSystem() {
        FileSystem fileSystem;
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            CndUtils.assertFalse(true, "Can not find RemoteProject in " + project.getProjectDirectory()); //NOI18N
            fileSystem = CndFileUtils.getLocalFileSystem();
        } else {
            ExecutionEnvironment env = rp.getSourceFileSystemHost();
            fileSystem = FileSystemProvider.getFileSystem(env);
        }
        CndUtils.assertNotNull(fileSystem, "null file system"); //NOI18N        
        return fileSystem;
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
        String projectRoot;
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            CndUtils.assertFalse(true, "Can not find RemoteProject in " + project.getProjectDirectory()); //NOI18N
            projectRoot = project.getProjectDirectory().getPath();
        } else {
            projectRoot = rp.getSourceBaseDir();
        }
        CndUtils.assertNotNull(projectRoot, "null projectRoot"); //NOI18N        
        return projectRoot;
    }

    @Override
    public String getProjectDisplayName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    @Override
    public List<NativeFileItem> getAllFiles() {
        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        if (descriptor != null) {
            MakeConfiguration conf = descriptor.getActiveConfiguration();
            if (conf != null) {
                List<NativeFileItem> list = new ArrayList<NativeFileItem>();
                Item[] items = descriptor.getProjectItems();
                for (Item item : items) {
                    ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);
                    if (itemConfiguration != null) {
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            list.add(item);
                        } else if (item.hasHeaderOrSourceExtension(true, true)) {
                            list.add(item);
                        }
                    }
                }
                return list;
            }
        }
        return Collections.emptyList();
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

    @Override
    public void fireFilesAdded(List<NativeFileItem> nativeFileIetms) {
        if (TRACE) {
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
            if (TRACE) {
                System.out.println("    " + ((Item) nativeFileIetm).getPath()); // NOI18N
            }
        }
        // Fire NativeProject change event
        if (!actualList.isEmpty()) {
            for (NativeProjectItemsListener listener : getListenersCopy()) {
                listener.filesAdded(actualList);
            }
        }
    }

    @Override
    public void fireFilesRemoved(List<NativeFileItem> nativeFileItems) {
        if (TRACE) {
            System.out.println("fireFilesRemoved "); // NOI18N
        }
        // Fire NativeProject change event
        if (!nativeFileItems.isEmpty()) {
            for (NativeProjectItemsListener listener : getListenersCopy()) {
                listener.filesRemoved(nativeFileItems);
            }
        }
    }

    @Override
    public void fireFileRenamed(String oldPath, NativeFileItem newNativeFileIetm) {
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.fileRenamed(oldPath, newNativeFileIetm);
        }
    }

    private void fireFilePropertiesChanged(NativeFileItem nativeFileIetm) {
        //System.out.println("fireFilePropertiesChanged " + nativeFileIetm.getFile());
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filePropertiesChanged(nativeFileIetm);
        }
    }

    @Override
    public void fireFilesPropertiesChanged(List<NativeFileItem> fileItems) {
        //System.out.println("fireFilesPropertiesChanged " + fileItems);
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filesPropertiesChanged(fileItems);
        }
    }

    @Override
    public void fireFilesPropertiesChanged() {
        if (TRACE) {
            new Exception().printStackTrace(System.err);
            System.out.println("fireFilesPropertiesChanged "); // NOI18N
        }
        for (NativeProjectItemsListener listener : getListenersCopy()) {
            listener.filesPropertiesChanged();
        }
    }

    public void fireProjectDeleted() {
        if (TRACE) {
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
    public NativeFileItem findFileItem(FileObject fileObject) {
        MakeConfigurationDescriptor descr = getMakeConfigurationDescriptor();
        if (descr != null) {
            return (NativeFileItem) descr.findItemByFileObject(fileObject);
        }
        return null;
    }

    private void checkConfigurationChanged(final Configuration oldConf, final Configuration newConf) {
        if (TRACE) {
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

        MakeConfigurationDescriptor descriptor = getMakeConfigurationDescriptor();
        Item[] items = descriptor.getProjectItems();
        Project proj = descriptor.getProject();

        ConfigurationDescriptorProvider.recordMetrics(ConfigurationDescriptorProvider.USG_PROJECT_CONFIG_CND, descriptor);

        if (oldConf == null) {
            // What else can we do?
            firePropertiesChanged(items, true, true, true);
            MakeLogicalViewProvider.checkForChangedViewItemNodes(proj, null, null);
            MakeLogicalViewProvider.checkForChangedName(proj);
            return;
        }

        // Check compiler collection. Fire if different (IZ 131825)
        if (!oldMConf.getCompilerSet().getName().equals(newMConf.getCompilerSet().getName())
                || !oldMConf.getDevelopmentHost().getExecutionEnvironment().equals(newMConf.getDevelopmentHost().getExecutionEnvironment())) {
            fireFilesPropertiesChanged(); // firePropertiesChanged(getAllFiles(), true);
            MakeLogicalViewProvider.checkForChangedViewItemNodes(proj, null, null);
            if (!oldMConf.getDevelopmentHost().getExecutionEnvironment().equals(newMConf.getDevelopmentHost().getExecutionEnvironment())) {
                MakeLogicalViewProvider.checkForChangedName(proj);
            }
            return;
        }

        CompilerSet oldCompilerSet = oldMConf.getCompilerSet().getCompilerSet();
        CompilerSet newCompilerSet = newMConf.getCompilerSet().getCompilerSet();

        // Check all items
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration oldItemConf = items[i].getItemConfiguration(oldMConf); //ItemConfiguration)oldMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            ItemConfiguration newItemConf = items[i].getItemConfiguration(newMConf); //ItemConfiguration)newMConf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
            if (oldItemConf == null || newItemConf == null) {
                continue;
            }

            if ((newItemConf.getExcluded().getValue() ^ oldItemConf.getExcluded().getValue())
                    && (newItemConf.getTool() == PredefinedToolKind.CCompiler
                    || newItemConf.getTool() == PredefinedToolKind.CCCompiler
                    || items[i].hasHeaderOrSourceExtension(true, true))) {
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

    private void firePropertiesChanged(Item[] items, boolean cFiles, boolean ccFiles, boolean projectChanged) {
        ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>();
        ArrayList<NativeFileItem> deleted = new ArrayList<NativeFileItem>();
        MakeConfiguration conf = getMakeConfiguration();
        // Handle project and file level changes
        for (int i = 0; i < items.length; i++) {
            ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf);
            if (itemConfiguration != null) { // prevent NPE for corrupted projects IZ#174350
                if (itemConfiguration.getExcluded().getValue()) {
                    deleted.add(items[i]);
                    continue;
                }
                if ((cFiles && itemConfiguration.getTool() == PredefinedToolKind.CCompiler)
                        || (ccFiles && itemConfiguration.getTool() == PredefinedToolKind.CCCompiler)
                        || items[i].hasHeaderOrSourceExtension(cFiles, ccFiles)) {
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
        if (TRACE) {
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
    public List<FSPath> getSystemIncludePaths() {
        ArrayList<FSPath> vec = new ArrayList<FSPath>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return vec;
            }
            AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            if (compiler != null) {
                FileSystem compilerFS = FileSystemProvider.getFileSystem(compiler.getExecutionEnvironment());
                vec.addAll(CndFileUtils.toFSPathList(compilerFS, compiler.getSystemIncludeDirectories()));
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
    public List<FSPath> getUserIncludePaths() {
        ArrayList<FSPath> vec = new ArrayList<FSPath>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            CCCompilerConfiguration cccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
            ArrayList<String> vec2 = new ArrayList<String>();
            vec2.addAll(cccCompilerConfiguration.getIncludeDirectories().getValue());
            // Convert all paths to absolute paths
            Iterator<String> iter = vec2.iterator();
            FileSystem fs = getFileSystem();
            while (iter.hasNext()) {
                String path = iter.next();
                if (CndPathUtilitities.isPathAbsolute(path)) {
                    vec.add(new FSPath(fs, path));                    
                } else {
                    path = CndPathUtilitities.toAbsolutePath(getProjectRoot(), path);
                    vec.add(new FSPath(fs, path));
                }
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
        return getProjectDisplayName() + " " + getProjectRoot(); // NOI18N
    }

    private void clearCache() {
        cachedDependency.clear();
    }

    @Override
    public NativeExitStatus execute(String executable, String[] env, String... args) throws IOException {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ExecutionEnvironment ev = makeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        if (ev.isLocal()) {
            String exePath = Path.findCommand(executable);
            if (exePath == null) {
                throw new IOException(getString("NOT_FOUND", executable));  // NOI18N
            }
            List<String> arguments = new ArrayList<String>(args.length + 1);
            arguments.add(exePath);
            arguments.addAll(Arrays.asList(args));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StringBuilder output = new StringBuilder();
           
            try {
                ProcessBuilder pb = new ProcessBuilder(arguments);
                if (env != null) {
                    for(String envEntry: env) {
                        String[] varValuePair = envEntry.split("=");  // NOI18N
                        pb.environment().put(varValuePair[0], varValuePair[1]);
                    }
                }
                Process p = pb.start();
                InputStream is = p.getInputStream();
                InputStreamReader ist = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(ist);
                String line;
                while ((line = br.readLine()) != null) {
                    output.append(line).append("\n"); // NOI18N
                }
                p.waitFor();
                br.close();
                ist.close();
                is.close();
                bos.close();
                return new NativeExitStatus(0, output.toString(), "");
            } catch (IOException ioe) {
                throw ioe;
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        } else {
            ServerRecord record = ServerList.get(ev);
            if (!record.isOnline()) {
                return new NativeExitStatus(-1, "", getString("HOST_OFFLINE", ev.getHost()));
            }
            try {
                // FIXUP: need to handle env!
                ExitStatus exitStatus;
                exitStatus = ProcessUtils.execute(ev, executable, args);
                return new NativeExitStatus(exitStatus.exitCode, exitStatus.output, exitStatus.error);
            }
            catch (Exception e) {
                return new NativeExitStatus(-1, "", e.getMessage());
            }
        }
    }

    @Override
    public String getPlatformName() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        String platformName = makeConfiguration.getDevelopmentHost().getBuildPlatformName();
        return platformName;
    }

    private static String getString(String s, String s2) {
        return NbBundle.getMessage(NativeProjectProvider.class, s, s2);
    }

    @Override
    public NativeFileSearch getNativeFileSearch() {
        NativeFileSearch search = null;
        for (FileProviderFactory fpf : Lookup.getDefault().lookupAll(FileProviderFactory.class)) {
            FileProvider provider = fpf.createFileProvider();
            if (provider instanceof NativeFileSearch) {
                search = (NativeFileSearch) provider;
            }
        }
        return search;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireFilesPropertiesChanged();
    }
}
