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
package org.netbeans.modules.nativeexecution;

import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport.Counters;
import org.netbeans.modules.nativeexecution.ConcurrentTasksSupport.TaskFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.openide.util.Exceptions;
import static org.junit.Assert.*;

/**
 *
 * @author ak119685
 */
public class LocalNativeProcessTest extends NativeExecutionBaseTestCase {

    private ExecutionEnvironment execEnv;

    public LocalNativeProcessTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        execEnv = ExecutionEnvironmentFactory.getLocal();
//        execEnv = getTestExecutionEnvironment();
//        try {
//            ConnectionManager.getInstance().connectTo(execEnv, "".toCharArray(), false);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (CancellationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Following test starts <tt>count</tt> infinite processes in parallel.
     * After that it tries to destroy all of them (also in concurrent mode).
     * Test assures that exactly <tt>count</tt> tests were started, killed and
     * destroyed.
     */
    @Test
    public void testDestroyInfiniteTasks() throws Exception {
        final BlockingQueue<NativeProcess> processQueue = new LinkedBlockingQueue<NativeProcess>();
        final Counters counters = new Counters();
        int count = 50;

        final TaskFactory infiniteTaskFactory = new TaskFactory() {

            @Override
            public Runnable newTask() {
                return new InfiniteTask(execEnv, counters, processQueue);
            }
        };

        performDestroyTest(execEnv, count, infiniteTaskFactory, counters, processQueue);

        counters.dump(System.err);

        assertEquals(count, counters.getCounter("Started").get()); // NOI18N
        assertEquals(count, counters.getCounter("Killed").get()); // NOI18N
        assertEquals(count, counters.getCounter("InterruptedException").get() + counters.getCounter("Finished").get()); // NOI18N
    }

    @Test
    public void testExecAndWaitTasks() throws Exception {
        final BlockingQueue<NativeProcess> processQueue = new LinkedBlockingQueue<NativeProcess>();
        final Counters counters = new Counters();
        int count = 5;

        final TaskFactory shortTasksFactory = new TaskFactory() {

            @Override
            public Runnable newTask() {
                return new ShortTask(execEnv, counters, processQueue);
            }
        };
        final TaskFactory longTasksFactory = new TaskFactory() {

            @Override
            public Runnable newTask() {
                return new LongTask(execEnv, counters, processQueue);
            }
        };

        ConcurrentTasksSupport startSupport = new ConcurrentTasksSupport(count);
        startSupport.addFactory(shortTasksFactory);
        startSupport.addFactory(longTasksFactory);
        startSupport.init();
        startSupport.start();
        startSupport.waitCompletion();

        counters.dump(System.err);

        assertEquals(count, counters.getCounter("Started").get()); // NOI18N
        assertEquals(count, counters.getCounter("Done").get()); // NOI18N
        assertEquals(count, counters.getCounter("CorrectOutput").get()); // NOI18N
    }

    public void performDestroyTest(
            final ExecutionEnvironment execEnv,
            int count,
            final TaskFactory factory,
            final Counters counters,
            final BlockingQueue<NativeProcess> processQueue) throws Exception {

        final TaskFactory killTaskFactory = new TaskFactory() {

            @Override
            public Runnable newTask() {
                return new Runnable() {

                    final Random r = new Random();

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(r.nextInt(5000));
                            NativeProcess p = processQueue.take();
                            int pid = -1;

                            try {
                                pid = p.getPID();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            assertTrue(pid > 0);

                            // Make sure process exists...
                            // Do not perform this test on Windows...
                            boolean isWindows = false;

                            try {
                                isWindows = HostInfoUtils.getHostInfo(execEnv).getOSFamily() == HostInfo.OSFamily.WINDOWS;
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (CancellationException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            if (!isWindows) {
                                try {
                                    int result = CommonTasksSupport.sendSignal(execEnv, pid, Signal.NULL, null).get();
                                    assertTrue(result == 0);
                                } catch (InterruptedException ex) {
                                    System.out.println("kill interrupted..."); // NOI18N
                                } catch (ExecutionException ex) {
                                    Exceptions.printStackTrace(ex);
                                    fail();
                                }
                            }

                            System.out.println("Kill process " + pid); // NOI18N
                            p.destroy();

                            // Make sure process doesn't exist...
                            // Again, skip Windows

                            if (!isWindows) {
                                int result = -1;
                                try {
                                    result = CommonTasksSupport.sendSignal(execEnv, pid, Signal.NULL, null).get();
                                } catch (ExecutionException ex) {
                                    Exceptions.printStackTrace(ex);
                                    fail();
                                }
                                assertTrue(result != 0);
                            }

                            counters.getCounter("Killed").incrementAndGet(); // NOI18N

                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
            }
        };

        ConcurrentTasksSupport startSupport = new ConcurrentTasksSupport(count);
        ConcurrentTasksSupport killSupport = new ConcurrentTasksSupport(count);

        startSupport.addFactory(factory);
        killSupport.addFactory(killTaskFactory);

        startSupport.init();
        killSupport.init();

        startSupport.start();
        killSupport.start();

        startSupport.waitCompletion();
        killSupport.waitCompletion();

    }

    private class ShortTask implements Runnable {

        private final String expectedOutput = "test passed"; // NOI18N
        private final Counters counters;
        private final BlockingQueue<NativeProcess> pqueue;
        private final NativeProcessBuilder npb;

        public ShortTask(ExecutionEnvironment execEnv, Counters counters, BlockingQueue<NativeProcess> pqueue) {
            this.counters = counters;
            this.pqueue = pqueue;
            npb = NativeProcessBuilder.newLocalProcessBuilder();
            npb.setExecutable("echo").setArguments(expectedOutput); // NOI18N
        }

        @Override
        public void run() {
            try {
                NativeProcess p = npb.call();
                pqueue.put(p);
                System.out.println("Short Process started: " + p.getPID()); // NOI18N
                counters.getCounter("Started").incrementAndGet(); // NOI18N
                System.out.println("Process done. Result is: " + p.waitFor()); // NOI18N
                counters.getCounter("Done").incrementAndGet(); // NOI18N
                if (expectedOutput.equals(ProcessUtils.readProcessOutputLine(p))) { // NOI18N
                    counters.getCounter("CorrectOutput").incrementAndGet(); // NOI18N
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                counters.getCounter("InterruptedException").incrementAndGet(); // NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                counters.getCounter("IOException").incrementAndGet(); // NOI18N
            }

        }
    };

    private class LongTask implements Runnable {

        private final Counters counters;
        private final ExecutionEnvironment execEnv;
        private final BlockingQueue<NativeProcess> pqueue;
        private final NativeProcessBuilder npb;

        public LongTask(ExecutionEnvironment execEnv, Counters counters, BlockingQueue<NativeProcess> pqueue) {
            this.execEnv = execEnv;
            this.counters = counters;
            this.pqueue = pqueue;
            npb = NativeProcessBuilder.newLocalProcessBuilder();
            npb.setExecutable("sleep").setArguments("3"); // NOI18N
        }

        @Override
        public void run() {
            try {
                NativeProcess p = npb.call();
                pqueue.put(p);
                System.out.println("Long Process started: " + p.getPID()); // NOI18N
                counters.getCounter("Started").incrementAndGet(); // NOI18N
                int result = p.waitFor();
                System.out.println("Process done. Result is: " + result); // NOI18N
                counters.getCounter("Done").incrementAndGet(); // NOI18N
                assertTrue(result == 0);
                counters.getCounter("CorrectOutput").incrementAndGet(); // NOI18N
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                counters.getCounter("InterruptedException").incrementAndGet(); // NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                counters.getCounter("IOException").incrementAndGet(); // NOI18N
            }
        }
    }

    private class InfiniteTask implements Runnable {

        private final Counters counters;
        private final BlockingQueue<NativeProcess> pqueue;
        private final ExecutionEnvironment execEnv;
        private final NativeProcessBuilder npb;

        public InfiniteTask(ExecutionEnvironment execEnv, Counters counters, BlockingQueue<NativeProcess> pqueue) {
            this.execEnv = execEnv;
            this.counters = counters;
            this.pqueue = pqueue;
            HostInfo info = null;

            try {
                info = HostInfoUtils.getHostInfo(execEnv);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                Exceptions.printStackTrace(ex);
            }

            npb = NativeProcessBuilder.newProcessBuilder(execEnv);

            if (info == null || info.getOSFamily() != HostInfo.OSFamily.WINDOWS) {
                npb.setExecutable("sleep").setArguments("10000"); // NOI18N
            } else {
                npb.setExecutable("cmd").setArguments("/C", "pause"); // NOI18N
            }
        }

        @Override
        public void run() {
            try {
                NativeProcess p = npb.call();
                pqueue.put(p);
                System.out.println("Process started: " + p.getPID()); // NOI18N
                counters.getCounter("Started").incrementAndGet(); // NOI18N
                System.out.println("Process done. Result is: " + p.waitFor()); // NOI18N
                counters.getCounter("Finished").incrementAndGet(); // NOI18N
            } catch (InterruptedException ex) {
                counters.getCounter("InterruptedException").incrementAndGet(); // NOI18N
            } catch (InterruptedIOException ex) {
                counters.getCounter("InterruptedIOException").incrementAndGet(); // NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                counters.getCounter("IOException").incrementAndGet(); // NOI18N
            } finally {
                counters.getCounter("Done").incrementAndGet(); // NOI18N
            }
        }
    }
}
