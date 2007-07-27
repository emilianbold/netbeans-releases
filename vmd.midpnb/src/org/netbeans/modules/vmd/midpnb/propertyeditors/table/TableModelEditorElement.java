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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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

    public TableModelEditorElement() {
        tableModel = new CustomEditorTableModel();
        tableModel.addTableModelListener(this);
        initComponents();
    }

    public JComponent getJComponent() {
        return this;
    }

    public TypeID getTypeID() {
        return SimpleTableModelCD.TYPEID;
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
                    columns[0] = component.readProperty(SimpleTableModelCD.PROP_COLUMN_NAMES);
                    values[0] = component.readProperty(SimpleTableModelCD.PROP_VALUES);
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
    }

    public synchronized void tableChanged(TableModelEvent e) {
        if (isShowing() && !doNotFireEvent) {
            Vector dataVector = tableModel.getDataVector();
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
            // TODO save headers
        }
    }

    private synchronized void setTableValues(PropertyValue columns, PropertyValue values) {
        doNotFireEvent = true;
        if (values == null) {
            tableModel.clear();
        } else {
            String[] header = null;
            if (columns != null) {
                List<PropertyValue> list = columns.getArray();
                if (list != null) {
                    header = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        header[i] = MidpTypes.getString(list.get(i));
                    }
                }
            }

            List<PropertyValue> list = values.getArray();
            String[][] arrays = null;
            if (list != null && list.size() > 0) {
                List<PropertyValue> list2 = list.get(0).getArray();
                arrays = new String[list.size()][list2.size()];
                for (int x = 0; x < list.size(); x++) {
                    list2 = list.get(x).getArray();
                    for (int y = 0; y < list2.size(); y++) {
                        arrays[x][y] = MidpTypes.getString(list2.get(y));
                    }
                }
            }

            tableModel.setDataVector(arrays, header);
        }
        doNotFireEvent = false;
    }

    private void setAllEnabled(boolean isEnabled) {
        addColButton.setEnabled(isEnabled);
        addRowButton.setEnabled(isEnabled);
        removeColButton.setEnabled(isEnabled);
        removeRowButton.setEnabled(isEnabled);
        tableLabel.setEnabled(isEnabled);
        table.setEnabled(isEnabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addColButton = new javax.swing.JButton();
        removeColButton = new javax.swing.JButton();
        removeRowButton = new javax.swing.JButton();
        addRowButton = new javax.swing.JButton();

        tableLabel.setLabelFor(table);
        org.openide.awt.Mnemonics.setLocalizedText(tableLabel, org.openide.util.NbBundle.getMessage(TableModelEditorElement.class, "TableModelEditorElement.tableLabel.text")); // NOI18N
        tableLabel.setEnabled(false);

        table.setModel(tableModel);
        table.setEnabled(false);
        jScrollPane1.setViewportView(table);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tableLabel)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(removeColButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(addColButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(removeRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(addRowButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        layout.linkSize(new java.awt.Component[] {addColButton, addRowButton, removeColButton, removeRowButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tableLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addColButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeColButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(removeRowButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(addRowButton)
                        .addContainerGap())
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addColButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColButtonActionPerformed
        tableModel.addColumn(null);
    }//GEN-LAST:event_addColButtonActionPerformed

    private void removeColButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeColButtonActionPerformed
        tableModel.removeLastColumn();
    }//GEN-LAST:event_removeColButtonActionPerformed

    private void removeRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRowButtonActionPerformed
        int rowCount = table.getRowCount();
        if (rowCount > 0) {
            tableModel.removeRow(rowCount - 1);
        }
    }//GEN-LAST:event_removeRowButtonActionPerformed

    private void addRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowButtonActionPerformed
        tableModel.addRow(new String[table.getColumnCount()]);
    }//GEN-LAST:event_addRowButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addColButton;
    private javax.swing.JButton addRowButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeColButton;
    private javax.swing.JButton removeRowButton;
    private javax.swing.JTable table;
    private javax.swing.JLabel tableLabel;
    // End of variables declaration//GEN-END:variables
}
