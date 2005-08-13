/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.managerwizard;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.jmx.GenericWizardPanel;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.managerwizard.ManagerPopup;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class responsible for the Agent URL specification
 */
public class AgentURLPanel extends javax.swing.JPanel implements DocumentListener {
    
    private AgentURLWizardPanel wiz;
    private ResourceBundle bundle;
    private static WizardDescriptor wizDesc;
    
    //private boolean jmxURLcbxSelected = false;
    //private boolean rmiURLrbtnSelected = false;
    //private boolean freeFormURLrbtnSelected = false;
    
    private boolean securityCheckBoxSelected = false;
    private boolean userCredentialsRadioButtonSelected = false;
    private boolean codeExampleRadioButtonSelected = false;
    
    //private boolean hostFieldValid = true;
    //private boolean portFieldValid = true;
    private boolean userNameValid = true;
    private boolean userPasswordValid = true;
    
    
    /** 
     * Constructor
     * @param wiz the AgentURLWizardPanel to fill with user data
     */
    public AgentURLPanel(AgentURLWizardPanel wiz) {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(AgentURLPanel.class);
        
        initComponents();
        
        //jmxURLJTextField.setText(bundle.getString("TXT_rbtJmxAgent"));// NOI18N
        jmxURLJTextField.setSelectionStart(0);
        jmxURLJTextField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                ((JTextField)e.getSource()).selectAll();
            }
            
            public void focusLost(FocusEvent e) {
                ((JTextField)e.getSource()).setSelectionEnd(0);
            }
        });
        
        Mnemonics.setLocalizedText(patternJLabel,bundle.getString("TXT_jmxURLPattern"));// NOI18N
        
        Mnemonics.setLocalizedText(rmiUserNameJLabel,bundle.getString("LBL_userName.text"));// NOI18N
        Mnemonics.setLocalizedText(rmiPasswordJLabel,bundle.getString("LBL_password.text"));// NOI18N
        
        Mnemonics.setLocalizedText(jmxURLJLabel,bundle.getString("LBL_jtfJMXURL.text"));// NOI18N
        Mnemonics.setLocalizedText(securityJCheckBox,bundle.getString("LBL_jcbxSecurity.text"));// NOI18N
        Mnemonics.setLocalizedText(userCredentialsJRadioButton,bundle.getString("LBL_rbtnUserCredentials.text"));// NOI18N
        Mnemonics.setLocalizedText(codeExampleJRadioButton,bundle.getString("LBL_rbtnCodeExample.text"));// NOI18N
        Mnemonics.setLocalizedText(jmxAgentURLPopupJButton,bundle.getString("LBL_urlPopupBtn.text"));// NOI18N
        
        //for accessibility
        jmxURLJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_JMX_URL")); // NOI18N
        jmxURLJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_JMX_URL_DESCRIPTION")); // NOI18N
        jmxAgentURLPopupJButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_BUTTON_JMX_URL")); // NOI18N
        jmxAgentURLPopupJButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_BUTTON_JMX_URL_DESCRIPTION")); // NOI18N
        securityJCheckBox.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_AUTHENTICATED")); // NOI18N
        securityJCheckBox.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_AUTHENTICATED_DESCRIPTION")); // NOI18N
        codeExampleJRadioButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RADIOBUTTON_CODE_EXAMPLE")); // NOI18N
        codeExampleJRadioButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RADIOBUTTON_CODE_EXAMPLE_DESCRIPTION")); // NOI18N
        userCredentialsJRadioButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RADIOBUTTON_USER_CREDENTIALS")); // NOI18N
        userCredentialsJRadioButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RADIOBUTTON_USER_CREDENTIALS_DESCRIPTION")); // NOI18N
        rmiUserNameJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RMI_USER_NAME")); // NOI18N
        rmiUserNameJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RMI_USER_NAME_DESCRIPTION")); // NOI18N
        rmiPasswordJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RMI_USER_PASSWORD")); // NOI18N
        rmiPasswordJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RMI_USER_PASSWORD_DESCRIPTION")); // NOI18N
        
        // ensures that every time the text field changes, an event is thrown to the wiz
        rmiUserNameJTextField.getDocument().addDocumentListener(this);
        rmiPasswordJTextField.getDocument().addDocumentListener(this);
        
        //missing init flags
        //rmiURLrbtnSelected = rmiJMXAgentJRadioButton.isSelected();
        //freeFormURLrbtnSelected = jmxAgentURLJRadioButton.isSelected();
        setUserNameValidity(true);
        setUserPasswordValidity(true);
                
        // Provide a name in the title bar.
        setName(bundle.getString("LBL_AgentURL")); // NOI18N
    }
    
    /**
     * Setter for the host field validity variable
     * @param valid the boolean to set
     */
    /*
    public void setHostFieldValidity(boolean valid) {
        this.hostFieldValid = valid;
    }
    */
    /**
     * Getter for the host field validity variable
     * @return boolean the value of the variable
     */
    /*
    public boolean getHostFieldValidity() {
        return this.hostFieldValid;
    }
    */
    /**
     * Setter for the port field validity variable
     * @param valid the boolean to set
     */
    /*
    public void setPortFieldValidity(boolean valid) {
        this.portFieldValid = valid;
    }
    */
    /**
     * Getter for the port field validity variable
     * @return boolean the value of the variable
     */
    /*
    public boolean getPortFieldValidity() {
        return this.portFieldValid;
    }
    */
    /**
     * Setter for the user name field validity variable
     * @param valid the boolean to set
     */
    
    public void setUserNameValidity(boolean valid) {
        this.userNameValid = valid;
    }
    
    /**
     * Getter for the user name field validity variable
     * @return boolean the value of the variable
     */
    
    public boolean getUserNameValidity() {
        return this.userNameValid;
    }
    
    /**
     * Setter for the user password field validity variable
     * @param valid the boolean to set
     */
    
    public void setUserPasswordValidity(boolean valid) {
        this.userPasswordValid = valid;
    }
    
    /**
     * Getter for the user password field validity variable
     * @return boolean the value of the variable
     */
    
    public boolean getUserPasswordValidity() {
        return this.userPasswordValid;
    }
   
    
    public AgentURLPanel getPanel() {
        return this;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        internalJPanel = new javax.swing.JPanel();
        securityJCheckBox = new javax.swing.JCheckBox();
        credentialJSeparator = new javax.swing.JSeparator();
        userCredentialsJRadioButton = new javax.swing.JRadioButton();
        codeExampleJRadioButton = new javax.swing.JRadioButton();
        urlPanel = new javax.swing.JPanel();
        jmxURLJTextField = new javax.swing.JTextField();
        jmxURLJLabel = new javax.swing.JLabel();
        jmxAgentURLPopupJButton = new javax.swing.JButton();
        patternJLabel = new javax.swing.JLabel();
        rmiPasswordJLabel = new javax.swing.JLabel();
        rmiUserNameJTextField = new javax.swing.JTextField();
        rmiUserNameJLabel = new javax.swing.JLabel();
        rmiPasswordJTextField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        setName("agentURLOptionPanel");
        setPreferredSize(new java.awt.Dimension(327, 339));
        internalJPanel.setLayout(new java.awt.GridBagLayout());

        internalJPanel.setName("agentURLOptionPanel");
        securityJCheckBox.setSelected(true);
        securityJCheckBox.setName("securityCbx");
        securityJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                securityJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        internalJPanel.add(securityJCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 11);
        internalJPanel.add(credentialJSeparator, gridBagConstraints);

        buttonGroup2.add(userCredentialsJRadioButton);
        userCredentialsJRadioButton.setName("customCredentialRbtn");
        userCredentialsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userCredentialsJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 22, 0, 11);
        internalJPanel.add(userCredentialsJRadioButton, gridBagConstraints);

        buttonGroup2.add(codeExampleJRadioButton);
        codeExampleJRadioButton.setSelected(true);
        codeExampleJRadioButton.setName("sampleRbtn");
        codeExampleJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeExampleJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 22, 0, 11);
        internalJPanel.add(codeExampleJRadioButton, gridBagConstraints);

        urlPanel.setLayout(new java.awt.GridBagLayout());

        urlPanel.setName("urlPanel");
        jmxURLJTextField.setDragEnabled(true);
        jmxURLJTextField.setMinimumSize(new java.awt.Dimension(250, 20));
        jmxURLJTextField.setName("customURLJTextField");
        jmxURLJTextField.setPreferredSize(new java.awt.Dimension(250, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        urlPanel.add(jmxURLJTextField, gridBagConstraints);

        jmxURLJLabel.setLabelFor(jmxURLJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        urlPanel.add(jmxURLJLabel, gridBagConstraints);

        jmxAgentURLPopupJButton.setName("RMIURLButton");
        jmxAgentURLPopupJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmxAgentURLPopupJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        urlPanel.add(jmxAgentURLPopupJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        urlPanel.add(patternJLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 11);
        internalJPanel.add(urlPanel, gridBagConstraints);

        rmiPasswordJLabel.setLabelFor(rmiPasswordJTextField);
        rmiPasswordJLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 44, 11, 0);
        internalJPanel.add(rmiPasswordJLabel, gridBagConstraints);

        rmiUserNameJTextField.setEnabled(false);
        rmiUserNameJTextField.setName("userNameJTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        internalJPanel.add(rmiUserNameJTextField, gridBagConstraints);

        rmiUserNameJLabel.setLabelFor(rmiUserNameJTextField);
        rmiUserNameJLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 44, 0, 0);
        internalJPanel.add(rmiUserNameJLabel, gridBagConstraints);

        rmiPasswordJTextField.setEnabled(false);
        rmiPasswordJTextField.setName("userPasswordJTextField");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 11, 11);
        internalJPanel.add(rmiPasswordJTextField, gridBagConstraints);

        add(internalJPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jmxAgentURLPopupJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmxAgentURLPopupJButtonActionPerformed
        ManagerPopup rmiPopup = new ManagerPopup(getPanel(), jmxURLJTextField);
    }//GEN-LAST:event_jmxAgentURLPopupJButtonActionPerformed

    private void codeExampleJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeExampleJRadioButtonActionPerformed
        codeExampleRadioButtonSelected = codeExampleJRadioButton.isSelected();
        
        rmiUserNameJLabel.setEnabled(false);
        rmiUserNameJTextField.setEnabled(false);
        rmiPasswordJLabel.setEnabled(false);
        rmiPasswordJTextField.setEnabled(false);
        
        wiz.event();
    }//GEN-LAST:event_codeExampleJRadioButtonActionPerformed

    private void userCredentialsJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userCredentialsJRadioButtonActionPerformed
        userCredentialsRadioButtonSelected = userCredentialsJRadioButton.isSelected();
        
        rmiUserNameJLabel.setEnabled(userCredentialsRadioButtonSelected);
        rmiUserNameJTextField.setEnabled(userCredentialsRadioButtonSelected);
        rmiPasswordJLabel.setEnabled(userCredentialsRadioButtonSelected);
        rmiPasswordJTextField.setEnabled(userCredentialsRadioButtonSelected);
        
        setUserNameValidity(!rmiUserNameJTextField.getText().equals(WizardConstants.EMPTYSTRING));
        setUserPasswordValidity(!rmiPasswordJTextField.getText().equals(WizardConstants.EMPTYSTRING));
        
        wiz.event();
    }//GEN-LAST:event_userCredentialsJRadioButtonActionPerformed

    private void securityJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_securityJCheckBoxActionPerformed
        securityCheckBoxSelected = securityJCheckBox.isSelected();
        
        if (securityCheckBoxSelected) {
            // enable the two rbtn by default
            codeExampleJRadioButton.setEnabled(true);
            userCredentialsJRadioButton.setEnabled(true);
            
            if (userCredentialsJRadioButton.isSelected()) {
                rmiUserNameJLabel.setEnabled(true);
                rmiUserNameJTextField.setEnabled(true);
                rmiPasswordJLabel.setEnabled(true);
                rmiPasswordJTextField.setEnabled(true);
            } 
            
        } else {
            codeExampleJRadioButton.setEnabled(false);
            userCredentialsJRadioButton.setEnabled(false);
            rmiUserNameJLabel.setEnabled(false);
            rmiUserNameJTextField.setEnabled(false);
            rmiPasswordJLabel.setEnabled(false);
            rmiPasswordJTextField.setEnabled(false);
        }
        
        wiz.event();
    }//GEN-LAST:event_securityJCheckBoxActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JRadioButton codeExampleJRadioButton;
    private javax.swing.JSeparator credentialJSeparator;
    private javax.swing.JPanel internalJPanel;
    private javax.swing.JButton jmxAgentURLPopupJButton;
    private javax.swing.JLabel jmxURLJLabel;
    private javax.swing.JTextField jmxURLJTextField;
    private javax.swing.JLabel patternJLabel;
    private javax.swing.JLabel rmiPasswordJLabel;
    private javax.swing.JTextField rmiPasswordJTextField;
    private javax.swing.JLabel rmiUserNameJLabel;
    private javax.swing.JTextField rmiUserNameJTextField;
    private javax.swing.JCheckBox securityJCheckBox;
    private javax.swing.JPanel urlPanel;
    private javax.swing.JRadioButton userCredentialsJRadioButton;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Static class which defines the wizard panel associated to the AgentURL
     * Panel
     */
    public static class AgentURLWizardPanel extends GenericWizardPanel 
        implements org.openide.WizardDescriptor.FinishablePanel {
        private AgentURLPanel panel = null;
        private WizardDescriptor wiz = null;
        
        /**
         * Returns the manager panel.
         * @return <CODE>Component</CODE> the manager panel
         */
        public Component getComponent () { return getPanel(); }
        
        /**
         * Returns the panel
         * @return AgentURLPanel
         */
        private AgentURLPanel getPanel() 
        {
            if (panel == null) {
                panel = new AgentURLPanel(this);
            }
            return panel;
        }
        
        /**
         * Method which fires an event to notify that there was a change in 
         * the data
         */
        public void event() {
            fireChangeEvent();
        }
        
        /**
         * Overriden method
         */
        public boolean isValid() {
            
            boolean credentials = true;
            
            String msg = WizardConstants.EMPTY_STRING;
            
            if (getPanel() != null) {
                        if (isSecurityCheckBoxSelected() &&
                                isUserCredentialRbtnSelected()) { //box checked
                            if (!getPanel().getUserNameValidity()) {
                                credentials = false;
                                //msg = NbBundle.getMessage(AgentURLPanel.class,"LBL_State_UserName_Incorrect");// NOI18N
                                msg = getPanel().bundle.getString("LBL_State_UserName_Incorrect");// NOI18N
                            }
                            if (!getPanel().getUserPasswordValidity()) {
                                credentials = false;
                                //msg = NbBundle.getMessage(AgentURLPanel.class,"LBL_State_UserPassword_Incorrect");// NOI18N
                                msg = getPanel().bundle.getString("LBL_State_UserPassword_Incorrect");// NOI18N
                            }
                        }
                setErrorMsg(msg);
            }
         
            return credentials;
        }
        
        /**
         * Implementation of the FinishablePanel Interface; provides the Finish
         * Button to be always enabled
         * @return finish true if the panel can be the last one and enables 
         * the finish button
         */
        public boolean isFinishPanel() { return true;}
        
        /**
         * Displays the given message in the wizard's message area.
         *
         * @param  message  message to be displayed, or <code>null</code>
         *                  if the message area should be cleared
         */
        private void setErrorMsg(String message) {
            if (wiz != null) {
                wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE,
                        message);
            }
        }
        
        /**
         * Returns if the user wants to generate authentication code
         * @return <CODE>boolean</CODE> true if the security checkBox is 
         * selected
         */
        public boolean isSecurityCheckBoxSelected() {
            return getPanel().securityJCheckBox.isSelected();
        }
        
        /**
         * Returns if the user wants to add custom user name and password
         * @return <CODE>boolean</CODE> true if the user credentials rbtn is 
         * selected
         */
        public boolean isUserCredentialRbtnSelected() {
            return getPanel().userCredentialsJRadioButton.isSelected();
        }
        
        /**
         * Returns if the user wants to generate sample credential code
         * @return <CODE>boolean</CODE> true if the sample credentials rbtn is 
         * selected
         */
        public boolean isSampleCredentialRbtnSelected() {
            return getPanel().codeExampleJRadioButton.isSelected();
        }
        
        /**
         * This method is called when a step is loaded.
         * @param settings <CODE>Object</CODE> an object containing the wizard informations.
         */
        public void readSettings (Object settings) 
        {
            wiz = (WizardDescriptor) settings;
            wiz.putProperty(WizardConstants.WIZARD_ERROR_MESSAGE, WizardConstants.EMPTY_STRING);
            //getPanel().mainClassJCheckBox.setEnabled(shouldEnableMainProjectClass());
        }
        
        /**
         * This method is called when the user quit a step.
         * @param settings <CODE>Object</CODE> an object containing the wizard informations.
         */
        public void storeSettings(Object settings) {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            // sets the initial properties to know which combination of
            // checkboxes/ radiobuttons has been selected
            
            wiz.putProperty(WizardConstants.PROP_MANAGER_AGENT_URL,
                    getPanel().jmxURLJTextField.getText());
            
            // storage for the security part of the panel
            wiz.putProperty(WizardConstants.PROP_MANAGER_SECURITY_SELECTED,
                    new Boolean(isSecurityCheckBoxSelected()));
            
            if (isSecurityCheckBoxSelected()) { // user added security
                
                wiz.putProperty(WizardConstants.PROP_MANAGER_CREDENTIAL_SAMPLE_SELECTED,
                        new Boolean(isSampleCredentialRbtnSelected()));
                wiz.putProperty(WizardConstants.PROP_MANAGER_USER_CREDENTIAL_SELECTED,
                        new Boolean(isUserCredentialRbtnSelected()));
                
                if (isUserCredentialRbtnSelected()) { //user provided name & passwd
                    wiz.putProperty(WizardConstants.PROP_MANAGER_USER_NAME,
                            getPanel().rmiUserNameJTextField.getText());
                    wiz.putProperty(WizardConstants.PROP_MANAGER_USER_PASSWORD,
                            getPanel().rmiPasswordJTextField.getText());
                }
            }
        }
        
        /**
         * Returns the corresponding help context.
         * @return <CODE>HelpCtx</CODE> the corresponding help context.
         */
        public HelpCtx getHelp() {
           return new HelpCtx("jmx_manager_app");// NOI18N
        }
      
    }
    
    // methods to implement from DocumentListener Interface
            public void insertUpdate(DocumentEvent e) {
                setUserNameValidity(!rmiUserNameJTextField.getText().equals(WizardConstants.EMPTYSTRING));
                setUserPasswordValidity(!rmiPasswordJTextField.getText().equals(WizardConstants.EMPTYSTRING));
                wiz.event();
            }
            
            public void removeUpdate(DocumentEvent e) {
                setUserNameValidity(!rmiUserNameJTextField.getText().equals(WizardConstants.EMPTYSTRING));
                setUserPasswordValidity(!rmiPasswordJTextField.getText().equals(WizardConstants.EMPTYSTRING));
                wiz.event();
            }
            
            public void changedUpdate(DocumentEvent e) {
                setUserNameValidity(!rmiUserNameJTextField.getText().equals(WizardConstants.EMPTYSTRING));
                setUserPasswordValidity(!rmiPasswordJTextField.getText().equals(WizardConstants.EMPTYSTRING));
                wiz.event();
            }
}
