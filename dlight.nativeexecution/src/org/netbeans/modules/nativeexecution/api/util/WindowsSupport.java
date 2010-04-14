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
import java.io.Serializable;
import java.nio.charset.Charset;
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
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport.ShellValidationStatus;
import org.netbeans.modules.nativeexecution.support.Computable;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.api.util.Shell.PathType;
import org.netbeans.modules.nativeexecution.api.util.Shell.ShellType;
import org.netbeans.modules.nativeexecution.support.TasksCachedProcessor;
import org.openide.util.Exceptions;
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
    private static final String cmd;
    private Shell activeShell = null;
    private Map<String, String> env = null;
    private String REG_EXE;

    static {
        String os = System.getProperty("os.name").toLowerCase(); // NOI18N
        cmd = os.contains("windows 9") ? "command.com" : "cmd.exe"; // NOI18N
    }

    private WindowsSupport() {
        isWindows = Utilities.isWindows();

        init();

        if (activeShell == null) {
            log.fine("WindowsSupport: no shell found"); // NOI18N
        } else {
            log.log(Level.FINE, "WindowsSupport: found {0} shell in {1}", new Object[]{activeShell.type, activeShell.bindir.getAbsolutePath()}); // NOI18N
        }
    }

    public static WindowsSupport getInstance() {
        return instance;
    }

    public String getShell() {
        return activeShell == null ? null : activeShell.shell;
    }

    public synchronized void init() {
        init(null);
    }

    public void init(String searchDir) {
        if (!isWindows) {
            return;
        }

        converter.resetCache();
        activeShell = findShell(searchDir);
    }

    private Shell findShell(String searchDir) {
        Shell shell = null;
        Shell candidate = null;

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

        String[][] cygwinRegKeys = new String[][]{
            new String[]{"HKLM\\SOFTWARE\\cygwin\\setup\\", "rootdir", ".*rootdir.*REG_SZ(.*)"}, // NOI18N
            new String[]{"HKLM\\SOFTWARE\\Wow6432Node\\cygwin\\setup\\", "rootdir", ".*rootdir.*REG_SZ(.*)"}, // NOI18N
            new String[]{"HKLM\\SOFTWARE\\Cygnus Solutions\\Cygwin\\mounts v2\\/", "native", ".*native.*REG_SZ(.*)"}, // NOI18N
            new String[]{"HKLM\\SOFTWARE\\Wow6432Node\\Cygnus Solutions\\Cygwin\\mounts v2\\/", "native", ".*native.*REG_SZ(.*)"}, // NOI18N
        };

        for (String[] regKey : cygwinRegKeys) {
            shell = initShell(ShellType.CYGWIN, queryWindowsRegistry(
                    regKey[0], regKey[1], regKey[2]));

            // If found cygwin in registry - it is assumed to be valid -
            // just choose one
            if (shell != null) {
                return shell;
            }
        }

        // No cygwin in the registry...
        // try msys

        String[] msysRegKeys = new String[]{
            "HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MSYS-1.0_is1", // NOI18N
            "HKLM\\SOFTWARE\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MSYS-1.0_is1", // NOI18N
        };

        for (String regKey : msysRegKeys) {
            shell = initShell(ShellType.MSYS, queryWindowsRegistry(
                    regKey,
                    "Inno Setup: App Path", // NOI18N
                    ".*REG_SZ(.*)")); // NOI18N

            if (shell != null) {
                // Again, if found one - use it
                return shell;
            }
        }

        // Registry search failed ;(
        // Try to find something in PATH
        String paths = System.getenv("PATH"); // NOI18N

        if (searchDir != null && searchDir.length() > 0) {
            paths = searchDir + ';' + paths;
        }

        if (paths != null) {
            for (String path : paths.split(";")) { // NOI18N
                File sh = new File(path, "sh.exe"); // NOI18N
                File parent = sh.getParentFile();

                if (sh.exists() && sh.canRead()) {
                    if ("bin".equals(parent.getName())) { // NOI18N
                        // Looks like we have found something...
                        // An attempt to understand what exactly we have found
                        if (new File(parent, "cygcheck.exe").exists()) { // NOI18N
                            // Well ...
                            // The problem is that is is not in registry...
                            // I.e. we will use it if on msys found on the system...
                            if (candidate == null) {
                                candidate = new Shell(ShellType.CYGWIN, sh.getAbsolutePath(), parent);
                            }
                            // Still there is a chance that this installation is
                            // OK (even if it is not in the registry).
                            // This could be the case if it is in [?]:/cygwin
                            ShellValidationStatus validationStatus = ShellValidationSupport.getValidationStatus(candidate);

                            if (validationStatus.isValid() && !validationStatus.hasWarnings()) {
                                return candidate;
                            }
                        } else if (new File(parent, "msysinfo").exists()) { // NOI18N
                            // Looks like this one is msys...
                            // As no valid cygwin was found - use it
                            return new Shell(ShellType.MSYS, sh.getAbsolutePath(), parent);
                        }
                    }
                }
            }
        }

        // if we found some "broken" cygwin - it will be in candidate...
        // or it will be null if nothing found
        return candidate;
    }

    public int getWinPID(int shellPID) {
        ProcessBuilder pb = null;
        File psFile = new File(getActiveShell().bindir, "ps.exe"); // NOI18N

        if (!psFile.exists()) {
            return shellPID;
        }

        String psCommand = psFile.getAbsolutePath();

        switch (getActiveShell().type) {
            case CYGWIN:
                pb = new ProcessBuilder(psCommand, "-W", "-p", Integer.toString(shellPID)); // NOI18N
                break;
            case MSYS:
                pb = new ProcessBuilder(psCommand, "-W"); // NOI18N
                break;
            default:
                return shellPID;
        }

        try {
            Process p = pb.start();
            List<String> output = ProcessUtils.readProcessOutput(p);
            Pattern pat = Pattern.compile("[I]*[\t ]*([0-9]+)[\t ]*([0-9]+)[\t ]*([0-9]+)[\t ]*([0-9]+).*"); // NOI18N
            for (String s : output) {
                Matcher m = pat.matcher(s);
                if (m.matches()) {
                    Integer pid = Integer.parseInt(m.group(1));
                    if (pid == shellPID) {
                        return Integer.parseInt(m.group(4));
                    }
                }
            }
        } catch (IOException ex) {
            return shellPID;
        }

        return shellPID;
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
        return convert(PathType.WINDOWS, PathType.CYGWIN, winPath, true);
    }

    public String convertFromCygwinPath(String cygwinPath) {
        return convert(PathType.CYGWIN, PathType.WINDOWS, cygwinPath, true);
    }

    public String convertToMSysPath(String winPath) {
        return convert(PathType.WINDOWS, PathType.MSYS, winPath, true);
    }

    public String convertFromMSysPath(String msysPath) {
        return convert(PathType.MSYS, PathType.WINDOWS, msysPath, true);
    }

    /**
     * Cygwin is preferrable shell (over msys). So it cygwin is
     * installed we will always use it's for shell
     */
    public String convertToShellPath(String path) {
        return activeShell == null ? null : convert(PathType.WINDOWS, activeShell.type.toPathType(), path, true);
    }

    public String convertToWindowsPath(String path) {
        return activeShell == null ? null : convert(activeShell.type.toPathType(), PathType.WINDOWS, path, true);
    }

    public String convertToAllShellPaths(String paths) {
        return activeShell == null ? null : convert(PathType.WINDOWS, activeShell.type.toPathType(), paths, false);
    }

    private String convert(PathType from, PathType to, String path, boolean isSinglePath) {
        if (to == null || from == null) {
            return null;
        }

        if (from == PathType.CYGWIN || to == PathType.CYGWIN) {
            if (activeShell == null || activeShell.type != ShellType.CYGWIN) {
                // This means we don't know how to correctly deal with cygwin paths
                return null;
            }
        }

        try {
            return converter.compute(new PathConverterParams(from, to, path, isSinglePath));
        } catch (InterruptedException ex) {
        }

        return null;
    }

    public synchronized Map<String, String> getEnv() {
        if (env == null && isWindows) {
            env = Collections.unmodifiableMap(readEnv());
        }

        return env;
    }

    /**
     * @return charset to be used when 'communicating' with a shell
     */
    public Charset getShellCharset() {
        return Charset.defaultCharset();
    }

    private static Map<String, String> readEnv() {
        Map<String, String> result = new TreeMap<String, String>(new CaseInsensitiveComparator());

        try {
            String codepage = getCodePage();

            ProcessBuilder pb = new ProcessBuilder(cmd, "/C", "set"); // NOI18N
            Process p = pb.start();

            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), codepage));

            while ((line = br.readLine()) != null) {
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

    private Shell initShell(ShellType type, String root) {
        if (root == null) {
            return null;
        }

        File sh = new File(root + "/bin/sh.exe"); // NOI18N

        if (!sh.exists() || !sh.canRead()) {
            return null;
        }

        return new Shell(type, sh.getAbsolutePath(), sh.getParentFile().getAbsoluteFile());
    }

    public Shell getActiveShell() {
        return activeShell;
    }

    private static class CaseInsensitiveComparator implements Comparator<String>, Serializable {

        CaseInsensitiveComparator() {
        }

        @Override
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

    private static final class PathConverterParams {

        private final PathType srcType;
        private final PathType trgType;
        private final String path;
        private final boolean isSinglePath;

        PathConverterParams(PathType srcType, PathType trgType, String path, boolean isSinglePath) {
            this.srcType = srcType;
            this.trgType = trgType;
            this.path = path;
            this.isSinglePath = isSinglePath;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof PathConverterParams)) {
                return false;
            }

            PathConverterParams that = (PathConverterParams) obj;

            return this.srcType == that.srcType
                    && this.trgType == that.trgType
                    && this.isSinglePath == that.isSinglePath
                    && ((this.path == null) ? (that.path == null) : this.path.equals(that.path));
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

        @Override
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
                        return (paths == null) ? null : paths.get(0);
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
                        return (paths == null) ? null : paths.get(0);
                    case MSYS:
                        result = path;

                        if (result.length() > 2 && result.charAt(1) == ':') {
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
                        List<String> res = cygpath("-u", Arrays.asList(origPaths)); // NOI18N

                        if (res == null) {
                            return null;
                        }

                        convertedPaths.addAll(res);
                        break;
                    case MSYS:
                        for (String path : origPaths) {
                            if (path.trim().length() > 0) {
                                String p = convertSingle(new PathConverterParams(PathType.WINDOWS, PathType.MSYS, path.trim(), true));
                                if (p != null) {
                                    convertedPaths.add(p);
                                }
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
            if (activeShell == null) {
                return null;
            }

            final File cygpath = new File(activeShell.bindir, "cygpath.exe"); // NOI18N

            if (!cygpath.exists()) {
                return null;
            }

            List<String> cmd = new ArrayList<String>();
            cmd.add(cygpath.getAbsolutePath());
            cmd.add(opts);
            cmd.addAll(paths);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            List<String> result = new ArrayList<String>();

            try {
                Process p = pb.start();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), getShellCharset()));
                while ((line = br.readLine()) != null) {
                    result.add(line);
                }
                int exitCode = p.waitFor();

                if (exitCode == 0) {
                    return result;
                }

                log.log(Level.FINE, "{0} failed.", cygpath.getAbsolutePath()); // NOI18N
                ProcessUtils.logError(Level.FINE, log, p);
            } catch (InterruptedException ex) {
            } catch (IOException ex) {
            }
            return null;
        }
    }

    private static String getCodePage() {
        try {
            ProcessBuilder pb = new ProcessBuilder(Arrays.asList(cmd, "/C", "chcp")); // NOI18N
            Process p = pb.start();
            p.waitFor();
            String out = ProcessUtils.readProcessOutputLine(p);
            Pattern pattern = Pattern.compile(".*: ([0-9]+)"); // NOI18N
            Matcher m = pattern.matcher(out);
            if (m.matches()) {
                return "CP" + m.group(1); // NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return "CP866"; // NOI18N
    }
}
