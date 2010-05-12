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
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import org.netbeans.api.debugger.jpda.DebuggerStartException;
import org.netbeans.modules.javacard.JCUtil;
import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.common.NodeRefresher;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.customizer.ClassicAppletProjectProperties;
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
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

public class JCProjectActionProvider implements ActionProvider {

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

    private final Set<String> supportedActionsSet;
    private JCProject project;

    public JCProjectActionProvider(JCProject project) {
        this.project = project;
        supportedActionsSet = new HashSet<String>();
        supportedActionsSet.addAll (Arrays.asList(getSupportedActions()));
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
            FileObject dir = project.getProjectDirectory();
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            // #177993 - since we are not using source groups for scripts/html
            // they are not deleted
            if (dir.isValid()) {
                try {
                    FileObject fo = dir.getFileObject("html"); //NOI18N
                    if (fo != null) {
                        fo.delete();
                    }
                    fo = dir.getFileObject("scripts"); //NOI18N
                    if (fo != null) {
                        fo.delete();
                    }
                    if (dir.getChildren().length == 0) {
                        dir.delete();
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
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
            final FileObject buildFo = JCUtil.findBuildXml(project);
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
                if (!debug) {
                    StartCapability starter = card.getCapability(StartCapability.class);
                    if (starter != null && start && state.isNotRunning()) {
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
                }

                AntTargetInterceptor icept = card.getCapability(AntTargetInterceptor.class);
                boolean run = icept == null;
                if (!run) {
                    AntTarget target = AntTarget.forName(command);
                    run = icept.onBeforeInvokeTarget(project, target, props);
                }
                
                if (run) {
                    final String[] tNames = targetNames;
                    final Properties p = props;
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            try {
                                OnAntProcessFinishedListener l = new OnAntProcessFinishedListener(project.getPlatform(), card);
                                if (debug) {
                                    new DebugProxyProcessLauncher(l).run();
                                }
                                ExecutorTask task = ActionUtils.runTarget(buildFo, tNames, p);
                                task.addTaskListener(l);
                                if (ActionNames.COMMAND_JC_GENPROXY.equals(command)) {
                                    PropertyEvaluator eval = project.evaluator();
                                    String useProxiesProp = eval.evaluate("{" + ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES + "}"); //NOI18N
                                    if (!Boolean.valueOf(useProxiesProp)) {
                                        task.addTaskListener (new EnsureUseProxiesSetAfterProxyGeneration(project));
                                    }
                                }
                                task.getInputOutput().select();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public boolean isActionEnabled(String command, Lookup context)
            throws IllegalArgumentException {
        if (!supportedActionsSet.contains(command)) {
            throw new IllegalArgumentException(
                    "Command not supported by Java Card project: " + command);
        }
        boolean debug = COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) ||
                COMMAND_DEBUG_STEP_INTO.equals(command) || COMMAND_DEBUG_TEST_SINGLE.equals(command);

        JavacardPlatform pform = project.getPlatform();
        Card card = project.getCard();
        if (actionRequiresValidPlatform(command)) {
            boolean result = pform != null && pform.isValid() && card != null && card.isValid();
            if (debug) {
                result &= card.getCapability(DebugCapability.class) != null;
            }
            return result;
        } else {
            return true;
        }
    }

    private boolean actionRequiresValidPlatform (String command) {
        return COMMAND_BUILD.equals(command) || COMMAND_DEBUG.equals(command) ||
                COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command) ||
                COMMAND_DEBUG_TEST_SINGLE.equals(command) || COMMAND_RUN.equals(command) ||
                COMMAND_RUN_SINGLE.equals(command) || COMMAND_TEST.equals(command) ||
                COMMAND_TEST_SINGLE.equals(command)  || ActionNames.COMMAND_JC_CREATE.equals(command) ||
                ActionNames.COMMAND_JC_DELETE.equals(command) || ActionNames.COMMAND_JC_GENPROXY.equals(command) ||
                ActionNames.COMMAND_JC_LOAD.equals(command) || ActionNames.COMMAND_JC_UNLOAD.equals(command);
    }

    public String[] getTargetNames(String command, Lookup context, Properties props) {
        List<String> targets = new ArrayList<String>();

        if (COMMAND_CLEAN.equals(command)) {
            targets.add("clean");
        } else if (COMMAND_BUILD.equals(command)) {
            targets.add("build");
        } else if (COMMAND_REBUILD.equals(command)) {
            targets.add("clean");
            targets.add("build");
        } else if (COMMAND_DEBUG.equals(command)) {
            targets.add("run-for-debug");
        } else if (COMMAND_RUN.equals(command)) {
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
            targets.add("build"); //NOI18N
            targets.add("load-bundle"); //NOI18N
        } else if (project.kind().isApplication() && ActionNames.COMMAND_JC_CREATE.equals(command)) {
            targets.add("create-instance"); //NOI18N
        } else if (project.kind().isApplication() && ActionNames.COMMAND_JC_DELETE.equals(command)) {
            targets.add("delete-instance"); //NOI18N
        } else if (ActionNames.COMMAND_JC_UNLOAD.equals(command)) {
            targets.add("unload-bundle"); //NOI18N
        } else if (ActionNames.COMMAND_JC_GENPROXY.equals(command)) {
            targets.add("generate-sio-proxies"); //NOI18N
            //Set these for the Ant process only.  Post-run Ant task listener will actually update the project
            //metadata.
            props.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASSIC_USE_MY_PROXIES, Boolean.TRUE.toString()); //NOI18N
            props.setProperty(ProjectPropertyNames.PROJECT_PROP_PROXY_SRC_DIR, ClassicAppletProjectProperties.PROXY_SOURCE_DIR);
        }
        String[] result = targets.toArray(new String[targets.size()]);
        return result;
    }

    private final class DebugProxyProcessLauncher implements Runnable {
        private final OnAntProcessFinishedListener tl;
        DebugProxyProcessLauncher(OnAntProcessFinishedListener tl) {
            this.tl = tl;
        }

        public void run() {
            Card card = project.getCard();
            String host = "[NO_HOST]"; //NOI18N
            int attachPort = -1;
            try {
                StopCapability stopper = card.getCapability(StopCapability.class);
                if (card.getState().isRunning() && stopper != null) {
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(JCProjectActionProvider.class, "MSG_STOPPING_SERVER")); //NOI18N
                    stopper.stop().await();
                }
                StartCapability starter = card.getCapability(StartCapability.class);

                if (starter != null) {
                    Condition c = starter.start(RunMode.DEBUG, project);
                    assert c != null;
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(JCProjectActionProvider.class, "MSG_WAIT_FOR_SERVER")); //NOI18N
                    c.await(30000, TimeUnit.MILLISECONDS);
                }
                //XXX move this stuff into a DebugCapability or something
                if (card.getCapability(DebugCapability.class) != null && card.getCapability(PortProvider.class)!= null) {
                    // get the port numbers from card instance
                    PortProvider p = card.getCapability(PortProvider.class);
                    attachPort = p.getPort(PortKind.DEBUG_RUNTIME_TO_IDE_PROXY);
                    host = p.getHost();
                    String msg = null;
                    if ((msg = waitForPort(host, attachPort)) != null) {
                        NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg);
                        DialogDisplayer.getDefault().notify(m);
                    }
                    JPDADebugger de = JPDADebugger.attach(
                            host, attachPort,
                            new Object[]{card.getSystemId() +
                            " - [Debugger]", "SocketAttach", "dt_socket"}); //NOI18N
                    tl.setDebuggerSession(de);
                }
            } catch (DebuggerStartException ex) {
                if (ex.getCause() instanceof ConnectException) {
                    StopCapability stopper = card.getCapability(StopCapability.class);
                    if (stopper != null) {
                        try {
                            stopper.stop().await();
                        } catch (InterruptedException ex1) {
                            Exceptions.printStackTrace(ex1);
                        }
                    }
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(
                                JCProjectActionProvider.class,
                                "MSG_DEBUG_PORT_TIMEOUT", //NOI18N
                                host, attachPort));
                } else {
                    Exceptions.printStackTrace(ex);
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        /**
         * Wait for the debug proxy to start accepting connections
         * @param host
         * @param attachPort
         * @return an error message on failure, null on success
         */
        private String waitForPort(String host, int attachPort) {
            try {
                //Debug proxy seems to need some time to recover
                //before it will not reject another connection
                Thread.sleep (7000);
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
//            HOW THIS OUGHT TO WORK:
//            for (int i=0; i < 6; i++) {
//                try {
//                    Socket socket = new Socket();
//                    System.err.println("Connect to " + host + ":" + attachPort + " try " + (i + 1));
//                    socket.connect(new InetSocketAddress(host, attachPort));
//                    if (socket.isConnected()) {
//                        System.err.println("Successful connect to " + host + ":" + attachPort);
//                        socket.close();
//                        try {
//                            Thread.sleep (500);
//                        } catch (InterruptedException e) {
//                            Exceptions.printStackTrace(e);
//                        }
//                        return null;
//                    }
//                } catch (UnknownHostException ex) {
//                    return ex.getLocalizedMessage();
//                } catch (IOException ex) {
//                    Logger.getLogger(JCProjectActionProvider.class.getName()).log(
//                            Level.FINEST, "Could not contact {0}:{1} try {2}", new Object[] { host, attachPort, i }); //NOI18N
//                    Logger.getLogger(JCProjectActionProvider.class.getName()).log(Level.FINEST, null, ex);
//                }
//            }
            //try anyway?
            return null;
        }
    }

    static final class EnsureUseProxiesSetAfterProxyGeneration implements TaskListener {
        private final JCProject project;
        EnsureUseProxiesSetAfterProxyGeneration (JCProject project) {
            this.project = project;
            assert project.kind().isClassic();
        }

        @Override
        public void taskFinished(Task task) {
            JCCustomizerProvider prov = project.getLookup().lookup(JCCustomizerProvider.class);
            Parameters.notNull("prov", prov);
            JCProjectProperties p = prov.createProjectProperties(project.kind(), project, project.evaluator(), project.getAntProjectHelper());
            if (p instanceof ClassicAppletProjectProperties) {
                ClassicAppletProjectProperties cp = (ClassicAppletProjectProperties) p;
                cp.setUseMyProxies(true);
                cp.storeProperties();
            }
        }

    }

    class OnAntProcessFinishedListener implements TaskListener, PropertyChangeListener {
        private final JavacardPlatform platform;
        private final Card card;
        private JPDADebugger debugger;
        private volatile boolean finished;
        OnAntProcessFinishedListener (JavacardPlatform platform, Card card) {
            this.platform = platform;
            this.card = card;
        }

        void setDebuggerSession (JPDADebugger debugger) {
            synchronized (this) {
                this.debugger = debugger;
            }
            if (!finished) {
                debugger.addPropertyChangeListener(this);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            String nv = String.valueOf(evt.getNewValue());
            //4 - ???
            if ("state".equals(name) && "4".equals(nv)) { //NOI18N
                if (card.isValid() && card.getState().isRunning()) {
                    StopCapability stopper = card.getCapability(StopCapability.class);
                    if (stopper != null) {
                        stopper.stop();
                    }
                }
            }
        }

        private void stopListeningToDebugger() {
            JPDADebugger de;
            synchronized (this) {
                de = debugger;
            }
            if (de != null) {
                de.removePropertyChangeListener(this);
            }
        }

        @SuppressWarnings("deprecation")
        public void taskFinished(org.openide.util.Task task) {
            finished = true;
            stopListeningToDebugger();
            String platformName = platform.getSystemName();
            CardInfo info = card.getCapability(CardInfo.class);
            String cardName = info == null ? card.toString() : info.getSystemId();
            DataObject dob = Utils.findDeviceForPlatform(platformName, cardName);
            if (dob.isValid()) {
                NodeRefresher n = dob == null ? null :
                    dob.getLookup().lookup(NodeRefresher.class);
                if (n != null) {
                    n.refreshNode();
                }
            }
        }
    }
}
