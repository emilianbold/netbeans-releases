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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.team.ui.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import javax.accessibility.AccessibleContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.netbeans.modules.team.ui.spi.DashboardProvider;
import org.netbeans.modules.team.ui.spi.LoginHandle;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.netbeans.modules.team.ui.util.treelist.SelectionList;
import org.netbeans.modules.team.ui.util.treelist.TreeLabel;
import org.netbeans.modules.team.ui.util.treelist.TreeList;
import org.netbeans.modules.team.ui.util.treelist.TreeListModel;
import org.netbeans.modules.team.ui.util.treelist.TreeListNode;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * Abstract class providing some common Team Dashboard functionality.
 *
 * @author S. Aubrecht, Tomas Stupka
 */
final class DefaultDashboard<P> implements DashboardSupport.DashboardImpl<P> {

    private LoginHandle login;
    private final TreeListModel model = new TreeListModel();
    private static final ListModel EMPTY_MODEL = new AbstractListModel() {
        @Override
        public int getSize() {
            return 0;
        }
        @Override
        public Object getElementAt(int index) {
            return null;
        }
    };
    private RequestProcessor requestProcessor = new RequestProcessor("Team Dashboard"); // NOI18N
    private final TreeList treeList = new TreeList(model);
    private final ArrayList<ProjectHandle> memberProjects = new ArrayList<>(50);
    private final ArrayList<ProjectHandle> openProjects = new ArrayList<>(50);
    //TODO: this should not be public
    private final JScrollPane dashboardComponent;
    private final PropertyChangeListener userListener;
    private boolean opened = false;
    private boolean memberProjectsLoaded = false;
    private boolean otherProjectsLoaded = false;

    private static final long TIMEOUT_INTERVAL_MILLIS = TreeListNode.TIMEOUT_INTERVAL_MILLIS;

    private OtherProjectsLoader otherProjectsLoader;
    private MemberProjectsLoader memberProjectsLoader;

    private final UserNode userNode;
    private final ErrorNode memberProjectsError;
    private final ErrorNode otherProjectsError;

    private final CategoryNode openProjectsNode;
    private final CategoryNode myProjectsNode;
    private final EmptyNode noOpenProjects = new EmptyNode(NbBundle.getMessage(DefaultDashboard.class, "NO_PROJECTS_OPEN"),NbBundle.getMessage(DefaultDashboard.class, "LBL_OpeningProjects"));
    private final EmptyNode noMyProjects = new EmptyNode(NbBundle.getMessage(DefaultDashboard.class, "NO_MY_PROJECTS"), NbBundle.getMessage(DefaultDashboard.class, "LBL_OpeningMyProjects"));

    private final Object LOCK = new Object();

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private PropertyChangeListener serverListener;
    private TeamServer server;
    private final DashboardProvider<P> dashboardProvider;

    public DefaultDashboard(TeamServer server, DashboardProvider<P> dashboardProvider) {
        this.dashboardProvider = dashboardProvider;
        dashboardComponent = new JScrollPane() {
            @Override
            public void requestFocus() {
                Component view = getViewport().getView();
                if (view != null) {
                    view.requestFocus();
                } else {
                    super.requestFocus();
                }
            }
            @Override
            public boolean requestFocusInWindow() {
                Component view = getViewport().getView();
                return view != null ? view.requestFocusInWindow() : super.requestFocusInWindow();
            }
        };
        dashboardComponent.setBorder(BorderFactory.createEmptyBorder());
        dashboardComponent.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
        userListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( LoginHandle.PROP_MEMBER_PROJECT_LIST.equals(evt.getPropertyName()) ) {
                    refreshMemberProjects(true);
                }
            }
        };

        userNode = new UserNode(
                new AbstractAction() {  // refresh
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        refreshProjects();
                    }
                },
                dashboardProvider.createLoginAction(),    // login    
                dashboardProvider.createLogoutAction(),
                dashboardProvider.getProjectAccessor().getNewTeamProjectAction(),
                dashboardProvider.getProjectAccessor().getOpenNonMemberProjectAction());
        model.addRoot(-1, userNode);
        openProjectsNode = new CategoryNode(org.openide.util.NbBundle.getMessage(DefaultDashboard.class, "LBL_OpenProjects"), null); // NOI18N
        model.addRoot(-1, openProjectsNode);
        model.addRoot(-1, noOpenProjects);

        myProjectsNode = new CategoryNode(org.openide.util.NbBundle.getMessage(DefaultDashboard.class, "LBL_MyProjects"), // NOI18N
                ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/bookmark.png", true)); // NOI18N
        if (login!=null) {
            if (!model.getRootNodes().contains(myProjectsNode)) {
                model.addRoot(-1, myProjectsNode);
            }
            if (!model.getRootNodes().contains(noMyProjects)) {
                model.addRoot(-1, noMyProjects);
            }
        }

        memberProjectsError = new ErrorNode(NbBundle.getMessage(DefaultDashboard.class, "ERR_OpenMemberProjects"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearError(memberProjectsError);
                refreshMemberProjects(true);
            }
        });

        otherProjectsError = new ErrorNode(NbBundle.getMessage(DefaultDashboard.class, "ERR_OpenProjects"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearError(otherProjectsError);
                refreshProjects();
            }
        });
        AccessibleContext accessibleContext = treeList.getAccessibleContext();
        String a11y = NbBundle.getMessage(DefaultDashboard.class, "A11Y_TeamProjects");
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        setServer(server);
    }

    /**
     * currently visible team instance
     * @return
     */
    @Override
    public TeamServer getServer() {
        return server;
    }

    @Override
    public ProjectHandle<P>[] getProjects(boolean onlyOpened) {
        TreeSet<ProjectHandle> s = new TreeSet();
        s.addAll(openProjects);
        if(!onlyOpened) {
            s.addAll(memberProjects);
        }
        return s.toArray(new ProjectHandle[s.size()]);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public boolean isMemberProject(ProjectHandle m) {
        return memberProjects.contains(m);
    }

    private void setServer(TeamServer server) {
        this.server = server;
        refreshNonMemberProjects();
        if (server==null) {
            return;
        }

        serverListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                switch (pce.getPropertyName()) {
                    case TeamServer.PROP_LOGIN:
                        final PasswordAuthentication newValue = (PasswordAuthentication) pce.getNewValue();
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run () {
                                if (newValue == null) {
                                    setUser(null);
                                } else {
                                    setUser(new LoginHandleImpl(newValue.getUserName()));
                                }
                                loggingFinished();
                            }
                        });
                        break;
                    case TeamServer.PROP_LOGIN_STARTED:
                        loggingStarted();
                        break;
                    case TeamServer.PROP_LOGIN_FAILED:
                        loggingFinished();
                        break; 
                }
            }
        };

        server.addPropertyChangeListener(WeakListeners.propertyChange(serverListener, server));
        final PasswordAuthentication newValue = server!=null?server.getPasswordAuthentication():null;
        if (newValue == null) {
            setUser(null);
        } else {
            setUser(new LoginHandleImpl(newValue.getUserName()));
        }
        
        final PasswordAuthentication pa = server.getPasswordAuthentication();
        this.login = pa==null ? null : new LoginHandleImpl(pa.getUserName());
        userNode.set(login, false);
    }
    
    @Override
    public void selectAndExpand(ProjectHandle project) {
        for (TreeListNode n:model.getRootNodes()) {
            if (n instanceof OpenProjectNode) {
                if (((OpenProjectNode)n).getProject().getId().equals(project.getId())) {
                    treeList.setSelectedValue(n, true);
                    n.setExpanded(true);
                }
            }
        }
    }

    /**
     * Display given Team user in the Dashboard window, the UI will start querying for
     * user's member projects.
     * Typically should be called after successful login.
     * @param login User login details.
     */
    public void setUser( final LoginHandle login ) {
        synchronized( LOCK ) {
            if( null != this.login ) {
                this.login.removePropertyChangeListener(userListener);
            }
            this.login = login;
            
            if (login==null) {

                //
                //remove private project from dashboard
                //private projects are visible only for
                //authenticated user who is member of this project
                Iterator<ProjectHandle> ph = openProjects.iterator();
                while (ph.hasNext()) {
                    final ProjectHandle next = ph.next();
                    if (next.isPrivate()) {
                        removeProjectsFromModel(Collections.singletonList(next));
                        ph.remove();
                    }
                    //storeAllProjects();
                }
                removeMemberProjectsFromModel(memberProjects);
                memberProjects.clear();

                model.removeRoot(myProjectsNode);
                model.removeRoot(noMyProjects);
            } else {
                if (!model.getRootNodes().contains(myProjectsNode)) {
                    model.addRoot(-1, myProjectsNode);
                }
                if (!model.getRootNodes().contains(noMyProjects)) {
                    model.addRoot(-1, noMyProjects);
                }
            }
//            removeMemberProjectsFromModel(memberProjects);
//            memberProjects.clear();
            memberProjectsLoaded = false;
            userNode.set(login, !openProjects.isEmpty());
            if( isOpened() ) {
                if( null != login ) {
                    startLoadingMemberProjects(false);
                    if (!otherProjectsLoaded) {
                        startLoadingAllProjects(false);
                    }
                }
                switchContent();
            }
            if( null != this.login ) {
                this.login.addPropertyChangeListener(userListener);
            }
        }
    }

    /**
     * Add a Team project to the Dashboard.
     * @param project
     * @param isMemberProject
     */
    @Override
    public void addProject(final ProjectHandle project, final boolean isMemberProject, final boolean select) {        
        TeamUIUtils.setSelectedServer(server);
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                synchronized (LOCK) {
                    Runnable selectAndExpand = new Runnable() {
                        @Override
                        public void run() {
                            selectAndExpand(project);
                        }
                    };
                    if (openProjects.contains(project)) {
                        if (select) {
                            SwingUtilities.invokeLater(selectAndExpand);
                        }
                        return;
                    }

                    if (isMemberProject && memberProjectsLoaded && !memberProjects.contains(project)) {
                        memberProjects.add(project);
                        setMemberProjects(new ArrayList<>(memberProjects));
                    }
                    openProjects.add(project);
                    storeAllProjects();
                    setOtherProjects(new ArrayList<>(openProjects));
                    userNode.set(login, !openProjects.isEmpty());
                    switchMemberProjects();
                    if (isOpened()) {
                        switchContent();
                        if (select) {
                            SwingUtilities.invokeLater(selectAndExpand);
                        }
                    }
                }
                changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
            }
        });
    }

    @Override
    public void removeProject( ProjectHandle<P> project ) {
        synchronized( LOCK ) {
            if( !openProjects.contains(project) ) {
                return;
            }
            openProjects.remove(project);

            storeAllProjects();
            ArrayList<ProjectHandle> tmp = new ArrayList<>(1);
            tmp.add(project);
            removeProjectsFromModel(tmp);
            if( isOpened() ) {
                switchContent();
            }
        }
        project.firePropertyChange(ProjectHandle.PROP_CLOSE, null, null);
        changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
    }

    private Action createWhatIsTeamAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    URLDisplayer.getDefault().showURL(
                            new URL(NbBundle.getMessage(DefaultDashboard.class, "URL_TeamOverview"))); //NOI18N
                } catch( MalformedURLException ex ) {
                    //shouldn't happen
                    Exceptions.printStackTrace(ex);
                }
            }
        };
    }

    boolean isOpened() {
        return opened;
    }

    void refreshProjects() {
        myProjectLoadingStarted();
        projectLoadingStarted();
        changeSupport.firePropertyChange(DashboardSupport.PROP_REFRESH_REQUEST, null, null);
        synchronized( LOCK ) {
            removeMemberProjectsFromModel(memberProjects);
            memberProjects.clear();
            memberProjectsLoaded = false;
            removeProjectsFromModel(openProjects);
            openProjects.clear();
            otherProjectsLoaded = false;
            if( isOpened() ) {
                startLoadingAllProjects(true);
                startLoadingMemberProjects(true);
            }
        }
    }

    public void refreshNonMemberProjects() {
        synchronized( LOCK ) {
            removeProjectsFromModel(openProjects);
            openProjects.clear();
            otherProjectsLoaded = false;
            if( isOpened() ) {
                startLoadingAllProjects(false);
            }
        }
    }

    @Override
    public void refreshMemberProjects(boolean force) {
        synchronized( LOCK ) {
            if (!force) {
                removeMemberProjectsFromModel(memberProjects);
            }
            memberProjects.clear();
            memberProjectsLoaded = false;
            if( isOpened() ) {
                startLoadingMemberProjects(force);
            }
        }
    }

    public void close() {
        synchronized( LOCK ) {
            treeList.setModel(EMPTY_MODEL);
            model.clear();
            opened = false;
        }
    }

    @Override
    public JComponent getComponent() {
        synchronized( LOCK ) {
            if (!opened) {
                requestProcessor.post(new Runnable() {
                    @Override
                    public void run() {
                        TeamUIUtils.waitStartupFinished();
                        myProjectLoadingStarted();
                        projectLoadingStarted();
                        if (null != login) {
                            if (!memberProjectsLoaded) {
                                startLoadingMemberProjects(false);
                            }
                            if (!otherProjectsLoaded) {
                                startLoadingAllProjects(false);
                            }
                        }
                    }
                });
                switchContent();
                opened = true;
            }
        }
        return dashboardComponent;
    }

    private void fillModel() {
        synchronized( LOCK ) {
            if( !model.getRootNodes().contains(userNode) ) {
                model.addRoot(0, userNode);
                model.addRoot(1, openProjectsNode);
                if (login!=null&& !model.getRootNodes().contains(myProjectsNode)) {
                    model.addRoot(-1, myProjectsNode);
                }
            }
            if(login!=null?model.getSize() > 3:model.getSize()>2 ) {
                return;
            }
            addProjectsToModel(-1, openProjects);
            addMemberProjectsToModel(-1, memberProjects);
        }
    }

    private void switchContent() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean isEmpty;

                synchronized( LOCK ) {
                    isEmpty = null == DefaultDashboard.this.login && openProjects.isEmpty();
                }

                boolean isTreeListShowing = dashboardComponent.getViewport().getView() == treeList;
                if( isEmpty ) {
                    if( isTreeListShowing || dashboardComponent.getViewport().getView() == null ) {
                        dashboardComponent.setViewportView(createEmptyContent());
                        dashboardComponent.invalidate();
                        dashboardComponent.revalidate();
                        dashboardComponent.repaint();
                    }
                } else {
                    fillModel();
                    treeList.setModel(model);
                    switchMemberProjects();
                    if( !isTreeListShowing ) {
                        dashboardComponent.setViewportView(treeList);
                        dashboardComponent.invalidate();
                        dashboardComponent.revalidate();
                        dashboardComponent.repaint();
                        // hack: ensure the dashboard component has focus (when
                        // added to already visible and activated TopComponent)
                        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, dashboardComponent);
                        if (tc != null && TopComponent.getRegistry().getActivated() == tc) {
                            treeList.requestFocus();
                        }
                    }
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() ) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private JComponent createEmptyContent() {
        JPanel res = new JPanel( new GridBagLayout() );
        res.setOpaque(false);

        JLabel lbl = new TreeLabel(NbBundle.getMessage(DefaultDashboard.class, "LBL_No_Team_Project_Open")); //NOI18N
        lbl.setForeground(ColorManager.getDefault().getDisabledColor());
        lbl.setHorizontalAlignment(JLabel.CENTER);
        LinkButton btnWhatIs = new LinkButton(NbBundle.getMessage(DefaultDashboard.class, "LBL_WhatIsTeam"), createWhatIsTeamAction() ); //NOI18N

        model.removeRoot(userNode);
        model.removeRoot(myProjectsNode);
        model.removeRoot(openProjectsNode);
        userNode.set(null, false);
        res.add( userNode.getComponent(UIManager.getColor("List.foreground"), ColorManager.getDefault().getDefaultBackground(), false, false, 200), new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 4, 3, 4), 0, 0) ); //NOI18N
        res.add( new JLabel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        res.add( lbl, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
        res.add( btnWhatIs, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
        res.add( new JLabel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        return res;
    }

    private void startLoadingAllProjects(boolean forceRefresh) {
        if (server==null) {
            return;
        }
        String teamName = server.getUrl().getHost();
        Preferences prefs = NbPreferences.forModule(DefaultDashboard.class).node(DashboardSupport.PREF_ALL_PROJECTS + ("kenai.com".equals(teamName)?"":"-"+teamName)); //NOI18N
        int count = prefs.getInt(DashboardSupport.PREF_COUNT, 0); //NOI18N
        if( 0 == count ) {
            projectLoadingFinished();
            return; //nothing to load
        }
        ArrayList<String> ids = new ArrayList<>(count);
        for( int i=0; i<count; i++ ) {
            String id = prefs.get(DashboardSupport.PREF_ID+i, null); //NOI18N
            if( null != id && id.trim().length() > 0 ) {
                ids.add( id.trim() );
            }
        }
        synchronized( LOCK ) {
            if( otherProjectsLoader != null ) {
                otherProjectsLoader.cancel();
            }
            if( ids.isEmpty() ) {
                projectLoadingFinished();
                return;
            }
            otherProjectsLoader = new OtherProjectsLoader(ids, forceRefresh);
            requestProcessor.post(otherProjectsLoader);
        }
    }

    private void storeAllProjects() {
        String serverName = server.getUrl().getHost();
        Preferences prefs = NbPreferences.forModule(DefaultDashboard.class).node(DashboardSupport.PREF_ALL_PROJECTS + ("kenai.com".equals(serverName)?"":"-"+serverName)); //NOI18N
        int index = 0;
        for( ProjectHandle project : openProjects ) {
            //do not store private projects
//            if (!project.isPrivate()) {
                prefs.put(DashboardSupport.PREF_ID+index++, project.getId()); //NOI18N
//            }
        }
        //store size
        prefs.putInt(DashboardSupport.PREF_COUNT, index); //NOI18N
    }

    private void setOtherProjects(ArrayList<ProjectHandle> projects) {
        synchronized( LOCK ) {
            removeProjectsFromModel( openProjects );
            if (!projects.isEmpty()) {
                model.removeRoot(noOpenProjects);
            }
            openProjects.clear();
            for( ProjectHandle p : projects ) {
                if( !openProjects.contains( p ) ) {
                    openProjects.add( p );
                }
            }
            Collections.sort(openProjects);
            otherProjectsLoaded = true;
            addProjectsToModel( -1, openProjects ); 
            userNode.set(login, !openProjects.isEmpty());
            storeAllProjects();

            switchMemberProjects();
            
            if( isOpened() ) {
                switchContent();
            }        
        }
        changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
    }

    private void switchMemberProjects() {
        for( TreeListNode n : model.getRootNodes() ) {
            if( !(n instanceof OpenProjectNode) ) {
                continue;
            }
            OpenProjectNode pn = (OpenProjectNode) n;
            pn.setMemberProject( memberProjects.contains( pn.getProject() ) );
        }
    }

    @Override
    public void bookmarkingStarted(ProjectHandle<P> project) {
        userNode.loadingStarted(NbBundle.getMessage(DefaultDashboard.class, "LBL_Bookmarking"));
    }

    @Override
    public void bookmarkingFinished(ProjectHandle<P> project) {
        userNode.loadingFinished();
    }
    
    @Override
    public void deletingStarted() {
        userNode.loadingStarted(NbBundle.getMessage(DefaultDashboard.class, "LBL_Deleting"));
    }

    @Override
    public void deletingFinished() {
        userNode.loadingFinished();
    }

    public void loggingStarted() {
        userNode.loadingStarted(NbBundle.getMessage(DefaultDashboard.class, "LBL_Authenticating"));
    }

    public void loggingFinished() {
        userNode.loadingFinished();
    }

    @Override
    public void xmppStarted() {
        userNode.loadingStarted(NbBundle.getMessage(DefaultDashboard.class, "LBL_ConnectingXMPP"));
    }

    @Override
    public void xmppFinsihed() {
        userNode.loadingFinished();
    }

    private void projectLoadingStarted() {
        noOpenProjects.loadingStarted();
    }

    private void projectLoadingFinished() {
        noOpenProjects.loadingFinished();
    }

    private void myProjectLoadingStarted() {
        noMyProjects.loadingStarted();
    }

    private void myProjectLoadingFinished() {
        noMyProjects.loadingFinished();
    }

    @Override
    public void myProjectsProgressStarted() {
        userNode.loadingStarted(NbBundle.getMessage(DefaultDashboard.class, "LBL_LoadingIssues"));
    }

    @Override
    public void myProjectsProgressFinished() {
        userNode.loadingFinished();
    }

    private void startLoadingMemberProjects(boolean forceRefresh) {
        synchronized( LOCK ) {
            if( memberProjectsLoader != null ) {
                memberProjectsLoader.cancel();
            }
            if( null == login ) {
                return;
            }
            memberProjectsLoader = new MemberProjectsLoader(login, forceRefresh);
            requestProcessor.post(memberProjectsLoader);
        }
    }

    private void setMemberProjects(ArrayList<ProjectHandle> projects) {
        synchronized( LOCK ) {
            if( projects.isEmpty() ) {
                if( isOpened() ) {
                    switchContent();
                }
            } 

            removeMemberProjectsFromModel(memberProjects );

            if(!projects.isEmpty() ) {
                model.removeRoot(noMyProjects);
            }
            
            memberProjects.clear();
            memberProjects.addAll(projects);
            memberProjectsLoaded = true;
            for( ProjectHandle p : projects ) {
                if( !memberProjects.contains(p) ) {
                    memberProjects.add(p);
                }
            }
            Collections.sort(memberProjects);
//            storeAllProjects();
            addMemberProjectsToModel(-1, memberProjects );
            userNode.set(login, !memberProjects.isEmpty());

            switchMemberProjects();

            if( isOpened() ) {
                switchContent();
            }
        }
        changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
    }

    private void addProjectsToModel( int index, final List<ProjectHandle> projects ) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                int counter = 2;
                for( ProjectHandle p : projects ) {
                    model.addRoot(counter++, new OpenProjectNode(p, DefaultDashboard.this));
                }
            }
        };
        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private void addMemberProjectsToModel( int index, List<ProjectHandle> projects ) {
        for( ProjectHandle p : projects ) {
            model.addRoot(index, dashboardProvider.createMyProjectNode(p, true, false, null) );
        }
    }

    private void removeProjectsFromModel( List<ProjectHandle> projects ) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<>(projects.size());
        int i=0;
        for( TreeListNode root : model.getRootNodes() ) {
            if( root instanceof OpenProjectNode ) {
                OpenProjectNode pn = (OpenProjectNode) root;
                i++;
                if( projects.contains( pn.getProject() ) ) {
                    nodesToRemove.add(root);
                }
            }
        }
        for( TreeListNode node : nodesToRemove ) {
            model.removeRoot(node);
        }
        if (i==projects.size()) {
            if (!model.getRootNodes().contains(noOpenProjects)) {
                model.addRoot(2, noOpenProjects);
            }
        }
    }

    private void removeMemberProjectsFromModel( List<ProjectHandle> projects ) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<>(projects.size());
        int i=0;
        for( TreeListNode root : model.getRootNodes() ) {
            if( root instanceof MyProjectNode ) {
                i++;
                if( projects.contains( ((MyProjectNode)root).getProject() ) ) {
                    nodesToRemove.add(root);
                }
            }
        }
        for( TreeListNode node : nodesToRemove ) {
            model.removeRoot(node);
        }
        if (i==projects.size()) {
            if (!model.getRootNodes().contains(noMyProjects) && login!=null) {
                model.addRoot(-1, noMyProjects);
            }
        }
    }

    private void showError( ErrorNode node ) {
        synchronized( LOCK ) {
            List<TreeListNode> roots = model.getRootNodes();
            if( !roots.contains(node) ) {
                model.addRoot(1, node);
            }
        }
    }

    private void clearError( ErrorNode node ) {
        synchronized( LOCK ) {
            model.removeRoot(node);
        }
    }

    @Override
    public DashboardProvider<P> getDashboardProvider() {
        return dashboardProvider;
    }

    Collection<ProjectHandle<P>> getMyProjects() {
        return dashboardProvider.getMyProjects();
    }

    @Override
    public SelectionList getProjectsList(boolean forceRefresh) {
        throw new IllegalStateException("Projects list shouldn't be requested when having multiple projects dashboard mode"); // NOI18N
    }

    private class OtherProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final ArrayList<String> projectIds;
        private final boolean forceRefresh;

        public OtherProjectsLoader( ArrayList<String> projectIds, boolean forceRefresh ) {
            this.projectIds = projectIds;
            this.forceRefresh = forceRefresh;
        }

        @Override
        public void run() {
            projectLoadingStarted();
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    ArrayList<ProjectHandle> projects = new ArrayList<>(projectIds.size());
                    ProjectAccessor<P> accessor = dashboardProvider.getProjectAccessor();
                    boolean err = false;
                    for( String id : projectIds ) {
                        ProjectHandle handle = accessor.getNonMemberProject(server, id, forceRefresh);
                        if (handle!=null) {
                            projects.add(handle);
                        } else {
                            err = true;
                        }
                    }
                    res[0] = err && projects.isEmpty() ? null : projects;
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TIMEOUT_INTERVAL_MILLIS );
            } catch( InterruptedException iE ) {
                //ignore
            }
            projectLoadingFinished();
            projectLoadingFinished();
            if( cancelled ) {
                return;
            }
            if( null == res[0] ) {
                showError( otherProjectsError );
                return;
            }

            setOtherProjects( res[0] );
            clearError( otherProjectsError );
        }

        @Override
        public boolean cancel() {
            cancelled = true;
            if( null != t ) {
                t.interrupt();
            }
            return true;
        }

    }

    private class MemberProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final LoginHandle user;
        private final boolean forceRefresh;

        public MemberProjectsLoader( LoginHandle login, boolean forceRefresh ) {
            this.user = login;
            this.forceRefresh = forceRefresh;
        }

        @Override
        public void run() {
            myProjectLoadingStarted();
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    ProjectAccessor<P> accessor = dashboardProvider.getProjectAccessor();
                    List<ProjectHandle<P>> l = accessor.getMemberProjects(server, user, forceRefresh);
                    res[0] = l == null ? null : new ArrayList<>( l );
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TIMEOUT_INTERVAL_MILLIS );
            } catch( InterruptedException iE ) {
                //ignore
            }
            myProjectLoadingFinished();
            myProjectLoadingFinished();
            if( cancelled ) {
                return;
            }
            if( null == res[0] ) {
                showError( memberProjectsError );
                return;
            }

            setMemberProjects( res[0] );
            clearError( memberProjectsError );
        }

        @Override
        public boolean cancel() {
            cancelled = true;
            if( null != t ) {
                t.interrupt();
            }
            return true;
        }
    }
}
