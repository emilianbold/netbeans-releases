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

import java.io.IOException;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;

public class CommandLineHelper {

    private static final WeakHashMap<ExecutionEnvironment, CommandLineHelper> cache =
            new WeakHashMap<ExecutionEnvironment, CommandLineHelper>();
    private final ExecutionEnvironment ee;
    private final HostInfo hostInfo;
    private final boolean isWindows;
    private final boolean isMacOS;

    private CommandLineHelper(ExecutionEnvironment ee) {
        this.ee = ee;

        HostInfo hinfo = null;
        try {
            hinfo = HostInfoUtils.getHostInfo(ee);
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        hostInfo = hinfo;
        isWindows = hostInfo != null && hostInfo.getOSFamily() == OSFamily.WINDOWS;
        isMacOS = hostInfo != null && hostInfo.getOSFamily() == OSFamily.MACOSX;
    }

    public static CommandLineHelper getInstance(ExecutionEnvironment execEnv) {
        CommandLineHelper result = null;

        synchronized (cache) {
            if (cache.containsKey(execEnv)) {
                result = cache.get(execEnv);
            }

            if (result == null) {
                result = new CommandLineHelper(execEnv);
                cache.put(execEnv, result);
            }
        }

        return result;
    }

    public String toShellPaths(String ldLibPath) {
        if (isWindows) {
            return WindowsSupport.getInstance().convertToAllShellPaths(ldLibPath);
        } else {
            return ldLibPath;
        }
    }

    public String toSystemPath(String orig) {
        String result;

        if (isWindows) {
            if (orig.startsWith("\"")) { // NOI18N
                orig = orig.substring(1, orig.length() - 1);
            }

            orig = WindowsSupport.getInstance().convertToWindowsPath(orig);
            result = "\"" + orig + "\""; // NOI18N
        } else {
            result = escapeSpaces(orig);
        }


        return result;
    }

    public String toShellPath(String orig) {
        String result;

        if (isWindows) {
            result = WindowsSupport.getInstance().convertToShellPath(orig);
        } else {
            result = escapeSpaces(orig);
        }

        return result;
    }

    private String escapeSpaces(String orig) {
        return orig.replaceAll("([^\\\\]) ", "$1\\\\ "); // NOI18N
    }
}
