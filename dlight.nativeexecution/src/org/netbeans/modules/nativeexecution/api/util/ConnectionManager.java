/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.ConnectionManagerAccessor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.jsch.JSchChannelsSupport;
import org.netbeans.modules.nativeexecution.jsch.JSchConnectionTask;
import org.netbeans.modules.nativeexecution.spi.support.JSchAccess;
import org.netbeans.modules.nativeexecution.support.Authentication;
import org.netbeans.modules.nativeexecution.support.HostConfigurationPanel;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.NativeTaskExecutorService;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author ak119685
 */
public final class ConnectionManager {

    public static class CancellationException extends Exception {

        public CancellationException() {
        }

        public CancellationException(String message) {
            super(message);
        }
    }
    private static final java.util.logging.Logger log = Logger.getInstance();
    // Actual sessions pools. One per host
    private static final ConcurrentHashMap<ExecutionEnvironment, JSchChannelsSupport> channelsSupport = new ConcurrentHashMap<ExecutionEnvironment, JSchChannelsSupport>();
    private static List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
    private static final Object channelsSupportLock = new Object();
    private static HashMap<ExecutionEnvironment, ConnectToAction> connectionActions = new HashMap<ExecutionEnvironment, ConnectToAction>();
    // Instance of the ConnectionManager
    private static final ConnectionManager instance = new ConnectionManager();
    private static final ConcurrentHashMap<ExecutionEnvironment, JSch> jschPool =
            new ConcurrentHashMap<ExecutionEnvironment, JSch>();
    private final ConcurrentHashMap<ExecutionEnvironment, JSchConnectionTask> connectionTasks =
            new ConcurrentHashMap<ExecutionEnvironment, JSchConnectionTask>();
    private static final boolean UNIT_TEST_MODE = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    private final ConnectionContinuation DEFAULT_CC;
    private final AbstractList<ExecutionEnvironment> recentConnections = new ArrayList<ExecutionEnvironment>();

    static {
        ConnectionManagerAccessor.setDefault(new ConnectionManagerAccessorImpl());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }));
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

        DEFAULT_CC = new ConnectionContinuation() {
            @Override
            public void connectionEstablished(ExecutionEnvironment env) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ConnectionManager.class, "ConnectionManager.status.established", env.getDisplayName())); // NOI18N
            }

            @Override
            public void connectionCancelled(ExecutionEnvironment env) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ConnectionManager.class, "ConnectionManager.status.cancelled", env.getDisplayName())); // NOI18N
            }

            @Override
            public void connectionFailed(ExecutionEnvironment env, IOException ex) {
                String message = NbBundle.getMessage(ConnectionManager.class, "ConnectionManager.status.failed", env.getDisplayName(), ex.getLocalizedMessage()); // NOI18N
                StatusDisplayer.getDefault().setStatusText(message);
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
        };

        restoreRecentConnectionsList();
    }

    public void addConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        // No need to lock - use thread-safe collection
        connectionListeners.remove(listener);
    }

    public List<ExecutionEnvironment> getRecentConnections() {
        synchronized (recentConnections) {
            return Collections.unmodifiableList(new ArrayList<ExecutionEnvironment>(recentConnections));
        }
    }

    /*package-local for test purposes*/ void updateRecentConnectionsList(ExecutionEnvironment execEnv) {
        synchronized (recentConnections) {
            recentConnections.remove(execEnv);
            recentConnections.add(0, execEnv);
            storeRecentConnectionsList();
        }
    }

    /*package-local for test purposes*/ void storeRecentConnectionsList() {
        Preferences prefs = NbPreferences.forModule(ConnectionManager.class);
        synchronized (recentConnections) {
            for (int i = 0; i < recentConnections.size(); i++) {
                prefs.put(getConnectoinsHistoryKey(i), ExecutionEnvironmentFactory.toUniqueID(recentConnections.get(i)));
            }
        }
    }

    /*package-local for test purposes*/ void restoreRecentConnectionsList() {
        Preferences prefs = NbPreferences.forModule(ConnectionManager.class);
        synchronized (recentConnections) {
            recentConnections.clear();
            int idx = 0;
            while (true) {
                String id = prefs.get(getConnectoinsHistoryKey(idx), null);
                if (id == null) {
                    break;
                }
                recentConnections.add(ExecutionEnvironmentFactory.fromUniqueID(id));
                idx++;
            }
        }
    }

    private static String getConnectoinsHistoryKey(int idx) {
        return ConnectionManager.class.getName() + "_connection.history_" + idx; //NOI18N
    }

    /**
     * for test purposes only; package-local
     */
    void clearRecentConnectionsList() {
        synchronized (recentConnections) {
            recentConnections.clear();
        }
    }

    private void fireConnected(ExecutionEnvironment execEnv) {
        // No need to lock - use thread-safe collection
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.connected(execEnv);
        }
        updateRecentConnectionsList(execEnv);
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
     *
     * @param execEnv execution environment to test connection with.
     * @return true if connection is established or if execEnv refers to the
     * localhost environment. false otherwise.
     */
    public boolean isConnectedTo(final ExecutionEnvironment execEnv) {
        if (execEnv.isLocal()) {
            return true;
        }
        JSchChannelsSupport support = channelsSupport.get(execEnv); // it's a ConcurrentHashMap => no lock is needed
        return (support != null) && support.isConnected();
    }
    private static final int RETRY_MAX = 10;

    /**
     * A request to initiate a connection with an ExecutionEnvironment.
     *
     * This method doesn't throw exceptions. Instead it uses
     * <tt>ConnectionContinuation</tt> for reporting resulting status. This
     * method does nothing if connection is already established.
     *
     * @param env - environment to initiate connection with
     * @param continuation - implementation of <tt>ConnectionContinuation</tt>
     * to handle resulting status. No status is reported if continuation is
     * <tt>null</tt>.
     * @return <tt>true</tt> if host is connected when return from the method.
     * @see connect(ExecutionEnvironment)
     */
    private boolean connect(final ExecutionEnvironment env, final ConnectionContinuation continuation) {
        boolean connected = isConnectedTo(env);

        if (connected) {
            return true;
        }

        try {
            connectTo(env);
        } catch (IOException ex) {
            if (continuation != null) {
                continuation.connectionFailed(env, ex);
            }
            return false;
        } catch (CancellationException ex) {
            if (continuation != null) {
                continuation.connectionCancelled(env);
            }
            return false;
        }

        connected = isConnectedTo(env);
        if (connected && continuation != null) {
            continuation.connectionEstablished(env);
        }
        return connected;
    }

    /**
     * A request to initiate a connection with an ExecutionEnvironment.
     *
     * This method doesn't throw exceptions. Instead it reports the resulting
     * status in status bar. In case of IOException a error dialog is displayed.
     * This method does nothing if connection is already established.
     *
     * @param env - environment to initiate connection with
     * @return <tt>true</tt> if host is connected when return from the method.
     */
    public boolean connect(final ExecutionEnvironment env) {
        return connect(env, DEFAULT_CC);
    }

    /**
     *
     * @param env <tt>ExecutionEnvironment</tt> to connect to.
     * @throws IOException
     * @throws CancellationException
     */
    public void connectTo(final ExecutionEnvironment env) throws IOException, CancellationException {
        if (SwingUtilities.isEventDispatchThread()) {
            // otherwise UI can hang forever
            throw new IllegalThreadStateException("Should never be called from AWT thread"); // NOI18N
        }

        if (isConnectedTo(env)) {
            return;
        }

        JSch jsch = jschPool.get(env);

        if (jsch == null) {
            jsch = new JSch();
            JSch old = jschPool.putIfAbsent(env, jsch);
            if (old != null) {
                jsch = old;
            }
        }

        if (!UNIT_TEST_MODE) {
            initiateConnection(env, jsch);
        } else {
            // Attempt to workaround "Auth fail" in tests, see IZ 190458
            // We try to reconnect up to 10 times if "Auth fail" exception happens
            int retry = RETRY_MAX;
            IOException ex = null;
            while (retry > 0) {
                try {
                    initiateConnection(env, jsch);
                    return;
                } catch (IOException e) {
                    if (!(e.getCause() instanceof JSchException)) {
                        throw e;
                    }
                    if (!"Auth fail".equals(e.getCause().getMessage())) { //NOI18N
                        throw e;
                    }
                    ex = e;
                }
                System.out.println("AUTH_FAIL: Connection failed, re-runing test " + retry); // NOI18N
                retry--;
            }
            System.out.println("AUTH_FAIL: Retry limit reached"); // NOI18N
            throw ex;
        }
    }

    private void initiateConnection(final ExecutionEnvironment env, final JSch jsch) throws IOException, CancellationException {
        JSchConnectionTask connectionTask = connectionTasks.get(env);

        try {
            if (connectionTask == null) {
                JSchConnectionTask newTask = new JSchConnectionTask(jsch, env);
                JSchConnectionTask oldTask = connectionTasks.putIfAbsent(env, newTask);
                if (oldTask != null) {
                    connectionTask = oldTask;
                } else {
                    connectionTask = newTask;
                    connectionTask.start();
                }
            }

            JSchChannelsSupport cs = connectionTask.getResult();

            if (cs != null) {
                if (!cs.isConnected()) {
                    throw new IOException("JSchChannelsSupport lost connection with " + env.getDisplayName() + "during initialization "); // NOI18N
                }

                synchronized (channelsSupportLock) {
                    channelsSupport.put(env, cs);
                }
            } else {
                JSchConnectionTask.Problem problem = connectionTask.getProblem();
                switch (problem.type) {
                    case CONNECTION_CANCELLED:
                        throw new CancellationException("Connection cancelled for " + env); // NOI18N
                    default:
                        // Note that AUTH_FAIL is generated not only on bad password,
                        // but on socket timeout as well. These cases are
                        // indistinguishable based on information from JSch.
                        if (problem.cause instanceof Error) {
                            log.log(Level.INFO, "Error when connecting " + env, problem.cause); //NOI18N
                        }
                        throw new IOException(problem.type.name(), problem.cause);
                }
            }

            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            log.log(Level.FINE, "New connection established: {0} - {1}", new String[]{env.toString(), hostInfo.getOS().getName()}); // NOI18N

            fireConnected(env);
        } catch (InterruptedException ex) {
            // don't report interrupted exception
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            connectionTasks.remove(env);
        }
    }

    public static ConnectionManager getInstance() {
        HostInfoCache.initializeIfNeeded();
        return instance;
    }

    /**
     * Returns {@link Action javax.swing.Action} that can be used to get
     * connected to the {@link ExecutionEnvironment}. It is guaranteed that the
     * same Action is returned for equal execution environments.
     *
     * @param execEnv - {@link ExecutionEnvironment} to connect to.
     * @param onConnect - Runnable that is executed when connection is
     * established.
     * @return action to be used to connect to the <tt>execEnv</tt>.
     * @see Action
     */
    public synchronized AsynchronousAction getConnectToAction(
            final ExecutionEnvironment execEnv, final Runnable onConnect) {

        if (connectionActions.containsKey(execEnv)) {
            return connectionActions.get(execEnv);
        }

        ConnectToAction action = new ConnectToAction(execEnv, onConnect);

        connectionActions.put(execEnv, action);

        return action;
    }

    private void reconnect(ExecutionEnvironment env) throws IOException, InterruptedException {
        synchronized (channelsSupportLock) {
            if (channelsSupport.containsKey(env)) {
                try {
                    channelsSupport.get(env).reconnect(env);
                } catch (JSchException ex) {
                    throw new IOException(ex);
                }
            }
        }
    }

    public void disconnect(ExecutionEnvironment env) {
        disconnectImpl(env);
        PasswordManager.getInstance().onExplicitDisconnect(env);
    }

    private void disconnectImpl(final ExecutionEnvironment env) {
        synchronized (channelsSupportLock) {
            if (channelsSupport.containsKey(env)) {
                JSchChannelsSupport cs = channelsSupport.remove(env);
                cs.disconnect();
                fireDisconnected(env);
            }
        }
    }

    private static void shutdown() {
        log.fine("Shutting down Connection Manager");
        synchronized (channelsSupportLock) {
            for (JSchChannelsSupport cs : channelsSupport.values()) {
                cs.disconnect();
            }
        }
    }

    public ValidateablePanel getConfigurationPanel(ExecutionEnvironment env) {
        return new HostConfigurationPanel(env);
    }

    /**
     * Do clean up for the env. Any stored settings will be removed
     *
     * @param env
     */
    public void forget(ExecutionEnvironment env) {
        if (env == null) {
            return;
        }

        Authentication.getFor(env).remove();
        jschPool.remove(env);
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

    private static final class ConnectionManagerAccessorImpl
            extends ConnectionManagerAccessor {

        @Override
        public Channel openAndAcquireChannel(ExecutionEnvironment env, String type, boolean waitIfNoAvailable) throws InterruptedException, JSchException, IOException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.acquireChannel(type, waitIfNoAvailable);
                }
            }

            return null;
        }

        @Override
        public void closeAndReleaseChannel(final ExecutionEnvironment env, final Channel channel) throws JSchException {
            JSchChannelsSupport cs = null;

            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    cs = channelsSupport.get(env);
                }
            }

            if (cs != null && channel != null) {
                cs.releaseChannel(channel);
            }
        }

        @Override
        public void reconnect(final ExecutionEnvironment env) throws IOException {
            try {
                instance.reconnect(env);
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
        }

        @Override
        public void changeAuth(ExecutionEnvironment env, Authentication auth) {
            JSch jsch = jschPool.get(env);

            if (jsch != null) {
                try {
                    jsch.removeAllIdentity();
                } catch (JSchException ex) {
                    Exceptions.printStackTrace(ex);
                }

                try {
                    String knownHosts = auth.getKnownHostsFile();
                    if (knownHosts != null) {
                        jsch.setKnownHosts(knownHosts);
                    }
                } catch (JSchException ex) {
                    Exceptions.printStackTrace(ex);
                }

                switch (auth.getType()) {
                    case SSH_KEY:
                        try {
                            jsch.addIdentity(auth.getSSHKeyFile());
                        } catch (JSchException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                }
            }
        }

        @Override
        public JSchAccess getJSchAccess(ExecutionEnvironment env) {
            return new JSchAccessImpl(env);
        }
    }

    private static class JSchAccessImpl implements JSchAccess {

        private final ExecutionEnvironment env;

        public JSchAccessImpl(final ExecutionEnvironment env) {
            this.env = env;
        }

        @Override
        public String getServerVersion() throws JSchException {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.getServerVersion();
                }
            }

            return null;
        }

        @Override
        public Channel openChannel(String type) throws JSchException, InterruptedException, JSchException {
            try {
                return ConnectionManagerAccessor.getDefault().openAndAcquireChannel(env, type, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }

        @Override
        public void releaseChannel(Channel channel) throws JSchException {
            ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(env, channel);
        }

        @Override
        public void setPortForwardingR(String bind_address, int rport, String host, int lport) throws JSchException {
            synchronized (channelsSupportLock) {
                JSchChannelsSupport cs = channelsSupport.get(env);
                cs.setPortForwardingR(bind_address, rport, host, lport);
            }
        }

        @Override
        public int setPortForwardingL(int lport, String host, int rport) throws JSchException {
            synchronized (channelsSupportLock) {
                JSchChannelsSupport cs = channelsSupport.get(env);
                return cs.setPortForwardingL(lport, host, rport);
            }
        }

        @Override
        public void delPortForwardingR(int rport) throws JSchException {
            synchronized (channelsSupportLock) {
                JSchChannelsSupport cs = channelsSupport.get(env);
                cs.delPortForwardingR(rport);
            }
        }

        @Override
        public void delPortForwardingL(int lport) throws JSchException {
            synchronized (channelsSupportLock) {
                JSchChannelsSupport cs = channelsSupport.get(env);
                cs.delPortForwardingL(lport);
            }
        }

        @Override
        public String getConfig(String key) {
            synchronized (channelsSupportLock) {
                if (channelsSupport.containsKey(env)) {
                    JSchChannelsSupport cs = channelsSupport.get(env);
                    return cs.getConfig(key);
                }
            }

            return null;
        }
    }

    private interface ConnectionContinuation {

        void connectionEstablished(ExecutionEnvironment env);

        void connectionCancelled(ExecutionEnvironment env);

        void connectionFailed(ExecutionEnvironment env, IOException ex);
    }
}
