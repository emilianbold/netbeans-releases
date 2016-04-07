/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jshell.launch;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.UnaryOperator;
import jdk.jshell.JDIRemoteAgent;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class DebugExecutionEnvironment extends JDIRemoteAgent implements RemoteJShellAccessor, ShellLaunchListener {
    private boolean added;
    private volatile JShellConnection shellConnection;
    private boolean closed;
    final ShellAgent agent;

    public DebugExecutionEnvironment(ShellAgent agent) {
        super(UnaryOperator.identity());
        this.agent = agent;
    }
    
    public JShellConnection getOpenedConnection() {
        synchronized (this) {
            return shellConnection;
        }
    }
    
    public ShellAgent getAgent() {
        return agent;
    }

    @NbBundle.Messages({
        "MSG_AgentConnectionBroken2=Connection to Java Shell agent broken. Please re-run the project.", 
        "# {0} - error message", 
        "MSG_ErrorConnectingToAgent=Error connecting to Java Shell agent: {0}"
    })
    private JShellConnection getConnection(boolean dontConnect) throws IOException {
        synchronized (this) {
            if (closed || (dontConnect && shellConnection == null)) {
                throw new IOException(Bundle.MSG_AgentConnectionBroken2());
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
            StatusDisplayer.getDefault().setStatusText(Bundle.MSG_ErrorConnectingToAgent(ex.getLocalizedMessage()), 100);
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
    public void connectionClosed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getConnection() != this.shellConnection || closed) {
                return;
            }
            closed = true;
        }
//        shellEnv.reportClosedBridge(reportSession, true);
    }

    @Override
    public void agentDestroyed(ShellLaunchEvent ev) {
        synchronized (this) {
            if (ev.getAgent() != agent || closed) {
                return;
            }
            closed = true;
        }
//        shellEnv.reportClosedBridge(reportSession, false);
    }
    
}
