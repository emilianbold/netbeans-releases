/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.projectimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Position;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.bridge.ProjectBridge;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension.ProjectKind;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public class ImportExecutable implements PropertyChangeListener {
    private static final boolean DLL_FILE_SEARCH = true;
    private static final RequestProcessor RP = new RequestProcessor(ImportExecutable.class.getName(), 2);
    private final Map<String, Object> map;
    private Project lastSelectedProject;
    private final ProjectKind projectKind;
    private CreateDependencies cd;
    private final boolean createProjectMode;
    private String sourcesPath;
    private List<String> dependencies;

    public ImportExecutable(Map<String, Object> map, Project lastSelectedProject, ProjectKind projectKind) {
        this.map = map;
        this.lastSelectedProject = lastSelectedProject;
        this.projectKind = projectKind;
        if (lastSelectedProject == null) {
            createProjectMode = true;
            postCreateProject();
        } else {
            createProjectMode = false;
        }
    }

    private void postCreateProject() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportExecutable.class, "ImportExecutable.Progress.ProjectCreating")); // NOI18N
                progress.start();
                try {
                    createProject();
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    progress.finish();
                }
            }
        });
    }


    @SuppressWarnings("unchecked")
    private void createProject() {
        String binaryPath = (String) map.get(WizardConstants.PROPERTY_BUILD_RESULT); // NOI18N
        sourcesPath = (String) map.get(WizardConstants.PROPERTY_SOURCE_FOLDER_PATH); // NOI18N
        File projectFolder = (File) map.get(WizardConstants.PROPERTY_PROJECT_FOLDER);  // NOI18N;
        String projectName = (String) map.get(WizardConstants.PROPERTY_NAME); // NOI18N
        dependencies = (List<String>) map.get(WizardConstants.PROPERTY_DEPENDENCIES);
        String baseDir;
        if (projectFolder != null) {
            projectFolder = CndFileUtils.normalizeFile(projectFolder);
            baseDir = projectFolder.getAbsolutePath();
            if (projectName == null) {
                projectName = projectFolder.getName();
            }
        } else {
            String projectParentFolder = ProjectGenerator.getDefaultProjectFolder();
            if (projectName == null) {
                projectName = ProjectGenerator.getValidProjectName(projectParentFolder, new File(binaryPath).getName());
            }
            baseDir = projectParentFolder + File.separator + projectName;
            projectFolder = CndFileUtils.createLocalFile(baseDir);
        }
        String hostUID = (String) map.get(WizardConstants.PROPERTY_HOST_UID); // NOI18N
        CompilerSet toolchain = (CompilerSet) map.get(WizardConstants.PROPERTY_TOOLCHAIN); // NOI18N
        MakeConfiguration conf = new MakeConfiguration(projectFolder.getPath(), "Default", MakeConfiguration.TYPE_MAKEFILE, hostUID, toolchain); // NOI18N
        String workingDirRel = ProjectSupport.toProperPath(CndPathUtilitities.naturalize(baseDir),  sourcesPath, 
                MakeProjectOptions.getPathMode()); // it's better to pass project source mode here (once full remote is supprted here)
        conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        // Executable
        String exe = binaryPath;
        exe = CndPathUtilitities.toRelativePath(CndPathUtilitities.naturalize(baseDir), exe);
        exe = CndPathUtilitities.normalize(exe);
        conf.getMakefileConfiguration().getOutput().setValue(exe);
        String exePath = new File(binaryPath).getParentFile().getAbsolutePath();
        exePath = CndPathUtilitities.toRelativePath(CndPathUtilitities.naturalize(baseDir), exePath);
        exePath = CndPathUtilitities.normalize(exePath);
        conf.getProfile().setRunDirectory(exePath);
        conf.getProfile().setBuildFirst(false);

        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectFolder);
        prjParams.setOpenFlag(false)
                 .setConfiguration(conf)
                 .setImportantFiles(Collections.<String>singletonList(binaryPath).iterator());
        Boolean trueSourceRoot = (Boolean) map.get(WizardConstants.PROPERTY_TRUE_SOURCE_ROOT);
        if (trueSourceRoot != null && trueSourceRoot.booleanValue()) {
            List<SourceFolderInfo> list = new ArrayList<SourceFolderInfo>();
            list.add(new SourceFolderInfo() {

                @Override
                public FileObject getFileObject() {
                    return CndFileUtils.toFileObject(CndFileUtils.normalizeAbsolutePath(sourcesPath));
                }

                @Override
                public String getFolderName() {
                    return getFileObject().getNameExt();
                }

                @Override
                public boolean isAddSubfoldersSelected() {
                    return true;
                }
            });
            prjParams.setSourceFolders(list.iterator());
        }
        prjParams.setSourceFoldersFilter(MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT);
        try {
            lastSelectedProject = ProjectGenerator.createProject(prjParams);
            OpenProjects.getDefault().addPropertyChangeListener(this);
            map.put("DW:buildResult", binaryPath); // NOI18N
            map.put("DW:consolidationLevel", "file"); // NOI18N
            map.put("DW:rootFolder", lastSelectedProject.getProjectDirectory().getPath()); // NOI18N
            OpenProjects.getDefault().open(new Project[]{lastSelectedProject}, false);
            OpenProjects.getDefault().setMainProject(lastSelectedProject);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                OpenProjects.getDefault().removePropertyChangeListener(this);
                if (lastSelectedProject == null) {
                    return;
                }
                IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
                if (extension != null) {
                    process((DiscoveryExtension)extension);
                }
            }
        }
    }

    public void process(final DiscoveryExtension extension){
        switchModel(false, lastSelectedProject);
        Runnable run = new Runnable() {

            @Override
            public void run() {
                try {
                    ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getBundle(ImportExecutable.class).getString("ImportExecutable.Progress")); // NOI18N
                    progress.start();
                    Applicable applicable = null;
                    try {
                        ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                        MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor(true);
                        applicable = extension.isApplicable(map, lastSelectedProject);
                        if (applicable.isApplicable()) {
                            if (!createProjectMode) {
                                resetCompilerSet(configurationDescriptor.getActiveConfiguration(), applicable);
                            }
                            String additionalDependencies = null;
                            if (projectKind == ProjectKind.IncludeDependencies) {
                                additionalDependencies = additionalDependencies(applicable, configurationDescriptor.getActiveConfiguration());
                                if (additionalDependencies != null && !additionalDependencies.isEmpty()) {
                                    map.put("DW:libraries", additionalDependencies); // NOI18N
                                }
                            }
                            if (extension.canApply(map, lastSelectedProject)) {
                                try {
                                    extension.apply(map, lastSelectedProject);
                                    discoverScripts(lastSelectedProject);
                                    saveMakeConfigurationDescriptor(lastSelectedProject);
                                    if (projectKind == ProjectKind.CreateDependencies && (additionalDependencies == null || additionalDependencies.isEmpty())) {
                                        cd = new CreateDependencies(lastSelectedProject, DiscoveryWizardDescriptor.adaptee(map).getDependencies(), dependencies);
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    } finally {
                        progress.finish();
                    }
                    Position mainFunction = applicable.getMainFunction();
                    boolean open = true;
                    if (mainFunction != null) {
                        FileObject toFileObject = CndFileUtils.toFileObject(mainFunction.getFilePath()); // should it be normalized?
                        if (toFileObject != null && toFileObject.isValid()) {
                            if (CsmUtilities.openSource(toFileObject, mainFunction.getLine(), 0)) {
                                open = false;
                            }
                        }

                    }
                    switchModel(true, lastSelectedProject);
                    String main = open ? "main": null;  // NOI18N
                    onProjectParsingFinished(main, lastSelectedProject);
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        RP.post(run);
    }

    private static void discoverScripts(Project project) {
        ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (provider == null) {
            return;
        }
        MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor(true);
        if (configurationDescriptor == null) {
            return;
        }
        MakeConfiguration activeConfiguration = configurationDescriptor.getActiveConfiguration();
        if (activeConfiguration == null) {
            return;
        }
        String root = findFolderPath(getRoot(configurationDescriptor));
        if (root == null) {
            return;
        }
        File rootFile = new File(root);
        DiscoveredConfigure configure = scanFolder(rootFile);
        if (configure.script == null || configure.makefile == null) {
            File parentFile = rootFile.getParentFile();
            DiscoveredConfigure parentConfigure = scanFolder(parentFile);
            if (parentConfigure.script != null && parentConfigure.makefile != null) {
                if (configure.scriptWeight < parentConfigure.scriptWeight) {
                    configure = parentConfigure;
                }
            }
        }
        if (configure.script == null || configure.makefile == null) {
            File[] listFiles = rootFile.listFiles();
            if (listFiles != null) {
                for(File file : listFiles) {
                    if (file.isDirectory()) {
                        DiscoveredConfigure childConfigure = scanFolder(file);
                        if (childConfigure.script != null && childConfigure.makefile != null) {
                            if (configure.scriptWeight < childConfigure.scriptWeight) {
                                configure = childConfigure;
                            }
                        }
                    }
                }
            }
        }
        if (configure.makefile != null) {
            activeConfiguration.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(configure.makefile.getParentFile().getAbsolutePath());
            activeConfiguration.getMakefileConfiguration().getBuildCommand().setValue("$(MAKE) -f "+configure.makefile.getName()); // NOI18N
            activeConfiguration.getMakefileConfiguration().getCleanCommand().setValue("$(MAKE) -f "+configure.makefile.getName()+" clean"); // NOI18N
            Folder externalItemFolder = configurationDescriptor.getExternalItemFolder();
            for(Item item : externalItemFolder.getAllItemsAsArray()){
                if (MIMENames.MAKEFILE_MIME_TYPE.equals(item.getMIMEType())) {
                    externalItemFolder.removeItem(item);
                    break;
                }
            }
            externalItemFolder.addItem(new Item(configure.makefile.getAbsolutePath()));
            if (configure.script != null) {
                externalItemFolder.addItem(new Item(configure.script.getAbsolutePath()));
            }
        }
    }


    private static DiscoveredConfigure scanFolder(File rootFile) {
        DiscoveredConfigure configure = new DiscoveredConfigure();
        File[] listFiles = rootFile.listFiles();
        if (listFiles != null) {
            for(File file : listFiles) {
                if (file.isFile()) {
                    String name = file.getName();
                    if ("CMakeLists.txt".equals(name)) { // NOI18N
                        configure.setScript(file, 3);
                    } else if (name.endsWith(".pro")) { // NOI18N
                        configure.setScript(file, 4);
                    } else if ("configure".equals(name) || "configure.exe".equals(name)) { // NOI18N
                        configure.setScript(file, 5);
                    } else if ("Makefile".equals(name)) { // NOI18N
                        configure.setMakefile(file, 5);
                    } else if ("makefile".equals(name)) { // NOI18N
                        configure.setMakefile(file, 4);
                    } else if ("GNUmakefile".equals(name)) { // NOI18N
                        configure.setMakefile(file, 3);
                    } else if (name.endsWith(".mk")) { // NOI18N
                        configure.setMakefile(file, 2);
                    }
                }
            }
        }
        return configure;
    }

    private static final class DiscoveredConfigure {
        private File script;
        private int scriptWeight;
        private File makefile;
        private int makefileWeight;
        private void setScript(File script, int weight) {
            if (this.script == null || scriptWeight < weight) {
                this.script = script;
                scriptWeight = weight;
            }
        }
        private void setMakefile(File makefile, int weight) {
            if (this.makefile == null || makefileWeight < weight) {
                this.makefile = makefile;
                makefileWeight = weight;
            }
        }
    }

    private String additionalDependencies(Applicable applicable, MakeConfiguration activeConfiguration) {
        if (dependencies == null) {
            String root = sourcesPath;
            if (root == null || root.length()==0) {
                root = applicable.getSourceRoot();
            }
            if (root == null || root.length() == 0) {
                return null;
            }
            if (applicable.getDependencies() == null || applicable.getDependencies().isEmpty()) {
                return null;
            }
            Map<String,String> dllPaths = new HashMap<String, String>();
            String ldLibPath = getLdLibraryPath(activeConfiguration);
            boolean search = false;
            for(String dll : applicable.getDependencies()) {
                String p = findLocation(dll, ldLibPath);
                if (p != null) {
                    dllPaths.put(dll, p);
                } else {
                    search = true;
                    dllPaths.put(dll, null);
                }
            }
            if (search) {
                gatherSubFolders(new File(root), new HashSet<String>(), dllPaths);
            }
            StringBuilder buf = new StringBuilder();
            for(Map.Entry<String, String> entry : dllPaths.entrySet()) {
                if (entry.getValue() != null) {
                    if (isMyDll(entry.getValue(), root)) {
                        if (buf.length() > 0) {
                            buf.append(';');
                        }
                        buf.append(entry.getValue());
                    }
                }
            }
            return buf.toString();
        } else {
            StringBuilder buf = new StringBuilder();
            for(String path : dependencies) {
                if (buf.length() > 0) {
                    buf.append(';');
                }
                buf.append(path);
            }
            return buf.toString();
        }
    }

    static String getLdLibraryPath(MakeConfiguration activeConfiguration) {
        String ldLibraryPathName = getLdLibraryPathName(activeConfiguration);
        String ldLibPath = activeConfiguration.getProfile().getEnvironment().getenv(ldLibraryPathName); // NOI18N
        ExecutionEnvironment eenv = activeConfiguration.getDevelopmentHost().getExecutionEnvironment();
        if (ldLibPath != null) {
            try {
                ldLibPath = MacroExpanderFactory.getExpander(eenv).expandMacros(ldLibPath, HostInfoUtils.getHostInfo(eenv).getEnvironment());
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (ldLibPath == null) {
            ldLibPath = HostInfoProvider.getEnv(eenv).get(ldLibraryPathName); // NOI18N
        }
        return ldLibPath;
    }

    private static String getLdLibraryPathName(MakeConfiguration conf) {
        switch (conf.getDevelopmentHost().getBuildPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
                PlatformInfo pi = conf.getPlatformInfo();
                return pi.getPathName();
            case PlatformTypes.PLATFORM_MACOSX:
                return "DYLD_LIBRARY_PATH"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
            case PlatformTypes.PLATFORM_LINUX:
            default:
                return "LD_LIBRARY_PATH"; // NOI18N
        }
    }

    private static final List<CsmProgressListener> listeners = new ArrayList<CsmProgressListener>(1);

    private void onProjectParsingFinished(final String functionName, final Project makeProject) {
        if (makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.getPlatformProject().equals(np)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        listeners.remove(this);
                        if (project instanceof ProjectBase && functionName != null) {
                            String from = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + ':' + functionName + '('; // NOI18N
                            Collection<CsmOffsetableDeclaration> decls = ((ProjectBase)project).findDeclarationsByPrefix(from);
                            for(CsmOffsetableDeclaration decl : decls){
                                CsmUtilities.openSource(decl);
                                break;
                            }
                        }
                        fixExcludedHeaderFiles(makeProject);
                        if (cd != null) {
                            cd.create();
                        }
                    }
                }
            };
            listeners.add(listener);
            CsmListeners.getDefault().addProgressListener(listener);
        }
    }

    static void switchModel(boolean state, Project makeProject) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                ((ModelImpl) model).enableProject(np);
            } else {
                ((ModelImpl) model).disableProject(np);
            }
        }
    }

    private static void postModelDiscovery(final Project makeProject) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            final CsmProject p = model.getProject(np);
            if (p == null) {
                return;
            }
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.equals(p)) {
                        try {
                            listeners.remove(this);
                            CsmListeners.getDefault().removeProgressListener(this); // ignore java warning "usage of this in anonymous class"
                            fixExcludedHeaderFiles(makeProject);
                        } catch (Throwable ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            };
            CsmListeners.getDefault().addProgressListener(listener);
            listeners.add(listener);
        }
    }

    private static void fixExcludedHeaderFiles(Project makeProject) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            final CsmProject p = model.getProject(np);
            if (p != null && np != null) {
                Set<String> needCheck = new HashSet<String>();
                Set<String> needAdd = new HashSet<String>();
                Map<String,Item> normalizedItems = ImportProject.initNormalizedNames(makeProject);
                for (CsmFile file : p.getAllFiles()) {
                    if (file instanceof FileImpl) {
                        FileImpl impl = (FileImpl) file;
                        NativeFileItem item = impl.getNativeFileItem();
                        if (item == null) {
                            String path = CndFileUtils.normalizeFile(impl.getFile()).getAbsolutePath();
                            item = normalizedItems.get(path);
                        }
                        boolean isLineDirective = false;
                        if (!file.isSourceFile() && (p instanceof ProjectBase)) {
                            ProjectBase pb = (ProjectBase) p;
                            Set<CsmFile> parentFiles = pb.getGraph().getParentFiles(file);
                            if (parentFiles.isEmpty()) {
                                isLineDirective = true;
                            }
                        }
                        if (item != null && np.equals(item.getNativeProject()) && item.isExcluded()) {
                            if (item instanceof Item) {
                                ProjectBridge.setExclude((Item) item, false);
                                if (file.isHeaderFile()) {
                                    needCheck.add(item.getFile().getAbsolutePath());
                                }
                            }
                        } else if (isLineDirective && item != null && np.equals(item.getNativeProject()) && !item.isExcluded()) {
                            if (item instanceof Item) {
                                ProjectBridge.setExclude((Item) item, true);
                            }
                        } else if (item == null) {
                            // It should be in project?
                            if (file.isHeaderFile()) {
                                String path = CndFileUtils.normalizeFile(impl.getFile()).getAbsolutePath();
                                needAdd.add(path);
                            }
                        }
                    }
                }
                if (needCheck.size() > 0 || needAdd.size() > 0) {
                    ProjectBridge bridge = new ProjectBridge(makeProject);
                    if (bridge.isValid()) {
                        if (needAdd.size() > 0) {
                            Map<String, Folder> prefferedFolders = bridge.prefferedFolders();
                            for(String path : needAdd) {
                                String name = path;
                                if (Utilities.isWindows()) {
                                    path = path.replace('\\', '/');
                                }
                                int i = path.lastIndexOf('/');
                                if (i >= 0){
                                    String folderPath = path.substring(0,i);
                                    Folder prefferedFolder = prefferedFolders.get(folderPath);
                                    if (prefferedFolder != null) {
                                        Item item = bridge.createItem(name);
                                        item = prefferedFolder.addItem(item);
                                        bridge.setHeaderTool(item);
                                        if(!MIMENames.isCppOrCOrFortran(item.getMIMEType())){
                                            needCheck.add(path);
                                        }
                                    }
                                }
                            }
                        }
                        if (needCheck.size() > 0) {
                            bridge.checkForNewExtensions(needCheck);
                        }
                    }
                }
                saveMakeConfigurationDescriptor(makeProject);
            }
        }
    }

    static void resetCompilerSet(MakeConfiguration configuration, Applicable applicable){
        if (configuration != null) {
            CompilerSetManager manager = CompilerSetManager.get(configuration.getDevelopmentHost().getExecutionEnvironment());
            if (applicable.isSunStudio()) {
                CompilerSet def = manager.getDefaultCompilerSet();
                if (def != null && def.getCompilerFlavor().isSunStudioCompiler()) {
                    return;
                }
                List<CompilerSet> compilerSets = CompilerSetManager.get(configuration.getDevelopmentHost().getExecutionEnvironment()).getCompilerSets();
                def = null;
                for(CompilerSet set : compilerSets) {
                    if (set.getCompilerFlavor().isSunStudioCompiler()) {
                        if ("OracleSolarisStudio".equals(set.getName())) { // NOI18N
                            def = set;
                        }
                        if (def == null) {
                            def = set;
                        }
                    }
                }
                if (def != null) {
                    configuration.getCompilerSet().setValue(def.getName());
                }
            } else {
                // It seems GNU compiler. applicable.getCompilerName() contains a producer information.
                // Unfortunately producer field does not give information about flavor, only compiler version
                CompilerSet def = manager.getDefaultCompilerSet();
                if (def != null && !def.getCompilerFlavor().isSunStudioCompiler()) {
                    return;
                }
                List<CompilerSet> compilerSets = CompilerSetManager.get(configuration.getDevelopmentHost().getExecutionEnvironment()).getCompilerSets();
                def = null;
                for(CompilerSet set : compilerSets) {
                    if (!set.getCompilerFlavor().isSunStudioCompiler()) {
                        if (def == null) {
                            def = set;
                        }
                    }
                }
                if (def != null) {
                    configuration.getCompilerSet().setValue(def.getName());
                }
            }
        }
    }

    static void saveMakeConfigurationDescriptor(Project lastSelectedProject) {
        ConfigurationDescriptorProvider pdp = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (makeConfigurationDescriptor != null) {
            makeConfigurationDescriptor.setModified();
            makeConfigurationDescriptor.save();
            makeConfigurationDescriptor.checkForChangedItems(lastSelectedProject, null, null);
        }
    }

    static boolean isMyDll(String path, String root) {
        if (path.startsWith(root)) {
            return true;
        } else {
            String[] p1 = path.split("/");  // NOI18N
            String[] p2 = root.split("/");  // NOI18N
            for(int i = 0; i < Math.min(p1.length - 1, p2.length); i++) {
                if (!p1[i].equals(p2[i])) {
                    if (i > 2) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    static String findLocation(String dll, String ldPath){
        if (ldPath != null) {
            for(String search : ldPath.split(":")) {  // NOI18N
                File file = new File(search, dll);
                if (file.isFile() && file.exists()) {
                    String path = file.getAbsolutePath();
                    return path.replace('\\', '/');
                }
            }
        }
        return null;
    }

    static void gatherSubFolders(File d, HashSet<String> set, Map<String,String> result){
        if (!DLL_FILE_SEARCH) {
            return;
        }
        if (d.exists() && d.isDirectory() && d.canRead()){
            String path = d.getAbsolutePath();
            path = path.replace('\\', '/'); // NOI18N
            if (!set.contains(path)){
                set.add(path);
                File[] ff = d.listFiles();
                for (int i = 0; i < ff.length; i++) {
                    try {
                        String canPath = ff[i].getCanonicalPath();
                        String absPath = ff[i].getAbsolutePath();
                        if (!absPath.equals(canPath) && absPath.startsWith(canPath)) {
                            continue;
                        }
                    } catch (IOException ex) {
                        //Exceptions.printStackTrace(ex);
                    }
                    String name = ff[i].getName();
                    if (result.containsKey(name)) {
                       result.put(name, ff[i].getAbsolutePath());
                    }
                    gatherSubFolders(ff[i], set, result);
                }
            }
        }
    }

    static Folder getRoot(MakeConfigurationDescriptor configurationDescriptor) {
        Folder folder = configurationDescriptor.getLogicalFolders();
        List<Folder> sources = folder.getFolders();
        for (Folder sub : sources){
            if (sub.isProjectFiles()) {
                if (MakeConfigurationDescriptor.SOURCE_FILES_FOLDER.equals(sub.getName())) {
                    return sub;
                } else if (MakeConfigurationDescriptor.HEADER_FILES_FOLDER.equals(sub.getName()) ||
                           MakeConfigurationDescriptor.RESOURCE_FILES_FOLDER.equals(sub.getName())){
                    // skip
                } else {
                    return sub;
                }
            }
        }
        return folder;
    }

    static String findFolderPath(Folder root) {
        if (root == null) {
            return null;
        }
        if (root.isDiskFolder()) {
            return root.getRoot();
        }
        List<String> candidates = new ArrayList<String>();
        for (Object o : root.getElements()) {
            if (o instanceof Folder) {
                Folder f = (Folder) o;
                String res = findFolderPath(f, "/"+f.getName()); // NOI18N
                if (res != null) {
                    candidates.add(res+"/"+f.getName()); // NOI18N
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        } else if (candidates.size() == 1) {
            return candidates.get(0);
        }
        String bestCandidate = null;
        for(String candidate : candidates) {
            if (bestCandidate == null) {
                bestCandidate = candidate;
            } else {
                if (bestCandidate.startsWith(candidate)) {
                    bestCandidate = candidate;
                } else {
                    if (bestCandidate.length() > candidate.length()) {
                        bestCandidate = candidate;
                    }
                }
            }
        }
        return bestCandidate;
    }

    static String findFolderPath(Folder root, String prefix) {
        for (Object o : root.getElements()) {
            if (o instanceof Item) {
                Item i = (Item) o;
                String path = i.getAbsPath();
                if (!prefix.isEmpty()) {
                    path = path.replace('\\', '/'); // NOI18N
                    int j = path.indexOf(prefix+"/"); // NOI18N
                    if (j >= 0) {
                        return path.substring(0,j);
                    }
                }
            }
        }
        for (Object o : root.getElements()) {
            if (o instanceof Folder) {
                Folder f = (Folder) o;
                String res = findFolderPath(f, prefix+"/"+f.getName()); // NOI18N
                if (res != null) {
                    if (prefix.isEmpty()) {
                        return res+"/"+f.getName(); // NOI18N
                    } else {
                        return res;
                    }
                }
            }
        }
        return null;
    }
}
