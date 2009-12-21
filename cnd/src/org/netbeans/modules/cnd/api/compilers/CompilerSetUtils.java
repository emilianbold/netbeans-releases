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

package org.netbeans.modules.cnd.api.compilers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.compilers.impl.ToolchainManagerImpl;
import org.openide.util.Utilities;

/**
 *
 * @author Alexander Simon
 */
public final class CompilerSetUtils {
    private static String cygwinBase;
    private static Map<ToolchainDescriptor, String> commandsFolders = new HashMap<ToolchainDescriptor, String>(8);

    private CompilerSetUtils() {
    }

    /**
     * Get the Cygwin base directory from Cygwin.xml (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getCygwinBase() {
        if (cygwinBase == null) {
            ToolchainManagerImpl tcm = ToolchainManager.getImpl();
            ToolchainDescriptor td = tcm.getToolchain("Cygwin", PlatformTypes.PLATFORM_WINDOWS); // NOI18N
            if (td != null) {
                String cygwinBin = tcm.getBaseFolder(td, PlatformTypes.PLATFORM_WINDOWS);
                if (cygwinBin != null) {
                    cygwinBase = cygwinBin.substring(0, cygwinBin.length() - 4).replace("\\", "/"); // NOI18N
                }
            }
            if (cygwinBase == null) {
                for (String dir : Path.getPath()) {
                    dir = dir.toLowerCase().replace("\\", "/"); // NOI18N
                    if (dir.contains("cygwin")) { // NOI18N
                        if (dir.endsWith("/")) { // NOI18N
                            dir = dir.substring(0, dir.length() - 1);
                        }
                        if (dir.toLowerCase().endsWith("/usr/bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 8);
                            break;
                        } else if (dir.toLowerCase().endsWith("/bin")) { // NOI18N
                            cygwinBase = dir.substring(0, dir.length() - 4);
                            break;
                        }
                    }
                }
            }
        }
        return cygwinBase;
    }

    /**
     * Get the command folder (toolchain definition, which users the Windows registry) or the user's path
     */
    public static String getCommandFolder(ToolchainDescriptor descriptor) {
        if (!Utilities.isWindows()) {
            return null;
        }
        String res = getCommandDir(descriptor);
        if (res != null) {
            return res;
        }
        ToolchainManagerImpl tcm = ToolchainManager.getImpl();
        for(ToolchainDescriptor td : tcm.getToolchains(PlatformTypes.PLATFORM_WINDOWS)){
            if (td != null) {
                res = getCommandDir(td);
                if (res != null) {
                    return res;
                }
            }
        }
        for (String dir : Path.getPath()) {
            dir = dir.toLowerCase().replace("\\", "/"); // NOI18N
            if (dir.contains("/msys/1.0") && dir.contains("/bin")) { // NOI18N
                return dir;
            }
        }
        return null;
    }

    private static String getCommandDir(ToolchainDescriptor td) {
        if (td != null) {
            String dir = commandsFolders.get(td);
            if (dir == null) {
                ToolchainManagerImpl tcm = ToolchainManager.getImpl();
                String msysBin = tcm.getCommandFolder(td, PlatformTypes.PLATFORM_WINDOWS);
                if (msysBin != null) {
                    dir = msysBin.replace("\\", "/"); // NOI18N
                } else {
                    dir = ""; // NOI18N
                }
                commandsFolders.put(td, dir);
            }
            if (dir.length() > 0) {
                return dir;
            }
        }
        return null;
    }

    static String getPlatformName(int platform) {
        switch (platform) {
            case PlatformTypes.PLATFORM_LINUX:
                return "linux"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_SPARC:
                return "sun_sparc"; // NOI18N
            case PlatformTypes.PLATFORM_SOLARIS_INTEL:
                return "sun_intel"; // NOI18N
            case PlatformTypes.PLATFORM_WINDOWS:
                return "windows"; // NOI18N
            case PlatformTypes.PLATFORM_MACOSX:
                return "mac"; // NOI18N
            default:
                return "none"; // NOI18N
        }
    }

    public static int computeLocalPlatform() {
        String os = System.getProperty("os.name"); // NOI18N

        if (os.equals("SunOS")) { // NOI18N
            return System.getProperty("os.arch").equals("x86") ? PlatformTypes.PLATFORM_SOLARIS_INTEL : PlatformTypes.PLATFORM_SOLARIS_SPARC; // NOI18N
        } else if (os.startsWith("Windows ")) { // NOI18N
            return PlatformTypes.PLATFORM_WINDOWS;
        } else if (os.toLowerCase().contains("linux")) { // NOI18N
            return PlatformTypes.PLATFORM_LINUX;
        } else if (os.toLowerCase().contains("mac") || os.startsWith("Darwin")) { // NOI18N
            return PlatformTypes.PLATFORM_MACOSX;
        } else {
            return PlatformTypes.PLATFORM_GENERIC;
        }
    }

    static String findCommand(String name) {
        String path = Path.findCommand(name);
        if (path == null) {
            String dir = CompilerSetUtils.getCommandFolder(null);
            if (dir != null) {
                path = findCommand(name, dir); // NOI18N
            }
        }
        return path;
    }

    static String findCommand(String cmd, String dir) {
        File file;
        String cmd2 = null;
        if (cmd.length() > 0) {
            if (Utilities.isWindows() && !cmd.endsWith(".exe")) { // NOI18N
                cmd2 = cmd + ".exe"; // NOI18N
            }

            file = new File(dir, cmd);
            if (file.exists()) {
                return file.getAbsolutePath();
            } else {
                if (Utilities.isWindows() && cmd.endsWith(".exe")){// NOI18N
                    File file2 = new File(dir, cmd+".lnk");// NOI18N
                    if (file2.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
            if (cmd2 != null) {
                file = new File(dir, cmd2);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
                File file2 = new File(dir, cmd2+".lnk");// NOI18N
                if (file2.exists()) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }
}
