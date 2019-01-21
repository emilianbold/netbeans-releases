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

package org.netbeans.modules.team.server.ui.common;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.accessibility.AccessibleContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.net.PasswordAuthentication;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.server.TeamView;
import org.netbeans.modules.team.server.ui.common.DashboardSupport.DashboardImpl;
import org.netbeans.modules.team.server.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.ide.spi.TeamDashboardComponentProvider;
import org.netbeans.modules.team.server.ui.picker.MegaMenu;
import org.netbeans.modules.team.server.ui.spi.DashboardProvider;
import org.netbeans.modules.team.server.ui.spi.LoginHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.netbeans.modules.team.server.ui.spi.TeamServer;
import org.netbeans.modules.team.server.api.TeamUIUtils;
import org.netbeans.modules.team.commons.treelist.ListNode;
import org.netbeans.modules.team.commons.treelist.SelectionList;
import org.netbeans.modules.team.commons.treelist.TreeList;
import org.netbeans.modules.team.commons.treelist.TreeListListener;
import org.netbeans.modules.team.commons.treelist.TreeListModel;
import org.netbeans.modules.team.commons.treelist.TreeListNode;
import org.netbeans.modules.team.server.Utilities;
import org.netbeans.modules.team.server.ui.spi.RemoteMachineAccessor;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * Abstract class providing some common Team Dashboard functionality.
 *
 */
public final class OneProjectDashboard<P> implements DashboardImpl<P> {

    private LoginHandle login;
    
    private final DashboardComponent dashboardComponent;
    private final RequestProcessor requestProcessor = new RequestProcessor("Team Dashboard", 1, true); // NOI18N
               
    private final JScrollPane scrollPane;
    private final JPanel dashboardPanel;
    private final PropertyChangeListener userListener;
    private boolean opened = false;

    private static final long TIMEOUT_INTERVAL_MILLIS = TreeListNode.TIMEOUT_INTERVAL_MILLIS;

    private boolean memberProjectsLoaded = false;
    private boolean otherProjectsLoaded = false;    
    private OtherProjectsLoader otherProjectsLoader;
    private MemberProjectsLoader memberProjectsLoader;
    private SelectedProjectsLoader selectedProjectsLoader;    

    private final Object LOCK = new Object();

    private final TeamServer server;
    private PropertyChangeListener serverListener;
    private final DashboardProvider<P> dashboardProvider;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final Map<ProjectHandle<P>, MyProjectNode> projectNodes;
    private final Map<String, List<TreeListNode>> projectChildren = new HashMap<>(3);
    private final ArrayList<ProjectHandle<P>> memberProjects = new ArrayList<>(50);
    private final ArrayList<ProjectHandle<P>> otherProjects = new ArrayList<>(50);
    
    private final ProjectHistoryList projectHistory = ProjectHistoryList.create();
    private WeakReference<SelectionList> selectionListRef;

    private ErrorNode errorNode;
    
    private static final Map<TeamServer, OneProjectDashboard> dashboardMap = new WeakHashMap<>(3);
    
    private static final String PREF_SYNC_MODE = "team.dashboard.sync.mode"; // NOI18N
    private static final String PREF_PROJECT_HISTORY = "team.dashboard.project.history"; // NOI18N
    
    public enum SyncMode {
        PREF_ALL,
        PREF_SELECTED,
        PREF_ALL_SELECTED
    }
    
    public static <P> OneProjectDashboard create(TeamServer server, DashboardProvider<P> dashboardProvider) {
        OneProjectDashboard<P> d = new OneProjectDashboard<>(server, dashboardProvider);
        synchronized(dashboardMap) {
            dashboardMap.put(server, d);
        }
        return d;
    }
    
    public static OneProjectDashboard forServer(TeamServer server) {
        synchronized(dashboardMap) {
            return dashboardMap.get(server);
        }
    }
    
    public static void setSyncMode(TeamServer server, SyncMode mode) {
        String url = server.getUrl().toString();
        String oldValue = getPrefs().get(PREF_SYNC_MODE, null);
        StringBuilder sb = new StringBuilder();
        SyncMode oldMode = SyncMode.PREF_ALL_SELECTED;
        if( oldValue == null ) {
            sb.append(url).append("<=>").append(mode.name());
        } else {
            String[] modeString = oldValue.split(";");
            boolean done = false;
            for (int i = 0; i < modeString.length; i++) {
                String srv = modeString[i];
                String[] urlAndMode = srv.split("<=>");
                if(urlAndMode[0].equals(url)) {
                    sb.append(url).append("<=>").append(mode.name());
                    oldMode = SyncMode.valueOf(urlAndMode[1]);
                    done = true;
                } else {
                    sb.append(srv);
                }
                if(i < modeString.length -1) {
                    sb.append(";");
                }
            }
            if(!done) {
                sb.append(";").append(url).append("<=>").append(mode.name());
            } 
        }
        getPrefs().put(PREF_SYNC_MODE, sb.toString());
        if(oldMode != mode) {
            OneProjectDashboard d = dashboardMap.get(server);
            if( d != null ) {
                d.fireOpenedProjects();
            }
        }
    }
    
    public static SyncMode getSyncMode(TeamServer server) {
        String url = server.getUrl().toString();
        String value = getPrefs().get(PREF_SYNC_MODE, null);
        SyncMode mode = SyncMode.PREF_ALL_SELECTED;
        if(value != null) {
            String[] modeStrings = value.split(";");
            for (int i = 0; i < modeStrings.length; i++) {
                String srv = modeStrings[i];
                String[] urlAndMode = srv.split("<=>");
                if(urlAndMode[0].equals(url)) {
                    mode = SyncMode.valueOf(urlAndMode[1]);
                    break;
                } 
            }
        }
        return mode;
    }
    
    private OneProjectDashboard(TeamServer server, DashboardProvider<P> dashboardProvider) {
        this.dashboardProvider = dashboardProvider;
        this.server = server;
        
        projectNodes = Collections.synchronizedMap(new TreeMap<ProjectHandle<P>, MyProjectNode>(new Comparator<ProjectHandle<P>>(){
            @Override
            public int compare(ProjectHandle<P> p1, ProjectHandle<P> p2) {
                return p1.compareTo(p2);
            }
        }));
                 
        dashboardPanel = new JPanel(new BorderLayout());
        
        scrollPane = new JScrollPane() {
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
        
        this.dashboardComponent = createComponent();

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(ColorManager.getDefault().getDefaultBackground());
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder());
        dashboardPanel.setBackground(ColorManager.getDefault().getDefaultBackground());
        
        scrollPane.getViewport().setBackground(ColorManager.getDefault().getDefaultBackground());
        scrollPane.setViewportView(dashboardComponent.getComponent());
        
        dashboardPanel.add(scrollPane, BorderLayout.CENTER);
                    
        userListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( LoginHandle.PROP_MEMBER_PROJECT_LIST.equals(evt.getPropertyName()) ) {
                    refreshMemberProjects(true);
                }
            }
        };

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
        SyncMode mode = getSyncMode(server);
        synchronized( LOCK ) {
            switch( mode ) {
                case PREF_ALL:
                    s.addAll(otherProjects);
                    s.addAll(memberProjects);
                    break;
                case PREF_ALL_SELECTED:
                    for (String id : projectHistory) {
                        ProjectHandle<P> ph = projectById(id);
                        if(ph != null) {
                            s.add(ph);
                        }
                    }
                    break;
                case PREF_SELECTED:
                    String id = getSelectedProjectId();
                    if( id != null ) {
                        ProjectHandle<P> ph = projectById(id);
                        if(ph != null) {
                            s.add(ph);
                        }                 
                    }
                    break;
            }
        }
        
        return s.toArray(new ProjectHandle[s.size()]);
    }

    private ProjectHandle<P> projectById(String id) {
        ProjectHandle<P> ph = getProject(id, otherProjects);
        if (ph != null) {
            return ph;
        }
        return getProject(id, memberProjects);
    }

    private ProjectHandle<P> getProject(String id, ArrayList<ProjectHandle<P>> projects) {
        synchronized( LOCK ) {
            for (ProjectHandle ph : projects) {
                if(id.equals(ph.getId())) {
                    return ph;
                }
            }
        }
        return null;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    boolean isMemberProject(ProjectHandle m) {
        synchronized( LOCK ) {
            return memberProjects.contains(m);
        }
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
    }
    
    @Override
    public void selectAndExpand(ProjectHandle project) {
        switchProject(project, false);
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
                otherProjects.clear();
                memberProjects.clear();
                projectNodes.clear();
                OneProjectDashboardPicker picker = getProjectPicker();
                if(picker.isSelectedServer(server)) {
                    picker.setCurrentProject(null, null, null, false);
                    switchProject(null, true);
                }
            } 
            memberProjectsLoaded = false;
            
            if( null != this.login ) {
                this.login.addPropertyChangeListener(userListener);
                loadSelectedProject(false);                
            }
        }
    }

    /**
     * Add a Team project to the Dashboard.
     * @param projects
     * @param isMemberProject
     * @param select
     */
    @Override
    public void addProjects(final ProjectHandle<P>[] projects, final boolean isMemberProject, final boolean select) {        
        requestProcessor.post(new Runnable() {
            @Override
            public void run() {
                synchronized (LOCK) {
                    for (ProjectHandle<P> project : projects) {
                        if (isMemberProject && !memberProjects.contains(project)) {
                            memberProjects.add(project);
                            setMemberProjects(new ArrayList<>(memberProjects));
                            getProjectNode(project);
                        } else if(!isMemberProject && !otherProjects.contains(project)) {
                            otherProjects.add(project);
                            setOtherProjects(new ArrayList<>(otherProjects));
                            getProjectNode(project);
                        }
                    }
                    if(projects.length == 1) {
                        switchProject(projects[0], getProjectNode(projects[0]), true);
                    } else {
                        if(selectionListRef != null) {
                            final SelectionList sl = selectionListRef.get();
                            if(sl != null) {
                                final ListNode selection = sl.getSelectedValue();
                                DefaultListModel m = (DefaultListModel) sl.getModel();
                                m.clear();
                                for (ListNode n : projectNodes.values()) {
                                    m.addElement(n);
                                }
                                UIUtils.runInAWT(new Runnable() {
                                    @Override
                                    public void run() {
                                        sl.setSelectedValue(selection, true);
                                    }
                                });
                            }
                        }
                    }
                }
                fireOpenedProjects();
            }
        });
    }
    
    @Override
    public void removeProject( ProjectHandle<P> project ) {
        boolean removed;
        synchronized( LOCK ) {
            removed = otherProjects.remove(project);
            removeProjectNodes( Collections.singleton(project) );
            
            if(getProjectPicker().removed(server, project)) { // could it be even otherwise?
                switchProject(null, false);
            }
        }
        if(removed) {
            project.firePropertyChange(ProjectHandle.PROP_CLOSE, null, null);
            fireOpenedProjects();
        }
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
            dashboardComponent.close();
            opened = false;
        }
    }

    @Override
    public JComponent getComponent() {
        // XXX seems that there is no real functional need for this ... 
        // commented in to fix issue #245404 - feel free to remove entirely if no regression appears
//        synchronized( LOCK ) {
//            if (!opened) {
//                if (null != login) {
//                    startAllProjectsLoading(false, false);
//                }
//                opened = true;
//            }
//        }
        return dashboardPanel;
    }

    private void switchContent() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                dashboardComponent.beforeShow();
                switchMemberProjects();
                scrollPane.invalidate();
                scrollPane.revalidate();
                scrollPane.repaint();
                // hack: ensure the dashboard component has focus (when
                // added to already visible and activated TopComponent)
                if (!MegaMenu.isShowing()) {
                    TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, scrollPane);
                    if (tc != null && TopComponent.getRegistry().getActivated() == tc) {
                        getProjectPicker().restoreFocus();
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

            switchMemberProjects();
            
            if( isOpened() ) {
                switchContent();
            }
        }
        fireOpenedProjects();
    }

    private void fireOpenedProjects() {
        changeSupport.firePropertyChange(DashboardSupport.PROP_OPENED_PROJECTS, null, null);
    }

    private void switchMemberProjects() {
        synchronized ( LOCK ) {
            for( ListNode n : projectNodes.values() ) {
                if( !(n instanceof MyProjectNode) ) {
                    continue;
                }
                MyProjectNode pn = (MyProjectNode) n;
                pn.setIsMember( memberProjects.contains( pn.getProject() ) );
            }
        }
    }

    @Override
    public void bookmarkingStarted(final ProjectHandle<P> project) {
        doNotRunInAWT(new Runnable() {
            @Override
            public void run() {
                final MyProjectNode node = projectNodes.get(project);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(node != null) {
                            node.bookmarkingStarted();
                        }
                        getProjectPicker().bookmarkingStarted(server, project);        
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
                final MyProjectNode node = projectNodes.get(project);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if(node != null) {
                            node.bookmarkingFinished();
                        }
                        getProjectPicker().bookmarkingFinished(server, project);        
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

    private void loadSelectedProject(final boolean forceRefresh) {
        synchronized( LOCK ) {
            String selectedUrl = getSelectedServerUrl();
            if(selectedUrl != null && selectedUrl.equals(server.getUrl().toString())) {
                String id = getSelectedProjectId();
                if(id != null) {
                    getProjectPicker().startLoadingSelection();
                    if(selectedProjectsLoader != null) {
                        selectedProjectsLoader.cancel();
                    }
                    selectedProjectsLoader = new SelectedProjectsLoader(id, forceRefresh);
                    selectedProjectsLoader.post();
                }
            }
        }
    }

    private RequestProcessor.Task startLoadingOtherProjects(boolean forceRefresh) {
        synchronized( LOCK ) {
            if(otherProjectsLoader != null) {
                otherProjectsLoader.cancel();
            }
            
            if(forceRefresh && !otherProjects.isEmpty()) {
                ArrayList<String> ids = new ArrayList<>(otherProjects.size());
                for(ProjectHandle<P> ph : otherProjects) {
                    ids.add(ph.getId());
                }
                    
                otherProjectsLoader = new OtherProjectsLoader(ids, forceRefresh);
                return otherProjectsLoader.post();
            } else {
                setOtherProjects(otherProjects);
                return null;
            } 
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
                if( otherProjects.contains(p) ) {
                    otherProjects.remove(p);
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
        fireOpenedProjects();
    }

    void switchProject(ProjectHandle ph, ListNode node, boolean hideMenu) {
        switchProject(ph, node, hideMenu, false);
    }
    
    void switchProject(ProjectHandle ph, ListNode node, boolean hideMenu, boolean forceRefresh) {
        getProjectPicker().setCurrentProject(server, ph, node, hideMenu);
        switchProject(ph, false, forceRefresh);
    }
            
    void switchProject(ProjectHandle project, boolean preserveSelection) {
        switchProject(project, preserveSelection, false);
    }
    
    void switchProject(ProjectHandle project, boolean preserveSelection, boolean forceRefresh) {
        switchContent();
        dashboardComponent.clear();
        if(project != null) {
            synchronized(projectChildren) {
                List<TreeListNode> children = projectChildren.get(project.getId());
                if(children == null) {
                    children = createProjectChildren(project);
                    projectChildren.put(project.getId(), children);
                } else if(forceRefresh) {
                    List<TreeListNode> oldChildren = projectChildren.remove(project.getId());
                    List<TreeListNode> newChildren = createProjectChildren(project);
                    projectChildren.put(project.getId(), newChildren);
                    if(oldChildren != null && oldChildren.size() == newChildren.size()) { // paranoid ?
                        for (int i = 0; i < oldChildren.size(); i++) {
                            if(oldChildren.get(i).isExpanded()) {
                                newChildren.get(i).setExpanded(true);
                            }
                        }
                    }
                }
                dashboardComponent.addChildren(getDashboardProvider().createProjectLinksComponent(project), children);
            }
        }
        if(!preserveSelection) {
            if(project != null) {
                storeSelectedProject(server, project);
            } else {
                storeSelectedProject(null, null);
            }
        }
        fireOpenedProjects();
    }

    private void storeSelectedProject(TeamServer server, ProjectHandle<P> project) {
        Preferences prefs = getPrefs();
        if(server == null) {
            prefs.remove(DashboardSupport.PREF_SELECTED_SERVER);
            prefs.remove(DashboardSupport.PREF_SELECTED_PROJECT);
        } else {
            prefs.put(DashboardSupport.PREF_SELECTED_SERVER, server.getUrl().toString());
            prefs.put(DashboardSupport.PREF_SELECTED_PROJECT, project.getId());            
        }
        if(project != null) {
            projectHistory.add(project.getId());
        }
    }

    private String getSelectedServerUrl() {
        String url = getPrefs().get(DashboardSupport.PREF_SELECTED_SERVER, "");
        return !"".equals(url) ? url : null;
    }
    
    private String getSelectedProjectId() {
        String id = getPrefs().get(DashboardSupport.PREF_SELECTED_PROJECT, "");
        return "".equals(id) ? null : id;
    }
    
    private static Preferences getPrefs() {
        return NbPreferences.forModule(DefaultDashboard.class).node(DashboardSupport.PREF_ALL_PROJECTS); //NOI18N
    }
    
    protected List<TreeListNode> createProjectChildren(ProjectHandle project) {
        ArrayList<TreeListNode> children = new ArrayList<>();
        DashboardProvider<P> provider = getDashboardProvider();
        if( null != provider.getMemberAccessor() ) {
            children.add( new MemberListNode(null, project, provider) );
        }
        BuilderAccessor builds = provider.getBuilderAccessor();
        if (builds != null) {
            children.add(new BuildListNode(null, project, builds));
        }
        if( null != provider.getQueryAccessor() ) {
            children.add(new QueryListNode(null, project, provider));
        }
        if( null != provider.getSourceAccessor() ) {
            children.add( provider.createSourceListNode(null, project) );
        }
        RemoteMachineAccessor<P> remoteMachines = provider.getRemoteMachineAccessor();
        if( remoteMachines != null ) {
            children.add( new RemoteMachineListNode(null, project, remoteMachines));
        }
        return children;
    }

    private void removeProjectNodes( Collection<ProjectHandle<P>> projects ) {
        for( ProjectHandle p : projects ) {
            projectNodes.remove( p );
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
            
        synchronized( LOCK ) {
            if(forceRefresh) {
                projectChildren.clear();
                OneProjectDashboardPicker picker = getProjectPicker();
                if(picker.isSelectedServer(server)) {
                    picker.setCurrentProject(null, null, null, false);
                    switchProject(null, true);
                }
            }
        }
            
        if(forceRefresh || !otherProjectsLoaded || !memberProjectsLoaded) {
            startAllProjectsLoading(forceRefresh, true);
        } 
        
        final SelectionList res = new SelectionList();
        synchronized( LOCK ) {
            for (ProjectHandle<P> p : memberProjects) {
                getProjectNode(p); // adds node to projectNodes
            }
            for (ProjectHandle p : otherProjects) {
                getProjectNode(p); // adds node to projectNodes
            }
            ArrayList<ListNode> l = new ArrayList<ListNode>(projectNodes.values());
            if(errorNode != null) {
                l.add(errorNode);
            }             
            res.setItems(l);
            
        }

        this.selectionListRef = new WeakReference<>(res);
        return res;
    };

    private MyProjectNode getProjectNode(ProjectHandle<P> p) {
        MyProjectNode n = projectNodes.get(p);
        if(n == null) {
            n = dashboardProvider.createMyProjectNode(p, false, true, new CloseProjectAction(p));
            n.setIsMember(isMemberProject(p));
            projectNodes.put(p, n);
        }
        return n;
    }

    Action getCloseProjectAction(ProjectHandle currentProject) {
        return new CloseProjectAction(currentProject);
    }

    void refresh(ProjectHandle currentProject) {
        dashboardComponent.clear();
        loadSelectedProject(true);
    }
    
    private void createErrorNode() throws MissingResourceException {
        errorNode = new ErrorNode(NbBundle.getMessage(DashboardSupport.class, "ERR_LoadingProjects"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MegaMenu.showAgain();
            }
        });
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
                createErrorNode();
                otherProjectsLoaded = false;
                return;
            }

            setOtherProjects( res[0] );
            errorNode = null;
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
    
    private class SelectedProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final String projectId;
        private final boolean forceRefresh;
        private RequestProcessor.Task task;

        public SelectedProjectsLoader( String projectId, boolean forceRefresh ) {
            this.projectId = projectId;
            this.forceRefresh = forceRefresh;
        }

        @Override
        public void run() {
            final ProjectHandle[] res = new ProjectHandle[1];
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    ProjectAccessor<P> accessor = dashboardProvider.getProjectAccessor();
                    ProjectHandle handle = accessor.getNonMemberProject(server, projectId, forceRefresh);
                    res[0] = handle;
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TIMEOUT_INTERVAL_MILLIS );
            } catch( InterruptedException iE ) {
                //ignore
            }
            if( cancelled ) {
                switchProject(null, true);
                return;
            }
            if(res[0] != null) {
                synchronized( LOCK ) {
                    if(memberProjects.remove(res[0])) {
                        memberProjects.add(res[0]);
                    } else {
                        otherProjects.remove(res[0]);
                        otherProjects.add(res[0]);
                    }
                }
                switchProject(res[0], getProjectNode(res[0]), false, forceRefresh);
            } 
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
                createErrorNode();
                memberProjectsLoaded = false;
                return;
            } 

            setMemberProjects( res[0] );
            errorNode = null;
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

    private class CloseProjectAction extends AbstractAction {
        private final ProjectHandle prj;
        public CloseProjectAction(ProjectHandle project) {
            super(org.openide.util.NbBundle.getMessage(OneProjectDashboard.class, "CTL_RemoveProject")); // NOI18N
            this.prj = project;
        }
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if(selectionListRef != null) {
                SelectionList sl = selectionListRef.get();
                if(sl != null) {
                    MyProjectNode n = projectNodes.get(prj);
                    if(n != null) {
                        ((DefaultListModel) sl.getModel()).removeElement(n);
                    }
                }
            }
            removeProject(prj);
        }
    }        
    
    private OneProjectDashboardPicker getProjectPicker() {
        return TeamView.getInstance().getProjectPicker();
    }

    DashboardComponent createComponent() {
        Collection<? extends TeamDashboardComponentProvider> c = Lookup.getDefault().lookupAll(TeamDashboardComponentProvider.class);
        TeamDashboardComponentProvider providerImpl = c != null && c.size() > 0 ? c.iterator().next() : null;
        if(providerImpl != null) {
            return new ProvidedDashboardComponent(providerImpl);
        } else if(Boolean.getBoolean("team.dashboard.useDummyComponentProvider")) { // NOI18N
            return new ProvidedDashboardComponent(new DummyDashboardComponentProviderImpl());
        } else {    
            return new NbDashboardComponent();
        }
    }

    private abstract class DashboardComponent {
        
        protected void setAccessibleCtx(AccessibleContext accessibleContext) {
            String a11y = NbBundle.getMessage(DashboardSupport.class, "A11Y_TeamProjects"); // NOI18N
            accessibleContext.setAccessibleName(a11y);
            accessibleContext.setAccessibleDescription(a11y);
        }

        abstract JComponent getComponent();

        abstract void close();

        abstract void beforeShow();

        abstract void clear();

        abstract void addChildren(JComponent projectLinks, Collection<TreeListNode> children);
    }

    private class NbDashboardComponent extends DashboardComponent {
            
        private final TreeListModel model = new TreeListModel();
        private final ListModel EMPTY_MODEL = new AbstractListModel() {
            @Override
            public int getSize() {
                return 0;
            }
            @Override
            public Object getElementAt(int index) {
                return null;
            }
        };
        private final TreeList treeList = new TreeList(model);
        private final JPanel panel;

        public NbDashboardComponent() {
            panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);

            setAccessibleCtx(treeList.getAccessibleContext());
        }

        @Override
        JComponent getComponent() {
            return panel;
        }

        @Override
        void close() {
            treeList.setModel(EMPTY_MODEL);
            model.clear();
        }

        @Override
        void beforeShow() {
            treeList.setModel(model);
        }

        @Override
        void clear() {
            for( TreeListNode node : model.getRootNodes() ) {
                model.removeRoot(node);
            }
            panel.removeAll();
            panel.repaint();
        }

        @Override
        void addChildren(JComponent projectLinks, Collection<TreeListNode> children) {
            clear(); 
            projectLinks.setBorder(new EmptyBorder(3, 3, 5, 4));
            panel.add(projectLinks, BorderLayout.NORTH);
            panel.add(treeList, BorderLayout.CENTER);

            int idx = 1;
            for (TreeListNode n : children) {
                model.addRoot(idx++, n);
            }
        }
    }

    private class ProvidedDashboardComponent extends DashboardComponent {

        private final TeamDashboardComponentProvider provider;
        private final JComponent component;
        private Collection<TreeListNode> children;
        private JComponent projectLinks;

        private final Object LOCK = new Object();

        private ProvidedDashboardComponent(TeamDashboardComponentProvider provider) {
            this.provider = provider;

            component = new JPanel(new BorderLayout());
            component.setOpaque(false);
        }

        @Override
        JComponent getComponent() {
            return component;
        }

        @Override
        void close() {
            clear();
        }

        @Override
        void beforeShow() {
            populate();
        }

        @Override
        void clear() {
            component.removeAll();
        }

        @Override
        void addChildren(JComponent projectLinks, Collection<TreeListNode> children) {
            this.children = children;
            this.projectLinks = projectLinks;
            projectLinks.setBorder(new EmptyBorder(3, 3, 5, 4));
            populate();
        }

        private void populate() {
            synchronized ( LOCK ) {
                if(children == null) {
                    return;
                }
                List<TeamDashboardComponentProvider.Section> sections = new ArrayList<>(children.size());
                for (TreeListNode n : children) {
                    if(n instanceof QueryListNode  || 
                       n instanceof BuildListNode  || 
                       n instanceof SourceListNode || 
                       n instanceof MemberListNode) 
                    {
                        sections.add(new SectionImpl((SectionNode)n));
                    } 
                }

                JComponent cmp = provider.create(sections.toArray(new TeamDashboardComponentProvider.Section[sections.size()]));

                setAccessibleCtx(cmp.getAccessibleContext());

                component.removeAll();
                component.add(projectLinks, BorderLayout.NORTH);
                component.add(cmp, BorderLayout.CENTER);
            }
        }
    }
        
    private class SectionImpl implements TeamDashboardComponentProvider.Section {
        
        private final String title;
        private final TreeListNode node;
        
        private final TreeListModel model = new TreeListModel();
        private final TreeList treeList = new TreeList(model);
            
        private final Object LOCK = new Object();

        @Messages({"# {0} - project category, e.g. Builds or Tasks",
                   "A11Y_ProjectItems=List of items of project service: {0}"})
        public SectionImpl(SectionNode node) {
            this.title = node.getDisplayName();
            this.node = node;
            this.node.setIndentChildren(false);

            treeList.getAccessibleContext().setAccessibleName(title);
            treeList.getAccessibleContext().setAccessibleDescription(Bundle.A11Y_ProjectItems(title));

            // XXX Hacking the SectioNode/treelist so that instead of the actuall node, 
            // its children are shown as roots in the list (kind of not show root nodes mode)
            this.node.setListener(new TreeListListener() {
                @Override
                public void childrenRemoved(TreeListNode parent) {
                    synchronized ( LOCK ) {
                        clearModel();
                    }
                }

                @Override
                public void childrenAdded(TreeListNode parent) {
                    synchronized ( LOCK ) {
                        clearModel();
                        List<TreeListNode> children = parent.getChildren();
                        for (int i = 0; i < children.size(); i++) {
                            model.addRoot(i, children.get(i));
                        }
                    }    
                }
                @Override public void contentChanged(ListNode node) { }
                @Override public void contentSizeChanged(ListNode node) { }

                private void clearModel() {
                    for( TreeListNode node : model.getAllNodes() ) {
                        model.removeRoot(node);
                    }
                }
            });
            if(node.isExpanded()) {
                List<TreeListNode> children = node.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    model.addRoot(i, children.get(i));
                }
            }
        }

        @Override
        public void setExpanded(boolean expand) {
            if(!node.isExpanded()) {
                synchronized ( LOCK ) {
                    EmptyNode emptyNode = new EmptyNode(NbBundle.getMessage(OneProjectDashboard.class, "LBL_LoadingTitle"), NbBundle.getMessage(OneProjectDashboard.class, "LBL_LoadingInProgress")); // NOI18N
                    emptyNode.loadingStarted();
                    model.addRoot(-1, emptyNode);
                }
            } 
            
            node.setExpanded(expand);
        }

        @Override
        public JComponent getComponent() {
            return treeList;
        }

        @Override
        public String getDisplayName() {
            return title;
        }

        @Override
        public boolean isExpanded() {
            return node.isExpanded();
        }
        
    }
    
    public static class DummyDashboardComponentProviderImpl implements TeamDashboardComponentProvider {
        private final static Icon expandedIcon;
        private final static Icon collapsedIcon;

        static {
            JTree tv = new JTree();
            BasicTreeUI tvui = (BasicTreeUI) tv.getUI();
            expandedIcon = tvui.getExpandedIcon();
            collapsedIcon = tvui.getCollapsedIcon();
        }
        
        @Override
        public JComponent create(final TeamDashboardComponentProvider.Section... sections) {
            JPanel panel = new JPanel( new GridBagLayout() );
            panel.setOpaque(false);
            
            for (int i = 0; i < sections.length; i++) {

                final Section section = sections[i];
                
                JPanel sp = new JPanel(new BorderLayout());
                final JLabel titleLabel = new JLabel(section.getDisplayName());
                titleLabel.setIcon(collapsedIcon);
                
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, (float)(titleLabel.getFont().getSize2D() * 1.2)));
                
                final JComponent cmp = section.getComponent();
                
                titleLabel.addMouseListener(new MouseListener() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        boolean expanded = section.isExpanded();

                        cmp.setVisible(!expanded);
                        section.setExpanded(!expanded);
                        titleLabel.setIcon(!expanded ? expandedIcon : collapsedIcon);
                        
                    }
                    @Override public void mousePressed(MouseEvent e) { }
                    @Override public void mouseReleased(MouseEvent e) { }
                    @Override public void mouseEntered(MouseEvent e) { }
                    @Override public void mouseExited(MouseEvent e) { }
                });
                
                titleLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, ColorManager.getDefault().getDefaultBackground()),
                    BorderFactory.createEmptyBorder(1, 1, 0, 1))
                );

                Color c = UIManager.getColor("PropSheet.setBackground");
                titleLabel.setBackground(new Color((int) Math.max(0.0, c.getRed() * 0.85), (int) Math.max(0.0, c.getGreen() * 0.85), (int) Math.max(0.0, c.getBlue() * 0.85)));
            
                sp.add(titleLabel, BorderLayout.NORTH);
                sp.add( cmp, BorderLayout.CENTER);
                
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = i;
                gridBagConstraints.fill = GridBagConstraints.BOTH;
                gridBagConstraints.anchor = GridBagConstraints.WEST;
        
                panel.add( sp, gridBagConstraints );
            }
                
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 2;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;         
            JPanel p = new JPanel();
            p.setOpaque(false);
            panel.add( p, gridBagConstraints );
            
            return panel;
        }

        @Override
        public JComponent createNoProjectComponent(Action newServerAction) {
            throw new UnsupportedOperationException();
        }
    }    
    
    private static class ProjectHistoryList extends ArrayList<String> {

        private final static int MAX_HISTORY_SIZE = Integer.getInteger("team.dashboard.recentProjectsCount", 10); // NOI18N

        static ProjectHistoryList create() {
            String histString = getPrefs().get(PREF_PROJECT_HISTORY, null); 
            LinkedList l = new LinkedList();
            if(histString != null) {
                String[] strs = histString.split("<=>");
                for (String str : strs) {
                    l.add(str);
                }       
            }     
            return new ProjectHistoryList(l);
        }
        
        private ProjectHistoryList(Collection c) {
            super(c);
        }
        
        @Override
        public boolean add(String projectId) {
            synchronized(this) {
                while(contains(projectId)) {
                    remove(projectId);
                }
                while(size() >= MAX_HISTORY_SIZE) {
                    remove(0);
                }
                boolean ret = super.add(projectId); 
                if(ret) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < size() - 1; i++) {
                        sb.append(get(i));
                        sb.append("<=>");
                    }
                    sb.append(projectId);
                    getPrefs().put(PREF_PROJECT_HISTORY, sb.toString());
                }
                return ret;  
            }
        }
        
        @Override
        public String set(int index, String element) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean addAll(int index, Collection<? extends String> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, String element) {
            throw new UnsupportedOperationException();
        }
        
    }
}
