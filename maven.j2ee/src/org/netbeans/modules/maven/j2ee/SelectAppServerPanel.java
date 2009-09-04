/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class SelectAppServerPanel extends javax.swing.JPanel {
    private NotificationLineSupport nls;
    private Project project;

    /** Creates new form SelectAppServerPanel */
    public SelectAppServerPanel(boolean showIgnore, Project project) {
        this.project = project;
        initComponents();
        buttonGroup1.add(rbSession);
        buttonGroup1.add(rbPermanent);
        loadComboModel();
        if (showIgnore) {
            buttonGroup1.add(rbIgnore);
            checkIgnoreEnablement();
            comServer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    checkIgnoreEnablement();
                }
            });
            rbIgnore.addItemListener(new ItemListener() {
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

    String getSelectedServerType() {
        Wrapper wr = (Wrapper) comServer.getSelectedItem();
        return wr.getServerID();
    }

    String getSelectedServerInstance() {
        Wrapper wr = (Wrapper) comServer.getSelectedItem();
        return wr.getServerInstanceID();
    }

    boolean isPermanent() {
        return rbPermanent.isSelected();
    }

    boolean isIgnored() {
        return rbIgnore.isSelected();
    }

    Project getChosenProject() {
        return project;
    }

    private void loadComboModel() {
        String[] ids = Deployment.getDefault().getServerInstanceIDs();
        Collection<Wrapper> col = new ArrayList<Wrapper>();
//        Wrapper selected = null;
        col.add(new Wrapper(ExecutionChecker.DEV_NULL));
        for (int i = 0; i < ids.length; i++) {
            Wrapper wr = new Wrapper(ids[i]);
            col.add(wr);
//            if (selectedId.equals(ids[i])) {
//                selected = wr;
//            }

        }
        comServer.setModel(new DefaultComboBoxModel(col.toArray()));
//        if (selected != null) {
//            comServer.setSelectedItem(selected);
//        }
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(lblProject))
                    .add(layout.createSequentialGroup()
                        .add(lblServer)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(comServer, 0, 363, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, rbSession)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(rbPermanent)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 168, Short.MAX_VALUE)
                        .add(btChange))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, rbIgnore))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblServer)
                    .add(comServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(rbSession)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(rbPermanent)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lblProject))
                    .add(btChange))
                .add(18, 18, 18)
                .add(rbIgnore)
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
        /*JFileChooser fc = new JFileChooser(FileUtil.toFile(projDir));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setControlButtonsAreShown(false);*/
        SelectProjectPanel spp = new SelectProjectPanel(project);
        final DialogDescriptor dd = new DialogDescriptor(spp,
                NbBundle.getMessage(SelectAppServerPanel.class, "TIT_ChooseParent"));
        spp.attachDD(dd);
        /*fc.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propName) ||
                        JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(propName)) {
                    Object val = evt.getNewValue();
                    if (val instanceof File) {
                        FileObject curFO = FileUtil.toFileObject((File)val);
                        if (curFO != null) {
                            if (curFO.getFileObject("pom.xml") != null) {
                                fcNls.clearMessages();
                                dd.setValid(true);
                                return;
                            }
                        }
                    }
                    fcNls.setErrorMessage(NbBundle.getMessage(
                            SelectAppServerPanel.class, "ERR_NotMaven"));
                    dd.setValid(false);
                }
            }
        });*/

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
        Wrapper sel = (Wrapper) comServer.getSelectedItem();
        if (ExecutionChecker.DEV_NULL.equals(sel.getServerID())) {
            rbIgnore.setEnabled(true);
        } else {
            if (rbIgnore.isSelected()) {
                rbSession.setSelected(true);
            }
            rbIgnore.setEnabled(false);
        }
    }

    void setNLS(NotificationLineSupport notif) {
        nls = notif;
    }
    
    private void printIgnoreWarning() {
        if (rbIgnore.isSelected()) {
            nls.setWarningMessage(NbBundle.getMessage(SelectAppServerPanel.class, "WARN_Ignore_Server"));
        } else {
            nls.clearMessages();
        }
    }

    private void updateProjectLbl () {
        ProjectInformation pi = project.getLookup().lookup(ProjectInformation.class);
        if (pi != null) {
            lblProject.setText(NbBundle.getMessage(SelectAppServerPanel.class,
                    "MSG_InProject", pi.getDisplayName()));
        }
    }

}
