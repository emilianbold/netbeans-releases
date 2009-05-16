/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.support;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

/**
 * This support is intended to work with RemoteNativeExecution and provide input (and eventually
 * output) for project actions.
 *
 * @author gordonp
 */
public class RemoteNativeExecutionSupport extends RemoteConnectionSupport {

    public RemoteNativeExecutionSupport(ExecutionEnvironment execEnv, File dirf, String cmd,
            String args, Map<String, String> env, PrintWriter out, Reader userInput) {
        super(execEnv);

        log.fine("RNES<Init>: Running [" + cmd + "] on " + executionEnvironment);
        Process process;
        try {
            //String cmd = makeCommand(dirf, exe, args, envp);
            NativeProcessBuilder pb = new NativeProcessBuilder(executionEnvironment, cmd + " 2>&1",false); //NOI18N
            pb = pb.addEnvironmentVariables(env);
            if (args != null) {
                pb = pb.setArguments(args.trim().split("[ \t]+")); //NOI18N
            }
            String path = null;
            if (dirf != null) {
                path = RemotePathMap.getPathMap(executionEnvironment).getRemotePath(dirf.getAbsolutePath(),true);
                if (log.isLoggable(Level.FINEST) && path.contains(" ")) {
                    log.finest("A PATH WITH A SPACE\n");
                }
                pb = pb.setWorkingDirectory(path);
            }
            process = pb.call();
            InputStream is = process.getInputStream();
            Reader in = new InputStreamReader(is);
            if (userInput != null) {
                InputReaderThread inputReaderThread = new InputReaderThread(
                        process.getOutputStream(), new ReaderInputStream(userInput));
                inputReaderThread.start();
            }
            
//            do {
                int read;
                while ((read = in.read()) != -1) {
                    if (read == 10) { // from LocalNativeExecution (MAC conversion?)
                        out.append('\n');
                    } else {
                        out.append((char) read);
                    }
                }
                try {
                    Thread.sleep(100); // according to jsch samples
                } catch (InterruptedException ie) {
                }
//            } while (!channel.isClosed());

            int rc = process.waitFor();            
            if (rc != 0 && log.isLoggable(Level.FINEST)) {
                    log.finest("RNES: " + cmd + " on " + executionEnvironment + " in " + path + " finished; rc=" + rc);
                    if (env == null) {
                        log.finest("RNES: env == null");
                    } else {
                        for (Map.Entry<String, String> entry : env.entrySet()) {
                            log.finest("\tRNES: " + entry.getKey() + "=" + entry.getValue());
                        }
                    }
                    String errMsg;
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((errMsg = reader.readLine()) != null) {
                        log.finest("RNES ERROR: " + errMsg);
                    }
            }
            setExitStatus(rc);
            out.flush();
            is.close();
            in.close();

        } catch (InterruptedException ie) {
            // this occurs, for example, when user stops running program - need no report
        } catch (IOException ioe) {
            log.log(Level.WARNING, ioe.getMessage(), ioe);
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            log.finest("RNES return value: " + getExitStatus());
//            disconnect();
        }
    }

//    private String makeCommand(File dirf, String exe, String args, String[] envp) {
//        String dircmd;
//        String path = RemotePathMap.getPathMap(executionEnvironment).getRemotePath(dirf.getAbsolutePath());
//
//        if (path != null) {
//            dircmd = "cd \"" + path + "\"; "; // NOI18N
//        } else {
//            dircmd = "";
//        }
//
//        StringBuilder command = new StringBuilder(); // NOI18N
//
//        // if (enableDisplayVariable) {
//        if (envp == null ) {
//            envp = new String[1];
//            envp[0] = getDisplayString();
//        } else {
//            String[] envp2 = new String[envp.length + 1 ];
//            for (int i = 0; i < envp.length; i++) {
//                envp2[i] = envp[i];
//            }
//            envp2[envp.length] = getDisplayString();
//            envp = envp2;
//        }
//
//        if (envp != null) {
//            command.append(ShellUtils.prepareExportString(envp));
//        }
//        command.append(exe).append(" ").append(args).append(" 2>&1"); // NOI18N
//        command.insert(0, dircmd);
//
//        String theCommand = ShellUtils.wrapCommand(executionEnvironment, command.toString());
//        return theCommand;
//    }

    private static String displayString ;

    private static String getDisplayString() {
        if (displayString == null) {
            try {
                String localDisplay = PlatformInfo.getDefault(ExecutionEnvironmentFactory.getLocal()).getEnv().get("DISPLAY"); //NOI18N
                if (localDisplay == null) {
                    localDisplay = ":.0"; //NOI18N
                }
                displayString = "DISPLAY=" + InetAddress.getLocalHost().getHostAddress() + localDisplay; //NOI18N
            } catch (UnknownHostException ex) {
                displayString = "";
            }
        }
        return displayString;
    }


    /** Helper class to read the input from the build */
    private static final class InputReaderThread extends Thread {

        /** This is all output, not just stderr */
        private InputStream in;
        private OutputStream pout;

        public InputReaderThread(OutputStream pout, InputStream in) {
            this.pout = pout;
            this.in = in;
            setName("inputReaderThread"); // NOI18N - Note NetBeans doesn't xlate "IDE Main"
        }

        /**
         *  Reader proc to read input from Output2's input textfield and send it
         *  to the running process.
         */
        @Override
        public void run() {
            int ch;

            try {
                while ((ch = in.read()) != (-1)) {
                    pout.write((char) ch);
                    pout.flush();
                }
            } catch (IOException e) {
            } finally {
                // Handle EOF and other exits
                try {
                    pout.flush();
                    pout.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    //TODO (execution): why do we need this?
    private final static class ReaderInputStream extends InputStream {

        private final Reader reader;

        public ReaderInputStream(Reader reader) {
            super();
            this.reader = reader;
        }

        public int read() throws IOException {
            int t = reader.read();
            return t;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if ((off < 0) || (off > b.length) || (len < 0) ||
                    ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int c = read();
            if (c == -1) {
                return -1;
            }
            b[off] = (byte) c;
            return 1;
        }
    }
}
