/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.j2ee.ddloaders.multiview.ui;


import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.netbeans.modules.xml.multiview.Refreshable;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * SecurityForm.java
 *
 * Form for adding and editing the run-as and method-permission elemens of
 * the ejb deployment descriptor.
 *
 * @author  ptliu
 */
public class SecurityForm extends SectionNodeInnerPanel {
    public static String USE_CALLER_ID = "useCallerID";  //NOI18N
    public static String RUN_AS = "runAs";              //NOI18N
    public static String NO_SECURITY_ID = "noSecurityID";   //NOI18N
    public static String ALL_METHOD_PERMISSION = "allMethodPermission"; //NOI18N
    public static String SET_ROLE_METHOD_PERMISSION = "setRoleMethodPermission";    //NOI18N
    public static String NO_METHOD_PERMISSION = "noMethodPermission";   //NOI18N
    /**
     * Creates new form SecurityForm
     */
    public SecurityForm(SectionNodeView sectionNodeView) {
        super(sectionNodeView);
        initComponents();
        
        noSecurityIDRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, NO_SECURITY_ID);
        useCallerIDRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, USE_CALLER_ID);
        runAsRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, RUN_AS);
        
        allMethodPermissionRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, ALL_METHOD_PERMISSION);
        setRoleMethodPermissionRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, SET_ROLE_METHOD_PERMISSION);
        noPermissionsRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, NO_METHOD_PERMISSION);
    }
    
    public JComponent getErrorComponent(String errorId) {
        return null;
    }
    
    public void setValue(JComponent source, Object value) {
        
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
        
    }
    
    public JRadioButton getNoSecurityIDRB() {
        return noSecurityIDRB;
    }
    
    public ButtonGroup getSecurityIDButtonGroup() {
        return buttonGroup1;
    }
    
    public JRadioButton getUseCallerIDRB() {
        return useCallerIDRB;
    }
    
    public JRadioButton getRunAsRB() {
        return runAsRB;
    }
    
    public JTextField getRunAsRoleNameTF() {
        return runAsRoleNameTF;
    }
    
    public JTextField getRunAsDescriptionTF() {
        return runAsDescriptionTF;
    }
    
    public JRadioButton getNoPermissionRB() {
        return noPermissionsRB;
    }
    
    public ButtonGroup getGlobalMethodPermissionButtonGroup() {
        return buttonGroup2;
    }
    
    public JTextField getSetRoleRoleNamesTF() {
        return setRoleRoleNamesTF;
    }
    
    public JRadioButton getAllMethodPermissionRB() {
        return allMethodPermissionRB;
    }
    
    public JRadioButton getSetRolePermissionRB() {
        return setRoleMethodPermissionRB;
    }
    
    protected void updateVisualState() {
        
        if (runAsRB.isSelected()) {
            runAsRoleNameLabel.setEnabled(true);
            runAsRoleNameTF.setEnabled(true);
            runAsDescriptionLabel.setEnabled(true);
            runAsDescriptionTF.setEnabled(true);
        } else {
            runAsRoleNameLabel.setEnabled(false);
            runAsRoleNameTF.setEnabled(false);
            runAsDescriptionLabel.setEnabled(false);
            runAsDescriptionTF.setEnabled(false);
        }
               
        if (setRoleMethodPermissionRB.isSelected()) {
            setRoleRoleNamesLabel.setEnabled(true);
            setRoleRoleNamesTF.setEnabled(true);
            setRoleRoleNamesHintLabel.setEnabled(true);
        } else {
            setRoleRoleNamesLabel.setEnabled(false);
            setRoleRoleNamesTF.setEnabled(false);
            setRoleRoleNamesHintLabel.setEnabled(false);
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        runAsRoleNameLabel = new javax.swing.JLabel();
        runAsRoleNameTF = new javax.swing.JTextField();
        runAsDescriptionLabel = new javax.swing.JLabel();
        runAsDescriptionTF = new javax.swing.JTextField();
        setRoleRoleNamesLabel = new javax.swing.JLabel();
        setRoleRoleNamesTF = new javax.swing.JTextField();
        setRoleRoleNamesHintLabel = new javax.swing.JLabel();
        allMethodPermissionRB = new javax.swing.JRadioButton();
        setRoleMethodPermissionRB = new javax.swing.JRadioButton();
        useCallerIDRB = new javax.swing.JRadioButton();
        runAsRB = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        noSecurityIDRB = new javax.swing.JRadioButton();
        noPermissionsRB = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();

        runAsRoleNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_SecurityRoleName"));
        runAsRoleNameLabel.setEnabled(false);

        runAsRoleNameTF.setEnabled(false);

        runAsDescriptionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_Description"));
        runAsDescriptionLabel.setEnabled(false);

        runAsDescriptionTF.setEnabled(false);

        setRoleRoleNamesLabel.setLabelFor(setRoleRoleNamesTF);
        setRoleRoleNamesLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_RoleNames"));
        setRoleRoleNamesLabel.setEnabled(false);

        setRoleRoleNamesTF.setEnabled(false);

        setRoleRoleNamesHintLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_RoleNamesHint"));
        setRoleRoleNamesHintLabel.setEnabled(false);

        buttonGroup2.add(allMethodPermissionRB);
        allMethodPermissionRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_AllUsesAllMethodsPermission"));
        allMethodPermissionRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allMethodPermissionRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allMethodPermissionRB.setOpaque(false);

        buttonGroup2.add(setRoleMethodPermissionRB);
        setRoleMethodPermissionRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_SetRolesAllMethodsPermission"));
        setRoleMethodPermissionRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setRoleMethodPermissionRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        setRoleMethodPermissionRB.setOpaque(false);

        buttonGroup1.add(useCallerIDRB);
        useCallerIDRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_CallerIdentity"));
        useCallerIDRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useCallerIDRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useCallerIDRB.setOpaque(false);

        buttonGroup1.add(runAsRB);
        runAsRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_RunAs"));
        runAsRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        runAsRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        runAsRB.setOpaque(false);

        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_SecurityIdentity"));

        buttonGroup1.add(noSecurityIDRB);
        noSecurityIDRB.setSelected(true);
        noSecurityIDRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_NoSecurityIdentity"));
        noSecurityIDRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noSecurityIDRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noSecurityIDRB.setOpaque(false);

        buttonGroup2.add(noPermissionsRB);
        noPermissionsRB.setSelected(true);
        noPermissionsRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_NoGlobalMethodPermissions"));
        noPermissionsRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noPermissionsRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noPermissionsRB.setOpaque(false);

        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle").getString("LBL_GlobalMethodPermission"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(noSecurityIDRB)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(runAsRB)
                                    .add(useCallerIDRB)
                                    .add(layout.createSequentialGroup()
                                        .add(17, 17, 17)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createSequentialGroup()
                                                .add(runAsDescriptionLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(runAsDescriptionTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE))
                                            .add(layout.createSequentialGroup()
                                                .add(runAsRoleNameLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(runAsRoleNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))))))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(allMethodPermissionRB)
                            .add(setRoleMethodPermissionRB)
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(setRoleRoleNamesLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(setRoleRoleNamesTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                                    .add(setRoleRoleNamesHintLabel)))
                            .add(noPermissionsRB))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addContainerGap(324, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel2)
                        .addContainerGap(272, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noSecurityIDRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(useCallerIDRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(runAsRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(runAsRoleNameLabel)
                    .add(runAsRoleNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(runAsDescriptionLabel)
                    .add(runAsDescriptionTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(noPermissionsRB)
                .add(7, 7, 7)
                .add(allMethodPermissionRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(setRoleMethodPermissionRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(setRoleRoleNamesLabel)
                    .add(setRoleRoleNamesTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(setRoleRoleNamesHintLabel)
                .add(31, 31, 31))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allMethodPermissionRB;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JRadioButton noPermissionsRB;
    private javax.swing.JRadioButton noSecurityIDRB;
    private javax.swing.JLabel runAsDescriptionLabel;
    private javax.swing.JTextField runAsDescriptionTF;
    private javax.swing.JRadioButton runAsRB;
    private javax.swing.JLabel runAsRoleNameLabel;
    private javax.swing.JTextField runAsRoleNameTF;
    private javax.swing.JRadioButton setRoleMethodPermissionRB;
    private javax.swing.JLabel setRoleRoleNamesHintLabel;
    private javax.swing.JLabel setRoleRoleNamesLabel;
    private javax.swing.JTextField setRoleRoleNamesTF;
    private javax.swing.JRadioButton useCallerIDRB;
    // End of variables declaration//GEN-END:variables
    
}
