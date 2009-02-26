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

package org.netbeans.modules.dlight.memory;

import java.io.File;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * An utility class that helps working with native mmonitor and magent
 * @author Vladimir Kvashin
 */
// package-local
class MemoryMonitorUtil {

    private static String getExecutableSuffix() {
        return Utilities.isWindows() ? ".exe" : ""; //NOI18N
    }

    private static String getSharedLibrarySuffix() {
        if (Utilities.isWindows()) {
            return ".dll";  //NOI18N
        } else if(Utilities.isMac()) {
            return ".dylib";    //NOI18N
        } else if (Utilities.isUnix()) {
            return ".so";   //NOI18N
        } else {
            DLightLogger.instance.warning("unknown platform"); //NOI18N
            return "";
        }
    }

    private static String getPlatformPath() {

        String result = null;

        String arch = System.getProperty("os.arch"); //NOI18N
        
        if (Utilities.isWindows()) {
            result = "Windows-x86"; //NOI18N
        } else if (Utilities.getOperatingSystem() == Utilities.OS_LINUX) {
            result = "Linux-x86"; //NOI18N
        } else if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            if (arch.indexOf("86") >= 0) { //NOI18N
                result = "SunOS-x86"; //NOI18N
            } else {
                result = "SunOS-sparc"; //NOI18N
            }
        } else if (Utilities.isMac()) {
            if (arch.indexOf("86") >= 0) {
                result = "Mac_OS_X-x86"; //NOI18N
            }
        } else {
            DLightLogger.instance.warning("unknown platform"); //NOI18N
        }
        return result;
    }

    public static String getMonitorCmd() {
        return getPlatformBinary("mmonitor" + getExecutableSuffix()); //NOI18N
    }

    public static String getAgentLib() {
        return getPlatformBinary("magent" + getSharedLibrarySuffix()); //NOI18N
    }

    public static String getEnvVar() {
        return Utilities.isMac() ? "DYLD_INSERT_LIBRARIES" : "LD_PRELOAD"; //NOI18N
    }

    private static String getPlatformBinary(String nameWithSuffix) {
        String platformPath = getPlatformPath();
        if (platformPath != null) {
            String relativePath = "bin" + File.separator + platformPath + File.separator + nameWithSuffix; //NOI18N
            File file = InstalledFileLocator.getDefault().locate(relativePath, null, false);
            if (file != null && file.exists()) {
                return file.getAbsolutePath();
//                return file.getParentFile().getAbsolutePath() + "${_isa}" + '/' + file.getName();
            }
        }
        return null;
    }
}
