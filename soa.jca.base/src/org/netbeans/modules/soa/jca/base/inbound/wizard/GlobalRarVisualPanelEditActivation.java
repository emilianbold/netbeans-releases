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

package org.netbeans.modules.soa.jca.base.inbound.wizard;

import javax.swing.JPanel;
import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import org.netbeans.modules.soa.jca.base.Util;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import org.netbeans.modules.soa.jca.base.spi.InboundConfigCustomPanel;
import java.awt.Dimension;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;

/**
 * GUI part of wizard panel
 *
 * @author echou
 */
public final class GlobalRarVisualPanelEditActivation extends JPanel implements ChangeListener {

    private GlobalRarWizardPanelEditActivation wizardPanel;
    private Project project;
    private WizardDescriptor wizard;
    private GlobalRarRegistry globalRarRegistry;
    private DefaultInboundConfigCustomPanel defaultPanel;
    private InboundConfigCustomPanel customPanel;
    private TxComboBoxModel txComboBoxModel = new TxComboBoxModel();

    /** Creates new form GLOBALRARVisualPanel1 */
    public GlobalRarVisualPanelEditActivation(GlobalRarWizardPanelEditActivation wizardPanel,
            Project project, WizardDescriptor wizard) {
        this.wizardPanel = wizardPanel;
        this.project = project;
        this.wizard = wizard;
        globalRarRegistry = GlobalRarRegistry.getInstance();
        initComponents();
    }

    public void initFromSettings(WizardDescriptor settings) {
        String rarName = (String) settings.getProperty(GlobalRarInboundWizard.RAR_NAME_PROP);
        GlobalRarProvider provider = globalRarRegistry.getRar(rarName);
        if (!provider.supportsInboundTx()) {
            txLabel.setVisible(false);
            txComboBox.setVisible(false);
        } else {
            txLabel.setVisible(true);
            txComboBox.setVisible(true);
        }

        String projName = ProjectUtils.getInformation(project).getName();
        String pkgName = Util.getSelectedPackageName(
            Templates.getTargetFolder(wizard)).replaceAll("\\.", "-"); // NOI18N
        String fileName = Templates.getTargetName(wizard);

        String contextName = projName + "-" + pkgName + "-" + fileName; // NOI18N
        customPanel = provider.getInboundConfigCustomPanel(project, contextName);
        if (customPanel == null) {
            defaultPanel = new DefaultInboundConfigCustomPanel(provider);
            defaultPanel.setPreferredSize(new Dimension(570, 402));
            defaultPanel.addChangeListener(this);
            jScrollPane1.setViewportView(defaultPanel);
        } else {
            customPanel.addChangeListener(this);
            jScrollPane1.setViewportView(customPanel);
        }

        if (txComboBox.getSelectedIndex() == -1) {
            txComboBox.setSelectedIndex(0);
        }
    }

    public void storeToSettings(WizardDescriptor settings) {
        try {
            InboundConfigDataImpl inboundConfigData = new InboundConfigDataImpl();
            if (customPanel == null) {
                defaultPanel.removeChangeListener(this);
                defaultPanel.storeToInboundConfigData(inboundConfigData);
            } else {
                customPanel.removeChangeListener(this);
                customPanel.storeToInboundConfigData(inboundConfigData);
            }
            settings.putProperty(GlobalRarInboundWizard.INBOUND_CONFIG_DATA_PROP, inboundConfigData);
            settings.putProperty(GlobalRarInboundWizard.TX_PROP, txComboBox.getSelectedItem());
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Exception(e);
            DialogDisplayer.getDefault().notifyLater(d);
            return;
        }

    }

    public boolean isWizardValid() {
        if (customPanel != null) {
            String errorStr = customPanel.isPanelValid();
            if (errorStr != null && errorStr.length() > 0) {
                errorLabel.setText(errorStr);
                return false;
            }
        }

        errorLabel.setText(null);
        return true;
    }

    public String getName() {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/wizard/Bundle").getString("Edit_Activation_Configuration");
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        txLabel = new javax.swing.JLabel();
        txComboBox = new javax.swing.JComboBox();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelEditActivation.class, "a11y.name.scrollpane")); // NOI18N

        txLabel.setLabelFor(txComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(txLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelEditActivation.class, "lbl_tx_management")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(txLabel, gridBagConstraints);
        txLabel.getAccessibleContext().setAccessibleDescription("transaction management");

        txComboBox.setModel(txComboBoxModel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(txComboBox, gridBagConstraints);

        errorLabel.setForeground(new java.awt.Color(255, 51, 0));
        errorLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, "holder string");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(errorLabel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelEditActivation.class, "a11y.name.rootpanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelEditActivation.class, "a11y.description.rootpanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel errorLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox txComboBox;
    private javax.swing.JLabel txLabel;
    // End of variables declaration//GEN-END:variables


    class TxComboBoxModel extends DefaultComboBoxModel {

        private String[] data = new String[] { "CONTAINER", "BEAN" }; // NOI18N

        @Override
        public Object getElementAt(int index) {
            return data[index];
        }

        @Override
        public int getSize() {
            return data.length;
        }

    }

    public void stateChanged(ChangeEvent e) {
        wizardPanel.fireChangeEvent();
    }

}

