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
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.windows.InputOutput;

/**
 *
 */
public class Erprint {

    private final ExecutionEnvironment execEnv;
    private final String er_printCmd;
    private final String experimentDirectory;
    private Future<Integer> er_printTask;
    private final StringWriter out = new StringWriter(1024);
    private PrintWriter in;
    private String terminalString = "";

    public Erprint(ExecutionEnvironment execEnv, String sproHome, String experimentDirectory) {
        this.execEnv = execEnv;
        this.er_printCmd = sproHome + "/bin/er_print"; // NOI18N
        this.experimentDirectory = experimentDirectory;
    }

    void start() {
        NativeProcessBuilder npb = new NativeProcessBuilder(execEnv, er_printCmd);
        npb = npb.setArguments(experimentDirectory).addNativeProcessListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                NativeProcess process = (NativeProcess) e.getSource();
                if (process.getState() == NativeProcess.State.RUNNING) {
                    final PrintWriter writer = new PrintWriter(process.getOutputStream());
                    new Thread(new Runnable() {

                        public void run() {
                            out.getBuffer().setLength(0);
                            writer.write("limit -1\n");
                            writer.flush();
                            while (true) {
                                Thread.yield();
                                terminalString = out.toString();
                                if (terminalString.length() > 0) {
                                    break;
                                }
                            }
                            in = writer;
                        }
                    }).start();
                }
            }
        });

        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.inputOutput(InputOutput.NULL);
        descr = descr.errProcessorFactory(new ErprintErrorRedirectorFactory());
        descr = descr.outProcessorFactory(new ErprintOutputRedirectorFactory(out));
        ExecutionService service = ExecutionService.newService(npb, descr, "collect"); // NOI18N
        er_printTask = service.run();
    }

    void stop() {
        // TODO: do it more gracefully
        er_printTask.cancel(true);
        er_printTask = null;
    }

    private String[] exec(String command) {
        if (er_printTask == null || er_printTask.isDone()) {
            start();
        }

        if (in == null) {
            return null;
        }

        out.getBuffer().setLength(0);
        in.write(command + "\n");
        in.flush();
        in.write("limit -1\n");
        in.flush();
        while (!out.toString().endsWith(terminalString)) {
            Thread.yield();
        }

        return out.getBuffer().toString().replace(terminalString, "").split("\n");
    }

    String setMetrics(String mspec) {
        String[] result = exec("metrics " + mspec);
        return result == null ? "" : result[0].substring(result[0].indexOf(':') + 1);
    }

    private static class ErprintErrorRedirectorFactory
            implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor p) {
            return InputProcessors.copying(new OutputStreamWriter(System.err) {

                final StringBuilder sb = new StringBuilder();
                final static String prefix = "!!!!! ER_PRINT !!!! : ";

                @Override
                public void write(char[] chars) throws IOException {
                    sb.setLength(0);
                    sb.append(prefix);
                    for (int i = 0; i < chars.length; i++) {
                        sb.append(chars[i]);
                        if (i < chars.length - 1 && chars[i] == '\n') {
                            sb.append(prefix);
                        }
                    }
                    super.write(sb.toString().toCharArray());
                }
            });
        }
    }

    private static class ErprintOutputRedirectorFactory
            implements InputProcessorFactory {

        private Writer writer;

        public ErprintOutputRedirectorFactory(Writer writer) {
            this.writer = writer;
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.copying(writer);
        }
    }
}
