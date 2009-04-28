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
package org.netbeans.modules.dlight.tools.impl;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * An utility class that helps working with native prof_monitor and prof_agent
 * @author Alexey Vladykin
 */
/*package*/ class NativeToolsUtil {

    private NativeToolsUtil() {
    }
    public static final String _64 = "_64"; // NOI18N

    public static String getLdPathName(ExecutionEnvironment execEnv) {
        try {
            HostInfo info = HostInfoUtils.getHostInfo(execEnv);
            return info.getOSFamily() == HostInfo.OSFamily.MACOSX
                    ? "DYLD_LIBRARY_PATH" : "LD_LIBRARY_PATH"; // NOI18N
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        return "LD_LIBRARY_PATH"; // NOI18N
    }

    public static String getLdPreloadName(ExecutionEnvironment execEnv) {
        try {
            HostInfo info = HostInfoUtils.getHostInfo(execEnv);
            return info.getOSFamily() == HostInfo.OSFamily.MACOSX
                    ? "DYLD_INSERT_LIBRARIES" : "LD_PRELOAD"; // NOI18N
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        return "LD_PRELOAD"; // NOI18N
    }

    public static Map<String, File> getCompatibleBinaries(ExecutionEnvironment execEnv, String name) {
        MacroExpander mef = MacroExpanderFactory.getExpander(execEnv);
        try {
            Map<String, File> binaries = new LinkedHashMap<String, File>();
            String dirname = mef.expandPredefinedMacros("${osname}-${platform}${_isa}"); // NOI18N
            String filename = mef.expandPredefinedMacros(name);
            File primary = locateFile(dirname, filename);
            if (primary != null) {
                binaries.put(dirname, primary);
            }
            if (dirname.endsWith(_64)) {
                dirname = dirname.substring(0, dirname.length() - _64.length());
                File secondary = locateFile(dirname, filename);
                if (secondary != null) {
                    binaries.put(dirname, secondary);
                }
            }
            return binaries;
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyMap();
        }
    }

    private static File locateFile(String dirname, String filename) {
        return InstalledFileLocator.getDefault().locate(
                "tools" + File.separator + dirname + File.separator + // NOI18N
                "bin" + File.separator + filename, null, false); // NOI18N
    }
}
