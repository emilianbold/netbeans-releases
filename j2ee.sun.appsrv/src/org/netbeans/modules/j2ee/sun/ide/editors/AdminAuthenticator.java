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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * AdminAuthenticator.java
 *
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.SunURIManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;



/** Global password protected sites Authenticator for IDE
 *
 * @author  Ludo, Petr Hrebejk
 */

public class AdminAuthenticator extends java.net.Authenticator {
    private  SunDeploymentManagerInterface preferredSunDeploymentManagerInterface;
    private boolean displayed = false;
    
    public AdminAuthenticator( ) {
        preferredSunDeploymentManagerInterface=null ;
    }
    
    public AdminAuthenticator(SunDeploymentManagerInterface dm) {
        preferredSunDeploymentManagerInterface=dm ;
    }

    protected java.net.PasswordAuthentication getPasswordAuthentication() {
        if (!displayed) {
            displayed = true;
            
            java.net.InetAddress site = getRequestingSite();
            ResourceBundle bundle = NbBundle.getBundle( AdminAuthenticator.class );
            String host = site == null ? bundle.getString( "CTL_PasswordProtected" ) : site.getHostName(); // NOI18N
            String title = getRequestingPrompt();
            InstanceProperties ip = null;
            String keyURI;
            String name=""; // NOI18N
            if (title.equals("admin-realm")){ // NOI18N so this is a request from the sun server
                if (preferredSunDeploymentManagerInterface!=null){
                    //Make sure this is really the admin port and not the app port (see bug 85605
                    if ( preferredSunDeploymentManagerInterface.getPort()==getRequestingPort()){
                        ip =SunURIManager.getInstanceProperties(
                                preferredSunDeploymentManagerInterface.getPlatformRoot(),
                                preferredSunDeploymentManagerInterface.getHost(),
                                preferredSunDeploymentManagerInterface.getPort());
                    }
                }
                //            else {
                //            keyURI=SunURIManager.SUNSERVERSURI+site.getHostName()+":"+getRequestingPort();
                //            ip= InstanceProperties.getInstanceProperties(keyURI);
                //
                //        }
            }
            if (ip!=null){
                title =ip.getProperty(InstanceProperties.DISPLAY_NAME_ATTR);
                name = ip.getProperty(InstanceProperties.USERNAME_ATTR);
                
            }
            
            
            
            PasswordPanel passwordPanel = new PasswordPanel(name);
            
            DialogDescriptor dd = new DialogDescriptor( passwordPanel, host );
            passwordPanel.setPrompt(title  );
            java.awt.Dialog dialog = DialogDisplayer.getDefault().createDialog( dd );
            dialog.setVisible(true);
            
            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ){
                
                if (ip!=null){
                    String oldpass = ip.getProperty(InstanceProperties.PASSWORD_ATTR);
                    ip.setProperty(InstanceProperties.USERNAME_ATTR, passwordPanel.getUsername());
                    ip.setProperty(InstanceProperties.PASSWORD_ATTR, passwordPanel.getTPassword());
                    if (preferredSunDeploymentManagerInterface!=null){
                        preferredSunDeploymentManagerInterface.setUserName(passwordPanel.getUsername());
                        preferredSunDeploymentManagerInterface.setPassword(passwordPanel.getTPassword());
                    }
                    ip.refreshServerInstance();
                    
                    if ("".equals(oldpass)){
                        ip.setProperty(InstanceProperties.PASSWORD_ATTR ,oldpass);
                        
                    }
                }
                
                return new java.net.PasswordAuthentication( passwordPanel.getUsername(), passwordPanel.getPassword() );
            } else{
                return null;
            }
        } else {
            return null;
        }
    }
    
    
    
    /** Inner class for JPanel with Username & Password fields */
    
    static class PasswordPanel extends javax.swing.JPanel {
        
        private static final int DEFAULT_WIDTH = 200;
        private static final int DEFAULT_HEIGHT = 0;
        
        /** Generated serialVersionUID */
        static final long serialVersionUID = 1555749205340031767L;
        
        ResourceBundle bundle = org.openide.util.NbBundle.getBundle(AdminAuthenticator.class);
        
        /** Creates new form PasswordPanel */
        public PasswordPanel(String userName) {
            initComponents();
            usernameField.setText(userName);
            usernameField.setSelectionStart(0);
            usernameField.setSelectionEnd(userName.length());
            usernameField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UserNameField"));
            passwordField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_PasswordField"));
        }
        
        public java.awt.Dimension getPreferredSize() {
            java.awt.Dimension sup = super.getPreferredSize();
            return new java.awt.Dimension( Math.max(sup.width, DEFAULT_WIDTH), Math.max(sup.height, DEFAULT_HEIGHT ));
        }
        
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the FormEditor.
         */
        private void initComponents() {
            setLayout(new java.awt.BorderLayout());
            
            mainPanel = new javax.swing.JPanel();
            mainPanel.setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints1;
            mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
            
            promptLabel = new javax.swing.JLabel();
            promptLabel.setHorizontalAlignment(0);
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 6, 0);
            mainPanel.add(promptLabel, gridBagConstraints1);
            
            jLabel1 = new javax.swing.JLabel();
            org.openide.awt.Mnemonics.setLocalizedText(jLabel1, 
                    bundle.getString("LAB_AUTH_User_Name")); // NOI18N            
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add(jLabel1, gridBagConstraints1);
            
            usernameField = new javax.swing.JTextField();
            usernameField.setMinimumSize(new java.awt.Dimension(70, 20));
            usernameField.setPreferredSize(new java.awt.Dimension(70, 20));
            jLabel1.setLabelFor(usernameField);
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add(usernameField, gridBagConstraints1);
            
            jLabel2 = new javax.swing.JLabel();
            org.openide.awt.Mnemonics.setLocalizedText(jLabel2, 
                    bundle.getString("LAB_AUTH_Password")); // NOI18N            
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add(jLabel2, gridBagConstraints1);
            
            passwordField = new javax.swing.JPasswordField();
            passwordField.setMinimumSize(new java.awt.Dimension(70, 20));
            passwordField.setPreferredSize(new java.awt.Dimension(70, 20));
            jLabel2.setLabelFor(passwordField);
            
            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add(passwordField, gridBagConstraints1);
            
            add(mainPanel, "Center"); // NOI18N
            
        }
        
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel mainPanel;
        private javax.swing.JLabel promptLabel;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JTextField usernameField;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JPasswordField passwordField;
        // End of variables declaration//GEN-END:variables

        String getUsername( ) {
            return usernameField.getText();
        }

        char[] getPassword( ) {
            return passwordField.getPassword();
        }

        String getTPassword( ) {
            return passwordField.getText();
        }

        void setPrompt( String prompt ) {
            if ( prompt == null ) {
                promptLabel.setVisible( false );
                getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NbAuthenticatorPasswordPanel"));
            }
            else {
                promptLabel.setVisible( true );
                promptLabel.setText( prompt );
                getAccessibleContext().setAccessibleDescription(prompt);
            }
        }
    }
}
