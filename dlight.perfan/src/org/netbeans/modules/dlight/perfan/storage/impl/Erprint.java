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
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.perfan.spi.datafilter.CollectedObjectsFilter;
import org.netbeans.modules.dlight.perfan.stack.impl.FunctionCallImpl;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

final class Erprint {

    private static final Pattern specPattern = Pattern.compile("[^:]*: (.*)"); // NOI18N
    private static final Pattern sortPattern = Pattern.compile(".* \\( (.*) \\)"); // NOI18N
    private static final Pattern choicePattern = Pattern.compile("^[ \t]+([0-9]+)\\) .* \\((.*)\\)"); // NOI18N
    private static final String choiceMarker = "Available name list:"; // NOI18N
    private final Logger log = DLightLogger.getLogger(Erprint.class);
    private final AtomicInteger locks = new AtomicInteger();
    private final NativeProcess process;
    private final InputStream out;
    private final InputStream err;
    private final OutputStream in;
    private final OutputProcessor outProcessor;
    private int currentLimit = -1;
    private boolean stopped = false;
    private final String logPrefix;

    Erprint(NativeProcessBuilder npb, int sessionID) throws IOException {
        process = npb.call();
        logPrefix = "er_print [" + process.getPID() + "]: "; // NOI18N
        addLock();

        String logFlag = System.getProperty("nativeexecution.support.logger.er_print"); // NOI18N

        if (logFlag == null) {
            log.setLevel(Level.INFO);
        }

        if (log.isLoggable(Level.FINEST)) {
            log.finest(logPrefix + "started"); // NOI18N
        }
        out = process.getInputStream();
        in = process.getOutputStream();
        err = process.getErrorStream();
        outProcessor = new OutputProcessor();
        releaseLock();
    }

    void addLock() throws IllegalStateException {
        synchronized (this) {
            if (stopped) {
                throw new IllegalStateException("er_print is scheduled to be stopped already!"); // NOI18N
            }
            locks.incrementAndGet();
            if (log.isLoggable(Level.FINEST)) {
                log.finest(logPrefix + "locks count == " + locks.toString()); // NOI18N
            }
        }
    }

    void releaseLock() {
        synchronized (this) {
            locks.decrementAndGet();
            if (log.isLoggable(Level.FINEST)) {
                log.finest(logPrefix + "locks count == " + locks.toString()); // NOI18N
            }
        }
    }

    void stop() {
        synchronized (this) {
            if (stopped) {
                return;
            }

            stopped = true;
        }

        DLightExecutorService.submit(new Runnable() {

            public void run() {
                if (log.isLoggable(Level.FINEST)) {
                    log.finest(logPrefix + "Scheduled for termination"); // NOI18N
                }
                int attempts = 30;

                while (locks.get() != 0 && --attempts > 0) {
                    try {
                        if (log.isLoggable(Level.FINEST)) {
                            log.finest(logPrefix + "waiting for lock release [" + locks.get() + "] ..."); // NOI18N
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        if (log.isLoggable(Level.FINEST)) {
                            log.log(Level.FINEST, logPrefix + "Exception while terminating", ex); // NOI18N
                        }
                        break;
                    }
                }

                if (log.isLoggable(Level.FINEST)) {
                    if (locks.get() > 0) {
                        log.finest(logPrefix + "do force termination"); // NOI18N
                    } else {
                        log.finest(logPrefix + "do termination"); // NOI18N
                    }
                }

                process.destroy();
            }
        }, "Stopping er_print " + logPrefix); // NOI18N
    }

    private void post(String cmd) throws IOException {
        in.write((cmd + "\n").getBytes()); // NOI18N
        in.flush();
    }

    public synchronized int setLimit(int limit) throws IOException {
        if (currentLimit == limit) {
            return currentLimit;
        }

        int prevLimit = currentLimit;
        exec("limit " + limit); // NOI18N
        currentLimit = limit;

        return prevLimit;
    }

    Metrics setMetrics(Metrics metrics) throws IOException {

        synchronized (this) {
            // Get current metrics ...
            String[] data = exec("metrics"); // NOI18N

            if (data == null || data.length != 2) {
                return null;
            }

            Matcher specMatcher = specPattern.matcher(data[0]);
            Matcher sortMatcher = sortPattern.matcher(data[1]);
            Metrics prevMetrics = null;

            if (specMatcher.matches() && sortMatcher.matches()) {
                prevMetrics = new Metrics(specMatcher.group(1), sortMatcher.group(1));
            }

            // Set new metrics (ignore output)
            exec("metrics " + metrics.mspec); // NOI18N
            exec("sort " + metrics.msort); // NOI18N

            return prevMetrics;
        }
    }

    String[] getHotFunctions(String command, int limit) throws IOException {
        String[] stat = exec(command);
        ArrayList<String> result = new ArrayList<String>();

        for (String str : stat) {
            if (str.matches("^ *[0-9]+.*")) { // NOI18N
                result.add(str.trim());
                if (--limit == 0) {
                    break;
                }
            }
        }

        return result.toArray(new String[0]);
    }

    String[] getHotFunctions(int limit) throws IOException {
        return getHotFunctions("functions", limit); // NOI18N
    }

    ExperimentStatistics getExperimentStatistics() throws IOException {
        String[] stat = exec("statistics"); // NOI18N
        return new ExperimentStatistics(stat);
    }

    ThreadsStatistic getThreadsStatistics() throws IOException {
        exec("threads"); // NOI18N
        String[] toParse = exec("thread_list"); // NOI18N
        return new ThreadsStatistic(toParse);
    }

    void selectObjects(CollectedObjectsFilter collectedObjectsFilter) throws IOException {
        if (stopped || collectedObjectsFilter == null) {
            return;
        }

        Pattern objs_pattern = Pattern.compile(".* <(.*)>.*"); // NOI18N
        StringBuilder object_select = new StringBuilder();

        synchronized (this) {
            String[] objects = exec("object_list"); // NOI18N

            Collection<String> selectedObjects = collectedObjectsFilter.selectedObjects();
            Collection<String> hiddenObjects = collectedObjectsFilter.hiddenObjects();

            hiddenObjects.add("libcollector.so"); // NOI18N
            hiddenObjects.add("er_heap.so"); // NOI18N
            hiddenObjects.add("er_sync.so"); // NOI18N
            hiddenObjects.add("Unknown"); // NOI18N

            for (String o : objects) {
                Matcher m = objs_pattern.matcher(o);
                if (m.matches()) {
                    String object = m.group(1);
                    if (selectedObjects.contains(object) ||
                            !hiddenObjects.contains(object)) {
                        object_select.append(object).append(","); // NOI18N
                    }
                }
            }


            if (object_select.length() != 0) {
                exec("object_select " + object_select.toString()); // NOI18N
            }
        }
    }

    LeaksStatistics getExperimentLeaks() throws IOException {
        String[] stat = exec("leaks"); // NOI18N
        return new LeaksStatistics(stat);
    }

    List<DataraceImpl> getDataRaces() throws IOException {
        String[] races = exec("rdetail all"); // NOI18N
        return DataraceImpl.fromErprint(races);
    }

    List<DeadlockImpl> getDeadlocks() throws IOException {
        String[] deadlocks = exec("ddetail all"); // NOI18N
        return DeadlockImpl.fromErprint(deadlocks);
    }

    FunctionStatistic getFunctionStatistic(String functionName) throws IOException {
        String[] stat = exec("fsingle \"" + functionName + "\" 1"); // NOI18N
        return new FunctionStatistic(stat);
    }

    FunctionStatistic getFunctionStatistic(FunctionCall functionCall) throws IOException {
        synchronized (this) {
            if (stopped) {
                return new FunctionStatistic(new String[0]);
            }

            String functionName = functionCall.getFunction().getName();
            String[] stat = exec("fsingle \"" + functionName + "\""); // NOI18N

            if (stat != null && stat.length > 0 && choiceMarker.equals(stat[0])) { // NOI18N
                String choice = "1"; // NOI18N

                FunctionCallImpl fci = (functionCall instanceof FunctionCallImpl)
                        ? (FunctionCallImpl) functionCall : null;

                String fname = (fci == null) ? null : fci.getFileName();

                if (fname != null) {
                    for (String line : stat) {
                        Matcher m = choicePattern.matcher(line);
                        String cfname;
                        if (m.matches()) {
                            choice = m.group(1);
                            cfname = m.group(2);

                            if (cfname.endsWith(fname)) {
                                break;
                            }
                        }
                    }
                }

                post(choice);
                stat = outProcessor.getOutput();
            }

            return new FunctionStatistic(stat);
        }
    }

    private String[] exec(String command) throws IOException {
        synchronized (this) {
            if (stopped) {
                return new String[0];
            }

            long startTime = System.currentTimeMillis();

            try {
                if (log.isLoggable(Level.FINEST)) {
                    log.finest("> " + command + "'"); // NOI18N
                }
                post(command);
            } catch (IOException ex) {
                stop();
                return new String[0];
            }

            String[] output = outProcessor.getOutput();

            if (log.isLoggable(Level.FINEST)) {
                log.finest("Command '" + command + "' done in " + // NOI18N
                    (System.currentTimeMillis() - startTime) / 1000 +
                    " secs. Response is " + output.length + " lines."); // NOI18N
            }

            return output;
        }
    }

    private class OutputProcessor {

        private final ArrayList<String> resultBuffer = new ArrayList<String>();
        private final StringBuilder lineBuffer = new StringBuilder();
        private final char[] prompt;
        private final char[] enterSelectionPrompt = "Enter selection: ".toCharArray(); // NOI18N
        private final InputStream pis;

        public OutputProcessor() throws IOException {
            pis = process.getInputStream();
            prompt = getPrompt();
        }

        public String[] getOutput() throws IOException {
            int promptPos = 0;
            int enterSelectionPromptPos = 0;

            resultBuffer.clear();
            lineBuffer.setLength(0);

            while (true) {
                int c;
                c = pis.read();

                if (c < 0) {
                    break;
                }

                if (c == '\n') {
                    resultBuffer.add(lineBuffer.toString());
                    lineBuffer.setLength(0);
                } else {
                    lineBuffer.append((char) c);
                }

                if (promptPos == prompt.length) {
                    break;
                }

                if (prompt[promptPos] == c) {
                    promptPos++;
                    if (promptPos == prompt.length) {
                        break;
                    }
                } else {
                    promptPos = 0;
                }

                if (enterSelectionPrompt[enterSelectionPromptPos] == c) {
                    enterSelectionPromptPos++;
                    if (enterSelectionPromptPos == enterSelectionPrompt.length) {
                        break;
                    }
                } else {
                    enterSelectionPromptPos = 0;
                }
            }

            return resultBuffer.toArray(new String[0]);
        }

        private char[] getPrompt() throws IOException {
            int currPos = 0;
            int pos1 = 0;
            int pos2 = 0;
            int c;
            char[] parray = new char[256];
            char[] result = null;

            // Read first char before perform post...
            // in other case er_print can jusr 'skip' this request...

            try {
                parray[0] = (char) out.read();

                post(""); // NOI18N

                while (true) {
                    c = out.read();

                    if (c < 0) {
                        break;
                    }

                    parray[++currPos] = (char) c;

                    if (parray[pos1] == parray[currPos]) {
                        if (pos2 == 0) {
                            pos2 = currPos;
                        }

                        if (++pos1 == pos2) {
                            break;
                        }
                    } else {
                        pos2 = 0;
                        pos1 = 0;
                    }
                }

                result = new char[pos1];
                System.arraycopy(parray, 0, result, 0, pos1);
            } catch (InterruptedIOException ex) {
                Thread.currentThread().interrupt();
                stop();
                result = "<Terminated>".toCharArray(); // NOI18N
            }

            return result;
        }
    }
}
