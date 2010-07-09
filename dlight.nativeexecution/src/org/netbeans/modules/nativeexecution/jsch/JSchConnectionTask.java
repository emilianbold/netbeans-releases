/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution.jsch;

import com.jcraft.jsch.JSch;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.jsch.JSchConnectionTask.Problem;
import org.netbeans.modules.nativeexecution.support.Authentication;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.support.ui.AuthTypeSelectorDlg;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 * not thread safe. but always is executed in a single-thread executor
 * @author ak119685
 */
public final class JSchConnectionTask implements Callable<JSchChannelsSupport>, Cancellable {

    private static final int SOCKET_CREATION_TIMEOUT = Integer.getInteger("socket.connection.timeout", 10000); // NOI18N
    // ------------------------------------------------------------------------
    private final JSch jsch;
    private final ExecutionEnvironment env;
    private Problem problem;
    private volatile boolean cancelled;

    public JSchConnectionTask(final JSch jsch, final ExecutionEnvironment env) {
        this.jsch = jsch;
        this.env = env;
        cancelled = false;
    }

    @Override
    public JSchChannelsSupport call() throws Exception {
        try {
            try {
                env.prepareForConnection();
            } catch (Throwable th) {
                problem = new Problem(ProblemType.ENV_PREPARE_ERROR, th);
                return null;
            }

            if (cancelled) {
                problem = new Problem(ProblemType.CONNECTION_CANCELLED);
                return null;
            }

            if (!isReachable()) {
                problem = new Problem(ProblemType.HOST_UNREACHABLE);
                return null;
            }

            if (cancelled) {
                problem = new Problem(ProblemType.CONNECTION_CANCELLED);
                return null;
            }

            if (!initJsch(env)) {
                problem = new Problem(ProblemType.CONNECTION_CANCELLED);
                return null;
            }

            // Start special shell session that will serve administrative tasks
            // like sending signals to processes...

            JSchChannelsSupport cs = new JSchChannelsSupport(jsch, env);
            cs.connect();

            // OK. Connection established.

            return cs;
        } catch (Throwable th) {
            Exceptions.printStackTrace(th);
            problem = new Problem(ProblemType.CONNECTION_FAILED, th);
        }

        return null;
    }

    private static boolean initJsch(ExecutionEnvironment env) {
        Authentication auth = Authentication.getFor(env);

        if (!auth.isDefined()) {
            AuthTypeSelectorDlg dlg = new AuthTypeSelectorDlg();
            if (!dlg.initAuthentication(auth)) {
                return false;
            }
        } else {
            auth.apply();
        }

        return true;
    }

    public Problem getProblem() {
        return problem;
    }

    private boolean isReachable() throws IOException {
        // IZ#165591 - Trying to connect to wrong host breaks remote host setup (for other hosts)
        // To prevent this first try to just open a socket and
        // go to the jsch code in case of success only.

        // The important thing here is that we still need to be interruptable
        // In case of wrong IP address (unreachable) the SocketImpl's connect()
        // method may hang in system call for a long period of time, being
        // insensitive to interrupts.
        // So do this in a separate thread...

        Callable<Boolean> checker = new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                final Socket socket = new Socket();
                final SocketAddress addressToConnect =
                        new InetSocketAddress(env.getHostAddress(), env.getSSHPort());
                try {
                    socket.connect(addressToConnect, SOCKET_CREATION_TIMEOUT);
                } catch (Exception ioe) {
                    return false;
                } finally {
                    socket.close();
                }
                return true;
            }
        };

        final Future<Boolean> task = NativeTaskExecutorService.submit(
                checker, "Host " + env.getHost() + " availability test"); // NOI18N

        while (!cancelled && !task.isDone()) {
            try {
                task.get(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                // normally should never happen
            } catch (ExecutionException ex) {
                // normally should never happen
            } catch (TimeoutException ex) {
                // OK.. still be waiting
            }
        }

        boolean result = false;

        if (task.isDone()) {
            try {
                result = task.get();
            } catch (Exception ex) {
                // normally should never happen
            }
        }

        return result;
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public static final class Problem {

        public final ProblemType type;
        public final Throwable cause;

        public Problem(ProblemType type) {
            this(type, null);
        }

        public Problem(ProblemType type, Throwable cause) {
            this.type = type;
            this.cause = cause;
        }
    }

    public static enum ProblemType {

        ENV_PREPARE_ERROR,
        AUTH_FAIL,
        HOST_UNREACHABLE,
        CONNECTION_CANCELLED,
        CONNECTION_FAILED,
        CONNECTION_TIMEOUT,
    }
}
