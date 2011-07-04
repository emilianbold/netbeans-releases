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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostLabel)
                        .addGap(53, 53, 53)
                        .addComponent(hostTF, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(portLabel)
                        .addGap(55, 55, 55)
                        .addComponent(portTF, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(contextRootLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(contextRootTF, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(usernameLabel)
                        .addGap(20, 20, 20)
                        .addComponent(usernameTF, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passwordLabel)
                        .addGap(23, 23, 23)
                        .addComponent(passwordTF, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(hostTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextRootLabel)
                    .addComponent(contextRootTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
