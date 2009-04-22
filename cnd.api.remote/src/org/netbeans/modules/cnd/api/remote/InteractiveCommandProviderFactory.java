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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Sergey Grinev
 */
public abstract class InteractiveCommandProviderFactory {

    protected abstract InteractiveCommandProvider createInstance(ExecutionEnvironment execEnv);

    protected InteractiveCommandProviderFactory() {
    }

    public static InteractiveCommandProvider create(ExecutionEnvironment execEnv) {
        InteractiveCommandProviderFactory factory = null;
        if (execEnv.isLocal()) {
            factory = Default.instance;
        } else {
            factory = Lookup.getDefault().lookup(InteractiveCommandProviderFactory.class);
        }
        return factory == null ? null : factory.createInstance(execEnv);
    }

    private static class Default extends InteractiveCommandProviderFactory {

       private static InteractiveCommandProviderFactory instance = new Default();

        @Override
        public InteractiveCommandProvider createInstance(ExecutionEnvironment execEnv) {
            if (execEnv.isLocal()) {
                return new LocalInteractiveCommandProvider();
            }
            return null;
        }
    }

    private static final class LocalInteractiveCommandProvider implements InteractiveCommandProvider {

        private Process process;
        int exitStatus = -1;

        public boolean run(List<String> commandAndArgs, String workingDirectory, Map<String, String> env) {
            ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
            Map<String, String> pbenv = pb.environment();
            for (Map.Entry<String, String> entry : env.entrySet()) {
                pbenv.put(entry.getKey(), entry.getValue());
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

        public InputStream getInputStream() throws IOException {
            return process == null ? null : process.getInputStream();
        }

        public OutputStream getOutputStream() throws IOException {
            return process == null ? null : process.getOutputStream();
        }

        public void disconnect() {
            // do nothing
        }

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

        public int getExitStatus() {
            return exitStatus;
        }


        public boolean run(ExecutionEnvironment execEnv, String cmd, Map<String, String> env) {
            throw new UnsupportedOperationException("deprecated."); // NOI18N
        }
    }
}
