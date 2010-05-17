/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.jmx.configwizard;

import java.awt.Component;
import java.util.Iterator;
import java.util.Vector;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.GenericWizardPanel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.openide.util.HelpCtx;

/**
 *
 * Class handling the graphical part of the standard Options wizard panel
 *
 */
public class SNMPPanel extends javax.swing.JPanel {
    
    private SNMPWizardPanel wiz;
    private ResourceBundle bundle;
    
    private boolean aclSelected = false;
    private boolean sNMPSelected = false;
    private JFileChooser chooser;
    
    /**
     * Create the wizard panel component and set up some basic properties.
     * @param wiz <CODE>WizardDescriptor</CODE> a wizard
     */
    public SNMPPanel (SNMPWizardPanel wiz) 
    {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(ConfigPanel.class);
        initComponents ();
        
        chooser = new JFileChooser();
        
        Mnemonics.setLocalizedText(sNMPJCheckBox,
                                   bundle.getString("LBL_SNMP"));//NOI18N
        Mnemonics.setLocalizedText(aclJCheckBox,
                                   bundle.getString("LBL_SNMP_AccessList"));//NOI18N
        Mnemonics.setLocalizedText(sNMPPortJLabel,
                                   bundle.getString("LBL_SNMP_Port"));//NOI18N
        
        Mnemonics.setLocalizedText(customACL,
                                   bundle.getString("LBL_SNMP_CustomACL"));//NOI18N
        
        Mnemonics.setLocalizedText(aclFileJButton,
                                   bundle.getString("LBL_SNMP_acl_File_Edit"));//NOI18N
        
        Mnemonics.setLocalizedText(interfaceJLabel,
                                   bundle.getString("LBL_SNMP_Interface"));//NOI18N
        Mnemonics.setLocalizedText(sNMPTrapPortJLabel,
                                   bundle.getString("LBL_SNMP_Trap_Port"));//NOI18N
     
        sNMPJCheckBox.setToolTipText(bundle.getString("TLTP_SNMP"));//NOI18N
        aclJCheckBox.setToolTipText(bundle.getString("TLTP_ACL"));//NOI18N
        customACL.setToolTipText(bundle.getString("TLTP_CUSTOM_ACL"));//NOI18N
        aclFileJButton.setToolTipText(bundle.getString("TLTP_CUSTOM_ACL"));//NOI18N
        sNMPPortJLabel.setToolTipText(bundle.getString("TLTP_SNMP_PORT"));//NOI18N
        interfaceJLabel.setToolTipText(bundle.getString("TLTP_SNMP_Interface"));//NOI18N
        sNMPTrapPortJLabel.setToolTipText(bundle.getString("TLTP_SNMP_TrapPort"));//NOI18N
        
        updateSelected();
        setSNMPPanelEnabled(false);
        updateSelected();
        
        // Provide a name in the title bar.
        //setName(NbBundle.getMessage(ConfigPanel.class, "LBL_SNMP_Panel"));   // NOI18N   
        setName(bundle.getString("LBL_SNMP_Panel"));// NOI18N
        //agentNameField.getDocument().addDocumentListener(this);   
        sNMPPortJTextField.addKeyListener(new KeyListener() {                   
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        if (!(Character.isDigit(c) ||
                                c == KeyEvent.VK_BACK_SPACE ||
                                c == KeyEvent.VK_DELETE)) {
                            sNMPTrapPortJLabel.getToolkit().beep();
                            e.consume();
                        }
                    }
                    
                    public void keyPressed(KeyEvent e) {
                    }
                    
                    public void keyReleased(KeyEvent e) {
                        
                    }
                });
               
        trapPortJTextField.addKeyListener(new KeyListener() {                   
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        if (!(Character.isDigit(c) ||
                                c == KeyEvent.VK_BACK_SPACE ||
                                c == KeyEvent.VK_DELETE)) {
                            sNMPTrapPortJLabel.getToolkit().beep();
                            e.consume();
                        }
                    }
                    
                    public void keyPressed(KeyEvent e) {
                    }
                    
                    public void keyReleased(KeyEvent e) {
                        
                    }
                });
         //Accessibility       
        sNMPJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SNMP")); // NOI18N
        sNMPJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SNMP_DESCRIPTION")); // NOI18N
        
        sNMPPortJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SNMP_PORT")); // NOI18N
        sNMPPortJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SNMP_PORT_DESCRIPTION")); // NOI18N
        
        sNMPPortJLabel.setLabelFor(sNMPPortJTextField);
        
        interfaceJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SNMP_INTERFACE")); // NOI18N
        interfaceJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SNMP_INTERFACE_DESCRIPTION")); // NOI18N
        
        interfaceJLabel.setLabelFor(interfaceJTextField);
        
        trapPortJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SNMP_TRAP")); // NOI18N
        trapPortJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SNMP_TRAP_DESCRIPTION")); // NOI18N
        
        sNMPTrapPortJLabel.setLabelFor(trapPortJTextField);
        
        aclJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SNMP_ACL")); // NOI18N
        aclJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SNMP_ACL_DESCRIPTION")); // NOI18N
        
        aclFileJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SNMP_ACL_FILE")); // NOI18N
        aclFileJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SNMP_ACL_FILE_DESCRIPTION")); // NOI18N
        
        customACL.setLabelFor(aclFileJTextField);
        
        aclFileJButton.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_SNMP_ACL_FILE_BROWSE")); // NOI18N
        aclFileJButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_SNMP_ACL_FILE_BROWSE_DESCRIPTION")); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    /**
     * update all the selected flags 
     */
    private void updateSelected() {
        aclSelected = aclJCheckBox.isSelected();
        sNMPSelected = sNMPJCheckBox.isSelected();
    }
    
    /**
     * calls setEnabled(enable) method of all components included in SNMPPanel
     */
    private void setSNMPPanelEnabled(boolean enable) {
        Vector<JComponent> jcVector = new Vector<JComponent>();
        jcVector.add(sNMPPortJLabel);
        jcVector.add(sNMPPortJTextField);
        jcVector.add(aclJCheckBox);
        jcVector.add(sNMPTrapPortJLabel);
        jcVector.add(trapPortJTextField);
        jcVector.add(interfaceJLabel);
        jcVector.add(interfaceJTextField);
        jcVector.add(customACL);
        for (Iterator<JComponent> it = jcVector.iterator();it.hasNext();) {
            JComponent jc = it.next();
            jc.setEnabled(enable);
        }
        if (enable && aclSelected) {
            setAclFileEnabled(true);
        } else {
            setAclFileEnabled(false);
        }
    }
    
    /*
     * calls setEnabled(enable) method of all components for acl File option
     */
    private void setAclFileEnabled(boolean enable) {
        Vector<JComponent> jcVector = new Vector<JComponent>();
        jcVector.add(aclFileJTextField);
        jcVector.add(aclFileJButton);
        jcVector.add(customACL);
        for (Iterator<JComponent> it = jcVector.iterator();it.hasNext();) {
            JComponent jc = it.next();
            jc.setEnabled(enable);
        }
    }
    
    private void checkPortValue(javax.swing.JTextField jt) {
        if (Integer.getInteger(jt.getText()) > 65536) {
            jt.setText(new Integer(65536).toString());
        } else if (Integer.getInteger(jt.getText()) < 0) {
            jt.setText(new Integer(0).toString());
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

        managementFilesGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        sNMPJCheckBox = new javax.swing.JCheckBox();
        sNMPPortJLabel = new javax.swing.JLabel();
        sNMPPortJTextField = new javax.swing.JTextField();
        interfaceJLabel = new javax.swing.JLabel();
        interfaceJTextField = new javax.swing.JTextField();
        sNMPTrapPortJLabel = new javax.swing.JLabel();
        trapPortJTextField = new javax.swing.JTextField();
        aclJCheckBox = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        customACL = new javax.swing.JLabel();
        aclFileJTextField = new javax.swing.JTextField();
        aclFileJButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        sNMPJCheckBox.setName("sNMPJCheckBox");
        sNMPJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sNMPJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(sNMPJCheckBox, gridBagConstraints);

        sNMPPortJLabel.setText("port");
        sNMPPortJLabel.setName("sNMPPortJLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 12);
        jPanel1.add(sNMPPortJLabel, gridBagConstraints);

        sNMPPortJTextField.setMinimumSize(new java.awt.Dimension(55, 20));
        sNMPPortJTextField.setName("sNMPPortJTextField");
        sNMPPortJTextField.setPreferredSize(new java.awt.Dimension(55, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(sNMPPortJTextField, gridBagConstraints);

        interfaceJLabel.setText("interface");
        interfaceJLabel.setName("interfaceJLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 12);
        jPanel1.add(interfaceJLabel, gridBagConstraints);

        interfaceJTextField.setMinimumSize(new java.awt.Dimension(100, 20));
        interfaceJTextField.setName("interfaceJTextField");
        interfaceJTextField.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel1.add(interfaceJTextField, gridBagConstraints);

        sNMPTrapPortJLabel.setText("trap");
        sNMPTrapPortJLabel.setName("sNMPTrapPortJLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        jPanel1.add(sNMPTrapPortJLabel, gridBagConstraints);

        trapPortJTextField.setMinimumSize(new java.awt.Dimension(55, 20));
        trapPortJTextField.setName("trapPortJTextField");
        trapPortJTextField.setPreferredSize(new java.awt.Dimension(55, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(trapPortJTextField, gridBagConstraints);

        aclJCheckBox.setName("aclJCheckBox");
        aclJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aclJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        jPanel1.add(aclJCheckBox, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        customACL.setName("customACL");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanel10.add(customACL, gridBagConstraints);

        aclFileJTextField.setMinimumSize(new java.awt.Dimension(200, 20));
        aclFileJTextField.setName("aclFileJTextField");
        aclFileJTextField.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanel10.add(aclFileJTextField, gridBagConstraints);

        aclFileJButton.setMaximumSize(new java.awt.Dimension(90, 20));
        aclFileJButton.setMinimumSize(new java.awt.Dimension(90, 20));
        aclFileJButton.setName("aclFileJButton");
        aclFileJButton.setPreferredSize(new java.awt.Dimension(90, 20));
        aclFileJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aclFileJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel10.add(aclFileJButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 24, 0, 0);
        jPanel1.add(jPanel10, gridBagConstraints);

        add(jPanel1, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void aclFileJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aclFileJButtonActionPerformed
        int returnVal = chooser.showOpenDialog(aclFileJButton);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            aclFileJTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_aclFileJButtonActionPerformed

    private void aclJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aclJCheckBoxActionPerformed
        updateSelected();
        if (aclSelected) {
            aclFileJTextField.setEnabled(true);
            aclFileJButton.setEnabled(true);
            customACL.setEnabled(true);
        } else {
            aclFileJTextField.setEnabled(false);
            aclFileJButton.setEnabled(false);
            customACL.setEnabled(false);
        }
        //wiz.fireChangeEvent (); 
    }//GEN-LAST:event_aclJCheckBoxActionPerformed

    private void sNMPJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sNMPJCheckBoxActionPerformed
        updateSelected();
        setSNMPPanelEnabled(sNMPSelected);
        //wiz.fireChangeEvent (); 
    }//GEN-LAST:event_sNMPJCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aclFileJButton;
    private javax.swing.JTextField aclFileJTextField;
    private javax.swing.JCheckBox aclJCheckBox;
    private javax.swing.JLabel customACL;
    private javax.swing.JLabel interfaceJLabel;
    private javax.swing.JTextField interfaceJTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.ButtonGroup managementFilesGroup;
    private javax.swing.JCheckBox sNMPJCheckBox;
    private javax.swing.JLabel sNMPPortJLabel;
    private javax.swing.JTextField sNMPPortJTextField;
    private javax.swing.JLabel sNMPTrapPortJLabel;
    private javax.swing.JTextField trapPortJTextField;
    // End of variables declaration//GEN-END:variables
    
    /**
     *
     * Class handling the standard Agent wizard panel
     *
     */
    public static class SNMPWizardPanel extends GenericWizardPanel 
            implements org.openide.WizardDescriptor.FinishablePanel
    {    
        private SNMPPanel panel = null;
        private String projectLocation   = null;
        
        public Component getComponent () { return getPanel(); }
        
        private SNMPPanel getPanel() 
        {
            if (panel == null) {
                panel = new SNMPPanel(this);
            }
            return panel;
        }
        
        public boolean isFinishPanel() { return false;}

        //=====================================================================
        // Called to read information from the wizard map in order to populate
        // the GUI correctly.
        //=====================================================================
        public void readSettings (Object settings) 
        {
            WizardDescriptor wiz = (WizardDescriptor) settings;
            
            getPanel().sNMPPortJTextField.setText(
                    wiz.getProperty(WizardConstants.SNMP_PORT).toString());
            getPanel().trapPortJTextField.setText(
                    wiz.getProperty(WizardConstants.SNMP_TRAP_PORT).toString());
            getPanel().interfaceJTextField.setText(
                    (String) wiz.getProperty(WizardConstants.SNMP_INTERFACES));
        }

        //=====================================================================
        // Called to store information from the GUI into the wizard map.
        //=====================================================================
        public void storeSettings (Object settings) 
        { 
            getPanel().updateSelected();
            WizardDescriptor wiz = (WizardDescriptor) settings;
        
            wiz.putProperty(WizardConstants.SNMP_SELECTED, 
                    new Boolean(getPanel().sNMPSelected));
            wiz.putProperty(WizardConstants.SNMP_TRAP_PORT, 
                    Integer.valueOf(getPanel().trapPortJTextField.getText()));
            wiz.putProperty(WizardConstants.SNMP_PORT, 
                    Integer.valueOf(getPanel().sNMPPortJTextField.getText()));
            wiz.putProperty(WizardConstants.SNMP_INTERFACES, 
                    getPanel().interfaceJTextField.getText());
            wiz.putProperty(WizardConstants.SNMP_ACL, 
                    new Boolean(getPanel().aclSelected));
            wiz.putProperty(WizardConstants.SNMP_ACL_FILE, 
                    getPanel().aclFileJTextField.getText());
        }
        public HelpCtx getHelp() {
           return new HelpCtx( "mgt_properties");  // NOI18N
        } 
    }

}
