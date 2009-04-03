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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.windows.InputOutput;

/**
 *
 */
public final class Erprint {
    private final Logger log = DLightLogger.getLogger(Erprint.class);
    private final Object lock = new String(Erprint.class.getName());
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

    public ExperimentStatistics getExperimentStatistics(boolean restart) {
        restart(restart);
        String[] stat = exec("statistics"); // NOI18N
        return new ExperimentStatistics(stat);
    }

    LeaksStatistics getExperimentLeaks(boolean restart) {
        restart(restart);
        String[] stat = exec("leaks"); // NOI18N
        return new LeaksStatistics(stat);
    }

    private final void start() {
        final CountDownLatch doneSignal = new CountDownLatch(1);

        NativeProcessBuilder npb = new NativeProcessBuilder(execEnv, er_printCmd);
        npb = npb.addNativeProcessListener(new ErprintListener(doneSignal));
        npb = npb.setArguments(experimentDirectory);

        ExecutionDescriptor descr = new ExecutionDescriptor();
        descr = descr.inputOutput(InputOutput.NULL);

        if (log.isLoggable(Level.FINEST)) {
            descr = descr.errProcessorFactory(new ErprintErrorRedirectorFactory());
        }
        
        descr = descr.outProcessorFactory(new InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return erOutputProcessor;
            }
        });

        ExecutionService service = ExecutionService.newService(npb, descr, "er_print"); // NOI18N
        er_printTask = service.run();

        try {
            // Wait until prompt received ...
            // comes from the listener (ErprintListener)
            doneSignal.await();
        } catch (InterruptedException ex) {
        }
    }

    public final void stop() {
        // TODO: do it more gracefully
        synchronized (lock) {
            if (er_printTask != null) {
                er_printTask.cancel(true);
                er_printTask = null;
                try {
                    erOutputProcessor.reset();
                } catch (IOException ex) {
                }
            }
        }
    }

    public final void restart(boolean restart) {
        if (!restart) {
            return;
        }

        synchronized (lock) {
            stop();
            start();
        }
    }

    private String[] exec(String command) {
        return exec(command, -1);
    }

    private String[] exec(String command, int limit) {
        String[] result = null;

        synchronized (lock) {
            if (er_printTask == null || er_printTask.isDone()) {
                restart(true);
            }

            if (in == null) {
                return null;
            }

            final CountDownLatch doneSignal = new CountDownLatch(limit > 0 ? 3 : 2);
            erOutputProcessor.reset(doneSignal);

            if (limit > 0) {
                in.println("limit " + limit); // NOI18N
                in.flush();
            }

            in.println(command);
            in.flush();
            in.println("limit -1"); // NOI18N
            in.flush();

            try {
                doneSignal.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            result = erOutputProcessor.getBuffer();
        }

        return result;
    }

    String setMetrics(String mspec) {
        String[] result = null;
        result = exec("metrics " + mspec); // NOI18N
        return (result == null || result.length == 0) ? "" // NOI18N
                : result[0].substring(result[0].indexOf(':') + 1); // NOI18N
    }

    String setSortBy(String msort) {
        String[] result = exec("sort " + msort); // NOI18N
        return (result == null || result.length == 0) ? "" // NOI18N
                : result[0].substring(result[0].indexOf(':') + 1); // NOI18N
    }

    public String[] getHotFunctions(Metrics metrics, int limit, boolean restart) {
        String[] result = null;
        synchronized (lock) {
            restart(restart);
            setMetrics(metrics.mspec);
            setSortBy(metrics.msort);
            erOutputProcessor.setFilterType(FilterType.startsWithNumber);
            result = exec("functions", limit); // NOI18N
            erOutputProcessor.setFilterType(FilterType.noFiltering);
        }
        return result;
    }

    public String[] getHotFunctions(String command, Metrics metrics, int limit, boolean restart) {
        String[] result = null;
        synchronized (lock) {
            restart(restart);
            setMetrics(metrics.mspec);
            setSortBy(metrics.msort);
            erOutputProcessor.setFilterType(FilterType.startsWithNumber);
            result = exec(command, limit); // NOI18N
            erOutputProcessor.setFilterType(FilterType.noFiltering);
        }
        return result;
    }

    public FunctionStatistic getFunctionStatistic(String functionName, boolean restart){
        FunctionStatistic result = null;
        synchronized (lock) {
            restart(restart);
            result = new FunctionStatistic(exec("fsingle " + functionName, Integer.MAX_VALUE)); // NOI18N
        }
        return result;
    }

    String[] getCallersCallees(int limit) {
        String[] ccOut = exec("callers-callees", limit); // NOI18N
        // TODO: process output
        return new String[0];
    }

    private static class ErprintErrorRedirectorFactory
            implements InputProcessorFactory {

        public InputProcessor newInputProcessor(InputProcessor p) {
            return InputProcessors.copying(new OutputStreamWriter(System.err) {

                final StringBuilder sb = new StringBuilder();
                final static String prefix = "!!!!! ER_PRINT SAYS !!!! : "; // NOI18N

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
        // Buffer must be synchronized, because it could be accessed
        // from several threads (example: one thread - onTimer, another one -
        // user clicks on indicator)
        // The problem may appear when one thread gets buffer, while another one
        // cleans it... 

        private final Object lock = new String(FilteredInputProcessor.class.getName());
        private final List<String> buffer = Collections.synchronizedList(new ArrayList<String>());
        private FilterType filterType = FilterType.noFiltering;
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
            synchronized (lock) {
                String data = new String(chars, 0, chars.length);
                String[] lines = data.split("\n"); // NOI18N

                for (String line : lines) {
                    if (prompt != null && line.startsWith(prompt)) {
                        do {
                            doneSignal.countDown();
                            line = line.substring(prompt.length());
                        } while (line.contains(prompt));
                    }

                    if (line.length() != 0) {
                        switch (filterType) {
                            // TODO: fixme Need correct filtering.
                            case startsWithNumber:
                                if (!line.matches("^ *[0-9]+.*")) { // NOI18N
                                    continue;
                                }
                        }
                        buffer.add(line.trim());
                    }
                }
            }
        }

        public void reset() throws IOException {
            synchronized (lock) {
                if (doneSignal != null) {
                    while (doneSignal.getCount() > 0) {
                        doneSignal.countDown();
                    }
                }

                reset(null);
            }
        }

        public void close() throws IOException {
            // do nothing
        }

        public String[] getBuffer() {
            return buffer.toArray(new String[0]);
        }

        private void reset(CountDownLatch doneSignal) throws IllegalThreadStateException {
            if (this.doneSignal != null && this.doneSignal.getCount() > 0) {
                String message = "SYNCHRONIZATION PROBLEM!!!\n" + // NOI18N
                        "An attempt to reset er_print input processor \n" + // NOI18N
                        "BEFORE previous output is processed"; // NOI18N

                throw new IllegalThreadStateException(message);
            }
            buffer.clear();
            this.doneSignal = doneSignal;
            // null - a special case. This means that er_print process has
            // changed. So, prompt will be fetched again.... 
            if (doneSignal == null) {
                prompt = null;
            }
        }
    }

    private static enum FilterType {

        noFiltering,
        startsWithNumber,
    }

    private final class ErprintListener implements ChangeListener {

        private final CountDownLatch doneSignal;

        public ErprintListener(CountDownLatch doneSignal) {
            this.doneSignal = doneSignal;
        }

        public void stateChanged(ChangeEvent e) {
            NativeProcess process = (NativeProcess) e.getSource();

            switch (process.getState()) {
                case RUNNING:
                    final PrintWriter writer = new PrintWriter(process.getOutputStream());
                    final Runnable onStart = new Runnable() {

                        public void run() {
                            try {
                                writer.println("limit -1"); // NOI18N
                                writer.flush();
                                String[] er_output;
                                while (true) {
                                    if (Thread.currentThread().isInterrupted()) {
                                        break;
                                    }

                                    er_output = erOutputProcessor.getBuffer();
                                    if (er_output.length == 1) {
                                        erOutputProcessor.setPrompt(er_output[0]);
                                        break;
                                    } else {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException ex) {
                                            Thread.currentThread().interrupt();
                                        }
                                    }
                                }

                                in = writer;
                            } finally {
                                doneSignal.countDown();
                            }
                        }
                    };

                    DLightExecutorService.submit(onStart, "ER_PRINT Prompt Thread Reader"); // NOI18N

                    break;
                case ERROR:
                    doneSignal.countDown();
                    break;
            }
        }
    }
}
