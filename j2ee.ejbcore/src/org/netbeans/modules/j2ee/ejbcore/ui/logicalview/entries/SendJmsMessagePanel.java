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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.openide.NotificationLineSupport;
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
    private NotificationLineSupport statusLine;
    
    // private because correct initialization is needed
    private SendJmsMessagePanel(J2eeModuleProvider provider, Set<MessageDestination> moduleDestinations,
            Set<MessageDestination> serverDestinations, List<SendJMSMessageUiSupport.MdbHolder> mdbs,
            String lastLocator, ClasspathInfo cpInfo) {
        initComponents();
        
        this.provider = provider;
        this.moduleDestinations = moduleDestinations;
        this.serverDestinations = serverDestinations;
        this.mdbs = mdbs;
        isDestinationCreationSupportedByServerPlugin = provider.getConfigSupport().supportsCreateMessageDestination();
        slPanel = new ServiceLocatorStrategyPanel(lastLocator, cpInfo);
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                verifyAndFire();
            }
            public void ancestorRemoved(AncestorEvent event) {
                verifyAndFire();
            }
            public void ancestorMoved(AncestorEvent event) {
                verifyAndFire();
            }
        });
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
            final String lastLocator, ClasspathInfo cpInfo) {
        SendJmsMessagePanel sjmp = new SendJmsMessagePanel(
                provider,
                moduleDestinations,
                serverDestinations,
                mdbs,
                lastLocator,
                cpInfo);
        sjmp.initialize();
        sjmp.verifyAndFire();
        sjmp.handleConnectionFactory();
        return sjmp;
    }

    public void setNotificationLine(NotificationLineSupport statusLine) {
        this.statusLine = statusLine;
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
                handleConnectionFactory();
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
        if (J2eeModule.Type.EJB.equals(provider.getJ2eeModule().getType())) {
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
        mdbRadio.setEnabled(J2eeModule.Type.EJB.equals(provider.getJ2eeModule().getType()) || Utils.isPartOfJ2eeApp(provider));
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
            setInfo("ERR_NoDestinationSelected"); // NOI18N
            return false;
        }
        
        if (getConnectionFactory().trim().length() < 1) {
            setInfo("ERR_NoConnectionFactorySelected"); // NOI18N
            return false;
        }
        
        // no errors
        if (statusLine != null) {
            statusLine.clearMessages();
        }
        return true;
    }
    
    private void setInfo(String key) {
        if (statusLine != null) {
            statusLine.setInformationMessage(NbBundle.getMessage(SendJmsMessagePanel.class, key));
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        org.openide.awt.Mnemonics.setLocalizedText(destinationLabel, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_Destination")); // NOI18N

        destinationText.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_ConnectionFactory")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serviceLocatorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectDestinationsRadio)
                            .addComponent(serverDestinationsRadio)
                            .addComponent(mdbRadio)
                            .addComponent(destinationLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(destinationText, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                            .addComponent(mdbCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 360, Short.MAX_VALUE)
                            .addComponent(serverDestinationsCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 360, Short.MAX_VALUE)
                            .addComponent(projectDestinationsCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 360, Short.MAX_VALUE)
                            .addComponent(connectionFactoryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton)
                        .addContainerGap())
                    .addComponent(jLabel1)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectDestinationsRadio)
                    .addComponent(addButton)
                    .addComponent(projectDestinationsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverDestinationsRadio)
                    .addComponent(serverDestinationsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mdbRadio)
                    .addComponent(mdbCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationLabel)
                    .addComponent(destinationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(connectionFactoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serviceLocatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        projectDestinationsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_JMSProjectDestination")); // NOI18N
        serverDestinationsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_JMSServerDestination")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_AddMessageDestination")); // NOI18N
        mdbRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_JMSMessageDestination")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_SendJMSMessage")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_SendJMSMessage")); // NOI18N
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
