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
package org.netbeans.modules.nativeexecution.util;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ObservableAction;
import org.netbeans.modules.nativeexecution.api.ObservableActionListener;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.support.RemoteUserInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Manager for open ssh connections.
 * It is a singleton and should be accessed via static <tt>getInstance()</tt>
 * method.
 *
 * @see org.netbeans.modules.nativeexecution.ExecutionEnvironment
 * @author ak119685
 */
public class ConnectionManager {
    // Instance of the ConnectionManager

    private static ConnectionManager instance;

    // Map that contains all connected sessions;
    private Map<String, Session> sessions = null;

    // Provider of 'ConnectTo...' actions
    private ActionsProvider actionsProvider = null;

    // Actual sessions pool
    private final JSch jsch;

    private ConnectionManager() {
        this.jsch = new JSch();
    }

    /**
     * Returns instance of <tt>ConnectionManager</tt>.
     * @return instance of <tt>ConnectionManager</tt>
     */
    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }

        return instance;
    }

    /**
     * Returns the ssh session for requested <tt>ExecutionEnvironment</tt>.
     * If open <tt>Session</tt> already exists for specified
     * <tt>ExecutionEnvironment</tt>, existent one is returned. <br>
     * If no <tt>Session</tt> existed for the execution environment before the
     * call of the method, <tt>Session</tt> is created and authorization is
     * performed.
     *
     * @param execEnv - execution environment to get <tt>Session</tt> for.
     *
     * @return <tt>null</tt> on error; <br>
     *         New or already existent <tt>Session</tt> for specified
     *         <tt>execEnv</tt> on success.
     */
    public synchronized Session getConnectionSession(final ExecutionEnvironment execEnv) {
        final String sessionKey = execEnv.toString();
        Session session = null;

        if (sessions == null) {
            sessions = Collections.synchronizedMap(new HashMap<String, Session>());
        }

        if (sessions.containsKey(sessionKey)) {
            session = sessions.get(sessionKey);
            if (session.isConnected()) {
                return session;
            }
        }

        String user = execEnv.getUser();
        String host = execEnv.getHost();
        int sshPort = execEnv.getSSHPort();

        ProgressHandle ph = ProgressHandleFactory.createHandle(
                loc("ConnectionManager.Connecting", execEnv.toString())); // NOI18N

        ph.start();

        try {
            synchronized (jsch) {
                session = jsch.getSession(user, host, sshPort);
            }

            session.setUserInfo(RemoteUserInfo.getUserInfo(execEnv, true));
            session.connect();
        } catch (JSchException e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage());
            DialogDisplayer.getDefault().notify(nd);
            session = null;
        } finally {
            ph.finish();
        }

        if (session != null) {
            sessions.put(sessionKey, session);
        }

        return session;
    }

    boolean isConnectedTo(ExecutionEnvironment execEnv) {
        if (sessions == null) {
            return false;
        }

        if (sessions.containsKey(execEnv.toString())) {
            return sessions.get(execEnv.toString()).isConnected();
        }

        return false;
    }

    /**
     * Returns <tt>ObservableAction&lt;Boolean&gt;</tt> action that can be used
     * to get connected to an <tt>ExecutionEnvironment</tt>.
     * It is guaranteed that the same Action is returned for equal execution
     * environments.
     *
     * @param execEnv - <tt>ExecutionEnvironment</tt> to connect to.
     * @return action to be used to connect to the <tt>execEnv</tt>.
     * @see org.netbeans.modules.nativeexecution.ObservableAction
     */
    public synchronized ObservableAction<Boolean> getConnectAction(final ExecutionEnvironment execEnv) {
        if (actionsProvider == null) {
            actionsProvider = new ActionsProvider();
        }

        return actionsProvider.getConnectAction(execEnv);
    }

    private static class ActionsProvider implements ObservableActionListener<Boolean> {
        // Map that contains currently running "ConnectTo" actions.
        // In case of subsequent requests for connection - the same task will
        // be returned.

        private final Map<ExecutionEnvironment, ConnectAction> hash =
                Collections.synchronizedMap(new HashMap<ExecutionEnvironment, ConnectAction>());

        private ObservableAction<Boolean> getConnectAction(ExecutionEnvironment execEnv) {
            ConnectAction ca = null;

            synchronized (hash) {
                if (hash.containsKey(execEnv)) {
                    ca = hash.get(execEnv);
                } else {
                    ca = new ConnectAction(execEnv);
                    ca.addObservableActionListener(this);
                    hash.put(execEnv, ca);
                }

                return ca;
            }
        }

        public void actionCompleted(Action source, Boolean result) {
            ConnectAction ca = (ConnectAction) source;
            hash.remove(ca.getExecEnv());
        }

        public void actionStarted(Action source) {
        }

        private static class ConnectAction extends ObservableAction<Boolean> {

            private final ExecutionEnvironment execEnv;

            public ConnectAction(final ExecutionEnvironment execEnv) {
                super(loc("ConnectionManager.ConnectToAction.text", execEnv.toString())); // NOI18N
                this.execEnv = execEnv;
            }

            private ExecutionEnvironment getExecEnv() {
                return execEnv;
            }

            @Override
            protected Boolean performAction(ActionEvent e) {
                if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                    return true;
                }

                return ConnectionManager.getInstance().getConnectionSession(execEnv) != null;
            }
        }
    }

    private static String loc(String key, Object... params) {
        return NbBundle.getMessage(ConnectionManager.class, key, params);
    }
}
