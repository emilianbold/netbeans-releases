/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.Utilities;
import org.netbeans.modules.kenai.ui.spi.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.ui.*;
import org.netbeans.modules.kenai.ui.treelist.*;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.*;
import org.openide.windows.TopComponent;

/**
 * Singleton providing access to Kenai Dashboard window.
 *
 * @author S. Aubrecht
 */
public final class DashboardImpl extends Dashboard {

    public static final String PREF_ALL_PROJECTS = "allProjects"; //NOI18N
    public static final String PREF_COUNT = "count"; //NOI18N
    public static final String PREF_ID = "id"; //NOI18N
    private LoginHandle login;
    private final TreeListModel model = new TreeListModel();
    private static final ListModel EMPTY_MODEL = new AbstractListModel() {
        public int getSize() {
            return 0;
        }
        public Object getElementAt(int index) {
            return null;
        }
    };
    private RequestProcessor requestProcessor = new RequestProcessor("Kenai Dashboard"); // NOI18N
    private final TreeList treeList = new TreeList(model);
    private final ArrayList<ProjectHandle> memberProjects = new ArrayList<ProjectHandle>(50);
    private final ArrayList<ProjectHandle> openProjects = new ArrayList<ProjectHandle>(50);
    //TODO: this should not be public
    public final JScrollPane dashboardComponent;
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
    private final EmptyNode noOpenProjects = new EmptyNode(this, NbBundle.getMessage(DashboardImpl.class, "NO_PROJECTS_OPEN"),NbBundle.getMessage(DashboardImpl.class, "LBL_OpeningProjects"));
    private final EmptyNode noMyProjects = new EmptyNode(this, NbBundle.getMessage(DashboardImpl.class, "NO_MY_PROJECTS"), NbBundle.getMessage(DashboardImpl.class, "LBL_OpeningMyProjects"));

    private final Object LOCK = new Object();

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private Kenai kenai = Utilities.getPreferredKenai();

    private PropertyChangeListener kenaiListener;

    private DashboardImpl() {
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
            public void propertyChange(PropertyChangeEvent evt) {
                if( LoginHandle.PROP_MEMBER_PROJECT_LIST.equals(evt.getPropertyName()) ) {
                    refreshMemberProjects(true);
                }
            }
        };

        userNode = new UserNode(this);
        model.addRoot(-1, userNode);
        openProjectsNode = new CategoryNode(this, org.openide.util.NbBundle.getMessage(CategoryNode.class, "LBL_OpenProjects"), null); // NOI18N
        model.addRoot(-1, openProjectsNode);
        model.addRoot(-1, noOpenProjects);

        myProjectsNode = new CategoryNode(this, org.openide.util.NbBundle.getMessage(CategoryNode.class, "LBL_MyProjects"), // NOI18N
                ImageUtilities.loadImageIcon("org/netbeans/modules/kenai/ui/resources/bookmark.png", true)); // NOI18N
        if (login!=null) {
            if (!model.getRootNodes().contains(myProjectsNode)) {
                model.addRoot(-1, myProjectsNode);
            }
            if (!model.getRootNodes().contains(noMyProjects)) {
                model.addRoot(-1, noMyProjects);
            }
        }

        memberProjectsError = new ErrorNode(NbBundle.getMessage(DashboardImpl.class, "ERR_OpenMemberProjects"), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearError(memberProjectsError);
                refreshMemberProjects(true);
            }
        });

        otherProjectsError = new ErrorNode(NbBundle.getMessage(DashboardImpl.class, "ERR_OpenProjects"), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                clearError(otherProjectsError);
                refreshProjects();
            }
        });
        setKenai(kenai);
    }

    /**
     * currently visible kenai instance
     * @return
     */
    public Kenai getKenai() {
        return kenai;
    }

    public static DashboardImpl getInstance() {
        return Holder.theInstance;
    }

    @Override
    public ProjectHandle[] getOpenProjects() {
        TreeSet<ProjectHandle> s = new TreeSet();
        s.addAll(openProjects);
        s.addAll(memberProjects);
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

    @Override
    public boolean isMemberProject(ProjectHandle m) {
        return memberProjects.contains(m);
    }

    public void setKenai(Kenai kenai) {
        this.kenai = kenai;
        final PasswordAuthentication newValue = kenai!=null?kenai.getPasswordAuthentication():null;
        if (newValue == null) {
            setUser(null);
        } else {
            setUser(new LoginHandleImpl(newValue.getUserName()));
        }
        refreshNonMemberProjects();
        if (kenai==null) {
            return;
        }

        kenaiListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pce) {
                if (Kenai.PROP_LOGIN.equals(pce.getPropertyName())) {

                    final PasswordAuthentication newValue = (PasswordAuthentication) pce.getNewValue();
                    if (newValue == null) {
                        setUser(null);
                    } else {
                        setUser(new LoginHandleImpl(newValue.getUserName()));
                    }
                    loggingFinished();
                } else if (Kenai.PROP_LOGIN_STARTED.equals(pce.getPropertyName())) {
                    loggingStarted();
                } else if (Kenai.PROP_LOGIN_FAILED.equals(pce.getPropertyName())) {
                    loggingFinished();
                } else if (Kenai.PROP_XMPP_LOGIN_STARTED.equals(pce.getPropertyName())) {
                    xmppStarted();
                } else if (Kenai.PROP_XMPP_LOGIN.equals(pce.getPropertyName())) {
                    xmppFinsihed();
                } else if (Kenai.PROP_XMPP_LOGIN_FAILED.equals(pce.getPropertyName())) {
                    xmppFinsihed();
                }
            }
        };

        kenai.addPropertyChangeListener(WeakListeners.propertyChange(kenaiListener, kenai));

        KenaiConnection.getDefault(kenai).addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (KenaiConnection.PROP_XMPP_STARTED.equals(evt.getPropertyName())) {
                    xmppStarted();
                } else if (KenaiConnection.PROP_XMPP_FINISHED.equals(evt.getPropertyName())) {
                    xmppFinsihed();
                }
            }
        });

        final PasswordAuthentication pa = kenai.getPasswordAuthentication();
        this.login = pa==null ? null : new LoginHandleImpl(pa.getUserName());
        userNode.set(login, false);
    }
    private static class Holder {
        private static final DashboardImpl theInstance = new DashboardImpl();
    }

    public void selectAndExpand(KenaiProject project) {
        for (TreeListNode n:model.getRootNodes()) {
            if (n instanceof ProjectNode) {
                if (((ProjectNode)n).getProject().getId().equals(project.getName())) {
                    treeList.setSelectedValue(n, true);
                    n.setExpanded(true);
                }
            }
        }
    }

    /**
     * Display given Kenai user in the Dashboard window, the UI will start querying for
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
                }
                switchContent();
            }
            if( null != this.login ) {
                this.login.addPropertyChangeListener(userListener);
            }
        }
    }

    /**
     * Add a Kenai project to the Dashboard.
     * @param project
     * @param isMemberProject
     */
    @Override
    public void addProject(final ProjectHandle project, final boolean isMemberProject, final boolean select) {
        if (project.getKenaiProject().getKenai()!=this.kenai)
            KenaiTopComponent.findInstance().setSelectedKenai(project.getKenaiProject().getKenai());
        requestProcessor.post(new Runnable() {

            public void run() {
                synchronized (LOCK) {
                    if (openProjects.contains(project)) {
                        if (select) {
                            selectAndExpand(((ProjectHandleImpl) project).getKenaiProject());
                        }
                        return;
                    }

                    if (isMemberProject && memberProjectsLoaded && !memberProjects.contains(project)) {
                        memberProjects.add(project);
                        setMemberProjects(new ArrayList<ProjectHandle>(memberProjects));
                    }
                    openProjects.add(project);
                    storeAllProjects();
                    setOtherProjects(new ArrayList<ProjectHandle>(openProjects));
                    userNode.set(login, !openProjects.isEmpty());
                    switchMemberProjects();
                    if (isOpened()) {
                        switchContent();
                        if (select) {
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    selectAndExpand(((ProjectHandleImpl) project).getKenaiProject());
                                }
                            });
                        }
                    }
                }
                changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
            }
        });
    }

    public void removeProject( ProjectHandle project ) {
        synchronized( LOCK ) {
            if( !openProjects.contains(project) ) {
                return;
            }
            openProjects.remove(project);

            storeAllProjects();
            ArrayList<ProjectHandle> tmp = new ArrayList<ProjectHandle>(1);
            tmp.add(project);
            removeProjectsFromModel(tmp);
            if( isOpened() ) {
                switchContent();
            }
        }
        project.firePropertyChange(ProjectHandle.PROP_CLOSE, null, null);
        changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
    }

    Action createLoginAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                UIUtils.showLogin(kenai);
            }
        };
    }

    private Action createWhatIsKenaiAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    URLDisplayer.getDefault().showURL(
                            new URL(NbBundle.getMessage(DashboardImpl.class, "URL_KenaiOverview"))); //NOI18N
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

    public JComponent getComponent() {
        synchronized( LOCK ) {
            opened = true;
            requestProcessor.post(new Runnable() {
                public void run() {
                    UIUtils.waitStartupFinished();
                    myProjectLoadingStarted();
                    projectLoadingStarted();
                    if (null != login && !memberProjectsLoaded) {
                        startLoadingMemberProjects(false);
                    }
                    if (!otherProjectsLoaded) {
                        startLoadingAllProjects(false);
                    }
                }
            });
            switchContent();
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
            if(login!=null?model.getSize() > 3:model.getSize()>2 )
                return;
            addProjectsToModel(-1, openProjects);
            addMemberProjectsToModel(-1, memberProjects);
        }
    }

    private void switchContent() {
        Runnable r = new Runnable() {
            public void run() {
                boolean isEmpty = true;

                synchronized( LOCK ) {
                    isEmpty = null == DashboardImpl.this.login && openProjects.isEmpty();
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

        JLabel lbl = new TreeLabel(NbBundle.getMessage(DashboardImpl.class, "LBL_No_Kenai_Project_Open")); //NOI18N
        lbl.setForeground(ColorManager.getDefault().getDisabledColor());
        lbl.setHorizontalAlignment(JLabel.CENTER);
        LinkButton btnWhatIs = new LinkButton(NbBundle.getMessage(DashboardImpl.class, "LBL_WhatIsKenai"), createWhatIsKenaiAction() ); //NOI18N

        model.removeRoot(userNode);
        model.removeRoot(myProjectsNode);
        model.removeRoot(openProjectsNode);
        userNode.set(null, false);
        res.add( userNode.getComponent(UIManager.getColor("List.foreground"), ColorManager.getDefault().getDefaultBackground(), false, false), new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 4, 3, 4), 0, 0) ); //NOI18N
        res.add( new JLabel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        res.add( lbl, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
        res.add( btnWhatIs, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
        res.add( new JLabel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        return res;
    }

    private void startLoadingAllProjects(boolean forceRefresh) {
        if (kenai==null)
            return;
        String kenaiName = kenai.getUrl().getHost();
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class).node(PREF_ALL_PROJECTS + ("kenai.com".equals(kenaiName)?"":"-"+kenaiName)); //NOI18N
        int count = prefs.getInt(PREF_COUNT, 0); //NOI18N
        if( 0 == count ) {
            projectLoadingFinished();
            return; //nothing to load
        }
        ArrayList<String> ids = new ArrayList<String>(count);
        for( int i=0; i<count; i++ ) {
            String id = prefs.get(PREF_ID+i, null); //NOI18N
            if( null != id && id.trim().length() > 0 ) {
                ids.add( id.trim() );
            }
        }
        synchronized( LOCK ) {
            if( otherProjectsLoader != null )
                otherProjectsLoader.cancel();
            if( ids.isEmpty() ) {
                projectLoadingFinished();
                return;
            }
            otherProjectsLoader = new OtherProjectsLoader(ids, forceRefresh);
            requestProcessor.post(otherProjectsLoader);
        }
    }

    private void storeAllProjects() {
        String kenaiName = kenai.getUrl().getHost();
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class).node(PREF_ALL_PROJECTS + ("kenai.com".equals(kenaiName)?"":"-"+kenaiName)); //NOI18N
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

    private void setOtherProjects(ArrayList<ProjectHandle> projects) {
        synchronized( LOCK ) {
            removeProjectsFromModel( openProjects );
            if (!projects.isEmpty()) {
                model.removeRoot(noOpenProjects);
            }
            openProjects.clear();
            for( ProjectHandle p : projects ) {
                if( !openProjects.contains( p ) )
                    openProjects.add( p );
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
        changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
    }

    private void switchMemberProjects() {
        for( TreeListNode n : model.getRootNodes() ) {
            if( !(n instanceof ProjectNode) )
                continue;
            ProjectNode pn = (ProjectNode) n;
            pn.setMemberProject( memberProjects.contains( pn.getProject() ) );
        }
    }

    public void bookmarkingStarted() {
        userNode.loadingStarted(NbBundle.getMessage(UserNode.class, "LBL_Bookmarking"));
    }

    public void bookmarkingFinished() {
        userNode.loadingFinished();
    }

    public void deletingStarted() {
        userNode.loadingStarted(NbBundle.getMessage(UserNode.class, "LBL_Deleting"));
    }

    public void deletingFinished() {
        userNode.loadingFinished();
    }


    private void loggingStarted() {
        userNode.loadingStarted(NbBundle.getMessage(UserNode.class, "LBL_Authenticating"));
    }

    private void loggingFinished() {
        userNode.loadingFinished();
    }

    private void xmppStarted() {
        userNode.loadingStarted(NbBundle.getMessage(UserNode.class, "LBL_ConnectingXMPP"));
    }

    private void xmppFinsihed() {
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

    void myProjectsProgressStarted() {
        userNode.loadingStarted(NbBundle.getMessage(UserNode.class, "LBL_LoadingIssues"));
    }

    void myProjectsProgressFinished() {
        userNode.loadingFinished();
    }

    private void startLoadingMemberProjects(boolean forceRefresh) {
        synchronized( LOCK ) {
            if( memberProjectsLoader != null )
                memberProjectsLoader.cancel();
            if( null == login )
                return;
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
                if( !memberProjects.contains(p) )
                    memberProjects.add(p);
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
        changeSupport.firePropertyChange(PROP_OPENED_PROJECTS, null, null);
    }

    private void addProjectsToModel( int index, List<ProjectHandle> projects ) {
        int counter = 2;
        for( ProjectHandle p : projects ) {
            model.addRoot(counter++,new ProjectNode(p));
        }
    }

    private void addMemberProjectsToModel( int index, List<ProjectHandle> projects ) {
        for( ProjectHandle p : projects ) {
            model.addRoot(index, new MyProjectNode(p) );
        }
    }

    private void removeProjectsFromModel( List<ProjectHandle> projects ) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<TreeListNode>(projects.size());
        int i=0;
        for( TreeListNode root : model.getRootNodes() ) {
            if( root instanceof ProjectNode ) {
                ProjectNode pn = (ProjectNode) root;
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
            if (!model.getRootNodes().contains(noOpenProjects))
                model.addRoot(2, noOpenProjects);
        }
    }

    private void removeMemberProjectsFromModel( List<ProjectHandle> projects ) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<TreeListNode>(projects.size());
        int i=0;
        for( TreeListNode root : model.getRootNodes() ) {
            if( root instanceof MyProjectNode ) {
                MyProjectNode pn = (MyProjectNode) root;
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
            if (!model.getRootNodes().contains(noMyProjects) && login!=null)
                model.addRoot(-1, noMyProjects);
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

    private class OtherProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final ArrayList<String> projectIds;
        private boolean forceRefresh;

        public OtherProjectsLoader( ArrayList<String> projectIds, boolean forceRefresh ) {
            this.projectIds = projectIds;
            this.forceRefresh = forceRefresh;
        }

        public void run() {
            projectLoadingStarted();
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                public void run() {
                    ArrayList<ProjectHandle> projects = new ArrayList<ProjectHandle>(projectIds.size());
                    ProjectAccessor accessor = ProjectAccessor.getDefault();
                    for( String id : projectIds ) {
                        ProjectHandle handle = accessor.getNonMemberProject(kenai, id, forceRefresh);
                        if (handle!=null) {
                            projects.add(handle);
                        } else {
                            //projects=null;
                        }
                    }
                    res[0] = projects;
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
            if( cancelled )
                return;
            if( null == res[0] ) {
                showError( otherProjectsError );
                return;
            }

            setOtherProjects( res[0] );
            clearError( otherProjectsError );
        }

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
        private boolean forceRefresh;

        public MemberProjectsLoader( LoginHandle login, boolean forceRefresh ) {
            this.user = login;
            this.forceRefresh = forceRefresh;
        }

        public void run() {
            myProjectLoadingStarted();
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                public void run() {
                    ProjectAccessor accessor = ProjectAccessor.getDefault();
                    res[0] = new ArrayList( accessor.getMemberProjects(kenai, user, forceRefresh) );
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
            if( cancelled )
                return;
            if( null == res[0] ) {
                showError( memberProjectsError );
                return;
            }

            setMemberProjects( res[0] );
            clearError( memberProjectsError );
        }

        public boolean cancel() {
            cancelled = true;
            if( null != t ) {
                t.interrupt();
            }
            return true;
        }
    }
}
