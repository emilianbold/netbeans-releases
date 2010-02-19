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
package org.netbeans.modules.nativeexecution.pty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.pty.PtyCreatorImpl.PtyImplementation;

/**
 *
 * @author ak119685
 */
public final class SttySupport {

    private final static HashMap<ExecutionEnvironment, SttySupport> cache =
            new HashMap<ExecutionEnvironment, SttySupport>();
    private final Process sh;
    private final OutputStream in;
    private final InputStream out;
    private final OutputProcessor outProcessor;
//    private final InputStream err;
//    private final OutputProcessor errProcessor;
    private final ExecutionEnvironment env;
    private volatile boolean stopped = false;

    private SttySupport(final ExecutionEnvironment env) throws IOException {
        this.env = env;

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("/bin/sh").setArguments("-s"); // NOI18N
        npb.getEnvironment().put("LC_ALL", "C"); // NOI18N

        sh = npb.call();
        in = sh.getOutputStream();
        out = sh.getInputStream();
        outProcessor = new OutputProcessor(out);


//        err = sh.getErrorStream();
//        errProcessor = new OutputProcessor(err);
    }

    public static synchronized SttySupport getFor(final ExecutionEnvironment env) throws IOException {
        if (!cache.containsKey(env)) {
            SttySupport newSupport = new SttySupport(env);
            cache.put(env, newSupport);
        }

        return cache.get(env);
    }

    // TODO: When?
    void stop() {
        if (stopped) {
            return;
        }

        synchronized (cache) {
            cache.remove(env);
        }

        ProcessUtils.destroy(sh);
    }

    public synchronized String[] apply(final PtyImplementation pty, final String args) throws IOException {
        if (stopped) {
            throw new IllegalArgumentException("This SttySupport is already stopped... "); // NOI18N
        }

        post("/bin/stty " + args + " < " + pty.getSlaveName() + " && echo ==="); // NOI18N
        return outProcessor.getOutput();
    }

    private void post(String cmd) throws IOException {
        in.write((cmd + "\n").getBytes()); // NOI18N
        in.flush();
    }

    private static class OutputProcessor {

        private final ArrayList<String> resultBuffer = new ArrayList<String>();
        private final StringBuilder lineBuffer = new StringBuilder();
        private final InputStream out;

        public OutputProcessor(InputStream out) {
            this.out = out;
        }

        public String[] getOutput() throws IOException {
            resultBuffer.clear();
            lineBuffer.setLength(0);

            int c;

            while (true) {
                if ((c = out.read()) < 0) {
                    break;
                }

                if (c == '\n') {
                    String line = lineBuffer.toString();
                    lineBuffer.setLength(0);
                    if (line.startsWith("===")) { // NOI18N
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
}
