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

import org.netbeans.modules.odcs.ui.utils.Utils;
import com.tasktop.c2c.server.profile.domain.activity.*;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.ui.project.activity.ActivityDisplayer;
import org.netbeans.modules.odcs.ui.project.activity.BuildActivityDisplayer;
import org.netbeans.modules.odcs.ui.project.activity.ScmActivityDisplayer;
import org.netbeans.modules.odcs.ui.project.activity.TaskActivityDisplayer;
import org.netbeans.modules.odcs.ui.project.activity.WikiActivityDisplayer;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;

/**
 *
 * @author jpeska
 */
public class ActivityPanel extends javax.swing.JPanel implements Expandable {

    private static final Border NORMAL_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153));
    private static final Border EXPAND_BORDER = BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 255));
    private boolean detailsExpanded;
    private ActivityDisplayer activityAccessor;

    /**
     * Creates new form ActivityPanel
     */
    public ActivityPanel(ProjectActivity activity, ProjectHandle<ODCSProject> projectHandle, int maxWidth) {
        if (activity instanceof TaskActivity) {
            activityAccessor = new TaskActivityDisplayer((TaskActivity) activity, projectHandle, maxWidth);
        } else if (activity instanceof BuildActivity) {
            activityAccessor = new BuildActivityDisplayer((BuildActivity) activity, projectHandle, maxWidth);
        } else if (activity instanceof ScmActivity) {
            ProjectDetailsTopComponent tc = ProjectDetailsTopComponent.findInstance(activity.getProjectIdentifier());
            activityAccessor = new ScmActivityDisplayer((ScmActivity) activity, projectHandle, tc.getProject().getScmUrl(), maxWidth);
        } else if (activity instanceof WikiActivity) {
            activityAccessor = new WikiActivityDisplayer((WikiActivity) activity, maxWidth);
        }
        initComponents();
        lblIcon.setIcon(activityAccessor.getActivityIcon());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        lblTime.setText(dateFormat.format(activityAccessor.getActivityDate()));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 3);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        final JComponent titleComponent = activityAccessor.getTitleComponent();
        titleComponent.setOpaque(false);
        pnlTitle.add(titleComponent, gbc);

        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        pnlTitle.add(new JLabel(), gbc);

        gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        final JComponent shortDescriptionComponent = activityAccessor.getShortDescriptionComponent();
        shortDescriptionComponent.setOpaque(false);
        pnlShortDesc.add(shortDescriptionComponent, gbc);

        JComponent detailsComponent = activityAccessor.getDetailsComponent();
        if (detailsComponent != null) {
            pnlDetails.removeAll();
            gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            pnlDetails.add(detailsComponent, gbc);
        } else {
            lblExpand.setVisible(false);
        }
        detailsExpanded = false;
        pnlDetails.setVisible(false);
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

        pnlDetails = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblIcon = new javax.swing.JLabel();
        pnlTitle = new javax.swing.JPanel();
        lblTime = new javax.swing.JLabel();
        pnlShortDesc = new javax.swing.JPanel();
        lblExpand = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        setOpaque(false);

        pnlDetails.setOpaque(false);
        pnlDetails.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ActivityPanel.class, "ActivityPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 75, 366);
        pnlDetails.add(jLabel1, gridBagConstraints);

        jPanel1.setOpaque(false);

        lblIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/odcs/ui/resources/activity_unknown.png"))); // NOI18N

        pnlTitle.setOpaque(false);
        pnlTitle.setLayout(new java.awt.GridBagLayout());

        lblTime.setForeground(new java.awt.Color(153, 153, 153));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pnlTitle.add(lblTime, gridBagConstraints);

        pnlShortDesc.setOpaque(false);
        pnlShortDesc.setLayout(new java.awt.GridBagLayout());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(lblIcon)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlShortDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblIcon)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(pnlTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlShortDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(5, 5, 5))
        );

        lblExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/odcs/ui/resources/arrow-down.png"))); // NOI18N
        lblExpand.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        lblExpand.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblExpand)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pnlDetails, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(39, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblExpand, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblExpand;
    private javax.swing.JLabel lblIcon;
    private javax.swing.JLabel lblTime;
    private javax.swing.JPanel pnlDetails;
    private javax.swing.JPanel pnlShortDesc;
    private javax.swing.JPanel pnlTitle;
    // End of variables declaration//GEN-END:variables

    @Override
    public void toggleExpandablePanel() {
        pnlDetails.setVisible(!detailsExpanded);
        detailsExpanded = !detailsExpanded;
        lblExpand.setIcon(detailsExpanded ? Utils.COLLAPSE_ICON : Utils.EXPAND_ICON);
    }

    @Override
    public void mouseEnteredExpandable() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.setBorder(EXPAND_BORDER);
        lblExpand.setEnabled(true);
    }

    @Override
    public void mouseExitedExpandable() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        this.setBorder(NORMAL_BORDER);
        lblExpand.setEnabled(false);
    }

    @Override
    public void revalidateExpandable() {
        this.revalidate();
    }

    boolean hasDetails() {
        return lblExpand.isVisible();
    }
}
