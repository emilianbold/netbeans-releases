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

package org.netbeans.core;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Global password protected sites Authenticator for IDE
 *
 * @author Jiri Rechtacek
 */

class NbAuthenticator extends java.net.Authenticator {
    NbAuthenticator () {
        Preferences proxySettingsNode = NbPreferences.root ().node ("/org/netbeans/core");
        assert proxySettingsNode != null;
        proxySettingsNode.putBoolean (ProxySettings.USE_PROXY_AUTHENTICATION, false);
    }

    protected java.net.PasswordAuthentication getPasswordAuthentication() {
        Logger.getLogger (NbAuthenticator.class.getName ()).log (Level.FINER, "Authenticator.getPasswordAuthentication() with prompt " + this.getRequestingPrompt());
        
        // XXX: temprary solution while issue 75856 not fixed
        if (! ProxySettings.useAuthentication ()) {

            java.net.InetAddress site = getRequestingSite();
            ResourceBundle bundle = NbBundle.getBundle( NbAuthenticator.class );
            String host = site == null ? bundle.getString( "CTL_PasswordProtected" ) : site.getHostName(); // NOI18N
            Preferences proxySettingsNode = NbPreferences.root ().node ("/org/netbeans/core");
            assert proxySettingsNode != null;
            
            PasswordPanel passwordPanel = new PasswordPanel (ProxySettings.getAuthenticationUsername (), ProxySettings.getAuthenticationPassword ());

            DialogDescriptor dd = new DialogDescriptor( passwordPanel, host );
            // #48931: set help Id (asked by web team)
            dd.setHelpCtx (new HelpCtx (NbAuthenticator.class.getName () + ".getPasswordAuthentication")); // NOI18N
            passwordPanel.setPrompt( getRequestingPrompt() );
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog( dd );
            dialog.setVisible (true);

            if ( dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
                Logger.getLogger (NbAuthenticator.class.getName ()).log (Level.WARNING, "Store authentication for " + this.getRequestingURL ());
                proxySettingsNode.putBoolean (ProxySettings.USE_PROXY_AUTHENTICATION, true);
                proxySettingsNode.put (ProxySettings.PROXY_AUTHENTICATION_USERNAME, passwordPanel.getUsername ());
                proxySettingsNode.put (ProxySettings.PROXY_AUTHENTICATION_PASSWORD, new String (passwordPanel.getPassword ()));
            }
        }
        
        if (ProxySettings.useAuthentication ()) {
            Logger.getLogger (NbAuthenticator.class.getName ()).log (Level.FINER, "Username set to " + ProxySettings.getAuthenticationUsername () + " while request " + this.getRequestingURL ());
            return new java.net.PasswordAuthentication (ProxySettings.getAuthenticationUsername (), ProxySettings.getAuthenticationPassword ());
        } else {
            Logger.getLogger (NbAuthenticator.class.getName ()).log (Level.WARNING, "No authentication set while requesting " + this.getRequestingURL ());
            return null;
        }
        
    }
    
    /** Inner class for JPanel with Username & Password fields */

    static class PasswordPanel extends javax.swing.JPanel {

        private static final int DEFAULT_WIDTH = 200;
        private static final int DEFAULT_HEIGHT = 0;

        /** Generated serialVersionUID */
        static final long serialVersionUID = 1555749205340031767L;

        ResourceBundle bundle = org.openide.util.NbBundle.getBundle(NbAuthenticator.class);
        
        /** Creates new form PasswordPanel */
        public PasswordPanel (String username, char [] password) {
            initComponents ();
            
            usernameField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UserNameField"));
            passwordField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_PasswordField"));
            
            usernameField.setText(username);
            passwordField.setText(new String (password));
            
        }

        public java.awt.Dimension getPreferredSize () {
            java.awt.Dimension sup = super.getPreferredSize ();
            return new java.awt.Dimension ( Math.max (sup.width, DEFAULT_WIDTH), Math.max (sup.height, DEFAULT_HEIGHT ));
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the FormEditor.
         */
        private void initComponents () {
            setLayout (new java.awt.BorderLayout ());

            mainPanel = new javax.swing.JPanel ();
            mainPanel.setLayout (new java.awt.GridBagLayout ());
            java.awt.GridBagConstraints gridBagConstraints1;
            mainPanel.setBorder (new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));

            promptLabel = new javax.swing.JLabel ();
            promptLabel.setHorizontalAlignment (0);

            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets (0, 0, 6, 0);
            mainPanel.add (promptLabel, gridBagConstraints1);

            jLabel1 = new javax.swing.JLabel ();
            Mnemonics.setLocalizedText (jLabel1, bundle.getString("LAB_AUTH_User_Name"));

            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.insets = new java.awt.Insets (0, 0, 5, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add (jLabel1, gridBagConstraints1);

            usernameField = new javax.swing.JTextField ();
            usernameField.setMinimumSize (new java.awt.Dimension(70, 20));
            usernameField.setPreferredSize (new java.awt.Dimension(70, 20));
            jLabel1.setLabelFor(usernameField);

            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets (0, 0, 5, 0);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add (usernameField, gridBagConstraints1);

            jLabel2 = new javax.swing.JLabel ();
            Mnemonics.setLocalizedText (jLabel2, bundle.getString("LAB_AUTH_Password"));

            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.insets = new java.awt.Insets (0, 0, 0, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add (jLabel2, gridBagConstraints1);

            passwordField = new javax.swing.JPasswordField ();
            passwordField.setMinimumSize (new java.awt.Dimension(70, 20));
            passwordField.setPreferredSize (new java.awt.Dimension(70, 20));
            jLabel2.setLabelFor(passwordField);

            gridBagConstraints1 = new java.awt.GridBagConstraints ();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add (passwordField, gridBagConstraints1);

            add (mainPanel, "Center"); // NOI18N

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
