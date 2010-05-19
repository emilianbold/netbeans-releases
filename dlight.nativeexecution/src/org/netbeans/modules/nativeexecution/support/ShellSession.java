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
package org.netbeans.modules.nativeexecution.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;

/**
 * This class holds a single shell session per environment
 * to run *quick* tasks.
 * It is synchronized. Use with care!
 * Failed/closed session will be restored automatically on first request.
 *
 * @author ak119685
 */
public final class ShellSession {

    private final static HashMap<ExecutionEnvironment, NativeProcess> sessions =
            new HashMap<ExecutionEnvironment, NativeProcess>();
    private static final String eop = "ShellSession.CMDDONE"; // NOI18N

    private ShellSession() {
    }

    public static synchronized void shutdown(final ExecutionEnvironment env) {
        if (sessions.containsKey(env)) {
            NativeProcess process = sessions.get(env);
            ProcessUtils.destroy(process);
            sessions.remove(env);
        }
    }

    private static synchronized NativeProcess startProcess(ExecutionEnvironment env) throws IOException {
        NativeProcess result;
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        HostInfo info = HostInfoUtils.getHostInfo(env);
        npb.setExecutable(info.getShell()).setArguments("-s"); // NOI18N
        npb.getEnvironment().put("LC_ALL", "C"); // NOI18N
        result = npb.call();
        sessions.put(env, result);
        return result;
    }

    public static synchronized String[] execute(final ExecutionEnvironment env, final String command) throws IOException {
        NativeProcess process = sessions.get(env);

        if (process == null || process.getState() != NativeProcess.State.RUNNING) {
            process = startProcess(env);
        }

        String cmd = command + "; echo " + eop + " \n"; // NOI18N
        process.getOutputStream().write(cmd.getBytes());
        process.getOutputStream().flush();
        String[] result = getResult(process.getInputStream());

        return result;
    }

    private static String[] getResult(InputStream inputStream) throws IOException {
        ArrayList<String> resultBuffer = new ArrayList<String>();
        StringBuilder lineBuffer = new StringBuilder();
        int c;

        while (true) {
            if ((c = inputStream.read()) < 0) {
                break;
            }

            if (c == '\n') {
                String line = lineBuffer.toString();
                lineBuffer.setLength(0);
                if (line.startsWith(eop)) {
                    break;
                }
                resultBuffer.add(line);
            } else {
                lineBuffer.append((char) c);
            }
        }

        return resultBuffer.toArray(new String[0]);
    }
}
