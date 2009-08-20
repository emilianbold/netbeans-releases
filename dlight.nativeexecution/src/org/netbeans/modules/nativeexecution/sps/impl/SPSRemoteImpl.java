/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.sps.impl;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.security.acl.NotOwnerException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

public final class SPSRemoteImpl extends SPSCommonImpl {

    private final ExecutionEnvironment execEnv;
    private String pid = null;

    private SPSRemoteImpl(ExecutionEnvironment execEnv) {
        super(execEnv);
        this.execEnv = execEnv;
    }

    public static SPSCommonImpl getNewInstance(ExecutionEnvironment execEnv) {
        return new SPSRemoteImpl(execEnv);
    }

    synchronized String getPID() {
        if (pid != null) {
            return pid;
        }

        NativeProcess pidFetchProcess = null;

        try {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setCommandLine("/bin/ptree $$"); // NOI18N
            pidFetchProcess = npb.call();

            int result = pidFetchProcess.waitFor();

            if (result != 0) {
                throw new IOException("Unable to get sshd pid"); // NOI18N
            }

            List<String> out = ProcessUtils.readProcessOutput(pidFetchProcess);
            String pidCandidate = null;

            for (String line : out) {
                line = line.trim();
                if (line.endsWith("sshd")) { // NOI18N
                    try {
                        pidCandidate = line.substring(0, line.indexOf(' '));
                    } catch (NumberFormatException ex) {
                    }
                }
            }

            pid = pidCandidate;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            Logger.getInstance().fine(ex.toString());
            try {
                ProcessUtils.logError(Level.FINE, Logger.getInstance(), pidFetchProcess);
            } catch (IOException ioex) {
                Exceptions.printStackTrace(ioex);
            }
        }

        return pid;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        pid = null;
    }

    public synchronized void requestPrivileges(Collection<String> requestedPrivileges, String user, char[] passwd) throws NotOwnerException {
        ConnectionManager mgr = ConnectionManager.getInstance();

        final Session session = ConnectionManagerAccessor.getDefault().
                getConnectionSession(mgr, execEnv, true);

        if (session == null) {
            return;
        }

        // Construct privileges list
        StringBuffer sb = new StringBuffer();

        for (String priv : requestedPrivileges) {
            sb.append(priv).append(","); // NOI18N
        }

        String requestedPrivs = sb.toString();

        OutputStream out = null;
        InputStream in = null;

        String script = "/usr/bin/ppriv -s I+" + // NOI18N
                requestedPrivs + " " + getPID(); // NOI18N

        StringBuffer cmd = new StringBuffer("/sbin/su - "); // NOI18N
        cmd.append(user).append(" -c \""); // NOI18N
        cmd.append(script).append("\"; echo ExitStatus:$?\n"); // NOI18N

        ChannelShell channel = null;
        try {
            channel = (ChannelShell) session.openChannel("shell"); // NOI18N
            channel.setPty(true);
            channel.setPtyType("ldterm"); // NOI18N

            out = channel.getOutputStream();
            in = channel.getInputStream();

            channel.connect();

            PrintWriter w = new PrintWriter(out);
            w.write(cmd.toString());
            w.flush();

            expect(in, "Password:"); // NOI18N

            w.println(passwd);
            w.flush();

            String exitStatus = expect(in, "ExitStatus:%"); // NOI18N

            int status = 1;
            try {
                status = Integer.valueOf(exitStatus).intValue();
            } finally {
                if (status != 0) {
                    NotifyDescriptor dd =
                            new NotifyDescriptor.Message("/sbin/su failed"); // NOI18N
                    DialogDisplayer.getDefault().notify(dd);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (JSchException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            // DO NOT CLOSE CHANNEL HERE...
            // channel.disconnect();

            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Expects some predefined string to appear in reader's stream
     * @param r - reader to use
     * @param expectedString
     * @return
     */
    private final static String expect(
            final InputStream in,
            final String expectedString) {

        int pos = 0;
        int len = expectedString.length();
        char[] cbuf = new char[2];
        StringBuffer sb = new StringBuffer();

        try {
            Reader r = new InputStreamReader(in);
            while (pos != len && r.read(cbuf, 0, 1) != -1) {
                char currentChar = expectedString.charAt(pos);
                if (currentChar == '%') {
                    pos++;
                    sb.append(cbuf[0]);
                } else if (currentChar == cbuf[0]) {
                    pos++;
                } else {
                    pos = 0;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return sb.toString();

    }
}