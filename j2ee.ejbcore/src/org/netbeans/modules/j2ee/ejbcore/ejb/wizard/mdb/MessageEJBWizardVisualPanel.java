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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.util.Exceptions;

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
    
    public boolean isServerConfigured() {
        String id = provider.getServerInstanceID();
        try {
            return id != null && Deployment.getDefault().getServerInstance(id).getJ2eePlatform() != null;
        } catch (InstanceRemovedException ex) {
            return false;
        }
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
        projectDestinationsCombo.setEnabled(projectDestinationsRadio.isSelected());
        serverDestinationsCombo.setEnabled(serverDestinationsRadio.isSelected());
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(projectDestinationsRadio)
                    .addComponent(serverDestinationsRadio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serverDestinationsCombo, 0, 261, Short.MAX_VALUE)
                    .addComponent(projectDestinationsCombo, 0, 261, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton)
                .addContainerGap())
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
