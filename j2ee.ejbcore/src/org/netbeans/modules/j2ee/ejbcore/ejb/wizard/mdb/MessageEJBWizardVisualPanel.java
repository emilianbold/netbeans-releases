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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;

/**
 * Panel for specifying message destination. Project or server message destination can be chosen.
 * @author Tomas Mysik
 */
public class MessageEJBWizardVisualPanel extends javax.swing.JPanel {
    
    public static final String CHANGED = MessageEJBWizardVisualPanel.class.getName() + ".CHANGED";
    
    private final J2eeModuleProvider provider;
    private final Set<MessageDestination> moduleDestinations;
    private final Set<MessageDestination> serverDestinations;
    private final boolean isDestinationCreationSupportedByServerPlugin;
    
    // private because correct initialization is needed
    private MessageEJBWizardVisualPanel(J2eeModuleProvider provider, Set<MessageDestination> moduleDestinations, Set<MessageDestination> serverDestinations) {
        initComponents();
        
        this.provider = provider;
        this.moduleDestinations = moduleDestinations;
        this.serverDestinations = serverDestinations;
        isDestinationCreationSupportedByServerPlugin = provider.getConfigSupport().supportsCreateMessageDestination();
    }
    
    /**
     * Factory method for creating new instance.
     * @param provider Java EE module provider.
     * @param moduleDestinations project message destinations.
     * @param serverDestinations server message destinations.
     * @return MessageEJBWizardVisualPanel instance.
     */
    public static MessageEJBWizardVisualPanel newInstance(final J2eeModuleProvider provider, final Set<MessageDestination> moduleDestinations,
            final Set<MessageDestination> serverDestinations) {
        MessageEJBWizardVisualPanel mdp = new MessageEJBWizardVisualPanel(provider, moduleDestinations, serverDestinations);
        mdp.initialize();
        return mdp;
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
        return null;
    }
    
    /**
     * Return <code>true</code> if current server supports message destination creation.
     * @return <code>true</code> if current server supports message destination creation, <code>false</code> otherwise.
     */
    public boolean isDestinationCreationSupportedByServerPlugin() {
        return isDestinationCreationSupportedByServerPlugin;
    }
    
    private void initialize() {
        registerListeners();
        setupAddButton();
        handleComboBoxes();
        
        populate();
    }
    
    private void registerListeners() {
        // radio buttons
        projectDestinationsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fire();
                handleComboBoxes();
            }
        });
        serverDestinationsRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fire();
                handleComboBoxes();
            }
        });
        
        // combo boxes
        projectDestinationsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fire();
            }
        });
        serverDestinationsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fire();
            }
        });
    }
    private void setupAddButton() {
        if (!isDestinationCreationSupportedByServerPlugin) {
            // missing server?
            addButton.setEnabled(false);
        }
    }
   
    private void handleComboBoxes() {
        if (projectDestinationsRadio.isSelected()) {
            projectDestinationsCombo.setEnabled(true);
            serverDestinationsCombo.setEnabled(false);
        } else {
            projectDestinationsCombo.setEnabled(false);
            serverDestinationsCombo.setEnabled(true);
        }
    }
    
    private void populate() {
        MessageDestinationUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, null);
        MessageDestinationUiSupport.populateDestinations(serverDestinations, serverDestinationsCombo, null);
    }
    
    private void fire() {
        firePropertyChange(CHANGED, null, null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationsGroup = new javax.swing.ButtonGroup();
        projectDestinationsRadio = new javax.swing.JRadioButton();
        serverDestinationsRadio = new javax.swing.JRadioButton();
        projectDestinationsCombo = new javax.swing.JComboBox();
        addButton = new javax.swing.JButton();
        serverDestinationsCombo = new javax.swing.JComboBox();

        destinationsGroup.add(projectDestinationsRadio);
        projectDestinationsRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(projectDestinationsRadio, org.openide.util.NbBundle.getMessage(MessageEJBWizardVisualPanel.class, "LBL_ProjectDestinations")); // NOI18N
        projectDestinationsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectDestinationsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        destinationsGroup.add(serverDestinationsRadio);
        org.openide.awt.Mnemonics.setLocalizedText(serverDestinationsRadio, org.openide.util.NbBundle.getMessage(MessageEJBWizardVisualPanel.class, "LBL_ServerDestinations")); // NOI18N
        serverDestinationsRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serverDestinationsRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(MessageEJBWizardVisualPanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(projectDestinationsRadio)
                    .add(serverDestinationsRadio))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serverDestinationsCombo, 0, 261, Short.MAX_VALUE)
                    .add(projectDestinationsCombo, 0, 261, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addButton)
                .addContainerGap())
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
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        MessageDestination destination = 
                MessageDestinationUiSupport.createMessageDestination(provider, moduleDestinations, serverDestinations);
        if (destination != null) {
            moduleDestinations.add(destination);
            MessageDestinationUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, destination);
        }
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.ButtonGroup destinationsGroup;
    private javax.swing.JComboBox projectDestinationsCombo;
    private javax.swing.JRadioButton projectDestinationsRadio;
    private javax.swing.JComboBox serverDestinationsCombo;
    private javax.swing.JRadioButton serverDestinationsRadio;
    // End of variables declaration//GEN-END:variables
    
}
