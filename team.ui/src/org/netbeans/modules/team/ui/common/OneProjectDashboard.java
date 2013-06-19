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
import java.lang.ref.WeakReference;
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
    private final RequestProcessor requestProcessor = new RequestProcessor("Team Dashboard", 1, true); // NOI18N
    private final TreeList treeList = new TreeList(model);
               
    private final JScrollPane dashboardComponent;
    private final JPanel dashboardPanel;
    private final PropertyChangeListener userListener;
    private boolean opened = false;

    private static final long TIMEOUT_INTERVAL_MILLIS = TreeListNode.TIMEOUT_INTERVAL_MILLIS;

    private boolean memberProjectsLoaded = false;
    private boolean otherProjectsLoaded = false;    
    private OtherProjectsLoader otherProjectsLoader;
    private MemberProjectsLoader memberProjectsLoader;

    private final ErrorNode memberProjectsError;
    private final ErrorNode otherProjectsError;

    private final ProjectPicker projectPicker;

    private final Object LOCK = new Object();

    private final TeamServer server;
    private PropertyChangeListener serverListener;
    private final DashboardProvider<P> dashboardProvider;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final Map<String, MyProjectNode> projectNodes = Collections.synchronizedMap(new HashMap<String, MyProjectNode>());
    private final Map<String, List<TreeListNode>> projectChildren = new HashMap<>(3);
    private final ArrayList<ProjectHandle<P>> memberProjects = new ArrayList<>(50);
    private final ArrayList<ProjectHandle<P>> otherProjects = new ArrayList<>(50);
    private WeakReference<SelectionList> selectionListRef;
    
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
                startAllProjectsLoading(true, false);
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
        synchronized( LOCK ) {
            s.addAll(otherProjects);
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

    private boolean isMemberProject(ProjectHandle m) {
        return memberProjects.contains(m);
    }

    private void initServer() {

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
    }
    
    @Override
    public void selectAndExpand(ProjectHandle project) {
        switchProject(project);
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
                
                // remove private project from dashboard
                // private projects are visible only for
                // authenticated user who is member of this project
                Iterator<ProjectHandle<P>> ph = otherProjects.iterator();
                while (ph.hasNext()) {
                    final ProjectHandle<P> next = ph.next();
                    if (next.isPrivate()) {
                        removeProjectNodes(Collections.singletonList(next));
                        ph.remove();
                    }
                    storeAllProjects();
                }
                memberProjects.clear();
                setNoProject();
            } 
            memberProjectsLoaded = false;
            
            // the reload is handled from the mega menu in case it happens 
//            if( null != login ) {
//                startAllProjectsLoading(false, false);
//            }
            if( null != this.login ) {
                this.login.addPropertyChangeListener(userListener);
            }
        }
    }

    /**
     * Add a Team project to the Dashboard.
     * @param projects
     * @param isMemberProject
     */
    @Override
    public void addProjects(final ProjectHandle<P>[] projects, final boolean isMemberProject, final boolean select) {        
        TeamUIUtils.setSelectedServer(server);
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                synchronized (LOCK) {
                    for (ProjectHandle<P> project : projects) {
                        addProject(project, isMemberProject, select);
                    }
                }
                changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
            }
        });
    }

    private void addProject(final ProjectHandle<P> project, final boolean isMemberProject, final boolean select) {
                    Runnable selectAndExpand = new Runnable() {
                        @Override
                        public void run() {
                            selectAndExpand(project);
                        }
                    };
                    if (otherProjects.contains(project)) {
                        if (select) {
                            SwingUtilities.invokeLater(selectAndExpand);
                        }
                        return;
                    }
                    if (isMemberProject && memberProjectsLoaded && !memberProjects.contains(project)) {
                        memberProjects.add(project);
                        setMemberProjects(new ArrayList<>(memberProjects));
                    }
                    otherProjects.add(project);
                    storeAllProjects();
                    setOtherProjects(new ArrayList<>(otherProjects));
                    switchMemberProjects();
                    switchProject(project, createProjectNode(project));
                    if (isOpened()) {
                        switchContent();
                        if (select) {
                            SwingUtilities.invokeLater(selectAndExpand);
                        }
                    }
                }

    @Override
    public void removeProject( ProjectHandle<P> project ) {
        synchronized( LOCK ) {
            if( !otherProjects.contains(project) ) {
                return;
            }
            otherProjects.remove(project);
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

    private boolean isOpened() {
        return opened;
    }

    private void startAllProjectsLoading(final boolean forceRefresh, boolean sync) {
        if(forceRefresh) {
            changeSupport.firePropertyChange(DashboardSupport.PROP_REFRESH_REQUEST, null, null);
        }
        
        TeamUIUtils.waitStartupFinished();
        RequestProcessor.Task memberTask = null;
        RequestProcessor.Task othersTask = null;
        synchronized (LOCK) {
            if (!memberProjectsLoaded || forceRefresh) {
                memberTask = startLoadingMemberProjects(forceRefresh);
            }
            if (!otherProjectsLoaded || forceRefresh) {
                othersTask = startLoadingOtherProjects(forceRefresh);
            }
        }
        if(memberTask != null && sync) {
            memberTask.waitFinished();
        }
        if(othersTask != null && sync) {
            othersTask.waitFinished();
        }
        switchContent();
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
                if (null != login) {
                    startAllProjectsLoading(false, false);
                }
                opened = true;
            }
        }
        return dashboardPanel;
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

        res.add( new JLabel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        res.add( lbl, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
        res.add( btnWhatIs, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
        res.add( new JLabel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        return res;
    }

    private void storeAllProjects() {
        String serverName = server.getUrl().getHost();
        Preferences prefs = NbPreferences.forModule(DashboardSupport.class).node(DashboardSupport.PREF_ALL_PROJECTS + ("kenai.com".equals(serverName)?"":"-"+serverName)); //NOI18N
        int index = 0;
        for( ProjectHandle project : otherProjects ) {
            //do not store private projects
//            if (!project.isPrivate()) {
                prefs.put(DashboardSupport.PREF_ID+index++, project.getId()); //NOI18N
//            }
        }
        //store size
        prefs.putInt(DashboardSupport.PREF_COUNT, index); //NOI18N
    }

    private void setOtherProjects(ArrayList<ProjectHandle<P>> projects) {
        synchronized( LOCK ) {
            
            ArrayList<ProjectHandle<P>> toRemove = new ArrayList<>(otherProjects);
            
            otherProjects.clear();
            for( ProjectHandle p : projects ) {
                if( !otherProjects.contains( p ) ) {
                    otherProjects.add( p );
                    toRemove.remove(p);
                }
            }
            Collections.sort(otherProjects);
            
            toRemove.removeAll(otherProjects);
            toRemove.removeAll(memberProjects);
            removeProjectNodes( toRemove );
            
            otherProjectsLoaded = true;
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
    public void deletingStarted() { }

    @Override
    public void deletingFinished() { }

    public void loggingStarted() { }

    public void loggingFinished() { }

    @Override
    public void xmppStarted() { }

    @Override
    public void xmppFinsihed() { }

    private void projectLoadingStarted() { }

    private void projectLoadingFinished() { }

    private void myProjectLoadingStarted() { }

    private void myProjectLoadingFinished() { }

    @Override
    public void myProjectsProgressStarted() { }

    @Override
    public void myProjectsProgressFinished() { }

    private RequestProcessor.Task startLoadingMemberProjects(boolean forceRefresh) {
        synchronized( LOCK ) {
            if( memberProjectsLoader != null ) {
                memberProjectsLoader.cancel();
            }
            if( null == login ) {
                return null;
            }
            memberProjectsLoader = new MemberProjectsLoader(login, forceRefresh);
        }
        RequestProcessor.Task t = memberProjectsLoader.post();
        return t;        
    }

    private RequestProcessor.Task startLoadingOtherProjects(boolean forceRefresh) {
        String teamName = server.getUrl().getHost();
        Preferences prefs = NbPreferences.forModule(DashboardSupport.class).node(DashboardSupport.PREF_ALL_PROJECTS + ("kenai.com".equals(teamName)?"":"-"+teamName)); //NOI18N
        int count = prefs.getInt(DashboardSupport.PREF_COUNT, 0); //NOI18N
        if( 0 == count ) {
            projectLoadingFinished();
            return null; //nothing to load
        }
        ArrayList<String> ids = new ArrayList<>(count);
        for( int i=0; i<count; i++ ) {
            String id = prefs.get(DashboardSupport.PREF_ID+i, null); //NOI18N
            if( null != id && id.trim().length() > 0 ) {
                ids.add( id.trim() );
            }
        }
        synchronized( LOCK ) {
            if(otherProjectsLoader != null) {
                otherProjectsLoader.cancel();
            }
            if( ids.isEmpty() ) {
                projectLoadingFinished();
                return null;
            }
            otherProjectsLoader = new OtherProjectsLoader(ids, forceRefresh);
            return otherProjectsLoader.post();
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
            for( ProjectHandle p : projects ) {
                if( !memberProjects.contains(p) ) {
                    memberProjects.add(p);
                    toRemove.remove(p);
                }
            }
            Collections.sort(memberProjects);
            memberProjectsLoaded = true;
            
            toRemove.removeAll(memberProjects);
            toRemove.removeAll(otherProjects);
            removeProjectNodes( toRemove );
            
//            storeAllProjects();

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
            
    private void switchProject(ProjectHandle project) {
        switchContent();
        for( TreeListNode node : model.getRootNodes() ) {
            model.removeRoot(node);
        }
        if(project != null) {
            synchronized(projectChildren) {
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
        if(forceRefresh || !otherProjectsLoaded || !memberProjectsLoaded) {
            startAllProjectsLoading(forceRefresh, true);
        } 
        
        final SelectionList res = new SelectionList();
        synchronized( LOCK ) {
            for (ProjectHandle<P> p : memberProjects) {
                if(!projectNodes.containsKey(p.getId())) {
                    projectNodes.put(p.getId(), createProjectNode(p));
                }
            }
            for (ProjectHandle p : otherProjects) {
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
        this.selectionListRef = new WeakReference<>(res);
        return res;
    };

    private MyProjectNode createProjectNode(ProjectHandle<P> p) {
        MyProjectNode n = dashboardProvider.createMyProjectNode(p, false, true, new CloseProjectAction(p));
        n.setIsMember(isMemberProject(p));
        return n;
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
        private RequestProcessor.Task task;

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
            if(task != null) {
                task.cancel();
                task = null;
            }            
            return true;
        }
        
        private RequestProcessor.Task post() {
            task = requestProcessor.post(this);
            return task;
        }        

    }

    private class MemberProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final LoginHandle user;
        private final boolean forceRefresh;
        private RequestProcessor.Task task;

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
            if(task != null) {
                task.cancel();
                task = null;
            }
            return true;
        }

        private RequestProcessor.Task post() {
            task = requestProcessor.post(this);
            return task;
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
            if(selectionListRef != null) {
                SelectionList sl = selectionListRef.get();
                if(sl != null) {
                    MyProjectNode n = projectNodes.get(prj.getId());
                    if(n != null) {
                        ((DefaultListModel) sl.getModel()).removeElement(n);
                    }
                }
            }
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
