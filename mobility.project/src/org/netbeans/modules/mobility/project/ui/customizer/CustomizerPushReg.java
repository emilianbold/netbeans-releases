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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * Customizer.java
 *
 * Created on 23.Mar 2004, 11:31
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;

import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * 
 */
public class CustomizerPushReg extends JPanel implements CustomizerPanel, VisualPropertyGroup, ListSelectionListener, ChangeListener, TableModelListener {
    static final long serialVersionUID = 1191422396924194938L;
    static final String[] PROPERTY_GROUP = new String[] { DefaultPropertiesDescriptor.MANIFEST_PUSHREGISTRY };
    
    final protected JTable table;
    final protected StorableTableModel tableModel;
    final private DefaultComboBoxModel cbmClassesForAdd = new DefaultComboBoxModel();
    
    private VisualPropertySupport vps;
    private String configuration;
    private String configurationProfileValue;
    private String defaultProfileValue;
    protected HashSet<String> classes;
    
    /** Creates new form CustomizerConfigs */
    public CustomizerPushReg() {
        initComponents();
        initAccessibility();
        table = new JTable(tableModel = new StorableTableModel());
        tableModel.addTableModelListener(this);
        scrollPane.setViewportView(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
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
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cDefault = new javax.swing.JCheckBox();
        lTable = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        bAdd = new javax.swing.JButton();
        bEdit = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        bMoveUp = new javax.swing.JButton();
        bMoveDown = new javax.swing.JButton();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(cDefault, org.openide.util.NbBundle.getMessage(CustomizerPushReg.class, "LBL_Use_Default")); // NOI18N
        cDefault.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(cDefault, gridBagConstraints);

        lTable.setLabelFor(lTable);
        org.openide.awt.Mnemonics.setLocalizedText(lTable, org.openide.util.NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_Table")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(lTable, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(scrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bAdd, org.openide.util.NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_Add")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(bAdd, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bEdit, org.openide.util.NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_Edit")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(bEdit, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bRemove, org.openide.util.NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_Remove")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(bRemove, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bMoveUp, org.openide.util.NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_MoveUp")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
        add(bMoveUp, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bMoveDown, org.openide.util.NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_MoveDown")); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(bMoveDown, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerPushReg.class, "ACSN_Push"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerPushReg.class, "ACSD_Push"));
    }
    
    private void bEditActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEditActionPerformed
        final int row = table.getSelectedRow();
        if (row < 0)
            return;
        final AddPushRegistryPanel add = new AddPushRegistryPanel(cbmClassesForAdd, (String) tableModel.getValueAt(row, 0), (String) tableModel.getValueAt(row, 1), (String) tableModel.getValueAt(row, 2));
        final DialogDescriptor dd = new DialogDescriptor(
                add, NbBundle.getMessage(CustomizerPushReg.class, "TITLE_EditPush"),
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
    
    private void bAddActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        final AddPushRegistryPanel add = new AddPushRegistryPanel(cbmClassesForAdd);
        final DialogDescriptor dd = new DialogDescriptor(
                add, NbBundle.getMessage(CustomizerPushReg.class, "TITLE_AddPush"), //NOI18N
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
    
    private void bMoveDownActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMoveDownActionPerformed
        int i = table.getSelectedRow();
        if (i >= tableModel.getRowCount() - 1)
            return;
        tableModel.moveDown(i);
        i ++;
        table.getSelectionModel().setSelectionInterval(i, i);
    }//GEN-LAST:event_bMoveDownActionPerformed
    
    private void bMoveUpActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bMoveUpActionPerformed
        int i = table.getSelectedRow();
        if (i <= 0)
            return;
        tableModel.moveUp(i);
        i --;
        table.getSelectionModel().setSelectionInterval(i, i);
    }//GEN-LAST:event_bMoveUpActionPerformed
    
    private void bRemoveActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRemoveActionPerformed
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
    
    public void initValues(ProjectProperties props, String configuration) {
        this.configuration = configuration;
        configurationProfileValue = (String) props.get(VisualPropertySupport.translatePropertyName(configuration, "platform.profile", false)); //NOI18N
        defaultProfileValue = (String) props.get("platform.profile"); //NOI18N
        
        this.configuration = configuration;
        
        classes = null;
        final MIDletScanner scanner = MIDletScanner.getDefault(props);
        scanner.scan(cbmClassesForAdd, null, configuration, this);
        vps = VisualPropertySupport.getDefault(props);
        vps.register(cDefault, configuration, this);
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    private boolean isMIDP10() {
        String value = null;
        if (configuration != null)
            value = configurationProfileValue;
        if (value == null)
            value = defaultProfileValue;
        return value != null  &&  value.equals("MIDP-1.0"); //NOI18N
    }
    
    public void initGroupValues(final boolean useDefault) {
        vps.register(tableModel, new String[]{DefaultPropertiesDescriptor.MANIFEST_PUSHREGISTRY}, useDefault);
        bAdd.setEnabled(!useDefault);
        bEdit.setEnabled(!useDefault);
        bRemove.setEnabled(!useDefault);
        table.setEnabled(!useDefault);
        lTable.setEnabled(!useDefault);
        table.setBackground(UIManager.getDefaults().getColor(useDefault ?  "TextField.inactiveBackground" : "Table.background")); //NOI18N
        valueChanged(null);
    }
    
    public void tableChanged(@SuppressWarnings("unused")
	final TableModelEvent e) {
        if (classes != null && !classes.containsAll(tableModel.getClasses())) {
            errorPanel.setErrorBundleMessage("ERR_CustMIDlets_WrongMIDlets"); //NOI18N
        } else {
            errorPanel.setErrorBundleMessage(isMIDP10() ? "ERR_Push_NotUsed" : null); //NOI18N
        }
    }
    
    public void valueChanged(@SuppressWarnings("unused")
	final javax.swing.event.ListSelectionEvent e) {
        final boolean enabled = table.isEnabled()  &&  table.getSelectedRow() >= 0;
        bEdit.setEnabled(enabled);
        bRemove.setEnabled(enabled);
        bMoveUp.setEnabled(enabled  &&  table.getSelectedRow() > 0);
        bMoveDown.setEnabled(enabled  &&  table.getSelectedRow() < tableModel.getRowCount() - 1);
    }
  
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e) {
        classes = new HashSet<String>();
        for (int i=0; i<cbmClassesForAdd.getSize(); i++) classes.add((String)cbmClassesForAdd.getElementAt(i));
        tableModel.fireTableDataChanged();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bEdit;
    private javax.swing.JButton bMoveDown;
    private javax.swing.JButton bMoveUp;
    private javax.swing.JButton bRemove;
    private javax.swing.JCheckBox cDefault;
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JLabel lTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
    private static class StorableTableModel extends AbstractTableModel implements VisualPropertySupport.StorableTableModel {
        
        public static final String PREFIX = "MIDlet-Push-"; //NOI18N
        
        private static final int[] COLUMN_MAP = new int[] {1, 2, 0}; // class, sender, string
        
        private HashMap<String,String> map = new HashMap<String,String>();
        private int rows = 0;
        
        private static final long serialVersionUID = 920375326055211848L;
        
        private StorableTableModel()
        {
            //just to avoid creation of accessor class
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
                case 0: return NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_Column_Class"); //NOI18N
                case 1: return NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_Column_Sender"); //NOI18N
                case 2: return NbBundle.getMessage(CustomizerPushReg.class, "LBL_Push_Column_String"); //NOI18N
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
            return new Object[]{map};
        }
        
		public synchronized void setDataDelegates(final Object data[]) {
            map = data[0] == null ? new HashMap<String,String>() : (HashMap<String,String>) data[0];
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
        }
        
        public void setRow(final int row, final String clazz, final String sender, final String string) {
            setRow(row, string + ',' + clazz + ',' + sender);
            fireTableRowsUpdated(row, row);
        }
        
    }
    
}
