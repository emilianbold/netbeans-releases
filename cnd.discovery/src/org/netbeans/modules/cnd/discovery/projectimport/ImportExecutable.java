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
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFunctionDefinitionResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Position;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.support.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.CommonUtilities;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension.ProjectKind;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

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
    private boolean addSourceRoot;
    private List<String> dependencies;
    private CsmModel model;
    private IteratorExtension extension;

    public ImportExecutable(Map<String, Object> map, Project lastSelectedProject, ProjectKind projectKind) {
        this.map = map;
        this.lastSelectedProject = lastSelectedProject;
        this.projectKind = projectKind;
        if (lastSelectedProject == null) {
            createProjectMode = true;
            postCreateProject();
        } else {
            addSourceRoot = true;
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
        String binaryPath = (String) map.get(WizardConstants.PROPERTY_BUILD_RESULT);
        sourcesPath = (String) map.get(WizardConstants.PROPERTY_SOURCE_FOLDER_PATH);
        FSPath projectFolder = (FSPath) map.get(WizardConstants.PROPERTY_PROJECT_FOLDER);
        String projectName = (String) map.get(WizardConstants.PROPERTY_NAME);
        dependencies = (List<String>) map.get(WizardConstants.PROPERTY_DEPENDENCIES);
        String baseDir;
        if (projectFolder != null) {
            projectFolder = new FSPath(projectFolder.getFileSystem(), RemoteFileUtil.normalizeAbsolutePath(projectFolder.getPath(), FileSystemProvider.getExecutionEnvironment(projectFolder.getFileSystem())));
            baseDir = projectFolder.getPath();
            if (projectName == null) {
                projectName = CndPathUtilities.getBaseName(baseDir);
            }
        } else {
            String projectParentFolder = ProjectGenerator.getDefaultProjectFolder();
            if (projectName == null) {
                projectName = ProjectGenerator.getValidProjectName(projectParentFolder, CndPathUtilities.getBaseName(binaryPath));
            }
            ExecutionEnvironment ee = ExecutionEnvironmentFactory.getLocal();
            baseDir = projectParentFolder + File.separator + projectName;
            projectFolder = new FSPath(FileSystemProvider.getFileSystem(ee), RemoteFileUtil.normalizeAbsolutePath(baseDir, ee));
        }
        String hostUID = (String) map.get(WizardConstants.PROPERTY_HOST_UID);
        CompilerSet toolchain = (CompilerSet) map.get(WizardConstants.PROPERTY_TOOLCHAIN);
        boolean defaultToolchain = Boolean.TRUE.equals(map.get(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
        MakeConfiguration conf = MakeConfiguration.createMakefileConfiguration(projectFolder, "Default",  hostUID, toolchain, defaultToolchain); // NOI18N
        String workingDirRel = ProjectSupport.toProperPath(CndPathUtilities.naturalizeSlashes(baseDir),  sourcesPath,
                MakeProjectOptions.getPathMode()); // it's better to pass project source mode here (once full remote is supprted here)
        conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        // Executable
        String exe = binaryPath;
        exe = CndPathUtilities.toRelativePath(CndPathUtilities.naturalizeSlashes(baseDir), exe);
        exe = CndPathUtilities.normalizeSlashes(exe);
        conf.getMakefileConfiguration().getOutput().setValue(exe);
        String exePath = new File(binaryPath).getParentFile().getAbsolutePath();
        exePath = CndPathUtilities.toRelativePath(CndPathUtilities.naturalizeSlashes(baseDir), exePath);
        exePath = CndPathUtilities.normalizeSlashes(exePath);
        conf.getProfile().setRunDirectory(exePath);
        conf.getProfile().setBuildFirst(false);

        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectFolder);
        prjParams.setOpenFlag(false)
                 .setConfiguration(conf)
                 .setImportantFiles(Collections.<String>singletonList(binaryPath).iterator())
                 .setMakefileName(""); //NOI18N
        Boolean trueSourceRoot = (Boolean) map.get(WizardConstants.PROPERTY_TRUE_SOURCE_ROOT);
        if (trueSourceRoot != null && trueSourceRoot.booleanValue()) {
            List<SourceFolderInfo> list = new ArrayList<>();
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
        } else {
            addSourceRoot = true;
        }
        prjParams.setSourceFoldersFilter(MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT);
        try {
            lastSelectedProject = ProjectGenerator.createProject(prjParams);
            OpenProjects.getDefault().addPropertyChangeListener(this);
            map.put(DiscoveryWizardDescriptor.BUILD_RESULT, binaryPath);
            if (sourcesPath != null && sourcesPath.length()>1) {
                 map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, sourcesPath);
            } else {
                map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, lastSelectedProject.getProjectDirectory().getPath()); // NOI18N
            }
            model = CsmModelAccessor.getModel();
            switchModel(model, false, lastSelectedProject);
            extension = Lookup.getDefault().lookup(IteratorExtension.class);
            OpenProjects.getDefault().open(new Project[]{lastSelectedProject}, false);
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
                if (extension != null) {
                    process((DiscoveryExtension)extension);
                }
            }
        }
    }

    public void process(final DiscoveryExtension extension){
        model = CsmModelAccessor.getModel();
        switchModel(model, false, lastSelectedProject);
        Runnable run = new Runnable() {

            @Override
            public void run() {
                try {
                    ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportExecutable.class, "ImportExecutable.Progress")); // NOI18N
                    progress.start();
                    Applicable applicable = null;
                    try {
                        ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                        MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor();
                        applicable = extension.isApplicable(map, lastSelectedProject, true);
                        if (applicable.isApplicable()) {
                            if (sourcesPath == null) {
                                sourcesPath = applicable.getSourceRoot();
                            }
                            if (addSourceRoot && sourcesPath != null && sourcesPath.length()>1) {
                                configurationDescriptor.addSourceRoot(sourcesPath);
                                map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, sourcesPath);
                            }
                            if (!createProjectMode) {
                                resetCompilerSet(configurationDescriptor.getActiveConfiguration(), applicable);
                            }
                            String additionalDependencies = null;
                            if (projectKind == ProjectKind.IncludeDependencies) {
                                additionalDependencies = additionalDependencies(applicable, configurationDescriptor.getActiveConfiguration(),
                                        DiscoveryWizardDescriptor.adaptee(map).getBuildResult());
                                if (additionalDependencies != null && !additionalDependencies.isEmpty()) {
                                    map.put(DiscoveryWizardDescriptor.ADDITIONAL_LIBRARIES, additionalDependencies);
                                }
                            }
                            if (extension.canApply(map, lastSelectedProject, null)) {
                                try {
                                    extension.apply(map, lastSelectedProject, null);
                                    discoverScripts(lastSelectedProject, DiscoveryWizardDescriptor.adaptee(map).getBuildResult());
                                    DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(lastSelectedProject, null);
                                    if (projectKind == ProjectKind.CreateDependencies && (additionalDependencies == null || additionalDependencies.isEmpty())) {
                                        cd = new CreateDependencies(lastSelectedProject, DiscoveryWizardDescriptor.adaptee(map).getDependencies(), dependencies,
                                                DiscoveryWizardDescriptor.adaptee(map).getSearchPaths(), DiscoveryWizardDescriptor.adaptee(map).getBuildResult());
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace(System.err);
                                }
                            }
                        }
                    } finally {
                        progress.finish();
                    }
                    Position mainFunction = applicable.getMainFunction();
                    boolean open = true;
                    if (mainFunction != null) {
                        String mainFilePath = mainFunction.getFilePath();
                        if (sourcesPath != null) {
                            if (!mainFilePath.startsWith(sourcesPath)) {
                                String mainFileName = CndPathUtilities.getBaseName(mainFilePath);
                                NativeProject np = lastSelectedProject.getLookup().lookup(NativeProject.class);
                                List<NativeFileItem> items = new ArrayList<>();
                                if (np != null) {
                                    for(NativeFileItem item : np.getAllFiles()) {
                                        String itemPath = item.getAbsolutePath();
                                        String name = CndPathUtilities.getBaseName(itemPath);
                                        if (name.equals(mainFileName)) {
                                            items.add(item);
                                        }
                                    }
                                }
                                if (items.size() > 0) {
                                    String bestCandidate = null;
                                    int min = Integer.MAX_VALUE;
                                    for(NativeFileItem item : items) {
                                        String candidate = item.getAbsolutePath();
                                        int end = commonEnd(mainFilePath, candidate);
                                        if (end < min) {
                                            bestCandidate = candidate;
                                            min = end;
                                        }
                                    }
                                    mainFilePath = bestCandidate;
                                }
                            }
                        }
                        FileObject toFileObject = CndFileUtils.toFileObject(mainFilePath); // should it be normalized?
                        if (toFileObject != null && toFileObject.isValid()) {
                            if (CsmUtilities.openSource(toFileObject, mainFunction.getLine(), 0)) {
                                open = false;
                            }
                        }

                    }
                    switchModel(model, true, lastSelectedProject);
                    String main = open ? "main": null;  // NOI18N
                    onProjectParsingFinished(main, lastSelectedProject);
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        RP.post(run);
    }
    
    private int commonEnd(String mainFilePath, String candidate) {
        int len = mainFilePath.length() - 1;
        for(int i = candidate.length()-1; i >= 0; i--) {
            char c1 = candidate.charAt(i);
            char c2 = mainFilePath.charAt(len);
            if (c1 != c2) {
                if ((c1 == '\\' || c1 == '/') && (c2 == '\\' || c2 == '/')) {
                    // skip
                } else {
                    break;
                }
            }
            len--;
            if (len < 0) {
                break;
            }
        }
        return len;
    }

    private static void discoverScripts(Project project, String binary) {
        ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (provider == null) {
            return;
        }
        MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor();
        if (configurationDescriptor == null) {
            return;
        }
        MakeConfiguration activeConfiguration = configurationDescriptor.getActiveConfiguration();
        if (activeConfiguration == null) {
            return;
        }
        String root = findFolderPath(configurationDescriptor, getRoot(configurationDescriptor));
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
            String binaryName = CndPathUtilities.getBaseName(binary);
            if (binaryName.indexOf('.') > 0 ) {
                binaryName = binaryName.substring(0, binaryName.lastIndexOf('.'));
            }
            File[] listFiles = rootFile.listFiles();
            if (listFiles != null) {
                for(File file : listFiles) {
                    if (file.isDirectory()) {
                        DiscoveredConfigure childConfigure = scanFolder(file);
                        if (childConfigure.script != null && childConfigure.makefile != null) {
                            if (configure.scriptWeight < childConfigure.scriptWeight) {
                                configure = childConfigure;
                            }
                        } else if (childConfigure.makefile != null && configure.makefile == null && binaryName.equals(file.getName())) {
                            configure.setMakefile(childConfigure.makefile, childConfigure.makefileWeight);
                        }
                    }
                }
            }
        }
        if (configure.makefile != null) {
            activeConfiguration.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(configure.makefile.getParentFile().getAbsolutePath());
            activeConfiguration.getMakefileConfiguration().getBuildCommand().setValue("${MAKE} -f "+configure.makefile.getName()); // NOI18N
            activeConfiguration.getMakefileConfiguration().getCleanCommand().setValue("${MAKE} -f "+configure.makefile.getName()+" clean"); // NOI18N
            Folder externalItemFolder = configurationDescriptor.getExternalItemFolder();
            for(Item item : externalItemFolder.getAllItemsAsArray()){
                if (MIMENames.MAKEFILE_MIME_TYPE.equals(item.getMIMEType())) {
                    externalItemFolder.removeItem(item);
                    break;
                }
            }
            externalItemFolder.addItem(Item.createInFileSystem(configurationDescriptor.getBaseDirFileSystem(), configure.makefile.getAbsolutePath()));
            if (configure.script != null) {
                externalItemFolder.addItem(Item.createInFileSystem(configurationDescriptor.getBaseDirFileSystem(), configure.script.getAbsolutePath()));
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

    private String additionalDependencies(Applicable applicable, MakeConfiguration activeConfiguration, String binary) {
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
            Set<String> checkedDll = new HashSet<>();
            checkedDll.add(binary);
            Map<String,String> dllPaths = new HashMap<>();
            String ldLibPath = CommonUtilities.getLdLibraryPath(activeConfiguration);
            ldLibPath = CommonUtilities.addSearchPaths(ldLibPath, applicable.getSearchPaths(), binary);
            for(String dll : applicable.getDependencies()) {
                dllPaths.put(dll, findLocation(dll, ldLibPath));
            }
            while(true) {
                List<String> secondary = new ArrayList<>();
                for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                    if (entry.getValue() != null) {
                        if (!checkedDll.contains(entry.getValue())) {
                            checkedDll.add(entry.getValue());
                            final Map<String, Object> extMap = new HashMap<>();
                            extMap.put("DW:buildResult", entry.getValue()); // NOI18N
                            if (extension != null) {
                                extension.discoverArtifacts(extMap);
                                @SuppressWarnings("unchecked")
                                List<String> dlls = (List<String>) extMap.get("DW:dependencies"); // NOI18N
                                if (dlls != null) {
                                    for(String so : dlls) {
                                        if (!dllPaths.containsKey(so)) {
                                            secondary.add(so);
                                        }
                                    }
                                    //@SuppressWarnings("unchecked")
                                    //List<String> searchPaths = (List<String>) map.get("DW:searchPaths"); // NOI18N
                                }
                            }
                        }
                    }
                }
                for(String so : secondary) {
                    dllPaths.put(so, findLocation(so, ldLibPath));
                }
                int search = 0;
                for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                    if (entry.getValue() == null) {
                        search++;
                    }
                }
                if (search > 0 && root.length() > 1) {
                    gatherSubFolders(new File(root), new HashSet<String>(), dllPaths);
                }
                int newSearch = 0;
                for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                    if (entry.getValue() == null) {
                        newSearch++;
                    }
                }
                if (newSearch == search && secondary.isEmpty()) {
                    break;
                }
            }
            
            StringBuilder buf = new StringBuilder();
            String binaryDir = CndPathUtilities.getDirName(binary);
            for(Map.Entry<String, String> entry : dllPaths.entrySet()) {
                if (entry.getValue() != null) {
                    if (isMyDll(entry.getValue(), root) || isMyDll(entry.getValue(), binaryDir)) {
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

    private static final List<CsmProgressListener> listeners = new ArrayList<>(1);

    private void onProjectParsingFinished(final String functionName, final Project makeProject) {
        if (makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    final Object id = project.getPlatformProject();
                    if (id != null && id.equals(np)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        listeners.remove(this);
                        if (functionName != null) {
                            Collection<CsmOffsetableDeclaration> decls = CsmFunctionDefinitionResolver.getDefault().findDeclarationByName(project, functionName);
                            CsmOffsetableDeclaration best = null;
                            for(CsmOffsetableDeclaration decl : decls){
                                if (functionName.contentEquals(decl.getName())) {
                                    if (CsmKindUtilities.isFunctionDefinition(decl)) {
                                        best = decl;
                                        break;
                                    }
                                    if (best == null) {
                                        best = decl;
                                    }
                                }
                            }
                            if (best != null) {
                                CsmUtilities.openSource(best);
                            }
                        }
                        DiscoveryProjectGenerator.fixExcludedHeaderFiles(makeProject, ImportProject.logger);
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

    static void switchModel(CsmModel model, boolean state, Project makeProject) {
        if (model != null && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                model.enableProject(np);
            } else {
                model.disableProject(np);
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
            String separator = ":";  // NOI18N
            if (ldPath.indexOf(';') > 0) {
                separator = ";";  // NOI18N
            }
            for(String search : ldPath.split(separator)) {  // NOI18N
                File file = new File(search, dll);
                if (file.isFile() && file.exists()) {
                    String path = file.getAbsolutePath();
                    return path.replace('\\', '/');
                }
            }
        }
        return null;
    }

    static void gatherSubFolders(File startFolder, HashSet<String> set, Map<String,String> result){
        if (!DLL_FILE_SEARCH) {
            return;
        }
        List<File> down = new ArrayList<>();
        down.add(startFolder);
        while(!down.isEmpty()) {
            ArrayList<File> next = new ArrayList<>();
            for (File file : down) {
                if (CndPathUtilities.isIgnoredFolder(file)){
                    continue;
                }
                if (file.exists() && file.isDirectory() && file.canRead()){
                    String canPath;
                    try {
                        canPath = file.getCanonicalPath();
                    } catch (IOException ex) {
                        continue;
                    }
                    if (!set.contains(canPath)){
                        set.add(canPath);
                        File[] fileList = file.listFiles();
                        if (fileList != null) {
                            for (int i = 0; i < fileList.length; i++) {
                                if (fileList[i].isDirectory()) {
                                    next.add(fileList[i]);
                                } else {
                                    String name = fileList[i].getName();
                                    if (result.containsKey(name)) {
                                       result.put(name, fileList[i].getAbsolutePath());
                                       boolean finished = true;
                                       for(String path : result.values()) {
                                           if (path == null || path.isEmpty()) {
                                               finished = false;
                                               break;
                                           }
                                       }
                                       if (finished) {
                                           return;
                                       }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            down = next;
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

    static String findFolderPath(MakeConfigurationDescriptor configurationDescriptor, Folder root) {
        if (root == null) {
            return null;
        }
        if (root.isDiskFolder()) {
            String AbsRootPath = CndPathUtilities.toAbsolutePath(configurationDescriptor.getBaseDirFileObject(), root.getRoot());
            return RemoteFileUtil.normalizeAbsolutePath(AbsRootPath, configurationDescriptor.getProject());
        }
        List<String> candidates = new ArrayList<>();
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
