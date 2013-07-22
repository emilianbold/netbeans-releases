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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.SessionContent;
import org.netbeans.modules.maven.j2ee.utils.LoggingUtils;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;
import org.netbeans.modules.maven.j2ee.utils.Server;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class SelectAppServerPanel extends javax.swing.JPanel {

    private NotificationLineSupport nls;
    private Project project;


    private SelectAppServerPanel(boolean showIgnore, Project project) {
        this.project = project;
        initComponents();
        buttonGroup1.add(rbSession);
        buttonGroup1.add(rbPermanent);
        loadComboModel();
        if (showIgnore) {
            buttonGroup1.add(rbIgnore);
            checkIgnoreEnablement();
            comServer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkIgnoreEnablement();
                }
            });
            rbIgnore.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    printIgnoreWarning();
                }

            });
        } else {
            rbIgnore.setVisible(false);
        }
        updateProjectLbl();
        rbPermanentStateChanged(null);
    }

    public static boolean showServerSelectionDialog(Project project, J2eeModuleProvider provider, RunConfig config) {
        if (ExecutionChecker.DEV_NULL.equals(provider.getServerInstanceID())) {
            boolean isDefaultGoal = config == null ? true : neitherJettyNorCargo(config.getGoals()); //TODO how to figure if really default or overridden by user?
            SelectAppServerPanel panel = new SelectAppServerPanel(!isDefaultGoal, project);
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(ExecutionChecker.class, "TIT_Select"));
            panel.setNLS(dd.createNotificationLineSupport());
            Object obj = DialogDisplayer.getDefault().notify(dd);
            if (obj == NotifyDescriptor.OK_OPTION) {
                String instanceId = panel.getSelectedServerInstance();
                String serverId = panel.getSelectedServerType();
                if (!ExecutionChecker.DEV_NULL.equals(instanceId)) {
                    boolean permanent = panel.isPermanent();
                    if (permanent) {
                        persistServer(project, instanceId, serverId, panel.getChosenProject());
                    } else {
                        SessionContent sc = project.getLookup().lookup(SessionContent.class);
                        if (sc != null) {
                            sc.setServerInstanceId(instanceId);
                        }

                        // We want to initiate context path to default value if there isn't related deployment descriptor yet
                        MavenProjectSupport.changeServer(project, true);
                    }

                    LoggingUtils.logUsage(ExecutionChecker.class, "USG_PROJECT_CONFIG_MAVEN_SERVER", new Object[] { MavenProjectSupport.obtainServerName(project) }, "maven"); //NOI18N

                    return true;
                } else {
                    //ignored used now..
                    if (panel.isIgnored() && config != null) {
                        removeNetbeansDeployFromActionMappings(project, config.getActionName());
                        return true;
                    }
                }
            }
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExecutionChecker.class, "ERR_Action_without_deployment_server"));
            return false;
        }
        return true;
    }

    private static void removeNetbeansDeployFromActionMappings(Project project, String actionName) {
        try {
            ProjectConfiguration cfg = project.getLookup().lookup(ProjectConfigurationProvider.class).getActiveConfiguration();
            NetbeansActionMapping mapp = ModelHandle2.getMapping(actionName, project, cfg);
            if (mapp != null) {
                mapp.getProperties().remove(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY);
                ModelHandle2.putMapping(mapp, project, cfg);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static boolean neitherJettyNorCargo(List<String> goals) {
        for (String goal : goals) {
            if (goal.contains("jetty") || goal.contains("cargo")) {
                return false;
            }
        }
        return true;
    }

    private static void persistServer(Project project, final String iID, final String sID, final Project targetPrj) {
        MavenProjectSupport.setServerInstanceID(project, iID);
        MavenProjectSupport.setServerID(project, sID);

        // We want to initiate context path to default value if there isn't related deployment descriptor yet
        MavenProjectSupport.changeServer(project, true);

        // refresh all subprojects
        SubprojectProvider spp = targetPrj.getLookup().lookup(SubprojectProvider.class);
        //mkleint: we are assuming complete result (transitive projects included)
        //that's ok as far as the current maven impl goes afaik, but not according to the
        //documentation for SubProjectprovider
        Set<? extends Project> childrenProjs = spp.getSubprojects();
        if (!childrenProjs.contains(project)) {
            NbMavenProject.fireMavenProjectReload(project);
        }
        for (Project curPrj : childrenProjs) {
            NbMavenProject.fireMavenProjectReload(curPrj);
        }

    }

    private String getSelectedServerType() {
        return ((Server) comServer.getSelectedItem()).getServerID();
    }

    private String getSelectedServerInstance() {
        return ((Server) comServer.getSelectedItem()).getServerInstanceID();
    }

    private boolean isPermanent() {
        return rbPermanent.isSelected();
    }

    private boolean isIgnored() {
        return rbIgnore.isSelected();
    }

    private Project getChosenProject() {
        return project;
    }

    private void loadComboModel() {
        Ear ear = Ear.getEar(project.getProjectDirectory());
        EjbJar ejb = EjbJar.getEjbJar(project.getProjectDirectory());
        WebModule war = WebModule.getWebModule(project.getProjectDirectory());
        J2eeModule.Type type = ear != null ? J2eeModule.Type.EAR :
                                     ( war != null ? J2eeModule.Type.WAR :
                                           (ejb != null ? J2eeModule.Type.EJB : J2eeModule.Type.CAR));
        Profile profile = ear != null ? ear.getJ2eeProfile() :
                                     ( war != null ? war.getJ2eeProfile() :
                                           (ejb != null ? ejb.getJ2eeProfile() : Profile.JAVA_EE_6_FULL));
        String[] ids = Deployment.getDefault().getServerInstanceIDs(Collections.singletonList(type), profile);
        Collection<Server> col = new ArrayList<Server>();
        col.add(new Server(ExecutionChecker.DEV_NULL));

        for (int i = 0; i < ids.length; i++) {
            Server wr = new Server(ids[i]);
            col.add(wr);
        }
        comServer.setModel(new DefaultComboBoxModel(col.toArray()));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblServer = new javax.swing.JLabel();
        comServer = new javax.swing.JComboBox();
        rbSession = new javax.swing.JRadioButton();
        rbPermanent = new javax.swing.JRadioButton();
        rbIgnore = new javax.swing.JRadioButton();
        lblProject = new javax.swing.JLabel();
        btChange = new javax.swing.JButton();

        lblServer.setLabelFor(comServer);
        org.openide.awt.Mnemonics.setLocalizedText(lblServer, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.lblServer.text")); // NOI18N

        rbSession.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbSession, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.rbSession.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(rbPermanent, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.rbPermanent.text")); // NOI18N
        rbPermanent.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbPermanentStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(rbIgnore, org.openide.util.NbBundle.getBundle(SelectAppServerPanel.class).getString("SelectAppServerPanel.rbIgnore.text")); // NOI18N

        lblProject.setFont(lblProject.getFont().deriveFont(lblProject.getFont().getSize()-1f));
        org.openide.awt.Mnemonics.setLocalizedText(lblProject, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.lblProject.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btChange, org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.btChange.text")); // NOI18N
        btChange.setToolTipText(org.openide.util.NbBundle.getMessage(SelectAppServerPanel.class, "SelectAppServerPanel.btChange.toolTipText")); // NOI18N
        btChange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btChangeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(lblProject))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblServer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comServer, 0, 400, Short.MAX_VALUE))
                    .addComponent(rbSession, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(rbPermanent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 168, Short.MAX_VALUE)
                        .addComponent(btChange))
                    .addComponent(rbIgnore, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblServer)
                    .addComponent(comServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(rbSession)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbPermanent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblProject))
                    .addComponent(btChange))
                .addGap(18, 18, 18)
                .addComponent(rbIgnore)
                .addContainerGap(20, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbPermanentStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbPermanentStateChanged
        boolean isSel = rbPermanent.isSelected();
        btChange.setEnabled(isSel);
        lblProject.setEnabled(isSel);
        if (nls != null) {
            if (isSel) {
                nls.setInformationMessage(NbBundle.getMessage(
                        SelectAppServerPanel.class, "MSG_ParentHint"));
            } else {
                nls.clearMessages();
            }
        }
    }//GEN-LAST:event_rbPermanentStateChanged

    private void btChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btChangeActionPerformed
        SelectProjectPanel spp = new SelectProjectPanel(project);
        final DialogDescriptor dd = new DialogDescriptor(spp, NbBundle.getMessage(SelectAppServerPanel.class, "TIT_ChooseParent"));
        spp.attachDD(dd);

        Object obj = DialogDisplayer.getDefault().notify(dd);
        if (obj == NotifyDescriptor.OK_OPTION) {
            project = spp.getSelectedProject();
            updateProjectLbl();
        }

    }//GEN-LAST:event_btChangeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btChange;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox comServer;
    private javax.swing.JLabel lblProject;
    private javax.swing.JLabel lblServer;
    javax.swing.JRadioButton rbIgnore;
    javax.swing.JRadioButton rbPermanent;
    javax.swing.JRadioButton rbSession;
    // End of variables declaration//GEN-END:variables

    private void checkIgnoreEnablement() {
        if (ExecutionChecker.DEV_NULL.equals(getSelectedServerType())) {
            rbIgnore.setEnabled(true);
        } else {
            if (rbIgnore.isSelected()) {
                rbSession.setSelected(true);
            }
            rbIgnore.setEnabled(false);
        }
    }

    private void setNLS(NotificationLineSupport notif) {
        nls = notif;
    }

    private void printIgnoreWarning() {
        if (rbIgnore.isSelected()) {
            nls.setWarningMessage(NbBundle.getMessage(SelectAppServerPanel.class, "WARN_Ignore_Server"));
        } else {
            nls.clearMessages();
        }
    }

    private void updateProjectLbl() {
        ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
        if (pi != null) {
            lblProject.setText(NbBundle.getMessage(SelectAppServerPanel.class,
                    "MSG_InProject", pi.getDisplayName()));
        }
    }

}
