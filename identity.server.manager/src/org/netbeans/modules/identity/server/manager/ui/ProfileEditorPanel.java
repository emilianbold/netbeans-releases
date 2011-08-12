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

import java.awt.Dialog;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.netbeans.modules.identity.profile.api.configurator.ConfiguratorException;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Configurable;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator;
import org.netbeans.modules.identity.profile.api.configurator.Configurator.AccessMethod;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Type;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
import org.netbeans.modules.identity.server.manager.api.ServerInstance;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Visual panel for editor the security mechanism profiles.
 *
 * Created on April 14, 2006, 3:03 PM
 *
 * @author  ptliu
 */
public class ProfileEditorPanel extends JPanel 
        implements ChangeListener, EditDialogDescriptor.Panel {
    private static final long WAIT_TIME = 400;
    
    private static final String JKS_EXTENSION = ".jks"; //NOI18N
    
    private static final String HELP_ID = "idmtools_am_config_am_sec_mech"; //NOI18N
    
    private static final String AM_INVALID_KEYSTORE_ERR = "Invalid KeyStore";   //NOI18N
    
    private enum State {
        INITIALIZING,
        INITIALIZED,
        NOT_RUNNING,
        INIT_FAILED
    }
    
    private transient ProviderConfigurator configurator;
    private transient SecurityMechanism secMech;
    private ServerInstance instance;
    private Collection<ChangeListener> listeners;
    private Throwable cause;
    private State state;
 
    /**
     * Creates new form ProfileEditorPanel
     */
    public ProfileEditorPanel(SecurityMechanism secMech, ServerInstance instance) {
        initComponents();
       
        this.secMech = secMech;
        this.instance = instance;
        listeners = new ArrayList<ChangeListener>();
        state = State.INITIALIZING;
        
        initVisualState();
        initConfiguratorAsync();
    }
    
    private void initConfiguratorAsync() {
        
        // Call the actual initConfigurator() in a separate thread.
        Thread thread = new Thread() {
            public void run() {
                initConfigurator();
            }
        };
        thread.start();
        
        // Call pingServer() in another separate thread.
        thread = new Thread() {
            public void run() {
                pingServer();
            }
        };
        thread.start();
        
        //
        // Wait a little while for the configurator to initialize before
        // we return. In the case the configurator takes less than
        // WAIT_TIME to intialize, we give the user the illusion that
        // the panel is initialized synchronously.
        //
        synchronized (this) {
            try {
                this.wait(WAIT_TIME);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    
    private void initConfigurator() {
        try {
            configurator = ProviderConfigurator.getConfigurator(secMech.getName(), Type.WSP,
                    AccessMethod.DYNAMIC,
                    instance.getServerProperties(),
                    instance.getID());
            
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    configurator.addModifier(Configurable.SIGN_RESPONSE, signResponseCB);
                    configurator.addModifier(Configurable.USE_DEFAULT_KEYSTORE, useDefaultKeyStoreCB);
                    configurator.addModifier(Configurable.KEYSTORE_LOCATION, keystoreLocationTF);
                    configurator.addModifier(Configurable.KEYSTORE_PASSWORD, keystorePasswordTF);
                    configurator.addModifier(Configurable.KEY_ALIAS, keyAliasTF);
                    configurator.addModifier(Configurable.KEY_PASSWORD, keyPasswordTF);
                    configurator.addModifier(Configurable.USERNAME_PASSWORD_PAIRS, userNameTable);
                    
                    enableAllComponents();
                    initUserNameTable();
                    updateVisualState();
                }
            });
            
            updateState(State.INITIALIZED);
            configurator.addChangeListener(this);
        } catch (InvocationTargetException ex) {
            //ex.printStackTrace();
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        } catch (ConfiguratorException ex) {
            cause = ex.getCause();
            //cause.printStackTrace();
            updateState(State.INIT_FAILED);
        }
    }
    
    private void pingServer() {
        if (!instance.isRunning()) {
            updateState(State.NOT_RUNNING);
        }
    }
    
    private synchronized void updateState(State newState) {
        //System.out.println("updateState newState = " + newState);
        this.state = newState;
        fireStateChanged();
    }
    
    private void initUserNameTable() {
        if (secMech.isPasswordCredentialRequired()) {
            // Make the table single select
            final ListSelectionModel selectionModel = userNameTable.getSelectionModel();
            selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            selectionModel.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
                    
                    if (selectionModel.isSelectionEmpty()) {
                        editButton.setEnabled(false);
                        removeButton.setEnabled(false);
                    } else {
                        editButton.setEnabled(true);
                        removeButton.setEnabled(true);
                    }
                }
            });
            
            // Change the password column's cell render to display ****
            TableColumn tableColumn = userNameTable.getColumn(userNameTable.getColumnName(1));
            
            tableColumn.setCellRenderer(new DefaultTableCellRenderer() {
                protected void setValue(Object value) {
                    if (value instanceof String) {
                        StringBuffer buf = new StringBuffer((String) value);
                        int length = buf.length();
                        
                        for (int i = 0; i < length; i++) {
                            buf.setCharAt(i, '*');
                        }
                        
                        value = buf.toString();
                    }
                    
                    super.setValue(value);
                }
            });
        }
    }
    
    private void initVisualState() {
        if (secMech.isLiberty()) {
            signResponseCB.setSelected(true);
            signResponseCB.setEnabled(false);
        }
        
        if (!secMech.isPasswordCredentialRequired()) {
            userNameTableScrollPane.setVisible(false);
            addButton.setVisible(false);
            editButton.setVisible(false);
            removeButton.setVisible(false);
            usernameInfoLabel.setVisible(false);
            usernameInfoDescLabel.setVisible(false);
        }
    }
    
    private void updateVisualState() {
        if (secMech.isPasswordCredentialRequired()) {
            if (signResponseCB.isSelected()) {
                certSettingsLabel.setEnabled(true);
                useDefaultKeyStoreCB.setEnabled(true);
                
                if (useDefaultKeyStoreCB.isSelected()) {
                    updateKeystoreVisualState(false);
                } else {
                    updateKeystoreVisualState(true);
                }
            } else {
                certSettingsLabel.setEnabled(false);
                useDefaultKeyStoreCB.setEnabled(false);
                updateKeystoreVisualState(false);
            }
        } else {
            if (useDefaultKeyStoreCB.isSelected()) {
                updateKeystoreVisualState(false);
            } else {
                updateKeystoreVisualState(true);
            }
        }
    }
    
    private void updateKeystoreVisualState(boolean flag) {
        keystoreLocationTF.setEnabled(flag);
        keystoreLocationLabel.setEnabled(flag);
        keystorePasswordTF.setEnabled(flag);
        keystorePasswordLabel.setEnabled(flag);
        keyAliasTF.setEnabled(flag);
        keyAliasLabel.setEnabled(flag);
        keyPasswordLabel.setEnabled(flag);
        keyPasswordTF.setEnabled(flag);
        
        if (flag) {
            if (instance.isLocal()) {
                browseButton.setEnabled(true);
            } else {
                browseButton.setEnabled(false);
            }
        } else {
            browseButton.setEnabled(false);
        }
    }
    
    public void enableAllComponents() {
        if (!secMech.isLiberty()) {
            signResponseCB.setEnabled(true);
        }
        
        certSettingsLabel.setEnabled(true);
        useDefaultKeyStoreCB.setEnabled(true);
        keystoreLocationTF.setEnabled(true);
        keystoreLocationLabel.setEnabled(true);
        keystorePasswordTF.setEnabled(true);
        keystorePasswordLabel.setEnabled(true);
        keyAliasTF.setEnabled(true);
        keyAliasLabel.setEnabled(true);
        browseButton.setEnabled(true);
        userNameTableScrollPane.setEnabled(true);
        addButton.setEnabled(true);
        //editButton.setEnabled(true);
        //removeButton.setEnabled(true);
        usernameInfoLabel.setEnabled(true);
        usernameInfoDescLabel.setEnabled(true);
    }
    
    public void save() {
        if (configurator != null) {
            configurator.save();
        }
    }
    
    public JComponent[] getEditableComponents() {
        return new JComponent[] {};
    }
    
    public synchronized String checkValues() {
        switch (state) {
            case INITIALIZING:
                return EditDialogDescriptor.STATUS_PREFIX +
                        NbBundle.getMessage(ProfileEditorPanel.class,
                        "MSG_Initializing");
            case NOT_RUNNING:
                return NbBundle.getMessage(ProfileEditorPanel.class,
                        "MSG_ServerNotRunning");
            case INIT_FAILED:
                //return "<html>" + cause.getMessage() + "</html>";   //NOI18N
                return NbBundle.getMessage(ProfileEditorPanel.class,
                        "MSG_InitFailed", cause.toString());
            case INITIALIZED:
                String errorMsg = configurator.getError();
                
                //
                // Replace the unlocalized "Invalid KeyStore" message
                // from the AM client code with our localized version.
                //
                if (errorMsg != null &&
                        errorMsg.startsWith(AM_INVALID_KEYSTORE_ERR)) {
                    errorMsg = NbBundle.getMessage(ProfileEditorPanel.class, 
                            "ERR_InvalidKeystore");
                }
                
                return errorMsg;
        }
        
        return null;
    }
    
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        
        for (ChangeListener l : listeners) {
            l.stateChanged(event);
        }
    }
    
    public void stateChanged(ChangeEvent event) {
        fireStateChanged();
    }
    
    private void showUserNamePasswordEditor(boolean add) {
        final UserNamePasswordEditorPanel panel =
                new UserNamePasswordEditorPanel(add, getUserNames());
        DefaultTableModel model = (DefaultTableModel) userNameTable.getModel();
        int row = -1;
        
        if (!add) {
            row = userNameTable.getSelectedRow();
            
            // simply return if there is no row selected
            if (row == -1) return;
            
            panel.setUserName((String) model.getValueAt(row, 0));
            panel.setPassword((String) model.getValueAt(row, 1));
        } else {
            row = model.getRowCount();
        }
        
        EditDialogDescriptor descriptor = new EditDialogDescriptor(
                panel,
                NbBundle.getMessage(ProfileEditorPanel.class, "TTL_User"),
                add,
                panel.getEditableComponents(),
                getHelpCtx()) {
            public String validate() {
                return panel.checkValues();
            }
        };
        
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
            
            if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                String userName = panel.getUserName();
                String password = panel.getPassword();
                
                if (!add) {
                    model.setValueAt(userName, row, 0);
                    model.setValueAt(password, row, 1);
                    model.fireTableRowsUpdated(row, row);
                } else {
                    model.insertRow(row, new Object[] {userName, password});
                    model.fireTableRowsInserted(row, row);
                }
            }
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }
    
    private String[] getUserNames() {
        DefaultTableModel model = (DefaultTableModel) userNameTable.getModel();
        int rowCount = model.getRowCount();
        String[] userNames = new String[rowCount];
        
        for (int i = 0; i < rowCount; i++) {
            userNames[i]  = (String) model.getValueAt(i, 0);
        }
        
        return userNames;
    }
    
    private HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        signResponseCB = new javax.swing.JCheckBox();
        certSettingsLabel = new javax.swing.JLabel();
        keystoreLocationLabel = new javax.swing.JLabel();
        keystoreLocationTF = new javax.swing.JTextField();
        keystorePasswordLabel = new javax.swing.JLabel();
        keyAliasLabel = new javax.swing.JLabel();
        keyAliasTF = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        userNameTableScrollPane = new javax.swing.JScrollPane();
        userNameTable = new javax.swing.JTable();
        usernameInfoLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        usernameInfoDescLabel = new javax.swing.JLabel();
        keystorePasswordTF = new javax.swing.JPasswordField();
        useDefaultKeyStoreCB = new javax.swing.JCheckBox();
        keyPasswordLabel = new javax.swing.JLabel();
        keyPasswordTF = new javax.swing.JPasswordField();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(signResponseCB, bundle.getString("LBL_SignResponse")); // NOI18N
        signResponseCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        signResponseCB.setEnabled(false);
        signResponseCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        signResponseCB.setOpaque(false);
        signResponseCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signResponseCBActionPerformed(evt);
            }
        });

        certSettingsLabel.setText(bundle.getString("LBL_CertificateSettings")); // NOI18N
        certSettingsLabel.setEnabled(false);

        keystoreLocationLabel.setLabelFor(keystoreLocationTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationLabel, bundle.getString("LBL_KeyStoreLocation")); // NOI18N
        keystoreLocationLabel.setEnabled(false);

        keystoreLocationTF.setEnabled(false);
        keystoreLocationTF.setNextFocusableComponent(browseButton);

        keystorePasswordLabel.setLabelFor(keystorePasswordTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystorePasswordLabel, bundle.getString("LBL_KeystorePassword")); // NOI18N
        keystorePasswordLabel.setEnabled(false);

        keyAliasLabel.setLabelFor(keyAliasTF);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, bundle.getString("LBL_KeyAlias")); // NOI18N
        keyAliasLabel.setEnabled(false);

        keyAliasTF.setEnabled(false);
        keyAliasTF.setNextFocusableComponent(keyPasswordTF);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, bundle.getString("LBL_Browse")); // NOI18N
        browseButton.setEnabled(false);
        browseButton.setNextFocusableComponent(keystorePasswordTF);
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        userNameTableScrollPane.setEnabled(false);

        userNameTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        userNameTableScrollPane.setViewportView(userNameTable);

        usernameInfoLabel.setLabelFor(userNameTable);
        org.openide.awt.Mnemonics.setLocalizedText(usernameInfoLabel, bundle.getString("LBL_UsernameTokenProfileInfo")); // NOI18N
        usernameInfoLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("LBL_Add")); // NOI18N
        addButton.setEnabled(false);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editButton, bundle.getString("LBL_Edit")); // NOI18N
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("LBL_Remove")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        usernameInfoDescLabel.setText(bundle.getString("LBL_UsernameInfoDesc")); // NOI18N
        usernameInfoDescLabel.setEnabled(false);

        keystorePasswordTF.setEnabled(false);
        keystorePasswordTF.setNextFocusableComponent(keyAliasTF);

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultKeyStoreCB, bundle.getString("LBL_UseDefaultKeyStore")); // NOI18N
        useDefaultKeyStoreCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useDefaultKeyStoreCB.setEnabled(false);
        useDefaultKeyStoreCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useDefaultKeyStoreCB.setNextFocusableComponent(keystoreLocationTF);
        useDefaultKeyStoreCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useDefaultKeyStoreCBActionPerformed(evt);
            }
        });

        keyPasswordLabel.setLabelFor(keyPasswordTF);
        org.openide.awt.Mnemonics.setLocalizedText(keyPasswordLabel, bundle.getString("LBL_KeyAliasPassword")); // NOI18N
        keyPasswordLabel.setEnabled(false);

        keyPasswordTF.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(useDefaultKeyStoreCB))
                    .addComponent(signResponseCB)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(certSettingsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 309, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keystorePasswordLabel)
                                    .addComponent(keyAliasLabel)
                                    .addComponent(keystoreLocationLabel)
                                    .addComponent(keyPasswordLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(keyPasswordTF, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                                    .addComponent(keyAliasTF, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                                    .addComponent(keystorePasswordTF, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE)
                                    .addComponent(keystoreLocationTF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
                                .addGap(7, 7, 7)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton))
                    .addComponent(usernameInfoLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton))
                    .addComponent(usernameInfoDescLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 614, Short.MAX_VALUE)
                    .addComponent(userNameTableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(signResponseCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(certSettingsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useDefaultKeyStoreCB, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystoreLocationLabel)
                    .addComponent(browseButton)
                    .addComponent(keystoreLocationTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keystorePasswordLabel)
                    .addComponent(keystorePasswordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyAliasLabel)
                    .addComponent(keyAliasTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyPasswordLabel)
                    .addComponent(keyPasswordTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usernameInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usernameInfoDescLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userNameTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(editButton)
                    .addComponent(removeButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void useDefaultKeyStoreCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useDefaultKeyStoreCBActionPerformed
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_useDefaultKeyStoreCBActionPerformed
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
// TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile()) {
                    if (file.getName().endsWith(JKS_EXTENSION)) {
                        return true;
                    } else {
                        return false;
                    }
                }
                
                return true;
            }
            
            public String getDescription() {
                return NbBundle.getMessage(ProfileEditorPanel.class, "TXT_JavaKeyStore");
            }
        });
        
        int returnVal = chooser.showOpenDialog(this);
        
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            keystoreLocationTF.setText(chooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed
    
    private void signResponseCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_signResponseCBActionPerformed
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_signResponseCBActionPerformed
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
// TODO add your handling code here:
        int row = userNameTable.getSelectedRow();
        
        // simply return if there is no row selected;
        if (row == -1) return;
        
        DefaultTableModel model = (DefaultTableModel) userNameTable.getModel();
        String userName = (String) model.getValueAt(row, 0);
        
        NotifyDescriptor d =
                new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(ProfileEditorPanel.class, "LBL_ReallyRemove",
                userName),
                NbBundle.getMessage(ProfileEditorPanel.class, "TTL_RemoveUser"),
                NotifyDescriptor.OK_CANCEL_OPTION);
        
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            model.removeRow(row);
            model.fireTableRowsDeleted(row, row);
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
// TODO add your handling code here:
        showUserNamePasswordEditor(false);
    }//GEN-LAST:event_editButtonActionPerformed
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
// TODO add your handling code here:
        showUserNamePasswordEditor(true);
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel certSettingsLabel;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel keyAliasLabel;
    private javax.swing.JTextField keyAliasTF;
    private javax.swing.JLabel keyPasswordLabel;
    private javax.swing.JPasswordField keyPasswordTF;
    private javax.swing.JLabel keystoreLocationLabel;
    private javax.swing.JTextField keystoreLocationTF;
    private javax.swing.JLabel keystorePasswordLabel;
    private javax.swing.JPasswordField keystorePasswordTF;
    private javax.swing.JButton removeButton;
    private javax.swing.JCheckBox signResponseCB;
    private javax.swing.JCheckBox useDefaultKeyStoreCB;
    private javax.swing.JTable userNameTable;
    private javax.swing.JScrollPane userNameTableScrollPane;
    private javax.swing.JLabel usernameInfoDescLabel;
    private javax.swing.JLabel usernameInfoLabel;
    // End of variables declaration//GEN-END:variables
    
}
