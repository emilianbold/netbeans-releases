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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;

/**
 * Run a remote command. This remote command should <b>not</b> expect input. The output
 * from the command is stored in a StringWriter and can be gotten via toString().
 *
 * @author gordonp
 */
public class RemoteCommandSupport extends RemoteConnectionSupport {

    private final StringBuilder out = new StringBuilder();
    private final StringBuilder err = new StringBuilder();
    private final String cmd;
    private final Map<String, String> env;
    private final String[] args;

    private boolean interrupted = false;

    public static int run(ExecutionEnvironment execEnv, String cmd) {
        RemoteCommandSupport support = new RemoteCommandSupport(execEnv, cmd);
        return support.run();
    }

    public static int run(ExecutionEnvironment execEnv, String cmd, String... args) {
        RemoteCommandSupport support = new RemoteCommandSupport(execEnv, cmd, null, args);
        return support.run();
    }

    public RemoteCommandSupport(ExecutionEnvironment execEnv, String cmd, Map<String, String> env, String... args) {
        super(execEnv);
        this.cmd = cmd;
        this.env = env;
        this.args = args;
    }


    public RemoteCommandSupport(ExecutionEnvironment execEnv, String cmd, Map<String, String> env) {
        super(execEnv);
        this.cmd = cmd;
        this.env = env;
        this.args = null;
    }

    public RemoteCommandSupport(ExecutionEnvironment execEnv, String cmd) {
        this(execEnv, cmd, null);
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public int run() {
        if (!isFailedOrCancelled()) {
            RemoteUtil.LOGGER.log(Level.FINE, "RemoteCommandSupport<Init>: Running [{0}] on {1}", new Object[]{cmd, executionEnvironment});
            if (SwingUtilities.isEventDispatchThread()) {
                String text = "Running remote command in EDT: " + cmd; //NOI18N
                if (RemoteUtil.LOGGER.isLoggable(Level.FINE)) {
                    RemoteUtil.LOGGER.log(Level.FINE, text, new Exception(text));
                } else {
                    RemoteUtil.LOGGER.warning(text);
                }
            }
            BufferedReader remoteProcessOut = null;
            BufferedReader remoteProcessErr = null;
            try {
//                final String substitutedCommand = substituteCommand();
                NativeProcessBuilder pb = NativeProcessBuilder.newProcessBuilder(executionEnvironment);

                if (args == null) {
                    pb.setCommandLine(cmd);
                } else {
                    pb.setExecutable(cmd);
                    pb.setArguments(args);
                }

                pb.getEnvironment().putAll(env);

                Process process = pb.call();
                InputStream is = process.getInputStream();
                InputStream er = process.getErrorStream();
                if (is == null) { // otherwise we can get an NPE in reader
                    throw new IOException("process (" + process.getClass().getName() + ") returned null input stream"); //NOI18N
                }
                if (er == null) { // otherwise we can get an NPE in reader
                    throw new IOException("process (" + process.getClass().getName() + ") returned null error stream"); //NOI18N
                }
                remoteProcessOut = new BufferedReader(new InputStreamReader(is));
                remoteProcessErr = new BufferedReader(new InputStreamReader(er));
                String line;
                while ((line = remoteProcessOut.readLine()) != null) {
                    if (line != null) {
                        out.append(line).append('\n');
                    }
                }
// TODO (execution) should we revive this?
//                try {
//                    Thread.sleep(100); // according to jsch samples
//                } catch (InterruptedException e) {
//                }
                int rc = process.waitFor();
                RemoteUtil.LOGGER.log(Level.FINE, "RemoteCommandSupport: {0} on {1} finished; rc={2}", new Object[]{cmd, executionEnvironment, rc});
                String errMsg;
                while ((errMsg = remoteProcessErr.readLine()) != null) {
                    if (errMsg != null) {
                        err.append(errMsg).append('\n');
                        if (RemoteUtil.LOGGER.isLoggable(Level.FINEST)) {
                            RemoteUtil.LOGGER.log(Level.FINEST, "RemoteCommandSupport ERROR: {0}", errMsg);
                        }
                    }
                }
                setExitStatus(rc);
            } catch (InterruptedException ie) {
                interrupted = true;
                // log just for information, it's quite normal
                RemoteUtil.LOGGER.log(Level.FINEST, "Interrupted", ie);
            } catch (InterruptedIOException ie) {
                interrupted = true;
                // log just for information, it's quite normal
                RemoteUtil.LOGGER.log(Level.FINEST, "Interrupted", ie);
            } catch (IOException ex) {
                RemoteUtil.LOGGER.log(Level.WARNING, "IO failure during running {0} at {1}", new Object[]{cmd, env});
            } finally {
                if (remoteProcessOut != null) {
                    try {
                        remoteProcessOut.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (remoteProcessErr != null) {
                    try {
                        remoteProcessErr.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return getExitStatus();
    }

    @Override
    public String toString() {
        return getOutput();
    }

    public String getOutput() {
        return out.toString();
    }

    public String getErr() {
        return err.toString();
    }
}
