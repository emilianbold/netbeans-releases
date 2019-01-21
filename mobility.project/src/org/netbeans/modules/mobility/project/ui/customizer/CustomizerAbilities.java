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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.GlobalAbilitiesCache;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public class CustomizerAbilities extends JPanel implements CustomizerPanel, VisualPropertyGroup, ListSelectionListener {
    
    static final String[] PROPERTY_GROUP = new String[] { DefaultPropertiesDescriptor.ABILITIES };
    
    final protected JTable table;
    final protected StorableTableModel tableModel;
    private VisualPropertySupport vps;
    private ProjectProperties props;
    
    /** Creates new form CustomizerConfigs */
    public CustomizerAbilities() {
        initComponents();
        initAccessibility();
        table = new JTable(tableModel = new StorableTableModel());
        scrollPane.setViewportView(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.addMouseListener(new MouseAdapter() {
            @SuppressWarnings("synthetic-access")
			public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2  &&  e.getButton() == MouseEvent.BUTTON1)
                    bEditActionPerformed(null);
            }
        });
        //just to init abilities scan here
        GlobalAbilitiesCache.getDefault();
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

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(cDefault, org.openide.util.NbBundle.getMessage(CustomizerAbilities.class, "LBL_Use_Default")); // NOI18N
        cDefault.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(cDefault, gridBagConstraints);

        lTable.setLabelFor(lTable);
        org.openide.awt.Mnemonics.setLocalizedText(lTable, org.openide.util.NbBundle.getMessage(CustomizerAbilities.class, "LBL_Abilities_Abilities")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 4);
        add(lTable, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(scrollPane, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bAdd, org.openide.util.NbBundle.getMessage(CustomizerAbilities.class, "LBL_Abilities_AddAbility")); // NOI18N
        bAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 11, 0);
        add(bAdd, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bEdit, org.openide.util.NbBundle.getMessage(CustomizerAbilities.class, "LBL_Abilities_EditAbility")); // NOI18N
        bEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(bEdit, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(bRemove, org.openide.util.NbBundle.getMessage(CustomizerAbilities.class, "LBL_Abilities_RemoveAbility")); // NOI18N
        bRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRemoveActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(bRemove, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerAbilities.class, "ACSN_CustAbilities"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerAbilities.class, "ACSD_CustAbilities"));
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        this.props = props;
        vps = VisualPropertySupport.getDefault(props);
        vps.register(cDefault, configuration, this);
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    public void initGroupValues(final boolean useDefault) {
        vps.register(tableModel, new String[] {DefaultPropertiesDescriptor.ABILITIES}, useDefault);
        final boolean notUseDefault=useDefault ^ true;
        bAdd.setEnabled(notUseDefault);
        bEdit.setEnabled(notUseDefault);
        bRemove.setEnabled(notUseDefault);
        table.setEnabled(notUseDefault);
        lTable.setEnabled(notUseDefault);
        table.setBackground(javax.swing.UIManager.getDefaults().getColor(useDefault ? "Panel.background" : "Table.background")); //NOI18N
        valueChanged(null);
    }
    
    private Set<String> getUsedIdentifiers() {
        final Set<String> uI = new HashSet<String>(tableModel.getKeys());
        final ProjectConfiguration cfg[] = props.getConfigurations();
        for (int i=0; i<cfg.length; i++) uI.add(cfg[i].getDisplayName());
        return uI;
    }
    
    private void bAddActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddActionPerformed
        final AddAbilityPanel add = new AddAbilityPanel();
        final Set<String> usedIdentifiers = getUsedIdentifiers();
        final Vector<String> proposedAbilities = new Vector<String>(GlobalAbilitiesCache.getDefault().getAllAbilities());
        proposedAbilities.removeAll(usedIdentifiers);
        add.init(false, proposedAbilities, usedIdentifiers, null, null);
        final DialogDescriptor dd = new DialogDescriptor(
                add, NbBundle.getMessage(CustomizerAbilities.class, "TITLE_AddAbility"), //NOI18N
                true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(AddAttributePanel.class),
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                    String key = add.getKey();
                    GlobalAbilitiesCache.getDefault().addAbility(key);
                    int row = tableModel.addRow(key, add.getValue());
                    table.getSelectionModel().setSelectionInterval(row, row);
                }
            }
        }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bAddActionPerformed
    
    private void bEditActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEditActionPerformed
        final int row = table.getSelectedRow();
        if (row < 0)
            return;
        final String key = (String) tableModel.getValueAt(row, 0);
        final String value = (String) tableModel.getValueAt(row, 1);
        final AddAbilityPanel add = new AddAbilityPanel();
        final Set<String> usedIdentifiers = getUsedIdentifiers();
        final Vector<String> proposedAbilities = new Vector<String>(GlobalAbilitiesCache.getDefault().getAllAbilities());
        proposedAbilities.removeAll(usedIdentifiers);
        if (key != null) proposedAbilities.add(key);
        add.init(true, proposedAbilities, usedIdentifiers, key, value);
        final DialogDescriptor dd = new DialogDescriptor(
                add, NbBundle.getMessage(CustomizerAbilities.class, "TITLE_EditAbility"), //NOI18N
                true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(AddAttributePanel.class),
                new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (NotifyDescriptor.OK_OPTION.equals(e.getSource())) {
                    String newkey = add.getKey();
                    GlobalAbilitiesCache.getDefault().addAbility(newkey);
                    if (key == null  ||  ! key.equals(newkey)) {
                        if (key != null)
                            tableModel.removeRow(row);
                        int newrow = tableModel.addRow(newkey, add.getValue());
                        table.getSelectionModel().setSelectionInterval(newrow, newrow);
                    } else {
                        tableModel.editRow(newkey, add.getValue());
                        table.getSelectionModel().setSelectionInterval(row, row);
                    }
                }
            }
        }
        );
        add.setDialogDescriptor(dd);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }//GEN-LAST:event_bEditActionPerformed
    
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
    
    public void valueChanged(@SuppressWarnings("unused") javax.swing.event.ListSelectionEvent e) {
        final int row = table.getSelectedRow();
        final boolean enabled = table.isEnabled()  &&  row >= 0;
        bEdit.setEnabled(enabled);
        bRemove.setEnabled(enabled);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAdd;
    private javax.swing.JButton bEdit;
    private javax.swing.JButton bRemove;
    private javax.swing.JCheckBox cDefault;
    private javax.swing.JLabel lTable;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    
    public static class StorableTableModel extends AbstractTableModel implements VisualPropertySupport.StorableTableModel {
        
        private ArrayList<Map.Entry<String,String>> entries = new ArrayList<Map.Entry<String,String>>();
        private HashMap<String,String> map = new HashMap<String,String>();
        private static final long serialVersionUID = -2195421895353167171L;
                
        
        public int getRowCount() {
            return entries.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public boolean isCellEditable(@SuppressWarnings("unused")
		final int rowIndex, @SuppressWarnings("unused")
		final int columnIndex) {
            return false;
        }
        
        public Set<String> getKeys() {
            return map.keySet();
        }
        
        public String getColumnName(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return NbBundle.getMessage(CustomizerAbilities.class, "LBL_Abilities_Column_Ability"); //NOI18N
                case 1:
                    return NbBundle.getMessage(CustomizerAbilities.class, "LBL_Abilities_Column_Value"); //NOI18N
                default:
                    return null;
            }
        }
        
        public Class<?> getColumnClass(final int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                default:
                    return null;
            }
        }
        
        public synchronized Object[] getDataDelegates() {
            return new Object[] {map};
        }
        
        public String getValueAt(final int rowIndex, final int columnIndex) {
            assert rowIndex < entries.size();
            final Map.Entry<String,String> e = entries.get(rowIndex);
            return columnIndex == 0 ? e.getKey() : (e.getValue() == null ? "" : e.getValue());
        }
        
                 
        private static class AbilitiesComparator implements Comparator<Map.Entry<String,String>>
        {
            public int compare(Map.Entry<String,String> o1, Map.Entry<String,String> o2)
            {
                return o1.getKey().compareToIgnoreCase(o2.getKey());
            }

            public boolean equals(Object obj)
            {
                return this.equals(obj);
            }
        }
        
        
        public synchronized void setDataDelegates(final Object data[]) {
            assert data != null;
            map = data[0] == null ? new HashMap<String,String>() : (HashMap<String,String>) data[0];
            entries = new ArrayList<Map.Entry<String,String>>(map.entrySet());
            Collections.sort(entries,new AbilitiesComparator());
            fireTableDataChanged();
        }
        
        public int addRow(final String key, final String value) {
            if (map.containsKey(key)) return -1;
            map.put(key, value);
            Map.Entry e;
            final Iterator it = map.entrySet().iterator();
            while (!key.equals((e = (Map.Entry)it.next()).getKey()));
            final int row = entries.size();
            entries.add(e);
            fireTableRowsInserted(row, row);
            return row;
        }
        
        public void editRow(final String key, final String value) {
            map.put(key, value);
            for (int i=0; i<entries.size(); i++) {
                if (entries.get(i).getKey().equals(key)) {
                    fireTableRowsUpdated(i, i);
                    return;
                }
            }
        }
        
        public void removeRow(final int row) {
            assert row < entries.size();
            final Map.Entry e = entries.remove(row);
            map.entrySet().remove(e);
            fireTableRowsDeleted(row, entries.size() + 1);            
        }
        
    }
    
}
