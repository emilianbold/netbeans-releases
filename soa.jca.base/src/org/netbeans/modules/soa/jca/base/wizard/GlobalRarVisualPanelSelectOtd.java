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

package org.netbeans.modules.soa.jca.base.wizard;

import javax.swing.JPanel;
import org.netbeans.modules.soa.jca.base.GlobalRarRegistry;
import org.netbeans.modules.soa.jca.base.otd.api.OTDLink;
import org.netbeans.modules.soa.jca.base.otd.api.OTDLinks;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;

/**
 * GUI part of wizard panel
 *
 * @author echou
 */
public final class GlobalRarVisualPanelSelectOtd extends JPanel implements ListSelectionListener {

    private GlobalRarWizardPanelSelectOtd wizardPanel;
    private GlobalRarRegistry globalRarRegistry;
    private OTDTableModel otdTableModel;
    private SelectedOTDTableModel selectedOtdTableModel;

    public GlobalRarVisualPanelSelectOtd(GlobalRarWizardPanelSelectOtd wizardPanel, Project project) {
        this.wizardPanel = wizardPanel;
        globalRarRegistry = GlobalRarRegistry.getInstance();
        otdTableModel = new OTDTableModel(project);
        selectedOtdTableModel = new SelectedOTDTableModel(0);
        initComponents();

        // set component names for easier testability
        otdTable.setName("otdTbl");
        addButton.setName("addBtn");

        otdTable.getTableHeader().setReorderingAllowed(false);
        otdTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        otdTable.getSelectionModel().addListSelectionListener(this);
        selectedOtdTable.getTableHeader().setReorderingAllowed(false);
        selectedOtdTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectedOtdTable.getSelectionModel().addListSelectionListener(this);
        selectedOtdTable.getModel().addTableModelListener(wizardPanel);
    }

    public void initFromSettings(WizardDescriptor settings) {
        otdTableModel.setRarName(
                (String) settings.getProperty(GlobalRarWizardAction.RAR_NAME_PROP));
        otdTableModel.fireTableStructureChanged();

        sizeColumns(otdTable);
        setButtonStatus();
    }

    public void storeToSettings(WizardDescriptor settings) {
        if (selectedOtdTableModel.getSelectedOTDs().size() > 0) {
            settings.putProperty(GlobalRarWizardAction.OTD_TYPE_PROP,
                    selectedOtdTableModel.getSelectedOTDs().get(0).type);
        }
    }

    public boolean isWizardValid() {
        setButtonStatus();
        int size = selectedOtdTable.getRowCount();
        if (size == 0) {
            errorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Error_Select_OTD"));
            return false;
        }

        errorLabel.setText(null);
        return true;
    }

    public String getName() {
        return java.util.ResourceBundle.getBundle("org/netbeans/modules/soa/jca/base/wizard/Bundle").getString("Choose_OTD");
    }

    private void sizeColumns(JTable table) {
        JTableHeader header = table.getTableHeader();
        TableCellRenderer defaultHeaderRenderer = null;
        if (header != null) {
            defaultHeaderRenderer = header.getDefaultRenderer();
        }

        TableColumnModel columns = table.getColumnModel();
        TableModel data = table.getModel();
        int margin = columns.getColumnMargin() * 2;
        int columnCount = columns.getColumnCount();
        int rowCount = data.getRowCount();
        for (int col = 0; col < columnCount; col++) {
            TableColumn column = columns.getColumn(col);
            int modelCol = column.getModelIndex();

            TableCellRenderer h = column.getHeaderRenderer();
            if (h == null) {
                h = defaultHeaderRenderer;
            }

            int width = 0;
            if (h != null) {
                Component c = h.getTableCellRendererComponent(
                        table, column.getHeaderValue(), false, false, -1, col);
                width = c.getPreferredSize().width;
            }

            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer r = table.getCellRenderer(row, col);
                int w = r.getTableCellRendererComponent(
                  table, data.getValueAt(row, modelCol), false, false, row, col)
                  .getPreferredSize().width;
                if (w > width) {
                    width = w;
                }
            }
            column.setPreferredWidth(width + margin);
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
        otdTable = new javax.swing.JTable();
        errorLabel = new javax.swing.JLabel();
        availableLabel = new javax.swing.JLabel();
        selectedOtdLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectedOtdTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setMinimumSize(new java.awt.Dimension(180, 260));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(180, 260));

        otdTable.setModel(otdTableModel);
        otdTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(otdTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane1, gridBagConstraints);

        errorLabel.setForeground(new java.awt.Color(255, 51, 51));
        errorLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, "error label holder");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(errorLabel, gridBagConstraints);

        availableLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        availableLabel.setLabelFor(otdTable);
        org.openide.awt.Mnemonics.setLocalizedText(availableLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelSelectOtd.class, "lbl_available_otd")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(availableLabel, gridBagConstraints);

        selectedOtdLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        selectedOtdLabel.setLabelFor(selectedOtdTable);
        org.openide.awt.Mnemonics.setLocalizedText(selectedOtdLabel, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelSelectOtd.class, "lbl_selected_otd")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(selectedOtdLabel, gridBagConstraints);

        jScrollPane2.setMinimumSize(new java.awt.Dimension(180, 260));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(180, 260));

        selectedOtdTable.setModel(selectedOtdTableModel);
        jScrollPane2.setViewportView(selectedOtdTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelSelectOtd.class, "lbl_add_button")); // NOI18N
        addButton.setMaximumSize(new java.awt.Dimension(90, 23));
        addButton.setMinimumSize(new java.awt.Dimension(90, 23));
        addButton.setPreferredSize(new java.awt.Dimension(90, 23));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(GlobalRarVisualPanelSelectOtd.class, "lbl_remove_button")); // NOI18N
        removeButton.setMaximumSize(new java.awt.Dimension(90, 23));
        removeButton.setMinimumSize(new java.awt.Dimension(90, 23));
        removeButton.setPreferredSize(new java.awt.Dimension(90, 23));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "        ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        selectedOtdTableModel.addRow(new Object[] {
            otdTableModel.getSelectedOTDType(otdTable.getSelectedRow())});
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        selectedOtdTableModel.removeRow(selectedOtdTable.getSelectedRow());
    }//GEN-LAST:event_removeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel availableLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable otdTable;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel selectedOtdLabel;
    private javax.swing.JTable selectedOtdTable;
    // End of variables declaration//GEN-END:variables


    class OTDTableModel extends DefaultTableModel {

        private String[] columnNamesStaticOTD = new String[] { "Type" };
        private String[] columnNamesOTDLink = new String[] { "Name", "Path", "Root Class" };

        private String rarName;
        private boolean isStaticOTD = true;
        private Collection<? extends OTDLinks> otdLinksCollection;
        private List<OTDLink> filteredOTDLinks = new ArrayList<OTDLink> ();

        public OTDTableModel(Project project) {
            otdLinksCollection = project.getLookup().lookupAll(OTDLinks.class);
        }

        public void setRarName(String rarName) {
            this.rarName = rarName;
            filteredOTDLinks.clear();
            if (globalRarRegistry.getRar(rarName).getOTDTypes() == null ||
                    globalRarRegistry.getRar(rarName).getOTDTypes().size() == 0) {
                isStaticOTD = false;
                for (OTDLinks otdLinks : otdLinksCollection) {
                    for (OTDLink otdLink : otdLinks.getOTDList()) {
                        for (String supportedDynamicOTDType : globalRarRegistry.getRar(rarName).getSupportedDynamicOTDTypes()) {
                            if (supportedDynamicOTDType.equals(otdLink.getType())) {
                                filteredOTDLinks.add(otdLink);
                                break;
                            }
                        }
                    }
                }
            } else {
                isStaticOTD = true;
            }

        }

        @Override
        public int getColumnCount() {
            if (rarName == null) {
                return 0;
            }
            if (isStaticOTD) {
                return columnNamesStaticOTD.length;
            } else {
                return columnNamesOTDLink.length;
            }
        }

        @Override
        public String getColumnName(int column) {
            if (rarName == null) {
                return null;
            }
            if (isStaticOTD) {
                return columnNamesStaticOTD[column];
            } else {
                return columnNamesOTDLink[column];
            }
        }

        @Override
        public int getRowCount() {
            if (rarName == null) {
                return 0;
            }
            if (isStaticOTD) {
                return globalRarRegistry.getRar(rarName).getOTDTypes().size();
            } else {
                return filteredOTDLinks.size();
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (rarName == null) {
                return null;
            }
            if (isStaticOTD) {
                return globalRarRegistry.getRar(rarName).getOTDTypes().get(row);
            } else {
                OTDLink otdLink = filteredOTDLinks.get(row);
                switch (column) {
                    case 0:
                        return otdLink.getName();
                    case 1:
                        return otdLink.getPath();
                    case 2:
                        return otdLink.getRootClass();
                    default:
                        return null;
                }
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public String getSelectedOTDType(int row) {
            if (rarName == null) {
                return null;
            }
            if (isStaticOTD) {
                return globalRarRegistry.getRar(rarName).getOTDTypes().get(row);
            } else {
                OTDLink otdLink = filteredOTDLinks.get(row);
                return otdLink.getRootClass();
            }
        }
    }


    class SelectedOTDTableModel extends DefaultTableModel {

        private String[] columnNames = new String[] { "Type" };
        private List<SelectedOTD> data = new ArrayList<SelectedOTD> ();

        public SelectedOTDTableModel(int size) {
            System.out.println("data = " + data);
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public int getRowCount() {
            if (data == null) {
                return 0;
            }
            return data.size();
        }

        @Override
        public Object getValueAt(int row, int column) {
            SelectedOTD selectedOTD = data.get(row);
            switch (column) {
                case 0:
                    return selectedOTD.type;
                default:
                    return null;
            }
        }

        @Override
        public void addRow(Object[] rowData) {
            data.add(new SelectedOTD((String) rowData[0]));
            this.fireTableDataChanged();
        }

        @Override
        public void removeRow(int row) {
            data.remove(row);
            this.fireTableDataChanged();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public List<SelectedOTD> getSelectedOTDs() {
            return Collections.unmodifiableList(data);
        }

    }

    class SelectedOTD {

        private String type;

        public SelectedOTD(String type) {
            this.type = type;
        }

    }

    public void valueChanged(ListSelectionEvent e) {
        setButtonStatus();
    }

    private void setButtonStatus() {
        if (selectedOtdTable.getRowCount() < 1) {
            if (otdTable.getSelectedRow() == -1) {
                addButton.setEnabled(false);
            } else {
                addButton.setEnabled(true);
            }
        } else {
            addButton.setEnabled(false);
        }

        if (selectedOtdTable.getSelectedRow() == -1) {
            removeButton.setEnabled(false);
        } else {
            removeButton.setEnabled(true);
        }

    }
}

