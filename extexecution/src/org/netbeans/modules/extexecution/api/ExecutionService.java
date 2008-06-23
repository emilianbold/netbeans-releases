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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputProcessors;
import org.netbeans.modules.extexecution.api.input.InputReaderTask;
import org.netbeans.modules.extexecution.api.input.InputReaders;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
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

    /** Display names of currently active processes. */
    private static final Set<String> ACTIVE_DISPLAY_NAMES = new HashSet<String>();

    private final Callable<Process> processCreator;

    private final ExecutionDescriptor descriptor;

    private final String originalDisplayName;

    private String displayName;

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
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Method must be invoked from EDT");
        }

        InputOutputManager.InputOutputData ioData = getInputOutput(required);

        resetProcessors();

        final ProgressHandle handle = createProgressHandle(ioData.getInputOutput());
        final InputOutput io = ioData.getInputOutput();

        final boolean input = descriptor.isInputVisible();

        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final Reader in = io.getIn();
        final StopAction workingStopAction = ioData.getStopAction();
        final RerunAction workingRerunAction = ioData.getRerunAction();

        // FIXME custom io - cleanup task
        final InputOutput inputOutput = descriptor.getInputOutput() != null ? null : ioData.getInputOutput();

        Callable<Integer> callable = new Callable<Integer>() {
            public Integer call() throws Exception {
                boolean interrupted = false;
                Process process = null;
                Integer ret = null;
                ExecutorService executor = null;
                try {
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

                    executor = Executors.newFixedThreadPool(input ? 3 : 2);
                    executor.submit(InputReaderTask.newDrainingTask(
                        InputReaders.forStream(new BufferedInputStream(process.getInputStream()), Charset.defaultCharset()),
                        createOutProcessor(out)));
                    executor.submit(InputReaderTask.newDrainingTask(
                        InputReaders.forStream(new BufferedInputStream(process.getErrorStream()), Charset.defaultCharset()),
                        createErrProcessor(err)));
                    if (input) {
                        executor.submit(InputReaderTask.newTask(
                            InputReaders.forReader(in),
                            createInProcessor(process.getOutputStream())));
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
                        SwingUtilities.invokeLater(new CleanupTask(
                                displayName, executor, workingRerunAction, workingStopAction, handle, inputOutput));

                        final Runnable post = descriptor.getPostExecution();
                        if (post != null) {
                            post.run();
                        }

                        if (interrupted) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                return ret;
            }
        };

        current = new FutureTask<Integer>(callable);
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

    private InputOutputManager.InputOutputData getInputOutput(InputOutput required) {
        InputOutput io = null;
        StopAction stopAction = null;
        RerunAction rerunAction = null;

        synchronized (ACTIVE_DISPLAY_NAMES) {
            io = descriptor.getInputOutput();

            // try to acquire required one (rerun action)
            // this will always succeed if this method is called from EDT
            if (io == null) {
                InputOutputManager.InputOutputData freeIO = InputOutputManager.getInputOutput(
                        required);

                if (freeIO != null) {
                    io = freeIO.getInputOutput();
                    displayName = freeIO.getDisplayName();
                    stopAction = freeIO.getStopAction();
                    rerunAction = freeIO.getRerunAction();
                }
            }

            // try to find free output windows
            if (io == null) {
                InputOutputManager.InputOutputData freeIO = InputOutputManager.getInputOutput(
                        originalDisplayName, descriptor.isControllable());

                if (freeIO != null) {
                    io = freeIO.getInputOutput();
                    displayName = freeIO.getDisplayName();
                    stopAction = freeIO.getStopAction();
                    rerunAction = freeIO.getRerunAction();
                }
            }

            // free IO was not found, create new one
            if (io == null) {
                displayName = getNonActiveDisplayName(originalDisplayName);

                if (descriptor.isControllable()) {
                    stopAction = new StopAction();
                    rerunAction = new RerunAction();

                    io = IOProvider.getDefault().getIO(displayName,
                            new Action[]{rerunAction, stopAction});
                    rerunAction.setParent(io);
                } else {
                    io = IOProvider.getDefault().getIO(displayName, true);
                }
            }

            configureInputOutput(io);
            ACTIVE_DISPLAY_NAMES.add(displayName);
        }

        return new InputOutputManager.InputOutputData(io, displayName, stopAction, rerunAction);
    }

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

    private void resetProcessors() {
        InputProcessor outProcessor = descriptor.getOutProcessor();
        try {
            if (outProcessor != null) {
                outProcessor.reset();
            }

        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        InputProcessor errProcessor = descriptor.getErrProcessor();
        try {
            if (errProcessor != null) {
                errProcessor.reset();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    private ProgressHandle createProgressHandle(InputOutput inputOutput) {
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

    private InputProcessor createOutProcessor(OutputWriter writer) {
        InputProcessor outProcessor = InputProcessors.ansiStripping(
                InputProcessors.printing(writer, descriptor.getOutConvertor(), true));

        InputProcessor descriptorOut = descriptor.getOutProcessor();
        if (descriptorOut != null) {
            outProcessor = InputProcessors.proxy(outProcessor, descriptorOut);
        }

        return outProcessor;
    }

    private InputProcessor createErrProcessor(OutputWriter writer) {
        InputProcessor errProcessor = InputProcessors.ansiStripping(
                InputProcessors.printing(writer, descriptor.getErrConvertor(), false));

        InputProcessor descriptorErr = descriptor.getErrProcessor();
        if (descriptorErr != null) {
            errProcessor = InputProcessors.proxy(errProcessor, descriptorErr);
        }

        return errProcessor;
    }

    private InputProcessor createInProcessor(OutputStream os) {
        return InputProcessors.copying(new OutputStreamWriter(os));
    }

    private static String getNonActiveDisplayName(String displayNameBase) {
        synchronized (ACTIVE_DISPLAY_NAMES) {
            String nonActiveDN = displayNameBase;
            if (ACTIVE_DISPLAY_NAMES.contains(nonActiveDN)) {
                // Uniquify: "prj (targ) #2", "prj (targ) #3", etc.
                int i = 2;
                String testdn;

                do {
                    testdn = NbBundle.getMessage(ExecutionService.class, "Uniquified", nonActiveDN, i++);
                } while (ACTIVE_DISPLAY_NAMES.contains(testdn));

                nonActiveDN = testdn;
            }
            assert !ACTIVE_DISPLAY_NAMES.contains(nonActiveDN);
            return nonActiveDN;
        }
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

    private static class CleanupTask implements Runnable {

        private final String displayName;

        private final ExecutorService executor;

        private final RerunAction rerunAction;

        private final StopAction stopAction;

        private final ProgressHandle handle;

        private final InputOutput inputOutput;

        public CleanupTask(String displayName, ExecutorService executor,
                RerunAction rerunAction, StopAction stopAction,
                ProgressHandle handle, InputOutput inputOutput) {

            this.displayName = displayName;
            this.executor = executor;
            this.rerunAction = rerunAction;
            this.stopAction = stopAction;
            this.handle = handle;
            this.inputOutput = inputOutput;
        }

        public void run() {
            boolean interrupted = false;
            if (executor != null) {
                try {
                    AccessController.doPrivileged(new PrivilegedAction<Void>(){
                        public Void run() {
                            executor.shutdownNow();
                            return null;
                        }
                    });
                    while (!executor.awaitTermination(EXECUTOR_SHUTDOWN_SLICE, TimeUnit.MILLISECONDS));
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }

            try {
                inputOutput.getOut().close();
                inputOutput.getErr().close();
                inputOutput.getIn().close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }

            synchronized (ACTIVE_DISPLAY_NAMES) {
                if (inputOutput != null) {
                    InputOutputManager.addInputOutput(inputOutput, displayName,
                            stopAction, rerunAction);
                }

                ACTIVE_DISPLAY_NAMES.remove(displayName);

                if (handle != null) {
                    handle.finish();
                }

                if (stopAction != null) {
                    stopAction.setEnabled(false);
                }

                if (rerunAction != null) {
                    rerunAction.setEnabled(true);
                }
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
