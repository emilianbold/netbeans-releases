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
package org.netbeans.modules.nativeexecution;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.*;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.netbeans.modules.nativeexecution.spi.ProcessInfoProviderFactory;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public abstract class AbstractNativeProcess extends NativeProcess {

    protected final static java.util.logging.Logger LOG = Logger.getInstance();
    private final static Integer PID_TIMEOUT =
            Integer.valueOf(System.getProperty(
            "dlight.nativeexecutor.pidtimeout", "70")); // NOI18N
    private final static Integer SIGKILL_TIMEOUT =
            Integer.valueOf(System.getProperty(
            "dlight.nativeexecutor.forcekill.timeout", "5")); // NOI18N
    protected final NativeProcessInfo info;
    protected final HostInfo hostInfo;
    protected long creation_ts = -1;
    private final String id;
    private final ExecutionEnvironment execEnv;
    // Immutable listeners list.
    private final Collection<ChangeListener> listeners;
    private final Object stateLock;
    private volatile State state;
    private volatile int pid = 0;
    private volatile boolean isInterrupted;
    private final AtomicBoolean cancelledFlag = new AtomicBoolean(false);
    private Future<ProcessInfoProvider> infoProviderSearchTask;
    private Future<Integer> waitTask = null;
    private final Object resultLock = new Object();
    private Integer result = null;
    private InputStream inputStream;
    private InputStream errorStream;
    private OutputStream outputStream;

    public AbstractNativeProcess(NativeProcessInfo info) {
        this.info = info;
        isInterrupted = false;
        state = State.INITIAL;

        inputStream = new ByteArrayInputStream(new byte[0]);
        errorStream = new ByteArrayInputStream(new byte[0]);
        outputStream = new ByteArrayOutputStream();

        execEnv = info.getExecutionEnvironment();
        String cmd = info.getCommandLineForShell();

        if (cmd == null) {
            cmd = Arrays.toString(info.getCommand().toArray(new String[0]));
        }

        id = execEnv.toString() + ' ' + cmd;
        stateLock = "StateLock: " + id; // NOI18N

        HostInfo hinfo = null;
        try {
            hinfo = HostInfoUtils.getHostInfo(execEnv);
        } catch (CancellationException ex) {
            // no logging for cancellation
        } catch (InterruptedIOException ex) {
            // no logging for interrupting
        } catch (IOException ex) {
//            log.log(Level.INFO, "Exception while getting host info:", ex); //NOI18N
//            Exceptions.printStackTrace(ex);
        }
        hostInfo = hinfo;
        listeners = info.getListenersSnapshot();
    }

    public final NativeProcess createAndStart() {
        try {
            if (hostInfo == null) {
                throw new IllegalStateException("Unable to create process - no HostInfo available"); // NOI18N
            }

            setState(State.STARTING);
            create();
            setState(State.RUNNING);
            findInfoProvider();
            waitTask = NativeTaskExecutorService.submit(new Callable<Integer>() {

                @Override
                public Integer call() throws Exception {
                    int exitCode = -1;
                    State state = null;

                    try {
                        exitCode = waitResult();
                        state = State.FINISHED;
                    } catch (InterruptedException ex) {
                        state = State.CANCELLED;
                        throw ex;
                    } catch (Throwable th) {
                        state = State.ERROR;
                        Exceptions.printStackTrace(th);
                    } finally {
                        setResult(exitCode);
                        if (cancelledFlag.get()) {
                            setState(State.CANCELLED);
                        } else if (state != null) {
                            setState(state);
                        }
                    }

                    return exitCode;
                }
            }, "Waiting for " + id); // NOI18N
        } catch (Throwable ex) {
            setResult(-2);
            setState(State.ERROR);
            destroy();
            LOG.log(Level.INFO, loc("NativeProcess.exceptionOccured.text", ex.getMessage()), ex); // NOI18N
            String msg = (ex.getMessage() == null ? ex.toString() : ex.getMessage());
            errorStream = new ByteArrayInputStream((msg + "\n").getBytes()); // NOI18N
        }

        return this;
    }

    private void setResult(int exitCode) {
        synchronized (resultLock) {
            result = exitCode;
        }
    }

    abstract protected void create() throws Throwable;

    protected final boolean isInterrupted() {
        try {
            Thread.sleep(0);
        } catch (InterruptedException ex) {
            isInterrupted = true;
            Thread.currentThread().interrupt();
        }

        isInterrupted |= Thread.currentThread().isInterrupted();
        return isInterrupted;
    }

    @Override
    public final ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    @Override
    public final int getPID() throws IOException {
        synchronized (this) {
            if (pid == 0) {
                if (isInterrupted()) {
                    destroy();
                    throw new InterruptedIOException();
                } else {
                    throw new IOException("PID of process '" + id + "' is not received!"); // NOI18N
                }
            }

            return pid;
        }
    }

    /**
     * To be implemented by a successor. It must implement the specific
     * termination of the underlying system process on this method call. It is
     * guaranteed that this method is called only once. Implementation should
     * not (but may) wait for the actual termination before returning from the
     * call. If destroyImpl() returns and process's waitFor() still not exited
     * during specified (by return value) seconds (i.e. process was not actually
     * terminated), then a SIGTERM is send to the process.
     *
     * Default implementation just returns 0. So SIGTERM is send immediately to
     * force-terminate the process.
     *
     * SIGKILL is send if after SIGTERM process is still alive for
     * "dlight.nativeexecutor.forcekill.timeout".
     *
     * @return number of seconds to wait before doing an attempt to
     * force-terminate the process with the SIGTERM (and SIGKILL) signal (signal
     * is send only if process was not finished by that time).
     */
    protected int destroyImpl() {
        return 0;
    }

    /**
     * To be implemented by a successor. This method must cause the current
     * thread to wait until the underlaying system process is done and return
     * it's exit code.
     *
     * @return exit code of underlaying system process.
     * @exception  InterruptedException if the current thread is
     *             {@link Thread#interrupt() interrupted} by another thread
     *             while it is waiting, then the wait is ended and an
     *             {@link InterruptedException} is thrown.
     */
    protected abstract int waitResult() throws InterruptedException;

    @Override
    public final State getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    /**
     * Returns human-readable identification of the <tt>AbstractNativeProcess</tt>.
     * @return string that identifies the <tt>AbstractNativeProcess</tt>.
     */
    @Override
    public final String toString() {
        return (id == null) ? super.toString() : id.trim();
    }

    /**
     * Terminates the underlying system process. The system process represented
     * by this <code>AbstractNativeProcess</code> object is forcibly terminated.
     *
     * Returning from the call of this method does not mean that the process was
     * already terminated.
     *
     * May block caller thread for significant time
     */
    @Override
    public final void destroy() {
        if (cancelledFlag.getAndSet(true)) {
            return;
        }

        final int timeToWait = destroyImpl();

        try {
            waitTask.get(timeToWait, TimeUnit.SECONDS);
            return;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
        } catch (TimeoutException ex) {
        }

        try {
            exitValue();
            // No exception means successful termination
            return;
        } catch (IllegalThreadStateException ex) {
        }

        try {
            CommonTasksSupport.sendSignalGrp(execEnv, pid, Signal.SIGTERM, null).get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
        }

        try {
            waitTask.get(SIGKILL_TIMEOUT, TimeUnit.SECONDS);
            return;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
        } catch (TimeoutException ex) {
        }

        try {
            exitValue();
            // No exception means successful termination
            return;
        } catch (IllegalThreadStateException ex) {
        }

        try {
            CommonTasksSupport.sendSignalGrp(execEnv, pid, Signal.SIGKILL, null).get();
        } catch (InterruptedException ex1) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex1) {
        }
    }

    /**
     * Causes the current thread to wait, if necessary, until the
     * process represented by this <code>AbstractNativeProcess</code> object has
     * terminated. This method returns immediately if the subprocess has already
     * terminated. If the subprocess has not yet terminated, the calling thread
     * will be blocked until the subprocess exits.
     *
     * @return     the exit value of the process. By convention,
     *             <code>0</code> indicates normal termination.
     * @exception  InterruptedException  if the current thread is
     *             {@link Thread#interrupt() interrupted} by another thread
     *             while it is waiting, then the wait is ended and an
     *             {@link InterruptedException} is thrown.
     */
    @Override
    public final int waitFor() throws InterruptedException {
        int exitStatus = -1;

        if (waitTask == null) {
            // createAndStart() failed
            return exitStatus;
        }

        try {
            exitStatus = waitTask.get();
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof InterruptedException) {
                throw (InterruptedException) ex.getCause();
            } else {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            // Will clear interrupted flag (if set) as it is a general
            // convension that "any method that exits by throwing an
            // InterruptedException clears interrupt status when it does so."
            // http://java.sun.com/docs/books/tutorial/essential/concurrency/interrupt.html
            //
            // This convension is violated in java.lang.Process.waitFor()
            // doesn't do this (http://bugs.sun.com/view_bug.do?bug_id=6420270)
            //
            // But having this Thread.interrupted() here doesn't harm in other
            // cases as well.

            Thread.interrupted();
        }

        return exitStatus;
    }

    /**
     * Returns the exit code for the underlying system process.
     *
     * @return  the exit code of the system process represented by this
     *          <code>AbstractNativeProcess</code> object. By convention, the value
     *          <code>0</code> indicates normal termination.
     * @exception  IllegalThreadStateException if the system process
     *             represented by this <code>Process</code> object has not
     *             yet terminated.
     */
    @Override
    public final int exitValue() {
        if (waitTask == null || !waitTask.isDone()) {
            synchronized (resultLock) {
                if (result != null) {
                    return result.intValue();
                }
            }

            // Process not started/finished yet...
            throw new IllegalThreadStateException();
        }
        try {
            return waitTask.get();
        } catch (InterruptedException ex) {
            // cancelled
            return -1;
        } catch (ExecutionException ex) {
            return -1;
        }
    }

    private void setState(State state) {
        synchronized (stateLock) {
            if (this.state == state) {
                return;
            }

            /*
             * Process has determinated order of states it can be set to:
             * INITIAL ---> STARTING  ---> RUNNING  ---> FINISHED
             *          |-> CANCELLED  |-> CENCELLED |-> CANCELLED
             *          |-> ERROR      |-> ERROR     |-> ERROR
             *
             * CANCELLED, ERROR and FINISHED are terminal states.
             */

            if (this.state == State.CANCELLED
                    || this.state == State.ERROR
                    || this.state == State.FINISHED) {
                return;
            }

            try {
                if (isInterrupted()) {
                    // clear flag.
                    // will restore in finally block.
                    Thread.interrupted();
                }

                if (!isInterrupted()) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest(String.format("%s: State changed: %s -> %s", // NOI18N
                                this.toString(), this.state, state));
                    }
                }

                this.state = state;

                if (!listeners.isEmpty()) {
                    final ChangeEvent event = new NativeProcessChangeEvent(this, state, pid);

                    for (ChangeListener l : listeners) {
                        l.stateChanged(event);
                    }

                    if (this.state == State.CANCELLED
                            || this.state == State.ERROR
                            || this.state == State.FINISHED) {
                        listeners.clear();
                    }
                }
            } finally {
                if (isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // To be called from successors' constructor only...
    protected final void readPID(final InputStream is) throws IOException {
        int c = -1;
        pid = 0;

        while (!isInterrupted()) {
            c = is.read();

            if (c >= '0' && c <= '9') {
                pid = pid * 10 + (c - '0');
            } else {
                break;
            }
        }
    }

    @Override
    public ProcessInfo getProcessInfo() {
        ProcessInfoProvider provider = null;

        try {
            provider = infoProviderSearchTask.get();
        } catch (Throwable ex) {
            LOG.finest(ex.getMessage());
        }

        return provider == null ? new ProcessInfo() {

            @Override
            public long getCreationTimestamp(TimeUnit unit) {
                return unit.convert(creation_ts, TimeUnit.NANOSECONDS);
            }
        } : provider.getProcessInfo();
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(AbstractNativeProcess.class, key, params);
    }

    @Override
    public final InputStream getErrorStream() {
        return errorStream;
    }

    @Override
    public final OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public final InputStream getInputStream() {
        return inputStream;
    }

    protected final void setErrorStream(InputStream error) {
        errorStream = error;
    }

    protected final void setOutputStream(OutputStream output) {
        outputStream = output;
    }

    protected final void setInputStream(InputStream input) {
        inputStream = input;
    }

    private void findInfoProvider() {
        Callable<ProcessInfoProvider> callable = new Callable<ProcessInfoProvider>() {

            @Override
            public ProcessInfoProvider call() throws Exception {
                final Collection<? extends ProcessInfoProviderFactory> factories =
                        Lookup.getDefault().lookupAll(ProcessInfoProviderFactory.class);

                ProcessInfoProvider pip = null;

                for (ProcessInfoProviderFactory factory : factories) {
                    pip = factory.getProvider(execEnv, pid);
                    if (pip != null) {
                        break;
                    }
                }
                return pip;
            }
        };

        infoProviderSearchTask = NativeTaskExecutorService.submit(callable,
                "get info provider for process " + pid); // NOI18N
    }
}
