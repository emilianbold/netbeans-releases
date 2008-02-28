/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.websvc.customization.jaxwssettings.panel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author  rico
 */
public class WsimportOptionsPanel extends javax.swing.JPanel {

    private static final String[] reservedOptions = new String[]{"xendorsed", "verbose", "sourcedestdir",
        "extension", "destdir", "wsdl", "wsdlLocation", "catalog"
    };

    /** Creates new form WsimportOptionsPanel */
    public WsimportOptionsPanel(Map<String, String> optionMap) {
        List<Option> options = new ArrayList<Option>();
        Set<String> keys = optionMap.keySet();
        for (String key : keys) {
            options.add(new Option(key, optionMap.get(key)));
        }
        initComponents();
        addBtn.addActionListener(new AddButtonActionListener());
        removeBtn.addActionListener(new RemoveButtonActionListener());
        String[] columnNames = new String[]{NbBundle.getMessage(WsimportOptionsPanel.class, "HEADING_OPTION"),
            NbBundle.getMessage(WsimportOptionsPanel.class, "HEADING_VALUE")
        };
        optionsTableModel = new OptionsTableModel(columnNames, options);
        optionsTable.setModel(optionsTableModel);
        optionsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
        ListSelectionListener listSelectionListener = new ListSelectionListenerImpl();
        optionsTable.getSelectionModel().addListSelectionListener(listSelectionListener);
        optionsTable.getColumnModel().getSelectionModel().addListSelectionListener(listSelectionListener);
        updateButtons();
        setBackground(SectionVisualTheme.getDocumentBackgroundColor());
    }

    private void updateButtons() {
        boolean oneSelected = optionsTable.getSelectedRowCount() == 1;
        removeBtn.setEnabled(oneSelected);
    }

    public Map<String, String> getWsimportOptions() {
        Map<String, String> optionsMap = new HashMap<String, String>();
        List<Option> optionList = optionsTableModel.getOptions();
        for (Option op : optionList) {
            optionsMap.put(op.getName(), op.getValue());
        }
        return optionsMap;
    }

    public TableModel getOptionsTableModel() {
        return optionsTableModel;
    }

    class RemoveButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int selectedRow = getSelectedRow();
            String option = (String) optionsTableModel.getValueAt(selectedRow, 0);
            if (confirmDeletion(option)) {
                if (selectedRow > -1) {
                    optionsTableModel.removeOption(selectedRow);
                }
                if (selectedRow == optionsTable.getRowCount()) {
                    selectedRow--;
                }
                optionsTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            }
            updateButtons();
        }

        private boolean confirmDeletion(String option) {
            NotifyDescriptor.Confirmation notifyDesc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(WsimportOptionsPanel.class, "MSG_CONFIRM_DELETE", option), NbBundle.getMessage(WsimportOptionsPanel.class, "TTL_CONFIRM_DELETE"), NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return notifyDesc.getValue() == NotifyDescriptor.YES_OPTION;
        }
    }

    class AddButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            int index = optionsTableModel.addOption();
            optionsTable.getSelectionModel().setSelectionInterval(index, index);
            optionsTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
            updateButtons();
        }
    }

    class OptionsTable extends JTable {

        public OptionsTable() {
            JTableHeader header = getTableHeader();
            header.setResizingAllowed(false);
            header.setReorderingAllowed(false);
            ListSelectionModel model = getSelectionModel();
            model.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        optionsTable = new javax.swing.JTable();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/jaxwssettings/panel/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addBtn, bundle.getString("Add_DotDotDot_label")); // NOI18N
        addBtn.setToolTipText(org.openide.util.NbBundle.getMessage(WsimportOptionsPanel.class, "HINT_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, bundle.getString("Remove_label")); // NOI18N
        removeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(WsimportOptionsPanel.class, "HINT_Remove")); // NOI18N

        optionsTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jScrollPane2.setViewportView(optionsTable);
        optionsTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(WsimportOptionsPanel.class, "ACSD_WSIMPORT_OPTIONS")); // NOI18N
        optionsTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(WsimportOptionsPanel.class, "ACSD_Wsimport_OPTIONS_TABLE")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                    .add(removeBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(addBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeBtn))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private int getSelectedRow() {
        ListSelectionModel lsm = (ListSelectionModel) optionsTable.getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            return -1;
        } else {
            return lsm.getMinSelectionIndex();
        }
    }

    public static class Option {

        private String name;
        private String value;

        public Option(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    class OptionsTableModel extends DefaultTableModel {

        private List<Option> options;
        private String[] columnNames;

        public OptionsTableModel(Object[] columnNames, List<Option> options) {
            super(columnNames, options.size());
            this.columnNames = (String[]) columnNames;
            this.options = new ArrayList<Option>(options);

        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public List<Option> getOptions() {
            return options;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }

        private String generateUniqueName(final String name) {
            String uniqueName = name;
            int suffix = 1;
            Set<String> names = new HashSet<String>();
            for (Option option : options) {
                names.add(option.getName());
            }
            while (names.contains(uniqueName)) {
                uniqueName = name + ++suffix;
            }
            return uniqueName;
        }

        public int addOption() {
            String name = generateUniqueName("option");
            int index = options.size();
            this.addRow(new String[]{name, "value"});
            options.add(new Option(name, "value"));
            fireTableRowsInserted(index, index);
            return index;
        }

        public void removeOption(int index) {
            options.remove(index);
            this.removeRow(index);
            fireTableRowsDeleted(index, index);
        }

        @Override
        public Object getValueAt(int row, int column) {
            Object result = null;
            if (row >= 0) {
                Option option = options.get(row);
                switch (column) {
                    case 0:
                        result = option.getName();
                        break;
                    case 1:
                        result = option.getValue();
                        break;
                }
            }
            return result;
        }

        @Override
        public void setValueAt(Object aValue, int row, int column) {
            Option opt = null;
            String insertedValue = (String)aValue;
            String oldValue = (String) getValueAt(row, 1);
            // check if option name is reserved
            // if so, fall back to old value
            if (column == 0) {
                String oldKey = (String) getValueAt(row, 0);
                if(oldKey.equals(insertedValue)) return;
                for (String option : reservedOptions) {
                    if (insertedValue.trim().equals(option)) {
                        NotifyDescriptor descriptor =
                                new NotifyDescriptor.Message(NbBundle.getMessage(WsimportOptionsPanel.class, "ERR_RESERVED_OPTION", insertedValue));
                        DialogDisplayer.getDefault().notify(descriptor);
                        options.set(row, new Option(oldKey, oldValue));
                        fireTableCellUpdated(row, column);
                        return;
                    }
                }
                opt = new Option(insertedValue, (String)getValueAt(row, 1));
            }else if(column == 1){
                if(oldValue.equals(insertedValue)) return;
                opt = new Option((String)getValueAt(row, 0), insertedValue);
            }
            options.set(row, opt);
            fireTableCellUpdated(row, column);
        }
    }

    private class ListSelectionListenerImpl implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            optionsTable.editCellAt(optionsTable.getSelectedRow(), optionsTable.getSelectedColumn());
            Component editor = optionsTable.getEditorComponent();

            if (editor != null) {
                editor.requestFocus();
            }
            if (editor instanceof JTextComponent) {
                JTextComponent textComp = (JTextComponent) editor;
                textComp.selectAll();
            }
            updateButtons();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable optionsTable;
    private OptionsTableModel optionsTableModel;
    private javax.swing.JButton removeBtn;
    // End of variables declaration//GEN-END:variables
}
