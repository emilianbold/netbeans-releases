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
 * BeanPoolPanel.java        October 21, 2003, 11:31 AM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupportClient;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class BeanPoolPanel extends javax.swing.JPanel 
        implements ErrorSupportClient {

    private EjbCustomizer ejbCutomizer;
    protected ErrorSupport errorSupport;
    protected ValidationSupport validationSupport;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N
   

    /** Creates new form BeanPoolPanel */
    public BeanPoolPanel(EjbCustomizer customizer) {
        initComponents();
        this.ejbCutomizer = customizer;
        errorSupport = new ErrorSupport(this);
        validationSupport = new ValidationSupport();
    }


    public void setValues(BeanPool beanPool){
        if(beanPool != null){
            steadyPoolSizeTextField.setText(beanPool.getSteadyPoolSize());
            resizeQuantityTextField.setText(beanPool.getResizeQuantity());
            maxPoolSizeTextField.setText(beanPool.getMaxPoolSize());
            poolIdleTimeoutInSecondsTextField.setText(
                beanPool.getPoolIdleTimeoutInSeconds());
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
        gridBagConstraints.gridy = 4;
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
        
        //Bean Pool fields Validation
        String property = steadyPoolSizeTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-pool/steady-pool-size", //NOI18N
                bundle.getString("LBL_Steady_Pool_Size")));         //NOI18N
            
        property = resizeQuantityTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-pool/resize-quantity", //NOI18N
                bundle.getString("LBL_Resize_Quantity")));          //NOI18N

        property = poolIdleTimeoutInSecondsTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-pool/pool-idle-timeout-in-seconds", //NOI18N
                bundle.getString("LBL_Pool_Idle_Timeout_In_Seconds"))); //NOI18N

        property = maxPoolSizeTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-pool/max-pool-size", //NOI18N
                bundle.getString("LBL_Max_Pool_Size")));            //NOI18N

        return errors;
    }
	
	public java.awt.Color getMessageForegroundColor() {
		return BeanCustomizer.ErrorTextForegroundColor;
	}

    private void validateEntries(){
        if(errorSupport != null){
            errorSupport.showErrors();
            ejbCutomizer.validate();
            this.validate();
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

        steadyPoolSizeTextField = new javax.swing.JTextField();
        steadyPoolSizeLabel = new javax.swing.JLabel();
        resizeQuantityLabel = new javax.swing.JLabel();
        resizeQuantityTextField = new javax.swing.JTextField();
        maxPoolSizeLabel = new javax.swing.JLabel();
        maxPoolSizeTextField = new javax.swing.JTextField();
        poolIdleTimeoutInSecondsLabel = new javax.swing.JLabel();
        poolIdleTimeoutInSecondsTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        steadyPoolSizeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                steadyPoolSizeActionPerformed(evt);
            }
        });
        steadyPoolSizeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                steadyPoolSizeKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(steadyPoolSizeTextField, gridBagConstraints);
        steadyPoolSizeTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Steady_Pool_Size_Acsbl_Name"));
        steadyPoolSizeTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Steady_Pool_Size_Acsbl_Desc"));

        steadyPoolSizeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Steady_Pool_Size").charAt(0));
        steadyPoolSizeLabel.setLabelFor(steadyPoolSizeTextField);
        steadyPoolSizeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Steady_Pool_Size_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(steadyPoolSizeLabel, gridBagConstraints);
        steadyPoolSizeLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Steady_Pool_Size_Acsbl_Name"));
        steadyPoolSizeLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Steady_Pool_Size_Acsbl_Desc"));

        resizeQuantityLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Resize_Quantity").charAt(0));
        resizeQuantityLabel.setLabelFor(resizeQuantityTextField);
        resizeQuantityLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Resize_Quantity_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(resizeQuantityLabel, gridBagConstraints);
        resizeQuantityLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resize_Quantity_Acsbl_Name"));
        resizeQuantityLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Pool_Resize_Quantity_Acsbl_Desc"));

        resizeQuantityTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Pool_Resize_Quantity_Tool_Tip"));
        resizeQuantityTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resizeQuantityActionPerformed(evt);
            }
        });
        resizeQuantityTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                resizeQuantityFocusGained(evt);
            }
        });
        resizeQuantityTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                resizeQuantityKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(resizeQuantityTextField, gridBagConstraints);
        resizeQuantityTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Resize_Quantity_Acsbl_Name"));
        resizeQuantityTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Pool_Resize_Quantity_Acsbl_Desc"));

        maxPoolSizeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Max_Pool_Size").charAt(0));
        maxPoolSizeLabel.setLabelFor(maxPoolSizeTextField);
        maxPoolSizeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Max_Pool_Size_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(maxPoolSizeLabel, gridBagConstraints);
        maxPoolSizeLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Pool_Size_Acsbl_Name"));
        maxPoolSizeLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Pool_Size_Acsbl_Desc"));

        maxPoolSizeTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Pool_Size_Tool_Tip"));
        maxPoolSizeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxPoolSizeActionPerformed(evt);
            }
        });
        maxPoolSizeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                maxPoolSizeFocusGained(evt);
            }
        });
        maxPoolSizeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPoolSizeKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(maxPoolSizeTextField, gridBagConstraints);
        maxPoolSizeTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Pool_Size_Acsbl_Name"));
        maxPoolSizeTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Pool_Size_Acsbl_Desc"));

        poolIdleTimeoutInSecondsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Pool_Idle_Timeout_In_Seconds").charAt(0));
        poolIdleTimeoutInSecondsLabel.setLabelFor(poolIdleTimeoutInSecondsTextField);
        poolIdleTimeoutInSecondsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Pool_Idle_Timeout_In_Seconds_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(poolIdleTimeoutInSecondsLabel, gridBagConstraints);
        poolIdleTimeoutInSecondsLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pool_Idle_Timeout_In_Seconds_Acsbl_Name"));
        poolIdleTimeoutInSecondsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pool_Idle_Timeout_In_Seconds_Acsbl_Desc"));

        poolIdleTimeoutInSecondsTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pool_Idle_Timeout_In_Seconds_Tool_Tip"));
        poolIdleTimeoutInSecondsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                poolIdleTimeoutInSecondsActionPerformed(evt);
            }
        });
        poolIdleTimeoutInSecondsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                poolIdleTimeoutInSecondsFocusGained(evt);
            }
        });
        poolIdleTimeoutInSecondsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                poolIdleTimeoutInSecondsKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(poolIdleTimeoutInSecondsTextField, gridBagConstraints);
        poolIdleTimeoutInSecondsTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pool_Idle_Timeout_In_Seconds_Acsbl_Name"));
        poolIdleTimeoutInSecondsTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pool_Idle_Timeout_In_Seconds_Acsbl_Desc"));

    }
    // </editor-fold>//GEN-END:initComponents

    private void poolIdleTimeoutInSecondsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_poolIdleTimeoutInSecondsActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_poolIdleTimeoutInSecondsActionPerformed

    private void poolIdleTimeoutInSecondsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_poolIdleTimeoutInSecondsFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_poolIdleTimeoutInSecondsFocusGained

    private void maxPoolSizeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxPoolSizeFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_maxPoolSizeFocusGained

    private void maxPoolSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxPoolSizeActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_maxPoolSizeActionPerformed

    private void resizeQuantityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_resizeQuantityFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_resizeQuantityFocusGained

    private void resizeQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizeQuantityActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_resizeQuantityActionPerformed

    private void steadyPoolSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_steadyPoolSizeActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_steadyPoolSizeActionPerformed

    private void poolIdleTimeoutInSecondsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_poolIdleTimeoutInSecondsKeyReleased
        // Add your handling code here:
        String item = poolIdleTimeoutInSecondsTextField.getText();
        ejbCutomizer.updatePoolIdleTimeoutInSeconds(item);
        validateEntries();
    }//GEN-LAST:event_poolIdleTimeoutInSecondsKeyReleased

    private void maxPoolSizeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPoolSizeKeyReleased
        // Add your handling code here:
        String item = maxPoolSizeTextField.getText();
        ejbCutomizer.updateMaxPoolSize(item);
        validateEntries();
    }//GEN-LAST:event_maxPoolSizeKeyReleased

    private void resizeQuantityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resizeQuantityKeyReleased
        // Add your handling code here:
        String item = resizeQuantityTextField.getText();
        ejbCutomizer.updateResizeQuantity(item); 
        validateEntries();
    }//GEN-LAST:event_resizeQuantityKeyReleased

    private void steadyPoolSizeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_steadyPoolSizeKeyReleased
        // Add your handling code here:
        String item = steadyPoolSizeTextField.getText();
        ejbCutomizer.updateSteadyPoolSize(item);
        validateEntries();
    }//GEN-LAST:event_steadyPoolSizeKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel maxPoolSizeLabel;
    private javax.swing.JTextField maxPoolSizeTextField;
    private javax.swing.JLabel poolIdleTimeoutInSecondsLabel;
    private javax.swing.JTextField poolIdleTimeoutInSecondsTextField;
    private javax.swing.JLabel resizeQuantityLabel;
    private javax.swing.JTextField resizeQuantityTextField;
    private javax.swing.JLabel steadyPoolSizeLabel;
    private javax.swing.JTextField steadyPoolSizeTextField;
    // End of variables declaration//GEN-END:variables
}
