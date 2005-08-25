/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.plugins.api.OptionalDeploymentManagerFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author  Andrei Badea
 */
public class ServerChooserVisual extends javax.swing.JPanel {
    private final List listeners = new ArrayList();
    private AddServerInstanceWizard wizard;
    private HashMap displayNames;
    
    public ServerChooserVisual() {
        displayNames = new HashMap();
        initComponents();
        
        ServerAdapter selected = (ServerAdapter)serverComboBox.getSelectedItem();
        if (selected != null)
            fillDisplayName(selected.getServer());
        
        displayNameEditField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }
            
            public void removeUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }
            
            public void changedUpdate(DocumentEvent e) {
                displayNameEditFieldUpdate();
            }
        });
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public void read(AddServerInstanceWizard wizard) {
        if (this.wizard == null)
            this.wizard = wizard;
        
        Object prop = wizard.getProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME);
        if (prop != null)
            displayNameEditField.setText((String)prop);
    }
    
    public void store(AddServerInstanceWizard wizard) {
        wizard.putProperty(AddServerInstanceWizard.PROP_DISPLAY_NAME, displayNameEditField.getText());
        Object selectedItem = serverComboBox.getSelectedItem();
        if (selectedItem != null) {
            wizard.putProperty(AddServerInstanceWizard.PROP_SERVER, ((ServerAdapter)selectedItem).getServer());
        }
    }
    
    public boolean isValid() {
        boolean result = isServerValid() && isDisplayNameValid();
        if (result)
            wizard.setErrorMessage(null);
        
        return result;
    }
    
    private boolean isServerValid() {
        boolean result = serverComboBox.getSelectedItem() != null;
        if (!result)
            wizard.setErrorMessage(NbBundle.getMessage(ServerChooserVisual.class, "MSG_SCV_ChooseServer"));
        return result;
    }
    
    private boolean isDisplayNameValid() {
        String trimmed = displayNameEditField.getText().trim();
        boolean result;
        
        if (trimmed.length() <= 0) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerChooserVisual.class, "MSG_SCV_DisplayName"));
            return false;
        }
        
        if (getServerInstance(trimmed) != null) {
            wizard.setErrorMessage(NbBundle.getMessage(ServerChooserVisual.class, "MSG_SCV_DisplayNameExists"));
            return false;
        }
        
        return true;
    }
    
    private ServerInstance getServerInstance(String displayName) {
        Iterator iter = ServerRegistry.getInstance().getInstances().iterator();
        while (iter.hasNext()) {
            ServerInstance instance = (ServerInstance)iter.next();
            if (instance.getDisplayName().compareToIgnoreCase(displayName) == 0)
                return instance;
        }
        return null;
    }
    
    private void displayNameEditFieldUpdate() {
        fireChange();
    }
    
    private void fireChange() {
        ChangeEvent event = new ChangeEvent(this);
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext())
            ((ChangeListener)iter.next()).stateChanged(event);
    }   
    
    private String generateDisplayName(Server server) {
        String name;
        int count = 0;
        
        do {
            name = server.getDisplayName();
            if (count != 0)
                name += " (" + String.valueOf(count) + ")";
            
            count++;
        } while (getServerInstance(name) != null);
        
        return name;
    }
    
    private void fillDisplayName(Server server) {
        String name = (String)displayNames.get(server);
        if (name == null)
            name = generateDisplayName(server);
        displayNameEditField.setText(name);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        serverComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        displayNameEditField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setName(org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_Name"));
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_NAME"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_DESC"));
        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_Server_mnem").charAt(0));
        jLabel1.setLabelFor(serverComboBox);
        jLabel1.setText(org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_Server"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel1, gridBagConstraints);

        serverComboBox.setModel(new ServerModel());
        serverComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                serverComboBoxItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(serverComboBox, gridBagConstraints);
        serverComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_NAME_Server"));
        serverComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_DESC_Server"));

        jLabel2.setDisplayedMnemonic(org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_DisplayName_mnem").charAt(0));
        jLabel2.setLabelFor(displayNameEditField);
        jLabel2.setText(org.openide.util.NbBundle.getBundle(ServerChooserVisual.class).getString("LBL_SCV_DisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(jLabel2, gridBagConstraints);

        displayNameEditField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                displayNameEditFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(displayNameEditField, gridBagConstraints);
        displayNameEditField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_NAME_DisplayName"));
        displayNameEditField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerChooserVisual.class, "A11Y_SCV_DESC_DisplayName"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents

    private void displayNameEditFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_displayNameEditFieldKeyReleased
        displayNames.put(((ServerAdapter)serverComboBox.getSelectedItem()).getServer(), displayNameEditField.getText());
    }//GEN-LAST:event_displayNameEditFieldKeyReleased

    private void serverComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_serverComboBoxItemStateChanged
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            Server server = ((ServerAdapter)evt.getItem()).getServer();
            fillDisplayName(server);
        }
        fireChange();
    }//GEN-LAST:event_serverComboBoxItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField displayNameEditField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox serverComboBox;
    // End of variables declaration//GEN-END:variables
    
    private static class ServerModel implements ComboBoxModel {
        private List servers;
        private ServerAdapter selected;
                
        public ServerModel() {
            servers = new ArrayList();
            Collection allServers = ServerRegistry.getInstance().getServers();
            Iterator iter = allServers.iterator();
            while (iter.hasNext()) {
                Server server = (Server)iter.next();
                OptionalDeploymentManagerFactory factory = server.getOptionalFactory();
                if (factory != null && factory.getAddInstanceIterator() != null) {
                    ServerAdapter serverAdapter = new ServerAdapter(server);
                    servers.add(serverAdapter);
                    if ("J2EE".equals(server.getShortName())) { // NOI18N
                        selected = serverAdapter;
                    }
                }
            }
            Collections.sort(servers);
            if (selected == null) {
                selected = (servers.size() > 0) ? (ServerAdapter)servers.get(0) : null;
            }
        }
        
        public Object getElementAt(int index) {
            return servers.get(index);
        }

        public void removeListDataListener(javax.swing.event.ListDataListener l) {
        }

        public void addListDataListener(javax.swing.event.ListDataListener l) {
        }

        public int getSize() {
            return servers.size();
        }
        
        public Object getSelectedItem() {
            return selected;
        }
        
        public void setSelectedItem(Object anItem) {
            selected = (ServerAdapter)anItem;
        }
    }
    
    private static class ServerAdapter implements Comparable {
        private Server server;
        
        public ServerAdapter(Server server) {
            this.server = server;
        }
        
        public Server getServer() {
            return server;
        }
        
        public String toString() {
            return server.getDisplayName();
        }
        
        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
    }
}
