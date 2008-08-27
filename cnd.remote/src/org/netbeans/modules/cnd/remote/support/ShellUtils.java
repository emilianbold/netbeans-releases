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
package org.netbeans.modules.cnd.remote.support;

import java.util.Map;
import org.netbeans.modules.cnd.remote.mapper.RemoteHostInfoProvider;

/**
 *
 * @author Sergey Grinev
 */
public class ShellUtils {

    private ShellUtils() {
    }

    public static String prepareExportString(String hkey, Map<String, String> env) {
        boolean isCshShell = RemoteHostInfoProvider.getHostInfo(hkey).isCshShell();
        return prepareExportString(isCshShell, env);
    }

    public static String prepareExportString(boolean isCshShell, Map<String, String> env) {
        StringBuilder cmdline = new StringBuilder();
        String exportCommand = getExportCommand(isCshShell);
        String middleFix = isCshShell ? " " : "="; // NOI18N
        for (String ev : env.keySet()) {
            cmdline.append(exportCommand).append(ev).append(middleFix).append("\"").append(env.get(ev)).append("\";"); // NOI18N
        }
        return cmdline.toString();
    }

    public static String prepareExportString(String hkey, String[] envp) {
        boolean isCshShell = RemoteHostInfoProvider.getHostInfo(hkey).isCshShell();
        return prepareExportString(isCshShell, envp);
    }

    static String prepareExportString(boolean isCshShell, String[] envp) {
        StringBuilder cmdline = new StringBuilder();
        String exportCommand = getExportCommand(isCshShell);
        for (String ev : envp) {
            ev = ev.replace("=", isCshShell ? " \"":"=\""); //NOI18N
            cmdline.append(exportCommand).append(ev).append("\";"); // NOI18N
        }
        return cmdline.toString();
    }

    private static final String BashExport = "export"; //NOI8N
    private static final String CshExport = "setenv"; //NOI8N
    // all other shells are smart enough to know both
    
    private static String getExportCommand(boolean isCshShell) {
        return (isCshShell ? CshExport : BashExport) + " ";
    }
}
