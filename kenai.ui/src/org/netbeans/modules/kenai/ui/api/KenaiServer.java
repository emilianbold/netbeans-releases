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
package org.netbeans.modules.kenai.ui.api;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.ui.NewKenaiProjectAction;
import org.netbeans.modules.kenai.ui.OpenKenaiProjectAction;
import org.netbeans.modules.kenai.ui.ProjectHandleImpl;
import org.netbeans.modules.kenai.ui.dashboard.DashboardProviderImpl;
import org.netbeans.modules.kenai.ui.impl.LoginPanelSupportImpl;
import org.netbeans.modules.kenai.ui.impl.TeamServerProviderImpl;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.team.ui.common.UserNode;
import org.netbeans.modules.team.ui.spi.LoginPanelSupport;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import org.netbeans.modules.team.ui.util.treelist.SelectionList;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ondrej Vrabec
 */
public final class KenaiServer implements TeamServer {
    private static final Map<Kenai, KenaiServer> serverMap = new WeakHashMap<Kenai, KenaiServer>(3);

    private final Kenai kenai;
    private PropertyChangeListener l;
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private final PropertyChangeListener kenaiListener;

    private DashboardSupport<KenaiProject> dashboard;
    
    private KenaiServer (Kenai kenai) {
        this.kenai = kenai;
        dashboard = new DashboardSupport<KenaiProject>(this, new DashboardProviderImpl(this));
        kenai.addPropertyChangeListener(WeakListeners.propertyChange(l=new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                String propName = pce.getPropertyName();
                if (propName.equals(Kenai.PROP_LOGIN)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN, pce.getOldValue(), pce.getNewValue());
                } else if (propName.equals(Kenai.PROP_LOGIN_STARTED)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN_STARTED, pce.getOldValue(), pce.getNewValue());
                } else if (propName.equals(Kenai.PROP_LOGIN_FAILED)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN_FAILED, pce.getOldValue(), pce.getNewValue());
                }
            }
        }, kenai));
        
        kenaiListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (Kenai.PROP_XMPP_LOGIN_STARTED.equals(pce.getPropertyName())) {
                    dashboard.xmppStarted();
                } else if (Kenai.PROP_XMPP_LOGIN.equals(pce.getPropertyName())) {
                    dashboard.xmppFinsihed();
                } else if (Kenai.PROP_XMPP_LOGIN_FAILED.equals(pce.getPropertyName())) {
                    dashboard.xmppFinsihed();
                }
            }
        };

        kenai.addPropertyChangeListener(WeakListeners.propertyChange(kenaiListener, this));

        KenaiConnection.getDefault(getKenai()).addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (KenaiConnection.PROP_XMPP_STARTED.equals(evt.getPropertyName())) {
                    dashboard.xmppStarted();
                } else if (KenaiConnection.PROP_XMPP_FINISHED.equals(evt.getPropertyName())) {
                    dashboard.xmppFinsihed();
                }
            }
        });        
    }

    public DashboardSupport<KenaiProject> getDashboard() {
        return dashboard;
    }
    
    public static KenaiServer forKenai (Kenai kenai) {
        KenaiServer serverUi;
        synchronized (serverMap) {
            serverUi = serverMap.get(kenai);
            if (serverUi == null) {
                serverUi = new KenaiServer(kenai);
                serverMap.put(kenai, serverUi);
            }
        }
        return serverUi;
    }
    
    public static DashboardSupport<KenaiProject> getDashboard(ProjectHandle<KenaiProject> pHandle) {
        return getDashboard(pHandle.getTeamProject().getKenai());
    }
    
    public static DashboardSupport<KenaiProject> getDashboard(Kenai kenai) {
        KenaiServer server = forKenai(kenai);
        return server.getDashboard();
    }
    
    public static ProjectHandle<KenaiProject>[] getDashboardProjects(boolean onlyOpened) {
        ArrayList<KenaiServer> servers;
        synchronized (serverMap) {
            servers = new ArrayList<KenaiServer>(serverMap.values());
        }
        LinkedList<ProjectHandle<KenaiProject>> ret = new LinkedList<ProjectHandle<KenaiProject>>();
        for (KenaiServer s : servers) {
            ProjectHandle<KenaiProject>[] projects = s.getDashboard().getProjects(onlyOpened);
            if(projects != null) {
                ret.addAll(Arrays.asList(projects));
            }
        }
        return ret.toArray(new ProjectHandle[ret.size()]);
    }
    
    @Override
    public URL getUrl () {
        return kenai.getUrl();
    }

    @Override
    public Status getStatus () {
        return kenai.getStatus() != Kenai.Status.OFFLINE ? Status.ONLINE : Status.OFFLINE;
    }

    @Override
    public void logout () {
        kenai.logout();
    }

    @Override
    public String getDisplayName () {
        return kenai.getName();
    }

    @Override
    public Icon getIcon () {
        return kenai.getIcon();
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener propertyChange) {
        propertyChangeSupport.addPropertyChangeListener(propertyChange);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener propertyChange) {
        propertyChangeSupport.removePropertyChangeListener(propertyChange);
    }

    @Override
    public TeamServerProvider getProvider () {
        return TeamServerProviderImpl.getDefault();
    }

    public Kenai getKenai () {
        return kenai;
    }

    @Override
    public LoginPanelSupport createLoginSupport () {
        return new LoginPanelSupportImpl(kenai);
    }

    @Override
    public JComponent getDashboardComponent () {
        return dashboard.getComponent();
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return kenai.getPasswordAuthentication();
    }

    public Collection<ProjectHandle<KenaiProject>> getMyProjects() {
        try {
            Collection<KenaiProject> projects = kenai.getMyProjects();
            List<ProjectHandle<KenaiProject>> ret = new ArrayList<ProjectHandle<KenaiProject>>(projects.size());
            for (KenaiProject p : projects) {
                ret.add(new ProjectHandleImpl(p));
            }
            return ret;
        } catch (KenaiException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }

    @Override
    public SelectionList getProjects( boolean forceRefresh ) {
        return getDashboard().getProjectsList(forceRefresh);
    }
    
    @Override
    public Action getNewProjectAction() {
        AbstractAction newProjectAction = new AbstractAction() {
            private NewKenaiProjectAction a = new NewKenaiProjectAction(kenai);
            @Override
            public void actionPerformed(ActionEvent e) {
                a.actionPerformed(e);
            }
        };
        newProjectAction.putValue( Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/new_team_project.png", true));
        newProjectAction.putValue( Action.SHORT_DESCRIPTION, NbBundle.getMessage(UserNode.class, "LBL_NewProject") );
        return newProjectAction;
    }
    
    @Override
    public Action getOpenProjectAction() {
        OpenKenaiProjectAction openProjectAction = new OpenKenaiProjectAction(kenai);
        openProjectAction.putValue( Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/open_team_project.png", true));
        openProjectAction.putValue( Action.SHORT_DESCRIPTION, NbBundle.getMessage(UserNode.class, "LBL_OpenProject") );
        return openProjectAction;
    }
}
