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
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static jdk.internal.jshell.debug.InternalDebugControl.DBG_GEN;

/**
 * Makes a bridge from original ExecutionControl and delegates remote
 * operation to the environment. ExecutionControl should handle only protocol-level
 * initiate startup and shutdown.
 * 
 * @author sdedic
 */
public class NbExecutionControl extends ExecutionControl {
    private static final Logger LOG = Logger.getLogger(NbExecutionControl.class.getName());
    
    public static final int CMD_VERSION_INFO = 100;

    public static final int CMD_REDEFINE   =    101;
    public static final int CMD_STOP        =   102;
    public static final int CMD_CLASSID     =   103;
    
    private final RemoteJShellService  execEnv;
    private final JDIEnv jdi;
    private final JShell proc;
    
    public NbExecutionControl(JDIEnv env, RemoteJShellService execEnv, SnippetMaps maps, JShell proc) {
        super(new NullEnv(proc), maps, proc);
        this.execEnv = execEnv;
        this.jdi = env;
        this.proc = proc;
    }
    
    private static class NullEnv extends JDIEnv {
        private JShell state;
        
        public NullEnv(JShell state) {
            super(state);
            this.state = state;
        }

        @Override
        void shutdown() {
            // FIXME: check
            state.closeDown();
        }

        @Override
        VirtualMachine vm() {
            return super.vm();
        }

        @Override
        JDIConnection connection() {
            return null;
        }
    }
    
    boolean commandRedefine(Map<Object, byte[]> mp) {
        try {
            execEnv.redefineClasses(mp);
            return true;
        } catch (UnsupportedOperationException ex) {
            return false;
        } catch (Exception ex) {
            proc.debug(DBG_GEN, "Exception on JDI redefine: %s\n", ex);
            return false;
        }
    }

    Object nameToRef(String name) {
        return execEnv.getClassHandle(name);
    }


    void commandStop() {
        synchronized (STOP_LOCK) {
            if (!isUserCodeRunning())
                return ;
            try {
                proc.debug(DBG_GEN, "Attempting to stop the client code...\n");
                execEnv.sendStopUserCode();
            } catch (IllegalStateException | IOException ex) {
                ex.printStackTrace();
                proc.debug(DBG_GEN, "Exception on remote stop: %s\n", ex.getCause());
            }
        }
    }
    
    void launch() throws IOException {
        execEnv.waitConnected(60000);
        OutputStream os = execEnv.getCommandStream();
        InputStream is = execEnv.getResponseStream();
        out = os instanceof ObjectOutputStream ? (ObjectOutputStream)os : 
                new ObjectOutputStream(os);
        in = is instanceof ObjectInputStream ? (ObjectInputStream)is : 
                new ObjectInputStream(is);

        /*
        try (ServerSocket listener = new ServerSocket(0)) {
            // timeout after 60 seconds
            listener.setSoTimeout(60000);
            int port = listener.getLocalPort();
            jdiGo(port);
            socket = listener.accept();
            // out before in -- match remote creation so we don't hang
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        */
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

    ///////////----------------- NetBeans ----------------///////////
    static String defaultJavaVMParameters() {
        String classPath = System.getProperty("java.class.path");
        String bootclassPath = System.getProperty("sun.boot.class.path");
        String javaArgs = "-classpath " + classPath + "-Xbootclasspath:" + bootclassPath;
        return javaArgs;
    }
}
