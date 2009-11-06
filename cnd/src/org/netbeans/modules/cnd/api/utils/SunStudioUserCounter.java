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

package org.netbeans.modules.cnd.api.utils;

import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class SunStudioUserCounter {

    private static final boolean SUNW_NO_UPDATE_NOTIFY;
    private static final RequestProcessor SS_USER_COUNT = new RequestProcessor("SunStudio check_update"); // NOI18N

    static {
        SUNW_NO_UPDATE_NOTIFY = true;//(System.getProperty("SUNW_NO_UPDATE_NOTIFY") != null);  // NOI18N
    }

    private enum IDEType {

        CND("cnd"), // NOI18N
        SUN_STUDIO_IDE("ide"), // NOI18N
        DBX_TOOL("dbxtool"); // NOI18N
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

    private static IDEType getIDEType() {
        if (appType == null) {
            // default is CND
            appType = IDEType.CND;
            // check for SS IDE
            Collection<? extends ModuleInfo> modules = Lookup.getDefault().lookupAll(ModuleInfo.class);
            for (ModuleInfo moduleInfo : modules) {
                if (moduleInfo.isEnabled()) {
                    String codeNameBase = moduleInfo.getCodeNameBase();
                    if ("com.sun.tools.debugger.dbxfacade.ide".equals(codeNameBase)) { // NOI18N
                        appType = IDEType.SUN_STUDIO_IDE;
                        break;
                    } else if ("com.sun.tools.debugger.dbxfacade.tool".equals(codeNameBase)) { // NOI18N
                        appType = IDEType.DBX_TOOL;
                        break;
                    }
                }
            }
        }
        return appType;
    }

    public static void countIDE(final String basePath, final ExecutionEnvironment execEnv) {
        final String tag;
        IDEType type = getIDEType();
        switch (type) {
            case CND:
            case SUN_STUDIO_IDE:
                // register CND and SS_IDE
                tag = type.getTag();
                break;
            default:
                // other IDEs are not tracked yet
                return;
        }
        countTool(basePath, execEnv, tag);
    }

    public static void countTool(final String basePath, final ExecutionEnvironment execEnv, final String toolTag) {
        if (!SUNW_NO_UPDATE_NOTIFY && ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            SS_USER_COUNT.post(new Runnable() {
                public void run() {
                    NativeProcessBuilder nb = NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable(basePath + "/../prod/bin/check_update").setArguments(toolTag); // NOI18N
                    try {
                        nb.call();
                    } catch (IOException ex) {
                        if (CndUtils.isDebugMode()) {
                            ex.printStackTrace(System.err);
                        }
                    }
                }
            });
        }
    }
}
