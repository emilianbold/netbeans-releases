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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.openide.util.Exceptions;

/**
 * An implementation of SetupProvider that nandles RFS related binaries
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.remote.SetupProvider.class)
public class RfsSetupProvider implements SetupProvider {

    private static final String PRELOAD_PATTERN = "%s/rfs_preload-%s.so"; // NOI18N
    private static final String CONTROLLER_PATTERN = "%s/rfs_controller-%s"; // NOI18N

    private Map<String, String> binarySetupMap;

    public RfsSetupProvider() {
        binarySetupMap = new HashMap<String, String>();
        binarySetupMap.put("rfs_preload-Linux-x86.so", "bin/Linux-x86/rfs_preload.so"); // NOI18N
        binarySetupMap.put("rfs_preload-Linux-x86_64.so", "bin/Linux-x86_64/rfs_preload.so"); // NOI18N
        binarySetupMap.put("rfs_controller-Linux-x86", "bin/Linux-x86/rfs_controller"); // NOI18N
        binarySetupMap.put("rfs_controller-Linux-x86_64", "bin/Linux-x86_64/rfs_controller"); // NOI18N
        binarySetupMap.put("rfs_preload-SunOS-x86.so", "bin/SunOS-x86/rfs_preload.so"); // NOI18N
        binarySetupMap.put("rfs_preload-SunOS-x86_64.so", "bin/SunOS-x86_64/rfs_preload.so"); // NOI18N
        binarySetupMap.put("rfs_controller-SunOS-x86", "bin/SunOS-x86/rfs_controller"); // NOI18N
        binarySetupMap.put("rfs_controller-SunOS-x86_64", "bin/SunOS-x86_64/rfs_controller"); // NOI18N
        //binarySetupMap.put("rfs_preload-SunOS-sparc.so", "bin/SunOS-sparc/rfs_preload.so"); // NOI18N
        binarySetupMap.put("rfs_preload-SunOS-sparc_64.so", "bin/SunOS-sparc_64/rfs_preload.so"); // NOI18N
        //binarySetupMap.put("rfs_controller-SunOS-sparc", "bin/SunOS-sparc/rfs_controller"); // NOI18N
        binarySetupMap.put("rfs_controller-SunOS-sparc_64", "bin/SunOS-sparc_64/rfs_controller"); // NOI18N
    }

    public Map<String, String> getBinaryFiles() {
        return binarySetupMap;
    }

    public Map<String, Double> getScriptFiles() {
        return null;
    }

    public static String getPreload(ExecutionEnvironment execEnv) {
        return getBinary(execEnv, PRELOAD_PATTERN);
    }

    public static String getController(ExecutionEnvironment execEnv) {
        return getBinary(execEnv, CONTROLLER_PATTERN);
    }

    private static String getBinary(ExecutionEnvironment execEnv, String pattern) {
        String libDir = HostInfoProvider.getLibDir(execEnv); //NB: should contain trailing '/'
        String osname = null;
        try {
            MacroExpander mef = MacroExpanderFactory.getExpander(execEnv);
            osname = mef.expandPredefinedMacros("${osname}-${platform}${_isa}"); // NOI18N
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        String result = String.format(pattern, libDir, osname);
        return result;
    }
}
