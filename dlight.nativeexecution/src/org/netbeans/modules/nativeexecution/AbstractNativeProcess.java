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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessChangeEvent;
import org.netbeans.modules.nativeexecution.api.ProcessInfo;
import org.netbeans.modules.nativeexecution.spi.ProcessInfoProviderFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.ProcessInfoProvider;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public abstract class AbstractNativeProcess extends NativeProcess {

    protected final static java.util.logging.Logger LOG = Logger.getInstance();
    private final static Integer PID_TIMEOUT =
            Integer.valueOf(System.getProperty(
            "dlight.nativeexecutor.pidtimeout", "70")); // NOI18N
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
    private volatile Integer exitValue = null;
    private volatile boolean isInterrupted;
    private boolean cancelled = false;
    private Future<ProcessInfoProvider> infoProviderSearchTask;

    public AbstractNativeProcess(NativeProcessInfo info) {
        this.info = info;
        isInterrupted = false;
        state = State.INITIAL;
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
            ex.printStackTrace();
        }
        hostInfo = hinfo;

        Collection<ChangeListener> ll = info.getListeners();
        listeners = (ll == null || ll.isEmpty()) ? null
                : Collections.unmodifiableList(
                new ArrayList<ChangeListener>(ll));
    }

    public final NativeProcess createAndStart() throws IOException {
        try {
            if (hostInfo == null) {
                throw new IllegalStateException("Unable to create process - no HostInfo available"); // NOI18N
            }

            setState(State.STARTING);
            create();
            setState(State.RUNNING);
            findInfoProvider();
        } catch (Throwable ex) {
            LOG.log(Level.INFO, loc("NativeProcess.exceptionOccured.text"), ex.toString());
            setState(State.ERROR);
        }

        return this;
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

    protected final void interrupt() {
        isInterrupted = true;
        destroy();
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
     * To be implemented by a successor.
     * It must terminate the underlaying system process on this method call.
     */
    protected abstract void cancel();

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
     * Terminates the underlaying system process. The system process represented
     * by this <code>AbstractNativeProcess</code> object is forcibly terminated.
     */
    @Override
    public final void destroy() {
        synchronized (this) {
            if (cancelled) {
                return;
            }

            cancelled = true;
        }

        synchronized (stateLock) {
            cancel();
            setState(State.CANCELLED);
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
        setExitValue(waitResult());

        if (exitValue == null) {
            throw new InterruptedException(
                    "Process has been cancelled."); // NOI18N
        }

        return exitValue.intValue();
    }

    /**
     * Returns the exit code for the underlaying system process.
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
        synchronized (stateLock) {
            if (exitValue == null) {
                if (state == State.CANCELLED) {
                    // TODO: ??
                    // Removed CancellationException because it is not proceeded
                    // in ExecutionService...
                    // throw new CancellationException("Process has been cancelled");
                    return -1;
                } else {
                    // Process not started yet...
                    throw new IllegalThreadStateException();
                }
            }
        }

        return exitValue;
    }

    private void setExitValue(int exitValue) {
        synchronized (stateLock) {
            if (this.exitValue != null) {
                return;
            }

            this.exitValue = Integer.valueOf(exitValue);

            if (state == State.CANCELLED || state == State.ERROR) {
                return;
            }

            setState(State.FINISHED);
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
                        LOG.finest(String.format("%s: State changed: %s -> %s\n", // NOI18N
                                this.toString(), this.state, state));
                    }
                }

                this.state = state;

                if (listeners == null) {
                    return;
                }

                final ChangeEvent event = new NativeProcessChangeEvent(this, state, pid);

                for (ChangeListener l : listeners) {
                    l.stateChanged(event);
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
