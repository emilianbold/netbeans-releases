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

package org.netbeans.modules.odcs.ui.dashboard;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.team.ui.common.DashboardSupport;
import org.netbeans.modules.team.ui.common.LinkButton;
import org.netbeans.modules.team.ui.common.MyProjectNode;
import org.netbeans.modules.team.ui.spi.BuildHandle.Status;
import org.netbeans.modules.team.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.ui.spi.JobHandle;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.QueryHandle;
import org.netbeans.modules.team.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.ui.util.treelist.ProgressLabel;
import org.netbeans.modules.team.ui.util.treelist.TreeLabel;
import org.openide.awt.Notification;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * My Project's root node
 *
 * @author Jan Becicka
 */
public class OdcsProjectNode extends MyProjectNode<ODCSProject> {

    private Notification bugNotification;
    private final ProjectHandle<ODCSProject> project;
    private final ProjectAccessor accessor;
    private final QueryAccessor qaccessor;
    private final BuilderAccessor<ODCSProject> buildAccessor;
    private PropertyChangeListener buildHandleStatusListener;
    private QueryHandle allIssuesQuery;
    private PropertyChangeListener notificationListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (QueryHandle.PROP_QUERY_ACTIVATED.equals(evt.getPropertyName())) {
                if (bugNotification != null) {
                    bugNotification.clear();
                }
                allIssuesQuery.removePropertyChangeListener(notificationListener);
            }
        }
    };

    private Action openAction;

    private JPanel component = null;
    private JLabel lbl = null;
    private LinkButton btnOpen = null;
    private LinkButton btnBugs = null;
    private LinkButton btnBuilds = null;
    private ProgressLabel lblBookmarkingProgress = null;
    
    private boolean isMemberProject = false;

    private final Object LOCK = new Object();

    private final PropertyChangeListener projectListener;
    private TreeLabel rightPar;
    private TreeLabel leftPar;
    private TreeLabel delim;
    private RequestProcessor issuesRP = new RequestProcessor(OdcsProjectNode.class);
    private final DashboardSupport<ODCSProject> dashboard;
    private final boolean canOpen;
    private final boolean canBookmark;
    private LinkButton btnBookmark;
    private Action closeAction;
    private LinkButton btnClose;
    private JLabel myPrjLabel;
    private JLabel closePlaceholder;

    public OdcsProjectNode( final ProjectHandle<ODCSProject> project, final DashboardSupport<ODCSProject> dashboard, boolean canOpen, boolean canBookmark, Action closeAction) {
        super( ODCSUiServer.forServer(project.getTeamProject().getServer()), null );
        
        this.dashboard = dashboard;
        this.projectListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( ProjectHandle.PROP_CONTENT.equals( evt.getPropertyName()) ) {
                    refreshChildren();
                    if( null != lbl ) {
                        lbl.setText(project.getDisplayName());
                    }
                } else if (QueryHandle.PROP_QUERY_RESULT.equals(evt.getPropertyName())) {
                    List<QueryResultHandle> queryResults = (List<QueryResultHandle>) evt.getNewValue();
                    for (QueryResultHandle queryResult : queryResults) {
                        if (queryResult.getResultType() == QueryResultHandle.ResultType.ALL_CHANGES_RESULT) {
                            dashboard.myProjectsProgressStarted();
                            setBugsLater(queryResult);
                            return;
                        }
                    }
                } else if (ProjectHandle.PROP_BUILD_LIST.equals(evt.getPropertyName())) {
                    scheduleUpdateBuilds();
                }
            }
        };
        this.project = project;
        this.canOpen = canOpen;
        this.canBookmark = canBookmark;
        this.closeAction = closeAction;
        this.accessor = dashboard.getDashboardProvider().getProjectAccessor();
        this.qaccessor = dashboard.getDashboardProvider().getQueryAccessor();
        this.buildAccessor = dashboard.getDashboardProvider()
                .getBuildAccessor(ODCSProject.class);
        this.project.addPropertyChangeListener( projectListener );
        project.getTeamProject().getServer().addPropertyChangeListener(projectListener);
        project.getTeamProject().addPropertyChangeListener(projectListener);
    }


    @Override
    public void setIsMember(boolean isMember) {
        if( isMember == this.isMemberProject ) {
            return;
        }
        this.isMemberProject = isMember;
        fireContentChanged();
        refreshChildren();
    }
    
    @Override
    public ProjectHandle getProject() {
        return project;
    }

    ProjectAccessor getAccessor() {
        return accessor;
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int maxWidth) {
        synchronized( LOCK ) {
            if( null == component ) {
                component = new JPanel( new GridBagLayout() );
                component.setOpaque(false);
                lbl = new TreeLabel(project.getDisplayName());
                component.add( lbl, new GridBagConstraints(0,0,1,1,0.0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,3), 0,0) );
                leftPar = new TreeLabel("("); // NOI18N
                delim = new TreeLabel("|"); //NOI18N
                rightPar = new TreeLabel(")"); // NOI18N
                component.add(leftPar, new GridBagConstraints(1, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                component.add(delim, new GridBagConstraints(4, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                component.add(rightPar, new GridBagConstraints(6, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                setOnline(false);
                
                issuesRP.post(new Runnable() {
                    @Override
                    public void run() {
                        dashboard.myProjectsProgressStarted();
                        allIssuesQuery = qaccessor == null || !project.getTeamProject().hasTasks() 
                                ? null : qaccessor.getAllIssuesQuery(project);
                        if (allIssuesQuery != null) {
                            allIssuesQuery.addPropertyChangeListener(projectListener);
                            List<QueryResultHandle> queryResults = qaccessor.getQueryResults(allIssuesQuery);
                            for (QueryResultHandle queryResult:queryResults) {
                                if (queryResult.getResultType()==QueryResultHandle.ResultType.ALL_CHANGES_RESULT) {
                                    setBugsLater(queryResult);
                                    return;
                                }
                            }
                        }
                        dashboard.myProjectsProgressFinished();
                        
                    }
                });
                scheduleUpdateBuilds();

                component.add( new JLabel(), new GridBagConstraints(7,0,1,1,1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0) );
                
                int idxX = 8;
                if(canBookmark) {
                    AbstractAction ba = new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            accessor.bookmark(project);
                        }
                    };
                    ImageIcon bookmarkImage = ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/" + (isMemberProject?"bookmark.png":"unbookmark.png"), true);
                    btnBookmark = new LinkButton(bookmarkImage, ba); 
                    btnBookmark.putClientProperty(DashboardSupport.PROP_BTN_NOT_CLOSING_MEGA_MENU, true);
                    btnBookmark.setRolloverEnabled(true);
                    btnBookmark.setToolTipText(NbBundle.getMessage(OdcsProjectNode.class, isMemberProject?"LBL_LeaveProject":"LBL_Bookmark"));
                    component.add( btnBookmark, new GridBagConstraints(idxX++,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                    if(canOpen) {
                        myPrjLabel = new JLabel();
                        component.add( myPrjLabel, new GridBagConstraints(idxX++,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );                    
                    }
                    lblBookmarkingProgress = createProgressLabel("");
                    lblBookmarkingProgress.setVisible(false);
                    component.add( lblBookmarkingProgress, new GridBagConstraints(idxX++,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );                                        
                }
                final ImageIcon closeImage = ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/close.png", true);
                closePlaceholder = new JLabel();
                Dimension d = new Dimension(closeImage.getIconWidth(), closeImage.getIconHeight());
                closePlaceholder.setMinimumSize(d);
                closePlaceholder.setMaximumSize(d);
                closePlaceholder.setPreferredSize(d);
                // placeholder for missing present close 
                component.add( closePlaceholder, new GridBagConstraints(idxX++,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL, new Insets(0,3,0,0), 0,0) );
                      
                if(closeAction != null) {
                    btnClose = new LinkButton(closeImage, closeAction); //NOI18N
                    btnClose.putClientProperty(DashboardSupport.PROP_BTN_NOT_CLOSING_MEGA_MENU, true);
                    btnClose.setToolTipText(NbBundle.getMessage(OdcsProjectNode.class, "LBL_Close"));
                    btnClose.setRolloverEnabled(true);
                    btnClose.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/team/ui/resources/close_over.png", true)); // NOI18N
                    component.add( btnClose, new GridBagConstraints(idxX++,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                }
                if(canOpen) {
                    btnOpen = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/open.png", true), getOpenAction()); //NOI18N
                    btnOpen.setText(null);
                    btnOpen.setToolTipText(NbBundle.getMessage(OdcsProjectNode.class, "LBL_Open"));
                    btnOpen.setRolloverEnabled(true);
                    btnOpen.setRolloverIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/open_over.png", true)); // NOI18N
                    component.add( btnOpen, new GridBagConstraints(idxX++,0,1,1,0.0,0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                }
            }
            
            if(btnBookmark != null) {
                btnBookmark.setForeground(foreground, isSelected);
                setBookmarkIcon(); 
                btnBookmark.setToolTipText(NbBundle.getMessage(OdcsProjectNode.class, isMemberProject?"LBL_LeaveProject":"LBL_Bookmark"));
            }
            if(btnBugs != null) {
                btnBugs.setForeground(foreground, isSelected);
            }
            if(btnBuilds != null) {
                btnBuilds.setForeground(foreground, isSelected);
            }            
            if(btnClose != null) {
                if(isSelected) {
                    btnClose.setVisible(!isMemberProject);
                    btnClose.setForeground(foreground, isSelected);
                } else {
                    btnClose.setVisible(false);
                }
                closePlaceholder.setVisible(btnClose == null || !btnClose.isVisible());
            } 
            
            lbl.setForeground(foreground);
            return component;
        }
    }

    private void setBookmarkIcon() {
        btnBookmark.setIcon(ImageUtilities.loadImageIcon(
                    "org/netbeans/modules/team/ui/resources/" + (isMemberProject?"bookmark.png":"unbookmark.png"), true)); // NOI18N
        btnBookmark.setRolloverIcon(ImageUtilities.loadImageIcon(
                    "org/netbeans/modules/team/ui/resources/" + (isMemberProject?"bookmark_over.png":"unbookmark_over.png"), true)); // NOI18N
    }
    
    @Override
    public Action getDefaultAction() {
        return accessor.getDefaultAction(project, false);
    }

    private void setOnline(final boolean b) {
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if ((btnBugs == null || "0".equals(btnBugs.getText())) && btnBuilds == null) { // NOI18N
                    if (leftPar != null) {
                        leftPar.setVisible(b);
                    }
                    if (delim != null) {
                        delim.setVisible(b);
                    }
                    if (rightPar != null) {
                        rightPar.setVisible(b);
                    }
                }
                dashboard.getComponent().repaint();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }

    private Action getOpenAction() {
        if (openAction == null) {
            openAction = getDefaultAction();
        }
        return openAction;
    }

    @Override
    public Action[] getPopupActions() {
        return accessor.getPopupActions(project, false);
    }

    @Override
    public void bookmarkingStarted() {
        if(btnBookmark != null) {
            btnBookmark.setVisible(false);
            lblBookmarkingProgress.setVisible(true);
            fireContentChanged();
        }
    }

    @Override
    public void bookmarkingFinished() {
        if(btnBookmark != null) {
            btnBookmark.setVisible(true);
            lblBookmarkingProgress.setVisible(false);
            setBookmarkIcon();
            fireContentChanged();
        }
    }
    
    @Override
    protected void dispose() {
        super.dispose();
        
        project.removePropertyChangeListener( projectListener );
        project.getTeamProject().getServer().removePropertyChangeListener(projectListener);
        project.getTeamProject().removePropertyChangeListener(projectListener);
        
        if (allIssuesQuery != null) {
            allIssuesQuery.removePropertyChangeListener(projectListener);
            allIssuesQuery=null;
        }
        
        if (bugNotification != null) {
            bugNotification.clear();
        }
    }

    private void setBugsLater(final QueryResultHandle bug) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (btnBugs!=null) {
                    component.remove(btnBugs);
                }
                btnBugs = new LinkButton(bug.getText(), ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/ui/resources/bug.png", true), qaccessor.getOpenQueryResultAction(bug)); // NOI18N
                btnBugs.putClientProperty("MM.Closing", true);
                btnBugs.setHorizontalTextPosition(JLabel.LEFT);
                btnBugs.setToolTipText(bug.getToolTipText());
                component.add( btnBugs, new GridBagConstraints(3,0,1,1,0,0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,3,0,0), 0,0) );
                updateDetailsVisible();
            }
        });
    }

    private void scheduleUpdateBuilds() {
        issuesRP.post(new Runnable() {
            @Override
            public void run() {
                if (buildAccessor != null) {
                    if (buildAccessor.hasBuilds(project)) {
                        if (buildHandleStatusListener == null) {
                            initBuildHandleStatusListener();
                        }
                        List<JobHandle> builds =
                                buildAccessor.getJobs(project);
                        for (JobHandle buildHandle : builds) {
                            buildHandle.removePropertyChangeListener(buildHandleStatusListener);
                            buildHandle.addPropertyChangeListener(buildHandleStatusListener);
                        }
                        JobHandle bh = buildAccessor
                                .chooseMostInterrestingJob(builds);
                        setBuildsLater(bh, prepareTooltipText(bh, builds));
                    }
                }
            }

            private void initBuildHandleStatusListener() {
                buildHandleStatusListener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(
                            PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals(
                                JobHandle.PROP_STATUS)) {
                            scheduleUpdateBuilds();
                        }
                    }
                };
            }
        });
    }

      @NbBundle.Messages({
        "# {0} - name of single failed build",
        "MSG_failed_single=Build {0} has failed",
        "# {0} - number of failed builds",
        "MSG_failed_multiple={0} builds have failed",
        "# {0} - name of single unstable build",
        "MSG_unstable_single=Build {0} is unstable",
        "# {0} - number of unstable builds",
        "MSG_unstable_multiple={0} builds are unstable"
    })
    private String prepareTooltipText(JobHandle interrestingBuild,
            List<JobHandle> allBuilds) {
        if (interrestingBuild == null) {
            return null;
        }
        JobHandle unstable = null;
        JobHandle failed = null;
        int countUnstable = 0;
        int countFailed = 0;
        for (JobHandle bh : allBuilds) {
            if (bh.getStatus().equals(Status.UNSTABLE)) {
                countUnstable++;
                unstable = bh;
            } else if (bh.getStatus().equals(Status.FAILED)) {
                countFailed++;
                failed = bh;
            }
        }
        if (countFailed + countUnstable == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("<html>");                 //NOI18N
        if (countFailed == 1 && failed != null) {
            sb.append(Bundle.MSG_failed_single(failed.getDisplayName()));
        } else if (countFailed > 1) {
            sb.append(Bundle.MSG_failed_multiple(countFailed));
        }
        if (countFailed > 0 && countUnstable > 0) {
            sb.append("<br/>");                                         //NOI18N
        }
        if (countUnstable == 1 && unstable != null) {
            sb.append(Bundle.MSG_unstable_single(unstable.getDisplayName()));
        } else if (countUnstable > 1) {
            sb.append(Bundle.MSG_unstable_multiple(countUnstable));
        }
        sb.append("</html>");                                           //NOI18N
        return sb.toString();
    }

    /**
     * Set interresting builds button.
     *
     * @param buildHandle Handle of the interresting build.
     */
    private void setBuildsLater(final JobHandle buildHandle,
            final String tooltipText) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (btnBuilds != null) {
                    component.remove(btnBuilds);
                }
                if (buildHandle != null) {
                    Action action = buildHandle.getDefaultAction();
                    Icon actionIcon = (Icon) action.getValue(Action.SMALL_ICON);
                    String actionName = (String) action.getValue(Action.NAME);
                    btnBuilds = new LinkButton(actionName, actionIcon, action);
                    btnBuilds.setHorizontalTextPosition(JLabel.LEFT);
                    btnBuilds.setVerticalAlignment(JButton.CENTER);
                    btnBuilds.setToolTipText(tooltipText);
                    component.add(btnBuilds, new GridBagConstraints(5, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
                } else {
                    btnBuilds = null;
                }
                updateDetailsVisible();
            }
        });
    }

    private void updateDetailsVisible() {
        boolean bugsVisible = btnBugs != null && !"0".equals(btnBugs.getText()); //NOI18N
        boolean buildsVisible = btnBuilds != null;
        boolean visible = bugsVisible || buildsVisible;
        leftPar.setVisible(visible);
        rightPar.setVisible(visible);
        if (btnBugs != null) {
            btnBugs.setVisible(bugsVisible);
        }
        if (btnBuilds != null) {
            btnBuilds.setVisible(buildsVisible);
        }
        delim.setVisible(bugsVisible && buildsVisible);
        component.validate();
        dashboard.myProjectsProgressFinished();
        fireContentChanged();
        fireContentSizeChanged();
    }

    @Override
    public String toString () {
        return project.getDisplayName();
    }

    @Override
    protected Type getType() {
        return Type.CLOSED;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.project != null ? this.project.getId().hashCode() : 0);
        hash = 41 * hash + (this.dashboard.getServer() != null ? this.dashboard.getServer().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OdcsProjectNode other = (OdcsProjectNode) obj;
        if (this.project != other.project && (this.project == null || !this.project.getId().equals(other.project.getId()))) {
            return false;
        }
        if (this.dashboard.getServer() != other.dashboard.getServer() && (this.dashboard.getServer() == null || !this.dashboard.getServer().equals(other.dashboard.getServer()))) {
            return false;
        }
        return true;
    }

}
