/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdk.internal.jshell.jdi;

import org.netbeans.lib.nbjshell.NbExecutionControl;
import org.netbeans.lib.nbjshell.RemoteJShellService;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import jdk.jshell.*;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jdk.internal.jshell.debug.InternalDebugControl.DBG_GEN;
import static org.netbeans.lib.nbjshell.NbExecutionControl.CMD_VERSION_INFO;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;

/**
 * JDI-based RemoteShellService implementation.
 * @author sdedic
 */
public class JDIRemoteAgent extends JDIExecutionControl implements ExecutionControl, RemoteJShellService, NbExecutionControl {
    private static final String REMOTE_AGENT_CLASS = "org.netbeans.lib.jshell.agent.AgentWorker"; // NOI18N
    private static final Logger LOG = Logger.getLogger(JDIRemoteAgent.class.getName());
    ObjectOutputStream out;
    ObjectInputStream in;

    public JDIRemoteAgent(UnaryOperator<String> optionsSupplier) {
    }

    @Override
    public void start(ExecutionEnv execEnv) throws IOException {
        super.start(execEnv);
        try {
            Field f = JDIExecutionControl.class.getDeclaredField("remoteIn"); // NOI18N
            f.setAccessible(true);
            in = (ObjectInputStream)f.get(this);
            
            f = JDIExecutionControl.class.getDeclaredField("remoteOut"); // NOI18N
            f.setAccessible(true);
            out = (ObjectOutputStream)f.get(this);
        } catch (ReflectiveOperationException | SecurityException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() {
        super.close();
        closeStreams();
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

    protected void jdiGo(int port) {
        String connectorName = "com.sun.jdi.CommandLineLaunch"; // NOI18N
        Map<String, String> argumentName2Value = new HashMap<>();
        argumentName2Value.put("main", REMOTE_AGENT_CLASS + " " + port); // NOI18N
        StringBuilder sb = new StringBuilder();
        
        execEnv.extraRemoteVMOptions().stream().forEach(s -> {
                    sb.append(" ");
                    sb.append(s);
        });
        argumentName2Value.put("options", sb.toString()); // NOI18N

        boolean launchImmediately = true;
        int traceFlags = 0;// VirtualMachine.TRACE_SENDS | VirtualMachine.TRACE_EVENTS;

        jdiEnv.init(connectorName, argumentName2Value, launchImmediately, traceFlags);
        
        if (jdiEnv.connection().isOpen() && jdiEnv.vm().canBeModified()) {
            /*
             * Connection opened on startup. Start event handler
             * immediately, telling it (through arg 2) to stop on the
             * VM start event.
             */
            new JDIEventHandler(jdiEnv);
        }
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
    
    /**
     * Interrupt a running invoke.
     */
    @Override
    public void stop() {
        Object lock;
        try {
            Field f = JDIExecutionControl.class.getDeclaredField("STOP_LOCK"); // NOI18N
            f.setAccessible(true);
            lock = f.get(this);
            
            f = JDIExecutionControl.class.getDeclaredField("userCodeRunning"); // NOI18N
            f.setAccessible(true);
            synchronized (lock) {
                Object o = f.get(this);
                if (o == Boolean.FALSE) {
                    return;
                }

                VirtualMachine vm = jdiEnv.vm();
                vm.suspend();
                try {
                    OUTER:
                    for (ThreadReference thread : vm.allThreads()) {
                        // could also tag the thread (e.g. using name), to find it easier
                        for (StackFrame frame : thread.frames()) {
                            String remoteAgentName = REMOTE_AGENT_CLASS;
                            if (remoteAgentName.equals(frame.location().declaringType().name())
                                    && "commandLoop".equals(frame.location().method().name())) {
                                ObjectReference thiz = frame.thisObject();
                                if (((BooleanValue) thiz.getValue(thiz.referenceType().fieldByName("inClientCode"))).value()) {
                                    thiz.setValue(thiz.referenceType().fieldByName("expectingStop"), vm.mirrorOf(true));
                                    ObjectReference stopInstance = (ObjectReference) thiz.getValue(thiz.referenceType().fieldByName("stopException"));

                                    vm.resume();
                                    debug(DBG_GEN, "Attempting to stop the client code...\n");
                                    thread.stop(stopInstance);
                                    thiz.setValue(thiz.referenceType().fieldByName("expectingStop"), vm.mirrorOf(false));
                                }

                                break OUTER;
                            }
                        }
                    }
                } catch (ClassNotLoadedException | IncompatibleThreadStateException | InvalidTypeException ex) {
                    debug(DBG_GEN, "Exception on remote stop: %s\n", ex);
                } finally {
                    vm.resume();
                }
            }
        } catch (ReflectiveOperationException | SecurityException ex) {
            
        }
    }

    @Override
    public boolean requestShutdown() {
        if (jdiEnv != null) {
            jdiEnv.shutdown();
        }
        return true;
    }

    public Map<String, String> commandVersionInfo() {
        Map<String, String> result = new HashMap<>();
        try {
            out.writeInt(CMD_VERSION_INFO);
            out.flush();
            int num = in.readInt();
            for (int i = 0; i < num; i++) {
                String key = in.readUTF();
                String val = in.readUTF();
                result.put(key, val);
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Error invoking JShell agent", ex.toString());
        }
        return result;
    }

    @Override
    public String getTargetSpec() {
        return null;
    }
}
