/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.api.server.output;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class WaitingLineProcessorTest extends NbTestCase {

    private static final String RELEASE_STRING = "test"; // NOI18N

    private static final long DEADLOCK_TIMEOUT = 1000;

    private static final int WAIT_THREAD_COUNT = 5;

    private static final int PRODUCER_THREAD_COUNT = 5;

    private static final long TEST_TIMEOUT = 5000;

    private ExecutorService pool;

    public WaitingLineProcessorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pool = Executors.newCachedThreadPool();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        pool.shutdownNow();
    }

    public void testLineProcessing() throws InterruptedException, BrokenBarrierException {
        WaitingLineProcessor lineProcessor = new WaitingLineProcessor(Pattern.compile(RELEASE_STRING));
        CyclicBarrier barrier = new CyclicBarrier(2);

        pool.execute(new WaitRunnable(lineProcessor, barrier));
        barrier.await();
        lineProcessor.processLine(RELEASE_STRING); // NOI18N

        try {
            barrier.await(DEADLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            fail("Deadlock occurs"); // NOI18N
        }

        pool.execute(new WaitRunnable(lineProcessor, barrier));
        barrier.await();
        try {
            barrier.await(DEADLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            fail("Deadlock occurs"); // NOI18N
        }
    }

    public void testThreadSafety() throws InterruptedException, BrokenBarrierException {
        final WaitingLineProcessor lineProcessor = new WaitingLineProcessor(Pattern.compile(RELEASE_STRING));
        CyclicBarrier barrier = new CyclicBarrier(WAIT_THREAD_COUNT + 1);

        for (int i = 0; i < WAIT_THREAD_COUNT; i++) {
            pool.execute(new WaitRunnable(lineProcessor, barrier));
        }

        barrier.await();

        Random random = new Random();
        for (int i = 0; i < PRODUCER_THREAD_COUNT; i++) {
            pool.execute(new ProducerRunnable(lineProcessor, RELEASE_STRING, random.nextInt(5)));
        }

        // guarantee finish
        pool.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(TEST_TIMEOUT);
                    lineProcessor.processLine(RELEASE_STRING);
                } catch (InterruptedException ex) {
                    //throw new RuntimeException(ex);
                }
            }
        });

        try {
            barrier.await(TEST_TIMEOUT + DEADLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            fail("Deadlock occurs"); // NOI18N
        }
    }

    private static class WaitRunnable implements Runnable {

        private final WaitingLineProcessor lineProcessor;

        private final CyclicBarrier barrier;

        public WaitRunnable(WaitingLineProcessor lineProcessor, CyclicBarrier barrier) {
            this.lineProcessor = lineProcessor;
            this.barrier = barrier;
        }

        public void run() {
            try {
                barrier.await();
                lineProcessor.await();
                barrier.await();
            } catch (InterruptedException ex) {
                // timeouted test
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException ex) {
                // timeouted test
            }
        }

    }

    private static class ProducerRunnable implements Runnable {

        private final WaitingLineProcessor lineProcessor;

        private final String releaseString;

        private final Random random = new Random();

        private final int iterations;

        public ProducerRunnable(WaitingLineProcessor lineProcessor, String releaseString, int iterations) {
            this.lineProcessor = lineProcessor;
            this.releaseString = releaseString;
            this.iterations = iterations;
        }

        public void run() {
            for (int i = 0; i < iterations; i++) {
                if (Thread.interrupted()) {
                    return;
                }

                int val = random.nextInt(10);
                if (val == 0) {
                    lineProcessor.processLine(releaseString);
                    return;
                } else {
                    lineProcessor.processLine("generated " + val);
                }

                try {
                    Thread.sleep(random.nextInt(300));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
