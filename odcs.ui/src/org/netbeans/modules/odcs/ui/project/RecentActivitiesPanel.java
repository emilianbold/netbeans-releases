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
package org.netbeans.modules.odcs.ui.project;

import com.tasktop.c2c.server.profile.domain.activity.BuildActivity;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.activity.ScmActivity;
import com.tasktop.c2c.server.profile.domain.activity.TaskActivity;
import com.tasktop.c2c.server.profile.domain.activity.WikiActivity;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.server.ui.spi.JobHandle;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author jpeska
 */
public class RecentActivitiesPanel extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor(RecentActivitiesPanel.class);
    private final ProjectHandle<ODCSProject> projectHandle;
    private final ODCSClient client;
    private JCheckBox chbTask;
    private JCheckBox chbWiki;
    private JCheckBox chbBuildWatched;
    private JCheckBox chbBuildUnwatched;
    private JCheckBox chbScm;
    private List<ProjectActivity> recentActivities = Collections.emptyList();
    private final Map<ProjectActivity, ActivityPanel> activity2Panel = new HashMap<ProjectActivity, ActivityPanel>();
    private int maxWidth = -1;
    private final BuilderAccessor<ODCSProject> buildAccessor;
    private Cursor defaultCursor;

    /**
     * Creates new form RecentActivitiesPanel
     */
    public RecentActivitiesPanel(ODCSClient client, ProjectHandle<ODCSProject> projectHandle) {
        this.client = client;
        this.projectHandle = projectHandle;
        buildAccessor = ODCSUiServer.forServer(projectHandle.getTeamProject().getServer()).getDashboard().getDashboardProvider().getBuildAccessor(ODCSProject.class);
        initComponents();
        createShowButtons();
        loadRecentActivities();
    }

    void update() {
        activity2Panel.clear();
        loadRecentActivities();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblError = new javax.swing.JLabel();
        lblEmptyContent = new javax.swing.JLabel();
        pnlTitle = new TitlePanel();
        lblTitle = new javax.swing.JLabel();
        pnlShow = new javax.swing.JPanel();
        lblShow = new javax.swing.JLabel();
        pnlContent = new javax.swing.JPanel();
        lblLoading = new javax.swing.JLabel();

        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/odcs/ui/resources/error.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblError, org.openide.util.NbBundle.getMessage(RecentActivitiesPanel.class, "RecentActivitiesPanel.lblError.text")); // NOI18N

        lblEmptyContent.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblEmptyContent.setForeground(new java.awt.Color(102, 102, 102));
        org.openide.awt.Mnemonics.setLocalizedText(lblEmptyContent, org.openide.util.NbBundle.getMessage(RecentActivitiesPanel.class, "RecentActivitiesPanel.lblEmptyContent.text")); // NOI18N

        setBackground(new java.awt.Color(255, 255, 255));

        lblTitle.setFont(lblTitle.getFont().deriveFont(lblTitle.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(lblTitle, org.openide.util.NbBundle.getMessage(RecentActivitiesPanel.class, "RecentActivitiesPanel.lblTitle.text")); // NOI18N

        pnlShow.setOpaque(false);
        pnlShow.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblShow, org.openide.util.NbBundle.getMessage(RecentActivitiesPanel.class, "RecentActivitiesPanel.lblShow.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 7);
        pnlShow.add(lblShow, gridBagConstraints);

        javax.swing.GroupLayout pnlTitleLayout = new javax.swing.GroupLayout(pnlTitle);
        pnlTitle.setLayout(pnlTitleLayout);
        pnlTitleLayout.setHorizontalGroup(
            pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTitleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 247, Short.MAX_VALUE)
                .addComponent(pnlShow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlTitleLayout.setVerticalGroup(
            pnlTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTitleLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(lblTitle)
                .addGap(3, 3, 3))
            .addComponent(pnlShow, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pnlContent.setOpaque(false);
        pnlContent.setLayout(new java.awt.GridBagLayout());

        lblLoading.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblLoading.setForeground(new java.awt.Color(102, 102, 102));
        org.openide.awt.Mnemonics.setLocalizedText(lblLoading, org.openide.util.NbBundle.getMessage(RecentActivitiesPanel.class, "RecentActivitiesPanel.lblLoading.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(100, 0, 0, 0);
        pnlContent.add(lblLoading, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 333, Short.MAX_VALUE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(24, 24, 24)
                    .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblEmptyContent;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblLoading;
    private javax.swing.JLabel lblShow;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlShow;
    private javax.swing.JPanel pnlTitle;
    // End of variables declaration//GEN-END:variables

    private void loadRecentActivities() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<ProjectActivity> recentActivities = client.getRecentActivities(projectHandle.getTeamProject().getId());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (recentActivities != null && !recentActivities.isEmpty()) {
                                RecentActivitiesPanel.this.recentActivities = recentActivities;
                                showRecentActivities();
                            } else {
                                RecentActivitiesPanel.this.recentActivities = Collections.emptyList();
                                showEmptyContent();
                            }
                        }
                    });
                } catch (ODCSException ex) {
                    Utils.logException(ex, true);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showError();
                        }
                    });
                }
            }
        });
    }

    private void createShowButtons() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridheight = GridBagConstraints.REMAINDER;

        chbTask = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Tasks"), Utils.Settings.isShowTasks());
        chbTask.setOpaque(false);
        chbTask.addActionListener(new ShowActionListener(TaskActivity.class));
        pnlShow.add(chbTask, gbc);

        chbWiki = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Wiki"), Utils.Settings.isShowWiki());
        chbWiki.setOpaque(false);
        chbWiki.addActionListener(new ShowActionListener(WikiActivity.class));
        pnlShow.add(chbWiki, gbc);

        chbScm = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Commits"), Utils.Settings.isShowScm());
        chbScm.setOpaque(false);
        chbScm.addActionListener(new ShowActionListener(ScmActivity.class));
        pnlShow.add(chbScm, gbc);

        chbBuildWatched = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Builds"), Utils.Settings.isShowBuilds());
        chbBuildWatched.setOpaque(false);
        chbBuildWatched.addActionListener(new ShowActionListener(BuildActivity.class));
        pnlShow.add(chbBuildWatched, gbc);

        chbBuildUnwatched = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_BuildsUnwatched"), Utils.Settings.isShowBuildsUnwatched());
        chbBuildUnwatched.setOpaque(false);
        chbBuildUnwatched.addActionListener(new ShowActionListener(BuildActivity.class));
        pnlShow.add(chbBuildUnwatched, gbc);

        pnlShow.revalidate();
    }

    private void showRecentActivities() {
        showWaitCursor(true);
        try {
            Date lastDate = null;
            DateSeparatorPanel currentDatePanel = null;
            pnlContent.removeAll();
            boolean isEmpty = true;
            for (ProjectActivity activity : recentActivities) {
                if (!isActivityAllowed(activity)) {
                    continue;
                }
                isEmpty = false;
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(0, 3, 0, 0);
                gbc.anchor = GridBagConstraints.NORTHWEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                Date currentDate = activity.getActivityDate();
                // group activities by days
                if (isAnotherDay(lastDate, currentDate)) {
                    GridBagConstraints gbc1 = new GridBagConstraints();
                    gbc1.insets = new Insets(3, 3, 0, 3);
                    gbc1.anchor = GridBagConstraints.NORTHWEST;
                    gbc1.fill = GridBagConstraints.HORIZONTAL;
                    gbc1.weightx = 1.0;
                    gbc1.gridwidth = GridBagConstraints.REMAINDER;
                    currentDatePanel = new DateSeparatorPanel(currentDate);
                    currentDatePanel.addMouseListener(new ExpandableMouseListener(currentDatePanel, this));
                    pnlContent.add(currentDatePanel, gbc1);
                }
                if (maxWidth == -1) {
                    maxWidth = this.getVisibleRect().width - 150;
                }
                ActivityPanel activityPanel = activity2Panel.get(activity);
                if (activityPanel == null) {
                    activityPanel = new ActivityPanel(activity, projectHandle, maxWidth);
                    activity2Panel.put(activity, activityPanel);
                }
                if (activityPanel.hasDetails()) {
                    activityPanel.addMouseListener(new ExpandableMouseListener(activityPanel, this));
                }
                currentDatePanel.addActivityPanel(activityPanel, gbc);
                lastDate = currentDate;
            }
            if (isEmpty) {
                showEmptyContent();
            } else {
                GridBagConstraints gbc1 = new GridBagConstraints();
                gbc1.weighty = 1.0;
                gbc1.fill = GridBagConstraints.VERTICAL;
                pnlContent.add(new JLabel(), gbc1);
                pnlContent.revalidate();
                this.repaint();
            }
        } finally {    
            showWaitCursor(false);
        }
    }

    private void showWaitCursor(boolean showWait){
        Window window = null;
        Component glassPane = null;
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusOwner != null) {
            window = SwingUtilities.getWindowAncestor(focusOwner);
            if (window != null) {
                RootPaneContainer root = (RootPaneContainer) SwingUtilities.getAncestorOfClass(RootPaneContainer.class, focusOwner);
                glassPane = root.getGlassPane();
            }
        }
        if (window == null || glassPane == null) {
            window = WindowManager.getDefault().getMainWindow();
            glassPane = ((JFrame) window).getGlassPane();
        }

        if (showWait) {
            defaultCursor = window.getCursor();
            Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            window.setCursor(waitCursor);
            glassPane.setCursor(waitCursor);
            glassPane.setVisible(true);
        } else {
            glassPane.setVisible(false);
            window.setCursor(defaultCursor);
            glassPane.setCursor(defaultCursor);
        }
    }

    private boolean isActivityAllowed(ProjectActivity activity) {
        boolean isActivityAllowed = activity instanceof BuildActivity;
        if (isActivityAllowed) {
            boolean watched = isJobWatched((BuildActivity) activity);
            isActivityAllowed = watched ? chbBuildWatched.isSelected() : chbBuildUnwatched.isSelected();
        }
        return activity instanceof TaskActivity && chbTask.isSelected()
                || activity instanceof ScmActivity && chbScm.isSelected()
                || activity instanceof WikiActivity && chbWiki.isSelected()
                || isActivityAllowed;
    }

    private boolean isJobWatched(BuildActivity buildActivity) {
        JobHandle job = buildAccessor.getJob(projectHandle, buildActivity.getJobSummary().getName());
        return job != null ? job.isWatched() : false;
    }

    private boolean isActivityEnable(List<ProjectActivity> recentActivities, Class clazz) {
        for (ProjectActivity activity : recentActivities) {
            if (activity.getClass().isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
    private void showEmptyContent() {
        pnlContent.removeAll();
        pnlContent.add(lblEmptyContent, new GridBagConstraints());
        this.repaint();
    }

    private void showError() {
        pnlContent.removeAll();
        pnlContent.add(lblError, new GridBagConstraints());
        this.repaint();
    }

    private boolean isAnotherDay(Date lastDate, Date newDate) {
        if (lastDate == null) {
            return true;
        }
        return lastDate.getYear() != newDate.getYear() || lastDate.getMonth() != newDate.getMonth() || lastDate.getDay() != newDate.getDay();
    }

    private void persistShowSettings() {
        Utils.Settings.setShowBuilds(chbBuildWatched.isSelected());
        Utils.Settings.setShowBuildsUnwatched(chbBuildUnwatched.isSelected());
        Utils.Settings.setShowScm(chbScm.isSelected());
        Utils.Settings.setShowTasks(chbTask.isSelected());
        Utils.Settings.setShowWiki(chbWiki.isSelected());
    }

    private class ShowActionListener implements ActionListener {

        private final Class clazz;

        public ShowActionListener(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            persistShowSettings();
            if (isActivityEnable(recentActivities, clazz)) {
                showRecentActivities();
            }
        }
    }
}
