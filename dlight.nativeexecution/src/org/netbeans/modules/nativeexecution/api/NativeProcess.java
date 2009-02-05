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
 * The {@link NativeProcessBuilder} creates a native process and returns an
 * instance of <tt>NativeProcess</tt> which is subclass of <tt>java.io.Process</tt>.
 * The differentiator is that this implementation can represent as local as well
 * as remote process, has information about process' PID and state
 * ({@link NativeProcess.State})
 */
public abstract class NativeProcess extends Process {

    private final static java.util.logging.Logger log = Logger.getInstance();
    private final Object stateLock = new Object();
    private Collection<Listener> listeners = null;
    private State state = State.INITIAL;
    private Integer exitValue = null;
    private String id = "";


    static {
        NativeProcessAccessor.setDefault(new NativeProcessAccessorImpl());
    }

    /**
     * Returns PID of underlaying system process.<br>
     * <tt>java.lang.IllegalStateException</tt> is thrown if no PID was
     * obtained.
     * @return PID of underlaying system process.
     */
    public abstract int getPID();

    /**
     * To be implemented in successor. It must terminate underlaying system
     * process on this method call.
     */
    protected abstract void cancel();

    /**
     * To be implemented by an accessor. This method should cause the current
     * thread to wait until underlaying system process is done and return
     * it's exit code.
     *
     * @return exit code of underlaying system process.
     * @exception  InterruptedException  if the current thread is
     *             {@link Thread#interrupt() interrupted} by another thread
     *             while it is waiting, then the wait is ended and an
     *             {@link InterruptedException} is thrown.
     */
    protected abstract int waitResult() throws InterruptedException;

    /**
     * Returns the current state ({@link NativeProcess.State}) of the process.
     * @return current state if the process.
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
        return id;
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
     * Kills the underlaying system process. The system process represented by
     * this <code>NativeProcess</code> object is forcibly terminated.
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
     * terminated. This method returns
     * immediately if the subprocess has already terminated. If the
     * subprocess has not yet terminated, the calling thread will be
     * blocked until the subprocess exits.
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
            throw new InterruptedException("Process has been cancelled.");
        }

        return exitValue.intValue();
    }

    /**
     * Returns the exit value for the underlaying system process.
     *
     * @return  the exit value of the system process represented by this
     *          <code>NativeProcess</code> object. By convention, the value
     *          <code>0</code> indicates normal termination.
     * @exception  IllegalThreadStateException  if the system process
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

    private final void setExitValue(int exitValue) {
        if (this.exitValue != null) {
            return;
        }

        if (state == State.CANCELLED) {
            return;
        }

        this.exitValue = Integer.valueOf(exitValue);
        setState(State.FINISHED);
    }

    /**
     *
     */
    public static enum State {

        /**
         * Task is in Initial state
         */
        INITIAL,
        /**
         * Task is starting. This means that it is submitted, but no PID
         * is recieved yet.
         */
        STARTING,
        /**
         * Task runs
         */
        RUNNING,
        /**
         * Task finished
         */
        FINISHED,
        /**
         * Task failed due to some exception
         */
        ERROR,
        /**
         * Task cancelled
         */
        CANCELLED
    }

    /**
     *
     */
    public static interface Listener {

        /**
         *
         * @param process
         * @param oldState
         * @param newState
         */
        public void processStateChanged(
                NativeProcess process, State oldState, State newState);
    }

    private static class NativeProcessAccessorImpl
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
