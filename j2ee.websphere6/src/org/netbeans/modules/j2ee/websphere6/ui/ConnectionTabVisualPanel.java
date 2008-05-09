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
package org.netbeans.modules.j2ee.websphere6.ui;

import java.util.Vector;
import java.awt.event.*;
import javax.swing.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.websphere6.ui.InstancesModel;
import org.netbeans.modules.j2ee.websphere6.WSURIManager;
import org.netbeans.modules.j2ee.websphere6.j2ee.DeploymentManagerProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.awt.Mnemonics;
/**
 *
 * @author  dlm198383
 */
public class ConnectionTabVisualPanel extends javax.swing.JPanel {
    
    
    private final DeploymentManagerProperties targetData;
    
    
    /** Creates new form ConnectionTabVisualPanel */
    public class TabServerProperties extends ServerProperties {
        public TabServerProperties() {
            super();
        }
        public TabServerProperties(JComboBox serverCombobox,
                JComboBox localInstancesCombobox,
                JTextField domainPathField,
                JTextField hostField,
                JTextField portField) {
            super(serverCombobox,
                    localInstancesCombobox,
                    domainPathField,
                    hostField,
                    portField);
        }
        public class TabServerTypeActionListener extends ServerTypeActionListener{
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                isValid();
            }
        }
    }
    TabServerProperties tabServerProperties=null;
    
    public ConnectionTabVisualPanel(DeploymentManagerProperties data) {
        this.targetData=data;
        initComponents();
        tabServerProperties=new TabServerProperties(
                serverTypeCombo,
                localInstancesCombo,
                profilePathField,
                hostField,
                portField);
        InstanceProperties ips = targetData. getInstanceProperties();
        ips.refreshServerInstance();
        String url = ips.getProperty(InstanceProperties.URL_ATTR);
        url = WSURIManager.getUrlWithoutPrefix(url);
        
        localInstancesCombo.setModel(
                new InstancesModel(
                tabServerProperties.getServerInstances(
                targetData.getServerRoot())));
        for(int i=0;i<localInstancesCombo.getModel().getSize();i++) {
            if(((Instance)localInstancesCombo.getItemAt(i)).getDomainPath().equals(
                    targetData.getDomainRoot())) {
                    localInstancesCombo.setSelectedIndex(i);
                    break;
            }
        }
        
        //localInstancesCombo.addActionListener(tabServerProperties.getInstanceSelectionListener());
        
        
        int index = url.indexOf(":");
        String host = null;
        if (index > -1) {
            host = url.substring(0, index);
        }
        
        hostField.setText(host);
        
        
        userNameField.setText(targetData.getUserName());
        userNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String userName = userNameField.getText();
                targetData.setUserName(userName);
            }
        });
        
        passwordField.setText(targetData.getPassword());
        passwordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String password = new String(passwordField.getPassword());
                targetData.setPassword(password);
            }
        });
        
        
        
        //domainField.setText(targetData.getDomainName());
        profilePathField.setText(targetData.getDomainRoot());
        portField.setText(targetData.getPort());
        //portField.setModel(new SpinnerNumberModel(0,0,65535,1));
        //portField.setValue(new Integer(targetData.getPort()));
        /*
        portField.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                String port = portField.getValue().toString();
         
                if(!port.trim().matches("[0-9]+")){
                    portField.setValue(new Integer(targetData.getPort()));
                } else {
                    targetData.setPort(port);
                }
            }
        });
         */
        /*
        portField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                //String port = portField.getValue().toString();
                String port = portField.getText();
                if(!port.trim().matches("[0-9]+")){
                    //portField.setValue(new Integer(targetData.getPort()));
                    portField.setText(targetData.getPort());
                } else if((new java.lang.Integer(port)).intValue() > 65535) {
                    portField.setText(targetData.getPort());
                } else {
                    targetData.setPort(port);
                }
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                //String port = portField.getValue().toString();
                String port = portField.getText();
                if(!port.trim().matches("[0-9]+")){
                    //portField.setValue(new Integer(targetData.getPort()));
                    portField.setText(targetData.getPort());
                } else if((new java.lang.Integer(port)).intValue() > 65535) {
                    portField.setText(targetData.getPort());
                } else {
                    targetData.setPort(port);
                }
            }
        });
        */
        
        /*serverTypeCombo.addItem(NbBundle.getMessage(ConnectionTabVisualPanel.class, "TXT_serverTypeLocal"));
        serverTypeCombo.addItem(NbBundle.getMessage(ConnectionTabVisualPanel.class,"TXT_serverTypeRemote"));
         */
        Vector types=new Vector();
        types.add(NbBundle.getMessage(ConnectionTabVisualPanel.class, "TXT_ServerTypeLocal"));
        types.add(NbBundle.getMessage(ConnectionTabVisualPanel.class,"TXT_ServerTypeRemote"));
        serverTypeCombo.setModel(new InstancesModel(types));
        
        //serverTypeCombo.addActionListener(tabServerProperties.getServerTypeActionListener());
        
        /*
        serverTypeCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                if (serverTypeCombo.getSelectedItem().equals(NbBundle.
                        getMessage(ServerProperties.class,
                        "TXT_serverTypeLocal"))) {
                    targetData.setIsLocal(targetData.getIsLocal().equals("true")?"false":"true");
                }
            }
         
        }
        );*/
        
        portField.setEditable(false);
        //portField.setEnabled(false);
        hostField.setEditable(false);
        
        serverTypeCombo.setSelectedItem(targetData.isLocal() ?
            NbBundle.getMessage(ConnectionTabVisualPanel.class, "TXT_ServerTypeLocal"):
            NbBundle.getMessage(ConnectionTabVisualPanel.class, "TXT_ServerTypeRemote"));
        
        //setName(NbBundle.getMessage(ConnectionTabVisualPanel.class, "TITLE_AddUserDefinedLocalServerPanel"));
        
        setMnemonics(jLabel1);
        setMnemonics(jLabel2);
        setMnemonics(jLabel3);
        setMnemonics(jLabel4);
        setMnemonics(jLabel5);
        setMnemonics(jLabel6);
        setMnemonics(jLabel7);
        
    }
    private void setMnemonics(JLabel label) {
        String name = label.getText();
        int index = Mnemonics.findMnemonicAmpersand(name);
        if(index < 0) {
            Mnemonics.setLocalizedText(label,name);
            label.setDisplayedMnemonic(name.charAt(0));
        } else {
            Mnemonics.setLocalizedText(label,name.substring(0,index) + name.substring(index+1));
            label.setDisplayedMnemonic(name.charAt(index+1));
        }
    }
    
    /**
     * Checks whether the specified path is the valid domain root directory.
     *
     * @return true if the path is the valid domain root, false otherwise
     */
    public boolean isValid() {
        
        // if the server instance is local, then check the profile root
        // directory for validity
        if (serverTypeCombo.getSelectedItem().equals(NbBundle.getMessage(
                ConnectionTabVisualPanel.class,
                "TXT_ServerTypeLocal"))) {                             // NOI18N
            if (!tabServerProperties.isValidDomainRoot(profilePathField.getText())) {
                JOptionPane.showMessageDialog(null,
                        NbBundle.getMessage(ConnectionTabVisualPanel.class,
                        "ERR_InvalidDomainRoot"));                           // NOI18N
                return false;
            }
        }
        
        // check the host field (not empty)
        if (hostField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(ConnectionTabVisualPanel.class,
                    "ERR_InvalidHost"));                // NOI18N
            return false;
        }
        
        // check the port field (not empty and a positive integer)
        //if (!portField.getText().trim().matches("[0-9]+")) {
        //if (!portField.getValue().toString().trim().matches("[0-9]+")) {
        if (!portField.getText().trim().matches("[0-9]+")) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(ConnectionTabVisualPanel.class,
                    "ERR_InvalidPort"));                              // NOI18N
            return false;
        }
        
        // no checks for username & password as they may be intentionally blank
        
        // save the data to the parent instantiating iterator
        
        targetData.setDomainRoot(profilePathField.getText());
        targetData.setHost(hostField.getText());
        //targetData.setPort(portField.getValue().toString());
        targetData.setPort(portField.getText());
        targetData.setUserName(userNameField.getText());
        targetData.setPassword(new String(
                passwordField.getPassword()));
        
        targetData.setIsLocal(serverTypeCombo.getSelectedItem().
                equals(NbBundle.getMessage(ConnectionTabVisualPanel.class,
                "TXT_ServerTypeLocal")) ? "true" : "false");           // NOI18N
        
        targetData.setServerName(((Instance) localInstancesCombo.
                getSelectedItem()).getName());
        targetData.setConfigXmlPath(((Instance) localInstancesCombo.
                getSelectedItem()).getConfigXmlPath());
        
        targetData.getInstanceProperties().refreshServerInstance();
        // everything seems ok
        return true;
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        serverTypeCombo = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        localInstancesCombo = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        profilePathField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();

        jLabel2.setLabelFor(serverTypeCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("LBL_LocalRemote")); // NOI18N

        serverTypeCombo.setEnabled(false);

        jLabel3.setLabelFor(localInstancesCombo);
        jLabel3.setText(bundle.getString("LBL_LocalInstances")); // NOI18N

        localInstancesCombo.setEnabled(false);

        jLabel4.setLabelFor(hostField);
        jLabel4.setText(bundle.getString("LBL_Host")); // NOI18N

        jLabel5.setLabelFor(portField);
        jLabel5.setText(bundle.getString("LBL_Port")); // NOI18N

        hostField.setEditable(false);

        jLabel6.setLabelFor(userNameField);
        jLabel6.setText(bundle.getString("LBL_Username")); // NOI18N

        jLabel7.setLabelFor(passwordField);
        jLabel7.setText(bundle.getString("LBL_Password")); // NOI18N

        profilePathField.setEditable(false);

        jLabel1.setLabelFor(profilePathField);
        jLabel1.setText(bundle.getString("LBL_ProfilePath")); // NOI18N

        portField.setEditable(false);
        portField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                portFieldFocusLost(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel3))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel1))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel4))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel5))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel6))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel7)))
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(serverTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(profilePathField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                            .add(hostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 387, Short.MAX_VALUE)
                            .add(localInstancesCombo, 0, 387, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, portField)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, passwordField)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, userNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(serverTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(localInstancesCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(profilePathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(userNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        serverTypeCombo.getAccessibleContext().setAccessibleName(bundle.getString("TTL_AccessMethod")); // NOI18N
        serverTypeCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_AccessMethodDescription")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleName("Local Instances Label");
        localInstancesCombo.getAccessibleContext().setAccessibleName(bundle.getString("TTL_LocalInstances")); // NOI18N
        localInstancesCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_LocalInstances")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleName("Host Label");
        jLabel5.getAccessibleContext().setAccessibleName("Port Label");
        hostField.getAccessibleContext().setAccessibleName(bundle.getString("TTL_Host")); // NOI18N
        hostField.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_Host")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleName("Username Label");
        jLabel7.getAccessibleContext().setAccessibleName("Password Label");
        userNameField.getAccessibleContext().setAccessibleName(bundle.getString("TTL_Username")); // NOI18N
        userNameField.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_Username")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleName(bundle.getString("TTL_Password")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_Password")); // NOI18N
        profilePathField.getAccessibleContext().setAccessibleName(bundle.getString("TTL_ProfilePath")); // NOI18N
        profilePathField.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_ProfilePath")); // NOI18N
        portField.getAccessibleContext().setAccessibleName(bundle.getString("TTL_Port")); // NOI18N
        portField.getAccessibleContext().setAccessibleDescription(bundle.getString("MSG_Port")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void portFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portFieldFocusLost
        String port = portField.getText();
        if(!port.trim().matches("[0-9]+")){
            JOptionPane.showMessageDialog(portField,NbBundle.getMessage(ConnectionTabVisualPanel.class,"ERR_InvalidPort"));                                    
        } else if((new java.lang.Integer(port)).intValue() > 65535) {
            JOptionPane.showMessageDialog(portField,NbBundle.getMessage(ConnectionTabVisualPanel.class,"ERR_InvalidPort"));
        } else {
            targetData.setPort(port);
        }
    }//GEN-LAST:event_portFieldFocusLost
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JComboBox localInstancesCombo;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JTextField portField;
    private javax.swing.JTextField profilePathField;
    private javax.swing.JComboBox serverTypeCombo;
    private javax.swing.JTextField userNameField;
    // End of variables declaration//GEN-END:variables
    
    
    
    
    
    
    
    
    
    /**
     * A listener that reacts to the change of the server type combobox,
     * is the local server type is selected we should disable several fields
     * and enable some others instead.
     *
     * @author Kirill Sorokin, edited by Dmitry Lipin
     */
    private class ServerTypeActionListener implements ActionListener {
        /**
         * The main action handler. This method is called when the combobox
         * value changes
         */
        
        public void actionPerformed(ActionEvent e) {
            // if the selected type is local
            if (serverTypeCombo.getSelectedItem().equals(NbBundle.
                    getMessage(ServerProperties.class,
                    "TXT_ServerTypeLocal"))) {                         // NOI18N
                Instance instance = (Instance) localInstancesCombo.
                        getSelectedItem();
                
                // enable the local instances combo
                localInstancesCombo.setEnabled(true);
                
                // enable and set as read-only the domain path field
                profilePathField.setEnabled(true);
                profilePathField.setEditable(false);
                
                // enable and set as read-only the host field
                hostField.setEnabled(true);
                hostField.setEditable(false);
                hostField.setText(instance.getHost());
                
                // enable and set as read-only the port field
                //portField.setEnabled(true);
                //portField.setEditable(false);
                //portField.setText(instance.getPort());
                portField.setEnabled(true);
                portField.setEditable(false);
                //portField.setValue(new Integer(instance.getPort()));
                portField.setText(instance.getPort());
                
            } else {
                // disable the local instances combo
                localInstancesCombo.setEnabled(false);
                
                // disable the domain path field
                profilePathField.setEnabled(false);
                profilePathField.setEditable(false);
                
                // enable and set as read-write the host field
                hostField.setEnabled(true);
                hostField.setEditable(false);
                
                // enable and set as read-write the port field
                portField.setEnabled(true);
                portField.setEditable(true);
                //portField.setEditable(true);
            }
            
            isValid();
        }
    }
    /**
     * Updates the local instances combobox model with the fresh local
     * instances list
     */
    public void updateInstancesList() {
        localInstancesCombo.setModel(
                new InstancesModel(
                tabServerProperties.getServerInstances(
                targetData.getServerRoot())));
        updateInstanceInfo();
    }
    
    /**
     * Updates the selected local instance information, i.e. profile path,
     * host, port.
     */
    private void updateInstanceInfo() {
        // get the selected local instance
        Instance instance = (Instance) localInstancesCombo.getSelectedItem();
        
        // set the fields' values
        profilePathField.setText(instance.getDomainPath());
        hostField.setText(instance.getHost());
        //portField.setValue(new Integer(instance.getPort()));
        portField.setText(instance.getPort());
    }
    /**
     * A simple listeners that reacts to user's selectin a local instance. It
     * updates the selected instance info.
     *
     * @author Kirill Sorokin
     */
    private class InstanceSelectionListener implements ActionListener {
        /**
         * The main action handler. This method is called when a new local
         * instance is selected
         */
        public void actionPerformed(ActionEvent e) {
            updateInstanceInfo();
        }
    }
    
    
    
    
}
