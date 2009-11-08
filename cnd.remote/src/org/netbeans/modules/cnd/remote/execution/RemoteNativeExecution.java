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

package org.netbeans.modules.cnd.remote.execution;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.execution.NativeExecution;
import org.netbeans.modules.cnd.remote.support.RemoteNativeExecutionSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * This implementation of NativeExecution provides execution on a remote server.
 *
 * @author gordonp
 */
/* package-local */
class RemoteNativeExecution extends NativeExecution {

    private final ExecutionEnvironment execEnv;
    private final File runDirFile;
    private final String executable;
    private final String arguments;
    private final String[] envp;
    private final boolean unbuffer;
    private final boolean x11forwarding;
    private final RemoteNativeExecutionSupport support;

    public RemoteNativeExecution(ExecutionEnvironment execEnv, File runDirFile, String executable,
            String arguments, String[] envp, boolean unbuffer, boolean x11forwarding) {
        this.execEnv = execEnv;
        this.runDirFile = runDirFile;
        this.executable = executable;
        this.arguments = arguments;
        this.envp = envp;
        this.unbuffer = unbuffer;
        this.x11forwarding = x11forwarding;
        if (execEnv != null) {
            support = new RemoteNativeExecutionSupport(execEnv, runDirFile, executable,
                    arguments, envToMap(envp), x11forwarding);
        } else {
            support = null;
        }
    }

    /**
     * Execute an executable, a makefile, or a script
     * @return completion code
     */
    @Override
    public int execute(PrintWriter out, Reader in) throws IOException, InterruptedException {
        if (support == null) {
            return -1;
        } else {
            support.execute(out, in);
            return support.getExitStatus();
        }
    }

    public void stop() {
        if (support != null) {
            support.stop();
        }
    }

    private static Map<String, String> envToMap(String[] envp) {
        Map<String, String> map = new HashMap<String, String>(envp.length);
        for (int i = 0; i < envp.length; i++) {
            String line = envp[i];
            int pos = line.indexOf('=');
            if (pos < 0) {
                log.warning("Incorrect environmant setting: " + line);
            } else {
                String key = line.substring(0, pos).trim();
                String val = line.substring(pos+1).trim();
                map.put(key, val);
            }
        }
        return map;
    }
}
