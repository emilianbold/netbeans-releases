/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Vector;

import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;

import org.netbeans.lib.ddl.DBConnection;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseDriver;

import org.openide.util.NbBundle;

public class NewConnectionPanel extends javax.swing.JPanel implements DocumentListener, ListDataListener {

    private Vector templates;
    private DatabaseConnection connection;

    /** The support for firing property changes */
    private PropertyChangeSupport propertySupport;

    public NewConnectionPanel(Vector templates, String driver, String database, String loginname) {
        this(templates, new DatabaseConnection(driver, database, loginname, null));
    }

    public NewConnectionPanel(Vector templates, DatabaseConnection connection) {
//    public NewConnectionPanel() {
        propertySupport = new PropertyChangeSupport(this);
        this.templates = templates;
        this.connection = connection;
        initComponents();
        connectProgressBar.setBorderPainted(false);
        initAccessibility();

        PropertyChangeListener connectionListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals("connecting")) { //NOI18N
                    startProgress();
                }
                if (event.getPropertyName().equals("connected")) { //NOI18N
                    stopProgress(true);
                }
                if (event.getPropertyName().equals("failed")) { //NOI18N
                    stopProgress(false);
                }
            }
        };
        this.connection.addPropertyChangeListener(connectionListener);

        driverTextField.setText(connection.getDriver());
        urlTextField.setText(connection.getDatabase());
        userTextField.setText(connection.getUser());

        String driver = connection.getDriver();
        String driverName = connection.getDriverName();
        if (driver != null && driverName != null) {
            DatabaseDriver dbDriver;
            for (int i = 0; i < templates.size(); i++) {
                dbDriver = (DatabaseDriver) templates.elementAt(i);
                if (dbDriver.getURL().equals(driver) && dbDriver.getName().equals(driverName)) {
                    templateComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        driverTextField.getDocument().addDocumentListener(this);
        urlTextField.getDocument().addDocumentListener(this);
        userTextField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        templateComboBox.getModel().addListDataListener(this);
    }

    private void initAccessibility() {
        templateLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverNameA11yDesc")); //NOI18N
        templateComboBox.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverNameComboBoxA11yName")); //NOI18N
        driverLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverClassA11yDesc")); //NOI18N
        driverTextField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverClassComboBoxA11yName")); //NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDatabaseURLA11yDesc")); //NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDatabaseURLTextFieldA11yName")); //NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionUserNameA11yDesc")); //NOI18N
        userTextField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionPasswordTextFieldA11yName")); //NOI18N
        connectProgressBar.getAccessibleContext().setAccessibleName(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yName")); //NOI18N
        connectProgressBar.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc")); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        templateLabel = new javax.swing.JLabel();
        driverLabel = new javax.swing.JLabel();
        urlLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        templateComboBox = new javax.swing.JComboBox(templates);
        driverTextField = new javax.swing.JTextField();
        urlTextField = new javax.swing.JTextField();
        userTextField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        passwordCheckBox = new javax.swing.JCheckBox();
        connectProgressBar = new javax.swing.JProgressBar();

        setLayout(new java.awt.GridBagLayout());

        templateLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverName_Mnemonic").charAt(0));
        templateLabel.setLabelFor(templateComboBox);
        templateLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(templateLabel, gridBagConstraints);

        driverLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverClass_Mnemonic").charAt(0));
        driverLabel.setLabelFor(driverTextField);
        driverLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverClass"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(driverLabel, gridBagConstraints);

        urlLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDatabaseURL_Mnemonic").charAt(0));
        urlLabel.setLabelFor(urlTextField);
        urlLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDatabaseURL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(urlLabel, gridBagConstraints);

        userLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionUserName_Mnemonic").charAt(0));
        userLabel.setLabelFor(userTextField);
        userLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionUserName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(userLabel, gridBagConstraints);

        passwordLabel.setDisplayedMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionPassword_Mnemonic").charAt(0));
        passwordLabel.setLabelFor(passwordField);
        passwordLabel.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionPassword"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(passwordLabel, gridBagConstraints);

        templateComboBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverNameComboBoxA11yDesc"));
        templateComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                templateComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 11);
        add(templateComboBox, gridBagConstraints);

        driverTextField.setColumns(50);
        driverTextField.setEditable(false);
        driverTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverClassComboBoxA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(driverTextField, gridBagConstraints);

        urlTextField.setColumns(50);
        urlTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDatabaseURLTextFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(urlTextField, gridBagConstraints);

        userTextField.setColumns(50);
        userTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionUserNameTextFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(userTextField, gridBagConstraints);

        passwordField.setColumns(50);
        passwordField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionPasswordTextFieldA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordField, gridBagConstraints);

        passwordCheckBox.setMnemonic(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionRememberPassword_Mnemonic").charAt(0));
        passwordCheckBox.setText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionRememberPassword"));
        passwordCheckBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionRememberPasswordA11yDesc"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        add(passwordCheckBox, gridBagConstraints);

        connectProgressBar.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_ConnectionProgressBarA11yDesc"));
        connectProgressBar.setString("");
        connectProgressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(connectProgressBar, gridBagConstraints);

    }//GEN-END:initComponents

    private void templateComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboBoxActionPerformed
        javax.swing.JComboBox combo = (javax.swing.JComboBox) evt.getSource();
        Object drv = combo.getSelectedItem();
        String urlPrefix = null;
        String driver = null;
        if (drv != null && drv instanceof DatabaseDriver) {
           urlPrefix = ((DatabaseDriver) drv).getDatabasePrefix();
           driver = ((DatabaseDriver) drv).getURL();
        }
        if (urlPrefix != null)
           urlTextField.setText(urlPrefix);
        if (driver != null)
           driverTextField.setText(driver);
    }//GEN-LAST:event_templateComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar connectProgressBar;
    private javax.swing.JLabel driverLabel;
    private javax.swing.JTextField driverTextField;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JComboBox templateComboBox;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTextField;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    private String getSelectedDriver() {
        String drvval;
        int idx = templateComboBox.getSelectedIndex();

        if (idx != -1)
            drvval = ((DatabaseDriver) templateComboBox.getItemAt(idx)).getURL();
        else
            drvval = (String) templateComboBox.getSelectedItem();

        return drvval;
    }

    public void setConnectionInfo() {
        connection.setDriver(getSelectedDriver());
        connection.setDatabase(urlTextField.getText());
        connection.setUser(userTextField.getText());
        connection.setPassword(getPassword());
        connection.setRememberPassword(passwordCheckBox.isSelected());
    }

    public DBConnection getConnection() {
        return connection;
    }

    public String getDriver() {
        return getSelectedDriver();
    }

    public String getDatabase() {
        return urlTextField.getText();
    }

    public String getUser() {
        return userTextField.getText();
    }

    public String getPassword() {
        String password;
        String tempPassword = new String(passwordField.getPassword());
        if (tempPassword.length() > 0)
            password = tempPassword;
        else
            password = null;

        return password;
    }

    public boolean rememberPassword() {
        return passwordCheckBox.isSelected();
    }

    public String getTitle() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDialogTitle"); //NOI18N
    }

    private void startProgress() {
        connectProgressBar.setBorderPainted(true);
        connectProgressBar.setIndeterminate(true);
        connectProgressBar.setString(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Connecting")); //NOI18N
    }

    private void stopProgress(boolean connected) {
        if (connected) {
            connectProgressBar.setValue(connectProgressBar.getMaximum());
            connectProgressBar.setString(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Established")); //NOI18N
        } else {
            connectProgressBar.setValue(connectProgressBar.getMinimum());
            connectProgressBar.setString(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ConnectionProgress_Failed")); //NOI18N
        }
        connectProgressBar.setIndeterminate(false);
    }

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }

    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        fireChange();
    }

    public void contentsChanged(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    public void intervalAdded(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    private void fireChange() {
        propertySupport.firePropertyChange("argumentChanged", null, null);
        connectProgressBar.setBorderPainted(false);
        connectProgressBar.setValue(connectProgressBar.getMinimum());
        connectProgressBar.setString(""); //NOI18N
    }

    /** Add property change listener
    * Registers a listener for the PropertyChange event. The connection object
    * should fire a PropertyChange event whenever somebody changes driver, database,
    * login name or password.
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener (l);
    }

    /** Remove property change listener
    * Remove a listener for the PropertyChange event.
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener (l);
    }
}
