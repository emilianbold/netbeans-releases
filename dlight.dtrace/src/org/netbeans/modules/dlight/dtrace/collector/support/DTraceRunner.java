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
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessExecutionService;

/**
 * Helper class for running DTrace.
 * Keeps DTrace alive while it has something to say.
 *
 * @author Alexey Vladykin
 */
public final class DTraceRunner implements Runnable {

    private static final String EOF_MARKER = "__DTRACE_EOF_MARKER__"; // NOI18N
    private static final int SMALL_TIMEOUT = 10;
    private static final int BIG_TIMEOUT = 30;

    private final Future<Integer> dtraceTask;
    private final AtomicBoolean dataFlag;
    private final AtomicBoolean eofFlag;
    private final AtomicBoolean shutdownFlag;

    public DTraceRunner(ExecutionEnvironment execEnv, String command, LineProcessor outProcessor) {
        this.dataFlag = new AtomicBoolean();
        this.eofFlag = new AtomicBoolean();
        this.shutdownFlag = new AtomicBoolean();

        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setCommandLine(command);

        LineProcessorWrapper wrappedOutProcessor = new LineProcessorWrapper(outProcessor);
        this.dtraceTask = NativeProcessExecutionService.newService(
                npb, wrappedOutProcessor, null, "DTrace task").start(); // NOI18N

        DLightExecutorService.submit(this, "Monitoring DTrace task"); // NOI18N
    }

    public static void preprocessDTraceScript(File script) throws IOException {
        DTraceScriptUtils.appendToScript(script, "END { printf(\"" + EOF_MARKER + "\"); }\n"); // NOI18N
    }

    public void shutdown() {
        shutdownFlag.set(true);
    }

    @Override
    public void run() {
        while (true) {
            //System.out.println("In monitoring loop");
            if (dtraceTask.isDone()) {
                break;
            }

            if (eofFlag.get()) {
                shutdownImmediately();
                break;
            }

            if (shutdownFlag.get()) {
                shutdownGracefully();
                break;
            }

            if (!sleepOneSecond()) {
                shutdownImmediately();
                break;
            }
        }
    }

    private void shutdownGracefully() {
        //System.out.println("Shutting down gracefully");

        int ticksWithoutData = 0;
        for (int i = 0; i < BIG_TIMEOUT; ++i) {
            if (dtraceTask.isDone()) {
                return;
            }

            if (!sleepOneSecond() || eofFlag.get()) {
                break;
            }

            if (dataFlag.getAndSet(false)) {
                //System.out.println("Got data");
                ticksWithoutData = 0;
            } else {
                ++ticksWithoutData;
                //System.out.println(ticksWithoutData + "s without data");
                if (SMALL_TIMEOUT <= ticksWithoutData) {
                    break;
                }
            }
        }

        shutdownImmediately();
    }

    private void shutdownImmediately() {
        //System.out.println("Shutting down immediately");
        dtraceTask.cancel(true);
    }

    @Override
    public String toString() {
        return dtraceTask.toString();
    }

    private static boolean sleepOneSecond() {
        try {
            Thread.sleep(1000);
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    private class LineProcessorWrapper implements LineProcessor {

        private final LineProcessor originalProcessor;

        public LineProcessorWrapper(LineProcessor originalProcessor) {
            this.originalProcessor = originalProcessor;
        }

        public AtomicBoolean getDataFlag() {
            return dataFlag;
        }

        public AtomicBoolean getEOFFlag() {
            return eofFlag;
        }

        @Override
        public void processLine(String line) {
            if (EOF_MARKER.equals(line)) {
                //System.out.println("EOF flag");
                eofFlag.set(true);
            } else {
                dataFlag.set(true);
                originalProcessor.processLine(line);
            }
        }

        @Override
        public void reset() {
            originalProcessor.reset();
        }

        @Override
        public void close() {
            originalProcessor.close();
        }
    }
}
