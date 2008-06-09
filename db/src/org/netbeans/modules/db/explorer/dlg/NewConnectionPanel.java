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
import java.awt.Font;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.modules.db.util.JdbcUrl;
import org.openide.util.NbBundle;

public class NewConnectionPanel extends ConnectionDialog.FocusablePanel implements DocumentListener, ListDataListener {

    private ConnectionDialogMediator mediator;
    // private Vector templates;
    private DatabaseConnection connection;
    private ProgressHandle progressHandle;
    private Window window;
    
    private boolean updatingUrl = false;
    private boolean updatingFields = false;

    private static final String BUNDLE = "org.netbeans.modules.db.resources.Bundle"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(NewConnectionPanel.class.getName());
    
    private static ResourceBundle bundle() {
        return NbBundle.getBundle(BUNDLE);
    }
    
    private static String getMessage(String key, Object ... args) {
        return MessageFormat.format(bundle().getString(key), args);
    }

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
        
        userField.setText(connection.getUser());
        passwordField.setText(connection.getPassword());

        String driver = connection.getDriver();
        String driverName = connection.getDriverName();
        if (driver != null && driverName != null) {
            for (int i = 0; i < templateComboBox.getItemCount(); i++) {
                Object item = templateComboBox.getItemAt(i);
                if (item instanceof JdbcUrl) {
                    JdbcUrl url = ((JdbcUrl)item);
                    assert url.getDriver() != null;
                    if (url.getClassName().equals(driver) && url.getDriver().getName().equals(driverName)) {
                        templateComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }        
        }

        databaseField.getDocument().addDocumentListener(this);
        hostField.getDocument().addDocumentListener(this);
        portField.getDocument().addDocumentListener(this);
        additionalPropsField.getDocument().addDocumentListener(this);
        userField.getDocument().addDocumentListener(this);
        passwordField.getDocument().addDocumentListener(this);
        templateComboBox.getModel().addListDataListener(this);

        urlField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                updateFieldsFromUrl();
            }
            
        });
        
        setUpFields();
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    private void initAccessibility() {
        ResourceBundle b = NbBundle.getBundle(BUNDLE);
        templateLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDriverNameA11yDesc")); //NOI18N
        templateComboBox.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDriverNameComboBoxA11yName")); //NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionUserNameA11yDesc")); //NOI18N
        userField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionPasswordTextFieldA11yName")); //NOI18N
        hostLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionHostA11yDesc")); //NOI18N
        hostField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionHostTextFieldA11yName")); //NOI18N
        portLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionPortA11yDesc")); //NOI18N
        portField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionPortTextFieldA11yName")); //NOI18N
        serverNameField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionServerNameTextFieldA11yName")); //NOI18N
        serverNameLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionServerNameA11yDesc")); //NOI18N
        databaseField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDatabaseNameTextFieldA11yName")); //NOI18N
        databaseLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDatabaseNameA11yDesc")); //NOI18N
        additionalPropsField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionAdditionalPropertiesTextFieldA11yName")); //NOI18N
        additionalPropsLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionAdditionalPropertiesA11yDesc")); //NOI18N
        urlField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionJDBCURLTextFieldA11yName")); //NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionJDBCURLA11yDesc")); //NOI18N
    }

    public void initializeFocus() {
        getInitiallyFocusedComponent().requestFocusInWindow();
    }

    private JComponent getInitiallyFocusedComponent() {
        if (templateComboBox.getItemCount() <= 1) { // the first item is "Add Driver...""
            return templateComboBox;
        }
        if (userField.getText().length() == 0) {
            return userField;
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
        userField = new javax.swing.JTextField();
        additionalPropsField = new javax.swing.JTextField();
        serverNameLabel = new javax.swing.JLabel();
        serverNameField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        databaseField = new javax.swing.JTextField();
        portField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordCheckBox = new javax.swing.JCheckBox();
        additionalPropsLabel = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        passwordField = new javax.swing.JPasswordField();
        databaseLabel = new javax.swing.JLabel();
        showUrlCheckBox = new javax.swing.JCheckBox();
        urlField = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        errorLabel = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        templateLabel.setLabelFor(templateComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(templateLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDriverName")); // NOI18N

        templateComboBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionDriverNameComboBoxA11yDesc")); // NOI18N
        templateComboBox.addItemListener(formListener);
        templateComboBox.addActionListener(formListener);

        userField.setColumns(50);
        userField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionUserNameTextFieldA11yDesc")); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/db/resources/Bundle"); // NOI18N
        additionalPropsField.setToolTipText(bundle.getString("ACS_NewConnectionAdditionalPropertiesA11yDesc")); // NOI18N
        additionalPropsField.addActionListener(formListener);

        serverNameLabel.setLabelFor(serverNameField);
        org.openide.awt.Mnemonics.setLocalizedText(serverNameLabel, bundle.getString("NewConnectionServerName")); // NOI18N

        serverNameField.setToolTipText(bundle.getString("ACS_NewConnectionServerNameA11yDesc")); // NOI18N
        serverNameField.addActionListener(formListener);

        userLabel.setLabelFor(userField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionUserName")); // NOI18N

        databaseField.setToolTipText(bundle.getString("ACS_NewConnectionDatabaseNameA11yDesc")); // NOI18N
        databaseField.addActionListener(formListener);

        portField.setToolTipText(bundle.getString("ACS_NewConnectionPortA11yDesc")); // NOI18N
        portField.addActionListener(formListener);

        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, bundle.getString("NewConnectionPort")); // NOI18N

        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, "&Host:");

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionPassword")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passwordCheckBox, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionRememberPassword")); // NOI18N
        passwordCheckBox.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionRememberPasswordA11yDesc")); // NOI18N
        passwordCheckBox.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        additionalPropsLabel.setLabelFor(additionalPropsField);
        org.openide.awt.Mnemonics.setLocalizedText(additionalPropsLabel, bundle.getString("NewConnectionAdditionalProperties")); // NOI18N

        hostField.setText("localhost");
        hostField.setToolTipText(bundle.getString("ACS_NewConnectionHostA11yDesc")); // NOI18N
        hostField.addActionListener(formListener);
        hostField.addFocusListener(formListener);

        passwordField.setColumns(50);
        passwordField.setToolTipText(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ACS_NewConnectionPasswordTextFieldA11yDesc")); // NOI18N

        databaseLabel.setLabelFor(databaseField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLabel, bundle.getString("NewConnectionDatabase")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showUrlCheckBox, bundle.getString("NewConnectionShowJDBCURL")); // NOI18N
        showUrlCheckBox.setToolTipText("Check this to show or hide the JDBC URL");
        showUrlCheckBox.addActionListener(formListener);

        urlField.setToolTipText(bundle.getString("ACS_NewConnectionJDBCURLA11yDesc")); // NOI18N
        urlField.addActionListener(formListener);
        urlField.addFocusListener(formListener);
        urlField.addKeyListener(formListener);

        urlLabel.setLabelFor(urlField);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("NewConnectionDatabaseURL")); // NOI18N
        urlLabel.setFocusable(false);

        errorLabel.setForeground(new java.awt.Color(255, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, "Error label");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, showUrlCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, urlLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, additionalPropsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, serverNameLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, hostLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, passwordLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, userLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, databaseLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, portLabel)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, templateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(hostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                            .add(urlField)
                            .add(serverNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)
                            .add(additionalPropsField)
                            .add(userField, 0, 0, Short.MAX_VALUE)
                            .add(databaseField)
                            .add(portField)
                            .add(passwordField, 0, 0, Short.MAX_VALUE)
                            .add(passwordCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 308, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(templateComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE))
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
                    .add(hostLabel)
                    .add(hostField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portLabel)
                    .add(portField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(databaseLabel)
                    .add(databaseField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordCheckBox)
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverNameLabel)
                    .add(serverNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(additionalPropsLabel)
                    .add(additionalPropsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(20, 20, 20)
                .add(showUrlCheckBox)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(20, 20, 20))
        );

        layout.linkSize(new java.awt.Component[] {hostField, passwordField, portField, templateComboBox, userField, userLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        additionalPropsField.getAccessibleContext().setAccessibleDescription("Specify additional JDBC properties here.  This needs to be in the format expected by this JDBC driver.");
        serverNameField.getAccessibleContext().setAccessibleName("Server Name");
        serverNameField.getAccessibleContext().setAccessibleDescription("The server name for this connection");
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener, java.awt.event.ItemListener, java.awt.event.KeyListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == templateComboBox) {
                NewConnectionPanel.this.templateComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == additionalPropsField) {
                NewConnectionPanel.this.additionalPropsFieldActionPerformed(evt);
            }
            else if (evt.getSource() == serverNameField) {
                NewConnectionPanel.this.serverNameFieldActionPerformed(evt);
            }
            else if (evt.getSource() == databaseField) {
                NewConnectionPanel.this.databaseFieldActionPerformed(evt);
            }
            else if (evt.getSource() == portField) {
                NewConnectionPanel.this.portFieldActionPerformed(evt);
            }
            else if (evt.getSource() == hostField) {
                NewConnectionPanel.this.hostFieldActionPerformed(evt);
            }
            else if (evt.getSource() == showUrlCheckBox) {
                NewConnectionPanel.this.showUrlCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == urlField) {
                NewConnectionPanel.this.urlFieldActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == hostField) {
                NewConnectionPanel.this.hostFieldFocusLost(evt);
            }
            else if (evt.getSource() == urlField) {
                NewConnectionPanel.this.urlFieldFocusLost(evt);
            }
        }

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == templateComboBox) {
                NewConnectionPanel.this.templateComboBoxItemStateChanged(evt);
            }
        }

        public void keyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getSource() == urlField) {
                NewConnectionPanel.this.urlFieldKeyPressed(evt);
            }
        }

        public void keyReleased(java.awt.event.KeyEvent evt) {
        }

        public void keyTyped(java.awt.event.KeyEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void templateComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_templateComboBoxItemStateChanged
        setUpFields();
    }//GEN-LAST:event_templateComboBoxItemStateChanged

    private void templateComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboBoxActionPerformed
        setUpFields();
    }//GEN-LAST:event_templateComboBoxActionPerformed

private void hostFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostFieldActionPerformed
    JdbcUrl jdbcurl = getSelectedJdbcUrl();
    if (jdbcurl != null) {
        jdbcurl.put(JdbcUrl.TOKEN_HOST, hostField.getText());
    }
}//GEN-LAST:event_hostFieldActionPerformed

private void showUrlCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showUrlCheckBoxActionPerformed
    urlLabel.setVisible(showUrlCheckBox.isSelected());//GEN-LAST:event_showUrlCheckBoxActionPerformed
    if (showUrlCheckBox.isSelected()) {
        setUrlField();
    } else {
        updateUrlFromFields();
    }
    urlField.setVisible(showUrlCheckBox.isSelected());
    
    resize();
}

private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed

}//GEN-LAST:event_portFieldActionPerformed

private void databaseFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_databaseFieldActionPerformed

}//GEN-LAST:event_databaseFieldActionPerformed

private void serverNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverNameFieldActionPerformed
}//GEN-LAST:event_serverNameFieldActionPerformed

private void additionalPropsFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_additionalPropsFieldActionPerformed
}//GEN-LAST:event_additionalPropsFieldActionPerformed

private void urlFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlFieldActionPerformed

}//GEN-LAST:event_urlFieldActionPerformed

private void hostFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hostFieldFocusLost

}//GEN-LAST:event_hostFieldFocusLost

private void urlFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urlFieldKeyPressed
}//GEN-LAST:event_urlFieldKeyPressed

private void urlFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_urlFieldFocusLost

}//GEN-LAST:event_urlFieldFocusLost


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField additionalPropsField;
    private javax.swing.JLabel additionalPropsLabel;
    private javax.swing.JTextField databaseField;
    private javax.swing.JLabel databaseLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField serverNameField;
    private javax.swing.JLabel serverNameLabel;
    private javax.swing.JCheckBox showUrlCheckBox;
    private javax.swing.JComboBox templateComboBox;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    public void setConnectionInfo() {
        JdbcUrl url = getSelectedJdbcUrl();
        if (url != null) {
            JDBCDriver driver = url.getDriver();
            assert(driver != null);
            connection.setDriverName(driver.getName());
           connection.setDriver(driver.getClassName());
        }
        connection.setDatabase(urlField.getText());
        connection.setUser(userField.getText());
        connection.setPassword(getPassword());
        connection.setRememberPassword(passwordCheckBox.isSelected());
    }
    
    private void resize() {
        revalidate();
        if (window != null) {
            window.pack();
        }
    }
    
    /**
     * Set up which fields are enabled based on the URL template for the
     * selected driver
     */
    private void setUpFields() {
        Object item = templateComboBox.getSelectedItem();
        if ( item != null && !(item instanceof JdbcUrl)) {
            // This is an item indicating "Create a New Driver", and if
            // we futz with the fields, then the ComboBox wants to make the
            // drop-down invisible and the dialog never gets a chance to
            // get invoked.
            return;
        }
        
        JdbcUrl jdbcurl = (JdbcUrl)item;
        
        if (jdbcurl == null) {
            userField.setVisible(false);
            userLabel.setVisible(false);
            
            passwordField.setVisible(false);
            passwordLabel.setVisible(false);
            
            passwordCheckBox.setVisible(false);

            hostField.setVisible(false);
            hostLabel.setVisible(false);
            
            portField.setVisible(false);
            portLabel.setVisible(false);
            
            databaseField.setVisible(false);
            databaseLabel.setVisible(false);
            
            serverNameField.setVisible(false);
            serverNameLabel.setVisible(false);
            
            additionalPropsField.setVisible(false);
            additionalPropsLabel.setVisible(false);
            
            showUrlCheckBox.setVisible(false);
            urlField.setVisible(false);
            urlLabel.setVisible(false);
            
            checkValid();
            resize();
            return;
        }
                        
        // This assumes that all labels have the same font. Seems reasonable.
        // We use the bold font for required fields.
        Font regularFont = templateLabel.getFont();
        Font boldFont = regularFont.deriveFont(Font.BOLD);
        
        userField.setVisible(true);
        userLabel.setVisible(true);
        
        passwordField.setVisible(true);
        passwordLabel.setVisible(true);

        passwordCheckBox.setVisible(true);
        
        hostField.setVisible(jdbcurl.supportsToken(JdbcUrl.TOKEN_HOST));
        hostLabel.setVisible(jdbcurl.supportsToken(JdbcUrl.TOKEN_HOST));
        hostLabel.setFont(jdbcurl.requiresToken(JdbcUrl.TOKEN_HOST) ? boldFont : regularFont);
        
        portField.setVisible(jdbcurl.supportsToken(JdbcUrl.TOKEN_PORT));
        portLabel.setVisible(jdbcurl.supportsToken(JdbcUrl.TOKEN_PORT)); 
        portLabel.setFont(jdbcurl.requiresToken(JdbcUrl.TOKEN_PORT) ? boldFont : regularFont);
        
        boolean hasdb = jdbcurl.supportsToken(JdbcUrl.TOKEN_DB);
        databaseField.setVisible(hasdb);
        databaseLabel.setVisible(hasdb);
        if (hasdb) {
            databaseLabel.setText(jdbcurl.getDatabaseLabel());
        }
        databaseLabel.setFont(jdbcurl.requiresToken(JdbcUrl.TOKEN_DB) ? boldFont : regularFont);
        
        serverNameField.setVisible(jdbcurl.supportsToken(JdbcUrl.TOKEN_SERVERNAME));
        serverNameLabel.setVisible(jdbcurl.supportsToken(JdbcUrl.TOKEN_SERVERNAME));
        serverNameLabel.setFont(jdbcurl.requiresToken(JdbcUrl.TOKEN_SERVERNAME) ? boldFont : regularFont);

        if (! jdbcurl.urlIsParsed()) {
            additionalPropsField.setVisible(false);
            additionalPropsLabel.setVisible(false);
            
            showUrlCheckBox.setVisible(false);
            urlField.setVisible(true);
            urlLabel.setVisible(true);         
        } else {
            additionalPropsField.setVisible(true);
            additionalPropsLabel.setVisible(true);
            
            showUrlCheckBox.setVisible(true);
            showUrlCheckBox.setSelected(false);
            urlField.setVisible(false);
            urlLabel.setVisible(false);
        }
        
        setUrlField();
        checkValid();
        resize();
    }
    
    private JdbcUrl getSelectedJdbcUrl() {
        Object item = templateComboBox.getSelectedItem();
        if (! (item instanceof JdbcUrl)) {
            return null;
        }
        
        return (JdbcUrl)item;        
    }
    
    private void setUrlField() {
        if (!connection.getDatabase().equals("")) {
            urlField.setText(connection.getDatabase());
            return;
        }
        
        JdbcUrl jdbcurl = getSelectedJdbcUrl();
        if (jdbcurl == null) {
            urlField.setText("");
            return;
        }

        String url = jdbcurl.getUrl();
        if (!jdbcurl.urlIsParsed() && (isEmpty(url))) {
            urlField.setText(jdbcurl.getUrlTemplate());
        } else {
            urlField.setText(url);            
        }
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
        return NbBundle.getBundle(BUNDLE).getString("NewConnectionTitle"); //NOI18N
    }

    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(getMessage("ConnectionProgress_Connecting"));
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
        updateUrlFromFields();
        fireChange();
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateUrlFromFields();
        fireChange();
    }

    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateUrlFromFields();
        fireChange();
    }

    public void contentsChanged(javax.swing.event.ListDataEvent e) {
        updateUrlFromFields();
        fireChange();
    }

    public void intervalAdded(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    public void intervalRemoved(javax.swing.event.ListDataEvent e) {
        fireChange();
    }

    private void fireChange() {
        firePropertyChange("argumentChanged", null, null);
        resetProgress();
    }
    
    private void updateUrlFromFields() {
        JdbcUrl url = getSelectedJdbcUrl();
        if (url == null || !url.urlIsParsed()) {
            return;
        }
        
        // If the fields are being modified because the user is manually
        // changing the URL, don't circle back and update the URL again.
        if (! updatingUrl) {
            updatingFields = true;

            url.put(JdbcUrl.TOKEN_HOST, hostField.getText());
            url.put(JdbcUrl.TOKEN_PORT, portField.getText());
            url.put(JdbcUrl.TOKEN_DB, databaseField.getText());
            url.put(JdbcUrl.TOKEN_ADDITIONAL, additionalPropsField.getText());
            url.put(JdbcUrl.TOKEN_SERVERNAME, serverNameField.getName());

            urlField.setText(url.getUrl());
            
            updatingFields = false;
        }
        
        checkValid();        
    }
    
    private void checkValid() {
        JdbcUrl url = getSelectedJdbcUrl();
        if (url == null) {                        
            displayError(getMessage("NewConnection.MSG_SelectADriver")); 
        } else if (url != null && url.urlIsParsed()) {
            if (url.requiresToken(JdbcUrl.TOKEN_DB) && isEmpty(databaseField.getText())) {
                displayError(getMessage("NewConnection.ERR_FieldRequired", url.getDatabaseLabel()));
                databaseField.requestFocusInWindow();
            } else if (url.requiresToken(JdbcUrl.TOKEN_HOST) && isEmpty(hostField.getText())) {
                displayError(getMessage("NewConnection.ERR_FieldRequired", hostLabel.getText()));
                hostField.requestFocusInWindow();
            } else if (url.requiresToken(JdbcUrl.TOKEN_PORT) && isEmpty(portField.getText())) {
                displayError(getMessage("NewConnection.ERR_FieldRequired", portLabel.getText()));
                portField.requestFocusInWindow();
            } else if (url.requiresToken(JdbcUrl.TOKEN_SERVERNAME) && isEmpty(serverNameField.getText())) {
                displayError(getMessage("NewConnection.ERR_FieldRequired", serverNameLabel.getText()));
                serverNameField.requestFocusInWindow();
            } else {
                clearError();
            }
        } else if (isEmpty(urlField.getText())) {
            displayError(getMessage("NewConnection.MSG_SpecifyURL"));
        } else {
            clearError();
        }
    }
    
    private boolean isEmpty(String str) {
        return str == null || str.equals("");
    }
    
    private void updateFieldsFromUrl() {
        JdbcUrl url = getSelectedJdbcUrl();
        if (url == null) {
            return;
        }

        // If this is called because the URL is being changed due to
        // changes in the fields, then don't circle back and update
        // the fields again.
        if (updatingFields) {
            return;
        }
        
        try {
            url.setUrl(urlField.getText());
            clearError();
        } catch ( MalformedURLException e ) {
            LOGGER.log(Level.FINE, null, e);
            displayError(e.getMessage());
            return;
        }

        if (url.urlIsParsed()) {
            // Setting this flag prevents the docment listener for the fields
            // from trying to update the URL, thus causing a circular even loop.
            updatingUrl = true;
            
            hostField.setText(url.get(JdbcUrl.TOKEN_HOST));
            portField.setText(url.get(JdbcUrl.TOKEN_PORT));
            databaseField.setText(url.get(JdbcUrl.TOKEN_DB));
            serverNameField.setText(url.get(JdbcUrl.TOKEN_SERVERNAME));
            additionalPropsField.setText(url.get(JdbcUrl.TOKEN_ADDITIONAL));
        
            updatingUrl = false;
        }
    }
    
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        mediator.setValid(true);
        resize();
    }
    
    private void displayError(String message) {
        errorLabel.setText(message);        
        errorLabel.setVisible(true);
        mediator.setValid(false);
        resize();
    }    
}
