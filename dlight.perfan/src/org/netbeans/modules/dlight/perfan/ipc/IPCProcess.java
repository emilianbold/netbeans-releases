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
package org.netbeans.modules.dlight.perfan.ipc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.openide.util.Exceptions;

/**
 * Process which delegates unix Runtime.exec process
 * for er_print -IPC
 *
 */
public final class IPCProcess extends Process {

    private final NativeTask delegate;
    private final OutputStream stdin;
    private final InputStream stdout;
    private final InputStream stderr;
    private ExitMonitor monitor;
    public ProcListener a_Proc_Listener;

    public IPCProcess(final NativeTask delegate, final OutputStream stdin, final InputStream stdout, final InputStream stderr) {
        this.delegate = delegate;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public InputStream getErrorStream() {
        return stderr;
    }

    public OutputStream getOutputStream() {
        return stdin;
    }

    public InputStream getInputStream() {
        return stdout;
    }

    public int waitFor() {
        int status = -1;

        try {
            status = delegate.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return status;
    }

    public int exitValue() throws IllegalThreadStateException {
        Integer result = -1;

        try {
            result = delegate.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    public void destroy() {
        delegate.cancel();
    }

    public synchronized void setExitListener() {
        a_Proc_Listener = new ProcListener();

        if (monitor != null) {
            monitor.interrupt();
            monitor = null;
        }

        monitor = new ExitMonitor(this, a_Proc_Listener);
        monitor.start();
    }

    public synchronized void removeExitListener() {
        a_Proc_Listener = null;

        if (monitor != null) {
            monitor.interrupt();
            monitor = null;
        }
    }

    /*-------------------------------------    INNER CLASSES    ----------------------------------------------------*/
    /**
     * Class for catching process exit.
     * @see ExitListener
     */
    public final class ProcListener implements ExitListener {

        public ProcListener() {
        }

        /**
         * Implements method processExited of ExitListener interface
         * @param p - external process
         * Method prints error message and cleanups analyzer (standalone)
         * or close TopComponent (netbeans module).
         * @see ExitListener
         */
        public void processExited(final Process p) {
            String msg;
            int exit_value = -1;

            try {
                exit_value = p.exitValue();
            } catch (IllegalThreadStateException ex) {
            } // process has already exited

            switch (exit_value) {
                case -1:
                    msg = null; // process has not yet terminated, default exit value
                    break;
                case 0:
                    msg = "Communication channel will be closed"; //NOI18N
                    break;
                case 2: //SIGINT
                    msg = "er_print has been interrupted."; //NOI18N
                    break;
                case 3: //SIGQUIT
                    msg = "er_print has quit."; //NOI18N
                    break;
                case 4: //SIGILL
                    msg = "Illegal instruction in er_print."; //NOI18N
                    break;
                case 5: //SIGTRAP
                    msg = "Trace/Breakpoint trap in er_print."; //NOI18N
                    break;
                case 6: //SIGABRT
                    msg = "er_print has been aborted."; //NOI18N
                    break;
                case 7: //SIGEMT
                    msg = "Emulation trap in er_print."; //NOI18N
                    break;
                case 8: //SIGFPE
                    msg = "Floating point exception in er_print."; //NOI18N
                    break;
                case 9: //SIGKILL
                    msg = "er_print has been killed."; //NOI18N
                    break;
                case 10: //SIGBUS
                    msg = "Bus Error in er_print."; //NOI18N
                    break;
                case 11: //SIGSEGV
                    msg = "Segmentation Fault in er_print."; //NOI18N
                    break;
                case 15: //SIGTERM
                    msg = "er_print has been terminated."; //NOI18N
                    break;
                case 16: //SIGUSR1
                    msg = "Out of memory Error in er_print."; //NOI18N
                    break;
                default: // All other signals
                    msg = "er_print has exited unexpectedly."; //NOI18N
                    break;
            }

            if (exit_value != -1) {
                msg.concat(" Exit status is " + exit_value); //NOI18N
            }

            System.out.println("IPCProcess exited!   " + msg.toString());


//      handler.endConnection(msg);

            try {
                stdin.close();
                stdout.close();
                stderr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Monitor class for listening external process to catch process exit.
     */
    public final class ExitMonitor extends Thread {

        private final Process proc;
        private final ExitListener listener;

        /**
         * Monitor constructor
         * @param proc - external process for listening
         * @param listener - listener interface
         * @see ExitListener
         */
        ExitMonitor(final Process proc, final ExitListener listener) {
            this.proc = proc;
            this.listener = listener;
        }

        @Override
        public final void run() {
            try {
                proc.waitFor();
            } catch (InterruptedException ex) { // ignore monitor interruption
            }
            listener.processExited(proc);
            monitor = null;
        }
    }

    public interface ExitListener {

        public void processExited(Process p);
    }
}
