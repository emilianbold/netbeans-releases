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
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.Card;
import org.netbeans.modules.javacard.constants.ActionNames;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.util.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.netbeans.modules.javacard.api.CardState;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.javacard.card.ReferenceImplementation;
import org.netbeans.modules.javacard.card.loader.CardDataObject;
import org.netbeans.modules.javacard.card.loader.CardDataObject;
import org.openide.awt.StatusDisplayer;


public class JCProjectActionProvider implements ActionProvider, PropertyChangeListener {

    private static final String[] appSupportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_RUN,
        //COMMAND_DEBUG,
        COMMAND_REBUILD,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        ActionNames.COMMAND_JC_LOAD,
        ActionNames.COMMAND_JC_CREATE,
        ActionNames.COMMAND_JC_DELETE,
        ActionNames.COMMAND_JC_UNLOAD,};
    private static final String[] libSupportedActions = {
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

    private Set<String> supportedActionsSet;
    private JCProject project;    // Relative pathnames of files known to be modified.
    // The case when modifiedFiles==null should be treated as if all files are
    // modified.
    private Set<String> modifiedFiles;

    public JCProjectActionProvider(JCProject project) {
        this.project = project;
        supportedActionsSet = new HashSet<String>();
        String[] supportedActions = project.kind().isApplication() ? appSupportedActions : libSupportedActions;
        supportedActionsSet.addAll (Arrays.asList(supportedActions));
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
        return project.kind().isApplication() ? appSupportedActions : libSupportedActions;
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
                    FileObject buildFo = Utils.findBuildXml(project);
                    if (buildFo == null || !buildFo.isValid()) {
                        //The build.xml was deleted after the isActionEnabled was called
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                NbBundle.getMessage(JCProjectActionProvider.class,
                                "LBL_No_Build_XML_Found"), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    } else {
                        final Card card = project.getCard();
                        boolean start = ActionNames.COMMAND_JC_CREATE.equals(command) || ActionNames.COMMAND_JC_DELETE.equals(command) ||
                                ActionNames.COMMAND_JC_LOAD.equals(command) || ActionNames.COMMAND_JC_UNLOAD.equals(command);
                        if (start && card.isNotRunning()) {
                            try {
                                StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage(JCProjectActionProvider.class,
                                    "MSG_STARTING_SERVER",  //NOI18N
                                    card.getDisplayName()));
                                card.startServer(false, project).await();
                            } catch (InterruptedException ex) {
                                if (!card.isRunning()) {
                                    StatusDisplayer.getDefault().setStatusText(
                                            NbBundle.getMessage(
                                            JCProjectActionProvider.class,
                                            "MSG_WAIT_FAILED", //NOI18N
                                            card.getDisplayName()));
                                    return;
                                }
                            }
                        }

                        ActionUtils.runTarget(buildFo, targetNames, props).addTaskListener(new TaskListener() {

                            public void taskFinished(Task task) {
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
                                String cardName = card.getId();
                                DataObject dob = Utils.findDeviceForPlatform(platformName, cardName);
                                if (dob instanceof CardDataObject) {
                                    ((CardDataObject) dob).refreshNode();
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
        return project.getPlatform() != null && project.getCard() != null;
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
                        if (card.isRunning()) {
                            StatusDisplayer.getDefault().setStatusText(
                                    NbBundle.getMessage(JCProjectActionProvider.class, "MSG_STOPPING_SERVER"));
                            card.stopServer();
                        }
                        Condition c = card.startServer(true, project);
                        assert c != null;
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(JCProjectActionProvider.class, "MSG_WAIT_FOR_SERVER"));
                        c.await(30000, TimeUnit.MILLISECONDS);
                        if (card instanceof ReferenceImplementation) {
                            //XXX should not need to sleep here, but RI seems
                            //to need some time to initialize
                            Thread.sleep(4000);
                            // get the port numbers from card instance
                            String p2iPort = ((ReferenceImplementation) card).getProxy2idePort();
                            JPDADebugger de = JPDADebugger.attach(
                                    "localhost", Integer.parseInt(p2iPort),
                                    new Object[]{card.getId() +
                                    " - [Debugger]", "SocketAttach", "dt_socket"}); //NOI18N
                            de.addPropertyChangeListener(JCProjectActionProvider.this);
                        } else {
                            throw new UnsupportedOperationException ("Debug only implemented for reference impl");
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
            if (card.getState() == CardState.RUNNING_IN_DEBUG_MODE) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(JCProjectActionProvider.class, "MSG_STOPPING_SERVER"));
                card.stopServer();
            }
            if (card.getState().isNotRunning()) {
                Condition c = card.startServer(false, project);
                assert c != null;
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(JCProjectActionProvider.class, "MSG_WAIT_FOR_SERVER"));
                try {
                    c.await(30000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            targets.add("run");
        } else if (ActionNames.COMMAND_JC_LOAD.equals(command)) {
            if (needRebuild) {
                targets.add("build");
            }
            targets.add("load-bundle");
        } else if (project.kind().isApplication() && ActionNames.COMMAND_JC_CREATE.equals(command)) {
            targets.add("create-instance");
        } else if (project.kind().isApplication() && ActionNames.COMMAND_JC_DELETE.equals(command)) {
            targets.add("delete-instance");
        } else if (ActionNames.COMMAND_JC_UNLOAD.equals(command)) {
            targets.add("unload-bundle");
        }

        return targets.toArray(new String[targets.size()]);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        String ov = String.valueOf(evt.getOldValue());
        String nv = String.valueOf(evt.getNewValue());
        //4 - ???
        if ("state".equals(name) && "4".equals(nv)) {
            final Card server = project.getCard();
            server.stopServer();
        }
    }
}
