/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.jshell.launch;

import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import jdk.jshell.spi.ExecutionEnv;
import org.netbeans.lib.nbjshell.NbExecutionControlBase;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class DebugExecutionEnvironment extends NbExecutionControlBase<ReferenceType> implements RemoteJShellAccessor, ShellLaunchListener {
    private boolean added;
    private volatile JShellConnection shellConnection;
    private boolean closed;
    final ShellAgent agent;
    private String targetSpec;
    private VirtualMachine vm;

    public DebugExecutionEnvironment(ShellAgent agent, String targetSpec) {
        this.agent = agent;
        this.targetSpec = targetSpec;
    }
    
    public JShellConnection getOpenedConnection() {
        synchronized (this) {
            return shellConnection;
        }
    }
    
    public ShellAgent getAgent() {
        return agent;
    }

    @Override
    public Collection<ReferenceType> nameToRef(String className) {
        if (vm == null) {
            return Collections.emptyList();
        } else {
            return vm.classesByName(className);
        }
    }

    @Override
    protected boolean isClosed() {
        return closed;
    }

    @Override
    protected void shutdown() {
        agent.closeConnection(shellConnection);
        super.shutdown();
    }

    @Override
    public boolean redefineClasses(Map<ReferenceType, byte[]> toRedefine) throws IOException {
        if (vm == null) {
            return false;
        }
        vm.redefineClasses(toRedefine);
        return true;
    }

    @Override
    public void start(ExecutionEnv execEnv) throws Exception {
        JShellConnection c = getConnection(false);
        VirtualMachine m = c.getVirtualMachine();
        OutputStream out = c.getAgentInput();
        InputStream in = c.getAgentOutput();
        init(in, out, execEnv);
    }

    @Override
    public void stop() {
        synchronized (getLock()) {
            if (isUserCodeRunning()) {
                sendStopUserCode();
            }
        }
    }

    @Override
    public void close() {
        super.close();
        closeStreams();
    }
    
    public boolean sendStopUserCode() throws IllegalStateException {
        vm.suspend();
        try {
            ObjectReference myRef = getAgentObjectReference();

            OUTER:
            for (ThreadReference thread : vm.allThreads()) {
                // could also tag the thread (e.g. using name), to find it easier
                AGENT: for (StackFrame frame : thread.frames()) {
                    String remoteAgentName = "jdk.internal.jshell.remote.RemoteAgent";
                    if (remoteAgentName.equals(frame.location().declaringType().name()) && "performCommand".equals(frame.location().method().name())) {
                        ObjectReference thiz = frame.thisObject();
                        if (myRef != null && myRef != thiz) {
                            break AGENT;
                        }
                        if (((BooleanValue) thiz.getValue(thiz.referenceType().fieldByName("inClientCode"))).value()) {
                            thiz.setValue(thiz.referenceType().fieldByName("expectingStop"), vm.mirrorOf(true));
                            ObjectReference stopInstance = (ObjectReference) thiz.getValue(thiz.referenceType().fieldByName("stopException"));
                            vm.resume();
                            thread.stop(stopInstance);
                            thiz.setValue(thiz.referenceType().fieldByName("expectingStop"), vm.mirrorOf(false));
                        }
                        return true;
                    }
                }
            }
        } catch (ClassNotLoadedException | IncompatibleThreadStateException | InvalidTypeException ex) {
            throw new IllegalStateException(ex);
        } finally {
            vm.resume();
        }
        return false;
    }

    @Override
    public String getTargetSpec() {
        return targetSpec;
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

    protected ObjectReference getAgentObjectReference() {
        return shellConnection.getAgentHandle();
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
        shutdown();
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
        shutdown();
//        shellEnv.reportClosedBridge(reportSession, false);
    }
    
}
