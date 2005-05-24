/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * MdbConnectionFactoryPanel.java        October 27, 2003, 3:59 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupportClient;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class MdbConnectionFactoryPanel extends javax.swing.JPanel
                implements ErrorSupportClient {

    private MDEjbCustomizer mdEjbCutomizer;
    protected ErrorSupport errorSupport;
    protected ValidationSupport validationSupport;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N


    /** Creates new form MdbConnectionFactoryPanel */
    public MdbConnectionFactoryPanel(MDEjbCustomizer customizer) {
        initComponents();
        this.mdEjbCutomizer = customizer;
        errorSupport = new ErrorSupport(this);
        validationSupport = new ValidationSupport();
    }


    public void setValues(MdbConnectionFactory mdbConnectionFactory){
        if(mdbConnectionFactory != null){
            String jndiName = mdbConnectionFactory.getJndiName();
            if(jndiName != null){
                jndiNameTextField.setText(jndiName);
            }
            
            setDefaultResourcePrincipal(
                mdbConnectionFactory.getDefaultResourcePrincipal());
        }
    }
  

    public java.awt.Container getErrorPanelParent(){
        return this;
    }


    public java.awt.GridBagConstraints getErrorPanelConstraints(){
        java.awt.GridBagConstraints gridBagConstraints = 
            new java.awt.GridBagConstraints();

        gridBagConstraints.anchor = gridBagConstraints.SOUTH;
        gridBagConstraints.fill = gridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets.top = 20;
        gridBagConstraints.insets.left = 0;
        gridBagConstraints.insets.bottom = 0;
        gridBagConstraints.insets.right = 0;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;

        return gridBagConstraints;
    }


    public java.util.Collection getErrors(){
        if(validationSupport == null) assert(false);
        ArrayList errors = new ArrayList();

        //Mdb Connection Factory fields Validation
        
        String property;
        boolean mdbConnectionFactoryPresent = isMdbConnectionFactoryPresent();
        if(mdbConnectionFactoryPresent){
            property = jndiNameTextField.getText();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/mdb-connection-factory/jndi-name", //NOI18N
                    bundle.getString("LBL_Jndi_Name")));                    //NOI18N
        }

        boolean resourcePrincipalPresent = isDefaultResourcePrincipalPresent();
        if(resourcePrincipalPresent){
            property = nameTextField.getText();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/mdb-connection-factory/default-resource-principal/name", //NOI18N
                    bundle.getString("LBL_Name")));                     //NOI18N

            property = passwordTextField.getText();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/mdb-connection-factory/default-resource-principal/password", //NOI18N
                    bundle.getString("LBL_Password")));                 //NOI18N

        }

        return errors;
    }
	
	public java.awt.Color getMessageForegroundColor() {
		return BeanCustomizer.ErrorTextForegroundColor;
	}

    private boolean isMdbConnectionFactoryPresent(){
        boolean mdbConnectionFactoryPresent = false;
        String property = jndiNameTextField.getText();
        while(true){
            if((property != null) && (property.length() != 0)){
                mdbConnectionFactoryPresent = true;
                break;
            }
            
            if(isDefaultResourcePrincipalPresent()){
                mdbConnectionFactoryPresent = true;
                break;
            }
            break;
        }
        return mdbConnectionFactoryPresent;
    }


    private boolean isDefaultResourcePrincipalPresent(){
        boolean defaultResourcePrincipalPresent = false;
        String property = nameTextField.getText();
        while(true){
            if((property != null) && (property.length() != 0)){
                defaultResourcePrincipalPresent = true;
                break;
            }
            
            property = passwordTextField.getText();
            if((property != null) && (property.length() != 0)){
                defaultResourcePrincipalPresent = true;
                break;
            }
            break;
        }
        return defaultResourcePrincipalPresent;
    }


    private void validateEntries(){
        if(errorSupport != null){
            errorSupport.showErrors();
            ///mdEjbCutomizer.validate();
            ///this.validate();
        }
    }


    private void setDefaultResourcePrincipal(
        DefaultResourcePrincipal defaultResPrincipal){
        if(defaultResPrincipal != null){
            String name = defaultResPrincipal.getName();
            if(name != null){
                nameTextField.setText(name);
            }
            String password = defaultResPrincipal.getPassword();
            if(password != null){
                passwordTextField.setText(password);
            }
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jndiNamePanel = new javax.swing.JPanel();
        jndiNameLabel = new javax.swing.JLabel();
        jndiNameTextField = new javax.swing.JTextField();
        defaultResourcePrincipalLabel = new javax.swing.JLabel();
        defaultResourcePrincipalPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        jndiNamePanel.setLayout(new java.awt.GridBagLayout());

        jndiNamePanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        jndiNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Mdb_Conn_Fctry_Jndi_Name").charAt(0));
        jndiNameLabel.setLabelFor(jndiNameTextField);
        jndiNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Jndi_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        jndiNamePanel.add(jndiNameLabel, gridBagConstraints);
        jndiNameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jndi_Name_Acsbl_Name"));
        jndiNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mdb_Conn_Fctry_Jndi_Name_Acsbl_Desc"));

        jndiNameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mdb_Conn_Fctry_Jndi_Name_Tool_Tip"));
        jndiNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jndiNameActionPerformed(evt);
            }
        });
        jndiNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jndiNameFocusGained(evt);
            }
        });
        jndiNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jndiNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 4, 5);
        jndiNamePanel.add(jndiNameTextField, gridBagConstraints);
        jndiNameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jndi_Name_Acsbl_Name"));
        jndiNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Mdb_Conn_Fctry_Jndi_Name_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(jndiNamePanel, gridBagConstraints);

        defaultResourcePrincipalLabel.setLabelFor(defaultResourcePrincipalPanel);
        defaultResourcePrincipalLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Default_Resource_Principal"));
        defaultResourcePrincipalLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 5);
        add(defaultResourcePrincipalLabel, gridBagConstraints);

        defaultResourcePrincipalPanel.setLayout(new java.awt.GridBagLayout());

        defaultResourcePrincipalPanel.setBorder(new javax.swing.border.EtchedBorder());
        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Mdb_Conn_Fctry_Name").charAt(0));
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        defaultResourcePrincipalPanel.add(nameLabel, gridBagConstraints);
        nameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Acsbl_Desc"));

        nameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Tool_Tip"));
        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameActionPerformed(evt);
            }
        });
        nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFocusGained(evt);
            }
        });
        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        defaultResourcePrincipalPanel.add(nameTextField, gridBagConstraints);
        nameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Acsbl_Desc"));

        passwordLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Mdb_Conn_Fctry_Password").charAt(0));
        passwordLabel.setLabelFor(passwordTextField);
        passwordLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Password_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 5, 5);
        defaultResourcePrincipalPanel.add(passwordLabel, gridBagConstraints);
        passwordLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Password_Acsbl_Name"));
        passwordLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Acsbl_Desc"));

        passwordTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Tool_Tip"));
        passwordTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordActionPerformed(evt);
            }
        });
        passwordTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordFocusGained(evt);
            }
        });
        passwordTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwordKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        defaultResourcePrincipalPanel.add(passwordTextField, gridBagConstraints);
        passwordTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Password_Acsbl_Name"));
        passwordTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(defaultResourcePrincipalPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        validateEntries();
    }//GEN-LAST:event_formFocusGained

    private void passwordFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_passwordFocusGained

    private void passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_passwordActionPerformed

    private void nameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_nameFocusGained

    private void nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_nameActionPerformed

    private void jndiNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jndiNameFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_jndiNameFocusGained

    private void jndiNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jndiNameActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_jndiNameActionPerformed

    private void passwordKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordKeyReleased
        // Add your handling code here:
        String item = passwordTextField.getText();
        mdEjbCutomizer.updateDefaultResourcePrincipalPassword(item);
        validateEntries();
    }//GEN-LAST:event_passwordKeyReleased

    private void nameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameKeyReleased
        // Add your handling code here:
        String item = nameTextField.getText();
        mdEjbCutomizer.updateDefaultResourcePrincipalName(item);
        validateEntries();
    }//GEN-LAST:event_nameKeyReleased

    private void jndiNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jndiNameKeyReleased
        // Add your handling code here:
        String item = jndiNameTextField.getText();
        mdEjbCutomizer.updateMdbConnectionFactoryJndiName(item);
        validateEntries();
    }//GEN-LAST:event_jndiNameKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel defaultResourcePrincipalLabel;
    private javax.swing.JPanel defaultResourcePrincipalPanel;
    private javax.swing.JLabel jndiNameLabel;
    private javax.swing.JPanel jndiNamePanel;
    private javax.swing.JTextField jndiNameTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField passwordTextField;
    // End of variables declaration//GEN-END:variables
    
}
