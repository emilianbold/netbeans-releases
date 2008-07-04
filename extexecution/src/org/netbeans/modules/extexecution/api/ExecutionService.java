/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.extexecution.api;

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
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.extexecution.ProcessInputStream;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor.InputProcessorFactory;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputProcessors;
import org.netbeans.modules.extexecution.api.input.InputReaderTask;
import org.netbeans.modules.extexecution.api.input.InputReaders;
import org.openide.util.Cancellable;
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
 * All processes launched by this class are terminated on VM exit.
 * <p>
 * Note that once service is run for the first time. Subsequents runs can be
 * invoked by the user (rerun button) if it is allowed to do so
 * ({@link ExecutionDescriptor#isControllable()}).
 *
 * @author Petr Hejl
 * @see #newService(java.util.concurrent.Callable, org.netbeans.modules.extexecution.api.ExecutionDescriptor, java.lang.String)
 * @see ExecutionDescriptor
 */
public final class ExecutionService {

    private static final Logger LOGGER = Logger.getLogger(ExecutionService.class.getName());

    private static final Set<Process> RUNNING_PROCESSES = new HashSet<Process>();

    private static final int EXECUTOR_SHUTDOWN_SLICE = 1000;

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
                synchronized (RUNNING_PROCESSES) {
                    for (Process process : RUNNING_PROCESSES) {
                        process.destroy();
                    }
                }
            }
        });
    }

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final Callable<Process> processCreator;

    private final ExecutionDescriptor descriptor;

    private final String originalDisplayName;

    private FutureTask<Integer> current;

    private ExecutionService(Callable<Process> processCreator, String displayName, ExecutionDescriptor descriptor) {
        this.processCreator = processCreator;
        this.originalDisplayName = displayName;
        this.descriptor = descriptor;
    }

    /**
     * Creates new execution service. Service will wrap up the processes
     * created by <code>processCreator</code>.
     *
     * @param processCreator callable returning the process to wrap up
     * @param descriptor descriptor describing the configuration of service
     * @param displayName display name of this service
     * @return new execution service
     */
    public static ExecutionService newService(Callable<Process> processCreator,
            ExecutionDescriptor descriptor, String displayName) {
        return new ExecutionService(processCreator, displayName, descriptor);
    }


    /**
     * Runs the process described by this service.
     * <p>
     * This method must be invoked in Event Dispatch Thread.
     * <p>
     * For details on execution control see {@link ExecutionDescriptor}.
     *
     * @return task representing the actual run
     */
    public Future<Integer> run() {
        return run(null);
    }

    private synchronized Future<Integer> run(InputOutput required) {
//        if (!SwingUtilities.isEventDispatchThread()) {
//            throw new IllegalStateException("Method must be invoked from EDT");
//        }

        final InputOutputManager.InputOutputData ioData = getInputOutput(required);

        final String displayName = ioData.getDisplayName();
        final ProgressHandle handle = createProgressHandle(ioData.getInputOutput(), displayName);
        final InputOutput io = ioData.getInputOutput();

        final boolean input = descriptor.isInputVisible();

        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final Reader in = io.getIn();
        final StopAction workingStopAction = ioData.getStopAction();
        final RerunAction workingRerunAction = ioData.getRerunAction();

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] executed = new boolean[1];

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
                    executed[0] = true;
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

                    executor = Executors.newFixedThreadPool(input ? 3 : 2);

                    tasks.add(InputReaderTask.newDrainingTask(
                        InputReaders.forStream(new BufferedInputStream(outStream), Charset.defaultCharset()),
                        createOutProcessor(out)));
                    tasks.add(InputReaderTask.newDrainingTask(
                        InputReaders.forStream(new BufferedInputStream(errStream), Charset.defaultCharset()),
                        createErrProcessor(err)));
                    if (input) {
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
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                    throw ex;
                } finally {
                    try {
                        interrupted = interrupted || Thread.interrupted();

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
                                ret = process.exitValue();
                            } catch (IllegalThreadStateException ex) {
                                // still running
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                        throw ex;
                    } finally {
                        try {
                            cleanup(tasks, executor, handle, ioData,
                                    ioData.getInputOutput() != descriptor.getInputOutput());

                            final Runnable post = descriptor.getPostExecution();
                            if (post != null) {
                                post.run();
                            }
                        } finally {
                            latch.countDown();
                        }
                        if (interrupted) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                return ret;
            }
        };

        current = new FutureTask<Integer>(callable) {


            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean ret = super.cancel(mayInterruptIfRunning);
                if (!executed[0]) {
                    Runnable ui = new Runnable() {
                        public void run() {
                            if (ioData.getStopAction() != null) {
                                ioData.getStopAction().setEnabled(false);
                            }
                            if (ioData.getRerunAction() != null) {
                                ioData.getRerunAction().setEnabled(true);
                            }
                            if (handle != null) {
                                handle.finish();
                            }
                        }
                    };

                    if (SwingUtilities.isEventDispatchThread()) {
                        ui.run();
                    } else {
                        SwingUtilities.invokeLater(ui);
                    }

                    synchronized (InputOutputManager.class) {
                        if (ioData.getInputOutput() != descriptor.getInputOutput()) {
                            InputOutputManager.addInputOutput(ioData);
                        }
                    }
                } else {
// uncomment this if state after cancel should be the same as when completed normally
//                    if (ret && mayInterruptIfRunning && executed[0]) {
//                        try {
//                            latch.await();
//                        } catch (InterruptedException ex) {
//                            Thread.currentThread().interrupt();
//                        }
//                    }
                }
                return ret;
            }
        };

        if (workingStopAction != null) {
            synchronized (workingStopAction) {
                workingStopAction.setTask(current);
                workingStopAction.setEnabled(true);
            }
        }

        if (workingRerunAction != null) {
            synchronized (workingRerunAction) {
                workingRerunAction.setExecutionService(this);
                workingRerunAction.setRerunCondition(descriptor.getRerunCondition());
                workingRerunAction.setEnabled(false);
            }
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
                result = new InputOutputManager.InputOutputData(io, originalDisplayName, null, null);
            }

            // try to acquire required one (rerun action)
            // this will always succeed if this method is called from EDT
            if (result == null) {
                result = InputOutputManager.getInputOutput(required);
            }

            // try to find free output windows
            if (result == null) {
                result = InputOutputManager.getInputOutput(
                        originalDisplayName, descriptor.isControllable());
            }

            // free IO was not found, create new one
            if (result == null) {
                result = InputOutputManager.createInputOutput(
                        originalDisplayName, descriptor.isControllable());
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
        try {
            inputOutput.getOut().reset();
        } catch (IOException exc) {
            LOGGER.log(Level.INFO, null, exc);
        }

        // Note - do this AFTER the reset() call above; if not, weird bugs occur
        inputOutput.setErrSeparated(false);

        // Open I/O window now. This should probably be configurable.
        if (descriptor.isFrontWindow()) {
            inputOutput.select();
        }

        inputOutput.setInputVisible(descriptor.isInputVisible());
    }

    private ProgressHandle createProgressHandle(InputOutput inputOutput, String displayName) {
        if (!descriptor.showProgress() && !descriptor.showSuspended()) {
            return null;
        }

        Cancellable cancellable = null;
        if (descriptor.isControllable()) {
            cancellable = new ProgressCancellable(this);
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

    private void cleanup(final List<InputReaderTask> tasks, final ExecutorService processingExecutor, final ProgressHandle progressHandle,
            final InputOutputManager.InputOutputData inputOutputData, final boolean managed) {

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

        InputOutput io = inputOutputData.getInputOutput();
        io.getOut().close();
        io.getErr().close();

        try {
            io.getIn().close();
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }

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

        if (SwingUtilities.isEventDispatchThread()) {
            ui.run();
        } else {
            SwingUtilities.invokeLater(ui);
        }

        synchronized (InputOutputManager.class) {
            if (managed) {
                InputOutputManager.addInputOutput(inputOutputData);
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private InputProcessor createOutProcessor(OutputWriter writer) {
        LineConvertorFactory convertorFactory = descriptor.getOutConvertorFactory();
        InputProcessor outProcessor = InputProcessors.ansiStripping(
                InputProcessors.printing(writer,
                    convertorFactory != null ? convertorFactory.newLineConvertor() : null, true));

        InputProcessorFactory descriptorOutFactory = descriptor.getOutProcessorFactory();
        if (descriptorOutFactory != null) {
            outProcessor = InputProcessors.proxy(outProcessor, descriptorOutFactory.newInputProcessor());
        }

        return outProcessor;
    }

    private InputProcessor createErrProcessor(OutputWriter writer) {
        LineConvertorFactory convertorFactory = descriptor.getErrConvertorFactory();
        InputProcessor errProcessor = InputProcessors.ansiStripping(
                InputProcessors.printing(writer,
                    convertorFactory != null ? convertorFactory.newLineConvertor() : null, false));

        InputProcessorFactory descriptorErrFactory = descriptor.getErrProcessorFactory();
        if (descriptorErrFactory != null) {
            errProcessor = InputProcessors.proxy(errProcessor, descriptorErrFactory.newInputProcessor());
        }

        return errProcessor;
    }

    private InputProcessor createInProcessor(OutputStream os) {
        return InputProcessors.copying(new OutputStreamWriter(os));
    }

    private static class ProgressCancellable implements Cancellable {

        private final ExecutionService service;

        public ProgressCancellable(ExecutionService service) {
            this.service = service;
        }

        public boolean cancel() {
            synchronized (service) {
                if (service.current != null) {
                    service.current.cancel(true);
                }
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

}
