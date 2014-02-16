/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.j2me.project.ui.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class J2MEPushRegistryPanel extends javax.swing.JPanel {
    
    final protected JTable table;
    final private StorableTableModel tableModel;
    final private DefaultComboBoxModel cbmClassesForAdd = new DefaultComboBoxModel();
    
    protected HashSet<String> classes;

    private final J2MEProjectProperties uiProperties;
    private final ListSelectionListener listSelectionListener;
    private final ChangeListener changeListener;

    /**
     * Creates new form J2MEPushRegistryPanel
     */
    public J2MEPushRegistryPanel(J2MEProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        initComponents();
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(J2MEPushRegistryPanel.class, "ACSN_Push"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MEPushRegistryPanel.class, "ACSD_Push"));
        tableModel = this.uiProperties.PUSH_REGISTRY_TABLE_MODEL;
        table = new JTable(tableModel);
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tme) {
                if (classes != null && !classes.containsAll(tableModel.getClasses())) {
                    ((ErrorPanel) errorPanel).setErrorBundleMessage("ERR_CustMIDlets_WrongMIDlets"); //NOI18N
                }
            }
        });
        scrollPane.setViewportView(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                final boolean enabled = table.isEnabled() && table.getSelectedRow() >= 0;
                bEdit.setEnabled(enabled);
                bRemove.setEnabled(enabled);
                bMoveUp.setEnabled(enabled && table.getSelectedRow() > 0);
                bMoveDown.setEnabled(enabled && table.getSelectedRow() < tableModel.getRowCount() - 1);
            }
        };
        table.getSelectionModel().addListSelectionListener(listSelectionListener);
        TableColumn col;
        col = table.getColumnModel().getColumn(0);
        col.setResizable(true);
        col.setPreferredWidth(150);
        col = table.getColumnModel().getColumn(1);
        col.setResizable(true);
        col.setPreferredWidth(100);
        col = table.getColumnModel().getColumn(2);
        col.setResizable(true);
        col.setPreferredWidth(200);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setForeground(isSelected ? table.getSelectionForeground() : (column == 0  &&  classes != null  &&  !classes.contains(value) ? Color.RED : table.getForeground()));
                return component;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("synthetic-access")
			public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2  &&  e.getButton() == MouseEvent.BUTTON1)
                    bEditActionPerformed(null);
            }
        });
        changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                classes = new HashSet<>();
                for (int i = 0; i < cbmClassesForAdd.getSize(); i++) {
                    classes.add((String) cbmClassesForAdd.getElementAt(i));
                }
                tableModel.fireTableDataChanged();
            }
        };
        postInitComponents();
    }
    
    private void postInitComponents() {
        classes = null;
        final MIDletScanner scanner = MIDletScanner.getDefault(uiProperties);
        scanner.scan(cbmClassesForAdd, null, changeListener);
        String[] propertyNames = uiProperties.PUSH_REGISTRY_PROPERTY_NAMES;
        String values[] = new String[propertyNames.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = uiProperties.getEvaluator().getProperty(propertyNames[i]);
        }
        tableModel.setDataDelegates(values);
        table.setBackground(UIManager.getDefaults().getColor("Table.background")); //NOI18N
        listSelectionListener.valueChanged(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lTable = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        bAdd = new javax.swing.JButton();
        bEdit = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        bMoveUp = new javax.swing.JButton();
        bMoveDown = new javax.swing.JButton();
        errorPanel = new ErrorPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lTable, org.openide.util.NbBundle.getMessage(J2MEPushRegistryPanel.class, "J2MEPushRegistryPanel.lTable.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        add(lTable, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 0);
        add(scrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bAdd, org.openide.util.NbBundle.getMessage(J2MEPushRegistryPanel.class, "J2MEPushRegistryPanel.bAdd.text")); // NOI18N
        bAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 8);
        add(bAdd, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bEdit, org.openide.util.NbBundle.getMessage(J2MEPushRegistryPanel.class, "J2MEPushRegistryPanel.bEdit.text")); // NOI18N
        bEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 8);
        add(bEdit, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bRemove, org.openide.util.NbBundle.getMessage(J2MEPushRegistryPanel.class, "J2MEPushRegistryPanel.bRemove.text")); // NOI18N
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 8);
        add(bRemove, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bMoveUp, org.openide.util.NbBundle.getMessage(J2MEPushRegistryPanel.class, "J2MEPushRegistryPanel.bMoveUp.text")); // NOI18N
        bMoveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMoveUpActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 8);
        add(bMoveUp, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bMoveDown, org.openide.util.NbBundle.getMessage(J2MEPushRegistryPanel.class, "J2MEPushRegistryPanel.bMoveDown.text")); // NOI18N
        bMoveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bMoveDownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 8);
        add(bMoveDown, gridBagConstraints);

        javax.swing.GroupLayout errorPanelLayout = new javax.swing.GroupLayout(errorPanel);
        errorPanel.setLayout(errorPanelLayout);
        errorPanelLayout.setHorizontalGroup(
            errorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        errorPanelLayout.setVerticalGroup(
            errorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 8);
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void bAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        final AddPushRegistryPanel add = new AddPushRegistryPanel(cbmClassesForAdd);
        final DialogDescriptor dd = new DialogDescriptor(
            add, NbBundle.getMessage(J2MEPushRegistryPanel.class, "TITLE_AddPush"), //NOI18N
            true, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                        int row = tableModel.addRow(add.getClazz(), add.getSender(), add.getString());
                        table.getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bAddActionPerformed

    private void bEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEditActionPerformed
        final int row = table.getSelectedRow();
        if (row < 0)
        return;
        final AddPushRegistryPanel add = new AddPushRegistryPanel(cbmClassesForAdd, (String) tableModel.getValueAt(row, 0), (String) tableModel.getValueAt(row, 1), (String) tableModel.getValueAt(row, 2));
        final DialogDescriptor dd = new DialogDescriptor(
            add, NbBundle.getMessage(J2MEPushRegistryPanel.class, "TITLE_EditPush"),
            true, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                        tableModel.setRow(row, add.getClazz(), add.getSender(), add.getString());
                        table.getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bEditActionPerformed

    private void bRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        final int i = table.getSelectedRow();
        if (i < 0)
        return;
        tableModel.removeRow(i);
        final int max = tableModel.getRowCount();
        if (max <= 0)
        table.getSelectionModel().clearSelection();
        else if (i < max)
        table.getSelectionModel().setSelectionInterval(i, i);
        else
        table.getSelectionModel().setSelectionInterval(max - 1, max - 1);
    }//GEN-LAST:event_bRemoveActionPerformed

    private void bMoveUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMoveUpActionPerformed
        int i = table.getSelectedRow();
        if (i <= 0)
        return;
        tableModel.moveUp(i);
        i --;
        table.getSelectionModel().setSelectionInterval(i, i);
    }//GEN-LAST:event_bMoveUpActionPerformed

    private void bMoveDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMoveDownActionPerformed
        int i = table.getSelectedRow();
        if (i >= tableModel.getRowCount() - 1)
        return;
        tableModel.moveDown(i);
        i ++;
        table.getSelectionModel().setSelectionInterval(i, i);
    }//GEN-LAST:event_bMoveDownActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bEdit;
    private javax.swing.JButton bMoveDown;
    private javax.swing.JButton bMoveUp;
    private javax.swing.JButton bRemove;
    private javax.swing.JPanel errorPanel;
    private javax.swing.JLabel lTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    static class StorableTableModel extends AbstractTableModel {
        
        public static final String PREFIX = "MIDlet-Push-"; //NOI18N
        
        private static final int[] COLUMN_MAP = new int[] {1, 2, 0}; // class, sender, string
        
        private HashMap<String,String> map = new HashMap<String,String>();
        private int rows = 0;
        
        private static final long serialVersionUID = 920375326055211848L;
        private final J2MEProjectProperties uiProperties;
        private boolean dataDelegatesWereSet = false;
        
        public StorableTableModel(J2MEProjectProperties uiProperties) {
            this.uiProperties = uiProperties;
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public int getRowCount() {
            return rows;
        }
        
        public Set<String> getClasses() {
            final HashSet<String> classes = new HashSet<String>();
            for (int i=0; i<rows; i++)
                classes.add((String)getValueAt(i, 0));
            return classes;
        }
        
        public String getColumnName(final int column) {
            switch (column) {
                case 0: return NbBundle.getMessage(J2MEPushRegistryPanel.class, "LBL_Push_Column_Class"); //NOI18N
                case 1: return NbBundle.getMessage(J2MEPushRegistryPanel.class, "LBL_Push_Column_Sender"); //NOI18N
                case 2: return NbBundle.getMessage(J2MEPushRegistryPanel.class, "LBL_Push_Column_String"); //NOI18N
                default: return null;
            }
        }
        
        public int addRow(final String clazz, final String sender, final String string) {
            rows++;
            setRow(rows-1, string + ',' + clazz + ',' + sender);
            fireTableRowsInserted(rows-1, rows-1);
            return rows - 1;
        }
        
        private String getRow(final int row) {
            assert row < rows;
            final String value = map.get(PREFIX + String.valueOf(row + 1));
            return value == null ? ",," : value; //NOI18N
        }
        
        private void setRow(final int row, final String value) {
            assert row < rows;
            map.put(PREFIX + String.valueOf(row + 1), value);
        }
        
        public void removeRow(final int row) {
            for (int i=row; i < rows - 1; i++) {
                setRow(i, getRow(i+1));
            }
            map.remove(PREFIX + String.valueOf(rows));
            rows--;
            fireTableRowsDeleted(row, row);
        }
        
        public void moveUp(final int row) {
            final String r = getRow(row);
            setRow(row, getRow(row - 1));
            setRow(row - 1, r);
            fireTableRowsUpdated(row - 1, row);
        }
        
        public void moveDown(final int row) {
            final String r = getRow(row);
            setRow(row, getRow(row + 1));
            setRow(row + 1, r);
            fireTableRowsUpdated(row, row + 1);
        }
        
        public Object getValueAt(final int row, final int column) {
            assert column < 3;
            return getRow(row).split(",", 3)[COLUMN_MAP[column]].trim(); //NOI18N
        }
        
        public synchronized Object[] getDataDelegates() {
            if (!dataDelegatesWereSet) {
                String[] propertyNames = uiProperties.PUSH_REGISTRY_PROPERTY_NAMES;
                String values[] = new String[propertyNames.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = uiProperties.getEvaluator().getProperty(propertyNames[i]);
                }
                setDataDelegates(values);
            }
            return new Object[]{map};
        }
        
	public synchronized void setDataDelegates(final String data[]) {
            map = data[0] == null ? new HashMap<String,String>() : (HashMap<String,String>) uiProperties.decode(data[0]);
            rows = 0;
            for ( final String key : map.keySet() ) {
                if (key.startsWith(PREFIX)) try { //NOI18N
                    final int i = Integer.parseInt(key.substring(PREFIX.length()));
                    if (i > rows) rows = i;
                } catch (NumberFormatException nfe) {
                    assert false: nfe;
                }
            }
            fireTableDataChanged();
            dataDelegatesWereSet = true;
        }
        
        public void setRow(final int row, final String clazz, final String sender, final String string) {
            setRow(row, string + ',' + clazz + ',' + sender);
            fireTableRowsUpdated(row, row);
        }
        
    }

}
