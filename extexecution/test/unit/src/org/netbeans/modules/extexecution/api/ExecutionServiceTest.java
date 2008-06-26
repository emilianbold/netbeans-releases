/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.extexecution.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.extexecution.InputOutputManager;
import org.netbeans.modules.extexecution.api.input.TestInputUtils;

/**
 *
 * @author Petr Hejl
 */
public class ExecutionServiceTest extends NbTestCase {

    public ExecutionServiceTest(String name) {
        super(name);
    }

    @Override
    protected void tearDown() throws Exception {
        InputOutputManager.clear();
        super.tearDown();
    }


    public void testSimpleRun() throws InvocationTargetException, InterruptedException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable(process);

        ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
        ExecutionService service = ExecutionService.newService(
                callable, builder.create(), "Test");

        TestExecution execution = new TestExecution(service);
        SwingUtilities.invokeLater(execution);
        Future<Integer> task = execution.getTask();
        assertNotNull(task);
        assertFalse(process.isFinished());

        task.cancel(true);
        assertTrue(task.isCancelled());

        // maybe it didn't get to execution
        if (process.isStarted()) {
            process.waitFor();
            assertTrue(process.isFinished());
            assertEquals(0, process.exitValue());
        }

        process = new TestProcess(1);
        callable.setProcess(process);

        execution = new TestExecution(service);
        SwingUtilities.invokeLater(execution);
        task = execution.getTask();
        assertNotNull(task);
        assertFalse(process.isFinished());

        // we want to test real started process
        process.waitStarted();
        task.cancel(true);
        assertTrue(task.isCancelled());
        process.waitFor();
        assertTrue(process.isFinished());
        assertEquals(1, process.exitValue());
    }

    public void testHooks() throws InterruptedException, ExecutionException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable(process);

        class TestRunnable implements Runnable {

            public volatile boolean executed;

            public void run() {
                executed = true;
            }
        }

        TestRunnable preRunnable = new TestRunnable();
        TestRunnable postRunnable = new TestRunnable();

        ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
        builder.preExecution(preRunnable);
        builder.postExecution(postRunnable);

        ExecutionService service = ExecutionService.newService(
                callable, builder.create(), "Test");

        TestExecution execution = new TestExecution(service);
        SwingUtilities.invokeLater(execution);
        Future<Integer> task = execution.getTask();
        assertNotNull(task);

        process.waitStarted();
        assertTrue(preRunnable.executed);

        process.destroy();
        assertEquals(0, task.get().intValue());
        assertTrue(postRunnable.executed);
    }

    public void testIOHandling() throws InterruptedException, InvocationTargetException, ExecutionException {
        TestProcess process = new TestProcess(0);
        TestCallable callable = new TestCallable(process);

        ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
        ExecutionService service = ExecutionService.newService(
                callable, builder.create(), "Test");

        TestExecution execution = new TestExecution(service);
        SwingUtilities.invokeLater(execution);
        Future<Integer> task = execution.getTask();
        assertNotNull(task);

        assertNull(getInputOutput("Test", false));
        process.destroy();
        assertEquals(0, task.get().intValue());

        assertNotNull(getInputOutput("Test", false));

        // rerun once again
        process = new TestProcess(0);
        callable.setProcess(process);

        execution = new TestExecution(service);
        SwingUtilities.invokeLater(execution);
        task = execution.getTask();
        assertNotNull(task);

        assertNull(getInputOutput("Test", false));
        process.destroy();
        task.get();

        assertNotNull(getInputOutput("Test", false));
    }

    public void testIOHandlingMulti() throws InterruptedException, InvocationTargetException,
            ExecutionException {

        TestProcess process1 = new TestProcess(0);
        TestProcess process2 = new TestProcess(0);
        TestCallable callable = new TestCallable(process1);

        ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
        ExecutionService service = ExecutionService.newService(
                callable, builder.create(), "Test");

        TestExecution execution1 = new TestExecution(service);
        SwingUtilities.invokeLater(execution1);
        Future<Integer> task1 = execution1.getTask();
        assertNotNull(task1);

        assertNull(getInputOutput("Test", false));
        assertNull(getInputOutput("Test #2", false));

        process1.waitStarted();

        callable.setProcess(process2);

        TestExecution execution2 = new TestExecution(service);
        SwingUtilities.invokeLater(execution2);
        Future<Integer> task2 = execution2.getTask();
        assertNotNull(task2);

        assertNull(getInputOutput("Test", false));
        assertNull(getInputOutput("Test #2", false));

        process2.waitStarted();

        process1.destroy();
        process2.destroy();

        assertEquals(0, task1.get().intValue());
        assertEquals(0, task2.get().intValue());

        assertNotNull(getInputOutput("Test", false));
        assertNotNull(getInputOutput("Test #2", false));
    }

    public void testInvocationThread() {
        try {
            TestProcess process = new TestProcess(0);
            TestCallable callable = new TestCallable(process);

            ExecutionDescriptor.Builder builder = new ExecutionDescriptor.Builder();
            ExecutionService service = ExecutionService.newService(callable, builder.create(), "Test");

            Future<Integer> task = service.run();

            fail("Allows invocation outside of EDT");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    private static InputOutputManager.InputOutputData getInputOutput(String name, boolean actions) {
        synchronized (InputOutputManager.class) {
            InputOutputManager.InputOutputData data = InputOutputManager.getInputOutput(name, actions);
            // put it back
            if (data != null) {
                InputOutputManager.addInputOutput(data);
            }
            return data;
        }
    }

    private static class TestExecution implements Runnable {

        private final ExecutionService service;

        private Future<Integer> task;

        public TestExecution(ExecutionService service) {
            this.service = service;
        }

        public synchronized void run() {
            task = service.run();
            notifyAll();
        }

        public synchronized Future<Integer> getTask() throws InterruptedException {
            while (task == null) {
                wait();
            }
            return task;
        }
    }

    private static class TestCallable implements Callable<Process> {

        private TestProcess process;

        public TestCallable(TestProcess process) {
            this.process = process;
        }

        public synchronized void setProcess(TestProcess process) {
            this.process = process;
        }

        public synchronized Process call() throws Exception {
            if (process == null) {
                throw new IllegalStateException("No process configured");
            }

            TestProcess ret = process;
            process = null;
            ret.start();

            return ret;
        }
    }

    private static class TestProcess extends Process {

        private final int returnValue;

        private boolean finished;

        private boolean started;

        public TestProcess(int returnValue) {
            this.returnValue = returnValue;
        }

        public void start() {
            synchronized (this) {
                started = true;
                notifyAll();
            }
        }

        public boolean isStarted() {
            synchronized (this) {
                return started;
            }
        }

        public boolean isFinished() {
            synchronized (this) {
                return finished;
            }
        }

        @Override
        public void destroy() {
            synchronized (this) {
                if (finished) {
                    return;
                }

                finished = true;
                notifyAll();
            }
        }

        @Override
        public int exitValue() {
            synchronized (this) {
                if (!finished) {
                    throw new IllegalStateException("Not finished yet");
                }
            }
            return returnValue;
        }

        @Override
        public InputStream getErrorStream() {
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return -1;
                }
            };
        }

        @Override
        public InputStream getInputStream() {
            return TestInputUtils.prepareInputStream(
                    new String[] {"Process line 1", "Process line 2", "Process line 3"},
                    "\n", Charset.forName("UTF-8"), true);
        }

        @Override
        public OutputStream getOutputStream() {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    // throw it away
                }
            };
        }

        @Override
        public int waitFor() throws InterruptedException {
            synchronized (this) {
                while (!finished) {
                    wait();
                }
            }
            return returnValue;
        }

        public void waitStarted() throws InterruptedException {
            synchronized (this) {
                while (!started) {
                    wait();
                }
            }
        }
    }
}
