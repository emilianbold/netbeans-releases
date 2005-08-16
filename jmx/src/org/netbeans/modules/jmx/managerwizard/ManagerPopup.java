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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.jmx.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.listener.TextFieldFocusListener;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author  an156382
 */
public class ManagerPopup extends javax.swing.JPanel {
    
    private ResourceBundle bundle;
    private JTextField urlField;
    private JButton okJButton;
    
    /**
     * Creates new form ManagerPopup 
     */
    public ManagerPopup(JPanel ancestorPanel, JTextField urlField) {
        //super((java.awt.Dialog)ancestorPanel.getTopLevelAncestor()); 
        this.urlField = urlField;
        bundle = NbBundle.getBundle(ManagerPopup.class);
        initComponents();
        
        okJButton = new JButton(bundle.getString("LBL_okButton.text")); // NOI18N
        rmiHostJTextField.setText(bundle.getString("TXT_host"));// NOI18N
        rmiPortJTextField.setText(bundle.getString("TXT_port"));// NOI18N
        
        Mnemonics.setLocalizedText(rmiHostJLabel,bundle.getString("LBL_host.text"));// NOI18N
        Mnemonics.setLocalizedText(rmiPortJLabel,bundle.getString("LBL_port.text"));// NOI18N
        Mnemonics.setLocalizedText(okJButton,bundle.getString("LBL_okButton.text"));// NOI18N
        
        // Accessibility
        rmiHostJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RMI_HOST")); // NOI18N
        rmiHostJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RMI_HOST_DESCRIPTION")); // NOI18N
        rmiPortJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RMI_PORT")); // NOI18N
        rmiPortJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RMI_PORT_DESCRIPTION")); // NOI18N
        okJButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_OK")); // NOI18N
        okJButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OK_DESCRIPTION")); // NOI18N
        
        setName("ManagerPopup");// NOI18N
        addListeners();
        configure();
        
        //setDimensions(NbBundle.getMessage(ManagerPopup.class,"LBL_RMIAgentURL_Popup"));// NOI18N
        //setDimensions(bundle.getString("LBL_RMIAgentURL_Popup"));// NOI18N
    }
    
    /*protected void setDimensions(String str) {
        setTitle(str);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //setBounds(400,400,350,150);
        setVisible(true);
    }*/
    
    /**
     * Displays a configuration dialog and updates Register MBean options 
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if user clicks on Ok button.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_RMIAgentURL_Popup"); // NOI18N
        //btnOK.setEnabled(isAcceptable());
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor (
                        this,
                        title,
                        true,                       //modal
                        new Object[] {okJButton, DialogDescriptor.CANCEL_OPTION},
                        okJButton,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        null,
                        (ActionListener) null
                ));
        
        if (returned == okJButton) {
            return true;
        }
        return false;
    }
    
    public JTextField getHostField() {
        return rmiHostJTextField;
    }
    
    public String getHostFieldText() {
        return rmiHostJTextField.getText();
    }
    
    public JTextField getPortField() {
        return rmiPortJTextField;
    }
    
    public String getPortFieldText() {
        return rmiPortJTextField.getText();
    }
    
    public JButton getOKButton() {
        return okJButton;
    }
    
    private void addListeners() {
        getHostField().addFocusListener(new TextFieldFocusListener());
        getPortField().addFocusListener(new TextFieldFocusListener());
        getOKButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonAction();
            }
        });
    }
    
    private void okButtonAction() {
        urlField.setText(WizardConstants.EMPTYSTRING);
                urlField.setText("service:jmx:rmi:///jndi/rmi://" + // NOI18N
                    getHostFieldText()+":"+getPortFieldText()+"/jmxrmi");// NOI18N
        //this.dispose();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rmiParameterPanel = new javax.swing.JPanel();
        rmiHostJLabel = new javax.swing.JLabel();
        rmiPortJLabel = new javax.swing.JLabel();
        rmiHostJTextField = new javax.swing.JTextField();
        rmiPortJTextField = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        rmiParameterPanel.setLayout(new java.awt.GridBagLayout());

        rmiHostJLabel.setLabelFor(rmiHostJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        rmiParameterPanel.add(rmiHostJLabel, gridBagConstraints);

        rmiPortJLabel.setLabelFor(rmiPortJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        rmiParameterPanel.add(rmiPortJLabel, gridBagConstraints);

        rmiHostJTextField.setName("hostJTextField");
        rmiHostJTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 11);
        rmiParameterPanel.add(rmiHostJTextField, gridBagConstraints);

        rmiPortJTextField.setName("portJTextField");
        rmiPortJTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        rmiParameterPanel.add(rmiPortJTextField, gridBagConstraints);

        add(rmiParameterPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel rmiHostJLabel;
    protected javax.swing.JTextField rmiHostJTextField;
    private javax.swing.JPanel rmiParameterPanel;
    protected javax.swing.JLabel rmiPortJLabel;
    protected javax.swing.JTextField rmiPortJTextField;
    // End of variables declaration//GEN-END:variables
    
}
