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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.execution.NativeExecution;
import org.netbeans.modules.cnd.api.remote.CommandProvider;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.cnd.remote.server.RemoteServerSetup;
import org.netbeans.modules.cnd.remote.support.RemoteNativeExecutionSupport;
import org.openide.util.Lookup;

/**
 * This implementation of NativeExecution provides execution on a remote server.
 *
 * @author gordonp
 */
public class RemoteNativeExecution extends NativeExecution {
    
    /**
     * Execute an executable, a makefile, or a script
     * @param runDir absolute path to directory from where the command should be executed
     * @param executable absolute or relative path to executable, makefile, or script
     * @param arguments space separated list of arguments
     * @param envp environment variables (name-value pairs of the form ABC=123)
     * @param out Output
     * @param io Input
     * @param parseOutput true if output should be parsed for compiler errors
     * @return completion code
     */
    public int executeCommand(
            File runDirFile,
            String executable,
            String arguments,
            String[] envp,
            PrintWriter out,
            Reader in,
            boolean unbuffer) throws IOException, InterruptedException {
        // Updating environment variables
        List<String> envpList = new ArrayList<String>();
        if (envp != null) {
            envpList.addAll(Arrays.asList(envp));
        }
        envpList.add("SPRO_EXPAND_ERRORS="); // NOI18N
        
        if (unbuffer) {
            int platformType = PlatformInfo.getDefault(host).getPlatform();
            String unbufferPath = getUnbufferPath(platformType);
            if (unbufferPath != null) {
                if (platformType == PlatformTypes.PLATFORM_MACOSX) {
                    envpList.add("DYLD_INSERT_LIBRARIES=" + unbufferPath); // NOI18N
                    envpList.add("DYLD_FORCE_FLAT_NAMESPACE=yes"); // NOI18N
                } else {
                    envpList.add("LD_PRELOAD=" + unbufferPath); // NOI18N
                }
            }
        }
        envp = envpList.toArray(new String[envpList.size()]);

        RemoteNativeExecutionSupport support = null;
        if (host != null && host.length() > 0) {
            support = new RemoteNativeExecutionSupport(host, runDirFile, executable, arguments, envp, out, in);
        }
        return support == null ? -1 : support.getExitStatus();
    }
    
    public void stop() {
    }

    private String getUnbufferPath(int platformType) {
        String path = null;
        CommandProvider provider = (CommandProvider) Lookup.getDefault().lookup(CommandProvider.class);
        if (provider != null) {
            int rc = provider.run(host, "echo $HOME", null); // NOI18N
            if (rc == 0) {
                path = provider.toString().trim(); // remote the newline
            }
        }
        if (path == null) {
            path = "/home/" + System.getProperty("user.name"); // NOI18N
        }
        path += "/" + RemoteServerSetup.REMOTE_LIB_DIR;
        switch (platformType) {
            case PlatformTypes.PLATFORM_LINUX : return path + "unbuffer-Linux-x86.so"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_SPARC : return path + "unbuffer-SunOS-sparc.so"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL : return path + "unbuffer-SunOS-x86.so"; // NOI18N
        }
        return null;
    }
}
