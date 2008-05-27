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

package org.netbeans.modules.db.explorer.dlg;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.util.DatabaseExplorerInternalUIs;
import org.netbeans.modules.db.util.DriverListUtil;

import org.openide.util.NbBundle;

public class NewConnectionPanel extends ConnectionDialog.FocusablePanel implements DocumentListener, ListDataListener {

    private ConnectionDialogMediator mediator;
    // private Vector templates;
    private DatabaseConnection connection;
    private ProgressHandle progressHandle;

    private static final String BUNDLE = "org.netbeans.modules.db.resources.Bundle"; //NOI18N

    public NewConnectionPanel(ConnectionDialogMediator mediator, String driverClass, DatabaseConnection connection) {
        this.mediator = mediator;
        this.connection = connection;
        initComponents();
        initAccessibility();
        
        DatabaseExplorerInternalUIs.connect(templateComboBox, JDBCDriverManager.getDefault(), driverClass);
        
        ConnectionProgressListener progressListener = new ConnectionProgressListener() {
            public void connectionStarted() {
                startProgress();
            }
            
            public void connectionStep(String step) {
                setProgressMessage(step);
            }

            public void connectionFinished() {
                stopProgress(true);
            }

            public void connectionFailed() {
                stopProgress(false);
            }
        };
        mediator.addConnectionProgressListener(progressListener);
        
        userTextField.setText(connection.getUser());
        passwordField.setText(connection.getPassword());

        String driver = connection.getDriver();
        String driverName = connection.getDriverName();
        if (driver != null && driverName != null) {
            for (int i = 0; i < templateComboBox.getItemCount(); i++) {
                Object item = templateComboBox.getItemAt(i);
                if (item instanceof JDBCDriver) {
                    JDBCDriver dbDriver = (JDBCDriver)item;
                    if (dbDriver.getClassName().equals(driver) && dbDriver.getName().equals(driverName)) {
                        templateComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        databaseField.getDocument().addDocumentListener(this);
        hostField.getDocument().addDocumentListener(this);
        portField.getDocument().addDocumentListener(this);
        urlField.getDocument().addDocumentListener(this);
        userTextField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        templateComboBox.getModel().addListDataListener(this);

        checkValid();
    }

    private void initAccessibility() {
        ResourceBundle b = NbBundle.getBundle(BUNDLE);
        templateLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDriverNameA11yDesc")); //NOI18N
        templateComboBox.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDriverNameComboBoxA11yName")); //NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDatabaseURLA11yDesc")); //NOI18N
        urlField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDatabaseURLTextFieldA11yName")); //NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionUserNameA11yDesc")); //NOI18N
        userTextField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionPasswordTextFieldA11yName")); //NOI18N
    }

    public void initializeFocus() {
        getInitiallyFocusedComponent().requestFocusInWindow();
    }

    private JComponent getInitiallyFocusedComponent() {
        if (templateComboBox.getItemCount() <= 1) { // the first item is "Add Driver...""
            return templateComboBox;
        }
        if (userTextField.getText().length() == 0) {
            return userTextField;
        }
        if (passwordField.getPassword().length == 0) {
            return passwordField;
        }
        // fall back to the URL field
        return hostField;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        templateLabel = new javax.swing.JLabel();
        templateComboBox = new javax.swing.JComboBox();
        urlLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        userTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        passwordCheckBox = new javax.swing.JCheckBox();
        hostField = new javax.swing.JTextField();
        hostLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        toggleShowURLCheckbox = new javax.swing.JCheckBox();
        databaseField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        urlField = new javax.swing.JTextField();

        FormListener formListener = new FormListener();

        templateLabel.setLabelFor(templateComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(templateLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverName")); // NOI18N

        templateComboBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverNameComboBoxA11yDesc")); // NOI18N
        templateComboBox.addItemListener(formListener);
        templateComboBox.addActionListener(formListener);

        urlLabel.setLabelFor(urlField);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDatabaseURL")); // NOI18N
        urlLabel.setFocusable(false);

        userLabel.setLabelFor(userTextField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionUserName")); // NOI18N

        userTextField.setColumns(50);
        userTextField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionUserNameTextFieldA11yDesc")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionPassword")); // NOI18N

        passwordField.setColumns(50);
        passwordField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionPasswordTextFieldA11yDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passwordCheckBox, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionRememberPassword")); // NOI18N
        passwordCheckBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionRememberPasswordA11yDesc")); // NOI18N
        passwordCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        hostField.setText("localhost");
        hostField.setToolTipText("Specify the host name for the server");
        hostField.addActionListener(formListener);

        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, "&Host:");

        portField.setToolTipText("Specify the port number for the server; you can leave blank if you want to use the default port");

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "&Port:");

        org.openide.awt.Mnemonics.setLocalizedText(toggleShowURLCheckbox, "&Show JDBC URL");
        toggleShowURLCheckbox.setToolTipText("Check this to show or hide the JDBC URL");
        toggleShowURLCheckbox.addActionListener(formListener);

        databaseField.setToolTipText("Please specify the database name");

        jLabel2.setLabelFor(databaseField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "&Database:");

        urlField.setToolTipText("The JDBC URL for this connection");

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
                                .add(passwordLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(12, 12, 12)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(hostLabel)
                                    .add(userLabel)
                                    .add(templateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(jLabel1)))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(urlLabel))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(jLabel2)))
                        .add(13, 13, 13)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(templateComboBox, 0, 362, Short.MAX_VALUE)
                            .add(hostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                            .add(portField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                            .add(databaseField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                            .add(userTextField, 0, 0, Short.MAX_VALUE)
                            .add(passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(urlField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))))
                    .add(layout.createSequentialGroup()
                        .add(102, 102, 102)
                        .add(passwordCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)))
                .add(12, 12, 12))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(toggleShowURLCheckbox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(templateLabel)
                    .add(templateComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(hostLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(databaseField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(userLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordCheckBox)
                .add(18, 18, 18)
                .add(toggleShowURLCheckbox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {hostField, passwordField, portField, templateComboBox, userLabel, userTextField}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.ItemListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == templateComboBox) {
                NewConnectionPanel.this.templateComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == hostField) {
                NewConnectionPanel.this.hostFieldActionPerformed(evt);
            }
            else if (evt.getSource() == toggleShowURLCheckbox) {
                NewConnectionPanel.this.toggleShowURLCheckboxActionPerformed(evt);
            }
        }

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == templateComboBox) {
                NewConnectionPanel.this.templateComboBoxItemStateChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void templateComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_templateComboBoxItemStateChanged
        checkValid();
    }//GEN-LAST:event_templateComboBoxItemStateChanged

    private void templateComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboBoxActionPerformed
        Object item = templateComboBox.getSelectedItem();
        if (!(item instanceof JDBCDriver)) {
            return;
        }
        if (!connection.getDatabase().equals("")) // NOI18N
            urlField.setText(connection.getDatabase());
        
    }//GEN-LAST:event_templateComboBoxActionPerformed

private void hostFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_hostFieldActionPerformed

private void toggleShowURLCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleShowURLCheckboxActionPerformed
}//GEN-LAST:event_toggleShowURLCheckboxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField databaseField;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JComboBox templateComboBox;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JCheckBox toggleShowURLCheckbox;
    private javax.swing.JTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userTextField;
    // End of variables declaration//GEN-END:variables

    private JDBCDriver getSelectedDriver() {
        Object item = templateComboBox.getSelectedItem();
        if (item instanceof JDBCDriver) {
            return (JDBCDriver)item;
        }
        return null;
    }
    
    public void setConnectionInfo() {
        JDBCDriver driver = getSelectedDriver();
        if (driver != null) {
            connection.setDriverName(driver.getName());
            connection.setDriver(driver.getClassName());
        }
        connection.setDatabase(urlField.getText());
        connection.setUser(userTextField.getText());
        connection.setPassword(getPassword());
        connection.setRememberPassword(passwordCheckBox.isSelected());
    }

    private String getPassword() {
        String password;
        String tempPassword = new String(passwordField.getPassword());
        if (tempPassword.length() > 0)
            password = tempPassword;
        else
            password = null;

        return password;
    }

    public String getTitle() {
        return NbBundle.getBundle(BUNDLE).getString("NewConnectionDialogTitle"); //NOI18N
    }

    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(
                        NbBundle.getBundle(BUNDLE).getString("ConnectionProgress_Connecting"));
                progressHandle.start();
            }
        });
    }
    
    private void setProgressMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if  (progressHandle != null) {
                    progressHandle.setDisplayName(message);
                }
            }
        });
    }

    private void stopProgress(final boolean connected) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (progressHandle != null) {
                    progressHandle.finish();
                }
            }
        });
    }
    
    private void resetProgress() {
        if (progressHandle != null) {
            progressHandle.setDisplayName(""); // NOI18N
        }
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
        if (urlField.isVisible()) {
            updateURL();
        }
        firePropertyChange("argumentChanged", null, null);
        resetProgress();
    }
    
    private void updateURL() {
        // Get the URL template for this driver
    }
    
    private void checkValid() {
        mediator.setValid(getSelectedDriver() != null);
    }
}
