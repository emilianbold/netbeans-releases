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
import java.awt.Color;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.util.DatabaseExplorerInternalUIs;
import org.netbeans.modules.db.util.JdbcUrl;
import org.openide.util.NbBundle;

public class NewConnectionPanel extends ConnectionDialog.FocusablePanel {

    private ConnectionDialogMediator mediator;
    // private Vector templates;
    private DatabaseConnection connection;
    private ProgressHandle progressHandle;
    private Window window;

    private boolean updatingUrl = false;
    private boolean updatingFields = false;

    // keeps track of the user's last selection of whether or not to
    // show the jdbc url.  
    private boolean userSpecifiedShowUrl = false;
    
    private final LinkedHashMap<String, UrlField> urlFields =
            new LinkedHashMap<String, UrlField>();

    private static final String BUNDLE = "org.netbeans.modules.db.resources.Bundle"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(NewConnectionPanel.class.getName());

    private static ResourceBundle bundle() {
        return NbBundle.getBundle(BUNDLE);
    }

    private static String getMessage(String key, Object ... args) {
        return MessageFormat.format(bundle().getString(key), args);
    }

    private void initFieldMap() {
        // These should be in the order of display on the form, so that we correctly
        // put focus on the first visible field.
        urlFields.put(JdbcUrl.TOKEN_HOST, new UrlField(hostField, hostLabel));
        urlFields.put(JdbcUrl.TOKEN_PORT, new UrlField(portField, portLabel));
        urlFields.put(JdbcUrl.TOKEN_DB, new UrlField(databaseField, databaseLabel));
        urlFields.put(JdbcUrl.TOKEN_SID, new UrlField(sidField, sidLabel));
        urlFields.put(JdbcUrl.TOKEN_SERVICENAME, new UrlField(serviceField, serviceLabel));
        urlFields.put(JdbcUrl.TOKEN_TNSNAME, new UrlField(tnsField, tnsLabel));
        urlFields.put(JdbcUrl.TOKEN_DSN, new UrlField(dsnField, dsnLabel));
        urlFields.put(JdbcUrl.TOKEN_SERVERNAME, new UrlField(serverNameField, serverNameLabel));
        urlFields.put(JdbcUrl.TOKEN_INSTANCE, new UrlField(instanceField, instanceLabel));
        urlFields.put(JdbcUrl.TOKEN_ADDITIONAL, new UrlField(additionalPropsField, additionalPropsLabel));
    }

    public NewConnectionPanel(ConnectionDialogMediator mediator, String driverClass, DatabaseConnection connection) {
        this.mediator = mediator;
        this.connection = connection;
        initComponents();
        initAccessibility();
        initFieldMap();

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

        for (Entry<String,UrlField> entry : urlFields.entrySet()) {
            new InputAdapter(entry.getValue().getField());
        }

        new InputAdapter(templateComboBox);

        urlField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                updateFieldsFromUrl();
            }

        });

        // Set up initial defaults; user may change but that's ok
        urlField.setVisible(false);
        showUrlCheckBox.setSelected(false);


        setUrlField();
        updateFieldsFromUrl();
        setUpFields();
        
        DocumentListener docListener = new DocumentListener()
        {
            public void insertUpdate(DocumentEvent evt) 
            {
                fireChange();
            }

            public void removeUpdate(DocumentEvent evt) 
            {
                fireChange();
            }

            public void changedUpdate(DocumentEvent evt) 
            {
                fireChange();
            }
        };
        
        userField.getDocument().addDocumentListener(docListener);
        passwordField.getDocument().addDocumentListener(docListener);
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
        sidField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionSIDTextFieldA11yName")); //NOI18N
        sidLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionSIDA11yDesc")); //NOI18N
        serviceField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionServiceNameTextFieldA11yName")); //NOI18N
        serviceLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionServiceNameA11yDesc")); //NOI18N
        tnsField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionTNSNameTextFieldA11yName")); //NOI18N
        tnsLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionTNSNameA11yDesc")); //NOI18N
        dsnField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionDSNTextFieldA11yName")); //NOI18N
        dsnLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionDSNA11yDesc")); //NOI18N
        instanceField.getAccessibleContext().setAccessibleName(b.getString("ACS_NewConnectionInstanceNameTextFieldA11yName")); //NOI18N
        instanceLabel.getAccessibleContext().setAccessibleDescription(b.getString("ACS_NewConnectionInstanceNameA11yDesc")); //NOI18N
  }

    public void initializeFocus() {
        setFocus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showUrlCheckBox = new javax.swing.JCheckBox();
        templateComboBox = new javax.swing.JComboBox();
        hostField = new javax.swing.JTextField();
        templateLabel = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        databaseLabel = new javax.swing.JLabel();
        databaseField = new javax.swing.JTextField();
        sidLabel = new javax.swing.JLabel();
        sidField = new javax.swing.JTextField();
        serviceLabel = new javax.swing.JLabel();
        serviceField = new javax.swing.JTextField();
        tnsLabel = new javax.swing.JLabel();
        tnsField = new javax.swing.JTextField();
        serverNameLabel = new javax.swing.JLabel();
        serverNameField = new javax.swing.JTextField();
        instanceLabel = new javax.swing.JLabel();
        instanceField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        dsnLabel = new javax.swing.JLabel();
        dsnField = new javax.swing.JTextField();
        additionalPropsLabel = new javax.swing.JLabel();
        additionalPropsField = new javax.swing.JTextField();
        urlField = new javax.swing.JTextField();
        passwordCheckBox = new javax.swing.JCheckBox();
        errorInfoPanel = new org.netbeans.modules.db.util.ErrorInfoPanel();

        FormListener formListener = new FormListener();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/db/resources/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(showUrlCheckBox, bundle.getString("NewConnectionShowJDBCURL")); // NOI18N
        showUrlCheckBox.setToolTipText("Check this to show or hide the JDBC URL");
        showUrlCheckBox.setMargin(new java.awt.Insets(3, 0, 1, 1));
        showUrlCheckBox.addActionListener(formListener);

        templateComboBox.setToolTipText(bundle.getString("ACS_NewConnectionDriverClassComboBoxA11yDesc")); // NOI18N
        templateComboBox.addItemListener(formListener);
        templateComboBox.addActionListener(formListener);

        hostField.setToolTipText(bundle.getString("ACS_NewConnectionHostA11yDesc")); // NOI18N

        templateLabel.setLabelFor(templateComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(templateLabel, bundle.getString("NewConnectionDriverName")); // NOI18N

        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, bundle.getString("NewConnectionHost")); // NOI18N

        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, bundle.getString("NewConnectionPort")); // NOI18N

        portField.setToolTipText(bundle.getString("ACS_NewConnectionPortA11yDesc")); // NOI18N

        databaseLabel.setLabelFor(databaseField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLabel, bundle.getString("NewConnectionDatabase")); // NOI18N

        databaseField.setToolTipText(bundle.getString("ACS_NewConnectionDatabaseNameA11yDesc")); // NOI18N

        sidLabel.setLabelFor(sidField);
        org.openide.awt.Mnemonics.setLocalizedText(sidLabel, bundle.getString("NewConnectionSID")); // NOI18N

        sidField.setToolTipText(bundle.getString("ACS_NewConnectionSIDA11yDesc")); // NOI18N

        serviceLabel.setLabelFor(serviceField);
        org.openide.awt.Mnemonics.setLocalizedText(serviceLabel, bundle.getString("NewConnectionServiceName")); // NOI18N

        serviceField.setToolTipText(bundle.getString("ACS_NewConnectionServiceNameA11yDesc")); // NOI18N

        tnsLabel.setLabelFor(tnsField);
        org.openide.awt.Mnemonics.setLocalizedText(tnsLabel, bundle.getString("NewConnectionTNSName")); // NOI18N

        tnsField.setToolTipText(bundle.getString("ACS_NewConnectionTNSNameA11yDesc")); // NOI18N

        serverNameLabel.setLabelFor(serverNameField);
        org.openide.awt.Mnemonics.setLocalizedText(serverNameLabel, bundle.getString("NewConnectionServerName")); // NOI18N

        serverNameField.setToolTipText(bundle.getString("ACS_NewConnectionServerNameA11yDesc")); // NOI18N

        instanceLabel.setLabelFor(instanceField);
        org.openide.awt.Mnemonics.setLocalizedText(instanceLabel, bundle.getString("NewConnectionInstanceName")); // NOI18N

        instanceField.setToolTipText(bundle.getString("ACS_NewConnectionInstanceNameA11yDesc")); // NOI18N

        userLabel.setLabelFor(userField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, bundle.getString("NewConnectionUserName")); // NOI18N

        userField.setToolTipText(bundle.getString("ACS_NewConnectionUserNameA11yDesc")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, bundle.getString("NewConnectionPassword")); // NOI18N

        passwordField.setToolTipText(bundle.getString("ACS_NewConnectionPasswordA11yDesc")); // NOI18N

        dsnLabel.setLabelFor(dsnField);
        org.openide.awt.Mnemonics.setLocalizedText(dsnLabel, bundle.getString("NewConnectionDSN")); // NOI18N

        dsnField.setToolTipText(bundle.getString("ACS_NewConnectionDSNA11yDesc")); // NOI18N

        additionalPropsLabel.setLabelFor(additionalPropsField);
        org.openide.awt.Mnemonics.setLocalizedText(additionalPropsLabel, bundle.getString("NewConnectionAdditionalProperties")); // NOI18N

        additionalPropsField.setToolTipText(bundle.getString("ACS_NewConnectionAdditionalPropertiesA11yDesc")); // NOI18N

        urlField.setToolTipText(bundle.getString("ACS_NewConnectionJDBCURLA11yDesc")); // NOI18N
        urlField.addActionListener(formListener);
        urlField.addFocusListener(formListener);
        urlField.addKeyListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(passwordCheckBox, bundle.getString("NewConnectionRememberPassword")); // NOI18N
        passwordCheckBox.setToolTipText(bundle.getString("ACS_NewConnectionRememberPasswordA11yDesc")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(errorInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(additionalPropsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, templateLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, hostLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, portLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, databaseLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, passwordLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, userLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, instanceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, serverNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, dsnLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, tnsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, serviceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, sidLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                            .add(showUrlCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 125, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(urlField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(additionalPropsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(templateComboBox, 0, 519, Short.MAX_VALUE)
                            .add(hostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(portField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(databaseField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(sidField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(serviceField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(tnsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(dsnField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(serverNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(instanceField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(userField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, passwordCheckBox))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(templateLabel)
                    .add(templateComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                    .add(sidLabel)
                    .add(sidField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serviceLabel)
                    .add(serviceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tnsLabel)
                    .add(tnsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dsnLabel)
                    .add(dsnField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverNameLabel)
                    .add(serverNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(instanceLabel)
                    .add(instanceField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(userLabel)
                    .add(userField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(passwordLabel)
                    .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(passwordCheckBox)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(additionalPropsLabel)
                    .add(additionalPropsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(32, 32, 32)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(showUrlCheckBox))
                .add(18, 18, 18)
                .add(errorInfoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener, java.awt.event.ItemListener, java.awt.event.KeyListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == showUrlCheckBox) {
                NewConnectionPanel.this.showUrlCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == templateComboBox) {
                NewConnectionPanel.this.templateComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == urlField) {
                NewConnectionPanel.this.urlFieldActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == urlField) {
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

    private void urlFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlFieldActionPerformed
        
    }//GEN-LAST:event_urlFieldActionPerformed

    private void urlFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_urlFieldFocusLost
        
    }//GEN-LAST:event_urlFieldFocusLost

    private void urlFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urlFieldKeyPressed
        
    }//GEN-LAST:event_urlFieldKeyPressed

    private void templateComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboBoxActionPerformed

    }//GEN-LAST:event_templateComboBoxActionPerformed

    private void templateComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_templateComboBoxItemStateChanged
        if (evt.getStateChange() == evt.SELECTED)
        {
            setUpFields();
            updateUrlFromFields();
            fireChange();
        }
    }//GEN-LAST:event_templateComboBoxItemStateChanged

private void showUrlCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
    showUrl();
}

private void showUrl() {
    userSpecifiedShowUrl = showUrlCheckBox.isSelected();
    
    if (showUrlCheckBox.isSelected()) {
        updateUrlFromFields();
    }
    urlField.setVisible(showUrlCheckBox.isSelected());

    resize();
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField additionalPropsField;
    private javax.swing.JLabel additionalPropsLabel;
    private javax.swing.JTextField databaseField;
    private javax.swing.JLabel databaseLabel;
    private javax.swing.JTextField dsnField;
    private javax.swing.JLabel dsnLabel;
    private org.netbeans.modules.db.util.ErrorInfoPanel errorInfoPanel;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JTextField instanceField;
    private javax.swing.JLabel instanceLabel;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField serverNameField;
    private javax.swing.JLabel serverNameLabel;
    private javax.swing.JTextField serviceField;
    private javax.swing.JLabel serviceLabel;
    private javax.swing.JCheckBox showUrlCheckBox;
    private javax.swing.JTextField sidField;
    private javax.swing.JLabel sidLabel;
    private javax.swing.JComboBox templateComboBox;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JTextField tnsField;
    private javax.swing.JLabel tnsLabel;
    private javax.swing.JTextField urlField;
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
            for (Entry<String, UrlField> entry : urlFields.entrySet()) {
                entry.getValue().getField().setVisible(false);
                entry.getValue().getLabel().setVisible(false);
            }

            urlField.setVisible(false);

            checkValid();
            resize();
            return;
        }

        userField.setVisible(true);
        userLabel.setVisible(true);

        passwordField.setVisible(true);
        passwordLabel.setVisible(true);

        passwordCheckBox.setVisible(true);

        for (Entry<String,UrlField> entry : urlFields.entrySet()) {
            entry.getValue().getField().setVisible(jdbcurl.supportsToken(entry.getKey()));
            entry.getValue().getLabel().setVisible(jdbcurl.supportsToken(entry.getKey()));
        }

        if (! jdbcurl.isParseUrl()) {
            showUrlCheckBox.setEnabled(false);
            showUrlCheckBox.setSelected(true);
            urlField.setVisible(true);
            setUrlField();
        } else {
            showUrlCheckBox.setEnabled(true);
            showUrlCheckBox.setSelected(userSpecifiedShowUrl);
            showUrl();
        }

        setFocus();
        checkValid();
        resize();
    }

    private void setFocus() {
        if (templateComboBox.getItemCount() <= 1) { // the first item is "Add Driver...""
            templateComboBox.requestFocusInWindow();
            return;
        }

        for (Entry<String,UrlField> entry : urlFields.entrySet()) {
            if (entry.getValue().getField().isVisible()) {
                entry.getValue().getField().requestFocusInWindow();
                return;
            }
        }

        userField.requestFocusInWindow();
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

        if (jdbcurl.isParseUrl()) {
            updateUrlFromFields();
        } else {
            urlField.setText(jdbcurl.getUrlTemplate());
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
        return NbBundle.getBundle(BUNDLE).getString("NewConnectionDialogTitle"); //NOI18N
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

    /**
     * Terminates the use of the progress bar.
     */
    public void terminateProgress()
    {
        stopProgress(false);
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

    private void fireChange() {

        // the user has changed some parameter, so if there's a connection it's
        // no longer in sync with the field data
        mediator.closeConnection();
        
        firePropertyChange("argumentChanged", null, null);
        resetProgress();
    }

    private void updateUrlFromFields() {
        
        JdbcUrl url = getSelectedJdbcUrl();
        if (url == null || !url.isParseUrl()) {
            return;
        }

        // If the fields are being modified because the user is manually
        // changing the URL, don't circle back and update the URL again.
        if (! updatingUrl) {
            updatingFields = true;

            for (Entry<String,UrlField> entry : urlFields.entrySet()) {
                url.put(entry.getKey(), entry.getValue().getField().getText());
            }

            urlField.setText(url.getUrl());

            updatingFields = false;
        }

        checkValid();
    }

    private void checkValid() {
        JdbcUrl url = getSelectedJdbcUrl();

        boolean requiredFieldMissing = false;
        if (url == null) {
            displayError(getMessage("NewConnection.MSG_SelectADriver"), false);
        } else if (url != null && url.isParseUrl()) {
            for (Entry<String,UrlField> entry : urlFields.entrySet()) {
                if (url.requiresToken(entry.getKey()) && isEmpty(entry.getValue().getField().getText())) {
                    requiredFieldMissing = true;
                    displayError(getMessage("NewConnection.ERR_FieldRequired",
                            entry.getValue().getLabel().getText()), false);
                }
            }

            if (! requiredFieldMissing) {
                clearError();
            }
        } else if (isEmpty(urlField.getText())) {
            displayError(getMessage("NewConnection.MSG_SpecifyURL"), false);
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
            displayError(e.getMessage(), true);
            return;
        }

        if (url.isParseUrl()) {
            // Setting this flag prevents the docment listener for the fields
            // from trying to update the URL, thus causing a circular even loop.
            updatingUrl = true;

            for (Entry<String,UrlField> entry : urlFields.entrySet()) {
                entry.getValue().getField().setText(url.get(entry.getKey()));
            }

            updatingUrl = false;
        }
    }

    private void clearError() {
        errorInfoPanel.clear();
        mediator.setValid(true);
    }

    private void displayError(String message, boolean isError) {
        errorInfoPanel.set(message, isError);
        mediator.setValid(false);
    }

    private class UrlField {
        private final JTextField field;
        private final JLabel label;

        public UrlField(JTextField field, JLabel label) {
            this.field = field;
            this.label = label;
        }

        public JTextField getField() {
            return field;
        }

        public JLabel getLabel() {
            return label;
        }

    }
    
    /**
     * This class is used to track user input for an associated input field.
     */
    private class InputAdapter implements DocumentListener, ListDataListener
    {
        public InputAdapter(JTextField source)
        {
            source.getDocument().addDocumentListener(this);
        }

        public InputAdapter(JComboBox source)
        {
            source.getModel().addListDataListener(this);
        }
        
        public void insertUpdate(DocumentEvent evt) 
        {
            updateUrlFromFields();
            fireChange();
        }

        public void removeUpdate(DocumentEvent evt) 
        {
            updateUrlFromFields();
            fireChange();
        }

        public void changedUpdate(DocumentEvent evt) 
        {
            updateUrlFromFields();
            fireChange();
        }

        public void intervalAdded(ListDataEvent evt) 
        {
            fireChange();
        }

        public void intervalRemoved(ListDataEvent evt) 
        {
            fireChange();
        }

        public void contentsChanged(ListDataEvent evt) 
        {
            updateUrlFromFields();
            fireChange();
        }
        
    }
            
}
