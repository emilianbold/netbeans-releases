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
package org.netbeans.modules.nativeexecution.support.filesearch.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearchParams;
import org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.filesearch.FileSearcher.class, position = 100)
public class RemoteFileSearcherImpl implements FileSearcher {

    private static final java.util.logging.Logger log = Logger.getInstance();

    public String searchFile(FileSearchParams fileSearchParams) {
        final ExecutionEnvironment execEnv = fileSearchParams.getExecEnv();

        if (execEnv.isLocal()) {
            return null;
        }

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);

            if (hostInfo == null) {
                return null;
            }

            String shell = hostInfo.getShell();

            if (shell == null) {
                return null;
            }

            List<String> sp = new ArrayList<String>(fileSearchParams.getSearchPaths());

            if (fileSearchParams.isSearchInUserPaths()) {
                sp.addAll(getPaths(execEnv, shell));
            }

            StringBuilder cmd = new StringBuilder();

            for (Iterator<String> i = sp.iterator(); i.hasNext();) {
                String path = i.next();
                if (path.indexOf('"') < 0) { // Will not use paths with "
                    cmd.append("/bin/ls \"").append(path); // NOI18N
                    cmd.append("/" + fileSearchParams.getFilename() + "\""); // NOI18N
                    if (i.hasNext()) {
                        cmd.append(" || "); // NOI18N
                    }
                }
            }


            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(shell).setArguments("-c", cmd.toString()); // NOI18N

            Process p = npb.call();
            String line = ProcessUtils.readProcessOutputLine(p);
            p.waitFor();

            return (line == null || "".equals(line.trim())) ? null : line.trim(); // NOI18N
        } catch (Throwable th) {
            log.log(Level.FINE, "Execption in UnixFileSearcherImpl:", th); // NOI18N
        }

        return null;
    }

    private List<String> getPaths(ExecutionEnvironment execEnv, String shell) {
        List<String> result = new ArrayList<String>();

        try {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable(shell).setArguments("-c", "echo $PATH"); // NOI18N

            Process p = npb.call();
            result.addAll(Arrays.asList(ProcessUtils.readProcessOutputLine(p).split(":"))); // NOI18N
            p.waitFor();
        } catch (Throwable ex) {
            log.log(Level.FINE, "Execption in UnixFileSearcherImpl.getPaths():", ex); // NOI18N
        }

        return result;
    }
}
