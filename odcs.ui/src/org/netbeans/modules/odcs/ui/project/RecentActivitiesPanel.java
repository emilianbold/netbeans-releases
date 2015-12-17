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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import oracle.clouddev.server.profile.activity.client.api.Activity;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.api.ODCSUiServer;
import org.netbeans.modules.odcs.ui.project.activity.ActivityTypes;
import org.netbeans.modules.odcs.ui.utils.Utils;
import org.netbeans.modules.team.server.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author jpeska
 */
public class RecentActivitiesPanel extends javax.swing.JPanel implements ActivityTypes {

    private static final RequestProcessor RP = new RequestProcessor(RecentActivitiesPanel.class);
    private final ProjectHandle<ODCSProject> projectHandle;
    private final ODCSClient client;
    private JCheckBox chbTask;
    private JCheckBox chbWiki;
    private JCheckBox chbBuilds;
    private JCheckBox chbReviews;
    private JCheckBox chbScm;
    private JCheckBox chbRss;
    private JCheckBox chbDeploy;
    private List<Activity> activities = Collections.emptyList();
    private final Map<Activity, ActivityPanel> activity2Panel = new HashMap<>();
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
                showWaitCursor(true);
                try {
                    List<Activity> activities = client.getRecentActivities(projectHandle.getTeamProject().getId());
                    RecentActivitiesPanel.this.activities = activities != null ? activities : Collections.EMPTY_LIST;
                    showSelectedActivities();
                } catch (ODCSException ex) {
                    Utils.logException(ex, true);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showError();
                        }
                    });
                } finally {
                    showWaitCursor(false);
                }
            }
        });
    }

    private void showSelectedActivities() {
        final Collection<Activity> selectedActivities = getShowableActivities(activities);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!selectedActivities.isEmpty()) {
                    showActivities(selectedActivities);
                } else {
                    showEmptyContent();
                }
            }
        });
    }

    private Collection<Activity> getShowableActivities(List<Activity> activities) {
        List<Activity> ret = new LinkedList<>();
        if (activities != null) {
            for (Activity a : activities) {
                if (isActivityShowable(a)) {
                    ret.add(a);
                }
            }
        }
        return ret;
    }

    private boolean isActivityShowable(Activity activity) {
        String type = activity.getType();
        return (TASK.equals(type) && chbTask.isSelected())
                || (REVIEW.equals(type) && chbReviews.isSelected())
                || ((SCM_COMMIT.equals(type) || SCM_REPO.equals(type)) && chbScm.isSelected())
                || (WIKI.equals(type) && chbWiki.isSelected())
                || (RSS.equals(type) && chbRss.isSelected())
                || (BUILD.equals(type) && chbBuilds.isSelected())
                || (DEPLOYMENT.equals(type) && chbDeploy.isSelected());
    }

    private void createShowButtons() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridheight = GridBagConstraints.REMAINDER;

        chbScm = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Commits"), Utils.Settings.isShowScm()); // NOI18N
        chbScm.setOpaque(false);
        chbScm.addActionListener(new ShowActionListener(SCM_COMMIT, SCM_REPO));
        pnlShow.add(chbScm, gbc);
        
        chbReviews = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Reviews"), Utils.Settings.isShowReviews()); // NOI18N
        chbReviews.setOpaque(false);
        chbReviews.addActionListener(new ShowActionListener(REVIEW));
        pnlShow.add(chbReviews, gbc);

        chbTask = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Tasks"), Utils.Settings.isShowTasks()); // NOI18N
        chbTask.setOpaque(false);
        chbTask.addActionListener(new ShowActionListener(TASK));
        pnlShow.add(chbTask, gbc);

        chbWiki = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Wiki"), Utils.Settings.isShowWiki()); // NOI18N
        chbWiki.setOpaque(false);
        chbWiki.addActionListener(new ShowActionListener(WIKI));
        pnlShow.add(chbWiki, gbc);

        chbBuilds = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Builds"), Utils.Settings.isShowBuilds()); // NOI18N
        chbBuilds.setOpaque(false);
        chbBuilds.addActionListener(new ShowActionListener(BUILD));
        pnlShow.add(chbBuilds, gbc);

        chbDeploy =  new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Deployments"), Utils.Settings.isShowDeployments()); // NOI18N
        chbDeploy.setOpaque(false);
        chbDeploy.addActionListener(new ShowActionListener(DEPLOYMENT));
        pnlShow.add(chbDeploy, gbc);

        chbRss = new JCheckBox(NbBundle.getMessage(RecentActivitiesPanel.class, "LBL_Rss"), Utils.Settings.isShowRss()); // NOI18N
        chbRss.setOpaque(false);
        chbRss.addActionListener(new ShowActionListener(RSS));
        pnlShow.add(chbRss, gbc);

        pnlShow.revalidate();
    }

    private void showActivities(Collection<Activity> activities) {
        Date lastDate = null;
        DateSeparatorPanel currentDatePanel = null;
        pnlContent.removeAll();
        boolean isEmpty = true;
        RuntimeException ex = null;
        for (Activity activity : activities) {
            try {
                isEmpty = false;
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(0, 3, 0, 0);
                gbc.anchor = GridBagConstraints.NORTHWEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                Date currentDate = activity.getTimestamp();
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
            } catch (RuntimeException e) {
                // for robustness - working with external data the specific activity displayers may
                // fail - it is still a bug but we don't want to ruin the list of activities completely
                if (ex == null) {
                    ex = e;
                }
            }
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
        if (ex != null) {
            throw ex;
        }
    }

    private void showWaitCursor(final boolean showWait){
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
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
        });
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
        Utils.Settings.setShowBuilds(chbBuilds.isSelected());
        Utils.Settings.setShowReviews(chbReviews.isSelected());
        Utils.Settings.setShowScm(chbScm.isSelected());
        Utils.Settings.setShowTasks(chbTask.isSelected());
        Utils.Settings.setShowWiki(chbWiki.isSelected());
        Utils.Settings.setShowRss(chbRss.isSelected());
        Utils.Settings.setShowDeployments(chbDeploy.isSelected());
    }

    private class ShowActionListener implements ActionListener {
        private final String[] types;

        ShowActionListener(String... types) {
            this.types = types;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            persistShowSettings();
            if (containsActivity(types)) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        showWaitCursor(true);
                        try {
                            showSelectedActivities();
                        } finally {
                            showWaitCursor(false);
                        }    
                    }
                });
            }
        }
    }

    private boolean containsActivity(String[] types) {
        for (Activity activity : activities) {
            for (String type : types) {
                if (type.equalsIgnoreCase(activity.getType())) {
                    return true;
                }
            }
        }
        return false;
    }
}
