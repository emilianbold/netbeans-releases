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

import org.netbeans.modules.extexecution.ManagedInputOutput;
import org.netbeans.modules.extexecution.StopAction;
import org.netbeans.modules.extexecution.RerunAction;
import java.awt.event.ActionEvent;
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

    static {
        RerunAction.Accessor.setDefault(new RerunAction.Accessor() {

            @Override
            public Future<Integer> run(ExecutionService service, InputOutput required) {
                return service.run(required);
            }
        });

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    /** Display names of currently active processes. */
    private static final Set<String> ACTIVE_DISPLAY_NAMES = new HashSet<String>();

    private final Callable<Process> processCreator;

    private final ExecutionDescriptor descriptor;

    private final String originalDisplayName;

    private StopAction stopAction;

    private RerunAction rerunAction;

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
     * Runs the process described by this service. Service can be executed
     * only once. If you need to run the same service again, use
     * {@link #newService(org.netbeans.modules.extexecution.api.ExecutionService)}.
     * <p>
     * Method must be invoked in Event Dispatch Thread.
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

        InputOutput customInputOutput = descriptor.getInputOutput();
        InputOutput workingIO = null;

        synchronized (ACTIVE_DISPLAY_NAMES) {
            workingIO = customInputOutput;

            // try to acquire required one (rerun action)
            // this will always succeed if this method is called from EDT
            if (workingIO == null) {
                ManagedInputOutput freeIO = ManagedInputOutput.getInputOutput(
                        required);

                if (freeIO != null) {
                    workingIO = freeIO.getInputOutput();
                    displayName = freeIO.getDisplayName();
                    stopAction = freeIO.getStopAction();
                    rerunAction = freeIO.getRerunAction();
                }
            }

            // try to find free output windows
            if (workingIO == null) {
                ManagedInputOutput freeIO = ManagedInputOutput.getInputOutput(
                        originalDisplayName, descriptor.isControllable());

                if (freeIO != null) {
                    workingIO = freeIO.getInputOutput();
                    displayName = freeIO.getDisplayName();
                    stopAction = freeIO.getStopAction();
                    rerunAction = freeIO.getRerunAction();
                }
            }

            // free IO was not found, create new one
            if (workingIO == null) {
                displayName = getNonActiveDisplayName(originalDisplayName);

                if (descriptor.isControllable()) {
                    stopAction = new StopAction();
                    rerunAction = new RerunAction();

                    workingIO = IOProvider.getDefault().getIO(displayName,
                            new Action[]{rerunAction, stopAction});
                    rerunAction.setParent(workingIO);
                } else {
                    workingIO = IOProvider.getDefault().getIO(displayName, true);
                }
            }

            configureInputOutput(workingIO);
            ACTIVE_DISPLAY_NAMES.add(displayName);
        }

        resetProcessors();

        final ProgressHandle handle = createProgressHandle(workingIO);
        final InputOutput io = workingIO;

        configureActions(rerunAction, stopAction);

        final boolean input = descriptor.isInputVisible();
        final ExecutorService executor = Executors.newFixedThreadPool(input ? 3 : 2);

        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final Reader in = io.getIn();

        Callable<Integer> callable = new Callable<Integer>() {
            public Integer call() throws Exception {
                boolean interrupted = false;
                Process process = null;
                Integer ret = null;

                try {
                    final Runnable pre = descriptor.getPreExecution();
                    if (pre != null) {
                        pre.run();
                    }

                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }

                    process = processCreator.call();

                    executor.submit(InputReaderTask.newTask(
                        InputReaders.forStream(process.getInputStream(), Charset.defaultCharset()),
                        createOutProcessor(out)));
                    executor.submit(InputReaderTask.newTask(
                        InputReaders.forStream(process.getErrorStream(), Charset.defaultCharset()),
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
                        final Runnable post = descriptor.getPostExecution();
                        if (post != null) {
                            post.run();
                        }
                    } finally {
                        if (process != null) {
                            process.destroy();
                            ret = process.exitValue();
                        }

                        if (interrupted) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                return ret;
            }
        };

        current = new ExecutionTask(this, callable, executor, out, err, in, handle,
                customInputOutput != null ? null : workingIO);
        if (stopAction != null) {
            synchronized (stopAction) {
                stopAction.setTask(current);
                stopAction.setEnabled(true);
            }
        }
        EXECUTOR_SERVICE.execute(current);
        return current;
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

    private void configureActions(RerunAction rerunAction, StopAction stopAction) {
//        if (stopAction != null) {
//            synchronized (stopAction) {
//                stopAction.setExecutionService(this);
//                stopAction.setEnabled(true);
//            }
//        }

        if (rerunAction != null) {
            synchronized (rerunAction) {
                rerunAction.setExecutionService(this);
                rerunAction.setRerunCondition(descriptor.getRerunCondition());
                rerunAction.setEnabled(false);
            }
        }
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

    private synchronized void executionCleanup(ExecutorService executor,
            OutputWriter out, OutputWriter err, Reader in, ProgressHandle handle, InputOutput inputOutput) {

        boolean interrupted = false;
        try {
            interrupted = shutdownExecutor(executor);

            out.close();
            err.close();

            try {
                in.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        } finally {
            synchronized (ACTIVE_DISPLAY_NAMES) {
                if (inputOutput != null) {
                    ManagedInputOutput.addInputOutput(inputOutput, displayName,
                            stopAction, rerunAction);
                }

                ACTIVE_DISPLAY_NAMES.remove(displayName);
            }

            if (handle != null) {
                handle.finish();
            }

            if (stopAction != null) {
                stopAction.setEnabled(false);
            }

            if (rerunAction != null) {
                rerunAction.setEnabled(true);
            }
            if (interrupted) {
                System.out.println("Cleanup interrupted");
                Thread.currentThread().interrupt();
            }
        }
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

    private static boolean shutdownExecutor(final ExecutorService executor) {
            try {
                AccessController.doPrivileged(new PrivilegedAction<Void>(){
                    public Void run() {
                        executor.shutdownNow();
                        return null;
                    }
                });
                while (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS));
            } catch (InterruptedException ex) {
                return true;
            }
            return false;
    }

    private static class ExecutionTask extends FutureTask<Integer> {

        private final ExecutionService service;

        private final ExecutorService executor;

        private final OutputWriter out;

        private final OutputWriter err;

        private final Reader in;

        private final ProgressHandle handle;

        private final InputOutput inputOutput;

        public ExecutionTask(ExecutionService service, Callable<Integer> callable,
                ExecutorService executor, OutputWriter out, OutputWriter err, Reader in,
                ProgressHandle handle, InputOutput inputOutput) {
            super(callable);
            this.service = service;
            this.executor = executor;
            this.out = out;
            this.err = err;
            this.in = in;
            this.handle = handle;
            this.inputOutput = inputOutput;
        }

        @Override
        protected void done() {
            service.executionCleanup(executor, out, err, in, handle, inputOutput);
            super.done();
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

    private static class ShutdownThread extends Thread {

        @Override
        public void run() {
            boolean interrupted = shutdownExecutor(EXECUTOR_SERVICE);
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
