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
package org.netbeans.modules.j2ee.websphere6.ui;

import java.util.Vector;
import java.awt.event.*;
import javax.swing.*;
import org.openide.ErrorManager;
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
        String url = (String) ips.getProperty("url"); // NOI18N
        int dex = url.indexOf(WSURIManager.WSURI);
        if (dex > -1)
            url = url.substring(dex+WSURIManager.WSURI.length());
        
        localInstancesCombo.setModel(
                new InstancesModel(
                tabServerProperties.getServerInstances(
                targetData.getServerRoot())));
        
        //localInstancesCombo.addActionListener(tabServerProperties.getInstanceSelectionListener());
        
        
        dex=url.indexOf(":");
        if(dex>-1)
            url=url.substring(0,dex);
        hostField.setText(url);
        
        
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
        String getLocal=targetData.getIsLocal();
        if(getLocal!=null)
            serverTypeCombo.setSelectedItem(getLocal.equals("true")?
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
                        "ERR_INVALID_DOMAIN_ROOT"));                           // NOI18N
                return false;
            }
        }
        
        // check the host field (not empty)
        if (hostField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(ConnectionTabVisualPanel.class,
                    "ERR_INVALID_HOST"));                // NOI18N
            return false;
        }
        
        // check the port field (not empty and a positive integer)
        //if (!portField.getText().trim().matches("[0-9]+")) {
        //if (!portField.getValue().toString().trim().matches("[0-9]+")) {
        if (!portField.getText().trim().matches("[0-9]+")) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(ConnectionTabVisualPanel.class,
                    "ERR_INVALID_PORT"));                              // NOI18N
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("LBL_LocalRemote"));

        serverTypeCombo.setEnabled(false);
        serverTypeCombo.getAccessibleContext().setAccessibleName("Access Method");

        jLabel3.setLabelFor(localInstancesCombo);
        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("LBL_LocalInstances"));
        jLabel3.getAccessibleContext().setAccessibleName("Local Instances Label");

        localInstancesCombo.setEnabled(false);
        localInstancesCombo.getAccessibleContext().setAccessibleName("Local Instances");

        jLabel4.setLabelFor(hostField);
        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("LBL_Host"));
        jLabel4.getAccessibleContext().setAccessibleName("Host Label");

        jLabel5.setLabelFor(portField);
        jLabel5.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("LBL_Port"));
        jLabel5.getAccessibleContext().setAccessibleName("Port Label");

        hostField.setEditable(false);
        hostField.getAccessibleContext().setAccessibleName("Host");

        jLabel6.setLabelFor(userNameField);
        jLabel6.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("LBL_Username"));
        jLabel6.getAccessibleContext().setAccessibleName("Username Label");

        jLabel7.setLabelFor(passwordField);
        jLabel7.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("LBL_Password"));
        jLabel7.getAccessibleContext().setAccessibleName("Password Label");

        userNameField.getAccessibleContext().setAccessibleName("Username");

        profilePathField.setEditable(false);

        jLabel1.setLabelFor(profilePathField);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/ui/Bundle").getString("LBL_ProfilePath"));

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
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel3)
                            .add(jLabel2)
                            .add(jLabel1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(serverTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 90, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(localInstancesCombo, 0, 320, Short.MAX_VALUE)
                            .add(profilePathField)))
                    .add(layout.createSequentialGroup()
                        .add(36, 36, 36)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel7)
                            .add(jLabel6)
                            .add(jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(passwordField)
                                .add(userNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(62, 62, 62)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(hostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)))
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
                    .add(profilePathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(userNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void portFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portFieldFocusLost
        String port = portField.getText();
        if(!port.trim().matches("[0-9]+")){
            JOptionPane.showMessageDialog(portField,NbBundle.getMessage(ConnectionTabVisualPanel.class,"ERR_INVALID_PORT"));                                    
        } else if((new java.lang.Integer(port)).intValue() > 65535) {
            JOptionPane.showMessageDialog(portField,NbBundle.getMessage(ConnectionTabVisualPanel.class,"ERR_INVALID_PORT"));
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
