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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.ActionNames;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.loaders.DataObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.netbeans.modules.javacard.JCUtil;
import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.common.NodeRefresher;
import org.netbeans.modules.javacard.spi.capabilities.AntTargetInterceptor;
import org.netbeans.modules.javacard.spi.capabilities.AntTarget;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.CardState;
import org.netbeans.modules.javacard.spi.capabilities.DebugCapability;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.capabilities.PortKind;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.StopCapability;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;


public class JCProjectActionProvider implements ActionProvider, PropertyChangeListener {

    private static final String[] webOrExtAppSupportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_REBUILD,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        ActionNames.COMMAND_JC_LOAD,
        ActionNames.COMMAND_JC_CREATE,
        ActionNames.COMMAND_JC_DELETE,
        ActionNames.COMMAND_JC_UNLOAD,
    };
    private static final String[] classicAppSupportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_RUN,
        COMMAND_DEBUG,
        COMMAND_REBUILD,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        ActionNames.COMMAND_JC_LOAD,
        ActionNames.COMMAND_JC_CREATE,
        ActionNames.COMMAND_JC_DELETE,
        ActionNames.COMMAND_JC_UNLOAD,
        ActionNames.COMMAND_JC_GENPROXY,
    };

    private static final String[] extLibSupportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        ActionNames.COMMAND_JC_LOAD,
        ActionNames.COMMAND_JC_UNLOAD,
    };
    private static final String[] classicLibSupportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        ActionNames.COMMAND_JC_LOAD,
        ActionNames.COMMAND_JC_UNLOAD,
        ActionNames.COMMAND_JC_GENPROXY,
    };

    private Set<String> supportedActionsSet;
    private JCProject project;    // Relative pathnames of files known to be modified.
    // The case when modifiedFiles==null should be treated as if all files are
    // modified.
    private Set<String> modifiedFiles;

    public JCProjectActionProvider(JCProject project) {
        this.project = project;
        supportedActionsSet = new HashSet<String>();
        supportedActionsSet.addAll (Arrays.asList(getSupportedActions()));
        modifiedFiles = new TreeSet<String>();
        SourceRoots roots = project.getRoots();
        for (FileObject srcRoot : roots.getRoots()) {
            Enumeration<? extends FileObject> files = srcRoot.getChildren(true);
            while (files.hasMoreElements()) {
                FileObject file = files.nextElement();
                fileModified(file);
            }
        }

        try {
            FileSystem fs = project.getProjectDirectory().getFileSystem();
            fs.addFileChangeListener(
                    FileUtil.weakFileChangeListener(modificationListener, fs));
        } catch (FileStateInvalidException x) {
            Exceptions.printStackTrace(x);
        }
    }
    private final FileChangeListener modificationListener =
            new FileChangeAdapter() {

                public
                @Override
                void fileChanged(FileEvent fe) {
                    fileModified(fe.getFile());
                }

                public
                @Override
                void fileDataCreated(FileEvent fe) {
                    fileModified(fe.getFile());
                }
            };

    private void fileModified(FileObject f) {
        String path = FileUtil.getRelativePath(project.getProjectDirectory(), f);

        if (path != null) {
            synchronized (this) {
                if (modifiedFiles != null) {
                    modifiedFiles.add(path);
                }
            }
        }
    }

    public String[] getSupportedActions() {
        String[] sa = null;
        if(project.kind().isApplication()) {
            if(project.kind().isClassic()) {
                sa = classicAppSupportedActions;
            } else {
                sa = webOrExtAppSupportedActions;
    }
        } else { // library
            if(project.kind().isClassic()) { // classic library
                sa = classicLibSupportedActions;
            } else {
                sa = extLibSupportedActions;
            }
        }
        return sa;
    }

    public void invokeAction(final String command, final Lookup context)
            throws IllegalArgumentException {
        if (COMMAND_DELETE.equals(command)) {
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
        final boolean debug = COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) ||
                COMMAND_DEBUG_STEP_INTO.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command);

        final Runnable action = new Runnable() {

            public void run() {
                Properties props = new Properties();
                String[] targetNames;

                targetNames = getTargetNames(command, context, props);
                if (targetNames == null) {
                    return;
                }
                if (targetNames.length == 0) {
                    targetNames = null;
                }
                if (props.keySet().size() == 0) {
                    props = null;
                }
                try {
                    FileObject buildFo = JCUtil.findBuildXml(project);
                    if (buildFo == null || !buildFo.isValid()) {
                        //The build.xml was deleted after the isActionEnabled was called
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                NbBundle.getMessage(JCProjectActionProvider.class,
                                "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE); //NOI18N
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        final Card card = project.getCard();
                        boolean start = ActionNames.COMMAND_JC_CREATE.equals(command) || ActionNames.COMMAND_JC_DELETE.equals(command) ||
                                ActionNames.COMMAND_JC_LOAD.equals(command) || ActionNames.COMMAND_JC_UNLOAD.equals(command);
                        CardState state = card.getState();
                        CardInfo info = card.getCapability(CardInfo.class);
                        StartCapability starter = card.getCapability(StartCapability.class);
                        if (start && state.isNotRunning()) {
                            try {
                                StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage(JCProjectActionProvider.class,
                                    "MSG_STARTING_SERVER",  //NOI18N
                                    info == null ? card.toString() : info.getDisplayName()));
                                starter.start(debug ? RunMode.DEBUG : RunMode.RUN, project).await();
                            } catch (InterruptedException ex) {
                                if (!card.getState().isRunning()) {
                                    String name = card.getCapability(CardInfo.class) == null ? card.toString() :
                                        card.getCapability(CardInfo.class).getDisplayName();
                                    StatusDisplayer.getDefault().setStatusText(
                                            NbBundle.getMessage(
                                            JCProjectActionProvider.class,
                                            "MSG_WAIT_FAILED", //NOI18N
                                            name));
                                    return;
                                }
                            }
                        }

                        AntTargetInterceptor icept = card.getCapability(AntTargetInterceptor.class);
                        boolean run = icept == null;
                        if (!run) {
                            AntTarget target = AntTarget.forName(command);
                            run = icept.onBeforeInvokeTarget(project, target, props);
                        }

                        if (run)
                        ActionUtils.runTarget(buildFo, targetNames, props).addTaskListener(new TaskListener() {

                            public void taskFinished(org.openide.util.Task task) {
                                if (((ExecutorTask) task).result() != 0) {
                                    synchronized (JCProjectActionProvider.this) {
                                        // If build fails, disable optimization
                                        modifiedFiles = null;
                                    }
                                } else {
                                    // After successfull build, all files are
                                    // marked as not-modified
                                    modifiedFiles = new TreeSet<String>();
                                }
                                JavacardPlatform platform = project.getPlatform();
                                String platformName = platform.getSystemName();
                                CardInfo info = card.getCapability(CardInfo.class);
                                String cardName = info == null ? card.toString() : info.getSystemId();
                                //XXX probably only needed for RI
                                DataObject dob = Utils.findDeviceForPlatform(platformName, cardName);
                                NodeRefresher n = dob == null ? null :
                                    dob.getLookup().lookup(NodeRefresher.class);
                                if (n != null) {
                                    n.refreshNode();
                                }
                            }
                        });
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        };
        action.run();
    }

    public boolean isActionEnabled(String command, Lookup context)
            throws IllegalArgumentException {
        if (!supportedActionsSet.contains(command)) {
            throw new IllegalArgumentException(
                    "Command not supported by Java Card project: " + command);
        }
        return COMMAND_DELETE.equals(command) ||
                project.getPlatform() != null && project.getCard() != null;
    }

    private void updateModifiedFiles() {
        if (modifiedFiles != null) {
            // Count files that are modified in memory and not yet saved on disk
            for (DataObject dataObj : DataObject.getRegistry().getModified()) {
                fileModified(dataObj.getPrimaryFile());
            }
        }
    }

    public String[] getTargetNames(String command, Lookup context, Properties props) {
        updateModifiedFiles();
        boolean needRebuild = modifiedFiles == null || !modifiedFiles.isEmpty();

        List<String> targets = new ArrayList<String>();

        if (COMMAND_CLEAN.equals(command)) {
            targets.add("clean");
        } else if (COMMAND_BUILD.equals(command)) {
            targets.add("build");
        } else if (COMMAND_REBUILD.equals(command)) {
            targets.add("clean");
            targets.add("build");
        } else if (COMMAND_DEBUG.equals(command)) {
            if (needRebuild) {
                targets.add("build");
            }
            targets.add("run-for-debug");
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    try {
                        Card card = project.getCard();
                        StopCapability stopper = card.getCapability(StopCapability.class);
                        if (card.getState().isRunning() && stopper != null) {
                            StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage(JCProjectActionProvider.class, "MSG_STOPPING_SERVER"));
                            stopper.stop().await();
                        }
                        StartCapability starter = card.getCapability(StartCapability.class);

                        if (starter != null) {
                            Condition c = starter.start(RunMode.DEBUG, project);
                            assert c != null;
                            StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage(JCProjectActionProvider.class, "MSG_WAIT_FOR_SERVER"));
                            c.await(30000, TimeUnit.MILLISECONDS);
                        }
                        //XXX move this stuff into a DebugCapability or something
                        if (card.getCapability(DebugCapability.class) != null && card.getCapability(PortProvider.class)!= null) {
                            //XXX should not need to sleep here, but RI seems
                            //to need some time to initialize.
                            //Pending - parse output and trigger
                            Thread.sleep(4000);
                            // get the port numbers from card instance
                            PortProvider p = card.getCapability(PortProvider.class);
                            int attachPort = p.getPort(PortKind.DEBUG_RUNTIME_TO_IDE_PROXY);
                            String host = p.getHost();
                            JPDADebugger de = JPDADebugger.attach(
                                    host, attachPort,
                                    new Object[]{card.getSystemId() +
                                    " - [Debugger]", "SocketAttach", "dt_socket"}); //NOI18N
                            de.addPropertyChangeListener(JCProjectActionProvider.this);
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } else if (COMMAND_RUN.equals(command)) {
            if (needRebuild) {
                targets.add("build");
            }
            Card card = project.getCard();
            if (card == null) {
                return new String[0];
            }
            StopCapability stopper = card.getCapability(StopCapability.class);
            if (stopper != null && card.getState() == CardState.RUNNING_IN_DEBUG_MODE) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(JCProjectActionProvider.class, "MSG_STOPPING_SERVER")); //NOI18N
                Condition c = stopper.stop();
                if (c != null) {
                    try {
                        c.await(30000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            StartCapability starter = card.getCapability(StartCapability.class);
            if (starter != null && card.getState().isNotRunning()) {
                Condition c = starter.start(RunMode.RUN, project);
                assert c != null;
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(JCProjectActionProvider.class, "MSG_WAIT_FOR_SERVER")); //NOI18N
                try {
                    c.await(30000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            targets.add("run"); //NOI18N
        } else if (ActionNames.COMMAND_JC_LOAD.equals(command)) {
            if (needRebuild) {
                targets.add("build"); //NOI18N
            }
            targets.add("load-bundle"); //NOI18N
        } else if (project.kind().isApplication() && ActionNames.COMMAND_JC_CREATE.equals(command)) {
            targets.add("create-instance"); //NOI18N
        } else if (project.kind().isApplication() && ActionNames.COMMAND_JC_DELETE.equals(command)) {
            targets.add("delete-instance"); //NOI18N
        } else if (ActionNames.COMMAND_JC_UNLOAD.equals(command)) {
            targets.add("unload-bundle"); //NOI18N
        } else if (ActionNames.COMMAND_JC_GENPROXY.equals(command)) {
            targets.add("generate-sio-proxies"); //NOI18N
        }
        return targets.toArray(new String[targets.size()]);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        String nv = String.valueOf(evt.getNewValue());
        //4 - ???
        if ("state".equals(name) && "4".equals(nv)) { //NOI18N
            final Card server = project.getCard();
            StopCapability stopper = server.getCapability(StopCapability.class);
            if (stopper != null) {
                stopper.stop();
            }
        }
    }
}
