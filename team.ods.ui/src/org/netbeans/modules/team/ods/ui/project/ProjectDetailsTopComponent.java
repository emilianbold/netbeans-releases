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
package org.netbeans.modules.team.ods.ui.project;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.team.c2c.api.ODSProject;
import org.netbeans.modules.team.c2c.client.api.ClientFactory;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.netbeans.modules.team.ods.ui.project//ProjectDetails//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "ProjectDetailsTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "org.netbeans.modules.team.ods.ui.project.ProjectDetailsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ProjectDetailsAction",
preferredID = "ProjectDetailsTopComponent")
@Messages({
    "CTL_ProjectDetailsAction=ProjectDetails",
    "CTL_ProjectDetailsTopComponent=ProjectDetails Window",
    "HINT_ProjectDetailsTopComponent=This is a ProjectDetails window"
})
public final class ProjectDetailsTopComponent extends TopComponent {

    private static Map<String, ProjectDetailsTopComponent> projectToTC = new HashMap<String, ProjectDetailsTopComponent>();
    static final Logger LOG = Logger.getLogger(ProjectDetailsTopComponent.class.toString()); // NOI18N
    private boolean detailsExpanded;
    private ExpandMouseListener expandMouseListener;
    private ImageIcon expandIcon;
    private ImageIcon collapseIcon;
    private final Color borderColor = new java.awt.Color(150, 150, 150);
    private ODSProject project = null;
    private CloudClient client = null;

    static ProjectDetailsTopComponent getInstanceFor(ODSProject project) {
        if (projectToTC.containsKey(project.getId())) {
            return projectToTC.get(project.getId());
        }
        final ProjectDetailsTopComponent newInstance = new ProjectDetailsTopComponent(project);
        projectToTC.put(project.getId(), newInstance);
        return newInstance;
    }

    public ProjectDetailsTopComponent() {
        init();
    }

    private ProjectDetailsTopComponent(ODSProject project) {
        this.project = project;
        init();
    }

    private void init() {
        if (project != null) {
            setName(project.getName());
            initComponents();
            expandIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/team/ods/ui/resources/arrow-down.png", true);
            collapseIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/team/ods/ui/resources/arrow-up.png", true);
            detailsExpanded = false;
            pnlDetails.setVisible(false);
            lblExpandIcon.setVisible(false);
            lblExpandIcon.setIcon(expandIcon);
            expandMouseListener = new ExpandMouseListener();
            lblProjectName.addMouseListener(expandMouseListener);
            client = ClientFactory.getInstance().createClient(project.getServer().getUrl().toString(), project.getServer().getPasswordAuthentication());
            loadBuildStatus();
            loadRecentActivities();
        } else {
            setName(Bundle.CTL_ProjectDetailsTopComponent());
            this.setLayout(new GridBagLayout());
            this.add(new JLabel(NbBundle.getMessage(ProjectDetailsTopComponent.class, "LBL_NoProject")), new GridBagConstraints());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlContent = new javax.swing.JPanel();
        pnlProjectName = new TitlePanel();
        lblProjectName = new javax.swing.JLabel();
        lblExpandIcon = new javax.swing.JLabel();
        pnlDetails = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new LinkLabel() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl(getText());
            }
        };
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new LinkLabel() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl(getText());
            }
        };
        jLabel12 = new LinkLabel() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl(getText());
            }
        };
        jLabel13 = new LinkLabel() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl(getText());
            }
        };
        jLabel2 = new javax.swing.JLabel();
        pnlMainContent = new javax.swing.JPanel();

        setBackground(java.awt.Color.white);

        pnlContent.setBackground(getBackgroundColor());
        pnlContent.setRequestFocusEnabled(false);
        pnlContent.setLayout(new java.awt.GridBagLayout());

        pnlProjectName.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, 1, 1, borderColor));

        lblProjectName.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblProjectName, project.getName() + " - " +  NbBundle.getMessage(ProjectDetailsTopComponent.class, "LBL_Details"));

        lblExpandIcon.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblExpandIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/team/ods/ui/resources/arrow-down.png"))); // NOI18N

        javax.swing.GroupLayout pnlProjectNameLayout = new javax.swing.GroupLayout(pnlProjectName);
        pnlProjectName.setLayout(pnlProjectNameLayout);
        pnlProjectNameLayout.setHorizontalGroup(
            pnlProjectNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProjectNameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProjectName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblExpandIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(765, Short.MAX_VALUE))
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
        gridBagConstraints.ipadx = 588;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        pnlContent.add(pnlProjectName, gridBagConstraints);

        pnlDetails.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1, borderColor));
        pnlDetails.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel4.text")); // NOI18N

        jLabel6.setForeground(new java.awt.Color(0, 51, 204));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel7.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel9, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel9.text")); // NOI18N

        jLabel11.setForeground(new java.awt.Color(0, 51, 204));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel11.text")); // NOI18N

        jLabel12.setForeground(new java.awt.Color(0, 51, 204));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel12.text")); // NOI18N

        jLabel13.setForeground(new java.awt.Color(0, 51, 204));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel13, org.openide.util.NbBundle.getMessage(ProjectDetailsTopComponent.class, "ProjectDetailsTopComponent.jLabel13.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, project.getDescription());

        javax.swing.GroupLayout pnlDetailsLayout = new javax.swing.GroupLayout(pnlDetails);
        pnlDetails.setLayout(pnlDetailsLayout);
        pnlDetailsLayout.setHorizontalGroup(
            pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailsLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDetailsLayout.createSequentialGroup()
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel7))
                        .addGap(59, 59, 59)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel12))
                        .addGap(176, 176, 176)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel9))
                        .addGap(47, 47, 47)
                        .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel11))
                        .addGap(0, 221, Short.MAX_VALUE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlDetailsLayout.setVerticalGroup(
            pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9)
                    .addComponent(jLabel11)
                    .addComponent(jLabel13))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 662;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        pnlContent.add(pnlDetails, gridBagConstraints);

        pnlMainContent.setOpaque(false);
        pnlMainContent.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 728;
        gridBagConstraints.ipady = 557;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        pnlContent.add(pnlMainContent, gridBagConstraints);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContent, javax.swing.GroupLayout.PREFERRED_SIZE, 940, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlContent, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel lblExpandIcon;
    private javax.swing.JLabel lblProjectName;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlDetails;
    private javax.swing.JPanel pnlMainContent;
    private javax.swing.JPanel pnlProjectName;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        projectToTC.remove(project.getId());
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
    }

    private Color getBackgroundColor() {
        return UIManager.getDefaults().getColor("TextArea.background");
    }

    private void loadBuildStatus() {
        BuildStatusPanel buildStatusPanel = new BuildStatusPanel(client, project.getId());
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
        RecentActivitiesPanel activityPanel = new RecentActivitiesPanel(client, project.getId());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 0, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 2.5;
        gbc.weighty = 1.0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        pnlMainContent.add(activityPanel, gbc);
    }

    private void toggleDetails() {
        pnlDetails.setVisible(!detailsExpanded);
        detailsExpanded = !detailsExpanded;
        lblExpandIcon.setIcon(detailsExpanded ? collapseIcon : expandIcon);
        pnlProjectName.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 1, detailsExpanded ? 0 : 1, 1, borderColor));
        pnlContent.revalidate();
        this.repaint();
    }

    private class ExpandMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
            toggleDetails();
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            ProjectDetailsTopComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblProjectName.setForeground(Color.BLUE);
            lblExpandIcon.setVisible(true);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            ProjectDetailsTopComponent.this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            lblProjectName.setForeground(Color.BLACK);
            lblExpandIcon.setVisible(false);
        }
    }
}
