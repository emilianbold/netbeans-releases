/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.propertysupport.options.panels;

import javax.swing.JCheckBox;
import org.openide.util.NbPreferences;

/**
 *
 * @author  krichard
 */
public class ShowMeDialogsListPanel extends javax.swing.JPanel {
    
    /** Creates new form ShowMeDialogsListPanel */
    public ShowMeDialogsListPanel() {
        initComponents();
        
    }
    
    /**
     * Setting all the ui elements to match their respective prefences.
     * This is called in the corresponding UMLOptionsPanel's update method.
     */
    public void load() {
     
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Allow_Lengthy_Searches", true)) {
            allowLengthyCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Automatically_Create_Classifiers", true)) {
            autoCreateCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Delete_Combined_Fragment_Messages", true)) {
            deleteCombinedFragCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Delete_Connector_Messages", true)) {
            deleteConnectorCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Delete_File_when_Deleting_Artifacts", true)) {
            deleteFileCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Dont_Show_Filter_Warning_Dialog", true)) {
            filterWarningCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Modify_Redefined_Operations", true)) {
            modifyCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Move_Invoked_Operation", true)) {
            moveInvokedCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Overwrite_Existing_Participants", true)) {
            overwriteCB.setSelected(true);
        }
        if (NbPreferences.forModule(ShowMeDialogsListPanel.class).getBoolean("UML_ShowMe_Transform_When_Elements_May_Be_Lost", true)) {
            transformCB.setSelected(true);
        }
        
    }
    
    public void store() {
        
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Allow_Lengthy_Searches", allowLengthyCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Automatically_Create_Classifiers", autoCreateCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Delete_Combined_Fragment_Messages", deleteCombinedFragCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Delete_Connector_Messages", deleteConnectorCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Delete_File_when_Deleting_Artifacts", deleteFileCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Dont_Show_Filter_Warning_Dialog", filterWarningCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Modify_Redefined_Operations", modifyCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Move_Invoked_Operation", moveInvokedCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Overwrite_Existing_Participants", overwriteCB.isSelected());
        NbPreferences.forModule(ShowMeDialogsListPanel.class).putBoolean("UML_ShowMe_Transform_When_Elements_May_Be_Lost", transformCB.isSelected());
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        dialogListPanel = new javax.swing.JPanel();
        deleteFileCB = new javax.swing.JCheckBox();
        filterWarningCB = new javax.swing.JCheckBox();
        transformCB = new javax.swing.JCheckBox();
        modifyCB = new javax.swing.JCheckBox();
        overwriteCB = new javax.swing.JCheckBox();
        deleteConnectorCB = new javax.swing.JCheckBox();
        autoCreateCB = new javax.swing.JCheckBox();
        deleteCombinedFragCB = new javax.swing.JCheckBox();
        moveInvokedCB = new javax.swing.JCheckBox();
        allowLengthyCB = new javax.swing.JCheckBox();
        buttonPanel = new javax.swing.JPanel();
        deselectButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        mainLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();

        checkboxes = new JCheckBox[10];

        checkboxes[0] = allowLengthyCB ;
        checkboxes[1] = autoCreateCB ;
        checkboxes[2] = deleteCombinedFragCB ;
        checkboxes[3] = deleteConnectorCB ;
        checkboxes[4] = deleteFileCB ;
        checkboxes[5] = filterWarningCB ;
        checkboxes[6] = modifyCB ;
        checkboxes[7] = moveInvokedCB ;
        checkboxes[8] = overwriteCB ;
        checkboxes[9] = transformCB ;

        deleteFileCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox1.text_1")); // NOI18N
        deleteFileCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        filterWarningCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox2.text_1")); // NOI18N
        filterWarningCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        transformCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox3.text_1")); // NOI18N
        transformCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transformCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transformCBActionPerformed(evt);
            }
        });

        modifyCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox4.text_1")); // NOI18N
        modifyCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        overwriteCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox5.text_1")); // NOI18N
        overwriteCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        deleteConnectorCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox6.text_1")); // NOI18N
        deleteConnectorCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        autoCreateCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox7.text_1")); // NOI18N
        autoCreateCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        deleteCombinedFragCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox8.text")); // NOI18N
        deleteCombinedFragCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        moveInvokedCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox9.text")); // NOI18N
        moveInvokedCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        allowLengthyCB.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jCheckBox10.text")); // NOI18N
        allowLengthyCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout dialogListPanelLayout = new org.jdesktop.layout.GroupLayout(dialogListPanel);
        dialogListPanel.setLayout(dialogListPanelLayout);
        dialogListPanelLayout.setHorizontalGroup(
            dialogListPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dialogListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(dialogListPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(deleteFileCB)
                    .add(filterWarningCB)
                    .add(transformCB)
                    .add(modifyCB)
                    .add(overwriteCB)
                    .add(deleteConnectorCB)
                    .add(autoCreateCB)
                    .add(deleteCombinedFragCB)
                    .add(moveInvokedCB)
                    .add(allowLengthyCB))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        dialogListPanelLayout.setVerticalGroup(
            dialogListPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(dialogListPanelLayout.createSequentialGroup()
                .add(deleteFileCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(filterWarningCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(transformCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(modifyCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(overwriteCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(deleteConnectorCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(autoCreateCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(deleteCombinedFragCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(moveInvokedCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(allowLengthyCB))
        );

        deselectButton.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jButton1.text")); // NOI18N
        deselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectButtonActionPerformed(evt);
            }
        });

        jButton1.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "jButton1.text_1")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout buttonPanelLayout = new org.jdesktop.layout.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jButton1)
                .add(17, 17, 17)
                .add(deselectButton)
                .addContainerGap(120, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(buttonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(deselectButton)
                .add(jButton1))
        );

        mainLabel.setText(org.openide.util.NbBundle.getMessage(ShowMeDialogsListPanel.class, "mainLabel.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainLabel)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 435, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 435, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dialogListPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(92, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(mainLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dialogListPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(26, 26, 26)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(buttonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void transformCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transformCBActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_transformCBActionPerformed
    
private void deselectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectButtonActionPerformed
    deselectAll() ;
}//GEN-LAST:event_deselectButtonActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    selectAll() ;
}//GEN-LAST:event_jButton1ActionPerformed

private void selectAll() {
    for (JCheckBox box: checkboxes) {
        box.setSelected(true);
    }
    
}

private void deselectAll() {
    for (JCheckBox box: checkboxes) {
        box.setSelected(false);
    }
    
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allowLengthyCB;
    private javax.swing.JCheckBox autoCreateCB;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JCheckBox deleteCombinedFragCB;
    private javax.swing.JCheckBox deleteConnectorCB;
    private javax.swing.JCheckBox deleteFileCB;
    private javax.swing.JButton deselectButton;
    private JCheckBox[] checkboxes = null ;
    private javax.swing.JPanel dialogListPanel;
    private javax.swing.JCheckBox filterWarningCB;
    private javax.swing.JButton jButton1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel mainLabel;
    private javax.swing.JCheckBox modifyCB;
    private javax.swing.JCheckBox moveInvokedCB;
    private javax.swing.JCheckBox overwriteCB;
    private javax.swing.JCheckBox transformCB;
    // End of variables declaration//GEN-END:variables
    
}
