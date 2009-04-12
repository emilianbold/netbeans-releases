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
import org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author gordonp
 * @author Sergey Grinev
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.remote.HostInfoProviderFactory.class)
public class RemoteHostInfoProviderFactory implements HostInfoProviderFactory {

    public static class RemoteHostInfo extends HostInfoProvider {

        private final ExecutionEnvironment executionEnvironment;
        private String home = null;
        private PathMap mapper;
        private Map<String, String> envCache = null;
        private Boolean isCshShell;
        private Integer platform;

        @Override
        public boolean fileExists(String path) {
            RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment,
                    "test -d \"" + path + "\" -o -f \"" + path + "\""); // NOI18N
            return support.run() == 0;
        }

        @Override
        public String getLibDir() {
            String base = getHome();
            if (base == null) {
                return null;
            }
            return base + "/" + RemoteServerSetup.REMOTE_LIB_DIR; // NOI18N
        }

        private RemoteHostInfo(ExecutionEnvironment executionEnvironment) {
            this.executionEnvironment = executionEnvironment;
        }

        private String getHome() {
            if (home == null) {
                RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment, "pwd"); // NOI18N
                if (support.run() == 0) {
                    home = support.getOutput().trim();
                }
            }
            return home;
        }

        @Override
        public synchronized PathMap getMapper() {
            if (mapper == null) {
                mapper = RemotePathMap.getPathMap(executionEnvironment);
            }
            return mapper;
        }

        @Override
        public synchronized Map<String, String> getEnv() {
            if (envCache == null) {
                envCache = new HashMap<String, String>();
                RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment, "env"); // NOI18N
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

        //TODO (execution): do we still need this?
        public boolean isCshShell() {
            if (isCshShell == null) {
                //N.B.: this is only place where RemoteCommandSupport should take PATH= !!
                RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment, "PATH=/bin:/usr/bin export"); // NOI18N
//                support.setPreserveCommand(true); // to avoid endless loop
                isCshShell = Boolean.valueOf(support.run() != 0);
            }
            return isCshShell.booleanValue();
        }

        @Override
        public int getPlatform() {
            if (platform == null) {
                RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment, "uname -sm"); //NOI18N
                int result;
                if (support.run() == 0) {
                    result = recognizePlatform(support.getOutput());
                } else {
                    result = PlatformTypes.PLATFORM_GENERIC;
                }
                platform = Integer.valueOf(result);
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

    private final static Map<ExecutionEnvironment, RemoteHostInfo> env2hostinfo =
            new HashMap<ExecutionEnvironment, RemoteHostInfo>();

    public static synchronized RemoteHostInfo getHostInfo(ExecutionEnvironment execEnv) {
        RemoteHostInfo hi = env2hostinfo.get(execEnv);
        if (hi == null) {
            hi = new RemoteHostInfo(execEnv);
            env2hostinfo.put(execEnv, hi);
        }
        return hi;
    }

    public boolean canCreate(ExecutionEnvironment execEnv) {
        return execEnv.isRemote();
    }

    public HostInfoProvider create(ExecutionEnvironment execEnv) {
        return getHostInfo(execEnv);
    }
}
