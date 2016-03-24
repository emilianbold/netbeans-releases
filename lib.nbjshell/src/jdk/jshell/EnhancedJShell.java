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
import java.util.logging.Level;
import java.util.logging.Logger;
import static jdk.jshell.ExecutionControl.defaultJavaVMParameters;

/**
 *
 * @author sdedic
 */
public class EnhancedJShell extends JShell {
    private ExecutionEnv execEnv;
    
    public EnhancedJShell(Builder b, ExecutionEnv env) {
        super(b);
        this.execEnv = env;
    }
    
    protected String decorateLaunchArgs(String s) {
        return s;
    }

    @Override
    protected ExecutionControl createExecutionControl() {
        if (execEnv != null) {
            if (execEnv instanceof JDILaunchControl) {
                ((JDILaunchControl)execEnv).init(this);
            }
            return new NbExecutionControl(null, execEnv, maps, this);
        } else {
            // the default implementation
            JDIEnv env = new JDIEnv(this);
            return new ExecutionControl(env, new JDILaunchControl().init(this), maps, this);
        }
    }
    
    public static class JDILaunchControl implements ExecutionEnv, Executor {
        JDIEnv jdi;
        Socket socket;
        OutputStream out;
        InputStream in;
        JShell state;
        VirtualMachine vm;
        
        public JDILaunchControl() {
        }
        
        public JDILaunchControl init(JShell instance) {
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
            vm.redefineClasses((Map<ReferenceType, byte[]>)(Map)maps);
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

        private boolean userCodeRunning;
        
        @Override
        public void execute(Runnable command) {
            synchronized (this) {
                userCodeRunning = true;
            }
            try {
                command.run();
            } finally {
                synchronized (this) {
                    userCodeRunning = false;
                }
            }
        }
        
        
        protected VirtualMachine acquireVirtualMachine() throws IOException {
            JShell.Subscription sub = null;
            try (ServerSocket listener = new ServerSocket(0)) {
                // timeout after 60 seconds
                listener.setSoTimeout(60000);
                int port = listener.getLocalPort();
                VirtualMachine vm = jdiGo(port);
                sub = state.onShutdown(e -> {
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
        //MessageOutput.textResources = ResourceBundle.getBundle("impl.TTYResources",
            //        Locale.getDefault());

            String connect = "com.sun.jdi.CommandLineLaunch:";
            String cmdLine = "jdk.internal.jshell.remote.AgentWorker";
            String javaArgs = state instanceof EnhancedJShell ?
                    ((EnhancedJShell)state).decorateLaunchArgs(defaultJavaVMParameters().get()) : defaultJavaVMParameters().get();

            String connectSpec = connect + "main=" + cmdLine + " " + port + ",options=" + javaArgs + ",";
            boolean launchImmediately = true;
            int traceFlags = 0;// VirtualMachine.TRACE_SENDS | VirtualMachine.TRACE_EVENTS;

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

        @Override
        public boolean sendStopUserCode() throws IllegalStateException {
            vm.suspend();
            try {
                OUTER: for (ThreadReference thread : vm.allThreads()) {
                    // could also tag the thread (e.g. using name), to find it easier
                    for (StackFrame frame : thread.frames()) {
                        String remoteAgentName = "jdk.internal.jshell.remote.RemoteAgent";
                        if (remoteAgentName.equals(frame.location().declaringType().name()) &&
                            "commandLoop".equals(frame.location().method().name())) {
                            ObjectReference thiz = frame.thisObject();
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
            } catch (ClassNotLoadedException | IncompatibleThreadStateException | InvalidTypeException  ex) {
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
}
