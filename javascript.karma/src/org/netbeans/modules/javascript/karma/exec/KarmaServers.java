/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.karma.exec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;

public final class KarmaServers {

    private static final KarmaServers INSTANCE = new KarmaServers();

    // @GuardedBy("this")
    private final Map<Project, KarmaServerInfo> karmaServers = new HashMap<>();
    private final KarmaServersListener.Support listenerSupport = new KarmaServersListener.Support();
    private final ChangeListener serverListener = new ServerListener();


    private KarmaServers() {
    }

    public static KarmaServers getInstance() {
        return INSTANCE;
    }

    public void addKarmaServersListener(KarmaServersListener listener) {
        listenerSupport.addKarmaServersListener(listener);
    }

    public void removeKarmaServersListener(KarmaServersListener listener) {
        listenerSupport.removeKarmaServersListener(listener);
    }

    public synchronized void startServer(Project project) {
        assert Thread.holdsLock(this);
        if (isServerRunning(project)) {
            return;
        }
        KarmaServerInfo serverInfo = karmaServers.get(project);
        if (serverInfo == null) {
            serverInfo = new KarmaServerInfo();
            karmaServers.put(project, serverInfo);
        }
        assert serverInfo.getServer() == null : serverInfo;
        KarmaServer karmaServer = new KarmaServer(serverInfo.getPort(), project);
        karmaServer.addChangeListener(serverListener);
        serverInfo.setServer(karmaServer);
        karmaServer.start();
    }

    public synchronized void runTests(Project project) {
        assert Thread.holdsLock(this);
        startServer(project);
        KarmaServerInfo serverInfo = karmaServers.get(project);
        assert serverInfo != null;
        KarmaServer karmaServer = serverInfo.getServer();
        // XXX
        while (karmaServer.isStarting()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                break;
            }
        }
        karmaServer.runTests();
    }

    public synchronized void stopServer(Project project, boolean cleanup) {
        assert Thread.holdsLock(this);
        KarmaServerInfo serverInfo = karmaServers.get(project);
        if (serverInfo == null) {
            return;
        }
        KarmaServer karmaServer = serverInfo.getServer();
        karmaServer.stop();
        karmaServer.removeChangeListener(serverListener);
        if (cleanup) {
            karmaServers.remove(project);
        } else {
            serverInfo.setServer(null);
        }
    }

    public synchronized void restartServer(Project project) {
        assert Thread.holdsLock(this);
        stopServer(project, false);
        startServer(project);
    }

    public synchronized boolean isServerStarting(Project project) {
        assert Thread.holdsLock(this);
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            return false;
        }
        return karmaServer.isStarting();
    }

    public synchronized boolean isServerStarted(Project project) {
        assert Thread.holdsLock(this);
        KarmaServer karmaServer = getKarmaServer(project);
        if (karmaServer == null) {
            return false;
        }
        return karmaServer.isStarted();
    }

    public synchronized boolean isServerRunning(Project project) {
        return isServerStarting(project)
                || isServerStarted(project);
    }

    @CheckForNull
    private synchronized KarmaServer getKarmaServer(Project project) {
        assert Thread.holdsLock(this);
        KarmaServerInfo serverInfo = karmaServers.get(project);
        if (serverInfo == null) {
            return null;
        }
        return serverInfo.getServer();
    }

    void fireServerChange(KarmaServer karmaServer) {
        assert karmaServer != null;
        listenerSupport.fireServerChanged(karmaServer);
    }

    //~ Inner classes

    private static final class KarmaServerInfo {

        private static final AtomicInteger CURRENT_PORT = new AtomicInteger(9876);

        private final int port;

        // @GuardedBy("this")
        private KarmaServer server;


        public KarmaServerInfo() {
            port = CURRENT_PORT.getAndIncrement();
        }

        public int getPort() {
            return port;
        }

        public synchronized KarmaServer getServer() {
            return server;
        }

        public synchronized void setServer(KarmaServer karmaServer) {
            this.server = karmaServer;
        }

        @Override
        public String toString() {
            return "KarmaServerInfo{" + "port=" + port + ", server=" + server + '}'; // NOI18N
        }

    }

    private final class ServerListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            fireServerChange((KarmaServer) e.getSource());
        }

    }

}
