/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.extexecution;

import org.netbeans.modules.extexecution.InputOutputManager;
import org.netbeans.modules.extexecution.StopAction;
import org.netbeans.modules.extexecution.RerunAction;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.extexecution.ProcessInputStream;
import org.netbeans.api.extexecution.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.api.extexecution.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.InputReaderTask;
import org.netbeans.api.extexecution.input.InputReaders;
import org.netbeans.api.extexecution.input.LineProcessors;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Execution service provides the facility to execute the process while
 * displaying the output and handling the input.
 * <p>
 * It will execute the program with an associated I/O window, with stop and
 * restart buttons. It will also obey various descriptor properties such as
 * whether or not to show a progress bar.
 * <p>
 * All processes launched by this class are terminated on VM exit (if
 * these are not finished or terminated earlier).
 * <p>
 * Note that once service is run for the first time, subsequents runs can be
 * invoked by the user (rerun button) if it is allowed to do so
 * ({@link ExecutionDescriptor#isControllable()}).
 *
 * <div class="nonnormative">
 * <p>
 * Sample usage - executing ls command:
 * <pre>
 *     ExecutionDescriptor descriptor = new ExecutionDescriptor()
 *          .frontWindow(true).controllable(true);
 *
 *     ExternalProcessBuilder processBuilder = new ExternalProcessBuilder("ls");
 *
 *     ExecutionService service = ExecutionService.newService(processBuilder, descriptor, "ls command");
 *     Future&lt;Integer&gt task = service.run();
 * </pre>
 * </div>
 *
 * @author Petr Hejl
 * @see #newService(java.util.concurrent.Callable, org.netbeans.api.extexecution.ExecutionDescriptor, java.lang.String)
 * @see ExecutionDescriptor
 */
public final class ExecutionService {

    private static final Logger LOGGER = Logger.getLogger(ExecutionService.class.getName());

    private static final Set<Process> RUNNING_PROCESSES = new HashSet<Process>();

    private static final int EXECUTOR_SHUTDOWN_SLICE = 1000;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    static {
        // rerun accessor
        RerunAction.Accessor.setDefault(new RerunAction.Accessor() {

            @Override
            public Future<Integer> run(ExecutionService service, InputOutput required) {
                return service.run(required);
            }
        });

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                EXECUTOR_SERVICE.shutdown();

                synchronized (RUNNING_PROCESSES) {
                    for (Process process : RUNNING_PROCESSES) {
                        process.destroy();
                    }
                }
            }
        });
    }

    private final Callable<Process> processCreator;

    private final ExecutionDescriptor descriptor;

    private final String originalDisplayName;

    private ExecutionService(Callable<Process> processCreator, String displayName, ExecutionDescriptor descriptor) {
        this.processCreator = processCreator;
        this.originalDisplayName = displayName;
        this.descriptor = descriptor;
    }

    /**
     * Creates new execution service. Service will wrap up the processes
     * created by <code>processCreator</code> and will manage them.
     *
     * @param processCreator callable returning the process to wrap up
     * @param descriptor descriptor describing the configuration of service
     * @param displayName display name of this service
     * @return new execution service
     */
    @NonNull
    public static ExecutionService newService(@NonNull Callable<Process> processCreator,
            @NonNull ExecutionDescriptor descriptor, @NonNull String displayName) {
        return new ExecutionService(processCreator, displayName, descriptor);
    }

    /**
     * Runs the process described by this service. The call does not block
     * and the task is represented by the returned value. Integer returned
     * as a result of the {@link Future} is exit code of the process.
     * <p>
     * The output tabs are reused (if caller does not use the custom one,
     * see {@link ExecutionDescriptor#getInputOutput()}) - the tab to reuse
     * (if any) is selected by having the same name and same buttons
     * (control and option). If there is no output tab to reuse new one
     * is opened.
     * <p>
     * This method can be invoked multiple times returning the different and
     * unrelated {@link Future}s. On each call <code>Callable&lt;Process&gt;</code>
     * passed to {@link #newService(java.util.concurrent.Callable, org.netbeans.api.extexecution.ExecutionDescriptor, java.lang.String)}
     * is invoked in order to create the process. If the process creation fails
     * (throwing an exception) returned <code>Future</code> will throw
     * {@link java.util.concurrent.ExecutionException} on {@link Future#get()}
     * request.
     * <p>
     * For details on execution control see {@link ExecutionDescriptor}.
     *
     * @return task representing the actual run, value representing result
     *             of the {@link Future} is exit code of the process
     */
    @NonNull
    public Future<Integer> run() {
        return run(null);
    }

    private Future<Integer> run(InputOutput required) {
        final InputOutputManager.InputOutputData ioData = getInputOutput(required);

        final String displayName = ioData.getDisplayName();
        final ProgressCancellable cancellable = descriptor.isControllable() ? new ProgressCancellable() : null;
        final ProgressHandle handle = createProgressHandle(ioData.getInputOutput(), displayName, cancellable);
        final InputOutput io = ioData.getInputOutput();

        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final Reader in = io.getIn();

        final CountDownLatch finishedLatch = new CountDownLatch(1);

        class ExecutedHolder {
            private boolean executed = false;
        }
        final ExecutedHolder executed = new ExecutedHolder();

        Callable<Integer> callable = new Callable<Integer>() {
            public Integer call() throws Exception {

                boolean interrupted = false;
                Process process = null;
                Integer ret = null;
                ExecutorService executor = null;

                ProcessInputStream outStream = null;
                ProcessInputStream errStream = null;

                List<InputReaderTask> tasks = new ArrayList<InputReaderTask>();

                try {
                    executed.executed = true;
                    final Runnable pre = descriptor.getPreExecution();
                    if (pre != null) {
                        pre.run();
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }

                    process = processCreator.call();
                    synchronized (RUNNING_PROCESSES) {
                        RUNNING_PROCESSES.add(process);
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }

                    outStream = new ProcessInputStream(process, process.getInputStream());
                    errStream = new ProcessInputStream(process, process.getErrorStream());

                    executor = Executors.newFixedThreadPool(descriptor.isInputVisible() ? 3 : 2);

                    Charset charset = descriptor.getCharset();
                    if (charset == null) {
                        charset = Charset.defaultCharset();
                    }

                    tasks.add(InputReaderTask.newDrainingTask(
                        InputReaders.forStream(new BufferedInputStream(outStream), charset),
                        createOutProcessor(out)));
                    tasks.add(InputReaderTask.newDrainingTask(
                        InputReaders.forStream(new BufferedInputStream(errStream), charset),
                        createErrProcessor(err)));
                    if (descriptor.isInputVisible()) {
                        tasks.add(InputReaderTask.newTask(
                            InputReaders.forReader(in),
                            createInProcessor(process.getOutputStream())));
                    }
                    for (InputReaderTask task : tasks) {
                        executor.submit(task);
                    }

                    process.waitFor();
                } catch (InterruptedException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                    interrupted = true;
                } catch (Throwable t) {
                    LOGGER.log(Level.INFO, null, t);
                    throw new WrappedException(t);
                } finally {
                    try {
                        // fully evaluated - we want to clear interrupted status in any case
                        interrupted = interrupted | Thread.interrupted();

                        if (!interrupted) {
                            if (outStream != null) {
                                outStream.close(true);
                            }
                            if (errStream != null) {
                                errStream.close(true);
                            }
                        }

                        if (process != null) {
                            process.destroy();
                            synchronized (RUNNING_PROCESSES) {
                                RUNNING_PROCESSES.remove(process);
                            }

                            try {
                                return process.exitValue();
                            } catch (IllegalThreadStateException ex) {
                                LOGGER.log(Level.FINE, "Process not yet exited", ex);
                            }
                        }
                    } catch (Throwable t) {
                        LOGGER.log(Level.INFO, null, t);
                        throw new WrappedException(t);
                    } finally {
                        try {
                            cleanup(tasks, executor, handle, ioData,
                                    ioData.getInputOutput() != descriptor.getInputOutput());

                            final Runnable post = descriptor.getPostExecution();
                            if (post != null) {
                                post.run();
                            }
                        } finally {
                            finishedLatch.countDown();
                            if (interrupted) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }

                return ret;
            }
        };

        final FutureTask<Integer> current = new FutureTask<Integer>(callable) {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean ret = super.cancel(mayInterruptIfRunning);
                if (!executed.executed) {
                    cleanup(handle, ioData);

                    synchronized (InputOutputManager.class) {
                        if (ioData.getInputOutput() != descriptor.getInputOutput()) {
                            InputOutputManager.addInputOutput(ioData);
                        }
                    }
                } else {
// uncomment this if state after cancel should be the same as when completed normally
//                    if (ret && mayInterruptIfRunning && executed.executed) {
//                        try {
//                            latch.await();
//                        } catch (InterruptedException ex) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }
                }
                return ret;
            }

            @Override
            protected void setException(Throwable t) {
                if (t instanceof WrappedException) {
                    super.setException(((WrappedException) t).getCause());
                } else {
                    super.setException(t);
                }
            }

        };

        // TODO cleanup
        final StopAction workingStopAction = ioData.getStopAction();
        final RerunAction workingRerunAction = ioData.getRerunAction();

        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                if (workingStopAction != null) {
                    synchronized (workingStopAction) {
                        workingStopAction.setTask(current);
                        workingStopAction.setEnabled(true);
                    }
                }

                if (workingRerunAction != null) {
                    synchronized (workingRerunAction) {
                        workingRerunAction.setExecutionService(ExecutionService.this);
                        workingRerunAction.setRerunCondition(descriptor.getRerunCondition());
                        workingRerunAction.setEnabled(false);
                    }
                }
            }
        });

        if (cancellable != null) {
            cancellable.setTask(current);
        }

        EXECUTOR_SERVICE.execute(current);
        return current;
    }

    /**
     * Retrives or creates the output window usable for the current run.
     *
     * @param required output window required by rerun or <code>null</code>
     * @return the output window usable for the current run
     */
    private InputOutputManager.InputOutputData getInputOutput(InputOutput required) {
        InputOutputManager.InputOutputData result = null;

        synchronized (InputOutputManager.class) {
            InputOutput io = descriptor.getInputOutput();
            if (io != null) {
                result = new InputOutputManager.InputOutputData(io,
                        originalDisplayName, null, null, null);
            }

            // try to acquire required one (rerun action)
            // this will always succeed if this method is called from EDT
            if (result == null) {
                result = InputOutputManager.getInputOutput(required);
            }

            // try to find free output windows
            if (result == null) {
                result = InputOutputManager.getInputOutput(
                        originalDisplayName, descriptor.isControllable(), descriptor.getOptionsPath());
            }

            // free IO was not found, create new one
            if (result == null) {
                result = InputOutputManager.createInputOutput(
                        originalDisplayName, descriptor.isControllable(), descriptor.getOptionsPath());
            }

            configureInputOutput(result.getInputOutput());
        }

        return result;
    }

    /**
     * Configures the output window before usage.
     *
     * @param inputOutput output window to configure
     */
    private void configureInputOutput(InputOutput inputOutput) {
        if (inputOutput == InputOutput.NULL) {
            return;
        }

        if (descriptor.getInputOutput() == null || !descriptor.noReset()) {
            try {
                inputOutput.getOut().reset();
            } catch (IOException exc) {
                LOGGER.log(Level.INFO, null, exc);
            }

            // Note - do this AFTER the reset() call above; if not, weird bugs occur
            inputOutput.setErrSeparated(false);
        }

        // Open I/O window now. This should probably be configurable.
        if (descriptor.isFrontWindow()) {
            inputOutput.select();
        }

        inputOutput.setInputVisible(descriptor.isInputVisible());
    }

    private ProgressHandle createProgressHandle(InputOutput inputOutput,
            String displayName, Cancellable cancellable) {

        if (!descriptor.showProgress() && !descriptor.showSuspended()) {
            return null;
        }

        ProgressHandle handle = ProgressHandleFactory.createHandle(displayName,
                cancellable, new ProgressAction(inputOutput));

        handle.setInitialDelay(0);
        handle.start();
        handle.switchToIndeterminate();

        if (descriptor.showSuspended()) {
            handle.suspend(NbBundle.getMessage(ExecutionService.class, "Running"));
        }

        return handle;
    }

    private void cleanup(final List<InputReaderTask> tasks, final ExecutorService processingExecutor,
            final ProgressHandle progressHandle, final InputOutputManager.InputOutputData inputOutputData,
            final boolean managed) {

        boolean interrupted = false;
        if (processingExecutor != null) {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        processingExecutor.shutdown();
                        return null;
                    }
                });
                for (Cancellable cancellable : tasks) {
                    cancellable.cancel();
                }
                while (!processingExecutor.awaitTermination(EXECUTOR_SHUTDOWN_SLICE, TimeUnit.MILLISECONDS)) {
                    LOGGER.log(Level.INFO, "Awaiting processing finish");
                }
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }

        cleanup(progressHandle, inputOutputData);

        synchronized (InputOutputManager.class) {
            if (managed) {
                InputOutputManager.addInputOutput(inputOutputData);
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void cleanup(final ProgressHandle progressHandle, final InputOutputManager.InputOutputData inputOutputData) {
        Runnable ui = new Runnable() {
            public void run() {
                if (inputOutputData.getStopAction() != null) {
                    inputOutputData.getStopAction().setEnabled(false);
                }
                if (inputOutputData.getRerunAction() != null) {
                    inputOutputData.getRerunAction().setEnabled(true);
                }
                if (progressHandle != null) {
                    progressHandle.finish();
                }
            }
        };

        Mutex.EVENT.readAccess(ui);
    }

    private InputProcessor createOutProcessor(OutputWriter writer) {
        LineConvertorFactory convertorFactory = descriptor.getOutConvertorFactory();
        InputProcessor outProcessor = null;
        if (descriptor.isOutLineBased()) {
            outProcessor = InputProcessors.bridge(LineProcessors.printing(writer,
                    convertorFactory != null ? convertorFactory.newLineConvertor() : null, true));
        } else {
            outProcessor = InputProcessors.printing(writer,
                    convertorFactory != null ? convertorFactory.newLineConvertor() : null, true);
        }

        InputProcessorFactory descriptorOutFactory = descriptor.getOutProcessorFactory();
        if (descriptorOutFactory != null) {
            outProcessor = descriptorOutFactory.newInputProcessor(outProcessor);
        }

        return outProcessor;
    }

    private InputProcessor createErrProcessor(OutputWriter writer) {
        LineConvertorFactory convertorFactory = descriptor.getErrConvertorFactory();
        InputProcessor errProcessor = null;
        if (descriptor.isErrLineBased()) {
            errProcessor = InputProcessors.bridge(LineProcessors.printing(writer,
                    convertorFactory != null ? convertorFactory.newLineConvertor() : null, false));
        } else {
            errProcessor = InputProcessors.printing(writer,
                    convertorFactory != null ? convertorFactory.newLineConvertor() : null, false);
        }

        InputProcessorFactory descriptorErrFactory = descriptor.getErrProcessorFactory();
        if (descriptorErrFactory != null) {
            errProcessor = descriptorErrFactory.newInputProcessor(errProcessor);
        }

        return errProcessor;
    }

    private InputProcessor createInProcessor(OutputStream os) {
        return InputProcessors.copying(new OutputStreamWriter(os));
    }

    private static class ProgressCancellable implements Cancellable {

        private Future<Integer> task;

        public ProgressCancellable() {
            super();
        }

        public synchronized void setTask(Future<Integer> task) {
            this.task = task;
        }

        public synchronized boolean cancel() {
            if (task != null) {
                task.cancel(true);
            }
            return true;
        }
    }

    private static class ProgressAction extends AbstractAction {

        private final InputOutput io;

        public ProgressAction(InputOutput io) {
            this.io = io;
        }

        public void actionPerformed(ActionEvent e) {
            io.select();
        }
    }

    private static class WrappedException extends Exception {

        public WrappedException(Throwable cause) {
            super(cause);
        }

    }
}
