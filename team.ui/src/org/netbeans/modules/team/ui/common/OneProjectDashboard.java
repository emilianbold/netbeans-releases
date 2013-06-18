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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ToolBarUI;
import org.netbeans.modules.team.ui.TeamView;
import org.netbeans.modules.team.ui.picker.MegaMenu;
import org.netbeans.modules.team.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.ui.spi.DashboardProvider;
import org.netbeans.modules.team.ui.spi.LoginHandle;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.netbeans.modules.team.ui.util.treelist.ListNode;
import org.netbeans.modules.team.ui.util.treelist.ProgressLabel;
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
final class OneProjectDashboard<P> implements DashboardSupport.DashboardImpl<P> {

    /**
     * fired when user clicks refresh
     */
    public static final String PROP_REFRESH_REQUEST = "refreshRequest";// NOI18N
    
    public static final String PREF_ALL_PROJECTS = "allProjects"; //NOI18N
    public static final String PREF_COUNT = "count"; //NOI18N
    public static final String PREF_ID = "id"; //NOI18N
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
    private final RequestProcessor requestProcessor = new RequestProcessor("Team Dashboard"); // NOI18N
    private final TreeList treeList = new TreeList(model);
                
    private final ArrayList<ProjectHandle<P>> memberProjects = new ArrayList<>(50);
    private final ArrayList<ProjectHandle<P>> openProjects = new ArrayList<>(50);
    //TODO: this should not be public
    public final JScrollPane dashboardComponent;
    public final JPanel dashboardPanel;
    private final PropertyChangeListener userListener;
    private boolean opened = false;
    private boolean memberProjectsLoaded = false;
    private boolean otherProjectsLoaded = false;

    private static final long TIMEOUT_INTERVAL_MILLIS = TreeListNode.TIMEOUT_INTERVAL_MILLIS;

    private OtherProjectsLoader otherProjectsLoader;
    private MemberProjectsLoader memberProjectsLoader;

    private final ErrorNode memberProjectsError;
    private final ErrorNode otherProjectsError;

    private final ProjectPicker projectPicker;

    private final Object LOCK = new Object();

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private PropertyChangeListener serverListener;
    private TeamServer server;
    private final DashboardProvider<P> dashboardProvider;
    private final Map<String, MyProjectNode> projectNodes = Collections.synchronizedMap(new HashMap<String, MyProjectNode>());

    public OneProjectDashboard(TeamServer server, DashboardProvider<P> dashboardProvider) {
        this.dashboardProvider = dashboardProvider;
        this.server = server;
        
        projectPicker = new ProjectPicker();
        projectPicker.setOpaque(false);
        
        dashboardPanel = new JPanel(new BorderLayout());
        
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
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder());
        dashboardPanel.setBackground(ColorManager.getDefault().getDefaultBackground());
        projectPicker.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardComponent.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
        
        dashboardPanel.add(projectPicker, BorderLayout.NORTH);
        dashboardPanel.add(dashboardComponent, BorderLayout.CENTER);
                    
        userListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( LoginHandle.PROP_MEMBER_PROJECT_LIST.equals(evt.getPropertyName()) ) {
                    refreshMemberProjects(true);
                }
            }
        };

//        userNode = new UserNode(
//                new AbstractAction() {  // refresh
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        refreshProjects();
//                    }
//                },
//                dashboardProvider.createLoginAction(),    // login    
//                dashboardProvider.createLogoutAction(),
//                dashboardProvider.getProjectAccessor().getNewTeamProjectAction(),
//                dashboardProvider.getProjectAccessor().getOpenNonMemberProjectAction());
//        model.addRoot(-1, userNode);
//        openProjectsNode = new CategoryNode(org.openide.util.NbBundle.getMessage(DefaultDashboard.class, "LBL_OpenProjects"), null); // NOI18N
//        model.addRoot(-1, openProjectsNode);
//        model.addRoot(-1, noOpenProjects);

//        myProjectsNode = new CategoryNode(org.openide.util.NbBundle.getMessage(DefaultDashboard.class, "LBL_MyProjects"), // NOI18N
//                ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/bookmark.png", true)); // NOI18N
//        if (login!=null) {
//            if (!model.getRootNodes().contains(myProjectsNode)) {
//                model.addRoot(-1, myProjectsNode);
//            }
//            if (!model.getRootNodes().contains(noMyProjects)) {
//                model.addRoot(-1, noMyProjects);
//            }
//        }

        memberProjectsError = new ErrorNode(NbBundle.getMessage(DashboardSupport.class, "ERR_OpenMemberProjects"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearError(memberProjectsError);
                refreshMemberProjects(true);
            }
        });

        otherProjectsError = new ErrorNode(NbBundle.getMessage(DashboardSupport.class, "ERR_OpenProjects"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearError(otherProjectsError);
                refreshProjects();
            }
        });
        AccessibleContext accessibleContext = treeList.getAccessibleContext();
        String a11y = NbBundle.getMessage(DashboardSupport.class, "A11Y_TeamProjects");
        accessibleContext.setAccessibleName(a11y);
        accessibleContext.setAccessibleDescription(a11y);
        
        initServer();
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
//        if(!onlyOpened) { // XXX lets consider all projects as open - they are listed in the mega menu anyway
            s.addAll(memberProjects);
//        }
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

    private void initServer() {

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
                                    handleLogin(null);
                                    setNoProject();
                                } else {
                                    handleLogin(new LoginHandleImpl(newValue.getUserName()));
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
            handleLogin(null);
        } else {
            handleLogin(new LoginHandleImpl(newValue.getUserName()));
        }
        
        final PasswordAuthentication pa = server.getPasswordAuthentication();
        this.login = pa==null ? null : new LoginHandleImpl(pa.getUserName());
//        userNode.set(login, false);
    }
    
    @Override
    public void selectAndExpand(ProjectHandle project) {
        // XXX does not work - there are no Project nodes in the model
        for (TreeListNode n:model.getRootNodes()) {
            if (n instanceof MyProjectNode) {
                if (((MyProjectNode)n).getProject().getId().equals(project.getId())) {
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
    private void handleLogin( final LoginHandle login ) {
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
                Iterator<ProjectHandle<P>> ph = openProjects.iterator();
                while (ph.hasNext()) {
                    final ProjectHandle<P> next = ph.next();
                    if (next.isPrivate()) {
                        removeProjectNodes(Collections.singletonList(next));
                        ph.remove();
                    }
                    //storeAllProjects();
                }
//                removeMemberProjectsFromModel(memberProjects);
                memberProjects.clear();

//                model.removeRoot(myProjectsNode);
//                model.removeRoot(noMyProjects);
            } else {
//                if (!model.getRootNodes().contains(myProjectsNode)) {
//                    model.addRoot(-1, myProjectsNode);
//                }
//                if (!model.getRootNodes().contains(noMyProjects)) {
//                    model.addRoot(-1, noMyProjects);
//                }
                }
//            removeMemberProjectsFromModel(memberProjects);
            memberProjectsLoaded = false;
//            userNode.set(login, !openProjects.isEmpty());
            if( isOpened() ) {
                if( null != login ) {
                    requestProcessor.post(new Runnable() {
                        @Override
                        public void run() {
                            startLoadingMemberProjects(false);
                            if (!otherProjectsLoaded) {
                                startLoadingAllProjects(false);
                            }
                            switchContent();
                        }    
                    });
                }
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
    public void addProject(final ProjectHandle<P> project, final boolean isMemberProject, final boolean select) {        
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
//                    userNode.set(login, !openProjects.isEmpty());
                    switchMemberProjects();
                    switchProject(project, createProjectNode(project));
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
            removeProjectNodes( Collections.singleton(project) );
            
            storeAllProjects();

            ProjectHandle currentProject = projectPicker.getCurrentProject();
            if(currentProject != null && project.getId().equals(currentProject.getId())) {
                setNoProject();
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
                            new URL(NbBundle.getMessage(DashboardSupport.class, "URL_TeamOverview"))); //NOI18N
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
        changeSupport.firePropertyChange(PROP_REFRESH_REQUEST, null, null);
        synchronized( LOCK ) {
            memberProjectsLoaded = false;
            otherProjectsLoaded = false;
//            if( isOpened() ) {
                startLoadingAllProjects(true);
                startLoadingMemberProjects(true);
//            }
        }
    }

    public void refreshNonMemberProjects() {
        synchronized( LOCK ) {
            otherProjectsLoaded = false;
            if( isOpened() ) {
                startLoadingAllProjects(false);
            }
        }
    }

    @Override
    public void refreshMemberProjects(boolean force) {
        synchronized( LOCK ) {
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
        return dashboardPanel;
    }

    private void fillModel() {
//        synchronized( LOCK ) {
//            if( !model.getRootNodes().contains(userNode) ) {
//                model.addRoot(0, userNode);
//                model.addRoot(1, openProjectsNode);
//                if (login!=null&& !model.getRootNodes().contains(myProjectsNode)) {
//                    model.addRoot(-1, myProjectsNode);
//                }
//                }
//            if(login!=null?model.getSize() > 3:model.getSize()>2 ) {
//                return;
//            }
//            addProjectsToModel(-1, openProjects);
//            addMemberProjectsToModel(-1, memberProjects);
//        }
    }

    private void switchContent() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                boolean isEmpty;

                synchronized( LOCK ) {
                    isEmpty = projectPicker.getCurrentProject() == null;
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

        JLabel lbl = new TreeLabel(NbBundle.getMessage(DashboardSupport.class, "LBL_No_Team_Project_Open")); //NOI18N
        lbl.setForeground(ColorManager.getDefault().getDisabledColor());
        lbl.setHorizontalAlignment(JLabel.CENTER);
        LinkButton btnWhatIs = new LinkButton(NbBundle.getMessage(DashboardSupport.class, "LBL_WhatIsTeam"), createWhatIsTeamAction() ); //NOI18N

//        model.removeRoot(userNode);
////        model.removeRoot(myProjectsNode);
////        model.removeRoot(openProjectsNode);
//        userNode.set(null, false);
//        res.add( userNode.getComponent(UIManager.getColor("List.foreground"), ColorManager.getDefault().getDefaultBackground(), false, false, 200), new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 4, 3, 4), 0, 0) ); //NOI18N
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
        Preferences prefs = NbPreferences.forModule(DashboardSupport.class).node(PREF_ALL_PROJECTS + ("kenai.com".equals(teamName)?"":"-"+teamName)); //NOI18N
        int count = prefs.getInt(PREF_COUNT, 0); //NOI18N
        if( 0 == count ) {
            projectLoadingFinished();
            return; //nothing to load
        }
        ArrayList<String> ids = new ArrayList<>(count);
        for( int i=0; i<count; i++ ) {
            String id = prefs.get(PREF_ID+i, null); //NOI18N
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
            otherProjectsLoader.run();
//            requestProcessor.post(otherProjectsLoader).waitFinished();
        }
    }

    private void storeAllProjects() {
        String serverName = server.getUrl().getHost();
        Preferences prefs = NbPreferences.forModule(DashboardSupport.class).node(PREF_ALL_PROJECTS + ("kenai.com".equals(serverName)?"":"-"+serverName)); //NOI18N
        int index = 0;
        for( ProjectHandle project : openProjects ) {
            //do not store private projects
//            if (!project.isPrivate()) {
                prefs.put(PREF_ID+index++, project.getId()); //NOI18N
//            }
        }
        //store size
        prefs.putInt(PREF_COUNT, index); //NOI18N
    }

    private void setOtherProjects(ArrayList<ProjectHandle<P>> projects) {
        synchronized( LOCK ) {
            
            ArrayList<ProjectHandle<P>> toRemove = new ArrayList<>(openProjects);
            
            openProjects.clear();
            for( ProjectHandle p : projects ) {
                if( !openProjects.contains( p ) ) {
                    openProjects.add( p );
                    toRemove.remove(p);
                }
            }
            Collections.sort(openProjects);
            
            toRemove.removeAll(openProjects);
            toRemove.removeAll(memberProjects);
            removeProjectNodes( toRemove );
            
            otherProjectsLoaded = true;
//            addProjectsToModel( -1, openProjects );
//            userNode.set(login, !openProjects.isEmpty());
            storeAllProjects();

            switchMemberProjects();
            
            if( isOpened() ) {
                switchContent();
            }
        }
        changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
    }

    private void switchMemberProjects() {
        for( ListNode n : projectNodes.values() ) {
            if( !(n instanceof MyProjectNode) ) {
                continue;
            }
            MyProjectNode pn = (MyProjectNode) n;
            pn.setIsMember( memberProjects.contains( pn.getProject() ) );
        }
    }

    @Override
    public void bookmarkingStarted(final ProjectHandle<P> project) {
        doNotRunInAWT(new Runnable() {
            @Override
            public void run() {
                final MyProjectNode node = projectNodes.get(project.getId());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(node != null) {
                            node.bookmarkingStarted();
                        }
                        projectPicker.bookmarkingStarted(project);        
                    }
                });
            }
        }); 
    }

    @Override
    public void bookmarkingFinished(final ProjectHandle<P> project) {
        doNotRunInAWT(new Runnable() {
            @Override
            public void run() {
                refreshMemberProjects(false);
                final MyProjectNode node = projectNodes.get(project.getId());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(node != null) {
                            node.bookmarkingFinished();
                        }
                        projectPicker.bookmarkingFinished(project);        
                    }
                });
            }
        });
    }

    private void doNotRunInAWT(Runnable r) {
        if(SwingUtilities.isEventDispatchThread()) {
            requestProcessor.post(r);
        } else {
            r.run();
        }
    }
    
    @Override
    public void deletingStarted() {
//        userNode.loadingStarted(NbBundle.getMessage(DashboardSupport.class, "LBL_Deleting"));
    }

    @Override
    public void deletingFinished() {
//        userNode.loadingFinished();
    }

    public void loggingStarted() {
//        userNode.loadingStarted(NbBundle.getMessage(DashboardSupport.class, "LBL_Authenticating"));
    }

    public void loggingFinished() {
//        userNode.loadingFinished();
    }

    @Override
    public void xmppStarted() {
//        userNode.loadingStarted(NbBundle.getMessage(DashboardSupport.class, "LBL_ConnectingXMPP"));
    }

    @Override
    public void xmppFinsihed() {
//        userNode.loadingFinished();
    }

    private void projectLoadingStarted() {
//        noOpenProjects.loadingStarted();
    }

    private void projectLoadingFinished() {
//        noOpenProjects.loadingFinished();
    }

    private void myProjectLoadingStarted() {
//        noMyProjects.loadingStarted();
    }

    private void myProjectLoadingFinished() {
//        noMyProjects.loadingFinished();
    }

    @Override
    public void myProjectsProgressStarted() {
//        userNode.loadingStarted(NbBundle.getMessage(DashboardSupport.class, "LBL_LoadingIssues"));
    }

    @Override
    public void myProjectsProgressFinished() {
//        userNode.loadingFinished();
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
//            requestProcessor.post(memberProjectsLoader).waitFinished();
            memberProjectsLoader.run();
        }
    }

    private void setMemberProjects(ArrayList<ProjectHandle<P>> projects) {
        synchronized( LOCK ) {
            if( projects.isEmpty() ) {
                if( isOpened() ) {
                    switchContent();
                }
            } 

            ArrayList<ProjectHandle<P>> toRemove = new ArrayList<>(memberProjects);
            
            memberProjects.clear();
            memberProjects.addAll(projects);
            memberProjectsLoaded = true;
            for( ProjectHandle p : projects ) {
                if( !memberProjects.contains(p) ) {
                    memberProjects.add(p);
                    toRemove.remove(p);
                }
            }
            Collections.sort(memberProjects);
            
            toRemove.removeAll(memberProjects);
            toRemove.removeAll(openProjects);
            removeProjectNodes( toRemove );
            
//            storeAllProjects();
//            addMemberProjectsToModel(-1, memberProjects );
//            userNode.set(login, !memberProjects.isEmpty());

            switchMemberProjects();

            if( isOpened() ) {
                switchContent();
            }
        }
        changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
    }

    private void switchProject(ProjectHandle ph, ListNode node) {
        projectPicker.setCurrentProject(ph, node);
        switchProject(ph);
        TeamView.getInstance().setSelectedServer(server);
        projectPicker.hideMenu(); // in case the mega menu is hanging around
    }
            
    Map<String, List<TreeListNode>> projectChildren = new HashMap<>();
    private void switchProject(ProjectHandle project) {
        switchContent();
        for( TreeListNode node : model.getRootNodes() ) {
            model.removeRoot(node);
        }
        if(project != null) {
            List<TreeListNode> children = projectChildren.get(project.getId());
            if(children == null) {
                children = createProjectChildren(project);
                projectChildren.put(project.getId(), children);
            } 
            int idx = 1;
            for (TreeListNode n : children) {
                model.addRoot(idx++, n);
            }
        }
    }

    protected List<TreeListNode> createProjectChildren(ProjectHandle project) {
        ArrayList<TreeListNode> children = new ArrayList<>();
        DashboardProvider<P> provider = getDashboardProvider();
        children.add( provider.createProjectLinksNode(null, project) ); 
        if( null != provider.getMemberAccessor() ) {
            children.add( new MemberListNode(null, project, provider) );
        }
        BuilderAccessor builds = provider.getBuilderAccessor();
        if (builds != null && builds.isEnabled(project)) {
            children.add(new BuildListNode(null, project, builds));
        }
        if( null != provider.getQueryAccessor() ) {
            children.add( new QueryListNode(null, project, provider) );
        }
        if( null != provider.getSourceAccessor() ) {
            children.add( provider.createSourceListNode(null, project) );
        }
        return children;
    }

    private void removeProjectNodes( Collection<ProjectHandle<P>> projects ) {
        for( ProjectHandle p : projects ) {
            projectNodes.remove( p.getId() );
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
        if(forceRefresh) {
            refreshProjects();
        }
        final SelectionList res = new SelectionList();
        synchronized( LOCK ) {
            for (ProjectHandle<P> p : memberProjects) {
                if(!projectNodes.containsKey(p.getId())) {
                    projectNodes.put(p.getId(), createProjectNode(p));
                }
            }
            for (ProjectHandle p : openProjects) {
                if(!projectNodes.containsKey(p.getId())) {
                    projectNodes.put(p.getId(), createProjectNode(p));
                }
            }
            res.setItems(new ArrayList<ListNode>(projectNodes.values()));
        }
        
        res.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ListNode node = res.getSelectedValue();
                if(node == null) {
                    return;
                }
                ProjectHandle ph = ((MyProjectNode)node).getProject();
                if(!node.equals(projectPicker.getCurrentProjectNode())) {
                    switchProject(ph, node);
                }
            }
        });
        return res;
    };

    private MyProjectNode createProjectNode(ProjectHandle<P> p) {
        return dashboardProvider.createMyProjectNode(p, false, true, new CloseProjectAction(p));
    }

    private void setNoProject() throws MissingResourceException {
        projectPicker.setNoProject();
        switchProject(null);
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

    @NbBundle.Messages({"LBL_Switch=Select project", 
                        "LBL_NewServer=New Connection"})
    public class ProjectPicker extends JPanel {
        
        private ProjectHandle currentProject;
        private ListNode currentProjectNode;

        private final JLabel lbl;
        private final LinkButton btnPick;
        
        private final LinkButton btnNewServer;
        private final LinkButton btnBookmark;
        private final ProgressLabel lblBookmarkingProgress;
        private final LinkButton btnClose;
        private final JToolBar.Separator separator;
        
        private final AbstractAction bookmarkAction;
        
        private final MListener mListener;
        private final JLabel placeholder;
        private boolean bookmarking;
        
        public ProjectPicker() {
            setLayout( new GridBagLayout() );
            setOpaque(false);

            JSeparator jSeparator = new javax.swing.JSeparator();
            add(jSeparator, new GridBagConstraints(0,1,5,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,3,0,0), 0,0));            
            
            lbl = new JLabel();
            lbl.setFont( lbl.getFont().deriveFont( Font.BOLD, lbl.getFont().getSize2D() + 1 ) );
            add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8,8,8,3), 0,0) );
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    switchProject();
                }
                @Override
                public void mousePressed(MouseEvent e) { }
            };
            lbl.addMouseListener(mouseAdapter);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            ImageIcon arrowDown = ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/arrow-down.png", true); //NOI18N
            btnPick = new LinkButton(arrowDown, new AbstractAction(Bundle.LBL_Switch()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switchProject();
                }
            }); 
            btnPick.setToolTipText(Bundle.LBL_Switch());
            btnPick.setRolloverEnabled(true);
            add( btnPick, new GridBagConstraints(1,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,0,0), 0,0) );            

            placeholder = new JLabel();
            add( placeholder, new GridBagConstraints(2,0,1,1,1.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0,0) );

            JToolBar toolbar = new ProjectToolbar();
            add( toolbar, new GridBagConstraints(3,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3,3,3,3), 0,0) );            

            bookmarkAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    assert currentProject != null;
                    DashboardProvider<P> provider = getDashboardProvider();
                    ProjectAccessor<P> paccessor = provider.getProjectAccessor();
                    paccessor.bookmark(currentProject);
                }
            };
            
            btnBookmark = new LinkButton( (ImageIcon) null, bookmarkAction ); //NOI18N
            btnBookmark.setRolloverEnabled(true);
            btnBookmark.setVisible(false);
            toolbar.add(btnBookmark);
            lblBookmarkingProgress = new ProgressLabel("", this);
            lblBookmarkingProgress.setVisible(false);
            toolbar.add(lblBookmarkingProgress);
            
            Action closeAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    assert currentProject != null;
                    if(currentProject != null) {
                        if(OneProjectDashboard.this.isMemberProject(currentProject)) {
                            OneProjectDashboard.this.setNoProject();
                        } else {
                            new CloseProjectAction(currentProject).actionPerformed(e);
                        }
                    }
                }
            };
            btnClose = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/close.png", true), closeAction); //NOI18N
            btnClose.setToolTipText(NbBundle.getMessage(OneProjectDashboard.class, "LBL_Close"));
            btnClose.setRolloverEnabled(true);
            btnClose.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/close_over.png", true)); // NOI18N
            btnClose.setVisible(false);
            btnClose.setToolTipText(NbBundle.getMessage(OneProjectDashboard.class, "LBL_Close"));
            toolbar.add(btnClose);
            
            separator = new JToolBar.Separator();
            toolbar.add(separator);
            
            btnNewServer = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/new_team_project.png", true), new AddInstanceAction()); 
            btnNewServer.setToolTipText(Bundle.LBL_NewServer());
            btnNewServer.setRolloverEnabled(true);
            btnNewServer.setVisible(false);
            btnNewServer.setToolTipText(NbBundle.getMessage(OneProjectDashboard.class, "LBL_NewProject"));
            toolbar.add(btnNewServer);
            
            mListener = new MListener();
            addMouseListener(mListener);
            addMouseMotionListener(mListener);
            lbl.addMouseListener(mListener);
            lbl.addMouseMotionListener(mListener);
            btnPick.addMouseListener(mListener);
            btnPick.addMouseMotionListener(mListener);
            placeholder.addMouseListener(mListener);
            placeholder.addMouseMotionListener(mListener);
            btnNewServer.addMouseListener(mListener);
            btnNewServer.addMouseMotionListener(mListener);
            btnClose.addMouseListener(mListener);
            btnClose.addMouseMotionListener(mListener);
            btnBookmark.addMouseListener(mListener);
            btnBookmark.addMouseMotionListener(mListener);
            separator.addMouseListener(mListener);
            separator.addMouseMotionListener(mListener);
            lblBookmarkingProgress.addMouseListener(mListener);
            lblBookmarkingProgress.addMouseMotionListener(mListener);
            
            setNoProject();
        }

        private void setButtons() {
            btnNewServer.setVisible(mListener.mouseOver);
                
            if(currentProject != null) {
                btnClose.setVisible(mListener.mouseOver);
                btnBookmark.setVisible(mListener.mouseOver && !bookmarking);
                lblBookmarkingProgress.setVisible(mListener.mouseOver && bookmarking);
                separator.setVisible(mListener.mouseOver);
            
                boolean isMemberProject = isMemberProject(currentProject);

                btnBookmark.setToolTipText(NbBundle.getMessage(OneProjectDashboard.class, isMemberProject?"LBL_LeaveProject":"LBL_Bookmark"));
                btnBookmark.setIcon(
                    ImageUtilities.loadImageIcon(
                        "org/netbeans/modules/team/ui/resources/"  + (isMemberProject ? "bookmark.png" : "unbookmark.png"), true));
                btnBookmark.setRolloverIcon(
                    ImageUtilities.loadImageIcon(
                        "org/netbeans/modules/team/ui/resources/" + (isMemberProject ? "bookmark_over.png" : "unbookmark_over.png"), true)); // NOI18N                                
            } else {
                btnClose.setVisible(false);
                btnBookmark.setVisible(false);
                lblBookmarkingProgress.setVisible(false);
                separator.setVisible(false);                
            }
        }
        
        public ProjectHandle getCurrentProject() {
            return currentProject;
        }

        void setCurrentProject(ProjectHandle project, ListNode node) {
            if (project != null) {
                this.currentProject = project;
                this.currentProjectNode = node;
                setProjectLabel(project.getDisplayName());
            } else {
                setNoProject();
            }
            setButtons();
        }

        public ListNode getCurrentProjectNode() {
            return currentProjectNode;
        }
        
        private void setProjectLabel(String name) {
            lbl.setText(name);
            lbl.setIcon(server.getIcon());
        }
        
        void switchProject() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MegaMenu mm = MegaMenu.create();
                    mm.setInitialSelection(server, currentProjectNode);
                    mm.show(ProjectPicker.this);
                }
            });
        }

        void hideMenu() {
            final MegaMenu mm = MegaMenu.getCurrent();
            if(mm != null) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        mm.hide();
                    }
                };
                if(SwingUtilities.isEventDispatchThread()) {
                    r.run();
                } else {
                    SwingUtilities.invokeLater(r);
                }
            }
        }

        private void setNoProject() {
            this.currentProject = null;
            this.currentProjectNode = null;
            setProjectLabel(NbBundle.getMessage(DashboardSupport.class, "CLICK_TO_SELECT"));
        }

        private void bookmarkingStarted(ProjectHandle<P> project) {
            if(currentProject != null && currentProject.getId().equals(project.getId())) {
                bookmarking = true;
                setButtons();
            }
        }

        private void bookmarkingFinished(ProjectHandle<P> project) {
            if(currentProject != null && currentProject.getId().equals(project.getId())) {
                bookmarking = false;
                setButtons();
            }
        }
        
        private class MListener extends MouseAdapter {
            boolean mouseOver = false;
            @Override
            public void mouseEntered( MouseEvent e ) {
                mouseMoved( e );
            }
            @Override
            public void mouseExited( MouseEvent e ) {
                mouseOver = false;
                setButtons();
            }
            @Override
            public void mouseMoved( MouseEvent e ) {
                mouseOver = true;
                setButtons();
            }
        };
    }
    
    private class CloseProjectAction extends AbstractAction {
        private final ProjectHandle prj;
        public CloseProjectAction(ProjectHandle project) {
            super(org.openide.util.NbBundle.getMessage(OneProjectDashboard.class, "CTL_RemoveProject"));
            this.prj = project;
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            removeProject(prj);
            projectPicker.setButtons();
        }
    }        
    
    private class ProjectToolbar extends JToolBar {
        @Override
        public String getUIClassID() {
            if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
                return "Nb.Toolbar.ui"; //NOI18N
            } else {
                return super.getUIClassID();
            }
        }
        @Override
        public void setUI( ToolBarUI ui ) {
            super.setUI( ui );
            configure();
        }

        private void configure() {
            setOpaque( false );
            setFloatable( false );
            setBorderPainted( false );
            setBorder( BorderFactory.createEmptyBorder() );
        }
    }
}
