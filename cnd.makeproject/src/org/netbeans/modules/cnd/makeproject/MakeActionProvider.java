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

import java.util.concurrent.CancellationException;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.StepControllerProvider.StepController;
import org.netbeans.modules.cnd.utils.ui.ModalMessageDlg;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.toolchain.ui.BuildToolsAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.RunDialogPanel;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.ui.utils.ConfSelectorPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.dlight.util.usagetracking.SunStudioUserCounter;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.AssemblerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.FortranCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.platform.Platform;
import org.netbeans.modules.cnd.makeproject.platform.Platforms;
import org.netbeans.modules.cnd.makeproject.api.StepControllerProvider;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.spi.toolchain.CompilerSetFactory;
import org.netbeans.modules.cnd.api.toolchain.ui.LocalToolsPanelModel;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelModel;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.NamedRunnable;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport.ShellValidationStatus;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/** Action provider of the Make project. This is the place where to do
 * strange things to Make actions. E.g. compile-single.
 */
public final class MakeActionProvider implements ActionProvider {

    // Commands available from Make project
    public static final String COMMAND_BATCH_BUILD = "batch_build"; // NOI18N
    public static final String COMMAND_BUILD_PACKAGE = "build_packages"; // NOI18N
    public static final String COMMAND_DEBUG_LOAD_ONLY = "debug.load.only"; // NOI18N
    public static final String COMMAND_CUSTOM_ACTION = "custom.action"; // NOI18N
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DEBUG_LOAD_ONLY,
        COMMAND_DEBUG_SINGLE,
        COMMAND_BATCH_BUILD,
        COMMAND_BUILD_PACKAGE,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        COMMAND_CUSTOM_ACTION,
        
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
    };

    // Project
    private MakeProject project;

    // Project Descriptor
    private MakeConfigurationDescriptor projectDescriptor = null;
    /** Map from commands to ant targets */
    private Map<String, String[]> commands;
    private Map<String, String[]> commandsNoBuild;
    private boolean lastValidation = false;

    private static final String SAVE_STEP = "save"; // NOI18N
    private static final String BUILD_STEP = "build"; // NOI18N
    private static final String BUILD_PACKAGE_STEP = "build-package"; // NOI18N
    private static final String CLEAN_STEP = "clean"; // NOI18N
    private static final String RUN_STEP = "run"; // NOI18N
    private static final String DEBUG_STEP = "debug"; // NOI18N
    private static final String DEBUG_STEPINTO_STEP = "debug-stepinto"; // NOI18N
    private static final String DEBUG_LOAD_ONLY_STEP = "debug-load-only"; // NOI18N
    private static final String RUN_SINGLE_STEP = "run-single"; // NOI18N
    private static final String DEBUG_SINGLE_STEP = "debug-single"; // NOI18N
    private static final String COMPILE_SINGLE_STEP = "compile-single"; // NOI18N
    private static final String CUSTOM_ACTION_STEP = "custom-action"; // NOI18N
    private static final String VALIDATE_TOOLCHAIN = "validate-toolchain"; // NOI18N
    private static final String BUILD_TESTS_STEP = "build-tests"; // NOI18N
    private static final String TEST_STEP = "test"; // NOI18N
    private static final String TEST_SINGLE_STEP = "test-single"; // NOI18N

    public MakeActionProvider(MakeProject project) {
        this.project = project;
        commands = loadAcrionSteps("CND/BuildAction"); // NOI18N
        commandsNoBuild = loadAcrionSteps("CND/NoBuildAction"); // NOI18N
    }

    private Map<String, String[]> loadAcrionSteps(String root) {
        Map<String, String[]> res = new HashMap<String, String[]>();
        FileObject folder = FileUtil.getConfigFile(root);
        if (folder != null && folder.isFolder()) {
            for (FileObject subFolder : folder.getChildren()) {
                if (subFolder.isFolder()) {
                    TreeMap<Integer,String> map = new TreeMap<Integer, String>();
                    for(FileObject file : subFolder.getChildren()) {
                        Integer position = (Integer) file.getAttribute("position"); // NOI18N
                        map.put(position, file.getNameExt());
                    }
                    res.put(subFolder.getNameExt(), map.values().toArray(new String[map.size()]));
                }
            }
        }
        return res;
    }

    private boolean isProjectDescriptorLoaded() {
        if (projectDescriptor == null) {
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            return pdp.gotDescriptor();
        } else {
            return true;
        }
    }

    private MakeConfigurationDescriptor getProjectDescriptor() {
        if (projectDescriptor == null) {
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            projectDescriptor = pdp.getConfigurationDescriptor();
        }
        return projectDescriptor;
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    @Override
    public void invokeAction(String command, final Lookup context) throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
            project.setDeleted();
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return;
        }

        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return;
        }

        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return;
        }

        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return;
        }

        if (COMMAND_RUN_SINGLE.equals(command)) {
            Node node = context.lookup(Node.class);
            if (node != null) {
                ShellRunAction.performAction(node);
            }
            return;
        }

        // Basic info
        final MakeConfigurationDescriptor pd = getProjectDescriptor();
        MakeConfiguration activeConf = pd.getActiveConfiguration();
        if (activeConf == null) {
            return;
        }

        final List<MakeConfiguration> confs = new ArrayList<MakeConfiguration>();
        if (command.equals(COMMAND_BATCH_BUILD)) {
            BatchConfigurationSelector batchConfigurationSelector = new BatchConfigurationSelector(project, pd.getConfs().toArray());
            String batchCommand = batchConfigurationSelector.getCommand();
            Configuration[] confsArray = batchConfigurationSelector.getSelectedConfs();
            if (batchCommand == null || confsArray == null || confsArray.length == 0) {
                return;
            }
            command = batchCommand;
            for (Configuration conf : confsArray) {
                confs.add((MakeConfiguration) conf);
            }
        } else {
            confs.add(activeConf);
        }
        final String finalCommand = command;

        CancellableTask actionWorker = new CancellableTask() {
            @Override
            protected void runImpl() {
                final ArrayList<ProjectActionEvent> actionEvents = new ArrayList<ProjectActionEvent>();
                for (MakeConfiguration conf : confs) {
                    addAction(actionEvents, pd, conf, finalCommand, context, cancelled);
                }
                // Execute actions
                if (actionEvents.size() > 0 && ! cancelled.get()) {
                    RequestProcessor.getDefault().post(new NamedRunnable("Make Project Action Worker") { //NOI18N

                        @Override
                        protected void runImpl() {
                            ProjectActionSupport.getInstance().fireActionPerformed(actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]));
                        }
                    });
                }
            }
        };
        runActionWorker(activeConf.getDevelopmentHost().getExecutionEnvironment(), actionWorker);
    }

    private static void runActionWorker(ExecutionEnvironment exeEnv, CancellableTask actionWorker) {
        ServerRecord record = ServerList.get(exeEnv);
        assert record != null;
        invokeLongAction(record, actionWorker);
    }

    public void invokeCustomAction(final MakeConfigurationDescriptor pd, final MakeConfiguration conf, final ProjectActionHandler customProjectActionHandler) {
        CancellableTask actionWorker = new CancellableTask() {
           @Override
            protected void runImpl() {
                ArrayList<ProjectActionEvent> actionEvents = new ArrayList<ProjectActionEvent>();
                addAction(actionEvents, pd, conf, MakeActionProvider.COMMAND_CUSTOM_ACTION, null, cancelled);
                ProjectActionSupport.getInstance().fireActionPerformed(
                        actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]),
                        customProjectActionHandler);
            }
        };
        runActionWorker(conf.getDevelopmentHost().getExecutionEnvironment(), actionWorker);
    }

    private static void invokeLongAction(final ServerRecord record, final CancellableTask actionWorker) {
        CancellableTask wrapper;
        if (!record.isDeleted() && record.isOnline()) {
            wrapper = actionWorker;
        } else {
            String message;
            if (record.isDeleted()) {
                message = MessageFormat.format(getString("ERR_RequestingDeletedConnection"), record.getDisplayName());
                int res = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), message, getString("DLG_TITLE_DeletedConnection"), JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION) {
                    ServerList.addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), false, true);
                } else {
                    return;
                }
            }
            // start validation phase
            wrapper = new CancellableTask() {
                @Override
                public boolean cancel() {
                    return actionWorker.cancel();
                }

                @Override
                public void runImpl() {
                    try {
                        if (!ConnectionManager.getInstance().isConnectedTo(record.getExecutionEnvironment())) {
                            ConnectionManager.getInstance().connectTo(record.getExecutionEnvironment());
                        }
                        record.validate(true);
                        // initialize compiler sets for remote host if needed
                        CompilerSetManager csm = CompilerSetManager.get(record.getExecutionEnvironment());
                        csm.initialize(true, true, null);
                    } catch (CancellationException ex) {
                        cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                        final String message = MessageFormat.format(getString("ERR_Cant_Connect"), record.getDisplayName()); //NOI18N
                        final String title = getString("DLG_TITLE_Cant_Connect"); //NOI18N
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                        message, title, JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                    if (record.isOnline()) {
                        actionWorker.run();
                    }
                }
            };
        }
        Frame mainWindow = WindowManager.getDefault().getMainWindow();
        String msg = NbBundle.getMessage(MakeActionProvider.class, "MSG_Validate_Host", record.getDisplayName());
        String title = NbBundle.getMessage(MakeActionProvider.class, "DLG_TITLE_Validate_Host");
        ModalMessageDlg.runLongTask(mainWindow, wrapper, null, wrapper, title, msg);
    }

    private void addAction(ArrayList<ProjectActionEvent> actionEvents,
            MakeConfigurationDescriptor pd, MakeConfiguration conf, String command, Lookup context,
            AtomicBoolean cancelled) throws IllegalArgumentException {

        if (cancelled.get()) {
            return;
        }

        AtomicBoolean validated = new AtomicBoolean(false);
        lastValidation = false;

        String[] targetNames = getTargetNames(command);
        if (targetNames == null || targetNames.length == 0) {
            return;
        }

        for (int i = 0; i < targetNames.length; i++) {
            final String targetName = targetNames[i];
            List<String> tail = new ArrayList<String>();
            for (int j = i + 1; j < targetNames.length; j++) {
                tail.add(targetNames[j]);
            }
            List<String> delegate = validateStep(targetName, tail);
            if (delegate != null) {
                for(String target : delegate) {
                    if (!addTarget(target, actionEvents, pd, conf, context, cancelled, validated)) {
                        break;
                    }
                }
            } else {
                if (!addTarget(targetName, actionEvents, pd, conf, context, cancelled, validated)) {
                    break;
                }
            }
        }
    }

    private boolean addTarget(String targetName, ArrayList<ProjectActionEvent> actionEvents, 
            MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, AtomicBoolean cancelled, AtomicBoolean validated) throws IllegalArgumentException {
        if (cancelled.get()) {
            return false; // getPlatformInfo() might be costly for remote host
        }

        if (targetName.equals(SAVE_STEP)) {
            return onSaveStep();
        } else if (targetName.equals(VALIDATE_TOOLCHAIN)) {
            return onValidateToolchainStep(pd, conf, cancelled, validated);
        } else if (targetName.equals(BUILD_STEP)) {
            return onBuildStep(actionEvents, pd, conf, ProjectActionEvent.PredefinedType.BUILD);
        } else if (targetName.equals(BUILD_TESTS_STEP)) {
            return onBuildStep(actionEvents, pd, conf, ProjectActionEvent.PredefinedType.BUILD_TESTS);
        } else if (targetName.equals(BUILD_PACKAGE_STEP)) {
            return onBuildPackageStep(actionEvents, conf, ProjectActionEvent.PredefinedType.BUILD);
        } else if (targetName.equals(CLEAN_STEP)) {
            return onCleanStep(actionEvents, pd, conf, ProjectActionEvent.PredefinedType.CLEAN);
        } else if (targetName.equals(COMPILE_SINGLE_STEP)) {
            return onCompileSingleStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.BUILD);
        } else if (targetName.equals(RUN_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.RUN);
        } else if (targetName.equals(TEST_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.TEST);
        } else if (targetName.equals(TEST_SINGLE_STEP)) {
            return onTestSingleStep(actionEvents, pd, conf, context, ProjectActionEvent.PredefinedType.TEST);
        } else if (targetName.equals(RUN_SINGLE_STEP) || targetName.equals(DEBUG_SINGLE_STEP)) {
            return onRunSingleStep(conf, actionEvents, context, ProjectActionEvent.PredefinedType.RUN);
        } else if (targetName.equals(DEBUG_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.DEBUG);
        } else if (targetName.equals(DEBUG_STEPINTO_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.DEBUG_STEPINTO);
        } else if (targetName.equals(DEBUG_LOAD_ONLY_STEP)) {
            return onRunStep(actionEvents, pd, conf, cancelled, validated, context, ProjectActionEvent.PredefinedType.DEBUG_LOAD_ONLY);
        } else if (targetName.equals(CUSTOM_ACTION_STEP)) {
            return onCustomActionStep(actionEvents, conf, context, ProjectActionEvent.PredefinedType.CUSTOM_ACTION);
        }
        return onExtendedStep(actionEvents, conf, context, targetName);
    }

    private boolean onSaveStep() {
        // Save all files and projects
        if (MakeOptions.getInstance().getSave()) {
            LifecycleManager.getDefault().saveAll();
        }
        if (!ProjectSupport.saveAllProjects(getString("NeedToSaveAllText"))) {// NOI18N
            return false;
        }
        return true;
    }

    private boolean onRunStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, AtomicBoolean cancelled, AtomicBoolean validated, Lookup context, Type actionEvent) {
        PlatformInfo pi = conf.getPlatformInfo();
        validated.set(true);

        if (actionEvent == ProjectActionEvent.PredefinedType.TEST) {
            if (conf.isCompileConfiguration() && !validateProject(conf)) {
                return true;
            }
            MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
            String buildCommand;
            buildCommand = makeArtifact.getBuildCommand(getMakeCommand(pd, conf) + " test", ""); // NOI18N
            String args = "";
            int index = buildCommand.indexOf(' ');
            if (index > 0) {
                args = buildCommand.substring(index + 1);
                buildCommand = buildCommand.substring(0, index);
            }
            RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform());
            profile.setArgs(args);
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true);
            actionEvents.add(projectActionEvent);
        } else if (conf.isMakefileConfiguration()) {
            String path;
            if (actionEvent == ProjectActionEvent.PredefinedType.RUN) {
                path = conf.getMakefileConfiguration().getOutput().getValue();
                if (path.length() > 0 && !CndPathUtilitities.isPathAbsolute(path)) {
                    // make path relative to run working directory
                    // path here should always be in unix style, see issue 149404
                    path = conf.getMakefileConfiguration().getAbsOutput();
                    path = CndPathUtilitities.toRelativePath(conf.getProfile().getRunDirectory(), path);
                }
            } else {
                // Always absolute
                path = conf.getMakefileConfiguration().getAbsOutput();
                path = CndPathUtilitities.normalize(path);
            }
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, null, false);
            actionEvents.add(projectActionEvent);
            RunDialogPanel.addElementToExecutablePicklist(path);
        } else if (conf.isLibraryConfiguration()) {
            // Should never get here...
            assert false;
            return false;
        } else if (conf.isApplicationConfiguration()) {
            RunProfile runProfile = null;
            int platform = conf.getDevelopmentHost().getBuildPlatform();
            if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                // On Windows we need to add paths to dynamic libraries from subprojects to PATH
                runProfile = conf.getProfile().clone(conf);
                Set<String> subProjectOutputLocations = conf.getSubProjectOutputLocations();
                String path = ""; // NOI18N
                // Add paths from subprojetcs
                Iterator<String> iter = subProjectOutputLocations.iterator();
                while (iter.hasNext()) {
                    String location = CndPathUtilitities.naturalize(iter.next());
                    path = location + ";" + path; // NOI18N
                }
                // Add paths from -L option
                List<String> list = conf.getLinkerConfiguration().getAdditionalLibs().getValue();
                iter = list.iterator();
                while (iter.hasNext()) {
                    String location = CndPathUtilitities.naturalize(iter.next());
                    path = location + ";" + path; // NOI18N
                }
                String userPath = runProfile.getEnvironment().getenv(pi.getPathName());
                if (userPath == null) {
                    if (cancelled.get()) {
                        return false; // getEnv() might be costly for remote host
                    }
                    userPath = HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get(pi.getPathName());
                }
                path = path + ";" + userPath; // NOI18N
                runProfile.getEnvironment().putenv(pi.getPathName(), path);
            } else if (platform == PlatformTypes.PLATFORM_MACOSX) {
                // On Mac OS X we need to add paths to dynamic libraries from subprojects to DYLD_LIBRARY_PATH
                StringBuilder path = new StringBuilder();
                Set<String> subProjectOutputLocations = conf.getSubProjectOutputLocations();
                // Add paths from subprojetcs
                Iterator<String> iter = subProjectOutputLocations.iterator();
                while (iter.hasNext()) {
                    String location = CndPathUtilitities.naturalize(iter.next());
                    if (path.length() > 0) {
                        path.append(":"); // NOI18N
                    }
                    path.append(location);
                }
                // Add paths from -L option
                List<String> list = conf.getLinkerConfiguration().getAdditionalLibs().getValue();
                iter = list.iterator();
                while (iter.hasNext()) {
                    String location = CndPathUtilitities.naturalize(iter.next());
                    if (path.length() > 0) {
                        path.append(":"); // NOI18N
                    }
                    path.append(location);
                }
                if (path.length() > 0) {
                    runProfile = conf.getProfile().clone(conf);
                    String extPath = runProfile.getEnvironment().getenv("DYLD_LIBRARY_PATH"); // NOI18N
                    if (extPath == null) {
                        if (cancelled.get()) {
                            return false; // getEnv() might be costly for remote host
                        }
                        extPath = HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get("DYLD_LIBRARY_PATH"); // NOI18N
                    }
                    if (extPath != null) {
                        path.append(":").append(extPath); // NOI18N
                    }
                    runProfile.getEnvironment().putenv("DYLD_LIBRARY_PATH", path.toString()); // NOI18N
                }
            } else if (platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC || platform == PlatformTypes.PLATFORM_LINUX) {
                // Add paths from -L option
                StringBuilder path = new StringBuilder();
                List<String> list = conf.getLinkerConfiguration().getAdditionalLibs().getValue();
                Iterator<String> iter = list.iterator();
                while (iter.hasNext()) {
                    String location = CndPathUtilitities.naturalize(iter.next());
                    if (path.length() > 0) {
                        path.append(":"); // NOI18N
                    }
                    path.append(location);
                }
                if (path.length() > 0) {
                    runProfile = conf.getProfile().clone(conf);
                    String extPath = runProfile.getEnvironment().getenv("LD_LIBRARY_PATH"); // NOI18N
                    if (extPath == null) {
                        if (cancelled.get()) {
                            return false; // NOI18N
                        }
                        extPath = HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get("LD_LIBRARY_PATH"); // NOI18N
                    }
                    if (extPath != null) {
                        path.append(":").append(extPath); // NOI18N
                    }
                    runProfile.getEnvironment().putenv("LD_LIBRARY_PATH", path.toString()); // NOI18N
                }
            }
            if (platform == PlatformTypes.PLATFORM_MACOSX || platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC || platform == PlatformTypes.PLATFORM_LINUX) {
                // Make sure DISPLAY variable has been set
                if (cancelled.get()) {
                    return false; // getEnv() might be costly for remote host
                }
                if (conf.getDevelopmentHost().getExecutionEnvironment().isLocal() &&
                    HostInfoProvider.getEnv(conf.getDevelopmentHost().getExecutionEnvironment()).get("DISPLAY") == null && // NOI18N
                    conf.getProfile().getEnvironment().getenv("DISPLAY") == null) {// NOI18N
                    // DISPLAY hasn't been set
                    if (runProfile == null) {
                        runProfile = conf.getProfile().clone(conf);
                    }
                    runProfile.getEnvironment().putenv("DISPLAY", ":0.0"); // NOI18N
                }
            }
            MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
            String path;
            if (actionEvent == ProjectActionEvent.PredefinedType.RUN) {
                // naturalize if relative
                path = makeArtifact.getOutput();
                //TODO: we also need remote aware CndPathUtilitities..........
                if (!CndPathUtilitities.isPathAbsolute(path)) {
                    // make path relative to run working directory
                    path = makeArtifact.getWorkingDirectory() + "/" + path; // NOI18N
                    path = CndPathUtilitities.naturalize(path);
                    path = CndPathUtilitities.toRelativePath(conf.getProfile().getRunDirectory(), path);
                    path = CndPathUtilitities.naturalize(path);
                }
            } else {
                // Always absolute
                path = CndPathUtilitities.toAbsolutePath(conf.getBaseDir(), makeArtifact.getOutput());
            }

            // Unit tests
            Folder targetFolder = context.lookup(Folder.class);
            if (targetFolder == null) {
                Node node = context.lookup(Node.class);
                if (node != null) {
                    targetFolder = (Folder) node.getValue("Folder"); // NOI18N
                }
            }
            if (targetFolder != null) {
                if (targetFolder.isTest()) {
                    Item[] items = targetFolder.getAllItemsAsArray();
                    for (int k = 0; k < items.length; k++) {
                        path = items[k].getPath();
                        path = path.replaceFirst("\\..*", ""); // NOI18N

                        path = MakeConfiguration.BUILD_FOLDER + '/' + "${CND_CONF}" + '/' + "${CND_PLATFORM}" + "/" + "tests" + "/" + path; // NOI18N

                        path = conf.expandMacros(path);

                        path = CndPathUtilitities.toAbsolutePath(conf.getBaseDir(), path);

                    }
                }
            }

            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, runProfile, false);
            actionEvents.add(projectActionEvent);
            RunDialogPanel.addElementToExecutablePicklist(path);
        } else {
            assert false;
        }
        return true;
    }

    private boolean onRunSingleStep(MakeConfiguration conf, ArrayList<ProjectActionEvent> actionEvents, Lookup context, Type actionEvent) {
        // FIXUP: not sure this is used...
        if (conf.isMakefileConfiguration()) {
            DataObject d = context.lookup(DataObject.class);
            String path = FileUtil.toFile(d.getPrimaryFile()).getPath();
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, path, conf, null, false);
            actionEvents.add(projectActionEvent);
            RunDialogPanel.addElementToExecutablePicklist(path);
        } else {
            assert false;
        }
        return true;
    }

    private boolean onBuildStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Type actionEvent) {
        if (conf.isCompileConfiguration() && !validateProject(conf)) {
            return true;
        }
        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
        String buildCommand;
        if(actionEvent == ProjectActionEvent.PredefinedType.BUILD_TESTS) {
            buildCommand = makeArtifact.getBuildCommand(getMakeCommand(pd, conf) + " build-tests", ""); // NOI18N
        } else {
            buildCommand = makeArtifact.getBuildCommand(getMakeCommand(pd, conf), ""); // NOI18N
        }
        String args = "";
        int index = buildCommand.indexOf(' ');
        if (index > 0) {
            args = buildCommand.substring(index + 1);
            buildCommand = buildCommand.substring(0, index);
        }
        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform());
        profile.setArgs(args);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onBuildPackageStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, Type actionEvent) {
        if (!validatePackaging(conf)) {
            actionEvents.clear();
            return true;
        }
        String buildCommand;
        String args;
        if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
            buildCommand = "cmd.exe"; // NOI18N
            args = "/c sh "; // NOI18N
        } else {
            buildCommand = "bash"; // NOI18N
            args = "";
        }
        if (conf.getPackagingConfiguration().getVerbose().getValue()) {
            args += " -x "; // NOI18N
        }
        args += "nbproject/Package-" + conf.getName() + ".bash"; // NOI18N
        RunProfile profile = new RunProfile(conf.getBaseDir(), conf.getDevelopmentHost().getBuildPlatform());
        profile.setArgs(args);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onCleanStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Type actionEvent) {
        MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
        String buildCommand = makeArtifact.getCleanCommand(getMakeCommand(pd, conf), ""); // NOI18N
        String args = ""; // NOI18N
        int index = buildCommand.indexOf(' '); // NOI18N
        if (index > 0) {
            args = buildCommand.substring(index + 1);
            buildCommand = buildCommand.substring(0, index);
        }
        RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform());
        profile.setArgs(args);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onExtendedStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, Lookup context, final String extendedStep) {
        Type actionEvent = new MyType(extendedStep);
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, null, conf, null, true, context);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onCompileSingleStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, Type actionEvent) {
        Iterator<? extends Node> it = context.lookupAll(Node.class).iterator();
        while (it.hasNext()) {
            Node node = it.next();
            Item item = getNoteItem(node); // NOI18N
            if (item == null) {
                return false;
            }
            ItemConfiguration itemConfiguration = item.getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
            if (itemConfiguration == null) {
                return false;
            }
            if (itemConfiguration.getExcluded().getValue()) {
                return false;
            }
            if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified()) {
                return false;
            }
            MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
            String outputFile = null;
            if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                CCompilerConfiguration cCompilerConfiguration = itemConfiguration.getCCompilerConfiguration();
                outputFile = cCompilerConfiguration.getOutputFile(item, conf, true);
            } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                CCCompilerConfiguration ccCompilerConfiguration = itemConfiguration.getCCCompilerConfiguration();
                outputFile = ccCompilerConfiguration.getOutputFile(item, conf, true);
            } else if (itemConfiguration.getTool() == PredefinedToolKind.FortranCompiler) {
                FortranCompilerConfiguration fortranCompilerConfiguration = itemConfiguration.getFortranCompilerConfiguration();
                outputFile = fortranCompilerConfiguration.getOutputFile(item, conf, true);
            } else if (itemConfiguration.getTool() == PredefinedToolKind.Assembler) {
                AssemblerConfiguration assemblerConfiguration = itemConfiguration.getAssemblerConfiguration();
                outputFile = assemblerConfiguration.getOutputFile(item, conf, true);
            } else if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
                CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                outputFile = customToolConfiguration.getOutputs().getValue();
            }
            outputFile = conf.expandMacros(outputFile);
            // Clean command
            String commandLine;
            String args;
            if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                commandLine = "cmd.exe"; // NOI18N
                args = "/c rm -rf " + outputFile; // NOI18N
            } else {
                commandLine = "rm"; // NOI18N
                args = "-rf " + outputFile; // NOI18N
            }
            RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform());
            profile.setArgs(args);
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, ProjectActionEvent.PredefinedType.CLEAN, commandLine, conf, profile, true);
            actionEvents.add(projectActionEvent);
            // Build commandLine
            commandLine = getMakeCommand(pd, conf) + " -f nbproject" + '/' + "Makefile-" + conf.getName() + ".mk " + outputFile; // Unix path // NOI18N
            args = ""; // NOI18N
            int index = commandLine.indexOf(' '); // NOI18N
            if (index > 0) {
                args = commandLine.substring(index + 1);
                commandLine = commandLine.substring(0, index);
            }
            profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform());
            profile.setArgs(args);
            projectActionEvent = new ProjectActionEvent(project, actionEvent, commandLine, conf, profile, true);
            actionEvents.add(projectActionEvent);
        }
        return true;
    }

    private boolean onTestSingleStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfigurationDescriptor pd, MakeConfiguration conf, Lookup context, Type actionEvent) {

        if (actionEvent == ProjectActionEvent.PredefinedType.TEST) {
            if (conf.isCompileConfiguration() && !validateProject(conf)) {
                return true;
            }
            
            Folder targetFolder = context.lookup(Folder.class);
            if (targetFolder == null) {
                Node node = context.lookup(Node.class);
                if (node == null) {
                    return true;
                }
                targetFolder = (Folder) node.getValue("Folder"); // NOI18N
            }
            if (targetFolder == null) {
                return true;
            }

            List<Folder> list = targetFolder.getAllTests();
            if (targetFolder.isTest()) {
                list.add(targetFolder);
            }

            loop: for (Folder folder : list) {
                Item[] items = folder.getAllItemsAsArray();
                for (int k = 0; k < items.length; k++) {
                    String test = items[k].getName();
                    test = test.replaceFirst("\\..*", ""); // NOI18N

                    MakeArtifact makeArtifact = new MakeArtifact(pd, conf);
                    String buildCommand;
                    buildCommand = makeArtifact.getBuildCommand(getMakeCommand(pd, conf) + " test TEST=" + test, ""); // NOI18N
                    String args = "";
                    int index = buildCommand.indexOf(' ');
                    if (index > 0) {
                        args = buildCommand.substring(index + 1);
                        buildCommand = buildCommand.substring(0, index);
                    }
                    RunProfile profile = new RunProfile(makeArtifact.getWorkingDirectory(), conf.getDevelopmentHost().getBuildPlatform());
                    profile.setArgs(args);
                    ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, buildCommand, conf, profile, true);
                    actionEvents.add(projectActionEvent);

                    break loop;
                }
            }
        }

        return true;
    }

    private boolean onCustomActionStep(ArrayList<ProjectActionEvent> actionEvents, MakeConfiguration conf, Lookup context, Type actionEvent) {
        String exe = conf.getAbsoluteOutputValue();
        ProjectActionEvent projectActionEvent = new ProjectActionEvent(project, actionEvent, exe, conf, null, true, context);
        actionEvents.add(projectActionEvent);
        return true;
    }

    private boolean onValidateToolchainStep(MakeConfigurationDescriptor pd, MakeConfiguration conf, AtomicBoolean cancelled, AtomicBoolean validated) {
        if (!validateBuildSystem(pd, conf, validated.get(), cancelled)) {
            return false; // Stop here
        }
        validated.set(true);
        return true;
    }

    private boolean validateProject(MakeConfiguration conf) {
        boolean ret = false;

        if (getProjectDescriptor().getProjectItems().length == 0) {
            ret = false;
        } else {
            for (int i = 0; i < getProjectDescriptor().getProjectItems().length; i++) {
                Item item = getProjectDescriptor().getProjectItems()[i];
                ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);
                if (!itemConfiguration.getExcluded().getValue() &&
                        (itemConfiguration.getTool() != PredefinedToolKind.CustomTool || itemConfiguration.getCustomToolConfiguration().getCommandLine().getValue().length() > 0)) {
                    ret = true;
                    break;
                }
            }
        }

        if (!ret) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(getString("ERR_EMPTY_PROJECT"), NotifyDescriptor.ERROR_MESSAGE)); // NOI18N
        }

        return ret;
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    private String[] getTargetNames(String command) throws IllegalArgumentException {
        String[] targetNames = new String[0];
        if (command.equals(COMMAND_COMPILE_SINGLE)) {
            targetNames = commands.get(command);
        } else if (command.equals(COMMAND_RUN) ||
                command.equals(COMMAND_DEBUG) ||
                command.equals(COMMAND_DEBUG_STEP_INTO) ||
                command.equals(COMMAND_DEBUG_LOAD_ONLY) ||
                command.equals(COMMAND_CUSTOM_ACTION)) {
            MakeConfigurationDescriptor pd = getProjectDescriptor();
            MakeConfiguration conf = pd.getActiveConfiguration();
            if (conf == null) {
                return null;
            }
            RunProfile profile = (RunProfile) conf.getAuxObject(RunProfile.PROFILE_ID);
            if (profile == null) { // See IZ 89349
                return null;
            }
            if (profile.getBuildFirst()) {
                targetNames = commands.get(command);
            } else {
                targetNames = commandsNoBuild.get(command);
            }
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else if (command.equals(COMMAND_RUN_SINGLE) || command.equals(COMMAND_DEBUG_SINGLE)) {
            targetNames = commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        } else {
            targetNames = commands.get(command);
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        return targetNames;
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) {
        if (!isProjectDescriptorLoaded()) {
            return false;
        }
        MakeConfiguration conf = getProjectDescriptor().getActiveConfiguration();
        if (conf == null) {
            return false;
        }
        if (command.equals(COMMAND_CLEAN)) {
            return true;
        } else if (command.equals(COMMAND_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_BUILD_PACKAGE)) {
            return true;
        } else if (command.equals(COMMAND_BATCH_BUILD)) {
            return true;
        } else if (command.equals(COMMAND_REBUILD)) {
            return true;
        } else if (command.equals(COMMAND_RUN)) {
            return !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG)) {
            return conf.hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG_STEP_INTO)) {
            return conf.hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_DEBUG_LOAD_ONLY)) {
            return conf.hasDebugger() && !conf.isLibraryConfiguration();
        } else if (command.equals(COMMAND_COMPILE_SINGLE)) {
            boolean enabled = true;
            Iterator<? extends Node> it = context.lookupAll(Node.class).iterator();
            while (it.hasNext()) {
                Node node = it.next();
                Item item = getNoteItem(node);
                if (item == null) {
                    return false;
                }
                ItemConfiguration itemConfiguration = item.getItemConfiguration(conf);//ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(item.getPath()));
                if (itemConfiguration == null) {
                    return false;
                }
                if (itemConfiguration.getExcluded().getValue()) {
                    return false;
                }
                if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool && !itemConfiguration.getCustomToolConfiguration().getModified()) {
                    return false;
                }
                if (conf.isMakefileConfiguration()) {
                    return false;
                }
            }
            return enabled;
        } else if (command.equals(COMMAND_DELETE) ||
                command.equals(COMMAND_COPY) ||
                command.equals(COMMAND_MOVE) ||
                command.equals(COMMAND_RENAME)) {
            return true;
        } else if (command.equals(COMMAND_RUN_SINGLE)) {
            Node node = context.lookup(Node.class);
            return (node != null) && (node.getCookie(ShellExecSupport.class) != null);
        } else if (command.equals(COMMAND_TEST)) {
            return true;
        } else {
            return false;
        }
    }

    private Item getNoteItem(Node node) {
        Item item = (Item) node.getValue("Item"); // NOI18N
        if (item == null) {
            // try to find Item in associated data object if any
            try {
                DataObject dao = node.getCookie(DataObject.class);
                if (dao != null) {
                    File file = FileUtil.toFile(dao.getPrimaryFile());
                    item = getProjectDescriptor().findItemByFile(file);
                }
            } catch (NullPointerException ex) {
                // not found item
            }
        }
        return item;
    }

    private static String getMakeCommand(MakeConfigurationDescriptor pd, MakeConfiguration conf) {
        String cmd = null;
        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
        if (cs != null) {
            cmd = cs.getTool(PredefinedToolKind.MakeTool).getPath();
        } else {
            assert false;
            cmd = "make"; // NOI18N
        }
        //cmd = cmd + " " + MakeOptions.getInstance().getMakeOptions(); // NOI18N
        return cmd;
    }

    private List<String> validateStep(String id, List<String> tailSteps){
        StepController validator = StepControllerProvider.getController(id);
        if (validator == null) {
            return null;
        }
        return validator.validate(project, id, tailSteps);
    }

    private boolean validateBuildSystem(MakeConfigurationDescriptor pd, MakeConfiguration conf,
            boolean validated, AtomicBoolean cancelled) {
        CompilerSet2Configuration csconf = conf.getCompilerSet();
        ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(conf.getDevelopmentHost().getHostKey());
        ArrayList<String> errs = new ArrayList<String>();
        CompilerSet cs;
        String csname;
        File file;
        boolean cRequired = conf.hasCFiles(pd);
        boolean cppRequired = conf.hasCPPFiles(pd);
        boolean fRequired = conf.hasFortranFiles(pd);
        boolean asRequired = conf.hasAssemblerFiles(pd);
        boolean runBTA = false;

        if (validated) {
            return lastValidation;
        }

        if (!conf.getDevelopmentHost().isLocalhost()) {
            ServerRecord record = ServerList.get(env);
            assert record != null;
            record.validate(false);
            if (cancelled.get()) {
                return false;
            }
            if (!record.isOnline()) {
                lastValidation = false;
                runBTA = true;
            }
            // TODO: all validation below works, but it may be more efficient to make a verifying script
        }

        // Check build/run/debug platform vs. host platform
        int buildPlatformId = conf.getDevelopmentHost().getBuildPlatform();
        ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        int hostPlatformId = CompilerSetManager.get(execEnv).getPlatform();
        if (buildPlatformId != hostPlatformId) {
            if (!conf.isMakefileConfiguration()) {
                Platform buildPlatform = Platforms.getPlatform(buildPlatformId);
                Platform hostPlatform = Platforms.getPlatform(hostPlatformId);
                String errormsg = getString("WRONG_PLATFORM", hostPlatform.getDisplayName(), buildPlatform.getDisplayName());
                if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(errormsg, NotifyDescriptor.WARNING_MESSAGE)) != NotifyDescriptor.OK_OPTION) {
                    return false;
                }
            }
            conf.getDevelopmentHost().setBuildPlatform(hostPlatformId);
        }

        if (csconf.getFlavor() != null && csconf.getFlavor().equals("Unknown")) { // NOI18N
            // Confiiguration was created with unknown tool set. Use the now default one.
            csname = csconf.getOption();
            cs = CompilerSetManager.get(env).getCompilerSet(csname);
            if (cs == null) {
                cs = CompilerSetManager.get(env).getDefaultCompilerSet();
            }
            errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_UnknownCompiler", csname)); // NOI18N
            runBTA = true;
        } else if (csconf.isValid()) {
            csname = csconf.getOption();
            cs = CompilerSetManager.get(env).getCompilerSet(csname);
        } else {
            csname = csconf.getOldName();
            CompilerFlavor flavor = null;
            if (csconf.getFlavor() != null) {
                flavor = CompilerFlavor.toFlavor(csconf.getFlavor(), conf.getPlatformInfo().getPlatform());
            }
            if (flavor == null) {
                flavor = CompilerFlavor.getUnknown(conf.getPlatformInfo().getPlatform());
            }
            cs = CompilerSetFactory.getCompilerSet(env, flavor, csname);
            csconf.setValid();
        }

        Tool cTool = cs.getTool(PredefinedToolKind.CCompiler);
        Tool cppTool = cs.getTool(PredefinedToolKind.CCCompiler);
        Tool fTool = cs.getTool(PredefinedToolKind.FortranCompiler);
        Tool asTool = cs.getTool(PredefinedToolKind.Assembler);
        Tool makeTool = cs.getTool(PredefinedToolKind.MakeTool);
        Tool qmakeTool = cs.getTool(PredefinedToolKind.QMakeTool);

        if (cancelled.get()) {
            return false;
        }

        PlatformInfo pi = conf.getPlatformInfo();
        // Check for a valid make program
        if (conf.getDevelopmentHost().isLocalhost()) {
            file = new File(makeTool.getPath());
            if ((!exists(makeTool.getPath(), pi) && Path.findCommand(makeTool.getPath()) == null) || ToolsPanelSupport.isUnsupportedMake(file.getPath())) {
                runBTA = true;
            }
        } else {
            if (!isValidExecutable(makeTool.getPath(), pi)) {
                runBTA = true;
            }
        }

        // Check compilers
        if (cancelled.get()) {
            return false;
        }
        if (cRequired && !exists(cTool.getPath(), pi)) {
            //errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_MissingCCompiler", csname, cTool.getDisplayName())); // NOI18N
            runBTA = true;
        }
        if (cancelled.get()) {
            return false;
        }
        if (cppRequired && !exists(cppTool.getPath(), pi)) {
            //errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_MissingCppCompiler", csname, cppTool.getDisplayName())); // NOI18N
            runBTA = true;
        }
        if (cancelled.get()) {
            return false;
        }
        if (fRequired && !exists(fTool.getPath(), pi)) {
            //errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_MissingFortranCompiler", csname, fTool.getDisplayName())); // NOI18N
            runBTA = true;
        }
        if (cancelled.get()) {
            return false;
        }
        if (asRequired && !exists(asTool.getPath(), pi)) {
            //errs.add(NbBundle.getMessage(MakeActionProvider.class, "ERR_MissingFortranCompiler", csname, fTool.getDisplayName())); // NOI18N
            runBTA = true;
        }
        if (conf.isQmakeConfiguration() && !exists(qmakeTool.getPath(), pi)) {
            runBTA = true;
        }

        if (cancelled.get()) {
            return false;
        }

        // user counting mode
        if (cs.getCompilerFlavor().isSunStudioCompiler() && !CndUtils.isUnitTestMode()) {
            SunStudioUserCounter.countIDE(cs.getDirectory(), execEnv);
        }
        if (runBTA) {
            if (CndUtils.isUnitTestMode()) {
                // do not show any dialogs in unit test mode, just silently fail validation
                lastValidation = false;
            } else if (conf.getDevelopmentHost().isLocalhost()) {
                BuildToolsAction bt = SystemAction.get(BuildToolsAction.class);
                bt.setTitle(NbBundle.getMessage(BuildToolsAction.class, "LBL_ResolveMissingTools_Title")); // NOI18N
                ToolsPanelModel model = new LocalToolsPanelModel();
                model.setSelectedDevelopmentHost(env); // only localhost until BTA becomes more functional for remote sets
                model.setEnableDevelopmentHostChange(false);
                model.setCompilerSetName(null); // means don't change
                model.setSelectedCompilerSetName(csname);
                model.setMakeRequired(true);
                model.setDebuggerRequired(false);
                model.setCRequired(cRequired);
                model.setCppRequired(cppRequired);
                model.setFortranRequired(fRequired);
                model.setAsRequired(asRequired);
                model.setShowRequiredBuildTools(true);
                model.setShowRequiredDebugTools(false);
                model.setEnableRequiredCompilerCB(conf.isMakefileConfiguration());
                if (bt.initBuildTools(model, errs, cs) && pd.okToChange()) {
                    String name = model.getSelectedCompilerSetName();
                    ToolsPanelModel.resetCompilerSetName(name);
                    conf.getCRequired().setValue(model.isCRequired());
                    conf.getCppRequired().setValue(model.isCppRequired());
                    conf.getFortranRequired().setValue(model.isFortranRequired());
                    conf.getAssemblerRequired().setValue(model.isAsRequired());
                    conf.getCompilerSet().setValue(name);
                    pd.setModified();
                    pd.save();
                    lastValidation = true;
                } else {
                    lastValidation = false;
                }
            } else {
                if (cancelled.get()) {
                    return false;
                }
                // User can't change anything in BTA for remote host yet,
                // so showing above dialog will only confuse him
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        NbBundle.getMessage(MakeActionProvider.class, "ERR_INVALID_COMPILER_SET",
                        csname, conf.getDevelopmentHost().getDisplayName(false)));
                DialogDisplayer.getDefault().notify(nd);
                lastValidation = false;
            }
        } else {
            lastValidation = true;
        }

        if (lastValidation == true) {
            if (cs.getCompilerFlavor().isCygwinCompiler()) {
                Shell activeShell = WindowsSupport.getInstance().getActiveShell();
                ShellValidationStatus shellValidationStatus = ShellValidationSupport.getValidationStatus(activeShell);
                boolean isOK = shellValidationStatus.isValid() && !shellValidationStatus.hasWarnings();

                if (!isOK) {
                    String binDir = cs.getDirectory();

                    if (activeShell == null || !binDir.equals(activeShell.bindir.getAbsolutePath())) {
                        // Perhaps one that is provided is better?
                        WindowsSupport.getInstance().init(binDir);
                        activeShell = WindowsSupport.getInstance().getActiveShell();
                        shellValidationStatus = ShellValidationSupport.getValidationStatus(activeShell);
                        isOK = shellValidationStatus.isValid() && !shellValidationStatus.hasWarnings();
                    }
                }

                if (!isOK) {
                    lastValidation = ShellValidationSupport.confirm(shellValidationStatus);
                }
            }
        }

        return lastValidation;
    }
    
    private boolean validatePackaging(MakeConfiguration conf) {
        String errormsg = null;

        if (conf.getPackagingConfiguration().getFiles().getValue().isEmpty()) {
            errormsg = getString("ERR_EMPTY_PACKAGE");
        }

        if (PackagerManager.getDefault().getPackager(conf.getPackagingConfiguration().getType().getValue()) == null) {
            errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL4", conf.getPackagingConfiguration().getType().getValue()); // NOI18N
        }

        // Don;t verify the tool. It is too difficult to get right (148728)
//        if (errormsg == null) {
//            String tool = conf.getPackagingConfiguration().getToolValue();
//            if (conf.getDevelopmentHost().isLocalhost()) {
//                if (!CndPathUtilitities.isPathAbsolute(tool) && Path.findCommand(tool) == null) {
//                    errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL1", tool); // NOI18N
//                } else if (CndPathUtilitities.isPathAbsolute(tool) && !(new File(tool).exists())) {
//                    errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL2", tool); // NOI18N
//                }
//            } else {
//                ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
//                ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
//                if (serverList != null) {
//                    if (!serverList.isValidExecutable(execEnv, tool)) {
//                        errormsg = NbBundle.getMessage(MakeActionProvider.class, "ERR_MISSING_TOOL3", tool, execEnv.getHost()); // NOI18N
//                    }
//                }
//            }
//        }

        if (errormsg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
            if (conf.getPackagingConfiguration().getFiles().getValue().isEmpty()) {
                MakeCustomizerProvider makeCustomizerProvider = project.getLookup().lookup(MakeCustomizerProvider.class);
                if (makeCustomizerProvider != null) {
                    makeCustomizerProvider.showCustomizer("Packaging"); // NOI18N
                }
            }
            return false;
        }

        return true;
    }

    /** cache for file existence status. */
    private static Map<String, Boolean> fileExistenceCache = new HashMap<String, Boolean>();

    /** cache for valid executables */
    private static Map<String, Boolean> validExecutablesCache = new HashMap<String, Boolean>();

    private static boolean isValidExecutable(String path, PlatformInfo pi) {
        return existsImpl(path, pi, true);
    }
    private static boolean exists(String path, PlatformInfo pi) {
        return existsImpl(path, pi, false);
    }

    private static boolean existsImpl(String path, PlatformInfo pi, boolean checkExecutable) {
        ExecutionEnvironment execEnv = pi.getExecutionEnvironment();
        String key = path + ExecutionEnvironmentFactory.toUniqueID(execEnv);
        Map<String, Boolean> map = checkExecutable ? validExecutablesCache : fileExistenceCache;

        synchronized (map) {
            Boolean cached = map.get(key);
            if (cached != null && cached.booleanValue()) {
                return true;
            }
        }

        boolean result;
        if (checkExecutable) {
            result = ServerList.isValidExecutable(execEnv, path);
        } else {
            result = pi.fileExists(path) || pi.isWindows() && pi.fileExists(path+".lnk") || pi.findCommand(path) != null; // NOI18N
        }

        if (result) {
            synchronized (map) {
                map.put(key, Boolean.TRUE);
            }
        }
        return result;
    }

    // Private methods -----------------------------------------------------
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeActionProvider.class, s);
    }
    private static String getString(String s, String arg1, String arg2) {
        return NbBundle.getMessage(MakeActionProvider.class, s, arg1, arg2);
    }

    private abstract static class CancellableTask implements Runnable, Cancellable {

        protected abstract void runImpl();

        @Override
        public final void run() {
            thread = Thread.currentThread();
            if (!cancelled.get()) {
                runImpl();
            }
        }

        @Override
        public boolean cancel() {
            cancelled.set(true);
            if (thread != null) { // we never set it back to null => no sync
                thread.interrupt();
            }
            return true;
        }

        private volatile Thread thread;
        protected final AtomicBoolean cancelled = new AtomicBoolean(false);
    }

    private static class BatchConfigurationSelector implements ActionListener {

        private JButton buildButton = new JButton(getString("BuildButton"));
        private JButton rebuildButton = new JButton(getString("CleanBuildButton"));
        private JButton cleanButton = new JButton(getString("CleanButton"));
        private JButton closeButton = new JButton(getString("CloseButton"));
        private ConfSelectorPanel confSelectorPanel;
        private String command = null;
        private Dialog dialog = null;

        BatchConfigurationSelector(MakeProject project, Configuration[] confs) {
            confSelectorPanel = new ConfSelectorPanel(getString("CheckLabel"), getString("CheckLabelMn").charAt(0), confs, new JButton[]{buildButton, rebuildButton, cleanButton});

            String dialogTitle = MessageFormat.format(getString("BatchBuildTitle"), // NOI18N
                    new Object[]{ProjectUtils.getInformation(project).getDisplayName()});

            buildButton.setMnemonic(getString("BuildButtonMn").charAt(0));
            buildButton.getAccessibleContext().setAccessibleDescription(getString("BuildButtonAD"));
            buildButton.addActionListener(BatchConfigurationSelector.this);
            rebuildButton.setMnemonic(getString("CleanBuildButtonMn").charAt(0));
            rebuildButton.addActionListener(BatchConfigurationSelector.this);
            rebuildButton.getAccessibleContext().setAccessibleDescription(getString("CleanBuildButtonAD"));
            cleanButton.setMnemonic(getString("CleanButtonMn").charAt(0));
            cleanButton.addActionListener(BatchConfigurationSelector.this);
            cleanButton.getAccessibleContext().setAccessibleDescription(getString("CleanButtonAD"));
            closeButton.getAccessibleContext().setAccessibleDescription(getString("CloseButtonAD"));
            // Show the dialog
            DialogDescriptor dd = new DialogDescriptor(confSelectorPanel, dialogTitle, true, new Object[]{closeButton}, closeButton, 0, null, null);
            //DialogDisplayer.getDefault().notify(dd);
            dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.getAccessibleContext().setAccessibleDescription(getString("BatchBuildDialogAD"));
            dialog.setVisible(true);
        }

        public Configuration[] getSelectedConfs() {
            return confSelectorPanel.getSelectedConfs();
        }

        public String getCommand() {
            return command;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == buildButton) {
                command = COMMAND_BUILD;
            } else if (evt.getSource() == rebuildButton) {
                command = COMMAND_REBUILD;
            } else if (evt.getSource() == cleanButton) {
                command = COMMAND_CLEAN;
            } else {
                assert false;
            }
            dialog.dispose();
        }
    }

    private static final class MyType implements Type {

        private final String extendedStep;
        private String locName;

        private MyType(String extendedStep) {
            this.extendedStep = extendedStep;
            locName = extendedStep;
        }

        @Override
        public int ordinal() {
            return extendedStep.hashCode();
        }

        @Override
        public String name() {
            return extendedStep;
        }

        @Override
        public String getLocalizedName() {
            return locName;
        }

        @Override
        public void setLocalizedName(String name) {
            locName = name;
        }
    }
}
