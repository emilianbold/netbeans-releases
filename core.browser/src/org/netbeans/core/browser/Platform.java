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
package org.netbeans.core.browser;

import java.util.StringTokenizer;

/**
 * List of supported platforms.
 * 
 * @author S. Aubrecht
 */
public enum Platform {
    OSX("macosx"), Linux("linux"), Win32("win32"), Solaris("solaris-x86"), Unsupported(""); //NOI18N //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    public static final Platform platform;
    public static final String arch;
    public static final String codeNameBase;
    static final String xulRunnerHome;

    static {
        String osname = System.getProperty("os.name"); //NOI18N
        if ("Mac OS X".equals(osname)) { //NOI18N
            platform = Platform.OSX;
            codeNameBase = "org.netbeans.core.browser.macosx"; //NOI18N
            xulRunnerHome = "modules/lib/native/macosx/xulrunner"; //NOI18N
        } else if ("Linux".equals(osname)) { //NOI18N
            platform = Platform.Linux;
            codeNameBase = "org.netbeans.core.browser.linux"; //NOI18N
            xulRunnerHome = "modules/lib/native/linux/xulrunner"; //NOI18N
        } else if ("SunOS".equals(osname)) { //NOI18N
            platform = Platform.Solaris;
            codeNameBase = "org.netbeans.core.browser.solaris"; //NOI18N
            xulRunnerHome = "modules/lib/native/solaris-x86/xulrunner"; //NOI18N
        } else if (osname.contains("Windows")) {
            platform = Platform.Win32;
            codeNameBase = "org.netbeans.core.browser.win"; //NOI18N
            xulRunnerHome = "modules/lib/native/win32/xulrunner"; //NOI18N
        } else {
            platform = Platform.Unsupported;
            codeNameBase = "org.netbeans.core.browser.unsupported"; //NOI18N
            xulRunnerHome = "modules/lib/native/unsupported/xulrunner"; //NOI18N
        }

        String osarch = System.getProperty("os.arch"); //NOI18N
        String archTemp = osarch;
        for (String x : new String[]{"i386", "i486", "i586", "i686"}) { //NOI18N //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            if (x.equals(osarch)) {
                archTemp = "x86"; //NOI18N
                break;
            }
        }
        arch = archTemp;
    }

    private final String libDir;

    private Platform(String libDir) {
        this.libDir = libDir;
    }

    public String libDir() {
        return libDir;
    }

    public String arch() {
        return arch;
    }

    public boolean is32Bit() {
        return "x86".equals(arch);
    }

    public static boolean usingGTK2Toolkit() {
        return platform==Linux || platform==Solaris;
    }

    public static long getJavaVersion() {
        String v = System.getProperty("java.version"); //NOI18N
        return jdkVersionToNumber(v);
    }

    public static boolean checkJavaVersion(String minVersion, String maxVersion) {
        long current = getJavaVersion();

        if (minVersion!=null && minVersion.length()>0) {
            long min = jdkVersionToNumber(minVersion);
            if (current<min) return false;
        }

        if (maxVersion!=null && maxVersion.length()>0) {
            long max = jdkVersionToNumber(maxVersion);
            if (current>max) return false;
        }

        return true;
    }

    private static long jdkVersionToNumber(String verStr) {
        try {
            String numStr;
            String buildStr;
            int idx = verStr.indexOf("_"); //NOI18N
            if (idx>=0) {
                numStr = verStr.substring(0, idx);
                buildStr = verStr.substring(idx+1);
            } else {
                numStr = verStr;
                buildStr = ""; //NOI18N
            }

            long num = 0;
            StringTokenizer st = new StringTokenizer(numStr, "."); //NOI18N
            long shift = 100*100*100;
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                int n = Integer.parseInt(s);
                num += n*shift;
                shift /= 100;
            }

            if (buildStr.startsWith("b")) buildStr = buildStr.substring(1); //NOI18N
            if (buildStr.length()>0) {
                int n = Integer.parseInt(buildStr);
                num += n;
            }

            return num;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Error parsing java version: "+verStr); //NOI18N
        }
    }

}
