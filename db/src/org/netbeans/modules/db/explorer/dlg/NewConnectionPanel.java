/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
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
import org.netbeans.modules.db.explorer.ConnectionList;
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

    private boolean fieldEntryMode = true;
    
    // keeps track of the user's last selection of whether or not to
    // show the jdbc url.  
    private boolean userSpecifiedShowUrl = false;
    
    private final LinkedHashMap<String, UrlField> urlFields =
            new LinkedHashMap<String, UrlField>();

    private Set<String> knownConnectionNames = new HashSet<String>();

    private static final Logger LOGGER = Logger.getLogger(NewConnectionPanel.class.getName());

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
        urlFields.put(JdbcUrl.TOKEN_DISPLAY_NAME, new UrlField(displayNameField, displayNameLabel));
        urlFields.put(JdbcUrl.TOKEN_ADDITIONAL, new UrlField(additionalPropsField, additionalPropsLabel));
    }

    public NewConnectionPanel(ConnectionDialogMediator mediator, String driverClass, DatabaseConnection connection) {
        this.mediator = mediator;
        this.connection = connection;
        initComponents();
        initAccessibility();
        initFieldMap();

        // sets up colors and icons
        errorInfoPanel.setup();

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

        if (!connection.getDisplayName().equals(connection.getName())) {
            displayNameField.setText(connection.getDisplayName());
        }

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

        new InputAdapter(directUrlField);
        
        urlField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                updateFieldsFromUrl();
            }

        });
        urlField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                if (urlField.hasFocus()) {
                    updateFieldsFromUrl();
                }
            }

            public void removeUpdate(DocumentEvent e) {
                if (urlField.hasFocus()) {
                    updateFieldsFromUrl();
                }
            }

            public void changedUpdate(DocumentEvent e) {
                if (urlField.hasFocus()) {
                    updateFieldsFromUrl();
                }
            }
        });

        // Set up initial defaults; user may change but that's ok
        urlField.setVisible(false);
        showUrlCheckBox.setSelected(false);

        fieldEntryMode = true;
        fieldInputCheckBox.setSelected(true);

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

        for (DatabaseConnection existingConnection : ConnectionList.getDefault().getConnections()) {
            knownConnectionNames.add(existingConnection.getDisplayName());
        }
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    private void initAccessibility() {
        templateLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDriverNameA11yDesc")); //NOI18N
        templateComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDriverNameComboBoxA11yName")); //NOI18N
        displayNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDisplayNameA11yDesc")); //NOI18N
        displayNameField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDisplayNameTextFieldA11yName")); //NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionUserNameA11yDesc")); //NOI18N
        userField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPasswordTextFieldA11yName")); //NOI18N
        hostLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionHostA11yDesc")); //NOI18N
        hostField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionHostTextFieldA11yName")); //NOI18N
        portLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPortA11yDesc")); //NOI18N
        portField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPortTextFieldA11yName")); //NOI18N
        serverNameField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServerNameTextFieldA11yName")); //NOI18N
        serverNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServerNameA11yDesc")); //NOI18N
        databaseField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDatabaseNameTextFieldA11yName")); //NOI18N
        databaseLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDatabaseNameA11yDesc")); //NOI18N
        additionalPropsField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionAdditionalPropertiesTextFieldA11yName")); //NOI18N
        additionalPropsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionAdditionalPropertiesA11yDesc")); //NOI18N
        urlField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionJDBCURLTextFieldA11yName")); //NOI18N
        sidField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionSIDTextFieldA11yName")); //NOI18N
        sidLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionSIDA11yDesc")); //NOI18N
        serviceField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServiceNameTextFieldA11yName")); //NOI18N
        serviceLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServiceNameA11yDesc")); //NOI18N
        tnsField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionTNSNameTextFieldA11yName")); //NOI18N
        tnsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionTNSNameA11yDesc")); //NOI18N
        dsnField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDSNTextFieldA11yName")); //NOI18N
        dsnLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDSNA11yDesc")); //NOI18N
        instanceField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionInstanceNameTextFieldA11yName")); //NOI18N
        instanceLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionInstanceNameA11yDesc")); //NOI18N
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

        inputModeButtonGroup = new javax.swing.ButtonGroup();
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
        displayNameLabel = new javax.swing.JLabel();
        displayNameField = new javax.swing.JTextField();
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
        inputModelLabel = new javax.swing.JLabel();
        fieldInputCheckBox = new javax.swing.JRadioButton();
        directInputCheckBox = new javax.swing.JRadioButton();
        directUrlLabel = new javax.swing.JLabel();
        directUrlScroll = new javax.swing.JScrollPane();
        directUrlField = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();

        FormListener formListener = new FormListener();

        org.openide.awt.Mnemonics.setLocalizedText(showUrlCheckBox, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionShowJDBCURL")); // NOI18N
        showUrlCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionShowJDBCURLAllyDesc")); // NOI18N
        showUrlCheckBox.setMargin(new java.awt.Insets(3, 0, 1, 1));
        showUrlCheckBox.addActionListener(formListener);

        templateComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDriverClassComboBoxA11yDesc")); // NOI18N
        templateComboBox.addItemListener(formListener);
        templateComboBox.addActionListener(formListener);

        hostField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionHostA11yDesc")); // NOI18N

        templateLabel.setLabelFor(templateComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(templateLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDriverName")); // NOI18N

        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionHost")); // NOI18N

        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPort")); // NOI18N

        portField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPortA11yDesc")); // NOI18N

        databaseLabel.setLabelFor(databaseField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDatabase")); // NOI18N

        databaseField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDatabaseNameA11yDesc")); // NOI18N

        sidLabel.setLabelFor(sidField);
        org.openide.awt.Mnemonics.setLocalizedText(sidLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionSID")); // NOI18N

        sidField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionSIDA11yDesc")); // NOI18N

        serviceLabel.setLabelFor(serviceField);
        org.openide.awt.Mnemonics.setLocalizedText(serviceLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionServiceName")); // NOI18N

        serviceField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServiceNameA11yDesc")); // NOI18N

        tnsLabel.setLabelFor(tnsField);
        org.openide.awt.Mnemonics.setLocalizedText(tnsLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionTNSName")); // NOI18N

        tnsField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionTNSNameA11yDesc")); // NOI18N

        serverNameLabel.setLabelFor(serverNameField);
        org.openide.awt.Mnemonics.setLocalizedText(serverNameLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionServerName")); // NOI18N

        serverNameField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServerNameA11yDesc")); // NOI18N

        instanceLabel.setLabelFor(instanceField);
        org.openide.awt.Mnemonics.setLocalizedText(instanceLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionInstanceName")); // NOI18N

        instanceField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionInstanceNameA11yDesc")); // NOI18N

        displayNameLabel.setLabelFor(displayNameField);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDisplayName")); // NOI18N

        displayNameField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDisplayNameA11yDesc")); // NOI18N

        userLabel.setLabelFor(userField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionUserName")); // NOI18N

        userField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionUserNameA11yDesc")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPassword")); // NOI18N

        passwordField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPasswordA11yDesc")); // NOI18N

        dsnLabel.setLabelFor(dsnField);
        org.openide.awt.Mnemonics.setLocalizedText(dsnLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDSN")); // NOI18N

        dsnField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDSNA11yDesc")); // NOI18N

        additionalPropsLabel.setLabelFor(additionalPropsField);
        org.openide.awt.Mnemonics.setLocalizedText(additionalPropsLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionAdditionalProperties")); // NOI18N

        additionalPropsField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionAdditionalPropertiesA11yDesc")); // NOI18N

        urlField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionJDBCURLA11yDesc")); // NOI18N
        urlField.addActionListener(formListener);
        urlField.addFocusListener(formListener);
        urlField.addKeyListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(passwordCheckBox, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionRememberPassword")); // NOI18N
        passwordCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionRememberPasswordA11yDesc")); // NOI18N
        passwordCheckBox.setMargin(new java.awt.Insets(3, 0, 1, 1));

        inputModelLabel.setLabelFor(fieldInputCheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(inputModelLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewCOnnectionInputMode")); // NOI18N

        inputModeButtonGroup.add(fieldInputCheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(fieldInputCheckBox, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionFieldEntryMode")); // NOI18N
        fieldInputCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionFieldEntryModeA11yDesc")); // NOI18N
        fieldInputCheckBox.addActionListener(formListener);

        inputModeButtonGroup.add(directInputCheckBox);
        org.openide.awt.Mnemonics.setLocalizedText(directInputCheckBox, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDirectUrlEntryMode")); // NOI18N
        directInputCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDirectUrlEntryModeA11yDesc")); // NOI18N
        directInputCheckBox.addActionListener(formListener);

        directUrlLabel.setLabelFor(directUrlField);
        org.openide.awt.Mnemonics.setLocalizedText(directUrlLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDirectURL")); // NOI18N

        directUrlField.setColumns(20);
        directUrlField.setLineWrap(true);
        directUrlField.setRows(5);
        directUrlField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionJDBCURLA11yDesc")); // NOI18N
        directUrlScroll.setViewportView(directUrlField);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 686, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 19, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(inputModelLabel)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, displayNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, showUrlCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, directUrlLabel)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, templateLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, passwordLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, userLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, instanceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, serverNameLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, dsnLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, tnsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, serviceLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, sidLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, databaseLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, portLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, hostLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 121, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(additionalPropsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(fieldInputCheckBox)
                                .add(18, 18, 18)
                                .add(directInputCheckBox))
                            .add(passwordCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 256, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(templateComboBox, 0, 550, Short.MAX_VALUE)
                            .add(hostField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(portField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(databaseField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(sidField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(serviceField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(tnsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(dsnField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(serverNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(instanceField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(userField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(passwordField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(displayNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(additionalPropsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(urlField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
                            .add(directUrlScroll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE))
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, errorInfoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 686, Short.MAX_VALUE)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inputModelLabel)
                    .add(fieldInputCheckBox)
                    .add(directInputCheckBox))
                .add(13, 13, 13)
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(displayNameLabel)
                    .add(displayNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(passwordCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(additionalPropsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(additionalPropsLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(showUrlCheckBox)
                    .add(urlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(directUrlLabel)
                    .add(directUrlScroll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(errorInfoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(138, Short.MAX_VALUE))
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
            else if (evt.getSource() == fieldInputCheckBox) {
                NewConnectionPanel.this.fieldInputCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == directInputCheckBox) {
                NewConnectionPanel.this.directInputCheckBoxActionPerformed(evt);
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
        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
            Object item = templateComboBox.getSelectedItem();
            if ( item != null && !(item instanceof JdbcUrl)) {
                // This is an item indicating "Create a New Driver", and if
                // we futz with the fields, then the ComboBox wants to make the
                // drop-down invisible and the dialog never gets a chance to
                // get invoked.
                return;
            }

            JdbcUrl jdbcurl = (JdbcUrl)item;

            // Field entry mode doesn't make sense if this URL isn't parsed.  change the mode
            // now if appropriate
            if (! jdbcurl.isParseUrl()) {
                fieldInputCheckBox.setVisible(false);
                inputModelLabel.setVisible(false);
                directInputCheckBox.setVisible(false);
                if (fieldEntryMode) {
                    directInputCheckBox.setSelected(true);
                    updateInputMode(false);
                }
            } else {
                fieldInputCheckBox.setVisible(true);
                inputModelLabel.setVisible(true);
                directInputCheckBox.setVisible(true);
                directUrlField.setText("");
                setUpFields();
            }

            updateUrlFromFields();
            fireChange();
        }
    }//GEN-LAST:event_templateComboBoxItemStateChanged

    private void fieldInputCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldInputCheckBoxActionPerformed
        updateInputMode(false);
    }//GEN-LAST:event_fieldInputCheckBoxActionPerformed

    private void directInputCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directInputCheckBoxActionPerformed
        updateInputMode(true);
    }//GEN-LAST:event_directInputCheckBoxActionPerformed

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
    private javax.swing.JRadioButton directInputCheckBox;
    private javax.swing.JTextArea directUrlField;
    private javax.swing.JLabel directUrlLabel;
    private javax.swing.JScrollPane directUrlScroll;
    private javax.swing.JTextField displayNameField;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField dsnField;
    private javax.swing.JLabel dsnLabel;
    private org.netbeans.modules.db.util.ErrorInfoPanel errorInfoPanel;
    private javax.swing.JRadioButton fieldInputCheckBox;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.ButtonGroup inputModeButtonGroup;
    private javax.swing.JLabel inputModelLabel;
    private javax.swing.JTextField instanceField;
    private javax.swing.JLabel instanceLabel;
    private javax.swing.JPanel jPanel1;
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
        
        if (fieldEntryMode) {
            connection.setDatabase(urlField.getText());
        } else {
            connection.setDatabase(directUrlField.getText());
        }

        connection.setUser(userField.getText());
        connection.setPassword(getPassword());
        connection.setRememberPassword(passwordCheckBox.isSelected());
        connection.setDisplayName(displayNameField.getText());
    }

    private void resize() {
        revalidate();
        if (window != null) {
            window.pack();
        }
    }

    private void updateInputMode(boolean copyUrl) {
        fieldEntryMode = fieldInputCheckBox.isSelected();
        
        if (copyUrl) {
            // copy the url to the direct entry url field
            directUrlField.setText(urlField.getText());
        }
        
        setUpFields();
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

        if (fieldEntryMode) {
            directUrlLabel.setVisible(false);
            directUrlScroll.setVisible(false);
            
            showUrlCheckBox.setVisible(true);
            urlField.setVisible(showUrlCheckBox.isSelected());
            
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
        }
        else {
            directUrlLabel.setVisible(true);
            directUrlScroll.setVisible(true);
            
            showUrlCheckBox.setVisible(false);
            urlField.setVisible(false);

            for (Entry<String,UrlField> entry : urlFields.entrySet()) {
                entry.getValue().getField().setVisible(false);
                entry.getValue().getLabel().setVisible(false);
            }
        }
        displayNameField.setVisible(true);
        displayNameLabel.setVisible(true);
        
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
        return NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDialogTitle"); //NOI18N
    }

    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(NewConnectionPanel.class, "ConnectionProgress_Connecting"));
                progressHandle.start();
                enableInput(false);
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
                    enableInput(true);
                }
            }
        });
    }

    private void enableInput(boolean enable) {
        displayNameField.setEnabled(enable);
        fieldInputCheckBox.setEnabled(enable);
        directInputCheckBox.setEnabled(enable);
        templateComboBox.setEnabled(enable);
        userField.setEnabled(enable);
        passwordField.setEnabled(enable);
        passwordCheckBox.setEnabled(enable);
        showUrlCheckBox.setEnabled(enable);
        urlField.setEnabled(enable);
        directUrlField.setEnabled(enable);
        
        for (Entry<String,UrlField> entry : urlFields.entrySet()) {
            entry.getValue().getField().setEnabled(enable);
        }
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
        if (fieldEntryMode) {
            JdbcUrl url = getSelectedJdbcUrl();

            boolean requiredFieldMissing = false;
            if (url == null) {
                displayError(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.MSG_SelectADriver"), false);
            } else if (url != null && url.isParseUrl()) {
                for (Entry<String,UrlField> entry : urlFields.entrySet()) {
                    if (url.requiresToken(entry.getKey()) && isEmpty(entry.getValue().getField().getText())) {
                        requiredFieldMissing = true;
                        displayError(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.ERR_FieldRequired",
                                entry.getValue().getLabel().getText()), false);
                    }
                }

                if (! requiredFieldMissing) {
                    clearError();
                }
            } else if (isEmpty(urlField.getText())) {
                displayError(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.MSG_SpecifyURL"), false);
            } else {
                clearError();
            }
        } else {
            if (this.directUrlField.getText().trim().length() > 0) {
                clearError();
            } else {
                displayError(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.MSG_SpecifyURL"), false);
            }
        }
        if (knownConnectionNames.contains(displayNameField.getText().trim())) {
            displayError(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.MSG_DuplicateDisplayName"), false);
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
            // just log in but don't report it to users
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
        
        public InputAdapter(JTextArea source)
        {
            source.getDocument().addDocumentListener(this);
        }
        
        public void insertUpdate(DocumentEvent evt) 
        {
            updateUrlFromFields();
            checkValid();
            fireChange();
        }

        public void removeUpdate(DocumentEvent evt) 
        {
            updateUrlFromFields();
            checkValid();
            fireChange();
        }

        public void changedUpdate(DocumentEvent evt) 
        {
            updateUrlFromFields();
            checkValid();
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
            checkValid();
            fireChange();
        }
        
    }
            
}
