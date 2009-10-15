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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.TasksCachedProcessor;
import org.openide.util.Utilities;

/**
 * Currently remote Windows execution is not considered...
 *
 */
public final class WindowsSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final WindowsSupport instance = new WindowsSupport();
    private final TasksCachedProcessor<PathConverterParams, String> converter =
            new TasksCachedProcessor<PathConverterParams, String>(new PathConverter(), false);
    private final boolean isWindows;
    private ShellType type = ShellType.NO_SHELL;
    private String shell = null;
    private String bin = null;
    private Map<String, String> env = null;
    private String REG_EXE;

    private WindowsSupport() {
        isWindows = Utilities.isWindows();

        if (!isWindows) {
            return;
        }

        init();

        if (type == ShellType.NO_SHELL) {
            log.fine("WindowsSupport: no shell found"); // NOI18N
        } else {
            log.fine("WindowsSupport: found " + type + " shell in " + bin); // NOI18N
        }
    }

    public static WindowsSupport getInstance() {
        return instance;
    }

    public String getShell() {
        return shell;
    }

    public void init() {
        String reg_exe = "reg.exe"; // NOI18N

        try {
            String windir = System.getenv("WINDIR"); // NOI18N
            File sys32 = new File(windir, "System32"); // NOI18N
            reg_exe = new File(sys32, "reg.exe").getPath(); // NOI18N
        } catch (Throwable th) {
            System.out.println(th);
        }

        REG_EXE = reg_exe;

        // 1. Try to find cygwin ...
        String cygwinRoot = queryWindowsRegistry(
                "HKLM\\SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\/", // NOI18N
                "native", // NOI18N
                ".*native.*REG_SZ(.*)"); // NOI18N

        if (cygwinRoot == null) {
            cygwinRoot = queryWindowsRegistry(
                    "HKLM\\SOFTWARE\\cygwin\\setup\\", // NOI18N
                    "rootdir", // NOI18N
                    ".*rootdir.*REG_SZ(.*)"); // NOI18N
        }

        if (cygwinRoot == null) {
            cygwinRoot = queryWindowsRegistry(
                    "HKLM\\SOFTWARE\\Wow6432Node\\Cygnus Solutions\\Cygwin\\mounts v2\\/", // NOI18N
                    "native", // NOI18N
                    ".*native.*REG_SZ(.*)"); // NOI18N
        }

        if (cygwinRoot != null) {
            File sh = new File(cygwinRoot + "/bin/sh.exe"); // NOI18N
            if (sh.exists() && sh.canRead()) {
                type = ShellType.CYGWIN;
                shell = sh.getAbsolutePath();
                bin = sh.getParentFile().getAbsolutePath();
                return;
            }
        }

        // 2. Try msys
        String msysRoot = queryWindowsRegistry(
                "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MSYS-1.0_is1", // NOI18N
                "Inno Setup: App Path", // NOI18N
                ".*REG_SZ(.*)"); // NOI18N

        if (msysRoot == null) {
            msysRoot = queryWindowsRegistry(
                    "HKLM\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MSYS-1.0_is1", // NOI18N
                    "Inno Setup: App Path", // NOI18N
                    ".*REG_SZ(.*)"); // NOI18N
        }

        if (msysRoot != null) {
            File sh = new File(msysRoot + "/bin/sh.exe"); // NOI18N
            if (sh.exists() && sh.canRead()) {
                type = ShellType.MSYS;
                shell = sh.getAbsolutePath();
                bin = sh.getParentFile().getAbsolutePath();
                return;
            }
        }

        // 3. Search in PATH
        String paths = System.getenv("PATH"); // NOI18N
        if (paths != null) {
            String foundBin = null;
            String foundShell = null;
            ShellType foundType = ShellType.NO_SHELL;

            for (String path : paths.split(";")) { // NOI18N
                File sh = new File(path, "sh.exe"); // NOI18N
                File parent = sh.getParentFile();

                if (sh.exists() && sh.canRead()) {
                    if ("bin".equals(parent.getName())) { // NOI18N
                        // Looks like we have found something...
                        // An attempt to understand what exactly we have found
                        if (new File(parent, "cygcheck.exe").exists()) { // NOI18N
                            // Cygwin found! No need to continue further search
                            bin = parent.getAbsolutePath();
                            shell = sh.getAbsolutePath();
                            type = ShellType.CYGWIN;
                            return;
                        } else {
                            // If we found msys already - we are not intresting in this entry
                            // (which is either UNKNOWN shell or MSYS)
                            // (is it possible to have several msys installations? If yes - then this will
                            //  a guarantee that we are using the one that is the first in the PATH)
                            if (foundType == ShellType.MSYS) {
                                continue;
                            }

                            // We never found msys before... Perhaps this one is msys?
                            if (new File(parent, "msysinfo").exists()) { // NOI18N
                                // Looks like it is...
                                // Still will continue search - perhaps next one will be a preferred Cygwin.
                                foundType = ShellType.MSYS;
                            } else {
                                // This is some unknown shell ...
                                foundType = ShellType.UNKNOWN;
                            }

                            foundBin = parent.getAbsolutePath();
                            foundShell = sh.getAbsolutePath();
                        }
                    }
                }
            }

            bin = foundBin;
            shell = foundShell;
            type = foundType;
        }
    }

    private String queryWindowsRegistry(String key, String param, String regExpr) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    REG_EXE, "query", key, "/v", param); // NOI18N
            Process p = pb.start();
            Pattern pattern = Pattern.compile(regExpr);

            try {
                p.waitFor();
            } catch (InterruptedException ex) {
            }

            List<String> out = ProcessUtils.readProcessOutput(p);

            for (String s : out) {
                Matcher m = pattern.matcher(s);
                if (m.matches()) {
                    return m.group(1).trim();
                }
            }
        } catch (IOException e) {
        }

        // If not found, try to search for the entry in user's space ...
        if (key.toLowerCase().startsWith("hklm")) { // NOI18N
            return queryWindowsRegistry("HKCU" + key.substring(4), param, regExpr); // NOI18N
        }

        return null;
    }

    public String convertToCygwinPath(String winPath) {
        try {
            return converter.compute(new PathConverterParams(PathType.WINDOWS, PathType.CYGWIN, winPath, true));
        } catch (InterruptedException ex) {
        }

        return winPath;
    }

    public String convertFromCygwinPath(String cygwinPath) {
        try {
            return converter.compute(new PathConverterParams(PathType.CYGWIN, PathType.WINDOWS, cygwinPath, true));
        } catch (InterruptedException ex) {
        }

        return cygwinPath;
    }

    public String convertToMSysPath(String winPath) {
        try {
            return converter.compute(new PathConverterParams(PathType.WINDOWS, PathType.MSYS, winPath, true));
        } catch (InterruptedException ex) {
        }

        return winPath;
    }

    public String convertFromMSysPath(String msysPath) {
        try {
            return converter.compute(new PathConverterParams(PathType.MSYS, PathType.WINDOWS, msysPath, true));
        } catch (InterruptedException ex) {
        }

        return msysPath;
    }

    /**
     * Cygwin is preferrable shell (over msys). So it cygwin is
     * installed we will always use it's for shell
     */
    public String convertToShellPath(String path) {
        try {
            return converter.compute(new PathConverterParams(
                    PathType.WINDOWS,
                    type == ShellType.MSYS ? PathType.MSYS : PathType.CYGWIN,
                    path, true));
        } catch (InterruptedException ex) {
        }

        return path;
    }

    public String convertToWindowsPath(String path) {
        try {
            return converter.compute(new PathConverterParams(
                    type == ShellType.MSYS ? PathType.MSYS : PathType.CYGWIN,
                    PathType.WINDOWS,
                    path, true));
        } catch (InterruptedException ex) {
        }

        return path;
    }

    public String convertToAllShellPaths(String paths) {
        try {
            return converter.compute(new PathConverterParams(
                    PathType.WINDOWS,
                    type == ShellType.MSYS ? PathType.MSYS : PathType.CYGWIN,
                    paths, false));
        } catch (InterruptedException ex) {
        }

        return paths;
    }

    public synchronized Map<String, String> getEnv() {
        if (env == null && isWindows) {
            env = Collections.unmodifiableMap(readEnv());
        }

        return env;
    }

    private static Map<String, String> readEnv() {
        Map<String, String> result = new TreeMap<String, String>(new CaseInsensitiveComparator());

        try {
            String os = System.getProperty("os.name").toLowerCase(); // NOI18N
            String cmd = "cmd"; // NOI18N

            if (os.contains("windows 9")) { // NOI18N Win95, Win98.. not supported... but, still...
                cmd = "command.com"; // NOI18N
            }

            ProcessBuilder pb = new ProcessBuilder(cmd, "/c", "set"); // NOI18N
            Process p = pb.start();

            List<String> out = ProcessUtils.readProcessOutput(p);

            for (String line : out) {
                int idx = line.indexOf('=');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1);
                result.put(key, value);
            }

            int exitStatus = -1;

            try {
                exitStatus = p.waitFor();
            } catch (InterruptedException ex) {
            }

            if (exitStatus != 0) {
                log.log(Level.FINE, "Unable to read environment"); // NOI18N
                ProcessUtils.logError(Level.FINE, log, p);
            }
        } catch (IOException ex) {
            log.log(Level.FINE, "Unable to read environment", ex); // NOI18N
        }

        return result;
    }

    private enum ShellType {

        NO_SHELL,
        CYGWIN,
        MSYS,
        UNKNOWN
    }

    private static class CaseInsensitiveComparator implements Comparator<String>, Serializable {

        public CaseInsensitiveComparator() {
        }

        public int compare(String s1, String s2) {
            if (s1 == null && s2 == null) {
                return 0;
            }

            if (s1 == null) {
                return 1;
            }

            if (s2 == null) {
                return -1;
            }

            return s1.toUpperCase().compareTo(s2.toUpperCase());
        }
    }

    private enum PathType {

        CYGWIN,
        MSYS,
        WINDOWS
    }

    private static final class PathConverterParams {

        private final PathType srcType;
        private final PathType trgType;
        private final String path;
        private final boolean isSinglePath;

        public PathConverterParams(PathType srcType, PathType trgType, String path, boolean isSinglePath) {
            this.srcType = srcType;
            this.trgType = trgType;
            this.path = path;
            this.isSinglePath = isSinglePath;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PathConverterParams)) {
                return false;
            }

            PathConverterParams that = (PathConverterParams) obj;

            return this.srcType == that.srcType &&
                    this.trgType == that.trgType &&
                    this.isSinglePath == that.isSinglePath &&
                    this.path.equals(that.path);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + this.srcType.hashCode();
            hash = 43 * hash + this.trgType.hashCode();
            hash = 43 * hash + (this.path != null ? this.path.hashCode() : 0);
            hash = 43 * hash + (this.isSinglePath ? 1 : 0);
            return hash;
        }
    }

    private final class PathConverter implements Computable<PathConverterParams, String> {

        public String compute(final PathConverterParams taskArguments) throws InterruptedException {
            String path = taskArguments.path;

            if (path == null || path.length() == 0 || !isWindows) {
                return path;
            }

            if (taskArguments.isSinglePath) {
                return convertSingle(taskArguments);
            } else {
                return convertMulty(taskArguments);
            }
        }

        private String convertSingle(final PathConverterParams taskArguments) {
            String result = ""; // NOI18N
            String path = taskArguments.path;

            if (taskArguments.trgType == PathType.WINDOWS) {
                switch (taskArguments.srcType) {
                    case CYGWIN:
                        List<String> paths = cygpath("-w", Arrays.asList(path)); // NOI18N

                        if (paths != null) {
                            result = paths.get(0);
                        }
                        break;
                    case MSYS:
                        if (path.startsWith("/") && path.charAt(2) == '/') { // NOI18N
                            result = path.charAt(1) + ":"; // NOI18N
                            path = path.substring(2);
                        }

                        result = (result + path).replaceAll("/", "\\\\"); // NOI18N
                        break;
                    default:
                        result = path;
                }
            } else {
                switch (taskArguments.trgType) {
                    case CYGWIN:
                        List<String> paths = cygpath("-u", Arrays.asList(path)); // NOI18N

                        if (paths != null) {
                            result = paths.get(0);
                        }

                        break;
                    case MSYS:
                        result = path;

                        if (result.charAt(1) == ':') {
                            result = "/" + result.replaceFirst(":", ""); // NOI18N
                        }

                        result = result.replaceAll("\\\\", "/"); // NOI18N
                        break;
                    default:
                        result = path;
                }
            }

            return result;
        }

        private String convertMulty(final PathConverterParams taskArguments) {
            String result = ""; // NOI18N
            String paths = taskArguments.path;

            if (taskArguments.srcType == PathType.WINDOWS) {
                String[] origPaths = paths.split(";"); // NOI18N
                List<String> convertedPaths = new ArrayList<String>();

                switch (taskArguments.trgType) {
                    case CYGWIN:
                        // Will start cygpath only once...
                        convertedPaths.addAll(cygpath("-u", Arrays.asList(origPaths))); // NOI18N
                        break;
                    case MSYS:
                        for (String path : origPaths) {
                            if (path.trim().length() > 0) {
                                convertedPaths.add(convertSingle(new PathConverterParams(PathType.WINDOWS, PathType.MSYS, path.trim(), true)));
                            }
                        }
                        break;
                    default:
                }

                StringBuilder sb = new StringBuilder();

                for (String path : convertedPaths) {
                    sb.append(path).append(':');
                }

                result = sb.toString();
            } else {
                // NOT USED NOW
                result = paths;
            }

            return result;
        }

        private List<String> cygpath(String opts, List<String> paths) {
            File cygpath = new File(bin, "cygpath"); // NOI18N
            List<String> cmd = new ArrayList<String>();
            cmd.add(cygpath.getAbsolutePath());
            cmd.add(opts);
            cmd.addAll(paths);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            try {
                Process p = pb.start();
                List<String> result = ProcessUtils.readProcessOutput(p);
                int exitCode = p.waitFor();

                if (exitCode == 0) {
                    return result;
                }

                log.fine(cygpath.getAbsolutePath() + " failed."); // NOI18N
                ProcessUtils.logError(Level.FINE, log, p);
            } catch (InterruptedException ex) {
            } catch (IOException ex) {
            }
            return null;
        }
    }
}
