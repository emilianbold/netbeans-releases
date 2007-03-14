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
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.websvc.design.util.Util;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkuchtiak
 */
public class AddOperationFromSchemaPanel extends javax.swing.JPanel {
    private File wsdlFile;
    private List<URL> schemaFiles;
    private String parameterType, returnType, faultType;
    private Map<String, Import> map = new HashMap<String, Import>();
    private Map<String, Schema> map1 = new HashMap<String, Schema>();
    
    /** Creates new form NewJPanel */
    public AddOperationFromSchemaPanel(File wsdlFile) {
        this();
        this.wsdlFile=wsdlFile;
        //jTextField2.setText(NbBundle.getMessage(AddOperationFromSchemaPanel.class, "TXT_DefaultSchmas", wsdlFile.getName()));
        //jTextField2.setEditable(false);
        browseButton.setEnabled(false);
        try{
            populate();
        }catch(CatalogModelException e){
            ErrorManager.getDefault().notify(e);
        }
        schemaCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                schemaComboChanged(evt);
            }
        });
        SchemaPanelListCellRenderer renderer = new SchemaPanelListCellRenderer();
        parmCombo.setRenderer(renderer);
        returnCombo.setRenderer(renderer);
        faultCombo.setRenderer(renderer);
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
        
        public List<URL> getSchemaFiles() {
            return schemaFiles;
        }
        
        public GlobalElement[] getParameterTypes() {
            List<GlobalElement> list = new ArrayList<GlobalElement>();
            Object[] objs = parmCombo.getSelectedObjects();
            for(int i = 0; i < objs.length; i++){
                list.add((GlobalElement)objs[i]);
            }
            return list.<GlobalElement>toArray(new GlobalElement[list.size()]);
        }
        
        public GlobalElement getReturnType() {
            return (GlobalElement)returnCombo.getSelectedItem();
        }
        
        public GlobalElement getFaultType() {
            return (GlobalElement)faultCombo.getSelectedItem();
        }
        
        private void populate()throws CatalogModelException {
            WSDLModel model = Util.getWSDLModel(FileUtil.toFileObject(wsdlFile), true);
            Definitions definitions = model.getDefinitions();
            Types types = definitions.getTypes();
            Collection<Schema> schemas = types.getSchemas();
            for(Schema schema : schemas) {
                // populate with internal schema
                Collection<GlobalElement> elements = schema.getElements();
                if (elements.size()>0) {
                    schemaCombo.addItem(schema.getTargetNamespace());
                    map1.put(schema.getTargetNamespace(), schema);
                }
                // populate with imported schemas
                Collection<Import> importedSchemas = schema.getImports();
                for(Import importedSchema : importedSchemas){
                    String schemaLocation = importedSchema.getSchemaLocation();
                    map.put(schemaLocation, importedSchema);
                    schemaCombo.addItem(schemaLocation);
                }
            }
            
            String selectedItem = (String)schemaCombo.getItemAt(0);
            populateWithElements(selectedItem);
        }
        
        
        class SchemaPanelListCellRenderer extends JLabel implements ListCellRenderer{
            public Component getListCellRendererComponent(JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus){
                    String text = ((GlobalElement)value).getName();
                    setText(text);
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        opNameTxt = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        parmCombo = new javax.swing.JComboBox();
        returnCombo = new javax.swing.JComboBox();
        faultCombo = new javax.swing.JComboBox();
        schemaCombo = new javax.swing.JComboBox();

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_OperationName_mnem").charAt(0));
        jLabel1.setLabelFor(opNameTxt);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_OperationName")); // NOI18N

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_SchemaFiles_mnem").charAt(0));
        jLabel2.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_SchemaFiles")); // NOI18N

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_ParameterTypes_mnem").charAt(0));
        jLabel3.setLabelFor(parmCombo);
        jLabel3.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_ParameterTypes")); // NOI18N

        jLabel4.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_ReturnType_mnem").charAt(0));
        jLabel4.setLabelFor(returnCombo);
        jLabel4.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_ReturnType")); // NOI18N

        jLabel5.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_FaultType_mnem").charAt(0));
        jLabel5.setLabelFor(faultCombo);
        jLabel5.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_FaultType")); // NOI18N

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/design/view/actions/Bundle").getString("LBL_Browse_mnem").charAt(0));
        browseButton.setText(org.openide.util.NbBundle.getMessage(AddOperationFromSchemaPanel.class, "LBL_Browse")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, browseButton)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jLabel1)
                            .add(jLabel2)
                            .add(jLabel3)
                            .add(jLabel5))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, opNameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, parmCombo, 0, 469, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, faultCombo, 0, 469, Short.MAX_VALUE)
                            .add(returnCombo, 0, 469, Short.MAX_VALUE)
                            .add(schemaCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 459, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(opNameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(schemaCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton)
                .add(46, 46, 46)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(parmCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(returnCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(faultCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(68, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void schemaComboChanged(java.awt.event.ItemEvent evt) {
    // TODO add your handling code here:
    parmCombo.removeAllItems();
    returnCombo.removeAllItems();
    faultCombo.removeAllItems();
    String selectedItem = (String)schemaCombo.getSelectedItem();
    populateWithElements(selectedItem);
}


private void populateWithElements(String selectedItem) {
    Import importedSchema = map.get(selectedItem);
    if (importedSchema!=null) {
        String namespace = importedSchema.getNamespace();
        try {
            SchemaModel schemaModel = importedSchema.resolveReferencedModel();
            Collection<GlobalElement> elements = schemaModel.getSchema().getElements();
            for(GlobalElement element : elements){
                String elementName = element.getName();
                parmCombo.addItem(element);
                returnCombo.addItem(element);
                faultCombo.addItem(element);
            }
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
    } else {
        Schema schema = map1.get(selectedItem);
        if (schema!=null) {
            Collection<GlobalElement> elements = schema.getElements();
            for(GlobalElement element : elements){
                String elementName = element.getName();
                parmCombo.addItem(element);
                returnCombo.addItem(element);
                faultCombo.addItem(element);
            }
        }
    }
}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox faultCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JTextField opNameTxt;
    private javax.swing.JComboBox parmCombo;
    private javax.swing.JComboBox returnCombo;
    private javax.swing.JComboBox schemaCombo;
    // End of variables declaration//GEN-END:variables
    
    }
