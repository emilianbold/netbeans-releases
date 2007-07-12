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
package org.netbeans.modules.websvc.design.view.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.schema.model.Schema;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *ImportedSchemasPanel
 */
public class ImportedSchemasPanel extends javax.swing.JPanel {
    
    private Project project;
    private Schema[] importedSchemas;
    private DefaultListModel listModel;
    
    /*ImportedSchemasPanel */
    public ImportedSchemasPanel(Project project, Schema[] importedSchemas){
        
        this.project = project;
        this.importedSchemas =importedSchemas;
        initComponents();
        addBtn.addActionListener(new AddButtonActionListener());
        removeBtn.addActionListener(new RemoveButtonActionListener());
        listModel = new DefaultListModel();
        importedSchemasList.setModel(listModel);
        importedSchemasList.setCellRenderer(new ImportedSchemasPanelListCellRenderer());
        populateSchemas();
        removeBtn.setEnabled(false);
    }
    
    private void populateSchemas(){
        for(int i = 0; i < importedSchemas.length; i++){
            listModel.addElement(importedSchemas[i]);
        }
    }
    
    public DefaultListModel getListModel(){
        return listModel;
    }
    
    private int getSelectedRow() {
        ListSelectionModel lsm = (ListSelectionModel)importedSchemasList.getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            return -1;
        } else {
            return lsm.getMinSelectionIndex();
        }
    }
    
    class RemoveButtonActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            int selectedRow = getSelectedRow();
            if(selectedRow == -1) return;
            String className = (String)listModel.getElementAt(selectedRow);
            if(confirmDeletion(className)){
                listModel.removeElementAt(selectedRow);
            }
        }
        
        private boolean confirmDeletion(String schemaName) {
            NotifyDescriptor.Confirmation notifyDesc =
                    new NotifyDescriptor.Confirmation(NbBundle.getMessage
                    (ImportedSchemasPanel.class, "MSG_CONFIRM_DELETE", schemaName),
                    NbBundle.getMessage(ImportedSchemasPanel.class, "TTL_CONFIRM_DELETE"),
                    NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return (notifyDesc.getValue() == NotifyDescriptor.YES_OPTION);
        }
    }
    
    public Set<Schema> getSchemas(){
        Set<Schema> schemas = new HashSet<Schema>();
        for(int i = 0; i < listModel.getSize(); i++){
            Schema schema = (Schema)listModel.getElementAt(i);
            schemas.add(schema);
        }
        return schemas;
    }
    
    class AddButtonActionListener implements ActionListener{
        DialogDescriptor dlgDesc = null;
        public void actionPerformed(ActionEvent evt){
            ImportSchemaDialog importSchemaDialog = new ImportSchemaDialog(project);
            importSchemaDialog.show();
            if(importSchemaDialog.okButtonPressed()){
                Set<Schema> selectedSchemas = importSchemaDialog.getSelectedSchemas();
                for(Schema selectedSchema : selectedSchemas){
                    listModel.addElement(selectedSchema);
                }
            }
        }
    }
    
    class SchemaListSelectionListener implements ListSelectionListener{
        public void valueChanged(ListSelectionEvent e) {
            
        }
        
    }
    
    class ImportedSchemasPanelListCellRenderer extends JLabel implements ListCellRenderer{
        public ImportedSchemasPanelListCellRenderer(){
            setOpaque(true);
        }
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            if (value instanceof Schema) {
                Schema s = (Schema)value;
                setText(s.getTargetNamespace());
            } else {
                setText(value.toString());
            }
            setBackground(isSelected ? Color.black : Color.white);
            setForeground(isSelected ? Color.white : Color.black);
            return this;
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        importedSchemasList = new javax.swing.JList();
        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        schemasLabel = new javax.swing.JLabel();

        importedSchemasList.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        importedSchemasList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(importedSchemasList);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/panels/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addBtn, bundle.getString("Add_DotDotDot_label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, bundle.getString("Remove_label")); // NOI18N

        schemasLabel.setLabelFor(importedSchemasList);
        org.openide.awt.Mnemonics.setLocalizedText(schemasLabel, bundle.getString("LBL_IMPORTED_SCHEMAS")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(schemasLabel)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(addBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 112, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(removeBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {addBtn, removeBtn}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(schemasLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(addBtn)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(removeBtn))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JList importedSchemasList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton removeBtn;
    private javax.swing.JLabel schemasLabel;
    // End of variables declaration//GEN-END:variables
    
}
