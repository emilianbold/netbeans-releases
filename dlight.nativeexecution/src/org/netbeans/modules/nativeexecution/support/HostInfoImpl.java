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
package org.netbeans.modules.nativeexecution.support;

import org.netbeans.modules.nativeexecution.api.util.*;
import org.netbeans.modules.nativeexecution.api.*;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.openide.modules.InstalledFileLocator;

public class HostInfoImpl implements HostInfo {

    private static final File hostinfoScript;
    private static final java.util.logging.Logger log = Logger.getInstance();


    static {
        InstalledFileLocator fl = InstalledFileLocator.getDefault();
        hostinfoScript = fl.locate("bin/nativeexecution/hostinfo.sh", null, false); // NOI18N

        if (hostinfoScript == null) {
            log.severe("Unable to find hostinfo.sh script!"); // NOI18N
        }
    }
    private OS os;
    private CpuFamily cpuFamily;
    private String hostname;
    private String shell;
    private String tempDir;
    private int cpuNum;

    private HostInfoImpl() {
    }

    public static HostInfoImpl getHostInfo(ExecutionEnvironment execEnv) {
        HostInfoImpl hi = new HostInfoImpl();
        Properties props;

        if (hostinfoScript == null) {
            log.severe("Unable to find hostinfo.sh script!"); // NOI18N
            props = new Properties();
        } else {
            props = execEnv.isLocal()
                    ? getLocalHostInfo() : getRemoteHostInfo(execEnv);
        }

        hi.init(props);
//        props.list(System.err);
        return hi;
    }

    private static Properties getLocalHostInfo() {
        Properties hostInfo = new Properties();

        try {
            ProcessBuilder pb = new ProcessBuilder("sh", // NOI18N
                    hostinfoScript.getAbsolutePath());

            pb.environment().put("TMPBASE", // NOI18N
                    System.getProperty("java.io.tmpdir")); // NOI18N
            pb.environment().put("PATH", "/bin:/usr/bin"); // NOI18N

            Process hostinfoProcess = pb.start();

            int result = hostinfoProcess.waitFor();

            if (result == 0) {
                hostInfo.load(hostinfoProcess.getInputStream());
            } else {
                log.fine(hostinfoScript + " rc == " + result); // NOI18N
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (IOException ex) {
            log.fine("Exception while receiving HostInfo for localhost: " + ex); // NOI18N
        }

        return hostInfo;
    }

    private static Properties getRemoteHostInfo(final ExecutionEnvironment execEnv) {
        Properties hostInfo = new Properties();
        OutputStream hiOutputStream = null;
        InputStream hiInputStream = null;

        try {
            final ConnectionManager cm = ConnectionManager.getInstance();
            final Session session = ConnectionManagerAccessor.getDefault().
                    getConnectionSession(cm, execEnv, true);

            if (session != null) {
                ChannelExec echannel = null;

                synchronized (session) {
                    echannel = (ChannelExec) session.openChannel("exec"); // NOI18N
                    echannel.setEnv("PATH", "/bin:/usr/bin"); // NOI18N
                    echannel.setCommand("sh -s"); // NOI18N
                    echannel.connect();
                }

                hiOutputStream = echannel.getOutputStream();
                hiInputStream = echannel.getInputStream();

                BufferedReader scriptReader = new BufferedReader(new FileReader(hostinfoScript));
                String scriptLine = scriptReader.readLine();

                while (scriptLine != null) {
                    hiOutputStream.write((scriptLine + '\n').getBytes());
                    hiOutputStream.flush();
                    scriptLine = scriptReader.readLine();
                }

                scriptReader.close();
                hostInfo.load(hiInputStream);
            }
        } catch (IOException ex) {
            log.fine("Exception while receiving HostInfo for " + execEnv.toString() + ": " + ex); // NOI18N
        } catch (JSchException ex) {
            log.fine("Exception while receiving HostInfo for " + execEnv.toString() + ": " + ex); // NOI18N
        } finally {
            try {
                if (hiOutputStream != null) {
                    hiOutputStream.close();
                }
            } catch (IOException ex) {
            }
            try {
                if (hiInputStream != null) {
                    hiInputStream.close();
                }
            } catch (IOException ex) {
            }
        }

        return hostInfo;
    }
    private static final String UNKNOWN = "UNKNOWN"; // NOI18N

    private void init(Properties props) {
        OSImpl _os = new OSImpl();
        _os.setBitness(getInt(props, "BITNESS", 32)); // NOI18N
        _os.setFamily(props.getProperty("OSFAMILY", UNKNOWN));
        _os.setName(props.getProperty("OSNAME", UNKNOWN));
        _os.setVersion(props.getProperty("OSBUILD", UNKNOWN)); // NOI18N
        os = _os;

        hostname = props.getProperty("HOSTNAME", UNKNOWN); // NOI18N

        try {
            cpuFamily = CpuFamily.valueOf(props.getProperty("CPUFAMILY", UNKNOWN).toUpperCase()); // NOI18N
        } catch (IllegalArgumentException ex) {
        }

        shell = props.getProperty("SH", UNKNOWN); // NOI18N
        tempDir = props.getProperty("TMPDIRBASE", UNKNOWN); // NOI18N
        cpuNum = getInt(props, "CPUNUM", 1); // NOI18N
    }

    private int getInt(Properties props, String key, int defaultValue) {
        int result = defaultValue;
        String value = props.getProperty(key, null);
        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException ex) {
            }
        }

        return result;
    }

    public OS getOS() {
        return os;
    }

    public CpuFamily getCpuFamily() {
        return cpuFamily;
    }

    public OSFamily getOSFamily() {
        return os.getFamily();
    }

    public String getHostname() {
        return hostname;
    }

    public String getShell() {
        return shell;
    }

    public String getTempDir() {
        return tempDir;
    }

    public int getCpuNum() {
        return cpuNum;
    }

    private static final class OSImpl implements OS {

        private OSFamily family = OSFamily.UNKNOWN;
        private String name = UNKNOWN;
        private String version = UNKNOWN;
        private Bitness bitness = Bitness._32;

        public Bitness getBitness() {
            return bitness;
        }

        public String getVersion() {
            return version;
        }

        public OSFamily getFamily() {
            return family;
        }

        public String getName() {
            return name;
        }

        private void setVersion(String version) {
            this.version = version;
        }

        private void setBitness(int bitness) {
            this.bitness = bitness == 64 ? Bitness._64 : Bitness._32;
        }

        private void setFamily(String family) {
            try {
                this.family = OSFamily.valueOf(family.toUpperCase());
            } catch (IllegalArgumentException ex) {
            }
        }

        private void setName(String name) {
            this.name = name;
        }
    }
}
