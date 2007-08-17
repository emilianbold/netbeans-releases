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
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.client.subpanels;

import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitconf.ui.client.PanelEnabler;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.xml.wsdl.model.Binding;

/**
 *
 * @author  Martin Grebac
 */

public class DynamicCredsPanel extends javax.swing.JPanel implements PanelEnabler {
    
    private boolean inSync = false;

    private Binding binding;
    private boolean enable;
    
    private Project project;

    /** Creates new form DynamicCredentials */
    public DynamicCredsPanel(Binding b, Project project, boolean enable) {
        this.binding = b;
        this.enable = enable;
        this.project = project;
               
        initComponents();
        
//        passwdHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//        passwdHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//        usernameHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
//        usernameHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        sync();
    }
    
    public void sync() {
        inSync = true;

        String usernameCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER);
        if (usernameCallback != null) {
            setCallbackHandler(usernameCallback, CallbackHandler.USERNAME_CBHANDLER);
        }
        String passwdCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER);
        if (passwdCallback != null) {
            setCallbackHandler(passwdCallback, CallbackHandler.PASSWORD_CBHANDLER);
        }
        
        enableDisable();
        
        inSync = false;
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
            
        if (source.equals(usernameHandlerField)) {
            String classname = getCallbackHandler(CallbackHandler.USERNAME_CBHANDLER);
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, classname, null, true);
            return;
        }

        if (source.equals(passwdHandlerField)) {
            String classname = getCallbackHandler(CallbackHandler.PASSWORD_CBHANDLER);
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, classname, null, true);
            return;
        }
        
        enableDisable();
    }

    private void enableDisable() {        
        passwdBrowseButton.setEnabled(isPanelEnabled());
        passwdHandlerField.setEnabled(isPanelEnabled());
        passwdHandlerLabel.setEnabled(isPanelEnabled());
        usernameBrowseButton.setEnabled(isPanelEnabled());
        usernameHandlerField.setEnabled(isPanelEnabled());
        usernameHandlerLabel.setEnabled(isPanelEnabled());
    }

    public boolean isPanelEnabled() {
        return enable;
    }
    
    public void enablePanel(boolean doEnable) {
        enable = doEnable;
    }
    
    private String getCallbackHandler(String type) {
        if (CallbackHandler.USERNAME_CBHANDLER.equals(type)) return usernameHandlerField.getText();
        if (CallbackHandler.PASSWORD_CBHANDLER.equals(type)) return passwdHandlerField.getText();
        return null;
    }

    private void setCallbackHandler(String classname, String type) {
        if (CallbackHandler.USERNAME_CBHANDLER.equals(type)) this.usernameHandlerField.setText(classname);
        if (CallbackHandler.PASSWORD_CBHANDLER.equals(type)) this.passwdHandlerField.setText(classname);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        usernameHandlerLabel = new javax.swing.JLabel();
        usernameHandlerField = new javax.swing.JTextField();
        passwdHandlerLabel = new javax.swing.JLabel();
        passwdHandlerField = new javax.swing.JTextField();
        usernameBrowseButton = new javax.swing.JButton();
        passwdBrowseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(usernameHandlerLabel, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_UsernameCBHLabel")); // NOI18N

        usernameHandlerField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                usernameHandlerFieldKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(passwdHandlerLabel, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_PasswordCBHLabel")); // NOI18N

        passwdHandlerField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwdHandlerFieldKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(usernameBrowseButton, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_Username_Browse")); // NOI18N
        usernameBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(passwdBrowseButton, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_Password_Browse")); // NOI18N
        passwdBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwdBrowseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(passwdHandlerLabel)
                        .add(13, 13, 13))
                    .add(layout.createSequentialGroup()
                        .add(usernameHandlerLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(usernameHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                    .add(passwdHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(usernameBrowseButton)
                    .add(passwdBrowseButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameBrowseButton)
                    .add(usernameHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(usernameHandlerLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(passwdBrowseButton)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(passwdHandlerLabel)
                        .add(passwdHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void passwdHandlerFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwdHandlerFieldKeyReleased
    setValue(passwdHandlerField, null);
}//GEN-LAST:event_passwdHandlerFieldKeyReleased

private void usernameHandlerFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameHandlerFieldKeyReleased
    setValue(usernameHandlerField, null);
}//GEN-LAST:event_usernameHandlerFieldKeyReleased

    private void passwdBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwdBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.CallbackHandler"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, CallbackHandler.PASSWORD_CBHANDLER);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, selectedClass, null, true);
                    break;
                }
            }
        }
    }//GEN-LAST:event_passwdBrowseButtonActionPerformed

    private void usernameBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.CallbackHandler"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, CallbackHandler.USERNAME_CBHANDLER);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, selectedClass, null, true);
                    break;
                }
            }
        }
    }//GEN-LAST:event_usernameBrowseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton passwdBrowseButton;
    private javax.swing.JTextField passwdHandlerField;
    private javax.swing.JLabel passwdHandlerLabel;
    private javax.swing.JButton usernameBrowseButton;
    private javax.swing.JTextField usernameHandlerField;
    private javax.swing.JLabel usernameHandlerLabel;
    // End of variables declaration//GEN-END:variables
    
}
