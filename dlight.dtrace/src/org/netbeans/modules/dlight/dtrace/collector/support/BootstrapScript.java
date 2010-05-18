/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

/**
 *
 * @author ak119685
 */
public final class BootstrapScript {

    private static final String scriptURI = "/org/netbeans/modules/dlight/dtrace/resources/bootstrap.d"; // NOI18N
    private final String pattern;
    private String path = null;
    private final String originalScript;

    public BootstrapScript(String originalScript, String pattern) {
        this.originalScript = originalScript;
        if (pattern != null && pattern.trim().length() == 0) {
            pattern = null;
        }
        this.pattern = pattern;
    }

    public synchronized String getScriptPath(final ExecutionEnvironment env) throws IOException {
        if (path != null) {
            return path;
        }

        if (!HostInfoUtils.isHostInfoAvailable(env) || !ConnectionManager.getInstance().isConnectedTo(env)) {
            throw new IOException("Env " + env.getDisplayName() + " is not ready!"); // NOI18N
        }

        File tmp = File.createTempFile("dlight", ".d", HostInfoUtils.getHostInfo(env).getTempDirFile()); // NOI18N
        tmp.deleteOnExit();

        BufferedReader br = null;
        BufferedWriter bw = null;

        try {
            InputStream is = BootstrapScript.class.getResourceAsStream(scriptURI); // NOI18N
            br = new BufferedReader(new InputStreamReader(is));
            bw = new BufferedWriter(new FileWriter(tmp));

            StringBuilder predicateBuffer = new StringBuilder("/progenyof(\\$1)"); // NOI18N
            if (pattern == null) {
                predicateBuffer.append('/');
            } else {
                predicateBuffer.append(" && BSS_active == 1 && strstr(execname, \"").append(pattern).append("\") != NULL/"); // NOI18N
            }
            String predicate = predicateBuffer.toString();

            String line;

            while ((line = br.readLine()) != null) {
                line = line.replaceAll("__DLIGHT_PREDICATE__", predicate); // NOI18N
                line = line.replaceAll("__DLIGHT_DSCRIPT__", originalScript); // NOI18N
                bw.write(line);
                bw.newLine();
            }
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (br != null) {
                br.close();
            }
        }

        return DTraceScriptUtils.uploadScript(env, tmp);
    }
}
