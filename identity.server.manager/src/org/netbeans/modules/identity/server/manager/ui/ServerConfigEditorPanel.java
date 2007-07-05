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

package org.netbeans.modules.identity.server.manager.ui;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.netbeans.modules.identity.server.manager.api.ServerInstance;
import org.netbeans.modules.identity.server.manager.api.ServerManager;
import org.openide.util.NbBundle;

/**
 * UI panel for editing the configuration data of a ServerInstance.
 *
 * Created on June 22, 2006, 3:40 PM
 *
 * @author  ptliu
 */
public class ServerConfigEditorPanel extends javax.swing.JPanel
        implements EditDialogDescriptor.Panel {
    
    private static final int MAX_PORT = 65535;
    
    private static final int MIN_PORT = 0;
    
    private final List listeners = new ArrayList();
    
    private ServerInstance instance;
    
    /**
     * Creates new form ServerConfigEditorPanel
     */
    public ServerConfigEditorPanel(ServerInstance instance) {
        initComponents();
        
        this.instance = instance;
        init();
    }
    
    public JComponent[] getEditableComponents() {
        return new JTextField[] {hostTF,
        portTF, contextRootTF, usernameTF, passwordTF
        };
    }
    
    public void init() {
        hostTF.setText(instance.getHost());
        portTF.setText(instance.getPort());
        contextRootTF.setText(instance.getContextRoot());
        usernameTF.setText(instance.getUserName());
        passwordTF.setText(instance.getPassword());
        
        //        if (instance.isLocal()) {
        //            hostTF.setEditable(false);
        //            portTF.setEditable(false);
        //            contextRootTF.setEditable(false);
        //            usernameTF.setEditable(false);
        //            passwordTF.setEditable(false);
        //        } else {
        hostTF.setEditable(false);
        //       }
    }
    
    public String checkValues() {
        String host = hostTF.getText();
        
        if (host == null || host.trim().length() == 0) {
            return NbBundle.getMessage(ServerConfigEditorPanel.class,
                    "MSG_EnterHost");
        } else {
            if (host.split("\\s").length > 1) {             //NOI18N
                return NbBundle.getMessage(ServerConfigEditorPanel.class,
                        "MSG_InvalidHost");
            }
        }
        
        String port = portTF.getText();
        
        if (port == null || port.trim().length() == 0) {
            return NbBundle.getMessage(ServerConfigEditorPanel.class,
                    "MSG_EnterPort");
        }
        
        try {
            int portNumber = Integer.parseInt(port.trim());
            
            if (portNumber < MIN_PORT || portNumber > MAX_PORT) {
                return NbBundle.getMessage(ServerConfigEditorPanel.class,
                        "MSG_InvalidPort");
            }
        } catch (NumberFormatException ex) {
            return NbBundle.getMessage(ServerConfigEditorPanel.class,
                    "MSG_InvalidPort");
        }
        
        String contextRoot = contextRootTF.getText();
        
        if (contextRoot == null || contextRoot.trim().length() == 0) {
            return NbBundle.getMessage(ServerConfigEditorPanel.class,
                    "MSG_EnterContextRoot");
        }
        
        String username = usernameTF.getText();
        
        if (username == null || username.trim().length() == 0) {
            return NbBundle.getMessage(ServerConfigEditorPanel.class,
                    "MSG_EnterUsername");
        }
        
        String password = new String(passwordTF.getPassword());
        
        if (password == null || password.trim().length() == 0) {
            return NbBundle.getMessage(ServerConfigEditorPanel.class,
                    "MSG_EnterPassword");
        }
        
        return null;
    }
    
    
    public void updateInstance() {
        boolean isModified = false;
        
        String value = hostTF.getText().trim();
        if (!instance.getHost().equals(value)) {
            isModified = true;
            instance.setHost(value);
        }
        
        value = portTF.getText().trim();
        if (!instance.getPort().equals(value)) {
            isModified = true;
            instance.setPort(value);
        }
        
        value = contextRootTF.getText().trim();
        if (!instance.getContextRoot().equals(value)) {
            isModified = true;
            instance.setContextRoot(value);
        }
        
        value = usernameTF.getText().trim();
        if (!instance.getUserName().equals(value)) {
            isModified = true;
            instance.setUserName(value);
        }
        
        if (!instance.getPassword().equals(passwordTF.getPassword())) {
            isModified = true;
            instance.setPassword(new String(passwordTF.getPassword()));
        }
        
        if (isModified) {
            try {
                ServerManager.getDefault().writeInstanceToFile(instance);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        hostLabel = new javax.swing.JLabel();
        hostTF = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTF = new javax.swing.JTextField();
        usernameLabel = new javax.swing.JLabel();
        usernameTF = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordTF = new javax.swing.JPasswordField();
        contextRootLabel = new javax.swing.JLabel();
        contextRootTF = new javax.swing.JTextField();

        hostLabel.setLabelFor(hostTF);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, bundle.getString("LBL_Host")); // NOI18N

        portLabel.setLabelFor(portTF);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, bundle.getString("LBL_Port")); // NOI18N

        usernameLabel.setLabelFor(usernameTF);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, bundle.getString("LBL_AdminUserName")); // NOI18N

        passwordLabel.setLabelFor(passwordTF);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, bundle.getString("LBL_AdminPassword")); // NOI18N

        contextRootLabel.setLabelFor(contextRootTF);
        org.openide.awt.Mnemonics.setLocalizedText(contextRootLabel, bundle.getString("LBL_ContextRoot")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(hostLabel)
                        .add(53, 53, 53)
                        .add(hostTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(portLabel)
                        .add(55, 55, 55)
                        .add(portTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(contextRootLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(contextRootTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(usernameLabel)
                        .add(20, 20, 20)
                        .add(usernameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(passwordLabel)
                        .add(23, 23, 23)
                        .add(passwordTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostLabel)
                    .add(hostTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portLabel)
                    .add(portTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contextRootLabel)
                    .add(contextRootTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(usernameLabel)
                    .add(usernameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel contextRootLabel;
    private javax.swing.JTextField contextRootTF;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField hostTF;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordTF;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTF;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameTF;
    // End of variables declaration//GEN-END:variables
    
}
