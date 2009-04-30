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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Currently remote Windows execution is not considered...
 *
 */
public final class WindowsSupport {

    private static final WindowsSupport instance = new WindowsSupport();
    private String cygwinBase = null;
    private String cygwinShell = null;
    private String msysBase = null;
    private String msysShell = null;

    private WindowsSupport() {
    }

    public static WindowsSupport getInstance() {
        return instance;
    }

    public String getShell() {
        // 1. Try to find cygwin ...
        String cygwinRoot = queryWindowsRegistry(
                "HKLM\\SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\/", // NOI18N
                "native", // NOI18N
                ".*native.*REG_SZ(.*)"); // NOI18N

        if (cygwinRoot != null) {
            File sh = new File(cygwinRoot + "/bin/sh.exe"); // NOI18N
            if (sh.exists() && sh.canRead()) {
                cygwinShell = sh.getAbsolutePath();
                cygwinBase = sh.getParentFile().getParentFile().getAbsolutePath();
                return cygwinShell;
            }
        }

        // 2. Try msys
        String msysRoot = queryWindowsRegistry(
                "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MSYS-1.0_is1", // NOI18N
                "Inno Setup: App Path", // NOI18N
                ".*REG_SZ(.*)"); // NOI18N

        if (msysRoot != null) {
            File sh = new File(msysRoot + "/bin/sh.exe"); // NOI18N
            if (sh.exists() && sh.canRead()) {
                msysShell = sh.getAbsolutePath();
                msysBase = sh.getParentFile().getParentFile().getAbsolutePath();
                return msysShell;
            }
        }

        // 3. Search in PATH
        String paths = System.getenv("PATH"); // NOI18N
        if (paths != null) {
            for (String path : paths.split(";")) { // NOI18N
                File sh = new File(path, "sh.exe"); // NOI18N
                if (sh.exists() && sh.canRead()) {
                    return sh.getAbsolutePath();
                }
            }
        }

        return null;
    }

    private static String queryWindowsRegistry(String key, String param, String regExpr) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "c:\\windows\\system32\\reg.exe", // NOI18N
                    "query", key, "/v", param); // NOI18N
            Process p = pb.start();
            String s;
            Pattern pattern = Pattern.compile(regExpr);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
            }

            while (true) {
                s = br.readLine();
                if (s == null) {
                    break;
                }
                Matcher m = pattern.matcher(s);
                if (m.matches()) {
                    return m.group(1).trim();
                }
            }
        } catch (IOException e) {
        }

        return null;
    }

    /**
     * For now it is assumed that once cygwin was found it is used for starting
     * shell ... So in this case will convert to /cygdrive/....
     */
    public String normalizePath(final String path) {
        if (path == null) {
            return ""; // NOI18N
        }

        if (path.length() < 2) {
            return path;
        }

        if (path.charAt(1) != ':') {
            return path.replaceAll("\\\\", "/"); // NOI18N
        }

        char driveLetter = path.charAt(0);
        String p = path.substring(2);
        String result;

        if (cygwinBase != null) {
            result = "/cygdrive/" + driveLetter + p; // NOI18N
            result = result.replaceAll("\\\\", "/"); // NOI18N
        } else if (msysBase != null) {
            result = "/" + driveLetter + p; // NOI18N
            result = result.replaceAll("\\\\", "/"); // NOI18N
        } else {
            result = path;
        }

        return result;

    }

    public String convertoToWindowsPath(String path) {
        String result = ""; // NOI18N

        if (path.startsWith("/cygdrive/")) { // NOI18N
            path = path.substring(9);
        }

        if (path.startsWith("/") && path.charAt(2) == '/') { // NOI18N
            result = path.charAt(1) + ":"; // NOI18N
            path = path.substring(2);
        }

        result = result + path;
        return result.replaceAll("/", "\\\\"); // NOI18N
    }

    public String normalizeAllPaths(String paths) {
        String[] ps = paths.split(";"); // NOI18N
        StringBuilder sb = new StringBuilder();

        for (String path : ps) {
            sb.append(normalizePath(path));
            sb.append(':');
        }

        return sb.toString();
    }
}
