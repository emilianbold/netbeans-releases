/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jshell.project;

import org.netbeans.modules.jshell.launch.RemoteJShellAccessor;
import java.io.IOException;
import jdk.jshell.RemoteJShellService;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.launch.JShellConnection;
import org.netbeans.modules.jshell.launch.ShellAgent;
import org.netbeans.modules.jshell.launch.ShellLaunchEvent;
import org.netbeans.modules.jshell.launch.ShellLaunchListener;
import org.netbeans.modules.jshell.launch.ShellLaunchManager;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.windows.InputOutput;

/**
 *
 * @author sdedic
 */
class ProjectShellEnv extends JShellEnvironment {
    private final ShellAgent agent;

    public ProjectShellEnv(ShellAgent agent, Project project, String displayName) {
        super(project, displayName);
        this.agent = agent;
    }

    @Override
    protected InputOutput createInputOutput() {
        return agent.getIO();
    }

    public RemoteJShellService createExecutionEnv() {
        try {
            RemoteJShellAccessor accessor = agent.createRemoteService();
            CloseNotifier nf = new CloseNotifier(accessor, getSession());
            ShellLaunchManager.getInstance().addLaunchListener(nf);
            return accessor;
        } catch (IOException ex) {
            return null;
        }
    }

    protected void reportClosedBridge(ShellSession s, boolean disconnectOrShutdown) {
        if (disconnectOrShutdown) {
            notifyDisconnected(s);
        } else {
            notifyShutdown();
        }
    }
    
    ShellAgent getAgent() {
        return agent;
    }
    
    private class CloseNotifier implements ShellLaunchListener {
        private final RemoteJShellAccessor accessor;
        private final ShellSession         session;
        private boolean closed;
        
        public CloseNotifier(RemoteJShellAccessor accessor, ShellSession session) {
            this.accessor = accessor;
            this.session = session;
        }
        
        @Override
        public void connectionClosed(ShellLaunchEvent ev) {
            JShellConnection c = ev.getConnection();
            synchronized (this) {
                if (closed || c != accessor.getOpenedConnection()) {
                    return;
                }
                closed = true;
            }

            notifyDisconnected(session);
            ShellLaunchManager.getInstance().removeLaunchListener(this);
        }

        @Override
        public void agentDestroyed(ShellLaunchEvent ev) {
            synchronized (this) {
                if (closed || ev.getAgent() != agent) {
                    return;
                }
                closed = true;
            }
            notifyShutdown();
            ShellLaunchManager.getInstance().removeLaunchListener(this);
        }

        @Override
        public void connectionInitiated(ShellLaunchEvent ev) {}

        @Override
        public void handshakeCompleted(ShellLaunchEvent ev) {}

    }
}
