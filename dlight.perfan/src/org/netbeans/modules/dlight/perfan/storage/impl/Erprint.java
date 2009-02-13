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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
    private PrintWriter in;
    private final FilteredInputProcessor erOutputProcessor;

    public Erprint(ExecutionEnvironment execEnv, String sproHome, String experimentDirectory) {
        this.execEnv = execEnv;
        this.er_printCmd = sproHome + "/bin/er_print"; // NOI18N
        this.experimentDirectory = experimentDirectory;
        this.erOutputProcessor = new FilteredInputProcessor();
    }

    synchronized void start() {
        final CountDownLatch doneSignal = new CountDownLatch(1);

        NativeProcessBuilder npb = new NativeProcessBuilder(execEnv, er_printCmd);
        npb = npb.setArguments(experimentDirectory).addNativeProcessListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                NativeProcess process = (NativeProcess) e.getSource();
                if (process.getState() == NativeProcess.State.RUNNING) {
                    final PrintWriter writer = new PrintWriter(process.getOutputStream());
                    new Thread(new Runnable() {

                        public void run() {
                            writer.write("limit -1\n");
                            writer.flush();
                            String[] er_output;
                            while (true) {
                                Thread.yield();
                                er_output = erOutputProcessor.getBuffer();
                                if (er_output.length > 0) {
                                    erOutputProcessor.setPrompt(er_output[0]);
                                    break;
                                }
                            }
                            in = writer;
                            doneSignal.countDown();
                        }
                    }).start();
                }
            }
        });

        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.inputOutput(InputOutput.NULL);
        descr = descr.errProcessorFactory(new ErprintErrorRedirectorFactory());
        descr = descr.outProcessorFactory(new InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return erOutputProcessor;
            }
        });

        ExecutionService service = ExecutionService.newService(npb, descr, "collect"); // NOI18N
        er_printTask = service.run();

        try {
            doneSignal.await();
        } catch (InterruptedException ex) {
        }
    }

    synchronized void stop() {
        // TODO: do it more gracefully
        er_printTask.cancel(true);
        er_printTask = null;
    }

    synchronized void refresh() {
        stop();
        start();
    }

    synchronized private String[] exec(String command) {
        return exec(command, -1);
    }

    synchronized private String[] exec(String command, int limit) {
        if (er_printTask == null || er_printTask.isDone()) {
            start();
        }

        if (in == null) {
            return null;
        }

        CountDownLatch doneSignal = new CountDownLatch(limit > 0 ? 3 : 2);
        erOutputProcessor.reset(doneSignal);

        if (limit > 0) {
            in.write("limit " + limit + "\n");
            in.flush();
        }

        in.write(command + "\n");
        in.flush();
        in.write("limit -1\n");
        in.flush();

        try {
            doneSignal.await();
        } catch (InterruptedException ex) {
        }

        return erOutputProcessor.getBuffer();
    }

    String setMetrics(String mspec) {
        String[] result = exec("metrics " + mspec); // NOI18N
        return result == null ? "" // NOI18N
                : result[0].substring(result[0].indexOf(':') + 1); // NOI18N
    }

    String setSortBy(String msort) {
        String[] result = exec("sort " + msort); // NOI18N
        return result == null ? "" // NOI18N
                : result[0].substring(result[0].indexOf(':') + 1); // NOI18N
    }

    String[] getHotFunctions(int limit) {
        erOutputProcessor.setFilterType(FilterType.onlyContainsNumbers);
        String[] result = exec("functions", limit);
        erOutputProcessor.setFilterType(FilterType.noFiltering);
        return result;
    }

    String[] getCallersCallees(int limit) {
        String[] ccOut = exec("callers-callees", limit);
        ArrayList<String> result = new ArrayList<String>();

        for (String s : ccOut) {
            if (s.length() == 0) {
                result.add(s);
            } else {
                char c = s.charAt(0);
                if ((c >= '0' && c <= '9') || c == '\n') {
                    result.add(s);
                }
            }
        }

        return result.toArray(new String[0]);
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

    private static class FilteredInputProcessor implements InputProcessor {

        private FilterType filterType = FilterType.noFiltering;
        private List<String> buffer = new ArrayList();
        private String prompt = null;
        private CountDownLatch doneSignal;

        public void setFilterType(FilterType filterType) {
            this.filterType = filterType;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            String data = new String(chars, 0, chars.length);
            String[] lines = data.split("\n");

            for (String line : lines) {
                if (prompt != null && line.startsWith(prompt)) {
                    do {
                        doneSignal.countDown();
                        line = line.substring(prompt.length());
                    } while (line.contains(prompt));
                }

                if (line.length() != 0) {
                    switch (filterType) {
                        case onlyContainsNumbers:
                            if (!line.contains("0")) {
                                continue;
                            }
                    }
                    buffer.add(line);
                }
            }
        }

        public void reset() throws IOException {
        }

        public void close() throws IOException {
            // do nothing
        }

        public String[] getBuffer() {
            return buffer.toArray(new String[0]);
        }

        private void reset(CountDownLatch doneSignal) {
            buffer.clear();
            this.doneSignal = doneSignal;
        }
    }

    private static enum FilterType {

        noFiltering,
        onlyContainsNumbers,
    }
}
