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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 * @author Roman Svitanic
 */
public class J2MEAPIPermissionsPanel extends javax.swing.JPanel {
    
    protected JTable table;
    private final StorableTableModel tableModel;

    private final J2MEProjectProperties uiProperties;
    private final ListSelectionListener listSelectionListener;

    /**
     * Creates new form J2MEAPIPermissionsPanel
     */
    public J2MEAPIPermissionsPanel(J2MEProjectProperties uiProperties) {
        this.uiProperties = uiProperties;
        initComponents();
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "ACSN_Perm"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "ACSD_Perm"));
        tableModel = this.uiProperties.API_PERMISSIONS_TABLE_MODEL;
        table = new JTable(tableModel);
        scrollPane.setViewportView(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                bRemove.setEnabled(table.isEnabled()  &&  table.getSelectedRow() >= 0);
                bEdit.setEnabled(table.isEnabled()  &&  table.getSelectedRow() >= 0);
            }
        };
        table.getSelectionModel().addListSelectionListener(listSelectionListener);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    bEditActionPerformed(null);
                }
            }
        });
        TableColumn col0 = table.getColumnModel().getColumn(0);
        TableColumn col1 = table.getColumnModel().getColumn(1);
        col0.setResizable(true);
        col0.setPreferredWidth(300);
        col1.setResizable(true);
        col1.setPreferredWidth(80);
        postInitComponents();
    }
    
    private void postInitComponents() {
        String[] propertyNames = uiProperties.API_PERMISSIONS_PROPERTY_NAMES;
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
        bRemove = new javax.swing.JButton();
        bEdit = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lTable, org.openide.util.NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "J2MEAPIPermissionsPanel.lTable.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 6);
        add(lTable, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 8, 0);
        add(scrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bAdd, org.openide.util.NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "J2MEAPIPermissionsPanel.bAdd.text")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 5, 8);
        add(bAdd, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bRemove, org.openide.util.NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "J2MEAPIPermissionsPanel.bRemove.text")); // NOI18N
        bRemove.setEnabled(false);
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 5, 8);
        add(bRemove, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bEdit, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2me/project/ui/customizer/Bundle").getString("J2MEAPIPermissionsPanel.bEdit.text"), new Object[] {})); // NOI18N
        bEdit.setEnabled(false);
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
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 5, 8);
        add(bEdit, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void bAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        J2MEPlatform selectedPlatform = (J2MEPlatform) uiProperties.J2ME_PLATFORM_MODEL.getSelectedItem();
        if (selectedPlatform != null) {
            File libsDir = new File(selectedPlatform.getHomePath() + File.separator + "lib"); //NOI18N
            final AddPermissionPanel add = new AddPermissionPanel(null, new PermissionsProvider(libsDir), tableModel.getKeys(), null);
            final DialogDescriptor dd = new DialogDescriptor(add, NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "TITLE_AddAPI"), //NOI18N
                    true, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                                PermissionsProvider.PermissionDefinition pd = add.getPermission();
                                if (pd != null) {
                                    int row = tableModel.addRow(pd.toString(), pd.isPermissionClass());
                                    table.getSelectionModel().setSelectionInterval(row, row);
                                }
                            }
                        }
                    });
            add.setDialogDescriptor(dd);
            final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
        }
    }//GEN-LAST:event_bAddActionPerformed

    private void bRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
        final int i = table.getSelectedRow();
        if (i < 0) {
            return;
        }
        tableModel.removeRow(i);
        final int max = tableModel.getRowCount();
        if (max <= 0) {
            table.getSelectionModel().clearSelection();
        } else if (i < max) {
            table.getSelectionModel().setSelectionInterval(i, i);
        } else {
            table.getSelectionModel().setSelectionInterval(max - 1, max - 1);
        }
    }//GEN-LAST:event_bRemoveActionPerformed

    private void bEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEditActionPerformed
        J2MEPlatform selectedPlatform = (J2MEPlatform) uiProperties.J2ME_PLATFORM_MODEL.getSelectedItem();
        final int selectedRow = table.getSelectedRow();
        final StorableTableModel.Item selectedPermission = (StorableTableModel.Item) tableModel.getValueAt(selectedRow, 0);
        if (selectedPlatform != null) {
            File libsDir = new File(selectedPlatform.getHomePath() + File.separator + "lib"); //NOI18N
            final AddPermissionPanel add = new AddPermissionPanel(null, new PermissionsProvider(libsDir), tableModel.getKeys(), selectedPermission.getName());
            final DialogDescriptor dd = new DialogDescriptor(add, NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "TITLE_EditAPI"), //NOI18N
                    true, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                                PermissionsProvider.PermissionDefinition pd = add.getPermission();
                                if (pd != null) {
                                    tableModel.updateRow(selectedRow, pd.toString(), pd.isPermissionClass());
                                    table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                                }
                            }
                        }
                    });
            add.setDialogDescriptor(dd);
            final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
        }
    }//GEN-LAST:event_bEditActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bEdit;
    private javax.swing.JButton bRemove;
    private javax.swing.JLabel lTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    static class StorableTableModel extends AbstractTableModel {
        
        static class Item {
            
            private String name;
            private boolean required;
            private boolean permissionClass;
            
            public Item(String name, boolean required, boolean permissionClass) {
                this.name = name;
                this.required = required;
                this.permissionClass = permissionClass;
            }
            
            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public boolean isRequired() {
                return required;
            }
            
            public void setRequired(final boolean required) {
                this.required = required;
            }

            public boolean isPermissionClass() {
                return permissionClass;
            }

            public void setPermissionClass(boolean permissionClass) {
                this.permissionClass = permissionClass;
            }
            
            @Override
            public String toString() {
                return name;
            }
        }

        private HashMap<String,String> map = new HashMap<>();
        private HashMap<String,String> mapClassPerm = new LinkedHashMap<>();
        final private ArrayList<Item> items = new ArrayList<>();
        
        private static final long serialVersionUID = -6523408202243150812L;
        private final J2MEProjectProperties uiProperties;
        private boolean dataDelegatesWereSet = false;

        public StorableTableModel(J2MEProjectProperties uiProperties) {
            this.uiProperties = uiProperties;
        }
        
        public HashSet<String> getKeys() {
            final HashSet<String> set = new HashSet<>();
            for (int a = 0; a < items.size(); a++) {
                set.add(items.get(a).getName());
            }
            return set;
        }
        
        @Override
        public int getRowCount() {
            return items.size();
        }
        
        @Override
        public int getColumnCount() {
            return 2;
        }
        
        @Override
        public boolean isCellEditable(@SuppressWarnings("unused")
		final int rowIndex, final int columnIndex) {
            return columnIndex == 1;
        }
        
        @Override
        public String getColumnName(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "LBL_Perm_Column_API"); //NOI18N
                case 1:
                    return NbBundle.getMessage(J2MEAPIPermissionsPanel.class, "LBL_Perm_Column_Required"); //NOI18N
                default:
                    return null;
            }
        }
        
        @Override
		public Class<?> getColumnClass(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Boolean.class;
                default:
                    return null;
            }
        }
        
        public synchronized Object[] getDataDelegates() {
            if (!dataDelegatesWereSet) {
                String[] propertyNames = uiProperties.API_PERMISSIONS_PROPERTY_NAMES;
                String values[] = new String[propertyNames.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = uiProperties.getEvaluator().getProperty(propertyNames[i]);
                }
                setDataDelegates(values);
            }
            updateMapFromItems();
            return new Object[]{map, mapClassPerm};
        }
        
        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            assert rowIndex < items.size();
            switch (columnIndex) {
                case 0:
                    return items.get(rowIndex);
                case 1:
                    return Boolean.valueOf(items.get(rowIndex).isRequired());
                default:
                    return null;
            }
        }
        
        @Override
        public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
            assert columnIndex == 1  &&  value instanceof Boolean;
            items.get(rowIndex).setRequired(((Boolean) value).booleanValue());
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
        
	public synchronized void setDataDelegates(final String data[]) {
            assert data != null;
            map = data[0] == null ? new HashMap<String,String>() : (HashMap<String,String>) uiProperties.decode(data[0]);
            mapClassPerm = data[1] == null ? new LinkedHashMap<String,String>() : (HashMap<String,String>) uiProperties.decode(data[1]);
            updateItemsFromMap();
            fireTableDataChanged();
            dataDelegatesWereSet = true;
        }
        
        public void updateItemsFromMap() {
            items.clear();
            String perms;
            StringTokenizer tokens;
            
            String packaging = uiProperties.LIBLET_PACKAGING == false ? "MIDlet" : "LIBlet"; //NOI18N
            perms = map.get(packaging + "-Permissions"); //NOI18N
            if (perms != null) {
                tokens = new StringTokenizer(perms, ","); //NOI18N
                while (tokens.hasMoreTokens())
                    items.add(new Item(tokens.nextToken().trim(), true, false));
            }
            perms = map.get(packaging + "-Permissions-Opt"); //NOI18N
            if (perms != null) {
                tokens = new StringTokenizer(perms, ","); //NOI18N
                while (tokens.hasMoreTokens())
                    items.add(new Item(tokens.nextToken().trim(), false, false));
            }
            
            int i = 1;
            while ((perms = mapClassPerm.get(packaging + "-Permission-" + i)) != null) {
                items.add(new Item(perms.trim(), true, true));
                i++;
            }
            i = 1;
            while ((perms = mapClassPerm.get(packaging + "-Permission-Opt-" + i)) != null) {
                items.add(new Item(perms.trim(), false, true));
                i++;
            }
        }
        
        public void updateMapFromItems() {
            final ArrayList<String> reqClass = new ArrayList<>();
            final ArrayList<String> optClass = new ArrayList<>();
            final ArrayList<String> reqNotClass = new ArrayList<>();
            final ArrayList<String> optNotClass = new ArrayList<>();
            for (int a = 0; a < items.size(); a++) {
                final Item i = items.get(a);
                if (i.isPermissionClass()) {
                    if (i.isRequired()) {
                        reqClass.add(i.getName());
                    } else {
                        optClass.add(i.getName());
                    }
                } else {
                    if (i.isRequired()) {
                        reqNotClass.add(i.getName());
                    } else {
                        optNotClass.add(i.getName());
                    }
                }
            }

            String packaging = uiProperties.LIBLET_PACKAGING == false ? "MIDlet" : "LIBlet"; //NOI18N
            map = new HashMap<>();
            if (!reqNotClass.isEmpty()) {
                map.put(packaging + "-Permissions", commaSeparatedList(reqNotClass)); //NOI18N
            }
            if (!optNotClass.isEmpty()) {
                map.put(packaging + "-Permissions-Opt", commaSeparatedList(optNotClass)); //NOI18N
            }
            mapClassPerm = new LinkedHashMap<>();
            if (!reqClass.isEmpty()) {
                for (int i = 0; i < reqClass.size(); i++) {
                    mapClassPerm.put(packaging + "-Permission-" + (i + 1), reqClass.get(i)); //NOI18N
                }
            }
            if (!optClass.isEmpty()) {
                for (int i = 0; i < optClass.size(); i++) {
                    mapClassPerm.put(packaging + "-Permission-Opt-" + (i + 1), optClass.get(i)); //NOI18N
                }
            }
        }
        
        public String commaSeparatedList(final ArrayList<String> list) {
            final StringBuffer sb = new StringBuffer();
            boolean first = true;
            if (list != null) for (int a = 0; a < list.size(); a ++) {
                if (first)
                    first = false;
                else
                    sb.append(", "); //NOI18N
                sb.append(list.get(a));
            }
            return sb.toString();
        }
        
        public int addRow(final String name, final boolean permissionClass) {
            final int row = items.size();
            items.add(new Item(name, true, permissionClass));
            fireTableRowsInserted(row, row);
            return row;
        }

        public void updateRow(final int row, final String name, final boolean permissionClass) {
            assert row < items.size();
            items.get(row).setName(name);
            items.get(row).setPermissionClass(permissionClass);
            fireTableRowsUpdated(row, row);
        }

        public void removeRow(final int row) {
            assert row < items.size();
            items.remove(row);
            fireTableRowsDeleted(row, items.size() + 1);
        }
        
    }

}
