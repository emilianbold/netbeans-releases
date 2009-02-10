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
package org.netbeans.modules.nativeexecution.api;

import java.util.Collection;
import org.netbeans.modules.nativeexecution.api.impl.NativeProcessAccessor;
import org.netbeans.modules.nativeexecution.support.Logger;

/**
 * A {@link NativeProcessBuilder} starts a system process and returns an
 * instance of the {@link NativeProcess} which is a subclass of the
 * {@link Process java.lang.Process}.
 * The differentiator is that this implementation can represent as local as well
 * as remote process, has information about process' PID and about it's
 * {@link NativeProcess.State state}.
 */
public abstract class NativeProcess extends Process {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final Object stateLock = new Object();
    private Collection<Listener> listeners = null;
    private State state = State.INITIAL;
    private Integer exitValue = null;
    private String id = null;


    static {
        NativeProcessAccessor.setDefault(new NativeProcessAccessorImpl());
    }

    /**
     * Returns PID of underlaying system process.<br>
     * @return PID of underlaying system process.
     * @throws IllegalStateException if no PID was obtained prior to method
     *         invokation.
     */
    public abstract int getPID() throws IllegalStateException;

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

    /**
     * Returns the current {@link NativeProcess.State state} of the process.
     * @return current state of the process.
     */
    public final State getState() {
        return state;
    }

    /**
     * Returns human-readable identification of the <tt>NativeProcess</tt>.
     * @return string that identifies the <tt>NativeProcess</tt>.
     */
    @Override
    public String toString() {
        return (id == null) ? super.toString() : id.trim();
    }

    private final void setState(State state) {
        synchronized (stateLock) {
            if (this.state == state) {
                return;
            }

            State oldState = this.state;
            this.state = state;

            notifyListeners(oldState, state);
        }
    }

    /**
     * Terminates the underlaying system process. The system process represented
     * by this <code>NativeProcess</code> object is forcibly terminated.
     */
    @Override
    public final void destroy() {
        synchronized (stateLock) {
            if (this.state == State.RUNNING) {
                cancel();
                setState(State.CANCELLED);
            }
        }
    }

    /**
     * Causes the current thread to wait, if necessary, until the
     * process represented by this <code>NativeProcess</code> object has
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
     *          <code>NativeProcess</code> object. By convention, the value
     *          <code>0</code> indicates normal termination.
     * @exception  IllegalThreadStateException if the system process
     *             represented by this <code>Process</code> object has not
     *             yet terminated.
     */
    @Override
    public final int exitValue() {
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

        return exitValue;
    }

    private void notifyListeners(State oldState, State newState) {
        if (listeners == null) {
            return;
        }

        log.fine(this.toString() + " State change: " + // NOI18N
                oldState + " -> " + newState); // NOI18N

        for (Listener l : listeners) {
            l.processStateChanged(this, oldState, newState);
        }

    }

    private void setExitValue(int exitValue) {
        if (this.exitValue != null) {
            return;
        }

        if (state == State.CANCELLED || state == State.ERROR) {
            return;
        }

        this.exitValue = Integer.valueOf(exitValue);
        setState(State.FINISHED);
    }

    /**
     * Enumerates possible states of the {@link NativeProcess}.
     */
    public static enum State {

        /**
         * Native process is in an Initial state. This means that it has not been
         * started yet.
         */
        INITIAL,
        /**
         * Native process is starting. This means that it has been submitted,
         * but no PID is recieved so far.
         */
        STARTING,
        /**
         * Native process runs. This means that process successfully started and
         * it's PID is already known.
         */
        RUNNING,
        /**
         * Native process exited.
         */
        FINISHED,
        /**
         * Native process submission failed due to some exception.
         */
        ERROR,
        /**
         * Native process forcibly terminated.
         */
        CANCELLED
    }

    /**
     * The listener interface for recieving state change events from a
     * {@link NativeProcess}.
     * <p>
     * One can implement <tt>NativeProcess.Listener</tt> and subscribe it to the
     * {@link NativeProcess} to recieve <tt>processStateChanged</tt> events.
     * <br>
     * See {@link NativeProcessBuilder#addNativeProcessListener(org.netbeans.modules.nativeexecution.api.NativeProcess.Listener) NativeProcessBuilder.addNativeProcessListener()}
     */
    public static interface Listener {

        /**
         * A notification about process' state change. The notification is send
         * to every registered listener on every state change.
         * @param process {@link NativeProcess} which state changed.
         * @param oldState previous state of the process.
         * @param newState new state of the process.
         * @see State
         */
        public void processStateChanged(
                NativeProcess process, State oldState, State newState);
    }

    private final static class NativeProcessAccessorImpl
            extends NativeProcessAccessor {

        @Override
        public void setID(NativeProcess process, String id) {
            process.id = id;
        }

        @Override
        public void setState(NativeProcess process, State state) {
            process.setState(state);
        }

        @Override
        public void setListeners(NativeProcess process,
                Collection<Listener> listeners) {
            process.listeners = listeners;
        }
    }
}
