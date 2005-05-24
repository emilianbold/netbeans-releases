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
 * BeanCachePanel.java        October 22, 2003, 1:07 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupportClient;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class BeanCachePanel extends javax.swing.JPanel 
        implements ErrorSupportClient {

    private EjbCustomizer customizer;
    protected ErrorSupport errorSupport;
    protected ValidationSupport validationSupport;
    
    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N


    /** Creates new form BeanCachePanel */
    public BeanCachePanel(EjbCustomizer customizer){
        initComponents();
        this.customizer = customizer;
        errorSupport = new ErrorSupport(this);
        validationSupport = new ValidationSupport();
    }


    public void setValues(BeanCache beanCache){
        if(beanCache != null){
            maxCacheSizeTextField.setText(beanCache.getMaxCacheSize());
            resizeQuantityTextField.setText(beanCache.getResizeQuantity());
            isCacheOverflowAllowedComboBox.setSelectedItem(
                beanCache.getIsCacheOverflowAllowed());
            cacheIdleTimeoutInSecondsTextField.setText(
                beanCache.getCacheIdleTimeoutInSeconds());
            removalTimeoutInSecondsTextField.setText(
                beanCache.getRemovalTimeoutInSeconds());
            victimSelectionPolicyComboBox.setSelectedItem(
                beanCache.getVictimSelectionPolicy());
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
        gridBagConstraints.gridy = 6;
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
        
        //Bean Cache fields Validation
        String property = maxCacheSizeTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-cache/max-cache-size", //NOI18N
                bundle.getString("LBL_Max_Cache_Size")));           //NOI18N

        property = resizeQuantityTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-cache/resize-quantity", //NOI18N
            bundle.getString("LBL_Resize_Quantity")));          //NOI18N


        property = (String)isCacheOverflowAllowedComboBox.getSelectedItem();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-cache/is-cache-overflow-allowed", //NOI18N
                bundle.getString("LBL_Is_Cache_Overflow_Allowed")));        //NOI18N

        property = cacheIdleTimeoutInSecondsTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-cache/cache-idle-timeout-in-seconds", //NOI18N
                bundle.getString("LBL_Cache_Idle_Timeout_In_Seconds")));    //NOI18N
            
        property = removalTimeoutInSecondsTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-cache/removal-timeout-in-seconds", //NOI18N
                bundle.getString("LBL_Removal_Timeout_In_Seconds")));       //NOI18N

        property = (String)victimSelectionPolicyComboBox.getSelectedItem();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/ejb/bean-cache/victim-selection-policy", //NOI18N
                bundle.getString("LBL_Victim_Selection_Policy")));  //NOI18N
            
        return errors;
    }
	
	public java.awt.Color getMessageForegroundColor() {
		return BeanCustomizer.ErrorTextForegroundColor;
	}
	
    private void validateEntries(){
        if(errorSupport != null){
            errorSupport.showErrors();
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

        resizeQuantityLabel = new javax.swing.JLabel();
        resizeQuantityTextField = new javax.swing.JTextField();
        maxCacheSizeTextField = new javax.swing.JTextField();
        maxCacheSizeLabel = new javax.swing.JLabel();
        isCacheOverflowAllowedLabel = new javax.swing.JLabel();
        isCacheOverflowAllowedComboBox = new javax.swing.JComboBox();
        cacheIdleTimeoutInSecondsLabel = new javax.swing.JLabel();
        cacheIdleTimeoutInSecondsTextField = new javax.swing.JTextField();
        removalTimeoutInSecondsLabel = new javax.swing.JLabel();
        removalTimeoutInSecondsTextField = new javax.swing.JTextField();
        victimSelectionPolicyLabel = new javax.swing.JLabel();
        victimSelectionPolicyComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

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
        resizeQuantityLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Cache_Resize_Quantity_Acsbl_Desc"));

        resizeQuantityTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Cache_Resize_Quantity_Tool_Tip"));
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
        resizeQuantityTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Bean_Cache_Resize_Quantity_Acsbl_Desc"));

        maxCacheSizeTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Tool_Tip"));
        maxCacheSizeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxCacheSizeActionPerformed(evt);
            }
        });
        maxCacheSizeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                maxCacheSizeFocusGained(evt);
            }
        });
        maxCacheSizeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxCacheSizeKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(maxCacheSizeTextField, gridBagConstraints);
        maxCacheSizeTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Name"));
        maxCacheSizeTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Desc"));

        maxCacheSizeLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Max_Cache_Size").charAt(0));
        maxCacheSizeLabel.setLabelFor(maxCacheSizeTextField);
        maxCacheSizeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Max_Cache_Size_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(maxCacheSizeLabel, gridBagConstraints);
        maxCacheSizeLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Name"));
        maxCacheSizeLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Max_Cache_Size_Acsbl_Desc"));

        isCacheOverflowAllowedLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Is_Cache_Overflow_Allowed").charAt(0));
        isCacheOverflowAllowedLabel.setLabelFor(isCacheOverflowAllowedComboBox);
        isCacheOverflowAllowedLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Is_Cache_Overflow_Allowed_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(isCacheOverflowAllowedLabel, gridBagConstraints);
        isCacheOverflowAllowedLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Name"));
        isCacheOverflowAllowedLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Desc"));

        isCacheOverflowAllowedComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        isCacheOverflowAllowedComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Tool_Tip"));
        isCacheOverflowAllowedComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                isCacheOverflowAllowedItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(isCacheOverflowAllowedComboBox, gridBagConstraints);
        isCacheOverflowAllowedComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Name"));
        isCacheOverflowAllowedComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Is_Cache_Overflow_Allowed_Acsbl_Desc"));

        cacheIdleTimeoutInSecondsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Cache_Idle_Timeout_In_Seconds").charAt(0));
        cacheIdleTimeoutInSecondsLabel.setLabelFor(cacheIdleTimeoutInSecondsTextField);
        cacheIdleTimeoutInSecondsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Cache_Idle_Timeout_In_Seconds_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(cacheIdleTimeoutInSecondsLabel, gridBagConstraints);
        cacheIdleTimeoutInSecondsLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Name"));
        cacheIdleTimeoutInSecondsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Desc"));

        cacheIdleTimeoutInSecondsTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Tool_Tip"));
        cacheIdleTimeoutInSecondsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cacheIdleTimeoutInSecondsActionPerformed(evt);
            }
        });
        cacheIdleTimeoutInSecondsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cacheIdleTimeoutInSecondsFocusGained(evt);
            }
        });
        cacheIdleTimeoutInSecondsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cacheIdleTimeoutInSecondsKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(cacheIdleTimeoutInSecondsTextField, gridBagConstraints);
        cacheIdleTimeoutInSecondsTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Name"));
        cacheIdleTimeoutInSecondsTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cache_Idle_Timeout_In_Seconds_Acsbl_Desc"));

        removalTimeoutInSecondsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Removal_Timeout_In_Seconds").charAt(0));
        removalTimeoutInSecondsLabel.setLabelFor(removalTimeoutInSecondsTextField);
        removalTimeoutInSecondsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Removal_Timeout_In_Seconds_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(removalTimeoutInSecondsLabel, gridBagConstraints);
        removalTimeoutInSecondsLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Name"));
        removalTimeoutInSecondsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Desc"));

        removalTimeoutInSecondsTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Tool_Tip"));
        removalTimeoutInSecondsTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removalTimeoutInSecondsActionPerformed(evt);
            }
        });
        removalTimeoutInSecondsTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                removalTimeoutInSecondsFocusGained(evt);
            }
        });
        removalTimeoutInSecondsTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                removalTimeoutInSecondsKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(removalTimeoutInSecondsTextField, gridBagConstraints);
        removalTimeoutInSecondsTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Name"));
        removalTimeoutInSecondsTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Removal_Timeout_In_Seconds_Acsbl_Desc"));

        victimSelectionPolicyLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Victim_Selection_Policy").charAt(0));
        victimSelectionPolicyLabel.setLabelFor(victimSelectionPolicyComboBox);
        victimSelectionPolicyLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Victim_Selection_Policy_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(victimSelectionPolicyLabel, gridBagConstraints);
        victimSelectionPolicyLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Name"));
        victimSelectionPolicyLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Desc"));

        victimSelectionPolicyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "FIFO", "LRU", "NRU" }));
        victimSelectionPolicyComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Tool_Tip"));
        victimSelectionPolicyComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                victimSelectionPolicyItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 72;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        add(victimSelectionPolicyComboBox, gridBagConstraints);
        victimSelectionPolicyComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Name"));
        victimSelectionPolicyComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Victim_Selection_Policy_Acsbl_Desc"));

    }
    // </editor-fold>//GEN-END:initComponents

    private void removalTimeoutInSecondsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_removalTimeoutInSecondsFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_removalTimeoutInSecondsFocusGained

    private void removalTimeoutInSecondsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removalTimeoutInSecondsActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_removalTimeoutInSecondsActionPerformed

    private void cacheIdleTimeoutInSecondsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cacheIdleTimeoutInSecondsActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_cacheIdleTimeoutInSecondsActionPerformed

    private void cacheIdleTimeoutInSecondsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_cacheIdleTimeoutInSecondsFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_cacheIdleTimeoutInSecondsFocusGained

    private void maxCacheSizeFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_maxCacheSizeFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_maxCacheSizeFocusGained

    private void resizeQuantityFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_resizeQuantityFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_resizeQuantityFocusGained

    private void resizeQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resizeQuantityActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_resizeQuantityActionPerformed

    private void maxCacheSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxCacheSizeActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_maxCacheSizeActionPerformed

    private void victimSelectionPolicyItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_victimSelectionPolicyItemStateChanged
        // Add your handling code here:
        String item = (String)victimSelectionPolicyComboBox.getSelectedItem();
        customizer.updateVictimSelectionPolicy(item);
        validateEntries();
    }//GEN-LAST:event_victimSelectionPolicyItemStateChanged

    private void removalTimeoutInSecondsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_removalTimeoutInSecondsKeyReleased
        // Add your handling code here:
        String item = removalTimeoutInSecondsTextField.getText();
        customizer.updateRemovalTimeoutInSeconds(item);
        validateEntries();
    }//GEN-LAST:event_removalTimeoutInSecondsKeyReleased

    private void cacheIdleTimeoutInSecondsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cacheIdleTimeoutInSecondsKeyReleased
        // Add your handling code here:
        String item = cacheIdleTimeoutInSecondsTextField.getText();
        customizer.updateCacheIdleTimeoutInSeconds(item);
        validateEntries();
    }//GEN-LAST:event_cacheIdleTimeoutInSecondsKeyReleased

    private void isCacheOverflowAllowedItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_isCacheOverflowAllowedItemStateChanged
        // Add your handling code here:
        String item = (String)isCacheOverflowAllowedComboBox.getSelectedItem();
        customizer.updateIsCacheOverflowAllowed(item);
        validateEntries();
    }//GEN-LAST:event_isCacheOverflowAllowedItemStateChanged

    private void resizeQuantityKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_resizeQuantityKeyReleased
        // Add your handling code here:
        String item = resizeQuantityTextField.getText();
        customizer.updateCacheResizeQuantity(item);
        validateEntries();
    }//GEN-LAST:event_resizeQuantityKeyReleased

    private void maxCacheSizeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxCacheSizeKeyReleased
        // Add your handling code here:
        String item = maxCacheSizeTextField.getText();
        customizer.updateMaxCacheSize(item);
        validateEntries();
    }//GEN-LAST:event_maxCacheSizeKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cacheIdleTimeoutInSecondsLabel;
    private javax.swing.JTextField cacheIdleTimeoutInSecondsTextField;
    private javax.swing.JComboBox isCacheOverflowAllowedComboBox;
    private javax.swing.JLabel isCacheOverflowAllowedLabel;
    private javax.swing.JLabel maxCacheSizeLabel;
    private javax.swing.JTextField maxCacheSizeTextField;
    private javax.swing.JLabel removalTimeoutInSecondsLabel;
    private javax.swing.JTextField removalTimeoutInSecondsTextField;
    private javax.swing.JLabel resizeQuantityLabel;
    private javax.swing.JTextField resizeQuantityTextField;
    private javax.swing.JComboBox victimSelectionPolicyComboBox;
    private javax.swing.JLabel victimSelectionPolicyLabel;
    // End of variables declaration//GEN-END:variables
    
}
