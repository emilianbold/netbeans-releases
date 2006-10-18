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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.WSITEditor;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 *
 * @author Martin Grebac
 */
public class CallbackPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private Binding binding;

    private boolean inSync = false;
    
    private Project project;
    
    public CallbackPanel(SectionView view, WSDLModel model, Node node, Binding binding) {
        super(view);
        this.model = model;
        this.node = node;
        this.binding = binding;
        
        FileObject fo = (FileObject) node.getLookup().lookup(FileObject.class);
        if (fo != null) project = FileOwnerQuery.getOwner(fo);
        
        initComponents();

        defaultPasswordLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        defaultPasswordField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        defaultUsernameTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        defaultUsernameLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        passwdCallbackHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        passwdCallbackHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        passwdCbButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        passwdCbButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        userCallbackHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        userCbButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        usernameCallbackHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
                
        addImmediateModifier(defaultPasswordField);
        addImmediateModifier(defaultUsernameTextField);
        addImmediateModifier(passwdCallbackHandlerField);
        addImmediateModifier(userCallbackHandlerField);

        sync();
    }

    private String getDefaultPassword() {
        return this.defaultPasswordField.getPassword().toString();
    }

    private void setDefaultPassword(String passwd) {
        this.defaultPasswordField.setText(passwd);
    }

    private String getDefaultUsername() {
        return this.defaultUsernameTextField.getText();
    }

    private void setDefaultUsername(String username) {
        this.defaultUsernameTextField.setText(username);
    }
    
    private String getCallbackHandler(boolean passwd) {
        if (passwd) {
            return passwdCallbackHandlerField.getText();
        }
        return userCallbackHandlerField.getText();
    }

    private void setCallbackHandler(String classname, boolean passwd) {
        if (passwd) {
            this.passwdCallbackHandlerField.setText(classname);
        } else {
            this.userCallbackHandlerField.setText(classname);
        }
    }

    public void sync() {
        inSync = true;

        String defaultUsername = ProprietarySecurityPolicyModelHelper.getDefaultUsername(binding);
        if (defaultUsername != null) {
            setDefaultUsername(defaultUsername);
        }
        String defaultPassword = ProprietarySecurityPolicyModelHelper.getDefaultPassword(binding);
        if (defaultPassword != null) {
            setDefaultPassword(defaultPassword);
        }
//        String usernameCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER);
//        if (usernameCallback != null) {
//            setCallbackHandler(CallbackHandler.USERNAME_CBHANDLER, usernameCallback);
//        }
        inSync = false;
    }
    
    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
//        if (source.equals(usernameTextField) || source.equals(defaultUsernameTextField)) {
//            String classname = getCallbackHandler(CallbackHandler.USERNAME_CBHANDLER);
//            String defaultUsername = getDefaultUsername();
//            if ((classname != null) && (classname.length() == 0)) {
//                classname = null;
//            }
//            if ((defaultUsername != null) && (defaultUsername.length() == 0)) {
//                defaultUsername = null;
//            }
//            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, classname, defaultUsername, true);
//            return;
//        }
//
//        if (source.equals(passwordTextField) || source.equals(defaultPasswordTextField)) {
//            String classname = getCallbackHandler(CallbackHandler.PASSWORD_CBHANDLER);
//            String defaultPassword = getDefaultPassword();
//            if ((classname != null) && (classname.length() == 0)) {
//                classname = null;
//            }
//            if ((defaultPassword != null) && (defaultPassword.length() == 0)) {
//                defaultPassword = null;
//            }
//            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, classname, defaultPassword, true);
//            return;
//        }
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    private class JavaFileFilter extends FileFilter {
        private static final String JAVA_EXT = ".java";      //NOI18N
        @Override
        public boolean accept(File f) {
            if ((f != null) && f.exists() && (f.getName() != null) && ((f.getName().contains(JAVA_EXT)) || (f.isDirectory()))) {
                return true;
            }
            return false;
        }
        @Override
        public String getDescription() {
            return NbBundle.getMessage(WSITEditor.class, "JAVA_FILE");  //NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        usernameCallbackHandlerLabel = new javax.swing.JLabel();
        userCallbackHandlerField = new javax.swing.JTextField();
        userCbButton = new javax.swing.JButton();
        defaultUsernameLabel = new javax.swing.JLabel();
        defaultPasswordLabel = new javax.swing.JLabel();
        defaultUsernameTextField = new javax.swing.JTextField();
        defaultPasswordField = new javax.swing.JPasswordField();
        passwdCallbackHandlerLabel = new javax.swing.JLabel();
        passwdCallbackHandlerField = new javax.swing.JTextField();
        passwdCbButton = new javax.swing.JButton();

        usernameCallbackHandlerLabel.setText(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_UsernameLabel")); // NOI18N

        userCbButton.setText("Browse...");
        userCbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userCbButtonActionPerformed(evt);
            }
        });

        defaultUsernameLabel.setText(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_DefaultUsername")); // NOI18N

        defaultPasswordLabel.setText(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_DefaultPassword")); // NOI18N

        passwdCallbackHandlerLabel.setText(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_PasswordLabel")); // NOI18N

        passwdCbButton.setText(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_BrowseButton")); // NOI18N
        passwdCbButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwdCbButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(usernameCallbackHandlerLabel)
                    .add(passwdCallbackHandlerLabel)
                    .add(defaultUsernameLabel)
                    .add(defaultPasswordLabel))
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(passwdCallbackHandlerField)
                        .add(userCallbackHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, defaultUsernameTextField)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, defaultPasswordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(passwdCbButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(userCbButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameCallbackHandlerLabel)
                    .add(userCbButton)
                    .add(userCallbackHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwdCallbackHandlerLabel)
                    .add(passwdCallbackHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passwdCbButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(defaultUsernameLabel)
                    .add(defaultUsernameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(defaultPasswordLabel)
                    .add(defaultPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void passwdCbButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwdCbButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.PasswordCallback"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, false);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, selectedClass, getDefaultUsername(), true);          
                    break;
                }
            }
        }

    }//GEN-LAST:event_passwdCbButtonActionPerformed

    private void userCbButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userCbButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.NameCallback"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, false);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, selectedClass, getDefaultUsername(), true);          
                    break;
                }
            }
        }
    }//GEN-LAST:event_userCbButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField defaultPasswordField;
    private javax.swing.JLabel defaultPasswordLabel;
    private javax.swing.JLabel defaultUsernameLabel;
    private javax.swing.JTextField defaultUsernameTextField;
    private javax.swing.JTextField passwdCallbackHandlerField;
    private javax.swing.JLabel passwdCallbackHandlerLabel;
    private javax.swing.JButton passwdCbButton;
    private javax.swing.JTextField userCallbackHandlerField;
    private javax.swing.JButton userCbButton;
    private javax.swing.JLabel usernameCallbackHandlerLabel;
    // End of variables declaration//GEN-END:variables
    
}
