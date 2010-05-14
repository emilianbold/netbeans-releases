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
import org.netbeans.modules.soa.jca.base.otd.api.OTDLink;
import org.netbeans.modules.soa.jca.base.otd.api.OTDLinks;
import org.netbeans.modules.soa.jca.base.spi.GlobalRarProvider;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;

/**
 * GUI part of wizard panel
 *
 * @author echou
 */
public final class GlobalRarVisualPanelSelectInbound extends JPanel implements ListSelectionListener {

    private GlobalRarWizardPanelSelectInbound wizardPanel;
    private GlobalRarRegistry globalRarRegistry;
    private InboundListModel inboundRarListModel;
    private InboundOTDComboBoxModel inboundOtdComboBoxModel;

    /** Creates new form GLOBALRARVisualPanel1 */
    public GlobalRarVisualPanelSelectInbound(GlobalRarWizardPanelSelectInbound wizardPanel, Project project) {
        this.wizardPanel = wizardPanel;
        globalRarRegistry = GlobalRarRegistry.getInstance();
        inboundRarListModel = new InboundListModel();
        inboundOtdComboBoxModel = new InboundOTDComboBoxModel(project);
        initComponents();
        inboundRarList.setCellRenderer(new InboundCellRenderer());
        inboundRarList.addListSelectionListener(wizardPanel);
        inboundRarList.addListSelectionListener(this);
        inboundOtdComboBox.addItemListener(wizardPanel);
    }

    public void initFromSettings(WizardDescriptor settings) {
        if (inboundRarListModel.getSize() > 0 && inboundRarList.getSelectedIndex() == -1) {
            inboundRarList.setSelectedIndex(0);
        }
    }

    public void storeToSettings(WizardDescriptor settings) {
        if (inboundRarList.getSelectedIndex() != -1) {
            settings.putProperty(GlobalRarInboundWizard.RAR_NAME_PROP,
                    inboundRarListModel.getProviderAt(inboundRarList.getSelectedIndex()).getName());
            settings.putProperty(GlobalRarInboundWizard.LISTENER_NAME_PROP,
                    inboundRarListModel.getProviderAt(inboundRarList.getSelectedIndex()).getListenerInterfaces().get(0));
        }

        if (inboundOtdComboBox.isVisible()) {
            if (inboundOtdComboBox.getSelectedItem() == null) {
                settings.putProperty(GlobalRarInboundWizard.OTD_TYPE_PROP,
                    "");
            } else {
                settings.putProperty(GlobalRarInboundWizard.OTD_TYPE_PROP,
                    inboundOtdComboBox.getSelectedItem());
            }
        }
    }

    public boolean isWizardValid() {
        int index = inboundRarList.getSelectedIndex();
        if (index == -1) {
            return false;
        }
        if (inboundOtdComboBox.isVisible() && inboundOtdComboBox.getSelectedIndex() == -1) {
            return false;
        }

        return true;
    }

    public String getName() {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/inbound/wizard/Bundle").getString("Choose_Inbound_Rar");
    }

    public void valueChanged(ListSelectionEvent e) {
        int rarIndex = inboundRarList.getSelectedIndex();
        if (rarIndex == -1) {
            inboundOtdLabel.setVisible(false);
            inboundOtdComboBox.setVisible(false);
            return;
        }
        List<String> inboundStaticOtdTypes = inboundRarListModel.getProviderAt(rarIndex).getInboundStaticOTDTypes();
        List<String> inboundOtdTypes = inboundRarListModel.getProviderAt(rarIndex).getInboundOTDTypes();
        if ((inboundStaticOtdTypes == null || inboundStaticOtdTypes.size() < 1) &&
                (inboundOtdTypes == null || inboundOtdTypes.size() < 1)) {
            inboundOtdLabel.setVisible(false);
            inboundOtdComboBox.setVisible(false);
            return;
        }
        inboundOtdComboBoxModel.setRarName(inboundRarListModel.getProviderAt(rarIndex).getName());
        inboundOtdLabel.setVisible(true);
        inboundOtdComboBox.setVisible(true);
        if (inboundOtdComboBox.getItemCount() > 0) {
            inboundOtdComboBox.setSelectedIndex(0);
        }
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
        inboundRarList = new javax.swing.JList();
        inboundOtdLabel = new javax.swing.JLabel();
        inboundOtdComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setAutoscrolls(true);

        inboundRarList.setModel(inboundRarListModel);
        inboundRarList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(inboundRarList);
        inboundRarList.getAccessibleContext().setAccessibleName("Inbound List");
        inboundRarList.getAccessibleContext().setAccessibleDescription("Inbound List");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);

        inboundOtdLabel.setLabelFor(inboundOtdComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(inboundOtdLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelSelectInbound.class, "lbl_select_inbound_otd")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(inboundOtdLabel, gridBagConstraints);
        inboundOtdLabel.getAccessibleContext().setAccessibleDescription("inbound");

        inboundOtdComboBox.setModel(inboundOtdComboBoxModel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(inboundOtdComboBox, gridBagConstraints);
        inboundOtdComboBox.getAccessibleContext().setAccessibleName("Inbound OTD");
        inboundOtdComboBox.getAccessibleContext().setAccessibleDescription("Inbound OTD");

        getAccessibleContext().setAccessibleName("Select Inbound");
        getAccessibleContext().setAccessibleDescription("Select Inbound");
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox inboundOtdComboBox;
    private javax.swing.JLabel inboundOtdLabel;
    private javax.swing.JList inboundRarList;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    class InboundListModel extends DefaultListModel {

        private List<GlobalRarProvider> listData = new ArrayList<GlobalRarProvider> ();

        public InboundListModel() {
            super();
            for (GlobalRarProvider p : globalRarRegistry.getKnownRars()) {
                if (p.supportsInbound()) {
                    listData.add(p);
                }
            }
        }

        public int getSize() {
            return listData.size();
        }

        public Object getElementAt(int index) {
            return listData.get(index).getDisplayName();
        }

        public GlobalRarProvider getProviderAt(int index) {
            return listData.get(index);
        }

    }

    class InboundCellRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(
                JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus     // the list and the cell have the focus
                ) {
            setText(value.toString());
            setIcon(inboundRarListModel.getProviderAt(index).getIcon());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    class InboundOTDComboBoxModel extends DefaultComboBoxModel {

        private Collection<? extends OTDLinks> otdLinksCollection;
        private List<String> filteredOtdTypes = new ArrayList<String> ();

        public InboundOTDComboBoxModel(Project project) {
            otdLinksCollection = project.getLookup().lookupAll(OTDLinks.class);
        }

        public void setRarName(String rarName) {
            filteredOtdTypes.clear();
            if (globalRarRegistry.getRar(rarName).getInboundStaticOTDTypes() != null ||
                    globalRarRegistry.getRar(rarName).getInboundStaticOTDTypes().size() > 0) {
                filteredOtdTypes.addAll(globalRarRegistry.getRar(rarName).getInboundStaticOTDTypes());
            }
            if (globalRarRegistry.getRar(rarName).getInboundOTDTypes() != null ||
                    globalRarRegistry.getRar(rarName).getInboundOTDTypes().size() > 0) {
                for (OTDLinks otdLinks : otdLinksCollection) {
                    for (OTDLink otdLink : otdLinks.getOTDList()) {
                        for (String inboundOTDType : globalRarRegistry.getRar(rarName).getInboundOTDTypes()) {
                            if (inboundOTDType.equals(otdLink.getType())) {
                                filteredOtdTypes.add(otdLink.getRootClass());
                                break;
                            }
                        }
                    }
                }
            }
            this.fireContentsChanged(this, 0, 0);
        }

        @Override
        public Object getElementAt(int index) {
            if (filteredOtdTypes != null) {
                return filteredOtdTypes.get(index);
            }
            return null;
        }

        @Override
        public int getSize() {
            if (filteredOtdTypes != null) {
                return filteredOtdTypes.size();
            }
            return 0;
        }

    }

}

