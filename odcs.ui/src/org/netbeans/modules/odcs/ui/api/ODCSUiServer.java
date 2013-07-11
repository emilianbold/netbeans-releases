/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.ui.api;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.ui.dashboard.DashboardProviderImpl;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.team.ui.spi.LoginPanelSupport;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import org.openide.util.WeakListeners;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.odcs.api.ODCSManager;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.ODCSServerProviderImpl;
import org.netbeans.modules.odcs.ui.LoginPanelSupportImpl;
import org.netbeans.modules.odcs.ui.NewProjectAction;
import org.netbeans.modules.odcs.ui.OpenProjectAction;
import org.netbeans.modules.odcs.ui.Utilities;
import org.netbeans.modules.odcs.ui.spi.VCSAccessor;
import org.netbeans.modules.team.ui.common.UserNode;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.util.treelist.SelectionList;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Ondrej Vrabec
 */
public class ODCSUiServer implements TeamServer {

    private static final Map<ODCSServer, ODCSUiServer> serverMap = new WeakHashMap<ODCSServer, ODCSUiServer>(3);
    private final WeakReference<ODCSServer> impl;
    private PropertyChangeListener l;
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private final DashboardSupport<ODCSProject> dashboard;

    private ODCSUiServer (ODCSServer server) {
        this.impl = new WeakReference<ODCSServer>(server);
        dashboard = new DashboardSupport<ODCSProject>(this, new DashboardProviderImpl(this));
        server.addPropertyChangeListener(WeakListeners.propertyChange(l=new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                String propName = pce.getPropertyName();
                if (propName.equals(ODCSServer.PROP_LOGIN)) {
                    if (ODCSServer.PROP_LOGIN.equals(pce.getPropertyName())) {
                        final Preferences preferences = NbPreferences.forModule(ODCSServerProviderImpl.class);
                        preferences.put(Utilities.getPrefName((ODCSServer) pce.getSource(), Utilities.LOGIN_STATUS_PREF), Boolean.toString(pce.getNewValue() != null));
                    }
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN, pce.getOldValue(), pce.getNewValue());
                } else if (propName.equals(ODCSServer.PROP_LOGIN_STARTED)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN_STARTED, pce.getOldValue(), pce.getNewValue());
                } else if (propName.equals(ODCSServer.PROP_LOGIN_FAILED)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN_FAILED, pce.getOldValue(), pce.getNewValue());
                }
            }
        }, server));
    }

    public static ODCSUiServer forServer (ODCSServer server) {
        ODCSUiServer serverUi;
        synchronized (serverMap) {
            serverUi = serverMap.get(server);
            if (serverUi == null) {
                serverUi = new ODCSUiServer(server);
                serverMap.put(server, serverUi);
            }
        }
        return serverUi;
    }
    
    public static ProjectHandle<ODCSProject>[] getOpenProjects() {
        ArrayList<ODCSUiServer> servers;
        synchronized (serverMap) {
            servers = new ArrayList<ODCSUiServer>(serverMap.values());
        }
        LinkedList<ProjectHandle<ODCSProject>> ret = new LinkedList<ProjectHandle<ODCSProject>>();
        for (ODCSUiServer s : servers) {
            ProjectHandle<ODCSProject>[] projects = s.getDashboard().getProjects(true);
            if(projects != null) {
                ret.addAll(Arrays.asList(projects));
            }
        }
        return ret.toArray(new ProjectHandle[ret.size()]);
    }
    
    public DashboardSupport<ODCSProject> getDashboard() {
        return dashboard;
    }    

    @Override
    public URL getUrl () {
        return getImpl(true).getUrl();
    }

    @Override
    public Status getStatus () {
        return getImpl(true).isLoggedIn() ? Status.ONLINE : Status.OFFLINE;
    }

    @Override
    public void logout () {
        getImpl(true).logout();
    }

    @Override
    public String getDisplayName () {
        return getImpl(true).getDisplayName();
    }

    @Override
    public void setDisplayName(String name) {
        ODCSServer serverImpl = getImpl(true);
        String oName = serverImpl.getDisplayName();
        synchronized(serverMap) {
            serverImpl.setDisplayName(name);
            ODCSManager.getDefault().store();
        }
        propertyChangeSupport.firePropertyChange(TeamServer.PROP_NAME, oName, name);
    }

    @Override
    public void setUrl(String url) throws MalformedURLException {
        ODCSServer serverImpl = getImpl(true);
        String oUrl = serverImpl.getUrl().toString();
        synchronized(serverMap) {
            serverImpl.setUrl(url);
            ODCSManager.getDefault().store();
        }
        propertyChangeSupport.firePropertyChange(TeamServer.PROP_URL, oUrl, url);
    }
    
    @Override
    public Icon getIcon () {
        return getImpl(true).getIcon();
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public TeamServerProvider getProvider () {
        return ODCSServerProviderImpl.getDefault();
    }

    @Override
    public JComponent getDashboardComponent () {
        return dashboard.getComponent();
    }

    @Override
    public LoginPanelSupport createLoginSupport () {
        return new LoginPanelSupportImpl(getImpl(true));
    }

    public ODCSServer getServer () {
        return getImpl(false);
    }

    private ODCSServer getImpl (boolean checkOriginal) {
        ODCSServer server = impl.get();
        if (checkOriginal && server == null) {
            throw new IllegalStateException("Original ODCS server no longer exists.");
        }
        return server;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return getServer().getPasswordAuthentication();
    }

    @Override
    public SelectionList getProjects(boolean forceRefresh) {
        return getDashboard().getProjectsList(forceRefresh);
    }

    @Override
    public Action getNewProjectAction() {
        if(getPasswordAuthentication() == null) {
            return null;
        }
        NewProjectAction newProjectAction = new NewProjectAction(getImpl(false));
        newProjectAction.putValue( Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/new_team_project.png", true));
        newProjectAction.putValue( Action.SHORT_DESCRIPTION, NbBundle.getMessage(UserNode.class, "LBL_NewProject") );
        return newProjectAction;
    }
    
    @Override
    public Action getOpenProjectAction() {
        if(getPasswordAuthentication() == null) {
            return null;
        }        
        OpenProjectAction openProjectAction = new OpenProjectAction(ODCSUiServer.this);
        openProjectAction.putValue( Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/open_team_project.png", true));
        openProjectAction.putValue( Action.SHORT_DESCRIPTION, NbBundle.getMessage(UserNode.class, "LBL_OpenProject") );
        return openProjectAction;
    }

    @Override
    public Action[] getTeamMenuActions() {
        return new Action[] {
            new NewProjectAction(getServer()),
            new OpenProjectAction(this),
            VCSAccessor.getDefault().getOpenSourcesAction(this)
        };
    }
}
