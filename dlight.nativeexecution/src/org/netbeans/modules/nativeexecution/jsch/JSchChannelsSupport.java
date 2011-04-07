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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.support.Logger;
import org.netbeans.modules.nativeexecution.support.RemoteUserInfo;

/**
 *
 * @author ak119685
 */
public final class JSchChannelsSupport {

    private static final java.util.logging.Logger log = Logger.getInstance();
    private static final int JSCH_CONNECTION_TIMEOUT = Integer.getInteger("jsch.connection.timeout", 10000); // NOI18N
    private static final int JSCH_SESSIONS_PER_ENV = Integer.getInteger("jsch.sessions.per.env", 10); // NOI18N
    private static final int JSCH_CHANNELS_PER_SESSION = Integer.getInteger("jsch.channels.per.session", 10); // NOI18N
    private static final boolean UNIT_TEST_MODE = Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    private static final boolean USE_JZLIB = Boolean.getBoolean("jzlib"); // NOI18N
    private final JSch jsch;
    private final RemoteUserInfo userInfo;
    private final ExecutionEnvironment env;
    private final ReentrantLock sessionsLock = new ReentrantLock();
    private final Condition sessionAvailable = sessionsLock.newCondition();
    // AtomicInteger stores a number of available channels for the session
    // We use ConcurrentHashMap to be able fast isConnected() check; in most other cases sessions is guarded bu "this"
    private final ConcurrentHashMap<Session, AtomicInteger> sessions = new ConcurrentHashMap<Session, AtomicInteger>();
    private final Set<Channel> knownChannels = new HashSet<Channel>();

    public JSchChannelsSupport(JSch jsch, ExecutionEnvironment env) {
        this.jsch = jsch;
        this.env = env;
        this.userInfo = new RemoteUserInfo(env, !UNIT_TEST_MODE);
    }

    public ChannelShell getShellChannel(boolean waitIfNoAvailable) throws JSchException, IOException, InterruptedException {
        return (ChannelShell) acquireChannel("shell", waitIfNoAvailable); // NOI18N
    }

    public synchronized Channel acquireChannel(String type, boolean waitIfNoAvailable) throws JSchException, IOException, InterruptedException {
        Session session = null;

//        if (SwingUtilities.isEventDispatchThread()) {
//            Exception e = new Exception("O-la-la acquireChannel!!!");
//            Exceptions.printStackTrace(e);
//        }
//

        session = findFreeSession();

        if (session == null) {
            if (sessions.size() >= JSCH_SESSIONS_PER_ENV) {
                if (waitIfNoAvailable) {
                    try {
                        sessionsLock.lock();
                        while (session == null) {
                            sessionAvailable.await();
                            session = findFreeSession();
                        }
                    } finally {
                        sessionsLock.unlock();
                    }
                } else {
                    throw new IOException("All " + JSCH_SESSIONS_PER_ENV + " sessions for " + env.getDisplayName() + " are fully loaded"); // NOI18N
                }
            }
        }

        if (session == null) {
            session = startNewSession(true);
        }

        Channel result = session.openChannel(type);
        log.log(Level.FINE, "Acquired channel [{0}] from session [{1}].", new Object[]{System.identityHashCode(result), System.identityHashCode(session)}); // NOI18N

        knownChannels.add(result);

        return result;
    }

    public boolean isConnected() {
        // ConcurrentHashMap.keySet() never throws ConcurrentModificationException
        for (Session s : sessions.keySet()) {
            if (s.isConnected()) {
                return true;
            }
        }

        return false;
    }

    public synchronized void reconnect(ExecutionEnvironment env) throws IOException, JSchException {
        disconnect();
        connect();
    }

    private Session findFreeSession() {
        for (Entry<Session, AtomicInteger> entry : sessions.entrySet()) {
            Session s = entry.getKey();
            AtomicInteger availableChannels = entry.getValue();
            if (s.isConnected() && availableChannels.get() > 0) {
                log.log(Level.FINE, "availableChannels == {0}", new Object[]{availableChannels.get()}); // NOI18N
                int remains = availableChannels.decrementAndGet();
                log.log(Level.FINE, "Reuse session [{0}]. {1} channels remains...", new Object[]{System.identityHashCode(s), remains}); // NOI18N
                return s;
            }
        }

        return null;
    }

    public synchronized void connect() throws JSchException {
        if (isConnected()) {
            return;
        }

        startNewSession(false);
    }

    public synchronized void disconnect() {
        for (Session s : sessions.keySet()) {
            s.disconnect();
        }
    }

    private Session startNewSession(boolean acquireChannel) throws JSchException {
        Session newSession;
        
        while (true) {
            try {
                newSession = jsch.getSession(env.getUser(), env.getHostAddress(), env.getSSHPort());
                newSession.setUserInfo(userInfo);

                if (USE_JZLIB) {
                    newSession.setConfig("compression.s2c", "zlib@openssh.com,zlib,none"); // NOI18N
                    newSession.setConfig("compression.c2s", "zlib@openssh.com,zlib,none"); // NOI18N
                    newSession.setConfig("compression_level", "9"); // NOI18N
                }

                newSession.connect(JSCH_CONNECTION_TIMEOUT);
                break;
            } catch (JSchException ex) {
                if (!UNIT_TEST_MODE && "Auth fail".equals(ex.getMessage())) { // NOI18N
                    PasswordManager.getInstance().clearPassword(env);
                } else {
                    throw ex;
                }
            }
        }

        sessions.put(newSession, new AtomicInteger(JSCH_CHANNELS_PER_SESSION - (acquireChannel ? 1 : 0)));

        log.log(Level.FINE, "New session [{0}] started.", new Object[]{System.identityHashCode(newSession)}); // NOI18N

        return newSession;
    }

    public synchronized void releaseChannel(final Channel channel) throws JSchException {
//        if (SwingUtilities.isEventDispatchThread()) {
//            Exception e = new Exception("O-la-la releaseChannel!!!");
//            Exceptions.printStackTrace(e);
//        }

        if (!knownChannels.remove(channel)) {
            // Means it was not in the collection
            return;
        }

        Session s = channel.getSession();

        log.log(Level.FINE, "Releasing channel [{0}] for session [{1}].", new Object[]{System.identityHashCode(channel), System.identityHashCode(s)}); // NOI18N
        channel.disconnect();

        int count = sessions.get(s).incrementAndGet();

        List<Session> sessionsToRemove = new ArrayList<Session>();

        if (count == JSCH_CHANNELS_PER_SESSION) {
            // No more channels in this session ...
            // Do we have other ready-to-serve sessions?
            // In this case will close this one.
            for (Entry<Session, AtomicInteger> entry : sessions.entrySet()) {
                if (entry.getKey() == s) {
                    continue;
                }
                if (entry.getValue().get() > 0) {
                    log.log(Level.FINE, "Found another session [{0}] with {1} free slots. Will remove this one [{2}].", // NOI18N
                            new Object[]{
                                System.identityHashCode(entry.getKey()),
                                entry.getValue().get(),
                                System.identityHashCode(s)});

                    sessionsToRemove.add(s);
                    break;
                }
            }
        } else {
            // This sessions is capable to provide a channel on next request
            // Perhaps we have empty sessions that can be closed then?
            for (Entry<Session, AtomicInteger> entry : sessions.entrySet()) {
                if (entry.getKey() == s) {
                    continue;
                }

                if (entry.getValue().get() == JSCH_CHANNELS_PER_SESSION) {
                    log.log(Level.FINE, "Found empty session [{0}] while this one is also has free slots [{1}].", // NOI18N
                            new Object[]{
                                System.identityHashCode(entry.getKey()),
                                System.identityHashCode(s)});
                    sessionsToRemove.add(entry.getKey());
                }
            }
        }

        for (Session sr : sessionsToRemove) {
            log.log(Level.FINE, "Closing session [{0}].", new Object[]{System.identityHashCode(s)}); // NOI18N
            sr.disconnect();
            sessions.remove(sr);
        }

        try {
            sessionsLock.lock();
            sessionAvailable.signalAll();
        } finally {
            sessionsLock.unlock();
        }
    }
}
