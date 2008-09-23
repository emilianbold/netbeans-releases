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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import org.netbeans.modules.cnd.remote.mapper.RemoteHostInfoProvider;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;

/**
 * This support is intended to work with RemoteNativeExecution and provide input (and eventually
 * output) for project actions.
 *
 * @author gordonp
 */
public class RemoteNativeExecutionSupport extends RemoteConnectionSupport {

    public RemoteNativeExecutionSupport(String key, int port, File dirf, String exe, String args, String[] envp, PrintWriter out, Reader userInput) {
        super(key, port);

        log.fine("RNES<Init>: Running [" + exe + "] on " + key);
        try {
            setChannelCommand(dirf, exe, args, envp);
            InputStream is = channel.getInputStream();
            Reader in = new InputStreamReader(is);
            channel.setInputStream(new ReaderInputStream(userInput));

            channel.connect();

            do {
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
                } catch (Exception ee) {
                }
            } while (!channel.isClosed());

            out.flush();
            is.close();
            in.close();

        } catch (JSchException jse) {
        } catch (IOException ex) {
        } finally {
            log.finest("RNES return value: " + getExitStatus());
            disconnect();
        }
    }

    public RemoteNativeExecutionSupport(String key, File dirf, String exe, String args, String[] envp, PrintWriter out, Reader userInput) {
        this(key, 22, dirf, exe, args, envp, out, userInput);
    }

    private void setChannelCommand(File dirf, String exe, String args, String[] envp) throws JSchException {
        String dircmd;
        String path = RemotePathMap.getMapper(key).getRemotePath(dirf.getAbsolutePath());

        if (path != null) {
            dircmd = "cd " + path + "; "; // NOI18N
        } else {
            dircmd = "";
        }

        StringBuilder command = new StringBuilder(); // NOI18N
        if (envp != null) {
            command.append(ShellUtils.prepareExportString(envp));
        }
        command.append(exe).append(" ").append(args).append(" 2>&1"); // NOI18N
        command.insert(0, dircmd);

        String theCommand = ShellUtils.wrapCommand(key, command.toString());

        channel = createChannel();
        log.finest("RNES: running command: " + theCommand);
        ((ChannelExec) channel).setCommand(theCommand);
    }

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
