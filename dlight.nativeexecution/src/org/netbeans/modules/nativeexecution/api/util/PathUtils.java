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

package org.netbeans.modules.nativeexecution.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;

/**
 *
 * @author Egor Ushakov
 */
public class PathUtils {

    private PathUtils() {
    }

    public static String getPathFromSymlink(String path, ExecutionEnvironment execEnv) {
        try {
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
            npb.setExecutable("/bin/ls").setArguments("-l", path).redirectError(); // NOI18N
            final NativeProcess process = npb.call();
            BufferedReader br = ProcessUtils.getReader(process.getInputStream(), execEnv.isRemote());
            String line = br.readLine(); // just read 1st line...
            br.close();
            if (line != null) {
                int pos = line.indexOf("->"); // NOI18N
                if (pos > 0) {
                    return line.substring(pos + 2).trim();
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static String getExePath(long pid, ExecutionEnvironment execEnv) {
        if (pid > 0) {
            String procdir = "/proc/" + Long.toString(pid); // NOI18N
            String path = PathUtils.getPathFromSymlink(procdir + "/path/a.out", execEnv); // NOI18N - Solaris only?
            if (path == null) {
                path = PathUtils.getPathFromSymlink(procdir + "/exe", execEnv); // NOI18N - Linux?
            }
            if (path != null && path.length() > 0) {
                return path;
            }
        }
        return null;
    }
}
