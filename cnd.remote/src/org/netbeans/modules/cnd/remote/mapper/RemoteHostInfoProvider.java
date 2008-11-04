/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.mapper;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.server.RemoteServerSetup;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;

/**
 *
 * @author gordonp
 * @author Sergey Grinev
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.remote.HostInfoProvider.class)
public class RemoteHostInfoProvider extends HostInfoProvider {

    public static class RemoteHostInfo {

        private final String hkey;
        private String home = null;
        private PathMap mapper;
        private Map<String, String> envCache = null;
        private Boolean isCshShell;
        private Integer platform;

        private RemoteHostInfo(String hkey) {
            this.hkey = hkey;
        }

        public String getHome() {
            if (home == null) {
                RemoteCommandSupport support = new RemoteCommandSupport(hkey, "pwd"); // NOI18N
                if (support.run() == 0) {
                    home = support.getOutput().trim();
                }
            }
            return home;
        }

        public synchronized PathMap getMapper() {
            if (mapper == null) {
                mapper = RemotePathMap.getMapper(hkey);
            }
            return mapper;
        }

        public synchronized Map<String, String> getEnv() {
            if (envCache == null) {
                envCache = new HashMap<String, String>();
                RemoteCommandSupport support = new RemoteCommandSupport(hkey, "env"); // NOI18N
                if (support.run() == 0) {
                    String val = support.getOutput();
                    String[] lines = val.split("\n"); // NOI18N
                    for (int i = 0; i < lines.length; i++) {
                        int pos = lines[i].indexOf('=');
                        if (pos > 0) {
                            envCache.put(lines[i].substring(0, pos), lines[i].substring(pos + 1));
                        }
                    }
                }
            }
            return envCache;
        }

        public boolean isCshShell() {
            if (isCshShell == null) {
                //N.B.: this is only place where RemoteCommandSupport should take PATH= !!
                RemoteCommandSupport support = new RemoteCommandSupport(hkey, "PATH=/bin:/usr/bin export"); // NOI18N
                support.setPreserveCommand(true); // to avoid endless loop
                isCshShell = new Boolean(support.run() != 0);
            }
            return isCshShell.booleanValue();
        }

        public int getPlatform() {
            if (platform == null) {
                RemoteCommandSupport support = new RemoteCommandSupport(hkey, "uname -sm"); //NOI18N
                int result;
                if (support.run() == 0) {
                    result = recognizePlatform(support.getOutput());
                } else {
                    result = PlatformTypes.PLATFORM_GENERIC;
                }
                platform = new Integer(result);
            }
            return platform.intValue();
        }

        private static int recognizePlatform(String platform) {
            if (platform.startsWith("Windows")) { // NOI18N
                return PlatformTypes.PLATFORM_WINDOWS;
            } else if (platform.startsWith("Linux")) { // NOI18N
                return PlatformTypes.PLATFORM_LINUX;
            } else if (platform.startsWith("SunOS")) { // NOI18N
                return platform.contains("86") ? PlatformTypes.PLATFORM_SOLARIS_INTEL : PlatformTypes.PLATFORM_SOLARIS_SPARC; // NOI18N
            } else if (platform.toLowerCase().startsWith("mac")) { // NOI18N
                return PlatformTypes.PLATFORM_MACOSX;
            } else {
                return PlatformTypes.PLATFORM_GENERIC;
            }
        }
    }
    private final static Map<String, RemoteHostInfo> hkey2hostInfo = new HashMap<String, RemoteHostInfo>();

    public static synchronized RemoteHostInfo getHostInfo(String hkey) {
        RemoteHostInfo hi = hkey2hostInfo.get(hkey);
        if (hi == null) {
            hi = new RemoteHostInfo(hkey);
            hkey2hostInfo.put(hkey, hi);
        }
        return hi;
    }

    @Override
    public PathMap getMapper(String hkey) {
        return getHostInfo(hkey).getMapper();
    }

    @Override
    public Map<String, String> getEnv(String hkey) {
        return getHostInfo(hkey).getEnv();
    }

    @Override
    public String getLibDir(String key) {
        String home = getHostInfo(key).getHome();
        if (home == null) {
            return null;
        }
        return home + "/" + RemoteServerSetup.REMOTE_LIB_DIR; // NOI18N
    }

    @Override
    public boolean fileExists(String key, String path) {
        RemoteCommandSupport support = new RemoteCommandSupport(key,
                "test -d \"" + path + "\" -o -f \"" + path + "\""); // NOI18N
        return support.run() == 0;
    }

    @Override
    public int getPlatform(String hkey) {
        return getHostInfo(hkey).getPlatform();
    }
}
