/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 *
 * @author ak119685
 */
public final class JschSupport {

    private final static java.util.logging.Logger log = Logger.getInstance();

    private JschSupport() {
    }

    public static ChannelStreams execCommand(final ExecutionEnvironment env, final String command, final ChannelParams params)
            throws IOException, JSchException {

        final ConnectionManager mgr = ConnectionManager.getInstance();

        final Session session = ConnectionManagerAccessor.getDefault().
                getConnectionSession(mgr, env, true);


        int retry = 2;

        while (retry-- > 0) {
            try {
                ChannelExec echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                echannel.setCommand(command);
                echannel.setXForwarding(params == null ? false : params.x11forward);
                echannel.connect(10000);

                return new ChannelStreams(echannel,
                        echannel.getInputStream(),
                        echannel.getErrStream(),
                        echannel.getOutputStream());
            } catch (JSchException ex) {
                String message = ex.getMessage();
                Throwable cause = ex.getCause();
                if (cause != null && cause instanceof NullPointerException) {
                    // Jsch bug... retry?
                } else if ("java.io.InterruptedIOException".equals(message)) { // NOI18N
                    log.log(Level.FINE, "RETRY to open jsch channel in 0.5 seconds [%s]...", retry); // NOI18N
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else if ("channel is not opened.".equals(message)) { // NOI18N
                    // Looks like in this case an attempt to
                    // just re-attempt to open a channel
                    // will fail - so do disconnect...
                    mgr.disconnect(env);
                    log.log(Level.FINE, "RETRY to open jsch channel in 0.5 seconds [%s]...", retry); // NOI18N
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex1) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    throw ex;
                }

            } catch (NullPointerException npe) {
                // Jsch bug... retry? ;)
            }
        }

        throw new IOException("Failed to execute " + command); // NOI18N
    }

    public final static class ChannelStreams {

        public final InputStream out;
        public final InputStream err;
        public final OutputStream in;
        public final ChannelExec channel;

        public ChannelStreams(ChannelExec channel, InputStream out,
                InputStream err, OutputStream in) {
            this.channel = channel;
            this.out = out;
            this.err = err;
            this.in = in;
        }
    }

    public static final class ChannelParams {

        private boolean x11forward = false;

        public void setX11Forwarding(boolean forward) {
            this.x11forward = forward;
        }
    }
}
