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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.projectimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.actions.CMakeAction;
import org.netbeans.modules.cnd.actions.MakeAction;
import org.netbeans.modules.cnd.actions.QMakeAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.builds.CMakeExecSupport;
import org.netbeans.modules.cnd.builds.ImportUtils;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;
import org.netbeans.modules.cnd.discovery.buildsupport.BuildTraceSupport;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ConsolidationStrategy;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.support.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.discovery.wizard.support.impl.DiscoveryProjectGeneratorImpl;
import org.netbeans.modules.cnd.execution.ExecutionSupport;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.SnapShot;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.remote.api.RfsListener;
import org.netbeans.modules.cnd.remote.api.RfsListenerSupport;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class ImportProject implements PropertyChangeListener {

    private static final String BUILD_COMMAND = "${MAKE} -f Makefile";  // NOI18N
    private static final String CLEAN_COMMAND = "${MAKE} -f Makefile clean";  // NOI18N
    private static final boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    public static final Logger logger;
    static {
        logger = Logger.getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportProject"); // NOI18N
        if (TRACE) {
            logger.setLevel(Level.ALL);
        }
    }
    
    private static final RequestProcessor RP = new RequestProcessor(ImportProject.class.getName(), 2);
    private static final RequestProcessor RPR = new RequestProcessor(ImportProject.class.getName(), 1);
    private final String nativeProjectPath;
    private final FileObject nativeProjectFO;
    private final FSPath projectFolder;
    private String projectName;
    private String makefilePath;
    private String configurePath;
    private String configureRunFolder;
    private String configureArguments;
    private boolean runConfigure = false;
    private boolean manualCA = false;
    private boolean buildArifactWasAnalyzed = false;
    private final String hostUID;
    private final ExecutionEnvironment executionEnvironment;
    private final ExecutionEnvironment fileSystemExecutionEnvironment;
    private final MakeProjectOptions.PathMode pathMode;
    private CompilerSet toolchain;
    private boolean defaultToolchain;
    private String workingDir;
    private String buildCommand = BUILD_COMMAND;
    private String cleanCommand = CLEAN_COMMAND;
    private String buildResult = "";  // NOI18N
    private File existingBuildLog = null;
    private Project makeProject;
    private boolean runMake;
    private String includeDirectories = ""; // NOI18N
    private String macros = ""; // NOI18N
    private String consolidationStrategy = ConsolidationStrategy.FILE_LEVEL;
    private Iterator<SourceFolderInfo> sources;
    private Iterator<SourceFolderInfo> tests;
    private String sourceFoldersFilter = null;
    private FileObject configureFileObject;
    private Map<Step, State> importResult = new EnumMap<Step, State>(Step.class);
    private final CountDownLatch waitSources = new CountDownLatch(1);
    private final AtomicInteger openState = new AtomicInteger(0);
    private Interrupter interrupter;

    public ImportProject(WizardDescriptor wizard) {
        pathMode = MakeProjectOptions.getPathMode();
        projectFolder = (FSPath) wizard.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER);
        nativeProjectPath = (String) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_DIR);
        nativeProjectFO = (FileObject) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_FO);
        if (Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE))) { // NOI18N
            simpleSetup(wizard);
        } else {
            customSetup(wizard);
        }
        hostUID = (String) wizard.getProperty(WizardConstants.PROPERTY_HOST_UID); // NOI18N
        if (hostUID == null) {
            executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        } else {
            executionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
        }
        fileSystemExecutionEnvironment = ExecutionEnvironmentFactory.getLocal();
        assert nativeProjectPath != null;
    }

    private void simpleSetup(WizardDescriptor wizard) {
        projectName = CndPathUtilities.getBaseName(projectFolder.getPath());
        workingDir = nativeProjectPath;
        configurePath = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH); 
        if (configurePath != null) {
            configureArguments = (String) wizard.getProperty("realFlags");  // NOI18N
            runConfigure = true;
            // the best guess
            makefilePath = (String) wizard.getProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH); 
            if (makefilePath == null) {
                makefilePath = nativeProjectPath + "/Makefile"; // NOI18N;
            }
            configureRunFolder = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER);
        } else {
            makefilePath = (String) wizard.getProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH); 
        }
        runMake = Boolean.TRUE.equals(wizard.getProperty("buildProject"));  // NOI18N
        toolchain = (CompilerSet)wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        defaultToolchain = Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
        
        List<SourceFolderInfo> list = new ArrayList<SourceFolderInfo>();
        list.add(new SourceFolderInfo() {

            @Override
            public FileObject getFileObject() {
                return nativeProjectFO;
            }

            @Override
            public String getFolderName() {
                return nativeProjectFO.getNameExt();
            }

            @Override
            public boolean isAddSubfoldersSelected() {
                return true;
            }
        });
        sources = list.iterator();
        sourceFoldersFilter = MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT;
    }

    private void customSetup(WizardDescriptor wizard) {
        projectName = (String) wizard.getProperty(WizardConstants.PROPERTY_NAME);
        workingDir = (String) wizard.getProperty(WizardConstants.PROPERTY_WORKING_DIR);
        buildCommand = (String) wizard.getProperty(WizardConstants.PROPERTY_BUILD_COMMAND);
        cleanCommand = (String) wizard.getProperty(WizardConstants.PROPERTY_CLEAN_COMMAND);
        buildResult = (String) wizard.getProperty(WizardConstants.PROPERTY_BUILD_RESULT); 
        includeDirectories = (String) wizard.getProperty(WizardConstants.PROPERTY_INCLUDES); 
        macros = (String) wizard.getProperty(WizardConstants.PROPERTY_MACROS); 
        makefilePath = (String) wizard.getProperty(WizardConstants.PROPERTY_USER_MAKEFILE_PATH); 
        configurePath = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH);
        configureRunFolder = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_RUN_FOLDER);
        configureArguments = (String) wizard.getProperty(WizardConstants.PROPERTY_CONFIGURE_SCRIPT_ARGS);
        consolidationStrategy = (String) wizard.getProperty(WizardConstants.PROPERTY_CONSOLIDATION_LEVEL); 
        @SuppressWarnings("unchecked")
        Iterator<SourceFolderInfo> it = (Iterator<SourceFolderInfo>) wizard.getProperty(WizardConstants.PROPERTY_SOURCE_FOLDERS); 
        sources = it;
        @SuppressWarnings("unchecked")
        Iterator<SourceFolderInfo> it2 = (Iterator<SourceFolderInfo>) wizard.getProperty(WizardConstants.PROPERTY_TEST_FOLDERS); 
        tests = it2;
        sourceFoldersFilter = (String) wizard.getProperty(WizardConstants.PROPERTY_SOURCE_FOLDERS_FILTER);
        runConfigure = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_RUN_CONFIGURE)); // NOI18N
        if (runConfigure) {
            runMake = true;
        } else {
            runMake = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_RUN_REBUILD)); // NOI18N
        }
        String path = (String)wizard.getProperty(WizardConstants.PROPERTY_BUILD_LOG);
        if (path != null && !path.isEmpty()) {
            existingBuildLog = new File(path);
        }
        manualCA = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_MANUAL_CODE_ASSISTANCE)); // NOI18N
        toolchain = (CompilerSet)wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        defaultToolchain = Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
    }

    public Set<FileObject> create() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        MakeConfiguration extConf = MakeConfiguration.createConfiguration(projectFolder, "Default", MakeConfiguration.TYPE_MAKEFILE, null, hostUID, toolchain, defaultToolchain); // NOI18N
        String workingDirRel = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilities.naturalizeSlashes(workingDir), pathMode);
        workingDirRel = CndPathUtilities.normalizeSlashes(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommand().setValue(buildCommand);
        extConf.getMakefileConfiguration().getCleanCommand().setValue(cleanCommand);
        // Build result
        if (buildResult != null && buildResult.length() > 0) {
            buildResult = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilities.naturalizeSlashes(buildResult), pathMode);
            buildResult = CndPathUtilities.normalizeSlashes(buildResult);
            extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
        }
        extConf.getProfile().setRunDirectory(workingDirRel);       
        extConf.getProfile().setBuildFirst(false);
        // Include directories
        if (includeDirectories != null && includeDirectories.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(includeDirectories, ";"); // NOI18N
            List<String> includeDirectoriesVector = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                String includeDirectory = tokenizer.nextToken();
                includeDirectory = CndPathUtilities.toRelativePath(projectFolder.getPath(), CndPathUtilities.naturalizeSlashes(includeDirectory));
                includeDirectory = CndPathUtilities.normalizeSlashes(includeDirectory);
                includeDirectoriesVector.add(includeDirectory);
            }
            extConf.getCCompilerConfiguration().getIncludeDirectories().setValue(includeDirectoriesVector);
            extConf.getCCCompilerConfiguration().getIncludeDirectories().setValue(new ArrayList<String>(includeDirectoriesVector));
        }
        // Macros
        if (macros != null && macros.length() > 0) {
            StringTokenizer tokenizer = new StringTokenizer(macros, "; "); // NOI18N
            ArrayList<String> list = new ArrayList<String>();
            while (tokenizer.hasMoreTokens()) {
                list.add(tokenizer.nextToken());
            }
            // FIXUP
            extConf.getCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
            extConf.getCCCompilerConfiguration().getPreprocessorConfiguration().getValue().addAll(list);
        }
        // Add makefile and configure script to important files
        ArrayList<String> importantItems = new ArrayList<String>();
        if (makefilePath != null && makefilePath.length() > 0) {
            makefilePath = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilities.naturalizeSlashes(makefilePath), pathMode);
            makefilePath = CndPathUtilities.normalizeSlashes(makefilePath);
        }
        if (configurePath != null && configurePath.length() > 0) {
            String normPath = RemoteFileUtil.normalizeAbsolutePath(configurePath, fileSystemExecutionEnvironment);
            configureFileObject = RemoteFileUtil.getFileObject(normPath, fileSystemExecutionEnvironment);
            configurePath = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilities.naturalizeSlashes(configurePath), pathMode);
            configurePath = CndPathUtilities.normalizeSlashes(configurePath);
            importantItems.add(configurePath);
        }
        Iterator<String> importantItemsIterator = importantItems.iterator();
        if (!importantItemsIterator.hasNext()) {
            importantItemsIterator = null;
        }
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectFolder);
        prjParams
                .setConfiguration(extConf)
                .setSourceFolders(Collections.<SourceFolderInfo>emptyList().iterator())
//                .setSourceFolders(sources)
                .setSourceFoldersFilter(sourceFoldersFilter)
                .setTestFolders(tests)
                .setImportantFiles(importantItemsIterator)
                .setFullRemoteNativeProjectPath(nativeProjectPath)
                .setHostUID(hostUID);
        if (makefilePath != null) {
            prjParams.setMakefileName(makefilePath);
        } else {
            prjParams.setMakefileName(""); //NOI18N
        }
        makeProject = ProjectGenerator.createProject(prjParams);
        FileObject dir = projectFolder.getFileObject();
        importResult.put(Step.Project, State.Successful);
        switchModel(false);
        resultSet.add(dir);
        OpenProjects.getDefault().addPropertyChangeListener(this);
        return resultSet;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (openState.get() == 0) {
            if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                if (evt.getNewValue() instanceof Project[]) {
                    Project[] projects = (Project[])evt.getNewValue();
                    if (projects.length == 0) {
                        return;
                    }
                    interrupter = new Interrupter() {

                        @Override
                        public boolean cancelled() {
                            return !isProjectOpened();
                        }
                    };
                    openState.incrementAndGet();
                    RP.post(new Runnable() {

                        @Override
                        public void run() {
                            doWork();
                        }
                    });
                }
            }
        } else if (openState.get() == 1) {
            if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                if (evt.getNewValue() instanceof Project[]) {
                    Project[] projects = (Project[])evt.getNewValue();
                    for(Project p : projects) {
                        if (p == makeProject) {
                            return;
                        }
                    }
                    openState.incrementAndGet();
                    OpenProjects.getDefault().removePropertyChangeListener(this);
                }
            }
        }
    }

    boolean isProjectOpened() {
        return openState.get() == 1;
    }

    private void doWork() {
        try {
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.getConfigurationDescriptor();
            if (pdp.gotDescriptor()) {
                final MakeConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
                if (sources != null) {
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportProject.class, "ImportProject.Progress.AnalyzeRoot"));
                            handle.start();
                            while(sources.hasNext()) {
                                SourceFolderInfo next = sources.next();
                                configurationDescriptor.addFilesFromRoot(configurationDescriptor.getLogicalFolders(), next.getFileObject(), handle, interrupter, true, Folder.Kind.SOURCE_DISK_FOLDER, null);
                            }
                            handle.finish();
                            waitSources.countDown();
                        }
                    });
                } else {
                    waitSources.countDown();
                }
                if (configurationDescriptor.getActiveConfiguration() != null) {
                    if (runConfigure && configurePath != null && configurePath.length() > 0 &&
                            configureFileObject != null && configureFileObject.isValid()) {
                        waitSources.await(); // or should it be waitConfigurationDescriptor() ?
                        postConfigure();
                    } else {
                        if (runMake) {
                            makeProject(null);
                        } else {
                            discovery(1, existingBuildLog, null);
                        }
                    }
                } else {
                    isFinished = true;
                }
            } else {
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

//    private void parseConfigureLog(File configureLog){
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(configureLog));
//            while (true) {
//                String line;
//                line = reader.readLine();
//                if (line == null) {
//                    break;
//                }
//            }
//            reader.close();
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
    static File createTempFile(String prefix) {
        try {
            File file = File.createTempFile(prefix, ".log"); // NOI18N
            file.deleteOnExit();
            return file;
        } catch (IOException ex) {
            return null;
        }
    }

    private void postConfigure() {
        try {
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            if (configureLog == null) {
                configureLog = createTempFile("configure"); // NOI18N
            }
            Writer outputListener = null;
            try {
                outputListener = new BufferedWriter(new FileWriter(configureLog));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            DataObject dObj = DataObject.find(configureFileObject);
            Node node = dObj.getNodeDelegate();
            String mime = FileUtil.getMIMEType(configureFileObject);
            // Add arguments to configure script?
            if (configureArguments != null) {
                if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
                    ShellExecSupport ses = node.getLookup().lookup(ShellExecSupport.class);
                    try {
                        // Keep user arguments as is in args[0]
                        ses.setArguments(new String[]{configureArguments});
                        // duplicate configure variables in environment
                        List<String> vars = ImportUtils.parseEnvironment(configureArguments);
                        ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                        if (configureRunFolder != null) {
                            FileObject createdFolder = mkDir(configureFileObject.getParent(), configureRunFolder);
                            if (createdFolder != null) {
                                ses.setRunDirectory(createdFolder.getPath());
                            }
                        } else {
                            ses.setRunDirectory(configureFileObject.getParent().getPath());
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
                    CMakeExecSupport ses = node.getLookup().lookup(CMakeExecSupport.class);
                    try {
                        // extract configure variables in environment
                        List<String> vars = ImportUtils.parseEnvironment(configureArguments);
                        for (String s : ImportUtils.quoteList(vars)) {
                            int i = configureArguments.indexOf(s);
                            if (i >= 0){
                                configureArguments = configureArguments.substring(0, i) + configureArguments.substring(i + s.length());
                            }
                        }
                        ses.setArguments(new String[]{configureArguments});
                        ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                        if (configureRunFolder != null) {
                            FileObject createdFolder = mkDir(configureFileObject.getParent(), configureRunFolder);
                            if (createdFolder != null) {
                                ses.setRunDirectory(createdFolder.getPath());
                            }
                        } else {
                            ses.setRunDirectory(configureFileObject.getParent().getPath());
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
                    QMakeExecSupport ses = node.getLookup().lookup(QMakeExecSupport.class);
                    try {
                        ses.setArguments(new String[]{configureArguments});
                        if (configureRunFolder != null) {
                            FileObject createdFolder = mkDir(configureFileObject.getParent(), configureRunFolder);
                            if (createdFolder != null) {
                                ses.setRunDirectory(createdFolder.getPath());
                            }
                        } else {
                            ses.setRunDirectory(configureFileObject.getParent().getPath());
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            // If no makefile, create empty one so it shows up in Interesting Files
            //if (!makefileFile.exists()) {
            //    makefileFile.createNewFile();
            //}
            //final File configureLog = createTempFile("configure");
            ExecutionListener listener = new ExecutionListener() {
                private RfsListenerImpl listener;

                @Override
                public void executionStarted(int pid) {
                    if (executionEnvironment.isRemote()) {
                        listener = new RfsListenerImpl(executionEnvironment);
                        RfsListenerSupport.addListener(executionEnvironment, listener);
                    }
                }

                @Override
                public void executionFinished(int rc) {
                    if (rc == 0) {
                        importResult.put(Step.Configure, State.Successful);
                    } else {
                        importResult.put(Step.Configure, State.Fail);
                    }
                    if (listener != null) {
                        listener.download();
                        RfsListenerSupport.removeListener(executionEnvironment, listener);
                    }
                    if (runMake && rc == 0) {
                        //parseConfigureLog(configureLog);
                        // when run scripts we do full "clean && build" to
                        // remove old build artifacts as well
                        makeProject(configureLog);
                    } else {
                        switchModel(true);
                        postModelDiscovery(true);
                    }
                }
            };
            if (TRACE) {
                logger.log(Level.INFO, "#{0} {1}", new Object[]{configureFileObject, configureArguments}); // NOI18N
            }
            if (MIMENames.SHELL_MIME_TYPE.equals(mime)){
                Future<Integer> task = ShellRunAction.performAction(node, listener, outputListener, makeProject, null);
                if (task == null) {
                    throw new Exception("Cannot execute configure script"); // NOI18N
                }
            } else if (MIMENames.CMAKE_MIME_TYPE.equals(mime)){
                Future<Integer> task = CMakeAction.performAction(node, listener, null, makeProject, null);
                if (task == null) {
                    throw new Exception("Cannot execute cmake"); // NOI18N
                }
            } else if (MIMENames.QTPROJECT_MIME_TYPE.equals(mime)){
                Future<Integer> task = QMakeAction.performAction(node, listener, null, makeProject, null);
                if (task == null) {
                    throw new Exception("Cannot execute qmake"); // NOI18N
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#Configure script does not supported"); // NOI18N
                }
                importResult.put(Step.Configure, State.Fail);
                importResult.put(Step.MakeClean, State.Skiped);
                switchModel(true);
                postModelDiscovery(true);
            }
        } catch (Throwable e) {
            logger.log(Level.INFO, "Cannot configure project", e); // NOI18N
            importResult.put(Step.Configure, State.Fail);
            importResult.put(Step.MakeClean, State.Skiped);
            switchModel(true);
            postModelDiscovery(true);
        }
    }

    private FileObject mkDir(FileObject parent, String relative) {
         if (relative != null) {
            try {
                relative = relative.replace('\\', '/'); // NOI18N
                for(String segment : relative.split("/")) { // NOI18N
                    if (parent == null) {
                        return null;
                    }
                    if (segment.isEmpty()) {
                        continue;
                    } else if (".".equals(segment)) { // NOI18N
                        continue;
                    } else if ("..".equals(segment)) { // NOI18N
                        parent = parent.getParent();
                    } else {
                        FileObject test = parent.getFileObject(segment,null);
                        if (test != null) {
                            parent = test;
                        } else {
                            parent = parent.createFolder(segment);
                        }

                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
             return parent;
         }
         return null;
    }

    private void downloadRemoteFile(File file){
        if (file != null && !file.exists()) {
            if (executionEnvironment.isRemote()) {
                String remoteFile = HostInfoProvider.getMapper(executionEnvironment).getRemotePath(file.getAbsolutePath());
                try {
                    if (HostInfoUtils.fileExists(executionEnvironment, remoteFile)){
                        Future<Integer> task = CommonTasksSupport.downloadFile(remoteFile, executionEnvironment, file.getAbsolutePath(), null);
                        if (TRACE) {
                            logger.log(Level.INFO, "#download file {0}", file.getAbsolutePath()); // NOI18N
                        }
                        /*int rc =*/ task.get();
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private static final String configureCteatePattern = " creating "; // NOI18N
    private void scanConfigureLog(File logFile){
        if (logFile != null && logFile.exists() && logFile.canRead()){
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(logFile));
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    int i = line.indexOf(configureCteatePattern);
                    if (i > 0) {
                        String f = line.substring(i+configureCteatePattern.length()).trim();
                        //if (f.endsWith(".h")) { // NOI18N
                            downloadRemoteFile(CndFileUtils.createLocalFile(projectFolder.getPath(), f)); // NOI18N
                        //}
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }

    private void makeProject(File logFile) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        FileObject makeFileObject = null;
        if (makefilePath != null && makefilePath.length() > 0) {
            makeFileObject = CndFileUtils.toFileObject(FileUtil.normalizePath(CndPathUtilities.toAbsolutePath(projectFolder.getFileObject(), makefilePath)));
        }
        if (makeFileObject != null) {
            downloadRemoteFile(CndFileUtils.createLocalFile(makeFileObject.getPath())); // FileUtil.toFile SIC! - always local
            makeFileObject = CndFileUtils.toFileObject(FileUtil.normalizePath(CndPathUtilities.toAbsolutePath(projectFolder.getFileObject(), makefilePath)));
        }
        scanConfigureLog(logFile);
        if (CLEAN_COMMAND.equals(cleanCommand) && BUILD_COMMAND.equals(buildCommand)) {
            if (makeFileObject != null && makeFileObject.isValid()) {
                DataObject dObj;
                try {
                    dObj = DataObject.find(makeFileObject);
                    Node node = dObj.getNodeDelegate();
                    MakeExecSupport mes = node.getLookup().lookup(MakeExecSupport.class);
                    if (mes != null) {
                        mes.setBuildDirectory(makeFileObject.getParent().getPath());
                    }
                    postClean(node);
                } catch (DataObjectNotFoundException ex) {
                    isFinished = true;
                }
            } else {
                switchModel(true);
                postModelDiscovery(true);
            }
        } else {
            postClean();
        }
    }

    private void postClean(final Node node) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        ExecutionListener listener = new ExecutionListener() {

            @Override
            public void executionStarted(int pid) {
            }

            @Override
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.MakeClean, State.Successful);
                } else {
                    importResult.put(Step.MakeClean, State.Fail);
                }
                postMake(node);
            }
        };
        String arguments = ""; // NOI18N
        if (cleanCommand != null){
            arguments = getArguments(cleanCommand);
        }
        if (arguments.length()==0) {
            arguments = "clean"; // NOI18N
        }
        if (TRACE) {
            logger.log(Level.INFO, "#make {0}", arguments); // NOI18N
        }
        try {
            Future<Integer> task = MakeAction.execute(node, arguments, listener, null, makeProject, null, null);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute make clean"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private void postClean() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        ExecutionListener listener = new ExecutionListener() {

            @Override
            public void executionStarted(int pid) {
            }

            @Override
            public void executionFinished(int rc) {
                if (rc == 0) {
                    importResult.put(Step.MakeClean, State.Successful);
                } else {
                    importResult.put(Step.MakeClean, State.Fail);
                }
                postMake();
            }
        };
        if (TRACE) {
            logger.log(Level.INFO, "#{0}", cleanCommand); // NOI18N
        }
        try {
            ExecuteCommand ec = new ExecuteCommand(makeProject, workingDir, cleanCommand);
            Future<Integer> task = ec.performAction(listener, null, null);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute clean command"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private ExecutionListener createMakeExecutionListener() {
        if (makeLog == null) {
            makeLog = createTempFile("make"); // NOI18N
        }
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if(BuildTraceSupport.useBuildTrace(makeConfigurationDescriptor.getActiveConfiguration())) {
            if (BuildTraceSupport.supportedPlatforms(executionEnvironment)) {
                try {
                    HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                    execLog = createTempFile("exec"); // NOI18N
                    execLog.deleteOnExit();
                    if (executionEnvironment.isRemote()) {
                          remoteExecLog = hostInfo.getTempDir()+"/"+execLog.getName(); // NOI18N
                    }
                } catch (IOException ex) {
                } catch (CancellationException ex) {
                }
            }
        }

        return new ExecutionListener() {
            private RfsListenerImpl listener;

            @Override
            public void executionStarted(int pid) {
                if (executionEnvironment.isRemote()) {
                    listener = new RfsListenerImpl(executionEnvironment);
                    RfsListenerSupport.addListener(executionEnvironment, listener);
                }
            }

            @Override
            public void executionFinished(int rc) {
                if (listener != null) {
                    listener.download();
                    RfsListenerSupport.removeListener(executionEnvironment, listener);
                }
                if (rc == 0) {
                    importResult.put(Step.Make, State.Successful);
                } else {
                    importResult.put(Step.Make, State.Fail);
                }
                if (executionEnvironment.isRemote() && execLog != null) {
                    try {
                        if (HostInfoUtils.fileExists(executionEnvironment, remoteExecLog)){
                            Future<Integer> task = CommonTasksSupport.downloadFile(remoteExecLog, executionEnvironment, execLog.getAbsolutePath(), null);
                            if (TRACE) {
                                logger.log(Level.INFO, "#download file {0}", makeLog.getAbsolutePath()); // NOI18N
                            }
                            /*int rc =*/ task.get();
                        }
                    } catch (Throwable ex) {
                        Exceptions.printStackTrace(ex);
                        execLog = null;
                    }
                }
                if (rc == 0) {
                    discovery(rc, makeLog, execLog);
                } else {
                    discovery(-1, makeLog, execLog);
                }
            }
        };
    }
    
    private void postMake(Node node) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        ExecutionListener listener = createMakeExecutionListener();
        Writer outputListener = null;
        if (makeLog != null) {
            try {
                outputListener = new BufferedWriter(new FileWriter(makeLog));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        String arguments = ""; // NOI18N
        if (buildCommand != null){
            arguments = getArguments(buildCommand);
        }
        ExecutionSupport ses = node.getLookup().lookup(ExecutionSupport.class);
        List<String> vars = ImportUtils.parseEnvironment(configureArguments);
        if (ses != null) {
            try {
                ses.setEnvironmentVariables(vars.toArray(new String[vars.size()]));
                if (execLog != null) {
                    ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
                    vars.add(BuildTraceSupport.CND_TOOLS+"="+BuildTraceSupport.getTools(makeConfigurationDescriptor.getActiveConfiguration(), executionEnvironment)); // NOI18N
                    if (executionEnvironment.isLocal()) {
                        vars.add(BuildTraceSupport.CND_BUILD_LOG+"="+execLog.getAbsolutePath()); // NOI18N
                    } else {
                        vars.add(BuildTraceSupport.CND_BUILD_LOG+"="+remoteExecLog); // NOI18N
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (TRACE) {
            logger.log(Level.INFO, "#make {0}", arguments); // NOI18N
        }
        try {
            Future<Integer> task = MakeAction.execute(node, arguments, listener, outputListener, makeProject, vars, null);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute make"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private void postMake() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        ExecutionListener listener = createMakeExecutionListener();
        Writer outputListener = null;
        if (makeLog != null) {
            try {
                outputListener = new BufferedWriter(new FileWriter(makeLog));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        List<String> vars = ImportUtils.parseEnvironment(configureArguments);
        if (execLog != null) {
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            vars.add(BuildTraceSupport.CND_TOOLS+"="+BuildTraceSupport.getTools(makeConfigurationDescriptor.getActiveConfiguration(), executionEnvironment)); // NOI18N
            if (executionEnvironment.isLocal()) {
                vars.add(BuildTraceSupport.CND_BUILD_LOG+"="+execLog.getAbsolutePath()); // NOI18N
            } else {
                vars.add(BuildTraceSupport.CND_BUILD_LOG+"="+remoteExecLog); // NOI18N
            }
        }
        if (TRACE) {
            logger.log(Level.INFO, "#{0}", buildCommand); // NOI18N
        }
        try {
            ExecuteCommand ec = new ExecuteCommand(makeProject, workingDir, buildCommand);
            Future<Integer> task = ec.performAction(listener, outputListener, vars);
            if (task == null) {
                logger.log(Level.INFO, "Cannot execute build command"); // NOI18N
                isFinished = true;
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private String getArguments(String command){
        String arguments = _getArguments(command);
        int i = arguments.indexOf("-f "); // NOI18N
        if (i >= 0) {
            String res = arguments.substring(0, i);
            arguments = arguments.substring(i+3).trim();
            int j = arguments.indexOf(' ');
            if (j < 0) {
                return res;
            } else {
                return res+arguments.substring(j).trim();
            }
        }
        return arguments;
    }

    private String _getArguments(String command){
        if (command.startsWith("\"")) { // NOI18N
            int i = command.indexOf('"', 1); // NOI18N
            if (i > 0) {
                return command.substring(i).trim();
            }
            return ""; // NOI18N
        } else if (command.startsWith("\'")) { // NOI18N
            int i = command.indexOf('\'', 1); // NOI18N
            if (i > 0) {
                return command.substring(i).trim();
            }
            return ""; // NOI18N
        }
        int i = command.indexOf(' '); // NOI18N
        if (i > 0) {
            return command.substring(i).trim();
        }
        return ""; // NOI18N
    }

    private void waitConfigurationDescriptor() {
        // Discovery require a fully completed project
        // Make sure that descriptor was stored and readed
        ConfigurationDescriptorProvider provider = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        provider.getConfigurationDescriptor();
        try {
            waitSources.await();
        } catch (InterruptedException ex) {
        }
        refreshAfterBuild(interrupter);
    }

    private void refreshAfterBuild(final Interrupter interrupter) {
        RPR.post(new Runnable() {
            @Override
            public void run() {
                ConfigurationDescriptorProvider provider = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                Folder rootFolder = provider.getConfigurationDescriptor().getLogicalFolders();
                for(Folder sub : rootFolder.getFolders()) {
                    if (sub.isDiskFolder()) {
                        sub.refreshDiskFolder(interrupter);
                    }
                }
            }
        });
    }

    
    private void discovery(int rc, File makeLog, File execLog) {
        try {
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            waitConfigurationDescriptor();
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            boolean done = false;
            boolean exeLogDone = false;
            boolean makeLogDone = false;
            if (!manualCA) {
                if (rc == 0) {
                    // build successful 
                    if (execLog != null) {
                        done = discoveryByExecLog(execLog, done);
                        exeLogDone = true;
                    }
                    if (!done) {
                        if (!isProjectOpened()) {
                            isFinished = true;
                            return;
                        }
                        done = discoveryByDwarfOrBuildLog(done);
                        buildArifactWasAnalyzed = true;
                    }
                } else if (rc == 1) {
                    // build skiped
                    if (makeLog != null) {
                        // have a build log
                        done = dicoveryByBuildLog(makeLog, done);
                        makeLogDone = true;
                    } else {
                        done = discoveryByDwarfOrBuildLog(done);
                        buildArifactWasAnalyzed = true;
                    }
                } else if (rc == -1) {
                    // build faled
                }
                if (!done && makeLog != null) {
                    if (!isProjectOpened()) {
                        isFinished = true;
                        return;
                    }
                    done = dicoveryByBuildLog(makeLog, done);
                    makeLogDone = true;
                }
                if (done && makeLog != null && !exeLogDone && !makeLogDone) {
                    if (!isProjectOpened()) {
                        isFinished = true;
                        return;
                    }
                    discoveryMacrosByBuildLog(makeLog);
                }
            }
            switchModel(true);
            if (!done) {
                postModelDiscovery(true);
            } else {
                postModelDiscovery(false);
            }
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private void fixMacros(List<ProjectConfiguration> confs) {
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        SnapShot delta = pdp.startModifications();
        boolean changed = false;
        for (ProjectConfiguration conf : confs) {
            List<FileConfiguration> files = conf.getFiles();
            for (FileConfiguration fileConf : files) {
                if (fileConf.getUserMacros().size() > 0) {
                    Item item = findByNormalizedName(new File(fileConf.getFilePath()));
                    if (item != null) {
                        if (TRACE) {
                            logger.log(Level.FINE, "#fix macros for file {0}", fileConf.getFilePath()); // NOI18N
                        }
                        changed |= ProjectBridge.setSourceStandard(item, fileConf.getLanguageStandard(), false);
                        changed |= ProjectBridge.fixFileMacros(fileConf.getUserMacros(), item);
                    }
                }
            }
        }
        if (changed) {
            DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, delta);
        } else {
            pdp.endModifications(delta, false, null);
        }
    }

    private void postModelDiscovery(final boolean isFull) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            final CsmProject p = model.getProject(np);
            if (p == null) {
                if (TRACE) {
                    logger.log(Level.INFO, "#discovery cannot be done by model"); // NOI18N
                }
                isFinished = true;
                return;
            }
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.equals(p)) {
                        ImportProject.listeners.remove(p);
                        CsmListeners.getDefault().removeProgressListener(this); // ignore java warning "usage of this in anonymous class"
                        RP.post(new Runnable() {
                            @Override
                            public void run() {
                                postModelDiscovery(np, p, isFull);
                            }
                        });
                    }
                }

            };
            CsmListeners.getDefault().addProgressListener(listener);
            ImportProject.listeners.put(p, listener);
        } else {
            isFinished = true;
        }
    }

    private void postModelDiscovery(NativeProject np, CsmProject p, boolean isFull) {
        try {
            if (TRACE) {
                logger.log(Level.INFO, "#model ready, explore model"); // NOI18N
            }
            if (isFull) {
                modelDiscovery();
            } else {
                fixExcludedHeaderFiles();
            }
            showFollwUp(np);
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean isFinished = false;
    public boolean isFinished(){
        return isFinished;
    }

    public Map<Step, State> getState(){
        return new EnumMap<Step, State>(importResult);
    }

    public Project getProject(){
        return makeProject;
    }

    private boolean isUILessMode = false;
    public void setUILessMode(){
        isUILessMode = true;
    }

    private File configureLog = null;
    public void setConfigureLog(File configureLog) {
        this.configureLog = configureLog;
    }

    private File makeLog = null;
    private File execLog = null;
    private String remoteExecLog = null;
    public void setMakeLog(File makeLog) {
        this.makeLog = makeLog;
    }

    private void showFollwUp(final NativeProject project) {
        isFinished = true;
        if (isUILessMode) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                FollowUp.showFollowUp(ImportProject.this, project);
            }
        });
    }

    Project getMakeProject() {
        return makeProject;
    }

    Map<Step, State> getImportResult() {
        return importResult;
    }

    // remove wrong "exclude from project" flags
    private void fixExcludedHeaderFiles() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        if (TRACE) {
            logger.log(Level.INFO, "#start fixing excluded header files by model"); // NOI18N
        }
        if (DiscoveryProjectGenerator.fixExcludedHeaderFiles(makeProject, logger)) {
        }
        importResult.put(Step.FixExcluded, State.Successful);
    }

    private Map<String,Item> normalizedItems;
    private Item findByNormalizedName(File file){
        if (normalizedItems == null) {
            normalizedItems = DiscoveryProjectGenerator.initNormalizedNames(makeProject);
        }
        String path = CndFileUtils.normalizeFile(file).getAbsolutePath();
        return normalizedItems.get(path);
    }

    private void modelDiscovery() {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        boolean does = false;
        if (!manualCA && !buildArifactWasAnalyzed) {
            does = discoveryByDwarf(does);
        }
        if (!does) {
            if (!isProjectOpened() || !isModelAvaliable()) {
                isFinished = true;
                return;
            }
            if (TRACE) {
                logger.log(Level.INFO, "#start discovery by model"); // NOI18N
            }
            discoveryByModel();
        }
    }

    private void switchModel(boolean state) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                if (TRACE) {
                    logger.log(Level.INFO, "#enable model for {0}", np.getProjectDisplayName()); // NOI18N
                }
                model.enableProject(np);
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#disable model for {0}", np.getProjectDisplayName()); // NOI18N
                }
                model.disableProject(np);
            }
        }
    }

    private boolean isModelAvaliable(){
        if (CsmModelAccessor.getModelState() != CsmModelState.ON) {
            if (TRACE) {
                logger.log(Level.INFO, "#model is not ON: {0}", CsmModelAccessor.getModelState()); // NOI18N
            }
            return false;
        }
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            return CsmModelAccessor.getModel().getProject(makeProject) != null;
        }
        return false;
    }

    private static final Map<CsmProject, CsmProgressListener> listeners = new WeakHashMap<CsmProject, CsmProgressListener>();

    private boolean discoveryByExecLog(File execLog, boolean done) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
            map.put(DiscoveryWizardDescriptor.EXEC_LOG_FILE, execLog.getAbsolutePath());
            map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    logger.log(Level.INFO, "#start discovery by exec log file {0}", execLog.getAbsolutePath()); // NOI18N
                }
                try {
                    done = true;
                    extension.apply(map, makeProject, interrupter);
                    setBuildResults((List<String>) map.get(DiscoveryWizardDescriptor.BUILD_ARTIFACTS));
                    DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, null);
                    importResult.put(Step.DiscoveryLog, State.Successful);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#discovery cannot be done by exec log file {0}", execLog.getAbsolutePath()); // NOI18N
                }
            }
            map.put(DiscoveryWizardDescriptor.EXEC_LOG_FILE, null);
        }
        return done;
    }

    private void setBuildResults(List<String> buildArtifacts) {
        if (buildArtifacts == null || buildArtifacts.isEmpty()) {
            return;
        }
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (buildArtifacts.size() == 1) {
            MakeConfiguration activeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
            if (activeConfiguration != null) {
                String value = activeConfiguration.getMakefileConfiguration().getOutput().getValue();
                if (value == null || value.isEmpty()) {
                    buildResult = buildArtifacts.get(0);
                    buildResult = ProjectSupport.toProperPath(projectFolder.getPath(), CndPathUtilities.naturalizeSlashes(buildResult), pathMode);
                    buildResult = CndPathUtilities.normalizeSlashes(buildResult);
                    activeConfiguration.getMakefileConfiguration().getOutput().setValue(buildResult);
                }
            }
        }
        //Folder externalFileItems = makeConfigurationDescriptor.getExternalFileItems();
        //for(String binary : buildArtifacts) {
        //    externalFileItems.addItem(Item.createInFileSystem(makeConfigurationDescriptor.getBaseDirFileSystem(),binary));
        //}
     }

    
    private boolean discoveryByDwarfOrBuildLog(boolean done) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
            map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
            if (extension.canApply(map, makeProject, interrupter)) {
                DiscoveryProvider provider = (DiscoveryProvider) map.get(DiscoveryWizardDescriptor.PROVIDER);
                if (provider != null && "make-log".equals(provider.getID())) { // NOI18N
                    if (TRACE) {
                        logger.log(Level.INFO, "#start discovery by log file {0}", provider.getProperty("make-log-file").getValue()); // NOI18N
                    }
                } else {
                    if (TRACE) {
                        logger.log(Level.INFO, "#start discovery by object files"); // NOI18N
                    }
                }
                try {
                    done = true;
                    extension.apply(map, makeProject, interrupter);
                    if (provider != null && "make-log".equals(provider.getID())) { // NOI18N
                        importResult.put(Step.DiscoveryLog, State.Successful);
                    } else {
                        importResult.put(Step.DiscoveryDwarf, State.Successful);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#no dwarf information found in object files"); // NOI18N
                }
            }
        }
        return done;
    }

    private boolean dicoveryByBuildLog(File makeLog, boolean done) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
            map.put(DiscoveryWizardDescriptor.LOG_FILE, makeLog.getAbsolutePath());
            map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    logger.log(Level.INFO, "#start discovery by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                }
                try {
                    done = true;
                    extension.apply(map, makeProject, interrupter);
                    setBuildResults((List<String>) map.get(DiscoveryWizardDescriptor.BUILD_ARTIFACTS));
                    DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, null);
                    importResult.put(Step.DiscoveryLog, State.Successful);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#discovery cannot be done by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                }
            }
        }
        return done;
    }

    private void discoveryMacrosByBuildLog(File makeLog) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            final Map<String, Object> map = new HashMap<String, Object>();
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
            map.put(DiscoveryWizardDescriptor.LOG_FILE, makeLog.getAbsolutePath());
            map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    logger.log(Level.INFO, "#start fix macros by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                }
                @SuppressWarnings("unchecked")
                List<ProjectConfiguration> confs = (List<ProjectConfiguration>) map.get(DiscoveryWizardDescriptor.CONFIGURATIONS);
                fixMacros(confs);
                importResult.put(Step.FixMacros, State.Successful);
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#fix macros cannot be done by log file {0}", makeLog.getAbsolutePath()); // NOI18N
                }
            }
        }
    }

    private boolean discoveryByDwarf(boolean does) {
        final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
            map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
            map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
            if (extension.canApply(map, makeProject, interrupter)) {
                if (TRACE) {
                    logger.log(Level.INFO, "#start discovery by object files"); // NOI18N
                }
                try {
                    extension.apply(map, makeProject, interrupter);
                    importResult.put(Step.DiscoveryDwarf, State.Successful);
                    does = true;
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                if (TRACE) {
                    logger.log(Level.INFO, "#no dwarf information found in object files"); // NOI18N
                }
            }
        }
        return does;
    }

    private void discoveryByModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, nativeProjectPath);
        map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, consolidationStrategy);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider("model-folder"); // NOI18N
        provider.getProperty("folder").setValue(nativeProjectPath); // NOI18N
        if (manualCA) {
            provider.getProperty("prefer-local").setValue(Boolean.TRUE); // NOI18N
        }
        map.put(DiscoveryWizardDescriptor.PROVIDER, provider);
        map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        descriptor.setProject(makeProject);
        DiscoveryExtension.buildModel(descriptor, interrupter);
        try {
            DiscoveryProjectGeneratorImpl generator = new DiscoveryProjectGeneratorImpl(descriptor);
            generator.makeProject();
            importResult.put(Step.DiscoveryModel, State.Successful);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static enum State {

        Successful, Fail, Skiped
    }

    public static enum Step {

        Project, Configure, MakeClean, Make, DiscoveryDwarf, DiscoveryLog, FixMacros, DiscoveryModel, FixExcluded
    }

    static final class RfsListenerImpl implements RfsListener {
        private final Map<String, File> storage = new HashMap<String, File>();
        private final ExecutionEnvironment execEnv;

        RfsListenerImpl(ExecutionEnvironment execEnv) {
            this.execEnv = execEnv;
        }

        @Override
        public void fileChanged(ExecutionEnvironment env, File localFile, String remotePath) {
            if (env.equals(execEnv)) {
                storage.put(remotePath, localFile);
            }
        }
        void download() {
            Map<String, File> copy = new HashMap<String, File>(storage);
            for(Map.Entry<String, File> entry : copy.entrySet()) {
                downloadImpl(entry.getKey(),entry.getValue());
            }
        }

        private void downloadImpl(String remoteFile, File localFile) {
            try {
                Future<Integer> task = CommonTasksSupport.downloadFile(remoteFile, execEnv, localFile.getAbsolutePath(), null);
                if (TRACE) {
                    logger.log(Level.INFO, "#download file {0}", localFile.getAbsolutePath()); // NOI18N
                }
                task.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
