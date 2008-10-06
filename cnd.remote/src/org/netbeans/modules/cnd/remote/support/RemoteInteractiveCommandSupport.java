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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Run an interactive remote command. This class differs from RemoteCommandSupport in that
 * it doesn't read input. Instead, the user is expected to process all I/O.
 * 
 * @author gordonp
 */
public class RemoteInteractiveCommandSupport extends RemoteConnectionSupport {

    private InputStream in;
    private OutputStream out;
    private final String cmd;
    private final Map<String, String> env;

    public RemoteInteractiveCommandSupport(String hkey, String cmd, Map<String, String> env) {
        this(hkey, cmd, env, null, null);
    }

    public RemoteInteractiveCommandSupport(String hkey, String cmd, Map<String, String> env, InputStream in, OutputStream out) {
        super(hkey, 22);
        this.cmd = cmd;
        this.env = env;
        this.in = in;
        this.out = out;

        if (!isFailedOrCancelled()) {
            log.fine("RemoteInteractiveCommandSupport<Init>: Running [" + cmd + "] on " + hkey);
            try {
                channel = createChannel();
            } catch (JSchException jse) {
            }
        }
    }
    
    public InputStream getInputStream() throws IOException {
        return channel.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        return channel.getOutputStream();
    }

    @Override
    protected Channel createChannel() throws JSchException {
        ChannelExec echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
        StringBuilder cmdline = new StringBuilder();

        if (env != null) {
            cmdline.append(ShellUtils.prepareExportString(env));
        }
        cmdline.append(cmd);

        echannel.setCommand(ShellUtils.wrapCommand(key, cmdline.toString()));
        echannel.setInputStream(in);
        echannel.setOutputStream(out);
        echannel.setErrStream(out);
        echannel.connect();
        return echannel;
    }

    int waitFor() {
        while (channel != null && !channel.isClosed() && channel.isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        return getExitStatus();

    }
}
