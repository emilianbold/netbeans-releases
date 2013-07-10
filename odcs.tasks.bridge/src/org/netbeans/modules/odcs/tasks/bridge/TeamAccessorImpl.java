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

package org.netbeans.modules.odcs.tasks.bridge;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JLabel;
import org.netbeans.modules.bugtracking.team.spi.TeamAccessor;
import org.netbeans.modules.bugtracking.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.team.spi.RepositoryUser;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.api.OdcsUIUtil;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.team.ui.common.NbModuleOwnerSupport.OwnerInfo;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Stupka
 */
@org.openide.util.lookup.ServiceProviders({@ServiceProvider(service=TeamAccessor.class),
                                           @ServiceProvider(service=TeamAccessorImpl.class)})
public class TeamAccessorImpl extends TeamAccessor {

    private final List<PropertyChangeListener> allTeamListeners = new ArrayList<PropertyChangeListener>(1);
    public TeamAccessorImpl() {
        super();
        ODCSManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(ODCSManager.PROP_INSTANCES.equals(evt.getPropertyName())) {
                    if(evt.getNewValue() != null) {
                        ODCSServer s = (ODCSServer) evt.getNewValue();
                        synchronized(allTeamListeners) {
                            for (PropertyChangeListener l : allTeamListeners) {
                                addPropertyChangeListener(l, s);
                            }
                        }
                    } else {
                        ODCSServer s = (ODCSServer) evt.getOldValue();
                        synchronized(allTeamListeners) {
                            for (PropertyChangeListener l : allTeamListeners) {
                                removePropertyChangeListener(l, s);
                            }
                        }
                    }
                }
            }
        });
    }
    
    static TeamAccessorImpl getInstance() {
        return Lookup.getDefault().lookup(TeamAccessorImpl.class);
    }

    @Override
    public void logTeamUsage (Object... parameters) {
        OdcsUIUtil.logODCSUsage(parameters); 
    }

    @Override
    public boolean isLoggedIn (String url) {
        boolean loggedIn = false;
        if (url == null) {
            // is the user logged into any server?
            // we're just interested if the user works with any servers
            for (ODCSServer server : ODCSManager.getDefault().getServers()) {
                if (server.getPasswordAuthentication() != null) {
                    loggedIn = true;
                    break;
                }
            }
        } else {
            // is the user logged into a concrete server instance?
            ODCSServer server = getServer(url);
            loggedIn = (server != null) && (server.getPasswordAuthentication() != null);
        }
        return loggedIn;
    }


    @Override
    public boolean showLogin() {
        return OdcsUIUtil.showLogin();
    }

    @Override
    public Collection<RepositoryUser> getProjectMembers(TeamProject kp) throws IOException {
        // unknown
        return Collections.EMPTY_LIST;
    }


    @Override
    public PasswordAuthentication getPasswordAuthentication(String url, boolean forceLogin) {
        ODCSServer server = getServer(url);
        if (server == null) {
            Support.LOG.log(Level.FINEST, "no server for url : [{0}]", url);
            return null;
        }
        return getPasswordAuthentication(server, forceLogin);
    }

    @Override
    public boolean isNBTeamServerRegistered () {
        return false;
    }

    @Override
    public JLabel createUserWidget(String userName, String host, String chatMessage) {
        return null; // no user/member info provided
    }

    @Override
    public org.netbeans.modules.bugtracking.team.spi.OwnerInfo getOwnerInfo(Node node) {
        return null; // available only for netbeans.org 
    }

    @Override
    public org.netbeans.modules.bugtracking.team.spi.OwnerInfo getOwnerInfo(File file) {
        return null; // available only for netbeans.org 
    }

    @Override
    public TeamProject[] getDashboardProjects(boolean onlyOpened) {
        ProjectHandle<ODCSProject>[] handles = ODCSUiServer.getOpenProjects();
        if ((handles == null) || (handles.length == 0)) {
            return new TeamProjectImpl[0];
        }

        List<TeamProjectImpl> teamProjects = new LinkedList<TeamProjectImpl>();
        for (ProjectHandle<ODCSProject> handle : handles) {
            ODCSProject project = handle.getTeamProject();
            if (project != null) {
                teamProjects.add(TeamProjectImpl.getInstance(project));
            } else {
                Support.LOG.log(
                        Level.WARNING,
                        "No Team project is available for ProjectHandle" + " [{0}, {1}]", //NOI18N
                        new Object[]{handle.getId(), handle.getDisplayName()}); 
            }
        }
        return teamProjects.toArray(new TeamProjectImpl[teamProjects.size()]);
    }

    @Override
    public TeamProject getTeamProjectForRepository(String url) throws IOException {
        ODCSProject odcsp = ODCSProject.findProjectForRepository(url);
        return odcsp != null ? TeamProjectImpl.getInstance(odcsp) : null;
    }

    @Override
    public TeamProject getTeamProject(String url, String projectName) throws IOException {
        ODCSServer server = getServer(url);
        if (server == null) {
            Support.LOG.log(Level.FINEST, "no server for url : [{0}]", url);
            return null;
        }
        ODCSProject odcsProj;
        try {
            odcsProj = server.getProject(projectName, false);
        } catch (ODCSException ex) {
            throw new IOException(ex);
        }
        return odcsProj != null ? TeamProjectImpl.getInstance(odcsProj) : null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener, String teamHostUrl) {
        ODCSServer server = getServer(teamHostUrl);
        if(server != null) {
            addPropertyChangeListener(listener, server);
        } else {
            Support.LOG.log(Level.WARNING, "trying to unregister on a unknown server host {0}", teamHostUrl);  //NOI18N
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener, String teamHostUrl) {
        ODCSServer server = getServer(teamHostUrl);
        if(server != null) {
            removePropertyChangeListener(listener, server);
        } else {
            Support.LOG.log(Level.WARNING, "trying to unregister on a unknown server host {0}", teamHostUrl);  //NOI18N
        }
    }

    @Override
    public boolean isOwner (String url) {
        return getServer(url) != null;
    }

    /**
     * Returns true if logged into server, otherwise false.
     *
     * @return
     */
    static boolean isLoggedIn(ODCSServer server) {
        return server.isLoggedIn();
    }

    private ODCSServer getServer(String url) {
        // 1st - full url match
        ODCSServer serverCandidate = ODCSManager.getDefault().getServer(url);
        if (serverCandidate != null) {
            return serverCandidate;
        }
        // 2nd - VCS repository url match
        try {
            ODCSProject kp = ODCSProject.findProjectForRepository(url);
            if (kp != null) {
                return kp.getServer();
            }
        } catch (ODCSException ex) {
            Support.LOG.log(Level.FINE, url, ex);
        }
        // 3rd - bugtracking issue url match
        for (ODCSServer server : ODCSManager.getDefault().getServers()) {
            String serverUrl = server.getUrl().toString();
            if (url.startsWith(serverUrl)) {
                Support.LOG.log(Level.FINE, "getServer: url {0} matches server url {1}", new String[] {url, serverUrl}); //NOI18N
                return server;
            }
        }
        return null;
    }

    /**
     * Returns an instance of PasswordAuthentication holding the actual
     * Team Server credentials.
     *
     * @param url a {@link TeamServer} instance url
     * @param forceLogin  forces a login if user not logged in
     * @return PasswordAuthentication
     */
    static PasswordAuthentication getPasswordAuthentication(ODCSServer server, boolean forceLogin) {
        PasswordAuthentication a = server.getPasswordAuthentication();
        if(a != null) {
            return a;
        }

        if(!forceLogin) {
            return null;
        }

        if(!OdcsUIUtil.showLogin(server)) {
            return null;
        }

        return server.getPasswordAuthentication();
    }

    void addPropertyChangeListener(PropertyChangeListener listener, ODCSServer server) {
        getTeamListener(server).add(listener);
    }

    void removePropertyChangeListener(PropertyChangeListener listener, ODCSServer server) {
        getTeamListener(server).remove(listener);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized(allTeamListeners) {
            allTeamListeners.add(listener);
        }
        for (ODCSServer server : ODCSManager.getDefault().getServers()) {
            addPropertyChangeListener(listener, server);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        synchronized(allTeamListeners) {
            allTeamListeners.remove(listener);
        }
        for (ODCSServer server : ODCSManager.getDefault().getServers()) {
            removePropertyChangeListener(listener, server);
        }
    }
    
    private class OwnerInfoImpl extends org.netbeans.modules.bugtracking.team.spi.OwnerInfo {
        private final OwnerInfo delegate;

        public OwnerInfoImpl(OwnerInfo delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getOwner() {
            return delegate.getOwner();
        }

        @Override
        public List<String> getExtraData() {
            return delegate.getExtraData();
        }
    }

    private Map<String, DelegateODCSListener> teamListeners;
    private synchronized DelegateODCSListener getTeamListener(ODCSServer server) {
        if(teamListeners == null) {
            teamListeners = new HashMap<String, DelegateODCSListener>();
        }
        DelegateODCSListener l = teamListeners.get(server.getUrl().toString());
        if(l == null) {
            l = new DelegateODCSListener(server);
            teamListeners.put(server.getUrl().toString(), l);
        }
        return l;
    }

    private class DelegateODCSListener implements PropertyChangeListener {
        private final Collection<PropertyChangeListener> delegates = new LinkedList<PropertyChangeListener>();
        private final ODCSServer server;
        public DelegateODCSListener (ODCSServer server) {
            this.server = server;
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(TeamServer.PROP_LOGIN) ||
               evt.getPropertyName().equals(DashboardSupport.PROP_OPENED_PROJECTS)) 
            {
                PropertyChangeListener[] la;
                synchronized (delegates) {
                   la = delegates.toArray(new PropertyChangeListener[delegates.size()]);
                }
                String propName;
                if(TeamServer.PROP_LOGIN.equals(evt.getPropertyName())) {
                    propName = PROP_LOGIN;
                } else if (DashboardSupport.PROP_OPENED_PROJECTS.equals(evt.getPropertyName())) {
                    propName = PROP_PROJETCS_CHANGED;
                } else {
                    throw new IllegalStateException("Unknown event " + evt.getPropertyName()); // NOI18N
                }
                for (PropertyChangeListener l : la) {
                    l.propertyChange(new PropertyChangeEvent(evt.getSource(), propName, evt.getOldValue(), evt.getNewValue()));
                }
            }
        }
        private synchronized void add(PropertyChangeListener l) {
            delegates.add(l);
            if(delegates.size() == 1) {
                server.addPropertyChangeListener(this);
                OdcsUIUtil.addDashboardListener(server, this);
            }
        }
        private synchronized void remove(PropertyChangeListener l) {
            delegates.remove(l);
            if(delegates.isEmpty()) {
                server.removePropertyChangeListener(this);
            }
        }
    }
}
