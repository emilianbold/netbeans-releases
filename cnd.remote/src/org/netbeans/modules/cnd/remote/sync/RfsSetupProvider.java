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

package org.netbeans.modules.cnd.remote.sync;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * An implementation of SetupProvider that nandles RFS related binaries
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.remote.SetupProvider.class)
public class RfsSetupProvider implements SetupProvider {

    private static final String CONTROLLER_LINUX_X86 = "rfs_controller-Linux-x86"; // NOI18N
    private static final String CONTROLLER_SUNOS_X86 = "rfs_controller-SunOS-x86"; // NOI18N
    private static final String CONTROLLER_SUNOS_SPARC = "rfs_controller-SunOS-sparc"; // NOI18N
    private static final String PRELOAD_LINUX_X86 = "rfs_preload-Linux-x86.so"; // NOI18N
    private static final String PRELOAD_SUNOS_X86 = "rfs_preload-SunOS-x86.so"; // NOI18N
    private static final String PRELOAD_SUNOS_SPARC = "rfs_preload-SunOS-sparc.so"; // NOI18N

    private Map<String, String> binarySetupMap;

    public RfsSetupProvider() {
        binarySetupMap = new HashMap<String, String>();
        binarySetupMap.put(PRELOAD_LINUX_X86, "bin/Linux-x86/rfs_preload.so"); // NOI18N
        binarySetupMap.put(PRELOAD_SUNOS_X86, "bin/SunOS-x86/rfs_preload.so"); // NOI18N
        binarySetupMap.put(PRELOAD_SUNOS_SPARC, "bin/SunOS-sparc/rfs_preload.so"); // NOI18N
        binarySetupMap.put(CONTROLLER_LINUX_X86, "bin/Linux-x86/rfs_controller"); // NOI18N
        binarySetupMap.put(CONTROLLER_SUNOS_X86, "bin/SunOS-x86/rfs_controller"); // NOI18N
        binarySetupMap.put(CONTROLLER_SUNOS_SPARC, "bin/SunOS-sparc/rfs_controller"); // NOI18N
    }

    public Map<String, String> getBinaryFiles() {
        return binarySetupMap;
    }

    public Map<String, Double> getScriptFiles() {
        return null;
    }

    public static String getPreload(ExecutionEnvironment execEnv) {
        String libDir = HostInfoProvider.getLibDir(execEnv); //NB: should contain trailing '/'
        int platform = HostInfoProvider.getPlatform(execEnv);
        switch (platform) {
            case PlatformTypes.PLATFORM_LINUX : return libDir + PRELOAD_LINUX_X86;
            case PlatformTypes.PLATFORM_SOLARIS_SPARC : return libDir + PRELOAD_SUNOS_SPARC;
            case PlatformTypes.PLATFORM_SOLARIS_INTEL : return libDir + PRELOAD_SUNOS_X86;
            default:
                RemoteUtil.LOGGER.warning("RFS binary search: unexpected platform number " + platform);
                return null;
        }
    }

    public static String getController(ExecutionEnvironment execEnv) {
        String libDir = HostInfoProvider.getLibDir(execEnv); //NB: should contain trailing '/'
        int platform = HostInfoProvider.getPlatform(execEnv);
        switch (platform) {
            case PlatformTypes.PLATFORM_LINUX : return libDir + CONTROLLER_LINUX_X86;
            case PlatformTypes.PLATFORM_SOLARIS_SPARC : return libDir + CONTROLLER_SUNOS_SPARC;
            case PlatformTypes.PLATFORM_SOLARIS_INTEL : return libDir + CONTROLLER_SUNOS_X86;
            default:
                RemoteUtil.LOGGER.warning("RFS binary search: unexpected platform number " + platform);
                return null;
        }
    }

}
