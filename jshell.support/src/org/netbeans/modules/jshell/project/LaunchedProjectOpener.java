/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.project;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.UnaryOperator;
import jdk.jshell.JDIRemoteAgent;
import jdk.jshell.RemoteJShellService;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.netbeans.modules.jshell.launch.JShellConnection;
import org.netbeans.modules.jshell.launch.ShellLaunchEvent;
import org.netbeans.modules.jshell.launch.ShellLaunchListener;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * 
 * @author sdedic
 */
public class LaunchedProjectOpener implements ShellLaunchListener {
    private static LaunchedProjectOpener INSTANCE = null;
    
    static {
        ShellLaunchManager.getInstance().addLaunchListener(new LaunchedProjectOpener());
    }
    
    public static void init() {}

    @Override
    public void connectionInitiated(ShellLaunchEvent ev) {
        // not important (yet)
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Title_JShellOnDebugger=JShell - debugging {0}"
    })
    @Override
    public void handshakeCompleted(ShellLaunchEvent ev) {
        ShellAgent agent = ev.getAgent();
//        JShellConnection con;
//        try {
//            con = agent.createConnection();
//        } catch (IOException ex) {
//            StatusDisplayer.getDefault().setStatusText("Error connecting to JShell agent: " +
//                    ex.getLocalizedMessage(), 100);
//            return;
//        }
        Project p = agent.getProject();
        if (p == null) {
            return;
        }
        
        final JShellEnvironment attachEnv = new DebugShellEnv(agent, p, 
                Bundle.Title_JShellOnDebugger(ProjectUtils.getInformation(p).getDisplayName()));

        boolean ok = false;
        try {
            ShellRegistry.get().startJShell(attachEnv);
            attachEnv.open();
            ok = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (!ok) {
                attachEnv.getSession().closeSession();
            }
        }
    }
    
    private static class DebugShellEnv extends JShellEnvironment {
        private final ShellAgent agent;

        public DebugShellEnv(ShellAgent agent, Project project, String displayName) {
            super(project, displayName);
            this.agent = agent;
        }

        @Override
        protected InputOutput createInputOutput() {
            return agent.getIO();
        }
        
        public RemoteJShellService createExecutionEnv() {
            return new DebugExecutionEnvironment(getSession(), agent, this);
        }
        
        protected void reportClosedBridge(ShellSession s, boolean disconnectOrShutdown) {
            if (disconnectOrShutdown) {
                notifyDisconnected(s);
            } else {
                notifyShutdown();
            }
        }
    }

    @Override
    public void agentDestroyed(ShellLaunchEvent ev) { }

    @Override
    public void connectionClosed(ShellLaunchEvent ev) { }
    
    static class DebugExecutionEnvironment extends JDIRemoteAgent implements ShellLaunchListener {
        private boolean             added;
        volatile JShellConnection    shellConnection;
        private boolean             closed;
        
        final ShellAgent          agent;
        final DebugShellEnv   shellEnv;
        final ShellSession    reportSession;
        
        public DebugExecutionEnvironment(ShellSession s, ShellAgent agent, DebugShellEnv env) {
            super(UnaryOperator.identity());
            this.shellEnv = env;
            this.agent = agent;
            this.reportSession = s;
        }
        
        @NbBundle.Messages("MSG_AgentConnectionBroken=Connection to JShell agent broken. Please re-run the project.")
        private JShellConnection getConnection(boolean dontConnect) throws IOException {
            synchronized (this) {
                if (closed || (dontConnect && shellConnection == null)) {
                    throw new IOException(Bundle.MSG_AgentConnectionBroken());
                }
                if (shellConnection != null) {
                    return shellConnection;
                }
            }
            try {
                JShellConnection x = agent.createConnection();
                synchronized (this) {
                    if (!added) {
                        ShellLaunchManager.getInstance().addLaunchListener(this);
                        added = true;
                    }
                    return this.shellConnection = x;
                }
            } catch (IOException ex) {
                StatusDisplayer.getDefault().setStatusText("Error connecting to JShell agent: " +
                        ex.getLocalizedMessage(), 100);
                throw ex;
            }
        }
        
        @Override
        public OutputStream getCommandStream() throws IOException {
            return getConnection(true).getAgentInput();
        }

        @Override
        public InputStream getResponseStream() throws IOException {
            return getConnection(true).getAgentOutput();
        }

        @Override
        public synchronized void closeStreams() {
            if (shellConnection == null) {
                return;
            }
            try {
                OutputStream os = shellConnection.getAgentInput();
                os.close();
            } catch (IOException ex) {
            }
            try {
                InputStream is = shellConnection.getAgentOutput();
                is.close();
            } catch (IOException ex) {
            }
            
            requestShutdown();
        }

        @Override
        protected ObjectReference getAgentObjectReference() {
            return shellConnection.getAgentHandle();
        }

        @Override
        protected VirtualMachine acquireVirtualMachine() throws IOException {
            return getConnection(false).getVirtualMachine();
        }

        @Override
        public boolean requestShutdown() {
            agent.closeConnection(shellConnection);
            return false;
        }

        @Override
        public void connectionInitiated(ShellLaunchEvent ev) {
        }

        @Override
        public void handshakeCompleted(ShellLaunchEvent ev) {
        }
        
        @Override
        public  void connectionClosed(ShellLaunchEvent ev) {
            synchronized (this) {
                if (closed) {
                    return;
                }
                closed = true;
            }
            shellEnv.reportClosedBridge(reportSession, true);
        }

        @Override
        public void agentDestroyed(ShellLaunchEvent ev) {
            synchronized (this) {
                if (ev.getAgent() != agent || closed) {
                    return;
                }
                this.shellConnection = null;
                closed = true;
            }
            shellEnv.reportClosedBridge(reportSession, false);
        }
    }
}
