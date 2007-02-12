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
        passwdHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        passwdHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        usernameHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        usernameHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
                
        addImmediateModifier(defaultPasswordField);
        addImmediateModifier(defaultUsernameTextField);
        addImmediateModifier(passwdHandlerField);
        addImmediateModifier(usernameHandlerField);
        addImmediateModifier(samlHandlerField);

        sync();
    }

    private String getDefaultPassword() {
        return new String(this.defaultPasswordField.getPassword());
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
    
    private String getCallbackHandler(String type) {
        if (CallbackHandler.SAML_CBHANDLER.equals(type)) return samlHandlerField.getText();
        if (CallbackHandler.USERNAME_CBHANDLER.equals(type)) return usernameHandlerField.getText();
        if (CallbackHandler.PASSWORD_CBHANDLER.equals(type)) return passwdHandlerField.getText();
        return null;
    }

    private void setCallbackHandler(String classname, String type) {
        if (CallbackHandler.SAML_CBHANDLER.equals(type)) this.samlHandlerField.setText(classname);
        if (CallbackHandler.USERNAME_CBHANDLER.equals(type)) this.usernameHandlerField.setText(classname);
        if (CallbackHandler.PASSWORD_CBHANDLER.equals(type)) this.passwdHandlerField.setText(classname);
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
        String usernameCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER);
        if (usernameCallback != null) {
            setCallbackHandler(usernameCallback, CallbackHandler.USERNAME_CBHANDLER);
        }
        String passwdCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER);
        if (passwdCallback != null) {
            setCallbackHandler(passwdCallback, CallbackHandler.PASSWORD_CBHANDLER);
        }
        String samlCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER);
        if (samlCallback != null) {
            setCallbackHandler(samlCallback, CallbackHandler.SAML_CBHANDLER);
        }
        inSync = false;
    }
    
    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
        
        if (source.equals(usernameHandlerField) || source.equals(defaultUsernameTextField)) {
            String classname = getCallbackHandler(CallbackHandler.USERNAME_CBHANDLER);
            String defaultUsername = getDefaultUsername();
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            if ((defaultUsername != null) && (defaultUsername.length() == 0)) {
                defaultUsername = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, classname, defaultUsername, true);
            return;
        }

        if (source.equals(passwdHandlerField) || source.equals(defaultPasswordField)) {
            String classname = getCallbackHandler(CallbackHandler.PASSWORD_CBHANDLER);
            String defaultPassword = getDefaultPassword();
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            if ((defaultPassword != null) && (defaultPassword.length() == 0)) {
                defaultPassword = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, classname, defaultPassword, true);
            return;
        }

        if (source.equals(samlHandlerField)) {
            String classname = getCallbackHandler(CallbackHandler.SAML_CBHANDLER);
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER, classname, null, true);
            return;
        }
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

        usernameHandlerLabel = new javax.swing.JLabel();
        usernameHandlerField = new javax.swing.JTextField();
        usernameBrowseButton = new javax.swing.JButton();
        defaultUsernameLabel = new javax.swing.JLabel();
        defaultPasswordLabel = new javax.swing.JLabel();
        defaultUsernameTextField = new javax.swing.JTextField();
        defaultPasswordField = new javax.swing.JPasswordField();
        passwdHandlerLabel = new javax.swing.JLabel();
        passwdHandlerField = new javax.swing.JTextField();
        passwdBrowseButton = new javax.swing.JButton();
        samlHandlerLabel = new javax.swing.JLabel();
        samlHandlerField = new javax.swing.JTextField();
        samlBrowseButton = new javax.swing.JButton();

        usernameHandlerLabel.setLabelFor(usernameHandlerField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameHandlerLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_UsernameLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(usernameBrowseButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_UCHBrowseButton")); // NOI18N
        usernameBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameBrowseButtonActionPerformed(evt);
            }
        });

        defaultUsernameLabel.setLabelFor(defaultUsernameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(defaultUsernameLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_DefaultUsername")); // NOI18N

        defaultPasswordLabel.setLabelFor(defaultPasswordField);
        org.openide.awt.Mnemonics.setLocalizedText(defaultPasswordLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_DefaultPassword")); // NOI18N

        passwdHandlerLabel.setLabelFor(passwdHandlerField);
        org.openide.awt.Mnemonics.setLocalizedText(passwdHandlerLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_PasswordLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passwdBrowseButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_PCHBrowseButton")); // NOI18N
        passwdBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwdBrowseButtonActionPerformed(evt);
            }
        });

        samlHandlerLabel.setLabelFor(samlHandlerField);
        org.openide.awt.Mnemonics.setLocalizedText(samlHandlerLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(samlBrowseButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton")); // NOI18N
        samlBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlBrowseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(samlHandlerLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(usernameHandlerLabel)
                            .add(passwdHandlerLabel)
                            .add(defaultPasswordLabel)
                            .add(defaultUsernameLabel))
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(passwdHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                                    .add(usernameHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                                    .add(samlHandlerField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                        .add(passwdBrowseButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(usernameBrowseButton))
                                    .add(samlBrowseButton)))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, defaultPasswordField)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, defaultUsernameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(defaultPasswordLabel)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(defaultUsernameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(defaultUsernameLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(defaultPasswordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameHandlerLabel)
                    .add(usernameBrowseButton)
                    .add(usernameHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwdHandlerLabel)
                    .add(passwdHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(passwdBrowseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(samlHandlerLabel)
                    .add(samlHandlerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(samlBrowseButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void samlBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.CallbackHandler"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, CallbackHandler.SAML_CBHANDLER);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER, selectedClass, null, true);          
                    break;
                }
            }
        }
    }//GEN-LAST:event_samlBrowseButtonActionPerformed

    private void passwdBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwdBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.PasswordCallback"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, CallbackHandler.PASSWORD_CBHANDLER);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, selectedClass, getDefaultPassword(), true);          
                    break;
                }
            }
        }
    }//GEN-LAST:event_passwdBrowseButtonActionPerformed

    private void usernameBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.NameCallback"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, CallbackHandler.USERNAME_CBHANDLER);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, selectedClass, getDefaultUsername(), true);          
                    break;
                }
            }
        }
    }//GEN-LAST:event_usernameBrowseButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField defaultPasswordField;
    private javax.swing.JLabel defaultPasswordLabel;
    private javax.swing.JLabel defaultUsernameLabel;
    private javax.swing.JTextField defaultUsernameTextField;
    private javax.swing.JButton passwdBrowseButton;
    private javax.swing.JTextField passwdHandlerField;
    private javax.swing.JLabel passwdHandlerLabel;
    private javax.swing.JButton samlBrowseButton;
    private javax.swing.JTextField samlHandlerField;
    private javax.swing.JLabel samlHandlerLabel;
    private javax.swing.JButton usernameBrowseButton;
    private javax.swing.JTextField usernameHandlerField;
    private javax.swing.JLabel usernameHandlerLabel;
    // End of variables declaration//GEN-END:variables
    
}
