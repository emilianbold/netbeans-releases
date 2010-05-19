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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.dlight.util.Util;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Exceptions;

/**
 * @author Alexey Vladykin
 */
public final class DTraceScriptUtils {

    private DTraceScriptUtils() {
    }

    public static final String EOF_MARKER = "__EOF_MARKER__"; // NOI18N

    public static void insertEOFMarker(File script) throws IOException {
        BufferedWriter w = new BufferedWriter(new FileWriter(script, true));
        try {
            w.write("END { printf(\"" + EOF_MARKER + "\") }"); // NOI18N
        } finally {
            w.close();
        }
    }

public static File mergeScripts(Map<String, URL> scripts, boolean fixStarttime) throws IOException {
        File result = File.createTempFile("dlight", ".d"); // NOI18N
        result.deleteOnExit();

        boolean beginContext = false;

        BufferedWriter w = new BufferedWriter(new FileWriter(result));
        try {
            w.write("#!/usr/sbin/dtrace -wZCqs\n"); // NOI18N
            w.write("BEGIN{system(\"prun %d\", $1);}\n"); // NOI18N
            for (Map.Entry<String, URL> entry : scripts.entrySet()) {
                String prefix = entry.getKey();
                URL url = entry.getValue();
                BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
                try {
                    String replacement = "$1" + prefix; // NOI18N
                    for (String line = r.readLine(); line != null; line = r.readLine()) {
                        if (!line.startsWith("#!")) { // NOI18N
                            line = line.replaceAll("(print[af]\\(\")", replacement); // NOI18N
                            if (fixStarttime) {
                                if (!beginContext && line.trim().startsWith("BEGIN")) { // NOI18N
                                    beginContext = true;
                                }

                                if (beginContext) {
                                    line = line.replaceAll("=.*timestamp", "= \\$2"); // NOI18N
                                    if (line.trim().endsWith("}")) { // NOI18N
                                        beginContext = false;
                                    }
                                }
                            }
                            w.write(line); // NOI18N
                            w.write('\n'); // NOI18N
                        }
                    }
                    w.write('\n'); // NOI18N
                    w.write("proc:::exit/pid==$1/{exit(0);}\n"); // NOI18N
                } finally {
                    r.close();
                }
            }
        } finally {
            w.close();
        }
        return result;
    }

    public static String uploadScript(ExecutionEnvironment execEnv, File scriptFile) {
        String scriptPath = null;
        if (execEnv.isLocal()) {
            // No need to copy file on localhost -
            // just ensure execution permissions...
            scriptPath = scriptFile.getAbsolutePath();
            Util.setExecutionPermissions(Arrays.asList(scriptPath));
        } else {
            String briefName = scriptFile.getName();
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
                scriptPath = hostInfo.getTempDir() + "/" + briefName; // NOI18N
                Future<Integer> copyResult = CommonTasksSupport.uploadFile(
                        scriptFile.getAbsolutePath(), execEnv, scriptPath, 0777, null);
                copyResult.get();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return scriptPath;
    }
}
