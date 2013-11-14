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
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class J2MEMIDletsPanel extends javax.swing.JPanel {

    final private DefaultComboBoxModel cbmClassesForAdd, cbmIconsForAdd;
    protected HashSet<String> classes, icons;
    final private MIDletsTableModel model;

    private final J2MEProjectProperties uiProperties;
    private final ListSelectionListener listSelectionListener;
    private final ChangeListener changeListener;

    /**
     * Creates new form J2MEMIDletsPanel
     */
    public J2MEMIDletsPanel(J2MEProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        model = this.uiProperties.MIDLETS_TABLE_MODEL;
        initComponents();
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(J2MEMIDletsPanel.class, "ACSN_CustMIDlets"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MEMIDletsPanel.class, "ACSD_CustMIDlets"));
        cbmClassesForAdd = new DefaultComboBoxModel();
        cbmIconsForAdd = new DefaultComboBoxModel();
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tme) {
                if (classes != null && !classes.containsAll(model.getClasses())) {
                    ((ErrorPanel) errorPanel).setErrorBundleMessage("ERR_CustMIDlets_WrongMIDlets"); //NOI18N
                } else if (icons != null && !icons.containsAll(model.getIcons())) {
                    ((ErrorPanel) errorPanel).setErrorBundleMessage("ERR_CustMIDlets_WrongIcons"); //NOI18N
                } else {
                    ((ErrorPanel) errorPanel).setErrorBundleMessage(null);
                }
            }
        });
        midletsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                final int i = midletsTable.getSelectedRow();
                final boolean enabled = midletsTable.isEnabled();
                final boolean selected = enabled && i >= 0;
                editButton.setEnabled(selected);
                removeButton.setEnabled(selected);
                upButton.setEnabled(enabled && i > 0);
                downButton.setEnabled(selected && i < model.getRowCount() - 1);
            }
        };
        midletsTable.getSelectionModel().addListSelectionListener(listSelectionListener);
        midletsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setForeground(isSelected ? table.getSelectionForeground() : (column == 1 && classes != null && !classes.contains(value) ? Color.RED : table.getForeground()));
                return component;
            }
        });
        midletsTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                component.setForeground(isSelected ? table.getSelectionForeground() : (column == 2 && icons != null && !icons.contains(value) ? Color.RED : table.getForeground()));
                return component;
            }
        });
        midletsTable.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    editButtonActionPerformed(null);
                }
            }
        });
        changeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                classes = new HashSet<>();
                for (int i = 0; i < cbmClassesForAdd.getSize(); i++) {
                    classes.add((String) cbmClassesForAdd.getElementAt(i));
                }
                icons = new HashSet<>();
                for (int i = 0; i < cbmIconsForAdd.getSize(); i++) {
                    icons.add((String) cbmIconsForAdd.getElementAt(i));
                }
                model.fireTableDataChanged();
            }
        };
        postInitComponents();
    }

    private void postInitComponents() {
        classes = icons = null;
        final MIDletScanner scanner = MIDletScanner.getDefault(uiProperties);
        scanner.scan(cbmClassesForAdd, cbmIconsForAdd, changeListener);

        String[] propertyNames = uiProperties.MIDLETS_PROPERTY_NAMES;
        String values[] = new String[propertyNames.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = uiProperties.getEvaluator().getProperty(propertyNames[i]);
        }
        model.setDataDelegates(values);

        midletsTable.setBackground(UIManager.getDefaults().getColor("Table.background")); //NOI18N
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

        tableLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        midletsTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        errorPanel = new ErrorPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(tableLabel, NbBundle.getMessage(J2MEMIDletsPanel.class, "J2MEMIDletsPanel.tableLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 0);
        add(tableLabel, gridBagConstraints);

        midletsTable.setModel(model);
        scrollPane.setViewportView(midletsTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 0);
        add(scrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, NbBundle.getMessage(J2MEMIDletsPanel.class, "J2MEMIDletsPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 8);
        add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, NbBundle.getMessage(J2MEMIDletsPanel.class, "J2MEMIDletsPanel.editButton.text")); // NOI18N
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 8);
        add(editButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(J2MEMIDletsPanel.class, "J2MEMIDletsPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 8);
        add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(J2MEMIDletsPanel.class, "J2MEMIDletsPanel.upButton.text")); // NOI18N
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 8);
        add(upButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(J2MEMIDletsPanel.class, "J2MEMIDletsPanel.downButton.text")); // NOI18N
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 12, 8);
        add(downButton, gridBagConstraints);

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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 8);
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        final HashSet<String> hs = new HashSet<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            hs.add(model.getValueAt(i, 2).trim());
        }
        String cname;
        int i = 0;
        while (i < cbmClassesForAdd.getSize() && hs.contains(cbmClassesForAdd.getElementAt(i))) {
            i++;
        }
        cname = i < cbmClassesForAdd.getSize() ? (String) cbmClassesForAdd.getElementAt(i) : null;
        if (cname != null) {
            cname = cname.trim();
        }
        final AddMIDletPanel p = new AddMIDletPanel(null, cname, null, cbmClassesForAdd, cbmIconsForAdd);
        final DialogDescriptor desc = new DialogDescriptor(p, NbBundle.getMessage(J2MEMIDletsPanel.class, "Title_CustMIDlets_AddMIDlet"), true,
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.j2me.project.ui.customizer.J2MEMIDletsPanel"), null); //NOI18N
        p.setDialogDescriptor(desc);
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            model.addRow(p.getName(), p.getClazz(), p.getIcon());
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        final int editedRow = midletsTable.getSelectedRow();
        if (editedRow < 0) return;
        final String s[] = model.getRow(editedRow).split(",", 3); //NOI18N
        final AddMIDletPanel p = new AddMIDletPanel(s[0].trim(), s[2].trim(), s[1].trim(), cbmClassesForAdd, cbmIconsForAdd);
        final DialogDescriptor desc = new DialogDescriptor(p, NbBundle.getMessage(J2MEMIDletsPanel.class, "Title_CustMIDlets_EditMIDlet"), true,
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.j2me.project.ui.customizer.J2MEMIDletsPanel"), null); //NOI18N
        p.setDialogDescriptor(desc);
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(desc))) {
            model.setRow(editedRow, p.getName(), p.getClazz(), p.getIcon());
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        final int editedRow = midletsTable.getSelectedRow();
        if (editedRow < 0) return;
        model.remove(editedRow);
    }//GEN-LAST:event_removeButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        final int editedRow = midletsTable.getSelectedRow();
        if (editedRow < 1) return;
        model.moveUp(editedRow);
        midletsTable.getSelectionModel().setSelectionInterval(editedRow - 1, editedRow - 1);
    }//GEN-LAST:event_upButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        final int editedRow = midletsTable.getSelectedRow();
        if (editedRow < 0 || editedRow > midletsTable.getRowCount()-2) return;
        model.moveDown(editedRow);
        midletsTable.getSelectionModel().setSelectionInterval(editedRow + 1, editedRow + 1);
    }//GEN-LAST:event_downButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton downButton;
    private javax.swing.JButton editButton;
    private javax.swing.JPanel errorPanel;
    private javax.swing.JTable midletsTable;
    private javax.swing.JButton removeButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel tableLabel;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    public static class MIDletsTableModel extends AbstractTableModel {

        public static final String PREFIX = "MIDlet-"; //NOI18N

        private HashMap<String, String> map = new HashMap<>();
        private int rows = 0;
        private static final int[] COLUMN_MAP = new int[]{0, 2, 1}; // name, class, icon
        private static final String[] COLUMN_NAMES = new String[]{NbBundle.getMessage(J2MEMIDletsPanel.class, "LBL_CustMIDletsName_Name"),
            NbBundle.getMessage(J2MEMIDletsPanel.class, "LBL_CustMIDlets_Class"), NbBundle.getMessage(J2MEMIDletsPanel.class, "LBL_CustMIDlets_Icon")};

        private static final long serialVersionUID = -7485135564331430899L;
        private final J2MEProjectProperties uiProperties;
        private boolean dataDelegatesWereSet = false;

        //NOI18N
        public MIDletsTableModel(J2MEProjectProperties uiProperties) {
            this.uiProperties = uiProperties;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return rows;
        }

        @Override
        public String getColumnName(final int column) {
            assert column < 3;
            return COLUMN_NAMES[column];
        }

        public Set<String> getClasses() {
            final HashSet<String> classes = new HashSet<>();
            for (int i = 0; i < rows; i++) {
                classes.add(getValueAt(i, 1));
            }
            return classes;
        }

        public Set<String> getIcons() {
            final HashSet<String> icons = new HashSet<>();
            for (int i = 0; i < rows; i++) {
                final String icon = getValueAt(i, 2);
                if (icon.length() > 0) {
                    icons.add(icon);
                }
            }
            return icons;
        }

        public void addRow(final String name, final String clazz, final String icon) {
            rows++;
            setRow(rows - 1, name + ',' + icon + ',' + clazz);
            fireTableRowsInserted(rows - 1, rows - 1);
        }

        protected String getRow(final int row) {
            assert row < rows;
            final String value = map.get(PREFIX + String.valueOf(row + 1));
            return value == null ? ",," : value; //NOI18N
        }

        private void setRow(final int row, final String value) {
            assert row < rows;
            map.put(PREFIX + String.valueOf(row + 1), value);
        }

        public void remove(final int row) {
            for (int i = row; i < rows - 1; i++) {
                setRow(i, getRow(i + 1));
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

        @Override
        public String getValueAt(final int row, final int column) {
            assert column < 3;
            return getRow(row).split(",", 3)[COLUMN_MAP[column]].trim(); //NOI18N
        }

        public synchronized Object[] getDataDelegates() {
            if (!dataDelegatesWereSet) {
                String[] propertyNames = uiProperties.MIDLETS_PROPERTY_NAMES;
                String values[] = new String[propertyNames.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = uiProperties.getEvaluator().getProperty(propertyNames[i]);
                }
                setDataDelegates(values);
            }
            return new Object[]{map};
        }

        public synchronized void setDataDelegates(final String[] data) {
            map = data[0] == null ? new HashMap<String, String>() : (HashMap<String, String>) uiProperties.decode(data[0]);
            rows = 0;
            for (final String key : map.keySet()) {
                if (key.startsWith(PREFIX)) {
                    try { //NOI18N
                        final int i = Integer.parseInt(key.substring(PREFIX.length()));
                        if (i > rows) {
                            rows = i;
                        }
                    } catch (NumberFormatException nfe) {
                        assert false : nfe;
                    }
                }
            }
            fireTableDataChanged();
            dataDelegatesWereSet = true;
        }

        public void setRow(final int row, final String name, final String clazz, final String icon) {
            setRow(row, name + ',' + icon + ',' + clazz);
            fireTableRowsUpdated(row, row);
        }

    }
}
