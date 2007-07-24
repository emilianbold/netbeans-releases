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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.util.NbBundle;

/**
 *
 * @author  Milan Kuchtiak
 */
public final class ParametersPanel extends javax.swing.JPanel {
    
    private static final int COL_NAME_INDEX = 0;
    private static final int COL_TYPE_INDEX = 1;
    private static final String[] columnNames = {
        NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.LBL_Name"),
        NbBundle.getMessage(ParametersPanel.class, "ParametersPanel.LBL_Type"),
    };
    
    private final ParamsTableModel tableModel;
    
    private List<ReferenceableSchemaComponent> schemaTypes;
    private WSDLModel wsdlModel;
    
    public ParametersPanel() {
        initComponents();
        tableModel = new ParamsTableModel();
    }
    
    public ParametersPanel(WSDLModel wsdlModel) {
        this.wsdlModel=wsdlModel;
        initComponents();
        tableModel = new ParamsTableModel();
        table.setModel(tableModel);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                updateButtons();
            }
        });
        table.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                updateButtons();
            }
        });
        
        tableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent tableModelEvent) {
                updateButtons();
            }
        });
        
        
        try {
            schemaTypes = getSchemaTypes();
            if(schemaTypes == null) return;
            String[] refTypes = new String[schemaTypes.size()];
            int i=0;
            for (ReferenceableSchemaComponent schemaType:schemaTypes) {
                refTypes[i++]=Utils.getDisplayName(schemaType);
            }
            TableColumn col = table.getColumnModel().getColumn(COL_TYPE_INDEX);
            col.setPreferredWidth(300);
            col.setCellEditor(new MyComboBoxEditor(refTypes));
            col.setCellRenderer(new MyComboBoxRenderer(refTypes));
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void refreshSchemaTypes(){
        try {
            schemaTypes = getSchemaTypes();
            String[] refTypes = new String[schemaTypes.size()];
            int i=0;
            for (ReferenceableSchemaComponent schemaType:schemaTypes) {
                refTypes[i++]=Utils.getDisplayName(schemaType);
            }
            TableColumn col = table.getColumnModel().getColumn(COL_TYPE_INDEX);
            col.setPreferredWidth(300);
            col.setCellEditor(new MyComboBoxEditor(refTypes));
            col.setCellRenderer(new MyComboBoxRenderer(refTypes));
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
    }
    
    private List<ReferenceableSchemaComponent> getSchemaTypes() throws CatalogModelException {
        Definitions definitions = wsdlModel.getDefinitions();
        Types types = definitions.getTypes();
        if(types == null) return null;
        Collection<Schema> schemas = types.getSchemas();
        
        List<ReferenceableSchemaComponent> schemaTypes = new ArrayList<ReferenceableSchemaComponent>();
        // primitive types
        schemaTypes.addAll(Utils.getPrimitiveTypes());
        for(Schema schema : schemas) {            
            // populate with internal schema
            String schemaNamespace = schema.getTargetNamespace();
            if (schemaNamespace!=null) {
                populateWithElements(wsdlModel, schema.getModel(), schemaTypes);
            }
            // populate with imported schemas
            Collection<Import> importedSchemas = schema.getImports();
            for(Import importedSchema : importedSchemas){
                SchemaModel schemaModel = importedSchema.resolveReferencedModel();
                populateWithElements(wsdlModel, schemaModel, schemaTypes);
            }
        }
        return schemaTypes;
    }
    
    private void populateWithElements(WSDLModel wsdlModel, SchemaModel schemaModel, List<ReferenceableSchemaComponent> schemaTypes) {
        Collection<GlobalElement> elements = schemaModel.getSchema().getElements();
        for(GlobalElement element : elements) {
            if (!Utils.isUsedInOperation(wsdlModel, element)) {
                schemaTypes.add(element);
            }
        }
        Collection<? extends GlobalType> complexTypes = schemaModel.getSchema().getComplexTypes();
        for(GlobalType type : complexTypes){
            schemaTypes.add(type);
        }
        Collection<? extends GlobalType> simpleTypes = schemaModel.getSchema().getSimpleTypes();
        for(GlobalType type : simpleTypes){
            schemaTypes.add(type);
        }
        
    }
    
    public List<ParamModel> getParameters() {
        return tableModel.getParameters();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "LBL_ADD")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "LBL_REMOVE")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "LBL_UP")); // NOI18N
        upButton.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(ParametersPanel.class, "LBL_DOWN")); // NOI18N
        downButton.setEnabled(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addButton)
                    .add(removeButton)
                    .add(upButton)
                    .add(downButton))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addButton, downButton, removeButton, upButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeButton)
                        .add(22, 22, 22)
                        .add(upButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(downButton))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
    int selectedRow = table.getSelectedRow();
    if (selectedRow > -1) {
        tableModel.removeParameter(selectedRow);
    }
    if (selectedRow == table.getRowCount()) {
        selectedRow--;
    }
    table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
    updateButtons();
}//GEN-LAST:event_removeButtonActionPerformed

private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
    int index = tableModel.addParameter();
    table.getSelectionModel().setSelectionInterval(index, index);
    updateButtons();
}//GEN-LAST:event_addButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton downButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeButton;
    private javax.swing.JTable table;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
    
    private void updateButtons() {
        int selectedRowsCount = table.getSelectedRowCount();
        removeButton.setEnabled(selectedRowsCount != 0);
        upButton.setEnabled(selectedRowsCount == 1);
        downButton.setEnabled(selectedRowsCount == 1);
    }
    
    // accessible for test
    private class ParamsTableModel extends AbstractTableModel {
        
        private final List<ParamModel> parameters;
        
        public ParamsTableModel() {
            parameters = new ArrayList<ParamModel>();
        }
        
        public List<ParamModel> getParameters() {
            return parameters;
        }
        
        public int addParameter() {
            String name = generateUniqueName("arg"); //NOI18N
            ParamModel parameter = new ParamModel(name);
            int index = parameters.size();
            parameters.add(parameter);
            if (schemaTypes.size()>0) {
                ReferenceableSchemaComponent ref = schemaTypes.get(0);
                String value = ref.getName();
                //TODO: need a better way to detect primitive types
                if(ref instanceof GlobalType){
                    value = Utils.getDisplayName(ref);
                }
                setValueAt(value, index, 1);
            }
            fireTableRowsInserted(index, index);
            return index;
        }
        
        public void removeParameter(int index) {
            parameters.remove(index);
            fireTableRowsDeleted(index, index);
        }
        
        public int getRowCount() {
            return parameters.size();
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public Object getValueAt(int row, int column) {
            Object result = null;
            ParamModel parameter = parameters.get(row);
            if (parameter != null) {
                switch (column) {
                case COL_NAME_INDEX: result = parameter.getParamName(); break;
                case COL_TYPE_INDEX: result = parameter.getDisplayName(); break;
                }
            }
            return result;
        }
        
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        public boolean isCellEditable(int row, int column) {
            return true;
        }
        
        public void setValueAt(Object aValue, int row, int column) {
            ParamModel parameter = parameters.get(row);
            ParamModel changedParameter = new ParamModel();
            if (column==COL_NAME_INDEX) {
                changedParameter.setParamName((String) aValue);
                changedParameter.setParamType(parameter.getParamType());
            } else if (column==COL_TYPE_INDEX) {
                for (ReferenceableSchemaComponent schemaType: schemaTypes) {
                    if (aValue!=null && Utils.getDisplayName(schemaType).equals(aValue)) {
                        changedParameter.setParamType(schemaType);
                        break;
                    }
                }
                changedParameter.setParamName(parameter.getParamName());
            }
            parameters.set(row, changedParameter);
            fireTableCellUpdated(row, column);
        }
        
        // JTable uses this method to determine the default renderer/editor for each cell.
        // If we didn't implement this method, then the last column would contain
        // text ("true"/"false"), rather than a check box.
        
        //        public Class getColumnClass(int c) {
        //            //if ()
        //            return getValueAt(0, c).getClass();
        //        }
        
        private String generateUniqueName(String name) {
            List<Integer> numberSuffixes = new ArrayList<Integer>();
            for (ParamModel param : parameters) {
                if (!name.equals(param.getParamName()) && param.getParamName().startsWith(name)) {
                    String suffix = param.getParamName().substring(name.length());
                    if (isNumber(suffix)) {
                        numberSuffixes.add(Integer.parseInt(suffix));
                    }
                }
            }
            Collections.sort(numberSuffixes);
            String result = name;
            if (numberSuffixes.size() > 0) {
                int newSuffix = numberSuffixes.get(numberSuffixes.size() - 1) + 1;
                result = name + newSuffix;
            } else if (parameters.size() > 0) {
                result = name + 1;
            }
            return result;
        }
        
        private boolean isNumber(String value) {
            for (char character : value.toCharArray()) {
                if (!Character.isDigit(character)) {
                    return false;
                }
            }
            return true;//!value.trim().equals("");
        }
        
    }
    
    private class MyComboBoxRenderer extends JComboBox implements TableCellRenderer {
        public MyComboBoxRenderer(String[] items) {
            super(items);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            
            // Select the current value
            setSelectedItem(value);
            return this;
        }
        
    }
    
    private class MyComboBoxEditor extends DefaultCellEditor {
        public MyComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }
}
