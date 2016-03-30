/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdk.jshell;

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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.UnaryOperator;

/**
 *
 * @author sdedic
 */
public class JDIRemoteAgent implements RemoteJShellService, Executor {
    JDIEnv jdi;
    Socket socket;
    OutputStream out;
    InputStream in;
    JShell state;
    VirtualMachine vm;
    UnaryOperator<String> vmOptionsSupplier;

    public JDIRemoteAgent(UnaryOperator<String> optionsSupplier) {
        this.vmOptionsSupplier = optionsSupplier == null ? UnaryOperator.identity() : optionsSupplier;
    }

    public JDIRemoteAgent init(JShell instance) {
        this.state = instance;
        return this;
    }

    @Override
    public void closeStreams() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException ex) {
            // ignore
        }
    }

    @Override
    public void redefineClasses(Map<Object, byte[]> maps) {
        vm.redefineClasses((Map<ReferenceType, byte[]>) (Map) maps);
    }

    @Override
    public Object getClassHandle(String name) {
        List<ReferenceType> rtl = vm.classesByName(name);
        if (rtl.size() != 1) {
            return null;
        }
        return rtl.get(0);
    }

    @Override
    public Executor getCodeExecutor() {
        return this;
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }

    protected VirtualMachine acquireVirtualMachine() throws IOException {
        JShell.Subscription sub = null;
        try (final ServerSocket listener = new ServerSocket(0)) {
            // timeout after 60 seconds
            listener.setSoTimeout(60000);
            int port = listener.getLocalPort();
            VirtualMachine vm = jdiGo(port);
            sub = state.onShutdown((jdk.jshell.JShell e) -> {
                try {
                    listener.close();
                } catch (IOException ex) {
                }
            });
            socket = listener.accept();
            // out before in -- match remote creation so we don't hang
            out = socket.getOutputStream();
            in = socket.getInputStream();
            return vm;
        } finally {
            if (sub != null) {
                state.unsubscribe(sub);
            }
        }
    }

    @Override
    public void waitConnected(long l) throws IOException {
        vm = acquireVirtualMachine();
    }

    protected VirtualMachine jdiGo(int port) {
        this.jdi = new JDIEnv(state);
        String connect = "com.sun.jdi.CommandLineLaunch:";
        String cmdLine = "jdk.internal.jshell.remote.AgentWorker";
        String javaArgs = vmOptionsSupplier.apply(NbExecutionControl.defaultJavaVMParameters());
        String connectSpec = connect + "main=" + cmdLine + " " + port + ",options=" + javaArgs + ",";
        boolean launchImmediately = true;
        int traceFlags = 0; // VirtualMachine.TRACE_SENDS | VirtualMachine.TRACE_EVENTS;
        jdi.init(connectSpec, launchImmediately, traceFlags);
        if (jdi.connection().isOpen() && jdi.vm().canBeModified()) {
            /*
             * Connection opened on startup. Start event handler
             * immediately, telling it (through arg 2) to stop on the
             * VM start event.
             */
            new JDIEventHandler(jdi);
        }
        return jdi.vm();
    }

    @Override
    public InputStream getResponseStream() throws IOException {
        return in;
    }

    @Override
    public OutputStream getCommandStream() throws IOException {
        return out;
    }
    
    /**
     * Returns the agent's object reference obtained from the debugger.
     * May return null, so the {@link #sendStopUserCode()} will stop the first
     * running agent it finds.
     * 
     * @return the target agent's reference
     */
    protected ObjectReference  getAgentObjectReference() {
        return null;
    }

    @Override
    public boolean sendStopUserCode() throws IllegalStateException {
        vm.suspend();
        try {
            ObjectReference myRef = getAgentObjectReference();

            OUTER:
            for (ThreadReference thread : vm.allThreads()) {
                // could also tag the thread (e.g. using name), to find it easier
                AGENT: for (StackFrame frame : thread.frames()) {
                    String remoteAgentName = "jdk.internal.jshell.remote.RemoteAgent";
                    if (remoteAgentName.equals(frame.location().declaringType().name()) && "commandLoop".equals(frame.location().method().name())) {
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
    public String decorateLaunchArgs(String args) {
        return args;
    }

    @Override
    public boolean requestShutdown() {
        if (jdi != null) {
            jdi.shutdown();
        }
        return true;
    }
    
}
