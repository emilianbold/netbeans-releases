/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JRadioButton;
import org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig;
import org.netbeans.modules.j2ee.dd.api.web.LoginConfig;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;

/**
 * LoginConfigPanel.java
 *
 * Panel for adding and editing the login-config element of the web deployment 
 * descriptor.
 *
 * @author  ptliu
 */
public class LoginConfigPanel extends SectionInnerPanel {
    private static final Logger LOG = Logger.getLogger(LoginConfigPanel.class.getName());
    
    private static String NONE = "NONE";                //NOI18N
    private static String BASIC = "BASIC";              //NOI18N
    private static String DIGEST = "DIGEST";            //NOI18N
    private static String FORM = "FORM";                //NOI18N
    private static String CLIENT_CERT = "CLIENT-CERT";  //NOI18N
    
    private WebApp webApp;
    private LoginConfig loginConfig;
    private DDDataObject dObj;
    
    /**
     * Creates new form LoginConfigPanel
     */
    public LoginConfigPanel(SectionView view, DDDataObject dObj) {
        super(view);
        initComponents();
        
        this.dObj = dObj;
        this.webApp = dObj.getWebApp();
        this.loginConfig = webApp.getSingleLoginConfig();
        
        initPanel();
    }
    
    private void initPanel() {
        if (loginConfig == null) {
            updateVisualState(NONE);
        } else {
            String authMethod = loginConfig.getAuthMethod();
            updateVisualState(authMethod);
            
            if (authMethod.equals(BASIC)) {
                realmNameTF.setText(loginConfig.getRealmName());
            } else if (authMethod.equals(FORM)) {
                FormLoginConfig formLoginConfig = loginConfig.getFormLoginConfig();
                loginPageTF.setText(formLoginConfig.getFormLoginPage());
                errorPageTF.setText(formLoginConfig.getFormErrorPage());
                realmNameTF.setText(loginConfig.getRealmName());
            }
        }
        
        addModifier(noneRB);
        addModifier(digestRB);
        addModifier(clientCertRB);
        addModifier(basicRB);
        addModifier(formRB);
        
        addValidatee(realmNameTF);
        addValidatee(loginPageTF);
        addValidatee(errorPageTF);
    }
    
    private void updateVisualState(final String state) {
        if (state.equals(BASIC)) {
            basicRB.setSelected(true);
            realmNameLabel.setEnabled(true);
            realmNameTF.setEnabled(true);
            loginPageLabel.setEnabled(false);
            loginPageTF.setEnabled(false);
            loginPageBrowseButton.setEnabled(false);
            errorPageLabel.setEnabled(false);
            errorPageTF.setEnabled(false);
            errorPageBrowseButton.setEnabled(false);
        } else if (state.equals(FORM)) {
            formRB.setSelected(true);
            realmNameLabel.setEnabled(true);
            realmNameTF.setEnabled(true);
            loginPageLabel.setEnabled(true);
            loginPageTF.setEnabled(true);
            loginPageBrowseButton.setEnabled(true);
            errorPageLabel.setEnabled(true);
            errorPageTF.setEnabled(true);
            errorPageBrowseButton.setEnabled(true);
        } else {
            if (state.equals(NONE)) {
                noneRB.setSelected(true);
            } else if (state.equals(DIGEST)) {
                digestRB.setSelected(true);
            } else if (state.equals(CLIENT_CERT)) {
                clientCertRB.setSelected(true);
            }
            
            realmNameLabel.setEnabled(false);
            realmNameTF.setEnabled(false);
            loginPageLabel.setEnabled(false);
            loginPageTF.setEnabled(false);
            loginPageBrowseButton.setEnabled(false);
            errorPageLabel.setEnabled(false);
            errorPageTF.setEnabled(false);
            errorPageBrowseButton.setEnabled(false);
        }
    }
    
    public void linkButtonPressed(Object obj, String id) {
    }
    
    public javax.swing.JComponent getErrorComponent(String name) {
        return null;
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        /* TODO: Is there anything to validate?
        if (comp == realmNameTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Realm Name", realmNameTF));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
         */
    }
    
    private void setLoginConfig(LoginConfig config) {
        webApp.setLoginConfig(config);
        loginConfig = config;
    }
    
    private LoginConfig getLoginConfig() {
        if (loginConfig == null) {
            try {
                loginConfig = (LoginConfig) webApp.createBean("LoginConfig");  //NOI18N
                webApp.setLoginConfig(loginConfig);
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
            }
        }
        
        return loginConfig;
    }
    
    private FormLoginConfig getFormLoginConfig() {
        LoginConfig loginConfig = getLoginConfig();
        FormLoginConfig formLoginConfig = loginConfig.getFormLoginConfig();
        
        if (formLoginConfig == null) {
            try {
                formLoginConfig = (FormLoginConfig) webApp.createBean("FormLoginConfig");  //NOI18N
                loginConfig.setFormLoginConfig(formLoginConfig);
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
            }
        }
        
        return formLoginConfig;
    }
    
    public void setValue(javax.swing.JComponent source, Object value) { 
        if (source instanceof JRadioButton) {
            String authMethod = null;
            
            if (source == noneRB) {
                authMethod = NONE;
            } else if (source == digestRB) {
                authMethod = DIGEST;
            } else if (source == clientCertRB) {
                authMethod = CLIENT_CERT;
            } else if (source == basicRB) {
                authMethod = BASIC;
            } else if (source == formRB) {
                authMethod = FORM;
            } else {
                authMethod = NONE;
            }
            
            // Null out the existing loginConfig
            setLoginConfig(null);
            
            if (!authMethod.equals(NONE)) {
                LoginConfig loginConfig = getLoginConfig();
                loginConfig.setAuthMethod(authMethod);
                
                // Revive any previously set values.
                if (authMethod.equals(BASIC)) {
                    loginConfig.setRealmName(realmNameTF.getText());
                } else if (authMethod.equals(FORM)) {
                    loginConfig.setRealmName(realmNameTF.getText());
                    FormLoginConfig formLoginConfig = getFormLoginConfig();
                    formLoginConfig.setFormLoginPage(loginPageTF.getText());
                    formLoginConfig.setFormErrorPage(errorPageTF.getText());
                }
            }
            
            updateVisualState(authMethod);
            
        } else if (source == realmNameTF) {
            getLoginConfig().setRealmName((String) value);
        } else if (source == loginPageTF) {
            getFormLoginConfig().setFormLoginPage((String) value);
        } else if (source == errorPageTF) {
            getFormLoginConfig().setFormErrorPage((String) value);
        }
    }
    
    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    /** This will be called before model is changed from this panel
     */
    @Override
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    @Override
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        realmNameLabel = new javax.swing.JLabel();
        loginPageLabel = new javax.swing.JLabel();
        errorPageLabel = new javax.swing.JLabel();
        realmNameTF = new javax.swing.JTextField();
        loginPageTF = new javax.swing.JTextField();
        errorPageTF = new javax.swing.JTextField();
        loginPageBrowseButton = new javax.swing.JButton();
        errorPageBrowseButton = new javax.swing.JButton();
        noneRB = new javax.swing.JRadioButton();
        digestRB = new javax.swing.JRadioButton();
        clientCertRB = new javax.swing.JRadioButton();
        basicRB = new javax.swing.JRadioButton();
        formRB = new javax.swing.JRadioButton();

        realmNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_realmName_mnem").charAt(0));
        realmNameLabel.setLabelFor(realmNameTF);
        realmNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_RealmName"));
        realmNameLabel.setEnabled(false);

        loginPageLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_loginPage_mnem").charAt(0));
        loginPageLabel.setLabelFor(loginPageTF);
        loginPageLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_FormLoginPage"));
        loginPageLabel.setEnabled(false);

        errorPageLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_errorPage_mnem").charAt(0));
        errorPageLabel.setLabelFor(loginPageTF);
        errorPageLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_FormErrorPage"));
        errorPageLabel.setEnabled(false);

        realmNameTF.setEnabled(false);

        loginPageTF.setEnabled(false);

        errorPageTF.setEnabled(false);

        loginPageBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_browse_mnem3").charAt(0));
        loginPageBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_browse"));
        loginPageBrowseButton.setEnabled(false);
        loginPageBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginPageBrowseButtonActionPerformed(evt);
            }
        });

        errorPageBrowseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_browse_mnem2").charAt(0));
        errorPageBrowseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_browse"));
        errorPageBrowseButton.setEnabled(false);
        errorPageBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorPageBrowseButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(noneRB);
        noneRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_none_mnem").charAt(0));
        noneRB.setSelected(true);
        noneRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_NoneAuthMethod"));
        noneRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noneRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noneRB.setOpaque(false);
        noneRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noneRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(digestRB);
        digestRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_digest_mnem").charAt(0));
        digestRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_DigestAuthMethod"));
        digestRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        digestRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        digestRB.setOpaque(false);
        digestRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                digestRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(clientCertRB);
        clientCertRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_clientCert_mnem").charAt(0));
        clientCertRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_ClientCertAuthMethod"));
        clientCertRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        clientCertRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clientCertRB.setOpaque(false);
        clientCertRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientCertRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(basicRB);
        basicRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_basic_mnem").charAt(0));
        basicRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_BasicAuthMethod"));
        basicRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        basicRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        basicRB.setOpaque(false);
        basicRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                basicRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(formRB);
        formRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_form_mnem").charAt(0));
        formRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_FormAuthMethod"));
        formRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        formRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        formRB.setOpaque(false);
        formRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formRBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(noneRB)
                    .add(digestRB)
                    .add(clientCertRB)
                    .add(basicRB)
                    .add(layout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(realmNameLabel)
                            .add(loginPageLabel)
                            .add(errorPageLabel))
                        .add(4, 4, 4)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(errorPageTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, realmNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, loginPageTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(errorPageBrowseButton)
                            .add(loginPageBrowseButton)))
                    .add(formRB))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(noneRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(digestRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clientCertRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(basicRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(realmNameLabel)
                    .add(realmNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(formRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(loginPageBrowseButton)
                    .add(loginPageLabel)
                    .add(loginPageTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(errorPageBrowseButton)
                    .add(errorPageLabel)
                    .add(errorPageTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formRBActionPerformed
        updateVisualState(FORM);
    }//GEN-LAST:event_formRBActionPerformed

    private void basicRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_basicRBActionPerformed
        updateVisualState(BASIC);
    }//GEN-LAST:event_basicRBActionPerformed

    private void clientCertRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientCertRBActionPerformed
        updateVisualState(CLIENT_CERT);
    }//GEN-LAST:event_clientCertRBActionPerformed

    private void digestRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_digestRBActionPerformed
        updateVisualState(DIGEST);
    }//GEN-LAST:event_digestRBActionPerformed

    private void noneRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noneRBActionPerformed
        updateVisualState(NONE);
    }//GEN-LAST:event_noneRBActionPerformed
    
    private void errorPageBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorPageBrowseButtonActionPerformed
        try {
            org.netbeans.api.project.SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String res = "/"+DDUtils.getResourcePath(groups,fo,'/',true);  //NOI18N
                
                if (!res.equals(errorPageTF.getText())) {
                    dObj.modelUpdatedFromUI();
                    errorPageTF.setText(res);
                    dObj.setChangedFromUI(true);
                    getFormLoginConfig().setFormErrorPage(res);
                    dObj.setChangedFromUI(false);
                    getSectionView().checkValidity();
                }
            }
        } catch (java.io.IOException ex) {
            LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
        }
    }//GEN-LAST:event_errorPageBrowseButtonActionPerformed
    
    private void loginPageBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginPageBrowseButtonActionPerformed
         try {
            org.netbeans.api.project.SourceGroup[] groups = DDUtils.getDocBaseGroups(dObj);
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String res = "/"+DDUtils.getResourcePath(groups,fo,'/',true);  //NOI18N
                
                if (!res.equals(loginPageTF.getText())) {
                    dObj.modelUpdatedFromUI();
                    loginPageTF.setText(res);
                    dObj.setChangedFromUI(true);
                    getFormLoginConfig().setFormLoginPage(res);
                    dObj.setChangedFromUI(false);
                    getSectionView().checkValidity();
                }
            }
        } catch (java.io.IOException ex) {
            LOG.log(Level.FINE, "ignored exception", ex); //NOI18N
        }
    }//GEN-LAST:event_loginPageBrowseButtonActionPerformed
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton basicRB;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton clientCertRB;
    private javax.swing.JRadioButton digestRB;
    private javax.swing.JButton errorPageBrowseButton;
    private javax.swing.JLabel errorPageLabel;
    private javax.swing.JTextField errorPageTF;
    private javax.swing.JRadioButton formRB;
    private javax.swing.JButton loginPageBrowseButton;
    private javax.swing.JLabel loginPageLabel;
    private javax.swing.JTextField loginPageTF;
    private javax.swing.JRadioButton noneRB;
    private javax.swing.JLabel realmNameLabel;
    private javax.swing.JTextField realmNameTF;
    // End of variables declaration//GEN-END:variables
    
}
