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
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.netbeans.modules.nativeexecution.support.RemoteUserInfoProvider;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ak119685
 */
public final class ConnectionManager {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final boolean USE_JZLIB = Boolean.getBoolean("jzlib"); // NOI18N
    private static final int JSCH_CONNECTION_TIMEOUT = Integer.getInteger("jsch.connection.timeout", 10000); // NOI18N
    private static final int SOCKET_CREATION_TIMEOUT = Integer.getInteger("socket.connection.timeout", 10000); // NOI18N
    private static final boolean isUnitTest = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    // Connections are always established sequently in connectorThread
    private static final RequestProcessor connectorThread = new RequestProcessor("ConnectionManager queue", 1); // NOI18N
    // Map that contains all connected sessions;
    private static final HashMap<ExecutionEnvironment, Session> sessions = new HashMap<ExecutionEnvironment, Session>();
    // Actual sessions pool
    private static final JSch jsch = new JSch();
    private static List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
    // Instance of the ConnectionManager
    private static final ConnectionManager instance = new ConnectionManager();

    static {
        ConnectionManagerAccessor.setDefault(new ConnectionManagerAccessorImpl());
    }

    private ConnectionManager() {
        // init jsch logging

        if (log.isLoggable(Level.FINEST)) {
            JSch.setLogger(new com.jcraft.jsch.Logger() {

                @Override
                public boolean isEnabled(int level) {
                    return true;
                }

                @Override
                public void log(int level, String message) {
                    log.log(Level.FINEST, "JSCH: {0}", message); // NOI18N
                }
            });
        }

        try {
            jsch.setKnownHosts(System.getProperty("user.home") + // NOI18N
                    "/.ssh/known_hosts"); // NOI18N
        } catch (JSchException ex) {
            log.log(Level.WARNING, "Unable to setKnownHosts for jsch. {0}", ex.getMessage()); // NOI18N
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.remove(listener);
    }

    private void fireConnected(ExecutionEnvironment execEnv) {
        // No need to lock - use thread-safe collection
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connected(execEnv);
        }
    }

    private void fireDisconnected(ExecutionEnvironment execEnv) {
        // No need to lock - use thread-safe collection
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.disconnected(execEnv);
        }
    }

    /**
     * Tests whether the connection with the <tt>execEnv</tt> is established or
     * not.
     * @param execEnv execution environment to test connection with.
     * @return true if connection is established or if execEnv refers to the
     * localhost environment. false otherwise.
     */
    public boolean isConnectedTo(final ExecutionEnvironment execEnv) {
        if (execEnv.isLocal()) {
            return true;
        }

        synchronized (sessions) {
            if (sessions.containsKey(execEnv)) {
                return sessions.get(execEnv).isConnected();
            }

            return false;
        }
    }

    /**
     *
     * @param env <tt>ExecutionEnvironment</tt> to connect to.
     * @throws IOException
     * @throws CancellationException
     */
    public void connectTo(final ExecutionEnvironment env) throws IOException, CancellationException {
        connectTo(env, PasswordManager.getInstance().get(env));
    }

    /**
     *
     * @param env <tt>ExecutionEnvironment</tt> to connect to.
     * @param password password to be used for identification
     * @throws java.lang.Throwable
     */
    public synchronized void connectTo(
            final ExecutionEnvironment env,
            final char[] password) throws IOException, CancellationException {

        if (SwingUtilities.isEventDispatchThread()) {
            // otherwise UI can hang forever
            throw new IllegalThreadStateException("Should never be called from AWT thread"); // NOI18N
        }

        if (isConnectedTo(env)) {
            return;
        }

        if (password != null) {
            PasswordManager.getInstance().put(env, password);
        }

        int attempts = 2;

        while (attempts > 0) {
            if (doConnect(env)) {
                break;
            }

            // For non-unit-tests-mode will itterate
            // while either connection is established or
            // is cancelled implicitly.

            if (isUnitTest) {
                attempts--;
            }
        }

    }

    private boolean doConnect(final ExecutionEnvironment env) throws IOException, CancellationException {

        final ConnectionTask connectionTask = new ConnectionTask(env);
        final Future<Session> connectionTaskResult = connectorThread.submit(connectionTask);
        final ProgressHandle ph = ProgressHandleFactory.createHandle(
                loc("ConnectionManager.Connecting", // NOI18N
                env.toString()), connectionTask);

        ph.start();

        Session session = null;

        try {
            session = connectionTaskResult.get();
        } catch (InterruptedException ex) {
            // should not occur
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            // should not occur
            // any exception thrown from the connectionTask will be wrapped
            // in this one...
            Exceptions.printStackTrace(ex);
        } finally {
            ph.finish();
        }

        if (connectionTask.cancelled) {
            throw new CancellationException("Connection cancelled"); // NOI18N
        }

        if (connectionTask.problem != null) {
            if (connectionTask.problem == Problem.AUTH_FAIL) {
                PasswordManager.getInstance().clearPassword(env);
                return false;
            } else if (connectionTask.problem == Problem.CONNECTION_TIMEOUT) {
                throw new IOException("Connection timeout"); // NOI18N
            } else {
                throw new IOException(connectionTask.problem.name());
            }
        }

        assert session != null;

        synchronized (sessions) {
            sessions.put(env, session);
        }

        HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
        log.log(Level.FINE, "New connection established: {0} - {1}", new String[]{env.toString(), hostInfo.getOS().getName()}); // NOI18N

        fireConnected(env);

        return true;
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    /**
     * Returns {@link Action javax.swing.Action} that can be used
     * to get connected to the {@link ExecutionEnvironment}.
     * It is guaranteed that the same Action is returned for equal execution
     * environments.
     *
     * @param execEnv - {@link ExecutionEnvironment} to connect to.
     * @param onConnect - Runnable that is executed when connection is
     *        established.
     * @return action to be used to connect to the <tt>execEnv</tt>.
     * @see Action
     */
    public AsynchronousAction getConnectToAction(
            final ExecutionEnvironment execEnv, final Runnable onConnect) {

        return new ConnectToAction(execEnv, onConnect);
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(ConnectionManager.class, key, params);
    }

    /**
     *
     * @param execEnv
     * @param restoreLostConnection
     * @return returns previously created session.
     * If execEnv was not connected before - returns null
     * disregarding the restoreLostConnection flag.
     * It also could return null if attempt to restore
     * lost connection failed.
     */
    private Session getSession(ExecutionEnvironment execEnv, boolean restoreLostConnection) {
        synchronized (sessions) {
            if (sessions.containsKey(execEnv)) {
                Session session = sessions.get(execEnv);
                if (!restoreLostConnection || session.isConnected()) {
                    return session;
                }
                try {
                    reconnect(execEnv);
                    return getSession(execEnv, false);
                } catch (IOException ex) {
                    return null;
                }
            }

            return null;
        }
    }

    private void reconnect(ExecutionEnvironment env) throws IOException {
        synchronized (sessions) {
            disconnect(env);
            connectTo(env);
        }
    }

    public void disconnect(ExecutionEnvironment env) {
        synchronized (sessions) {
            Session session = sessions.remove(env);
            if (session != null) {
                session.disconnect();
                fireDisconnected(env);
            }
        }
    }

    private static final class ConnectionTask implements Callable<Session>, Cancellable {

        private final ExecutionEnvironment env;
        private volatile boolean cancelled = false;
        private volatile Problem problem = null;

        private ConnectionTask(final ExecutionEnvironment env) {
            this.env = env;
        }

        @Override
        public Session call() throws Exception {
            Session newSession = null;
            try {
                try {
                    env.prepareForConnection();
                } catch (Throwable th) {
                    problem = Problem.ENV_PREPARE_ERROR;
                    return null;
                }

                if (cancelled) {
                    return null;
                }

                if (!isReachable()) {
                    problem = Problem.HOST_UNREACHABLE;
                    return null;
                }

                if (cancelled) {
                    return null;
                }

                char[] passwd = PasswordManager.getInstance().get(env);
                boolean askForPassword = !isUnitTest && passwd == null;
                UserInfo userInfo = RemoteUserInfoProvider.getUserInfo(env, askForPassword);

                newSession = jsch.getSession(env.getUser(), env.getHostAddress(), env.getSSHPort());
                newSession.setUserInfo(userInfo);

                if (USE_JZLIB) {
                    newSession.setConfig("compression.s2c", "zlib@openssh.com,zlib,none"); // NOI18N
                    newSession.setConfig("compression.c2s", "zlib@openssh.com,zlib,none"); // NOI18N
                    newSession.setConfig("compression_level", "9"); // NOI18N
                }

                try {
                    // connect methods tries to handle auth fail
                    // itself by re-asking for password 6 times
                    // only after 6th attempt it throws an exception
                    newSession.connect(JSCH_CONNECTION_TIMEOUT);
                } catch (JSchException e) {
                    if (e.getMessage().equals("Auth fail")) { // NOI18N
                        problem = Problem.AUTH_FAIL;
                        return null;
                    } else if (e.getMessage().equals("Auth cancel")) { // NOI18N
                        cancelled = true;
                        return null;
                    } else if (e.getMessage().contains("java.net.SocketTimeoutException") // NOI18N
                            || e.getMessage().contains("timeout")) { // NOI18N
                        problem = Problem.CONNECTION_TIMEOUT;
                        return null;
                    }
                    problem = Problem.CONNECTION_FAILED;
                } catch (CancellationException ex) {
                    cancelled = true;
                    return null;
                }
            } catch (Throwable th) {
                Exceptions.printStackTrace(th);
                problem = Problem.CONNECTION_FAILED;
            }

            return newSession;
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
    }

    /**
     * onConnect will be invoked ONLY if this action has initiated a new
     * connection.
     */
    private static class ConnectToAction
            extends AbstractAction implements AsynchronousAction {

        private final static ConnectionManager cm = ConnectionManager.getInstance();
        private final ExecutionEnvironment env;
        private final Runnable onConnect;

        private ConnectToAction(ExecutionEnvironment execEnv, Runnable onConnect) {
            this.env = execEnv;
            this.onConnect = onConnect;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NativeTaskExecutorService.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        invoke();
                    } catch (Throwable ex) {
                        log.warning(ex.getMessage());
                    }
                }
            }, "Connecting to " + env.toString()); // NOI18N
        }

        @Override
        public synchronized void invoke() throws IOException, CancellationException {
            if (cm.isConnectedTo(env)) {
                return;
            }

            cm.connectTo(env);
            onConnect.run();
        }
    }

    private enum Problem {

        ENV_PREPARE_ERROR,
        AUTH_FAIL,
        HOST_UNREACHABLE,
        CONNECTION_FAILED,
        CONNECTION_TIMEOUT,
    }

    private static final class ConnectionManagerAccessorImpl
            extends ConnectionManagerAccessor {

        @Override
        public Session getConnectionSession(ExecutionEnvironment env, boolean restoreLostConnection) {
            return instance.getSession(env, restoreLostConnection);
        }

        @Override
        public void reconnect(final ExecutionEnvironment env) throws IOException {
            instance.reconnect(env);
        }
    }
}
