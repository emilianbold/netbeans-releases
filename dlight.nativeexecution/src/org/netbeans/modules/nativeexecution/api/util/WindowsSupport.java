/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.api.util.Shell.ShellType;
import org.netbeans.modules.nativeexecution.api.util.ShellValidationSupport.ShellValidationStatus;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.windows.PathConverter;
import org.netbeans.modules.nativeexecution.support.windows.PathConverter.PathType;
import org.netbeans.modules.nativeexecution.support.windows.SimpleConverter;
import org.openide.util.Utilities;

/**
 * Currently remote Windows execution is not considered...
 *
 */
public final class WindowsSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final Object initLock = new Object();
    private static final WindowsSupport instance;
    private Shell activeShell = null;
    private String REG_EXE;
    private PathConverter pathConverter = null;
    private AtomicReference<String> pathKeyRef  = new AtomicReference<String>();
    private Charset charset;

    static {
        synchronized (initLock) {
            instance = new WindowsSupport();
            instance.init();
        }
    }

    private WindowsSupport() {
    }

    public static WindowsSupport getInstance() {
        synchronized (initLock) {
            return instance;
        }
    }

    public String getShell() {
        return activeShell == null ? null : activeShell.shell;
    }

    public void init() {
        init(null);

        if (activeShell == null) {
            log.fine("WindowsSupport: no shell found"); // NOI18N
        } else {
            log.log(Level.FINE, "WindowsSupport: found {0} shell in {1}", new Object[]{activeShell.type, activeShell.bindir.getAbsolutePath()}); // NOI18N
        }
    }

    public void init(String searchDir) {
        synchronized (initLock) {
            if (!Utilities.isWindows()) {
                return;
            }

            pathConverter = new SimpleConverter();
            activeShell = findShell(searchDir);
            initCharset();
        }
    }

    private Shell findShell(String searchDir) {
        Shell shell;
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
        if (activeShell == null) {
            return shellPID;
        }

        ProcessBuilder pb;
        File psFile = new File(activeShell.bindir, "ps.exe"); // NOI18N

        if (!psFile.exists()) {
            return shellPID;
        }

        String psCommand = psFile.getAbsolutePath();

        switch (activeShell.type) {
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
     * Cygwin is preferrable shell (over msys). So it cygwin is installed we
     * will always use it's for shell
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

        return isSinglePath
                ? pathConverter.convert(from, to, path)
                : pathConverter.convertAll(from, to, path);
    }

    /**
     * @return charset to be used when 'communicating' with a shell
     */
    public Charset getShellCharset() {
        return charset;
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

    private void initCharset() {
        charset = Charset.defaultCharset();

        if (activeShell == null || activeShell.type != ShellType.CYGWIN) {
            return;
        }

        // 1. is LANG defined?
        try {
            // Will not use NativeProcessBuilder here. This may lead to deadlock
            // if no HostInfo is available. (See bugs #202550, #202568)
            // Actually, there is no any need in using NBP here.

            ExitStatus result = ProcessUtils.execute(new ProcessBuilder(activeShell.shell, "--login", "-c", "echo $LANG")); // NOI18N

            if (result.isOK()) {
                String shellOutput = result.output;
                int dotIndex = shellOutput.indexOf('.');
                if (dotIndex >= 0) {
                    shellOutput = shellOutput.substring(dotIndex + 1).trim();
                }
                try {
                    charset = Charset.forName(shellOutput);
                    return;
                } catch (Exception ex) {
                }
            }

            String cygwinVersion = null;
            ProcessBuilder pb = new ProcessBuilder(activeShell.bindir + "\\uname.exe", "-r"); // NOI18N
            Process process = pb.start();
            process.waitFor();
            String output = ProcessUtils.readProcessOutputLine(process);
            Pattern p = Pattern.compile("^([0-9\\.]*).*"); // NOI18N
            Matcher m = p.matcher(output);
            if (m.matches()) {
                cygwinVersion = m.group(1);
            }
            if (cygwinVersion == null) {
                return;
            }

            if (cygwinVersion.startsWith("1.7")) { // NOI18N
                charset = Charset.forName("UTF-8"); // NOI18N
            }
        } catch (Exception ex) {
        }
    }

    public String getPathKey() {
        if (pathKeyRef.get() == null) {
            ProcessBuilder pb = new ProcessBuilder(""); // NOI18N
            String pathKey = "PATH"; // NOI18N
            for (String key : pb.environment().keySet()) {
                if ("PATH".equalsIgnoreCase(key)) { //NOI18N
                    pathKey = key;
                    break;
                }
            }
            pathKeyRef.compareAndSet(null, pathKey);
        }
        return pathKeyRef.get();
    }
}
