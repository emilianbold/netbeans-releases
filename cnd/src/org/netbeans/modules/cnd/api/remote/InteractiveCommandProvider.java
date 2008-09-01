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
package org.netbeans.modules.cnd.api.remote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.utils.RemoteUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * An interface to allow cnd modules to run a RemoteCommandSupport from cnd.remote.
 * 
 * @author gordonp
 */
public abstract class InteractiveCommandProvider {

    /**
     * Run a remote commane via cnd.remote's RemoteInteractiveCommandSupport.
     * 
     * @param hkey The user and remote host (user@host)
     * @param cmd The command to run
     * @param env The (possibly null) environment to send to the remote command
     * @return true if the command started, otherwise false
     */
    public abstract boolean run(String hkey, String cmd, Map<String, String> env);

    public abstract boolean run(List<String> commandAndArgs, String workingDirectory, Map<String, String> env);

    public abstract InputStream getInputStream() throws IOException;

    public abstract OutputStream getOutputStream() throws IOException;

    public abstract void disconnect();

    public abstract int waitFor();

    public abstract int getExitStatus();

    protected abstract void init(String hkey);

    public static InteractiveCommandProvider getDefault(String hkey) {
        if (RemoteUtils.isLocalhost(hkey)) {
            return localInstance;
        } else {
            if (remoteInstance == null) {
                remoteInstance = Lookup.getDefault().lookup(InteractiveCommandProvider.class);
                remoteInstance.init(hkey);
            }
            return remoteInstance;
        }
    }
    private static final InteractiveCommandProvider localInstance = new LocalInteractiveCommandProvider();
    private static InteractiveCommandProvider remoteInstance = null;

    private static final class LocalInteractiveCommandProvider extends InteractiveCommandProvider {

        private Process process;
        int exitStatus = -1;

        @Override
        public boolean run(List<String> commandAndArgs, String workingDirectory, Map<String, String> env) {
            ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
            Map<String, String> pbenv = pb.environment();
            for (String key : env.keySet()) {
                pbenv.put(key, env.get(key));
            }
            pb.directory(new File(workingDirectory));
            pb.redirectErrorStream(true);
            try {
                process = pb.start();
            } catch (IOException ex) {
                //TODO: IOException 
                Exceptions.printStackTrace(ex);
                return false;
            }
            return false;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return process == null ? null : process.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return process == null ? null : process.getOutputStream();
        }

        @Override
        public void disconnect() {
            // do nothing
        }

        @Override
        public int waitFor() {
            if (process != null) {
                try {
                    exitStatus = process.waitFor();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            return exitStatus;
        }

        @Override
        public int getExitStatus() {
            return exitStatus;
        }

        @Override
        protected void init(String hkey) {
            assert RemoteUtils.isLocalhost(hkey);
        }

        @Override
        public boolean run(String hkey, String cmd, Map<String, String> env) {
            throw new UnsupportedOperationException("deprecated.");
        }
    }
}
