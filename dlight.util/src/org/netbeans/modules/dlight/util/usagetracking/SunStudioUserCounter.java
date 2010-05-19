/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.dlight.util.usagetracking;

import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class SunStudioUserCounter {

    private static final boolean SUNW_NO_UPDATE_NOTIFY;
    private static final RequestProcessor SS_USER_COUNT = new RequestProcessor("SunStudio check_update"); // NOI18N

    static {
        SUNW_NO_UPDATE_NOTIFY = (System.getProperty("SUNW_NO_UPDATE_NOTIFY") != null);  // NOI18N
    }
 
    public enum IDEType {

        CND("cnd"), // NOI18N
        SUN_STUDIO_IDE("ide"), // NOI18N
        DBX_TOOL("dbxtool"), // NOI18N
        DLIGHTTOOL("dlighttool"); // NOI18N
        private final String tag;

        private IDEType(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    };
    private static IDEType appType = null;

    private SunStudioUserCounter() {
    }

    public static IDEType getIDEType() {
        if (appType == null) {
            // default is CND
            String ide = System.getProperty("spro.ide.name"); // NOI18N
            if ("sside".equals(ide)) { // NOI18N
                appType = IDEType.SUN_STUDIO_IDE;
            } else if ("dlighttool".equals(ide)) { // NOI18N
                appType = IDEType.DLIGHTTOOL;
            } else if ("dbxtool".equals(ide)) {// NOI18N
                appType = IDEType.DBX_TOOL;
            } else {
                appType = IDEType.CND;
            }
        }
        return appType;
    }

    private static String getSunStudioBinDir(ExecutionEnvironment env) {
        if (env != null && env.isRemote()) {
            return null;
        }
        String ssBin = System.getProperty("spro.bin");// NOI18N
        if (ssBin == null) {
            ssBin = System.getProperty("spro.home");// NOI18N
            if (ssBin != null) {
                if (!ssBin.endsWith("/") && !ssBin.endsWith("\\")) { // NOI18N
                    ssBin += "/"; // NOI18N
                }
                ssBin += "bin/"; // NOI18N
            }
        } else {
            ssBin += "/"; // NOI18N
        }
        return ssBin;
    }

    private static String getCheckUpdatePath(String ssBinPath, ExecutionEnvironment env) {
        String checkUpdatePath = System.getProperty("spro.check_update");// NOI18N
        if (checkUpdatePath == null || (ssBinPath != null && env.isRemote())) {
            ssBinPath = ssBinPath == null ? getSunStudioBinDir(env) : ssBinPath;
            if (ssBinPath != null) {
                if (!ssBinPath.endsWith("/") && !ssBinPath.endsWith("\\")) { // NOI18N
                    ssBinPath += "/"; // NOI18N
                }
                checkUpdatePath = ssBinPath + "../prod/bin/check_update"; // NOI18N
            }
        }
        return checkUpdatePath;
    }
    /**
     * count active user of the IDE
     * @param checkUpdatePath path to SunStudio "bin" directory
     * @param execEnv execution environment
     */
    public static void countIDE(final String ssBaseDir, final ExecutionEnvironment execEnv) {
        countTool(getCheckUpdatePath(ssBaseDir, execEnv), execEnv, getIDEType().getTag());
    }

    public static void countDLight(final ExecutionEnvironment execEnv) {
        if (SUNW_NO_UPDATE_NOTIFY) {
            return;
        }
        String tool;
        if (SunStudioUserCounter.getIDEType() == SunStudioUserCounter.IDEType.DLIGHTTOOL) {
            tool = "dlight"; // NOI18N
        } else {
            tool = "dlightss"; // NOI18N
        }
        if (!execEnv.isLocal()) {
            final ExecutionEnvironment localEnv = ExecutionEnvironmentFactory.getLocal();
            final String localCheckUpdatePath = getCheckUpdatePath(null, localEnv);
            if (localCheckUpdatePath != null) {
                // register localy
                countTool(localCheckUpdatePath, localEnv, tool);
                return;
            }
        }
        countTool(getCheckUpdatePath(null, execEnv), execEnv, tool);
    }

    public static void countGizmo(final String sprohome, final ExecutionEnvironment execEnv) {
        String ssBin = null;
        if (sprohome != null) {
            if (!sprohome.endsWith("/") && !sprohome.endsWith("\\")) { // NOI18N
                ssBin = sprohome + "/bin/";// NOI18N
            } else {
                ssBin = sprohome + "bin/";// NOI18N
            }
        }
        countTool(getCheckUpdatePath(ssBin, execEnv), execEnv, "gizmo"); // NOI18N
    }
    /**
     * count active user of the tool
     * @param checkUpdatePath path to SunStudio check_update
     * @param execEnv execution environment
     */
    private static void countTool(final String checkUpdatePath, final ExecutionEnvironment execEnv, final String toolTag) {
        if (SUNW_NO_UPDATE_NOTIFY) {
            return;
        }
        if (checkUpdatePath == null || execEnv == null || toolTag == null) {
            return;
        }
        if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            SS_USER_COUNT.post(new Runnable() {
                public void run() {
                    NativeProcessBuilder nb = NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(checkUpdatePath).setArguments(toolTag);
                    try {
                        nb.call();
                    } catch (IOException ex) {
                        // skip
                    }
                }
            });
        }
    }
}
