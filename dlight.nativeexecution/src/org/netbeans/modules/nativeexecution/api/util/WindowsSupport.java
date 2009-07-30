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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.util.Exceptions;

/**
 * Currently remote Windows execution is not considered...
 *
 */
public final class WindowsSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final WindowsSupport instance = new WindowsSupport();
    private ShellType type = ShellType.NO_SHELL;
    private String shell = null;
    private String bin = null;
    private Properties env = null;

    private WindowsSupport() {
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

    private static String queryWindowsRegistry(String key, String param, String regExpr) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "c:\\windows\\system32\\reg.exe", // NOI18N
                    "query", key, "/v", param); // NOI18N
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

    /**
     * Cygwin is preferrable shell (over msys). So it cygwin is
     * installed we will always use it's for shell
     */
    public String convertToShellPath(String path) {
        String result = ""; // NOI18N

        if (path == null || path.length() == 0) {
            return result;
        }

        switch (type) {
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
            // No shell ...
        }

        return result;
    }
    private final static long CygpathTimeout = 5;

    private List<String> cygpath(String opts, List<String> paths) {
        File cygpath = new File(bin, "cygpath"); // NOI18N
        List<String> cmd = new ArrayList<String>();
        cmd.add(cygpath.getAbsolutePath());
        cmd.add(opts);
        cmd.addAll(paths);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        try {
            Process p = pb.start();
            StreamReader outReader = new StreamReader(p.getInputStream());
            StreamReader errReader = new StreamReader(p.getErrorStream());

            Future<List<String>> futureOutput = NativeTaskExecutorService.submit(outReader, "Cygpath output reader"); // NOI18N
            Future<List<String>> futureError = NativeTaskExecutorService.submit(errReader, "Cygpath error reader"); // NOI18N

            int exitCode = p.waitFor();

            if (exitCode == 0) {
                try {
                    return futureOutput.get(CygpathTimeout, TimeUnit.SECONDS);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (TimeoutException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            try {
                List<String> errorOutput = futureError.get(CygpathTimeout, TimeUnit.SECONDS);
                log.fine(cygpath.getAbsolutePath() + " failed."); // NOI18N
                for (String errorLine : errorOutput) {
                    log.fine("cygpath: " + errorLine); // NOI18N
                }
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            } catch (TimeoutException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public String convertToWindowsPath(String path) {
        String result = ""; // NOI18N

        switch (type) {
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
        }

        return result;
    }

    public String convertToAllShellPaths(String paths) {
        if (paths == null) {
            return ""; // NOI18N
        }

        String[] origPaths = paths.split(";"); // NOI18N
        List<String> convertedPaths = new ArrayList<String>();

        switch (type) {
            case CYGWIN:
                // Will start cygpath only once...
                convertedPaths.addAll(cygpath("-u", Arrays.asList(origPaths))); // NOI18N
                break;
            case MSYS:
                for (String path : origPaths) {
                    convertedPaths.add(convertToShellPath(path));
                }
                break;
            default:
        }

        StringBuilder sb = new StringBuilder();

        for (String path : convertedPaths) {
            sb.append(path).append(':');
        }

        return sb.toString();
    }

    public synchronized Properties getEnv() {
        if (env == null) {
            env = readEnv();
        }

        return env;
    }

    private static Properties readEnv() {
        Properties result = new Properties();

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
                String key = line.substring(0, idx).trim().toUpperCase();
                String value = line.substring(idx + 1);
                result.setProperty(key, value);
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

    private class StreamReader implements Callable<List<String>> {

        private final InputStream is;
        private final List<String> result;

        public StreamReader(InputStream is) {
            this.is = is;
            result = new ArrayList<String>();
        }

        public List<String> call() throws Exception {
            try {
                String s;
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((s = br.readLine()) != null) {
                    result.add(s);
                }
            } catch (IOException ex) {
            }

            return result;
        }
    }

    private enum ShellType {

        NO_SHELL,
        CYGWIN,
        MSYS,
        UNKNOWN
    }
}
