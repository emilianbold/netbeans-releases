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
package org.netbeans.modules.cordova.template;

import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.cordova.CordovaPlatform;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class CordovaTemplatePanel extends javax.swing.JPanel {

    private CordovaTemplate.CordovaExtender ext;
    
    public static String PROP_EXT_ENABLED = "PROP_EXT_ENABLED";//NOI18N
    
    /**
     * Creates new form CordovaTemplatePanel
     */
    public CordovaTemplatePanel(CordovaTemplate.CordovaExtender ext) {
        this.ext = ext;
        setName(NbBundle.getMessage(CordovaTemplatePanel.class, "LBL_CordovaSetup"));//NOI18N
        initComponents();
        update();
    }
    
    public void update() {
        platformSetup.setVisible(CordovaPlatform.getDefault().getSdkLocation()==null);
        androidTarget.setVisible(false);
        iosTarget.setVisible(false);
        androidTargetCombo.setVisible(false);
        iosTargetCombo.setVisible(false);
        controlsPanel.setEnabled(phonegapCheckBox.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlsPanel = new javax.swing.JPanel();
        packageTextField = new javax.swing.JTextField();
        packageLabel = new javax.swing.JLabel();
        platformSetup = new javax.swing.JButton();
        androidTarget = new javax.swing.JLabel();
        iosTarget = new javax.swing.JLabel();
        androidTargetCombo = new javax.swing.JComboBox();
        iosTargetCombo = new javax.swing.JComboBox();
        phonegapCheckBox = new javax.swing.JCheckBox();

        packageTextField.setText(org.openide.util.NbBundle.getMessage(CordovaTemplatePanel.class, "CordovaTemplatePanel.packageTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(CordovaTemplatePanel.class, "CordovaTemplatePanel.packageLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(platformSetup, org.openide.util.NbBundle.getMessage(CordovaTemplatePanel.class, "CordovaTemplatePanel.platformSetup.text")); // NOI18N
        platformSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformSetupActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(androidTarget, org.openide.util.NbBundle.getMessage(CordovaTemplatePanel.class, "CordovaTemplatePanel.androidTarget.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(iosTarget, org.openide.util.NbBundle.getMessage(CordovaTemplatePanel.class, "CordovaTemplatePanel.iosTarget.text")); // NOI18N

        androidTargetCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "API Level 14 (Android 4.0+)", "API Level 8 (Android 2.2+)" }));

        iosTargetCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "iOS 5.1", "iOS 5.0" }));

        org.jdesktop.layout.GroupLayout controlsPanelLayout = new org.jdesktop.layout.GroupLayout(controlsPanel);
        controlsPanel.setLayout(controlsPanelLayout);
        controlsPanelLayout.setHorizontalGroup(
            controlsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, controlsPanelLayout.createSequentialGroup()
                .add(0, 0, Short.MAX_VALUE)
                .add(platformSetup))
            .add(controlsPanelLayout.createSequentialGroup()
                .add(controlsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(androidTarget)
                    .add(iosTarget))
                .add(15, 15, 15)
                .add(controlsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(androidTargetCombo, 0, 287, Short.MAX_VALUE)
                    .add(iosTargetCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(controlsPanelLayout.createSequentialGroup()
                .add(packageLabel)
                .add(18, 18, 18)
                .add(packageTextField))
        );
        controlsPanelLayout.setVerticalGroup(
            controlsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlsPanelLayout.createSequentialGroup()
                .add(controlsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(packageLabel)
                    .add(packageTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controlsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(androidTarget)
                    .add(androidTargetCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controlsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(iosTarget)
                    .add(iosTargetCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 154, Short.MAX_VALUE)
                .add(platformSetup)
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(phonegapCheckBox, org.openide.util.NbBundle.getMessage(CordovaTemplatePanel.class, "CordovaTemplatePanel.phonegapCheckBox.text")); // NOI18N
        phonegapCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                phonegapCheckBoxStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(phonegapCheckBox)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(controlsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(phonegapCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(controlsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void platformSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformSetupActionPerformed
        OptionsDisplayer.getDefault().open("Advanced/MobilePlatforms");//NOI18N
    }//GEN-LAST:event_platformSetupActionPerformed

    private void phonegapCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_phonegapCheckBoxStateChanged
        setControlsEnabled(phonegapCheckBox.isSelected());  
        ext.setEnabled(phonegapCheckBox.isSelected());
        firePropertyChange(PROP_EXT_ENABLED, !phonegapCheckBox.isSelected(), phonegapCheckBox.isSelected());
    }//GEN-LAST:event_phonegapCheckBoxStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel androidTarget;
    private javax.swing.JComboBox androidTargetCombo;
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JLabel iosTarget;
    private javax.swing.JComboBox iosTargetCombo;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageTextField;
    private javax.swing.JCheckBox phonegapCheckBox;
    private javax.swing.JButton platformSetup;
    // End of variables declaration//GEN-END:variables

    void setPanelEnabled(boolean b) {
        phonegapCheckBox.setSelected(b);
    }
    
    private void setControlsEnabled(boolean enabled) {
        androidTarget.setEnabled(enabled);
        androidTargetCombo.setEnabled(enabled);
        iosTarget.setEnabled(enabled);
        iosTargetCombo.setEnabled(enabled);
        packageLabel.setEnabled(enabled);
        packageTextField.setEnabled(enabled);
        platformSetup.setEnabled(enabled);
        controlsPanel.setEnabled(enabled);
    }
}
