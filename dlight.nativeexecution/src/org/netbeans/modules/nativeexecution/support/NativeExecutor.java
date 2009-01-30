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
package org.netbeans.modules.nativeexecution.support;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.netbeans.modules.nativeexecution.api.NativeTaskListener;
import org.netbeans.modules.nativeexecution.api.TaskExecutionState;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

public abstract class NativeExecutor implements Cancellable {

    public final String CANCEL_ACTION = "Cancel"; // NOI18N
    public final String RESTART_ACTION = "Restart"; // NOI18N
    protected final static java.util.logging.Logger log = Logger.getInstance();
    final Object stateMonitor = new Object();
    private TaskExecutionState state = TaskExecutionState.INITIAL;
    final List<NativeTaskListener> taskListeners =
            Collections.synchronizedList(new ArrayList<NativeTaskListener>());
    protected final NativeTask task;
    private int pid;
    private Integer exitValue;
    private Throwable exception;
    private AbstractAction cancelAction,  restartAction;
    private Action[] actions;
    private ProgressHandle ph = null;
    volatile boolean submitted = false;
    final Object submisionLock = new Object();

    public NativeExecutor(NativeTask task) {
        this.task = task;
    }

    /**
     *
     * @return PID of the started process
     * @throws java.lang.Exception
     */
    abstract protected int doInvoke() throws Exception;

    abstract protected Integer doGet();

    abstract public InputStream getTaskInputStream() throws IOException;

    abstract public InputStream getTaskErrorStream() throws IOException;

    abstract public OutputStream getTaskOutputStream() throws IOException;

    public final synchronized Integer get() throws ExecutionException {
        if (exception != null) {
            throw new ExecutionException(exception);
        }

        return doGet();
    }

    public final Action[] getActions() {
        if (actions == null) {
            actions = new Action[]{
                        getAction(CANCEL_ACTION),
                        getAction(RESTART_ACTION)};
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
    static int step = 1;

    public final Integer invokeAndWait() throws Exception {
        synchronized (stateMonitor) {
            // if we are in progress, just wait for this task completion...

            if (state == TaskExecutionState.RUNNING) {
                while (state == TaskExecutionState.RUNNING) {
                    stateMonitor.wait();
                }
                return exitValue;
            }

        // else do start...
        }

        if (state != TaskExecutionState.INITIAL) {
            reset();
        }

        NativeTaskAccessor taskInfo = NativeTaskAccessor.getDefault();
        ph = taskInfo.getProgressHandler(task);

        if (ph != null) {
            ph.start();
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

            Writer w = null;
            Reader r = null;

            if (taskOut != null &&
                    (w = taskInfo.getRedirectionOutputWriter(task)) != null) {
                String name = "output from " + task.toString(); // NOI18N
                ot = new StreamRedirector(taskOut, w, name);
                ot.start();
            }

            InputStream taskErr = getTaskErrorStream();
            et = null;
            if (taskErr != null &&
                    (w = taskInfo.getRedirectionErrorWriter(task)) != null) {
                String name = "error from " + task.toString(); // NOI18N
                et = new StreamRedirector(taskErr, w, name);
                et.start();
            }

            OutputStream taskIn = getTaskOutputStream();
            it = null;
            if (taskIn != null &&
                    (r = taskInfo.getRedirectionInputReader(task)) != null) {
                String name = "input from " + task.toString(); // NOI18N
                it = new StreamRedirector(r, getTaskOutputStream(), name);
                it.start();
            }

            setState(TaskExecutionState.RUNNING);

            Integer result = get();

            if (ot != null) {
                try {
                    ot.join(1000);
                } catch (Exception e) {
                }
            }
            if (et != null) {
                try {
                    et.join(1000);
                } catch (Exception e) {
                }
            }
            if (it != null) {
                try {
                    it.join(1000);
                } catch (Exception e) {
                }
            }

            setResult(result);
        } catch (CancellationException ex) {
            exception = ex;
            setState(TaskExecutionState.CANCELLED);
        } catch (Throwable ex) {
            if (ex instanceof ExecutionException) {
                ex = ex.getCause();
            }
            exception = ex;
            setState(TaskExecutionState.ERROR);
        } finally {
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            log.fine("Task " + task + " finished with rc=" + // NOI18N
                    task.get() + " in " + duration + " s"); // NOI18N

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

    public Future<Integer> invoke(boolean waitStart) {
        synchronized (submisionLock) {
            if (submitted) {
                return task;
            }
        }

        NativeTaskExecutorService.submit(new Callable<Integer>() {

            public Integer call() {
                submitted = true;

                if (state != TaskExecutionState.INITIAL) {
                    reset();
                }

                synchronized (submisionLock) {
                    submisionLock.notifyAll();
                }

                Integer result = -1;
                try {
                    result = invokeAndWait();
                } catch (Exception ex) {
                } finally {
                    // No matter what was the reason for task competion,
                    // notify on submisionLock.
                    submitted = false;

                    synchronized (submisionLock) {
                        submisionLock.notifyAll();
                    }
                }

                return result;
            }
        });

        // Be sure that newly created thread started

        synchronized (submisionLock) {
            while (!submitted) {
                try {
                    submisionLock.wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        if (waitStart) {
            // Waiting for PID ...
            synchronized (stateMonitor) {
                if (state == TaskExecutionState.INITIAL ||
                        state == TaskExecutionState.STARTING) {
                    while (state == TaskExecutionState.INITIAL ||
                            state == TaskExecutionState.STARTING) {
                        try {
                            stateMonitor.wait();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }

        return task;
    }

    public int getPID() {
        synchronized (stateMonitor) {
            if (state == TaskExecutionState.INITIAL ||
                    state == TaskExecutionState.STARTING) {
                String err = "Task is not started yet"; // NOI18N
                throw new IllegalStateException(err);
            }

            while (state == TaskExecutionState.INITIAL) {
                try {
                    stateMonitor.wait();
                } catch (InterruptedException ex) {
                }
            }
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

    private void notifyListeners() {
        synchronized (stateMonitor) {
            log.fine("Task " + task + " changed state to " + state); // NOI18N
            NativeTaskListener[] ll =
                    taskListeners.toArray(new NativeTaskListener[0]);

            for (NativeTaskListener l : ll) {
                try {
                    switch (state) {
                        case RUNNING:
                            l.taskStarted(task);
                            break;
                        case FINISHED:
                            l.taskFinished(task, exitValue);
                            break;
                        case ERROR:
                            l.taskError(task, exception);
                            break;
                        case CANCELLED:
                            l.taskCancelled(task,
                                    (CancellationException) exception);
                            break;
                    }
                } catch (Exception e) {
                    log.severe(
                            "Exception during ExecutorTaskListener " // NOI18N
                            + l + " notification. " + e.toString()); // NOI18N
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    protected final synchronized void setResult(Integer exitValue) {
        this.exitValue = exitValue;

        if (isCancelled()) {
            return;
        }

        setState(TaskExecutionState.FINISHED);
    }

    public final boolean isCancelled() {
        return state.equals(TaskExecutionState.CANCELLED);
    }

    public final boolean isDone() {
        return !(state == TaskExecutionState.RUNNING ||
                state == TaskExecutionState.STARTING);
    }

    public final TaskExecutionState getState() {
        return state;
    }

    private void reset() {
        exception = null;
        pid = -1;
        NativeTaskAccessor.getDefault().resetTask(task);
        setState(TaskExecutionState.INITIAL);
    }

    private void setState(TaskExecutionState newState) {
        synchronized (stateMonitor) {
            if (newState == state) {
                return;
            }

            state = newState;
            notifyListeners();
            stateMonitor.notifyAll();
        }
    }

    private void updateActions() {
        synchronized (stateMonitor) {
            if (state == TaskExecutionState.RUNNING ||
                    state == TaskExecutionState.STARTING) {
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
    }

    class SimpleCancelAction extends AbstractAction {

        public SimpleCancelAction() {
            super("Cancel " + task.toString(), // NOI18N
                    ImageLoader.loadIcon("CancelTask.png")); // NOI18N
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            if (task.isRunning()) {
                cancel();
                setEnabled(false);
            }
        }
    }

    class SimpleRestartAction extends AbstractAction {

        public SimpleRestartAction() {
            super("Restart " + task.toString(), // NOI18N
                    ImageLoader.loadIcon("RerunTask.png")); // NOI18N
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            task.submit(true, true);
            setEnabled(false);
        }
    }
}
