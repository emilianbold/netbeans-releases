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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.midpnb.propertyeditors.table;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.propertyeditors.resource.elements.PropertyEditorResourceElement;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;

/**
 *
 * @author Anton Chechel
 */
public class TableModelEditorElement extends PropertyEditorResourceElement implements TableModelListener {

    private boolean doNotFireEvent;
    private long componentID;
    private CustomEditorTableModel tableModel;
    private TableCellRenderer renderer;

    public TableModelEditorElement() {
        tableModel = new CustomEditorTableModel();
        tableModel.addTableModelListener(this);
        initComponents();
        renderer = new HeaderCellRenderer();
        setTableCellRenderer();
    }

    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return SimpleTableModelCD.TYPEID;
    }

    public List<String> getPropertyValueNames() {
        return Arrays.asList(new String[] {SimpleTableModelCD.PROP_COLUMN_NAMES, SimpleTableModelCD.PROP_VALUES});
    }

    @Override
    public String getResourceNameSuggestion() {
        return "tableModel"; // NOI18N
    }

    public void setDesignComponentWrapper(final DesignComponentWrapper wrapper) {
        if (wrapper == null) {
            // UI stuff
            setTableValues(null, null);
            setAllEnabled(false);
            return;
        }

        this.componentID = wrapper.getComponentID();
        final PropertyValue[] columns = new PropertyValue[1];
        final PropertyValue[] values = new PropertyValue[1];

        final DesignComponent component = wrapper.getComponent();
        if (component != null) {
            // existing component
            if (!component.getType().equals(getTypeID())) {
                throw new IllegalArgumentException("Passed component must have typeID " + getTypeID() + " instead passed " + component.getType()); // NOI18N
            }

            this.componentID = component.getComponentID();
            component.getDocument().getTransactionManager().readAccess(new Runnable() {

                public void run() {
                    PropertyValue propertyValue = component.readProperty(SimpleTableModelCD.PROP_COLUMN_NAMES);
                    if (!isPropertyValueAUserCodeType(propertyValue)) {
                        columns[0] = propertyValue;
                    }
                    PropertyValue propertyValue2 = component.readProperty(SimpleTableModelCD.PROP_VALUES);
                    if (!isPropertyValueAUserCodeType(propertyValue2)) {
                        values[0] = propertyValue2;
                    }
                }
            });
        }

        if (wrapper.hasChanges()) {
            Map<String, PropertyValue> changes = wrapper.getChanges();
            for (String propertyName : changes.keySet()) {
                final PropertyValue propertyValue = changes.get(propertyName);
                if (SimpleTableModelCD.PROP_VALUES.equals(propertyName)) {
                    values[0] = propertyValue;
                } else if (SimpleTableModelCD.PROP_COLUMN_NAMES.equals(propertyName)) {
                    columns[0] = propertyValue;
                }
            }
        }

        // UI stuff
        setAllEnabled(true);
        setTableValues(columns[0], values[0]);
        checkRemoveButtons();
    }

    public synchronized void tableChanged(TableModelEvent e) {
        setTableCellRenderer();
        if (isShowing() && !doNotFireEvent) {
            Vector dataVector = tableModel.getDataVector();
            boolean useHeader = tableModel.hasHeader();
            
            if (useHeader && dataVector.size() > 0 && ((Vector) dataVector.get(0)).size() != tableModel.getHeader().size()) {
                // TODO debug only
                Debug.illegalState("Headers size must be qual to column count!"); // NOI18N
            }
            setTextFields(dataVector.size(), tableModel.getColumnCount());
            checkRemoveButtons();
            
            List<PropertyValue> propertyValueColumn = new ArrayList<PropertyValue>(dataVector.size());
            for (int i = 0; i < dataVector.size(); i++) {
                Vector row = (Vector) dataVector.elementAt(i);
                List<PropertyValue> propertyValueRow = new ArrayList<PropertyValue>(row.size());
                for (int j = 0; j < row.size(); j++) {
                    String str = (String) row.elementAt(j);
                    propertyValueRow.add(MidpTypes.createStringValue(str != null ? str : "")); // NOI18N
                }
                propertyValueColumn.add(PropertyValue.createArray(MidpTypes.TYPEID_JAVA_LANG_STRING, propertyValueRow));
            }

            PropertyValue values = PropertyValue.createArray(MidpTypes.TYPEID_JAVA_LANG_STRING.getArrayType(), propertyValueColumn);
            fireElementChanged(componentID, SimpleTableModelCD.PROP_VALUES, values);

            PropertyValue headers;
            if (useHeader) {
                List<PropertyValue> propertyValueHeader = new ArrayList<PropertyValue>(tableModel.getColumnCount());
                Vector<String> header = tableModel.getHeader();
                for (int j = 0; j < header.size(); j++) {
                    String str = header.elementAt(j);
                    propertyValueHeader.add(MidpTypes.createStringValue(str != null ? str : "")); // NOI18N
                }
                headers = PropertyValue.createArray(MidpTypes.TYPEID_JAVA_LANG_STRING, propertyValueHeader);
            } else {
                headers = PropertyValue.createNull();
            }
            fireElementChanged(componentID, SimpleTableModelCD.PROP_COLUMN_NAMES, headers);
        }
    }

    private void setTableCellRenderer() {
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setCellRenderer(renderer);
        }
    }

    private synchronized void setTableValues(PropertyValue columns, PropertyValue values) {
        doNotFireEvent = true;

        boolean useHeader = (columns != null) && (columns.getArray() != null);
        String[] header = null;
        String[][] arrays = null;
        
        if (useHeader) {
            List<PropertyValue> list = columns.getArray();
            header = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                header[i] = MidpTypes.getString(list.get(i));
            }
        }

        if (values == null) {
            tableModel.clear();
            setTextFields(0, 0);
        } else {
            List<PropertyValue> rows = values.getArray();
            if (rows != null && rows.size() > 0) {
                List<PropertyValue> cols = rows.get(0).getArray();
                int rowCount = rows.size();
                int columnCount = cols.size();
                arrays = new String[rowCount][columnCount];

                for (int x = 0; x < rowCount; x++) {
                    cols = rows.get(x).getArray();
                    for (int y = 0; y < columnCount; y++) {
                        arrays[x][y] = MidpTypes.getString(cols.get(y));
                    }
                }
                setTextFields(rowCount, columnCount);
            } else {
                setTextFields(0, 0);
            }
        }

        tableModel.setUseHeader(useHeader);
        headerCheckBox.setSelected(useHeader);
        tableModel.setDataVector(arrays, header);

        doNotFireEvent = false;
    }

    private void checkRemoveButtons() {
        removeRowButton.setEnabled(tableModel.getDataVector().size() > 0);
        removeColButton.setEnabled(tableModel.getColumnCount() > 0);
    }

    void setAllEnabled(boolean isEnabled) {
        addColButton.setEnabled(isEnabled);
        addRowButton.setEnabled(isEnabled);
        removeColButton.setEnabled(isEnabled);
        removeRowButton.setEnabled(isEnabled);
        tableLabel.setEnabled(isEnabled);
        table.setEnabled(isEnabled);
        headerCheckBox.setEnabled(isEnabled);
        rowsLabel.setEnabled(isEnabled);
        columnsLabel.setEnabled(isEnabled);
        rowsTextField.setEnabled(isEnabled);
        columnsTextField.setEnabled(isEnabled);
    }

    private void setTextFields(int row, int col) {
        rowsTextField.setText(row > -1 ? String.valueOf(row) : "-"); // NOI18N
        columnsTextField.setText(col > -1 ? String.valueOf(col) : "-"); // NOI18N
    }

    private class HeaderCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (tableModel.hasHeader() && row == 0) {
                renderer.setBackground(Color.LIGHT_GRAY);
            } else {
                renderer.setBackground(Color.WHITE);
            }
            renderer.setForeground(Color.BLACK);
            return renderer;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        tableLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addColButton = new javax.swing.JButton();
        removeColButton = new javax.swing.JButton();
        removeRowButton = new javax.swing.JButton();
        addRowButton = new javax.swing.JButton();
        headerCheckBox = new javax.swing.JCheckBox();
        rowsLabel = new javax.swing.JLabel();
        rowsTextField = new javax.swing.JTextField();
        columnsLabel = new javax.swing.JLabel();
        columnsTextField = new javax.swing.JTextField();

        jTextField1.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.jTextField1.text")); // NOI18N

        tableLabel.setLabelFor(table);
        org.openide.awt.Mnemonics.setLocalizedText(tableLabel, org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.tableLabel.text")); // NOI18N
        tableLabel.setEnabled(false);

        table.setModel(tableModel);
        table.setEnabled(false);
        table.setTableHeader(null);
        jScrollPane1.setViewportView(table);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(addColButton, org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.addColButton.text")); // NOI18N
        addColButton.setEnabled(false);
        addColButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addColButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeColButton, org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.removeColButton.text")); // NOI18N
        removeColButton.setEnabled(false);
        removeColButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeRowButton, org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.removeRowButton.text")); // NOI18N
        removeRowButton.setEnabled(false);
        removeRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addRowButton, org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.addRowButton.text")); // NOI18N
        addRowButton.setEnabled(false);
        addRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(headerCheckBox, org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.headerCheckBox.text")); // NOI18N
        headerCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        headerCheckBox.setEnabled(false);
        headerCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        headerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerCheckBoxActionPerformed(evt);
            }
        });

        rowsLabel.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.rowsLabel.text")); // NOI18N
        rowsLabel.setEnabled(false);

        rowsTextField.setEditable(false);
        rowsTextField.setEnabled(false);

        columnsLabel.setText(org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.columnsLabel.text")); // NOI18N
        columnsLabel.setEnabled(false);

        columnsTextField.setEditable(false);
        columnsTextField.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableLabel)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(headerCheckBox)
                        .add(36, 36, 36))
                    .add(layout.createSequentialGroup()
                        .add(columnsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(columnsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(addColButton)
                    .add(removeColButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, removeRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, addRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(rowsLabel)
                            .add(18, 18, 18)
                            .add(rowsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
        );

        layout.linkSize(new java.awt.Component[] {addColButton, removeColButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {columnsTextField, rowsTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tableLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(headerCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(rowsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(rowsLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addRowButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeRowButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(columnsTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(columnsLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addColButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeColButton)
                        .addContainerGap())
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addColButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColButtonActionPerformed
        tableModel.addColumn(""); // NOI18N
    }//GEN-LAST:event_addColButtonActionPerformed

    private void removeColButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeColButtonActionPerformed
        tableModel.removeLastColumn();
    }//GEN-LAST:event_removeColButtonActionPerformed

    private void removeRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRowButtonActionPerformed
        int rowCount = tableModel.getDataVector().size();
        if (rowCount > 0) {
            tableModel.removeRow(rowCount - 1);
        }
    }//GEN-LAST:event_removeRowButtonActionPerformed

    private void addRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowButtonActionPerformed
        int columnCount = table.getColumnCount();
        String[] row = new String[columnCount];
        for (int i = 0; i < row.length; i++) {
            row[i] = ""; // NOI18N
        }
        tableModel.addRow(row);
    }//GEN-LAST:event_addRowButtonActionPerformed

    private void headerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headerCheckBoxActionPerformed
        tableModel.setUseHeader(headerCheckBox.isSelected());
    }//GEN-LAST:event_headerCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addColButton;
    private javax.swing.JButton addRowButton;
    private javax.swing.JLabel columnsLabel;
    private javax.swing.JTextField columnsTextField;
    private javax.swing.JCheckBox headerCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton removeColButton;
    private javax.swing.JButton removeRowButton;
    private javax.swing.JLabel rowsLabel;
    private javax.swing.JTextField rowsTextField;
    private javax.swing.JTable table;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
}
