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
package org.netbeans.modules.cnd.remote.projectui.wizard.cnd;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
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
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.builds.CMakeExecSupport;
import org.netbeans.modules.cnd.builds.ImportUtils;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.builds.QMakeExecSupport;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.wizard.api.ConsolidationStrategy;
import org.netbeans.modules.cnd.discovery.wizard.api.support.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.execution.ExecutionSupport;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
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
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class ImportRemoteProject implements PropertyChangeListener {

    private static final boolean TRACE = Boolean.getBoolean("cnd.discovery.trace.projectimport"); // NOI18N
    public static final Logger logger;
    static {
        logger = Logger.getLogger("org.netbeans.modules.cnd.discovery.projectimport.ImportProject"); // NOI18N
        if (TRACE) {
            logger.setLevel(Level.ALL);
        }
    }
    
    private static final RequestProcessor RP = new RequestProcessor(ImportRemoteProject.class.getName(), 2);
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
    private String buildCommand = "${MAKE} -f Makefile";  // NOI18N
    private String cleanCommand = "${MAKE} -f Makefile clean";  // NOI18N
    private String buildResult = "";  // NOI18N
    private Project makeProject;
    private boolean runMake;
    private String includeDirectories = ""; // NOI18N
    private String macros = ""; // NOI18N
    private Iterator<SourceFolderInfo> sources;
    private Iterator<SourceFolderInfo> tests;
    private String sourceFoldersFilter = null;
    private FileObject configureFileObject;
    private Map<Step, State> importResult = new EnumMap<Step, State>(Step.class);

    private static final String CND_TOOLS = "__CND_TOOLS__"; //NOI18N
    private static final String CND_TOOLS_VALUE = System.getProperty("cnd.buildtrace.tools", "gcc:c++:g++:gfortran:g77:g90:g95:cc:CC:ffortran:f77:f90:f95"); //NOI18N
    private static final String CND_BUILD_LOG = "__CND_BUILD_LOG__"; //NOI18N
    private boolean useBuildTrace = true;
    private final CountDownLatch waitSources = new CountDownLatch(1);
    private final AtomicInteger openState = new AtomicInteger(0);
    private Interrupter interrupter;


    public ImportRemoteProject(WizardDescriptor wizard) {
        hostUID = (String) wizard.getProperty(WizardConstants.PROPERTY_HOST_UID); // NOI18N
        if (hostUID == null) {
            executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        } else {
            executionEnvironment = ExecutionEnvironmentFactory.fromUniqueID(hostUID);
        }
        fileSystemExecutionEnvironment = executionEnvironment;
        pathMode = MakeProjectOptions.getPathMode();
        projectFolder = (FSPath) wizard.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER);
        nativeProjectPath = (String) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_DIR);
        assert nativeProjectPath != null;
        FileObject npfo = (FileObject) wizard.getProperty(WizardConstants.PROPERTY_NATIVE_PROJ_FO);
        // #230539 NPE while creation a full remote project
        // IMHO we duplicate information here: nativeProjectFO and pair (nativeProjectPath, executionEnvironment);
        // but I'm not sure I understand all project creation nuances in minute details, so I left this as is, just added a check
        if (npfo == null) {
            npfo = FileSystemProvider.getFileObject(executionEnvironment, nativeProjectPath);
            if (logger.isLoggable(Level.INFO)) {
                String warning = "Null file object for " + nativeProjectPath + " at " + executionEnvironment + //NOI18N
                        ((npfo== null) ? " NOT " : "") + " found at 2-nd attempt"; //NOI18N
                logger.log(Level.INFO, warning, new Exception(warning));
            }
        } else {
            FileObject npfo2 = FileSystemProvider.getFileObject(executionEnvironment, nativeProjectPath);
            if (!npfo.equals(npfo2)) {
                String warning = "Inconsistent file objects when creating a project: " + npfo + " vs " + npfo2; //NOI18N
                logger.log(Level.INFO, warning, new Exception(warning));
            }
        }
        nativeProjectFO = npfo;
        if (Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE))) { // NOI18N
            simpleSetup(wizard);
        } else {
            customSetup(wizard);
        }
        Preferences makeProjectPref = NbPreferences.root().node("/org/netbeans/modules/cnd/makeproject"); //NOI18N
        if (makeProjectPref != null) {
            useBuildTrace = makeProjectPref.getBoolean("useBuildTrace", true);
        }
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
        runConfigure = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_RUN_CONFIGURE)); // NOI18N
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
        manualCA = "true".equals(wizard.getProperty(WizardConstants.PROPERTY_MANUAL_CODE_ASSISTANCE)); // NOI18N
        toolchain = (CompilerSet)wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        defaultToolchain = Boolean.TRUE.equals(wizard.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
    }

    public Set<FileObject> create() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        String aHostUID = ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal());
        MakeConfiguration extConf = MakeConfiguration.createMakefileConfiguration(projectFolder, "Default", aHostUID, toolchain, defaultToolchain); // NOI18N
        int platform = CompilerSetManager.get(executionEnvironment).getPlatform();
        extConf.getDevelopmentHost().setBuildPlatform(platform);
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
                // see comment above
                // makeFileObject = CndFileUtils.toFileObject(CndPathUtilities.toAbsolutePath(projectFolder.getAbsolutePath(), makefilePath));
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
                //.setSourceFolders(sources)
                .setSourceFoldersFilter(sourceFoldersFilter)
                .setTestFolders(tests)
                .setImportantFiles(importantItemsIterator)
                .setFullRemoteNativeProjectPath(nativeProjectPath)
                .setHostUID(aHostUID);
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
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportRemoteProject.class, "ImportProject.Progress.AnalyzeRoot"));
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
                if (pdp.getConfigurationDescriptor().getActiveConfiguration() != null) {
                    if (runConfigure && configurePath != null && configurePath.length() > 0 &&
                            configureFileObject != null && configureFileObject.isValid()) {
                        postConfigure();
                    } else {
                        if (runMake) {
                            makeProject(true, null);
                        } else {
                            discovery(0, null, null);
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

                @Override
                public void executionStarted(int pid) {
                }

                @Override
                public void executionFinished(int rc) {
                    if (rc == 0) {
                        importResult.put(Step.Configure, State.Successful);
                    } else {
                        importResult.put(Step.Configure, State.Fail);
                    }
                    if (runMake && rc == 0) {
                        //parseConfigureLog(configureLog);
                        // when run scripts we do full "clean && build" to
                        // remove old build artifacts as well
                        makeProject(true, configureLog);
                    } else {
                        switchModel(true);
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
            }
        } catch (DataObjectNotFoundException e) {
            logger.log(Level.INFO, "Cannot configure project", e); // NOI18N
            isFinished = true;
        } catch (Throwable e) {
            logger.log(Level.INFO, "Cannot configure project", e); // NOI18N
            isFinished = true;
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

    private void makeProject(boolean doClean, File logFile) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        FileObject makeFileObject = null;
        if (makefilePath != null && makefilePath.length() > 0) {
            nativeProjectFO.refresh();
            makeFileObject = CndFileUtils.toFileObject(projectFolder.getFileSystem(), CndPathUtilities.toAbsolutePath(projectFolder.getFileObject(), makefilePath));
        }
        if (makeFileObject != null && makeFileObject.isValid()) {
            DataObject dObj;
            try {
                dObj = DataObject.find(makeFileObject);
                Node node = dObj.getNodeDelegate();
                MakeExecSupport mes = node.getLookup().lookup(MakeExecSupport.class);
                if (mes != null) {
                    mes.setBuildDirectory(makeFileObject.getParent().getPath());
                }
                if (doClean) {
                    postClean(node);
                } else {
                    postMake(node);
                }
            } catch (DataObjectNotFoundException ex) {
                isFinished = true;
            }
        } else {
            String path = nativeProjectPath;
            File file = new File(path + "/Makefile"); // NOI18N
            if (file.exists() && file.isFile() && file.canRead()) {
                makefilePath = file.getAbsolutePath();
            } else {
                file = new File(path + "/makefile"); // NOI18N
                if (file.exists() && file.isFile() && file.canRead()) {
                    makefilePath = file.getAbsolutePath();
                }
            }
            switchModel(true);
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

    private void postMake(Node node) {
        if (!isProjectOpened()) {
            isFinished = true;
            return;
        }
        if (makeLog == null) {
            makeLog = createTempFile("make"); // NOI18N
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                switch (hostInfo.getOSFamily()) {
                case SUNOS:
                case LINUX:
                    if (fileSystemExecutionEnvironment.isRemote()) {
                        remoteMakeLog = hostInfo.getTempDir()+"/"+makeLog.getName(); // NOI18N
                    }
                }
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }
        if(useBuildTrace) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment);
                switch (hostInfo.getOSFamily()) {
                case SUNOS:
                case LINUX:
                    execLog = createTempFile("exec"); // NOI18N
                    execLog.deleteOnExit();
                    if (executionEnvironment.isRemote()) {
                        remoteExecLog = hostInfo.getTempDir()+"/"+execLog.getName(); // NOI18N
                    }
                }
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }

        ExecutionListener listener = new ExecutionListener() {

            @Override
            public void executionStarted(int pid) {
            }

            @Override
            public void executionFinished(int rc) {
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
                                logger.log(Level.INFO, "#download file {0}", execLog.getAbsolutePath()); // NOI18N
                            }
                            /*int rc =*/ task.get();
                        }
                    } catch (Throwable ex) {
                        Exceptions.printStackTrace(ex);
                        execLog = null;
                    }
                }
                if (fileSystemExecutionEnvironment.isRemote() && remoteMakeLog != null) {
                    try {
                        Future<UploadStatus> task = CommonTasksSupport.uploadFile(makeLog, fileSystemExecutionEnvironment, remoteMakeLog, 0555);
                        if (TRACE) {
                            logger.log(Level.INFO, "#upload file {0}", remoteMakeLog); // NOI18N
                        }
                        /*int rc =*/ task.get();
                    } catch (Throwable ex) {
                        Exceptions.printStackTrace(ex);
                        execLog = null;
                    }
                }
                discovery(rc, remoteMakeLog, execLog);
            }
        };
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
                    vars.add(CND_TOOLS+"="+CND_TOOLS_VALUE); // NOI18N
                    if (executionEnvironment.isLocal()) {
                        vars.add(CND_BUILD_LOG+"="+execLog.getAbsolutePath()); // NOI18N
                    } else {
                        vars.add(CND_BUILD_LOG+"="+remoteExecLog); // NOI18N
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
    }

    private void discovery(int rc, String makeLog, File execLog) {
        try {
            String ROOT_FOLDER = "DW:rootFolder"; // NOI18N
            String EXEC_LOG_FILE = "DW:execLogFile"; // NOI18N
            String CONSOLIDATION_STRATEGY = "DW:consolidationLevel"; // NOI18N
            if (!isProjectOpened()) {
                isFinished = true;
                return;
            }
            waitConfigurationDescriptor();
            boolean done = false;
            boolean exeLogDone = false;
            if (!manualCA) {
                final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
                if (rc == 0) {
                    if (execLog != null) {
                        if (extension != null) {
                            final Map<String, Object> map = new HashMap<String, Object>();
                            map.put(ROOT_FOLDER, nativeProjectPath);
                            map.put(EXEC_LOG_FILE, execLog.getAbsolutePath());
                            map.put(CONSOLIDATION_STRATEGY, ConsolidationStrategy.FILE_LEVEL);
                            if (extension.canApply(map, makeProject, interrupter)) {
                                if (TRACE) {
                                    logger.log(Level.INFO, "#start discovery by exec log file {0}", execLog.getAbsolutePath()); // NOI18N
                                }
                                try {
                                    done = true;
                                    exeLogDone = true;
                                    extension.apply(map, makeProject, interrupter);
                                    importResult.put(Step.DiscoveryLog, State.Successful);
                                } catch (IOException ex) {
                                    ex.printStackTrace(System.err);
                                }
                            } else {
                                if (TRACE) {
                                    logger.log(Level.INFO, "#discovery cannot be done by exec log file {0}", execLog.getAbsolutePath()); // NOI18N
                                }
                            }
                            map.put(EXEC_LOG_FILE, null);
                        }
                    }
                    if (extension != null && !done) {
                        if (TRACE) {
                            logger.log(Level.INFO, "#start remote discovery by log file {0}", makeLog); // NOI18N
                        }
                        // TODO detect real return code
                        /*done = */updateRemoteProjectImpl(makeLog);
                        buildArifactWasAnalyzed = true;
                        // TODO reload configuration descriptor
                    }
                }
            }
            switchModel(true);
            postModelDiscovery();
        } catch (Throwable ex) {
            isFinished = true;
            Exceptions.printStackTrace(ex);
        }
    }

    
    private boolean updateRemoteProjectImpl(String makeLog) {
        ProgressHandle createHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CreateProjectFromBinary.class, "CONFIGURING_PROJECT_CREATOR",executionEnvironment.getDisplayName()));
        createHandle.start();
        try {
            FileObject projectCreator = findProjectCreator();
            if (projectCreator == null) {
                if (TRACE) {
                    logger.log(Level.INFO, NbBundle.getMessage(CreateProjectFromBinary.class, "ERROR_FIND_PROJECT_CREATOR",executionEnvironment.getDisplayName())); // NOI18N
                }
                return false;
            }
            if (TRACE) {
                logger.log(Level.INFO, "#{0} --netbeans-project={1} --project-reconfigure build-log={2}", // NOI18N
                            new Object[]{projectCreator.getPath(), projectFolder.getPath(), makeLog});
            }
            DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(makeProject, null);
            FileObject conf1 = projectFolder.getFileObject().getFileObject("nbproject/configurations.xml"); //NOI18N
            ExitStatus execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                         , "--netbeans-project="+projectFolder.getPath() // NOI18N
                                         , "--project-reconfigure", "build-log="+makeLog // NOI18N
                                         );
            if (TRACE) {
                logger.log(Level.INFO, "#exitCode={0}", execute.exitCode); // NOI18N
                logger.log(Level.INFO, execute.error);
                logger.log(Level.INFO, execute.output);
            }
            if (!execute.isOK()) {
                // probably java does not found an
                // try to find java in environment variables
                String java = null; 
                try {
                    java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JDK_HOME"); // NOI18N
                    if (java == null || java.isEmpty()) {
                        java = HostInfoUtils.getHostInfo(executionEnvironment).getEnvironment().get("JAVA_HOME"); // NOI18N
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (CancellationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (java != null) {
                    execute = ProcessUtils.execute(executionEnvironment, projectCreator.getPath()
                                         , "--netbeans-project="+projectFolder.getPath() // NOI18N
                                         , "--project-reconfigure", "build-log="+makeLog // NOI18N
                                         );
                }
                if (!execute.isOK()) {
                    if (TRACE) {
                        logger.log(Level.INFO, NbBundle.getMessage(CreateProjectFromBinary.class, "ERROR_RUN_PROJECT_CREATOR",executionEnvironment.getDisplayName())); // NOI18N
                    }
                    return false;
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            makeProject.getProjectDirectory().refresh(true);
            conf1.getParent().refresh(true);
            Type genericSuperclass = conf1.getClass().getGenericSuperclass();
            if (genericSuperclass instanceof Class) {
                Type genericSuperclass1 = ((Class)genericSuperclass).getGenericSuperclass();
                if (genericSuperclass1 instanceof Class) {
                    Method[] declaredMethods =((Class)genericSuperclass1).getDeclaredMethods();
                    for(Method method : declaredMethods) {
                        String name = method.getName();
                        if ("getListeners".equals(name)) { // NOI18N
                            try {
                                method.setAccessible(true);
                                Object invoke = method.invoke(conf1);
                                if (invoke != null) {
                                    Enumeration<FileChangeListener> aListeners = (Enumeration<FileChangeListener>) invoke;
                                    while(aListeners.hasMoreElements()) {
                                        FileChangeListener nextElement = aListeners.nextElement();
                                        nextElement.fileChanged(new FileEvent(conf1));
                                    }
                                }
                            } catch (IllegalAccessException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (IllegalArgumentException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (InvocationTargetException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            break;
                        }
                    }
                }
            }
            waitConfigurationDescriptor();
            return true;
        } finally {
            createHandle.finish();
        }
    }

    private FileObject findProjectCreator() {
        FileSystem fileSystem = FileSystemProvider.getFileSystem(fileSystemExecutionEnvironment);
        for(CompilerSet set : CompilerSetManager.get(fileSystemExecutionEnvironment).getCompilerSets()) {
            if (set.getCompilerFlavor().isSunStudioCompiler()) {
                String directory = set.getDirectory();
                FileObject projectCreator = fileSystem.findResource(directory+"/../lib/ide_project/bin/ide_project"); // NOI18N
                if (projectCreator != null && projectCreator.isValid()) {
                    return projectCreator;
                }
            }
        }
        return null;
    }
    
    private void postModelDiscovery() {
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
                        try {
                            ImportRemoteProject.listeners.remove(p);
                            CsmListeners.getDefault().removeProgressListener(this); // ignore java warning "usage of this in anonymous class"
                            if (TRACE) {
                                logger.log(Level.INFO, "#model ready, explore model"); // NOI18N
                            }
                            DiscoveryProjectGenerator.fixExcludedHeaderFiles(makeProject, logger);
                        } catch (Throwable ex) {
                            isFinished = true;
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            };
            CsmListeners.getDefault().addProgressListener(listener);
            ImportRemoteProject.listeners.put(p, listener);
        } else {
            isFinished = true;
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
    private String remoteMakeLog = null;
    public void setMakeLog(File makeLog) {
        this.makeLog = makeLog;
    }

    Project getMakeProject() {
        return makeProject;
    }

    Map<Step, State> getImportResult() {
        return importResult;
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

    private static final Map<CsmProject, CsmProgressListener> listeners = new WeakHashMap<CsmProject, CsmProgressListener>();

    public static enum State {

        Successful, Fail, Skiped
    }

    public static enum Step {

        Project, Configure, MakeClean, Make, DiscoveryDwarf, DiscoveryLog, FixMacros, DiscoveryModel, FixExcluded
    }

}
