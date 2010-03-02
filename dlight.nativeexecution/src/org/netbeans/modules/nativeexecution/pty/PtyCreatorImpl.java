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

package org.netbeans.modules.nativeexecution.pty;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.pty.PtyOpenUtility.PtyInfo;
import org.netbeans.modules.nativeexecution.spi.pty.PtyAllocator;
import org.netbeans.modules.nativeexecution.spi.pty.PtyImpl;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author ak119685
 */
@ServiceProvider(service = PtyAllocator.class)
public class PtyCreatorImpl implements PtyAllocator {

    @Override
    public PtyImplementation allocate(final ExecutionEnvironment env) throws IOException {
        PtyImplementation result = null;
        OutputStream output = null;
        InputStream input = null;

        final String ptyOpenUtilityPath = PtyOpenUtility.getInstance().getPath(env);

        try {
            if (env.isLocal()) {
                ProcessBuilder pb = new ProcessBuilder(ptyOpenUtilityPath);
                Process pty = pb.start();
                output = pty.getOutputStream();
                input = pty.getInputStream();
            } else {
                ConnectionManagerAccessor access = ConnectionManagerAccessor.getDefault();
                Session session = access.getConnectionSession(ConnectionManager.getInstance(), env, false);
                ChannelExec echannel = null;

                if (session != null) {
                    synchronized (session) {
                        echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                        echannel.setCommand(ptyOpenUtilityPath);
                        echannel.connect();
                        output = echannel.getOutputStream();
                        input = echannel.getInputStream();
                    }
                }
            }

            PtyInfo ptyInfo = PtyOpenUtility.getInstance().readSatelliteOutput(input);
            result = new PtyImplementation(env, ptyInfo.tty, ptyInfo.pid, input, output);
        } catch (Exception ex) {
            if (input != null) {
                input.close();
            }

            if (output != null) {
                output.close();
            }

            throw new IOException(ex);
        }

        return result;
    }

    @Override
    public boolean isApplicable(ExecutionEnvironment env) {
        return true;
    }

    public final static class PtyImplementation implements PtyImpl {

        private final String tty;
        private final int pid;
        private final InputStream istream;
        private final OutputStream ostream;
        private final ExecutionEnvironment env;
        private final boolean pxlsAware;

        PtyImplementation(ExecutionEnvironment env, String tty, int pid, InputStream istream, OutputStream ostream) throws IOException {
            this.tty = tty;
            this.pid = pid;
            this.istream = istream;
            this.ostream = ostream;
            this.env = env;

            try {
                if (OSFamily.SUNOS.equals(HostInfoUtils.getHostInfo(env).getOSFamily())) {
                    pxlsAware = true;
                } else {
                    pxlsAware = false;
                }
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public final void close() throws IOException {
            ostream.close();
            istream.close();
        }

        @Override
        public String toString() {
            return tty + " (" + pid + ")"; // NOI18N
        }

        @Override
        public InputStream getInputStream() {
            return istream;
        }

        @Override
        public OutputStream getOutputStream() {
            return ostream;
        }

        InputStream getErrorStream() {
            return null;
        }

        public void slaveTIOCSWINSZ(int rows, int cols, int height, int width) {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public void masterTIOCSWINSZ(int cols, int rows, int xpixels, int ypixels) {
            String cmd = pxlsAware
                    ? String.format("cols %d rows %d xpixels %d ypixels %d", cols, rows, xpixels, ypixels) // NOI18N
                    : String.format("cols %d rows %d", cols, rows); // NOI18N
            try {
                SttySupport.getFor(env).apply(this, cmd);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public String getSlaveName() {
            return tty;
        }
    }
}
