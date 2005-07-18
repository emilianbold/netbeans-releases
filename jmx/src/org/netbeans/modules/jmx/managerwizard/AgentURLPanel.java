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
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.jmx.GenericWizardPanel;
import org.netbeans.modules.jmx.WizardConstants;
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
    
    private boolean jmxURLcbxSelected = false;
    private boolean rmiURLrbtnSelected = false;
    private boolean freeFormURLrbtnSelected = false;
    
    private boolean securityCheckBoxSelected = false;
    private boolean userCredentialsRadioButtonSelected = false;
    private boolean codeExampleRadioButtonSelected = false;
    
    private boolean hostFieldValid = true;
    private boolean portFieldValid = true;
    private boolean userNameValid = false;
    private boolean userPasswordValid = false;
    
    
    /** 
     * Constructor
     * @param wiz the AgentURLWizardPanel to fill with user data
     */
    public AgentURLPanel(AgentURLWizardPanel wiz) {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(AgentURLPanel.class);
        
       
        initComponents();
        
        //custom initialisation
        hostJLabel.setEnabled(false);
        portJLabel.setEnabled(false);
        jmxURLJTextField.setText(bundle.getString("TXT_rbtJmxAgent"));// NOI18N
        hostJTextField.setText(bundle.getString("TXT_host"));// NOI18N
        hostJLabel.setEnabled(true);
        portJTextField.setText(bundle.getString("TXT_port"));// NOI18N
        portJLabel.setEnabled(true);
        
        Mnemonics.setLocalizedText(rmiJMXAgentJRadioButton,bundle.getString("LBL_rbtRmiJmxAgent.text"));// NOI18N
        Mnemonics.setLocalizedText(hostJLabel,bundle.getString("LBL_host.text"));// NOI18N
        Mnemonics.setLocalizedText(portJLabel,bundle.getString("LBL_port.text"));// NOI18N
        Mnemonics.setLocalizedText(rmiUserNameJLabel,bundle.getString("LBL_userName.text"));// NOI18N
        Mnemonics.setLocalizedText(rmiPasswordJLabel,bundle.getString("LBL_password.text"));// NOI18N
        Mnemonics.setLocalizedText(jmxAgentURLJRadioButton,bundle.getString("LBL_rbtJmxAgent.text"));// NOI18N
        Mnemonics.setLocalizedText(jmxURLJLabel,bundle.getString("LBL_jtfJMXURL.text"));// NOI18N
        Mnemonics.setLocalizedText(securityJCheckBox,bundle.getString("LBL_jcbxSecurity.text"));// NOI18N
        Mnemonics.setLocalizedText(userCredentialsJRadioButton,bundle.getString("LBL_rbtnUserCredentials.text"));// NOI18N
        Mnemonics.setLocalizedText(codeExampleJRadioButton,bundle.getString("LBL_rbtnCodeExample.text"));// NOI18N
        
        // ensures that every time the text field changes, an event is thrown to the wiz
        rmiUserNameJTextField.getDocument().addDocumentListener(this);
        rmiPasswordJTextField.getDocument().addDocumentListener(this);
        
        //missing init flags
        rmiURLrbtnSelected = rmiJMXAgentJRadioButton.isSelected();
        freeFormURLrbtnSelected = jmxAgentURLJRadioButton.isSelected();
                
        // Provide a name in the title bar.
        setName(NbBundle.getMessage(AgentURLPanel.class, "LBL_AgentURL_Panel")); // NOI18N
    }
    
    /**
     * Setter for the host field validity variable
     * @param valid the boolean to set
     */
    public void setHostFieldValidity(boolean valid) {
        this.hostFieldValid = valid;
    }
    
    /**
     * Getter for the host field validity variable
     * @return boolean the value of the variable
     */
    public boolean getHostFieldValidity() {
        return this.hostFieldValid;
    }
    
    /**
     * Setter for the port field validity variable
     * @param valid the boolean to set
     */
    public void setPortFieldValidity(boolean valid) {
        this.portFieldValid = valid;
    }
    
    /**
     * Getter for the port field validity variable
     * @return boolean the value of the variable
     */
    public boolean getPortFieldValidity() {
        return this.portFieldValid;
    }
    
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
        jmxURLJTextField = new javax.swing.JTextField();
        jmxAgentURLJRadioButton = new javax.swing.JRadioButton();
        rmiJMXAgentJRadioButton = new javax.swing.JRadioButton();
        hostJLabel = new javax.swing.JLabel();
        hostJTextField = new javax.swing.JTextField();
        portJTextField = new javax.swing.JTextField();
        portJLabel = new javax.swing.JLabel();
        rmiUserNameJLabel = new javax.swing.JLabel();
        rmiPasswordJLabel = new javax.swing.JLabel();
        rmiPasswordJTextField = new javax.swing.JTextField();
        rmiUserNameJTextField = new javax.swing.JTextField();
        jmxURLJLabel = new javax.swing.JLabel();
        securityJCheckBox = new javax.swing.JCheckBox();
        credentialJSeparator = new javax.swing.JSeparator();
        userCredentialsJRadioButton = new javax.swing.JRadioButton();
        codeExampleJRadioButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.BorderLayout());

        setName("agentURLOptionPanel");
        setPreferredSize(new java.awt.Dimension(327, 339));
        internalJPanel.setLayout(new java.awt.GridBagLayout());

        internalJPanel.setName("agentURLOptionPanel");
        jmxURLJTextField.setDragEnabled(true);
        jmxURLJTextField.setEnabled(false);
        jmxURLJTextField.setMinimumSize(new java.awt.Dimension(250, 20));
        jmxURLJTextField.setName("customURLJTextField");
        jmxURLJTextField.setPreferredSize(new java.awt.Dimension(250, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 12);
        internalJPanel.add(jmxURLJTextField, gridBagConstraints);

        buttonGroup1.add(jmxAgentURLJRadioButton);
        jmxAgentURLJRadioButton.setName("customProtocolRbtn");
        jmxAgentURLJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmxAgentURLJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
        internalJPanel.add(jmxAgentURLJRadioButton, gridBagConstraints);

        buttonGroup1.add(rmiJMXAgentJRadioButton);
        rmiJMXAgentJRadioButton.setSelected(true);
        rmiJMXAgentJRadioButton.setName("rmiProtocolRbtn");
        rmiJMXAgentJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rmiJMXAgentJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
        internalJPanel.add(rmiJMXAgentJRadioButton, gridBagConstraints);

        hostJLabel.setLabelFor(hostJTextField);
        hostJLabel.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 33, 0, 0);
        internalJPanel.add(hostJLabel, gridBagConstraints);

        hostJTextField.setMinimumSize(new java.awt.Dimension(90, 20));
        hostJTextField.setName("hostJTextField");
        hostJTextField.setPreferredSize(new java.awt.Dimension(90, 20));
        hostJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hostJTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 12);
        internalJPanel.add(hostJTextField, gridBagConstraints);

        portJTextField.setMinimumSize(new java.awt.Dimension(90, 20));
        portJTextField.setName("portJTextField");
        portJTextField.setPreferredSize(new java.awt.Dimension(90, 20));
        portJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                portJTextFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                portJTextFieldKeyTyped(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 12);
        internalJPanel.add(portJTextField, gridBagConstraints);

        portJLabel.setLabelFor(portJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 33, 0, 0);
        internalJPanel.add(portJLabel, gridBagConstraints);

        rmiUserNameJLabel.setLabelFor(rmiUserNameJTextField);
        rmiUserNameJLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 55, 0, 0);
        internalJPanel.add(rmiUserNameJLabel, gridBagConstraints);

        rmiPasswordJLabel.setLabelFor(rmiPasswordJTextField);
        rmiPasswordJLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 55, 12, 0);
        internalJPanel.add(rmiPasswordJLabel, gridBagConstraints);

        rmiPasswordJTextField.setEnabled(false);
        rmiPasswordJTextField.setMinimumSize(new java.awt.Dimension(90, 20));
        rmiPasswordJTextField.setName("userPasswordJTextField");
        rmiPasswordJTextField.setPreferredSize(new java.awt.Dimension(90, 20));
        rmiPasswordJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                rmiPasswordJTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 12, 12);
        internalJPanel.add(rmiPasswordJTextField, gridBagConstraints);

        rmiUserNameJTextField.setEnabled(false);
        rmiUserNameJTextField.setMinimumSize(new java.awt.Dimension(90, 20));
        rmiUserNameJTextField.setName("userNameJTextField");
        rmiUserNameJTextField.setPreferredSize(new java.awt.Dimension(90, 20));
        rmiUserNameJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                rmiUserNameJTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 12);
        internalJPanel.add(rmiUserNameJTextField, gridBagConstraints);

        jmxURLJLabel.setLabelFor(jmxURLJTextField);
        jmxURLJLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 33, 0, 0);
        internalJPanel.add(jmxURLJLabel, gridBagConstraints);

        securityJCheckBox.setSelected(true);
        securityJCheckBox.setName("securityCbx");
        securityJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                securityJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
        internalJPanel.add(securityJCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        internalJPanel.add(credentialJSeparator, gridBagConstraints);

        buttonGroup2.add(userCredentialsJRadioButton);
        userCredentialsJRadioButton.setName("customCredentialRbtn");
        userCredentialsJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userCredentialsJRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 33, 0, 0);
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 33, 0, 0);
        internalJPanel.add(codeExampleJRadioButton, gridBagConstraints);

        add(internalJPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void rmiPasswordJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rmiPasswordJTextFieldKeyReleased
           String text = rmiPasswordJTextField.getText();
           //the only verification done is that the user password must not be empty
           //sets the host variable so the panel can enable/disable
           //the finish button according to the value
           setUserPasswordValidity(!text.equals(WizardConstants.EMPTYSTRING));
           //an event must be thrown to notify the change to the panel
           wiz.event();
    }//GEN-LAST:event_rmiPasswordJTextFieldKeyReleased

    private void rmiUserNameJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rmiUserNameJTextFieldKeyReleased

            String text = rmiUserNameJTextField.getText();
            //the only verification done is that the user name must not be empty
            //sets the host variable so the panel can enable/disable
            //the finish button according to the value
            setUserNameValidity(!text.equals(WizardConstants.EMPTYSTRING));
            //an event must be thrown to notify the change to the panel
            wiz.event();
    }//GEN-LAST:event_rmiUserNameJTextFieldKeyReleased

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

    private void portJTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_portJTextFieldKeyTyped
        //the keylistener is only on the active field when
        //rmi connector is selected
        if (rmiJMXAgentJRadioButton.isSelected()) {
            char typedKey = evt.getKeyChar();
            
            //defines all the keys that are accepted in the port field
            //as user input
            boolean acceptedKey = Character.isDigit(typedKey) || 
                   (typedKey == KeyEvent.VK_BACK_SPACE) || 
                   (typedKey == KeyEvent.VK_DELETE);
            
            //if the user types something else, beep and no entry
            if (!acceptedKey) {
                getToolkit().beep();
                evt.consume();
            }
         }
    }//GEN-LAST:event_portJTextFieldKeyTyped

    private void portJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_portJTextFieldKeyReleased
      //the verification of the user input is only done if its a rmi connector
        if (rmiJMXAgentJRadioButton.isSelected()) {
            String text = portJTextField.getText();
            boolean isEmpty = text.equals(WizardConstants.EMPTYSTRING);
            boolean isValidPort = false;
            
        if (!isEmpty) {
                try {
                    //defines the right range for the port number
                    isValidPort = ((new Integer(text) <= 
                            WizardConstants.MANAGER_MAX_PORT_NUMBER) && 
                            (new Integer(text) >= 0));
                } catch (NumberFormatException e) {}
            }
        
            //sets the port variable so the panel can enable/disable
            //the finish button according to the value
        setPortFieldValidity(!isEmpty && isValidPort);
        //an event must be thrown to notify the change to the panel
        wiz.event();
      }
    }//GEN-LAST:event_portJTextFieldKeyReleased

    private void hostJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_hostJTextFieldKeyReleased
        //verification on the user input on the host field is only performed 
        //when it is a RMI connector
        if (rmiJMXAgentJRadioButton.isSelected()) {
            String text = hostJTextField.getText();
            //the only verification done is that the host name must not be empty
            //sets the host variable so the panel can enable/disable
            //the finish button according to the value
            setHostFieldValidity(!text.equals(WizardConstants.EMPTYSTRING));
            //an event must be thrown to notify the change to the panel
            wiz.event();
        }
    }//GEN-LAST:event_hostJTextFieldKeyReleased

    private void jmxAgentURLJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmxAgentURLJRadioButtonActionPerformed
        freeFormURLrbtnSelected = jmxAgentURLJRadioButton.isSelected();
        
        //enables/disables the components on the panel according to what 
        //connector has been chosen
        
        hostJLabel.setEnabled(!freeFormURLrbtnSelected);
        hostJTextField.setEnabled(!freeFormURLrbtnSelected);
        portJLabel.setEnabled(!freeFormURLrbtnSelected);
        portJTextField.setEnabled(!freeFormURLrbtnSelected);
        
        jmxURLJLabel.setEnabled(freeFormURLrbtnSelected);
        jmxURLJTextField.setEnabled(freeFormURLrbtnSelected);
        
        //since no verification is done on user input when the free form
        //connector is chosen, the finish button must be enabled
        setHostFieldValidity(true);
        setPortFieldValidity(true);
        //an event must be thrown to notify the change to the panel
        wiz.event();
    }//GEN-LAST:event_jmxAgentURLJRadioButtonActionPerformed

    private void rmiJMXAgentJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rmiJMXAgentJRadioButtonActionPerformed
        rmiURLrbtnSelected = rmiJMXAgentJRadioButton.isSelected();
        
        //enables/disables the components on the panel according to what 
        //connector has been chosen
        
        hostJLabel.setEnabled(rmiURLrbtnSelected);
        hostJTextField.setEnabled(rmiURLrbtnSelected);
        portJLabel.setEnabled(rmiURLrbtnSelected);
        portJTextField.setEnabled(rmiURLrbtnSelected);
        
        jmxURLJLabel.setEnabled(!rmiURLrbtnSelected);
        jmxURLJTextField.setEnabled(!rmiURLrbtnSelected);
        
        //an event must be thrown to notify the change to the panel
        wiz.event();
    }//GEN-LAST:event_rmiJMXAgentJRadioButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JRadioButton codeExampleJRadioButton;
    private javax.swing.JSeparator credentialJSeparator;
    private javax.swing.JLabel hostJLabel;
    private javax.swing.JTextField hostJTextField;
    private javax.swing.JPanel internalJPanel;
    private javax.swing.JRadioButton jmxAgentURLJRadioButton;
    private javax.swing.JLabel jmxURLJLabel;
    private javax.swing.JTextField jmxURLJTextField;
    private javax.swing.JLabel portJLabel;
    private javax.swing.JTextField portJTextField;
    private javax.swing.JRadioButton rmiJMXAgentJRadioButton;
    private javax.swing.JLabel rmiPasswordJLabel;
    private javax.swing.JTextField rmiPasswordJTextField;
    private javax.swing.JLabel rmiUserNameJLabel;
    private javax.swing.JTextField rmiUserNameJTextField;
    private javax.swing.JCheckBox securityJCheckBox;
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
            boolean port = true;
            boolean host = true;
            boolean credentials = true;
            
            String msg = WizardConstants.EMPTY_STRING;
            
            if (getPanel() != null) {
                if (!getPanel().getHostFieldValidity()) {
                    host = false;
                    msg = NbBundle.getMessage(AgentURLPanel.class,"LBL_State_Host_Incorrect");// NOI18N
                }
                else {
                    if (!getPanel().getPortFieldValidity()) {
                        port = false;
                        msg = NbBundle.getMessage(AgentURLPanel.class,"LBL_State_Port_Incorrect");// NOI18N
                    } else {
                        if (isSecurityCheckBoxSelected() &&
                                isUserCredentialRbtnSelected()) { //box checked
                            if (!getPanel().getUserNameValidity()) {
                                credentials = false;
                                msg = NbBundle.getMessage(AgentURLPanel.class,"LBL_State_UserName_Incorrect");// NOI18N
                            }
                            if (!getPanel().getUserPasswordValidity()) {
                                credentials = false;
                                msg = NbBundle.getMessage(AgentURLPanel.class,"LBL_State_UserPassword_Incorrect");// NOI18N
                            }
                        }
                    }
                }
                setErrorMsg(msg);

            }
            return (host && port && credentials);
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
         * Returns if the RMI JMX URL is selected
         * @return <CODE>boolean</CODE> true if the RMI JMX URL is selected 
         */
        public boolean isRmiJmxUrlSelected() {
            return getPanel().rmiJMXAgentJRadioButton.isSelected();
        }
        
        /**
         * Returns if the free form JMX URL is selected
         * @return <CODE>boolean</CODE> true if the free form JMX URL is 
         * selected 
         */
        public boolean isFreeFormJmxUrlSelected() {
            return getPanel().jmxAgentURLJRadioButton.isSelected();
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
        public void storeSettings (Object settings) 
        {            
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            // sets the initial properties to know which combination of
            // checkboxes/ radiobuttons has been selected
            wiz.putProperty(WizardConstants.PROP_MANAGER_RMI_URL_SELECTED, 
                    new Boolean(isRmiJmxUrlSelected()));
            wiz.putProperty(WizardConstants.PROP_MANAGER_FREEFORM_URL_SELECTED, 
                    new Boolean(isFreeFormJmxUrlSelected()));
            
            // storage for the url type selected
            if (isRmiJmxUrlSelected()) {
                wiz.putProperty(WizardConstants.PROP_MANAGER_HOST,
                        new String(getPanel().hostJTextField.getText()));
                wiz.putProperty(WizardConstants.PROP_MANAGER_PORT,
                        new String(getPanel().portJTextField.getText()));                               
                wiz.putProperty(WizardConstants.PROP_MANAGER_USER_NAME,
                        new String(getPanel().rmiUserNameJTextField.getText()));                                                               
                wiz.putProperty(WizardConstants.PROP_MANAGER_USER_PASSWORD,
                        new String(getPanel().rmiPasswordJTextField.getText()));
            } else {
                if (isFreeFormJmxUrlSelected()) {
                    wiz.putProperty(WizardConstants.PROP_MANAGER_FREEFORM_URL,
                            getPanel().jmxURLJTextField.getText());                                       
                }
            }
            
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
                wiz.event();
            }
            
            public void removeUpdate(DocumentEvent e) {
                wiz.event();
            }
            
            public void changedUpdate(DocumentEvent e) {
                wiz.event();
            }
}
