/*
 * DO NOT ALTER OR REMOVE

 @Override
 public void mouseClicked(MouseEvent e) {
 throw new UnsupportedOperationException("Not supported yet.");
 }
 }
 @Override
 public void mouseClicked(MouseEvent e) {
 throw new UnsupportedOperationException("Not supported yet.");
 }
 }COPYRIGHT NOTICES OR THIS HEADER.
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
import com.tasktop.c2c.server.scm.domain.ScmRepository;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSFactory;
import org.netbeans.modules.odcs.client.api.ODCSClient;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.settings.OdcsSettings;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.netbeans.modules.odcs.ui.project//ProjectDetails//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "ProjectDetailsTopComponent",
iconBase = "org/netbeans/modules/odcs/ui/resources/odcs.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ProjectDetailsAction",
preferredID = "ProjectDetailsTopComponent")
@Messages({
    "CTL_ProjectDetailsAction=ProjectDetails",
    "CTL_ProjectDetailsTopComponent=ProjectDetails Window",
    "HINT_ProjectDetailsTopComponent=This is a ProjectDetails window"
})
public final class ProjectDetailsTopComponent extends TopComponent implements Expandable, PropertyChangeListener {

    private static Map<String, ProjectDetailsTopComponent> projectToTC = new HashMap<String, ProjectDetailsTopComponent>();
    private boolean detailsExpanded;
    private ExpandableMouseListener expandMouseListener;
    private final Color borderColor = new java.awt.Color(150, 150, 150);
    private ProjectHandle<ODCSProject> projectHandle;
    private ODCSProject project = null;
    private ODCSClient client = null;
    private BuildStatusPanel buildStatusPanel;
    private static final RequestProcessor RP = new RequestProcessor(ProjectDetailsTopComponent.class.getName());
    private DashboardRefresher refresher;
    private RecentActivitiesPanel activitiesPanel;

    static ProjectDetailsTopComponent getInstanceFor(ProjectHandle<ODCSProject> projectHandle) {
        ODCSProject project = projectHandle.getTeamProject();
        if (projectToTC.containsKey(project.getId())) {
            return projectToTC.get(project.getId());
        }
        final ProjectDetailsTopComponent newInstance = new ProjectDetailsTopComponent(projectHandle);
        projectToTC.put(project.getId(), newInstance);
        return newInstance;
    }

    static ProjectDetailsTopComponent findInstance(String projectId) {
        if (projectToTC.containsKey(projectId)) {
            return projectToTC.get(projectId);
        } else {
            return null;
        }
    }

    public ProjectDetailsTopComponent() {
        initComponents();
    }

    private ProjectDetailsTopComponent(ProjectHandle<ODCSProject> projectHandle) {
        this.projectHandle = projectHandle;
        this.project = projectHandle.getTeamProject();
        this.refresher = new DashboardRefresher();
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblError = new javax.swing.JLabel();
        pnlContent = new javax.swing.JPanel();
        pnlProjectName = new TitlePanel();
        lblProjectName = new javax.swing.JLabel();
        lblExpandIcon = new javax.swing.JLabel();
        scrollPanelMain = new javax.swing.JScrollPane();
        pnlMainContent = new javax.swing.JPanel();
        scrollPanelDetails = new javax.swing.JScrollPane();
        pnlDetails = new ScrollablePanel();
        lblDescription = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        pnlLinksMaven = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        linkProject = new LinkLabel(true) {
            public void mouseClicked(MouseEvent e) {
                openUrl(this.getText());
            }
        };
        linkWiki = new LinkLabel(true) {
            public void mouseClicked(MouseEvent e) {
                if (isEnabled()) {
                    openUrl(this.getText());
                }
            }
        };
        pnlMaven = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        textMaven = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        textArtifacts = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        pnlSources = new javax.swing.JPanel();
        lblSources = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        lblError.setForeground(new java.awt.Color(255, 0, 0));
        lblError.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/odcs/ui/resources/error.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblError, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.lblError.text")); // NOI18N

        setBackground(java.awt.Color.white);

        pnlContent.setBackground(getBackgroundColor());
        pnlContent.setRequestFocusEnabled(false);
        pnlContent.setLayout(new java.awt.GridBagLayout());

        pnlProjectName.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, borderColor));

        lblProjectName.setFont(lblProjectName.getFont().deriveFont(lblProjectName.getFont().getSize()+9f));
        org.openide.awt.Mnemonics.setLocalizedText(lblProjectName, project.getName() + " - " +  NbBundle.getMessage(ProjectDetailsTopComponent.class, "LBL_Dashboard"));

        lblExpandIcon.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblExpandIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/odcs/ui/resources/arrow-down.png"))); // NOI18N
        lblExpandIcon.setEnabled(false);

        javax.swing.GroupLayout pnlProjectNameLayout = new javax.swing.GroupLayout(pnlProjectName);
        pnlProjectName.setLayout(pnlProjectNameLayout);
        pnlProjectNameLayout.setHorizontalGroup(
            pnlProjectNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProjectNameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProjectName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 702, Short.MAX_VALUE)
                .addComponent(lblExpandIcon)
                .addContainerGap())
        );
        pnlProjectNameLayout.setVerticalGroup(
            pnlProjectNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProjectNameLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(pnlProjectNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblExpandIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProjectName))
                .addGap(3, 3, 3))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        pnlContent.add(pnlProjectName, gridBagConstraints);

        scrollPanelMain.setBorder(null);
        scrollPanelMain.setOpaque(false);

        pnlMainContent.setBackground(new java.awt.Color(255, 255, 255));
        pnlMainContent.setLayout(new java.awt.GridBagLayout());
        scrollPanelMain.setViewportView(pnlMainContent);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlContent.add(scrollPanelMain, gridBagConstraints);

        scrollPanelDetails.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, borderColor));
        scrollPanelDetails.setOpaque(false);

        pnlDetails.setBackground(new java.awt.Color(255, 255, 255));
        pnlDetails.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblDescription, "<html>" + project.getDescription() + "</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(14, 29, 0, 0);
        pnlDetails.add(lblDescription, gridBagConstraints);

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new java.awt.GridBagLayout());

        pnlLinksMaven.setOpaque(false);
        pnlLinksMaven.setLayout(new java.awt.GridBagLayout());

        jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getStyle() | java.awt.Font.BOLD, jLabel2.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        pnlLinksMaven.add(jLabel2, gridBagConstraints);

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | java.awt.Font.BOLD, jLabel1.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlLinksMaven.add(jLabel1, gridBagConstraints);

        linkProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/odcs/ui/resources/link.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 18, 0, 0);
        pnlLinksMaven.add(linkProject, gridBagConstraints);

        linkWiki.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/odcs/ui/resources/link.png"))); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 18, 0, 0);
        pnlLinksMaven.add(linkWiki, gridBagConstraints);

        pnlMaven.setOpaque(false);
        pnlMaven.setLayout(new java.awt.GridBagLayout());

        jLabel4.setFont(jLabel4.getFont().deriveFont(jLabel4.getFont().getStyle() | java.awt.Font.BOLD, jLabel4.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        pnlMaven.add(jLabel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel5.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pnlMaven.add(jLabel5, gridBagConstraints);

        textMaven.setEditable(false);
        textMaven.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pnlMaven.add(textMaven, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel6.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        pnlMaven.add(jLabel6, gridBagConstraints);

        textArtifacts.setEditable(false);
        textArtifacts.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 11, 0);
        pnlMaven.add(textArtifacts, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        pnlMaven.add(jLabel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        pnlLinksMaven.add(pnlMaven, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(pnlLinksMaven, gridBagConstraints);

        pnlSources.setOpaque(false);
        pnlSources.setLayout(new java.awt.GridBagLayout());

        lblSources.setFont(lblSources.getFont().deriveFont(lblSources.getFont().getStyle() | java.awt.Font.BOLD, lblSources.getFont().getSize()+2));
        org.openide.awt.Mnemonics.setLocalizedText(lblSources, NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.lblSources.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        pnlSources.add(lblSources, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 0);
        pnlSources.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(pnlSources, gridBagConstraints);

        jSeparator1.setForeground(new java.awt.Color(180, 180, 180));
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setToolTipText(NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jSeparator1.toolTipText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        jPanel2.add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 29, 10, 10);
        pnlDetails.add(jPanel2, gridBagConstraints);

        scrollPanelDetails.setViewportView(pnlDetails);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.6;
        pnlContent.add(scrollPanelDetails, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblExpandIcon;
    private javax.swing.JLabel lblProjectName;
    private javax.swing.JLabel lblSources;
    private javax.swing.JLabel linkProject;
    private javax.swing.JLabel linkWiki;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlDetails;
    private javax.swing.JPanel pnlLinksMaven;
    private javax.swing.JPanel pnlMainContent;
    private javax.swing.JPanel pnlMaven;
    private javax.swing.JPanel pnlProjectName;
    private javax.swing.JPanel pnlSources;
    private javax.swing.JScrollPane scrollPanelDetails;
    private javax.swing.JScrollPane scrollPanelMain;
    private javax.swing.JTextField textArtifacts;
    private javax.swing.JTextField textMaven;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        refresher.setRefreshEnabled(true);
        refresher.setupDashboardRefresh();
        setName(project.getName());
        scrollPanelMain.getVerticalScrollBar().setUnitIncrement(16);
        scrollPanelDetails.getVerticalScrollBar().setUnitIncrement(16);
        detailsExpanded = false;
        scrollPanelDetails.setVisible(false);
        expandMouseListener = new ExpandableMouseListener(this, this);
        pnlProjectName.addMouseListener(expandMouseListener);
        OdcsSettings.getInstance().addPropertyChangedListener(this);
        client = ODCSFactory.getInstance().createClient(project.getServer().getUrl().toString(), project.getServer().getPasswordAuthentication());
        initDetails();
        loadRecentActivities();
        loadBuildStatus();
    }

    @Override
    public void componentClosed() {
        refresher.setRefreshEnabled(false);
        if (project != null) {
            projectToTC.remove(project.getId());
        }
        buildStatusPanel.removeBuildListeners();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private void openUrl(String url) {
        Utils.openBrowser(url);
    }

    private Color getBackgroundColor() {
        return UIManager.getDefaults().getColor("TextArea.background");
    }

    private void loadBuildStatus() {
        buildStatusPanel = new BuildStatusPanel(projectHandle);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 0, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        pnlMainContent.add(buildStatusPanel, gbc);
    }

    private void loadRecentActivities() {
        activitiesPanel = new RecentActivitiesPanel(client, projectHandle);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 0, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 2.5;
        gbc.weighty = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        pnlMainContent.add(activitiesPanel, gbc);
    }

    public ODCSProject getProject() {
        return project;
    }

    @Override
    public void toggleExpandablePanel() {
        scrollPanelDetails.setVisible(!detailsExpanded);
        detailsExpanded = !detailsExpanded;
        lblExpandIcon.setIcon(detailsExpanded ? Utils.COLLAPSE_ICON : Utils.EXPAND_ICON);
        pnlProjectName.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, detailsExpanded ? 0 : 1, 1, borderColor));
    }

    @Override
    public void mouseEnteredExpandable() {
        ProjectDetailsTopComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblProjectName.setForeground(Color.BLUE);
        lblExpandIcon.setEnabled(true);
    }

    @Override
    public void mouseExitedExpandable() {
        ProjectDetailsTopComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        lblProjectName.setForeground(Color.BLACK);
        lblExpandIcon.setEnabled(false);
    }

    @Override
    public void revalidateExpandable() {
        pnlContent.revalidate();
    }

    @NbBundle.Messages("LBL_Service_Not_Enabled=Not enabled")
    private void initDetails() {
        linkProject.setText(project.getWebUrl());
        if (project.hasWiki()) {
            linkWiki.setText(Utils.getRealUrl(project.getWikiUrl()));
        } else {
            linkWiki.setText(Bundle.LBL_Service_Not_Enabled());
            linkWiki.setEnabled(false);
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 0, 0);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        Collection<ScmRepository> repositories = Collections.emptyList();
        try {
            if (project.hasScm()) {
                repositories = project.getRepositories();
            }
        } catch (ODCSException ex) {
            Utils.getLogger().warning(ex.getLocalizedMessage());
            pnlSources.add(lblError, gbc);
        }
        for (ScmRepository repository : repositories) {
            List<String> list = new ArrayList<String>();
            list.add(repository.getUrl());
            String alternateUrl = repository.getAlternateUrl();
            if (alternateUrl != null && !alternateUrl.isEmpty()) {
                list.add(alternateUrl);
            }
            pnlSources.add(new RepositoryPanel(repository.getName(), list), gbc);
        }

        textMaven.setText(project.getMavenUrl());
        textArtifacts.setText("dav:" + project.getMavenUrl());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OdcsSettings.AUTO_SYNC_SETTINGS_CHANGED)) {
            refresher.setupDashboardRefresh();
        }
    }

    private void updateContent() {
        activitiesPanel.update();
        //TODO update project details
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return this.getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private class DashboardRefresher {

        private RequestProcessor.Task refreshDashboard;
        private boolean refreshEnabled = false;
        private boolean dashboardBusy = false;
        private boolean refreshWaiting = false;

        public DashboardRefresher() {
            refreshDashboard = RP.create(new Runnable() {
                @Override
                public void run() {
                    if (!refreshEnabled) {
                        return;
                    }
                    if (dashboardBusy) {
                        refreshWaiting = true;
                        return;
                    }
                    try {
                        updateContent();
                    } finally {
                        setupDashboardRefresh();
                    }
                }
            });
        }

        public void setupDashboardRefresh() {
            final OdcsSettings settings = OdcsSettings.getInstance();
            refreshDashboard.cancel();
            if (!settings.isAutoSync() || !refreshEnabled) {
                return;
            }
            scheduleDashboardRefresh();
        }

        private void scheduleDashboardRefresh() {
            final OdcsSettings settings = OdcsSettings.getInstance();
            int delay = settings.getAutoSyncValue();
            refreshDashboard.schedule(delay * 60 * 1000); // given in minutes
        }

        public void setRefreshEnabled(boolean refreshEnabled) {
            this.refreshEnabled = refreshEnabled;
        }

        public void setDashboardBusy(boolean dashboardBusy) {
            this.dashboardBusy = dashboardBusy;
            if (!dashboardBusy && refreshWaiting) {
                refreshWaiting = false;
                refreshDashboard.run();
            }
        }
    }
}
