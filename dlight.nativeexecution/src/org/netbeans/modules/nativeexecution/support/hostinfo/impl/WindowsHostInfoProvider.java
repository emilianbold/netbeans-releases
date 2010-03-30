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
package org.netbeans.modules.nativeexecution.support.hostinfo.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.CpuFamily;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.support.hostinfo.HostInfoProvider.class, position = 90)
public class WindowsHostInfoProvider implements HostInfoProvider {

    public HostInfo getHostInfo(ExecutionEnvironment execEnv) throws IOException {
        // Windows is supported for localhosts only.
        if (!execEnv.isLocal() || !Utilities.isWindows()) {
            return null;
        }

        HostInfoImpl info = new HostInfoImpl();
        info.initTmpDirs();
        return info;
    }

    private static class HostInfoImpl implements HostInfo {

        private final OS os;
        private final Bitness osBitness;
        private final OSFamily osFamily;
        private final String osVersion;
        private final String osName;
        private final CpuFamily cpuFamily;
        private final int cpuNum;
        private final String hostname;
        private final String shell;
        private final String path;
        private File tmpDirFile;
        private String tmpDir;

        public HostInfoImpl() {
            Map<String, String> env = WindowsSupport.getInstance().getEnv();

            // Use os.arch to detect bitness.
            // Another way is described in the following article:
            // http://blogs.msdn.com/david.wang/archive/2006/03/26/HOWTO-Detect-Process-Bitness.aspx
            osBitness = ("x86".equals(System.getProperty("os.arch"))) ? Bitness._32 : Bitness._64; // NOI18N
            osFamily = OSFamily.WINDOWS;
            osVersion = System.getProperty("os.version"); // NOI18N
            osName = System.getProperty("os.name"); // NOI18N
            cpuFamily = CpuFamily.X86;
            int _cpuNum = 1;

            try {
                _cpuNum = Integer.parseInt(env.get("NUMBER_OF_PROCESSORS")); // NOI18N
            } catch (Exception ex) {
            }

            cpuNum = _cpuNum;
            hostname = env.get("COMPUTERNAME"); // NOI18N
            shell = WindowsSupport.getInstance().getShell();
            if (shell != null) {
                path = env.get("PATH") + ';' + new File(shell).getParent(); // NOI18N
            } else {
                path = env.get("PATH"); // NOI18N
            }

            os = new OS() {

                public OSFamily getFamily() {
                    return osFamily;
                }

                public String getName() {
                    return osName;
                }

                public String getVersion() {
                    return osVersion;
                }

                public Bitness getBitness() {
                    return osBitness;
                }
            };
        }

        public void initTmpDirs() throws IOException {
            File _tmpDirFile = null;
            String _tmpDir = null;
            String ioTmpDir = System.getProperty("java.io.tmpdir"); // NOI18N
            Map<String, String> env = WindowsSupport.getInstance().getEnv();

            _tmpDirFile = new File(ioTmpDir, "dlight_" + env.get("USERNAME")); // NOI18N
            _tmpDirFile = new File(_tmpDirFile, HostInfoFactory.getNBKey());
            _tmpDir = _tmpDirFile.getAbsolutePath();

            if (shell != null) {
                _tmpDir = WindowsSupport.getInstance().convertToShellPath(_tmpDir);
            }

            // create the directory if absent (IZ#174327)
            if (!_tmpDirFile.exists() && !_tmpDirFile.mkdirs()) {
                throw new IOException("Unable to create tmpdir " + _tmpDirFile); // NOI18N
            }

            tmpDirFile = _tmpDirFile;
            tmpDir = _tmpDir;
        }

        public OS getOS() {
            return os;
        }

        public CpuFamily getCpuFamily() {
            return cpuFamily;
        }

        public int getCpuNum() {
            return cpuNum;
        }

        public OSFamily getOSFamily() {
            return osFamily;
        }

        public String getHostname() {
            return hostname;
        }

        public String getShell() {
            return shell;
        }

        public String getTempDir() {
            return tmpDir;
        }

        public File getTempDirFile() {
            return tmpDirFile;
        }

        public long getClockSkew() {
            return 0;
        }

        public String getPath() {
            return path;
        }
    }
}
