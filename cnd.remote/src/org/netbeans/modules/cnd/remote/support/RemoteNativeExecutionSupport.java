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
import java.io.InterruptedIOException;
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
import org.openide.util.Utilities;

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

        RemoteUtil.LOGGER.fine("RNES<Init>: Running [" + cmd + "] on " + executionEnvironment);
        Process process;
        try {
            //String cmd = makeCommand(dirf, exe, args, envp);
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
            pb.setExecutable(cmd);
            pb.addEnvironmentVariables(env);

            if (args != null) {
                pb.setArguments(Utilities.parseParameters(args));
            }

            pb.redirectError();
            if (env == null || !env.containsKey("DISPLAY")) { // NOI18N
                pb.setX11Forwarding(true);
            }

            String path = null;
            if (dirf != null) {
                path = RemotePathMap.getPathMap(executionEnvironment).getRemotePath(dirf.getAbsolutePath(),true);
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST) && path.contains(" ")) { // NOI18N
                    RemoteUtil.LOGGER.finest("A PATH WITH A SPACE\n");
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
            if (rc != 0 && RemoteUtil.LOGGER.isLoggable(Level.FINEST)) {
                    RemoteUtil.LOGGER.finest("RNES: " + cmd + " on " + executionEnvironment + " in " + path + " finished; rc=" + rc);
                    if (env == null) {
                        RemoteUtil.LOGGER.finest("RNES: env == null");
                    } else {
                        for (Map.Entry<String, String> entry : env.entrySet()) {
                            RemoteUtil.LOGGER.finest("\tRNES: " + entry.getKey() + "=" + entry.getValue());
                        }
                    }
                    String errMsg;
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((errMsg = reader.readLine()) != null) {
                        RemoteUtil.LOGGER.finest("RNES ERROR: " + errMsg);
                    }
            }
            setExitStatus(rc);
            out.flush();
            is.close();
            in.close();

        } catch (InterruptedException ie) {
            // this occurs, for example, when user stops running program - need no report
        } catch (InterruptedIOException ie) {
            // this occurs, for example, when user stops running program - need no report
        } catch (IOException ioe) {
            RemoteUtil.LOGGER.log(Level.WARNING, ioe.getMessage(), ioe);
        } catch (Exception ex) {
            RemoteUtil.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            RemoteUtil.LOGGER.finest("RNES return value: " + getExitStatus());
//            disconnect();
        }
    }

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
