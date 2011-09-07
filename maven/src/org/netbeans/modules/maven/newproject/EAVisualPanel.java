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

package org.netbeans.modules.maven.newproject;

import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.maven.api.MavenValidators;
import org.netbeans.modules.maven.api.archetype.ProjectInfo;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle.Messages;

public final class EAVisualPanel extends JPanel  {

    private EAWizardPanel panel;
    private final ValidationGroup vg;


    /** Creates new form EAVisualPanel */
    @SuppressWarnings("unchecked")
    public EAVisualPanel(EAWizardPanel panel) {
        this.panel = panel;
        initComponents();
        vg = ValidationGroup.create();
        vg.add(tfWeb, Validators.merge(true,
                MavenValidators.createArtifactIdValidators(),
                Validators.REQUIRE_VALID_FILENAME
                ));
        vg.add(tfEar, Validators.merge(true,
                MavenValidators.createArtifactIdValidators(),
                Validators.REQUIRE_VALID_FILENAME
                ));
        vg.add(tfEjb, Validators.merge(true,
                MavenValidators.createArtifactIdValidators(),
                Validators.REQUIRE_VALID_FILENAME
                ));
        tfWeb.putClientProperty(ValidationListener.CLIENT_PROP_NAME, "Web ArtifactId");
        tfEar.putClientProperty(ValidationListener.CLIENT_PROP_NAME, "Ear ArtifactId");
        tfEjb.putClientProperty(ValidationListener.CLIENT_PROP_NAME, "Ejb ArtifactId");

        getAccessibleContext().setAccessibleDescription(LBL_EESettings());
    }

    @Messages("LBL_EESettings=Settings")
    @Override
    public String getName() {
        return LBL_EESettings();
    }

    void readSettings(WizardDescriptor wizardDescriptor) {
        fillTextFields(wizardDescriptor);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.getValidationGroup().addValidationGroup(vg, true);
            }
        });
    }

    void storeSettings(WizardDescriptor d) {
        int eeLevelIdx = Math.max(cmbEEVersion.getSelectedIndex(), 0);

        File parent = (File) d.getProperty("projdir");
        String earText = tfEar.getText().trim();
        d.putProperty("ear_projdir", new File(parent, earText));
        ProjectInfo pi = new ProjectInfo((String) d.getProperty("groupId"), earText, (String) d.getProperty("version"), null);
        d.putProperty("ear_versionInfo", pi);
        d.putProperty("ear_archetype", ArchetypeWizardUtils.EAR_ARCHS[eeLevelIdx]);

        if (chkEjb.isSelected()) {
            String ejbText = tfEjb.getText().trim();
            d.putProperty("ejb_projdir", new File(parent, ejbText));
            pi = new ProjectInfo((String) d.getProperty("groupId"), ejbText, (String) d.getProperty("version"), null);
            d.putProperty("ejb_versionInfo", pi);
            d.putProperty("ejb_archetype", ArchetypeWizardUtils.EJB_ARCHS[eeLevelIdx]);
        } else {
            d.putProperty("ejb_projdir", null);
            d.putProperty("ejb_versionInfo", null);
            d.putProperty("ejb_archetype", null);
        }

        if (chkWeb.isSelected()) {
            String webText = tfWeb.getText().trim();
            d.putProperty("web_projdir", new File(parent, webText));
            pi = new ProjectInfo((String) d.getProperty("groupId"), webText, (String) d.getProperty("version"), null);
            d.putProperty("web_versionInfo", pi);
            d.putProperty("web_archetype", ArchetypeWizardUtils.WEB_APP_ARCHS[eeLevelIdx]);

        } else {
            d.putProperty("web_projdir", null);
            d.putProperty("web_versionInfo", null);
            d.putProperty("web_archetype", null);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                panel.getValidationGroup().removeValidationGroup(vg);
            }
        });
    }

    private void fillTextFields(WizardDescriptor wiz) {
        String artifId = (String)wiz.getProperty("artifactId");
        tfEar.setText(artifId + "-ear");
        tfWeb.setText(artifId + "-web");
        tfEjb.setText(artifId + "-ejb");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbEEVersion = new javax.swing.JComboBox();
        lblEEVersion = new javax.swing.JLabel();
        chkEjb = new javax.swing.JCheckBox();
        chkWeb = new javax.swing.JCheckBox();
        lblEar = new javax.swing.JLabel();
        tfWeb = new javax.swing.JTextField();
        tfEjb = new javax.swing.JTextField();
        tfEar = new javax.swing.JTextField();

        cmbEEVersion.setModel(new DefaultComboBoxModel(BasicEEWizardIterator.eeLevels()));

        lblEEVersion.setLabelFor(cmbEEVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblEEVersion, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.lblEEVersion.text")); // NOI18N

        chkEjb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkEjb, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkEjb.text")); // NOI18N
        chkEjb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEjbActionPerformed(evt);
            }
        });

        chkWeb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkWeb, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkWeb.text")); // NOI18N
        chkWeb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWebActionPerformed(evt);
            }
        });

        lblEar.setLabelFor(tfEar);
        org.openide.awt.Mnemonics.setLocalizedText(lblEar, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.lblEar.text")); // NOI18N

        tfWeb.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfWeb.text")); // NOI18N

        tfEjb.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEjb.text")); // NOI18N

        tfEar.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEar.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblEEVersion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbEEVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chkWeb)
                            .addComponent(chkEjb)
                            .addComponent(lblEar))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tfEjb, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .addComponent(tfWeb, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .addComponent(tfEar, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEEVersion)
                    .addComponent(cmbEEVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfEjb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkEjb))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkWeb)
                    .addComponent(tfWeb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfEar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEar))
                .addContainerGap(174, Short.MAX_VALUE))
        );

        cmbEEVersion.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.cmbEEVersion.AccessibleContext.accessibleDescription")); // NOI18N
        chkEjb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkEjb.AccessibleContext.accessibleDescription")); // NOI18N
        chkWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkWeb.AccessibleContext.accessibleDescription")); // NOI18N
        tfWeb.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfWeb.AccessibleContext.accessibleName")); // NOI18N
        tfWeb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfWeb.AccessibleContext.accessibleDescription")); // NOI18N
        tfEjb.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEjb.AccessibleContext.accessibleName")); // NOI18N
        tfEjb.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEjb.AccessibleContext.accessibleDescription")); // NOI18N
        tfEar.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEar.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void chkEjbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEjbActionPerformed
        // TODO add your handling code here:
        tfEjb.setEnabled(chkEjb.isSelected());
        vg.validateAll();
    }//GEN-LAST:event_chkEjbActionPerformed

    private void chkWebActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWebActionPerformed
        // TODO add your handling code here:
        tfWeb.setEnabled(chkWeb.isSelected());
        vg.validateAll();
    }//GEN-LAST:event_chkWebActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkEjb;
    private javax.swing.JCheckBox chkWeb;
    private javax.swing.JComboBox cmbEEVersion;
    private javax.swing.JLabel lblEEVersion;
    private javax.swing.JLabel lblEar;
    private javax.swing.JTextField tfEar;
    private javax.swing.JTextField tfEjb;
    private javax.swing.JTextField tfWeb;
    // End of variables declaration//GEN-END:variables

}

