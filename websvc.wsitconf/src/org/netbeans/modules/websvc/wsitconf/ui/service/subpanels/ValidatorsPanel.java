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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.WSITEditor;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.Validator;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.Set;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;

/**
 *
 * @author Martin Grebac
 */
public class ValidatorsPanel extends JPanel {

    private WSDLModel model;
    private WSDLComponent comp;

    private Project p;
    
    private boolean inSync = false;
        
    public ValidatorsPanel(WSDLComponent comp, Project p) {
        super();
        this.model = comp.getModel();
        this.comp = comp;
        this.p = p;
        
        initComponents();

        usernameValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        usernameValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        timestampValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        timestampValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certificateValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certificateValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        sync();
    }

    private String getValidator(String type) {
        if (Validator.USERNAME_VALIDATOR.equals(type)) return usernameValidatorTextField.getText();
        if (Validator.TIMESTAMP_VALIDATOR.equals(type)) return timestampValidatorTextField.getText();
        if (Validator.CERTIFICATE_VALIDATOR.equals(type)) return certificateValidatorTextField.getText();
        if (Validator.SAML_VALIDATOR.equals(type)) return samlValidatorTextField.getText();
        return null;
    }

    private void setValidator(String type, String validator) {
        if (Validator.USERNAME_VALIDATOR.equals(type)) this.usernameValidatorTextField.setText(validator);
        if (Validator.TIMESTAMP_VALIDATOR.equals(type)) this.timestampValidatorTextField.setText(validator);
        if (Validator.CERTIFICATE_VALIDATOR.equals(type)) this.certificateValidatorTextField.setText(validator);
        if (Validator.SAML_VALIDATOR.equals(type)) this.samlValidatorTextField.setText(validator);
    }
    
    public void sync() {
        inSync = true;
                
        String usernameValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.USERNAME_VALIDATOR);
        if (usernameValidator != null) {
            setValidator(Validator.USERNAME_VALIDATOR, usernameValidator);
        }
        String timestampValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.TIMESTAMP_VALIDATOR);
        if (timestampValidator != null) {
            setValidator(Validator.TIMESTAMP_VALIDATOR, timestampValidator);
        }
        String certificateValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.CERTIFICATE_VALIDATOR);
        if (certificateValidator != null) {
            setValidator(Validator.CERTIFICATE_VALIDATOR, certificateValidator);
        }
        String samlValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.SAML_VALIDATOR);
        if (samlValidator != null) {
            setValidator(Validator.SAML_VALIDATOR, samlValidator);
        }
        inSync = false;
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source.equals(usernameValidatorTextField)) {
            String validator = getValidator(Validator.USERNAME_VALIDATOR);
            if ((validator != null) && (validator.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.USERNAME_VALIDATOR, null, false);
            } else {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.USERNAME_VALIDATOR, validator, false);
            }
            return;
        }

        if (source.equals(timestampValidatorTextField)) {
            String validator = getValidator(Validator.TIMESTAMP_VALIDATOR);
            if ((validator != null) && (validator.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, null, false);
            } else {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, validator, false);
            }
            return;
        }

        if (source.equals(certificateValidatorTextField)) {
            String validator = getValidator(Validator.CERTIFICATE_VALIDATOR);
            if ((validator != null) && (validator.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR, null, false);
            } else {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR,validator, false);
            }
            return;
        }

        if (source.equals(samlValidatorTextField)) {
            String validator = getValidator(Validator.SAML_VALIDATOR);
            if ((validator != null) && (validator.length() == 0)) {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.SAML_VALIDATOR, null, false);
            } else {
                ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.SAML_VALIDATOR, validator, false);
            }
            return;
        }
    }
    
    private class JavaFileFilter extends FileFilter {
        private static final String JAVA_EXT = ".java";      //NOI18N
        public boolean accept(File f) {
            if ((f != null) && f.exists() && (f.getName() != null) && ((f.getName().contains(JAVA_EXT)) || (f.isDirectory()))) {
                return true;
            }
            return false;
        }
        public String getDescription() {
            return NbBundle.getMessage(WSITEditor.class, "JAVA_FILE");  //NOI18N
        }
    }

    public void storeState() {
        String usernameV = getValidator(Validator.USERNAME_VALIDATOR);
        if ((usernameV == null) || (usernameV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.USERNAME_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.USERNAME_VALIDATOR, usernameV, false);
        }
        
        String certV = getValidator(Validator.CERTIFICATE_VALIDATOR);
        if ((certV == null) || (certV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR, certV, false);
        }
        
        String timeV = getValidator(Validator.TIMESTAMP_VALIDATOR);
        if ((timeV == null) || (timeV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, timeV, false);
        }
        
        String samlV = getValidator(Validator.SAML_VALIDATOR);
        if ((samlV == null) || (samlV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.SAML_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.SAML_VALIDATOR, samlV, false);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jToggleButton1 = new javax.swing.JToggleButton();
        usernameValidatorLabel = new javax.swing.JLabel();
        timestampValidatorLabel = new javax.swing.JLabel();
        certificateValidatorLabel = new javax.swing.JLabel();
        samlValidatorLabel = new javax.swing.JLabel();
        usernameValidatorTextField = new javax.swing.JTextField();
        timestampValidatorTextField = new javax.swing.JTextField();
        certificateValidatorTextField = new javax.swing.JTextField();
        samlValidatorTextField = new javax.swing.JTextField();
        usernameValidatorButton = new javax.swing.JButton();
        timestampValidatorButton = new javax.swing.JButton();
        certificateValidatorButton = new javax.swing.JButton();
        samlValidatorButton = new javax.swing.JButton();

        jToggleButton1.setText("jToggleButton1");

        usernameValidatorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/subpanels/Bundle").getString("LBL_ValidatorPanel_UsernameVLabel"));

        timestampValidatorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/subpanels/Bundle").getString("LBL_ValidatorPanel_TimestampVLabel"));

        certificateValidatorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/subpanels/Bundle").getString("LBL_ValidatorPanel_CertificateVLabel"));

        samlValidatorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/subpanels/Bundle").getString("LBL_ValidatorPanel_SAMLVLabel"));

        usernameValidatorButton.setText("Browse...");
        usernameValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameValidatorButtonActionPerformed(evt);
            }
        });

        timestampValidatorButton.setText("Browse...");
        timestampValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timestampValidatorButtonActionPerformed(evt);
            }
        });

        certificateValidatorButton.setText("Browse...");
        certificateValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certificateValidatorButtonActionPerformed(evt);
            }
        });

        samlValidatorButton.setText("Browse...");
        samlValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlValidatorButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(126, 126, 126)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(timestampValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(certificateValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(usernameValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 266, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, samlValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(samlValidatorButton)
                                    .add(certificateValidatorButton)
                                    .add(timestampValidatorButton)
                                    .add(usernameValidatorButton)))
                            .add(timestampValidatorLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 11, Short.MAX_VALUE)
                        .add(jToggleButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(certificateValidatorLabel)
                            .add(samlValidatorLabel)
                            .add(usernameValidatorLabel))
                        .addContainerGap(389, Short.MAX_VALUE))))
        );

        layout.linkSize(new java.awt.Component[] {certificateValidatorTextField, samlValidatorTextField, timestampValidatorTextField, usernameValidatorTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(69, 69, 69)
                        .add(jToggleButton1))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(usernameValidatorLabel)
                            .add(usernameValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(usernameValidatorButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(timestampValidatorLabel)
                            .add(timestampValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(timestampValidatorButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(certificateValidatorLabel)
                            .add(certificateValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(certificateValidatorButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(samlValidatorLabel)
                            .add(samlValidatorTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(samlValidatorButton))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void samlValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.SAMLAssertionValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.SAML_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_samlValidatorButtonActionPerformed

    private void certificateValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certificateValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.CertificateValidationCallback.CertificateValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.CERTIFICATE_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_certificateValidatorButtonActionPerformed

    private void timestampValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timestampValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.TimestampValidationCallback.TimestampValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.TIMESTAMP_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_timestampValidatorButtonActionPerformed

    private void usernameValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.PasswordValidationCallback.PasswordValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.USERNAME_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_usernameValidatorButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton certificateValidatorButton;
    private javax.swing.JLabel certificateValidatorLabel;
    private javax.swing.JTextField certificateValidatorTextField;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton samlValidatorButton;
    private javax.swing.JLabel samlValidatorLabel;
    private javax.swing.JTextField samlValidatorTextField;
    private javax.swing.JButton timestampValidatorButton;
    private javax.swing.JLabel timestampValidatorLabel;
    private javax.swing.JTextField timestampValidatorTextField;
    private javax.swing.JButton usernameValidatorButton;
    private javax.swing.JLabel usernameValidatorLabel;
    private javax.swing.JTextField usernameValidatorTextField;
    // End of variables declaration//GEN-END:variables
    
}
