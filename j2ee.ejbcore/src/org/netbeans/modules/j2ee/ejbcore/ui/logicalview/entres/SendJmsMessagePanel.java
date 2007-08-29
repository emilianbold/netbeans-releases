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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.openide.util.NbBundle;

/**
 * Panel for specifying message destination for Send JMS Message action.
 * @author Tomas Mysik
 */
public class SendJmsMessagePanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = SendJmsMessagePanel.class.getName() + ".IS_VALID";
    
    private final J2eeModuleProvider provider;
    private final Set<MessageDestination> moduleDestinations;
    private final Set<MessageDestination> serverDestinations;
    private final List<SendJMSMessageUiSupport.MdbHolder> mdbs;
    private final boolean isDestinationCreationSupportedByServerPlugin;
    private final ServiceLocatorStrategyPanel slPanel;
    
    // private because correct initialization is needed
    private SendJmsMessagePanel(J2eeModuleProvider provider, Set<MessageDestination> moduleDestinations,
            Set<MessageDestination> serverDestinations, List<SendJMSMessageUiSupport.MdbHolder> mdbs,
            String lastLocator) {
        initComponents();
        
        this.provider = provider;
        this.moduleDestinations = moduleDestinations;
        this.serverDestinations = serverDestinations;
        this.mdbs = mdbs;
        isDestinationCreationSupportedByServerPlugin = provider.getConfigSupport().supportsCreateMessageDestination();
        slPanel = new ServiceLocatorStrategyPanel(lastLocator);
    }
    
    /**
     * Factory method for creating new instance.
     * @param provider Java EE module provider.
     * @param moduleDestinations project message destinations.
     * @param serverDestinations server message destinations.
     * @param mdbs message-driven beans with their properties.
     * @param lastLocator name of the service locator.
     * @return SendJmsMessagePanel instance.
     */
    public static SendJmsMessagePanel newInstance(final J2eeModuleProvider provider, final Set<MessageDestination> moduleDestinations,
            final Set<MessageDestination> serverDestinations, final List<SendJMSMessageUiSupport.MdbHolder> mdbs,
            final String lastLocator) {
        SendJmsMessagePanel sjmp = new SendJmsMessagePanel(
                provider,
                moduleDestinations,
                serverDestinations,
                mdbs,
                lastLocator);
        sjmp.initialize();
        sjmp.verifyAndFire();
        sjmp.handleConnectionFactory();
        return sjmp;
    }
    
    /**
     * Get the message destination.
     * @return selected destination or <code>null</code> if no destination type is selected.
     */
    public MessageDestination getDestination() {
        if (projectDestinationsRadio.isSelected()) {
            return (MessageDestination) projectDestinationsCombo.getSelectedItem();
        } else if (serverDestinationsRadio.isSelected()) {
            return (MessageDestination) serverDestinationsCombo.getSelectedItem();
        }
        SendJMSMessageUiSupport.MdbHolder mdbHolder = (SendJMSMessageUiSupport.MdbHolder) mdbCombo.getSelectedItem();
        if (mdbHolder != null) {
            return mdbHolder.getMessageDestination();
        }
        return null;
    }

    public String getConnectionFactory() {
        return connectionFactoryTextField.getText();
    }
    
    /**
     * Get the service locator strategy.
     * @return the service locator strategy.
     */
    public String getServiceLocator() {
        return slPanel.classSelected();
    }

    /**
     * Return project holding MDB if MDB from some project is used, null otherwise.
     */
    public Project getMdbHolderProject() {
        if (mdbRadio.isSelected()) {
            SendJMSMessageUiSupport.MdbHolder mdbHolder = (SendJMSMessageUiSupport.MdbHolder) mdbCombo.getSelectedItem();
            return mdbHolder.getProject();
        }
        return null;
    }
    
    private void initialize() {
        registerListeners();
        setupProjectDestinationsOption();
        setupMessageDrivenOption();
        setupErrorLabel();
        setupServiceLocatorPanel();
        handleComboBoxes();
        
        populate();
    }
    
    private void registerListeners() {
        // radio buttons
        projectDestinationsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
                handleComboBoxes();
            }
        });
        serverDestinationsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
                handleComboBoxes();
            }
        });
        mdbRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
                handleComboBoxes();
            }
        });
        
        // combo boxes
        projectDestinationsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
            }
        });
        serverDestinationsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
            }
        });
        mdbCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
            }
        });
        connectionFactoryTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                verifyAndFire();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                verifyAndFire();
            }
            public void changedUpdate(DocumentEvent documentEvent) {
                verifyAndFire();
            }
        });
    }
    
    private void setupProjectDestinationsOption() {
        if (J2eeModule.EJB.equals(provider.getJ2eeModule().getModuleType())) {
            projectDestinationsRadio.setEnabled(true);
            setupAddButton();
            projectDestinationsRadio.setSelected(true);
        } else {
            projectDestinationsRadio.setEnabled(false);
            addButton.setEnabled(false);
            serverDestinationsRadio.setSelected(true);
        }
    }
    
    private void setupMessageDrivenOption() {
        mdbRadio.setEnabled(J2eeModule.EJB.equals(provider.getJ2eeModule().getModuleType()) || Utils.isPartOfJ2eeApp(provider));
    }
    
    private void setupAddButton() {
        if (!isDestinationCreationSupportedByServerPlugin) {
            // missing server?
            addButton.setEnabled(false);
        }
    }
   
    private void handleComboBoxes() {
        projectDestinationsCombo.setEnabled(projectDestinationsRadio.isSelected());
        serverDestinationsCombo.setEnabled(serverDestinationsRadio.isSelected());
        mdbCombo.setEnabled(mdbRadio.isSelected());
        destinationText.setEnabled(mdbRadio.isSelected());
        handleConnectionFactory();
    }
    
    private void handleConnectionFactory() {
        MessageDestination messageDestination = getDestination();
        if (messageDestination != null) {
            connectionFactoryTextField.setText(messageDestination.getName() + "Factory"); // NOI18N
        } else {
            connectionFactoryTextField.setText(null);
        }
    }
    
    private void setupErrorLabel() {
        setError(null);
        errorLabel.setForeground(getErrorColor());
    }
    
    private Color getErrorColor() {
        Color errorColor = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (errorColor != null) {
            return errorColor;
        }
        return new Color(255, 0, 0);
    }
    
    private void setupServiceLocatorPanel() {
        slPanel.addPropertyChangeListener(ServiceLocatorStrategyPanel.IS_VALID,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            boolean isServiceLocatorOk = ((Boolean) newvalue).booleanValue();
                            if (isServiceLocatorOk) {
                                verifyAndFire();
                            } else {
                                firePropertyChange(IS_VALID, true, false);
                            }
                        }
                    }
                });
        serviceLocatorPanel.add(slPanel, BorderLayout.CENTER);
    }
    
    private void populate() {
        SendJMSMessageUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, null);
        SendJMSMessageUiSupport.populateDestinations(serverDestinations, serverDestinationsCombo, null);
        SendJMSMessageUiSupport.populateMessageDrivenBeans(mdbs, mdbCombo, destinationText);
    }
    
    void verifyAndFire() {
        boolean isValid = verifyComponents();
        firePropertyChange(IS_VALID, !isValid, isValid);
    }
    
    private boolean verifyComponents() {
        // destination
        if (destinationGroup.getSelection() == null
                || getDestination() == null) {
            setError("ERR_NoDestinationSelected"); // NOI18N
            return false;
        }
        
        if ("".equals(getConnectionFactory().trim())) {
            setError("ERR_NoConnectionFactorySelected"); // NOI18N
            return false;
        }
        
        // no errors
        setError(null);
        return true;
    }
    
    private void setError(String key) {
        if (key == null) {
            errorLabel.setText("");
            return;
        }
        errorLabel.setText(NbBundle.getMessage(SendJmsMessagePanel.class, key));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationGroup = new javax.swing.ButtonGroup();
        projectDestinationsRadio = new javax.swing.JRadioButton();
        serverDestinationsRadio = new javax.swing.JRadioButton();
        projectDestinationsCombo = new javax.swing.JComboBox();
        serverDestinationsCombo = new javax.swing.JComboBox();
        addButton = new javax.swing.JButton();
        mdbRadio = new javax.swing.JRadioButton();
        mdbCombo = new javax.swing.JComboBox();
        serviceLocatorPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();
        destinationLabel = new javax.swing.JLabel();
        destinationText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        connectionFactoryTextField = new javax.swing.JTextField();

        destinationGroup.add(projectDestinationsRadio);
        projectDestinationsRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(projectDestinationsRadio, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_ProjectDestinations")); // NOI18N
        projectDestinationsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        destinationGroup.add(serverDestinationsRadio);
        org.openide.awt.Mnemonics.setLocalizedText(serverDestinationsRadio, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_ServerDestinations")); // NOI18N
        serverDestinationsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        destinationGroup.add(mdbRadio);
        org.openide.awt.Mnemonics.setLocalizedText(mdbRadio, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_MessageDrivenBean")); // NOI18N
        mdbRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        serviceLocatorPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ERR_NoDestinationSelected")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(destinationLabel, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_Destination")); // NOI18N

        destinationText.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Connection Factory:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(serviceLocatorPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(projectDestinationsRadio)
                            .add(serverDestinationsRadio)
                            .add(mdbRadio)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, destinationLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(destinationText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, mdbCombo, 0, 289, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, serverDestinationsCombo, 0, 289, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, projectDestinationsCombo, 0, 289, Short.MAX_VALUE)
                            .add(connectionFactoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addButton)
                        .addContainerGap())
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectDestinationsRadio)
                    .add(addButton)
                    .add(projectDestinationsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverDestinationsRadio)
                    .add(serverDestinationsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mdbRadio)
                    .add(mdbCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(destinationLabel)
                    .add(destinationText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(connectionFactoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serviceLocatorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(errorLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        MessageDestination destination =
                SendJMSMessageUiSupport.createMessageDestination(provider, moduleDestinations, serverDestinations);
        if (destination != null) {
            moduleDestinations.add(destination);
            SendJMSMessageUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, destination);
        }
    }//GEN-LAST:event_addButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTextField connectionFactoryTextField;
    private javax.swing.ButtonGroup destinationGroup;
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JTextField destinationText;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox mdbCombo;
    private javax.swing.JRadioButton mdbRadio;
    private javax.swing.JComboBox projectDestinationsCombo;
    private javax.swing.JRadioButton projectDestinationsRadio;
    private javax.swing.JComboBox serverDestinationsCombo;
    private javax.swing.JRadioButton serverDestinationsRadio;
    private javax.swing.JPanel serviceLocatorPanel;
    // End of variables declaration//GEN-END:variables
    
}
