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

import org.netbeans.modules.nativeexecution.support.NativeExecutor;
import org.netbeans.modules.nativeexecution.support.LocalNativeExecutor;
import org.netbeans.modules.nativeexecution.support.RemoteNativeExecutor;
import org.netbeans.modules.nativeexecution.util.HostInfo;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.support.IOTabManagerFactory;
import org.netbeans.modules.nativeexecution.support.NativeTaskAccessor;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 * A cancellable asynchronous native task.
 */
public final class NativeTask implements Future<Integer> {

    private String command;
    private final ExecutionEnvironment execEnv;
    private NativeExecutor executor;
    private Writer redirectionOutputWriter;
    private Writer redirectionErrorWriter;
    private Reader redirectionInputReader;
    private InputOutput redirectionIO = null;
    private boolean showProgress;


    static {
        NativeTaskAccessor.setDefault(new NativeTaskAccessorImpl());
    }

    /**
     * Creates <tt>NativeTask</tt> that can be executed in the
     * specified <tt>ExecutionEnvironment</tt>.
     *
     * System property <tt>dlight.nativetask.error.redirector</tt> controls
     * default process' error stream redirection behavior. If it is set to
     * <tt>true</tt>, any process' error will be redirected to
     * <tt>System.err</tt> (unless other redirection is setted up by
     * <tt>redirectErrTo()</tt> method).
     *
     * @param execEnv <tt>ExecutionEnvironment</tt> to execute task in.
     * @param cmd command to execute
     * @param args command's arguments
     */
    public NativeTask(final ExecutionEnvironment execEnv,
            final String cmd, final String[] args) {
        this.execEnv = execEnv;

        if (cmd == null) {
            System.out.println(Arrays.toString(args));
        }
        if (args != null) {
            StringBuilder sb = new StringBuilder(cmd);

            for (String arg : args) {
                sb.append(" ").append(arg); // NOI18N
            }

            this.command = sb.toString();
        } else {
            this.command = cmd;
        }

        boolean redirectError = Boolean.valueOf(System.getProperty(
                "dlight.nativetask.error.redirector", // NOI18N
                "false")); // NOI18N
        this.redirectionErrorWriter = redirectError
                ? new ErrorWriter(System.err) : null;
        this.redirectionOutputWriter = null;
        this.redirectionInputReader = null;

        executor = execEnv.isRemote()
                ? new RemoteNativeExecutor(this)
                : new LocalNativeExecutor(this);
    }

    /**
     * Creates NativeTask to be executed on the localhost.
     *
     * @param cmd command to execute
     */
    public NativeTask(String cmd) {
        this(new ExecutionEnvironment(null, HostInfo.LOCALHOST), cmd, null);
    }

    /**
     * Starts execution of <tt>NativeTask</tt> and waits for its completion.
     * @param showProgress
     * @return result (exit value) of underlaying native process
     * @throws java.lang.Exception when some exception occurs during execution.
     */
    final public Integer invoke(boolean showProgress) throws Exception {
        this.showProgress = showProgress;
        return executor.invokeAndWait();
    }

    /**
     * Asynchronously starts the task and, maybe, waits for PID.
     * @param waitStart - defines wheteher wait for task's PID or not
     * @param showProgress - if set to <tt>true</tt>, progress bar will be
     * displayed.
     */
    final public void submit(boolean waitStart, boolean showProgress) {
        this.showProgress = showProgress;
        executor.invoke(waitStart);
    }

    /**
     * Returns <tt>ExecutionEnvironment</tt> that is used by this task.
     * @return <tt>ExecutionEnvironment</tt> that is used by this task.
     */
    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    /**
     * Returns command that is executed by this task.
     * @return command that is executed by this task.
     */
    final public String getCommand() {
        return command;
    }

    /**
     * Returns native task's process ID
     * @return PID of underlaying native process
     */
    final public int getPID() {
        return executor.getPID();
    }

    /**
     * Set <tt>Reader</tt> do be used for input stream redirection. It is
     * allowable to pass <tt>null</tt>. In this case no input stream redirection
     * will occur.
     *
     * @param inReader <tt>Reader</tt> do be used for input stream redirection
     */
    public void redirectInFrom(Reader inReader) {
        this.redirectionInputReader = inReader;
    }

    /**
     * Set <tt>Writer</tt> do be used for error stream redirection. It is
     * allowable to pass <tt>null</tt>. In this case no error stream redirection
     * will occur.
     *
     * @param errWriter <tt>Writer</tt> do be used for error stream redirection
     */
    final public void redirectErrTo(Writer errWriter) {
        this.redirectionErrorWriter = errWriter;
    }

    /**
     * Set <tt>Writer</tt> do be used for output stream redirection. It is
     * allowable to pass <tt>null</tt>. In this case no output stream
     * redirection will occur.
     *
     * @param outWriter <tt>Writer</tt> do be used for output stream redirection
     */
    final public void redirectOutTo(Writer outWriter) {
        this.redirectionOutputWriter = outWriter;
    }

    /**
     * Adds <tt>NativeTask</tt> listener.
     * @param listener to add.
     */
    public void addListener(NativeTaskListener listener) {
        executor.addListener(listener);
    }

    /**
     * Removes <tt>NativeTask</tt> listener.
     * @param listener to remove.
     */
    public void removeListener(NativeTaskListener listener) {
        executor.removeListener(listener);
    }

    /**
     * Returns task's input stream.
     *
     * @return task's input stream.
     * @throws java.io.IOException
     */
    public InputStream getInputStream() throws IOException {
        return executor.getTaskInputStream();
    }

    /**
     * Returns task's error stream
     * @return task's error stream
     * @throws java.io.IOException
     */
    public InputStream getErrorStream() throws IOException {
        return executor.getTaskErrorStream();
    }

    /**
     * Returns task's output stream
     * @return task's output stream
     * @throws java.io.IOException
     */
    public OutputStream getOutputStream() throws IOException {
        return executor.getTaskOutputStream();
    }

    /**
     * Redirect all I/O streams to an {@link org.openide.windows.InputOutput}.
     * @param reuseTab if set to <tt>true</tt> previous tab for this task will
     * be reused.
     */
    final public void setInputOutput(final boolean reuseTab) {
        this.redirectionIO =
                IOTabManagerFactory.getIOTabManager().getIO(this, reuseTab);

        redirectOutTo(redirectionIO.getOut());
        redirectErrTo(redirectionIO.getErr());
        redirectInFrom(redirectionIO.getIn());

        redirectionIO.setInputVisible(true);
    }

    /**
     * Returns task's actions.
     * @return task's actions (like 'Cancel' and 'Restart').
     */
    private Action[] getTaskControlActions() {
        return executor.getActions();
    }

    /**
     * Returns current state of the task
     * @return task's current <tt>TaskExecutionState</tt>
     */
    final public TaskExecutionState getState() {
        return executor.getState();
    }

    void reset() {
        if (redirectionIO != null) {
            try {
                redirectionIO.getOut().reset();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            redirectionIO.setInputVisible(true);
            redirectionIO.setOutputVisible(true);
            redirectionIO.setErrVisible(true);
            redirectionIO.select();
        }
    }

    /**
     * Returns string representation of the <tt>NativeTask</tt>.
     * @return string representation of the <tt>NativeTask</tt>.
     */
    @Override
    public String toString() {
        return command + " [" + execEnv.toString() + "]"; // NOI18N
    }

    /**
     * Terminates running task
     *
     * @param mayInterruptIfRunning true if the thread executing this task
     *        should be interrupted; otherwise, in-progress tasks are allowed to
     *        complete <b>Not used in current implementation</b>
     *
     * @return true if task has been terminated
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        // TODO: xxx mayInterruptIfRunning
        return executor.cancel();
    }

    /**
     * Returns true if this task was cancelled before it completed normally.
     * @return true if task was cancelled before it completed.
     */
    public boolean isCancelled() {
        return executor.isCancelled();
    }

    /**
     * Returns true if this task completed. Completion may be due to normal
     * termination, an exception, or cancellation -- in all of these cases,
     * this method will return true.
     *
     * @return true if this task completed.
     */
    public boolean isDone() {
        return executor.isDone();
    }

    /**
     * Returns true if this task is starting or running.
     * @return true if this task is starting or running.
     */
    public boolean isRunning() {
        return !isDone();
    }

    /**
     * Waits if necessary for the execution to complete, and returns native
     * process' exit code.
     *
     * @return the exit status of the underlaying native task
     *
     * @throws java.lang.InterruptedException if the current thread was
     *         interrupted while waiting
     * @throws java.util.concurrent.ExecutionException if some exception occured
     *         while executing
     */
    public Integer get() throws InterruptedException, ExecutionException {
        return executor.get();
    }

    /**
     * Not implemented yet
     * @param timeout
     * @param unit
     * @return xxx
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws java.util.concurrent.TimeoutException
     */
    public Integer get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    private static class ErrorWriter extends PrintWriter {

        public ErrorWriter(OutputStream out) {
            super(out);
        }

        @Override
        public void write(String s) {
            super.write("TaskError: " + s); // NOI18N
        }
    }

    private static class NativeTaskAccessorImpl extends NativeTaskAccessor {

        @Override
        public NativeExecutor getExecutor(NativeTask task) {
            return task.executor;
        }

        @Override
        public ProgressHandle getProgressHandler(final NativeTask task) {
            return (task.showProgress
                    ? ProgressHandleFactory.createHandle(task.toString(),
                    task.executor,
                    task.redirectionIO == null ? null : new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    task.redirectionIO.select();
                }
            }) : null);
        }

        @Override
        public Writer getRedirectionErrorWriter(NativeTask task) {
            return task.redirectionErrorWriter;
        }

        @Override
        public Reader getRedirectionInputReader(NativeTask task) {
            return task.redirectionInputReader;
        }

        @Override
        public Writer getRedirectionOutputWriter(NativeTask task) {
            return task.redirectionOutputWriter;
        }

        @Override
        public void resetTask(NativeTask task) {
            task.reset();
        }

        @Override
        public Action[] getTaskControlActions(NativeTask task) {
            return task.getTaskControlActions();
        }
    }
}
