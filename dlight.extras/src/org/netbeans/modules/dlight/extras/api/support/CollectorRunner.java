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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.extras.api.support;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.dlight.spi.indicator.IndicatorNotificationsListener;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.NativeProcessExecutionService;

/**
 * Helper class for running collector tasks.
 * Keeps process alive while it has something to say.
 *
 * @author Alexey Vladykin
 */
public final class CollectorRunner implements Runnable {

    private static final int SMALL_TIMEOUT = 5;
    private static final int BIG_TIMEOUT = 30;

    private final IndicatorNotificationsListener listener;
    private final Future<Integer> collectorTask;
    private final AtomicBoolean dataFlag;
    private final AtomicBoolean eofFlag;
    private final AtomicBoolean shutdownFlag;
    private final String eofMarker;

    public CollectorRunner(IndicatorNotificationsListener listener, NativeProcessBuilder npb, LineProcessor outProcessor, String eofMarker, String taskName) {
        this.listener = listener;
        this.dataFlag = new AtomicBoolean();
        this.eofFlag = new AtomicBoolean();
        this.shutdownFlag = new AtomicBoolean();
        this.eofMarker = eofMarker;

        LineProcessorWrapper wrappedOutProcessor = new LineProcessorWrapper(outProcessor);
        this.collectorTask = NativeProcessExecutionService.newService(
                npb, wrappedOutProcessor, null, taskName).start(); // NOI18N

        DLightExecutorService.submit(this, "Monitoring task " + taskName); // NOI18N
    }

    public void shutdown() {
        shutdownFlag.set(true);
    }

    @Override
    public void run() {
        while (true) {
            if (collectorTask.isDone()) {
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
        int ticksWithoutData = 0;
        for (int i = 0; i < BIG_TIMEOUT; ++i) {
            if (collectorTask.isDone()) {
                return;
            }

            if (eofFlag.get() || !sleepOneSecond()) {
                break;
            }

            listener.suggestRepaint();

            if (dataFlag.getAndSet(false)) {
                //System.out.println("Got data");
                ticksWithoutData = 0;
            } else {
                ++ticksWithoutData;
                if (SMALL_TIMEOUT <= ticksWithoutData) {
                    break;
                }
            }
        }

        shutdownImmediately();
    }

    private void shutdownImmediately() {
        collectorTask.cancel(true);
    }

    @Override
    public String toString() {
        return collectorTask.toString();
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
            if (eofMarker.equals(line)) {
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
