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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.design.util.Util;
import org.netbeans.modules.websvc.design.view.panels.ImportedSchemasPanel;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkuchtiak
 */
public class AddOperationFromSchemaPanel extends javax.swing.JPanel {
    private File wsdlFile;
    private List<Schema> importedSchemas; //current imported Schemas
    private Set<Schema> newSchemas;  //new schemas to be retrieved
    private WSDLModel wsdlModel;
    private FaultsPanel faultsPanel;
    
    /** Creates new form NewJPanel */
    public AddOperationFromSchemaPanel(File wsdlFile) {
        this.wsdlFile=wsdlFile;
        initComponents();
        faultsPanel = new FaultsPanel(getWsdlModel());
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TTL_faultsPanel"), faultsPanel);
        opNameTxt.setText(NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TXT_DefaultOperationName"));
        schemaTypesTextField.setText(NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TXT_DefaultSchmas", wsdlFile.getName()));
        importSchemaBtn.setEnabled(true);
        importedSchemas = Utils.getImportedSchemas(getWSDLModel());
 
        try{
            populate();
        }catch(CatalogModelException e){
            ErrorManager.getDefault().notify(e);
        }
        newSchemas = new HashSet<Schema>();
        SchemaPanelListCellRenderer renderer = new SchemaPanelListCellRenderer();
        returnCombo.setRenderer(renderer);
        
    }
    /** Creates new form NewJPanel */
    public AddOperationFromSchemaPanel() {
        initComponents();
        opNameTxt.setText(NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TXT_DefaultOperationName"));
    }
    
    public File getWsdlFile() {
        return wsdlFile;
    }
    
    public String getOperationName(){
        return opNameTxt.getText();
    }
    
    public List<Schema> getImportedSchemas() {
        return importedSchemas;
    }
    
 
    public List<ParamModel> getParameterTypes() {
        return parametersPanel.getParameters();
    }
    
    public ReferenceableSchemaComponent getReturnType() {
        Object returnType = returnCombo.getSelectedItem();
        if (returnType instanceof ReferenceableSchemaComponent) {
            return (ReferenceableSchemaComponent) returnType;
        } else {
            return null;
        }
    }
 
  
    public List<ParamModel> getFaultTypes() {
        return faultsPanel.getFaults();
    }
    
    public Set<Schema> getNewSchemas(){
        return newSchemas;
    }
    private void populate()throws CatalogModelException {
        populateWithTypes(getWSDLModel());
    }
    
    private WSDLModel getWSDLModel(){
        if(wsdlModel == null){
            wsdlModel = Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
        }
        return wsdlModel;
    }
    
    
    class SchemaPanelListCellRenderer extends JLabel implements ListCellRenderer{
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            if (value instanceof ReferenceableSchemaComponent) {
                setText(Utils.getDisplayName((ReferenceableSchemaComponent)value));
            } else if (value instanceof String) {
                setText((String)value);
            }
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        wsdlLabel = new javax.swing.JLabel();
        returnLabel = new javax.swing.JLabel();
        opNameTxt = new javax.swing.JTextField();
        importSchemaBtn = new javax.swing.JButton();
        returnCombo = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        parametersPanel = new org.netbeans.modules.websvc.design.view.actions.ParametersPanel(getWsdlModel());
        jPanel1 = new javax.swing.JPanel();
        bindingStyleLabel = new javax.swing.JLabel();
        schemaTypesTextField = new javax.swing.JTextField();

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_OperationName_mnem").charAt(0));
        nameLabel.setLabelFor(opNameTxt);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_OperationName")); // NOI18N

        wsdlLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_SchemaFiles_mnem").charAt(0));
        wsdlLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_SchemaTypes")); // NOI18N

        returnLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_ReturnType_mnem").charAt(0));
        returnLabel.setLabelFor(returnCombo);
        returnLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_ReturnType")); // NOI18N

        importSchemaBtn.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_AddSchema_mnem").charAt(0));
        importSchemaBtn.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_AddSchema")); // NOI18N
        importSchemaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importSchemaBtnActionPerformed(evt);
            }
        });

        jLabel6.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_BindingStyle")); // NOI18N

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.parametersPanel.TabConstraints.tabTitle"), parametersPanel); // NOI18N

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 20, 10));

        bindingStyleLabel.setText("Document/Literal"); // NOI18N
        jPanel1.add(bindingStyleLabel);

        schemaTypesTextField.setEditable(false);
        schemaTypesTextField.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.wsdlTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(wsdlLabel)
                            .add(jLabel6)
                            .add(returnLabel))
                        .add(14, 14, 14)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(returnCombo, 0, 363, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 146, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 78, Short.MAX_VALUE)
                                .add(importSchemaBtn))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, opNameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
                            .add(schemaTypesTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(opNameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(wsdlLabel)
                    .add(schemaTypesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(importSchemaBtn)
                        .add(25, 25, 25))
                    .add(layout.createSequentialGroup()
                        .add(jLabel6)
                        .add(10, 10, 10))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(13, 13, 13)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 257, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(returnCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(returnLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void importSchemaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importSchemaBtnActionPerformed
    final Project project = getProject();
    final ImportedSchemasPanel panel = new ImportedSchemasPanel(project,
            (getImportedSchemas().toArray(new Schema[getImportedSchemas().size()])));
    String title = NbBundle.getMessage(AddOperationFromSchemaPanel.class,"TTL_AddImportedSchemasPanel");
    DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
    dialogDesc.setButtonListener(new ImportSchemaListener(panel) );
    Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
    dialog.setVisible(true);
}//GEN-LAST:event_importSchemaBtnActionPerformed



class ImportSchemaListener implements ActionListener{
    
    ImportedSchemasPanel panel;
    public ImportSchemaListener(ImportedSchemasPanel panel){
        this.panel = panel;
    }
    public void actionPerformed(ActionEvent evt) {
        newSchemas.clear();
        if(evt.getSource() == NotifyDescriptor.OK_OPTION) {
            Set<Schema> schemasFromPanel = panel.getSchemas();
            for(Schema schemaFromPanel : schemasFromPanel){
                boolean found = false;
                for(Schema importedSchema : importedSchemas){
                    if(importedSchema.fromSameModel(schemaFromPanel)){
                        found = true;
                    }
                }
                if(!found){
                    newSchemas.add(schemaFromPanel);
                }
            }
            if(newSchemas.size() > 0){
                try{
                    wsdlModel.startTransaction();
                    WSDLComponentFactory factory = wsdlModel.getFactory();
                    Definitions definitions = wsdlModel.getDefinitions();
                    Types types = definitions.getTypes();
                    for(Schema newSchema : newSchemas){
                        WSDLSchema wsdlSchema = factory.createWSDLSchema();
                        types.addExtensibilityElement(wsdlSchema);
                        SchemaModel schemaModel = wsdlSchema.getSchemaModel();
                        Schema schema = schemaModel.getSchema();
                        schema.setTargetNamespace(definitions.getTargetNamespace());
                        Import importedSchema = schemaModel.getFactory().createImport();
                        importedSchema.setNamespace(newSchema.getTargetNamespace());
                        ModelSource ms = newSchema.getModel().getModelSource();
                        FileObject fo = ms.getLookup().lookup(FileObject.class);
                        
                        importedSchema.setSchemaLocation(fo.getURL().toString());
                        schema.addExternalReference(importedSchema);
                    }
                    
                }catch(FileStateInvalidException e){
                    ErrorManager.getDefault().notify(e);
                    
                } finally{
                    wsdlModel.endTransaction();
                }
                parametersPanel.refreshSchemaTypes();
                populateWithTypes(wsdlModel);
            }
        }
    }
}


private Project getProject(){
    FileObject wsdlFO = FileUtil.toFileObject(this.getWsdlFile());
    return FileOwnerQuery.getOwner(wsdlFO);
}



private void populateWithTypes(WSDLModel wsdlModel) {
    returnCombo.removeAllItems();
    returnCombo.addItem("void"); //NOI18N
    try {
        List<ReferenceableSchemaComponent> schemaTypes = Utils.getSchemaTypes(wsdlModel);
        for (ReferenceableSchemaComponent schemaType:schemaTypes) {
            returnCombo.addItem(schemaType);
        }
    } catch (CatalogModelException ex) {
        ErrorManager.getDefault().notify(ex);
    }
    
}

private WSDLModel getWsdlModel() {
    return Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bindingStyleLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton importSchemaBtn;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField opNameTxt;
    private org.netbeans.modules.websvc.design.view.actions.ParametersPanel parametersPanel;
    private javax.swing.JComboBox returnCombo;
    private javax.swing.JLabel returnLabel;
    private javax.swing.JTextField schemaTypesTextField;
    private javax.swing.JLabel wsdlLabel;
    // End of variables declaration//GEN-END:variables
  
}
