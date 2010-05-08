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
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Utilities;

/**
 * This support is intended to work with RemoteNativeExecution and provide input (and eventually
 * output) for project actions.
 *
 * @author gordonp
 */
public class RemoteNativeExecutionSupport extends RemoteConnectionSupport {

    private NativeProcess process;
    private final Object procLock = new Object();

    private final File dirf;
    private final String cmd;
    private final String args;
    private final Map<String, String> env;
    private final boolean x11forwarding;

    public RemoteNativeExecutionSupport(ExecutionEnvironment execEnv, File dirf, String cmd,
            String args, Map<String, String> env) {
        this(execEnv, dirf, cmd, args, env, false);
    }

    public RemoteNativeExecutionSupport(ExecutionEnvironment execEnv, File dirf, String cmd,
            String args, Map<String, String> env, boolean x11forwarding) {
        super(execEnv);
        this.args = args;
        this.dirf = dirf;
        this.cmd = cmd;
        this.env = env;
        this.x11forwarding = x11forwarding;
    }


    public void execute(PrintWriter out, Reader userInput) {
        CndUtils.assertTrue(process == null, "Instance of " + getClass().getSimpleName() + " should not be reused"); //NOI18N
        try {
            //String cmd = makeCommand(dirf, exe, args, envp);
            NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);
            pb.setExecutable(cmd);
            pb.getEnvironment().putAll(env);

            if (args != null) {
                pb.setArguments(Utilities.parseParameters(args));
            }

            pb.redirectError();
            pb.setX11Forwarding(x11forwarding);

            String path = null;
            if (dirf != null) {
                path = RemotePathMap.getPathMap(executionEnvironment).getRemotePath(dirf.getAbsolutePath(),true);
                if (RemoteUtil.LOGGER.isLoggable(Level.FINEST) && path.contains(" ")) { // NOI18N
                    RemoteUtil.LOGGER.finest("A PATH WITH A SPACE\n");
                }
                pb = pb.setWorkingDirectory(path);
            }
            RemoteUtil.LOGGER.log(Level.FINE, "RNES<Init>: Running [{0}] on {1} in {2}", new Object[]{cmd, executionEnvironment, path});
            synchronized (procLock) {
                process = pb.call();
            }
            // no use to synchronize in the rest of the method
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
                    RemoteUtil.LOGGER.log(Level.FINEST, "RNES: {0} on {1} in {2} finished; rc={3}", new Object[]{cmd, executionEnvironment, path, rc});
                    if (env == null) {
                        RemoteUtil.LOGGER.finest("RNES: env == null");
                    } else {
                        for (Map.Entry<String, String> entry : env.entrySet()) {
                            RemoteUtil.LOGGER.log(Level.FINEST, "\tRNES: {0}={1}", new Object[]{entry.getKey(), entry.getValue()});
                        }
                    }
                    String errMsg;
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((errMsg = reader.readLine()) != null) {
                        RemoteUtil.LOGGER.log(Level.FINEST, "RNES ERROR: {0}", errMsg);
                    }
            }
            setExitStatus(rc);
            out.flush();
            is.close();
            in.close();

        } catch (InterruptedException ie) {
            // this occurs, for example, when user stops running program - need no report
            RemoteUtil.LOGGER.fine("RNES: interrupted (1)");
            kill();
        } catch (InterruptedIOException ie) {
            // this occurs, for example, when user stops running program - need no report
            RemoteUtil.LOGGER.fine("RNES: interrupted (2)");
            kill();
        } catch (IOException ioe) {
            RemoteUtil.LOGGER.log(Level.WARNING, ioe.getMessage(), ioe);
        } catch (Exception ex) {
            RemoteUtil.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            RemoteUtil.LOGGER.log(Level.FINEST, "RNES return value: {0}", getExitStatus());
//            disconnect();
        }
    }

    private void kill() {
        Process p = null;
        synchronized (procLock) {
            p = process;
        }
        if (p == null) {
            RemoteUtil.LOGGER.fine("RNES: process is null, can't kill");
        } else {
            RemoteUtil.LOGGER.log(Level.FINE, "RNES: killing {0}", p);
            p.destroy();
        }
    }

    public void stop() {
        RemoteUtil.LOGGER.log(Level.FINE, "RNES: stop {0}", process);
        kill();
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

        @Override
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
