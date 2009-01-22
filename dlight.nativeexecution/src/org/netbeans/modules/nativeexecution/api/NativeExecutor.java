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
package org.netbeans.modules.nativeexecution.api;

import org.netbeans.modules.nativeexecution.support.StreamRedirector;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.nativeexecution.support.ImageLoader;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.openide.util.Cancellable;

public abstract class NativeExecutor implements /*ActionsProvider,*/ Cancellable {

    public final String CANCEL_ACTION = "Cancel"; // NOI18N
    public final String RESTART_ACTION = "Restart"; // NOI18N
    protected final static java.util.logging.Logger log = Logger.getInstance();
    final Object stateMonitor = new Object();
    TaskExecutionState state = TaskExecutionState.INITIAL;
    final List<NativeTaskListener> taskListeners = Collections.synchronizedList(new ArrayList<NativeTaskListener>());
    protected final NativeTask task;
    private int pid;
    private Integer exitValue;
    private Throwable exception;
    private AbstractAction cancelAction,  restartAction;
    private Action[] actions;
    private ProgressHandle ph = null;

    public NativeExecutor(NativeTask task) {
        this.task = task;
    }

    /**
     *
     * @return PID of the started process
     * @throws java.lang.Exception
     */
    abstract protected int doInvoke() throws Exception;

    abstract protected Integer get();

    abstract protected InputStream getTaskInputStream() throws IOException;

    abstract protected InputStream getTaskErrorStream() throws IOException;

    abstract protected OutputStream getTaskOutputStream() throws IOException;

    public final Action[] getActions() {
        if (actions == null) {
            actions = new Action[]{getAction(CANCEL_ACTION), getAction(RESTART_ACTION)};
        }
        return actions;
    }

    public final Action getAction(String id) {
        if (CANCEL_ACTION.equals(id)) {
            if (cancelAction == null) {
                cancelAction = new SimpleCancelAction();
            }
            return cancelAction;
        }

        if (RESTART_ACTION.equals(id)) {
            if (restartAction == null) {
                restartAction = new SimpleRestartAction();
            }
            return restartAction;
        }

        return null;
    }

    public final Integer invokeAndWait() throws Exception {
        if (state == TaskExecutionState.RUNNING ||
                state == TaskExecutionState.STARTING) {
            return -1;
        }

        ph = task.getProgressHandler();

        if (ph != null) {
            ph.start();
        }

        if (!(state == TaskExecutionState.INITIAL)) {
            reset();
        }

        setState(TaskExecutionState.STARTING);

        updateActions();

        Thread ot = null, et = null, it = null;
        long startTime = System.currentTimeMillis();

        try {
            pid = doInvoke();

            if (pid <= 0) {
                throw new Throwable("Unable to start native process"); // NOI18N
            }

            InputStream taskOut = getTaskInputStream();
            ot = null;
            if (taskOut != null && task.getRedirectionOutputWriter() != null) {
                ot = new StreamRedirector(taskOut, task.getRedirectionOutputWriter(), "output from " + task.toString()); // NOI18N
                ot.start();
            }

            InputStream taskErr = getTaskErrorStream();
            et = null;
            if (taskErr != null && task.getRedirectionErrorWriter() != null) {
                et = new StreamRedirector(taskErr, task.getRedirectionErrorWriter(), "error from " + task.toString()); // NOI18N
                et.start();
            }

            OutputStream taskIn = getTaskOutputStream();
            it = null;
            if (taskIn != null && task.getRedirectionInputReader() != null) {
                it = new StreamRedirector(task.getRedirectionInputReader(), getTaskOutputStream(), "input of " + task.toString()); // NOI18N
                it.start();
            }

            setState(TaskExecutionState.RUNNING);

            setResult(get());
        } catch (CancellationException ex) {
            exception = ex;
            setState(TaskExecutionState.CANCELED);
        } catch (Throwable ex) {
            if (ex instanceof ExecutionException) {
                ex = ex.getCause();
            }
            exception = ex;
            setState(TaskExecutionState.ERROR);
        } finally {
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            System.err.println("Task " + task + " finished in " + duration + "s");

            if (it != null) {
                it.interrupt();
            }

            updateActions();

            if (ph != null) {
                ph.finish();
            }
        }

        return exitValue;
    }

    int getPID() {
        if (state == TaskExecutionState.INITIAL) {
            throw new IllegalStateException("Task is not started yet"); // NOI18N
        }

        return pid;
    }

    protected void setProgress(String progress) {
        if (ph != null) {
            ph.progress(progress);
        }
    }

    public void setProgress(int workUnits) {
        if (ph != null) {
            ph.progress(workUnits);
        }
    }

    public void setProgressLimit(int workUnitsLimit) {
        if (ph != null) {
            ph.switchToDeterminate(workUnitsLimit);
        }
    }

    public void addListener(NativeTaskListener listener) {
        if (listener == null) {
            return;
        }

        if (!taskListeners.contains(listener)) {
            taskListeners.add(listener);
        }
    }

    public void removeListener(NativeTaskListener listener) {
        if (listener == null) {
            return;
        }

        taskListeners.remove(listener);
    }

    private synchronized void notifyListeners() {
        log.fine("Task " + task + " changed state to " + state); // NOI18N

        for (NativeTaskListener l : taskListeners.toArray(new NativeTaskListener[0])) {
            try {
                switch (state) {
                    case RUNNING:
                        l.taskStarted(task);
                        break;
                    case FINISHED:
                        l.taskFinished(task, exitValue);
                        break;
                    case FAILED:
                        l.taskError(task, exception);
                        break;
                    case CANCELED:
                        l.taskCancelled(task, (CancellationException) exception);
                        break;
                }
            } catch (Exception e) {
                log.severe("Exception during ExecutorTaskListener " + l + " notification. " + e.toString()); // NOI18N
            }
        }
    }

    protected void setResult(Integer exitValue) {
        this.exitValue = exitValue;

        if (isCancelled()) {
            return;
        }

        setState(exitValue == 0 ? TaskExecutionState.FINISHED : TaskExecutionState.FAILED);
    }

    boolean isCancelled() {
        return state.equals(TaskExecutionState.CANCELED);
    }

    boolean isDone() {
        return !(state == TaskExecutionState.RUNNING ||
                state == TaskExecutionState.STARTING);
    }

    private void reset() {
        exception = null;
        pid = -1;
        task.reset();
        setState(TaskExecutionState.INITIAL);
    }

    private void setState(TaskExecutionState newState) {
        if (newState == state) {
            return;
        }

        synchronized (stateMonitor) {
            state = newState;
            notifyListeners();
            stateMonitor.notifyAll();
        }
    }

    private void updateActions() {
        if (state == TaskExecutionState.RUNNING || state == TaskExecutionState.STARTING) {
            getAction(CANCEL_ACTION).setEnabled(true);
            getAction(RESTART_ACTION).setEnabled(false);
        } else if (state == TaskExecutionState.INITIAL) {
            getAction(CANCEL_ACTION).setEnabled(false);
            getAction(RESTART_ACTION).setEnabled(false);
        } else {
            getAction(CANCEL_ACTION).setEnabled(false);
            getAction(RESTART_ACTION).setEnabled(true);
        }
    }

    class SimpleCancelAction extends AbstractAction {

        public SimpleCancelAction() {
            super("Cancel " + task.toString(), ImageLoader.loadIcon("CancelTask.png")); // NOI18N
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            cancel();
            setEnabled(false);
        }
    }

    class SimpleRestartAction extends AbstractAction {

        public SimpleRestartAction() {
            super("Restart " + task.toString(), ImageLoader.loadIcon("RerunTask.png")); // NOI18N
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            task.submit();
            setEnabled(false);
        }
    }
}
