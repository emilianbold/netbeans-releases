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


/**
 * An implementation of SetupProvider that nandles RFS related binaries
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.remote.SetupProvider.class)
public class RfsSetupProvider implements SetupProvider {

    private Map<String, String> binarySetupMap;
    private static final String CONTROLLER = "rfs_controller"; // NOI18N
    private static final String PRELOAD = "rfs_preload.so"; // NOI18N

    public RfsSetupProvider() {
        String[] dirs = new String[] {
             "SunOS-x86" // NOI18N
            ,"SunOS-x86_64" // NOI18N
            ,"Linux-x86" // NOI18N
            ,"Linux-x86_64" // NOI18N
            ,"SunOS-sparc" // NOI18N
            ,"SunOS-sparc_64" // NOI18N
        };
        binarySetupMap = new HashMap<String, String>();
        for (String dir : dirs) {
            binarySetupMap.put(dir + "/" + PRELOAD, "bin/" +  dir + "/" + PRELOAD); // NOI18N
            binarySetupMap.put(dir + "/" + CONTROLLER, "bin/" +  dir + "/" + CONTROLLER); // NOI18N
        }
    }

    public Map<String, String> getBinaryFiles() {
        return binarySetupMap;
    }

    public Map<String, Double> getScriptFiles() {
        return null;
    }

    public static String getPreloadName(ExecutionEnvironment execEnv) {
        return PRELOAD;
    }

    /** Never returns null, throws instead */
    public static String getControllerPath(ExecutionEnvironment execEnv) throws ParseException {
        return getLibDir(execEnv) + '/' + CONTROLLER; // NOI18N
    }

    public static String getLdLibraryPath(ExecutionEnvironment execEnv) throws ParseException {
        String libDir = getLibDir(execEnv);
        return libDir + ':' + libDir + "_64"; // NOI18N
    }

    private static String getLibDir(ExecutionEnvironment execEnv) throws ParseException {
        String libDir = HostInfoProvider.getLibDir(execEnv); //NB: should contain trailing '/'
        MacroExpander mef = MacroExpanderFactory.getExpander(execEnv);
        String osname = mef.expandPredefinedMacros("${osname}-${platform}"); // NOI18N
        return libDir + '/' + osname;
    }
}
