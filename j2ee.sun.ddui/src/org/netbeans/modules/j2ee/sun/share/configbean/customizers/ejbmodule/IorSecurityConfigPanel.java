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
 * IorSecurityConfigPanel.java        October 20, 2003, 2:34 PM
 *
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig;
import org.netbeans.modules.j2ee.sun.share.configbean.BaseEjb;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupport;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ErrorSupportClient;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.ValidationSupport;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class IorSecurityConfigPanel extends javax.swing.JPanel 
        implements ErrorSupportClient {

    private EjbCustomizer customizer;
    
    protected ErrorSupport errorSupport;
    protected ValidationSupport validationSupport;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N

    
    /** Creates new form IorSecurityConfigPanel */
    public IorSecurityConfigPanel(EjbCustomizer customizer) {
        initComponents();
        this.customizer = customizer;
        errorSupport = new ErrorSupport(this);
        validationSupport = new ValidationSupport();
    }


    public void setValues(IorSecurityConfig iorSecCfg){
        if(iorSecCfg != null){
            TransportConfig transportConfig = iorSecCfg.getTransportConfig();
            if(transportConfig != null){
                integrityComboBox.setSelectedItem(
                    transportConfig.getIntegrity());
                
                confidentialityComboBox.setSelectedItem(
                    transportConfig.getConfidentiality());

                estbTrstInTrgtComboBox.setSelectedItem(
                    transportConfig.getEstablishTrustInTarget());

                estbTrstInClntComboBox.setSelectedItem(
                    transportConfig.getEstablishTrustInClient());
            }

            AsContext asContext = iorSecCfg.getAsContext();
            if(asContext != null){
                requiredComboBox.setSelectedItem(asContext.getRequired());
                authMethodComboBox.setSelectedItem(asContext.getAuthMethod());
                realmTextField.setText(asContext.getRealm());
            }

            SasContext sasContext = iorSecCfg.getSasContext();
            if(sasContext != null){
                callerPropagationComboBox.setSelectedItem(
                    sasContext.getCallerPropagation());
            }
        }
    }

    public java.awt.Container getErrorPanelParent(){
        return this;
    }


    public java.awt.GridBagConstraints getErrorPanelConstraints(){
        java.awt.GridBagConstraints gridBagConstraints = 
            new java.awt.GridBagConstraints();
        
        gridBagConstraints.anchor = gridBagConstraints.CENTER;
        gridBagConstraints.fill = gridBagConstraints.BOTH;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets.top = 0;
        gridBagConstraints.insets.left = 4;
        gridBagConstraints.insets.bottom = 5;
        gridBagConstraints.insets.right = 5;
        gridBagConstraints.ipadx = 0;
        gridBagConstraints.ipady = 0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;

        return gridBagConstraints;
    }


    public java.util.Collection getErrors(){
        if(validationSupport == null) assert(false);
        ArrayList errors = new ArrayList();
         String property;
        
        //Transport Config fields Validation
        boolean transportConfigPresent = isTransportConfigPresent();
        if(transportConfigPresent){
            property = (String)integrityComboBox.getSelectedItem();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/transport-config/integrity", //NOI18N
                    bundle.getString("LBL_Integrity")));                //NOI18N

            property = (String)confidentialityComboBox.getSelectedItem();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/transport-config/confidentiality", //NOI18N
                    bundle.getString("LBL_Confidentiality")));          //NOI18N

            property = (String)estbTrstInTrgtComboBox.getSelectedItem();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/transport-config/establish-trust-in-target", //NOI18N
                    bundle.getString("LBL_Establish_Trust_In_Target")));//NOI18N

            property = (String)estbTrstInClntComboBox.getSelectedItem();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/transport-config/establish-trust-in-client", //NOI18N
                    bundle.getString("LBL_Establish_Trust_In_Client")));//NOI18N
        }

        //As Context fields Validation
        boolean asContextPresent = isAsContextPresent();
        if(asContextPresent){
            property = (String)requiredComboBox.getSelectedItem();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/as-context/required", //NOI18N
                    bundle.getString("LBL_Required")));                 //NOI18N

            property = realmTextField.getText();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/as-context/realm", //NOI18N
                    bundle.getString("LBL_Realm")));                    //NOI18N

            property = (String)authMethodComboBox.getSelectedItem();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/as-context/auth-method", //NOI18N
                    bundle.getString("LBL_Auth_Method")));              //NOI18N
        }            
            
        //Sas Context fields Validation
        boolean sasContextPresent = isSasContextPresent();
        if(sasContextPresent){
            property = (String)callerPropagationComboBox.getSelectedItem();
            errors.addAll(validationSupport.validate(property,
                "/sun-ejb-jar/enterprise-beans/ejb/ior-security-config/sas-context/caller-propagation", //NOI18N
                    bundle.getString("LBL_Caller_Propagation")));       //NOI18N
        }

        return errors;
    }
	
	public java.awt.Color getMessageForegroundColor() {
		return BeanCustomizer.ErrorTextForegroundColor;
	}


    private boolean isTransportConfigPresent(){
        boolean transportConfigPresent = false;
        String property = (String)integrityComboBox.getSelectedItem();
        while(true){
            if((property != null) && (property.length() != 0)){
                transportConfigPresent = true;
                break;
            }

            property = (String)confidentialityComboBox.getSelectedItem();
            if((property != null) && (property.length() != 0)){
                transportConfigPresent = true;
                break;
            }

            property = (String)estbTrstInTrgtComboBox.getSelectedItem();
            if((property != null) && (property.length() != 0)){
                transportConfigPresent = true;
                break;
            }

            property = (String)estbTrstInClntComboBox.getSelectedItem();
            if((property != null) && (property.length() != 0)){
                transportConfigPresent = true;
                break;
            }
            break;
        }
        return transportConfigPresent;
    }


    private boolean isAsContextPresent(){
        boolean asContextPresent = false;
        String property = (String)requiredComboBox.getSelectedItem();
        while(true){
            if((property != null) && (property.length() != 0)){
                asContextPresent = true;
                break;
            }

            property = realmTextField.getText();
            if((property != null) && (property.length() != 0)){
                asContextPresent = true;
                break;
            }

            property = (String)authMethodComboBox.getSelectedItem();
            if((property != null) && (property.length() != 0)){
                asContextPresent = true;
                break;
            }
            break;
        }
        return asContextPresent;
    }


    private boolean isSasContextPresent(){
        boolean sasContextPresent = false;
        String property = (String)callerPropagationComboBox.getSelectedItem();
        while(true){
            if((property != null) && (property.length() != 0)){
                sasContextPresent = true;
                break;
            }
            break;
        }
        return sasContextPresent;
    }


    private void validateEntries(){
        if(errorSupport != null){
            errorSupport.showErrors();
            firePropertyChange("", null, null);
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

        asContextPanel = new javax.swing.JPanel();
        requiredComboBox = new javax.swing.JComboBox();
        authMethodLabel = new javax.swing.JLabel();
        authMethodComboBox = new javax.swing.JComboBox();
        realmLabel = new javax.swing.JLabel();
        requiredLabel = new javax.swing.JLabel();
        realmTextField = new javax.swing.JTextField();
        sasContextPanel = new javax.swing.JPanel();
        callerPropagationComboBox = new javax.swing.JComboBox();
        callerPropagationLabel = new javax.swing.JLabel();
        transportConfigLabel = new javax.swing.JLabel();
        asContextLabel = new javax.swing.JLabel();
        sasContextLabel = new javax.swing.JLabel();
        transportConfigPanel = new javax.swing.JPanel();
        confidentialityLabel1 = new javax.swing.JLabel();
        transportConfigPanelPanel1 = new javax.swing.JPanel();
        confidentialityLabel2 = new javax.swing.JLabel();
        integrityLabel = new javax.swing.JLabel();
        integrityComboBox = new javax.swing.JComboBox();
        confidentialityComboBox = new javax.swing.JComboBox();
        confidentialityLabel = new javax.swing.JLabel();
        transportConfigPanel2 = new javax.swing.JPanel();
        confidentialityLabel3 = new javax.swing.JLabel();
        estbTrstInTrgtLabel = new javax.swing.JLabel();
        estbTrstInClntLabel = new javax.swing.JLabel();
        estbTrstInTrgtComboBox = new javax.swing.JComboBox();
        estbTrstInClntComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(""));
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        asContextPanel.setLayout(new java.awt.GridBagLayout());

        asContextPanel.setBorder(new javax.swing.border.EtchedBorder());
        requiredComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        requiredComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Required_Tool_Tip"));
        requiredComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                requiredStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 15);
        asContextPanel.add(requiredComboBox, gridBagConstraints);
        requiredComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Required_Acsbl_Name"));
        requiredComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Required_Acsbl_Desc"));

        authMethodLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Auth_Method").charAt(0));
        authMethodLabel.setLabelFor(authMethodComboBox);
        authMethodLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Auth_Method_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        asContextPanel.add(authMethodLabel, gridBagConstraints);
        authMethodLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Auth_Method_Acsbl_Name"));
        authMethodLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Auth_Method_Acsbl_Desc"));

        authMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "USERNAME_PASSWORD" }));
        authMethodComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Auth_Method_Tool_Tip"));
        authMethodComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                authMethodStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        asContextPanel.add(authMethodComboBox, gridBagConstraints);
        authMethodComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Auth_Method_Acsbl_Name"));
        authMethodComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Auth_Method_Acsbl_Desc"));

        realmLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Auth_Method").charAt(0));
        realmLabel.setLabelFor(realmTextField);
        realmLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Realm_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        asContextPanel.add(realmLabel, gridBagConstraints);
        realmLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Realm_Acsbl_Name"));
        realmLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Realm_Acsbl_Desc"));

        requiredLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Required").charAt(0));
        requiredLabel.setLabelFor(requiredComboBox);
        requiredLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Required_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        asContextPanel.add(requiredLabel, gridBagConstraints);
        requiredLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Required_Acsbl_Name"));
        requiredLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Required_Acsbl_Desc"));

        realmTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Realm_Tool_Tip"));
        realmTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realmActionPerformed(evt);
            }
        });
        realmTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                realmTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 56;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        asContextPanel.add(realmTextField, gridBagConstraints);
        realmTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Realm_Acsbl_Name"));
        realmTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Realm_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(asContextPanel, gridBagConstraints);

        sasContextPanel.setLayout(new java.awt.GridBagLayout());

        sasContextPanel.setBorder(new javax.swing.border.EtchedBorder());
        callerPropagationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "NONE", "SUPPORTED" }));
        callerPropagationComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Caller_Propagation_Tool_Tip"));
        callerPropagationComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                callerPropagationStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        sasContextPanel.add(callerPropagationComboBox, gridBagConstraints);
        callerPropagationComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Caller_Propagation_Acsbl_Name"));
        callerPropagationComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Caller_Propagation_Acsbl_Desc"));

        callerPropagationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Caller_Propagation").charAt(0));
        callerPropagationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        callerPropagationLabel.setLabelFor(callerPropagationComboBox);
        callerPropagationLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Caller_Propagation_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        sasContextPanel.add(callerPropagationLabel, gridBagConstraints);
        callerPropagationLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Caller_Propagation_Acsbl_Name"));
        callerPropagationLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Caller_Propagation_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(sasContextPanel, gridBagConstraints);

        transportConfigLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Transport_Config"));
        transportConfigLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 5);
        add(transportConfigLabel, gridBagConstraints);

        asContextLabel.setLabelFor(asContextPanel);
        asContextLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_As_Context"));
        asContextLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(asContextLabel, gridBagConstraints);

        sasContextLabel.setLabelFor(sasContextPanel);
        sasContextLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Sas_Context"));
        sasContextLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        add(sasContextLabel, gridBagConstraints);

        transportConfigPanel.setLayout(new java.awt.GridBagLayout());

        transportConfigPanel.setBorder(new javax.swing.border.EtchedBorder());
        confidentialityLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Confidentiality").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        transportConfigPanel.add(confidentialityLabel1, gridBagConstraints);

        transportConfigPanelPanel1.setLayout(new java.awt.GridBagLayout());

        transportConfigPanelPanel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        confidentialityLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Confidentiality").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        transportConfigPanelPanel1.add(confidentialityLabel2, gridBagConstraints);

        integrityLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Integrity").charAt(0));
        integrityLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Integrity_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        transportConfigPanelPanel1.add(integrityLabel, gridBagConstraints);
        integrityLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Integrity_Acsbl_Name"));
        integrityLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Integrity_Acsbl_Desc"));

        integrityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "NONE", "SUPPORTED", "REQUIRED" }));
        integrityComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Integrity_Tool_Tip"));
        integrityComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                integrityStateChanged(evt);
            }
        });
        integrityComboBox.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                integrityComboBoxFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 15);
        transportConfigPanelPanel1.add(integrityComboBox, gridBagConstraints);
        integrityComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Integrity_Acsbl_Name"));
        integrityComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Integrity_Acsbl_Desc"));

        confidentialityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "NONE", "SUPPORTED", "REQUIRED" }));
        confidentialityComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Confidentiality_Tool_Tip"));
        confidentialityComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                confidentialityStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 15);
        transportConfigPanelPanel1.add(confidentialityComboBox, gridBagConstraints);
        confidentialityComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Confidentiality_Acsbl_Name"));
        confidentialityComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Confidentiality_Acsbl_Desc"));

        confidentialityLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Confidentiality").charAt(0));
        confidentialityLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Confidentiality_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        transportConfigPanelPanel1.add(confidentialityLabel, gridBagConstraints);
        confidentialityLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Confidentiality_Acsbl_Name"));
        confidentialityLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Confidentiality_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        transportConfigPanel.add(transportConfigPanelPanel1, gridBagConstraints);

        transportConfigPanel2.setLayout(new java.awt.GridBagLayout());

        transportConfigPanel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        confidentialityLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Confidentiality").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        transportConfigPanel2.add(confidentialityLabel3, gridBagConstraints);

        estbTrstInTrgtLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Establish_Trust_In_Target").charAt(0));
        estbTrstInTrgtLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Establish_Trust_In_Target_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        transportConfigPanel2.add(estbTrstInTrgtLabel, gridBagConstraints);
        estbTrstInTrgtLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Target_Acsbl_Name"));
        estbTrstInTrgtLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Target_Acsbl_Desc"));

        estbTrstInClntLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Establish_Trust_In_Client").charAt(0));
        estbTrstInClntLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Establish_Trust_In_Client_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        transportConfigPanel2.add(estbTrstInClntLabel, gridBagConstraints);
        estbTrstInClntLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Client_Acsbl_Name"));
        estbTrstInClntLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Client_Acsbl_Desc"));

        estbTrstInTrgtComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "NONE", "SUPPORTED" }));
        estbTrstInTrgtComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Target_Tool_Tip"));
        estbTrstInTrgtComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                estbTrstInTrgtStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        transportConfigPanel2.add(estbTrstInTrgtComboBox, gridBagConstraints);
        estbTrstInTrgtComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Target_Acsbl_Name"));
        estbTrstInTrgtComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Target_Acsbl_Desc"));

        estbTrstInClntComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "NONE", "SUPPORTED", "REQUIRED" }));
        estbTrstInClntComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Client_Tool_Tip"));
        estbTrstInClntComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                estbTrstInClntStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        transportConfigPanel2.add(estbTrstInClntComboBox, gridBagConstraints);
        estbTrstInClntComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Client_Acsbl_Name"));
        estbTrstInClntComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Establish_Trust_In_Client_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        transportConfigPanel.add(transportConfigPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(transportConfigPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
// TODO add your handling code here:
        validateEntries();
    }//GEN-LAST:event_formFocusGained

    private void integrityComboBoxFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_integrityComboBoxFocusLost
// TODO add your handling code here:
    }//GEN-LAST:event_integrityComboBoxFocusLost

    private void estbTrstInClntStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_estbTrstInClntStateChanged
        // Add your handling code here:
        String item = (String)estbTrstInClntComboBox.getSelectedItem();
        customizer.updateEstbTrstInClnt(item);
        validateEntries();
    }//GEN-LAST:event_estbTrstInClntStateChanged

    private void estbTrstInTrgtStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_estbTrstInTrgtStateChanged
        // Add your handling code here:
        String item = (String)estbTrstInTrgtComboBox.getSelectedItem();
        customizer.updateEstbTrstInTrgt(item);
        validateEntries();
    }//GEN-LAST:event_estbTrstInTrgtStateChanged

    private void realmActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_realmActionPerformed
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_realmActionPerformed

    private void callerPropagationStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_callerPropagationStateChanged
        // Add your handling code here:
        String item = (String)callerPropagationComboBox.getSelectedItem();
        customizer.updateCallerPropagation(item);
        validateEntries();
    }//GEN-LAST:event_callerPropagationStateChanged

    private void realmTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_realmTextFieldKeyReleased
        // Add your handling code here:
        String item = realmTextField.getText();
        customizer.updateRealm(item);
        validateEntries();
    }//GEN-LAST:event_realmTextFieldKeyReleased

    private void authMethodStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_authMethodStateChanged
        // Add your handling code here:
        String item = (String)authMethodComboBox.getSelectedItem();
        customizer.updateAuthMethod(item);
        validateEntries();
    }//GEN-LAST:event_authMethodStateChanged

    private void requiredStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_requiredStateChanged
        // Add your handling code here:
        String item = (String)requiredComboBox.getSelectedItem();
        customizer.updateRequired(item);
        validateEntries();
    }//GEN-LAST:event_requiredStateChanged

    private void confidentialityStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_confidentialityStateChanged
        // Add your handling code here:
        String item = (String)confidentialityComboBox.getSelectedItem();
        customizer.updateConfidentiality(item);
        validateEntries();
    }//GEN-LAST:event_confidentialityStateChanged

    private void integrityStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_integrityStateChanged
        // Add your handling code here:
        String item = (String)integrityComboBox.getSelectedItem();
        customizer.updateIntegrity(item);
        validateEntries();
    }//GEN-LAST:event_integrityStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel asContextLabel;
    private javax.swing.JPanel asContextPanel;
    private javax.swing.JComboBox authMethodComboBox;
    private javax.swing.JLabel authMethodLabel;
    private javax.swing.JComboBox callerPropagationComboBox;
    private javax.swing.JLabel callerPropagationLabel;
    private javax.swing.JComboBox confidentialityComboBox;
    private javax.swing.JLabel confidentialityLabel;
    private javax.swing.JLabel confidentialityLabel1;
    private javax.swing.JLabel confidentialityLabel2;
    private javax.swing.JLabel confidentialityLabel3;
    private javax.swing.JComboBox estbTrstInClntComboBox;
    private javax.swing.JLabel estbTrstInClntLabel;
    private javax.swing.JComboBox estbTrstInTrgtComboBox;
    private javax.swing.JLabel estbTrstInTrgtLabel;
    private javax.swing.JComboBox integrityComboBox;
    private javax.swing.JLabel integrityLabel;
    private javax.swing.JLabel realmLabel;
    private javax.swing.JTextField realmTextField;
    private javax.swing.JComboBox requiredComboBox;
    private javax.swing.JLabel requiredLabel;
    private javax.swing.JLabel sasContextLabel;
    private javax.swing.JPanel sasContextPanel;
    private javax.swing.JLabel transportConfigLabel;
    private javax.swing.JPanel transportConfigPanel;
    private javax.swing.JPanel transportConfigPanel2;
    private javax.swing.JPanel transportConfigPanelPanel1;
    // End of variables declaration//GEN-END:variables
}
