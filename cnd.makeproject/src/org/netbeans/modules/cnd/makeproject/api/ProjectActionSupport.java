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
package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectChangeSupport;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.BuildAction;
import org.netbeans.modules.cnd.makeproject.api.BuildActionsProvider.OutputStreamHandler;
import org.netbeans.modules.cnd.makeproject.api.IOTabsController.InputOutputTab;
import org.netbeans.modules.cnd.makeproject.api.IOTabsController.TabsGroup;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.PredefinedType;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent.Type;
import org.netbeans.modules.cnd.makeproject.api.configurations.DebuggerChooserConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.cnd.makeproject.ui.SelectExecutablePanel;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.api.terminal.TerminalSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;

/**
 * Most of the code here came from DefaultProjectActionHandler as result of
 * refactoring.
 */
public class ProjectActionSupport {

    private static ProjectActionSupport instance;
    private static final RequestProcessor RP = new RequestProcessor("ProjectActionSupport.refresh", 1); // NOI18N
    private static final RequestProcessor RPgo = new RequestProcessor("ProjectActionSupport.go", 1); // NOI18N
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    private final List<ProjectActionHandlerFactory> handlerFactories;

    private ProjectActionSupport() {
        handlerFactories = new ArrayList<ProjectActionHandlerFactory>(
                Lookup.getDefault().lookupAll(ProjectActionHandlerFactory.class));
    }

    /**
     * Singleton pattern: instance getter.
     *
     * @return singleton instance
     */
    public static synchronized ProjectActionSupport getInstance() {
        if (instance == null) {
            instance = new ProjectActionSupport();
        }
        return instance;
    }

    private static boolean isFileOperationsIntensive(ProjectActionEvent pae) {
        Type type = pae.getType();
        if (type == PredefinedType.BUILD || type == PredefinedType.CLEAN || type == PredefinedType.BUILD_TESTS) {
            return true;
        }
        return false;
    }

    private static void refreshProjectFilesOnFinish(final ProjectActionEvent curPAE, final FileOperationsNotifier fon) {
        try {
            if (curPAE.getType() != PredefinedType.RUN && !fon.isLastExpectedEvent(curPAE)) {
                return;
            }
            final Project project = curPAE.getProject();
            final Set<File> files = new HashSet<File>();
            final Set<FileObject> fileObjects = new HashSet<FileObject>();
            FileObject projectFileObject = project.getProjectDirectory();
            File f = FileUtil.toFile(projectFileObject);
            if (f != null) {
                files.add(f);
            } else {
                fileObjects.add(projectFileObject);
            }
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for (SourceGroup sourceGroup : groups) {
                FileObject rootFolder = sourceGroup.getRootFolder();
                File file = FileUtil.toFile(rootFolder);
                if (file != null) {
                    files.add(file);
                } else {
                    fileObjects.add(rootFolder);
                }
            }
            // IZ#201761  -  Too long refreshing file system after build.
            // refresh can take a lot of time for slow file systems
            // so we use worker and schedule it out of build process if auto refresh
            // is turned off by user in Tools->Options->Misk->Files->Enable auto-scanning of sources
            final Runnable refresher = new Runnable() {

                @Override
                public void run() {
                    final File[] array = files.toArray(new File[files.size()]);
                    if (array.length > 0) {
                        FileUtil.refreshFor(array);
                    }
                    if (!fileObjects.isEmpty()) {
                        for (FileObject fo : fileObjects) {
                            FileSystemProvider.scheduleRefresh(fo);
                        }
                    }
                }
            };
            final Preferences nd = NbPreferences.root().node("org/openide/actions/FileSystemRefreshAction"); // NOI18N
            boolean manual = (nd != null) && nd.getBoolean("manual", false);// NOI18N
            if (manual) {
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        FileUtil.runAtomicAction(refresher);
                        fon.onFinish(curPAE);
                        MakeLogicalViewProvider.refreshBrokenItems(project);
                    }
                });
            } else {
                FileUtil.runAtomicAction(refresher);
                fon.onFinish(curPAE);
                MakeLogicalViewProvider.refreshBrokenItems(project);
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot refresh project files", e);
        }
    }

    /**
     * Checks if given action type can be handled. All registered handler
     * factories are asked.
     *
     * @param conf
     * @param type
     * @return
     */
    public boolean canHandle(MakeConfiguration conf, Lookup context, ProjectActionEvent.Type type) {
        if (conf != null) {
            DebuggerChooserConfiguration chooser = conf.getDebuggerChooserConfiguration();
            CustomizerNode node = chooser.getNode();
            if (node instanceof ProjectActionHandlerFactory) {
                if (((ProjectActionHandlerFactory) node).canHandle(type, context, conf)) {
                    return true;
                }
            }
        }
        for (ProjectActionHandlerFactory factory : handlerFactories) {
            if (factory.canHandle(type, context, conf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes an array of project actions asynchronously.
     *
     * @param paes project actions
     */
    public void fireActionPerformed(ProjectActionEvent[] paes) {
        new HandleEvents(paes, null).go();
    }

    public void fireActionPerformed(ProjectActionEvent[] paes, ProjectActionHandler preferredHandler) {
        new HandleEvents(paes, preferredHandler).go();
    }
////////////////////////////////////////////////////////////////////////////////
    private final IOTabsController tabs = IOTabsController.getDefault();

    private final class ProjectFileOperationsNotifier {

        private final NativeProjectChangeSupport npcs;
        private final ProjectActionEvent startPAE;
        private ProjectActionEvent finishPAE;

        public ProjectFileOperationsNotifier(NativeProjectChangeSupport npcs, ProjectActionEvent startPAE) {
            this.npcs = npcs;
            this.startPAE = startPAE;
        }

        @Override
        public String toString() {
            return "ProjectFileOperationsNotifier{" + "npcs=" + npcs + ", startPAE=" + startPAE + ", finishPAE=" + finishPAE + '}'; // NOI18N
        }
    }

    private final class FileOperationsNotifier {

        private final Map<Project, ProjectFileOperationsNotifier> prjNotifier;

        public FileOperationsNotifier(Map<Project, ProjectFileOperationsNotifier> prjNotifier) {
            this.prjNotifier = prjNotifier;
        }

        private void onStart(ProjectActionEvent curPAE) {
            ProjectFileOperationsNotifier notifier = prjNotifier.get(curPAE.getProject());
            if (notifier != null && (notifier.npcs != null) && (curPAE == notifier.startPAE)) {
                notifier.npcs.fireFileOperationsStarted();
            }
        }

        public void onFinish(ProjectActionEvent curPAE) {
            ProjectFileOperationsNotifier notifier = prjNotifier.get(curPAE.getProject());
            if (notifier != null && (notifier.npcs != null) && (curPAE == notifier.finishPAE)) {
                notifier.npcs.fireFileOperationsFinished();
            }
        }

        private void finishAll() {
            for (ProjectFileOperationsNotifier notifier : prjNotifier.values()) {
                if (notifier.npcs != null) {
                    notifier.npcs.fireFileOperationsFinished();
                }
            }
        }

        private boolean isLastExpectedEvent(ProjectActionEvent curPAE) {
            ProjectFileOperationsNotifier notifier = prjNotifier.get(curPAE.getProject());
            if (notifier != null && (notifier.npcs != null) && (curPAE == notifier.finishPAE)) {
                return true;
            }
            return false;
        }
    }

    private final class HandleEvents implements ExecutionListener {

        private final TabsGroup tabs;
        private final ProjectActionEvent[] paes;
        private final AtomicInteger currentActionIdx = new AtomicInteger(-1);
        private final StopAction sa = new StopAction(this);
        private final RerunAction ra = new RerunAction(this);
        private final TermAction ta = new TermAction(this);
        private List<BuildAction> additional;
        private ProgressHandle progressHandle = null;
        private InputOutputTab ioTab = null;
        private final ProjectActionHandler customHandler;
        private ProjectActionHandler currentHandler = null;
        private final FileOperationsNotifier fon;

        public HandleEvents(ProjectActionEvent[] paes, ProjectActionHandler customHandler) {
            this.paes = paes;
            this.customHandler = customHandler;
            fon = getFileOperationsNotifier(paes);
            tabs = IOTabsController.getDefault().openTabsGroup(getTabName(paes), MakeOptions.getInstance().getReuse());
        }

        private Action[] getActions(String name) {
            List<Action> list = new ArrayList<Action>();
            list.add(sa);
            list.add(ra);
            list.add(ta);
            if (additional == null) {
                additional = BuildActionsProvider.getDefault().getActions(name, paes);
            }
            // TODO: actions should have acces to output writer. Action should listen output writer.
            // Provide parameter outputListener for DefaultProjectActionHandler.ProcessChangeListener
            list.addAll(additional);
            return list.toArray(new Action[list.size()]);
        }

        private String getTabName(ProjectActionEvent[] paes) {
            String projectName = ProjectUtils.getInformation(paes[0].getProject()).getDisplayName();
            StringBuilder name = new StringBuilder(projectName);
            name.append(" ("); // NOI18N
            for (int i = 0; i < paes.length; i++) {
                if (i >= 2) {
                    name.append("..."); // NOI18N
                    break;
                }
                name.append(paes[i].getActionName());
                if (i < paes.length - 1) {
                    name.append(", "); // NOI18N
                }
            }
            name.append(")"); // NOI18N
            if (paes.length > 0) {
                MakeConfiguration conf = paes[0].getConfiguration();
                if (!conf.getDevelopmentHost().isLocalhost()) {
                    String hkey = conf.getDevelopmentHost().getHostKey();
                    name.append(" - ").append(hkey); //NOI18N
                }
            }
            return name.toString();
        }

        private ProgressHandle createProgressHandle(final InputOutputTab ioTab) {
            ProgressHandle handle = ProgressHandleFactory.createHandle(ioTab.getName(), new Cancellable() {

                @Override
                public boolean cancel() {
                    sa.actionPerformed(null);
                    return true;
                }
            }, new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ioTab.select();
                }
            });
            return handle;
        }

        private ProgressHandle createProgressHandleNoCancel(final InputOutputTab ioTab) {
            ProgressHandle handle = ProgressHandleFactory.createHandle(ioTab.getName(),
                    new AbstractAction() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            ioTab.select();
                        }
                    });
            return handle;
        }

        private void reRun() {
            final Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    currentActionIdx.set(-1);
                    tabs.resetIO();
                    go();
                }
            };

            if (SwingUtilities.isEventDispatchThread()) {
                RPgo.post(runnable);
            } else {
                go();
            }
        }

        private void stopProgress() {
            if (progressHandle != null) {
                progressHandle.finish();
            }
            if (ioTab != null) {
                ioTab.closeOutput();
            }
            fon.finishAll();
        }

        private void go() {
            currentHandler = null;
            sa.setEnabled(false);
            ra.setEnabled(false);
            int currentAction = currentActionIdx.incrementAndGet();

            if (currentAction == 0) {
                LifecycleManager.getDefault().saveAll();
            }

            if (currentAction >= paes.length) {
                return;
            }

            final ProjectActionEvent pae = paes[currentAction];

            if (!checkProject(pae)) {
                stopProgress();
                return;
            }
            Type type = pae.getType();

            // Validate executable
            boolean isRunAction = (type == PredefinedType.RUN
                    || type == PredefinedType.DEBUG
                    || type == PredefinedType.DEBUG_STEPINTO
                    || type == PredefinedType.DEBUG_TEST
                    || type == PredefinedType.DEBUG_STEPINTO_TEST
                    || type == PredefinedType.CUSTOM_ACTION);

            if ((isRunAction || type == PredefinedType.CHECK_EXECUTABLE)) {
                if (!checkExecutable(pae)) {
                    stopProgress();
                    return;
                }
            }

            int consoleType = pae.getProfile().getConsoleType().getValue();
            if (consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL
                    && !pae.getConfiguration().getDevelopmentHost().isLocalhost()) {
                consoleType = RunProfile.CONSOLE_TYPE_DEFAULT;
            }

            if (consoleType == RunProfile.CONSOLE_TYPE_DEFAULT) {
                consoleType = RunProfile.getDefaultConsoleType();
            }

            InputOutputTab previousTab = ioTab;
            Action[] actions = getActions(pae.getActionName());

            if (isRunAction && consoleType == RunProfile.CONSOLE_TYPE_INTERNAL) {
                ioTab = tabs.getTab(IOProvider.get("Terminal"), getTabName(new ProjectActionEvent[]{pae}), actions); // NOI18N
            } else {
                ioTab = tabs.getTab(IOProvider.getDefault(), getTabName(paes), actions); // NOI18N
            }

            if (previousTab != null && previousTab != ioTab) {
                previousTab.closeOutput();
            }

            if (progressHandle != null) {
                progressHandle.finish();
            }

            ProjectActionHandler handlerToUse = null;
            if (type == PredefinedType.CUSTOM_ACTION && customHandler != null) {
                handlerToUse = customHandler;
            } else {
                for (ProjectActionHandlerFactory factory : handlerFactories) {
                    if (factory.canHandle(pae)) {
                        handlerToUse = factory.createHandler();
                        break;
                    }
                }
            }
            if (handlerToUse != null) {
                currentHandler = handlerToUse;
                initHandler(ioTab, handlerToUse, pae, paes);
                IOTabsController.getDefault().startHandlerInTab(handlerToUse, ioTab);
            }
        }

        private void initHandler(final InputOutputTab ioTab, final ProjectActionHandler handler, final ProjectActionEvent pae, final ProjectActionEvent[] paes) {
            if (additional == null) {
                additional = BuildActionsProvider.getDefault().getActions(pae.getActionName(), paes);
            }
            List<OutputStreamHandler> streamHandlers = new ArrayList<OutputStreamHandler>();
            for (BuildAction action : additional) {
                if (action instanceof OutputStreamHandler) {
                    streamHandlers.add((OutputStreamHandler) action);
                }
            }
            handler.init(pae, paes, streamHandlers);
            progressHandle = handler.canCancel() ? createProgressHandle(ioTab) : createProgressHandleNoCancel(ioTab);
            progressHandle.start();
            sa.setEnabled(handler.canCancel());
            handler.addExecutionListener(this);
        }

        public ProjectActionHandler getCurrentHandler() {
            return currentHandler;
        }

        private FileOperationsNotifier getFileOperationsNotifier(ProjectActionEvent[] paes) {
            Map<Project, ProjectFileOperationsNotifier> prj2Notifier = new HashMap<Project, ProjectFileOperationsNotifier>();
            for (ProjectActionEvent pae : paes) {
                if (isFileOperationsIntensive(pae)) {
                    Project project = pae.getProject();
                    ProjectFileOperationsNotifier notifer = prj2Notifier.get(project);
                    if (notifer == null) {
                        NativeProjectChangeSupport npcs = null;
                        try {
                            npcs = project.getLookup().lookup(NativeProjectChangeSupport.class);
                            if (npcs == null) {
                                NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
                                if (nativeProject instanceof NativeProjectChangeSupport) {
                                    npcs = (NativeProjectChangeSupport) nativeProject;
                                }
                            }
                        } catch (Exception e) {
                            // This may be ok. The project could have been removed ....
                            System.err.println("getNativeProject " + e);
                        }
                        notifer = new ProjectFileOperationsNotifier(npcs, pae);
                        prj2Notifier.put(project, notifer);
                    }
                    notifer.finishPAE = pae;
                }
            }
            return new FileOperationsNotifier(prj2Notifier);
        }

        public NativeProjectChangeSupport getNativeProjectChangeSupport(Project project) {
            NativeProject nativeProject = null;
            try {
                nativeProject = project.getLookup().lookup(NativeProject.class);
            } catch (Exception e) {
                // This may be ok. The project could have been removed ....
                System.err.println("getNativeProject " + e);
            }
            if (nativeProject instanceof NativeProjectChangeSupport) {
                return (NativeProjectChangeSupport) nativeProject;
            } else {
                return null;
            }
        }

        @Override
        public void executionStarted(int pid) {
            int currentAction = currentActionIdx.get();
            if (additional != null) {
                for (BuildAction action : additional) {
                    action.setStep(currentAction);
                    action.executionStarted(pid);
                }
            }
            fon.onStart(paes[currentAction]);
        }

        @Override
        public void executionFinished(int rc) {
            int currentAction = currentActionIdx.get();
            if (additional != null) {
                for (Action action : additional) {
                    ((ExecutionListener) action).executionFinished(rc);
                }
            }
            ProjectActionEvent curPAE = paes[currentAction];
            // Refresh FS
            refreshProjectFilesOnFinish(curPAE, fon);
            if (currentAction >= paes.length - 1 || rc != 0) {
                sa.setEnabled(false);
                ra.setEnabled(true);
                stopProgress();
                return;
            }

            // This code is executed in finishing ProjectActionHandler's thread.
            // Starting next handler in this thread may lead to problems, such as
            // new threads being created in old handler's thread group, and NetBeans
            // thinking that old handler has not completed.
            // So the call to go() is posted to RequestProcessor.
            if (rc == 0) {
                RPgo.post(new Runnable() {

                    @Override
                    public void run() {
                        go();
                    }
                });
            }
        }

        /**
         * checks whether the project is ok (not deleted)
         */
        private boolean checkProject(ProjectActionEvent pae) {
            Project project = pae.getProject();
            if (project != null) { // paranoidal null checks are better than latent NPE :)
                if (CndUtils.isUnitTestMode() || OpenProjects.getDefault().isProjectOpen(project)) { // OpenProjects don't work in test mode
                    FileObject projectDirectory = project.getProjectDirectory();
                    if (projectDirectory != null) {
                        FileObject nbproject = projectDirectory.getFileObject(MakeConfiguration.NBPROJECT_FOLDER); // NOI18N
                        return nbproject != null && nbproject.isValid();
//                        Should use the check below when working with local projects?
//                        if (nbproject != null) {
//                            // I'm more sure in java.io.File.exists() - practice shows that FileObjects might be sometimes cached...
//                            File file = FileUtil.toFile(nbproject);
//                            return file != null && file.exists();
//                        }

                    }
                }
            }
            return false;
        }

        private boolean checkExecutable(ProjectActionEvent pae) {
            // Check if something is specified
            String executable = pae.getExecutable();
            if (executable.length() == 0) {
                SelectExecutablePanel panel = new SelectExecutablePanel(pae);
                DialogDescriptor descriptor = new DialogDescriptor(panel, getString("SELECT_EXECUTABLE")); // NOI18N
                panel.setDialogDescriptor(descriptor);
                DialogDisplayer.getDefault().notify(descriptor);
                if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                    final String selectedExecutable = panel.getExecutable();
                    final MakeConfiguration projectConfiguration = pae.getConfiguration();

                    // Modify Configuration ...
                    RunProfile runProfile = projectConfiguration.getProfile();
                    if (runProfile != null) {
                        String currentValue = runProfile.getRunCommand().getValue();
                        if (currentValue.isEmpty()) {
                            // Will set this field to default macro
                            runProfile.getRunCommand().setValue("\"" + MakeConfiguration.CND_OUTPUT_PATH_MACRO + "\""); // NOI18N
                        }
//                      } else if (currentValue.indexOf(CND_OUTPUT_PATH_MACRO) < 0) {
//                          String relativeToRunDir = ProjectSupport.toProperPath(runProfile.getRunDirectory(), executable, MakeProjectOptions.getPathMode());
//                          runProfile.getRunCommand().setValue(relativeToRunDir);
//                          return;
//                      }
                    }

                    String relativeToBaseDir = ProjectSupport.toProperPath(projectConfiguration.getBaseDir(), selectedExecutable, pae.getProject());
                    projectConfiguration.getMakefileConfiguration().getOutput().setValue(relativeToBaseDir);

                    // Modify pae ...
                    pae.setExecutable(selectedExecutable);
                    pae.setFinalExecutable();
                    return true;
                }
                return false;
            }
            // Check existence of executable
            if (!CndPathUtilitities.isPathAbsolute(executable)) { // NOI18N
                //executable is relative to run directory - convert to absolute and check. Should be safe (?).
                String runDir = pae.getProfile().getRunDir();
                if (runDir != null) {
                    runDir = runDir.trim();
                    if (runDir.startsWith("~/") || runDir.startsWith("~\\") || runDir.equals("~")) { // NOI18N
                        try {
                            if (pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment().isLocal()) {
                                runDir = HostInfoUtils.getHostInfo(pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment()).getUserDirFile().getAbsolutePath() + runDir.substring(1);
                            } else {
                                runDir = HostInfoUtils.getHostInfo(pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment()).getUserDir() + runDir.substring(1);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ProjectActionSupport.class.getName()).log(Level.INFO, "", ex);  // NOI18N
                        } catch (CancellationException ex) {
                            Logger.getLogger(ProjectActionSupport.class.getName()).log(Level.INFO, "", ex);  // NOI18N
                        }
                    }
                }
                if (runDir == null || runDir.length() == 0) {
                    executable = CndPathUtilitities.toAbsolutePath(pae.getConfiguration().getBaseDir(), executable);
                } else {
                    runDir = CndPathUtilitities.toAbsolutePath(pae.getConfiguration().getBaseDir(), runDir);
                    if (pae.getConfiguration().getBaseDir().equals(runDir)) {
                        // In case if runDir is .
                        executable = CndPathUtilitities.toAbsolutePath(runDir, executable);
                    } else {
                        executable = CndPathUtilitities.toAbsolutePath(runDir, CndPathUtilitities.getBaseName(executable));
                    }
                }
                executable = CndPathUtilitities.normalizeSlashes(executable);
            }
            if (CndPathUtilitities.isPathAbsolute(executable)) {
                MakeConfiguration conf = pae.getConfiguration();
                boolean ok = true;

                if (conf != null && !conf.getDevelopmentHost().isLocalhost()) {
                    final ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
                    if (!pae.isFinalExecutable()) {
                        PathMap mapper = RemoteSyncSupport.getPathMap(pae.getProject());
                        if (mapper != null) {
                            String anExecutable = mapper.getRemotePath(executable, true);
                            if (anExecutable != null) {
                                executable = anExecutable;
                            }
                        } else {
                            LOGGER.log(Level.SEVERE, "Path Mapper not found for project {0} - using local path {1}", new Object[]{pae.getProject(), executable}); //NOI18N
                        }
                    }
                    ExitStatus res = ProcessUtils.execute(execEnv, "test", "-x", executable, "-a", "-f", executable); // NOI18N
                    ok = res.isOK();
                } else {
                    // FIXUP: getExecutable should really return fully qualified name to executable including .exe
                    // but it is too late to change now. For now try both with and without.
                    File file = new File(executable);
                    if (!file.exists() && Utilities.isWindows()) {
                        file = CndFileUtils.createLocalFile(executable + ".exe"); // NOI18N
                    }
                    if (!file.exists() || file.isDirectory()) {
                        ok = false;
                    }
                }
                if (!ok) {
                    String value = pae.getProfile().getRunCommand().getValue();
                    String errormsg = getString("EXECUTABLE_DOESNT_EXISTS", executable); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                    return false;
                }
            }

            // Finally set pae.executable to a real, verified file with an absolute
            // path that reflects file location on a target host (local or remote)

            pae.setExecutable(executable);
            pae.setFinalExecutable();

            return true;
        }
    }

// VK: inlined since it's used once; and caller should know not only return status,
// but mapped name as well => it's easier to inline
//    /**
//     * Verify a remote executable exists, is executable, and is not a directory.
//     *
//     * @param execEnv The remote host
//     * @param executable The file to remotely check
//     * @return true if executable exists and is an executable, otherwise false
//     */
//    private static boolean verifyRemoteExecutable(ExecutionEnvironment execEnv, String executable) {
//        PathMap mapper = HostInfoProvider.getMapper(execEnv);
//        String remoteExecutable = mapper.getRemotePath(executable);
//        CommandProvider cmd = Lookup.getDefault().lookup(CommandProvider.class);
//        if (cmd != null) {
//            return cmd.run(execEnv, "test -x " + remoteExecutable + " -a -f " + remoteExecutable, null) == 0; // NOI18N
//        }
//        return false;
//    }
    private static final class StopAction extends AbstractAction {

        private HandleEvents handleEvents;

        public StopAction(HandleEvents handleEvents) {
            this.handleEvents = handleEvents;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/stop.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, getString("TargetExecutor.StopAction.stop")); // NOI18N
            //System.out.println("handleEvents 1 " + handleEvents);
            //setEnabled(false); // initially, until ready
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            setEnabled(false);
            if (handleEvents.getCurrentHandler() != null) {
                handleEvents.getCurrentHandler().cancel();
            }
        }
    }

    private static final class RerunAction extends AbstractAction {

        private HandleEvents handleEvents;

        public RerunAction(HandleEvents handleEvents) {
            this.handleEvents = handleEvents;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/ui/resources/rerun.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, getString("TargetExecutor.RerunAction.rerun")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            handleEvents.reRun();
        }
    }

    private static final class TermAction extends AbstractAction {

        private HandleEvents handleEvents;

        public TermAction(HandleEvents handleEvents) {
            this.handleEvents = handleEvents;
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/terminal/ui/term.png", false)); // NOI18N
            putValue(Action.SHORT_DESCRIPTION, getString("TargetExecutor.TermAction.text")); // NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = handleEvents.paes.length - 1; i >= 0; i--) {
                ProjectActionEvent pae = handleEvents.paes[i];
                String projectName = ProjectUtils.getInformation(pae.getProject()).getDisplayName();
                String dir = pae.getProfile().getRunDirectory();
                ExecutionEnvironment env = pae.getConfiguration().getDevelopmentHost().getExecutionEnvironment();
                if (env.isRemote()) {
                    if (RemoteFileUtil.getProjectSourceExecutionEnvironment(pae.getProject()).isLocal()) {
                        PathMap pathMap = RemoteSyncSupport.getPathMap(pae.getProject());
                        if (pathMap != null) {
                            String aDir = pathMap.getRemotePath(dir);
                            if (aDir != null) {
                                dir = aDir;
                            }
                        } else {
                            LOGGER.log(Level.SEVERE, "Path Mapper not found for project {0} - using local path {1}", new Object[]{pae.getProject(), dir}); //NOI18N
                        }
                    }
                }
                TerminalSupport.openTerminal(getString("TargetExecutor.TermAction.tabTitle", projectName, env.getDisplayName()), env, dir); // NOI18N
                break;
            }
        }
    }

    /**
     * Look up i18n strings here
     */
    private static String getString(String s) {
        return NbBundle.getMessage(ProjectActionSupport.class, s);
    }

    private static String getString(String s, String... arg) {
        return NbBundle.getMessage(ProjectActionSupport.class, s, arg);
    }
}
