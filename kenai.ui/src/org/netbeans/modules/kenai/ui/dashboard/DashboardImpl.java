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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import org.netbeans.modules.kenai.ui.spi.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.ui.LoginAction;
import org.netbeans.modules.kenai.ui.LoginHandleImpl;
import org.netbeans.modules.kenai.ui.treelist.TreeList;
import org.netbeans.modules.kenai.ui.treelist.TreeListModel;
import org.netbeans.modules.kenai.ui.treelist.TreeListNode;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * Singleton providing access to Kenai Dashboard window.
 *
 * @author S. Aubrecht
 */
public final class DashboardImpl extends Dashboard {

    private LoginHandle login;
    private TreeListModel model;
    private ArrayList<ProjectHandle> memberProjects;
    private final ArrayList<ProjectHandle> otherProjects = new ArrayList<ProjectHandle>(50);
    private JPanel dashboardComponent;
    private final PropertyChangeListener userListener;
    private boolean opened = false;

    private static final long TIMEOUT_INTERVAL_MILLIS = 5*1000;

    private OtherProjectsLoader otherProjectsLoader;
    private MemberProjectsLoader memberProjectsLoader;

    private LoginNode loginNode;
    private UserNode userNode;

    private final Object LOCK = new Object();

    public DashboardImpl() {
        userListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( LoginHandle.PROP_MEMBER_PROJECT_LIST.equals(evt.getPropertyName()) ) {
                    refreshMemberProjects();
                }
            }
        };
        final Kenai kenai = Kenai.getDefault();
        final PasswordAuthentication pa = kenai.getPasswordAuthentication();
        setUser(pa==null?null:new LoginHandleImpl(pa.getUserName()));
        kenai.addPropertyChangeListener(Kenai.PROP_LOGIN, new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                final PasswordAuthentication newValue = (PasswordAuthentication) pce.getNewValue();
                if (newValue==null) {
                    setUser(null);
                } else {
                    setUser(new LoginHandleImpl(newValue.getUserName()));
                }
            }
        });
    }

    public static DashboardImpl getInstance() {
        return (DashboardImpl) Dashboard.getDefault();
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
            if( isOpened() ) {
                if( null != memberProjects )
                    removeProjectsFromModel(memberProjects);
            }
            memberProjects = null;
            if( isOpened() ) {
                Runnable r = new Runnable() {
                    public void run() {
                        initTreeList();
                        if( null != loginNode )
                            model.removeRoot( loginNode );
                        if( null != userNode )
                            model.removeRoot( userNode );
                        userNode = new UserNode(login, DashboardImpl.this);
                        model.addRoot(0, userNode);
                        initTreeList();
                        startLoadingMemberProjects();
                    }
                };
                if( SwingUtilities.isEventDispatchThread() ) {
                    r.run();
                } else {
                    SwingUtilities.invokeLater(r);
                }
            }
        }
    }

    /**
     * Add a Kenai project which current user isn't member of to the Dashboard.
     * @param project
     * @see ActionsFactory.getOpenNonMemberProjectAction
     */
    public void addNonMemberProject( ProjectHandle project ) {
        synchronized( LOCK ) {
            if( !otherProjects.contains(project) ) {
                otherProjects.add(project);
                storeOtherProjects();
            }
            if( isOpened() ) {
                initTreeList();
                ArrayList<ProjectHandle> tmp = new ArrayList<ProjectHandle>(1);
                tmp.add(project);
                addProjectsToModel(-1, tmp);
            }
        }
    }

    public void removeProject( ProjectHandle project ) {
        synchronized( LOCK ) {
            if( !otherProjects.contains(project) ) {
                return;
            }
            otherProjects.remove(project);
            storeOtherProjects();
            if( isOpened() ) {
                initTreeList();
                ArrayList<ProjectHandle> tmp = new ArrayList<ProjectHandle>(1);
                tmp.add(project);
                removeProjectsFromModel(tmp);
            }
        }
    }

    ActionListener createLoginAction() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LoginAction.getDefault().actionPerformed(e);
            }
        };
    }

    private ActionListener createWhatIsKenaiAction() {
        return new ActionListener() {
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

    private void setErrorMessage( String msg ) {
        if( null != userNode )
            userNode.setError(msg);
    }

    private void setStatusMessage( String msg ) {
        if( null != userNode )
            userNode.setStatus(msg);
    }

    private void clearMessages() {
        if( null != userNode )
            userNode.clearMessage();
    }

    private void initTreeList() {
        if( null != model ) {
            return;
        }
        dashboardComponent.removeAll();
        model = new TreeListModel();
        if( null == login ) {
            loginNode = new LoginNode(this);
            model.addRoot(-1, loginNode);
        } else {
            userNode = new UserNode(login, this);
            model.addRoot(-1, userNode);
        }
        TreeList treeList = new TreeList(model);
        dashboardComponent.add(treeList, BorderLayout.CENTER);
        dashboardComponent.invalidate();
        dashboardComponent.revalidate();
        dashboardComponent.repaint();
    }
    
    boolean isOpened() {
        return opened;
    }

    void refreshMemberProjects() {
        synchronized( LOCK ) {
            if( isOpened() ) {
                startLoadingMemberProjects();
            } else {
                memberProjects = null;
            }
        }
    }

    public void close() {
        synchronized( LOCK ) {
            if( null != dashboardComponent )
                dashboardComponent.removeAll();
            dashboardComponent = null;
            if( null != model )
                model.clear();
            model = null;
            opened = false;
        }
    }

    public JComponent getComponent() {
        synchronized( LOCK ) {
            if( null != login ) {
                if( null == memberProjects )
                    startLoadingMemberProjects();
                initTreeList();
            }
            opened = true;
            if( otherProjects.isEmpty() ) {
                startLoadingOtherProjects();
            }
            dashboardComponent = new JPanel(new BorderLayout());
            dashboardComponent.setBackground(ColorManager.defaultBackground);
            if( null == login && otherProjects.isEmpty() ) {
                dashboardComponent.add( createEmptyContent(), BorderLayout.CENTER );
            } else {
                initTreeList();
            }
        }
        return dashboardComponent;
    }

    private JComponent createEmptyContent() {
        JPanel res = new JPanel( new GridBagLayout() );
        res.setOpaque(false);
        LoginNode tmp = new LoginNode(this);

        JLabel lbl = new JLabel(NbBundle.getMessage(DashboardImpl.class, "LBL_No_Kenai_Project_Open")); //NOI18N
        lbl.setForeground(ColorManager.disabledColor);
        lbl.setHorizontalAlignment(JLabel.CENTER);
        LinkButton btnWhatIs = new LinkButton(NbBundle.getMessage(DashboardImpl.class, "LBL_WhatIsKenai"), createWhatIsKenaiAction() ); //NOI18N

        res.add( tmp.getComponent(UIManager.getColor("List.foreground"), ColorManager.defaultBackground, false, false), new GridBagConstraints(0, 0, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 4, 3, 4), 0, 0) ); //NOI18N
        res.add( new JLabel(), new GridBagConstraints(0, 1, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        res.add( lbl, new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 4, 0), 0, 0) );
        res.add( btnWhatIs, new GridBagConstraints(0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(4, 0, 0, 0), 0, 0) );
        res.add( new JLabel(), new GridBagConstraints(0, 4, 3, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0) );
        return res;
    }

    private void startLoadingOtherProjects() {
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class);
        int count = prefs.getInt("projectCount", 0); //NOI18N
        if( 0 == count )
            return; //nothing to load
        ArrayList<String> ids = new ArrayList<String>(count);
        for( int i=0; i<count; i++ ) {
            String id = prefs.get("project"+i, null); //NOI18N
            if( null != id && id.trim().length() > 0 ) {
                ids.add( id.trim() );
            }
        }
        synchronized( LOCK ) {
            if( otherProjectsLoader != null )
                otherProjectsLoader.cancel();
            if( ids.isEmpty() )
                return;
            otherProjectsLoader = new OtherProjectsLoader(ids);
            RequestProcessor.getDefault().post(otherProjectsLoader);
        }
    }

    private void storeOtherProjects() {
        Preferences prefs = NbPreferences.forModule(DashboardImpl.class);
        prefs.putInt("projectCount", otherProjects.size()); //NOI18N
        int index = 0;
        for( ProjectHandle project : otherProjects ) {
            prefs.put("project"+index++, project.getId()); //NOI18N
        }
    }

    private void setOtherProjects(ArrayList<ProjectHandle> projects) {
        clearMessages();
        synchronized( LOCK ) {
            if( isOpened() ) {
                initTreeList();
                removeProjectsFromModel( otherProjects );
                otherProjects.clear();
                otherProjects.addAll(projects);
                addProjectsToModel( -1, otherProjects );
            } else {
                otherProjects.clear();
                otherProjects.addAll(projects);
            }
            storeOtherProjects();
        }
    }


    private void startLoadingMemberProjects() {
        synchronized( LOCK ) {
            if( memberProjectsLoader != null )
                memberProjectsLoader.cancel();
            if( null == login )
                return;
            if( null != userNode ) {
                setStatusMessage(NbBundle.getMessage(DashboardImpl.class, "LBL_OpeningProjects")); //NOI18N
            }
            memberProjectsLoader = new MemberProjectsLoader(login);
            RequestProcessor.getDefault().post(memberProjectsLoader);
        }
    }

    private void setMemberProjects(ArrayList<ProjectHandle> projects) {
        clearMessages();
        synchronized( LOCK ) {
            if( isOpened() ) {
                initTreeList();
                if( null != memberProjects )
                    removeProjectsFromModel( memberProjects );
                memberProjects = projects;
                addProjectsToModel( 1, memberProjects );
            } else {
                memberProjects = projects;
            }
        }
    }

    private void addProjectsToModel( int index, List<ProjectHandle> projects ) {
        for( ProjectHandle p : projects ) {
            model.addRoot( index, new ProjectNode(p) );
        }
    }

    private void removeProjectsFromModel( List<ProjectHandle> projects ) {
        ArrayList<TreeListNode> nodesToRemove = new ArrayList<TreeListNode>(projects.size());
        for( TreeListNode root : model.getRootNodes() ) {
            if( root instanceof ProjectNode ) {
                ProjectNode pn = (ProjectNode) root;

                if( projects.contains( pn.getProject() ) ) {
                    nodesToRemove.add(root);
                }
            }
        }
        for( TreeListNode node : nodesToRemove ) {
            model.removeRoot(node);
        }
    }

    private class OtherProjectsLoader implements Runnable, Cancellable {

        private boolean cancelled = false;
        private Thread t = null;

        private final ArrayList<String> projectIds;

        public OtherProjectsLoader( ArrayList<String> projectIds ) {
            this.projectIds = projectIds;
        }

        public void run() {
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                public void run() {
                    ArrayList<ProjectHandle> projects = new ArrayList<ProjectHandle>(projectIds.size());
                    ProjectAccessor accessor = ProjectAccessor.getDefault();
                    for( String id : projectIds ) {
                        ProjectHandle handle = accessor.getNonMemberProject(id);
                        projects.add(handle);
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
            if( cancelled )
                return;
            if( null == res[0] ) {
                setErrorMessage( NbBundle.getMessage(DashboardImpl.class, "LBL_ErrLoadProject")); //NOI18N
                return;
            }

            setOtherProjects( res[0] );
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

        public MemberProjectsLoader( LoginHandle login ) {
            this.user = login;
        }

        public void run() {
            final ArrayList[] res = new ArrayList[1];
            Runnable r = new Runnable() {
                public void run() {
                    ProjectAccessor accessor = ProjectAccessor.getDefault();
                    res[0] = new ArrayList( accessor.getMemberProjects(user) );
                }
            };
            t = new Thread( r );
            t.start();
            try {
                t.join( TIMEOUT_INTERVAL_MILLIS );
            } catch( InterruptedException iE ) {
                //ignore
            }
            if( cancelled )
                return;
            if( null == res[0] ) {
                setErrorMessage( NbBundle.getMessage(DashboardImpl.class, "LBL_NotResponding")); //NOI18N
                return;
            }

            setMemberProjects( res[0] );
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
