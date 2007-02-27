/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
                    instance.getServerProperties());
            
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
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (ConfiguratorException ex) {
            cause = ex.getCause();
            cause.printStackTrace();
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
            if (instance.isDefault()) {
                browseButton.setEnabled(true);
            } else {
                browseButton.setEnabled(false);
            }
        } else {
            browseButton.setEnabled(false);
        }
    }
    
    public void enableAllComponents() {
        signResponseCB.setEnabled(true);
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
                return cause.getMessage();
                //return NbBundle.getMessage(ProfileEditorPanel.class,
                //        "MSG_InitFailed", cause.toString());
            case INITIALIZED:
                return configurator.getError();
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

        org.openide.awt.Mnemonics.setLocalizedText(signResponseCB, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_SignResponse"));
        signResponseCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        signResponseCB.setEnabled(false);
        signResponseCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        signResponseCB.setOpaque(false);
        signResponseCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                signResponseCBActionPerformed(evt);
            }
        });

        certSettingsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_CertificateSettings"));
        certSettingsLabel.setEnabled(false);

        keystoreLocationLabel.setLabelFor(keystoreLocationTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystoreLocationLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_KeyStoreLocation"));
        keystoreLocationLabel.setEnabled(false);

        keystoreLocationTF.setEnabled(false);
        keystoreLocationTF.setNextFocusableComponent(browseButton);

        keystorePasswordLabel.setLabelFor(keystorePasswordTF);
        org.openide.awt.Mnemonics.setLocalizedText(keystorePasswordLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_KeystorePassword"));
        keystorePasswordLabel.setEnabled(false);

        keyAliasLabel.setLabelFor(keyAliasTF);
        org.openide.awt.Mnemonics.setLocalizedText(keyAliasLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_KeyAlias"));
        keyAliasLabel.setEnabled(false);

        keyAliasTF.setEnabled(false);
        keyAliasTF.setNextFocusableComponent(keyPasswordTF);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_Browse"));
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
        org.openide.awt.Mnemonics.setLocalizedText(usernameInfoLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_UsernameTokenProfileInfo"));
        usernameInfoLabel.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_Add"));
        addButton.setEnabled(false);
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_Edit"));
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_Remove"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        usernameInfoDescLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_UsernameInfoDesc"));
        usernameInfoDescLabel.setEnabled(false);

        keystorePasswordTF.setEnabled(false);
        keystorePasswordTF.setNextFocusableComponent(keyAliasTF);

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultKeyStoreCB, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_UseDefaultKeyStore"));
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
        org.openide.awt.Mnemonics.setLocalizedText(keyPasswordLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/identity/server/manager/ui/Bundle").getString("LBL_KeyAliasPassword"));
        keyPasswordLabel.setEnabled(false);

        keyPasswordTF.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(useDefaultKeyStoreCB))
                    .add(signResponseCB)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(certSettingsLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 323, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(keystorePasswordLabel)
                                    .add(keyAliasLabel)
                                    .add(keystoreLocationLabel)
                                    .add(keyPasswordLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(keystoreLocationTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                    .add(keyAliasTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                    .add(keyPasswordTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                                    .add(keystorePasswordTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(usernameInfoLabel)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(editButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton))
                    .add(usernameInfoDescLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, userNameTableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(signResponseCB)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(certSettingsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(useDefaultKeyStoreCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(keystoreLocationLabel)
                            .add(keystoreLocationTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(browseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keystorePasswordLabel)
                    .add(keystorePasswordTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyAliasLabel)
                    .add(keyAliasTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keyPasswordLabel)
                    .add(keyPasswordTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(usernameInfoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(usernameInfoDescLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(userNameTableScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addButton)
                    .add(editButton)
                    .add(removeButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
