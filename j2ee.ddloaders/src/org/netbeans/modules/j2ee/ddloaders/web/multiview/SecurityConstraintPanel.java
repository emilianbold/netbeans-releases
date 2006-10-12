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


package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.awt.GridBagLayout;
import java.util.StringTokenizer;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.AuthConstraint;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * SecurityConstraintPanel.java
 *
 * Panel for adding and editing the security-constraint element of the web
 * deployment descriptor.
 *
 * @author  ptliu
 */
public class SecurityConstraintPanel extends SectionInnerPanel {
    
    private SectionView view;
    private DDDataObject dObj;
    private WebApp webApp;
    private SecurityConstraint constraint;
    
    /** Creates new form SecurityConstraintPanel */
    public SecurityConstraintPanel(SectionView view, DDDataObject dObj,
            SecurityConstraint constraint) {
        super(view);
        initComponents();
        
        this.view = view;
        this.dObj = dObj;
        this.webApp = dObj.getWebApp();
        this.constraint = constraint;
        
        initPanel();
    }
    
    private void initPanel() {
        displayNameTF.setText(constraint.getDefaultDisplayName());
        addValidatee(displayNameTF);
        
        AuthConstraint authConstraint = constraint.getAuthConstraint();
        if (authConstraint != null) {
            authConstraintCB.setSelected(true);
            updateVisualState();
            String nameString = getRoleNamesString(authConstraint);
            roleNamesTF.setText(nameString);
            authConstraintDescTF.setText(authConstraint.getDefaultDescription());
        }
        
        addModifier(authConstraintCB);
        //addValidatee(roleNamesTF);
        addModifier(authConstraintDescTF);
        
        UserDataConstraint userDataConstraint = constraint.getUserDataConstraint();
        if (userDataConstraint != null) {
            userDataConstraintCB.setSelected(true);
            updateVisualState();
            transportGuaranteeCB.setSelectedItem((String) userDataConstraint.getTransportGuarantee());
            userDataConstraintDescTF.setText(userDataConstraint.getDefaultDescription());
        }
        
        addModifier(userDataConstraintCB);
        addModifier(userDataConstraintDescTF);
        addModifier(transportGuaranteeCB);
        
        WebResourceCollectionTableModel model = new WebResourceCollectionTableModel();
        WebResourceCollectionTablePanel panel = new WebResourceCollectionTablePanel(dObj, model);
        panel.setModel(dObj.getWebApp(), constraint, constraint.getWebResourceCollection());
        
        webResourceCollectionPanel2.setLayout(new GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        //gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 5.0;
        webResourceCollectionPanel2.add(panel, gridBagConstraints);
        
    }
    
    private void updateVisualState() {
        if (authConstraintCB.isSelected()) {
            authConstraintDescLabel.setEnabled(true);
            authConstraintDescTF.setEnabled(true);
            roleNamesLabel.setEnabled(true);
            roleNamesTF.setEnabled(true);
            editButton.setEnabled(true);
        } else {
            authConstraintDescLabel.setEnabled(false);
            authConstraintDescTF.setEnabled(false);
            roleNamesLabel.setEnabled(false);
            roleNamesTF.setEnabled(false);
            editButton.setEnabled(false);
        }
        
        if (userDataConstraintCB.isSelected()) {
            userDataConstraintDescLabel.setEnabled(true);
            userDataConstraintDescTF.setEnabled(true);
            transportGuaranteeLabel.setEnabled(true);
            transportGuaranteeCB.setEnabled(true);
        } else {
            userDataConstraintDescLabel.setEnabled(false);
            userDataConstraintDescTF.setEnabled(false);
            transportGuaranteeLabel.setEnabled(false);
            transportGuaranteeCB.setEnabled(false);
        }
    }
    
    public void linkButtonPressed(Object obj, String id) {
    }
    
    
    public javax.swing.JComponent getErrorComponent(String name) {
        return null;
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==displayNameTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Display Name", displayNameTF));
                
                return;
            }
            
            SecurityConstraint[] constraints = webApp.getSecurityConstraint();
            for (int i=0; i < constraints.length;i++) {
                if (constraints[i] != constraint &&
                        val.equals(constraints[i].getDefaultDisplayName())) {
                    getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, val, displayNameTF));
                    return;
                }
            }
            getSectionView().getErrorPanel().clearError();
            
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source == displayNameTF) {
            String text = (String)value;
            constraint.setDisplayName(text);
            SectionPanel enclosingPanel = getSectionView().findSectionPanel(constraint);
            enclosingPanel.setTitle(text);
            enclosingPanel.getNode().setDisplayName(text);
        } else if (source == authConstraintCB) {
            if (authConstraintCB.isSelected()) {
                refillAuthConstraint();
            } else {
                setAuthConstraint(null);
            }
        } else if (source == roleNamesTF) {
            refillAuthConstraint();
        } else if (source == authConstraintDescTF) {
            refillAuthConstraint();
        } else if (source == userDataConstraintCB) {
            if (userDataConstraintCB.isSelected()) {
                refillUserDataConstraint();
            } else {
                setUserDataConstraint(null);
            }
        } else if (source == transportGuaranteeCB) {
            refillUserDataConstraint();
        } else if (source == userDataConstraintDescTF) {
            refillUserDataConstraint();
        }
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (source == displayNameTF) {
            displayNameTF.setText(constraint.getDefaultDisplayName());
        }
    }
    
    /** This will be called before model is changed from this panel
     */
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    
    private void setUserDataConstraint(UserDataConstraint userDataConstraint) {
        constraint.setUserDataConstraint(userDataConstraint);
    }
    
    private UserDataConstraint getUserDataConstraint() {
        UserDataConstraint userDataConstraint = constraint.getUserDataConstraint();
        if (userDataConstraint == null) {
            try {
                userDataConstraint = (UserDataConstraint) webApp.createBean("UserDataConstraint");  //NOI18N
                constraint.setUserDataConstraint(userDataConstraint);
            } catch (ClassNotFoundException ex) {
            }
        }
        
        return userDataConstraint;
    }
    
    private void refillUserDataConstraint() {
        setUserDataConstraint(null);
        UserDataConstraint userDataConstraint = getUserDataConstraint();
        userDataConstraint.setDescription(userDataConstraintDescTF.getText());
        userDataConstraint.setTransportGuarantee((String) transportGuaranteeCB.getSelectedItem());
    }
    
    private void setAuthConstraint(AuthConstraint authConstraint) {
        constraint.setAuthConstraint(authConstraint);
    }
    
    private AuthConstraint getAuthConstraint() {
        AuthConstraint authConstraint = constraint.getAuthConstraint();
        if (authConstraint == null) {
            try {
                authConstraint = (AuthConstraint) webApp.createBean("AuthConstraint"); //NOI18N
                constraint.setAuthConstraint(authConstraint);
            } catch (ClassNotFoundException ex) {
            }
        }
        
        return authConstraint;
    }
    
    private void refillAuthConstraint() {
        // Null out the previous authConstraint.
        setAuthConstraint(null);
        
        AuthConstraint authConstraint = getAuthConstraint();
        authConstraint.setDescription(authConstraintDescTF.getText());
        
        String roleNamesString = roleNamesTF.getText();
        StringTokenizer tokenizer = new StringTokenizer(roleNamesString, ","); //NOI18N
        
        while (tokenizer.hasMoreTokens()) {
            String roleName = tokenizer.nextToken().trim();
            
            if (roleName.length() > 0)
                authConstraint.addRoleName(roleName);
        }
    }
    
    private String getRoleNamesString(AuthConstraint authConstraint) {
        String names[] = authConstraint.getRoleName();
        String nameString = "";     //NOI18N
        
        for (int i = 0; i < names.length; i++) {
            if (i > 0)
                nameString += ", ";     //NOI18N
            
            nameString += names[i];
        }
        
        return nameString;
    }
    
    private String[] getSelectedRoleNames() {
        return constraint.getAuthConstraint().getRoleName();
    }
    
    private String[] getAllRoleNames() {
        SecurityRole[] roles = webApp.getSecurityRole();
        String[] roleNames = new String[roles.length];
        
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].getRoleName();
        }
        
        return roleNames;
    }
    
    private void setSelectedRoleNames(String[] roleNames) {
        AuthConstraint authConstraint = constraint.getAuthConstraint();
        
        authConstraint.setRoleName(roleNames);
        roleNamesTF.setText(getRoleNamesString(authConstraint));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        displayNameLabel = new javax.swing.JLabel();
        displayNameTF = new javax.swing.JTextField();
        roleNamesLabel = new javax.swing.JLabel();
        roleNamesTF = new javax.swing.JTextField();
        authConstraintDescLabel = new javax.swing.JLabel();
        authConstraintDescTF = new javax.swing.JTextField();
        transportGuaranteeLabel = new javax.swing.JLabel();
        transportGuaranteeCB = new javax.swing.JComboBox();
        webResourceCollectionLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        webResourceCollectionPanel = new javax.swing.JPanel();
        authConstraintCB = new javax.swing.JCheckBox();
        userDataConstraintCB = new javax.swing.JCheckBox();
        webResourceCollectionPanel2 = new javax.swing.JPanel();
        userDataConstraintDescLabel = new javax.swing.JLabel();
        userDataConstraintDescTF = new javax.swing.JTextField();
        editButton = new javax.swing.JButton();

        displayNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_displayName_mnem").charAt(0));
        displayNameLabel.setLabelFor(displayNameTF);
        displayNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_displayName"));

        roleNamesLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_roleNames_mnem").charAt(0));
        roleNamesLabel.setLabelFor(roleNamesTF);
        roleNamesLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_SecurityRoleNames"));
        roleNamesLabel.setEnabled(false);

        roleNamesTF.setEditable(false);
        roleNamesTF.setEnabled(false);

        authConstraintDescLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_description_mnem1").charAt(0));
        authConstraintDescLabel.setLabelFor(authConstraintDescTF);
        authConstraintDescLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_SecurityRoleDescription"));
        authConstraintDescLabel.setEnabled(false);

        authConstraintDescTF.setEnabled(false);

        transportGuaranteeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_transportGuarantee_mnem").charAt(0));
        transportGuaranteeLabel.setLabelFor(transportGuaranteeCB);
        transportGuaranteeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_TransportGuarantee"));
        transportGuaranteeLabel.setEnabled(false);

        transportGuaranteeCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NONE", "INTEGRAL", "CONFIDENTIAL" }));
        transportGuaranteeCB.setEnabled(false);

        webResourceCollectionLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_webResourceCollection_mnem").charAt(0));
        webResourceCollectionLabel.setLabelFor(webResourceCollectionPanel);
        webResourceCollectionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_WebResourceCollection"));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        webResourceCollectionPanel.setLayout(new java.awt.GridBagLayout());

        authConstraintCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_authConstraint_mnem").charAt(0));
        authConstraintCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_AuthConstraint"));
        authConstraintCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authConstraintCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authConstraintCB.setOpaque(false);
        authConstraintCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authConstraintCBActionPerformed(evt);
            }
        });

        userDataConstraintCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_userDataConstraint_mnem").charAt(0));
        userDataConstraintCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_UserDataConstraint"));
        userDataConstraintCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userDataConstraintCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        userDataConstraintCB.setOpaque(false);
        userDataConstraintCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userDataConstraintCBActionPerformed(evt);
            }
        });

        webResourceCollectionPanel2.setOpaque(false);
        org.jdesktop.layout.GroupLayout webResourceCollectionPanel2Layout = new org.jdesktop.layout.GroupLayout(webResourceCollectionPanel2);
        webResourceCollectionPanel2.setLayout(webResourceCollectionPanel2Layout);
        webResourceCollectionPanel2Layout.setHorizontalGroup(
            webResourceCollectionPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 331, Short.MAX_VALUE)
        );
        webResourceCollectionPanel2Layout.setVerticalGroup(
            webResourceCollectionPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 38, Short.MAX_VALUE)
        );

        userDataConstraintDescLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_description_mnem2").charAt(0));
        userDataConstraintDescLabel.setLabelFor(userDataConstraintDescTF);
        userDataConstraintDescLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_UserDataConstraintDescription"));
        userDataConstraintDescLabel.setEnabled(false);

        userDataConstraintDescTF.setEnabled(false);

        editButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_editRoleNames_mnem").charAt(0));
        editButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_EditRoleNames"));
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
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
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(transportGuaranteeLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(transportGuaranteeCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(userDataConstraintDescLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(userDataConstraintDescTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(layout.createSequentialGroup()
                        .add(displayNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(displayNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(userDataConstraintCB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(webResourceCollectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(webResourceCollectionLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, webResourceCollectionPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(roleNamesLabel)
                            .add(authConstraintDescLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(roleNamesTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                            .add(authConstraintDescTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)))
                    .add(authConstraintCB))
                .add(6, 6, 6)
                .add(editButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(displayNameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(webResourceCollectionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(webResourceCollectionPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(authConstraintCB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(authConstraintDescLabel)
                    .add(authConstraintDescTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(39, 39, 39)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(webResourceCollectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(roleNamesLabel)
                            .add(roleNamesTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(editButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(userDataConstraintCB)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userDataConstraintDescTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(userDataConstraintDescLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(transportGuaranteeCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transportGuaranteeLabel))
                .addContainerGap(36, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
// TODO add your handling code here:
        SecurityRolesEditorPanel dialogPanel = new SecurityRolesEditorPanel(
                getAllRoleNames(), getSelectedRoleNames());
        EditDialog dialog = new EditDialog(dialogPanel,
                NbBundle.getMessage(SecurityConstraintPanel.class,"TTL_RoleNames"),
                false) {
            protected String validate() {
                return null;
            }
        };
              
        javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
        d.setVisible(true);
        
        if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
            dObj.modelUpdatedFromUI();
            dObj.setChangedFromUI(true);
            
            String[] selectedRoles = dialogPanel.getSelectedRoles();
            setSelectedRoleNames(selectedRoles);
            dObj.setChangedFromUI(false);
        }
    }//GEN-LAST:event_editButtonActionPerformed
    
    private void userDataConstraintCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userDataConstraintCBActionPerformed
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_userDataConstraintCBActionPerformed
    
    private void authConstraintCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authConstraintCBActionPerformed
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_authConstraintCBActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox authConstraintCB;
    private javax.swing.JLabel authConstraintDescLabel;
    private javax.swing.JTextField authConstraintDescTF;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField displayNameTF;
    private javax.swing.JButton editButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel roleNamesLabel;
    private javax.swing.JTextField roleNamesTF;
    private javax.swing.JComboBox transportGuaranteeCB;
    private javax.swing.JLabel transportGuaranteeLabel;
    private javax.swing.JCheckBox userDataConstraintCB;
    private javax.swing.JLabel userDataConstraintDescLabel;
    private javax.swing.JTextField userDataConstraintDescTF;
    private javax.swing.JLabel webResourceCollectionLabel;
    private javax.swing.JPanel webResourceCollectionPanel;
    private javax.swing.JPanel webResourceCollectionPanel2;
    // End of variables declaration//GEN-END:variables
    
}
