/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.otool.debugger.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.otool.debugger.utils.ExecutorCND;


import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * A combination process/process factory object.
 */
public abstract class Executor {

    private final String name;
    private final ExecutionEnvironment host;

    private boolean destroyedByHand;

    protected Executor(String name, ExecutionEnvironment host) {
        this.name = name;
        this.host = host;
    }

    public final String name() {
        return name;
    }

    public ExecutionEnvironment host() {
        return host;
    }

    public boolean isRemote() {
        return host.isRemote();
    }

    public abstract int getExitValue();

    public abstract ExecutionEnvironment getExecutionEnvironment();

    public abstract boolean isAlive();

    public abstract void terminate() throws IOException;

    public abstract void interrupt() throws IOException;

    public abstract void interrupt(int pid) throws IOException;

    public abstract void interruptGroup() throws IOException;

    public abstract void sigqueue(int sig, int data) throws IOException;

    public abstract String readlink(long pid);

    public abstract String readlsof(long pid);

    public abstract String readDirLink(long pid);

    public abstract boolean is_64(String p);

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public abstract int startShellCmd(String engine_argv[]);

    public abstract int startEngine(String engine_path, String engine_argv[], Map<String, String> additionalEnv,
            String workDir, boolean usePty, boolean disableEcho);

    public abstract String getStartError();

    /**
     * Wait for command started with either startShellCmd() or startEngine().
     */
    protected abstract int waitForEngine() throws InterruptedException;

    protected void destroyEngine() {
        destroyedByHand = true;
    }

    /**
     * Start a thread to wait for the process started by this Executor to
     * terminate. Post an error if exits abnormally.
     */
    public abstract void reap();

    public boolean destroyedByHand() {
        return destroyedByHand;
    }

    public static final int NOPTY = 1;

    public static Executor getDefault(String name, ExecutionEnvironment host, int flags) {
        return getDefault(name, host, flags, null);
    }

    public static Executor getDefault(String name, ExecutionEnvironment host, int flags, ChangeListener changeListener) {
        return new ExecutorCND(name, host, changeListener);
    }

    /**
     * Tracker runs under a org.openide.execution.ExecutorTask which creates a
     * presence in the NB RuntimeTab under Processes. This allows the user to
     * terminate the process. User action results in a call to ExecutorTask.stop
     * which, in turn will .... - interrupt the thread, which we catch with
     * InterruptedException - stop the thread, which we'll catch with
     * ThreadDeath.
     *
     * When running under ExecutorTask all standard io is dynamically redirected
     * to the InputOutput which was passed to ExecutionEngine.execute(). Since
     * we pass InputOutput.NULL any io originating in this thread is effectively
     * discarded (and apparently not even logged). This is why we have
     * safeNotify() send the stuff over to the AWT eventQ.
     */
    static private class Tracker implements Runnable {

        private final Executor executor;

        public Tracker(Executor executor) {
            this.executor = executor;
        }

        private void safeNotify(final String msg, final Throwable t) {
            /* DEBUG
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    ErrorManager.getDefault().annotate(t, msg);
		    ErrorManager.getDefault().notify(t);
		}
	    });
             */
        }

        private void print(final String msg) {
            if (true) {
                return;
            }
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    System.out.printf(msg);
                }
            });
        }

        // interface Runnable
        @Override
        public void run() {
            try {
                if (false) {
                    System.out.printf("Tracker running ...\n"); // NOI18N
                }
                boolean done = false;
                while (!done) {
                    try {
                        executor.waitForEngine();
                        System.out.printf("Tracker done.\n"); // NOI18N
                        done = true;

                    } catch (java.lang.InterruptedException e) {
                        // We get here if Thread.interrupt() is used
                        print("Task.run(): InterruptedException\n"); // NOI18N
                        safeNotify("Task.runHelp(): ", e); // NOI18N
                    }
                }
                // normal termination
                // OLD executor.cleanup();

            } catch (java.lang.ThreadDeath e) {
                // We get here if Thread.stop() is used
                safeNotify("Task.runHelp(): ", e); // NOI18N
                print("Task.run(): ThreadDeath\n"); // NOI18N
                executor.destroyEngine();
                // OLD executor.cleanup();
                throw e;	// very important to re-throw

            }
        }
    }

    /**
     * Monitor output from process coming via pty and send it to output window.
     */
    protected static class OutputMonitor extends java.lang.Thread {

        private final InputStreamReader reader;
        private final Writer writer;

        private static int nextSerial = 0;
        private int serial = nextSerial++;

        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        OutputMonitor(InputStreamReader reader, Writer writer) {
            super("ExecutorUnix.OutputMonitor");	// NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        // interface Thread
        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }
                    writer.write(buf, 0, nread);
                }
            } catch (IOException e) {
                // ErrorManager.getDefault().notify(e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    // ErrorManager.getDefault().notify(e);
                }
            }
        }
    }

    /**
     * Monitor input from output window and send it to process via pty.
     */
    protected static class InputMonitor extends java.lang.Thread {

        private final Reader reader;
        private final Writer writer;

        private static int nextSerial = 0;
        private int serial = nextSerial++;

        private static final int BUFSZ = 1024;
        private final char[] buf = new char[BUFSZ];

        InputMonitor(Reader reader, Writer writer) {
            super("ExecutorUnix.InputMonitor");	// NOI18N
            this.reader = reader;
            this.writer = writer;

            // see bug 4921071
            setPriority(1);
        }

        // interface Thread
        @Override
        public void run() {
            try {
                while (true) {
                    int nread = reader.read(buf, 0, BUFSZ);
                    if (nread == -1) {
                        break;
                    }

                    writer.write(buf, 0, nread);
                    writer.flush();
                }
                writer.close();
            } catch (Exception e) {
                //	ErrorManager.getDefault().notify(e);
            }

        }
    }
}
