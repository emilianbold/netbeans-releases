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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.Bitness;
import org.netbeans.modules.nativeexecution.api.HostInfo.CpuFamily;
import org.netbeans.modules.nativeexecution.api.HostInfo.OS;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;

public final class HostInfoFactory {

    private static final String UNKNOWN = "UNKNOWN"; // NOI18N

    private HostInfoFactory() {
    }

    static protected HostInfo newHostInfo(Properties initData) {
        HostInfoImpl info = new HostInfoImpl();

        OSImpl _os = new OSImpl();
        _os.setBitness(getInt(initData, "BITNESS", 32)); // NOI18N
        _os.setFamily(initData.getProperty("OSFAMILY", UNKNOWN));
        _os.setName(initData.getProperty("OSNAME", UNKNOWN));
        _os.setVersion(initData.getProperty("OSBUILD", UNKNOWN)); // NOI18N
        info.os = _os;

        info.hostname = initData.getProperty("HOSTNAME", UNKNOWN); // NOI18N

        try {
            info.cpuFamily = CpuFamily.valueOf(initData.getProperty("CPUFAMILY", UNKNOWN).toUpperCase()); // NOI18N
        } catch (IllegalArgumentException ex) {
            info.cpuFamily = CpuFamily.UNKNOWN;
        }

        info.shell = initData.getProperty("SH", UNKNOWN); // NOI18N
        info.tempDir = initData.getProperty("TMPDIRBASE", UNKNOWN); // NOI18N
        info.cpuNum = getInt(initData, "CPUNUM", 1); // NOI18N

        if (initData.containsKey("LOCALTIME")) { // NOI18N
            long localTime = (Long)initData.get("LOCALTIME"); // NOI18N
            long remoteTime = getTime(initData, "DATETIME", localTime); // NOI18N
            info.clockSkew = remoteTime - localTime;
        }

        return info;
    }

    private static int getInt(Properties props, String key, int defaultValue) {
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

    private static long getTime(Properties props, String key, long defaultValue) {
        long result = defaultValue;
        String value = props.getProperty(key, null);
        if (value != null) {
            try {
                DateFormat df = new SimpleDateFormat("y-M-d H:m:s"); // NOI18N
                df.setTimeZone(TimeZone.getTimeZone("GMT")); // NOI18N
                Date date = df.parse(value);
                result = date.getTime();
            } catch (ParseException ex) {
            }
        }
        return result;
    }

    static private class HostInfoImpl implements HostInfo {

        private OS os;
        private CpuFamily cpuFamily;
        private String hostname;
        private String shell;
        private String tempDir;
        private int cpuNum;
        private long clockSkew;

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

        public File getTempDirFile() {
            if (getOSFamily() == OSFamily.WINDOWS) {
                return new File(WindowsSupport.getInstance().convertToWindowsPath(tempDir));
            } else {
                return new File(tempDir);
            }
        }

        public long getClockSkew() {
            return clockSkew;
        }
    }

    static final class OSImpl implements OS {

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
