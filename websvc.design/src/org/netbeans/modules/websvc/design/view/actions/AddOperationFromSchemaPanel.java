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
    private boolean useJava;
    
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
        useJava = false;
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
    
    public List<JavaParamModel> getJavaParameterTypes(){
        return javaParametersPanel.getParameters();
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
    
    public boolean isUseJava(){
        return useJava;
    }
    
    public String getJavaReturnType(){
        return (String)returnCombo.getSelectedItem();
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
        jRadioButton3 = new javax.swing.JRadioButton();
        useButtonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        schemaTypesLabel = new javax.swing.JLabel();
        returnLabel = new javax.swing.JLabel();
        opNameTxt = new javax.swing.JTextField();
        importSchemaBtn = new javax.swing.JButton();
        returnCombo = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        parametersPanel = new org.netbeans.modules.websvc.design.view.actions.ParametersPanel(getWsdlModel());
        jPanel1 = new javax.swing.JPanel();
        docLiteralRB = new javax.swing.JRadioButton();
        rpcLiteralRB = new javax.swing.JRadioButton();
        schemaTypesTextField = new javax.swing.JTextField();
        useXSDRB = new javax.swing.JRadioButton();
        useJavaRB = new javax.swing.JRadioButton();

        jRadioButton3.setText("jRadioButton3");
        jRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        useButtonGroup.add(useXSDRB);
        useButtonGroup.add(useJavaRB);

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_OperationName_mnem").charAt(0));
        nameLabel.setLabelFor(opNameTxt);
        nameLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_OperationName")); // NOI18N

        schemaTypesLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_SchemaFiles_mnem").charAt(0));
        schemaTypesLabel.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_SchemaTypes")); // NOI18N

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

        buttonGroup1.add(docLiteralRB);
        docLiteralRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("RB_DOCUMENT_LITERAL_mnem").charAt(0));
        docLiteralRB.setSelected(true);
        docLiteralRB.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "RB_DOCUMENT_LITERAL")); // NOI18N
        docLiteralRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        docLiteralRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(docLiteralRB);

        buttonGroup1.add(rpcLiteralRB);
        rpcLiteralRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("RB_RPC_LITERAL_mnem").charAt(0));
        rpcLiteralRB.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "RB_RPC_LITERAL")); // NOI18N
        rpcLiteralRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rpcLiteralRB.setEnabled(false);
        rpcLiteralRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jPanel1.add(rpcLiteralRB);

        schemaTypesTextField.setEditable(false);
        schemaTypesTextField.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.wsdlTextField.text")); // NOI18N

        useXSDRB.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_UseXSD")); // NOI18N
        useXSDRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useXSDRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useXSDRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useXSDHandler(evt);
            }
        });
        useXSDRB.setSelected(true);

        useJavaRB.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_UseJava")); // NOI18N
        useJavaRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useJavaRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useJavaRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useJavaHandler(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameLabel)
                            .add(importSchemaBtn)
                            .add(useXSDRB)
                            .add(returnLabel)
                            .add(jLabel6)
                            .add(schemaTypesLabel))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(schemaTypesTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, useJavaRB)
                                    .add(opNameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(returnCombo, 0, 314, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(17, 17, 17)
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameLabel)
                    .add(opNameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(useJavaRB)
                    .add(useXSDRB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(schemaTypesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(schemaTypesLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(importSchemaBtn)
                        .add(17, 17, 17)
                        .add(jLabel6)
                        .add(18, 18, 18))
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 257, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(returnCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(returnLabel))
                .addContainerGap(26, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void useJavaHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useJavaHandler
    showJavaOnly(true);
    this.invalidate();
    this.validate();
    if(javaParametersPanel == null){
        javaParametersPanel = new JavaParametersPanel();
    }
    jTabbedPane1.remove(0);
    jTabbedPane1.insertTab(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.parametersPanel.TabConstraints.tabTitle"), null,javaParametersPanel, "", 0); // NOI18N
    jTabbedPane1.setSelectedIndex(0);
    javaParametersPanel.refreshJavaPrimitiveTypes();
    useJava = true;
    populateWithJavaTypes();
}//GEN-LAST:event_useJavaHandler

private void useXSDHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useXSDHandler
    showJavaOnly(false);
    this.invalidate();
    this.validate();
    jTabbedPane1.remove(0);
    jTabbedPane1.insertTab(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "AddOperationFromSchemaPanel.parametersPanel.TabConstraints.tabTitle"), null,parametersPanel, "", 0); // NOI18N
    jTabbedPane1.setSelectedIndex(0);
    parametersPanel.refreshSchemaTypes();
    useJava = false;
    populateWithTypes(getWsdlModel());
}//GEN-LAST:event_useXSDHandler

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

private void showJavaOnly(boolean showJava){
    schemaTypesLabel.setVisible(!showJava);
    schemaTypesTextField.setVisible(!showJava);
    importSchemaBtn.setVisible(!showJava);
    
}


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


private void populateWithJavaTypes(){
    returnCombo.removeAllItems();
    String[] javaTypes = JavaParametersPanel.javaPrimitiveTypes;
    for(int i = 0; i < javaTypes.length; ++i){
        returnCombo.addItem(javaTypes[i]);
    }
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton docLiteralRB;
    private javax.swing.JButton importSchemaBtn;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField opNameTxt;
    private org.netbeans.modules.websvc.design.view.actions.ParametersPanel parametersPanel;
    private javax.swing.JComboBox returnCombo;
    private javax.swing.JLabel returnLabel;
    private javax.swing.JRadioButton rpcLiteralRB;
    private javax.swing.JLabel schemaTypesLabel;
    private javax.swing.JTextField schemaTypesTextField;
    private javax.swing.ButtonGroup useButtonGroup;
    private javax.swing.JRadioButton useJavaRB;
    private javax.swing.JRadioButton useXSDRB;
    // End of variables declaration//GEN-END:variables
    private JavaParametersPanel javaParametersPanel;
}
